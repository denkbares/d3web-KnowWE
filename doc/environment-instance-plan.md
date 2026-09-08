# KnowWE `Environment`: vom statischen Singleton zur Instanz

Plan, Stand, offene Punkte und Protokoll des Umbaus. Arbeitsumgebung `Projects/EnvInstance`, Branch `env-instance` in allen
beteiligten Repos (Liste unter "Arbeitsumgebung"). Stand: 2026-09-07.

## Ziel

Jeder Test (und jede Anwendung) erzeugt seine Environment mit `new Environment(connector)` bzw. `Environment.create(...)`
und wirft sie weg oder schließt sie mit `close()`. Danach ist nichts von ihr übrig: kein statischer Zustand, keine Threads,
keine Listener. Ein zweites `Environment.initInstance()` im selben JVM erbt nichts. `Environment.getInstance()` verschwindet
vollständig; wer die Environment braucht, holt sie sich von dem Objekt, das er ohnehin in der Hand hat (`Section`,
`Article`, `UserContext`, `Compiler`, `ArticleManager`) oder bekommt sie beim Start übergeben.

Ergänzung vom 2026-09-07 nach Rücksprache: Ein expliziter Reset statischer Registries (wie in Stufe 1 gebaut) ist nur ein
Provisorium. Er verlangt Disziplin und löst das Problem nicht. Ziel ist Besitz: aller Zustand hängt an der Environment.

## Befund (Kurzfassung)

- `Environment` ist intern bereits eine Instanz. Statisch sind nur `getInstance()`, `initInstance()`, `isInitialized()`,
  `retrieveLatestVersionNumber()` und die Konstante `DEFAULT_WEB`.
- ~880 direkte `getInstance()`-Aufrufe in ~520 Dateien (KnowWE 235, KnowWE-DES 272, Experimental 145, KRONE 127,
  KnowWE-SSP 87, denkbares-Internals 17, Information-Extraction 1) plus 9 JSPs.
- Klassifikation der Aufrufstellen nach vorhandenem Kontext (Skript-Auswertung 2026-09-07):

  | Verfügbarer Kontext | Aufrufe |
  |---|---|
  | `UserContext` als Parameter (Actions, Renderer, TagHandler) | 226 |
  | `Section` als Parameter oder Feld | 228 |
  | `Article`, `ArticleManager`, `Compiler` als Parameter oder Feld | 106 |
  | Tests | 128 |
  | Instanzmethode ohne Kontext | 84 |
  | Statische Methode ohne Kontext | 70 |
  | Feld-Initialisierer, statische Blöcke | 28 |

  Drei Viertel haben also schon ein Objekt, aus dem die Environment folgt.
- Der `web`-Parameter ist tot, es gibt nur `DEFAULT_WEB`.
- Statischer Zustand außerhalb von `Environment` (Stand vor Stufe 2): `Section.sectionMap`, `CompilerManager.scriptManagers`,
  `SectionizerModule.Registry`, `Messages.sectionsWithMessages`, `TerminologyManager.occupiedTerms`, `ToolUtils.BLOCKED_PROVIDERS`,
  `Plugins.registeredRightPanelTabs`, `NotificationManager.globalNotifications`, `LongOperationUtils` (Pool + Map),
  `CompilationLocal`-Caches, `Scope`-Caches, `AttachmentUpdateMarkup`-Statics, Singleton-Typen (`PlainText`, `LineContent`,
  `DashTreeElementContent` + 21 in Plugins), Plugin-Singletons aus dem `PluginManager` (Compiler, EventListener, TagHandler,
  Instantiations), Commons-Singletons `EventManager`/`PluginManager`, JUL/MDC.
- Threads: gut 60 Erzeugungsstellen in den Repos (18 in KnowWE). Aufgeräumt werden nur 10 davon, über
  `ServletContextEventListener.registerOnContextDestroyedTask`, und das nur beim Servlet-Shutdown, in Tests nie.

### Statische Zustände im Detail (Stand vor Stufe 2)

Diese Registries liegen außerhalb von `Environment` und setzen "eine Environment pro JVM" voraus:

| Ort | Zustand | Wird geleert bei |
|---|---|---|
| `Section.sectionMap` | globale Section-ID-Registry | nur Servlet-Context-Destroy, nicht bei `shutdown()` |
| `CompilerManager.scriptManagers` | ScriptManager pro Compiler-Klasse | `DeInitEvent` |
| `SectionizerModule.Registry` | statische Liste | nie |
| `Messages.sectionsWithMessages` | Map Type -> Sections | nie |
| `TerminologyManager.occupiedTerms` | Set | nie |
| `ToolUtils.BLOCKED_PROVIDERS` | Map | nie |
| `Plugins.registeredRightPanelTabs` | Liste | eigener EventListener |
| `NotificationManager.globalNotifications`, `LongOperationUtils`, `Priority.registeredPrioritiesHash`, `ServletContextEventListener`, `CredentialProviders`, `WikiAttachments.TEMP_FOLDER` | diverse | nie / teilweise |
| Typbaum | `AbstractType.checkInitializationStatus()` asserted `!Environment.isInitialized()`; geplugte Typen kommen per `getNewInstance()` (gut), `PlainText`/`LineContent`/`DashTreeElementContent` sind statische Singletons und werden bei Re-Init erneut dekoriert | nie |
| Commons | `EventManager.getInstance()`, `PluginManager.getInstance()` | nie (bleiben global) |
| JVM | `java.util.logging.LogManager`-Konfiguration, MDC | nie |

Deshalb funktioniert das heutige Muster in Tests (`shutdown()` + erneutes `initInstance()`, z.B. KRONE, CBX, TaskManagement) nur "meistens".

## Plan

| Stufe | Inhalt | Umfang | Status |
|---|---|---|---|
| 1 | Rückverbindung `ArticleManager.getEnvironment()`, `getInstance()` `@Deprecated`, `shutdown()` mit explizitem Reset (Provisorium, entfällt mit Stufe 2) | KnowWE-core | **erledigt** (9e19e1b30) |
| 2 | **Environment besitzt allen Zustand und alle Threads.** `Environment implements AutoCloseable`; `close()` statt `shutdown()`. Reset-Liste aus Stufe 1 ist leer | KnowWE-core (+ Commons für 2e) | **2a-2d erledigt** (56b2b2b63, Folgecommit), 2e offen |
| 3 | Aufrufer migrieren nach vier Mustern (s.u.), repo-weise: KnowWE, KnowWE-DES, KnowWE-SSP, Experimental, KRONE, Internals, IE | alle Consumer | Muster 1 in allen Repos erledigt (2026-09-08), Muster 2-4 offen |
| 4 | `getInstance()`, `initInstance()`, `DEFAULT_WEB`, `articleManagers`-Map löschen; Einstieg nur noch über den JSPWiki-Connector, der die Environment erzeugt und in den ServletContext legt; Testinfrastruktur auf `new Environment(...)` pro Test | alle, plus Kompilier-Check GINA, KMW, KnowledgeConverter, d3web-Player-Platform, SemanticServicePlatform | offen |

### Stufe 2 im Detail

- **2a Registries in die Instanz.** Section-ID-Registry, ScriptManager, SectionizerModule-Registry, Messages-Index,
  occupiedTerms, blockierte ToolProvider, globale Notifications, LongOperations, AttachmentUpdate-Zustand werden Felder
  der Environment (bzw. des ArticleManagers/CompilerManagers, wo sie hingehören). Caches, die nur an Typ- oder
  Compiler-Instanzen hängen (`Scope`, `CompilationLocal`), werden schwach referenziert oder wandern an den CompilerManager.
  Statische Zugriffe bleiben als Delegation über den Kontext (Section -> ArticleManager -> Environment) bzw. übergangsweise
  über `getInstance()` erhalten.
- **2b Environment-scoped Services + `close()`.** `environment.getService(Class, Supplier)` als Ablage für alles, was
  Plugins heute statisch halten; Services, die `AutoCloseable` sind, werden beim `close()` geschlossen. `onClose(Runnable)`
  für Aufräumarbeiten. `close()` = laufende Kompilierung abwarten, `DeInitEvent`, `onClose`-Tasks rückwärts, Services
  schließen, Executor hart stoppen und Rest loggen. `ServletContextEventListener.contextDestroyed` ruft nur noch `close()`.
  Die 10 Nutzer von `registerOnContextDestroyedTask` wandern auf `onClose`/Services. JUnit-Rule/Extension, die eine
  Environment aufbaut und im `after` schließt.
- **2c Singleton-Typen.** `PlainText`, `LineContent`, `DashTreeElementContent` werden normale Typen (`new`), `getInstance()`
  entfällt (10/2/1 Aufrufer, keine Identitätsvergleiche). Die 21 Plugin-Singletons folgen in Stufe 3 nach demselben Muster
  oder hängen ihren Zustand an `getService`.
- **2d Plugin-Singletons pro Environment.** Compiler aus dem Extension-Point per `getNewInstance()`, die Environment merkt
  sich ihre globalen Compiler (`isGlobalCompiler`). EventListener, die die Environment beim Start registriert, werden beim
  `close()` wieder abgemeldet (`EventManager.unregister`).
- **2e EventManager pro Environment (Commons).** Offen. Erst nötig für zwei gleichzeitig laufende Environments; für
  "alte weg, neue sauber" reicht 2d.

### Stufe 3: die vier Migrationsmuster

1. **Kontext vorhanden** (rund 560 Stellen): `Environment.getInstance()` -> `section.getArticleManager().getEnvironment()`,
   `context.getArticleManager().getEnvironment()`, `compiler.getCompilerManager().getArticleManager().getEnvironment()`.
   Meist reicht der `ArticleManager` selbst, die Environment ist nur für den `WikiConnector` nötig. Structural Replace.
2. **Statische Utilities ohne Parameter** (`KnowWEUtils.getApplicationRootPath()`, `getConfigBundle()`, `Compilers.getCompilerManager(web)`,
   `KnowWEUtils.getArticleManager(web)`): Parameter einführen (`ArticleManager` oder `Environment`), Aufrufer nachziehen.
3. **Hintergrund-Tätigkeiten** (CI-Builds, Attachment-Updates, Git-Scheduler, Task-Manager, Timer): Environment beim
   Start einfangen (Konstruktor/Feld), nicht zur Laufzeit holen. `Instantiation.init(String web)` -> `init(Environment)`.
4. **JSPs** (9 Dateien): Environment aus dem ServletContext-Attribut, das der JSPWiki-Connector beim Start setzt.

Reihenfolge: KnowWE -> KnowWE-DES -> KnowWE-SSP -> Experimental -> KRONE -> Internals/IE. Die Repos müssen gleichzeitig
kompilieren; fehlende Cross-Repo-Artefakte kommen als Snapshots von repo.denkbares.com.

## Stand

- [x] Analyse und Plan (2026-09-07)
- [x] Arbeitsumgebung: Host-Share `EnvInstance` gemountet, 7 Repos geklont, Branch `env-instance`
- [x] Isoliertes Maven-Repo `~/.m2/env-instance-repo`, Kette gebaut (42/43 KnowWE-Module, ohne `KnowWE-App`)
- [x] Baseline: KnowWE-core-Tests grün (11 Klassen, 67 Tests, 0 Fehler, 12 übersprungen)
- [x] Plan-Dokument in KnowWE-core committet (18730a4d6)
- [x] Stufe 1 (2026-09-07, Commit 9e19e1b30 auf env-instance, nicht gepusht)
  - `ArticleManager.getEnvironment()` + `DefaultArticleManager(Environment, String)`; alter Konstruktor deprecated (Experimental nutzt ihn noch in DatedRevision/UploadedRevision)
  - `Environment.getInstance()` deprecated; `shutdown()` wartet auf Kompilierung, feuert `DeInitEvent` bei gesetzter Instanz, ruft danach die neuen Reset-Methoden: `Section.clearSectionRegistry`, `CompilerManager.clearScriptManagers`, `SectionizerModule.Registry.clear`, `Messages.clearSectionsWithMessages`, `TerminologyManager.clearOccupiedTerms`, `ToolUtils.clearBlockedToolProviders`, `NotificationManager.clearGlobalNotifications`, `LongOperationUtils.shutdown`, `CompilationLocal.clearCaches`, `Scope.clearCaches`, `AttachmentUpdateMarkup.reset`, `PlainText/LineContent.resetInstance`, `DashTreeElementContent.resetDefaultInstance`
  - Nicht angefasst (bewusst): `Plugins.registeredRightPanelTabs` (eigener DeInit-Listener), `ServletContextEventListener`-Tasks (Servlet-Lebenszyklus), `Priority`, `CredentialProviders`, `WikiAttachments.TEMP_FOLDER`, Commons-Singletons `EventManager`/`PluginManager`, JUL/MDC
  - `EnvironmentLifecycleTest` in KnowWE-Plugin-Core (dafür GlobalTestUtils + ExtensionPoints als Test-Deps und maven-dependency-plugin für `target/dependencies/output.txt`)
  - Voller KnowWE-Build mit Tests (87 Module): grün bis auf Git-Provider-Tests in JSPWiki-Connector, siehe "Bekannte Baseline-Fehler"
- [ ] Stufe 2 (2a-2d erledigt, 2e offen)
  - [x] 2a Registries in die Instanz (2026-09-08): `SectionRegistry` pro Environment (`Section.get(id)` delegiert übergangsweise an die aktuelle), ScriptManager pro Environment (Compile-Scripts gehören jetzt dem `AbstractType` und werden beim Dekorieren des Typbaums pro Environment indexiert, `registerCompileScripts`), Sectionizer-Module, `occupiedTerms`, Messages-Index, blockierte ToolProvider, globale Notifications, RightPanel-Tabs als Environment-Felder bzw. Services; `CompilationLocal`-Caches am `CompilerManager`; `Scope`-Typcache schwach referenziert
  - [x] 2b Services + `close()` (2026-09-08): `Environment implements AutoCloseable`, `getService(Class, Supplier)`, `onClose(Runnable)`; `close()` = Kompilierung abwarten, `DeInitEvent`, CompilerManager/AttachmentManager stoppen, Close-Tasks rückwärts, Services schließen. `LongOperations` und `AttachmentUpdates` sind Services mit eigenem Thread-Pool/Timer. `ServletContextEventListener.contextDestroyed` schließt die Environment. `shutdown()` ist deprecated und delegiert. Neue Helfer `KnowWEUtils.findEnvironment/getEnvironment(Section|ArticleManager|Compiler|UserContext)`. Transitional: `Environment.peekInstance()`/`requireInstance()` (deprecated) für die verbliebenen kontextlosen statischen Aufrufer
  - [x] 2c Singleton-Typen (2026-09-08): `PlainText.getInstance()`, `LineContent.getInstance()`, `DashTreeElementContent.getDefaultInstance()` liefern jetzt eine Instanz pro Environment (über `getService`), ohne Environment eine frische. Kein Reset mehr in `close()`; die Reset-Liste aus Stufe 1 ist leer. Die 21 Singleton-Typen in den Plugins folgen in Stufe 3 nach demselben Muster.
  - [x] 2d Plugin-Singletons pro Environment (2026-09-08): `Plugins.getCompilers()` instanziiert die geplugten Compiler pro Aufruf (`getNewInstance`), die Environment merkt sich ihre (`isGlobalCompiler`). Die von der Environment registrierten Plugin-EventListener werden beim `close()` wieder abgemeldet. TagHandler, Instantiations, SectionizerModule bleiben Plugin-Singletons (zustandslos bzw. Zustand über `getService`).
  - [ ] 2e EventManager pro Environment (Commons, offen)
- [x] Consumer-Repos geklont (2026-09-07): Experimental, KnowWE-DES, KnowWE-SSP, KRONE (enthält KnowWE-KONAP-Integration), Information-Extraction. Zusammen mit denkbares-Internals sind das alle Repos mit `Environment.getInstance()`-Aufrufern.
- [ ] Nur für Stufe 4 relevant (referenzieren KnowWE-Artefakte, aber ohne `getInstance()`-Aufrufe): GINA, KMW, KnowledgeConverter, d3web-Player-Platform, SemanticServicePlatform. Vor dem Entfernen von APIs klonen und kompilieren.

## Offene Punkte / Beobachtungen

- 21 weitere Singleton-Typen (`private static X instance`) in den Plugins werden bei Re-Init nicht zurückgesetzt. Für Stufe 2 prüfen, ob ein generischer Mechanismus (z.B. Reset über `Plugins`/Typ-Registry) sinnvoller ist als Einzelfälle.
- `ArticleLifecycleFixture` (KnowWE-core-Tests) baut die Environment per Reflection; mit einem Test-Konstruktor/Factory ließe sich das sauber machen (Kandidat für Stufe 2/4).
- `getInstance()`-Deprecation erzeugt jetzt ~885 Warnungen über alle Repos. Bewusst so, als Migrationssignal; bei Bedarf mit einer Zeile zurücknehmbar.

## Bekannte Baseline-Fehler (unabhängig vom Umbau)

- KnowWE-DES (Testlauf 2026-09-08 nach Stufe 3, Muster 1): 9 Klassen `de.knowwe.versioning.git.*` (Git-Status/HEAD auf leerem Repo, deutsche git-Meldungen), `GeneratorAppTest` ("Cannot run program"), `TaskContextManagerPersistenceTest` (ein zweiter, über `TaskContextManagerInstance.get()` von einem Listener erzeugter BasicTaskContextManager schreibt dieselbe wc-Datei; fällt auch ohne die initInstance-Änderung, Baseline nicht verifizierbar ohne master-Build). KnowWE-SSP und Experimental: grün. KRONE und Information-Extraction: nur kompiliert.

- `GitPageProviderTest`, `GitWikiRepositoryTest`, `GitAttachmentProviderTest` (JSPWiki-Connector) brauchen eine Git-Identität. In der VM gibt es keine globale, daher 11 Fehler. Mit `GIT_AUTHOR_NAME/EMAIL` und `GIT_COMMITTER_NAME/EMAIL` als Umgebungsvariablen bleiben 2 Fehler (`sweepUp...`, "dirty tree must be reconciled"), auch mit `LC_ALL=C`. Betrifft das Git-Status-Parsing des GitConnectors aus denkbares-Internals, nicht die Environment. Nicht weiter verfolgt.

## Arbeitsumgebung

- Klone: `Projects/EnvInstance/<Repo>`, `origin` = git.denkbares.com, Remote `main-local` = `Projects/Main/<Repo>`.
  Repos: Maven-Parents, denkbares-Commons, d3web, d3web-DES, denkbares-Internals, jspwiki, KnowWE, Experimental, KnowWE-DES, KnowWE-SSP, KRONE, Information-Extraction.
- Cross-Repo-Abhängigkeiten der Consumer sind zyklisch auf Property-Ebene (KnowWE-DES <-> KnowWE-SSP <-> Experimental <-> KRONE). Was lokal nicht gebaut ist, zieht Maven als Snapshot von repo.denkbares.com; die Reihenfolge in `build-chain.sh` ist daher nur eine Näherung. coom, GINA, d3web-Player-Platform kommen komplett von dort.
  master nachziehen: `git fetch main-local master && git rebase main-local/master`. Push nur vom Host, nie ungefragt.
- Build: `./build-chain.sh [ab-repo]`, immer `-Dmaven.repo.local=/home/cody/.m2/env-instance-repo`,
  `JAVA_HOME=/usr/lib/jvm/zulu-25-amd64`. Nie ins geteilte `~/.m2` installieren. Kein `mvn clean` am Schluss.
- Besonderheiten der Kette (alle Repos auf 2026.7-SNAPSHOT):
  - Maven-Parents wird nicht gebaut (Workspace-Aggregatoren für die IDE, keine Parent-POMs)
  - denkbares-Internals ohne `denkbares-reaxcel` (braucht pnpm)
  - jspwiki nur `-pl jspwiki-war -am` (`jspwiki-multiwiki` gibt es auf master nicht mehr)
  - KnowWE ohne `KnowWE-App` (WAR braucht Plugins aus KnowWE-DES)
- Git in den Klonen: `core.fileMode=false` und Identität sind lokal gesetzt (vboxsf-Phantomänderungen, globale Identität fehlt).
- Setup-Script: `Projects/setup-env-instance.sh <ziel> [branch]`.

## Protokoll

- 2026-09-07: Analyse, Plan, Arbeitsumgebung eingerichtet, Kette gebaut, Baseline-Tests grün.
- 2026-09-07: Stufe 1 umgesetzt und committet (9e19e1b30). Voller KnowWE-Testlauf 1:53 min, nur die bekannten Git-Provider-Fehler.
- 2026-09-08: `Environment.initInstance()` schließt eine noch laufende Environment (c679ea688); vorher lief `TaskContextManagerEventTest` mit `initialized=true` in die Typbaum-Assertion.
- 2026-09-08: Stufe 3, Muster 1 in allen Repos per Skript (Kontextobjekt aus Methodenparameter oder Klassenfeld). Fallen: lokale Variablen nicht fuer Felder halten (nur Deklarationen mit genau einem Tab), Dateien mit CRLF-Zeilenenden byteweise behandeln (42 Dateien mussten repariert werden), `Environment`-Import nur entfernen, wenn das Wort nirgends mehr vorkommt (mehrzeilige Aufrufe). Kompilierfehler wurden zeilenweise aus git zurueckgesetzt (revert_errors.py), die Stellen bleiben beim statischen Zugriff.
- 2026-09-08: Stufe 2c/2d umgesetzt, voller KnowWE-Testlauf grün bis auf die bekannten Git-Tests. In KnowWE-core bleibt an statischem Zustand nur noch: der Singleton `Environment.instance/initialized` selbst (Stufe 4), zustandslose Singletons (Renderer, Visitor, SectionFinder), Konstanten/Caches ohne Lebenszyklus (`Priority`, `Scope`, `Section.COMPILER_REFS` schwach) und `ServletContextEventListener`-Tasks (Servlet-Lebenszyklus). `ResourceLoader` (JS/CSS-Registrierung, dedupliziert) bleibt bis Stufe 3/4.
- 2026-09-08: Stufe 2a/2b umgesetzt. Reset-Liste in `close()` besteht nur noch aus den drei Singleton-Typen (2c). Voller KnowWE-Testlauf grün bis auf die bekannten Git-Tests. Fallen: `close()` muss die Compiler vor den Services stoppen (Destroy-Scripts schreiben noch Messages); `AbstractType.removeCompileScript` darf beim Typ-Aufbau nicht auf einen noch nicht indexierten Typ zugreifen; `LongOperationUtilsTest` braucht jetzt eine Environment (per `ArticleLifecycleFixture`).
- 2026-09-07: Plan nach Rücksprache umgebaut: Ziel ist Besitz statt Reset (Stufe 2 neu definiert, Migrationsmuster für Stufe 3, `getInstance()` wird komplett entfernt). Consumer-Repos geklont; Experimental, KnowWE-DES, KnowWE-SSP kompilieren gegen Stufe 1. KRONE braucht `-Dskip.installnodenpm -Dskip.npm -Dskip.pnpm` (Symlinks auf vboxsf).
