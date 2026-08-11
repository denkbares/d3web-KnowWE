# Konzept: Wiki-Variablen für InterWikiImport

Status: Entwurf v4 (2026-08-10), noch nicht implementiert.
Änderungen gegenüber v3: Registrierungs- und Check-Script hängen am **ServiceMateCompiler**
(GroupingCompiler) statt am OntologyCompiler. Der `DelegateVariableManager` entfällt komplett;
die Mehrdeutigkeitsprüfung innerhalb einer Variante ist damit bereits zur Compile-Zeit
zuverlässig.

## 1. Ziel

`%%InterWikiImport` verlangt heute eine feste `@wiki`-URL pro Markup. In Wikis mit mehreren
Varianten (GroupingCompiler, z.B. `ServiceMateCompiler`) soll das Quell-Wiki stattdessen über
eine Variable angegeben werden können, deren Belegung per Packaging variantenspezifisch
definiert wird:

```
%%InterWikiImport
@wikiVariable: sourceWiki
@page: Fehlercodes
@package: shared-content
%
```

Belegung an anderer Stelle, variantenspezifisch paketiert:

```
%%InterWikiVariable
@name: sourceWiki
@value: https://wiki-variante-a.example.com
@package: variante-a-config
%
```

Der Import muss prüfen, dass die Variable im Kontext des aktuellen GroupingCompilers
genau eine Belegung hat.

**Nutzungsregel für die Wiki-Architektur:** Ein Import-Markup mit `@wikiVariable` darf nur
dann in einem von mehreren Varianten geteilten Package liegen, wenn alle diese Varianten die
Variable gleich belegen. Sobald sich die Belegungen unterscheiden, müssen die Import-Markups
in varianten-eindeutigen Packages (und wegen des Attachment-Pfads auf getrennten Artikeln)
liegen — der importierte Attachment-Artikel erbt nämlich die Packages des Import-Markups und
würde sonst von allen Varianten kompiliert (Details: verworfene Alternative in Abschnitt 5).
Verstöße werden zur Poll-Zeit als Konflikt am Markup gemeldet.

## 2. Annotation `@wikiVariable` statt Variablen-Syntax

- Neue Annotation `@wikiVariable: <name>` am `%%InterWikiImport` — kein Parsing nötig,
  für Nicht-Devs verständlicher als `${...}`.
- `@wiki` wird dadurch optional (`MARKUP.addAnnotation(WIKI_ANNOTATION, false)`);
  neue Annotation ebenfalls optional.
- **Exklusivitäts-Check:** genau eine der beiden Annotationen muss belegt sein. DefaultMarkup
  kann „genau eins von beiden“ nicht deklarativ, daher ein kleines Prüf-Script im
  Include-Plugin (`DefaultGlobalCompiler`-Script, strukturell, compiler-unabhängig):
    - beide leer → Fehler „Entweder @wiki oder @wikiVariable muss angegeben werden.“
    - beide belegt → Fehler „@wiki und @wikiVariable dürfen nicht gleichzeitig angegeben werden.“

## 3. VariableManager (KnowWE-core)

Neues, generisches Konzept in KnowWE-core (z.B. Package `de.knowwe.core.compile.variables`),
bewusst nicht InterWiki-spezifisch:

- **`VariableManager`** — Interface:
    - `Set<String> getValues(String name)` — alle (distinct) Belegungen der Variable
    - `Map<Section<?>, String> getValueSources(String name)` — Quell-Sections je Wert
      (für Diagnose-Messages, analog `LabelCache.getLabelSources`)
    - `Set<String> getVariableNames()`
    - Registrierungs-API: `add(String name, Section<?> source, String packageName, String value)`,
      `remove(String name, Section<?> source)` (bzw. `remove(name, source, packageName)`)
- **`DefaultVariableManager`** — Implementierung nach dem **LabelCache-Pattern**
  (`de.knowwe.ssc.tsm.LabelCache`):
    - Struktur: `name → (source-Section → (package → value))`; Section-Ebene als
      `Collections.synchronizedMap(new WeakHashMap<>())`, Lesen filtert mit
      `Sections.isLive(...)` — dadurch robustes Registrieren/Deregistrieren bei
      inkrementeller Kompilation, keine Leaks durch tote Sections.
    - Das **Package ist die Zusatzdimension** der Registrierung (analog zur Property/URI beim
      LabelCache): eine Definition mit mehreren Packages registriert je Package; damit sind
      spätere package-bezogene Abfragen und gezieltes Deregistrieren möglich.
- **Accessor** (analog `LabelCache.get(compiler)`):
  ```java
  static VariableManager get(PackageCompiler compiler) {
      return compiler.getCompileSection()
              .computeIfAbsent(compiler, "VariableManager", (c, s) -> new DefaultVariableManager());
  }
  ```
  Der Manager hängt am CompileSection-Store des Compilers → Lebenszyklus folgt automatisch dem
  Compiler. Langfristig können Compiler eigene Implementierungen vorab in ihren Store legen
  (oder eine Factory-Registrierung ergänzt werden) — `get()` liefert dann diese statt des
  Defaults. Ein Delegate-/Union-Manager für GroupingCompiler ist **nicht** nötig, da direkt
  am GroupingCompiler registriert wird (Abschnitt 4); sollte später doch einmal an
  ChildCompilern registriert werden, kann eine Union-Sicht ergänzt werden, ohne die API zu
  ändern.

## 4. Markup `%%InterWikiVariable` + Scripts am ServiceMateCompiler (KnowWE-Plugin-CBX)

Markup und Scripts liegen in **KnowWE-Plugin-CBX** — dort sind alle nötigen Abhängigkeiten
vorhanden (SemanticServiceCore für `ServiceMateCompiler`, Include-Plugin für
`InterWikiImportMarkup`, vgl. `ReferenceWikiAligner`).

**Warum der ServiceMateCompiler das kann:** `ServiceMateMarkup.getPackages()` liefert die
Union aller Packages der referenzierten Child-Compile-Sections (`@xps`/`@concepts`). Der
ServiceMateCompiler kompiliert also selbst alle Sections aller Packages seiner Variante —
insbesondere sowohl die `%%InterWikiVariable`- als auch die `%%InterWikiImport`-Sections.
Registrierung, Manager und Prüfung liegen damit im **selben Compiler**; weder Reverse-Lookup
(Child → Grouping) noch Union über ChildCompiler sind nötig, und der Manager-Lebenszyklus ist
trivial korrekt (wird der ServiceMateCompiler neu erzeugt, kompiliert er seine Packages neu →
Registrierungen laufen automatisch nach).

**Zeitliche Ordnung:** Compiler laufen in Prioritätsgruppen (`CompilerManager.addCompiler`):
Ontology-/D3webCompiler in Gruppe 5, ServiceMateCompiler in Gruppe 6 — wenn der
ServiceMateCompiler läuft, sind die Child-Compiler bereits fertig. Innerhalb des
ServiceMateCompiler-Laufs sind Script-Prioritäten Barrieren.

- `DefaultMarkup("InterWikiVariable")` mit:
    - `@name` (Pflicht, Pattern `[\w.-]+`)
    - `@value` (Pflicht; Quell-Wiki-URL, wird bei Verwendung durch `normalizeWiki` normalisiert)
    - `@package` über `PackageManager.addPackageAnnotation(MARKUP)`
- **Registrierungs-Script** (`CompileScript<ServiceMateCompiler, InterWikiVariableMarkup>`,
  `Priority.DEFAULT`):
    - `compile`: für jedes Package der Section
      `VariableManager.get(compiler).add(name, section, package, value)`
    - `destroy`: `VariableManager.get(compiler).remove(name, section)`
    - Sichtbarkeit einer Belegung = „die ServiceMateCompiler (Varianten), die das Package
      kompilieren“ — kein eigenes Scope-Matching nötig.
    - Weitere Compiler können später durch zusätzliche Scripts angebunden werden (der
      core-`VariableManager` ist compiler-agnostisch).
- **Re-Validierung anstoßen:** `compile` und `destroy` des Registrierungs-Scripts stellen
  zusätzlich alle `InterWikiImport`-Sections des Compilers für das Check-Script (Abschnitt 7)
  in die Compile-Queue: `compiler.addSectionToCompile(importSection, CheckScript.class)`
  (Script-Filter). Sonst würde bei inkrementeller Kompilation eine geänderte Belegung die
  Prüfung unveränderter Import-Sections nicht erneut auslösen.
- Renderer: zeigt Name → Wert und (Debug-Hilfe) die Varianten, in denen die Belegung
  registriert ist.
- Mehrere Variablen = mehrere Markups.

## 5. Auflösung im InterWikiImport (KnowWE-Plugin-Include)

Für eine Import-Section `S` mit `@wikiVariable: name` (Auflösung braucht nur KnowWE-core):

1. **Belegungen sammeln:** über alle `PackageCompiler`, die S kompilieren
   (`Compilers.getCompilers(S, PackageCompiler.class)` — enthält die ServiceMateCompiler der
   Varianten, da deren Packages die Union der Child-Packages sind):
   `VariableManager.get(C).getValues(name)` — Manager ohne Registrierungen (z.B. der
   OntologyCompiler) tragen schlicht nichts bei.
2. **Eindeutigkeit über die distinct-Union aller Werte:**
    - 0 Werte → „Variable `name` ist in keiner Variante definiert, die diese Section
      kompiliert.“
    - genau 1 Wert → `normalizeWiki(wert)` wie bisher.
    - \>1 Werte → Konflikt: die Section wird von mehreren Varianten mit unterschiedlichen
      Quellen kompiliert — ein Import (= ein Attachment) kann nur aus einer Quelle laden;
      das Markup muss per Packaging variantenspezifisch aufgeteilt werden (wegen des
      Attachment-Pfads `<Titel>/WikiImport-<Page>[-<Section>].txt` auf getrennten Artikeln).
      Quellen-Angabe in der Message via `getValueSources`.

**Verworfene Alternative: Attachment je Variante (Compiler-Name als Pfad-Präfix).** Die Idee,
bei unterschiedlichen Belegungen je Variante ein eigenes Attachment
(`WikiImport-<Variante>-<Page>.txt`) zu holen, löst nur die Speicher-Kollision, nicht den
eigentlichen Konflikt: Der importierte Attachment-Artikel erbt seine Packages vom
kompilierenden Import-Markup (`DefaultMarkupPackageScript.getPackageInfo` → compiling
AttachmentCompileType-Section). Beide Varianten-Attachments landen also in denselben
(geteilten) Packages und würden von **beiden** Varianten kompiliert — statt eines Konflikts
gäbe es duplizierten/falschen Inhalt in jeder Variante. Echte Varianten-Imports bräuchten
zusätzlich: per-Variante-Package-Scoping der Attachment-Artikel, `AttachmentCompileType`
1:n statt 1:1 (`getCompiledAttachmentPath` liefert genau einen Pfad; AttachmentManager,
Locks, Tracking sind darauf gebaut), je Variante eigenes `@latestChange` sowie
varianten-abhängiges Rendering. Das ist ein eigenes, größeres Vorhaben — falls der Bedarf
real wird, als separates Konzept angehen. Bis dahin: Fehler + Aufteilung per Packaging
(s.o.); identische Belegungen über Varianten hinweg sind ohnehin kein Konflikt.

Implementierungsdetails:

- Neue Methode `resolveWiki(section)` (`@Nullable`) in `InterWikiImportMarkup`:
  `@wiki` gesetzt → Literal wie bisher; sonst Auflösung über `@wikiVariable`. Alle bisherigen
  Aufrufer von `getWiki` (u.a. `getUrl`, Renderer-Header, Snapshot-Erstellung im
  UpdateService) auf `resolveWiki` umstellen.
- Auflösung darf nicht blockieren (läuft im Renderer und im Poller-Thread) und wird pro
  Kompilationsstand gecacht (`CompilationLocal.getCached(...)`).
- Ergebnis als kleines Record (Wert **oder** Fehlerliste inkl. betroffener Compiler), damit
  UpdateService, Renderer und Check-Script dieselben Meldungen verwenden.
- Wikis ohne ServiceMate/CBX: `@wikiVariable` bleibt dort unauflösbar (Warnung zur Poll-Zeit);
  das Feature ist vorerst bewusst CBX-gebunden, `@wiki` (literal) funktioniert überall.

## 6. Integration in die bestehende Mechanik

- `RegistrationScript`: registriert derzeit nur bei `getUrl != null`. Zum Zeitpunkt der
  globalen Kompilation sind die ServiceMateCompiler evtl. noch nicht fertig — die
  Registrierung darf nicht an der Auflösung scheitern. Neu: registrieren, sobald `@page` und
  (`@wiki` oder `@wikiVariable`) vorhanden sind; unauflösbare Sections werden beim Polling
  übersprungen.
- `InterWikiImportUpdateService`: `getImportSnapshots()` / `pollSingleMarkup` lösen **lazy zum
  Poll-Zeitpunkt** auf (die Gruppierung nach Quell-Wiki funktioniert danach unverändert über
  den aufgelösten Wert). Unauflösbar oder mehrdeutig → Markup überspringen (nicht zufällig
  eine Quelle wählen) und per `recordSyncOutcome` eine aussagekräftige Warnung setzen (statt
  stillem „No check yet“). Da der Poll erst nach abgeschlossener Kompilation läuft, sieht er
  einen konsistenten Stand — hier werden auch variantenübergreifende Konflikte (Punkt 5.2)
  sichtbar, unabhängig vom Check-Script.
- Renderer: Header zeigt bei Variablennutzung `name → aufgelöster-wert` (Link auf das
  aufgelöste Wiki); bei Auflösungsfehlern die Validierungs-Message wie gewohnt gerahmt.
- Actions (Refresh-, Tracking-Aktionen) laufen alle über `getUrl`/`resolveWiki` — keine
  weiteren Änderungen nötig.

## 7. Validierung: Check-Script am ServiceMateCompiler in niedriger Priorität (CBX)

- `InterWikiImportWikiVariableCheckScript` (CBX), ein
  `CompileScript<ServiceMateCompiler, InterWikiImportMarkup>` mit **`Priority.LOWEST`** —
  innerhalb des ServiceMateCompiler-Laufs sind Prioritäten Barrieren, das Script läuft also
  sicher nach allen Variablen-Registrierungen desselben Compilers (gleiches Muster wie die
  CBX-Check-Scripts, z.B. `CheckNotUsedInTransitionScript`).
- **Anbindung ohne Code-Änderung im Include-Plugin** über den `CompileScript`-Extension-Point
  (`Plugins.EXTENDED_POINT_CompileScript`): plugin.xml-Eintrag in CBX mit Scope
  `InterWikiImport` und `compilepriority` = 300 (LOWEST). Alternativ programmatisch, da CBX
  ohnehin von Include abhängt — Extension-Point ist der sauberere Weg.
- **Prüfung:** für den aktuellen ServiceMateCompiler `C` einfach
  `VariableManager.get(C).getValues(name)`:
    - genau 1 Wert → ok, Messages löschen
    - 0 / >1 Werte → Fehler-Message mit Quellen (`getValueSources`), gespeichert **je
      Compiler** (`Messages.storeMessage(compiler, section, source, msg)`), damit im UI
      sichtbar ist, welche Variante betroffen ist; eigene Message-Source-Klasse (analog
      `SyncMessages`), damit unabhängig löschbar.
    - `destroy`: Messages des Compilers löschen.
  Da alle Definitionen einer Variante im selben Manager landen, erkennt der Check
  **Mehrdeutigkeit innerhalb einer Variante zuverlässig zur Compile-Zeit** (in v3 war das
  wegen parallel laufender Geschwister-Compiler nicht garantiert).
- **Re-Trigger bei Belegungsänderung:** übernimmt das Registrierungs-Script des
  Variable-Markups via `addSectionToCompile(importSection, CheckScript.class)`
  (siehe Abschnitt 4).
- Optional zusätzlich Warnungen auf den Definitions-Markups bei Konflikten (gleiche Variable,
  gleicher Compiler, unterschiedliche Werte — via `getValueSources` leicht zu ermitteln).

Grenze (bewusst akzeptiert): **Variantenübergreifende** Konflikte — dieselbe Import-Section
wird von mehreren ServiceMateCompilern mit unterschiedlichen, je eindeutigen Werten
kompiliert — sieht das per-Compiler-Check-Script nicht (die Varianten laufen in Gruppe 6
parallel zueinander). Diese fängt die Auflösung zur Poll-Zeit ab (Abschnitt 5.2/6). Gleiche
Werte in mehreren Varianten sind kein Konflikt (distinct-Union = 1).

## 8. Stolperstein: Belegungswechsel und `@latestChange`

Wechselt die Belegung (der Import zeigt plötzlich auf ein anderes Wiki), enthält das
Attachment noch Inhalt und `@latestChange` des **alten** Wikis. Der reguläre Poll fragt das
neue Wiki mit dem alten `latestChange` — hat sich die Seite dort seither nicht geändert,
liefert es kein Update, und der veraltete/falsche Inhalt bliebe stehen.

Lösung im UpdateService selbst (kein zusätzlicher Trigger nötig): Der Service merkt sich das
zuletzt gepollte Wiki pro Attachment-Pfad (In-Memory-Map, analog `LAST_SYNC_ERRORS`). Weicht
das aktuell aufgelöste Wiki davon ab, wird dieser Poll mit `latestChange = null` (force)
ausgeführt; der Byte-Vergleich in `updateAttachmentWithSourceText` verhindert unnötige
Attachment-Versionen. Beim regulären 10-Minuten-Intervall greift das spätestens beim
nächsten Poll nach der Änderung.

Restart-Lücke: Der In-Memory-Merker geht beim Neustart verloren; ein Belegungswechsel „über
einen Neustart hinweg“ würde dann nicht erkannt. Empfehlung: das aufgelöste Wiki als
automatisch gepflegte Annotation `@sourceWiki` ins Markup zurückschreiben (gleicher
Replacement-Mechanismus wie bei `@latestChange`) — beim Poll wird gegen diese Annotation
verglichen; das schließt die Lücke und dokumentiert zugleich sichtbar, woher der Inhalt
tatsächlich stammt. Einfachere Alternative: Lücke akzeptieren und dokumentieren, dass nach
einem Belegungswechsel ggf. einmal der Refresh-Button nötig ist.

## 9. Implementierungsschritte

1. KnowWE-core: `VariableManager` (Interface) + `DefaultVariableManager` + Accessor
   `VariableManager.get(PackageCompiler)`
2. Include-Plugin: `@wikiVariable`-Annotation, `@wiki` optional, Exklusivitäts-Check-Script
3. Include-Plugin: `resolveWiki` (Auflösung + Eindeutigkeitsprüfung, Caching) + Umstellung
   der Aufrufer; `RegistrationScript` anpassen
4. Include-Plugin: `InterWikiImportUpdateService` — lazy Auflösung, Skip + Warnung bei
   unauflösbaren/mehrdeutigen Sections, Force-Poll bei Wiki-Wechsel (inkl. Entscheidung zu
   `@sourceWiki`)
5. CBX: `InterWikiVariableMarkup` (Markup + Renderer + plugin.xml) + Registrierungs-Script
   (ServiceMateCompiler) inkl. Re-Trigger der Import-Checks
6. CBX: `InterWikiImportWikiVariableCheckScript` (`Priority.LOWEST`) + plugin.xml-Anbindung
   über den `CompileScript`-Extension-Point
7. Renderer-Anpassungen (Header, Fehleranzeige)
8. Doku (Hilfeseiten) + Tests

## 10. Tests

- Unit-Tests für `DefaultVariableManager` (Registrierung, Deregistrierung, tote Sections,
  Eindeutigkeit, Package-Dimension).
- Integrationstest im CBX/SSP-Kontext (ServiceMate-Varianten mit unterschiedlich paketierten
  Definitionen): Auflösung, Check-Script-Messages (0/1/n Belegungen), Re-Trigger bei
  Definitionsänderung ohne Änderung der Import-Section, variantenübergreifender Konflikt →
  Poll-Warnung.
- Manueller Test in der lokalen Instanz: Variante wechseln, Belegung ändern → Force-Poll und
  Messages prüfen; `@wiki`/`@wikiVariable`-Exklusivität.

## 11. Offene Punkte

1. **`@sourceWiki`-Auto-Annotation:** ja (Empfehlung, schließt die Restart-Lücke) oder
   Refresh-Button als dokumentierter Workaround?
2. **Konflikt-Warnungen auch auf den Definitions-Markups** (Abschnitt 7) — gewünscht?
3. Später: Registrierung für weitere Compiler (z.B. reine Ontology-Wikis ohne ServiceMate),
   Nutzung der Variablen durch andere Markups.
