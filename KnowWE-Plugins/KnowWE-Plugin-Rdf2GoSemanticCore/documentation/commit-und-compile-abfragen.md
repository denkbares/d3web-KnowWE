# Commit im Rdf2GoCore und Abfragen während des Compiles

Stand: 23.11.2026. Betrifft `Rdf2GoCore` (dieses Modul), `OntologyCompiler` (KnowWE-Plugin-Ontology-MarkupSet) und
`ParallelScriptCompiler` (KnowWE-core).

## Worum es geht

Der Commit eines großen Wikis in das Repository dauert lange (Turm-Wiki: rund 24 s je Variante, überwiegend
OWL-Horst-Inferenz). Früher hielt `commit()` währenddessen die Statement-Sperre. Jeder Thread, der Statements
hinzufügen wollte, stand so lange, und mit ihm der d3web-Compiler, obwohl der aus dem Repository nichts liest.

## Was gilt

1. **Statements hinzufügen und entfernen wartet nie auf einen Commit.** `statementMutex` schützt nur noch die
   Buchführung (Statement-Cache, Einfüge- und Löschliste). Was während eines laufenden Commits dazukommt, gehört zum
   nächsten Commit.
2. **Es läuft immer nur ein Commit** (`commitLock`). `commit()` kehrt erst zurück, wenn alles, was vor dem Aufruf
   hinzugefügt oder entfernt wurde, im Repository steht, auch wenn ein anderer Commit gerade lief.
3. **SPARQL-Abfragen aus Compile-Threads warten, solange ein Commit ins Repository schreibt.** Das passiert zentral
   in `Rdf2GoCore.sparql(...)` über `awaitCommit()`. Ein Compile-Skript muss dafür nichts tun und kann es nicht
   vergessen.
4. **Meldet sich der `OntologyCompiler` fertig (Priorität DONE), steht alles im Repository.** Dafür setzt er am
   `ParallelScriptCompiler` eine Aufgabe (`setBeforeDone`), die nach dem letzten Skript und vor der Fertig-Meldung
   committet. Compiler, die auf eine frühere Priorität der Ontologie warten (z. B. der `D3webCompiler` über die
   `OntologyBridge`), laufen währenddessen weiter und können Statements hinzufügen; fragt eines ihrer Skripte das
   Repository ab, wartet es wegen Punkt 3 von selbst. Einschränkung: Wer auf eine Priorität wartet, wird erst
   freigegeben, wenn die Ontologie eine spätere Priorität beginnt oder sich fertig meldet
   (`CompilerManager.shouldWait`). Hat die Ontologie danach keine Skripte mehr, ist das erst nach dem Commit.

## Was sich nicht geändert hat

- Abfragen aus anderen Threads (Renderer, Aktionen) warten nicht auf einen Commit. Sie sehen bis zu dessen Ende den
  Stand davor, wie bisher.
- Statements, die ein Skript selbst hinzugefügt, aber noch nicht committet hat, sind für Abfragen nicht sichtbar. Wer
  sie mitten im Compile abfragen will, holt den Core weiterhin mit `OntologyCompiler.getRdf2GoCore(true)`. Das
  committet einmal je Priorität und ist entsprechend teuer.
- Bei `@commit: onDemand` wird am Ende des Compiles nicht committet.

## Regeln für neuen Code

- In einem Compile-Skript nicht selbst auf Commits warten oder Sperren des Cores nehmen. Abfragen über die
  `sparql…`-Methoden des `Rdf2GoCore` stellen, dann greift Punkt 3.
- Kein Ereignis-Listener sollte während `InsertStatementsEvent`, `RemoveStatementsEvent` oder
  `ChangedStatementsEvent` lange arbeiten: Diese Ereignisse werden gefeuert, solange `commitLock` gehalten wird, ein
  weiterer Commit wartet so lange. Abfragen und das Hinzufügen von Statements warten in dieser Zeit nicht mehr:
  Wartende Abfragen werden freigegeben, sobald das Repository geschrieben ist, noch bevor die Ereignisse gefeuert
  werden.
- Schlägt das Schreiben ins Repository fehl, bleiben die Änderungen für den nächsten Commit erhalten.

## Tests

`Rdf2GoCoreCommitTest` prüft die Punkte 1 bis 3: Hinzufügen während eines angehaltenen Commits, Warten des zweiten
Commits, die Sicht eines Compile-Threads während ein Commit schreibt, und dass Abfragen nicht auf die Listener eines
Commits warten.

## Offen

`TSMCopyMarkup` (KnowWE-SSP) holt den Core mit `getRdf2GoCore(true)` und löst damit den großen Commit schon in der
letzten Skript-Priorität des `OntologyCompiler` aus, nur um einzelne Property-Werte zu lesen. Das Hinzufügen von
Statements hält das nicht mehr auf. Es bleibt: Der `OntologyCompiler` meldet sich erst nach diesem Commit fertig,
Abfragen anderer Compile-Threads warten so lange, und für alles, was danach noch hinzukommt, läuft am Ende ein zweiter
Commit. Was das an einem großen Wiki kostet, ist noch nicht gemessen (Log: „Requesting Rdf2GoCore while compiling
priority …“ und die Zeile „Removed … statements from and added …“ des Schluss-Commits).
