# Plan: Page Annotate ("Blame") fuer JSPWiki

## Idee

Aehnlich wie `git blame` / "Annotate with Git Blame" in IntelliJ: fuer jede Zeile der aktuellen
Page-Version anzeigen, in welcher Version sie eingefuehrt wurde, von welchem Autor und wann.
Datenquelle ist ausschliesslich die Versions-Historie, die JSPWiki ohnehin pflegt — keine
Git-Anbindung.

## Zielbild

Im Info-Tab einer Wiki-Seite gibt es einen neuen Tab "Annotate". Er zeigt den aktuellen Seiten-
text Zeile fuer Zeile mit drei Meta-Spalten:

```
+----+----------+------------+--------------------------------------+
| v3 | albrecht | 2026-04-22 | Erste Zeile, eingefuehrt in v3       |
| v3 | albrecht | 2026-04-22 | Zweite Zeile, auch v3                |
| v7 | bernd    | 2026-05-01 | In v7 von Bernd geaendert            |
| v7 | bernd    | 2026-05-01 | Folgezeile, gleicher Hunk            |
| v9 | albrecht | 2026-05-08 | spaetere Aenderung                   |
+----+----------+------------+--------------------------------------+
```

Klick auf die Versionsnummer fuehrt zum bestehenden Diff-Tab und zeigt den Diff dieser Version
gegen ihre Vorgaengerversion (`Diff.jsp?page=X&r1=v6&r2=v7`).

## Fachliche Semantik

- "Blame" einer Zeile = die Version, in der die Zeile in ihrer aktuellen Form das letzte Mal
  eingefuehrt oder geaendert wurde.
- Whitespace-only Aenderungen werden per Default ignoriert: wenn Trim+Whitespace-Normalisierung
  zweier Zeilen identisch ist, gilt die Zeile als "unveraendert" und behaelt ihren Blame.
- Reverts bekommen den **neuen** Author/Datum (gleiches Verhalten wie git blame ohne `-w`).
- Renamed Pages: JSPWiki haengt die Historie an die neue Page an (zumindest grundsaetzlich)
  — wir nutzen `getVersionHistory(pageName)` und vertrauen JSPWiki.

## Datenmodell

```java
record LineBlame(
        int lineNumber,            // 1-basiert in der aktuellen Version
        int introducedInVersion,   // Versionsnummer (1-basiert)
        String author,
        Instant date,
        @Nullable String changeNote
) {}

record PageAnnotation(
        String pageName,
        int currentVersion,
        List<LineBlame> lines      // 1 Eintrag pro Zeile der aktuellen Version
) {}
```

## Algorithmus

Eingabe: `pageName`. Versionen aufsteigend `(v1, v2, ..., vn)`.

1. Hole alle Versionen ueber `PageManager.getVersionHistory(pageName)`. Pro Version
   brauchen wir Versionsnummer, Autor, Aenderungs-Datum, Change-Note und Text.
2. Initialisiere ein `List<LineBlame> current` aus dem Text von `v1`. Alle Zeilen bekommen
   `LineBlame(introducedInVersion=1, author=v1.author, date=v1.date)`.
3. Fuer jeden Schritt `vi -> vi+1`:
   - Diff zwischen normalisiertem Text von `vi` und `vi+1` (Zeilen mit
     `normalize(line)` als Vergleichsschluessel — siehe Whitespace-Default).
   - `java-diff-utils` (haben wir schon transitiv ueber `KnowWE-Plugin-TextDiff`).
   - Aus dem Diff: kompakte Operations-Liste (KEEP / DELETE / INSERT) baut die neue
     `current`-Liste auf:
     - KEEP: uebernehme bestehende `LineBlame` aus `current`.
     - DELETE: ueberspringe Zeile aus `current`.
     - INSERT: neuer `LineBlame(introducedInVersion=vi+1, author=vi+1.author, ...)`.
   - CHANGE wird vom java-diff-utils als DELETE+INSERT modelliert — passt direkt.
4. Nach dem letzten Schritt ist `current` die `PageAnnotation` fuer `vn`.

Die Zeilen-Texte werden beim Rendern nochmal aus dem aktuellen Versionstext gezogen — das
Modell traegt nur Blame-Metadaten. Damit muessen wir nicht jedes mal den Text durchschleppen.

### Komplexitaet

- O(versions x diff(textsize)). Bei 200 Versionen mit ~1000 Zeilen pro Version sind das
  einige Sekunden Cold-Cache. Daher Cache pro Page.
- Cache-Invalidierung via WikiEvents: Page-Save / Rename / Delete invalidieren den Eintrag
  fuer die betroffene Page. KnowWEPlugin laeuft schon als WikiEventListener, das nutzen wir
  mit.

## Whitespace-Behandlung

Default: ignorieren.

Implementierung: `LineMatcher` nutzt `normalize(line) = line.replaceAll("\\s+", " ").trim()`
als Vergleichsschluessel fuer den Diff. Damit gelten zwei Zeilen, die sich nur in
Whitespace unterscheiden, als gleich -> Blame bleibt erhalten.

Spaeter konfigurierbar machen, wenn jemand die rohe Variante braucht.

## Komponenten

```
PageAnnotator         - Algorithmus, package-private
PageAnnotationCache   - Map<pageName, AnnotationCacheEntry> + Event-Listener-Hook
PageAnnotation        - Modell (record)
LineBlame             - Modell (record)
AnnotatePageAction    - HTTP-Endpunkt, liefert server-rendered HTML
AnnotateRenderer      - HTML-Builder, analog zu DiffHtmlRenderer
AnnotateTab.jsp       - Tab-Inhalt fuer InfoContent.jsp
KnowWE-Plugin-Annotation.css/.js - clientseitige Styles und kleine Interaktionen
```

## Anzeige

Server-rendered HTML mit Web Component `<knowwe-page-annotate>` und declarative Shadow DOM
fuer Stil-Isolation (analog zu `<knowwe-text-diff>`):

- 4-spaltige Tabelle: Version | Autor | Datum | Inhalt.
- Jede Zelle hat `part`-Attribute fuer externes Styling.
- Versionsnummer ist Link auf `Diff.jsp?page=X&r1=v(i-1)&r2=vi`.
- Fuer `v1`-Zeilen ist der Diff-Link inaktiv (kein Vorgaenger).
- Hover auf Autor zeigt Tooltip mit Change-Note (falls vorhanden).
- Whitespace-Default: kein Toggle in der ersten Version, nur per Code aktiv.

## Implementierungsplan

### Schritt 1: Modul-Setup [erledigt]
- POM, `plugin.xml`, dieser Plan-Ordner. Modul ist in `KnowWE-Essentials/pom.xml` und im
  Parent-`dependencyManagement` registriert.

### Schritt 2: Datenmodell + Tests
- `LineBlame`, `PageAnnotation` als records.
- Unit-Tests mit synthetischer Versionshistorie (kein WikiContext, nur reine Listen von
  `(version, author, date, text)`-Tupeln).
- Verifikation: triviale Faelle (nur v1, v1+v2 mit insert/delete/change).

### Schritt 3: PageAnnotator
- Reine Logik ohne JSPWiki-Abhaengigkeit. Eingabe: `List<VersionEntry>` mit Text + Meta.
- `java-diff-utils` fuer line-Diff mit `LineMatcher` (Whitespace-Normalisierung).
- Tests: insert/delete/change-Sequenzen, Reverts, Whitespace-Aenderungen.

### Schritt 4: JSPWiki-Adapter
- Bruecke zwischen `PageManager.getVersionHistory(...)` und `PageAnnotator`-Input.
- Hier wohnt das `Context`-Wissen, sonst nichts.

### Schritt 5: PageAnnotationCache
- `ConcurrentHashMap<String, CacheEntry>` mit `(pageVersion, PageAnnotation)`.
- Invalidierung ueber `WikiEventListener`: bei Page-Save/Rename/Delete den Eintrag verwerfen.
- Lazy-Compute on demand.

### Schritt 6: Server-Renderer (HTML)
- `AnnotateRenderer.render(PageAnnotation, currentText)` -> HTML mit
  `<knowwe-page-annotate>` + declarative Shadow DOM.
- HTML-Escaping fuer Inhalt + Author + Change-Note.
- Diff-Link nur wenn Version > 1.

### Schritt 7: Action
- `AnnotatePageAction` als KnowWE-Action.
- Parameter: `page`, optional `version` (default = LATEST).
- Antwort: server-rendered HTML.
- Plugin.xml-Eintrag.

### Schritt 8: Tab in InfoContent.jsp
- Ueber `webapp/templates/default/InfoContent.jsp` einen neuen Tab anhaengen — JSPWiki
  rendert Tabs ueber das `tabs`-Tag.
- Tab-Inhalt laedt das HTML der Action via Lazy-Load (oder direkt server-rendered).

### Schritt 9: CSS + minimaler JS
- `KnowWE-Plugin-Annotation.css` als Shadow-DOM-Stylesheet (analog TextDiff).
- Minimaler JS, falls fuer Hover-Tooltips noetig — sonst pure CSS.

### Schritt 10: Verifikation
- Manueller Test im laufenden Wiki: bestehende Page mit Versionshistorie, Annotate-Tab
  oeffnen, Versionsnummer anklicken, Diff erscheint korrekt.
- Einheitstest fuer `PageAnnotator` mit ~5-10 syntheticen Szenarien.

## Risiken und offene Punkte

### Performance bei langer Historie

Pages mit hunderten Versionen sind selten, aber moeglich. Cold-Cache-Compute kann mehrere
Sekunden dauern. Der Tab oeffnet im Pull-Modus -> kein Page-Load-Block. Bei sichtbarer
Latenz spaeter Spinner einfuehren.

### Renames

JSPWiki's `getVersionHistory(pageName)` haengt fuer renamede Pages typischerweise die alte
Historie an. Falls nicht: nur Versionen seit Rename werden annotated. Akzeptabel als
v1-Verhalten, spaeter ggf. Rename-Tracking ergaenzen.

### Move-Detection / Copy-Detection

`git blame` kann Zeilen ueber Pages hinweg verfolgen (`-C`). Wir nicht — wir bleiben strikt
innerhalb der einen Page. Akzeptabel als v1.

### Whitespace-Toggle

Vorerst hardcoded ignorieren. Toggle im UI spaeter, wenn jemand danach fragt.

### Cache-Memory

Pro Page ein `LineBlame[]` mit ~50 Bytes pro Zeile. Bei 1000 Pages * 1000 Zeilen ~50 MB.
Akzeptabel. Bei sehr grossen Wikis ggf. LRU einfuehren.

## Status

- [x] Schritt 1: Modul-Setup
- [ ] Schritt 2: Datenmodell + Tests
- [ ] Schritt 3: PageAnnotator
- [ ] Schritt 4: JSPWiki-Adapter
- [ ] Schritt 5: PageAnnotationCache
- [ ] Schritt 6: Server-Renderer
- [ ] Schritt 7: Action
- [ ] Schritt 8: Tab in InfoContent.jsp
- [ ] Schritt 9: CSS + JS
- [ ] Schritt 10: Verifikation
