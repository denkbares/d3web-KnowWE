# Modernisierung der öffentlichen KnowWE-Distribution (Free-2-Use)

Stand: 2026-10-16. Konzept und Entscheidungsvorlage, noch nichts umgesetzt.
Quellen: Repo `knowwe-distribution` (git.denkbares.com `des/build/knowwe-distribution`), `KnowWE-App/pom.xml`,
`knowwe-des-infra`, jspwiki-Fork, Artifactory-Storage-API, Download und Start von Nightly (LATEST, 2026.8-SNAPSHOT)
und 2026.4.1 am 16.10.2026.
Legende: **[V]** = verifiziert (Code, Datei, HTTP oder Startversuch), **[?]** = Annahme, muss bestätigt werden.

---

## 0. Kurzfassung

**Empfehlung:** KnowWE wird öffentlich als **drei Artefakte aus einem Maven-Build** verteilt, alle mit demselben
Inhalt:

1. **`KnowWE.war`**, Free-2-Use-Variante, konfiguriert über ein Datenverzeichnis außerhalb des WARs (`KNOWWE_HOME`)
   bzw. über Umgebungsvariablen.
2. **Docker-Image** `knowwe:<version>` auf einer öffentlichen Registry. Basis ist das offizielle Tomcat-9-Image mit
   JRE 25, das Wiki liegt auf einem Volume, die Defaults sind sicher.
3. **Schlankes Zip** „KnowWE-Standalone“ mit WAR, minimalem Tomcat (aus Maven Central, nicht eingecheckt) und
   Startskripten. Das ersetzt das heutige Zip-im-Zip.

Die **App** (Doppelklick, ohne installiertes Java) folgt später auf Basis des Zips: eingebetteter Tomcat-Launcher
plus `jpackage`. Electron braucht es dafür nicht.

**d3web fällt aus der Distribution heraus** (Albrechts Vorschlag, 16.10.). `d3web/lib.zip`, `sources.zip` und
`doc.zip` braucht kein Endnutzer: Die Jars liegen in repo-public, die Quellen auf GitHub. Dasselbe gilt für
`KnowWE/sources.zip` und `KnowWE/doc.zip`; letzteres ist ohnehin seit Längerem kaputt (siehe 1.3). Für die
LGPL-Pflichten reicht ein Verweis auf die Quellen zum Release, dafür braucht es aber **Release-Tags** auf GitHub
(heute gibt es keine, siehe 1.6).

**Sofort, unabhängig vom Konzept:** Die heutige Distribution liefert eine `userdatabase.xml` mit 5 Admin-Konten aus.
Darunter sind `admin` und 4 Konten von Mitarbeitenden, alle mit Passwort-Hashes und E-Mail-Adressen. Das auf
d3web.de veröffentlichte Passwort passt laut Prüfung der Schwester-Session nicht auf `admin`. Die Hashes sind aber
offline angreifbar, und die Datei stammt vermutlich aus der d3web.de-Benutzerdatenbank. Dazu kommt eine Policy, die
Anonymen Editieren, Umbenennen und Löschen erlaubt (Abschnitt 1.5). Wer eine Installation übers Netz erreichen
kann, kann sie also anonym verändern. Die Datenbanken sollten aus dem Nightly verschwinden, und die alten Zips mit
`Install.jsp` sollten vom Netz.

**Entscheidungen, die du treffen musst:** am Ende in Abschnitt 8.

---

## 1. Ist-Zustand (belegt)

### 1.1 Wo das Zip gebaut wird

- **Repo `knowwe-distribution`** (git.denkbares.com `des/build/knowwe-distribution`, 56 MB, Branches `master`,
  `release-11` … `release-12.7`) [V]. Kern ist das Ant-Skript `d3web-KnowWE/build_script.xml` (511 Zeilen, Autoren
  Joachim Baumeister und du, ant-contrib 1.0b3). Daneben liegen in `d3web-KnowWE/external/` eingecheckt:
  - `Tomcat/`: ein komplettes **Tomcat 9.0.76** (Commit „Update tomcat to 9.0.76“, 19.06.2023) mit `docs`,
    `examples`, `manager`, `host-manager` und dem Skript `startWithLocalRep.sh` [V].
  - `Notes/`: `Release-Notes.md` (von Hand bzw. KI-gestützt gepflegt, siehe `Release-Notes-UPDATE-README.md`, mit
    `/home/albrecht/...`-Pfaden), die beiden Quickstart-PDFs (zuletzt geändert 18.07.2016), dazu Git-Log-Exporte
    seit 2021 [V].
  - `Licenses/`: `Licenses.pdf` und Lizenztexte als RTF/RTFD (macOS-Bundles, deshalb viele Dateien `TXT.rtf`),
    zuletzt geändert 15.01.2020 [V].
  - `Quality Reports/`: PDFs aus der Watney-Zeit (nur für die Commercial-Distribution).
- **Drei Ziele:** `BUILD_OPEN_SOURCE_DIST`, `BUILD_COMMERCIAL_DIST` (Default) und `BUILD_FREE_2_USE_DIST`. Öffentlich
  ist Free-2-Use [V]. Ablauf von `BUILD_FREE_2_USE_DIST`:
  1. `mvn clean install -Dmaven.test.skip=true` in `Maven-Parents/KnowWE-Parent`, also ein Clean-Install des
     **ganzen lokalen Workspaces** (`${basedir}/../..`), dessen Checkouts das Skript als gegeben voraussetzt.
  2. `d3web-DistributionBuilder` mit `-Pdist` → `d3web/lib.zip`, `sources.zip`, `doc.zip`.
  3. `KnowWE-App` mit `-Pdist,local,core-plugins,denkbares-free2use,des-repos`. Das Profil `local` gibt es in
     keinem POM mehr (Maven warnt nur). `des-repos` hängt das interne `repo-des` an.
  4. Tomcat aus `external/Tomcat` kopieren, `KnowWE-App-<version>.war` nach `webapps/KnowWE` entpacken,
     `jspwiki-custom.properties.default` umbenennen und `var.basedir = /folder/wikicontent` per Textersatz auf
     **`../wikicontent`** setzen.
  5. **`${WORKSPACE}/WikiVanilla`** (eigenes Repo, liegt nicht im Workspace) als `wikicontent` danebenlegen, die
     Benutzer- und Gruppendatenbank eingeschlossen.
  6. Alles zu `KnowWE-Tomcat.zip` zippen, Notes und Lizenzen dazu, außen herum noch ein Zip.
- **Commercial-Ziel:** verschlüsselt die Quellen per `zip -P`. Das Passwort steht **im Klartext** im Skript und
  landet auf der Kommandozeile (Wert hier bewusst nicht wiedergegeben). ZipCrypto ist zudem schwach. Für die
  öffentliche Distribution spielt das keine Rolle, bei einer Ablösung sollte es aber mit weg [V].
- **Version:** `help:evaluate` auf `KnowWE-App`, seit 14.11.2025 [V].

### 1.2 Veröffentlichung

- Ziel ist das Artifactory-Repo **`downloads`**, Pfad `distribution/d3web-KnowWE/<jahr>/` bzw. `nightly/`. Alle
  Dateien hat das Konto **`service.des.ci`** hochgeladen, ohne Build-Properties. Ein Artifactory-Build-Info gibt
  es also nicht, vermutlich lädt ein Jenkins-Job per HTTP-PUT hoch [V für Konto und fehlende Properties,
  [?] für den Job].
- **Nightly:** jede Nacht um 03:08 UTC (Zeitstempel im Zip 05:07/05:08 Lokalzeit). Er überschreibt
  `d3web-KnowWE-distribution-<X>-SNAPSHOT.zip` **und** `d3web-KnowWE-distribution-LATEST.zip`. Die
  SNAPSHOT-Zips früherer Linien bleiben liegen: 13 Stück ab 2025.1-SNAPSHOT, je rund 160 MB, zusammen ~2,1 GB.
  `LATEST` gibt es seit 28.05.2026 [V].
- **Releases:** Im Ordner `2026/` liegen nur 2026.3.1 (hochgeladen 28.05.2026) und 2026.4.1 (29.05.2026), in
  `2025/` nur 2025.4. Beide 2026er-Zips sind am selben Tag entstanden wie der Commit „Replace hopelessly outdated
  release notes“, also nachträglich. Für die Maven-Releases 2025.5, 2026.1, 2026.2, 2026.3, 2026.6 und 2026.7
  gibt es **keine** Zips. Ein Release-Zip entsteht offenbar nur, wenn jemand den Job von Hand mit einer
  Release-Version anwirft [V für den Bestand, [?] für den Ablauf].
- **Prüfsummen:** Artifactory berechnet SHA-256 (`/artifactory/api/storage/...`), die Downloads-Seite zeigt sie aber
  nicht an. Eine Signatur gibt es nicht [V].
- **Der Jenkins-Job selbst** liegt in keinem Repo (kein Jenkinsfile in `knowwe-distribution`). Seine Konfiguration
  (Parameter, Checkout der Workspace-Repos, Upload) kenne ich nicht → Frage 8.1.

### 1.3 Aufbau des Zips (Nightly vom 16.10.2026, 164 MB)

| Pfad | Inhalt | Befund |
|---|---|---|
| `KnowWE/KnowWE-Tomcat.zip` (158 MB) | Tomcat 9.0.76 + `webapps/KnowWE` (164 MB entpackt, 303 Jars, davon 151 MB `WEB-INF/lib`) + `wikicontent/` | Zip im Zip. Tomcat-Beispiel- und Manager-Apps sind dabei |
| `KnowWE/doc.zip` | **7,7 KB**: nur `javadoc.sh`, `options`, `packages` | Javadoc kaputt (`failOnError=false` verschluckt den Fehler), im 2026.4.1-Zip genauso (47 KB, 3 Dateien) [V] |
| `KnowWE/sources.zip` | 54 `*-sources.jar` (Open-Source-Teil) | ersetzbar durch GitHub |
| `KnowWE/KnowWE Quickstart.pdf` | von 2016 | veraltet |
| `d3web/{lib,sources,doc}.zip`, `d3web Quickstart.pdf` | d3web-Bibliotheken | für Endnutzer ohne Nutzen |
| `Licenses/` | statische PDF/RTF-Sammlung (Stand 2020) | nicht aus den tatsächlichen 303 Jars abgeleitet |
| `Release-Notes.md` | statisch aus `knowwe-distribution` | endet bei 2026.5, im 2026.4.1-Zip byte-gleich [V] |

### 1.4 Laufzeitbefunde (Test vom 16.10.2026)

- **„Leeres Wiki“ ist ein Pfadfehler, kein fehlender Inhalt** [V]. Das Zip enthält sehr wohl einen Wikiinhalt:
  `wikicontent/` mit 121 Seiten (Main, LeftMenu, rund 70 Doc-Seiten, Demos), 75 Anhängen und 630 alten Versionen
  unter `OLD/`. `var.basedir = ../wikicontent` ist aber **relativ zum Arbeitsverzeichnis des Prozesses**. Nur
  `startWithLocalRep.sh` (macht `cd bin`) bzw. ein Start aus `bin/` trifft `tc/wikicontent`. Wer
  `bin/startup.sh` aus dem Tomcat-Ordner startet (oder unter Windows aus einem anderen Ordner), bekommt ein neues,
  leeres Wiki im Ordner **neben** dem Tomcat. Gegenprobe heute: Start aus `bin/` liest `tc/wikicontent`, Main und
  LeftMenu sind da, Start in 12,6 s. Der frühere Test (Start aus `tc/`) las `…/KnowWE/wikicontent`.
  Der KnowWE-Quickstart beschreibt laut Schwester-Session genau den falschen Start aus dem Tomcat-Ordner.
- **Keine Ausführungsrechte:** `bin/*.sh` und `startWithLocalRep.sh` liegen im Zip als `rw-r--r--` (Ants
  `<zip>` übernimmt keine Unix-Modi). Unter Linux und macOS startet Tomcat erst nach `chmod +x` [V].
- `startWithLocalRep.sh` gibt am Ende `kill -9 <pid>` für **jeden** Java-Prozess der Maschine aus [V].
- **Wikiinhalt** kommt aus dem Repo `WikiVanilla` und enthält auch persönliche Seiten (`Albrecht Striffler`,
  `Joachim Baumeister`), Test-Seiten (`UITest-*`, `ST-BMI-*`, `Test123`, `Test Protocol - DiaFlux Editor`), eine
  `LeftMenuOld` und die komplette Versionsgeschichte seit 2005 [V].
- **Versionen:** Nightly hat KnowWE 2026.8-SNAPSHOT, Commons für Java 25 (class 69) und jspwiki-knowwe 2.12.10-SNAPSHOT.
  2026.4.1 läuft auf Java 17 mit jspwiki **2.12.6-SNAPSHOT**. Ursache: `release-2026.4` (und noch `release-2026.6`
  mit 2.12.8-SNAPSHOT) hatten eine SNAPSHOT-`jspwiki.version`; erst seit `release-2026.7` (2.12.9) setzt
  `prepare-release-branches.py` die jspwiki-Version mit [V]. Hinzu kommt der Clean-Install des lokalen Workspaces:
  Welche SNAPSHOTs eingebaut werden, hängt vom Zustand des Build-Rechners ab. Reproduzierbar ist das nicht.
- **Footer „JSPWiki v2.12.3-git-01“:** fest in `jspwiki-api/.../Release.java` (`MINORREVISION = 3`, `BUILD = "01"`).
  Der Release-Prozess pflegt diese Konstanten nicht [V].
- **Java:** muss der Nutzer selbst passend installieren. Nightly braucht 25, Releases bis 2026.4 brauchen 17,
  2026.5 braucht 21. Tomcat-Ports 8080/8005/8009 sind fest eingestellt [V].

### 1.5 Sicherheit

| Befund | Wirkung | Beleg |
|---|---|---|
| `wikicontent/userdatabase.xml` mit 5 Konten (`admin` + 4 Mitarbeitende, angelegt 2010–2016, `{SSHA}`-Hashes, 4 E-Mail-Adressen @denkbares.com), `groupdatabase.xml` mit Gruppe `Admin` (alle 5 plus `joba`, `Administrator`) | Jede Installation hat dieselben 5 Admin-Logins, deren Passwörter der Nutzer nicht kennt. Das auf d3web.de veröffentlichte Default-Passwort (User FAQ, How To AdminRights) passt laut Schwester-Session **nicht** auf `admin`. SSHA ist gesalzenes SHA-1 und offline schnell angreifbar. Wer ein Passwort knackt, ist auf jeder Installation Admin, und eventuell auch auf d3web.de: `AlbrechtStriffler` hat denselben Erstellungszeitstempel wie in der d3web.de-Datenbank. Personenbezogene Daten gehen an jeden Downloader (DSGVO) | [V], Nightly und 2026.4.1 identisch |
| `jspwiki.policy`: Rolle `Anonymous` hat `modify,rename,delete` und `createPages` auf `*:*` | Anonyme können alles ändern und löschen | [V] |
| 2026.4.1 und älter enthalten `Install.jsp` | Anonym ein Admin-Konto anlegen, solange keines existiert (jspwiki a5c07c6f0). In Kombination mit der mitgelieferten userdatabase greift das zwar nicht, wer die DB aber löscht, öffnet die Lücke | [V] laut Befund vom 16.10. |
| Tomcat 9.0.76 (06/2023) mit `examples`, `docs`, `manager`, `host-manager` | über drei Jahre Tomcat-Sicherheitsupdates fehlen. Die Beispiel-Apps sind laut Tomcat-Security-Guide zu entfernen. KnowWE selbst baut gegen `tomcat.version` 9.0.104 | [V] |
| Internes Image `knowwe-des-infra/d3web`: Artifactory-Zugang als `--build-arg` in der Download-URL im `RUN` | Build-Args, die ein `RUN` verwendet, stehen in der Image-History (`docker history`). Intern tolerierbar, für ein öffentliches Image ein Leck | [V] Dockerfile, Verhalten laut Docker-Doku |

### 1.6 Lizenzen und Quellen

- **Free-2-Use ist nicht Open Source** [V]. Das Profil `denkbares-free2use` packt Closed-Source-Binaries aus
  KnowWE-DES dazu (Admin-Utils, AutoComplete, EditMode, ImageMap, PlantUML, Todos) sowie
  `denkbares-SemanticCore-Plugin-GraphDB` mit **`graphdb-runtime` 10.8.12** (Ontotext GraphDB Free). Die Liste
  `closed-source-artifacts` führt fälschlich auch `KnowWE-Plugin-SparqlEndpoint` und `KnowWE-Plugin-RightPanel`;
  beide liegen im offenen KnowWE-Repo, ihre Quellen fehlen deshalb in `sources.zip`.
- **GraphDB Free** (`Licenses/License Texts/GraphDB-Free.txt`): Die Lizenz gilt zwischen Ontotext und dem
  *Licensee*. Artikel 3 verbietet u. a. Sublizenzierung, SaaS-Betrieb und gewinnorientierte Sites. Ob denkbares
  die Runtime öffentlich weiterverteilen darf, insbesondere in einem öffentlichen Docker-Image, lässt sich aus dem
  Text nicht sicher ablesen [?] → Frage 8.6. Technisch ist GraphDB optional: `KnowWE-Plugin-Rdf2GoSemanticCore`
  hängt nur im Test-Scope davon ab. Ob Demo- und Doc-Seiten GraphDB-Rulesets (z. B. OWL-Horst) brauchen, ist zu
  prüfen.
- **Lizenzliste** ist von Hand gepflegt (Stand 2020) und deckt die 303 Jars nicht nachweislich ab. PlantUML liegt
  als `plantuml-7995` bei, eine sehr alte Version [V].
- **Quellen:** GitHub `denkbares/d3web-KnowWE` und `Commons` sind Mirrors (LGPL-3.0), es gibt **keine Release-Tags**
  (KnowWE hat 2 Tags, keines für ein Release; Releases sind nur als Branches `release-YYYY.N` sichtbar) [V]. Wer
  `sources.zip` streicht, muss die Quellen pro Release anders eindeutig benennen.

### 1.7 Interne Docker-Images (`knowwe-des-infra`)

- `base`: `FROM docker.denkbares.com/des-infra/docker/tomcat/des2025:latest` (interne Registry), dazu graphviz,
  Fonts, Volume `/knowwe`, `jspwiki-custom.properties` mit `var.basedir = /knowwe`, Entrypoint mit
  `ADMIN_GROUP`/`USER_GROUP`/`READONLY_GROUP`/`EXTRA_POLICY` [V].
- `d3web`: lädt `KnowWE-d3web.war` aus `artifacts-des` (mit Zugangsdaten) nach `webapps/ROOT`. Der Entrypoint ergänzt
  seit **3aab156** (15.10.2026) die `web.xml` des WARs um Container-Auth, statt sie durch eine Kopie von JSPWiki 2.5
  zu ersetzen, und hält `web.xml.orig`/`jspwiki.policy.orig` für idempotente Neustarts [V].
- Gebaut wird per GitLab-CI (`docker-build`-Runner), immer als `:latest`, ohne Versions-Tag, Deploy von Hand.
  `jspwiki_templateDir haddock` im d3web-Image ist seit 2.11 überholt (`default`) [V].

**Was sich teilen lässt:** das Muster „WAR unverändert lassen, Konfiguration beim Start ableiten“ (`.orig`-Kopien,
Prüfungen, `exec`), die Gruppen- und Policy-Templates als *optionaler* Modus für Container-Auth, die Paketliste
(graphviz, Fonts). **Getrennt bleiben muss:** die Basis (die interne Registry ist öffentlich nicht erreichbar), der
WAR-Download mit Zugangsdaten, CAS/SSO und alles Kundenspezifische. Das öffentliche Image bekommt das WAR im
Build-Kontext, ohne Credentials.

---

## 2. Zielbild

```
                Maven-Reaktor KnowWE (Release-Branch bzw. master)
                               │  mvn -Pfree2use deploy
          ┌────────────────────┼─────────────────────────┐
          ▼                    ▼                         ▼
   KnowWE-free2use.war   KnowWE-Standalone-<v>.zip   Docker-Image knowwe:<v>
   (repo-public +        (WAR + Tomcat 9 aus Central  (tomcat:9-jre25 + WAR,
    downloads)            + Startskripte + Startwiki)  Volume /knowwe, multi-arch)
          │                    │                         │
          └──────── SHA-256SUMS (+ Signatur), generierte Release-Notes und Lizenzliste ─┘
                               │
                    www.d3web.de/Downloads: eine Zeile pro Release
                    später: App (Launcher + jpackage) auf Basis des Zips
```

Leitlinien:

- **Ein Inhalt, mehrere Verpackungen.** Dasselbe WAR, dasselbe Startwiki und dieselbe Erst-Einrichtung in allen
  Varianten, damit nur ein Pfad getestet werden muss.
- **WAR wird nie verändert.** Konfiguration liegt im Datenverzeichnis `KNOWWE_HOME` bzw. in Umgebungsvariablen.
  Updates heißen dann: WAR bzw. Image austauschen, Daten bleiben.
- **Sicher ab Werk.** Anonyme lesen nur, kein vorbelegtes Konto, Admin wird beim ersten Start eingerichtet.
- **Reproduzierbar aus Maven/CI.** Kein Ant, kein Clean-Install eines Workspaces, keine eingecheckten
  Fremd-Binaries, keine SNAPSHOTs in Releases.

---

## 3. Gemeinsame Bausteine (für alle Varianten)

### 3.1 Datenverzeichnis und Konfiguration

Der JSPWiki-Fork bringt die nötigen Mechanismen schon mit (`PropertyReader`) [V]:

- Umgebungsvariablen `jspwiki_*` werden zu `jspwiki.*` (z. B. `jspwiki_baseURL`). Achtung: Sie liegen in der
  Rangfolge **unter** der `jspwiki-custom.properties`. Was die Datei setzt, kann die Umgebung nicht überschreiben.
  `var.basedir` lässt sich so nicht setzen, weil nur Schlüssel mit Präfix `jspwiki` übernommen werden.
- `-Djspwiki.custom.config=<datei>` (Systemproperty oder Context-Parameter) lädt die Konfiguration aus einer Datei
  außerhalb des WARs. Systemproperties `-Djspwiki.*` haben den höchsten Rang.
- `${NAME}` in Werten wird aus Systemproperty oder Umgebung aufgelöst, **vor** der `$basedir`-Ersetzung.
  `var.basedir = ${KNOWWE_HOME}/wiki` funktioniert also.

Vorschlag:

```
KNOWWE_HOME/                      (Docker: /knowwe, Zip: ./data, Default ~/.knowwe für die App)
  jspwiki-custom.properties       beim ersten Start aus Vorlage erzeugt, danach Eigentum des Nutzers
  wiki/                           Seiten + Anhänge (VersioningFileProvider)
  work/                           Suchindex, Caches, SemanticCore-Repos
  users/userdatabase.xml, groupdatabase.xml
  logs/
```

- Das WAR enthält **keine** `jspwiki-custom.properties`. Das deployable WAR schließt sie heute schon aus
  (`packagingExcludes`); das Ant-Skript hat bisher die `.default`-Datei umbenannt. Stattdessen liefert es eine
  Vorlage mit `${KNOWWE_HOME}`-Pfaden und eine kleine Init-Logik mit (`ServletContextListener` oder in
  `KnowWEPlugin.initialize`): Fehlt `KNOWWE_HOME`, gibt es eine klare Fehlermeldung; fehlt die Datei, wird sie
  aus der Vorlage erzeugt.
- **Code-Anpassung nötig:** `KnowWEPlugin.initialize` liest für `copycorepages` die
  `WEB-INF/classes/jspwiki-custom.properties` direkt und nimmt den Rohwert von `pageDir`, also wörtlich `$basedir`.
  Mit externer Konfiguration greift das nie [V]. Die Logik muss die aufgelösten Engine-Properties verwenden.
- Ports, Kontextpfad (`/KnowWE` oder `/`) und `baseURL` werden über Variablen gesetzt, nicht über eine editierte
  `server.xml`.

### 3.2 Startwiki (Erstinhalt)

Heute gibt es **drei** Quellen, keine ist gepflegt:

| Quelle | Umfang | Stand |
|---|---|---|
| Repo `WikiVanilla` (vom Ant-Skript verwendet, nicht im Workspace) | 121 Seiten + `OLD/` + User-DB | mit persönlichen und Test-Seiten |
| `KnowWE-Resources/src/misc/resources/core-pages` | 58 Seiten, davon 49 auch in WikiVanilla | wird nur bei `knowweplugin.jspwikiconnector.copycorepages=true` kopiert (Default `false`) und ist dann wegen des `$basedir`-Fehlers wirkungslos |
| `KnowWE-Headless-App/src/main/resources/Wiki Vanilla` (KnowWE-DES) | 155 Dateien | dritte Kopie |
| `KnowWE-Plugin-d3web-Homepage/src/main/resources/wiki` | Abzug von d3web.de | Testmaterial für die Homepage, laut dir **kein** Startwiki |

Vorschlag: **eine** kuratierte Quelle im KnowWE-Repo (am naheliegendsten `core-pages`, umbenannt in `starter-wiki`),
ohne `OLD/`-Historie, ohne User-/Gruppen-DB, ohne persönliche und Test-Seiten. Inhalt: Main (Einstieg + Link auf
d3web.de), LeftMenu, die Doc-Seiten, die Car-Diagnosis- und Simpsons-Demos, eine Seite „Erste Schritte als Admin“.
Kopiert wird nur in ein **leeres** `wiki/`-Verzeichnis, danach nie wieder. Headless-App und Tests zeigen auf
dieselbe Quelle. Die Doc-Seiten auf d3web.de und im Startwiki könnten aus derselben Quelle kommen; das ist ein
späterer Schritt.

### 3.3 Sichere Defaults und Admin-Einrichtung

- **Policy im WAR:** `Anonymous` darf lesen, `Authenticated` bearbeiten, `Admin` alles. Die jetzige lose Vorlage
  bleibt als auskommentiertes Beispiel. Wer will, schaltet „Anonyme dürfen editieren“ über eine Variable frei
  (`KNOWWE_ANONYMOUS_EDIT=true`), nicht durch Editieren der Policy im WAR.
- **Erster Start ohne Benutzer-DB:** Konto `admin` anlegen. Das Passwort kommt
  1. aus `KNOWWE_ADMIN_PASSWORD` oder `KNOWWE_ADMIN_PASSWORD_FILE` (Docker-Secret), sonst
  2. wird es zufällig erzeugt, **einmal** ins Log geschrieben und in `KNOWWE_HOME/initial-admin-password`
     (Modus 0600) abgelegt, wie bei Jenkins. Nach der ersten Passwortänderung wird die Datei gelöscht.

  So gibt es kein veröffentlichtes Passwort und keine Install-Seite. Umsetzung als kleiner Startup-Hook in KnowWE,
  damit WAR, Zip, Image und App gleich funktionieren.
- **Optional Container-Auth** (wie intern): `ADMIN_GROUP`/`USER_GROUP` ergänzen die `web.xml` nach dem Muster
  von 3aab156. Für Free-2-Use reicht das JSPWiki-Login; der Modus wäre ein Angebot für Firmen-Setups.
- `jspwiki.login.throttling=true` bleibt, das Passwort-Hashing wird geprüft (SSHA → stärker, falls der Fork das
  hergibt [?]).

### 3.4 Build und Release

- **Maven statt Ant.** Ein neues Modul im KnowWE-Reaktor, z. B. `KnowWE-Distribution` (oder Profile an
  `KnowWE-App`):
  - `maven-war-plugin` mit Profil `free2use` → Artefakt `KnowWE-App:<v>:war:free2use` (Classifier) bzw. eigenes
    Modul `KnowWE-App-Free2Use`.
  - `maven-assembly-plugin` → Standalone-Zip. Tomcat kommt als Dependency `org.apache.tomcat:tomcat:<tomcat.version>:zip`
    aus Maven Central, nur `bin`, `lib`, `conf`; `webapps/*` bleibt draußen. Version = `tomcat.version` aus dem
    KnowWE-Parent. Das eingecheckte Tomcat 9.0.76 entfällt.
  - Docker-Image per **Jib** (`jib-maven-plugin`) im selben Maven-Lauf, Details in 4.2. Alternative ist
    `docker buildx` als eigener CI-Schritt.
- **Problem Closed-Source-Plugins:** Free-2-Use braucht KnowWE-DES-Artefakte. Das offene KnowWE-Repo kann sie nicht
  bauen; das Profil `denkbares` ist heute aus genau diesem Grund für externe Builds kaputt (Content-Audit). Das
  Distributionsmodul gehört deshalb **in ein DES-Repo** (KnowWE-DES, `KnowWE-Apps-DES`, dort liegt auch die
  Headless-App) oder in ein neues kleines Repo, das nur aus Maven-Artefakten zusammensetzt. Kein Workspace-Build.
  → Frage 8.3.
- **Keine SNAPSHOTs in Releases:** `maven-enforcer` mit `requireReleaseDeps` im Release-Profil des Distributions-
  moduls (`onlyWhenRelease=true`). Das hätte 2026.4.1 und 2026.6 gestoppt. Das Release-Skript setzt die
  jspwiki-Version seit 2026.7 mit; `Release.java` (Footer) sollte dabei mitgezogen bzw. aus der Maven-Version
  generiert werden.
- **Ein Zip pro Maven-Release:** Der Release-Build eines Branches `release-YYYY.N` erzeugt WAR, Zip und Image mit
  derselben Version und lädt sie hoch (`downloads/distribution/d3web-KnowWE/<jahr>/`, Registry-Tag `<v>`,
  zusätzlich `latest` und `YYYY`).
- **Nightly vom master:** nur noch `nightly/KnowWE-Standalone-LATEST.zip` und Image-Tag `nightly`. Die alten
  SNAPSHOT-Zips (~2,1 GB) werden gelöscht oder per Artifactory-Retention auf 1 begrenzt.
- **Release-Notes generiert:** Quelle sind die Commits der Repos im Free-2-Use-Umfang, pro Release (Tag bzw.
  Branch-Diff) statt pro Jahr. Pragmatisch: Das Kurations-Verfahren aus `Release-Notes-UPDATE-README.md` wird Teil
  des Release-Ablaufs und schreibt `RELEASE-NOTES.md` ins Distributionsmodul; die Datei wird mit dem Release
  committet und in alle Artefakte und auf die Downloads-Seite übernommen. Vollautomatisch (Conventional Commits)
  passt nicht zur heutigen Commit-Kultur.
- **Lizenzliste generiert:** `license-maven-plugin` (`org.codehaus.mojo`, `aggregate-add-third-party` +
  `download-licenses`) über den Dependency-Baum des WARs erzeugt `THIRD-PARTY.txt` und die Lizenztexte. Eine
  Mapping-Datei ergänzt Jars ohne Lizenzangabe im POM. Bei LGPL/GPL/unbekannt schlägt der Build fehl
  (`failOnBlacklist`). Ersetzt `Licenses.pdf`.
- **Prüfsummen und Signatur:** `SHA256SUMS` neben jedem Release, das Image signiert mit `cosign` (keyless über CI
  oder mit Schlüssel) [?]. Für die Zips reicht zunächst die Prüfsumme auf der Downloads-Seite; eine GPG-Signatur
  braucht einen gepflegten Schlüssel → Frage 8.7.
- **Release-Tags:** `prepare-release-branches.py` oder der Release-Job setzt `v<version>` auf KnowWE, d3web,
  Commons und jspwiki und spiegelt sie nach GitHub. Damit ist „Quellen zum Release“ ohne `sources.zip` eindeutig
  (LGPL), und GitHub-Releases werden möglich (optional: Zip als Release-Asset).

---

## 4. Varianten pro Artefakt

### 4.1 WAR

| | A1: Classifier an KnowWE-App | A2: eigenes Modul `KnowWE-App-Free2Use` (in KnowWE-DES) |
|---|---|---|
| Idee | Profil `free2use` baut ein zweites WAR mit Classifier | WAR-Overlay auf `KnowWE-App` (core-plugins) + Free-2-Use-Plugins + Startwiki + Policy |
| Pro | wenig neuer Code | Closed-Source-Abhängigkeiten liegen dort, wo sie gebaut werden können; KnowWE-App bleibt die offene Basis |
| Contra | Profilkombinatorik in KnowWE-App wird noch größer; Closed-Source-Plugins im offenen Repo | ein Modul mehr |
| Aufwand | 1 PT | 1–2 PT |

**Empfehlung A2.** Zusätzlich (beide Varianten): die Init-Logik aus 3.1/3.3 in KnowWE (2–3 PT inklusive Tests) und
die Bereinigung von `closed-source-artifacts` (entfällt, wenn keine `sources.zip` mehr gebaut wird).

### 4.2 Docker-Image

| | D1: offizielles `tomcat:9.0-jre25-temurin` + WAR | D2: eigenes Image aus Eclipse-Temurin-JRE + Tomcat aus Maven | D3: internes `base` öffentlich machen |
|---|---|---|---|
| Pro | gepflegt vom Docker-Official-Images-Team, multi-arch (amd64/arm64) ohne eigenen Aufwand, Updates per Rebuild | volle Kontrolle, gleicher Tomcat wie im Zip | eine Image-Linie |
| Contra | Tag-Verfügbarkeit für JRE 25 prüfen [?]; Tomcat-Version ≠ Zip-Version | mehr Pflege, multi-arch selbst | Basis liegt in interner Registry, enthält Interna; Lizenz- und Credential-Fragen |
| Aufwand | 2 PT | 3–4 PT | nicht empfohlen |

**Empfehlung D1.** Eckpunkte:

- Nicht-root-Benutzer, `WORKDIR /usr/local/tomcat`, WAR als `webapps/ROOT` (Kontext `/`, über Variable umstellbar),
  `webapps.dist` bleibt draußen.
- `VOLUME /knowwe` = `KNOWWE_HOME`, Konfiguration über `KNOWWE_*`/`jspwiki_*`, `KNOWWE_ADMIN_PASSWORD_FILE` für
  Docker-Secrets.
- `HEALTHCHECK` über einen leichten Endpunkt. Es gibt keinen dedizierten; ein kleiner Status-Action
  (`/action/Health` o. ä.) wäre sauberer als `Wiki.jsp` [?].
- graphviz und Fonts wie im internen `base`. `-XX:MaxRAMPercentage=75` statt fester Heap-Größe.
- **Bauweg: Jib aus Maven** (Empfehlung) oder `docker buildx`:
  - `jib:build` braucht **keinen Docker-Daemon**. Es baut die Schichten in Java und schiebt sie direkt in die
    Registry. `jib:buildTar` schreibt `target/jib-image.tar`, das sich als Maven-Artefakt ablegen oder per
    `docker load` laden lässt. Nur `jib:dockerBuild` braucht die `docker`-CLI.
  - WAR-Projekte: `<from><image>` = Tomcat-Image, `<container><appRoot>/usr/local/tomcat/webapps/ROOT</appRoot>`,
    Entrypoint-Skript und Konfigurationsvorlagen über `<extraDirectories>`, `<user>`, `<environment>`.
  - Multi-arch über `<from><platforms>` (amd64 + arm64 aus dem Manifest-List des Basis-Images), reproduzierbar
    (`creationTime` = Epoch).
  - Einschränkung: Jib kennt kein `RUN`. graphviz und Fonts müssen deshalb schon im Basis-Image stecken. Ein
    kleines öffentliches Basis-Image `knowwe-base` (Tomcat-Image + `apt-get install graphviz fonts…`) wird per
    `buildx` gebaut, nur bei Tomcat-/JRE-Updates neu. Das KnowWE-Image entsteht dann pro Build per Jib.
  - Zugangsdaten zur Registry kommen aus `settings.xml` (`<server>`, verschlüsselt) oder einem Credential-Helper,
    nicht aus Build-Args. Damit entfällt das Leck aus 1.5.
- Labels nach OCI (`org.opencontainers.image.*`), SBOM (bei Jib per CycloneDX-Plugin, bei buildx per `--sbom`).
- **Registry:** Docker Hub (`d3web/knowwe` oder `denkbares/knowwe`) ist für Endnutzer am bekanntesten, hat aber
  Pull-Limits für Anonyme. `ghcr.io/denkbares/knowwe` passt zum GitHub-Mirror und kostet nichts für öffentliche
  Images → Frage 8.4.
- CI: Wo die öffentlichen Images entstehen, hängt davon ab, wo das WAR entsteht (Jenkins) und wo Docker-Builds
  laufen (GitLab-Runner `docker-build`) → Frage 8.2.
- Doku: `docker-compose.yml`-Beispiel mit Volume, Port und Secret sowie ein Hinweis zu Reverse-Proxy/TLS
  (`jspwiki_baseURL`).

### 4.3 Zip / Standalone

| | Z1: schlankes Zip, Tomcat aus Central | Z2: Z1 + jlink-JRE pro OS | Z3: eingebetteter Tomcat (`java -jar knowwe.jar`) |
|---|---|---|---|
| Inhalt | `bin/knowwe(.sh/.cmd)`, `tomcat/` (bin, lib, conf), `webapps/KnowWE.war`, `data/` | wie Z1 + `runtime/` (jlink, ~50–60 MB) | ein Launcher-Jar mit `tomcat-embed-core` + `tomcat-embed-jasper` (JSPWiki braucht JSP), WAR daneben oder eingebettet |
| Java nötig | ja (25) | nein | ja (25) |
| Pro | einfach, nah an der heutigen Nutzung, ein Zip für alle OS | Doppelklick ohne Java-Installation | kleinste Distribution, sauberer Startpfad, Basis für App und Headless |
| Contra | Java-Voraussetzung bleibt | 3 Zips (Win/Linux/macOS) + macOS-Signatur-Thema | neuer Code; JSPWiki-JSPs unter Embedded-Jasper testen |
| Aufwand | 2–3 PT | +2 PT | 4–6 PT |

**Empfehlung Z1 jetzt.** Die Startskripte setzen `CATALINA_BASE`, `KNOWWE_HOME` (absolut, relativ zum
Skriptordner aufgelöst, damit der CWD-Fehler aus 1.4 verschwindet), Ports (`KNOWWE_PORT`, Shutdown-Port
abgeschaltet oder zufällig) und prüfen die Java-Version mit klarer Meldung („KnowWE <v> braucht Java 25, gefunden:
17“). Ausführungsrechte setzt das Assembly ausdrücklich (`<fileMode>0755</fileMode>` für `bin/*.sh` und
`tomcat/bin/*.sh`), ein Test in CI entpackt das Zip und startet es aus einem fremden Arbeitsverzeichnis. Kein Zip
im Zip, keine Tomcat-Beispiel-Apps, kein `kill -9`-Skript. Z3 ist der Weg zur App (4.4) und lohnt
sich erst dann.

### 4.4 App (später)

- **jpackage auf Basis von Z3** (Launcher + jlink-Runtime): Installer für Windows (MSI), macOS (DMG, Signierung und
  Notarisierung) und Linux (deb/AppImage). Die App startet den eingebetteten Tomcat auf einem freien Port, öffnet
  den Browser, `KNOWWE_HOME` liegt im Benutzerprofil. Erfahrungen aus dem Workspace: `jpackage` ≥ JDK 25 setzt
  Read-only auf die Launcher-Exe (JDK-8363926, siehe IETD-Translation-Checker); Windows-Pins zeigen nach Updates
  auf alte Pfade (ILME).
- **Electron** (wie `service-mate-electron`) bringt nur dann etwas, wenn es eigene Fenster-Funktionen geben soll
  (Tray, Offline-Hinweise, Auto-Update-UI). Es kostet Node-Toolchain, ~100 MB Chromium zusätzlich zum JRE und die
  bekannten Linux-Icon/AppImage-Probleme. Für „KnowWE lokal starten“ ist es zu schwer.
- Aufwand: 5–10 PT nach Z3, je nachdem, wie viele Plattformen signiert werden.

---

## 5. Was sich auf www.d3web.de/Downloads ändert

- **Oben ein Block „KnowWE <neueste Version>“** mit drei Wegen:
  1. Docker: `docker run -p 8080:8080 -v knowwe:/knowwe <registry>/knowwe:<v>`, danach Admin-Passwort aus dem Log.
  2. Zip: Download-Link, SHA-256, „Java 25 nötig“, `bin/knowwe.sh` bzw. `knowwe.cmd`.
  3. WAR: für eigene Tomcat-9-Installationen, mit Link auf die Konfigurationsdoku (`KNOWWE_HOME`, Variablen).
- **Nightly** darunter, klar als ungetestet markiert (Zip + Image-Tag `nightly`).
- **Pro Release eine Zeile:** Version, Datum, Java-Version, Links (Zip, WAR, Image-Tag, Release-Notes, Quellen-Tag
  auf GitHub), Prüfsumme. Die Release-Notes kommen aus derselben Datei wie im Zip.
- **Downloads Archive:** Releases vor der Umstellung mit dem Hinweis, dass sie anonymes Editieren erlauben,
  bekannte Admin-Konten enthalten und teils `Install.jsp` haben; „nicht übers Netz erreichbar betreiben“. Besser:
  2026.4.1 und älter ganz entfernen bzw. nur noch auf Anfrage → Frage 8.8.
- Entfallen: d3web-Bibliotheken als Download (Verweis auf repo-public und GitHub), Quickstart-PDFs (ersetzt durch
  die Seite „Erste Schritte“ im Startwiki und auf d3web.de), das veröffentlichte Default-Admin-Passwort in User FAQ
  und How To AdminRights.

---

## 6. Migrationsschritte

| Phase | Inhalt | Aufwand | Voraussetzung |
|---|---|---|---|
| **0. Sofort** | Im Nightly `userdatabase.xml`/`groupdatabase.xml` aus `wikicontent` entfernen (WikiVanilla), Policy auf „Anonyme lesen“; Passwörter aller 5 ausgelieferten Konten auf d3web.de und überall, wo sie wiederverwendet sein könnten, rotieren; Personen- und Testseiten aus WikiVanilla entfernen; Start-Hinweis im Quickstart korrigieren; Admin-Passwort von d3web.de-Seiten entfernen; Downloads-Hinweis „nicht öffentlich betreiben“ | 0,5 PT | deine Freigabe; Zugriff auf WikiVanilla + Jenkins-Job |
| **1. Grundlagen in KnowWE** | `KNOWWE_HOME`-Init, Konfigurationsvorlage, `copycorepages` reparieren bzw. ersetzen, Startwiki kuratieren (eine Quelle), Erst-Admin-Hook, sichere Default-Policy | 4–6 PT | Entscheidungen 8.5 (Startwiki), Policy |
| **2. WAR + Zip per Maven** | Modul `KnowWE-App-Free2Use` (A2) + Assembly Z1, Tomcat aus Central, `license-maven-plugin`, `requireReleaseDeps`, Release-Notes-Datei im Modul | 3–4 PT | Entscheidung 8.3 (Ort) |
| **3. CI** | Release-Job pro `release-YYYY.N` und Nightly, Upload nach `downloads`, `SHA256SUMS`, Tags `v<version>` + GitHub-Mirror; alten Ant-Job abschalten, `knowwe-distribution` archivieren (Commercial-Ziel vorher klären, Frage 8.9) | 2–3 PT | Jenkins-Zugang, 8.1 |
| **4. Docker** | Dockerfile (D1), buildx multi-arch, Healthcheck, Compose-Beispiel, Veröffentlichung, Image-Signatur | 2–3 PT | 8.2, 8.4, 8.6 (GraphDB) |
| **5. Downloads-Seite** | neuer Aufbau (Abschnitt 5) per MCP, Archiv bereinigen | 0,5–1 PT | erstes Release mit neuer Kette (2026.8) |
| **6. App (später)** | Launcher Z3 → jpackage-Installer | 9–16 PT | Bedarf bestätigt |

Bis Phase 5 rund **12–18 PT**. Sinnvoller erster Release mit der neuen Kette: **2026.8**. Für 2026.6 und 2026.7
nachträglich Zips zu bauen lohnt sich nicht, wegen der SNAPSHOT-Abhängigkeit (2026.6) und der alten Policy.

---

## 7. Risiken

- **GraphDB-Lizenz** kann das öffentliche Image (und streng genommen schon heute das Zip) blockieren. Ausweg:
  Free-2-Use ohne GraphDB, mit RDF4J/Jena als SemanticCore-Default, und GraphDB als Nachinstallation für eigene
  Lizenznehmer. Vorher prüfen, welche Demos/Doc-Seiten GraphDB-Rulesets voraussetzen.
- **Closed-Source-Plugins im öffentlichen Artefakt:** Die Binaries werden auch künftig öffentlich verteilt (wie
  heute). Wer das Image auseinandernimmt, sieht sie; das ist kein neues Risiko, aber eine bewusste Entscheidung.
- **Startwiki-Pflege:** Ohne Verantwortliche veraltet es wieder (wie WikiVanilla). Ein CI-Test, der das Startwiki
  compiliert und auf Fehler prüft (Headless-App ist dafür vorhanden), hält es ehrlich.
- **Tomcat 9 / javax:** Tomcat 9 wird noch gepflegt, das Ende ist aber absehbar [?]; der Umstieg auf Jakarta
  (Tomcat 10+) betrifft JSPWiki und KnowWE gleichermaßen und ist nicht Teil dieses Konzepts.

---

## 8. Offene Fragen und Entscheidungen

1. **Jenkins-Job des Nightly:** Wie heißt er, welcher Rechner/Workspace, welche Repos checkt er aus, wie lädt er
   hoch (HTTP-PUT mit `service.des.ci`)? Gibt es einen zweiten Job für Release-Zips oder wird derselbe mit
   `-DVERSION` angeworfen?
2. **CI-Heimat der öffentlichen Builds:** Jenkins (dort entstehen WARs und Releases) oder GitLab-CI (dort laufen die
   Docker-Builds)? Empfehlung: alles im Jenkins-Release-Job. Mit Jib braucht der Agent dafür kein Docker; nur das
   selten neu gebaute Basis-Image `knowwe-base` läuft per `buildx` (GitLab-Runner `docker-build`).
3. **Ort des Distributionsmoduls:** KnowWE-DES (`KnowWE-Apps-DES/KnowWE-App-Free2Use`), Empfehlung, oder ein
   neues schlankes Repo, oder `knowwe-distribution` auf Maven umbauen?
4. **Registry:** ghcr.io/denkbares (Empfehlung, passt zum GitHub-Mirror) oder Docker Hub? Name `knowwe` oder
   `d3web-knowwe`?
5. **Startwiki:** Quelle `core-pages` im KnowWE-Repo als einzige Quelle (Empfehlung)? Welche Demos bleiben? WikiVanilla
   und die Headless-Kopie danach abschaffen?
6. **GraphDB:** Darf denkbares `graphdb-runtime` öffentlich weiterverteilen (Zip und Image)? Wenn unklar: Free-2-Use
   ohne GraphDB?
7. **Signatur:** reichen SHA-256-Summen, oder sollen Zips GPG-signiert und das Image mit cosign signiert werden
   (Schlüsselverwaltung)?
8. **Archiv:** Releases vor 2026.8 mit Warnhinweis behalten oder entfernen (Empfehlung: entfernen bzw. nur auf
   Anfrage, wegen Admin-Konten, Policy und `Install.jsp`)?
9. **Commercial-Distribution** (`BUILD_COMMERCIAL_DIST` mit Headless-App, KBB, Testing-App, verschlüsselten
   Quellen): Wird sie noch gebraucht? Davon hängt ab, ob `knowwe-distribution` archiviert werden kann.
10. **Kontextpfad** in Image und Zip: `/` (einfacher hinter Proxies) oder weiter `/KnowWE` (kompatibel zu Links und
    Doku)?
11. **Startzeitpunkt:** Phase 0 sofort im bestehenden Nightly, Rest ab 2026.8?
