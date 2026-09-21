# Lokaler KnowWE-Testserver aus dem aktuellen Stand (Anleitung für Agenten)

> Ziel: eine KnowWE-Instanz aus dem aktuellen Stand der Repos in einem lokalen Tomcat laufen lassen, um
> Verhalten über HTTP und im Browser zu prüfen -- etwa das CI-Dashboard, Actions, Streams oder das Rendering
> einer Seite. Die Anleitung ist plattformneutral gehalten; Shell-Beispiele sind POSIX, funktionieren aber
> sinngemäß auch in PowerShell. Platzhalter: `<instanz>` ist das Verzeichnis der Testinstanz,
> `<task-repo>` das Maven-Repository für diese Aufgabe.
>
> Aufwand, wenn alles vorbereitet ist: Reaktor bauen ~1 min, WAR packen ~1 min, Tomcat-Start bis zur ersten
> Seite unter einer Minute.
>
> **Dieses Dokument wird von allen Agenten und Sessions weitergepflegt**, die den Testserver nutzen: gelöste
> Probleme in die Tabelle unter „Typische Startprobleme" aufnehmen, überholte Angaben korrigieren, neue Rezepte
> ergänzen und den Stand oben aktualisieren. Was nur für eine Maschine gilt (Pfade, Skripte, vorhandene
> Instanzen), gehört ins Memory des Agenten, nicht hierher.

## Voraussetzungen

- **Java 25** für Build und Laufzeit (das WAR ist für Class-File-Version 69 gebaut).
- **Apache Tomcat 9** (Servlet 4, `javax.servlet`). Eine passende Distribution liegt z. B. unter
  `KnowWE-Distribution` bzw. lässt sich als Zip von tomcat.apache.org beziehen.
- **Ein dediziertes Maven-Repository** für die Aufgabe. Nie in das geteilte `~/.m2/repository` installieren.
  Die Third-Party-Abhängigkeiten kommen aus dem vorhandenen lokalen Bestand, etwa per
  `rsync -a --ignore-existing` aus `~/.m2/repository` ohne die Gruppen `com/denkbares`, `de/d3web`,
  `de/uniwue` und `org/apache/jspwiki`. Maven läuft immer offline (`-o`); fehlt ein Third-Party-Artefakt, ist
  Maven Central ohne Zugangsdaten erreichbar.
- **Alle denkbares-Artefakte werden lokal gebaut**, nichts wird von `repo.denkbares.com` nachgeladen. Dafür
  müssen die Checkouts der beteiligten Repos im Workspace liegen: denkbares-Maven-Plugins, denkbares-Commons,
  d3web, d3web-DES, denkbares-Internals, d3web-Player-Platform, jspwiki, KnowWE, KnowWE-DES. Die Reihenfolge
  ermittelt das Workspace-Skript (Abschnitt 1) selbst.

## 1. Bauen

Den ganzen Weg vom leeren Task-Repo bis zum WAR übernimmt das Workspace-Skript aus `Maven-Parents`
(`Maven-Parents/denkbares-Parent/build-script/README.md`). Es kennt alle POMs des Workspace, berechnet die
repo-übergreifende Reihenfolge inklusive des Zyklus KnowWE ↔ KnowWE-DES und baut mit `--target` nur das, was
das Zielmodul transitiv braucht -- Parents, Dependencies, Plugin-Dependencies und per `dependency:unpack`
eingebundene Artefakte.

```bash
export JAVA_HOME=<jdk-25> MAVEN_OPTS=-Xmx2g
cd <workspace>            # das Verzeichnis mit den Geschwister-Repos (KnowWE, d3web, ...)

# Reihenfolge und Umfang ansehen ...
python3 Maven-Parents/denkbares-Parent/build-script/build-main-workspace-parallel.py analyze --root . --target KnowWE-App
# ... und bauen
python3 Maven-Parents/denkbares-Parent/build-script/build-main-workspace-parallel.py build --root . --target KnowWE-App \
  --goals install --maven-arg=-o --maven-arg=-Dmaven.repo.local=<task-repo> --reactor-jobs 4 -T 1C
ls -la KnowWE-App/target/KnowWE.war
```

- `--goals install` statt des Defaults `clean install`: `clean` entfernt `target/dependencies/output.txt`,
  das für IDE-Testläufe gebraucht wird.
- Gemessen am 2026-09-21 mit warmen `target`-Verzeichnissen: aus einem Task-Repo ohne ein einziges
  denkbares-Artefakt bis zum fertigen WAR gut zwei Minuten (121 von 229 Modulen in 9 Repos, 16 Bootstrap-
  und 39 Reactor-Schritte). Kalt, also mit kompletter Neukompilierung, entsprechend länger.
- Die Profile `core-plugins` und `denkbares` von `KnowWE-App` sind `activeByDefault` und werden vom Skript
  berücksichtigt; ihre Plugins aus KnowWE-DES und d3web-DES landen automatisch in der Hülle.
- Schlägt ein Schritt fehl, nennt das Skript Modul, Log und einen Resume-Befehl (`--resume-reactor-step`).
- Nach Cross-Repo-API-Änderungen vorher `touch` auf die betroffenen `*.java`, sonst kompiliert Maven
  unveränderte Module nicht neu.

### Nur KnowWE neu bauen

Sind die vorgelagerten Repos schon im Task-Repo, reicht für Änderungen in KnowWE der Reaktor selbst:

```bash
cd KnowWE
mvn -o -Dmaven.repo.local=<task-repo> -T 1C -DskipTests -Dmaven.javadoc.skip=true --fail-at-end install
```

`KnowWE-App` baut dabei mit, weil seine Default-Profile die DES-Plugins aus dem Task-Repo ziehen. Für ein
einzelnes Plugin: Modul bauen, Jar in `WEB-INF/lib` tauschen, Tomcat neu starten (siehe Abschnitt 3).

### WAR ohne DES-Plugins

Nur `core-plugins` reicht für Core-Funktionen (CI-Dashboard, Suche, Markups des Cores); Vanilla-Seiten mit
DES-Markups rendern dann als Text. Profile immer explizit nennen, denn sobald irgendein Profil per `-P`
aktiviert wird, sind die `activeByDefault`-Profile aus:

```bash
rm -rf KnowWE-App/target/KnowWE-App-*-SNAPSHOT        # exploded WAR früherer Builds nicht mit einpacken
mvn -o -Dmaven.repo.local=<task-repo> -pl KnowWE-App -Pcore-plugins -DskipTests package
```

## 2. Instanz vorbereiten

Ein Verzeichnis `<instanz>` mit:

| Inhalt | Zweck |
|---|---|
| `tomcat/` | entpackter Tomcat; Webapp landet unter `tomcat/webapps/KnowWE` |
| `wiki/` | Seitenordner: eine `<Seitenname>.txt` pro Seite (Leerzeichen als `+`). Als Ausgangsbasis eignet sich eine Kopie des Vanilla-Wikis. Neue Seiten einfach als Datei ablegen, sie werden beim Start kompiliert |
| `work/` | JSPWiki-Arbeitsverzeichnis (Suchindex, Caches) |
| `jspwiki-custom.properties` | Wiki-Konfiguration, Vorlage ist `WEB-INF/classes/jspwiki-custom.properties.default` aus dem WAR |
| `userdatabase.xml`, `groupdatabase.xml` | Benutzer und Gruppen für `XMLUserDatabase`/`XMLGroupDatabase` |

Die Konfiguration muss mindestens setzen:

```properties
var.basedir = <instanz>/wiki
jspwiki.fileSystemProvider.pageDir = $basedir
jspwiki.pageProvider = VersioningFileProvider
jspwiki.workDir = <instanz>/work
jspwiki.baseURL = http://localhost:8080/KnowWE/
jspwiki.applicationName = KnowWE
```

Grundsatz: **jede Einstellung, die eine Klasse nennt, muss zu den deployten Plugins passen** -- etwa
`jspwiki.searchProvider` (Master: `de.knowwe.jspwiki.ExtensibleLuceneSearchProvider`) oder Tool-Provider in
`settings.toolmenu.json`. Eine Konfiguration, die für einen Feature-Branch oder ein anderes Profil entstanden
ist, deshalb vor dem Start gegen den gebauten Stand durchsehen.

Die Vanilla-Policy (`WEB-INF/jspwiki.policy`) erlaubt anonymen Nutzern das Editieren, `GET`-Actions laufen
ohne CSRF-Token. Das macht Tests per `curl` einfach, ist aber nur für lokale Instanzen vertretbar.

## 3. Deployen, Starten, Prüfen, Stoppen

```bash
# deployen: alte Webapp ersetzen, Konfiguration einsetzen
rm -rf <instanz>/tomcat/webapps/KnowWE && mkdir <instanz>/tomcat/webapps/KnowWE
( cd <instanz>/tomcat/webapps/KnowWE && unzip -q <pfad>/KnowWE-App/target/KnowWE.war )
cp <instanz>/jspwiki-custom.properties <instanz>/tomcat/webapps/KnowWE/WEB-INF/classes/

# starten
export JAVA_HOME=<jdk-25> CATALINA_OPTS="-Xmx2g -Duser.language=en -Duser.country=US"
<instanz>/tomcat/bin/catalina.sh start          # Windows: catalina.bat start

# warten, bis die erste Seite mit 200 antwortet
until [ "$(curl -s -o /dev/null -m 5 -w '%{http_code}' 'http://localhost:8080/KnowWE/Wiki.jsp?page=Main')" = 200 ]; do sleep 2; done
```

- **Erfolg prüfen:** `Wiki.jsp?page=Main` liefert 200, und `catalina.out` enthält ab der letzten Zeile
  `Deploying web application directory [...KnowWE]` weder `Error in plugin` noch `startup failed`. Ein 404
  bei laufendem Prozess heißt, dass der Kontext nicht gestartet ist -- dann dort `Caused by` lesen.
- **Stoppen:** `catalina.sh stop` reicht mit KnowWE oft nicht, der Prozess bleibt hängen. Ihn dann hart
  beenden ist der abgesprochene Weg. Das Muster für den Kill so wählen, dass es nur die JVM trifft
  (`org.apache.catalina.startup.Bootstrap start`) und nicht die eigene Shell, in deren Befehlszeile dieselben
  Wörter stehen können. Deploy- und Startbefehle deshalb getrennt absetzen.
- **Ressourcen:** Tomcat und Maven-Build nicht gleichzeitig laufen lassen, beide brauchen je 2-3 GB Heap.
- **Änderungen nachziehen:** KnowWE entpackt Plugin-Ressourcen (JS, CSS) bei jedem Start neu aus den Jars, und
  die `?version=` an den Skript-URLs stammt aus dem Jar-Zeitstempel. Verlässlich ist daher: Modul bauen, Jar
  in `WEB-INF/lib` tauschen, Tomcat neu starten. Direkt in die Webapp kopierte Dateien sind nach dem nächsten
  Start weg und werden vom Browser gecacht.
- **Fremde Skripte prüfen:** Liegen im Instanzverzeichnis Deploy-Skripte aus einer anderen Aufgabe, vor dem
  Aufruf lesen, was sie kopieren. Ein Skript, das Ressourcen eines Feature-Branch-Worktrees über die Webapp
  legt, erzeugt einen Mischstand.

## 4. Rezepte

### Eigene Testseite

Eine Datei `<instanz>/wiki/<Seitenname>.txt` mit dem zu prüfenden Markup anlegen und Tomcat starten. Für
das CI-Dashboard reicht:

```
%%CIDashboard
  @name: SleepCI
  @trigger: onDemand
  @test: Sleep .*
  @test: ArticleHasErrors .*
%
```

### Build auslösen und beobachten, ohne Browser

```bash
curl 'http://localhost:8080/KnowWE/KnowWE.jsp?action=CIAction&task=executeNewBuild&name=SleepCI&page=<Seite>'
```

### Langsamer CI-Build

Für Fortschrittsanzeigen braucht man einen Build, der einige Sekunden läuft; die Core-Tests sind auf dem
Vanilla-Wiki in unter einer Sekunde durch. Bewährt ist ein temporärer Test, der pro Artikel schläft:

```java
public class SleepTest extends AbstractTest<Article> {
	@Override
	public Message execute(Article article, String[] args, String[]... ignores) throws InterruptedException {
		Thread.sleep(Long.getLong("ci.sleep.millis", 300L));
		TestingUtils.checkInterrupt();
		return Message.SUCCESS;
	}
	@Override public Class<Article> getTestObjectClass() { return Article.class; }
	@Override public String getDescription() { return "Sleeps per article (local debugging aid)."; }
}
```

Registriert wird er in der `plugin.xml` von `KnowWE-Plugin-CI4KE` als Extension am Punkt
`d3web-Plugin-TestingFramework/Test` mit `name=Sleep` (Vorlage: der Eintrag für `ArticleHasErrors`). Die
Tests laufen mit `0,75 * CPUs` Threads parallel; 125 Artikel bei 1000 ms und 12 Threads ergeben rund 10 s.
Die Dauer über `-Dci.sleep.millis=...` in `CATALINA_OPTS` steuern. **Nur lokal einbauen und vor dem Commit
zurücknehmen.**

### Server-Sent Events beobachten

Zeitstempel pro Zeile zeigen, ob Events gestreamt oder erst am Ende in einem Schwung geliefert werden:

```bash
curl -sN -H 'Accept: text/event-stream' 'http://localhost:8080/KnowWE/action/CIGetProgressAction?names=%5B%22SleepCI%22%5D' \
  | while IFS= read -r l; do printf '%s %s\n' "$(date +%T.%3N)" "$l"; done
```

Im Browser lässt sich der Empfang protokollieren, ohne Code zu ändern: `window.EventSource` vor dem Start
des Builds durch eine Hülle ersetzen, die jedes Event mit `performance.now()` in ein Array schreibt.

**Streamende Actions immer über `action/<ActionName>` ansprechen**, nicht über `KnowWE.jsp?action=...`:
Anfragen an `*.jsp` laufen durch den JSPWiki-`WikiJSPFilter`, der die komplette Antwort puffert und erst nach
Ende der Action ausliefert. Der Servlet-Pfad (`JSPActionServlet`, Mapping `/action/*`) streamt sofort.

## 5. Typische Startprobleme

| Symptom in `catalina.out` | Ursache | Abhilfe |
|---|---|---|
| `Error in plugin '...'` mit `NoSuchMethodError` | Plugin-Jar gegen einen anderen Core gebaut, z. B. ein Branch-Stand aus dem kopierten Task-Repo | Plugin aus dem passenden Checkout neu ins Task-Repo bauen |
| `Unable to load and setup properties ... searchProvider` bzw. `ClassNotFoundException` für eine konfigurierte Klasse | Konfiguration nennt eine Klasse aus einem nicht deployten Plugin oder Branch | Eintrag in der deployten `jspwiki-custom.properties` auf den gebauten Stand setzen |
| `Unable to find blocked tool provider` | `settings.toolmenu.json` (Seitenordner oder `KnowWEExtension/toolmenu/` in der Webapp) sperrt einen Tool-Provider, dessen Plugin fehlt; häufig ein Rest aus einem exploded WAR mit anderem Profil | Datei entfernen bzw. exploded WAR vor dem Packen löschen |
| `The POM for com.denkbares:...:jar:<version> is missing` oder `Symbol nicht gefunden` für Cross-Repo-Klassen | Artefakt eines anderen Repos fehlt im Task-Repo oder ist veraltet | Workspace-Skript mit `--target` laufen lassen (Abschnitt 1); es baut die fehlenden Module in der richtigen Reihenfolge |
| Bootstrap scheitert mit `denkbares-Maven-EnforcerRules ... is missing` | älterer Stand des Workspace-Skripts ohne die Phase *Bootstrap prerequisites* | Skript aktualisieren oder einmalig `denkbares-Maven-Plugins` ins Task-Repo installieren |

## 6. Aufräumen

- Temporäre Quelländerungen (Sleep-Test, `plugin.xml`) zurücknehmen; `git status` muss sauber sein.
- Eingriffe in die deployte Webapp (Konfiguration, getauschte Jars) sind nach dem nächsten Deploy weg. Was
  dauerhaft gelten soll, gehört in `<instanz>/jspwiki-custom.properties` bzw. in den Quellcode.
