# KnowWE `Environment`: vom statischen Singleton zur Instanz

Plan, Stand, offene Punkte und Protokoll des Umbaus. Arbeitsumgebung `Projects/EnvInstance`, Branch `env-instance` in allen
beteiligten Repos (Liste unter "Arbeitsumgebung"). Stand: 2026-09-11. Nummernschema siehe
"Namensschema".

## Ziel

Jeder Test (und jede Anwendung) erzeugt seine Environment mit `new Environment(connector)` bzw. `Environment.create(...)`
und wirft sie weg oder schließt sie mit `close()`. Danach ist nichts von ihr übrig: kein statischer Zustand, keine Threads,
keine Listener. Ein zweites `Environment.initInstance()` im selben JVM erbt nichts. `Environment.getInstance()` verschwindet
vollständig; wer die Environment braucht, holt sie sich von dem Objekt, das er ohnehin in der Hand hat (`Section`,
`Article`, `UserContext`, `Compiler`, `ArticleManager`) oder bekommt sie beim Start übergeben.

Ergänzung vom 2026-09-07 nach Rücksprache: Ein expliziter Reset statischer Registries (wie in `S1` gebaut) ist nur ein
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
- Statischer Zustand außerhalb von `Environment` (Stand vor `S2`): `Section.sectionMap`, `CompilerManager.scriptManagers`,
  `SectionizerModule.Registry`, `Messages.sectionsWithMessages`, `TerminologyManager.occupiedTerms`, `ToolUtils.BLOCKED_PROVIDERS`,
  `Plugins.registeredRightPanelTabs`, `NotificationManager.globalNotifications`, `LongOperationUtils` (Pool + Map),
  `CompilationLocal`-Caches, `Scope`-Caches, `AttachmentUpdateMarkup`-Statics, Singleton-Typen (`PlainText`, `LineContent`,
  `DashTreeElementContent` + 21 in Plugins), Plugin-Singletons aus dem `PluginManager` (Compiler, EventListener, TagHandler,
  Instantiations), Commons-Singletons `EventManager`/`PluginManager`, JUL/MDC.
- Threads: gut 60 Erzeugungsstellen in den Repos (18 in KnowWE). Aufgeräumt werden nur 10 davon, über
  `ServletContextEventListener.registerOnContextDestroyedTask`, und das nur beim Servlet-Shutdown, in Tests nie.

### Statische Zustände im Detail (Stand vor `S2`)

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

## Namensschema

Jede Arbeitseinheit trägt eine eindeutige, punktierte ID `S<Stufe>[.<Paket>[.<Welle|Schritt>]]`. Die ID enthält
immer ihren Pfad, damit z. B. "Welle 3" nicht mehr zwischen Paket 3 und Punkt 10 mehrdeutig ist. Die alten
Bezeichnungen (Stufe/Paket/Punkt/Muster, Klammerzahlen) sind nur noch Aliasse.

| ID | alte Bezeichnung | Inhalt |
|---|---|---|
| `S1` | Stufe 1 | Rückverbindung + Reset-Provisorium |
| `S2` | Stufe 2 | Zustand und Threads gehören der Environment |
| `S2.1` | 2a | Registries in die Instanz |
| `S2.2` | 2b | Services + `close()` |
| `S2.3` | 2c | Singleton-Typen |
| `S2.4` | 2d | Plugin-Singletons pro Environment |
| `S2.5` | 2e | EventManager pro Environment |
| `S3` | Stufe 3 | Aufrufer migrieren |
| `S3.M1` | Muster 1 | Kontextobjekt vorhanden |
| `S3.M2` | Muster 2 | Statische Utilities |
| `S3.M3` | Muster 3 | Hintergrund-Tätigkeiten und Dienste |
| `S3.M4` | Muster 4 | JSPs |
| `S3.T1` | Tests | Tests auf `Environment.create`/`close` |
| `S3b` | Stufe 3b | `web`-Parameter entfernen |
| `S4` | Stufe 4 | Abschluss: statische Accessoren löschen |
| `S4.1` | Paket 1 | Listener-Singletons pro Environment |
| `S4.2` | Paket 2 | `requireInstance()`-Reststellen |
| `S4.2a` | Paket 2a | geplugte EventListener pro Environment |
| `S4.2b` | Paket 2b | statische Registries; Schritte `S4.2b.1`–`S4.2b.7b` |
| `S4.3` | Paket 3 / Designliste Punkt 8 | deprecated Überladungen + Tests; Wellen `S4.3.1`, `S4.3.3`-`S4.3.6`, Abschluss `S4.3.7` |
| `S4.4` | Designliste Punkt 9 | Experimental-Legacy |
| `S4.5` | Designliste Punkt 10 | statische Accessoren löschen; Wellen `S4.5.1`–`S4.5.3` |
| `S4.6` | Paket 4 | `web`-Reste, Rdf2GoCore-Thread, Consumer-Kompiliercheck |
| `S4.7` | – (neu) | statische Markup-Typ-Singletons auf Fabriken umstellen (`DefaultMarkup`) |

Regeln: Referenzen nennen die ID, den Klarnamen höchstens einmal daneben (`S4.3.4` = "Paket 3, Welle 4").
Wellen und Schritte werden nie ohne ihren Elternpfad zitiert; "Welle 3" allein gibt es nicht mehr.

## Plan

| ID | Inhalt | Umfang | Status |
|---|---|---|---|
| `S1` | Rückverbindung `ArticleManager.getEnvironment()`, `getInstance()` `@Deprecated`, `shutdown()` mit explizitem Reset (Provisorium, entfällt mit `S2`) | KnowWE-core | **erledigt** (9e19e1b30) |
| `S2` | **Environment besitzt allen Zustand und alle Threads.** `close()` statt `shutdown()` (seit 2026-10-14 bewusst nicht mehr `AutoCloseable`, s. Protokoll). Reset-Liste aus `S1` ist leer | KnowWE-core (+ Commons für `S2.5`) | **erledigt** (`S2.1`-`S2.4`: 56b2b2b63, Folgecommit; `S2.5`: 2026-09-09) |
| `S3` | Aufrufer migrieren nach vier Mustern `S3.M1`-`S3.M4` (s.u.), repo-weise: KnowWE, KnowWE-DES, KnowWE-SSP, Experimental, KRONE, Internals, IE | alle Consumer | `S3.M1` in allen Repos erledigt (2026-09-08), `S3.M2`-`S3.M4` offen |
| `S3b` | **`web`-Parameter entfernen.** Es gibt nur `DEFAULT_WEB`; der Parameter ist toter Ballast in Signaturen und Aufrufen. Eigener Durchgang nach `S3.M1`-`S3.M3` und vor dem Löschen von `getInstance()`, weil er dieselben Stellen anfasst | alle Repos | **erledigt** (`S3b.1`-`S3b.3` 2026-09-08; `S3b.4` 2026-09-11, s. u.) |
| `S4` | Abschluss: `getInstance()`, `initInstance()` und die übrigen statischen Accessoren löschen; Einstieg nur noch über den JSPWiki-Connector, der die Environment erzeugt und in den ServletContext legt; Testinfrastruktur auf `new Environment(...)` pro Test. Unterpakete `S4.1`-`S4.6` | alle, plus Kompilier-Check GINA, KMW, KnowledgeConverter, d3web-Player-Platform, SemanticServicePlatform | **erledigt** (`S4.1`-`S4.7`, `S3b.4`; nur noch Beobachtungen in „Offene Punkte") |

### `S2` im Detail

- **`S2.1` (2a) Registries in die Instanz.** Section-ID-Registry, ScriptManager, SectionizerModule-Registry, Messages-Index,
  occupiedTerms, blockierte ToolProvider, globale Notifications, LongOperations, AttachmentUpdate-Zustand werden Felder
  der Environment (bzw. des ArticleManagers/CompilerManagers, wo sie hingehören). Caches, die nur an Typ- oder
  Compiler-Instanzen hängen (`Scope`, `CompilationLocal`), werden schwach referenziert oder wandern an den CompilerManager.
  Statische Zugriffe bleiben als Delegation über den Kontext (Section -> ArticleManager -> Environment) bzw. übergangsweise
  über `getInstance()` erhalten.
- **`S2.2` (2b) Environment-scoped Services + `close()`.** `environment.getService(Class, Supplier)` als Ablage für alles, was
  Plugins heute statisch halten; Services, die `AutoCloseable` sind, werden beim `close()` geschlossen. `onClose(Runnable)`
  für Aufräumarbeiten. `close()` = laufende Kompilierung abwarten, `DeInitEvent`, `onClose`-Tasks rückwärts, Services
  schließen, Executor hart stoppen und Rest loggen. `ServletContextEventListener.contextDestroyed` ruft nur noch `close()`.
  Die 10 Nutzer von `registerOnContextDestroyedTask` wandern auf `onClose`/Services. JUnit-Rule/Extension, die eine
  Environment aufbaut und im `after` schließt.
- **`S2.3` (2c) Singleton-Typen.** `PlainText`, `LineContent`, `DashTreeElementContent` werden normale Typen (`new`), `getInstance()`
  entfällt (10/2/1 Aufrufer, keine Identitätsvergleiche). Die 21 Plugin-Singletons folgen in `S3` nach demselben Muster
  oder hängen ihren Zustand an `getService`.
- **`S2.4` (2d) Plugin-Singletons pro Environment.** Compiler aus dem Extension-Point per `getNewInstance()`, die Environment merkt
  sich ihre globalen Compiler (`isGlobalCompiler`). EventListener, die die Environment beim Start registriert, werden beim
  `close()` wieder abgemeldet (`EventManager.unregister`).
- **`S2.5` (2e) EventManager pro Environment.** Erledigt 2026-09-09. Flach, ohne Hierarchie: `EventManager` (Commons) hat einen
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

### `S3b`: `web` entfernen

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

Vorgehen (`S3b.1`-`S3b.4`):
1. (`S3b.1`) Kern-APIs: `web`-lose Varianten anbieten (`Environment.getArticleManager()`, `getArticle(title)`, `createTemporaryArticle(text, title)`,
   `TermUtils.getTermInfo(articleManager, ...)`, `Sections.ReplaceContext.of(articleManager, ...)`, Events ohne `web`), die `web`-Varianten deprecated.
   `AbstractHTMLTagHandler.renderHTML` bekommt eine Default-Variante ohne `web`; die TagHandler ziehen repo-weise nach.
2. (`S3b.2`) Aufrufer per Skript: `Environment.DEFAULT_WEB`-Argumente streichen, `x.getWeb()`-Argumente streichen, wo die Zielmethode eine `web`-lose Variante hat.
3. (`S3b.3`) Eigene Methoden mit `String web`-Parameter in den Plugins (rund 90) manuell oder per Skript um den Parameter kürzen.
4. (`S3b.4`) Zuletzt `getWeb()` aus `Section`, `Article`, `UserContext`, `ArticleManager` und `DEFAULT_WEB` löschen (Teil von `S4`).

Stand 2026-09-08 (abends), Schritte `S3b.1`-`S3b.3` umgesetzt:

- `S3b.1` (KnowWE-core): `Environment` hat genau einen `ArticleManager` (`getArticleManager()`, Feld statt Map pro Web; `getArticleManager(web)` deprecated, warnt bei fremdem Web), `getArticle(title)`, `getTerminologyManager(master)`, `DefaultArticleManager(environment)`, `Article.createTemporaryArticle(text, title[, root])`, `Sections.ReplaceContext.of(env, userCanEdit, author)`, `TermUtils.getTermInfo(articleManager, ...)`, `TermInfoSet.initTerm(articleManager, id)`, `ListSectionsRenderer.getAllSectionsOfType(articleManager, ...)`, `Attachment*Event(parent, file)`, `Attributes.getBrokerConstant()`, `WikiConnector.getAllArticles()`, `AbstractHTMLTagHandler.renderHTML(title, user, parameters, result)` (die `web`-Variante ist nicht mehr abstrakt, wirft `UnsupportedOperationException`, wenn keine der beiden überschrieben ist). Alle `web`-Varianten deprecated.
- `S3b.2` (Skript `web2_callers.py`): `getArticleManager(DEFAULT_WEB|x.getWeb())` -> `getArticleManager()`/`x.getArticleManager()`, `Compilers.getCompilerManager(...)` -> `x.getArticleManager().getCompilerManager()`, `getArticle(web, title)` -> `getArticle(title)`, temporäre Artikel, Events, TermInfo, PackageManager ohne `web`; 129 Dateien in fünf Repos.
- `S3b.3` (Skript `web3_methods.py`, iteriert bis Konvergenz): Methoden mit `String web`-Parameter bekommen `ArticleManager articleManager`, wenn alle `web`-Verwendungen im Rumpf umschreibbar sind (29 Methoden), lokale `String web = ctx.getWeb()` entfallen (32), TagHandler überschreiben `renderHTML(title, user, ...)` (11), Aufrufer (auch namensgleiche in anderen Repos) übergeben den ArticleManager. Falle: das Skript ordnet Aufrufer nur nach Methodennamen zu, `D3webUtils.getKnowledgeBase(web, title)` bekam so die Umschreibung von `KnowSECUtils.getKnowledgeBase(web)`; gelöst durch eine ArticleManager-Variante in `D3webUtils`.
- Falle temporäre Artikel (Albrechts Einwand): `Article.getArticleManager()` ist `@Nullable`, für temporäre Artikel (Vorschau, `createTemporaryArticle`) null; `section.getArticleManager()` ist als Ersatz für `getArticleManager(section.getWeb())` also falsch. Lösung: `Article` kennt seine `Environment` (auch temporär: `createTemporaryArticle(environment, text, title[, root])`, `Article.getEnvironment()`/`findEnvironment()`), `KnowWEUtils.findEnvironment(Section|Article)` läuft über den Artikel statt nur über den Manager, und `KnowWEUtils.getArticleManager(Section|Article)` liefert für temporäre Artikel den Manager der Environment. Alle Umschreibungen mit Section-/Article-Empfänger (30 Dateien) nutzen den Helfer; `Compilers.getCompilers(context, section, ...)` hatte durch die Umschreibung einen wirkungslosen Fallback, jetzt ebenfalls über den Helfer. Die parameterlosen `createTemporaryArticle(text, title)` sind transitional (aktuelle Environment) und deprecated.
- Bleibt (`S4`, `web` als String-Wert): `TestObjectContainer(web, ...)` der CI-TestObjectProvider, `web`-Request-Parameter in URLs/JS (`DefaultTool`, `TestCaseToolProvider`, `DebuggerQuestionRenderer`, `Attributes.WEB` in `AbstractUserContext.getWeb()`, `CommentRenderer`, `BoxRenderer`), `D3webUtils.getWebEnvironmentPath` (`$web$`-Platzhalter), `ProgressListenerProvider`-Schlüssel (KBB), `AttachmentType`-Event-Vergleich, `PrefixedTestCaseProvider`, Revisions-Plugin (Experimental, eigener `ArticleManager` pro Web), `Article.web`/`Section.getWeb()`/`UserContext.getWeb()`/`ArticleManager.getWeb()`, Log-Ausgaben mit Web.
- `S3b.4` (2026-09-11, KnowWE/DES/SSP/Experimental/KRONE, Entscheidung Albrecht: „Java voll weg, `KWikiWeb` in JS/JSP inert lassen"): `getWeb()` aus `Section`, `Article`, `UserContext`/`AbstractUserContext`, `ArticleManager`/`DefaultArticleManager`, `Sections.ReplaceContext`, `CompilerManager`, `AttachmentEvent` entfernt; `Environment.DEFAULT_WEB`, `getArticle(web,title)`, `getArticleManager(web)`, `getAllArticles(web)`, `getBrokerConstant(web)`, `Instantiation.init(String)`, `AbstractHTMLTagHandler.renderHTML(web,...)` und die `(web,...)`-Konstruktoren von `Attachment*Event`, `DefaultArticleManager`, `Sections.ReplaceContext` gelöscht; `Article`/`DefaultArticleManager` ohne `web`-Feld, `Article.equals/hashCode` nur noch über `title`. Aufrufer: URL-Builder geben `web=`/`KWikiWeb=` nicht mehr aus (Server liest es nicht mehr), `ProgressListenerProvider`/`RevisionManager`/`AbstractRevision`/`DatedRevision`/`UploadedRevision`/`KBB`-Methoden ohne `web`, `scanForFindings` u. a. ohne `web`. `Attributes.WEB` bleibt als Konstante, weil die JSPs (`KnowWE.jsp`, `DiaFluxEditor.jsp`, `JSDebugger.jsp`, `commonheader.jsp`, `UserBox.jsp`, Defi-`ViewTemplate.jsp`) das Request-Attribut noch setzen/lesen — es ist jetzt wirkungslos (inert). Die JSP-Scriptlets auf die neuen APIs umgestellt (`Environment.of(request)`, `context.getEnvironment()`, `Sections.get(context|env, id)`, `getArticle(title)`). Verifikation: `build-chain.sh KnowWE` grün, `run-consumer-tests.sh` KnowWE/DES/SSP/Experimental grün bis auf die bekannten Baselines (`GitVersioningFileProviderTest`, `GeneratorAppTest`); KRONE nur externe/netzabhängige Fehler (SAP-Shading, Windchill, ssh-Timer, `DeductionSolutionSpace*`-Baseline).

| Repo | `String web` | `Environment.DEFAULT_WEB` | `getWeb()` |
|---|---|---|---|
| KnowWE | 55 (davon ~40 deprecated Kern-Varianten) | 21 | 44 |
| KnowWE-DES | 6 | 4 | 13 |
| KnowWE-SSP | 1 | 1 | 6 |
| Experimental | 16 | 47 | 12 |
| KRONE | 1 | 0 | 2 |

Die `S3.M1`-Umschreibungen haben schon viele `DEFAULT_WEB`-Stellen entfernt (`getArticleManager(DEFAULT_WEB)` -> `ctx.getArticleManager()`);
neue Aufrufer sollen ab jetzt keine `web`-Variante mehr benutzen.

### `S3`: die vier Migrationsmuster

1. **`S3.M1` Kontext vorhanden** (rund 560 Stellen): `Environment.getInstance()` -> `context.getEnvironment()` (UserContext), `KnowWEUtils.getEnvironment(section|compiler|articleManager)`, `section.getArticleManager().getEnvironment()`,
   `context.getArticleManager().getEnvironment()`, `compiler.getCompilerManager().getArticleManager().getEnvironment()`.
   Meist reicht der `ArticleManager` selbst, die Environment ist nur für den `WikiConnector` nötig. Structural Replace.
2. **`S3.M2` Statische Utilities ohne Parameter** (`KnowWEUtils.getApplicationRootPath()`, `getConfigBundle()`, `Compilers.getCompilerManager(web)`,
   `KnowWEUtils.getArticleManager(web)`): Parameter einführen (`ArticleManager` oder `Environment`), Aufrufer nachziehen.
3. **`S3.M3` Hintergrund-Tätigkeiten** (CI-Builds, Attachment-Updates, Git-Scheduler, Task-Manager, Timer): Environment beim
   Start einfangen (Konstruktor/Feld), nicht zur Laufzeit holen. `Instantiation.init(String web)` -> `init(Environment)`.
4. **`S3.M4` JSPs** (9 Dateien): Environment aus dem ServletContext-Attribut, das der JSPWiki-Connector beim Start setzt.

Reihenfolge: KnowWE -> KnowWE-DES -> KnowWE-SSP -> Experimental -> KRONE -> Internals/IE. Die Repos müssen gleichzeitig
kompilieren; fehlende Cross-Repo-Artefakte kommen als Snapshots von repo.denkbares.com.

## Stand

**Abgeschlossen (Stand 2026-09-15).** Der Umbau ist strukturell durch: `Environment` ist pro Instanz, alle statischen
Accessoren (`getInstance`/`requireInstance`/`peekInstance`/`initInstance`/`isInitialized`) sind gelöscht, im Live-Code
gibt es 0 Aufrufe (nur noch auskommentierte Kommentare in KRONE/Experimental). Aktueller `main-local/master` ist in alle
Repos gemergt. Offen sind nur noch die Beobachtungen unten (`ArticleLifecycleFixture`-Reflection, Masters
Auth-Test-Debt aus dem Merge, Jackson-Versionsmix in KRONE) — nichts Strukturelles. Die früheren `- [ ]`-Häkchen
unter `S3`/`S4` waren historisch und sind nachgetragen.

- [x] Analyse und Plan (2026-09-07)
- [x] Arbeitsumgebung: Host-Share `EnvInstance` gemountet, 7 Repos geklont, Branch `env-instance`
- [x] Isoliertes Maven-Repo `~/.m2/env-instance-repo`, Kette gebaut (42/43 KnowWE-Module, ohne `KnowWE-App`)
- [x] Baseline: KnowWE-core-Tests grün (11 Klassen, 67 Tests, 0 Fehler, 12 übersprungen)
- [x] Plan-Dokument in KnowWE-core committet (18730a4d6)
- [x] `S1` (Stufe 1, 2026-09-07, Commit 9e19e1b30 auf env-instance, nicht gepusht)
  - `ArticleManager.getEnvironment()` + `DefaultArticleManager(Environment, String)`; alter Konstruktor deprecated (Experimental nutzt ihn noch in DatedRevision/UploadedRevision)
  - `Environment.getInstance()` deprecated; `shutdown()` wartet auf Kompilierung, feuert `DeInitEvent` bei gesetzter Instanz, ruft danach die neuen Reset-Methoden: `Section.clearSectionRegistry`, `CompilerManager.clearScriptManagers`, `SectionizerModule.Registry.clear`, `Messages.clearSectionsWithMessages`, `TerminologyManager.clearOccupiedTerms`, `ToolUtils.clearBlockedToolProviders`, `NotificationManager.clearGlobalNotifications`, `LongOperationUtils.shutdown`, `CompilationLocal.clearCaches`, `Scope.clearCaches`, `AttachmentUpdateMarkup.reset`, `PlainText/LineContent.resetInstance`, `DashTreeElementContent.resetDefaultInstance`
  - Nicht angefasst (bewusst): `Plugins.registeredRightPanelTabs` (eigener DeInit-Listener), `ServletContextEventListener`-Tasks (Servlet-Lebenszyklus), `Priority`, `CredentialProviders`, `WikiAttachments.TEMP_FOLDER`, Commons-Singletons `EventManager`/`PluginManager`, JUL/MDC
  - `EnvironmentLifecycleTest` in KnowWE-Plugin-Core (dafür GlobalTestUtils + ExtensionPoints als Test-Deps und maven-dependency-plugin für `target/dependencies/output.txt`)
  - Voller KnowWE-Build mit Tests (87 Module): grün bis auf Git-Provider-Tests in JSPWiki-Connector, siehe "Bekannte Baseline-Fehler"
- [x] `S2` (2a-2d 2026-09-08, 2e 2026-09-09)
  - [x] `S2.1` (2a) Registries in die Instanz (2026-09-08): `SectionRegistry` pro Environment (`Section.get(id)` delegiert übergangsweise an die aktuelle), ScriptManager pro Environment (Compile-Scripts gehören jetzt dem `AbstractType` und werden beim Dekorieren des Typbaums pro Environment indexiert, `registerCompileScripts`), Sectionizer-Module, `occupiedTerms`, Messages-Index, blockierte ToolProvider, globale Notifications, RightPanel-Tabs als Environment-Felder bzw. Services; `CompilationLocal`-Caches am `CompilerManager`; `Scope`-Typcache schwach referenziert
  - [x] `S2.2` (2b) Services + `close()` (2026-09-08): `Environment implements AutoCloseable` (2026-10-14 zurückgenommen), `getService(Class, Supplier)`, `onClose(Runnable)`; `close()` = Kompilierung abwarten, `DeInitEvent`, CompilerManager/AttachmentManager stoppen, Close-Tasks rückwärts, Services schließen. `LongOperations` und `AttachmentUpdates` sind Services mit eigenem Thread-Pool/Timer. `ServletContextEventListener.contextDestroyed` schließt die Environment. `shutdown()` ist deprecated und delegiert. Neue Helfer `KnowWEUtils.findEnvironment/getEnvironment(Section|ArticleManager|Compiler|UserContext)`. Transitional: `Environment.peekInstance()`/`requireInstance()` (deprecated) für die verbliebenen kontextlosen statischen Aufrufer
  - [x] `S2.3` (2c) Singleton-Typen (2026-09-08): `PlainText.getInstance()`, `LineContent.getInstance()`, `DashTreeElementContent.getDefaultInstance()` liefern jetzt eine Instanz pro Environment (über `getService`), ohne Environment eine frische. Kein Reset mehr in `close()`; die Reset-Liste aus `S1` ist leer. Die 21 Singleton-Typen in den Plugins folgen in `S3` nach demselben Muster.
  - [x] `S2.4` (2d) Plugin-Singletons pro Environment (2026-09-08): `Plugins.getCompilers()` instanziiert die geplugten Compiler pro Aufruf (`getNewInstance`), die Environment merkt sich ihre (`isGlobalCompiler`). Die von der Environment registrierten Plugin-EventListener werden beim `close()` wieder abgemeldet. TagHandler, Instantiations, SectionizerModule bleiben Plugin-Singletons (zustandslos bzw. Zustand über `getService`).
  - [x] `S2.5` (2e) EventManager pro Environment (2026-09-09): Commons `EventManager` mit öffentlichem Konstruktor + `close()`; `Environment.getEventManager()`, `KnowWEUtils.getEventManager(...)`, `KnowWEUtils.currentEventManager()` (deprecated). Umgeschrieben per Skript in KnowWE, KnowWE-DES, KnowWE-SSP, Experimental, KRONE; Rest `currentEventManager()` im Hauptcode: KnowWE 18, DES 8, SSP 4, KRONE 2 (Servlet-/JSPWiki-Ebene, `Rdf2GoCore`, Listener-Singletons). Offen: Listener-Singletons (siehe offene Punkte)
- [x] `S3` (erledigt: `S3.M1` 2026-09-08, `S3.M3` 2026-09-08, `S3.M4`/JSPs via `S3b.4` 2026-09-11, `S3.T1`/Tests via `S4.3.6` 2026-09-10)
  - [x] `S3.M1` (Muster 1, Kontextobjekt vorhanden) per Skript in KnowWE (c5b69d0e4), Experimental, KnowWE-DES, KnowWE-SSP, KRONE, Information-Extraction: ~470 Stellen. Ausgenommen: KnowWE-Plugin-Agents (KnowWE-DES, lokale Variablen statt Felder, manuell nachziehen)
  - [x] `S3.M2` (Muster 2) in KnowWE (2026-09-08, 9a2dfc2e5, b12b64604): statische Utilities bekommen eine Environment-/ArticleManager-Variante, die alte Signatur bleibt deprecated und delegiert an `Environment.requireInstance()`. Betroffen: `KnowWEUtils` (11 Methoden), `CIUtils`, `D3webUtils`, `FlowchartUtils`, `FlowchartToolMenuAction`, `TestCaseUtils`, `GraphDataBuilder`, `DownloadWikiZIPAction`, `Fingerprint`, `SnapshotAction`, `WikiFileProviderUtils`, `RecentChangesUtils`, `Plugins.addRendererToType`, `AttachmentUpdateMarkup.isAutoUpdatingActive`. `UserContext.getEnvironment()` als Default-Methode. Rest in KnowWE-main: 31 Stellen (Erzeuger `KnowWEPlugin`, `web`-Delegationen, Wertobjekte `WikiPageInfo`/`WikiAttachmentInfo`/`WikiAttachmentPair`/`WikiAttachmentResource`, `Rdf2GoCore`, `Rdf2GoUtils`, `Config`, `InitTerminologyHandler`, `FileChange`, `DefaultWikiTestObjectProvider`, `VersionMarkupType`, Test-Utilities)
  - [x] `S3.M2` in den Consumer-Repos (abgeschlossen mit `S4.3.3`-`S4.3.5`, deprecated Statics gelöscht in `S4.3.7`): Aufrufer der deprecated Statics auf die neuen Varianten (per Skript, wo Kontext vorhanden)
  - [x] `S3.M3` (Muster 3), Anfang (2026-09-08): `Instantiation.init(Environment)` als Default-Methode; `WikiSearchService` ist Environment-Service (`of(environment)`, AutoCloseable), `RepositoryInitializer`, `WikiSearchInstantiation`, `SnapshotDirectoryPermissionInstantiation` nutzen `init(Environment)`
  - [x] `S3.M3`, Instantiations (2026-09-08): alle 22 Implementierungen nutzen `init(Environment)`; `getInstance()` im `init` durch den Parameter ersetzt. Fallen: lokale Variable `environment` (AgentInstantiation, Parameter heißt dort `knowweEnvironment`), Substring-Treffer `AgentEnvironment.getInstance()`/`RetrievalEnvironment.getInstance()` beim Ersetzen, `web`-Nutzung im Body (ImplicationsConversionInstantiation -> `DEFAULT_WEB`)
  - [x] `S3.M3`, Dienste mit ServletContext-Hook (2026-09-08, 25feeffc4 u.a.): `CIBuildManager`, `InterWikiImportUpdateService`, `MaengelKatalogRemoteRefreshService` sind Environment-Services (`of(environment)`, AutoCloseable); SemanticCore-Repository-Shutdown über `environment.onClose` im `RepositoryInitializer`. Kein Nutzer von `registerOnContextDestroyedTask` außer KnowWE-core mehr.
  - [x] `S3.M3`, weitere Dienste (2026-09-08): `CBXProgressDownloadJob` (SSP) fängt die Environment seines Compilers ein, `WikiGitRuntime` (KRONE) mit Environment-Varianten, `ProductContextHandler` (KRONE) bekommt sie im Konstruktor, `McpOAuthServer.forWiki(environment, config)`, `TestDefinitionCompletionSource` reicht sie im Completion-Pfad durch
  - [x] `S3.M3`, Task-Manager (2026-09-08): `TaskContextManagerInstance.of(Environment|UserContext|ArticleManager)`, `set(env, manager)`, `peek(env)`; der Manager liegt als Environment-Service (privater `Holder`, AutoCloseable) und wird mit dem Environment geschlossen (`close()` = `shutdown()` + Listener abmelden). Extension-Point `TaskContextManager` pluggt jetzt eine `TaskContextManagerFactory` (KRONE: `KONAPTaskContextManagerFactory`). `BasicTaskContextManager`/`KONAPTaskContextManager` bekommen das Environment im Konstruktor, dazu `taskBasedWorkManagementIsActive(env)`, `getPathname(env, name)`, `PropertyUtil.*(env, ...)`, `VersionenWikiPageTaskHistory(env, ...)`, `VersionenFileTaskHistory(env)`, `JiraTaskLinker.register(env, service)` (Abmeldung per `onClose`), `Patches.findTaskAndCreatePatch(env, ...)`, `WorkContextGitTool(env, history)`; alte Signaturen deprecated. 46 von 47 produktiven `get()`-Stellen umgestellt (Rest: `TaskAgentTools`, Agents-Plugin). Wertobjekte `TaskContext`/`WorkItem` und die Hooks `TaskContextSessionListener` (Servlet) / `TaskAuthorizationManager` (JSPWiki) bleiben ohne Kontextobjekt (`requireInstance()`/`peekInstance()`, Muster 4).
  - [x] `S3.M3`, KONAP-COOM-Listener (2026-09-08): `ModelUpdatedEvent` trägt die Environment seines Modells (gesetzt aus dem Compiler in `ModelUpdateScript` und `VariableOrderingUpdateJob`); `CoomKonapListener` reicht sie durch alle statischen Helfer (`updateProduct(env, ...)`, `isKSWModeOn(env)`, Section-Resolver, UI-Dateien, Modell-Dump). `CoomDslExportFactory.exportCoomDsl(env, ...)`; `CoomReleaseArtifactEndpoint` bekommt die Environment von seiner Factory (`context`/`section`), `UploadCoomDslWorkflowStep` (ohne Aufrufer) im Konstruktor. KRONE-`Utils`: `getFeaturePageName(articleManager, ...)`, `getContentFromPropertiesFile(env, ...)` (`stripPrefix*` nennen `Environment` nur noch im Kommentar).
  - [x] `S3.M3`, `WDManager` (2026-09-08): Environment-Service statt statischem Singleton, `WDManager.of(environment|articleManager|section|userContext|compiler)`, `getInstance()` deprecated; `getCoomDomainNamespace(env)`, `getCoomDomainNamespacePrefixAbbreviation(env)`, `getMasterCompiler(env)` (die `CompilerManager`-Variante ist wieder die eigentliche Implementierung); `KONAPModels.get(env|section)` und `RdfContext.get(env|section)` sind jetzt die bevorzugten Zugriffe, die parameterlosen deprecated (vorher umgekehrt markiert!). Skript `wdmanager.py`: 99 Aufrufstellen über den Kontextparameter der umgebenden Methode, 4 weitere über Felder; 33 bleiben beim deprecated `getInstance()`/`getMasterCompiler()` (statische Helfer ohne Kontext: `Utils.stripPrefix(URI)`/`shortenPrefix`, `TableUtils`, `LockedDialogHelper`, `CollisionApi` (JAX-RS), Konverter, `ModelUpdateScript.notify` vor Ermittlung des Compilers, Tool-Provider mit `ModelSet`/`Product`).
  - [x] `S3.M3`, `ReleaseContextManager` (2026-09-08): Environment-Service statt statischem Singleton, `ReleaseContextManager.of(environment|userContext|articleManager|section|compiler)`, `set(env, manager)` für Tests; die geplugte Klasse wird pro Environment über `getNewInstance()` erzeugt und mit `init(Environment)` gebunden (ruft den bestehenden Hook `init(Properties)` mit den Wiki-Properties), `close()` meldet den Event-Listener ab. `getInstance()`/`registerInstance()` deprecated. `KONAPReleaseContextManager`: `serverStatus` ist Instanzfeld (war statisch), Properties/Pfade über die Environment; `ServerStatus.serialize(env)`/`deserialize(env)`. `KnowWeModelReleaseContext` kennt seine Environment (Konstruktor). Skript `releasemanager.py`: 40 Aufrufstellen über Kontextparameter, 10 bleiben deprecated (`TaskIntegrationRunService`, `DiffTasksWorkflowStep`, `CoreReleaseCommandApi`/`QueryApi` mit Command-Objekten, `ConfigitBridgeMarkupAdapter.prepare`, anonymer OutputStream in `VariableOrderingUpdateJob`).
  - [x] `S3.M3`, KnowWE-core-Rest (2026-09-08): der `WikiConnector` kennt seine Environment (`setEnvironment`/`getEnvironment`, gebunden im Environment-Konstruktor; Implementierungen `JSPWikiConnector`, `DummyConnector`); die Wertobjekte `WikiPageInfo`, `WikiAttachmentInfo`, `WikiAttachmentPair`, `WikiAttachmentResource` halten den Connector, der sie erzeugt hat (alte Konstruktoren deprecated). `FileChange.fromArticle(articleManager, ...)`, `VersionMarkupType.getApplicationManifest(env)`, `PackageCompileLinkToTermDefinitionProvider.getTerminologyManager(env, ...)`/`createBaseURL(env)`, `Rdf2GoUtils.getLocalNamespace(env)` (pro Environment berechnet statt statisch beim Klassenladen), `Sections.ReplaceContext` trägt die Environment, `Config` (Visualization) merkt sich die Environment der Section, `InitTerminologyHandler` nutzt den Compiler. Bewusst bleiben (Übergang, `peekInstance`/`requireInstance`): `KnowWEPlugin` (Erzeuger, `S4`), Test-Utilities, `Section.get(id)`, `Messages.getSectionsWithMessages()`, `KnowWEUtils.findEnvironment`-Fallback, `AbstractType`-Skriptindex, `LineContent`/`PlainText`/`DashTreeElementContent.getInstance()`, `Plugins.getRightPanelTabs()`, `DefaultWikiTestObjectProvider` (TestObjectProvider ohne Kontext), `D3webUtils.getD3webBundle(request)`, `Rdf2GoCore`-Konstruktor.
  - [x] `S3.M3`, Agents + COOM-Retrieval (2026-09-08, KnowWE-DES): `AgentEnvironment.of(env|session)`, `WikiChatSessionRegistry.of(env)`, `RetrievalEnvironment.of(env)` sind Environment-Services statt JVM-Singletons; `WikiAgentSession.getEnvironment()` (WikiChatSession bekommt sie im Konstruktor, McpSession über `requireInstance()` = Transport ohne Kontext); Tools nutzen `session.getEnvironment()` bzw. `WikiAgentTools.requireEnvironment(task, request)`/`environment(session)` (Preview ohne Session -> aktuelle Environment), `TaskAgentTools.manager(session)`/`taskManagementActive(session)`, `AbstractRetrievalAction.require*(context)`. Bewusst bleiben: Tool-Schemata, die beim Erzeugen des Tools Dashboards/Cores/Compiler aufzählen (`CiAgentTools.dashboardParameter`, `SparqlQueryTool.coreParameter`, `WikiTermTool.termCompilers`), `AgentInstantiation.Property` (Enum-Konstanten), Websocket/Serializer ohne Session-Referenz.
  - [x] `S3.M3`, SSP-Rest (2026-09-08): `MaengelKatalogRemoteStateRepository(env)` (vom `MaengelKatalogRemoteRefreshService` mit seiner Environment erzeugt), Suggestion-Repository über die Dashboard-Section, `ErrorCodeLabelEditor`, `ListPackagesMarkup`, `VirtualRealityHandler`, `MalfunctionImportType.getKB(connector, ...)`, `CachingAdapterStatementExporter.parseCache(env, ...)`, ServiceMate-`WikiConfiguration` über den UserContext.
  - [x] `S3.M3`, Markierung der Reststellen (2026-09-08): alle verbliebenen `Environment.getInstance()` im produktiven Code von KnowWE-DES, KnowWE-SSP, Experimental und KRONE stehen jetzt als `Environment.requireInstance()` (Skript `generic_rest.py`: 6 weitere Stellen über Kontextparameter, 133 markiert). `requireInstance()`/`peekInstance()` sind damit die vollständige Liste der Stellen ohne Kontextobjekt (Stand: KnowWE 64/16, KnowWE-DES 93/3, KnowWE-SSP 11/0, Experimental 57/0, KRONE 45/0, darin auch die deprecated Delegationen). `getInstance()` nur noch in KnowWE (`KnowWEPlugin`, Test-Utilities, `DummyConnector`-Kommentar) und drei KRONE-Dateien.
  - [x] `S3.M3`, Rest mit Designbedarf (abgelöst durch `S4.5`/`S4.5.2`/`S4.5.3`): KRONE: 5 Kopien von `getPageProvider()` (TaskIntegrationRunService, GetTasksToChooseRunService, ReleaseOverviewRenderer, CoreReleaseQueryApi, TaskLogDiffUtil) -> `WikiGitRuntime.getPageProvider(env)`, `TaskLogDiffUtil.getInstance()`, `SynchronizingRepositoryClient.getRepoClient()`, `SAPClient.createClientFromWikiConfig()`, `ConfigitBridgeAction.getXmlFileName(product)`, `RequestPriority`, JAX-RS/Filter (`CollisionApi`, `ApiWikiFilter`), KRONE-`PropertyUtil`-Aufrufer; KnowWE-DES: `CoomModelProvider` (statische Modell-Map, 46 Stellen), `CoomCompiler.findByName(name)`, `CoomConceptStore`, `TemporaryModelRegistry`, `GitLabExportAction`/`GitLabServiceFactory`, `CoomAgentTools.allCompilers/awaitCompilation`, Git-Server-Connector (`KnowWEGitServerConnector`, `GitWikiProperties`), MCP-`engine()`-Helfer, KnowSEC (`SessionManager`, `KnowSECSearchProvider`, `KnowSECUtils.getTitle`), CI-TestObjectProvider (`CoomCompilerTestProvider`, `CompilerModelTestProvider`, `RegressionTestProvider`, KRONE `KONAPModelTestObjectProvider`, `CBXAbstractTest`); Experimental: 57 Stellen in Legacy-Plugins (Defi, WissKont, OphtoVis, Baseline, ...); Quartz-Einstieg `CBXProgressDownloadJob.execute`; `Rdf2GoCore` (asynchroner Shutdown-Thread, siehe offene Punkte)
  - [x] `S3.M4` (Muster 4, JSPs, 9 Dateien; erledigt in `S3b.4`: Scriptlets auf `Environment.of(request)`/`context.getEnvironment()`/`Sections.get(...)`)
  - [x] `S3.T1` (Tests, 128 Stellen; erledigt in `S4.3.6`: Tests auf `Environment.create`/`close`, `TestUserContext(Environment,...)`): Umstieg auf `new Environment`/JUnit-Rule sobald `S4` den Konstruktor öffnet
- [x] `S4` (erledigt: `S4.1`-`S4.7`, `S3b.4`, Master-Merge 2026-09-15; statische Accessoren in `Environment` gelöscht, `getInstance()`/`requireInstance()`/`peekInstance()` nirgends mehr im Live-Code)
  - [x] `S4.1` (Paket 1), Listener-Singletons pro Environment (2026-09-09, KnowWE 258261b0e + Folgecommit, DES/SSP/KRONE): siehe offene Punkte. Dabei `Rdf2GoCore` mit Environment.
  - [x] `S4.2` (Paket 2), `requireInstance()`-Reststellen (2026-09-09, alle Repos außer Experimental): Muster siehe offene Punkte. Neu dabei: `WikiTestObjectProvider` (CI4KE) mit `getTestObjects(Environment, ...)`, der CI-Build bindet die geplugten Provider-Singletons an die Environment des Dashboards (`WikiTestObjectProvider.bind`); alle 8 Provider umgestellt (`RegressionTestContext` trägt die Environment). Websocket-Handshake legt die Environment in die Socket-Properties (`AgentWebSocketConfigurator`), MCP-Server/-Sessions/-Authentication holen sie aus Request bzw. Servlet-Context, Property-Enums (`AgentInstantiation`, `JiraInstantiation`, `GitLabServiceFactory`) lesen pro Environment (`value(env)`) statt einmal beim Klassenladen, `TaskAuthorizationManager` und `TaskContextSessionListener` über den ServletContext (`findIn`, tolerant vor dem Wiki-Init), `TaskContext.editPage(manager, ...)`, KRONE `getPageProvider()`-Kopien ersetzt durch `WikiGitRuntime.getPageProvider(env)`, Run-Services/`TaskLogDiffUtil`/`ReleaseUtils.pullMasterTTL` mit Environment. Verbleibende `requireInstance()` außerhalb deprecated Delegationen: KnowWE 6 (Test-Utility, Fallbacks), DES 19, SSP 2, KRONE 9 (siehe unten), Experimental 65 (Legacy-Plugins, bewusst nicht angefasst)
  - [x] `S4.2a` (Paket 2a), geplugte EventListener pro Environment (2026-09-09, Albrechts Vorschlag): `Environment.initEventManager()` erzeugt die geplugten Listener mit `extension.getNewInstance()` statt `getSingleton()`, jede Environment hat eigene Instanzen; Listener, die `de.knowwe.core.EnvironmentAware` implementieren, bekommen vor der Registrierung `attach(environment)`; `Environment.getListener(Class)` liefert die Instanz einer Environment (statt statischer `getInstance()`). Umgesetzt: KnowSEC `PeriodicForEverTask` (Environment als Feld, Timer endet per `onClose`, vorher lief bei jeder Re-Initialisierung ein weiterer Timer für immer), `NormanDbPollOperation.of(env)`/`SessionUpdateOperation.of(env)` als Environment-Services statt Singletons (statische Sammel-Sets in `NormanDbPollOperation` bewusst gelassen), Tool-Provider holen die Operation über die Environment ihrer Section; `ChangeLog` (OntologyDebugger, Modul ist im Reactor auskommentiert und die Listener-Registrierung in der plugin.xml ebenfalls, Plugin ist tot) nur mechanisch auf `EnvironmentAware`.
  - [x] `S4.2b.1` (Paket 2b (1)), Agenten mit Environment (2026-09-09): `Environment.create(WikiConnector)` aus `S4.3` vorgezogen (öffentliche Factory, gibt die initialisierte Environment zurück; `initInstance()` deprecated und delegiert; übergangsweise wird die neue Environment auch die aktuelle Instanz). `WikiAgent`-Implementierungen (COOM `CoomBaseAgent`, `CoomDeveloperAgent`, `CoomExplorerAgent`, `CoomDSLAgent`, `CoomBOSAgent`, `CoomSearchAgent`; MCP `McpExplorerAgent`; SSP `CBXBaseAgent`) bekommen die Environment im Konstruktor, `AgentInstantiation.parseAgent()` und `McpInstantiation.instantiate(env, className)` rufen `getDeclaredConstructor(Environment.class)`. Tools, deren Schema oder Beschreibung Wiki-Inhalte auflistet, halten `@NotNull Environment` (`WikiTermTool`, `CiStatusTool`, `CiRunTool`, `SparqlQueryTool`, `CoomCompileTool`, `CoomModelTool`, `CoomItemTool`, `CoomReasoningTool`, `CoomExportTool`), ebenso die Edit-Tools (`WikiWriteTool`, `WikiDeleteTool`, `WikiRenameTool`, Vorschau ohne Session); `WikiAgentTools.environment(session)` und `CoomAgentTools.allCompilers()` (ohne Environment) sind weg, `CoomAgentPrompts.createKnowWEContext(env, ...)` ohne statischen URL-Cache, `AgentSessionHierarchicalSerializer.serializeSession(WikiChatSession)` nimmt die Tools vom Agenten der Session. Tests (`CoomExplorerAgentTest`, `CoomReasoningToolTest`, `McpExplorerAgentTest`, `McpInstantiationTest`) starten eine echte Environment (`InitPluginManager.init()` + `Environment.create(new DummyConnector())`); dafür `KnowWE-GlobalTestUtils`, `maven-dependency-plugin` (JPF-Liste) und die im WAR nur `provided` vorhandenen APIs (`javax.websocket-api`, MCP zusätzlich `KnowWE-Plugin-Core`/`-ExtensionPoints`) als Test-Dependencies.
  - [x] `S4.2b.2` (Paket 2b (2)), statische Registries (2026-09-09): COOM `TemporaryModelRegistry.of(env|context)` und `ConfiguratorSessionManagement.of(env|context)` sind Environment-Services (Sessions und temporäre Modelle pro Wiki, Solver werden mit der Environment geschlossen, `CoomModelProvider.of(env)` statt der deprecated Statics), `GitLabServiceFactory.init(env)` legt seine Konfiguration als Service ab (`isConfigured(env)`, `createService(env)`); KRONE `RequestPriority.of(env)`/`defaults()` (Wiki-Property-Overrides pro Environment, `getInstance()` deprecated), `RestConfigitBridgeClient.createXxxRequest(RequestPriority, ...)` mit deprecated alten Überladungen, Aufrufer mit Section/Context/Adapter umgestellt (`BridgeBuilderConfig.getRequestPriority()` für Läufe aus dem Builder-Modul; Rest: `ConfigitProcessManager.startValidate(String, ...)`, `Check` ohne Konfiguration -> `S4.3`), `BridgeBuilderConfig` trägt den `WikiConnector` (aus `create(Section, ...)` bzw. gesetzt in `createVariantVTFiles`), `VariableOrderingBuilder` liest das VOF-Attachment darüber (ohne Connector keine Attachment-Reihenfolge).
  - [x] `S4.2b.4` (Paket 2b (4)), Interfaces ohne Kontextparameter (2026-09-09): `DefaultLogoAction.getDefaultLogoPath(UserActionContext)`/`getDarkModeLogoPath(UserActionContext)` (alle Ableitungen in DES, SSP, KRONE, Experimental; `KONAPLogoAction` liest die Wiki-Property über die Environment des Requests); `WikiPageCompletionProvider(Environment)` (implementiert das externe `com.denkbares.semanticautocompletion.CompletionProvider`, hat derzeit keinen Nutzer); `CoreReleaseQueryApi(Environment)` wird von `GetTasksToChooseAction` pro Aufruf erzeugt, `TasksToChooseQuery` ohne `nativeContext` (die Command-Seite `CoreReleaseCommandApi` nutzt `nativeContext` weiter als UserActionContext, das ist ein echter Kontext, kein Environment-Ersatz).
  - [x] `S4.2b.5` (Paket 2b (5)), CI-Tests ohne Kontext (2026-09-09): `CITestSpecification extends TestSpecification` (CI4KE) trägt die Environment des Dashboards; `CIDashboard` bindet alle Specifications im Konstruktor (`CITestSpecification.bind(env, specs)`, egal wer sie erzeugt hat, auch die CBX-Profile), `CIUtils.environment(specification)` liefert sie im Test. Die `customInfos` der `TestSpecification` waren ungeeignet, weil `TestExecutor.prepareExecution()` sie vor jedem Lauf leert; d3web-Testing bleibt unverändert. Umgestellt: KRONE `VariantDstdTestConfigTest` (überschreibt `execute(TestSpecification, ModelSet)`) und `VariantDstdTestConfigTestCoom` (Renderer über den `UserContext`). Die übrigen Klassen der ursprünglichen Liste (`CIDashboardTest`, `TableTest`, `HeadlessAppTest`, `DeductionSolutionSpaceTest*`, `ModelSetUpdate*Test`, `CompileScriptTest`, `HeadlessTest`, `BridgeBuilderHeadlessTest`) sind JUnit-Tests -> `S4.3`.
  - [x] `S4.2b.6` (Paket 2b (6)), `WorkItem` (2026-09-09): `getLastChangeVersionNumber(GitConnector)` bekommt das Git-Repository des Wikis vom Aufrufer (`BasicTaskContextManager.wikiGitConnector(environment)`), das JSON-Wertobjekt hat keinen Environment-Zugriff mehr; Aufrufer `BasicTaskContextManager.revertPage` und `Patches.createPatchForWorkItems`. Nebenfund KRONE `KONAP-Plugin-COOM`: die Pom schloss `KnowWE-Plugin-Agents` aus, seit 2026-09-05 zieht COOM aber `KnowWE-Plugin-MCP` (Runtime) mit, das Agents verlangt -> Plugin-Manager startete in den Modultests nicht; jetzt wird MCP ebenfalls ausgeschlossen. Danach sichtbar (vorher nie gelaufen, Ursache offen): `CoomModelWriter*Test` mit OutOfMemory (Referenz-Wiki, kein `-Xmx` in der Surefire-`argLine`) und `DeductionSolutionSpaceTestTest` erwartet 30 Ergebnisse, bekommt 22.
  - [x] `S4.2b.7a` (Paket 2b (7a)), KnowWE-Kern (2026-09-10): Typ-Singletons `PlainText.getInstance()`, `LineContent.getInstance()`, `DashTreeElementContent.getDefaultInstance()` gelöscht, überall `new X()` (Albrechts Entscheidung: die Singletons waren Performance-Optimierung ohne messbaren Nutzen, ein Typ pro Aufbau reicht); `ServletContextEventListener.contextDestroyed` schließt `Environment.findIn(servletContext)` statt der aktuellen Instanz; `Sections.get(Environment|UserContext, id[, Class])` neu, `Sections.get(id)`/`Section.get(id)` deprecated (269 Aufrufer, mechanisch in `S4.3`). Verbleibende `peekInstance()`-Stellen im Kern sind Fallbacks (`Article`, `DefaultArticleManager`, `KnowWEUtils.findEnvironment`, `currentEventManager`, alter `Rdf2GoCore`-Konstruktor) und deprecated Statics (`RootType.getInstance`, `CompilerManager.getScriptManagers`, `Messages.getSectionsWithMessages`, `VersionMarkupType.getApplicationManifest`, `Plugins`-Registrierungen) -> `S4.3`.
  - [x] `S4.2b.7b` (Paket 2b (7b)), Script-Index und Kern-Tests (2026-09-10): Compile-Scripts sind Konstruktor-Konfiguration eines Typs; der Index (`ScriptManager` pro Compiler-Klasse, Typ -> Prioritätsliste plus Teilbaum-Flags) wird einmal in `decorateTypeTree` gezogen und danach nicht mehr angepasst. Alle 35 produktiven `removeCompileScript`/`clearCompileScripts`-Aufrufer sitzen in Typ-Konstruktoren, also vor dem Indexieren. Der `peekInstance()`-Abgleich in `AbstractType` ist weg; `registerCompileScripts` setzt `indexed`, spätere Änderungen loggen eine Warnung. `Environment.create(WikiConnector, RootType)`: Typbaum aus dem übergebenen Root, Plugin-Schritte nur bei initialisiertem `PluginManager` (`pluginsAvailable()`), damit Kern-Tests unterhalb der Plugin-Schicht eine echte Environment fahren. `ArticleLifecycleFixture` nutzt `create(connector, root)` und `close()` statt Reflection auf die statischen Felder (Rest-Reflection nur noch in `closeManager` für `DefaultArticleManagerTest`/`LongOperationUtilsTest`, `S4.3`); `EnvironmentLifecycleTest` (Plugin-Core, mit Plugins) nutzt `create`/`close`.
  - [x] `S4.2b`, offene Punkte 8 bis 10 der Designliste (Punkt 8 deprecated Überladungen -> `S4.3.7`, Experimental -> `S4.4`, Ende von `requireInstance()`/`peekInstance()` -> `S4.5.3`): deprecated No-Arg-Überladungen (mechanisch, `S4.3`), Experimental, Ende von `requireInstance()`/`peekInstance()`.
  - [x] `S4.2`, Designbedarf (in `S4.5`/`S4.5.2`/`S4.5.3` abgearbeitet; Nachweis 2026-09-15: 0 Live-`requireInstance()`/`peekInstance()` in KnowWE/DES/SSP/KRONE/Experimental): Tool-Schemata der Agenten, die beim Bau der Tools Wiki-Inhalte auflisten (`CiAgentTools.dashboardParameter`, `SparqlQueryTool.coreParameter`, `WikiTermTool.termCompilers`, `CoomAgentTools.artifactParameter`, `CoomCompileTool`-Beschreibung, `CoomAgentPrompts.getRelativeOrigin`): Tools werden von Agenten im Konstruktor ohne Environment erzeugt, `WikiAgentTool.parameters()` hat keinen Kontext; `AgentSessionHierarchicalSerializer.findTool`; `McpSession` ohne Environment in Tests; `WorkItem.sanitizeVersionNumberHistory` (JSON-Wertobjekt braucht den Git-Pfad); `TemporaryModelRegistry` (statische Registry); KnowSEC `NormanDbPoller`/`SessionUpdateTask.getLongOperationSection()` und `KnowSECUtils` mit `section == null`; `ChangeLog` (Singleton-Listener); `WikiPageCompletionProvider.getCompletions()` und `TestDefinitionCompletionSource`-Konstruktor (Vervollständigungsquellen ohne Kontext); `GitWikiProperties`/statische Git-Helfer in `VersioningAction` (Git-Server-Connector); `WikiAttachmentDownloadUpdate` (ohne Aufrufer); KRONE `RequestPriority`, `VariableOrderingBuilder` (ConfigitBridgeBuilder liest Attachments), `VariantDstdTestConfigTest*` (CI-Tests mit ModelSet als Testobjekt), `KONAPLogoAction`, `CoreReleaseQueryApi` (nativeContext ohne Typ), `GitLabServiceFactory` (statische Konfiguration aus `Instantiation.init`); Experimental-Legacy komplett
  - [x] `S4.3` (Paket 3 = Designliste Punkt 8, Wellen `S4.3.1`-`S4.3.7` abgeschlossen): `S4.3.1`, `S4.3.3`-`S4.3.6`; Welle 2 entfiel): Aufrufer der deprecated No-Arg-Überladungen auf Environment/Kontext umstellen, dann die Überladungen löschen; Tests auf `Environment.create`/`close`; zuletzt `getInstance()`/`initInstance()`/`requireInstance()`/`peekInstance()` löschen. Inventur per `mvn compile -Dmaven.compiler.showDeprecation=true` (Quellen vorher `touch`en, sonst kompiliert javac nicht; das javac-Warnlimit war kein Problem, `-Xmaxwarns` als `compilerArgument` löst dagegen einen `ConcurrentModificationException`-Compilerfehler aus).
    - [x] `S4.3.1` (Welle 1) KnowWE (2026-09-10, 402193428): `Sections.get(id)` -> `Sections.get(env|context, id)` (69 Stellen, Skript `depr_convert.py` + 22 Handstellen), `KnowWEUtils`-No-Arg-Helfer, `RootType.getInstance`, web-basierte Manager-Lookups, `KnowWEPlugin` hält die Environment seines Wikis (`Environment.findIn(servletContext)` sonst `create`), CI-SPARQL-Helfer/Provider mit Environment (alte Signaturen deprecated für Konsumenten), `InterWikiImportUpdateService(Environment)`, `WikiGitContext` feuert am Manager der Environment des Servlet-Kontexts (Fallback global vor Start), `ResultRenderer.renderResultTitle(UserContext, ...)`, `ServletContextEventListener` nutzt die Environment des Servlet-Kontexts. Rest in KnowWE: Fallbacks/deprecated Bodies (`S4.3`, Ende), `Sections.get(id)` in Kern-Tests (`S4.3.6`), `TestArticleManager`/`TestUserContext`/`WikiPageInfoFull` in GlobalTestUtils (`S4.3.6`), `D3webUtils`-web-Helfer (`S4.6`), fremde Deprecations (PackageManager, Compilers.getCompiler(Article), D3webUtils.getKnowledgeBase(ArticleManager, String)).
    - [x] `S4.3.3` (Welle 3) KnowWE-DES (2026-09-10, 887ab5374): 130 Stellen; `Sections.get`, `CoomCompiler.findByName(articleManager, name)`, `CoomModelProvider.of(env)`, `CoomSections.getModelEngine/getGraphBuilder(env, ...)` (alte deprecated), KnowSEC-Memo-Helfer mit Environment, `GitWikiProperties.*(env)`, Git-Release-Statics mit Environment, geplugte Listener (`ModelSelectorListener`, `CoomOntologyListener`) `EnvironmentAware`, `AsyncGitPushEventListener(env)`, `TestDefinitionCompletionSource` baut Templates lazy mit der Environment des ersten Requests, `KnowSECUtils.Referrer(env, ...)`, `HeadlessKnowWE` per `create`. Rest: `McpSession`/`KnowSECUtils`-Fallbacks, `PackageManager.getCompilingArticles` (fremd), `TimeChartRenderer`/`Journal*`/`KBB*` (`D3webUtils.getKnowledgeBase`, fremd), `ImageMapRenderer` (`Compilers.getCompilingArticles`, fremd).
    - [x] `S4.3.4` (Welle 4) KRONE (2026-09-10, d9c24f3f4): eigene Stellen ~110 umgestellt (die ~500 Deprecations zu `makeKcvUri`/`stripPrefix`/`kcv` sind KRONE-eigene, ältere Deprecations "use in tests only" und nicht Teil dieses Projekts). Offen nach der ersten Runde: `SynchronizingRepositoryClient` (statische Gitea-Konfiguration aus `PropertyUtil`, braucht `of(env)`), statische `WDManager.getInstance()`-Helfer in Konvertern/Static-Utilities, `WorkContextGitTool`-/`ReleaseUtils`-Static-Wrapper, `GitCommentTaskStrategy`, `ReleaseStatusMessageQueue`, `DeductionCompletenessTestCoom`, `KONAPQSReportProvider`, `CreatePageAction.doesIdentifierExist`, `Utils.stripPrefix(URI)`/`shortenPrefix(String)` (WDManager-Singleton).
    - [x] `S4.3.5` (Welle 5) KnowWE-SSP (2026-09-10, 854450332): 89 Stellen, davon 41 `Sections.get`; `CBXBaseAgent(Environment)`, Kontext-Threading in den TSM/SSC-Actions, `WikiAttachmentDownloadUpdate` gelöscht.
    - [x] `S4.3.6` (Welle 6) Tests auf `Environment.create`/`close` (Skript `test_env_convert.py`, Scratchpad; hebt `isInitialized`-Guards auf, ersetzt `initInstance`/`getInstance` durch ein Feld `environment` und ergänzt `@After`/`@AfterClass` mit `close()`; Klassendeklaration muss `public` sein, sonst Feld von Hand):
        - [x] KnowWE (2026-09-10, 254edafe1): `TestArticleManager.getEnvironment()` erzeugt lazy eine gemeinsame Environment, `TestUserContext(Environment, article[, user[, root]])`, 17 Tests umgestellt, `EnvironmentLifecycleTest` auf `create`/`close`, `Config(Environment)` für Konfigurationen ohne Section (OntoVis-Test). Voller Testlauf grün (449).
        - [x] KnowWE-DES (2026-09-10, f20de7783): 23 Tests per Skript, `TaskTestUtils.init(Environment)`, Versioning-Git-Tests holen die Environment per `Environment.of(engine.getServletContext())` aus der TestEngine, `TestCompileMarkup` schlüsselt seine Test-KB nach der Environment des Compilers. Voller Lauf 415 Tests, nur die bekannten GeneratorAppTest-Fehler. Echte Regression gefunden: `RevertAndApplyOpenPatchTest` hielt nach dem Helfer aus `CreatePatchTest` eine geschlossene Environment im eigenen statischen Feld (früher verdeckt durch `getInstance()`), jetzt übernimmt der Test die Environment des Kontexts.
        - [x] KnowWE-SSP (2026-09-10, aecaaa7a1): 6 Tests, `ConceptStoreTest` (JUnit 5) mit `@AfterAll`. Voller Lauf grün (127).
        - [x] KRONE (2026-09-10, 2189c39b4): 13 Tests; Headless-Tests nehmen `HeadlessKnowWE.getInstance().getEnvironment()`, `KONAPTaskContextManagerTest` baut seine Testkontexte nach dem `create`. Tests der 6 betroffenen Module: grün bis auf Baseline (`CoomModelWriter*Test` OOM, `DeductionSolutionSpaceTestTest` 30/22, `BridgeBuilderHeadlessTest` vergleicht zufällige VersionID und Hostname `MacBook-Pro-von-Jochen` aus dem Erwartungsfile).
    - [x] `S4.3.7` (deprecated Überladungen gelöscht, 2026-09-10): KnowWE c4aed1b1e + 8c7a65c4a (72 Member; Sections-Replace-Flow auf der Environment des ReplaceContext, Link-/Property-Helfer mit ArticleManager/Environment, `WikiPageInfoFull` ohne Connector, `WikiTestObjectProvider` muss gebunden sein, `ActionContext`/`JSPWikiUserContext` kennen ihre Environment aus ServletContext bzw. Engine, `AbstractUserContext.getEnvironment()` abstrakt (die Default-Herleitung über den ArticleManager rekursierte), `AbstractType` prüft `indexed` statt `Environment.isInitialized()`, `TestUtils.createTestActionContext(env, ...)`), KnowWE-DES 9ac31a3fb (40 Member; Versioning-Git-Helfer mit Kontext, `HeadlessKnowWE.customizeRootType` für Test-Markups, Task-Tests mit `TaskContextManagerInstance.of/set(env)`), KnowWE-SSP 95f58b3fb (Skript `link_callers.py` für `getWikiLink`-Aufrufer, `ProgressFilesReader` mit ArticleManager), KRONE 6930844ae (ConfigitBridgeBuilder-Factories nur noch mit `RequestPriority`, `Check` mit Priorität, `InstanceIdentity` liest das Manifest aus dem ServletContext des `ContextListener`, SAP `KONAPModels` ohne No-Arg-Varianten). Tests: KnowWE 449, DES 411 (+Module 175), SSP 127, KRONE-Module 190, alles grün bis auf die Baseline. Experimental kompiliert in 10 Legacy-Plugins nicht mehr (Comment, Renaming, DiaFlux-Refactoring/-Coverage/-Review, IncrementalCompilerTest, WikiBaseline, d3web-Visualizations, Revisions, TypeVis; ~22 Stellen: `Sections.get(id)`, `FlowchartUtils.findFlowchartSection(web, name)`, `getURLLink(title, version)`, `getArticleVersionAtDate`, `getArticleManager(web)`, `RootType.getInstance()`) = `S4.4`. Bewusst behalten (Rest für `S4.5`): KRONE `WDManager.getInstance()`-Statics, `RdfContext.get()`, `wd.model.KONAPModels.get()`, alle ReleaseManagement-Wrapper (`WikiGitRuntime`, `WorkContextGitTool`, `KONAPTaskContextManager`-Ctors, `ReleaseContextManager`), `SynchronizingRepositoryClient` (statische Gitea-Konfiguration über `PropertyUtil`-No-Arg-Überladungen, die in DES dafür wieder da sind), `Utils.stripPrefix`-Familie (KRONE-eigene Deprecations).
  - [x] `S4.4` (Designliste Punkt 9, 2026-09-10, Experimental f64f4e418): Albrechts Entscheidung: alle Plugins, die noch Module im Parent sind, leben und wurden umgestellt (Comment, Renaming, DiaFlux-Refactoring/-Coverage/-Review, IncrementalCompilerTest, WikiBaseline, d3web-Visualizations, Revisions, TypeVis, Defi); Experimental-Tests grün (94). Defi arbeitet weiter durchgehend auf `Environment.requireInstance()` (65 Stellen in Experimental insgesamt) = `S4.5`.
  - [x] `S4.5` (Designliste Punkt 10, seit 2026-09-11 in Wellen `S4.5.1`-`S4.5.3`): `S4.5.3` (Welle 3, 2026-09-11): die statischen Accessoren sind aus `Environment` gelöscht (`getInstance/requireInstance/peekInstance/initInstance/isInitialized`, statisches `instance`, `retrieveLatestVersionNumber`); `create` schließt keine vorherige Environment mehr, mehrere laufen nebeneinander (`EnvironmentLifecycleTest.environmentsRunSideBySide`). KnowWE: `GitCommentStrategy.init(Engine)`-Hook (Strategien werden per Klassenname reflektiv erzeugt), `ExtensibleLuceneSearchProvider` attacht `EnvironmentAware`-Suchprovider, `GoToDefinitionAction` instanziiert den IncrementalCompiler-Provider mit Environment, `KBRenderer` (stale Modul) ohne `getTerminologyManager(title)`. SSP: `WikiDanUpdate`/`WikiOntologyUpdate`/`WikiKnowledgeBaseUpdate` holen die Environment über das neue Interface `WikiOntologyBuilder`, das der `Ontology`-Builder des `RunPreprocessorToolProvider` implementiert. DES: `PropertyUtil`-No-Arg-Überladungen gelöscht. KRONE: `SynchronizingRepositoryClient.of(env)` und `TaskLogDiffUtil.getInstance(env)` als Environment-Services, alle ReleaseManagement-Wrapper gelöscht, `GitCommentTaskStrategy` über `init(Engine)`, `WDManager`-No-Arg-Statics/`RdfContext.get()`/`KONAPModels.get()` gelöscht, `Utils.shortenPrefix`/`stripPrefix` nur noch mit Kontext (`masterCore(env|section|user|compiler)`), 169 Aufrufer in 69 Dateien plus ~45 `CreateWd*PageTool(Provider)` mit `WDManager` (KnowWE-Plugin-KONAP, Wissensdienste, Integration per Subagenten); `AbstractSymbolicDeductionRdfCompileScript.isValidTargetVariable(IRI, Rdf2GoCore)`; `LanguageManagerJsonConverter` prüft `Ontology.kcv` statt der konfigurierten Domain-Namespace (kein Kontext in `JsonConverter.convertToWikiSource`, äquivalent in allen nicht-werfenden Fällen). KONAP-ProductMigration (standalone `main()`-Werkzeug ohne Environment) nutzt `shortenPrefixKcv`/`stripPrefixKcv` statt des Master-Cores (alle Eingaben sind kcv-URIs). Experimental: 86 Dateien (Defi-Utilities mit `Environment`-Parameter, Listener `EnvironmentAware`, `DefiSessionEventListener` registriert sich in `attach`, `AboutMe` in `registerCompileScripts(env)`, Defi-JSPs feuern am EventManager der Environment). Nicht kompiliert (aus dem Parent auskommentiert): RDFS-*, WissKont, OphtoVis(D3), LexiconInterface, dort nur textuell umgestellt. Consumer-Repos: GINA, KMW, KnowledgeConverter, d3web-Player-Platform importieren kein `de.knowwe` (kein Check nötig); SemanticServicePlatform (TSMExport, 16 Dateien) nutzt keine gelöschten Accessoren, Kompilier-Check offen (`S4.6`). `S4.5.1` (Welle 1) KnowWE (f228f27dd): alle Live-Fallbacks auf die aktuelle Environment entfernt (`DefaultArticleManager` nur noch mit Environment, `Config`/`PackageCompileLinkToTermDefinitionProvider` mit Environment, temporäre Artikel nur noch `createTemporaryArticle(environment, ...)`, `Rdf2GoCore` ohne Environment ist standalone, `Plugins.getRightPanelTabs(env)`, `Messages.getSectionsWithMessages(env, ...)`, `Sections.replaceAsSystem(env, ...)`, `Environment.isReady()` statt statischem `isInitialized()` in `Article`; Script-Compiler holen ihren ScriptManager lazy, weil Compiler vor `init(CompilerManager)` konstruiert werden). `S4.5.2` (Welle 2) DES/SSP/KRONE/Experimental (2026-09-11, DES 089d51176, SSP 7c9e4e139, KRONE 36819b549, Experimental f58a7dbb8; Tests: KnowWE 449, DES-Module MCP/KnowSEC/GrammarParser, SSP 127, KRONE-Module 84+14, Experimental 94 grün, KRONE-COOM Baseline unverändert): `McpSession(s)` mit @NotNull Environment, `KnowSECUtils` ohne Null-Section-Fallbacks, `SSCHierarchyProvider` ohne Compiler liefert null, `ServiceMateBridge.registerBridge(env, ...)`, KRONE `ReleaseStatusMessageQueue` feuert am EventManager seines `ReleaseContextManager`. Bewusst transitional gelassen (mit Kommentar): SSP `WikiDanUpdate`/`WikiKnowledgeBaseUpdate`/`WikiOntologyUpdate` (per Name konfigurierte Update-Hooks ohne Wiki-Bezug). Falle: Maven kompiliert unveränderte Module nicht neu, API-Brüche in ihnen bleiben unsichtbar; vor jeder Welle alle Quellen `touch`en (so kamen `ServiceMateBridge`, `TSMElementLine`, `ReleaseStatusMessageQueue`, `GrammarParserTest` erst jetzt hoch). Zweite Falle: das Shade-Bundle `KnowWE-Plugin-ProductReleaseManagement` shadet sein eigenes altes `target/*.jar` mit und konserviert so veraltete Klassen (NoSuchMethodError in `KONAPTaskContextManagerTest`); Abhilfe: dessen `target` löschen und neu installieren. Restliste für `S4.5.3`: KRONE `WDManager.getInstance()`-Statics (~25 Aufrufer), `RdfContext.get()`, `wd.model.KONAPModels.get()`, ReleaseManagement-Wrapper (`WikiGitRuntime`, `WorkContextGitTool`, `KONAPTaskContextManager`-Ctors, `ReleaseContextManager.getInstance/registerInstance`, `ServerStatus.serialize()`, `EndCreateReleaseOperation`/`ReleaseAction.getGitConnector()`, `TaskLogDiffUtil`), `SynchronizingRepositoryClient` (statische Gitea-Konfiguration → Environment-Service, danach DES `PropertyUtil`-No-Arg-Überladungen löschen); SSP `Wiki*Update`-Hooks; Experimental (~30 Stellen, v. a. Defi, WissKont, Baseline, Revisions-Tests); zuletzt die Accessoren in `Environment` selbst plus `EnvironmentLifecycleTest`-Singleton-Assertions und Kompilier-Check der Consumer-Repos.
  - [x] `S4.6` (Paket 4), `web`-Reste, `getWeb()`/`DEFAULT_WEB`, Rdf2GoCore-Shutdown-Thread, Kompilier-Check GINA, KMW, KnowledgeConverter, d3web-Player-Platform, SemanticServicePlatform (2026-09-11). Erledigt: `S3b.4` (`web`/`getWeb`/`DEFAULT_WEB` aus Java entfernt, JSPs umgestellt, `Attributes.WEB` inert belassen) und Rdf2GoCore synchroner Shutdown bei schließender Environment; Consumer-Check per Inspektion: GINA/KMW/KnowledgeConverter/d3web-Player-Platform importieren kein `de.knowwe`, SemanticServicePlatform nur `de.knowwe.export.*` (`TSMExport`, aus `denkbares-WikiExport`, unverändert) – kein Kompilierlauf nötig. Tests KnowWE/DES/SSP/Experimental grün bis auf Baselines (`GitVersioningFileProviderTest`, `GeneratorAppTest`); KRONE nur externe/netzabhängige Fehler.
  - [x] `S4.7` (neu, 2026-09-11): **Statische Markup-Typ-Singletons auf Fabriken umstellen.** Befund: `DefaultMarkupType`-Subklassen halten ihr `DefaultMarkup` in einem statischen Feld; das Markup enthält `Type`-Instanzen (`addContentType`, `addAnnotationContentType`, `addAnnotationNameType`). `Type` ist eine mutierbare KDOM-Zustandsmaschine (`childrenTypes`, `parents`, `successorTypes`, `indexed`), wird aber über alle Environments geteilt. Beim zweiten `Environment.create` in derselben JVM dekoriert `Plugins.addChildrenTypesToType` dieselbe Instanz → `AbstractType.checkInitializationStatus` schlägt fehl (`type X must not change after an environment has indexed it`); zusätzlich vermischen sich Eltern/Kinder zwischen Environments. Reproduziert über `CoomReasoningToolTest` (DES, isoliert grün, in der Modulsammelausführung rot). Lösung: `DefaultMarkup` hält `Supplier<Type>` (statt `Type`), `getTypes()`/`Annotation.getContentTypes()/getNameTypes()` erzeugen die Typen pro `DefaultMarkupType`-Aufbau frisch. Die Alt-Überladungen `(Type)` bleiben (delegieren auf `() -> type`) und werden nach dem Sweep deprecated/entfernt. Aufwand über alle Repos (Stand 2026-09-11):
    | Repo | Dateien mit statischem Markup + Typ-Einbettung | Registrierungen gesamt | inline `new` (Script) | Variable/mehrzeilig (manuell) |
    |---|---|---|---|---|
    | KnowWE | 43 | 95 | 56 | 33 |
    | KnowWE-DES | 31 | 53 | 38 | 15 |
    | KnowWE-SSP | 56 | 194 | 141 | 53 |
    | Experimental | 16 | 16 | 15 | 1 |
    | KRONE | 57 | 75 | 30 | 45 |
    | **Summe** | **203** | **433** | **280** | **147** |
    - [x] Mechanismus in `DefaultMarkup` (KnowWE-core): `Supplier<Type>`-Felder, Fabrik-Überladungen, Alt-Überladungen; Beispiel umgestellt (`SparqlMarkupType`, `SparqlConsoleMarkupType`, `TableType`, `AbstractionTableMarkup`).
    - [x] Sweep KnowWE (2026-09-11, 46 Dateien): 54 inline per Skript (`s4_7_inline.py`, `addContentType(new X(...))` -> `() -> new X(...)`), 33 Variable-Fälle manuell in Block-Lambdas bzw. Fabrikmethoden (`createContent()`/`createContentTable()`), u.a. `RuleTableMarkup`, `PropertyTableType`, `HierarchyTableMarkup`, `OntologyTableMarkup`, `OntologyExcelTableMarkup`, die `OntologyLineType`-Markups, `AttachmentMarkup`, `PackageMarkupType`, `ResourceType`, `DescriptionMarkup`. Nachweis: `KnowWE-Plugin-COOM` (DES) 68 Tests grün, die `must not change after an environment has indexed it`-Assertion ist weg. Bewusst nicht umgestellt: `SparqlVisualizationType` (baut sein Markup pro Instanz) und `Plugins.addAnnotations` (legt die Annotation pro Dekorationslauf neu an). Sonderfall **rekursiver Typ**: `PredicateObjectSentenceList` referenziert sich über `PredicateSentence -> ObjectList -> BlankNode` selbst; der Singleton setzt die Instanz vor `init()` und bricht so die Rekursion. Eine per-Environment-Fabrik rekurriert unendlich (Stack Overflow) und ist daher nicht möglich — bleibt vorerst Singleton (Kommentar im Code), braucht einen Mechanismus für rekursive Typen (z.B. Instanz vor Kinderaufbau registrieren).
    - [x] Sweep KnowWE-DES (2026-09-11, 30 Dateien, 3b5826841): 40 inline per Skript + 15 Variable-Fälle manuell (`TaskLogMarkup`, `VariableType`/`ConstantType` inkl. `AbstractAssignmentType.createSeparatorType`, `PartsType`, `ImageMapType`); Nachweis: voller DES-Lauf nur `GeneratorAppTest` (Baseline) rot, `KnowWE-Plugin-COOM` 68 Tests grün.
    - [x] Sweep KnowWE-SSP (2026-09-11, 46 Dateien, 5323f192f): 141 inline per Skript (40 Dateien) + 12 Variable-Fälle manuell (`CableMarkup`/`TestSocketMarkup`/`AKSTableMarkup`/`MVTableMarkup` inkl. `Types.successors`, `TSMCoveringMarkup`, `MalfunctionMarkup`, `IgnoredTestStepQuestionMarkup`, `NonStateActivities`/`SocketInRotatingArea`/`NonSolutionElements`, `IgnoreSignalNameTemplate`, `ItsSolutionProgress`, `SignalTemplate`, Adapter-/WiringImport). Voller Lauf grün (127).
    - [x] Sweep Experimental (2026-09-11, 16 Dateien, 7d3cc6804): 15 inline + 1 Variable-Fall (`WisskontSolutionPanel`); grün (94). Achtung Zeilenenden: 11 Dateien mit CRLF; das Skript wurde auf `newline=""` umgestellt, nachdem ein erster Lauf die Zeilenenden normalisiert hatte (Commit zurückgesetzt).
    - [x] Sweep KRONE (2026-09-11, 57 Dateien, f5e871c48): 30 inline + 33 uniforme Nachbarfälle per Skript (`s4_7_adjacent.py`) + 12 Variable-Fälle manuell (`Annotations.Annotation` auf `Supplier<T>` mit `createContentType()`, `WDMarkup`/`ProductMarkup` PackageRule, Feature-/Value-Attribut-Markups, `FeatureValueMarkup`, `UnitTableMarkup`, `GruppierungsmodulMarkup`). Tests der 15 geänderten Module grün (202).
    - [x] Singleton-Typen (2026-09-11, KnowWE b34bffe17, DES 26bd336c6, SSP 6e379ea6c): `UnrecognizedSyntaxType` (44 Nutzer) und `EndLineComment` (22) haben öffentliche Konstruktoren, `getInstance()` ist deprecated und liefert `new X()`; `CBX.SPLIT_TOKEN_TYPE`/`_WITH_SPACE` sind Fabrikmethoden (`CBX.splitTokenType()`/`splitTokenTypeWithSpace()`, im Markup `addContentType(CBX::splitTokenType)`). Tests KnowWE/DES/SSP grün bis auf Baseline (`GitVersioningFileProviderTest.testRollback`, `GeneratorAppTest`). `run-consumer-tests.sh` überspringt jetzt auch `*Benchmark*` (`GitVersioningBenchmarkTest` lief in den 15-min-Timeout).
    - [x] Rekursive Typen (2026-09-11, KnowWE 0430c0b8b): `de.knowwe.core.kdom.RecursiveType.create(ThreadLocal, ctor, init)` registriert die Instanz vor dem Kinderaufbau; `PredicateObjectSentenceList` (über `BlankNode`/`TurtleSPOSentence`) und `TurtleCollection` (über `CollectionItem`) erzeugen ihre Instanz per `create()` und sind damit auf einen Typbaum (Environment) gescopt statt JVM-weit. Nachweis: KnowWE-Lauf grün bis auf `GitVersioningFileProviderTest` (Baseline), DES-COOM 68 Tests grün, keine `must not change`-Assertion.
    - [x] Nachzügler (2026-09-11, DES f5f948b7c): `PartsType` hatte noch ein geteiltes `new KnowSECAnnotationNameType()` (vom ersten Skriptlauf vor dem Zeilenenden-Fix übersehen). Beide Sweep-Skripte melden jetzt 0 offene Fälle.
    - [x] Abschluss `S4.7` (2026-09-11, KnowWE 4565c97e5, DES be80d1b62, SSP 16c3053b4, Experimental 2dd4ef859): Alt-Überladungen `(Type)` in `DefaultMarkup` entfernt (`addContentType`, `addAnnotationContentType`, `addAnnotationNameType`, `Annotation.addNameType`/`addContentType`); verbleibende Nutzer auf Fabriken umgestellt (`SparqlVisualizationType` via `SparqlVisContentType::new`, `Plugins.addAnnotations` subType-Fabrik, `UMDAdapterMarkup` inkl. `Types.successors`, `IdentifierType` mit 4× `addNameType`-Lambdas, `HelloKotlinMarkup.kt` – Kotlin, war von den Sweep-Skripten nicht erfasst). Deprecated `getInstance()` von `PredicateObjectSentenceList`/`TurtleCollection` gelöscht. Tests: KnowWE/SSP/Experimental grün, DES nur `GeneratorAppTest` (Baseline). Nachweis-Ergebnis: `run-consumer-tests.sh` KnowWE/DES/SSP/Experimental EXIT=0. **Zwei-Environment-Nachweis erledigt (2026-09-15):** je ein `EnvironmentSideBySideTest` in SSP (`KnowWE-Plugin-CBX`, ae529c133), KRONE (`KnowWE-Plugin-KONAP`, 8b9960953) und Experimental (`KnowWE-Plugin-Renaming`, 10f1ffeb8): zweite Environment bei laufender erster starten, Artikel/Section-IDs bleiben gescopt, Schließen der zweiten lässt die erste weiterlaufen — alle grün. Dazu der sequenzielle Fall (mehrere Environment-Klassen im selben Surefire-Fork, `reuseForks=true`): CBX 98 grün, SemanticServiceCore 34 grün, KONAP-Bundle 3 grün.

- [x] master eingemergt (2026-09-09): KnowWE f85e756f7 (Konflikte in `Article`, `Section`, `Messages`: String-IDs mit Namespace und Publikation bei Queue-Zeit aus master, `SectionRegistry` pro Environment hält jetzt `Map<String, Section>`, Sperren auf der Registry), KnowWE-DES f667721bf + ad71597b1 (neue Task-Tools und `CoomExportTool` auf die Environment-APIs angepasst)
- [x] Consumer-Repos geklont (2026-09-07): Experimental, KnowWE-DES, KnowWE-SSP, KRONE (enthält KnowWE-KONAP-Integration), Information-Extraction. Zusammen mit denkbares-Internals sind das alle Repos mit `Environment.getInstance()`-Aufrufern.
- [x] Consumer-Check (2026-09-15, statisch gegen `Projects/Main`, nach dem Entfernen der APIs): GINA, KMW, KnowledgeConverter, d3web-Player-Platform importieren kein `de.knowwe` (0 Treffer). SemanticServicePlatform: die lebenden `SSP-*`-Module nutzen nur `de.knowwe.export.*` (`denkbares-WikiExport`, unverändert); der einzige `Environment.getInstance()`/`DEFAULT_WEB`-Nutzer `denkbares-TEKNO-Studio` ist im `denkbares-IE-Apps`-Reactor auskommentiert (toter Code) und im env-instance-Zweig von Information-Extraction bereits auf `HeadlessKnowWE` migriert (dort 0 `getInstance()`-Reste). Kein Klon/Kompilierlauf nötig.
- [x] CBX-Integrationstests (`Projects/EnvInstance/KnowWE-SSP-Integration-Tests`, eigener Git-Checkout, Branch `env-instance`) wieder grün. Regel: **immer einzeln** fahren (`reuseForks=false`, je eigener Fork, viel RAM).
  - [x] `CBXHeadlessAppQuickTest` 8, `-TurretTest` 26, `-ChassisTest` 24, `-PZHTest` 22, `-SchulungTest` 23, `-WisentTest` 18 — alle **0 Failures/0 Errors** (mit `TZ=Europe/Berlin`, `-Xmx12G`). `OntologyBridgeReproTest` 1 skipped (bewusst `@Disabled`, manuelles Repro-Template).
  - [x] CBX-Plugin-eigene Modultests nach dem Merge: 99/99 grün.
  - Ursachen der anfänglichen Fehler und Fix (master nachziehen) s. Protokoll 2026-09-26.

## Offene Punkte / Beobachtungen

- **`reuseForks=false` geprüft und entfernt** (2026-09-11): `KnowWE-SSP/KnowWE-Plugin-CBX`, `KnowWE-DES/KnowWE-Apps-DES/KnowWE-Headless-Plugin-Core`, `KRONE/KnowWE-Plugin-KONAP`, `KRONE/KnowWE-KONAP-Integration/KnowWE-Plugin-KONAP-Configit` laufen jetzt mit geteilten Forks (Default `true`). Dabei echte Leaks gefunden und behoben: die plugin.xml-`Annotation`-Dekoration (`Plugins.addAnnotations`) mutierte das geteilte statische `DefaultMarkup` (z. B. `OntologyMarkup.MARKUP`) bei jeder Type-Tree-Dekoration erneut → `annotation X already added` im zweiten Environment; jetzt idempotent (Annotation nur beim ersten Mal ergänzen, die per-Type-`AnnotationType`-Kinder weiterhin je Typbaum). Nachweis: CBX 97, DES-Headless 7, KRONE-KONAP 66 Tests grün mit geteilten Forks; volle Läufe KnowWE/DES/SSP/Experimental grün bis auf die bekannten Baselines (KRONE-Configit hat ohne die ausgeschlossenen Netz-Tests 0 Tests). `KnowWE-UITests` behält `reuseForks=false` (UI-/Server-Integrationstests, `skipTests=true`, nicht Teil dieses Projekts).
- **KDOM-`Type`-Singletons erledigt** (2026-09-11, `S4.7`-Anschluss): Die verbliebenen Typ-Singletons, die noch einen geteilten Teil-Typbaum über Environments hinweg hielten, sind auf `new` umgestellt (Konstruktoren public, `getInstance()`/statisches Feld entfernt). Betroffen: 15 DiaFlux-Typen (`FlowchartContentType`, `NodeType`, `NodeContentType`, `EdgeType`, `EdgeContentType`, `StartType`, `ExitType`, `ActionType`, `CommentType`, `SnapshotType`, `DecisionType`, `PositionType`, `OriginType`, `TargetType`, `GuardType`; `FlowchartType` baute per Fabrik, zog aber über `getInstance` denselben Kindbaum), ferner WikiBook `TableOfContentChapterContentType`, Comment `Forum`/`ForumBox` sowie die Renaming-Testutil-Typen. `NodeHandler`s halten jetzt eigene Instanzen (nutzen nur `type.getClass()`). Nachweis: Kette grün, DiaFlux-Modultests grün, KnowWE/DES/SSP/Experimental grün bis auf Baselines.
- **Service-Singletons: Batch 1+2 erledigt** (2026-09-11): `DefaultWikiTestObjectProvider` (stateless → `new`), `CBXServices` (Zustand liegt in der HttpSession → Methoden statisch), `IndicationHandler` (stateless Compile-Script → `new`), `KnowledgeRepositoryCache` (stateful → Environment-Service `of(env)`), `ChangeLog` (EnvironmentAware → Environment-Service `of(env)` mit `getListener`-Vorrang, `close()` stoppt Executor/Writer; Caller `OntologyDeltaDebugger` über `core.findEnvironment()`, `ExpectedSparqlResultDebuggerTest` merkt sich die Environment des Compilers), `MaengelKatalogTrainingJobService` (stateful Executor/Jobs → Environment-Service `of(env)` mit `close()`, Caller über `request.getEnvironment()`), `PreRenderWorker` (Executor → Environment-Service `of(env)` mit `close()`, Caller über `user.getEnvironment()`/`context.getEnvironment()`). Bewusst nicht migriert, weil **nicht Environment-gebunden**: `de.knowwe.edit.complete.CompletionManager` (globaler Extension-Cache), `VerbalizationManager` (globales Verbalizer-Register), `LoadedSessionContext` (identitätsbasierter, zustandsloser `SessionObjectSource`), `InstanceIdentity` (App-Manifest/ContextPath aus `ContextListener`), `QSReportProviderFactory` (JPF-Plugin-Lookup), `ConfigitProcessManager` (app-weite Remote-Bridge, per `Instantiation` initialisiert). Nachweis: Kette grün, KnowWE/DES/SSP/Experimental grün bis auf Baselines.
- **Verbleibende geteilte Typ-Instanzen** (Review 2026-09-16): Nach dem `S4.7`-Sweep sind es noch 21 statische
  `Type`-Instanzen, nicht mehr die Typbäume von früher: die 20 DSL-Typen in `CoomDSLType` (KnowWE-DES) ; `de.knowwe.kdom.verbatim.VerbatimType`
  hatte als einziger KnowWE-Typ noch ein statisches `instance`/`getInstance()`, das niemand mehr aufrief (die
  plugin.xml erzeugt den Typ über den Klassennamen) — entfernt, die Klasse bleibt. Die COOM-Typen sind unkritisch, solange die Dekoration idempotent ist, aber sie
  vertragen keine Identitätsvergleiche: `UnusedDefinitionTest` verglich `section.get() != CoomDSLType.DEF_DEFINE` und
  prüft jetzt die Termklasse (`getTermObjectClass(...) != Bindings.class`) — ein Identitätsvergleich würde bei einer
  späteren Umstellung auf Fabriken still falsch (alles "unbenutzt"), statt zu scheitern.
- **Services, die während `close()` entstehen** (Review 2026-09-16, behoben): `closeServices()` nahm einen Snapshot,
  leerte die Map und schloss der Reihe nach; `getService` wirft aber erst, wenn `closed` gesetzt ist, und das passiert
  erst nach `closeServices()`. Im Fenster dazwischen war die Map leer und beschreibbar: ein Service, der in seinem
  eigenen `close()` einen Geschwister-Service holt, oder ein Thread, der die Environment noch hält (ein Request, der
  beim Servlet-Shutdown noch rendert, z. B. `PreRenderWorker.of(user.getEnvironment())`), bekam eine **frische**
  Instanz samt neuem Thread-Pool, die niemand mehr schloss — der Pool überlebte die Environment. `closeServices()`
  schließt jetzt in Runden, bis nichts mehr nachkommt, und meldet ab der zweiten Runde per Warnung, wer da noch einen
  Service erzeugt hat. `getService(Class)` (die reine Abfrage) synchronisiert jetzt auf `services` wie die andere
  Überladung. Nachweis: `EnvironmentLifecycleTest.serviceCreatedWhileClosingIsClosedAsWell` (schlägt ohne die Runden
  fehl), 8 Tests der Klasse grün.
- **Geplugte Annotation-Renderer pro Typbaum** (Review 2026-09-16, behoben): `Plugins.addAnnotations` erzeugte den
  in der plugin.xml angegebenen Renderer einmal und hängte ihn an die `Annotation` des (meist geteilten) Markups —
  eine Instanz für alle Environments, während die Content-Types längst Fabriken sind. `DefaultMarkup` nimmt jetzt
  auch für den Renderer eine Fabrik (`addAnnotationRenderer(String, Supplier<Renderer>)`), `AnnotationType` fragt
  `annotation.createRenderer()`, und `Plugins` registriert die Fabrik. Fest registrierte Renderer-Instanzen der
  Markup-Autoren bleiben unverändert geteilt (zustandslos, bewusst).
  Dabei fiel ein zweiter Punkt auf: weil die geplugte Annotation am geteilten Markup hängt, legt schon
  `DefaultMarkupType.applyMarkup` im **zweiten** Environment einen `AnnotationType` dafür an, und `Plugins` hängte
  einen zweiten daneben. `DefaultMarkupType.addAnnotationType(Annotation)` prüft das jetzt. Nachweis:
  `AnnotationDecorationTest` (KnowWE-core, 3 Tests).
- **Zwei-Environment-Nachweis verschärft** (Review 2026-09-16): Die vier Seite-an-Seite-Tests (KnowWE, CBX/SSP,
  KONAP/KRONE, Renaming/Experimental) prüfen jetzt über den ganzen Typbaum, dass beide Environments dieselben Markups
  kennen (sonst wäre der Vergleich leer), dass kein Markup in beiden Bäumen dieselbe `DefaultMarkupType`-Instanz ist
  und dass kein Markup zwei Typen für dieselbe Annotation hat (`utils.TypeTrees` in KnowWE-GlobalTestUtils). Vorher
  verglichen sie nur Artikel und Section-IDs.
- **Testabdeckung des Sweeps** (Review 2026-09-16): `run-consumer-tests.sh` schliesst Klassennamen aus, nicht
  Module; `!*Configit*,!*BridgeBuilder*,!*ConsistentDSTD*,!*VariantDstd*` liess damit das ganze KRONE-Modul
  `KnowWE-Plugin-KONAP-Configit` aus, obwohl gerade dort `reuseForks=false` entfernt wurde. Einzeln nachgefahren
  (geteilte Forks): `ConfigitBridgeCompileProductVariantsTest` 2 Tests grün (84 s), `ConsistentDSTDCheckTest` 1 Test
  grün, `BridgeBuilderHeadlessTest` die bekannte Baseline (zufällige VersionID in der Erwartungsdatei; der Test
  schreibt dabei `bb_actualHeadless.xml` ins Modul, danach `git checkout` darauf), und
  `VariantDstdTestConfigIntegrationTest` braucht eine erreichbare Configit-Bridge und lief in Retries bis zur
  Zeitgrenze (Abbruch nach 10 min, EXIT=124). Die Ausschlussliste bleibt daher wie sie ist, ist jetzt aber im Script
  kommentiert, samt Kommandozeile für den separaten Lauf. Ebenfalls korrigiert: der Tippfehler in
  `GIT_COMMITTER_EMAIL` (`albrecht.strfler@`).
- **`HasEnvironment`** (2026-09-16, nach Signatur-Review): Es gab vier Schreibweisen, um an die Environment zu
  kommen — `x.getEnvironment()` (Article, ArticleManager, UserContext), `KnowWEUtils.getEnvironment(x)` (Section 213
  Aufrufe, Compiler 50, Rest 32), `Environment.of(ServletContext|Request)` und `Rdf2GoCore.findEnvironment()` —, und
  jeder Dienst schrieb sich seine eigene Overload-Familie (`WDManager.of` fünfmal, `ReleaseContextManager.of`
  fünfmal, insgesamt 49 solcher Methoden, alle reine Delegation). Jetzt gibt es
  `de.knowwe.core.HasEnvironment { @NotNull Environment getEnvironment(); }`, implementiert von `Section`, `Article`,
  `ArticleManager`, `UserContext`, `Compiler` (Default über den CompilerManager) und von der `Environment` selbst
  (liefert sich selbst). `Section` hat damit endlich eigene `getEnvironment()`/`findEnvironment()`-Methoden.
  Dienste nehmen nur noch `of(@NotNull HasEnvironment source)`; die 25 delegierenden Überladungen in 12 Klassen sind
  gelöscht, bestehende Aufrufer kompilieren unverändert weiter. Anschließend sind 275 Aufrufstellen in 178 Dateien
  von `KnowWEUtils.getEnvironment(x)` auf `x.getEnvironment()` umgeschrieben und die fünf `getEnvironment`-
  Überladungen aus `KnowWEUtils` gelöscht; `KnowWEUtils.findEnvironment(...)` bleibt als Nullable-Plumbing, auf dem
  `Section`/`Compiler` ihre Auflösung aufsetzen. Zwei Fallen dabei: (1) das Skript ersetzte auch in den neuen
  Accessoren selbst, `Section`/`Compiler`/`UserContext.getEnvironment()` riefen sich danach selbst auf (kompiliert,
  StackOverflow zur Laufzeit) — sie implementieren ihre Auflösung jetzt ausgeschrieben; (2) `Sections.get(context,
  id)` fragt jetzt `context.getEnvironment()` statt `context.getArticleManager()`, was Test-Fakes betrifft, die
  einen `UserActionContext` als dynamischen Proxy bauen: ein Proxy leitet auch Default-Methoden an seinen Handler
  (`ReRenderContentPartActionTest` angepasst); (3) Python-`read_text`/`write_text` normalisiert Zeilenenden, vier
  CRLF-Dateien (WikiBook `WikiBookUtils`/`ImageTypeRenderer`, DiaFlux-Anomalies `MissingArticlesTest`/
  `OrphanArticlesTest`) kamen als Komplett-Diff zurück und wurden byteweise auf CRLF zurückgestellt — bei
  Massenersetzungen immer `git diff --numstat` gegenlesen.
- **`reuseForks=false`: zwei neue Stellen geprüft** (2026-09-16): Nach dem Master-Merge stand die Einstellung wieder
  in `KnowWE-SSP/KnowWE-Plugin-SemanticServiceCore` und `KnowWE-DES/KnowWE-Plugin-KnowSEC`, beide mit der Begründung
  „Headless lifecycle tests require a fresh Environment per class". SSC läuft mit geteilten Forks (38 Tests grün,
  darunter `SearchMarkupLifecycleTest` und `ConceptStoreTest`), die Einstellung ist dort entfernt. KnowSEC nicht:
  `SessionManagerStartupIntegrationTest` prüft das Laden des Session-Caches, das nur beim **ersten** Start eines
  Wikis passiert, und `HeadlessKnowWE` ist bewusst ein Wiki pro JVM (`instance`-Singleton, siehe „Listener-
  Singletons"). Allein läuft die Klasse auch im geteilten Fork grün, nach einer anderen KnowSEC-Klasse nicht. Dabei
  gefunden und behoben: `SessionManager.startupDone` war ein JVM-globales `static volatile boolean`; die Phase hängt
  jetzt als `StartupState`-Service an der Environment. Die Einstellung bleibt in KnowSEC, mit dem echten Grund im
  POM. Sauber lösen ließe es sich, wenn `KnowSECSessionTestSupport` je Klasse ein eigenes `HeadlessKnowWE` baut
  (der Konstruktor ist public) statt über `getInstance()` zu gehen.
- **`ServiceKey` statt Fabrik am Aufruf** (2026-09-17, nach Signatur-Review Punkt 2): `getService(Class, Supplier)`
  hatte zwei Schwächen — jede Aufrufstelle wiederholte die Fabrik (zwei abweichende Fabriken: die erste gewinnt,
  still), und die Lambda konnte eine **fremde** Environment einfangen (`a.getService(X.class, () -> new X(b))`
  kompilierte). Jetzt deklariert jeder Dienst sich selbst, einmal, als Konstante auf seiner eigenen Klasse:
  `public static final ServiceKey<X> SERVICE = ServiceKey.of(X.class, X::new)`; die Fabrik ist eine
  `Function<Environment, X>` und bekommt die Environment, die gefragt wurde. `Environment.getService(ServiceKey)`
  merkt sich den bauenden Key und wirft, wenn ein zweiter, anderer Key für denselben Typ auftaucht
  (`aSecondDeclarationOfTheSameServiceIsRefused`). Die Fabrik-Überladung ist gelöscht; geblieben ist die reine
  Abfrage `getService(Class)` (nullable). Umfang: 41 Dienste über alle fünf Repos.
  Dabei aufgefallen und behoben: COOM und Ontology-TermBrowser bauten ihren `CompileEventListener` je aus der
  Section, die zufällig zuerst kompilierte, und cachten ihn als Environment-Dienst — der Kommentar sagte „one
  listener per environment", der Code hing an einer beliebigen, nach Edits veralteten Section. Der Listener hält
  jetzt die Environment und leert den Hierarchie-Cache aller TermBrowser-Sections. Nachweis: Kette grün bis
  Information-Extraction, Sweep KnowWE/Experimental/SSP/DES/KRONE auf Baseline-Niveau.
- **`get`/`find` symmetrisch** (2026-09-17, Signatur-Review Punkt 6): `HasEnvironment` deklariert jetzt
  `@Nullable findEnvironment()` als die primäre Methode und `@NotNull getEnvironment()` als Default darauf
  (wirft, wenn keine da ist). `Section` und `Article` überschreiben `getEnvironment()` nur noch für ihre sprechenden
  Meldungen, `Compiler` und `UserContext` lösen ausschließlich nullable auf, `ArticleManager` und `Environment`
  antworten immer. Die vier `KnowWEUtils.findEnvironment(...)`-Überladungen sind gelöscht; die Auflösung liegt jetzt
  bei den Objekten selbst (`Article.findEnvironment()` fällt dabei wie früher die Utility auf seinen ArticleManager
  zurück).
- **Zustandsnamen und `getServletContext()`** (2026-09-17, Signatur-Review Punkte 4 und 5): `isClosed()` lieferte in
  Wahrheit das Feld `closing`, während `getService` auf `closed` prüfte — beim Rdf2GoCore-Fix musste ich erst
  nachsehen, welches gemeint war. Jetzt gibt es beide Fragen ehrlich: `isClosing()` ab dem Beginn von `close()`
  („darf ich damit noch arbeiten?", das meinen alle produktiven Aufrufer) und `isClosed()`, wenn alles freigegeben
  ist. `Environment.getContext()` heißt `getServletContext()` (24 Aufrufstellen), und das Javadoc von
  `EnvironmentService` sagt jetzt, was der Typ bedeutet, statt auf die alte Signatur zu verweisen.
- **`ArticleLifecycleFixture` ohne Reflection** (erledigt 2026-09-17): Die Environment baut die Fixture längst über
  `Environment.create(connector, rootType)`; übrig war Reflection in `closeManager(...)`, die den `executor` aus dem
  `AttachmentManager` fischte, um auf sein Ende zu warten. `AttachmentManager.shutdownAndAwait(timeout, unit)` macht
  das jetzt sauber (der Servlet-Shutdown wartet weiterhin nicht). 143 KnowWE-core-Tests grün.
- `getInstance()`-Deprecation erzeugt jetzt ~885 Warnungen über alle Repos. Bewusst so, als Migrationssignal; bei Bedarf mit einer Zeile zurücknehmbar.
- `Rdf2GoCore.close()` fährt außerhalb des Servlet-Shutdowns (`ServletContextEventListener.isDestroyInProgress()`) in einem abgekoppelten `new Thread` herunter, damit Recompiles nicht blockieren. Beim `Environment.close()` (Tests!) läuft dieser Thread parallel zum Close-Task `SemanticCore.shutDownRepositoryManager()` weiter, entfernt sein Repository aus dem Manager und ließ den Manager-Shutdown mit NPE abbrechen (`getRepository(id)` null, `MaengelKatalogSearchActionTest`). `shutDownRepositoryManager()` ist seit 2026-09-08 null-sicher und fährt den Manager im `finally` herunter (denkbares-Commons). Der Thread selbst widerspricht dem Ziel "Environment schließen = alles sauber": `Rdf2GoCore` soll seine Environment kennen (`S3.M3`, `getInstance(compiler)` -> `KnowWEUtils.getEnvironment(compiler)`) und beim Schließen der Environment synchron herunterfahren bzw. die Environment auf den Thread warten.


- **Rdf2GoCore-Shutdown-Thread erledigt** (2026-09-11, `S4.6`): `Rdf2GoCore.close()` fährt jetzt auch synchron herunter, wenn `findEnvironment().isClosed()` gilt (Environment schließt gerade), nicht nur bei `ServletContextEventListener.isDestroyInProgress()`. Cores, die während eines Recompiles ersetzt werden, bleiben asynchron (Kommentar im Code). Nachweis: `Rdf2GoCoreTest`/`Rdf2GoCoreThreadPriorityTest` und `MaengelKatalogSearchActionTest` (21 Tests) grün.

- **Listener-Singletons** (seit `S2.5` relevant, erledigt 2026-09-09 als `S4.1`): statische Singletons, die
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
  an der Environment des Cores; Standalone-Cores teilen sich eine am globalen Manager registrierte Instanz. Der
  asynchrone Shutdown-Thread (siehe unten) fährt seit 2026-09-11 synchron, wenn die eigene Environment schließt
  (`Environment.isClosed()`).

- **`EnvironmentService` statt `AutoCloseable`** (2026-09-09): Environment-eigene Services (`getService`) implementieren
  `de.knowwe.core.EnvironmentService` mit `close()`, nicht `AutoCloseable`. Grund: Aufrufer holen einen Service, um
  ihn zu benutzen, nie um ihn zu schließen; `AutoCloseable` löste in IntelliJ überall die Inspektion "AutoCloseable
  used without try-with-resources" aus. `Environment.closeServices()` schließt beides (AutoCloseable bleibt für
  fremde Typen erlaubt). Echte Ressourcen mit try-with-resources (`WikiSearchIndex`) bleiben `AutoCloseable`; die
  `Environment` selbst auch, sie ist in Tests eine Ressource.
- **`S4.2`, Muster** (2026-09-09, Beispiele committet): Servlet-Einstiege über `Environment.of(ServletContext|
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
- KnowWE-DES (Testlauf 2026-09-08 nach `S3.M1`): 9 Klassen `de.knowwe.versioning.git.*` (siehe oben, gelöst), `GeneratorAppTest` ("Cannot run program"), `TaskContextManagerPersistenceTest` (gelöst 2026-09-08: der Test hängte seinen Manager nicht ans Environment, `TaskContext.editPage` erzeugte daher einen zweiten Default-Manager, der auf `ArticleUpdateEvent` dieselbe wc-Datei überschrieb; auf master nur zufällig grün, weil `get()` den abgeräumten Manager des vorherigen Testlaufs zurückgab. Fix im Test: `TaskContextManagerInstance.set(env, manager)`). KnowWE-SSP und Experimental: grün. KRONE: Modultests `KnowWE-Plugin-KONAP` grün, `KONAP-Plugin-COOM` 4 Klassen mit Baseline-Fehler (`CoomModelWriterTest`, `CoomModelWriterKSWTest`, `DeductionSolutionSpace*Test`: die POM schließt `KnowWE-Plugin-Agents` aus, `KnowWE-Plugin-MCP` importiert es aber -> Plugin-Manager-Init scheitert; unabhängig vom Umbau), `KnowWE-Plugin-KONAP-ReleaseVersioning` grün. Information-Extraction: nur kompiliert.

- `GitPageProviderTest`, `GitWikiRepositoryTest`, `GitAttachmentProviderTest` (JSPWiki-Connector) brauchen eine Git-Identität. In der VM gibt es keine globale, daher 11 Fehler. Mit `GIT_AUTHOR_NAME/EMAIL` und `GIT_COMMITTER_NAME/EMAIL` als Umgebungsvariablen bleiben 2 Fehler (`sweepUp...`, "dirty tree must be reconciled"), auch mit `LC_ALL=C`. Betrifft das Git-Status-Parsing des GitConnectors aus denkbares-Internals, nicht die Environment. Nicht weiter verfolgt.

- **Masters Autorisierungs-Test-Debt (Stand nach Merge 2026-09-15):** Masters Auth-Rollout ließ `assertUserAuth`/
  `assertCan*` in viele Actions einziehen, während zahlreiche von Masters eigenen Tests mit einem nicht-assertierten
  `TestUserContext` (Default `isAsserted=false`) direkt `action.execute(...)` aufrufen. In KnowWE-DES 127,
  in KRONE entsprechend viele `NotAuthorizedException: No authenticated user.` (z.B. `de.knowwe.versioning.git.*`,
  `CreatePatchTest`, `ApplyPatchTest`, `BasicTaskContextManagerTest`, `MetaDataStoreTest`). Reproduziert Masters
  Verhalten: die POMs setzen `maven.test.failure.ignore`/`testFailureIgnore=true`, der Build bleibt grün. Identisch
  auf master zu erwarten, also keine Merge-Regression; nicht angefasst. Betroffene Tests bräuchten einen assertierten
  Kontext (`new TestUserContext(article, admin, asserted, authenticated)`).
- **Jackson-NoSuchMethodError (KRONE, 2026-09-15):** `com.fasterxml.jackson.core.util.BufferRecycler.releaseToPool()`
  fehlt zur Laufzeit in Threads einiger KRONE-Module (jackson-core-Versionsmix auf dem Testclasspath). Der 900s-Sweep
  lief in COOM-Tests in den Timeout, daher nicht abschließend klassifiziert; kein Bezug zum Environment-Umbau erkannt.
- **KRONE-Sweep-Timeout (2026-09-15):** `KnowWE-Plugin-KONAP`-Modultests plus COOM benötigen > 15 min; die
  Teil-Ergebnisse zeigen die bekannten `GpaTest`/`OptionSetTest`/`DeductionSolutionSpace*`-Baselines und
  `ModelSetTest`. Die 15-min-Grenze wurde eingehalten, der Lauf daher abgebrochen (EXIT=124).

## Arbeitsumgebung

### Sandbox-Setup (Stand 2026-09-25, gilt für diese VM/openCode-Session)

- Das Home ist ein **Overlay**: nur `~/.m2` und `~/denkbares` überleben einen Neustart (plus die eingebackenen
  `~/.local/bin/maven-workspace-split`). `~/.local/share/maven` und `~/.mavenrc` sind nach jedem Neustart weg.
- Deshalb vor jedem `mvn` **sourcen**: `source Projects/EnvInstance/tools/mvn-env.sh`. Das installiert Maven 3.9.16
  persistent nach `~/.m2/.tools/` (nicht `~/denkbares/.tools`: auch das ist Overlay), legt `~/.mavenrc` neu an, setzt `JAVA_HOME=/usr/lib/jvm/zulu-25-amd64`,
  `MVN_REAL` und den Wrapper-`PATH`. Danach `command -v mvn` = Wrapper, `ls ~/.m2/repository` = `cached` + Workspace-Namen.
- Workspace-Prefix der Kopie ist `EnvInstance` (`~/.m2/repository/EnvInstance`), die Kette ist dort schon installiert.
- Die frühere `build-chain.sh`/`run-consumer-tests.sh`/`env-instance-repo`-Welt existiert hier **nicht**; es gilt das
  workspace-gesplittete Repo (`~/.m2/repository/<Workspace>/`), offline mit `-o`.
- **Workflow-Wunsch des Nutzers: bei Builds/Testläufen alle ~30 s ein Statusupdate.** Umsetzung: Lauf im Hintergrund
  nach `Projects/EnvInstance/logs/<name>.log` schreiben und den Log per separater Calls pollen (das Log liegt im
  persistenten Workspace, nicht in `/tmp`, das der Neustart löscht). Der Fortschritt ist gut an `tail` + `ps` (CPU%,
  ELAPSED) sichtbar; Surefire meldet die Testanzahl erst am Klassenende, bei `reuseForks=false` also einmal pro Klasse.
- **`TZ=Europe/Berlin` ist Pflicht** (im Bootstrap gesetzt): die VM läuft UTC, die CBX-Erwartungsdateien wurden in
  Europe/Berlin erzeugt. Ohne die Variable schlagen alle zeitstempelhaltigen Vergleiche fehl (1970-01-01 00:00 statt
  01:00, `+0000` statt `+0200`, 13:03 statt 14:03).
- CBX-Integrationstests: im Modul `KnowWE-SSP-Integration-Tests/KnowWE-Plugin-CBX-Integration-Tests`, Surefire
  `reuseForks=false`, `argLine -Xmx12G` (VM hat 16 GB) → immer **einzeln** starten.

### Ursprüngliche Arbeitsumgebung (Referenz)

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

- 2026-10-14: **`Environment` nicht mehr `AutoCloseable`.** Schließen darf nur der Besitzer (Servlet-Lebenszyklus,
  Headless-Anwendung, Test); alle anderen holen die Environment über Section/Kontext und dürfen sie nie schließen —
  `close()` fährt das Wiki herunter. `AutoCloseable` signalisierte das Gegenteil, IDE-Inspektionen verlangten
  try-with-resources an jedem Getter. `close()` bleibt; für Tests mit try-with-resources gibt es
  `utils.OwnedEnvironment` (KnowWE-GlobalTestUtils, JUnit-unabhängig). Gleiche Begründung wie bei `EnvironmentService`.

- 2026-10-13 (später): **`main-local/master` erneut in alle Repos gemergt** (Versionssprung auf `2026.8-SNAPSHOT`,
  jspwiki `2.12.10-SNAPSHOT`). Konflikte: KnowWE `DefaultArticleManager`/`LongOperationUtilsTest` (`committedTitles`
  am EventManager der Environment), `InterWikiImportUpdateService` (Masters Sammel-Polling + Page-Save-Listener als
  Environment-Service neu aufgesetzt, Listener am EventManager der Environment, Abmeldung in `close()`),
  `InterWikiImportMarkup`; KnowWE-DES `CoomCompiler`, `GetModelOverviewAction`, `RegressionTest`. Nachgezogen:
  neue COOM-CI-Tests auf `renderResultTitle(UserContext, …)`, `SearchResultRenderer` liest seine Limits über die
  Environment, neue Master-Tests mit eigener Environment, doppelte Test-Dependencies in der COOM-POM (beide Seiten
  hatten sie ergänzt). **Master-WIP:** `CIContainerTest` (`384dea3db`) nutzt ein nicht committetes
  `CIContainerMarkup`; auf dem Stand davor gehalten (Kommentar im Commit). Kette 2026.8 gebaut (dafür
  `denkbares-Maven-Plugins`, `denkbares-inference`, `KMW`, `SemanticServicePlatform`, `coom` neu aus `Main`
  geklont; GINA-/SSP-Plugin-Jars per `install-file` mit `.flattened-pom.xml` aus Main). Tests: KnowWE-core/Search/
  Include 330, COOM 66, CBX 104 (inkl. Bike) grün. Nachtrag: Masters `CIContainerMarkup` kam nach, damit
  `CIContainerTest` auf Masters Stand (Typen per Fabrik); `PreviewCache` (Search) ist jetzt ein Environment-Service
  (`PreviewCache.of(environment)`) statt JVM-statisch.

- 2026-10-13: **`S4.7`-Folgefehler aus dem Kundenwiki-Vergleich mit Master.** (1) „Unknown TSM ID class" massenhaft
  beim Start und TSM-IDs doppelt als `Activity` und `TsmId` registriert: Ursache war das geplugte CompileScript
  `TSMTermTurtleDefinitionHandler` (CBX, Scope `TurtleContent/**/Predicate`), das `ssc:tsmId`-Literale in `%%Turtle`
  als `TsmId`-Definition registriert. Auf Master war es tot: das JVM-Singleton `PredicateObjectSentenceList` wurde
  einmal über den kürzesten Pfad `OntologyTableMarkup/AnnotationType/AnnotationContentType` dekoriert, der Scope
  griff nie (per Master-Nachbildung im Typbaum belegt: 0 von 4 `Predicate` mit Script, auf dem Branch 6). Seit
  `0430c0b8b` hat jedes Turtle-Konstrukt eigene Instanzen, das Script lief. Entfernt (Extension + Klasse).
  (2) Beim Fabrik-Sweep verlorene Zeilen: `NonSolutionElementsMarkup` ohne `CommentLineType` (Kommentare wurden
  `TsmIdReferencePath`s), KRONE `GruppierungsmodulMarkup` ohne Pflicht-Annotation `@module`. Wiederhergestellt.
  Scan über alle Repos (Konfigurationsaufrufe pro Datei Merge-Base vs. HEAD, normalisiert; neu ungenutzte Imports;
  pfadgebundene plugin.xml-Scopes in früher geteilte Teilbäume) fand keine weiteren Fälle. Regressionstest
  `BikeWikiTsmRegistrationTest` (CBX, Bike-Wiki per `-Dcbx.bike.wiki=<pfad>`, sonst übersprungen): JSPWiki-artiges
  Laden, keine Doppelregistrierung spezifisch/`TsmId`, keine Warnungen, `NonSolutionElements`-Kommentare; mit Script
  bzw. ohne `CommentLineType` nachweislich rot. CBX-Modul 104 Tests grün. KRONE-Modul offline nicht baubar
  (DES-Artefakte fehlen im `EnvInstance`-Bereich), Änderung dort nur 3 Zeilen analog Master.

- 2026-09-27: **Breiten-Test-Sweep über alle Consumer-Repos** (nach den master-Merges, jeweils `mvn -o test`):
  | Repo | Tests | Failures | Errors | Bewertung |
  |---|---|---|---|---|
  | KnowWE | 599 | 0 | 0 | grün (12 skipped) |
  | KnowWE-SSP | 137 | 0 | 0 | grün |
  | Experimental | 95 | 0 | 0 | grün |
  | KnowWE-DES | 833 | 0 | 20 | nur `GeneratorAppTest` (`dot`/Graphviz fehlt = Baseline) |
  | KRONE | 499 | 4 | 5 | s. u. |
  - **KRONE-Fehler klassifiziert** (isoliert nachgefahren, teils mit `JAVA_TOOL_OPTIONS=-Xmx8g`):
    - Heap-abhängig (KRONE-Root-`argLine` hat kein `-Xmx`, nur den Mockito-Agent): mit 8g ist
      `DeductionSolutionSpaceTestTest` **grün**; `CoomModelWriterTest` OOMt nicht mehr, scheitert dann aber inhaltlich
      (`Unexpected behavior objects (156828)`) — `CoomModelWriterKSWTest` analog OOM.
    - Extern/netzabhängig (Baseline): `ConfigitBridgeCompileProductVariantsTest` (`ZipException: zip file is empty`),
      `BridgeBuilderHeadlessTest` (NPE), `VariantDstdTestConfigIntegrationTest` (Configit-Bridge).
    - **Ungeklärt**: `GruppierungenModelBuilderTest`, `GruppierungTableLineTest`,
      `WD6SymbolicDeductionRuleCompileScriptTest`. Test **und** Produktivcode sind master-identisch
      (`git diff master HEAD` leer), die Erwartung weicht aber ab (`Gruppierung_ActiveOptionA_OptionB` vs.
      `Gruppierung_Active_2`, "statement is null") → Verdacht auf geändertes Verhalten einer geteilten Abhängigkeit
      (KnowWE-core/coom auf env-instance) oder auf master bereits rote Tests. Braucht master-Vergleichslauf.
  - **Werkzeug KRONE:** Surefire-`argLine` im KRONE-Root-POM verdrahtet
    `-javaagent:${settings.localRepository}/org/mockito/mockito-core/5.23.0/…`. Im gesplitteten Repo liegt Mockito aber
    unter `cached/`, der Fork starb am Start ("No tests to run", 0 Tests). Fix im Bootstrap `tools/mvn-env.sh`:
    Symlink `~/.m2/repository/org/mockito` → `~/.m2/repository/cached/org/mockito`. Danach liefen die KRONE-Tests.
- 2026-09-26 (später): **`master` in alle Repos nachgezogen.** Hinterher hingen `d3web` (1), `d3web-DES` (1),
  `denkbares-Internals` (3) und `KnowWE-DES` (8) — die übrigen hatten `master` schon. `d3web`/`d3web-DES`
  fast-forward, die anderen konfliktfrei bis auf KnowWE-DES: Git-Konflikte in `AgentInstantiation`
  (`DESvLLMServer`→`DESServer`, env-aware `Property.VLLM_SERVER.value(env)`) und `ReviewVersions`
  (`index(Environment,…)` statt `Environment.getInstance()`); dazu ein **semantischer** Merge-Konflikt in
  `TaskChanges`/`TaskChangesTest` (neuer master-Code rief die `web`-lose `getReviewChanges`-Signatur). Gelöst:
  `Environment` durch `TaskChanges` und `GetDiffAction.getReviewChanges` gereicht, `ReviewVersions.at/index` auf
  `@Nullable Environment` (Tests ohne Wiki), `TaskChangesTest` übergibt `null`.
  Die merges brachten neue Abhängigkeiten, die es im Workspace noch nicht gab; alle aus `Projects/Main` nach
  `EnvInstance` installiert: `denkbares-inference` (`-DES`, `-vLLM`, `AgentUtils`, …),
  `denkbares-Clingo`/`-SQLite-Search` (aus Internals, da `reaxcel` an pnpm scheitert), `coom` (Retrieval-Module),
  `d3web-Player-Platform`. Dabei `jspwiki-wikipages` (Sprachmodule) gebaut, damit `jspwiki-war` und dann
  `KnowWE-Resources` durchlaufen; KnowWE und KnowWE-DES vollständig installiert (52/52 bzw. 42/42).
- 2026-09-26: **`CBXHeadlessAppTurretTest` grün (26/26).** Zwei Ursachen für die vorherigen 8 Failures:
  - **7 Zeitzonen**: VM UTC, Erwartungsdateien Europe/Berlin → `TZ=Europe/Berlin` im Bootstrap `tools/mvn-env.sh`.
    Nachgewiesen: mit TZ schrumpften die 8 Failures auf 1 (26/1).
  - **1 echter (`testCIDashboards`)**: `env-instance` fehlten die master-Commits der „no delivery"-Reihe. KnowWE-SSP war
    3 Commits hinter master (`5e574585c Do not add no delivery solutions to KB` aktiviert den `isNoDeliverySolution`-Filter
    in `WiresTest`, `900e43c89`, `d4e1582b1`), KnowWE 7 Commits (`ae46832e1` liefert die Hooks `isNoDeliverySolution` in
    `XCLModelCreator`/`XCLRelation`, ohne die die KnowWE-SSP-Overrides nicht kompilieren). `git merge master` in beiden
    Repos konfliktfrei; danach `KnowWE-Plugins/KnowWE-Plugin-d3web-MarkupSet` und `KnowWE-Plugin-CBX` neu installiert und
    Turret erneut → 26/26. Also kein Refactoring-Bug, sondern ein fehlender master-Stand.
  - **Werkzeug**: Beim Rebuild brach die Offline-Auflösung von `org.apache.jspwiki:*:2.12.9-SNAPSHOT` ab, weil Mavens
    Remote-Cache (`~/.m2/repository/cached`) auf einen Timestamp-Snapshot (`…-20260924.015502-45`) zeigte, dessen Jar nie
    geladen wurde. Fix: jspwiki lokal neu bauen/installieren (`mvn -o -pl jspwiki-war -am install -DskipTests`; `jspwiki-war`
    selbst scheitert an fehlendem `jspwiki-knowwe-wikipages-en`, Module 1–11 reichen). Maven-Install jetzt persistent unter
    `~/.m2/.tools` (nicht `~/denkbares/.tools`, das der Neustart löscht).
  - **Ergebnis**: `Quick` 8, `Turret` 26, `Chassis` 24, `PZH` 22, `Schulung` 23, `Wisent` 18 — alle grün;
    `OntologyBridgeReproTest` `@Disabled` (1 skipped). CBX-Plugin-Modultests 99/99. Nach dem Merge die geänderten
    KnowWE-Module neu installiert (`KnowWE-core`, `-Plugin-TextDiff`, `-Plugin-JSPWiki-Connector`, `-Plugin-d3web-MarkupSet`);
    voller KnowWE-Reinstall bricht nur an `KnowWE-Resources` (WAR braucht `jspwiki-knowwe-war`, das offline an
    `jspwiki-knowwe-wikipages-en` scheitert) — für die Tests irrelevant. Integrations-Modul-POM: `argLine` auf `-Xmx12G`
    gesenkt (VM 16 GB).
- 2026-09-25: Start der CBX-Integrationsetests. Sandbox-Build-Umgebung neu aufgebaut (Maven 3.9.16 fehlte nach dem
  Neustart, `~/.mavenrc` ebenso): persistentes Bootstrap `tools/mvn-env.sh` angelegt, Logs nach
  `Projects/EnvInstance/logs/`. `test-compile` grün, `CBXHeadlessAppQuickTest` grün (8 Tests, 26 s) bei `-Xmx8G`;
  Surefire-`argLine` im Modul-POM auf `-Xmx12G` erhöht (VM 16 GB).
  **`CBXHeadlessAppTurretTest`: 26 Tests, 8 Failures, 0 Errors** (876 s, kein OOM; RAM war mit 14/15 GB + ~5 GB Swap
  am Limit, der Fork lief aber durchgehend mit >300 % CPU, kein GC-Stillstand; Laufzeit pro Klasse ~15 min).
  Fehlerbild nach Ursache:
  - **7 Zeitzonen-Diffs (umgebungsbedingt, kein Code-Bug):** VM läuft UTC, Erwartung Europe/Berlin.
    `testAdapterTestStepCreation`, `testTsm/Malfunction/TestStep/SolutionsIncrementalCompilation` (alle dieselbe Datei
    `CreatedAdapterTestSteps/XPS-A6M-A3-Turm.txt`: `1970-01-01 01:00` vs. `00:00`), `testMeasurementVisualisation`
    (`03.02.2021 14:03` vs. `13:03`), `testInterviewAnalyser` (`2020-10-08 13:43:39.703 +0200` vs. `11:43:39.703 +0000`).
    Fix: `TZ=Europe/Berlin` im Bootstrap; Verifikationslauf war beim Neustart noch offen.
  - **1 echter Inhaltsunterschied `testCIDashboards` (`CI-Dashboard-Tests/A7P-HUN.txt`):** erwartet 17 „existing but
    ignores solutions" Warnungen, tatsächlich 16; konkret fehlt
    `** The wire [7|Cable W289#hash] with signal 'TURM BUS CAN GND' has a solution but is ignored.`
    Zusätzlich drei Zeilen CRLF-vs-LF (`* [Solution A11fufa.S1/S2`, `|Solution A11fufa.S1/S2`). Zu klären, ob echte
    Regression oder veraltete Erwartungsdatei; NICHT blind per Updater überschreiben.
- 2026-09-16: Review-Befunde behoben. (1) `KnowWE.jsp` und `JSDebugger.jsp` riefen noch das gelöschte
  `Environment.isInitialized()`/`initInstance()`; JSPs werden nicht mitkompiliert, der Bruch wäre erst zur Laufzeit
  aufgefallen. Der Block ist seit `KnowWEPlugin.initialize` (`Environment.create`) überflüssig und entfernt; die
  übrigen 11 JSPs waren bereits migriert. (2) `KnowWEGitServerConnectorFactory` hielt den Connector in einem
  statischen "last one wins"-Feld samt `static Environment` — jetzt ein Environment-Service
  (`environment.getService(KnowWEGitServerConnector.class, ...)`); damit ist die geschlossene Environment nicht mehr
  hart referenziert und zwei Environments bauen ihren Connector nicht mehr gegenseitig neu (176 Tests des Moduls
  `KnowWE-Plugin-Versioning-Git-Tests` grün). (3) Identitätsvergleich auf `CoomDSLType.DEF_DEFINE` durch die
  Termklasse ersetzt, (4) den toten Singleton aus `VerbatimType` entfernt, (5) `closeServices()` gegen Services
  abgesichert, die während des Schließens entstehen, (6) geplugte Annotation-Renderer auf Fabriken umgestellt und
  dabei den doppelten `AnnotationType` im zweiten Environment beseitigt, (7) den Zwei-Environment-Nachweis auf den
  ganzen Typbaum ausgeweitet und die Lücke des Sweeps bei KRONE-Configit geschlossen, (8) den Tippfehler in der
  Committer-Adresse des Testscripts behoben (alles s. o.).
- 2026-09-15 (Nachtrag): Master weiter nachgezogen. **denkbares-Internals** (+5, neues Modul `denkbares-Secrets`),
  **KnowWE** (+9, u. a. `5777663ef adapt tests to new auth requirements` — `TestUserContext(String,String)` ist jetzt
  assertiert+authentifiziert, plus `KnowWEUtils`-Refusals für leere Seite/Section), **KnowWE-DES** (+7, u. a.
  `63815a4cd adapt tests`, danach +1 COOM-Reformat), **KnowWE-SSP** (+2, SearchMarkup-Listener/Service). Damit ist der
  **Auth-Test-Debt aufgelöst**: DES von 22 roten Klassen auf nur noch `GeneratorAppTest`. Gemergte Konflikte: jeweils
  Masters neue Auth-/Feature-Logik + Environment-Scoping; TestUserContext behält die assert-Flags; SearchMarkup-/
  SearchMarkupLifecycleTest-Handler am EventManager der Environment.
  **KRONE bewusst nicht nachgezogen:** die 3 neueren Commits (`42fb59519`, `8ad52255d`, `d33a4c112`) sind
  unvollständiger WIP — `d33a4c112` legt `TechnischeFreigabeTableLine`/`RuleTableLineDefinitionRegistrationScript` an,
  die `TableUtils.getCell(Section,int)` und die Klasse selbst referenzieren; beides existiert erst im nicht gepushten
  lokalen `e36d81914` (Main/KRONE). `main-local/master` kompiliert damit nicht → KRONE bleibt auf dem letzten
  konsistenten Stand `8b9960953` (Test-Compile grün).
  **`ModelSetTest.modelInvalidateAndUpdateGPATest`** scheitert identisch auf `Main/KRONE` (e36d81914, 0,17 s,
  gleicher `AssertionError`) = Master-Baseline, nicht der Umbau.
  **Bau-Performance:** `MAVEN_OPTS` erreicht die Surefire-Forks nicht (KRONE-Parent-`<argLine>` setzt nur den
  Mockito-Agent); Fork-Heap über `JAVA_TOOL_OPTIONS` (mit `-Xmx6g` kein OOM mehr in `CoomModelWriterTest`).
  `-T 4C`/`PARALLEL=4` bringt KRONE von >15 min auf ~7,5 min (Baselines ausgeschlossen). Harness
  `run-consumer-tests.sh` um `PARALLEL`/`FORK_HEAP` erweitert; Bau-Erfahrungen in `EnvInstance/CLAUDE.md` gesammelt.
- 2026-09-15 (später): Zwei-Environment-Nachweis für SSP/Experimental/KRONE abgeschlossen: je ein
  `EnvironmentSideBySideTest` (zweite Environment bei laufender erster, Artikel-/Section-Isolation, Close-Verhalten) in
  `KnowWE-Plugin-CBX` (SSP, 98 Tests der Modulsuite zusätzlich grün im geteilten Fork), `KnowWE-Plugin-KONAP` (KRONE,
  dazu KONAP-Bundle 3 Environment-Klassen grün) und `KnowWE-Plugin-Renaming` (Experimental). Damit ist der letzte im
  `S4.7`-Abschluss offene Punkt erledigt.
- 2026-09-15: **`main-local/master` in `env-instance` gemergt** (alle 7 Repos). Master brachte die Autorisierungs-
  Checks (`requiredAccess`/`Action.Access`, `KnowWEUtils.assertUserAuth/assertCan*`, `AbstractAction.getSection`),
  `ContentDisposition`, SSE-Helfer und den Launcher/Sandbox-Umbau des Git-Versioning-Plugins. Konfliktauflösung
  durchgehend: Masters neue Prüfungen/Features übernehmen, Environment-Scoping behalten (`context.getEnvironment()`,
  Environment-Varianten der Statics). Höhepunkte:
  - Merges: denkbares-Commons, jspwiki (fast-forward), denkbares-Internals; KnowWE (~28 Konflikte), Experimental (17),
    KnowWE-DES (44), KnowWE-SSP (9), KRONE (9). In Experimental/DES/KRONE waren viele Konflikte reine CRLF-Artefakte
    (Master hat die von ihm bearbeiteten Action-Dateien mit CRLF committet); normierender 3-Wege-Merge (EOL ignorieren)
    entfernt sie.
  - **Versioning-Plugin (DES)**: Masters Launcher/Sandbox-Design übernommen (`KnowWEGitServerConnector.sandbox()`,
    `KnowWEGitServerConnectorFactory.credentials()`, `MetaDataStore`, `GetSandboxAction`, `UnsavedWorkAction`) und
    erneut Environment-gescopt: `GitWikiProperties`/Connector/Fabrik nehmen eine `Environment`, Actions reichen den
    `UserActionContext` durch, statische Helfer (`setTag`/`getTags`/`releaseIsFinished`) die Environment.
  - **ServiceMateBridge (SSP)**: Masters Read/Write-Lock-Snapshots (`getMappedTargetIds/getMappedMateIds`) plus
    Stale-Section-Behandlung übernommen, Section-Auflösung leitet die Environment vom Compiler/Section ab.
  - Master-Tests an Environment-Scoping angepasst (KnowWE): `AttachmentListenerLifecycleTest` feuert über den
    EventManager der Environment, `LongOperationLifecycleTest` verzichtet auf das entfernte statische
    `LONG_OPERATIONS`-Feld, `ReRenderContentPartActionTest` gibt dem Proxy eine echte Environment.
  - Kette installiert vollständig grün (KnowWE, Experimental, KnowWE-DES, KnowWE-SSP, KRONE, Information-Extraction).
    Test-Sweep: Experimental und KnowWE-SSP grün, KnowWE grün bis auf den bekannten `GitVersioningFileProviderTest`-
    Baseline; KnowWE-DES/KRONE zeigen Masters Auth-Test-Debt (s.u.) und erreichen die 15-min-Grenze (KRONE).
- 2026-09-11 (später): KDOM-`Type`-Singletons (DiaFlux/WikiBook/Comment/Renaming) auf `new` umgestellt; Service-Singletons in zwei Batches migriert (Environment-Services bzw. statisch/`new`, applikations-skopierte bewusst belassen); `reuseForks=false` entfernt und dabei die plugin.xml-Annotations-Dekoration idempotent gemacht. Kette grün, Tests grün bis auf Baselines.
- 2026-09-11: `S3b.4` (web/`getWeb()`/`DEFAULT_WEB` aus Java entfernt, `Attributes.WEB` inert), `S4.6` (Rdf2GoCore synchroner Shutdown, Consumer-Check) und `S4.7` abgeschlossen; ebenso `S4.5`/`S4.3.6`-Checkboxen konsolidiert. Zuletzt das ungenutzte deprecated `Environment.shutdown()` entfernt. Kette grün, Tests KnowWE/DES/SSP/Experimental grün bis auf Baselines. `S4` damit abgeschlossen; offen bleiben nur Beobachtungen (21 Singleton-Typen, `ArticleLifecycleFixture`-Reflection, Surefire-`reuseForks`-Prüfung am Ende).
- 2026-09-10: `S4.3` Wellen `S4.3.1`, `S4.3.3`-`S4.3.5` committet (KnowWE, DES, KRONE, SSP), `S4.3.6` in KnowWE committet, in DES vorbereitet (siehe Stand). Falle: Surefire kürzt Stacktraces auf die Testklasse (`trimStackTrace`), der OntoVis-NPE lag in `Utils.createConceptURL` (Section null im Test).
- 2026-09-07: Analyse, Plan, Arbeitsumgebung eingerichtet, Kette gebaut, Baseline-Tests grün.
- 2026-09-07: `S1` umgesetzt und committet (9e19e1b30). Voller KnowWE-Testlauf 1:53 min, nur die bekannten Git-Provider-Fehler.
- 2026-09-08: `S3b` (`web` entfernen) als eigener Durchgang aufgenommen, mit Messung und Vorgehen. `S3`-Umschreibung (`context.getEnvironment()`, Environment-Varianten der Statics) in allen Repos committet.
- 2026-09-08: `S3.M2` in KnowWE (statische Utilities mit Environment-Overload, deprecated Delegation), `Instantiation.init(Environment)`, `WikiSearchService` als erster Environment-Service, `UserContext.getEnvironment()`. KnowWE-main von 88 auf 31 `getInstance()`-Stellen.
- 2026-09-08: `Environment.initInstance()` schließt eine noch laufende Environment (c679ea688); vorher lief `TaskContextManagerEventTest` mit `initialized=true` in die Typbaum-Assertion.
- 2026-09-08: `S3.M1` in allen Repos per Skript (Kontextobjekt aus Methodenparameter oder Klassenfeld). Fallen: lokale Variablen nicht fuer Felder halten (nur Deklarationen mit genau einem Tab), Dateien mit CRLF-Zeilenenden byteweise behandeln (42 Dateien mussten repariert werden), `Environment`-Import nur entfernen, wenn das Wort nirgends mehr vorkommt (mehrzeilige Aufrufe). Kompilierfehler wurden zeilenweise aus git zurueckgesetzt (revert_errors.py), die Stellen bleiben beim statischen Zugriff.
- 2026-09-08: `S2.3`/`S2.4` umgesetzt, voller KnowWE-Testlauf grün bis auf die bekannten Git-Tests. In KnowWE-core bleibt an statischem Zustand nur noch: der Singleton `Environment.instance/initialized` selbst (`S4`), zustandslose Singletons (Renderer, Visitor, SectionFinder), Konstanten/Caches ohne Lebenszyklus (`Priority`, `Scope`, `Section.COMPILER_REFS` schwach) und `ServletContextEventListener`-Tasks (Servlet-Lebenszyklus). `ResourceLoader` (JS/CSS-Registrierung, dedupliziert) bleibt bis `S3`/`S4`.
- 2026-09-08: `S2.1`/`S2.2` umgesetzt. Reset-Liste in `close()` besteht nur noch aus den drei Singleton-Typen (`S2.3`). Voller KnowWE-Testlauf grün bis auf die bekannten Git-Tests. Fallen: `close()` muss die Compiler vor den Services stoppen (Destroy-Scripts schreiben noch Messages); `AbstractType.removeCompileScript` darf beim Typ-Aufbau nicht auf einen noch nicht indexierten Typ zugreifen; `LongOperationUtilsTest` braucht jetzt eine Environment (per `ArticleLifecycleFixture`).
- 2026-09-07: Plan nach Rücksprache umgebaut: Ziel ist Besitz statt Reset (`S2` neu definiert, Migrationsmuster für `S3`, `getInstance()` wird komplett entfernt). Consumer-Repos geklont; Experimental, KnowWE-DES, KnowWE-SSP kompilieren gegen Stufe 1. KRONE braucht `-Dskip.installnodenpm -Dskip.npm -Dskip.pnpm` (Symlinks auf vboxsf).
