# Content-Audit d3web.de

Stand: 2026-10-14. Quellen: Wiki-Kopie `/home/cody/knowwe-d3web/wiki/` (identisch mit `KnowWE-Plugin-d3web-Homepage/src/main/resources/wiki`), Live-Site www.d3web.de (rund 60 Requests), lokale Repos unter `Projects/Main`, GitHub-API (Org denkbares), repo.denkbares.com.
Legende: **[V]** = verifiziert (Code, Repo, HTTP oder DNS), **[?]** = muss das Team bestätigen.

---

## 0. Kurzfassung: die wichtigsten Befunde

1. **Downloads: Java-Angabe für den Nightly-Build ist falsch [V].** Dort steht "JDK 17". Der Nightly wird aus master (2026.8-SNAPSHOT) gebaut, und denkbares-Commons kompiliert seit Branch `release-2026.6` mit `<release>25</release>`. Das Nightly-Zip läuft also nur noch mit Java 25. Belegt aus dem Code; einen Startversuch mit dem Zip habe ich nicht gemacht.
2. **Produktseiten bewerben eingestellte Produkte [V].** Startseite und Product Overview nennen d3web-Mobile (Downloads von 2015, Windows Vista/7), droid3web (kein Code mehr in den Repos, Android 4.0.4), ProKEt und WUMPS (beide nicht mehr vorhanden).
3. **Impressum nennt wahrscheinlich den falschen Anbieter [V/?].** Das Impressum führt den Lehrstuhl Informatik VI der Uni Würzburg als Verantwortlichen nach "§5 TMG / §55 RStV". Beide Gesetze gibt es nicht mehr. Betrieben wird die Site aber offenbar von denkbares: Hosting auf `littlefinger.denkbares.com`, MX `mail.denkbares.com`, Matomo auf `stats.denkbares.com`, Downloads auf repo.denkbares.com, seit 10/2024 nur Commits von denkbares/servicemate. Die Datenschutzerklärung stammt von 2015, kennt die DSGVO nicht und erwähnt Matomo nicht.
4. **Sicherheit [V].** User FAQ und "How To AdminRights" veröffentlichen ein Default-Admin-Konto mit Passwort. Die passenden `userdatabase.xml`/`groupdatabase.xml` hängen als Anhang an beiden Seiten.
5. **Entwickler-Doku stammt aus der SVN-/Eclipse-Zeit [V].** Getting Started, Developer FAQ und How-To CreatePlugin beschreiben SVN-Check-ins, drei "main repositories", Eclipse-Run-Configs und Javadoc-Links auf den toten Host `isci.informatik.uni-wuerzburg.de`. Die Workspace-Seite empfiehlt JDK 14 und Tomcat 9.0.44.
6. **Sichtbare Renderfehler und interne Notizen [V].** Die 4 "Module …"-Seiten, auf die Products verlinkt, zeigen "No ontology found!" und "Internal error while rendering section: null". "Installed plugins" im linken Menü zeigt nur ein Todo mit Bugzilla-Link von 2014. Tutorial zeigt öffentlich eine TODO-Liste ("Get Flash-Plugin working again"). Doc Overview hat 3 rote Create-Page-Links.
7. **Live-Instanz ist veraltet [V].** Footer: "JSPWiki v2.11.0-git-12", RecentChanges: "KnowWE 20220509_17:48". Der aktuelle Fork ist 2.12.10-SNAPSHOT, KnowWE 2026.8-SNAPSHOT.

---

## 1. Verifizierte Rahmendaten (Grundlage für neue Texte)

| Thema | Fakt | Quelle |
|---|---|---|
| Aktuelle Entwicklungsversion | `2026.8-SNAPSHOT` (`revision` in KnowWE, d3web, denkbares-Commons) | pom.xml |
| Maven-Releases (repo-public, KnowWE-App) | … 2025.4 (05.09.2025), 2025.5, 2026.1, 2026.2, 2026.3, 2026.3.1, 2026.4.1, **2026.6 (12.08.2026)**, **2026.7 (29.09.2026)** | repo.denkbares.com/list/repo-public |
| Distribution-Zips | Nur bis **2026.4.1**. Im Ordner `2026/` liegen nur 2026.3.1 und 2026.4.1, im Ordner `nightly/` liegt LATEST (zuletzt 14.10.2026). Für 2026.6 und 2026.7 gibt es keine Zips. | repo.denkbares.com/list/downloads/… |
| Java pro Release (aus `denkbares-Commons` `<release>`) | release-2026.1 bis 2026.4: **17** · release-2026.5: **21** · release-2026.6, 2026.7, master: **25** | Release-Branches |
| Java im Code | KnowWE/d3web kompilieren mit `<release>17</release>`, hängen aber von Commons (Class-Files Version 69) ab. Laufzeit ist damit **Java 25**. | pom.xml, `KnowWE-App/documentation/Lokaler-Testserver.md` |
| Servlet-Container | **Tomcat 9** (javax.servlet), API-Version `tomcat.version=9.0.104` | KnowWE/pom.xml, Lokaler-Testserver.md |
| JSPWiki-Fork | `jspwiki-knowwe-*` 2.12.10-SNAPSHOT (Release 2.12.9 am 29.09.2026), `jdk.version=21`. Quelle: github.com/denkbares/jspwiki | jspwiki/pom.xml, repo-public |
| JSPWiki-Template | Ab 2.11 heißt "haddock" **default**. In `jspwiki-custom.properties.default` steht `jspwiki.templateDir = default`. | KnowWE-App |
| Policy-Klassen | `org.apache.wiki.auth.*` statt `com.ecyrd.jspwiki.*` | KnowWE-App/…/jspwiki.policy |
| GitHub | `Commons`, `d3web-Runtime`, `d3web-KnowWE` (LGPL-3.0, angelegt 04/2016, zuletzt gepusht 10/2026), `jspwiki` (Apache-2.0). Keine GitHub-Releases. Entwickelt wird auf git.denkbares.com, GitHub ist ein Mirror. | GitHub-API, `git remote` |
| GitHub Discussions | d3web-KnowWE: letzte Aktivität 13.01.2023. d3web-Runtime: leer. | github.com |
| SourceForge | d3web zuletzt aktualisiert 2017, knowwe 2014 | sourceforge.net |
| Bauen aus GitHub-Quellen | KnowWE-App aktiviert per Default das Profil `denkbares` (Closed-Source-Plugins). Diese Artefakte sind anonym nicht abrufbar (Verzeichnisse auf repo-public leer). Externe Builds brauchen deshalb `-P core-plugins`. | KnowWE-App/pom.xml, repo-public |
| Status der Apps | TestingApp (d3web-DES), Mobile-Application (KnowWE-DES), KnowWE-Headless-App (KnowWE-DES) und KnowledgeBaseBook (d3web-DES) liegen als Module in **Closed-Source**-Repos. Mobile-App: letzte inhaltliche Änderung 2022, danach nur Versions-Bumps. TestingApp: zuletzt 2023. **droid3web, ProKEt, WUMPS: kein Code im Workspace** (von ProKEt gibt es nur `d3web-Plugin-ProKEtProperties` in Experimental). | `find` / `git log` |
| Aktivität Uni Würzburg | Seit 10/2024 nur Commits von denkbares.com und servicemate.de, letzter Uni-Account 2016 | git log |
| Hosting/Mail/Analytics | www.d3web.de → littlefinger.denkbares.com (195.201.56.82). MX d3web.de → mail.denkbares.com. Matomo (`stats.denkbares.com`, siteId 3, **`disableCookies`**). Setzt nur das Cookie JSESSIONID (Secure, HttpOnly). | DNS, HTTP |
| denkbares-Impressum (offiziell) | denkbares GmbH, **John-Skilton-Straße 8, 97074 Würzburg**, +49 931 900050-00. Vertreten durch **Dr. Joachim Baumeister, Volker Belli**. Amtsgericht Würzburg **HRB10160**, USt-IdNr. DE265029014. | denkbares.com/impressum |

### Letzte Änderung der Hauptseiten (laut Live-Site)

| Seite | zuletzt geändert |
|---|---|
| Main | 02.05.2020 |
| Products | 22.02.2014 |
| Downloads | 29.05.2026 (einzige aktuell gepflegte Seite) |
| Success Stories | 21.12.2012 |
| History | 27.09.2016 |
| Tutorial | 14.03.2022 |
| Doc Overview | 29.04.2015 |
| User FAQ | 20.03.2018 |
| Demos | 28.05.2019 |
| Getting Started | 18.12.2012 |
| How-Tos | 29.04.2016 |
| Developer FAQ | 24.02.2016 |
| API Documentation | 22.02.2017 |
| Contact | 22.03.2023 |
| Imprint | 18.12.2012 |
| Privacy Statement | 28.01.2015 |
| KnowWE | 28.12.2012 |
| D3web-Core | 19.12.2012 |
| Mobile-App | 03.05.2013 |
| Droid3web-App | 18.12.2012 |
| Testing-App | 24.02.2016 |
| D3web-KnowWE Quickstart | 03.02.2017 |
| How-To Create Your Own Workspace | 14.03.2022 |
| LeftMenu | 26.08.2021 |

(Die `OLD/`-Ordner der Kopie sind leer. Die Daten stammen deshalb von der Live-Site.)

---

## 2. Befunde pro Seite

### LeftMenu
- "Repository" → `https://repo.denkbares.com/webapp/#/artifacts/browse/tree/General/releases-public` liefert **404** [V], weil Artifactory 7 diese alte UI-URL nicht mehr kennt. Ersatz: `https://repo.denkbares.com/list/repo-public/` (Verzeichnisliste, funktioniert ohne JS) oder `https://repo.denkbares.com/ui/repos/tree/General/releases-public` (beide 200).
- "Installed plugins" → `InstalledTagHandlers` zeigt nur ein öffentliches Todo ("listplugins currently isn't working", Bugzilla-Link tickets.denkbares.com, 2014) [V]. Den Link entfernen oder die Seite reparieren.
- "Forum" → GitHub Discussions. Der Link funktioniert, die Discussions sind aber seit 01/2023 inaktiv [V]. Klären, ob es weiter ein Forum geben soll [?].
- "YouTube" → Kanal d3webCommunity existiert (Canonical `UCcHY-bxNWYj1Lye3XgOH8zw`). Ob dort Inhalte gepflegt werden, ist offen [?].
- "Contact / Impressum": siehe Abschnitt 4.

### Main (d3web Home)
- Unter "Platform Overview" stehen **d3web-Mobile ("Win, Mac, Linux")** und **droid3web** als Kern-Apps [V: Mobile-App seit 2022 nur noch gewartet, Download-Builds "Alice" von 2015; droid3web ohne Code]. Diese Einträge entfernen oder als historisch markieren.
- "[testing knowledge|Testing-App]" und "[applying the knowledge|Mobile-App]" verlinken im Einleitungstext Closed-Source- bzw. Legacy-Apps. Besser auf KnowWE (CI-Dashboard, TestCase-Player) und d3web-Runtime (Embedding) verlinken.
- Es gibt keinen Hinweis auf das aktuelle Release und keinen GitHub-Link auf der Startseite. Die Anhänge `calendar_100.png` (früher News) sind ungenutzt.
- Bildnachweis CC-BY-SA 2.0: korrekt, solange die Bilder bleiben.

### Products (Product Overview)
- d3web-Core: Das Projekt heißt heute **d3web-Runtime** (GitHub). Der LGPL-Link geht auf gnu.de (deutsch). Besser `https://www.gnu.org/licenses/lgpl-3.0.html`.
- KnowWE: "As the successor of KnowME …" ist der Stand von 2008; Tippfehler "offeres".
- **ProKEt (LGPL), WUMPS (LGPL): kein Code mehr vorhanden [V].** Entfernen.
- **Mobile-App, Droid3web-App:** siehe oben. Entfernen oder in einen Abschnitt "Discontinued" verschieben.
- **KnowWE-Headless-App "(LGPL)": das Modul liegt im Closed-Source-Repo KnowWE-DES [V].** Die Lizenzangabe ist sehr wahrscheinlich falsch [?].
- KnowledgeBaseBook / Testing-App "(commercial application)": Module existieren (d3web-DES). Ob sie noch angeboten werden, muss das Team klären [?].
- "Modules (under construction)": alle 4 Modulseiten (TimeDB, CostBenefit, Rdf2Go, DiaFlux) zeigen live **"No ontology found! … Internal error while rendering section: null"** [V]. Sie nutzen eine alte Ontologie-Syntax (`def X`, `> X partOf:: Y`) und Plugin-Namen, die es nicht mehr gibt (`KnowWE-Plugin-RDFS-*`). Rdf2Go selbst ist seit Watney (2016) abgelöst. Empfehlung: den Abschnitt samt "Modules Under Construction" entfernen.

### Downloads
- Box "Attention": "JDK version is 17 or newer for version 12.9 and up" ist **falsch für 2026.5 (21) und den Nightly (25)** [V].
- Zeile "last night | JDK 17" muss **JDK 25** heißen [V]. Der Azul-Link zeigt auf Java 17.
- Link-Beschriftung: "v2026.4" lädt `2026.4.1.zip`, "v2026.3" lädt `2026.3.1.zip`. Die Beschriftung sollte die tatsächliche Version nennen [V]. Datum 2026.3: Seite sagt 20.04.2026, Maven-Release 2026.3.1 am 10.04.2026 [V, Kleinigkeit].
- **Maven-Releases 2026.6 und 2026.7 haben kein Distribution-Zip** [V]. Klären, ob die Zips fehlen oder diese Releases bewusst nicht als Zip erscheinen [?].
- Alle JDK-Links der Tabelle zeigen auf `adoptopenjdk.net/archive.html…`. AdoptOpenJDK ist eingestellt, die Seite verweist nur noch auf Adoptium. Ersatz: `https://adoptium.net/temurin/releases/?version=25` bzw. `?version=17` (beide 200).
- GraphDB-Hinweis: Das Maven-Artefakt heißt seit GraphDB 10 `graphdb-runtime` (aktuell 10.8.12), nicht `graphdb-free-runtime` [V]. mvnrepository antwortet 403 auf curl. ontotext.com leitet auf graphwise.ai weiter [V]. Ob GraphDB noch im Zip steckt, muss das Team prüfen [?] (Profil `denkbares-free2use` enthält `denkbares-SemanticCore-Plugin-GraphDB`).
- Die Archiv-Links (12.x, 2023–2025, Legacy 6–11) sind alle erreichbar (200) [V].
- Abschnitt "Sources": d3web-Runtime und d3web-KnowWE stimmen, es fehlen **Commons** und **jspwiki** (für einen eigenen Build nötig bzw. nützlich). "Since version 11, the sources are also contained in the … zips": laut Profil `dist`/`copy-sources` vermutlich weiterhin richtig [?].
- Abschnitt "d3web Mobile-App (deprecated)": Die Links sind mit "(SourceForge)" beschriftet, zeigen aber auf repo.denkbares.com. Die Builds (Alice, 2015, x86/x64, OSX) funktionieren. Klären, ob sie überhaupt noch angeboten werden sollen [?].
- "Help": "Forum" zeigt auf **SourceForge** (seit 2017 inaktiv), das linke Menü dagegen auf GitHub Discussions. Das vereinheitlichen. Die Wendung "mailing list" für info@d3web.de ist irreführend.
- "License": Der LGPL-Link geht auf gnu.de. "denkbares" verweist auf http://www.denkbares.com/ (funktioniert).

### Success Stories (und Unterseiten)
- Seit 21.12.2012 unverändert. Alle 6 Stories (ESAT, SmartCare, WiM-Vent, Cataract/WISSKONT, CliWE, KnowSEC) stammen aus 2011–2013. Die Forschungsprojekte WiM-Vent (2011–2014), CliWE und WISSKONT sind abgeschlossen. Ob die Stories noch gezeigt werden sollen und welche neuen dazukommen, muss das Team entscheiden [?].
- Tote externe Links [V]: `http://www.vo-fab.at/` (ESAT, keine Verbindung), `http://wimvent.org` (keine Verbindung), `http://guideline.gov/` (SmartCare, 403; die AHRQ-Clearinghouse wurde 2018 eingestellt), `http://www.ehealth20xx.at/eHealth2012/…` (Redirect auf eine kaputte URL). `http://leitlinien.dgk.org/` leitet auf herzmedizin.de weiter (funktioniert, aber umgezogen).
- KnowSEC: "Currently, the knowledge base is still under development … 214 questions … 11,000 substances" ist der Stand von 2012. KnowSEC wird intern noch aktiv weiterentwickelt (eigener Workspace). Zahlen und Text bei Interesse aktualisieren [?].
- Tippfehler: "gouvernemental", "Handicaped", "ophtalmologist", "sceenshot".

### History
- Die Tabelle endet 2013/2014. Zeile 1996 ("still current managing director is Thomas Unglert", "currently a market leader") ist ungeprüft und veraltet [?].
- Kopfzeile "2012-12-17: Start of this web site" ist eine alte News-Zeile.
- Vorschlag für Ergänzungen, alle belegbar [V]:
  - 2016: Open-Source-Code auf GitHub (Repos angelegt 26.04.2016); Release "Watney" 11.0 mit Quellen im Distribution-Zip
  - 2020–2022: Releases 12.2–12.10; ab 12.9 (06/2022) Java 17
  - 2023: Umstellung auf Kalender-Versionen (2023.1 am 12.01.2023)
  - 2026: Java 21 (2026.5) und **Java 25 (ab 2026.6)**; Lucene 10
  - Über die Jahre 2015–2026 entscheidet das Team [?].
- Tippfehler "diiploma", "FRANZLIP", "Pearl based wiki" (statt Perl), "Therapiestuerung".

### Tutorial
- **Öffentlich sichtbarer `%%Todo`-Block** ("Get Flash-Plugin working again", "Create YouTube-Channel d3web" …) [V]. Entfernen.
- Die 3 "Developer Screencasts" (IntelliJ-Projekt, "Check out … from Version Control", Module importieren) gehören dem Kanal think-further.de, nicht d3webCommunity [V]. Inhaltlich vermutlich aus der SVN-Zeit [?]. Ersetzen oder als veraltet kennzeichnen.
- Die Installations-Unterseiten sind veraltet, siehe unten (Quickstart, wikicontent location, user rights, multiple wikis).

### D3web-KnowWE Quickstart (Installation)
- "JDK in version 8 or newer", "Firefox 3.6", "Internet Explorer and Safari not officially supported, IE11/Edge should work fine": alles veraltet [V]. Richtig wäre JDK 17 für ≤2026.4, 21 für 2026.5, 25 ab 2026.6/Nightly.
- Der beschriebene Zip-Aufbau (`d3web/`, `KnowWE/`, `Licenses`, `KnowWE-Tomcat.zip`) stammt aus der Watney-Zeit. Ob das Zip heute noch so aufgebaut ist, muss das Team prüfen [?] (die Assembly ist nicht in den Repos, vermutlich in einer Jenkins-Job-Konfiguration).
- Der Hinweis "chmod -R 777 *" ist ein schlechter Rat (Sicherheit). Besser: Ausführungsrechte auf `bin/*.sh` setzen.
- Die JSPWiki-Doku-Links (Configuration, Wiki.Admin.Security) funktionieren [V].

### D3web-KnowWE - change wikicontent location / change user rights / host multiple wikis
- "change wikicontent location": nennt `KnowWE-OS-<Release>.zip` und `jspwiki.properties`. Heute heißt die Datei `WEB-INF/classes/jspwiki-custom.properties` (Key `var.basedir`, so steht es auch auf der Quickstart-Seite) [V].
- "change user rights": Die Beispiel-Policy nutzt **`com.ecyrd.jspwiki.*`**. Diese Klassen gibt es in JSPWiki 2.11+/dem Fork nicht mehr (heute `org.apache.wiki.auth.*`) [V]. Wer das Beispiel kopiert, bekommt eine kaputte Policy. Den Link `http://doc.jspwiki.org/2.4/…` beantwortet die Seite mit 404 [V].
- "host multiple wikis": `-XX:MaxPermSize` gibt es seit Java 8 nicht mehr (JVMs ab 17 brechen ab bzw. warnen) [V]. Ob mehrere Wikis heute noch so betrieben werden, muss das Team klären [?] (KnowWE/doc enthält `MultiWikiRetired.md`, Tag `multi-wiki-archive`).

### Doc Overview (Documentation)
- Links auf **nicht existierende Seiten** [V]: `Doc Status`, `Doc Tags`, `Doc Timechart`. Live erscheinen sie als "Create"-Links. Ein `Status`-Markup gibt es im Code nicht. `TimeChartType` existiert (TimeDB, closed source), `Tags` existiert im Tagging-Plugin.
- `Doc Timeline`: Ein `%%Timeline`-Markup finde ich im Code nicht mehr [V: grep ohne Treffer]. Wahrscheinlich entfernt [?].
- "Alternatively you may download the knowledge base … to run on Android or in a stand-alone application" bezieht sich auf droid3web bzw. die Mobile-App, siehe oben.
- "Further Links": `knowwe.sourceforge.net` / `d3web.sourceforge.net` leiten auf verwaiste SourceForge-Projekte (2014/2017) [V]. Durch GitHub ersetzen. `http://www.jspwiki.org/wiki/JSPWikiDocumentation` liefert **404** [V], Ersatz `https://jspwiki-wiki.apache.org/Wiki.jsp?page=Documentation` [?] (Ziel-URL nicht geprüft).
- Die kommerziellen Markups (Expressions, TimeDB, Variable, Constant, ImageMap, Todo, UML, KPI) existieren im Closed-Source-Repo [V]. Die Zuordnung "commercial extension" ist damit plausibel. Bei UML und Todo fehlt der Hinweis "commercial".
- Tippfehler: "how for format article", "dinstinct", "indiviuals".

### User FAQ
- "KnowWE runs … with **Java 8** or greater" ist veraltet [V].
- "d3web-Core3" (Tippfehler); "University of Würzburg (Prof. Puppe) and denkbares are improving … continuously". Seit 10/2024 committet nur noch denkbares [V]. Die Rolle der Uni muss das Team klären [?].
- **"How to use JSPWiki's new look 'haddock'":** `jspwiki.templateDir = haddock` ist veraltet. Haddock ist seit JSPWiki 2.11 das Template "default" und KnowWE setzt es bereits so [V]. Den Eintrag löschen.
- **"How can I get Admin rights …": veröffentlicht User und Passwort des Default-Admins und hängt `userdatabase.xml`/`groupdatabase.xml` an** [V]. Das gilt genauso für `How To AdminRights` und wird in `How-To Secure…` und `Doc FAQ` referenziert. Empfehlung: entfernen und die Einrichtung eines eigenen Admins über `userdatabase.xml` bzw. die JSPWiki-Doku beschreiben [?]. Ob das Konto in einer produktiven Instanz noch aktiv ist, habe ich bewusst nicht getestet.
- "SemanticGuide" (ontoprise/semafora): ontoprise ist seit 2012 insolvent. Der Eintrag ist historisch.
- Lizenz: "Changes on the existing code need to be pushed back to us." So steht das nicht in der LGPL. Sie verlangt nur, bei Weitergabe die Quellen der Änderungen bereitzustellen. Juristisch prüfen [?].
- "Is there a stand-alone runtime … Windows, Mac OS, Linux or Android": Mobile-App/droid3web, siehe oben.
- Die Literaturhinweise sind ok, aber alt (2001–2011).

### Demos
- Alle 9 Demo-Seiten laden live ohne sichtbare Fehlermeldungen [V, Prüfung nur auf Fehlertexte im HTML].
- Das Markup ist kaputt: Zeile "Simpsons" endet mit `‘‘` (typografische Quotes) statt `''`, dadurch wird der Rest kursiv gesetzt [V].
- Bild-Links nutzen `Wiki.jsp?page=…` und `attach/…`-Pfade. Sie funktionieren, sind aber brüchig.
- "Demo - Temperature" braucht TimeDB (closed source). Ein Hinweis darauf fehlt [?].

### Getting Started
- "set up an **eclipse** workspace", Forum auf **SourceForge** [V: veraltet]. Kontakt info@d3web.de geht an denkbares (MX).
- Vorschlag: die Seite komplett durch die neue Build-Anleitung ersetzen (Abschnitt 6, P1-5).

### How-To Create Your Own Workspace
- IntelliJ-Teil: "latest version of **JDK 14**" und Tomcat-Link **9.0.44** sind veraltet [V]. Richtig wäre JDK 25 und ein aktueller Tomcat 9 (9.0.123 ist aktuell auf tomcat.apache.org, das POM nutzt die API 9.0.104).
- Repos `Commons`, `d3web-Runtime`, `d3web-KnowWE` stimmen [V]. Pfad `KnowWE-App/src/resources/local/WEB-INF/classes/jspwiki-custom.properties.default` und `var.basedir` stimmen [V].
- Es fehlt der Hinweis, dass ohne Zugang zu den Closed-Source-Artefakten das **Profil `core-plugins`** aktiviert werden muss. Sonst schlägt die Auflösung von `com.denkbares:KnowWE-Plugin-Auth-OIDC` usw. fehl [V: Default-Profil `denkbares`, Artefakte anonym nicht abrufbar].
- Eclipse-Teil (Gastbeitrag von 2022): Temurin 17, Eclipse 2022-03, Maven 3.6.1, "JavaSE-14 compliance", "installed JDK 1.7" (Tippfehler). Insgesamt veraltet, außerdem ein kaputtes Link-Markup (`…/m2}` statt `]`) [V]. URL `http://localhost:8080/KnowWE-App` widerspricht dem Kontext `/KnowWE`.
- "community key for the Ultimate Edition … ask via dev list": Diese Liste gibt es vermutlich nicht mehr [?].

### How-Tos und die verlinkten Seiten
- How-To CreatePlugin: **SVN-Workflow** (Team → Share → SVN, svn:ignore), "three main repositories d3web-core / d3web-KnowWE / Research", Eclipse "Maven Module", JDK 6 Update 17, Link `d3webwiki.informatik.uni-wuerzburg.de` (DNS tot) [V]. Lizenz-Header "Copyright (C) 2012 Chair of AI … University of Wuerzburg". Aktuelle Dateien tragen "Copyright (C) 2026 denkbares GmbH, Germany" und zum Teil `SPDX-License-Identifier: LGPL-3.0-or-later` [V].
- Viele How-Tos verlinken Javadoc auf **`isci.informatik.uni-wuerzburg.de/javadoc/…`** (Host nicht erreichbar) [V]: How-To Create a Session, Markup, SubtreeHandler, PageAppendHandler, Extension Points, Add Javascript and CSS Files, Create CITest, Load a Knowledge Base.
- Veraltete APIs [V]: `SubtreeHandler` gibt es nicht mehr (die Seite ist selbst als DEPRECATED markiert, heute `CompileScript`/`PackageCompileScript`). `KnowWEAction` gibt es nicht mehr (`AbstractAction`). Eine Klasse `SectionStore` gibt es nicht mehr (die Seite hat selbst ein öffentliches Todo "Review this page"). Paketnamen `de.d3web.we.*` sind größtenteils durch `de.knowwe.*` ersetzt. Weiterhin vorhanden sind `PageAppendHandler`, `InitPluginManager` (jetzt `com.denkbares.plugin.test`), `ToolProvider`, `DefaultMarkupType`, `Environment`.
- How-To Secure A KnowWE Installation: `com.ecyrd`-Policy, kaputtes `%%prettify`-Markup (Before/After vertauscht bzw. verschachtelt), öffentliches Todo [V].
- Empfehlung: Die How-Tos als "Legacy (pre-2016)" kennzeichnen oder ausdünnen. Eine Überarbeitung Seite für Seite lohnt sich nur, wenn externe Entwickler Zielgruppe sind [?].

### Developer FAQ
- Plugin-Auswahl über Eclipse-Run-Configuration, Abschnitt "first check-in" mit **SVN**, Link `d3webwiki.informatik.uni-wuerzburg.de` (tot), Forum auf SourceForge, "Tomcat > Update Tomcat Context" (Eclipse-Sysdeo-Plugin), "update your JDK … oracle.com/technetwork/java/javaee" (leitet auf das Java-EE-8-SDK um) [V].
- `http://www.jspwiki.org/wiki/BugProblemsWithGermanUmlaute` liefert 404 [V].
- Lizenz-Header von 2010 mit "University of Wuerzburg", siehe oben. Anhang `codetemplates.xml` existiert, verweist aber per Wikilink `[codetemplates.xml]` auf eine Seite statt auf den Anhang.
- Weiter gültig: der Abschnitt "Session objects thread-safe" und die Erklärung zu "Unable to load and setup properties" (vermutlich weiterhin richtig [?]).

### API Documentation
- Javadoc steckt laut Seite in `doc.zip` im Distribution-Zip. Das Profil `dist` erzeugt Javadoc, ob es im Zip landet, muss das Team prüfen [?].
- Paketstruktur: `de.d3web.*` (d3web), `de.knowwe.*` (KnowWE) und **`com.denkbares.*`** (Commons, seit Watney) fehlen in der Beschreibung `de.d3web/knowwe.…`. Die genannten Klassen (KnowledgeBase, PersistenceManager, Session, Blackboard, KnowledgeBaseUtils, SessionFactory …) gibt es weiter [V, Stichprobe].
- Ein Link auf die GitHub-Quellen fehlt.

### KnowWE (Produktseite)
- **System Requirements: "JDK 6", "Windows XP, Windows 7, Mac OS 10.6", "Firefox 3.6"** [V: alles veraltet].
- "OWL uses the **Manchester syntax**": Im Code finde ich keine Manchester-Syntax mehr (grep ohne Treffer) [V]. Die README im Repo sagt nur noch "turtle markup". Text an die README anpassen.
- Link `http://www.jspwiki.org` leitet auf jspwiki.apache.org weiter (ok). Ein Hinweis auf den eigenen Fork (github.com/denkbares/jspwiki) fehlt.

### D3web-Core
- Name: heute **d3web-Runtime** (GitHub-Repo, `d3web` lokal).
- "Temporal rules" als Bestandteil von d3web-Core: TimeDB liegt in d3web-DES (closed source) [V]. Kennzeichnen.
- System Requirements: "Installation of **KnowWE** requires … JDK 6 … Linux not officially supported" ist auf der falschen Seite und veraltet [V].

### Testing-App
- Download "with the open source release from **sourceforge**" und "nightly build … from the build server" `isci.informatik.uni-wuerzburg.de/hudson/…` (nicht erreichbar) [V].
- "**Oracle Java 8**", "ANT-toolkit", "Chapter 4 of this manual" (Verweis ins PDF-Handbuch).
- Das Modul existiert noch in d3web-DES (closed source, Handbuch `Testing-App User Manual.pdf` im Modul). Die letzte inhaltliche Änderung war 2023 [V]. Ob es noch ein Produkt ist, muss das Team klären [?].
- Der Abschnitt "!!Licence" bricht nach "This application " ab (unvollständig) [V].

### Mobile-App
- "Windows **Vista or 7** 64 Bit, Mac OSX 64 Bit". Downloads sind die 2015er "Alice"-Builds [V]. Modul `Mobile-Application` in KnowWE-DES (Package `cc.denkbar.mobileapplication`), seit 2022 ohne inhaltliche Änderung [V].
- Vorschlag: Seite als "discontinued" markieren und die Links aus Main und Products entfernen [?].

### Droid3web-App
- "Android (min. 4.0.4 Ice Cream Sandwich)", "A new version of this application is currently in development" (Stand 2012). **Im Workspace gibt es keinen Code** [V]. Seite entfernen oder als eingestellt markieren.

### Licenses d3web KnowWE
- Die Liste ist von 03/2020. Abgleich mit den aktuellen POMs [V]:
  - **log4j**: Das POM managt weiterhin `log4j.version=2.22.0`, Logging läuft aber über slf4j/logback (1.5.19). Logback fehlt in der Liste (EPL-1.0/LGPL-2.1).
  - Es fehlen unter anderem Lucene 10 (Apache-2.0), Jackson, Gson, Guava, Jena 3.1.0 (Apache-2.0), Tomcat (Apache-2.0).
  - GraphDB: heute `graphdb-runtime` 10.x (Lizenz-URL graphdb.ontotext.com/LICENSE-GraphDB-Free.txt liefert 200).
  - RDF4J 4.3.15: Lizenz prüfen (die Seite sagt EPL, RDF4J nutzt nach meinem Wissen inzwischen BSD-3-Clause) [?].
  - PlantUML steckt im Closed-Source-Plugin. Ob es im Free-Zip enthalten ist, muss das Team prüfen [?].
- Empfehlung: Die Liste aus einem `license-maven-plugin`-Report des Distribution-Builds erzeugen statt von Hand pflegen [?].

---

## 3. Linkprüfung (Hauptseiten, `curl -sI -L -m 10`)

Geprüft: 85 eindeutige externe URLs aus 30 Seiten sowie die Links in den Success Stories. Hier nur die Auffälligkeiten; alle repo.denkbares.com-Downloadlinks, GitHub-, Lizenz- und YouTube-Videolinks liefern 200.

| Status | URL | Seite(n) | Vorschlag |
|---|---|---|---|
| 404 | `https://repo.denkbares.com/webapp/#/artifacts/browse/tree/General/releases-public` | LeftMenu | `https://repo.denkbares.com/list/repo-public/` |
| tot (DNS/Timeout) | `isci.informatik.uni-wuerzburg.de/hudson/…` und `/javadoc/…` | Testing-App und 8 How-Tos | entfernen bzw. Javadoc im Zip / GitHub |
| tot | `http://d3webwiki.informatik.uni-wuerzburg.de/…` | Developer FAQ, How-To CreatePlugin | entfernen |
| 404 | `http://doc.jspwiki.org/2.4/wiki/InstallingJSPWiki` | change user rights | jspwiki-wiki.apache.org |
| 404 | `http://www.jspwiki.org/wiki/JSPWikiDocumentation` | Doc Overview | jspwiki-wiki.apache.org |
| 404 | `http://www.jspwiki.org/wiki/BugProblemsWithGermanUmlaute` | Developer FAQ, How-To CreatePlugin | entfernen (Tomcat/JSPWiki laufen heute in UTF-8) |
| 403 | `https://mvnrepository.com/artifact/com.ontotext.graphdb/graphdb-free-runtime` | Downloads | 403 kommt wohl vom Bot-Schutz, das Artefakt heißt aber inzwischen `graphdb-runtime` |
| Redirect | `https://www.ontotext.com/products/graphdb/` → graphwise.ai | Downloads | aktualisieren |
| veraltet (200) | `adoptopenjdk.net/archive.html?…openjdk14/17` (11 Zeilen) | Downloads | adoptium.net/temurin/releases/?version=… |
| veraltet (200) | `oracle.com/technetwork/java/javaee/downloads` → Java EE 8 SDK | Developer FAQ | entfernen |
| verwaist (200) | `sourceforge.net/p/d3web/discussion/`, `sourceforge.net/projects/knowwe/forums`, `knowwe.sourceforge.net`, `d3web.sourceforge.net` | Downloads, Getting Started, Developer FAQ, Doc Overview | GitHub |
| tot | `http://www.vo-fab.at/`, `http://wimvent.org` | Success Stories | entfernen |
| 403 | `http://guideline.gov/` (Dienst 2018 eingestellt) | SmartCare | entfernen |
| kaputter Redirect | `http://www.ehealth20xx.at/eHealth2012/…` | ESAT | entfernen |
| Markup-Fehler | `http://www.fsf.org.`, `http://www.java.com)` (Satzzeichen landet in der URL) | Developer FAQ, Testing-App | korrigieren |

**Interne Links auf fehlende Seiten** (Skript über alle Hauptseiten): `Doc Status`, `Doc Tags`, `Doc Timechart` (Doc Overview); `[codetemplates.xml]` (Developer FAQ, gemeint ist der Anhang). Alle anderen internen Links der Hauptseiten zeigen auf vorhandene Seiten [V].

---

## 4. Rechtliche Seiten (nur Hinweise, keine Ersatztexte)

**Imprint**
- Rechtsgrundlage "§5 TMG und §55 RStV": Das TMG wurde am 14.05.2024 durch das **DDG** ersetzt (§ 5 DDG), der RStV am 07.11.2020 durch den **MStV** (§ 18 MStV) [V].
- **Verantwortlicher: Lehrstuhl für Informatik VI, Uni Würzburg** mit Telefon/Fax 888-6731/-6732, Aufsichtsbehörde Bayerisches Staatsministerium, USt-ID DE 134187690. Technisch betreibt aber offenbar denkbares die Site (Hosting, Mail, Analytics, Downloads, Entwicklung) [V]. Wer heute Anbieter ist, muss geklärt und das Impressum entsprechend angepasst werden [?]. Ob Lehrstuhl-Name und Telefonnummern noch stimmen, habe ich nicht geprüft.
- Der Haftungsausschluss "Haftung für Links" mit Verweis auf LG Hamburg 1998 gilt allgemein als überholte Floskel. Juristisch prüfen lassen [?].

**Contact**
- Nennt dieselbe Uni-Adresse. "info <at> d3web.de" landet laut MX bei denkbares [V]. GitHub-Discussions-Box: Links funktionieren, die Discussions sind aber inaktiv (siehe oben).

**Contact and Imprint** (nicht im Menü, aber öffentlich erreichbar)
- Alte denkbares-Daten: **Friedrich-Bergius-Ring 15** (einmal 97074, einmal 97076), alte Telefonnummern +49 931 20 53 90 11 / 870 96 10 03, als Geschäftsführer nur Volker Belli, Registernummer als "RHB10160" (Tippfehler) [V]. Aktuell laut denkbares.com: John-Skilton-Straße 8, 97074 Würzburg; +49 931 900050-00; vertreten durch Dr. Joachim Baumeister und Volker Belli; HRB10160 [V]. Die Seite löschen oder als Weiterleitung auf das gültige Impressum umbauen [?].
- Die Personenseiten (`Albrecht Striffler`, `Alex Legler`, `Joachim Baumeister`, `Joba Baumeister`, `Volker Belli` …) nennen ebenfalls die alte Adresse. Zum Teil sind sie öffentlich, zum Teil "ALLOW view Authenticated". Ob die Personenangaben noch stimmen, muss das Team prüfen [?].

**Privacy Statement**
- Stand 2015, Quelle "muster-vorlagen.net" [V]. Beruft sich auf "German data privacy laws"/"Federal Data Protection Act" (altes BDSG). **Die DSGVO wird nicht erwähnt.** Es fehlen unter anderem Verantwortlicher mit Kontaktdaten, Rechtsgrundlagen, Betroffenenrechte nach Art. 15–21 DSGVO, Beschwerderecht, Hosting und Server-Logs.
- **Matomo (stats.denkbares.com) wird nicht erwähnt**, obwohl es auf jeder Seite eingebunden ist [V]. Positiv: Das Tracking ist cookielos konfiguriert (`disableCookies`), und es gibt keine Google-Analytics- oder Google-Fonts-Einbindung [V]. Der Cookie-Abschnitt ("long-term cookies … recognition on your next visit") passt nicht zur Realität: Es wird nur das Session-Cookie JSESSIONID gesetzt (anonym, ohne Login gemessen) [V].
- Der Abschnitt "Inventory data … goods to be delivered" ist Shop-Mustertext und passt nicht zur Site.
- Empfehlung: Datenschutzerklärung durch den Anbieter (siehe Imprint) neu erstellen lassen, ggf. auf Basis der denkbares-Datenschutzerklärung (denkbares.com → "Datenschutz") [?].

---

## 5. Markup und Rendering

| Befund | Seite(n) | Status |
|---|---|---|
| Fehlerausgabe "No ontology found! … Internal error while rendering section: null" (alte `def`/`partOf::`-Syntax, nicht mehr existierende `KnowWE-Plugin-RDFS-*`) | Module TimeDB, Module CostBenefit, Module Rdf2Go, Module DiaFlux | live geprüft [V] |
| Öffentlich gerenderte `%%Todo`-Blöcke | Tutorial, InstalledTagHandlers, How To Use SectionStore, How-To Secure…, Personenseiten (`%%TodoList`) | live geprüft (Tutorial, InstalledTagHandlers) [V] |
| `[{KnowWEPlugin …}]`-TagHandler ohne Implementierung im Code: `describemodule`, `listinstalledModules`, `objectInfo`, `parseAllButton`, `showAllKBs`, `testsuite`, `TypeBrowser` | ModuleInfo, InstalledModules, ObjectInfoPage, Admin, KBIndex, Doc TestSuiteDemo, Test3 u. a. (nicht im Hauptmenü) | Code-grep [V]; `ObjectInfoTagHandler` existiert, der Tag-Name wäre zu prüfen. `renamingtool` gibt es nur noch in Experimental. |
| Weiter vorhandene TagHandler | `quickInterview`, `renderKDOM`, `tagcloud`, `tagsearch`, `version`, `dialog`, `renderKnowledge`, `ExpressionDoc` (TimeDB) | [V] |
| `%%Timeline` ohne Markup-Implementierung | Doc Timeline, 2 weitere Seiten | Code-grep [V] |
| Markup-Syntaxfehler | Demos (`‘‘` statt `''`), Workspace-Seite (`…/m2}`), How-To Secure (verschachteltes `%%prettify`), Testing-App (abgebrochener Licence-Abschnitt) | [V] |
| Standard-JSPWiki-Plugins `Image`, `TableOfContents`, `ALLOW`, `If`, `%%collapsebox-closed`, `%%information`, `%%prettify`, `%%quote` | überall | rendern live korrekt [V] |
| Live-Version | Footer "JSPWiki v2.11.0-git-12", Build "KnowWE 20220509_17:48" | [V]; nach einem Upgrade auf den aktuellen Stand erneut prüfen [?] |
| Öffentliche Test- und Sandbox-Seiten | `Test` (geändert 26.11.2025, erscheint in RecentChanges), `Test123`, `Test1234`, `Test12345`, `Test3`, `Tester`, `Tester2`, `Sandbox*`, `Deleteme`, `Pizza*`, `UITest-*`, `Rename`, `NurInterview`, `LeftMenuOld`, `ProductsTemp` | [V]; aufräumen [?] |

---

## 6. Priorisierte Änderungsvorschläge

### P1: sofort (falsche Fakten, Sicherheit, Recht)

1. **Downloads, Box "Attention" und Tabelle** [V]. Vorschlag:
   > *To run d3web KnowWE you need a JDK (Java Development Kit) and the environment variable JAVA_HOME pointing to it. Required Java version: **JDK 25** for the nightly build and releases 2026.6 and later, **JDK 21** for 2026.5, **JDK 17** for 12.9 – 2026.4, JDK 14 for older 12.x releases.*

   In der Tabelle: "last night | JDK 25 | d3web KnowWE LATEST". JDK-Links auf `https://adoptium.net/temurin/releases/?version=25` bzw. `?version=17`. Beschriftungen "v2026.4.1" und "v2026.3.1". [?] Fehlende Zips für 2026.6/2026.7 nachreichen oder bewusst weglassen.
2. **Default-Admin-Konto entfernen** [V]: den FAQ-Eintrag "How can I get Admin rights" (User FAQ), die Seite `How To AdminRights` und die Anhänge `userdatabase.xml`/`groupdatabase.xml` an beiden Seiten, plus die Verweise in `How-To Secure…` und `Doc FAQ`. Ersatz: Verweis auf die JSPWiki-Doku zur Benutzerverwaltung [?]. Zusätzlich prüfen, ob dieses Konto auf www.d3web.de oder anderen Instanzen aktiv ist [?].
3. **Impressum und Datenschutz** [V/?]: Anbieter klären (Uni oder denkbares), Rechtsgrundlagen auf DDG/MStV umstellen, Datenschutzerklärung DSGVO-konform neu fassen inklusive Matomo (cookielos, eigener Server) und Hosting. Die Seite `Contact and Imprint` mit den alten denkbares-Daten löschen oder korrigieren.
4. **Main und Products: eingestellte Produkte entfernen** [V]. Vorschlag für "Platform Overview" auf Main:
   > * **[KnowWE]** – Knowledge Wiki Environment: develop, test and maintain d3web knowledge bases and ontologies collaboratively in the browser.
   > * **[d3web-Runtime|D3web-Core]** – the open-source reasoning engine (Java library) to embed and run d3web knowledge bases in your own applications.
   > * Sources on [GitHub|https://github.com/denkbares], latest release on the [Downloads] page.

   Auf Products ProKEt, WUMPS, Droid3web-App und Mobile-App entfernen oder unter "Discontinued" verschieben. Die Lizenzangabe bei KnowWE-Headless-App korrigieren [?]. Den Abschnitt "Modules (under construction)" entfernen, weil er Renderfehler erzeugt.
5. **Build-Anleitung erneuern** (Getting Started bzw. How-To Create Your Own Workspace). Vorschlag aus dem Code abgeleitet [V]; einen kalten Testlauf ohne denkbares-Zugang habe ich nicht gemacht [?]:
   > **Prerequisites:** JDK 25 (e.g. Eclipse Temurin or Azul Zulu), Apache Maven 3.9+, Git, Apache Tomcat 9 (Servlet 4 / `javax.servlet`; Tomcat 10+ is not supported).
   > ```
   > git clone https://github.com/denkbares/Commons.git
   > git clone https://github.com/denkbares/d3web-Runtime.git
   > git clone https://github.com/denkbares/d3web-KnowWE.git
   > (cd Commons && mvn install -DskipTests)
   > (cd d3web-Runtime && mvn install -DskipTests)
   > (cd d3web-KnowWE && mvn install -DskipTests -P core-plugins)
   > ```
   > The web application is created as `d3web-KnowWE/KnowWE-App/target/KnowWE.war`. The profile `core-plugins` builds KnowWE with all open-source plugins; the default profile additionally requires denkbares' commercial plugins. Dependencies, including the KnowWE fork of JSPWiki (`org.apache.jspwiki:jspwiki-knowwe-*`, sources at https://github.com/denkbares/jspwiki), are resolved from `https://repo.denkbares.com/repo-public/`.
   > **Configuration:** copy `KnowWE-App/src/resources/local/WEB-INF/classes/jspwiki-custom.properties.default` to `jspwiki-custom.properties` and set `var.basedir` to your wiki content folder. Deploy `KnowWE.war` (or the exploded WAR from your IDE) to Tomcat with context path `/KnowWE`.

   Den Eclipse-Abschnitt entfernen oder als "community contribution, 2022, outdated" kennzeichnen.

### P2: zeitnah (veraltete Inhalte, kaputte Links und Rendering)

6. **LeftMenu**: "Repository" auf `https://repo.denkbares.com/list/repo-public/` setzen. "Installed plugins" entfernen. Über "Forum" und "YouTube" entscheiden [?].
7. **Systemanforderungen** auf KnowWE, D3web-Core und D3web-KnowWE Quickstart vereinheitlichen [V]. Vorschlag:
   > *KnowWE runs on Windows, macOS and Linux and requires a JDK (version see [Downloads]). It is a Java web application for Apache Tomcat 9. Supported browsers: current versions of Chrome/Chromium, Edge, Firefox and Safari.*

   Browser-Support muss das Team bestätigen [?]. "chmod 777" ersetzen. Die Beschreibung des Zip-Aufbaus nach Prüfung des aktuellen Zips anpassen [?].
8. **KnowWE-Seite**: Den Text "Manchester syntax" streichen und die Beschreibung an `KnowWE/README.md` angleichen [V]. Link auf GitHub und auf den JSPWiki-Fork ergänzen.
9. **User FAQ**: Java 8 → siehe Downloads. Haddock-Eintrag löschen. "Changes … need to be pushed back" korrigieren [?]. Rolle der Uni aktualisieren [?]. Antwort zur Stand-alone-Runtime ohne Mobile/Android formulieren.
10. **Installations-Unterseiten**: In "change user rights" die Policy-Beispiele auf `org.apache.wiki.auth.*` umstellen oder auf die mitgelieferte `WEB-INF/jspwiki.policy` (plus Varianten `jspwiki-readonly.policy`, `jspwiki-authonly.policy`) verweisen [V]. In "change wikicontent location" `jspwiki-custom.properties` statt `jspwiki.properties` nennen. In "host multiple wikis" `-XX:MaxPermSize` streichen und klären, ob die Seite noch gilt [?]. In How-To Secure ebenso.
11. **Doc Overview**: die Links `Doc Status`, `Doc Tags`, `Doc Timechart` entfernen oder die Seiten anlegen (Tags und TimeChart gibt es). Prüfen, ob es Timeline noch gibt [?]. Die SourceForge-"Further Links" durch GitHub ersetzen. Den JSPWiki-Doku-Link reparieren.
12. **Öffentliche Todos und Testseiten entfernen**: Tutorial-TODO, InstalledTagHandlers, Test-, Sandbox- und Pizza-Seiten, How To Use SectionStore.
13. **Developer FAQ / How-To CreatePlugin / How-Tos**: SVN-, Eclipse- und isci-Inhalte entfernen. Den Lizenz-Header auf die aktuelle Form umstellen. Welche Form maßgeblich ist, muss das Team festlegen (lang mit "denkbares GmbH, Germany" oder SPDX) [?]. Die How-Tos mit `SubtreeHandler`, `SectionStore` und `de.d3web.we.*` löschen oder als Legacy kennzeichnen.
14. **Licenses d3web KnowWE**: die Liste an die aktuellen Abhängigkeiten anpassen (logback, Lucene, Jackson, Gson, Guava, Jena, Tomcat, GraphDB 10), am besten generiert [?].
15. **Testing-App / Mobile-App / Droid3web-App / KnowledgeBaseBook / KnowWE-Headless-App**: Status je Produkt festlegen (aktiv, auf Anfrage oder eingestellt) [?]. Droid3web ist laut Code eingestellt [V].

### P3: bei Gelegenheit

16. **History** um 2016–2026 ergänzen (belegbare Meilensteine in Abschnitt 2). Die Zeile zu IISY/Thomas Unglert prüfen [?].
17. **Success Stories** überarbeiten: tote Links entfernen, abgeschlossene Projekte als solche kennzeichnen, aktuelle Referenzen ergänzen [?].
18. **API Documentation**: `com.denkbares.*` ergänzen, GitHub-Links setzen, prüfen, ob Javadoc noch im Zip steckt [?].
19. **Demos**: Markup-Fehler (`‘‘`) beheben, Hinweis "requires commercial TimeDB" bei Temperature [?].
20. Tippfehler bereinigen (siehe die einzelnen Seiten), LGPL-Links auf gnu.org vereinheitlichen.
21. **Live-Instanz** auf einen aktuellen KnowWE-Stand heben (derzeit ein Build vom 09.05.2022 mit JSPWiki 2.11.0) und danach alle Hauptseiten erneut auf Renderfehler prüfen [?].

---

## 7. Offene Fragen ans Team

1. Wer ist heute Anbieter von d3web.de (Uni Würzburg Lehrstuhl VI oder denkbares GmbH)? Davon hängen Impressum, Kontakt und Datenschutz ab.
2. Sind die Maven-Releases 2026.6 und 2026.7 ohne Distribution-Zip gewollt? Welche Releases sollen auf Downloads stehen?
3. Welche Produkte gelten noch: Testing-App, Mobile-App, KnowledgeBaseBook, KnowWE-Headless-App (und unter welcher Lizenz)? Kommerzielle Erweiterungen weiter auf d3web.de bewerben?
4. Welche Success Stories sollen bleiben? Gibt es neue, freigegebene Referenzen? Darf KnowSEC mit aktuellen Zahlen erscheinen?
5. Community-Kanäle: GitHub Discussions (seit 2023 still), YouTube-Kanal, SourceForge-Projekte (archivieren oder Hinweis auf GitHub)?
6. Wie ist der aktuelle Aufbau des Distribution-Zips (KnowWE-Tomcat.zip? Javadoc? Quellen? GraphDB/PlantUML enthalten)? Die Assembly liegt nicht in den Repos.
7. Unterstützte Browser und Betriebssysteme für die Systemanforderungen?
8. Ist das veröffentlichte Default-Admin-Konto irgendwo produktiv aktiv?
9. Welcher Lizenz-Header gilt künftig für Beiträge? (Nebenbefund [V]: In den Open-Source-Repos beginnen 181 Java-Dateien in KnowWE und 25 in d3web mit "Copyright (C) … denkbares GmbH. All rights reserved." statt mit LGPL-Header, z. B. `de/knowwe/core/Environment.java`. Das passt nicht zur Aussage "all open-source parts are LGPL" auf der Website und sollte separat geklärt werden.)
