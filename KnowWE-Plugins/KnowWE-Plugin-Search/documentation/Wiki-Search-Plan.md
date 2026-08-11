# mkdocs-artige Suche für KnowWE/JSPWiki

> Umsetzungsplan für `KnowWE-Plugin-Search`. Branch `wikisearch` (gleichnamig in allen
> beteiligten Repos: `KnowWE`, später `KnowWE-SSP`).
>
> **Dateipfade in diesem Dokument sind relativ zur Multi-Repo-Wurzel** (dem Ordner, der
> `KnowWE/`, `jspwiki/`, `denkbares-Internals/`, `KnowWE-SSP/` … enthält), nicht relativ
> zum `KnowWE`-Repo — sonst ließen sich die Verweise nach `jspwiki` und
> `denkbares-Internals` nicht ausdrücken.

## Context

Die Wiki-Suche ist aus drei Gründen schlecht — alle in der Substanz, nicht im CSS:

1. **Ein Lucene-Dokument pro Seite, über den rohen Wiki-Quelltext.**
   `LuceneSearchProvider.luceneIndexPage()` ([:359](jspwiki/jspwiki-main/src/main/java/org/apache/wiki/search/LuceneSearchProvider.java:359)) indiziert den Seitenquelltext inkl. `%%`-Markup, Annotationen und XML. Folge: Treffer auf Markup-Rauschen, keine Abschnittsauflösung, kein Deep-Link auf die Überschrift.
2. **`ClassicAnalyzer` als Default** ([:111](jspwiki/jspwiki-main/src/main/java/org/apache/wiki/search/LuceneSearchProvider.java:111)) — kein Deutsch, kein Stemming, kein camelCase-Splitting, keine Prefix-/Tippfehlertoleranz.
3. **Die Trefferliste ist eine Tabelle „Seitenname | Score-Balken".**
   [AJAXSearch.jsp](KnowWE/KnowWE-Essentials/KnowWE-Resources/src/main/webapp/templates/default/AJAXSearch.jsp) — Snippets nur mit gesetztem „details"-Häkchen, und dann Markup-Fragmente.

mkdocs-material macht es umgekehrt: **ein Dokument pro Abschnitt**, sauberer Text, Treffer als `Seite › Überschrift` mit Kontext und hervorgehobenen Begriffen. Dank KDOM können wir das übertreffen: statt eines Textschnipsels den **echt gerenderten Abschnitt**.

### Festgelegte Entscheidungen

| Frage | Entscheidung |
|---|---|
| Index | Server-seitig Lucene 10, **ein Doc je Überschriften-Abschnitt**, aus dem **KDOM** |
| Markup | `%%Question`, `@file` auffindbar (als `%%Question` *und* `Question`), niedrig gewichtet |
| Ort | Eigener KnowWE-Index + eigene Action — nur in KnowWE sind Sections/KDOM bekannt |
| Preview | **gerenderte** KnowWE-Preview je Treffer, nicht ausklappbar |
| CBX | `NGramLuceneSearchProvider`-Mechanik wandert nach KnowWE, CBX wird dünn |
| Anker | Section-ID → Fallback `positionInKDOM` → Überschriftentext; Stale-Hinweis |
| Reihenfolge | **Suchseite zuerst**, Schnellsuche danach auf derselben Action |

---

## Ist-Zustand (verifiziert)

- Aktives Template: **`templates/default/`** (Haddock-Overlay). `templates/KnowWE/` ist tot (dessen `AJAXSearch.jsp` importiert `org.apache.log4j.*`).
- Suchseite: `Search.jsp` → `ViewTemplate.jsp` → `FindContent.jsp` (**upstream, nicht überschrieben**) → JS `wiki/Search.js` XHRt gegen `wiki.XHRSearch`, gesetzt in [commonheader.jsp:109](KnowWE/KnowWE-Essentials/KnowWE-Resources/src/main/webapp/templates/default/commonheader.jsp:109) auf `templates/default/AJAXSearch.jsp` — **das gehört bereits KnowWE**.
- Schnellsuche: `wiki/Findpages.js` → `/ajax/search/pages/<q>/16` → `DefaultSearchManager.JSONSearch`. Nur Seitennamen.
- Konfigurierter Provider: `LuceneSearchProvider` (Profile 1/2), **`NGramLuceneSearchProvider`** (Profil 3).
- `ExtensibleLuceneSearchProvider` + Extension Point `SearchProvider`: **toter Code**, keine Implementierung, in keinem Profil aktiv.
- Lucene ist überall auf **10.4.0** gepinnt (`jspwiki/pom.xml:77`, `denkbares-Internals/pom.xml:59`) — eine Lucene-Version im Classpath.

---

## Zielbild GUI

```
┌───────────────────────────────────────────────────────────────┐
│  🔍  steckverbinder montage                               [×] │
│  128 Treffer · 34 ms       [Alles] [Titel] [Anhänge] [Markup] │
├───────────────────────────────────────────────────────────────┤
│  Kabelbaum X-200  ›  Montage  ›  Steckverbinder               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  gerenderte KnowWE-Preview des Abschnitts               │  │
│  │  (echte Tabellen, %%-Boxen), Treffer als <mark>         │  │
│  └─────────────────────────────────────────────────────────┘  │
│      ↳ Prüfung › Sichtprüfung        (weiterer Abschnitt      │
│                                       derselben Seite)         │
│  Prüfanweisung P-14  ›  Sichtprüfung                          │
│  …                                                             │
└───────────────────────────────────────────────────────────────┘
```

- Suche-beim-Tippen (300 ms Debounce), `?query=` deep-linkbar, Browser-History.
- Breadcrumb: Seitenteil → Seite, Überschriftenteil → `Wiki.jsp?page=X#<anchor>`.
- **Gruppierung nach Seite**: bester Abschnitt oben, weitere Abschnitte derselben Seite eingerückt darunter — sonst füllt eine große Seite die erste Trefferseite allein.
- Tastatur: `↑`/`↓`, `Enter`, `Esc`.
- „Mehr laden" statt Seitenzahlen (Previews sind hoch).
- Nulltreffer: anzeigen, **welcher** Begriff nirgends vorkam.
- Styling ausschließlich über die vorhandenen CSS-Variablen ([variables.css](KnowWE/KnowWE-Essentials/KnowWE-Resources/src/main/webapp/KnowWEExtension/css/variables.css)), damit `.dark-mode` mitläuft.

---

## Architektur

```
KDOM  ──InitializedArticlesEvent / ArticleManagerCommitDoneEvent──▶  WikiSearchService
                                                                      │  eigener Writer
                                                                      │  SearcherManager (NRT)
                                                                      ▼
                                                              Lucene 10, N Docs/Seite
                                                                      │
                        ┌─────────────────────────────────────────────┤
                        ▼                                             ▼
             WikiSearchAction (JSON)                    WikiSearchProviderAdapter
             + SearchResultRenderer                     implements org.apache.wiki
               (PreviewManager)                           .search.SearchProvider
                        ▼                                  (erst beim Cutover)
             KnowWE-Plugin-Search.js
             ├─ Suchseite    (Stufe 1)
             └─ Schnellsuche (Stufe 2)
```

**Warum *nicht* von `LuceneSearchProvider` erben** (das war mein erster Reflex, er trägt nicht):
- Dessen `updateLuceneIndex()` öffnet pro Seite `FSDirectory` + `IndexWriter` und committet — bei 50 000 Seiten untragbar.
- Der Hintergrund-Updater startet unabhängig vom KnowWE-Compile-Zyklus; er würde ein halb kompiliertes Wiki indizieren. KnowWEs `InitializedArticlesEvent` ([KnowWEPlugin.java:545](KnowWE/KnowWE-Essentials/KnowWE-Plugin-JSPWiki-Connector/src/main/java/de/knowwe/jspwiki/KnowWEPlugin.java:545)) feuert dagegen garantiert nach `awaitTermination()` des CompilerManagers.
- `SearchProvider.findPages` kann Abschnittstreffer nicht ausdrücken (Rückgabe ist seitenbasiert).
- Die ACL-Filterung dort holt `MAX_SEARCH_HITS = 99_999` Treffer und prüft pro Treffer — die Gesamtzahl wird damit bedeutungslos.

Stattdessen: eigener Service, eigene Action. Beim Cutover ersetzt ein **dünner Adapter** (`implements org.apache.wiki.search.SearchProvider`, ohne eigene Indizierung, `reindexPage`/`pageRemoved` = no-op) den JSPWiki-Provider, damit `/ajax/search/pages` und alles andere über `SearchManager` weiterhin funktioniert — **und damit JSPWikis eigener Lucene-Indexer abgeschaltet ist und nicht ein zweiter 50k-Index parallel läuft.** Die Extension-Point-Schleife aus `ExtensibleLuceneSearchProvider` wandert in diesen Adapter.

---

## Neues Modul: `KnowWE/KnowWE-Plugins/KnowWE-Plugin-Search`

Präzedenzfall für die Dependency-Richtung: [KnowWE-Plugin-Include/pom.xml](KnowWE/KnowWE-Plugins/KnowWE-Plugin-Include/pom.xml) hängt bereits an **beidem** — `KnowWE-Plugin-JSPWiki-MarkupSet` (HeaderType) und `KnowWE-Plugin-JSPWiki-Connector` (JSPWiki-SPI). Genau das Paar brauchen wir. Der Connector selbst darf *nicht* auf MarkupSet erweitert werden (erste Essentials→Plugins-Kante).

```
de.knowwe.search
  ├── WikiSearchService            Writer + SearcherManager + Queue
  ├── WikiSearchInstantiation      Instantiation-Extension (Registrierung)
  ├── WikiSearchIndexListener      EventListener-Extension
  ├── WikiSearchAction             AbstractAction, Action.JSON
  ├── WikiSearchProviderAdapter    JSPWiki-SPI (Stufe 4)
  ├── index/     SearchFields · KdomSectionSplitter · KdomTextExtractor
  │              SectionDocumentBuilder · AclExtractor · SectionAnchor
  ├── analysis/  WikiTextAnalyzer · MarkupTokenAnalyzer · EdgeGramAnalyzer
  ├── query/     WikiQueryBuilder · AclFilterBuilder
  └── render/    SearchResultRenderer
```

POM-Arbeit vorab: `KnowWE/pom.xml` hat **keine** `lucene.version` und kein Lucene-`dependencyManagement` (Lucene kommt bisher nur transitiv über `jspwiki-knowwe-main`). Beides ergänzen, plus `denkbares-LuceneUtils` mit `${denkbares-Internals.version}` analog zum vorhandenen `denkbares-Git-Essentials`-Eintrag.

⚠️ JS/CSS-Dateinamen **müssen** mit der Plugin-ID beginnen, sonst verwirft [`KnowWEPlugin.includeDOMResources`](KnowWE/KnowWE-Essentials/KnowWE-Plugin-JSPWiki-Connector/src/main/java/de/knowwe/jspwiki/KnowWEPlugin.java:709) sie mit einer ERROR-Zeile.

Zu ändern außerhalb des Moduls:
- **neu** `KnowWE-Resources/src/main/webapp/templates/default/FindContent.jsp` — dünner Mount-Point
- `KnowWE-App/pom.xml` — Dependency in allen Profilen
- `KnowWE-App/.../jspwiki-custom*.properties` — erst beim Cutover (Stufe 4)
- `KnowWE-SSP/KnowWE-Plugin-CBX` — `NGramLuceneSearchProvider` entkernen

---

## Abschnittsbildung aus dem KDOM

Verifiziert: `HeaderType`, `HeaderType1`, `HeaderType2` sind alle mit `scope=root` registriert — Überschriften sind **flache Geschwister** unter `article.getRootSection().getChildren()`, nicht verschachtelt. Der Regex-Finder macht eine `HeaderType`-Section genau **eine Zeile** lang. Die Ebene ist invertiert: `!!!` = H1, also `level = 4 - HeaderType.getMarkerCount()`.

`KdomSectionSplitter.split(Article)`:
1. Alles vor der ersten Überschrift → Intro-Chunk, Anker = `rootSection`.
2. Jede `HeaderType`-Section startet einen Chunk, der bis zur **nächsten Überschrift beliebiger Ebene** reicht.
   **Bewusst nicht `JSPWikiMarkupUtils.getContent(header)`** — das schließt Unterüberschriften samt Inhalt ein und erzeugt damit *überlappende* Dokumente (H1-Chunk enthält alle H2-Chunks). Disjunkte Chunks, Hierarchie über den Breadcrumb — so macht es mkdocs auch.
   Beim *Rendern* ist `getContent(header, true)` dagegen genau richtig; `HeaderPreviewRenderer` nutzt es bereits.
3. Breadcrumb über einen `String[3]`-Stack: bei Ebene *L* `stack[L-1]` setzen, tiefere nullen.
4. Seite ohne Überschriften → ein Dokument, Anker = `rootSection`. `ArticlePreviewRenderer` greift dann automatisch.
5. Leere Chunks verwerfen.

Textextraktion (`KdomTextExtractor`), rekursiv mit Typ-Strategien:
- [`PlainText`](KnowWE/KnowWE-Essentials/KnowWE-core/src/main/java/de/knowwe/core/kdom/basicType/PlainText.java)-Blatt → Text übernehmen.
- `DefaultMarkupType` → **Rohtext nicht** übernehmen, sondern `DefaultMarkupType.getContent()` + alle Annotation-Inhalte; Markup- und Annotationsnamen wandern ins `markup`-Feld (`%%Question`, `@file`).
- Rest → rekursiv; blattloser Nicht-`PlainText` → `getText()`.

Damit fällt das Markup-Rauschen raus, das heute die Snippets ruiniert.

---

## Dokumentmodell (`SearchFields`)

| Feld | Typ | gespeichert | Analyzer | Zweck |
|---|---|---|---|---|
| `pageKey` | StringField | – | – | `title.toLowerCase(ROOT)` — **Update-/Löschschlüssel** (ArticleManager ist case-insensitiv) |
| `type` | StringField | ✓ | – | `section` \| `attachment` |
| `title` | StoredField | ✓ | – | Anzeigeform |
| `titleText` / `titleGram` | TextField | – | Wiki / EdgeGram | Titel, höchster Boost + as-you-type |
| `heading` / `headingGram` | TextField | – | Wiki / EdgeGram | Überschrift des Abschnitts |
| `breadcrumb` | TextField + Stored | ✓ | Wiki | angezeigt **und** durchsuchbar |
| `body` | eigener FieldType | ✓ | Wiki | sauberer Abschnittstext, Quelle fürs Highlighting |
| `markup` | TextField | – | MarkupToken | Markup-/Annotationsnamen, Boost **0.3** |
| `sectionId` · `sectionPath` · `anchor` · `ordinal` | StoredField (+ DocValues) | ✓ | – | Anker + Sortierung |
| `lastModified` | LongPoint + DocValues | ✓ | – | Recency |
| `hasAcl` · `aclView` | StringField | – | – | ACL-Filter (s.u.) |
| `pageKeySort` | SortedDocValues | – | – | Gruppierung nach Seite |

`body` bekommt `IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS` (Postings-Offsets) statt Term-Vektoren — `UnifiedHighlighter` bevorzugt genau das, Term-Vektoren würden den Index etwa verdoppeln. Nur `body` braucht Offsets.

⚠️ `LuceneUtils.expand(text)` **nicht** auf `body` anwenden: es fügt `"(Variante)"` inline ein und zerstört damit die Zeichen-Offsets → kaputtes Highlighting. Auf den kurzen Feldern `titleText`/`heading` ist es dagegen ein billiger Recall-Gewinn.

---

## Anker-Auflösung (`SectionAnchor`)

`Section.getID()` ist **kein haltbarer Schlüssel**: die ID entsteht aus einem Hash über `web+title+offset+depth+text`, wird in einer JVM-globalen `sectionMap` registriert (lazy!) und Kollisionen werden durch Hochzählen aufgelöst. Nach JVM-Neustart kann eine gespeicherte ID auf `null` laufen — oder, schlimmer, auf eine *andere* Section.

Deshalb die von dir vorgeschlagene Kaskade, mit Verifikation nach Schritt 1:

1. `Sections.get(sectionId)` — **und prüfen**: `getTitle()` passt, `Sections.isLive(s)`, Typ ist `HeaderType`.
2. `Sections.get(article, positionInKDOM)` — die etablierte KnowWE-Idiomatik ([Sections.java:673](KnowWE/KnowWE-Essentials/KnowWE-core/src/main/java/de/knowwe/core/kdom/parsing/Sections.java:673) macht es genauso; `SectionInfo` persistiert `positionInKDOM` genau dafür).
3. Erste `HeaderType`-Section mit passendem `getHeaderText()`.
4. Sonst Artikel-Wurzel → Seitentreffer via `ArticlePreviewRenderer`.

Ab Stufe 2 bekommt der Treffer einen dezenten Hinweis („Seite wurde seit der Indizierung geändert"), damit ein veraltetes Preview nicht als aktuell gelesen wird.

---

## Analyzer

**`WikiTextAnalyzer`** (Index / Query über ein Konstruktor-Flag):
`StandardTokenizer` → `WordDelimiterGraphFilter` (`GENERATE_*|SPLIT_ON_CASE_CHANGE|SPLIT_ON_NUMERICS|CATENATE_*|PRESERVE_ORIGINAL`) → `FlattenGraphFilter` (nur Index) → `LowerCaseFilter` → `GermanNormalizationFilter` → `ASCIIFoldingFilter` → `KeywordRepeatFilter` → `GermanLightStemFilter` → `RemoveDuplicatesTokenFilter`.
Der `WordDelimiterGraphFilter` ist das Äquivalent zu mkdocs' camelCase-Tokenizer: `getPageName` → `get, page, name, getpagename, getPageName`. **Keine Stoppwörter** — die Konvention aus `LuceneUtils.TermAnalyzer.standard`. `SplitTokenFilter` wird davon subsumiert und nicht gebraucht.

**`MarkupTokenAnalyzer`** — löst die `%%Question`/`@file`-Anforderung:
`WhitespaceTokenizer` (behält `%` und `@`) → `LowerCaseFilter` → **`PatternCaptureGroupTokenFilter(preserveOriginal=true, "^[%@]+(.*)$")`** — emittiert `%%question` *und* `question` an derselben Position, ebenso `@file` und `file` → `WordDelimiterGraphFilter(SPLIT_ON_CASE_CHANGE|PRESERVE_ORIGINAL)` + `FlattenGraphFilter`, damit `%%KnowledgeBase` auch über `knowledge base` gefunden wird. Query-seitig derselbe Analyzer, damit `%%Question`, `%Question` und `Question` alle treffen. Die niedrige Gewichtung kommt aus dem Query-Boost, nicht aus dem Analyzer.

**`EdgeGramAnalyzer`** — `EdgeNGramTokenFilter(1, 20, preserveOriginal)` **nur** auf `titleGram`/`headingGram`, nie auf `body` (500 k Body-Dokumente zu grammen vervielfacht den Index). Query nie grammen.
Eskalation, falls deutsche Komposita zu schlecht ankommen (`mitte` → `Bauteilmitte`): zusätzliches Infix-Feld auf `LuceneUtils.TermAnalyzer.ngram_faster` — das ist der Analyzer, den das Haus schon getunt hat.

---

## Query

Handgebaut, **kein** `MultiFieldQueryParser` (wirft `ParseException` bei unbalancierten Quotes, kann keine Per-Field-Boosts und kein Dis-Max).

1. Strukturierte Präfixe vorab parsen: `author:`, `page:`, `after:`, `"Phrasen"`.
2. Freitext tokenisieren via `LuceneUtils.tokenize(...)`.
3. Je Term ein **`DisjunctionMaxQuery`** (tie 0.1): `titleText` 8.0 · `heading` 5.0 · `breadcrumb` 2.0 · `body` 1.0 · `attachmentNames` 0.5 · **`markup` 0.3** · `FuzzyQuery(body, 1)` 0.2 ab Länge 4.
   Auf der `FuzzyQuery` [`DisjunctionMaxRewrite`](denkbares-Internals/denkbares-LuceneUtils/src/main/java/com/denkbares/util/lucene/DisjunctionMaxRewrite.java) setzen — genau dafür existiert die Klasse: ohne sie summieren sich mehrere Fast-Treffer und schlagen den exakten.
4. Kombinieren als `SHOULD` mit `setMinimumNumberShouldMatch`: 1 Term → 1; 2–3 → alle; ≥4 → n−1. Das verhindert, dass eine Fünf-Wort-Anfrage leer läuft.
5. Bei `partial=true` (noch am Tippen): letztes Token zusätzlich gegen `titleGram`/`headingGram` und als `PrefixQuery(body)` mit **`CONSTANT_SCORE_BLENDED_REWRITE`** (bewusst begrenzt, s. Risiko 2).
6. Äußere Hülle: `scoring` als `MUST`, ACL und Filter als **`FILTER`** (kein Scoring, Bulk-Scorer-Pfad).
7. **Similarity: Lucene-Default BM25 belassen.** `PlainSimilarity` ausdrücklich *nicht* — sie setzt `idf()=1` und `lengthNorm()=1`, was für Konzeptlabels richtig, für Fließtext aber schädlich ist.
8. Recency als ein schwacher `SHOULD` (`LongPoint.newRangeQuery(lastModified, vorEinemJahr, MAX)`, Boost 0.3) statt `boostByValue` auf Epoch-Millis.

Snippets: `UnifiedHighlighter` auf `body`, nicht der Legacy-`Highlighter` (der analysiert je Treffer den ganzen gespeicherten Text neu).

---

## ACL: Filterklausel statt Nachfilterung

JSPWikis ACL-Modell ist nach Lektüre von `DefaultAuthorizationManager.checkPermission` **grant-only, ohne Deny, mit Namens-/Typgleichheit** — und damit exakt als Lucene-Filter ausdrückbar. `isUserInRole` konsultiert laut Javadoc *nicht* den Authorizer, sondern nur die beim Login injizierten Principals.

**Index-Seite:** `AclManager.getPermissions(page)` **nicht** aufrufen — bei nicht gecachter ACL rendert `DefaultAclManager` die *komplette Seite nach HTML*, nur um `[{ALLOW …}]` zu parsen. Stattdessen den öffentlichen `DefaultAclManager.ACL_PATTERN` direkt auf den Rohtext anwenden, je Eintrag mit `PagePermission.implies(view)` prüfen (sonst wird aus `[{ALLOW delete Admins}]` versehentlich ein View-Grant) und die Principals als `Role:…` / `GroupPrincipal:…` / `WikiPrincipal:…` in `aclView` schreiben, plus `hasAcl=1`.

**Query-Seite:** Admin (`AllPermission`) → gar kein Filter. Sonst `SHOULD`-Menge aus „`hasAcl` fehlt" (`MatchAllDocsQuery` + `MUST_NOT hasAcl` — reines `MUST_NOT` matcht nichts) und je Session-Principal ein `TermQuery(aclView, …)`.

Dadurch ist `totalHits` von vornherein ACL-korrekt und `searchAfter()` paginiert ohne Overfetch. Zusätzlich auf den tatsächlich zurückgegebenen ≤ pageSize Dokumenten `userCanViewArticle()` als zweite Instanz — Kosten sind durch die Seitengröße begrenzt, und ein Mismatch ist ein lautes Signal für einen veralteten Index.

Grenzfall, der zu dokumentieren ist: **Gruppen-Umbenennung/-Löschung** macht indizierte `GroupPrincipal:AlterName`-Terme stale (Gruppen*mitgliedschaft* dagegen nicht — die kommt aus der Session).

---

## Index-Lebenszyklus bei 50 000 Seiten

**Ort:** `Engine.getWorkDir()/knowwe-search/<schemaVersion>/<wiki>` — analog `LuceneSearchProvider.initialize():144`. **Nicht** `WikiConnector.getSavePath()`, das ist `var.basedir` und würde den Index dem Page-Provider (Datei/Git) unterschieben.

**Versionierung, doppelt:** Verzeichnis-Segment (fängt Feldänderungen) **und** `LuceneUtils.addCommitData(writer, "schemaVersion", …, "analyzerVersion", …)`, beim Öffnen gegengelesen (fängt reine Analyzer-Änderungen, die das Verzeichnis-Segment verpasst). Davor `LuceneIndexCompatibility.probeFormat()` — genau dafür existiert die Klasse.

**Erstaufbau:** getriggert von `InitializedArticlesEvent`, auf einem eigenen Daemon-Thread, in ein Geschwisterverzeichnis `<wiki>.building`, danach `Files.move(ATOMIC_MOVE)`. Ein abgestürzter Lauf hinterlässt ein `.building`, das der nächste Start löscht — selbstheilend, und niemand sieht einen halben Index. Writer mit `RAMBufferSizeMB(256)`, **ein** Commit am Ende, Fan-out über 4–8 Threads.

**Inkrementell:** `WikiSearchIndexListener` (registriert als `PERSISTENT` — `EventManager` hält Listener in einer `WeakHashMap`, sonst wird er wegkollektiert) auf `ArticleRegisteredEvent` (nur sammeln, feuert vor dem Compile), `ArticleManagerCommitDoneEvent` (flushen), `ArticleDeletedEvent`, `ArticleRenamedEvent`, `FullParseEvent`, `AttachmentStored/DeletedEvent`, `DeInitEvent`.
Ersetzt wird **atomar** mit `writer.updateDocuments(new Term(pageKey, key), docs)` — nie manuell löschen und dann hinzufügen, sonst existiert ein Fenster, in dem die Seite unauffindbar ist. Queue alle ~2 s drainen, nie pro Seite committen.

**Snapshot-Regel:** die Strings und Section-IDs *synchron* im Event-Callback ziehen, solange der `Article` noch der lebende ist (`articleManager.getArticle(title) == article` prüfen), und nur das unveränderliche Snapshot an den Hintergrund-Thread geben. Sonst rennt man gegen `unregisterOrUpdateSectionID` und indiziert tote Sections.

**Lesen:** ein `SearcherManager` im NRT-Modus über den eigenen Writer; `maybeRefresh()` macht neue Dokumente ohne Commit sichtbar. Falls [`SearcherContext`](denkbares-Internals/denkbares-LuceneUtils/src/main/java/com/denkbares/util/lucene/SearcherContext.java) verwendet wird: unmittelbar nach dem Konstruktor `setWriter(writer)` aufrufen (sonst öffnet es lazy einen *zweiten* Writer → `LockObtainFailedException`), `useWithWriter` **nicht** auf dem heißen Pfad benutzen (es committet bei *jedem* Aufruf), und beim Herunterfahren in dieser Reihenfolge: Queue drainen → `writer.commit()` → `ctx.close()` → `writer.close()` (der bekannte Windows-Leak: ein offener Writer verhindert dort das Löschen der Indexdateien für die JVM-Lebensdauer).

---

## Attachments

Ein Index, nicht zwei — zwei Indizes haben unvergleichbare Score-Skalen (verschiedene Korpusstatistik ⇒ verschiedene idf), lassen sich nicht nach Relevanz mischen und liefern keine sinnvolle Gesamtzahl.

Attachments werden `type:"attachment"`-Dokumente im selben Index, ein Doc je Anhang, `pageKey` = Schlüssel der **Elternseite** (damit stimmt die ACL automatisch, `DefaultAclManager` delegiert bei Attachments auf die Elternseite), Typ-Boost 0.6. Extraktion hinter einem kleinen SPI: Port von `LuceneSearchProvider.getAttachmentContent()` als Default, Tika-Variante per `ServiceLoader` — Tika ist heute in **keinem** KnowWE-POM und wäre echtes Zusatzgewicht, das eine bewusste Distributionsentscheidung verdient. Zweiter Durchlauf nach den Seiten, Fortschritt in den Commit-Daten.

---

## Umsetzung in Stufen

| Stufe | Ergebnis | Gate |
|---|---|---|
| **0** | POMs (neues Modul, `lucene.version` + dependencyManagement in `KnowWE/pom.xml`), Skelett-`plugin.xml`. CBX-Mechanik + `NGramLuceneSearchProviderRankingTest` nach KnowWE ziehen, Felder/Boosts **unverändert**, CBX auf dünne Subklasse reduzieren | Ranking-Test vor *und* nach dem Verschieben grün |
| **1** | `SearchFields`, `KdomSectionSplitter`, `KdomTextExtractor`, `SectionDocumentBuilder` + Tests über `Article.createTemporaryArticle(...)` | Breadcrumbs und Chunk-Grenzen korrekt; `contents` markupfrei |
| **2** | die drei Analyzer + Token-Stream-Tests | `%%Question` → `{%%question, question}`; `getPageName` → `{get,page,name,getpagename,getPageName}` |
| **3** | `WikiSearchService`, Instantiation, EventListener, Schema-Versionierung, Shutdown-Hook | Vollaufbau auf 50k-Korpus gemessen; Einzeledit < 5 s sichtbar |
| **4** | `WikiQueryBuilder`, `AclFilterBuilder`, `WikiSearchAction` | ACL-Tests: anonym / angemeldet / Gruppe / Admin |
| **5** | `SearchResultRenderer` + `FindContent.jsp`-Overlay + `KnowWE-Plugin-Search.js/.css` — **die Suchseite** | Ende-zu-Ende (s.u.) |
| **6** | Attachments | — |
| **7** | Schnellsuche: `SearchBox.jsp` überschreiben, `Wiki.Findpages` abklemmen, `limit=8&preview=false` | — |
| **8** | Cutover: `WikiSearchProviderAdapter` als `jspwiki.searchProvider`, JSPWiki-Indexer aus, `ExtensibleLuceneSearchProvider` + toter Extension Point entfernen | ein einziger Index im Betrieb |

`previewHtml` nur für das sichtbare Fenster (~10 Treffer) rendern — über `PreviewManager.getInstance().getPreviewRenderer(section)` und die Wiki-Syntax-Nachbehandlung aus [`RenderPreviewAction.handleWikiSyntax()`](KnowWE/KnowWE-Essentials/KnowWE-Plugin-Core/src/main/java/de/knowwe/core/action/RenderPreviewAction.java:88); Rahmen am Vorbild [`ObjectInfoRenderer.renderTermPreview()`:480](KnowWE/KnowWE-Essentials/KnowWE-Plugin-Core/src/main/java/de/knowwe/core/objectinfo/ObjectInfoRenderer.java:480), aber mit Breadcrumb statt Markup-Name. Hervorhebung clientseitig über die Textknoten (`<mark>`) — serverseitiges Highlighting würde in gerendertem HTML Tags zerschießen. Die Action liefert **zusätzlich** ein Textsnippet, damit die Schnellsuche es nutzen kann und wir einen Rückfallweg haben, falls das gerenderte Preview zu langsam ist.

⚠️ `ActionAllowListChecker` lässt einen POST nur durch, wenn die Parameter-Map **genau** `action=…` enthält oder **leer** ist. Ein form-encodetes `POST action=…&query=…` wird abgelehnt → Query im JSON-Body senden.

---

## Risiken

1. **`Section.getID()` ist nicht haltbar** — global, lazy, kollisionsbehaftet. Deshalb die Anker-Kaskade *mit Verifikation*, nie die ID als autoritativ behandeln. (Zusätzlich: `getID()` synchronisiert auf einer JVM-globalen Map — 500 000 Aufrufe aus 8 Threads serialisieren. IDs single-threaded ernten, nur Extraktion/Analyse parallelisieren.)
2. **`LuceneUtils`' statischer Initializer setzt `IndexSearcher.setMaxClauseCount(Integer.MAX_VALUE)` JVM-weit** — und wir *werden* die Klasse laden. Zusammen mit `setAllowLeadingWildcard(true)` im alten Provider ([:487](jspwiki/jspwiki-main/src/main/java/org/apache/wiki/search/LuceneSearchProvider.java:487)) heißt das: ein `*a*` in der *alten* Suchbox versucht auf 500 000 Dokumenten jede Term-Expansion zu materialisieren, statt schnell zu scheitern → OOM für das ganze Wiki. Im neuen Pfad keine Wildcards aus Nutzereingabe, begrenzte Rewrites, Mindestlänge für Prefixe — und beim Cutover verschwindet der alte Pfad.
3. **Kosten des gerenderten Previews.** Wiki-Syntax-Rendering pro Treffer ist deutlich teurer als ein Textsnippet. Deckel auf das sichtbare Fenster, Cache pro (sectionId, Rendering-Version), Textsnippet als Rückfallweg.
4. **Erstaufbau bei 50 k Seiten.** ~500 000 Dokumente. Die Lucene-Hälfte ist 1–4 Minuten; die KDOM-Hälfte ist die unbekannte — dafür Messzeit einplanen.
5. **CBX-Regression** durch Stufe 0. Absicherung ist der vorhandene Ranking-Test.
6. `denkbares-Internals` (closed source) wird harte Dependency eines LGPL-KnowWE-Plugins. Präzedenzfall existiert (`denkbares-Git-Essentials` im Connector), gehört aber bewusst abgenickt.

---

## Arbeitsumgebung

**Der Host-Ordner geht so nicht auf.** Alle vboxsf-Shares (`Main`, `KnowSEC`, `~/.m2/repository`, `VM-Share`) liegen auf demselben Host-Volume: 913 GB groß, **907 GB belegt, 5,9 GB frei**. Eine Kopie aller relevanten Repos passt dort nicht hin, ohne vorher aufzuräumen.

Vorschlag stattdessen — die Arbeitskopie auf die VM-Platte (78 GB, 54 GB frei), die Historie bleibt auf dem Host:

```
git -C Main/KnowWE worktree add /home/cody/denkbares/Projects/WikiSearch/KnowWE -b wikisearch
git config --global --add safe.directory /home/cody/denkbares/Projects/WikiSearch/KnowWE
```

(Der `safe.directory`-Eintrag ist nötig, weil das gemeinsame `.git` im root-owned vboxsf-Share
liegt — dieselben Einträge existieren bereits für `Main/KnowWE` und `Main/KnowWE-SSP`.)

- Die **Dateien** liegen VM-lokal (kostet den Host nichts), die **Commits** landen in `Main/KnowWE/.git` auf dem Host — die Arbeit ist also host-seitig als Branch gesichert, auch wenn der Checkout VM-lokal ist.
- `Main` bleibt unberührt: eigener Branch, eigenes Verzeichnis, deine uncommitteten Änderungen in `Main/KnowWE` bleiben liegen. Umgekehrt nimmt der Worktree sie **nicht** mit — falls in `KnowWE-App/pom.xml` oder `src/resources/local/` lokale Konfiguration steckt, die ich brauche, sag Bescheid.
- Die Session springt per `EnterWorktree` dorthin; für Lesezugriff auf `Main` (jspwiki-Quellen, `denkbares-LuceneUtils`) frage ich den Ordner an.
- `KnowWE-SSP` bekommt erst zur CBX-Stufe einen zweiten Worktree daneben.

Sobald auf dem Host Platz ist, lässt sich der Worktree jederzeit gegen einen echten Share tauschen — die Branch-Historie ist ja schon dort.

**Maven:** eigenes lokales Repository auf der VM-Platte, `-Dmaven.repo.local=/home/cody/.m2/wikisearch-repo`, damit `mvn install` dir die Snapshots im geteilten `~/.m2` nicht überschreibt. Es gibt keine `~/.m2/settings.xml`, die Repositories kommen aus den POMs — ein frisches Local-Repo löst also einmalig alles neu von Artifactory auf. Default bleibt trotzdem `mvn -am` **ohne** install (das neue Modul hat nur `KnowWE-App` als Abnehmer, beide im selben Reaktor); installiert wird nur, wo es wirklich nötig ist.

## Teststrategie

Drei Stufen, absichtlich von leicht nach schwer:

| Stufe | Womit | Was sie abdeckt |
|---|---|---|
| **1 · JUnit, kein Bootstrap** | `Article.createTemporaryArticle(...)` + Lucene-Index im Temp-Verzeichnis | Splitter, Extraktor, Analyzer-Tokenströme, Anker-Kaskade, Query-Aufbau, ACL-Filter, Ranking. Vorbild: `NGramLuceneSearchProviderRankingTest`. Trägt den Großteil und läuft in Sekunden. |
| **2 · HeadlessApp** | `de.knowwe.headless.app.HeadlessApp` mit `--wikiContentPath` auf ein echtes Wiki-Verzeichnis, kein Servlet-Container | Echte KDOMs, echter Compile-Zyklus, Indexaufbauzeit und Ranking auf echten Inhalten; über `ActionChecker` auch `WikiSearchAction` end-to-end. ⚠️ braucht `-Pdenkbares`. |
| **3 · Jetty/Tomcat + Browser** | erst ab Stufe 5 (Suchseite) | Nur die GUI: Breadcrumb, gerendertes Preview, Highlighting, Tastatur, Dark-Mode. Ich fahre das hoch und liefere Screenshots. |

**Testkorpus:** du stellst ein echtes Wiki bereit. Weil der Host voll ist, muss es auf der VM-Platte landen (z.B. `/home/cody/denkbares/Wikis/…`) — nicht in einem Share. Ideal wären zwei: ein mittleres zum Arbeiten und eines in der 50k-Größenordnung für die Aufbauzeit. Bis es da ist, arbeite ich mit dem `exampleWiki` (30 Seiten, `KnowWE-Resources/src/misc/exampleWiki/content`).

**Was ich selbständig tue:** Code und Tests schreiben, `mvn -am test`/`package` laufen lassen, HeadlessApp starten, ab Stufe 5 den Server hochfahren und die Suchseite durchklicken.
**Was ich nicht tue:** in `Main` schreiben · ins geteilte `~/.m2` installieren · committen oder pushen ohne Rückfrage · echte Kundenwikis verändern.

## Umsetzungsnotizen — wo die Realität den Plan korrigiert hat

**Stand:** Stufen 0–2 fertig. Der komplette Suchkern steht und ist getestet (80 Tests): Chunking, Textextraktion, Analyzer, Dokumentmodell, Lucene-Index mit Schema-Absicherung, Query-Aufbau, Anker-Auflösung, Snippets, Relaxation.

**Was noch fehlt, und alles davon braucht das laufende Wiki:** die Anbindung an die KnowWE-Events und den Index-Ort über `Engine.getWorkDir()` (`WikiSearchService`), die ACL-Felder samt Filterklausel, die `WikiSearchAction`, und die Oberfläche. Bis dahin ist die Suche nur über Tests erreichbar, nicht im Browser.

**Die KDOM-Struktur ist anders als im Plan angenommen.** Prosa liegt in `ParagraphType › WikiTextType`, **nicht** in `PlainText`. Der geplante „nur `PlainText`-Blätter"-Extraktor hätte nichts gefunden. Umgekehrt tragen die `PlainText`-Kinder eines Markups die `%%`- und `%`-Delimiter, die er mit indiziert hätte. Beides ist jetzt gegen die tatsächliche Struktur geschrieben.

**Chunk-Grenze ist auch das Markup, nicht nur die Überschrift.** Viele Seiten haben gar keine Überschriften; dort sind ein `%%`-Block und die Prosa drumherum die Einheiten, die ein Leser erkennt — und jeder Block hat ohnehin seinen eigenen `PreviewRenderer`. Der Splitter heißt deshalb `ArticleChunker`, um klarzustellen, dass er nichts am Parsen ändert: er liest nur `article.getRootSection().getChildren()`.

**Überschriftenebenen sind invertiert** (`!!!` = Ebene 1), und der Breadcrumb muss unbenutzte Ebenen überspringen — Seiten beginnen üblicherweise bei `!!`, nicht bei `!!!`. Ohne das bleibt der Breadcrumb leer.

**Query nach Position, nicht nach Token.** Die Analyzer geben absichtlich mehrere Tokens an derselben Position aus (ungestemmt neben gestemmt, ganzer Bezeichner neben seinen Teilen). Die flache Tokenliste als Wortliste zu behandeln hat drei Dinge gleichzeitig kaputtgemacht: eine Ein-Wort-Anfrage verlangte zwei Treffer, der As-you-type-Prefix landete auf der gestemmten Form statt auf dem Getippten, und eine Drei-Wort-Phrase wurde zu einer Fünf-Term-Phrase, die nie matchen kann. `QueryTokens` gruppiert nach Position, Phrasen bauen auf Lucenes `QueryBuilder` auf.

**Markup gehört in die Disjunktion, nicht daneben.** Als reiner `SHOULD`-Bonus konnte ein Block, dessen einziger Treffer sein Markup-Name ist, die Anfrage nie erfüllen.

**Lucene 10:** `setRewriteMethod` gibt es nicht mehr, die Rewrite-Methode ist Konstruktor-Argument.

**Reihenfolge im `MarkupTokenAnalyzer`:** Word-Delimiter-Splitting muss **vor** dem Lowercasing laufen, sonst hat `SPLIT_ON_CASE_CHANGE` keine Groß-/Kleinschreibung mehr und `%%KnowledgeBase` bleibt ein Token.

**Gemessen:** 121 Seiten → 569 Dokumente in 2,5 s, ~20 ms/Seite inklusive KDOM-Parsen. Hochgerechnet ~17 min für 50k Seiten; der Löwenanteil ist das Parsen, nicht Lucene.

**Noch offen und bewusst zurückgestellt:** eine kurze Anfrage, bei der ein Wort nicht vorkommt, liefert null Treffer (`leere Batterie` findet nichts, obwohl `Batterie` fünf Treffer hat). Das ist die Regel „bis 3 Wörter müssen alle matchen". Vorschlag für den Service: strikt suchen, und nur bei null Treffern relaxiert nachfassen.

## Verifikation

- `KdomSectionSplitterTest` / `KdomTextExtractorTest`: Fixture-Artikel → erwartete Chunks, Breadcrumbs, markupfreier `body`; `%%Question` landet als `%%question` **und** `question` im `markup`-Feld, `@file` ebenso.
- Analyzer-Tests über `LuceneUtils.tokenize` mit den oben genannten Erwartungen.
- `SectionAnchorTest`: Artikel reparsen, ID entwerten → Auflösung über `positionInKDOM` liefert dieselbe Section.
- `NGramLuceneSearchProviderRankingTest` (verschoben) bleibt grün, danach um Abschnittsfälle erweitern.
- ACL-Tests mit anonymer / angemeldeter / Gruppen- / Admin-Session gegen Fixtures mit `[{ALLOW view …}]` und `[{ALLOW delete …}]`.
- **Ende-zu-Ende:** `KnowWE-App` bauen und starten, Wiki mit ≥ 1 000 Seiten, `Search.jsp?query=…` öffnen und prüfen: Breadcrumb korrekt, Preview gerendert (Tabellen und `%%`-Boxen sichtbar), Deep-Link springt zur Überschrift, `%%Question` findet Question-Markups, ACL-geschützte Seite fehlt für unberechtigte Nutzer, Dark-Mode stimmt.
- Build-Kontext: `JAVA_HOME=/usr/lib/jvm/zulu-25-amd64`; am Ende **kein** `mvn clean` (`target/dependencies/output.txt` wird für IDE-Testläufe gebraucht).

---

## Offen (Stand 2026-08-09, Ende der GUI-Runde)

1. **Kaputtes HTML aus dem Parser-Recovery — wiegt am schwersten.**
   Ein frisch gerendertes Preview-Paket löst pro Suche mit ~34 Previews 8× die Warnung
   `JSPWikiMarkupParser - Line is longer than maximum allowed size (10240 characters)` aus. Das gelieferte HTML endet
   dann z.B. auf `<p></div</p>` — ein Tag mitten durchgeschnitten, ein `<div>` bleibt offen.
   Ursache ist `SearchResultRenderer.joinRenderedLines`: es ersetzt die Zeilenumbrüche zwischen zwei gerenderten Tags
   durch maskierte `<br/>` und macht damit aus einem ganzen Markup-Block **eine** Zeile; maskierte Zeichen sind lange
   Token, also reißt ein tabellenreicher Block die Grenze.
   Richtung: Absatzbildung unterbinden, ohne alles in eine Zeile zu ziehen (Umbrüche portionsweise stehen lassen), oder
   rein gerenderte Fragmente gar nicht erst durch `renderWikiSyntax` schicken. Ein Block-Wrapper hilft **nicht**, das ist
   gemessen. Test: `SearchResultRendererTest` plus neuer Fall „langer Block bleibt unter der Parser-Grenze".

2. **Verlauf am unteren Rand ist zu kurz.**
   95 px Preview, 21,75 px Verlauf, ~19 px Zeilenhöhe: der Verlauf deckt gerade eine Zeile und blendet deren untere
   Hälfte weg — sieht aus wie ein waagerechter Schnitt durch die Buchstaben. Auf ~3 em verlängern.

3. **Preview-Cache: Invalidierung nicht am laufenden Wiki geprüft.**
   `PreviewCacheTest` deckt die Logik ab, die Verdrahtung in `WikiSearchService.flush()` ist eine Zeile. Ein veraltetes
   Preview nach einer Seitenänderung wäre sichtbar — einmal an einer echten Bearbeitung nachmessen.

4. **ACL-Filterklausel (Stufe 4 Rest)** — weiterhin das einzige echte Release-Gate, siehe oben.

5. **Expand-Modus im `PreviewRenderer`.** Das Preview zeigt den Preview-*Vorfahren* des Treffers, nicht den Abschnitt
   selbst; beim Aufklappen erscheint deshalb Material darüber und darunter. Der Deckel begrenzt nur das Fenster.

Siebzehn Commits auf `wikisearch` (KnowWE), keiner gepusht — Push ist jeweils einzeln abzustimmen.

## Nachtrag 2026-08-10

### Tabellen im Preview: Kopf ohne Zeilen

`TablePreviewRenderer.renderTable()` rendert die erste Zeile plus jede Zeile, die zu den uebergebenen
`relevantSubSections` gehoert; passt keine, kommt eine Platzhalterzeile `...`. `SearchResultRenderer` uebergibt
`List.of(section)` — den Treffer-Abschnitt, also den ganzen Markup-Block, nie eine `TableLine`. Deshalb bleibt immer nur
der Kopf stehen.

Gewuenschtes Verhalten (Albrecht): Tabelle ohne Treffer ganz weglassen; Tabelle mit Treffer in einer Zeile → diese Zeile
zeigen; Treffer im Kopf → Kopf genuegt.

Zwei Wege mit Zielkonflikt:

- **Anfrage in den Renderer geben** — die Zeilen mit Suchbegriff als `relevantSubSections` uebergeben. Kleine Previews,
  aber das Preview haengt dann von der Anfrage ab und der `PreviewCache` verliert seine Wirkung (jeder Tastendruck ein
  anderes Preview: 20 ms → 120 ms).
- **Ganze Tabelle rendern, im Browser aufraeumen** — nach dem Hervorheben ist bekannt, wo die Treffer stehen. Cache
  bleibt wirksam, aber die Previews werden groesser und verschaerfen damit den offenen Parser-Fehler (10240 Zeichen).

Empfehlung: erst den Parser-Fehler (Punkt 1 oben) beheben, dann den zweiten Weg.

### Highlighting bevorzugt den laengsten Treffer

`matchCandidates()` in `KnowWE-Plugin-Search.js` bildet aus der Anfrage alle zusammenhaengenden Wortfolgen, laengste
zuerst; pro Textknoten gewinnt die erste, die passt. „Unit A40" markiert also `Unit A40` bzw. `Unit-A40` als Ganzes und
faellt nur dort auf einzelne Woerter zurueck, wo die Folge nicht vorkommt. Anfuehrungszeichen aendern nichts. Zwischen
zwei Woertern sind 0 bis 3 Nicht-Wortzeichen erlaubt, damit auch `TestCase` von „test case" getroffen wird.
Mit Node gegen neun Faelle geprueft, im Browser noch **nicht**.

## Nachtrag 2026-08-10, zweite Runde

### Phrasensuche ist kaputt — der naechste Punkt

Gemessen an einer Seite, die `Cable Nr-24` **heisst**, deren Text die Zeichenfolge aber nicht enthaelt:

| Anfrage | Ergebnis |
|---|---|
| `Cable Nr-24` | 5 Treffer, exakte Seite oben mit Score 325 |
| `"Cable Nr-24"` | `total: 3`, aber **0 Treffer ausgeliefert** |

Zwei Ursachen, die erste gesichert, die zweite noch nicht zu Ende verfolgt:

1. **`WikiQueryBuilder.phrase()` sucht nur in `SearchFields.BODY`.** Eine Seite, die exakt so heisst, ist mit
   Anfuehrungszeichen nicht findbar. Fix: Dis-Max ueber `TITLE_TEXT`, `HEADING`, `BREADCRUMB`, `BODY` mit den ueblichen
   Gewichten (Titel deutlich hoeher), analog zu `adjacent()`.
2. **`total` und ausgelieferte Trefferliste weichen ab** (3 gegen 0). Zwischen Lucene-Treffer und JSON faellt etwas weg:
   in Frage kommen der ACL-Nachfilter in `WikiSearchAction` (`getArticle(hit.title())` null?), das Fenster in
   `WikiSearcher.search`, oder `collect()` mit `request.offset()`. Muss gemessen werden, nicht geraten.
3. **Entspannung darf eine Phrase nicht fallen lassen.** `build()` haengt die Phrase bei `relaxed` als `SHOULD` an;
   damit werden aus null Treffern beliebige. Wer Anfuehrungszeichen setzt, meint sie — eine leere Antwort ist dann die
   ehrliche.

### Was in dieser Runde fertig wurde

- Nachbarschaft wird belohnt (`adjacent()`, `MultiPhraseQuery` ueber alle Wortformen je Position, Titel 20 / Text 6).
  Gemessen: exakter Seitenname 66 → 338, drei Woerter verstreut unveraendert 12, Seite mit 121x "cable" 8 → 47.
- Anhangs-Index als eigener Durchlauf nach den Seiten; Filter `Attachments` sucht nur darin, Treffer oeffnen
  `Upload.jsp?page=…`. Standard bleiben die Seiten.
- Suchseiten-Filter (`Attachments`, `All Variants`, `Page Name only`) mit tooltipster-Erklaerungen; `All Variants`
  erscheint nur bei mehr als einem `GroupingCompiler`.
- Varianten-Sortierung: DefaultCompiler einmal je Anfrage ermitteln, dann `isCompiling` je Treffer (erste Suche 500 → 80 ms).
- `FindContent.jsp` und `SearchBox.jsp` liegen jetzt im Plugin, nicht mehr in KnowWE-Resources. Ohne das Plugin bleibt
  JSPWikis eigene Suche — **ausser** `jspwiki.searchProvider` nennt `com.denkbares.knowwe.jspwiki.NGramLuceneSearchProvider`,
  dann startet das Wiki nicht (gemessen). Diese Kopplung ist noch offen.
- Score-Erklaerung auf Wunsch: `-Dknowwe.search.explain=true` schreibt Lucenes eigene Rechnung fuer die ersten fuenf
  Treffer ins Log.

### Rueckmeldung aus dem grossen Wiki (2026-08-10, offen)

1. **Suche deutlich langsamer.** Verdaechtig ist `adjacent()` auf `SearchFields.BODY`: eine `MultiPhraseQuery` muss
   Positionslisten fuer jede Wortform an jeder Position durchgehen, und das waechst mit der Indexgroesse. Lokal (121
   Seiten) kostet es 80–160 ms, dort offenbar erheblich mehr. Zwei Hebel, beide nicht gemessen:
   - die Phrase nur auf `TITLE_TEXT` (kurzes Feld, billig) — behaelt den Gewinn fuer den exakten Seitennamen, verliert
     ihn fuer zusammenhaengende Nennungen im Text;
   - oder die Anzahl der Alternativen je Position begrenzen, bevor die Phrase gebaut wird.
2. **Exakter Seitenname rankt weiter nicht oben.** Ohne die Zahlen aus dem Wiki ist das nicht zu klaeren:
   `-Dknowwe.search.explain=true` setzen, suchen, und die fuenf `Scores for "…"`-Bloecke aus dem Log lesen. Sie zeigen je
   Treffer, welches Feld mit welchem Gewicht, welcher Termfrequenz und welcher Feldlaenge beigetragen hat.
3. **Highlighting markierte nur `Cable`** — behoben: `matchCandidates` hatte Woerter mit bis zu zwei Zeichen verworfen,
   damit fielen `Nr` und `24` weg und die Wortfolge war nicht mehr bildbar. Jetzt ab zwei Zeichen.


## Nachtrag: Reindizieren, Stand 2026-08-11

Abgedeckt: Erstaufbau, Bearbeitung (alter Inhalt verschwindet), Commit-Grenze, Loeschen, Commit ohne Aenderung,
Ersetzen auf Indexebene, Gross-/Kleinschreibung des Schluessels, Schema-Wiederverwendung und -Verwerfen, kaputtes
Verzeichnis. Neu dazu:

- **Umbenennen** braucht keinen eigenen Zweig. `KnowWEUtils.renameArticle` macht `deleteArticle` + `registerArticle`
  innerhalb eines `open()`/`commit()`, kommt bei uns also als die zwei Ereignisse an, die wir schon behandeln.
  `renamingAPageMovesItInTheIndex` haelt das fest -- als Eigenschaft von `renameArticle`, nicht von unserem Code.
- **Preview-Cache** wird beim Bearbeiten der Seite vergessen (`anEditForgetsTheRenderedPreviewOfThatPage`).
- **Anhaenge** ueberleben ein Neuindizieren ihrer Seite und verschwinden mit ihr
  (`anAttachmentIsReplacedWithoutTouchingItsPage`, `deletingAPageTakesItsAttachmentsWithIt`).

Weiter offen: die **Verdrahtung** der Anhangs-Ereignisse (`AttachmentStoredEvent`/`AttachmentDeletedEvent`) ist nicht
getestet -- dafuer muesste der `DummyConnector` eine `WikiAttachment` zurueckgeben, was er ohne eigenes Geruest nicht tut.
Geprueft ist damit, was die Ereignisse ausloesen, nicht dass sie ausgeloest werden.

Zwei Beobachtungen aus dem Schreiben dieser Tests, beide keine Fehler, aber gut zu wissen:

- `GermanNormalizationFilter` bildet **"ue" und "ü" auf dasselbe** ab. Ein Testwort mit "ue" ist also nicht
  automatisch eindeutig -- "Ruettelpruefung" kollidierte mit dem "Rüttelprüfung" einer anderen Testklasse.
- Das `Environment` ist ein Singleton pro JVM: der Index eines Tests enthaelt die Seiten aller anderen Tests. Tests
  sollten daher nach **Vorhandensein** fragen, nicht nach Rang -- eine fremde Seite kann eine Anfrage unscharf
  beantworten und davor stehen.
