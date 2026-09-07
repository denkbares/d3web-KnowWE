# KnowWE `Environment`: vom statischen Singleton zur Instanz

Stand: 2026-09-07. Arbeitsbranch `env-instance` in `Projects/EnvInstance`.

## Ausgangslage

`de.knowwe.core.Environment` ist intern bereits eine Instanz. Statisch sind nur der Singleton-Zugriff
(`getInstance()`, `initInstance()`, `isInitialized()`, Felder `instance`/`initialized`),
`retrieveLatestVersionNumber(String)` und die Konstante `DEFAULT_WEB`.

Direkte Aufrufer von `Environment.getInstance()` (Stand master, 2026-09-07):

| Repo | Dateien | Aufrufe |
|---|---|---|
| KnowWE | 123 | 235 |
| KnowWE-DES | 155 | 272 |
| Experimental | 98 | 145 |
| KRONE | 77 | 127 |
| KnowWE-SSP | 51 | 87 |
| denkbares-Internals | 17 | 17 |
| Information-Extraction | 1 | 1 |

Aufgerufene Methoden: `getWikiConnector()` 406, `getArticleManager(..)` 136, `getArticle(..)` 69, `getContext()` 29, Rest einstellig.
Indirekt: `Environment.DEFAULT_WEB` 256, `Compilers.getCompilerManager(web)` ~490 (281 in KnowWE-SSP),
`KnowWEUtils.get(Default)ArticleManager()` ~115, `initInstance` 40 (fast nur Tests), `isInitialized` 21.

Der `web`-Parameter ist tot: es gibt keinen Aufruf mit einem anderen Literal als `DEFAULT_WEB`.

## Transitive statische Zustände (das eigentliche Problem)

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

## Zielbild

`ArticleManager` ist der natürliche Instanz-Hub: `Section`, `Article`, `UserContext` und `Compiler` (über `CompilerManager`)
führen heute schon zu `getArticleManager()`. Es fehlt nur die Rückverbindung `ArticleManager -> Environment`.

## Stufen

### Stufe 1: Rückverbindung, keine Aufrufer anfassen (KnowWE-core, 1-2 Tage)
- `ArticleManager.getEnvironment()`; `DefaultArticleManager` bekommt die Environment im Konstruktor.
- `Environment.getInstance()` bleibt, wird `@Deprecated`.
- `shutdown()` leert alle oben gelisteten Registries explizit (nicht nur per Event) und setzt die Singleton-Typen zurück.
- Ergebnis: robuste Re-Inits in Tests.

### Stufe 2: Registries in die Instanz ziehen (KnowWE-core, 3-5 Tage, hohe Regressionsgefahr)
- `sectionMap`, `scriptManagers`, SectionizerModule-Registry, Messages, occupiedTerms, ToolUtils-Settings werden Felder von
  `Environment` bzw. `ArticleManager`/`CompilerManager`. Statische Zugriffe bleiben als Delegation auf `getInstance()`.
- Achtung Threading in `Section` (synchronized auf `sectionMap`) und `CompilerManager`.
- Abnahme: komplette Test-Suiten aller Consumer-Repos.

### Stufe 3: Aufrufer migrieren, repo-weise (1-2 Tage pro Repo, 8-12 Tage gesamt)
- Muster A: `Environment.getInstance().getWikiConnector()` -> `section.getArticleManager().getEnvironment().getWikiConnector()`
  bzw. Helper `KnowWEUtils.getEnvironment(section|context|compiler)`.
- Muster B: `Environment.getInstance().getArticleManager(DEFAULT_WEB)` -> `context.getArticleManager()` / `section.getArticleManager()`.
- Muster C: `Compilers.getCompilerManager(DEFAULT_WEB)` -> `section.getArticleManager().getCompilerManager()`.
- Schwer: statische Utilities ohne Kontextobjekt (`KnowWEUtils`, `Compilers.getCompilerManager(String)`), dort Parameter einführen.
- Reihenfolge: KnowWE -> KnowWE-DES -> KnowWE-SSP -> Experimental -> KRONE -> Internals/IE. Repos müssen gleichzeitig kompilieren.

### Stufe 4: `web` und Singleton entfernen (2-3 Tage)
- `DEFAULT_WEB`, `articleManagers`-Map, `getArticleManager(String web)` weg.
- `getInstance()` löschen. Testinfrastruktur (`TestArticleManager`, `DummyConnector`, `HeadlessKnowWE`) auf `new Environment(connector)` pro Testklasse.

## Entscheidung
Stufe 1 und 2 lösen das Testproblem und bleiben in KnowWE-core. Nach Stufe 2 entscheiden, ob 3 und 4 den Aufwand wert sind.
`EventManager`/`PluginManager` aus Commons bleiben in jedem Fall global.

## Arbeitsumgebung
- Klone in `Projects/EnvInstance` (Host-Share), Branch `env-instance`, `origin` = git.denkbares.com, Remote `main-local` = `Projects/Main/<repo>`.
- Isoliertes Maven-Repo `/home/cody/.m2/env-instance-repo`, Build per `build-chain.sh`, `JAVA_HOME=/usr/lib/jvm/zulu-25-amd64`.
- Push nur vom Host.
