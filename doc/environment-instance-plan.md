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
| Commons | `EventManager.getInstance()`, `PluginManager.getInstance()` | EventManager seit 2e pro Environment; PluginManager bleibt global |
| JVM | `java.util.logging.LogManager`-Konfiguration, MDC | nie |

Deshalb funktioniert das heutige Muster in Tests (`shutdown()` + erneutes `initInstance()`, z.B. KRONE, CBX, TaskManagement) nur "meistens".

## Plan

| Stufe | Inhalt | Umfang | Status |
|---|---|---|---|
| 1 | Rückverbindung `ArticleManager.getEnvironment()`, `getInstance()` `@Deprecated`, `shutdown()` mit explizitem Reset (Provisorium, entfällt mit Stufe 2) | KnowWE-core | **erledigt** (9e19e1b30) |
| 2 | **Environment besitzt allen Zustand und alle Threads.** `Environment implements AutoCloseable`; `close()` statt `shutdown()`. Reset-Liste aus Stufe 1 ist leer | KnowWE-core (+ Commons für 2e) | **erledigt** (2a-2d: 56b2b2b63, Folgecommit; 2e: 2026-09-09) |
| 3 | Aufrufer migrieren nach vier Mustern (s.u.), repo-weise: KnowWE, KnowWE-DES, KnowWE-SSP, Experimental, KRONE, Internals, IE | alle Consumer | Muster 1 in allen Repos erledigt (2026-09-08), Muster 2-4 offen |
| 3b | **`web`-Parameter entfernen.** Es gibt nur `DEFAULT_WEB`; der Parameter ist toter Ballast in Signaturen und Aufrufen. Eigener Durchgang nach den Mustern 1-3 und vor dem Löschen von `getInstance()`, weil er dieselben Stellen anfasst | alle Repos | offen |
| 4 | Paket 1 (Listener-Singletons) erledigt 2026-09-09. `getInstance()`, `initInstance()`, `articleManagers`-Map löschen; Einstieg nur noch über den JSPWiki-Connector, der die Environment erzeugt und in den ServletContext legt; Testinfrastruktur auf `new Environment(...)` pro Test | alle, plus Kompilier-Check GINA, KMW, KnowledgeConverter, d3web-Player-Platform, SemanticServicePlatform | offen |

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
- **2e EventManager pro Environment.** Erledigt 2026-09-09. Flach, ohne Hierarchie: `EventManager` (Commons) hat einen
  öffentlichen Konstruktor und `close()`; die Environment besitzt einen eigenen (`Environment.getEventManager()`), alle
  Wiki-Events werden dort gefeuert und alle Wiki-Listener dort registriert. `EventManager.getInstance()` ist aus
  KnowWE-Sicht tabu und bleibt nur für Commons-eigene Events (SemanticCore: `RepositoryCreatedEvent`,
  `SemanticCoreConnection*Event`; einziger KnowWE-Hörer `PermitTracker`) und Standalone-Anwendungen (TEKNO-Studio).
  Eine Hierarchie (Environment-Manager unter dem globalen, Events in beide Richtungen) war implementiert und wurde
  verworfen: das Sicherheitsnetz wurde nur an zwei Stellen gebraucht und die Semantik "wer hört was" wäre auf Dauer
  unklar geblieben. Ein Listener am falschen Manager verliert Events jetzt still, das ist gewollt.
  Zugriff: `KnowWEUtils.getEventManager(Section|Article|ArticleManager|Compiler|UserContext)`, `environment.getEventManager()`;
  übergangsweise `KnowWEUtils.currentEventManager()` (deprecated, aktuelle Environment sonst global) für Stellen ohne
  Kontextobjekt. Der `CompilationLocal`-Cache-Listener wird von der Environment registriert (`createCacheListener`),
  `LongOperations` und `CIBuildManager` kennen ihre Environment, `OntologyExporter` ist ein geplugter EventListener.

### Stufe 3b: `web` entfernen

Stand 2026-09-08 (Hauptcode, nach den bisherigen Umschreibungen):

| Repo | `Environment.DEFAULT_WEB` | `getWeb()` | Methoden mit `String web` |
|---|---|---|---|
| KnowWE | 67 | 86 | 43 |
| KnowWE-DES | 40 | 58 | 26 |
| Experimental | 68 | 31 | 14 |
| KnowWE-SSP | 19 | 22 | 2 |
| KRONE | 35 | 6 | 2 |

Kern-APIs mit `web` in KnowWE-core: `Environment.getArticleManager(web)`/`getArticle(web, title)`, `DefaultArticleManager(env, web)`,
`Compilers.getCompilerManager(web)`, `KnowWEUtils.getArticleManager(web)`/`getArticle(web, title)`/`getPackageManager(web)`,
`Article.createTemporaryArticle(text, title, web[, root])`, `Sections.get(web, title, pos)`, `Sections.ReplaceContext.of(web, ...)`,
`TermUtils.getTermInfo(web, ...)`/`getTermIdentifiers(web)`, `TermInfoSet.initTerm(web, id)`, `ListSectionsRenderer.getAllSectionsOfType(web, ...)`,
`AbstractHTMLTagHandler.renderHTML(web, title, user, ...)` (abstrakt, von allen TagHandlern implementiert), die `Attachment*Event`-Konstruktoren,
`Attributes.getBrokerConstant(web)`, dazu `Section.getWeb()`, `Article.getWeb()`, `UserContext.getWeb()`, `ArticleManager.getWeb()`.

Vorgehen:
1. Kern-APIs: `web`-lose Varianten anbieten (`Environment.getArticleManager()`, `getArticle(title)`, `createTemporaryArticle(text, title)`,
   `TermUtils.getTermInfo(articleManager, ...)`, `Sections.ReplaceContext.of(articleManager, ...)`, Events ohne `web`), die `web`-Varianten deprecated.
   `AbstractHTMLTagHandler.renderHTML` bekommt eine Default-Variante ohne `web`; die TagHandler ziehen repo-weise nach.
2. Aufrufer per Skript: `Environment.DEFAULT_WEB`-Argumente streichen, `x.getWeb()`-Argumente streichen, wo die Zielmethode eine `web`-lose Variante hat.
3. Eigene Methoden mit `String web`-Parameter in den Plugins (rund 90) manuell oder per Skript um den Parameter kürzen.
4. Zuletzt `getWeb()` aus `Section`, `Article`, `UserContext`, `ArticleManager` und `DEFAULT_WEB` löschen (Teil von Stufe 4).

Stand 2026-09-08 (abends), Schritte 1-3 umgesetzt:

- Schritt 1 (KnowWE-core): `Environment` hat genau einen `ArticleManager` (`getArticleManager()`, Feld statt Map pro Web; `getArticleManager(web)` deprecated, warnt bei fremdem Web), `getArticle(title)`, `getTerminologyManager(master)`, `DefaultArticleManager(environment)`, `Article.createTemporaryArticle(text, title[, root])`, `Sections.ReplaceContext.of(env, userCanEdit, author)`, `TermUtils.getTermInfo(articleManager, ...)`, `TermInfoSet.initTerm(articleManager, id)`, `ListSectionsRenderer.getAllSectionsOfType(articleManager, ...)`, `Attachment*Event(parent, file)`, `Attributes.getBrokerConstant()`, `WikiConnector.getAllArticles()`, `AbstractHTMLTagHandler.renderHTML(title, user, parameters, result)` (die `web`-Variante ist nicht mehr abstrakt, wirft `UnsupportedOperationException`, wenn keine der beiden überschrieben ist). Alle `web`-Varianten deprecated.
- Schritt 2 (Skript `web2_callers.py`): `getArticleManager(DEFAULT_WEB|x.getWeb())` -> `getArticleManager()`/`x.getArticleManager()`, `Compilers.getCompilerManager(...)` -> `x.getArticleManager().getCompilerManager()`, `getArticle(web, title)` -> `getArticle(title)`, temporäre Artikel, Events, TermInfo, PackageManager ohne `web`; 129 Dateien in fünf Repos.
- Schritt 3 (Skript `web3_methods.py`, iteriert bis Konvergenz): Methoden mit `String web`-Parameter bekommen `ArticleManager articleManager`, wenn alle `web`-Verwendungen im Rumpf umschreibbar sind (29 Methoden), lokale `String web = ctx.getWeb()` entfallen (32), TagHandler überschreiben `renderHTML(title, user, ...)` (11), Aufrufer (auch namensgleiche in anderen Repos) übergeben den ArticleManager. Falle: das Skript ordnet Aufrufer nur nach Methodennamen zu, `D3webUtils.getKnowledgeBase(web, title)` bekam so die Umschreibung von `KnowSECUtils.getKnowledgeBase(web)`; gelöst durch eine ArticleManager-Variante in `D3webUtils`.
- Falle temporäre Artikel (Albrechts Einwand): `Article.getArticleManager()` ist `@Nullable`, für temporäre Artikel (Vorschau, `createTemporaryArticle`) null; `section.getArticleManager()` ist als Ersatz für `getArticleManager(section.getWeb())` also falsch. Lösung: `Article` kennt seine `Environment` (auch temporär: `createTemporaryArticle(environment, text, title[, root])`, `Article.getEnvironment()`/`findEnvironment()`), `KnowWEUtils.findEnvironment(Section|Article)` läuft über den Artikel statt nur über den Manager, und `KnowWEUtils.getArticleManager(Section|Article)` liefert für temporäre Artikel den Manager der Environment. Alle Umschreibungen mit Section-/Article-Empfänger (30 Dateien) nutzen den Helfer; `Compilers.getCompilers(context, section, ...)` hatte durch die Umschreibung einen wirkungslosen Fallback, jetzt ebenfalls über den Helfer. Die parameterlosen `createTemporaryArticle(text, title)` sind transitional (aktuelle Environment) und deprecated.
- Bleibt (Stufe 4, `web` als String-Wert): `TestObjectContainer(web, ...)` der CI-TestObjectProvider, `web`-Request-Parameter in URLs/JS (`DefaultTool`, `TestCaseToolProvider`, `DebuggerQuestionRenderer`, `Attributes.WEB` in `AbstractUserContext.getWeb()`, `CommentRenderer`, `BoxRenderer`), `D3webUtils.getWebEnvironmentPath` (`$web$`-Platzhalter), `ProgressListenerProvider`-Schlüssel (KBB), `AttachmentType`-Event-Vergleich, `PrefixedTestCaseProvider`, Revisions-Plugin (Experimental, eigener `ArticleManager` pro Web), `Article.web`/`Section.getWeb()`/`UserContext.getWeb()`/`ArticleManager.getWeb()`, Log-Ausgaben mit Web.

| Repo | `String web` | `Environment.DEFAULT_WEB` | `getWeb()` |
|---|---|---|---|
| KnowWE | 55 (davon ~40 deprecated Kern-Varianten) | 21 | 44 |
| KnowWE-DES | 6 | 4 | 13 |
| KnowWE-SSP | 1 | 1 | 6 |
| Experimental | 16 | 47 | 12 |
| KRONE | 1 | 0 | 2 |

Die Muster-1-Umschreibungen haben schon viele `DEFAULT_WEB`-Stellen entfernt (`getArticleManager(DEFAULT_WEB)` -> `ctx.getArticleManager()`);
neue Aufrufer sollen ab jetzt keine `web`-Variante mehr benutzen.

### Stufe 3: die vier Migrationsmuster

1. **Kontext vorhanden** (rund 560 Stellen): `Environment.getInstance()` -> `context.getEnvironment()` (UserContext), `KnowWEUtils.getEnvironment(section|compiler|articleManager)`, `section.getArticleManager().getEnvironment()`,
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
- [x] Stufe 2 (2a-2d 2026-09-08, 2e 2026-09-09)
  - [x] 2a Registries in die Instanz (2026-09-08): `SectionRegistry` pro Environment (`Section.get(id)` delegiert übergangsweise an die aktuelle), ScriptManager pro Environment (Compile-Scripts gehören jetzt dem `AbstractType` und werden beim Dekorieren des Typbaums pro Environment indexiert, `registerCompileScripts`), Sectionizer-Module, `occupiedTerms`, Messages-Index, blockierte ToolProvider, globale Notifications, RightPanel-Tabs als Environment-Felder bzw. Services; `CompilationLocal`-Caches am `CompilerManager`; `Scope`-Typcache schwach referenziert
  - [x] 2b Services + `close()` (2026-09-08): `Environment implements AutoCloseable`, `getService(Class, Supplier)`, `onClose(Runnable)`; `close()` = Kompilierung abwarten, `DeInitEvent`, CompilerManager/AttachmentManager stoppen, Close-Tasks rückwärts, Services schließen. `LongOperations` und `AttachmentUpdates` sind Services mit eigenem Thread-Pool/Timer. `ServletContextEventListener.contextDestroyed` schließt die Environment. `shutdown()` ist deprecated und delegiert. Neue Helfer `KnowWEUtils.findEnvironment/getEnvironment(Section|ArticleManager|Compiler|UserContext)`. Transitional: `Environment.peekInstance()`/`requireInstance()` (deprecated) für die verbliebenen kontextlosen statischen Aufrufer
  - [x] 2c Singleton-Typen (2026-09-08): `PlainText.getInstance()`, `LineContent.getInstance()`, `DashTreeElementContent.getDefaultInstance()` liefern jetzt eine Instanz pro Environment (über `getService`), ohne Environment eine frische. Kein Reset mehr in `close()`; die Reset-Liste aus Stufe 1 ist leer. Die 21 Singleton-Typen in den Plugins folgen in Stufe 3 nach demselben Muster.
  - [x] 2d Plugin-Singletons pro Environment (2026-09-08): `Plugins.getCompilers()` instanziiert die geplugten Compiler pro Aufruf (`getNewInstance`), die Environment merkt sich ihre (`isGlobalCompiler`). Die von der Environment registrierten Plugin-EventListener werden beim `close()` wieder abgemeldet. TagHandler, Instantiations, SectionizerModule bleiben Plugin-Singletons (zustandslos bzw. Zustand über `getService`).
  - [x] 2e EventManager pro Environment (2026-09-09): Commons `EventManager` mit öffentlichem Konstruktor + `close()`; `Environment.getEventManager()`, `KnowWEUtils.getEventManager(...)`, `KnowWEUtils.currentEventManager()` (deprecated). Umgeschrieben per Skript in KnowWE, KnowWE-DES, KnowWE-SSP, Experimental, KRONE; Rest `currentEventManager()` im Hauptcode: KnowWE 18, DES 8, SSP 4, KRONE 2 (Servlet-/JSPWiki-Ebene, `Rdf2GoCore`, Listener-Singletons). Offen: Listener-Singletons (siehe offene Punkte)
- [ ] Stufe 3 (in Arbeit seit 2026-09-08)
  - [x] Muster 1 (Kontextobjekt vorhanden) per Skript in KnowWE (c5b69d0e4), Experimental, KnowWE-DES, KnowWE-SSP, KRONE, Information-Extraction: ~470 Stellen. Ausgenommen: KnowWE-Plugin-Agents (KnowWE-DES, lokale Variablen statt Felder, manuell nachziehen)
  - [x] Muster 2 in KnowWE (2026-09-08, 9a2dfc2e5, b12b64604): statische Utilities bekommen eine Environment-/ArticleManager-Variante, die alte Signatur bleibt deprecated und delegiert an `Environment.requireInstance()`. Betroffen: `KnowWEUtils` (11 Methoden), `CIUtils`, `D3webUtils`, `FlowchartUtils`, `FlowchartToolMenuAction`, `TestCaseUtils`, `GraphDataBuilder`, `DownloadWikiZIPAction`, `Fingerprint`, `SnapshotAction`, `WikiFileProviderUtils`, `RecentChangesUtils`, `Plugins.addRendererToType`, `AttachmentUpdateMarkup.isAutoUpdatingActive`. `UserContext.getEnvironment()` als Default-Methode. Rest in KnowWE-main: 31 Stellen (Erzeuger `KnowWEPlugin`, `web`-Delegationen, Wertobjekte `WikiPageInfo`/`WikiAttachmentInfo`/`WikiAttachmentPair`/`WikiAttachmentResource`, `Rdf2GoCore`, `Rdf2GoUtils`, `Config`, `InitTerminologyHandler`, `FileChange`, `DefaultWikiTestObjectProvider`, `VersionMarkupType`, Test-Utilities)
  - [ ] Muster 2 in den Consumer-Repos: Aufrufer der deprecated Statics auf die neuen Varianten (per Skript, wo Kontext vorhanden)
  - [x] Muster 3, Anfang (2026-09-08): `Instantiation.init(Environment)` als Default-Methode; `WikiSearchService` ist Environment-Service (`of(environment)`, AutoCloseable), `RepositoryInitializer`, `WikiSearchInstantiation`, `SnapshotDirectoryPermissionInstantiation` nutzen `init(Environment)`
  - [x] Muster 3, Instantiations (2026-09-08): alle 22 Implementierungen nutzen `init(Environment)`; `getInstance()` im `init` durch den Parameter ersetzt. Fallen: lokale Variable `environment` (AgentInstantiation, Parameter heißt dort `knowweEnvironment`), Substring-Treffer `AgentEnvironment.getInstance()`/`RetrievalEnvironment.getInstance()` beim Ersetzen, `web`-Nutzung im Body (ImplicationsConversionInstantiation -> `DEFAULT_WEB`)
  - [x] Muster 3, Dienste mit ServletContext-Hook (2026-09-08, 25feeffc4 u.a.): `CIBuildManager`, `InterWikiImportUpdateService`, `MaengelKatalogRemoteRefreshService` sind Environment-Services (`of(environment)`, AutoCloseable); SemanticCore-Repository-Shutdown über `environment.onClose` im `RepositoryInitializer`. Kein Nutzer von `registerOnContextDestroyedTask` außer KnowWE-core mehr.
  - [x] Muster 3, weitere Dienste (2026-09-08): `CBXProgressDownloadJob` (SSP) fängt die Environment seines Compilers ein, `WikiGitRuntime` (KRONE) mit Environment-Varianten, `ProductContextHandler` (KRONE) bekommt sie im Konstruktor, `McpOAuthServer.forWiki(environment, config)`, `TestDefinitionCompletionSource` reicht sie im Completion-Pfad durch
  - [x] Muster 3, Task-Manager (2026-09-08): `TaskContextManagerInstance.of(Environment|UserContext|ArticleManager)`, `set(env, manager)`, `peek(env)`; der Manager liegt als Environment-Service (privater `Holder`, AutoCloseable) und wird mit dem Environment geschlossen (`close()` = `shutdown()` + Listener abmelden). Extension-Point `TaskContextManager` pluggt jetzt eine `TaskContextManagerFactory` (KRONE: `KONAPTaskContextManagerFactory`). `BasicTaskContextManager`/`KONAPTaskContextManager` bekommen das Environment im Konstruktor, dazu `taskBasedWorkManagementIsActive(env)`, `getPathname(env, name)`, `PropertyUtil.*(env, ...)`, `VersionenWikiPageTaskHistory(env, ...)`, `VersionenFileTaskHistory(env)`, `JiraTaskLinker.register(env, service)` (Abmeldung per `onClose`), `Patches.findTaskAndCreatePatch(env, ...)`, `WorkContextGitTool(env, history)`; alte Signaturen deprecated. 46 von 47 produktiven `get()`-Stellen umgestellt (Rest: `TaskAgentTools`, Agents-Plugin). Wertobjekte `TaskContext`/`WorkItem` und die Hooks `TaskContextSessionListener` (Servlet) / `TaskAuthorizationManager` (JSPWiki) bleiben ohne Kontextobjekt (`requireInstance()`/`peekInstance()`, Muster 4).
  - [x] Muster 3, KONAP-COOM-Listener (2026-09-08): `ModelUpdatedEvent` trägt die Environment seines Modells (gesetzt aus dem Compiler in `ModelUpdateScript` und `VariableOrderingUpdateJob`); `CoomKonapListener` reicht sie durch alle statischen Helfer (`updateProduct(env, ...)`, `isKSWModeOn(env)`, Section-Resolver, UI-Dateien, Modell-Dump). `CoomDslExportFactory.exportCoomDsl(env, ...)`; `CoomReleaseArtifactEndpoint` bekommt die Environment von seiner Factory (`context`/`section`), `UploadCoomDslWorkflowStep` (ohne Aufrufer) im Konstruktor. KRONE-`Utils`: `getFeaturePageName(articleManager, ...)`, `getContentFromPropertiesFile(env, ...)` (`stripPrefix*` nennen `Environment` nur noch im Kommentar).
  - [x] Muster 3, `WDManager` (2026-09-08): Environment-Service statt statischem Singleton, `WDManager.of(environment|articleManager|section|userContext|compiler)`, `getInstance()` deprecated; `getCoomDomainNamespace(env)`, `getCoomDomainNamespacePrefixAbbreviation(env)`, `getMasterCompiler(env)` (die `CompilerManager`-Variante ist wieder die eigentliche Implementierung); `KONAPModels.get(env|section)` und `RdfContext.get(env|section)` sind jetzt die bevorzugten Zugriffe, die parameterlosen deprecated (vorher umgekehrt markiert!). Skript `wdmanager.py`: 99 Aufrufstellen über den Kontextparameter der umgebenden Methode, 4 weitere über Felder; 33 bleiben beim deprecated `getInstance()`/`getMasterCompiler()` (statische Helfer ohne Kontext: `Utils.stripPrefix(URI)`/`shortenPrefix`, `TableUtils`, `LockedDialogHelper`, `CollisionApi` (JAX-RS), Konverter, `ModelUpdateScript.notify` vor Ermittlung des Compilers, Tool-Provider mit `ModelSet`/`Product`).
  - [x] Muster 3, `ReleaseContextManager` (2026-09-08): Environment-Service statt statischem Singleton, `ReleaseContextManager.of(environment|userContext|articleManager|section|compiler)`, `set(env, manager)` für Tests; die geplugte Klasse wird pro Environment über `getNewInstance()` erzeugt und mit `init(Environment)` gebunden (ruft den bestehenden Hook `init(Properties)` mit den Wiki-Properties), `close()` meldet den Event-Listener ab. `getInstance()`/`registerInstance()` deprecated. `KONAPReleaseContextManager`: `serverStatus` ist Instanzfeld (war statisch), Properties/Pfade über die Environment; `ServerStatus.serialize(env)`/`deserialize(env)`. `KnowWeModelReleaseContext` kennt seine Environment (Konstruktor). Skript `releasemanager.py`: 40 Aufrufstellen über Kontextparameter, 10 bleiben deprecated (`TaskIntegrationRunService`, `DiffTasksWorkflowStep`, `CoreReleaseCommandApi`/`QueryApi` mit Command-Objekten, `ConfigitBridgeMarkupAdapter.prepare`, anonymer OutputStream in `VariableOrderingUpdateJob`).
  - [x] Muster 3, KnowWE-core-Rest (2026-09-08): der `WikiConnector` kennt seine Environment (`setEnvironment`/`getEnvironment`, gebunden im Environment-Konstruktor; Implementierungen `JSPWikiConnector`, `DummyConnector`); die Wertobjekte `WikiPageInfo`, `WikiAttachmentInfo`, `WikiAttachmentPair`, `WikiAttachmentResource` halten den Connector, der sie erzeugt hat (alte Konstruktoren deprecated). `FileChange.fromArticle(articleManager, ...)`, `VersionMarkupType.getApplicationManifest(env)`, `PackageCompileLinkToTermDefinitionProvider.getTerminologyManager(env, ...)`/`createBaseURL(env)`, `Rdf2GoUtils.getLocalNamespace(env)` (pro Environment berechnet statt statisch beim Klassenladen), `Sections.ReplaceContext` trägt die Environment, `Config` (Visualization) merkt sich die Environment der Section, `InitTerminologyHandler` nutzt den Compiler. Bewusst bleiben (Übergang, `peekInstance`/`requireInstance`): `KnowWEPlugin` (Erzeuger, Stufe 4), Test-Utilities, `Section.get(id)`, `Messages.getSectionsWithMessages()`, `KnowWEUtils.findEnvironment`-Fallback, `AbstractType`-Skriptindex, `LineContent`/`PlainText`/`DashTreeElementContent.getInstance()`, `Plugins.getRightPanelTabs()`, `DefaultWikiTestObjectProvider` (TestObjectProvider ohne Kontext), `D3webUtils.getD3webBundle(request)`, `Rdf2GoCore`-Konstruktor.
  - [x] Muster 3, Agents + COOM-Retrieval (2026-09-08, KnowWE-DES): `AgentEnvironment.of(env|session)`, `WikiChatSessionRegistry.of(env)`, `RetrievalEnvironment.of(env)` sind Environment-Services statt JVM-Singletons; `WikiAgentSession.getEnvironment()` (WikiChatSession bekommt sie im Konstruktor, McpSession über `requireInstance()` = Transport ohne Kontext); Tools nutzen `session.getEnvironment()` bzw. `WikiAgentTools.requireEnvironment(task, request)`/`environment(session)` (Preview ohne Session -> aktuelle Environment), `TaskAgentTools.manager(session)`/`taskManagementActive(session)`, `AbstractRetrievalAction.require*(context)`. Bewusst bleiben: Tool-Schemata, die beim Erzeugen des Tools Dashboards/Cores/Compiler aufzählen (`CiAgentTools.dashboardParameter`, `SparqlQueryTool.coreParameter`, `WikiTermTool.termCompilers`), `AgentInstantiation.Property` (Enum-Konstanten), Websocket/Serializer ohne Session-Referenz.
  - [x] Muster 3, SSP-Rest (2026-09-08): `MaengelKatalogRemoteStateRepository(env)` (vom `MaengelKatalogRemoteRefreshService` mit seiner Environment erzeugt), Suggestion-Repository über die Dashboard-Section, `ErrorCodeLabelEditor`, `ListPackagesMarkup`, `VirtualRealityHandler`, `MalfunctionImportType.getKB(connector, ...)`, `CachingAdapterStatementExporter.parseCache(env, ...)`, ServiceMate-`WikiConfiguration` über den UserContext.
  - [x] Muster 3, Markierung der Reststellen (2026-09-08): alle verbliebenen `Environment.getInstance()` im produktiven Code von KnowWE-DES, KnowWE-SSP, Experimental und KRONE stehen jetzt als `Environment.requireInstance()` (Skript `generic_rest.py`: 6 weitere Stellen über Kontextparameter, 133 markiert). `requireInstance()`/`peekInstance()` sind damit die vollständige Liste der Stellen ohne Kontextobjekt (Stand: KnowWE 64/16, KnowWE-DES 93/3, KnowWE-SSP 11/0, Experimental 57/0, KRONE 45/0, darin auch die deprecated Delegationen). `getInstance()` nur noch in KnowWE (`KnowWEPlugin`, Test-Utilities, `DummyConnector`-Kommentar) und drei KRONE-Dateien.
  - [ ] Muster 3, Rest mit Designbedarf (eigenes Paket): KRONE: 5 Kopien von `getPageProvider()` (TaskIntegrationRunService, GetTasksToChooseRunService, ReleaseOverviewRenderer, CoreReleaseQueryApi, TaskLogDiffUtil) -> `WikiGitRuntime.getPageProvider(env)`, `TaskLogDiffUtil.getInstance()`, `SynchronizingRepositoryClient.getRepoClient()`, `SAPClient.createClientFromWikiConfig()`, `ConfigitBridgeAction.getXmlFileName(product)`, `RequestPriority`, JAX-RS/Filter (`CollisionApi`, `ApiWikiFilter`), KRONE-`PropertyUtil`-Aufrufer; KnowWE-DES: `CoomModelProvider` (statische Modell-Map, 46 Stellen), `CoomCompiler.findByName(name)`, `CoomConceptStore`, `TemporaryModelRegistry`, `GitLabExportAction`/`GitLabServiceFactory`, `CoomAgentTools.allCompilers/awaitCompilation`, Git-Server-Connector (`KnowWEGitServerConnector`, `GitWikiProperties`), MCP-`engine()`-Helfer, KnowSEC (`SessionManager`, `KnowSECSearchProvider`, `KnowSECUtils.getTitle`), CI-TestObjectProvider (`CoomCompilerTestProvider`, `CompilerModelTestProvider`, `RegressionTestProvider`, KRONE `KONAPModelTestObjectProvider`, `CBXAbstractTest`); Experimental: 57 Stellen in Legacy-Plugins (Defi, WissKont, OphtoVis, Baseline, ...); Quartz-Einstieg `CBXProgressDownloadJob.execute`; `Rdf2GoCore` (asynchroner Shutdown-Thread, siehe offene Punkte)
  - [ ] Muster 4 (JSPs, 9 Dateien)
  - [ ] Tests (128 Stellen): Umstieg auf `new Environment`/JUnit-Rule sobald Stufe 4 den Konstruktor öffnet
- [ ] Stufe 4 (seit 2026-09-09)
  - [x] Paket 1, Listener-Singletons pro Environment (2026-09-09, KnowWE 258261b0e + Folgecommit, DES/SSP/KRONE): siehe offene Punkte. Dabei `Rdf2GoCore` mit Environment.
  - [x] Paket 2, `requireInstance()`-Reststellen (2026-09-09, alle Repos außer Experimental): Muster siehe offene Punkte. Neu dabei: `WikiTestObjectProvider` (CI4KE) mit `getTestObjects(Environment, ...)`, der CI-Build bindet die geplugten Provider-Singletons an die Environment des Dashboards (`WikiTestObjectProvider.bind`); alle 8 Provider umgestellt (`RegressionTestContext` trägt die Environment). Websocket-Handshake legt die Environment in die Socket-Properties (`AgentWebSocketConfigurator`), MCP-Server/-Sessions/-Authentication holen sie aus Request bzw. Servlet-Context, Property-Enums (`AgentInstantiation`, `JiraInstantiation`, `GitLabServiceFactory`) lesen pro Environment (`value(env)`) statt einmal beim Klassenladen, `TaskAuthorizationManager` und `TaskContextSessionListener` über den ServletContext (`findIn`, tolerant vor dem Wiki-Init), `TaskContext.editPage(manager, ...)`, KRONE `getPageProvider()`-Kopien ersetzt durch `WikiGitRuntime.getPageProvider(env)`, Run-Services/`TaskLogDiffUtil`/`ReleaseUtils.pullMasterTTL` mit Environment. Verbleibende `requireInstance()` außerhalb deprecated Delegationen: KnowWE 6 (Test-Utility, Fallbacks), DES 19, SSP 2, KRONE 9 (siehe unten), Experimental 65 (Legacy-Plugins, bewusst nicht angefasst)
  - [x] Paket 2a, geplugte EventListener pro Environment (2026-09-09, Albrechts Vorschlag): `Environment.initEventManager()` erzeugt die geplugten Listener mit `extension.getNewInstance()` statt `getSingleton()`, jede Environment hat eigene Instanzen; Listener, die `de.knowwe.core.EnvironmentAware` implementieren, bekommen vor der Registrierung `attach(environment)`; `Environment.getListener(Class)` liefert die Instanz einer Environment (statt statischer `getInstance()`). Umgesetzt: KnowSEC `PeriodicForEverTask` (Environment als Feld, Timer endet per `onClose`, vorher lief bei jeder Re-Initialisierung ein weiterer Timer für immer), `NormanDbPollOperation.of(env)`/`SessionUpdateOperation.of(env)` als Environment-Services statt Singletons (statische Sammel-Sets in `NormanDbPollOperation` bewusst gelassen), Tool-Provider holen die Operation über die Environment ihrer Section; `ChangeLog` (OntologyDebugger, Modul ist im Reactor auskommentiert und die Listener-Registrierung in der plugin.xml ebenfalls, Plugin ist tot) nur mechanisch auf `EnvironmentAware`.
  - [ ] Paket 2b, Designentscheidungen vom 2026-09-09 (mit Albrecht): (1) Agenten-Tools: `WikiAgent`-Implementierungen bekommen die Environment im Konstruktor (`AgentInstantiation.parseAgent()` ruft `getDeclaredConstructor(Environment.class)`), Tool-Listen wandern aus den Feld-Initialisierern in die Konstruktoren, Tools halten `@NotNull Environment` (kein nullable, Tests bauen eine echte Test-Environment; dafür den Test-Konstruktor aus Paket 3 vorziehen), `AgentSessionHierarchicalSerializer` bekommt den Agenten von der Session, `WikiAgentTools.environment(session)` verliert den Fallback. (2) `TemporaryModelRegistry`, `GitLabServiceFactory`, `RequestPriority` als Environment-Services, `VariableOrderingBuilder` bekommt den `WikiConnector` vom `BridgeBuilder`. (3) erledigt durch Paket 2a. Punkte 4 bis 10 der Liste unten noch zu besprechen.
  - [ ] Paket 2, Designbedarf (bleibt `requireInstance()`, Besprechungsstand siehe Paket 2b): Tool-Schemata der Agenten, die beim Bau der Tools Wiki-Inhalte auflisten (`CiAgentTools.dashboardParameter`, `SparqlQueryTool.coreParameter`, `WikiTermTool.termCompilers`, `CoomAgentTools.artifactParameter`, `CoomCompileTool`-Beschreibung, `CoomAgentPrompts.getRelativeOrigin`): Tools werden von Agenten im Konstruktor ohne Environment erzeugt, `WikiAgentTool.parameters()` hat keinen Kontext; `AgentSessionHierarchicalSerializer.findTool`; `McpSession` ohne Environment in Tests; `WorkItem.sanitizeVersionNumberHistory` (JSON-Wertobjekt braucht den Git-Pfad); `TemporaryModelRegistry` (statische Registry); KnowSEC `NormanDbPoller`/`SessionUpdateTask.getLongOperationSection()` und `KnowSECUtils` mit `section == null`; `ChangeLog` (Singleton-Listener); `WikiPageCompletionProvider.getCompletions()` und `TestDefinitionCompletionSource`-Konstruktor (Vervollständigungsquellen ohne Kontext); `GitWikiProperties`/statische Git-Helfer in `VersioningAction` (Git-Server-Connector); `WikiAttachmentDownloadUpdate` (ohne Aufrufer); KRONE `RequestPriority`, `VariableOrderingBuilder` (ConfigitBridgeBuilder liest Attachments), `VariantDstdTestConfigTest*` (CI-Tests mit ModelSet als Testobjekt), `KONAPLogoAction`, `CoreReleaseQueryApi` (nativeContext ohne Typ), `GitLabServiceFactory` (statische Konfiguration aus `Instantiation.init`); Experimental-Legacy komplett
  - [ ] Paket 3, Konstruktor öffnen, Tests auf `new Environment` (128 Stellen), `getInstance()`/`initInstance()` löschen
  - [ ] Paket 4, `web`-Reste, `getWeb()`/`DEFAULT_WEB`, Rdf2GoCore-Shutdown-Thread, Kompilier-Check GINA, KMW, KnowledgeConverter, d3web-Player-Platform, SemanticServicePlatform
- [x] master eingemergt (2026-09-09): KnowWE f85e756f7 (Konflikte in `Article`, `Section`, `Messages`: String-IDs mit Namespace und Publikation bei Queue-Zeit aus master, `SectionRegistry` pro Environment hält jetzt `Map<String, Section>`, Sperren auf der Registry), KnowWE-DES f667721bf + ad71597b1 (neue Task-Tools und `CoomExportTool` auf die Environment-APIs angepasst)
- [x] Consumer-Repos geklont (2026-09-07): Experimental, KnowWE-DES, KnowWE-SSP, KRONE (enthält KnowWE-KONAP-Integration), Information-Extraction. Zusammen mit denkbares-Internals sind das alle Repos mit `Environment.getInstance()`-Aufrufern.
- [ ] Nur für Stufe 4 relevant (referenzieren KnowWE-Artefakte, aber ohne `getInstance()`-Aufrufe): GINA, KMW, KnowledgeConverter, d3web-Player-Platform, SemanticServicePlatform. Vor dem Entfernen von APIs klonen und kompilieren.

## Offene Punkte / Beobachtungen

- 21 weitere Singleton-Typen (`private static X instance`) in den Plugins werden bei Re-Init nicht zurückgesetzt. Für Stufe 2 prüfen, ob ein generischer Mechanismus (z.B. Reset über `Plugins`/Typ-Registry) sinnvoller ist als Einzelfälle.
- `ArticleLifecycleFixture` (KnowWE-core-Tests) baut die Environment per Reflection; mit einem Test-Konstruktor/Factory ließe sich das sauber machen (Kandidat für Stufe 2/4).
- `getInstance()`-Deprecation erzeugt jetzt ~885 Warnungen über alle Repos. Bewusst so, als Migrationssignal; bei Bedarf mit einer Zeile zurücknehmbar.
- `Rdf2GoCore.close()` fährt außerhalb des Servlet-Shutdowns (`ServletContextEventListener.isDestroyInProgress()`) in einem abgekoppelten `new Thread` herunter, damit Recompiles nicht blockieren. Beim `Environment.close()` (Tests!) läuft dieser Thread parallel zum Close-Task `SemanticCore.shutDownRepositoryManager()` weiter, entfernt sein Repository aus dem Manager und ließ den Manager-Shutdown mit NPE abbrechen (`getRepository(id)` null, `MaengelKatalogSearchActionTest`). `shutDownRepositoryManager()` ist seit 2026-09-08 null-sicher und fährt den Manager im `finally` herunter (denkbares-Commons). Der Thread selbst widerspricht dem Ziel "Environment schließen = alles sauber": `Rdf2GoCore` soll seine Environment kennen (Muster 3, `getInstance(compiler)` -> `KnowWEUtils.getEnvironment(compiler)`) und beim Schließen der Environment synchron herunterfahren bzw. die Environment auf den Thread warten.


- **Listener-Singletons** (seit 2e relevant, erledigt 2026-09-09 als Stufe 4, Paket 1): statische Singletons, die
  sich einmalig per `currentEventManager()` als Listener registrierten, hingen am Manager der beim Klassenladen
  aktuellen Environment. Jetzt Environment-Services (`X.of(env|section|compiler|...)`, `getInstance()` deprecated):
  KnowWE `CIHookManager`, `LazyReferenceManager`; DES `CompletionManager`, `SparqlCacheManager`; SSP `SSCUtils`;
  KRONE `ConfigitBridgeExecutorHelper` (Executor endet mit der Environment statt per ServletContext-Event). Geplugte
  EventListener statt Selbstregistrierung: `OntologyExporter`, Dialog `SessionKnowledgeBaseListener` (aus `StartCase`),
  KnowSEC `SessionManager.CompilerFinishedListener`. Pro Environment registrierte Hilfs-Listener über `getService`:
  `CoomConceptStore.Stores`/`WikiConceptStore.Stores` (Caches je Rdf2GoCore), `SSCService.Cleaner`,
  `D3webCompilerFinishedCommitter` (CBX), die `CompileEventListener` der TermBrowser-CompileScript-Provider.
  Geplugte `HierarchyProvider`-Singletons registrieren sich in `CachedHierarchyProvider.setCompiler` am Manager der
  Compiler-Environment (WEAK) statt im Konstruktor. `TermSetManager` hängt an der Environment seiner Section.
  Bewusst geblieben: `HeadlessKnowWE` (Erzeuger der Environment, ein Lauf pro JVM). Rest `currentEventManager()` im
  Hauptcode: KnowWE 7 (Servlet-/JSPWiki-Ebene, `KnowWEAttachmentProvider`, `WikiGitContext`), DES 2, KRONE 1.
- **`Rdf2GoCore` kennt seine Environment** (2026-09-09): neuer Konstruktor `Rdf2GoCore(@Nullable Environment, name,
  lns, config)`; `OntologyCompiler` und `InitTerminologyHandler` übergeben sie, die alten Konstruktoren nehmen
  übergangsweise `Environment.peekInstance()` (Standalone-Cores in Tests/Exporten ohne Environment bleiben erlaubt).
  `findEnvironment()` (nullable) und `getEventManager()` (Environment-Manager, sonst `currentEventManager()`); die
  Core-Events werden dort gefeuert. Caches je Core (`SparqlCacheManager`, Concept-Stores, `SSCUtils`) hängen damit
  an der Environment des Cores; Standalone-Cores teilen sich eine am globalen Manager registrierte Instanz. Offen
  bleibt der asynchrone Shutdown-Thread (siehe unten).

- **`EnvironmentService` statt `AutoCloseable`** (2026-09-09): Environment-eigene Services (`getService`) implementieren
  `de.knowwe.core.EnvironmentService` mit `close()`, nicht `AutoCloseable`. Grund: Aufrufer holen einen Service, um
  ihn zu benutzen, nie um ihn zu schließen; `AutoCloseable` löste in IntelliJ überall die Inspektion "AutoCloseable
  used without try-with-resources" aus. `Environment.closeServices()` schließt beides (AutoCloseable bleibt für
  fremde Typen erlaubt). Echte Ressourcen mit try-with-resources (`WikiSearchIndex`) bleiben `AutoCloseable`; die
  `Environment` selbst auch, sie ist in Tests eine Ressource.
- **Paket 2, Muster** (2026-09-09, Beispiele committet): Servlet-Einstiege über `Environment.of(ServletContext|
  HttpServletRequest)` (Environment liegt seit `initInstance` als Attribut im ServletContext ihres Connectors, `close()`
  entfernt sie; `findIn` nullable für Fälle vor dem Wiki-Init), Quartz-Jobs bekommen die Environment in die
  `JobDataMap` (`CBXProgressDownloadJob`), statische Registries werden Environment-Services (`CoomModelProvider.of`),
  externe Clients bekommen die Environment vom Aufrufer (`SAPClient.createClientFromWikiConfig(env)`).

- **Geplugte EventListener pro Environment, `EnvironmentAware`** (2026-09-09): Extensions am Punkt `EventListener` werden
  nicht mehr als JVM-weite Singletons (`getSingleton()`) an jeder Environment registriert, sondern pro Environment neu
  erzeugt (`getNewInstance()`). Ein Listener mit Zustand implementiert `de.knowwe.core.EnvironmentAware` und bekommt
  in `attach(Environment)` seine Environment, bevor er registriert wird; alles Weitere (ArticleManager, Compiler,
  WikiConnector, Timer per `onClose`) löst er von dort auf, nie über eine "aktuelle" Environment oder über Events.
  Von außen erreicht man einen Listener über `environment.getListener(Class)` statt über statische `getInstance()`.
  Voraussetzung: Listener dürfen keine statischen Felder mit Environment-Zustand haben (Stand 2026-09-09: 25 geplugte
  Listener in KnowWE, DES, SSP, KRONE, Experimental; einzig `IncrementalCompiler` in Experimental hält ein statisches
  Feld). Das Muster gilt sinngemäß für alle Extension-Punkte, deren Instanzen Environment-Zustand halten.

## Bekannte Baseline-Fehler (unabhängig vom Umbau)

- **Gelöst (2026-09-08, denkbares-Internals 1ade0480):** Die Git-Provider-Tests (`GitPageProviderTest`, `GitWikiRepositoryTest` in KnowWE, `de.knowwe.versioning.git.*` in KnowWE-DES) fielen in der VM, weil `LANGUAGE=de_DE` gesetzt ist und gettext diese Variable über `LANG`/`LC_ALL` stellt; git antwortete deutsch, der Status-Parser im GitConnector versteht nur Englisch. Auf master identisch reproduziert. `RawGitExecutor.prepareEnvironment` pinnt jetzt auch `LANGUAGE`.
- KnowWE-DES (Testlauf 2026-09-08 nach Stufe 3, Muster 1): 9 Klassen `de.knowwe.versioning.git.*` (siehe oben, gelöst), `GeneratorAppTest` ("Cannot run program"), `TaskContextManagerPersistenceTest` (gelöst 2026-09-08: der Test hängte seinen Manager nicht ans Environment, `TaskContext.editPage` erzeugte daher einen zweiten Default-Manager, der auf `ArticleUpdateEvent` dieselbe wc-Datei überschrieb; auf master nur zufällig grün, weil `get()` den abgeräumten Manager des vorherigen Testlaufs zurückgab. Fix im Test: `TaskContextManagerInstance.set(env, manager)`). KnowWE-SSP und Experimental: grün. KRONE: Modultests `KnowWE-Plugin-KONAP` grün, `KONAP-Plugin-COOM` 4 Klassen mit Baseline-Fehler (`CoomModelWriterTest`, `CoomModelWriterKSWTest`, `DeductionSolutionSpace*Test`: die POM schließt `KnowWE-Plugin-Agents` aus, `KnowWE-Plugin-MCP` importiert es aber -> Plugin-Manager-Init scheitert; unabhängig vom Umbau), `KnowWE-Plugin-KONAP-ReleaseVersioning` grün. Information-Extraction: nur kompiliert.

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
- **Maven immer offline (`-o`) gegen das isolierte Repo bauen.** Ohne `-o` aktualisiert Maven SNAPSHOT-Abhängigkeiten aus repo.denkbares.com (Update-Policy) und überschreibt die lokal gebauten env-instance-Artefakte mit master-Snapshots; Symptom: "Symbol nicht gefunden" für frisch eingeführte API (2026-09-08: `CIBuildManager.of(dashboard)` in KRONE nach Remote-Refresh von `KnowWE-Plugin-CI4KE`). `build-chain.sh`/`run-consumer-tests.sh` setzen `-o`; nach einem Refresh die Kette ab dem betroffenen Repo neu installieren.

## Protokoll

- 2026-09-07: Analyse, Plan, Arbeitsumgebung eingerichtet, Kette gebaut, Baseline-Tests grün.
- 2026-09-07: Stufe 1 umgesetzt und committet (9e19e1b30). Voller KnowWE-Testlauf 1:53 min, nur die bekannten Git-Provider-Fehler.
- 2026-09-08: Stufe 3b (`web` entfernen) als eigener Durchgang aufgenommen, mit Messung und Vorgehen. Stufe-3-Umschreibung (`context.getEnvironment()`, Environment-Varianten der Statics) in allen Repos committet.
- 2026-09-08: Stufe 3, Muster 2 in KnowWE (statische Utilities mit Environment-Overload, deprecated Delegation), `Instantiation.init(Environment)`, `WikiSearchService` als erster Environment-Service, `UserContext.getEnvironment()`. KnowWE-main von 88 auf 31 `getInstance()`-Stellen.
- 2026-09-08: `Environment.initInstance()` schließt eine noch laufende Environment (c679ea688); vorher lief `TaskContextManagerEventTest` mit `initialized=true` in die Typbaum-Assertion.
- 2026-09-08: Stufe 3, Muster 1 in allen Repos per Skript (Kontextobjekt aus Methodenparameter oder Klassenfeld). Fallen: lokale Variablen nicht fuer Felder halten (nur Deklarationen mit genau einem Tab), Dateien mit CRLF-Zeilenenden byteweise behandeln (42 Dateien mussten repariert werden), `Environment`-Import nur entfernen, wenn das Wort nirgends mehr vorkommt (mehrzeilige Aufrufe). Kompilierfehler wurden zeilenweise aus git zurueckgesetzt (revert_errors.py), die Stellen bleiben beim statischen Zugriff.
- 2026-09-08: Stufe 2c/2d umgesetzt, voller KnowWE-Testlauf grün bis auf die bekannten Git-Tests. In KnowWE-core bleibt an statischem Zustand nur noch: der Singleton `Environment.instance/initialized` selbst (Stufe 4), zustandslose Singletons (Renderer, Visitor, SectionFinder), Konstanten/Caches ohne Lebenszyklus (`Priority`, `Scope`, `Section.COMPILER_REFS` schwach) und `ServletContextEventListener`-Tasks (Servlet-Lebenszyklus). `ResourceLoader` (JS/CSS-Registrierung, dedupliziert) bleibt bis Stufe 3/4.
- 2026-09-08: Stufe 2a/2b umgesetzt. Reset-Liste in `close()` besteht nur noch aus den drei Singleton-Typen (2c). Voller KnowWE-Testlauf grün bis auf die bekannten Git-Tests. Fallen: `close()` muss die Compiler vor den Services stoppen (Destroy-Scripts schreiben noch Messages); `AbstractType.removeCompileScript` darf beim Typ-Aufbau nicht auf einen noch nicht indexierten Typ zugreifen; `LongOperationUtilsTest` braucht jetzt eine Environment (per `ArticleLifecycleFixture`).
- 2026-09-07: Plan nach Rücksprache umgebaut: Ziel ist Besitz statt Reset (Stufe 2 neu definiert, Migrationsmuster für Stufe 3, `getInstance()` wird komplett entfernt). Consumer-Repos geklont; Experimental, KnowWE-DES, KnowWE-SSP kompilieren gegen Stufe 1. KRONE braucht `-Dskip.installnodenpm -Dskip.npm -Dskip.pnpm` (Symlinks auf vboxsf).
