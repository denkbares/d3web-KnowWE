# Konzept: Wiki-Variablen für InterWikiImport

Status: Entwurf v5 (2026-11-16). Ticket: ILMERED-2279. Die Schritte 1 bis 6 (KnowWE-core, Include-Plugin,
CBX; Abschnitt 10) sind umgesetzt. Core und Include sind mit Tests abgedeckt; alles ist in der Testinstanz
(Abschnitt 13) geprüft, der CBX-Teil dort mit drei `%%ServiceMate`-Varianten. Offen sind Hilfeseiten, die
Migration und die Darstellungsfragen in Abschnitt 12.

Änderungen gegenüber v4:

- **Ein Attachment je Quell-Wiki** statt Fehler bei unterschiedlichen Belegungen. v4 hatte das
  verworfen, weil der Attachment-Artikel die Packages des Import-Markups erbt und damit von
  allen Varianten kompiliert würde. v5 löst das über eine Package-Regel am Attachment-Artikel
  (Abschnitt 5).
- **Variablen-Registry in KnowWE-core, package-basiert**, nicht mehr am ServiceMateCompiler.
  Die Packages der Attachment-Artikel werden im PackageRegistrationCompiler (Gruppe 2)
  festgelegt; der ServiceMateCompiler läuft erst in Gruppe 6 und käme zu spät.
- Die Nutzungsregel aus v4 („Import-Markups mit Variable nur in varianten-eindeutigen
  Packages“) entfällt.

## 1. Ziel

Es wird je zwei Turm- und FG-Wikis geben, die sich gegenseitig importieren, und zwar
variantenabhängig: Fahrzeug A im Turm-Wiki 1 importiert aus FG-Wiki 1, Fahrzeug B im selben
Turm-Wiki aus FG-Wiki 2. Die Import-Seiten sollen dafür nicht dupliziert werden.

```
%%InterWikiVariable
@name: fgWiki
@value: https://fg-wiki-1.example.com
@package: Fahrzeug-A
%

%%InterWikiVariable
@name: fgWiki
@value: https://fg-wiki-2.example.com
@package: Fahrzeug-B
%

%%InterWikiImport
@wikiVariable: fgWiki
@page: Fehlercodes
@package: shared
%
```

Ergebnis: Die Seite bekommt zwei Attachments, eines je Quell-Wiki. Variante A kompiliert und
zeigt das aus FG-Wiki 1, Variante B das aus FG-Wiki 2.

**Voraussetzung in den Wikis (bestätigt):** Je Fahrzeug gibt es ein Package, das alle Compiler
der Variante einbinden. In diesem Package liegt die Definition.

## 2. Annotation `@wikiVariable`

Unverändert aus v4:

- Neue Annotation `@wikiVariable: <name>` am `%%InterWikiImport`, kein `${...}`-Parsing.
- `@wiki` wird optional. Genau eine der beiden Annotationen muss belegt sein; das prüft ein
  kleines `DefaultGlobalCompiler`-Script im Include-Plugin:
    - beide leer → Fehler „Entweder @wiki oder @wikiVariable muss angegeben werden.“
    - beide belegt → Fehler „@wiki und @wikiVariable dürfen nicht gleichzeitig angegeben werden.“
- Importe mit `@wiki` (Literal) verhalten sich exakt wie bisher, inklusive Attachment-Name.

**Umgesetzt (`InterWikiImportMarkup`):** `getWikis(section)` liefert alle Quell-Wikis des Imports,
`getWikis(section, user)` bzw. `getWiki(section, user)` die der gewählten Variante. `getWiki(section)` und
`getWikiAttachmentPath(section)` (ohne Wiki) liefern nur dann etwas, wenn der Import genau eine Quelle hat;
bei mehreren Quellen ist das Ergebnis `null`. Aufrufer außerhalb des Plugins (CBX, Schritt 6) müssen für
solche Importe auf die Varianten mit Wiki bzw. Nutzer umgestellt werden.

## 3. Variablen in KnowWE-core

Markup `%%InterWikiVariable` mit `@name` (Pflicht, Pattern `[\w.-]+`), `@value` (Pflicht) und
`@package`. Mehrere Variablen = mehrere Markups. Markup und Registry liegen in KnowWE-core
(z. B. Package `de.knowwe.core.compile.variables`), damit der `AttachmentManager` und das
Package-Script sie nutzen können; das Feature ist damit nicht mehr an CBX gebunden.

Die Registry hat zwei Ebenen:

- **Statischer Index (`VariableIndex`)**: `name → Definitions-Sections`. Er wird beim
  Registrieren eines Artikels gefüllt (`ArticleRegisteredEvent`), nicht beim Kompilieren, und
  liest `@name` und `@value` direkt aus den Annotationen des KDOM. Lesen ist lock-frei
  (Concurrent-Maps), tote Sections werden über `Sections.isLive` gefiltert. Diese Ebene
  beantwortet „welche Werte hat die Variable im ganzen Wiki“ und wird für die Attachment-Pfade
  gebraucht (Abschnitt 6).
- **Package-Sicht**: Zu jeder Definition liefert `getPackageInfo(definition)` die Packages und
  Package-Regeln, unter denen sie gilt. Daraus ergibt sich
    - für einen Wert: die Bedingung „irgendeine Definition dieses Werts ist aktiv“
      (für die Package-Regel der Attachment-Artikel, Abschnitt 5),
    - für einen Compiler: die Werte, deren Definitionen er kompiliert
      (`VariableManager.get(compiler).getValues(name)`, für Anzeige und Prüfung).

Der Renderer des Markups zeigt Name → Wert und die Varianten, in denen die Belegung gilt.

**Umgesetzt (KnowWE-core, `de.knowwe.core.compile.variables`):**

- `InterWikiVariableMarkup`, registriert in der `plugin.xml` von KnowWE-Plugin-Core. Der Renderer ist noch der
  Standard-Renderer.
- Optionale Annotation `@displayName`: der Name, unter dem der Wert dieser Definition angezeigt wird, z. B.
  `FG-Wiki A` und `FG-Wiki B` für die beiden Definitionen der Variable `FG-Wiki`.
- `VariableIndex`, je Wiki über `DefaultArticleManager.getVariableIndex()` bzw. `VariableIndex.get(section)`.
  Die Sicht je Compiler ist keine eigene Klasse geworden: `getValues(name, compiler)` und
  `getDefinitions(name, compiler)` liefern die Definitionen, die der Compiler kompiliert.
- Der Index merkt sich je Seite die Artikelversion, aus der er gelesen hat. Ist die Seite inzwischen ersetzt
  (auch ohne Event, etwa nach einem Rollback), liest er aus der aktuellen Version statt veraltete Sections zu
  liefern.
- Definitionen in kompilierten Attachments werden ignoriert (sonst hinge die Menge der Attachments von ihrem
  eigenen Inhalt ab); das Markup zeigt dort eine Warnung.

## 4. Auflösung im Import-Markup

Für eine Import-Section mit `@wikiVariable: name`:

- **Alle Quellen des Markups**: die unterschiedlichen, per `normalizeWiki` normalisierten Werte
  aus dem statischen Index. Daraus ergeben sich die Attachments und die Poll-Ziele.
- **Quelle einer Variante**: die Werte, deren Definitionen der Compiler der Variante
  kompiliert. Erwartet wird genau einer (Prüfung in Abschnitt 8).

`getWiki(section)` gibt es für Variablen-Importe nicht mehr als einzelne Antwort. Die Aufrufer
(u. a. `getUrl`, Renderer-Header, Snapshots im UpdateService) werden auf „je Quelle“ bzw.
„für diese Variante“ umgestellt.

## 5. Ein Attachment je Quell-Wiki

**Pfad:** `<Titel>/WikiImport-<Page>[-<Section>]@<Schlüssel>.txt`. Der Schlüssel wird aus der
normalisierten URL gebildet (Schema entfernt, alles außer `[A-Za-z0-9.-]` zu `_`), z. B.
`WikiImport-Fehlercodes@fg-wiki-1.example.com.txt`. Varianten mit gleichem Wert teilen sich
das Attachment. Führen zwei unterschiedliche Werte zum selben Schlüssel, ist das ein Fehler am
Import-Markup.

**Package-Regel statt geerbter Packages:** `DefaultMarkupPackageScript.getPackageInfo` übernimmt
für Sections eines Attachment-Artikels heute die Packages des kompilierenden Markups. Neu kann
das `AttachmentCompileType` dem Pfad eine zusätzliche Bedingung mitgeben. Der Attachment-Artikel
für Wert V bekommt dann ausschließlich die Regel

```
(Packages des Import-Markups) AND (Packages der Definitionen von V)
```

im Beispiel `shared AND Fahrzeug-A`. Mehrere Packages bzw. Definitionen werden jeweils mit OR
verknüpft, vorhandene Package-Regeln als Teilausdruck übernommen. Die Regel wird wie jede
Package-Regel je Compiler gegen dessen Packages ausgewertet; deshalb muss das Package der
Definition von allen Compilern der Variante eingebunden sein (Abschnitt 1).

Gibt es zu einem Attachment keine Definition mehr, wird dafür kein Artikel registriert
(Abschnitt 6) und das Attachment beim nächsten Poll gelöscht.

Der Schlüssel im Pfad enthält keine Zugangsdaten aus der URL (`toSourceKey`). Der Import schreibt sein
`@package` wie bisher beim Holen in die importierten Markups; die Regel oben setzt deshalb auf den Packages
auf, die im Attachment stehen, und nur ohne solche auf denen des Import-Markups.

Gelöscht werden nur Attachments der Seite, die wie die eines Variablen-Imports heißen
(`WikiImport-<Page>[-<Section>]@….txt`) und von keinem Import der Seite mehr verwendet werden, und nie die
eines Imports im Tracking-Modus (dort steckt die Historie der Referenz). Das alte Attachment eines Imports,
der von `@wiki` auf `@wikiVariable` umgestellt wurde (`WikiImport-<Page>.txt`), bleibt liegen; das gehört
zur Migration (Abschnitt 12).

**Erweiterung der API (KnowWE-core):**

- `AttachmentCompileType`: zusätzlich `getCompiledAttachmentPaths(section)` (Menge), Default
  delegiert an `getCompiledAttachmentPath`. `%%Attachment` und Literal-Importe bleiben
  unverändert. *(umgesetzt)*
- `AttachmentCompileType.getCompileConditionSections(section, path)`: die Sections, von denen ein Compiler
  zusätzlich mindestens eine kompilieren muss, damit er das Attachment kompiliert. Für den Import sind das die
  Definitionen des Werts. Default ist `null` (keine Bedingung). Daraus baut
  `DefaultMarkupPackageScript.getPackageInfo` die Regel oben; Package-Namen werden bei Bedarf gequotet.
  *(umgesetzt)*
- `AttachmentManager`: arbeitet mit der Pfadmenge. `pathToSectionsMap` ist bereits n:m.
  Sections werden auch dann registriert, wenn ihre Pfadmenge gerade leer ist. Neue Methode
  `refreshCompiledAttachmentPaths()` (Abschnitt 6). *(umgesetzt)*
- Aufrufer der Pfad-API durchsehen (16 Dateien, davon 6 in CBX: `TsmElements`,
  `ListPackagesMarkup`, `DescriptionsContainer`, `SignalConsistencyTest`, `CableMarkupSummary`,
  `TSMSolutionDefinition`; dazu `ReferenceWikiAligner` und die A8-Exporte als Nutzer von
  `InterWikiImportMarkup`).

## 6. Startreihenfolge und Änderungen zur Laufzeit

### Ablauf heute

`KnowWEPlugin.initializeAllArticles` öffnet einen Registrierungsrahmen (`open()`), sektioniert
alle Seiten **parallel** und reiht sie mit `queueArticle` ein. Erst das `commit()` am Ende
startet die eine initiale Kompilation.

Jedes `queueArticle` feuert auf seinem Thread ein `ArticleRegisteredEvent`. Der
`AttachmentManager` liest daraufhin die Attachment-Pfade der neuen Seite
(`handleArticleUpdate`, `synchronized`) und reiht vorhandene Attachments sofort als Artikel in
denselben Rahmen ein. So sind importierte Inhalte Teil der initialen Kompilation.

### Das Problem

Mit Variablen hängen die Pfade einer Import-Seite von Definitionen auf **anderen** Seiten ab.
Wegen des parallelen Einlesens ist beim `ArticleRegisteredEvent` der Import-Seite nicht
garantiert, dass die Definitions-Seite schon sektioniert ist. Kompiliert ist zu diesem
Zeitpunkt ohnehin noch nichts; jede Lösung, die auf einen Compiler wartet, verschiebt die
importierten Inhalte in eine zweite Kompilation nach dem Start.

### Lösung: beidseitige Auswertung auf Registrierungsebene

Die Pfade werden ausschließlich aus dem statischen Index (Abschnitt 3) berechnet, der ohne
Compiler auskommt. Ausgewertet wird von beiden Seiten:

1. **Import-Seite wird registriert:** Der `AttachmentManager` trägt die Import-Section ein
   (auch bei leerer Pfadmenge) und berechnet danach ihre Pfade aus dem Index. Beides geschieht
   innerhalb von `handleArticleUpdate`, also unter dem Monitor des `AttachmentManager`.
2. **Definitions-Seite wird registriert:** Der `VariableIndex` entfernt die Definitionen der
   alten Seitenversion, trägt die neuen ein und ruft erst danach
   `AttachmentManager.refreshCompiledAttachmentPaths()` auf. Diese Methode läuft unter
   demselben Monitor, berechnet für alle Sections mit variablen Pfaden die Pfadmenge neu und
   vergleicht sie mit der bisher registrierten: neue Pfade werden als Artikel eingereiht,
   entfallene gelöscht.

Damit ist das Ergebnis unabhängig von der Reihenfolge:

- Läuft der Refresh (2) **nach** der Registrierung der Import-Section (1), findet er die
  Section und sieht den Wert, weil der Index vor dem Refresh geschrieben wurde.
- Läuft er **davor**, ist der Index bereits geschrieben, bevor (1) ihn liest, und die
  Import-Seite berechnet ihre Pfade selbst richtig.

Voraussetzung ist die feste Abfolge auf beiden Seiten: „erst eintragen, dann lesen“. Der Index
hält beim Aufruf des Refresh keinen eigenen Lock und ist lock-frei lesbar; sonst entstünde
eine umgekehrte Lock-Reihenfolge zwischen Index und `AttachmentManager`.

Beim Start laufen beide Schritte für alle Seiten innerhalb des offenen Rahmens. Nach dem
letzten `queueArticle` sind alle Pfade bekannt und alle vorhandenen Attachments eingereiht; es
bleibt bei **einer** initialen Kompilation.

### Packages in Gruppe 2

Die Package-Regel eines Attachment-Artikels (Abschnitt 5) wird im PackageRegistrationCompiler
berechnet. Zu diesem Zeitpunkt sind alle Seiten sektioniert, der Index ist vollständig. Die
Packages einer Definition werden mit `getPackageInfo(definition)` direkt aus deren Annotationen
gelesen und hängen nicht davon ab, ob das Registrierungs-Script der Definition schon gelaufen
ist.

Einzige Abhängigkeit von der Script-Reihenfolge: Hat die Definition kein eigenes `@package`,
gelten die Default-Packages ihrer Seite (`%%Package`), die ebenfalls in Gruppe 2 gesetzt
werden. Dieselbe Abhängigkeit besteht heute schon zwischen Attachment-Artikel und
Import-Markup. Sie wird per Test abgesichert; unabhängig davon sollten Definitionen ihr
`@package` ausdrücklich angeben.

### Noch nicht vorhandene Attachments

Existiert ein Attachment noch nicht (erster Start nach der Umstellung, neuer Wert), wird wie
heute kein Artikel angelegt. Der Poll direkt nach dem Start holt den Inhalt, das
`AttachmentStoredEvent` registriert den Artikel, und es folgt eine inkrementelle Kompilation
nur für den neuen Inhalt. Das entspricht dem heutigen Verhalten bei einem neuen Import.

### Änderungen zur Laufzeit

Derselbe Mechanismus greift, wenn eine Definitions-Seite gespeichert wird:

- **Wert geändert oder Definition entfernt/ergänzt:** Der Refresh ergibt neue und entfallene
  Pfade. Entfallene Artikel werden gelöscht, für neue Pfade wird ein Poll der betroffenen
  Markups angestoßen.
- **Nur das Package geändert:** Die Pfade bleiben gleich, aber die Package-Regel der
  Attachment-Artikel stimmt nicht mehr. Der `AttachmentManager` merkt sich deshalb je Pfad die
  Bedingungs-Sections (`getCompileConditionSections`). Ergibt der Refresh andere Sections als
  zuvor, wird der Attachment-Artikel neu registriert, sodass seine Package-Registrierung neu
  läuft. Das betrifft genau die Attachments der Werte, die auf der gespeicherten Seite definiert
  sind oder waren; Definitions-Seiten werden selten bearbeitet.

### Attachments im Compile-Fenster ihrer Seite

Wird die Seite eines Imports (oder eines `%%Attachment`) neu registriert, nimmt der `AttachmentManager`
die weiterhin kompilierten Attachments in dasselbe Compile-Fenster auf, wenn es nötig ist:

- die Package-Angaben der Seite haben sich geändert (`@package` eines Markups oder `%%Package`),
- das Markup hat das Kompilieren ein- oder ausgeschaltet,
- der Attachment-Text ist neuer als der kompilierte Artikel, etwa weil dasselbe Update Attachment und
  Seite (`@latestChange`) schreibt. Seite und Inhalt werden dann in einem Durchlauf kompiliert statt in
  zwei; das spätere `AttachmentStoredEvent` findet den Artikel aktuell vor und tut nichts mehr.

Bewusst nicht bei jeder Registrierung der Seite: Ein Update aus FG-Wiki 2 schreibt `@latestChange` in die
Import-Seite. Würden dabei immer alle Attachments der Seite neu registriert, müsste auch Variante A ihren
unveränderten Inhalt aus FG-Wiki 1 neu kompilieren.

Wechselt eine Variante ihr Quell-Wiki, entsteht dadurch ein anderes Attachment mit eigenem
Stand. Der Sonderfall „Belegungswechsel mit altem `@latestChange`“ aus v4 (Abschnitt 8 dort)
entfällt.

## 7. Polling (`InterWikiImportUpdateService`)

- Ein Snapshot je (Markup, Quell-Wiki) statt je Markup. Die Gruppierung nach Quell-Wiki und
  ein Request je Quelle bleiben.
- `processUpdates` braucht zum Update die Quelle, um das richtige Attachment zu schreiben.
- Locks, „letzte Prüfung“ und Sync-Meldungen werden je Attachment-Pfad geführt.
- **`@latestChange` je Quelle:** Bei Variablen-Importen wird die Belegung an den Zeitstempel
  angehängt, eine Annotation je Quelle:
  ```
  @latestChange: 2026-11-16T09:30:00Z https://fg-wiki-1.example.com
  @latestChange: 2026-11-14T17:02:11Z https://fg-wiki-2.example.com
  ```
  Bei Literal-Importen bleibt es beim reinen Zeitstempel. Zeilen zu Quellen, die es nicht mehr
  gibt, werden beim nächsten Update entfernt.
- Der Refresh-Button aktualisiert alle Quellen des Markups.

**Umgesetzt:** wie beschrieben. Der Zugriff auf die Quell-Wikis läuft über
`InterWikiImportUpdateService.ChangeRequester` (Standard: HTTP), damit Tests ihn ersetzen können.
Meldungen zu fehlgeschlagenen Abfragen nennen bei Variablen-Importen das betroffene Wiki und werden je
Quelle geführt. Wird eine Seite mit Definitionen gespeichert, werden die Importe mit Variablen sofort
abgefragt, damit eine neue Quelle nicht bis zum nächsten Poll leer bleibt. Vor jedem Poll werden
Attachments entfallener Quellen gelöscht (Abschnitt 5). Fehlt das Attachment einer Quelle, wird ihr Inhalt
unabhängig von `@latestChange` neu geholt; sonst bliebe eine Quelle leer, die nach dem Löschen ihres
Attachments wieder Wert der Variable wird.

## 8. Anzeige und Prüfung

**Anzeige (Include-Plugin):** Der Renderer ermittelt die Quelle der aktuell gewählten Variante
(Default-Compiler des Nutzers, `Compilers.isDefaultCompiler`) und rendert den zugehörigen
Attachment-Artikel. Beim Umschalten der Variante wird damit das andere Attachment gezeigt. Der
Header zeigt `fgWiki → aufgelöstes Wiki` mit Link. Hat die gewählte Variante keine Belegung,
erscheint ein Hinweis statt Inhalt. `waitForUpdate` wartet auf den Artikel der gewählten
Variante.

**Umgesetzt (Include):** Kopfzeile, Inhalt, „letzte Änderung“ und das Warten auf das erste Update richten
sich nach der Quelle der gewählten Variante. Maßgeblich sind die Definitionen, die die gewählten Compiler
kompilieren: Eine Variante ohne Definition sieht nichts, auch wenn alle anderen Varianten dasselbe Wiki
verwenden. Die Kopfzeile nennt statt der URL den Namen der Variable („Import from FG-Wiki: Fehlercodes“),
verlinkt auf die importierte Seite im Wiki der gewählten Variante (die URL steht als Tooltip am Link und
im Konfigurationsblock). Zur geltenden `%%InterWikiVariable`-Definition führt der Eintrag „Go to wiki
variable“ im Toolmenü des Markups; hat die gewählte Variante keine Definition, führt er zu den vorhandenen.
Hat die geltende Definition einen `@displayName`, steht dieser statt des Variablennamens in der Kopfzeile
(„Import from FG-Wiki A: Fehlercodes“); ebenso heißen die Wikis im Konfigurationsblock und an den
`@latestChange`-Zeilen dann so. Ohne Anzeigenamen wird der Name der Variable gezeigt. Hat die Variante keinen oder mehrere Werte, ist die Variable
gar nicht definiert oder lassen sich zwei Wikis im Attachment-Namen nicht unterscheiden, zeigt das Markup
statt des Inhalts einen Hinweis (`getMissingSourceMessage`). Diese Hinweise entstehen beim Rendern und
sind deshalb immer aktuell, auch wenn nur eine Definitions-Seite geändert wurde. Compile-Meldungen gibt es
im Include-Plugin nur für die Annotationen selbst (`@wiki` und `@wikiVariable` beide oder keine).

**Überschriften und Listen im importierten Inhalt:** Unabhängig von den Variablen blieb alles, was am
Zeilenanfang stehen muss (`!!!`, `*`, `#`, `----`), im Import oft roh. Der Import-Renderer arbeitet deshalb
nicht mehr im vorformatierten Stil, und der importierte Artikel wird so gerendert, dass der Inhalt und jeder
seiner obersten Abschnitte auf einer neuen Zeile beginnt (`LineStartRootRenderer`). Testseite im Turm-Wiki:
`Ueberschriften Test`.

**Prüfung je Variante:** Das Script `WikiVariableCheckScript` (Include-Plugin, generisch über den Compiler
der Variante) hängt an `%%InterWikiImport` und an `%%InterWikiVariable` und fragt
`VariableIndex.getDefinitions(name, compiler)`:

- genau ein Wert → keine Meldung
- kein Wert → Warnung am Import: in dieser Variante wird nichts importiert
- mehrere verschiedene Werte → Fehler am Import mit Wikis und Definitions-Seiten; die Variante kompiliert die
  Inhalte aller dieser Wikis
- an jeder Definition, zu der es in der Variante eine mit anderem Wert gibt → Warnung mit Verweis auf die
  anderen, auch wenn kein Import der Variante die Variable verwendet

Die Meldungen werden je Compiler gespeichert. Sie hängen von anderen Sections ab, deshalb prüft das Script
bei jedem Kompilieren oder Entfernen einer Definition die übrigen Definitionen der Variable und die Importe,
die sie verwenden, erneut. So bleiben die Meldungen aktuell, wenn nur eine Definitions-Seite geändert wird.
Ein Script-Compiler führt ein Script je Section und Kompilation nur einmal aus, eine Schleife entsteht nicht.

In CBX bindet `ServiceMateWikiVariableCheck` das Script an den `ServiceMateCompiler` (zwei
`CompileScript`-Einträge in der `plugin.xml`, Priorität LOWEST). Nebenwirkung: Wird die Import-Seite
geändert (auch durch `@latestChange`), läuft der `ServiceMateCompiler` jeder Variante, die den Import
kompiliert, kurz an und führt nur dieses Script aus; importierte Inhalte anderer Varianten werden dabei nicht
neu kompiliert. Wikis ohne solchen Compiler haben nur die Hinweise beim Rendern.

**Aufrufer in CBX:** `AttachmentCompileType.getCompiledAttachmentArticles(section)` liefert die Artikel aller
Attachments eines Imports. `TsmElements`, `ListPackagesMarkup`, `DescriptionsContainer`,
`SignalConsistencyTest` und `CableMarkupSummary` nutzen das jetzt; sie filtern anschließend ohnehin nach
Compiler. Nicht angepasst sind die Export- und Abgleichwerkzeuge (`A8WikiExportTurm`, `ReferenceWikiAligner`),
die mit `@wiki`-Importen arbeiten.

## 9. Tracking-Modus

`@mode: tracking` vergleicht den lokalen Text unter dem Markup mit einer Referenz. Der lokale
Text steht einmal auf der Seite und gilt für alle Varianten, er kann also nur einer Referenz
folgen. Deshalb:

- Variable mit genau einem Wert über alle Varianten → Tracking funktioniert wie bisher.
- Variable mit mehreren Werten → Fehler am Markup. Wer je Variante unterschiedlich tracken
  will, teilt Markup und lokalen Text per Packaging auf.

**Umgesetzt:** Bei mehreren Werten wird der Import nicht abgefragt und das Markup zeigt den Fehler beim
Rendern.

## 10. Umsetzungsschritte

1. **core (erledigt):** `%%InterWikiVariable`, `VariableIndex` mit Sicht je Compiler
2. **core (erledigt):** `AttachmentCompileType`/`AttachmentManager` auf Pfadmengen,
   `refreshCompiledAttachmentPaths`, Package-Regel in `getPackageInfo`, Tests zur
   Startreihenfolge und zu Änderungen im Betrieb (Abschnitt 11).
3. **Include (erledigt):** `@wikiVariable`, Exklusivitäts-Check, Auflösung, Pfad-Schlüssel
4. **Include (erledigt):** UpdateService je (Markup, Quelle), `@latestChange` je Quelle, Aufräumen
   verwaister Attachments
5. **Include (erledigt, Anzeige noch nicht im laufenden Wiki geprüft):** Renderer (Variante, Header,
   Hinweise), Tracking-Regel
6. **CBX (erledigt, im CBX-Turm der Testinstanz geprüft):** Check-Script, Warnung an den Definitionen, Aufrufer
   der Pfad-API anpassen
7. Hilfeseiten, dieses Dokument auf den umgesetzten Stand bringen
8. **Wikis (Ticket Punkt 5):** Variablen je Fahrzeug-Package definieren, sobald Schritt 1
   ausgerollt ist

**Zurückgestellt: Migrationsskript (Ticket Punkt 6).** Es kommt erst nach Abschluss aller
anderen Schritte und ist einfach: Es ersetzt in `%%InterWikiImport` das `@wiki: <URL>` anhand
einer Tabelle URL → Variable, mit Trockenlauf und Bericht. Ob vorhandene Attachments dabei
umbenannt oder beim nächsten Poll neu geholt werden, wird dann entschieden.

## 11. Tests

Kein Test importiert aus einem anderen Wiki. Geprüft wird in allen Fällen dasselbe Ergebnis:
welche Attachment-Artikel es gibt, welche Variante welche Sections kompiliert und welche
Meldungen an den Markups stehen.

### Stand: Tests in KnowWE-core

Die Core-Tests laufen unterhalb der Plugin-Schicht, wie die übrigen Lifecycle-Tests dort: echtes
`Environment` mit eigenem Typbaum, echter `DefaultArticleManager`, `AttachmentManager`,
`VariableIndex`, PackageRegistrationCompiler und `CompilerManager`, aber ohne `InitPluginManager`.

- `VariantAttachmentWiki` (Test-Quellen von KnowWE-core): das Test-Wiki. Enthält das echte
  `%%InterWikiVariable` und drei Test-Markups: `%%TestImport` (steht für den Import, ein Attachment
  je Wert der Variable), `%%TestVariant` (Compile-Markup mit `@uses`, erzeugt den aufzeichnenden
  PackageCompiler) und `%%TestContent`. Attachments liegen im Speicher; Speichern und Löschen
  feuern `AttachmentStoredEvent` bzw. `AttachmentDeletedEvent`.
- `VariableAttachmentStartOrderTest`: alle 720 Reihenfolgen der sechs Seiten, 150 parallele
  Durchläufe, Definition und Import auf einer Seite, Definition ohne `@package`, fehlendes und
  verwaistes Attachment, Literal-Import. Geprüft wird jeweils auch, dass es bei einer Kompilation
  bleibt.
- `VariableAttachmentChangeTest`: Änderungen an Definitionen, Importen und Attachments im
  laufenden Wiki; am Ende jedes Tests der Vergleich mit einem Neustart auf denselben Seiten und
  Attachments.
- `VariableIndexTest`: der Index für sich.

### Stand: Tests im Include-Plugin

Auch diese Tests laufen ohne `InitPluginManager`, anders als unten ursprünglich geplant: Das Test-Wiki
baut den Typbaum selbst aus den echten Typen (`%%InterWikiImport`, `%%InterWikiVariable`, `%%Package`)
und den Test-Markups für Variante und Inhalt. So hängen die Tests nicht an `target/dependencies/output.txt`
und brauchen kein Test-Plugin für die Test-Markups. Dafür registriert `InterWikiImportMarkup` seine Action
für die CSRF-Allowlist nur noch, wenn ein Plugin-Manager initialisiert ist.

- `InterWikiImportTestWiki`: das Test-Wiki mit `DummyConnector` (feuert die Attachment-Events) und der
  Attrappe `SourceWikis` für die Quell-Wikis. Sie hält je Wiki und Seite Text und Änderungszeitpunkt und
  antwortet wie `GetWikiChangesSinceAction`. Die geplanten Polls sind abgeschaltet, die Tests rufen
  `pollRegisteredMarkups()` auf.
- `InterWikiVariableImportTest`: erster Poll, Update nur einer Quelle (die andere Variante kompiliert
  nicht neu), beide Quellen, unveränderte Quellen, Quelle nicht erreichbar oder nicht autorisiert,
  Wertänderung und Entfernen einer Definition samt Löschen des alten Attachments, Definition ohne
  `@package` auf einer Seite mit `%%Package` in beiden Reihenfolgen, Umstellung von `@wiki` auf
  `@wikiVariable`, Annotations-Prüfung, Tracking mit einem und mit mehreren Werten, Quelle der gewählten
  Variante. Wo es passt, endet der Test mit dem Vergleich gegen einen Neustart.

Nicht durch Tests abgedeckt ist das gerenderte HTML; das braucht die Plugin-Schicht und wird im laufenden
Wiki geprüft.

### Testaufbau

- **Umgebung:** echtes `Environment` mit Plugins über `InitPluginManager` und
  `DummyConnector`, wie in den Tests des Search-Plugins. Das Include-Plugin hat
  `KnowWE-GlobalTestUtils` bereits als Abhängigkeit. Damit laufen die echten Markups, der
  PackageRegistrationCompiler und der `AttachmentManager`.
- **Attachment-Events:** `AttachmentStoredEvent` und `AttachmentDeletedEvent` feuert im
  Betrieb nur der JSPWiki-Connector. Der Test-Connector muss sie beim Speichern und Löschen
  ebenfalls feuern, sonst erreicht ein Update den `AttachmentManager` nicht.
- **Varianten:** ein minimaler PackageCompiler in den Test-Quellen (Compile-Markup mit
  `@uses`), der nur aufzeichnet, welche Sections er kompiliert. Zwei Instanzen mit
  unterschiedlichen Packages stehen für zwei Fahrzeuge. Ontology- oder d3web-Compiler werden
  nicht gebraucht.
- **Start nachstellen:** Die Seiten werden wie in `initializeAllArticles` in einem
  Registrierungsrahmen eingereiht und dann gemeinsam kompiliert. Die Reihenfolge des
  Einreihens gibt der Test vor.
- **Quell-Wiki ersetzen:** Im `InterWikiImportUpdateService` wird nur der HTTP-Aufruf
  (`requestChanges`) hinter eine kleine Schnittstelle gelegt. Der Test setzt dort eine
  Attrappe ein, die je Quell-Wiki und Seite einen Text mit Änderungszeitpunkt bereithält und
  wie das echte Quell-Wiki antwortet: Sie liefert ein Update nur, wenn der Inhalt neuer ist
  als das angefragte `@latestChange`. Alles danach läuft unverändert durch den Service:
  Gruppierung nach Quelle, `processUpdates`, Schreiben des Attachments, `@latestChange` in der
  Seite, `AttachmentStoredEvent`, Registrierung des Attachment-Artikels, Kompilation.
- **Polls auslösen:** Der Test ruft den Poll direkt und synchron auf. Der Zeitplaner des
  Service wird im Test nicht gestartet.

### Startverhalten

Ausgangslage sind eine Import-Seite, zwei Definitions-Seiten (Fahrzeug A und B), zwei
Varianten und vorhandene Attachments beider Quellen.

- Alle Reihenfolgen des Einreihens von Import-Seite, Definitions-Seiten und Varianten-Seiten
  werden durchgespielt (Permutationen), zusätzlich paralleles Einreihen in mehreren
  Durchläufen. Das Ergebnis muss in allen Fällen identisch sein und nach **einer** Kompilation
  feststehen.
- Dasselbe mit Definition und Import auf derselben Seite.
- Attachment einer Quelle fehlt noch: kein Artikel beim Start, nach dem ersten Poll vorhanden
  und nur von der passenden Variante kompiliert.
- Attachment ohne zugehörige Definition liegt vor: wird nicht kompiliert und beim Poll
  gelöscht.
- Definition ohne `@package`, Seite mit `%%Package`: gleiche Ergebnisse in beiden
  Reihenfolgen.

### Änderungen im Betrieb

Jeweils ausgehend vom fertig gestarteten Zustand, mit Prüfung nach der Änderung und nach dem
folgenden Poll:

- **Definitionen:** Wert ändern, Package ändern, Definition entfernen, Definition für eine
  weitere Variante ergänzen, zweite Definition mit anderem Wert in derselben Variante
  (Konflikt, Meldungen an Import und Definitionen) und wieder entfernen.
- **Importe:** Import-Markup neu anlegen, entfernen, `@package` ändern, von `@wiki` auf
  `@wikiVariable` umstellen und zurück, Variablenname ändern, `@page` ändern.
- **Updates aus der Quelle:** Inhalt in nur einer Quelle geändert (nur deren Attachment und
  deren `@latestChange` ändern sich, nur deren Variante kompiliert neu), in beiden Quellen
  geändert (eine Seitenänderung für beide), unveränderter Inhalt (keine neue
  Attachment-Version), eine Quelle nicht erreichbar oder nicht autorisiert (Meldung nur für
  diese Quelle, die andere wird aktualisiert).
- **Kombination:** Definition ändern, während ein Update ansteht; danach muss derselbe
  Zustand erreicht sein wie beim Start mit den neuen Inhalten.

Als Gegenprobe für die letzte Gruppe dient ein Neustart-Vergleich: Der Zustand nach den
Änderungen im Betrieb muss dem Zustand entsprechen, den ein frischer Start mit denselben
Seiten und Attachments ergibt.

### Weitere Tests

- `VariableIndex` für sich: Registrierung, neue Seitenversion, tote Sections.
- Literal-Importe (`@wiki`): unverändertes Verhalten, gleicher Attachment-Name wie bisher.
- Tracking mit einem Wert und mit mehreren Werten (Abschnitt 9).
- Anzeige: gerenderter Inhalt je gewählter Variante.
- Zum Abschluss ein manueller Durchlauf in einer lokalen Instanz mit zwei echten Quell-Wikis.

## 12. Offene Punkte

1. Migration der Attachments (umbenennen oder neu holen), zusammen mit dem Migrationsskript.
2. Später: Nutzung der Variablen durch andere Markups.
3. Erledigt: Geänderte Package-Angaben der Seite erreichen die kompilierten Attachments jetzt ohne
   Full-Parse (Abschnitt 6, „Attachments im Compile-Fenster ihrer Seite“).
4. `A8WikiExportTurm` und `ReferenceWikiAligner` (CBX) kennen nur `@wiki`-Importe: Für Importe mit mehreren
   Quellen liefert `getWikiAttachment(section)` dort `null`. Bei der Migration mit ansehen.
5. Schon vor dieser Änderung vorhanden, in der Testinstanz `~/knowwe-iwi` aufgefallen: Das Schreiben von
   `@latestChange` in die Seite löst über den Speichern-Listener sofort einen erzwungenen zweiten Abruf
   aller Quellen der Seite aus. Mit mehreren Quellen je Import fällt das stärker ins Gewicht.
6. Ebenfalls schon vorher vorhanden: Holt ein Import neue d3web-Lösungen oder -Fragen, meldet der
   `D3webCompiler` „Non-incremental script was added during incremental compilation“ und kompiliert
   danach vollständig; die Meldung bleibt bis zum Neustart an den importierten Markups stehen.

7. Darstellung der Warnung „in dieser Variante wird nichts importiert“ (im CBX-Turm aufgefallen): Sie
   erscheint wie jede Meldung eines anderen Compilers auch in den Ansichten der übrigen Varianten („compiled
   in C“); dadurch ist der Import in allen Varianten gerahmt, sobald irgendeine Variante keinen Wert hat; und
   in der Ansicht der betroffenen Variante steht die Aussage doppelt (Compile-Warnung und Render-Hinweis).
   Zu entscheiden: Warnung behalten, zur Information herabstufen oder weglassen (der Render-Hinweis bleibt).

## 13. Testinstanz

Unter `~/knowwe-iwi` laufen drei Wikis (Turm 8091 mit CBX-WAR, FG 1 8092 und FG 2 8093 als Vanilla) mit
Beispielseiten; Aufbau,
Skripte und das Umschalten der Variante stehen in `~/knowwe-iwi/README.md`.
