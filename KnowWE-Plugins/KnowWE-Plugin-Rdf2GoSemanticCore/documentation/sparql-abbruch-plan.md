# Plan: Laufende SPARQL-Abfragen abbrechen können

Stand 2026-09-29, Plan abgestimmt, Umsetzung noch nicht begonnen. Anlass ist der Vorfall bei UBA am 28.09.: Eine teure Abfrage wurde auf einer Seite mehrfach
geändert. Jede Fassung lief bis zu 10 Minuten weiter, alle 7 SPARQL-Threads waren belegt, und das Wiki war für
alle blockiert. Abhilfe war nur ein Neustart.

Ziel: Das Wiki kann eine laufende Abfrage gezielt abbrechen, und es tut das von selbst, wenn niemand ihr Ergebnis
mehr braucht. Ein Neustart ist dafür nicht mehr nötig.

---

## 1. Ausgangslage

**Code** (Master, Stand `b09ca1e7e`; Klassen unter `KnowWE-Plugin-Rdf2GoSemanticCore/.../de/knowwe/rdf2go/`):
- `SparqlCache.invalidate()` läuft bei jedem Commit mit geänderten Statements. Fertige Tasks wandern nach
  `outdated`. **Laufende Tasks werden aus dem Cache geworfen, aber nicht abgebrochen.** Der nächste Aufruf startet
  dieselbe Abfrage ein zweites Mal, und die Kopien laufen bis zu ihrem Zeitlimit parallel.
- `SparqlTask.cancel(true)` unterbricht nur den Java-Thread (`Thread.interrupt()`). `stop()` nutzt `Thread.stop()`,
  das ab JDK 20 mit `UnsupportedOperationException` scheitert. Beides wird nirgends aufgerufen. Der Reaper in
  `SparqlTask.run()` ist auskommentiert („causes severe issues with GraphDB“).
- Wirksam ist nur das Zeitlimit der Datenbank: `setMaxExecutionTime(@timeout)`, Standard 60 s. Der Render-Thread
  wartet doppelt so lange (`Rdf2GoCore.sparql`, `get(2 × timeout)`).
- Ein Task, der abgebrochen wurde, bleibt im Cache stecken: `Rdf2GoCore.sparql` erzeugt einen neuen Task nur bei
  `isCancelled() && timeout != options.timeoutMillis`.
- Ein Ergebnis mit Fehler oder Timeout gilt als „available“. Es bleibt bis zum nächsten Commit oder „Try again“
  (`ClearCachedSparqlAction`) im Cache und wird nach dem nächsten Commit automatisch neu gestartet.
- `SparqlCallable` hält während der ganzen Abfrage den Read-Lock `core.getUsageLock()`. Hängt eine Abfrage, wartet
  deshalb auch das Schließen des Cores.
- **SemanticCore** (`denkbares-Commons/denkbares-SemanticCore/.../semanticcore/`):
  `TupleQuery.evaluate(Map bindings)` ruft intern `evaluate().cachedAndClosed()` auf. Bei vorbereiteten Abfragen
  mit Bindings kommt man deshalb nie an das laufende Ergebnis. `TupleQueryResult.cachedAndClosed()` fragt in der
  Schleife `Thread.isInterrupted()` ab.

**Experiment** (2026-09-29, echter `Rdf2GoCore` mit eingebettetem GraphDB 10.8.12, rdf4j 4.3.15). Getestet
wurden ein Kreuzprodukt mit `COUNT` und eine Streaming-Abfrage mit `FILTER`, abgebrochen jeweils nach 3 s:

| Variante | Ergebnis |
|---|---|
| Zeitlimit 5 s (Referenz) | stoppt nach 5 s, auch mitten in der Aggregation |
| `TupleQueryResult.close()` aus anderem Thread | stoppt sofort, danach läuft in GraphDB nichts mehr; `hasNext()` liefert aber einfach `false`, das Ergebnis sieht leer aus |
| `RepositoryConnection.close()` aus anderem Thread | stoppt sofort, gleiches Verhalten |
| `Thread.interrupt()` | Aggregation läuft bis zum Zeitlimit weiter; beim Streaming `EntityPoolConnectionException … IOException` im GraphDB-Speicher. **Nicht verwenden** |
| GraphDB-Monitor `requestStop(trackId)` | stoppt sofort und sauber: `QueryInterruptedException: Query was aborted by the user.` |

`close()` aus einem fremden Thread ist genau der Weg, auf dem rdf4j selbst sein Zeitlimit umsetzt:
`TimeLimitIteration.interrupt()` setzt ein Flag und ruft vom Timer-Thread aus `close()` auf. Dieser Weg läuft in
Produktion also schon heute.

Den GraphDB-Monitor gibt es auch im eingebetteten Betrieb, als
`com.ontotext:type=RepositoryMonitor,name="<Repository-ID>"`. `TrackRecords` liefert pro laufender Abfrage
`trackId`, `trackAlias`, `sparqlString`, `state`, `msSinceCreated` und `type`, auch für Abfragen beim Kompilieren.

---

## 2. Lösungsansatz

**Abbrechen per `close()`**, ohne Interrupt:
- Die Ausführung merkt sich ihr laufendes Ergebnis (bei ASK die Verbindung).
- `cancel()` setzt ein Flag und schließt das Ergebnis bzw. die Verbindung.
- Endet das Lesen danach, obwohl das Flag gesetzt ist, wirft es eine `QueryInterruptedException` („cancelled“).
  Ein leeres Ergebnis wird also nie als gültig zurückgegeben oder zwischengespeichert.
- Der Weg funktioniert mit jedem rdf4j-Repository, nicht nur mit GraphDB.

**Unabhängig von GraphDB.** Abbruch, Button und Übersicht funktionieren mit jedem Repository, das SemanticCore
anbietet. Sie brauchen nur `close()` und eine eigene Registry der laufenden Abfragen. Der GraphDB-Monitor wird
nicht verwendet.

---

## 3. Entscheidungen (mit Albrecht Striffler abgestimmt, 2026-09-29)

- **Beim Commit, beim Kompilieren und beim Schließen werden alle laufenden Abfragen beendet.** Das gilt für jeden
  Commit mit geänderten Statements, jeden Start einer Kompilierung des zugehörigen `OntologyCompiler` und jedes
  Herunterfahren bzw. Schließen von Compiler oder `Rdf2GoCore`. Ein laufender Task läuft nach einem Commit also
  nicht als „veraltet“ weiter.
- **Abbrechen-Button im SPARQL-Markup** (neu, Phase 4).
- **Abbrechen dürfen alle**, die die Tabelle sehen (Phase 4). Mehrere Betrachter mit unterschiedlichen Rechten, die
  auf dieselbe Abfrage warten, sind zu selten, um das über Rechte zu regeln.
- **Übersicht der laufenden Abfragen im Ontology-Markup** (Phase 5), nicht als eigenes Admin-Werkzeug.
- **Einmaliger automatischer Neustart** für Aufrufer, die auf eine wegen Commit oder Kompilierung abgebrochene
  Abfrage warten (Phase 2).
- **Unabhängig von GraphDB**, siehe oben. Kein GraphDB-Monitor, die Übersicht zeigt die eigene Registry.
- **Entwicklung auf `master`** (Abschnitt 5).

---

## 4. Umsetzung in Phasen

### Phase 1: Abbrechbare Ausführung (Grundlage, ohne Verhaltensänderung)

**SemanticCore**
- `TupleQueryResult`: `cancel()` = Flag setzen + `delegate.close()`. `hasNext()` bzw. `cachedAndClosed()` werfen
  bei gesetztem Flag eine `QueryInterruptedException("SPARQL query was cancelled")`, statt normal zu enden.
  Die Abfrage von `Thread.isInterrupted()` in `cachedAndClosed()` entfällt.
- `TupleQuery.evaluate(Map bindings)`: eine Variante, die das laufende Ergebnis vor `cachedAndClosed()` herausgibt,
  z. B. `evaluate(Map bindings, Consumer<TupleQueryResult> onStarted)`. Dann lassen sich auch vorbereitete Abfragen
  abbrechen. Die bestehende Signatur bleibt erhalten.
- Tests im SemanticCore-Modul, gegen GraphDB **und** gegen ein Repository ohne GraphDB (rdf4j Memory-Store):
  Kreuzprodukt wie im Experiment mit kleinen Daten, Abbruch nach 1 s, Stopp unter 1 s, Exception statt leerem
  Ergebnis, Repository danach nutzbar.

**Rdf2Go**
- `SparqlCallable`: das laufende `TupleQueryResult` bzw. bei ASK die `RepositoryConnection` in einem
  `volatile`-Feld halten. `cancel(reason)` ruft `close()` darauf auf. Ein Abbruch vor dem Start setzt nur das
  Flag, dann startet die Abfrage gar nicht erst.
- `SparqlTask.cancel(...)`: ruft immer `callable.cancel(reason)` und dann `super.cancel(false)` auf, **nie mit
  Interrupt**.
- **Aufräumen der `Thread.stop()`-Reste:** Die GraphDB-Probleme, wegen denen der Reaper abgeschaltet wurde, kamen
  von `Thread.stop()`. Deshalb entfernen: `SparqlTask.stop()` samt `LockSupport.unpark`, den auskommentierten
  Aufruf `sparqlReaperPool.execute(new SparqlTaskReaper(this))` in `SparqlTask.run()` und das Feld `thread`, falls
  es danach nicht mehr gebraucht wird. Die Klasse `SparqlTaskReaper` und der Pool existieren nicht mehr.
- **Registry laufender Abfragen** in `Rdf2GoCore`: jeder Task, der gestartet wird, gecacht oder nicht, meldet sich
  an und nach dem Ende wieder ab. Die Registry ist die Grundlage für Phase 2, 4 und 5 und kommt ohne GraphDB aus.
  Pro Eintrag: Abfragetext, Priorität, Zeitlimit, Start- und Laufzeit, Zustand (wartend/laufend), anfordernde
  Artikel bzw. Sections (soweit beim Rendern bekannt), Grund eines Abbruchs.
- `Rdf2GoCore.sparql`: einen abgebrochenen Task im Cache immer durch einen neuen ersetzen. Das heutige
  `isCancelled() && timeout != …` wird zu `isCancelled()`.
- Log: `SPARQL query was cancelled after … (<Grund>): <query>`.
- Abfragen beim Kompilieren selbst (`runInThread`, laufen im Compile-Thread) nehmen an der Registry teil, damit die
  Übersicht sie zeigt. Automatisch abgebrochen werden sie aber nicht, weil sie zur laufenden Kompilierung gehören.
- Tests im Rdf2Go-Modul: Abbruch eines laufenden Tasks für SELECT, SELECT mit Bindings und ASK; der Task endet
  als „cancelled“ und der nächste Aufruf startet neu. Die Daten erzeugen die Tests selbst, keine großen Dateien.

### Phase 2: Automatisch abbrechen bei Commit, Kompilierung und Schließen

Hätte es das am 28.09. gegeben, wäre jede Fassung der Abfrage spätestens beim Speichern der nächsten abgebrochen
worden, statt 10 Minuten weiterzulaufen.

- `Rdf2GoCore.commit()`: vor `sparqlCache.invalidate()` alle laufenden und wartenden Tasks aus der Registry
  abbrechen (Grund: „data changed“). `invalidate()` hat damit keine laufenden Tasks mehr, die es verwaisen lassen
  könnte.
- `OntologyCompiler`: beim Start einer Kompilierung alle laufenden Abfragen des zugehörigen Cores abbrechen (Grund:
  „compilation started“). Das deckt auch Änderungen ab, die keine Statements ändern, etwa wenn nur die Abfrage
  einer Tabelle bearbeitet wird. Dann gibt es keinen Commit, die alte Fassung würde sonst weiterlaufen.
- `Rdf2GoCore.close()` sowie das Herunterfahren bzw. Zerstören des Compilers: zuerst alle Tasks abbrechen, dann
  den Write-Lock holen (Grund: „core closed“). So wartet das Herunterfahren nicht mehr auf hängende Abfragen, was
  auch beim Tomcat-Neustart hilft.
- „Try again“ (`ClearCachedSparqlAction`): bricht eine noch laufende Ausführung derselben Abfrage ab, bevor neu
  gestartet wird.
- **Einmaliger automatischer Neustart für wartende Aufrufer.** Wird eine Abfrage wegen eines Commits oder einer
  Kompilierung abgebrochen, wartet oft jemand auf ihr Ergebnis:
  - ein Render-Thread in `sparqlTask.get(...)`, weil jemand die Tabelle gerade öffnet oder die Ladeanzeige sieht,
  - ein interner Aufrufer ohne Cache, z. B. `KnowSECUtils.getTitle` für die Brotkrumen-Navigation, oder eine Aktion.

  Ohne weitere Regel bekäme dieser Aufrufer eine Exception. Die Tabelle zeigte „abgebrochen“ bzw. die Navigation
  einen Fehler, obwohl der Betrachter nichts getan hat, nur weil gleichzeitig jemand anders eine Seite gespeichert
  hat. Heute passiert das nicht: Die alte Abfrage läuft weiter und liefert am Ende ein veraltetes Ergebnis.

  Regel: Endet eine Abfrage mit dem Grund „data changed“ oder „compilation started“, startet `Rdf2GoCore.sparql`
  dieselbe Abfrage sofort **einmal** neu, jetzt auf dem neuen Datenstand, und wartet weiter. Der Betrachter merkt
  davon nur, dass es etwas länger dauert, und bekommt ein aktuelles Ergebnis. Der neue Lauf ist ein normaler
  Cache-Eintrag, andere Betrachter derselben Tabelle hängen sich daran.

  Grenzen:
  - Nur **ein** Neustart pro Aufruf. Wird auch der zweite Lauf abgebrochen, etwa bei einem Import mit vielen
    Commits, bekommt der Aufrufer die Meldung „Abfrage wegen laufender Datenänderungen abgebrochen. [Erneut
    ausführen]“. So entsteht keine Endlosschleife.
  - Kein Neustart bei „core closed“ (das Wiki fährt herunter) und bei einem Abbruch von Hand (Button, Übersicht,
    „Try again“ startet selbst neu).
  - Das Zeitlimit gilt für den neuen Lauf wieder voll. Die Wartezeit des Aufrufers kann sich so höchstens
    verdoppeln.
- **Risiko Aushungern:** Wird öfter kompiliert oder committet, als eine Abfrage dauert, kommt sie nie zu Ende.
  Heute läuft sie dann doppelt, künftig nie fertig. Das ist die bewusst gewählte, ressourcenschonende Seite. Der
  Zustand ist in der Übersicht sichtbar („mehrfach abgebrochen“), und Phase 3 begrenzt das automatische Neustarten.
- Tests: je Auslöser (Commit, Kompilierstart, Close, „Try again“) eine langsame Abfrage, die innerhalb 1 s stoppt
  und mit Grund im Log steht; danach ein Neustart mit dem neuen Datenstand bzw. kein Neustart bei „core closed“.

### Phase 3: Abfragen nach einem Timeout nicht automatisch neu starten

- Ist eine Abfrage beim letzten Lauf ins Zeitlimit gelaufen, bleibt das Ergebnis „timed out“ auch über Commits
  hinweg gültig. Dafür wird es beim Invalidieren als eigener Zustand übernommen.
- Die Tabelle zeigt dann: „Die Abfrage hat beim letzten Lauf das Zeitlimit von X s überschritten.
  [Erneut ausführen]“. Das nutzt das vorhandene „Try again“.
- Nach einer Änderung der Abfrage (anderer Text) gilt das natürlich nicht mehr.
- Wirkung: Eine kaputte Abfrage auf einer viel besuchten Seite läuft einmal ins Limit und belastet den Server danach
  nicht mehr.

### Phase 4: Abbrechen-Button im SPARQL-Markup

- Solange eine Tabelle auf ihr Ergebnis wartet, also beim Anzeigen mit Ladeanzeige oder bei „Database has changed.
  Showing previous result while rerunning query…“, erscheint daneben **„Abbrechen“**.
- Neue Aktion, z. B. `CancelSparqlAction`: ermittelt aus der Section die Abfrage und die Optionen, bricht den
  zugehörigen Task ab (Grund: „cancelled by <Benutzer>“) und lädt die Tabelle neu. Die Tabelle zeigt dann „Abfrage
  abgebrochen. [Erneut ausführen]“ und bleibt so, bis jemand neu ausführt oder sich die Daten ändern.
- Abbrechen darf jeder, der die Tabelle sieht. Weil der Cache geteilt ist, trifft ein Abbruch zwar alle, die
  gerade dieselbe Abfrage ansehen, das kommt aber zu selten vor, um es über Rechte zu regeln. Jeder Abbruch wird
  mit Benutzer und Seite geloggt.
- Technisch hängt der Button an der Vorschau, die das asynchrone Rendern ohnehin ausgibt (`AsynchronousRenderer`,
  `SparqlResultRenderer`, Nachladen per `ReRenderContentPartAction`). Beim Umsetzen prüfen, wo er in dieser
  Ausgabe hinpasst.

### Phase 5: Übersicht laufender Abfragen im Ontology-Markup

- Im `%%Ontology`-Markup (`OntologyMarkup`) eine Ansicht „Laufende SPARQL-Abfragen“, als Werkzeug im Menü des
  Markups oder als aufklappbarer Bereich.
- **Quelle ist die Registry aus Phase 1.** Die gibt es immer, weil jede Abfrage, die über `Rdf2GoCore` läuft, sich
  dort an- und abmeldet, unabhängig von der Datenbank. Das umfasst Tabellen, interne Abfragen wie `getTitle`,
  Aktionen und Abfragen beim Kompilieren.
- Pro Abfrage: Anfang des Abfragetexts (ausklappbar), Seite(n), Zustand wartend/laufend, Laufzeit, Zeitlimit,
  Priorität, Anzahl bisheriger Abbrüche und „Abbrechen“ (wie Phase 4, für alle).
- Aktualisierung per Neuladen der Ansicht (Button bzw. alle paar Sekunden, solange sie offen ist), kein Dauer-Polling.
- Tests: die Ansicht mit einer laufenden Abfrage, unter GraphDB und unter dem Memory-Store.

### Nicht in diesem Plan (eigene Tickets)

- Reservierte Kapazität für kurze, interne Abfragen (`getTitle`, Import-Analyse), damit sie nicht hinter langen
  Tabellen-Abfragen warten.
- Obergrenze für `@timeout` und Laufzeitanzeige bzw. Warnhinweise für Autoren.
- Entschärfen der konkreten UBA-Abfrage auf „Dossierbewertung Gesamt“.
- **GraphDB-Monitor** (`com.ontotext:type=RepositoryMonitor,…`, `TrackRecords`, `requestStop`): derzeit nicht
  vorgesehen, die Übersicht ist bewusst unabhängig von GraphDB. Er sähe zusätzlich nur die Abfragen über den
  SPARQL-Endpoint (`KnowWE-Plugin-SparqlEndpoint`, `RepositoryInterceptor` geht am `Rdf2GoCore` vorbei; laut Suche
  der einzige solche Weg in KnowWE und KnowWE-DES). Falls das später gebraucht wird: nur über generisches JMX, ohne
  GraphDB-Klassen, und mit dem Hinweis „Datenbank-Monitoring ist mit dem aktuellen Reasoning bzw. der aktuellen
  Datenbank nicht verfügbar“, wenn das MBean fehlt.

---

## 5. Branch und Auslieferung

- **Entwicklung auf `master`**, in KnowWE und denkbares-Commons. Gründe:
  - Die Änderung betrifft KnowWE-Kern und SemanticCore, also alle Anwendungen, nicht nur KnowSEC.
  - Master hat `SparqlCache` bereits überarbeitet (`6e8925e61` „make ScopeExtensions and SparqlCache
    thread-safe“, K. Herud, 03.08.). Auf `knowsec/release-2026-07` fehlt das, dort wäre auf einem veralteten Stand
    zu arbeiten, und es gäbe Konflikte.
- **Auslieferung an UBA:** über den nächsten Merge von Master in den KnowSEC-Branch (`knowsec/merge-2026-07`
  enthält den Thread-Safety-Commit bereits). Falls es früher gebraucht wird, Phase 1 und 2 per Cherry-Pick
  nachziehen, dann aber zusammen mit `6e8925e61`.
- Reihenfolge: Phase 1 → 2 → 3, jeweils als eigener Commit mit Tests. Phase 4 und 5 bauen auf Phase 1 auf und
  können danach parallel entstehen.
- Vor dem Start mit K. Herud abstimmen (letzte Änderung an `SparqlCache`).

---

## 6. Risiken und offene Punkte

- **`close()` aus einem fremden Thread, während der Lese-Thread in `hasNext()` steckt.** rdf4j macht das bei jedem
  Timeout genauso, im Experiment gab es keine Fehler. Trotzdem die Tests mehrfach und parallel laufen lassen, etwa
  20 Abbrüche gleichzeitig, danach prüfen, dass das Repository heil ist.
- **Andere Repository-Typen** (nicht GraphDB, z. B. Memory-Store in Tests): gleiches Verhalten erwartet, weil es
  der Standard-Weg in rdf4j ist. Im Test mit abdecken.
- **CONSTRUCT** liefert ein `GraphQueryResult` ungecacht an den Aufrufer zurück, der Abbruch betrifft dort nur die
  Auswertung. Prüfen, ob CONSTRUCT über den Cache überhaupt vorkommt.
- **Abfragen beim Kompilieren** (`runInThread`, `future.get()` ohne Timeout) sind nicht betroffen und bleiben, wie
  sie sind.
- **Kompilierstart als Auslöser** bricht auch Abfragen ab, deren Ergebnis durch die Kompilierung gar nicht
  betroffen wäre. Das ist gewollt, einfach und ressourcenschonend. Beobachten, ob Tabellen auf Seiten mit sehr
  häufigen Änderungen dadurch gar nicht mehr fertig werden (siehe Aushungern in Phase 2).
- **Automatischer Neustart nach Abbruch** (Phase 2): höchstens einmal pro Aufruf, damit bei Dauer-Commits keine
  Schleife entsteht.
