# „Show Info“-Dialog (CompositeEdit)

Stand: 16.11.2026, nach dem Umbau auf mehrere Elemente (Branch `show-info-multi`). Das Verhalten der alten
Fassung und ihre Fehler stehen am Ende.

## Beteiligte Teile

| Teil | Ort | Aufgabe |
|---|---|---|
| `CompositeEditToolProvider` (+ `CompositeEditTool`) | `de.knowwe.core.tools` | Tool „Show Info“ für jeden `Term`, Ziel-Gruppe `SHOW_INFO` (siehe `ToolTarget`); `CompositeEditTool` ist ein `CombinableTool` |
| `CombinableTool` | KnowWE-core, `de.knowwe.tools` | Tool, das für alle Ziele eines zusammengefassten Tools auf einmal ausgeführt werden kann |
| `TargetedTool` | KnowWE-core, `de.knowwe.tools` | zusammengefasstes Tool; ist das Default-Tool ein `CombinableTool`, führt der Klick dessen kombinierte Aktion aus (`isCombined()`), sonst das Default-Ziel |
| `CompositeEditToolVerbalizer` | `de.knowwe.core.tools` | Typen bestimmen den Text des Tools/Ziel-Labels und ob es für ein Identifier erscheint |
| `CompositeEditOpenDialogAction` | `de.knowwe.core.showinfo` | rendert den Dialog für `termIdentifier` (ein Term) oder `termIdentifiers` (JSON-Array) |
| `ShowInfoRenderer` | `de.knowwe.core.showinfo` | Definitionen und Referenzen aller Elemente, Teile mit `data-elements` markiert; Labels der Elemente |
| `ShowInfoLabelRenderer` | `de.knowwe.core.showinfo` | von Typen implementiert, deren Section im Wiki mehr als das Element rendert (z.B. Kabelname + Prompts in CBX): rendert nur das Element als Label |
| `ObjectInfoRenderer` | `de.knowwe.core.objectinfo` | rendert Kopf, Definitionen und Referenzen (auch für die ObjectInfo-Seite); `renderReferences(…, elementsOf)` gruppiert die Referenzen für den Dialog |
| `CompositeEditSaveAction` | `de.knowwe.core.showinfo` | ersetzt Sections: JSON-Array `{id, text}` als Request-Body (`data`) oder wie früher als Parameter `replaceSections` |
| `RenderPreviewAction` | `de.knowwe.core.action` | rendert die zugeklappten Referenzgruppen nach (Previews, mit `editanchor`) |
| `KnowWE-Plugin-Core-CompositeEdit.js` | `webapp/KnowWEExtension/scripts` | Dialog, Element-Leiste, Zuklappen, Edit-Modus (`_CE`) |
| `KnowWE-Plugin-CompositeEdit.css` | `webapp/KnowWEExtension/css` | Dialog, Leiste, Filter, Edit-Bereiche |
| `InitEditModeAction` | KnowWE-DES, `KnowWE-Plugin-EditMode` | liefert Wikitext + JS-Namespace der editierbaren Sections, sperrt die aktuelle Seite |
| `InstantEditEnableAction`/`-DisableAction` | `KnowWE-Plugin-InstantEdit` | Sperre je Section beim Öffnen/Schließen |

## Mehrere Elemente

- Ein Tool-Menü fasst die „Show Info“-Tools mehrerer Elemente (z.B. State-Frage, Unit, UI; oder Frage, Unit, Socket,
  Pin) zu einem `TargetedTool` zusammen. Sind alle Ziele `CompositeEditTool`s, öffnet der Klick auf das Tool einen
  Dialog mit **allen** Zielen in Dropdown-Reihenfolge
  (`openCompositeEditDialog(['<id1>', '<id2>', …])`); im Dropdown ist dann kein Ziel als Default markiert, jeder
  Eintrag öffnet weiter nur sein Element. Ein „Show Info“ mit nur einem Ziel verhält sich wie früher.
- CBX: `GeneratedTestStepElementsToolMenuAction` (Menü generierter Testschritt-Elemente) erzeugt dafür
  `CompositeEditTool`s statt `DefaultTool`s, auch für die Kopien der TSM-Tools.
- Dialog: Titel „Show Info (n elements)“, oben eine mitlaufende Leiste mit einer Checkbox je Element (anfangs alle
  an), einer Nummer und dem Element, wie im Wiki gerendert (Farben usw.), dahinter der Typ. Gerendert wird die Section
  des Elements **an der angeklickten Stelle**: Das JS schickt die Section des offenen Tool-Menüs mit
  (`contextSection`, aus `#toolPopupMenuID[data-menu-id]` → Dekorator → `toolmenuidentifier`); der Server nimmt die
  Definition/Referenz des Elements in dieser Section (bzw. darüber). Bei `Bedienkonsole.Hauptschalter@state` in L0008:
  1 = `Bedienkonsole.Hauptschalter@state`, 2 = `Bedienkonsole`, 3 = `Bedienkonsole.Hauptschalter`. Ohne Kontext (z.B.
  generierte Testschritt-Elemente): eine Definition, sonst eine Referenz, bevorzugt mit dem Namen des Elements als
  Text. Implementiert der Typ der Section `ShowInfoLabelRenderer`, rendert dieser das Label (nur das Element, z.B. ohne
  die Prompts eines Kabels). Klicks ins Label schalten nur die Checkbox (keine Links/Tool-Menüs).
- Die Nummern stehen als Badges auch an jeder Definition und jeder (Unter-)Gruppe der Referenzen: welche Elemente
  darin vorkommen, „all“ wenn alle.
- Inhalt wie bei einem Element: **Definition** (alle Definitionen aller Elemente), dann **References** (alle Referenzen,
  nach Typ gruppiert, Pakete nach Artikel). Elemente ohne Definition bekommen eine Zeile „<Name>: No definitions
  found!“ bzw. „Term not defined“.
- **Previews werden je Kategorie zusammengeführt:** Eine Preview-Section erscheint einmal je Kategorie mit allen
  zugehörigen Fundstellen. Ein Testschritt mit Fundstellen in Required und Transitions erscheint in beiden
  Referenzgruppen. Definitionen erscheinen in ihrer eigenen Kategorie. Die Gruppenköpfe zählen die Referenz-Sections.
- **Filter:** Alle Teile (Definitionen, Gruppen, Untergruppen, nachgeladene Previews) tragen `data-elements` (Indizes
  der Elemente). Ein Teil wird ausgeblendet (`.showinfo-hidden`), wenn keins seiner Elemente angehakt ist; Abhaken
  verwirft nichts. Die Open-Action liefert `referenceElements` (Referenz-Section-ID → Elemente). Nachgeladene Previews
  verwenden die Referenzen aus ihrem Attribut `data-references`. Die Zahlen in den Gruppenköpfen bleiben
  die Gesamtzahlen.
- Große Teile (Referenzgruppen) sind wie bisher zugeklappt; geöffnete Gruppen merkt sich der Dialog in `simpleStorage`
  unter `CompositeEditCollapseState-<Termname>` bzw. `…-<Name1> | <Name2> | …` und dem Gruppennamen. Panels öffnen
  über die Klasse `open` (nicht mehr über Inline-Style).

## Ablauf Ansicht

1. `openCompositeEditDialog(identifier | [identifier…], contextSection?)` bzw. `openDialog(identifier | [identifier…], action)`
   holt per `_KA` die Action (Default `CompositeEditOpenDialogAction`). Antwort: `header`, `result`, `canWriteAll`,
   `elements` (je `term`, `name`, `type`, bei mehreren `label`), `referenceElements`. Andere Actions (CBX
   `ShowMeasurementsTool`) liefern nur `header`, `result`, `canWriteAll`; ihr Inhalt wird unverändert gezeigt.
2. Modaler jQuery-UI-Dialog (90 % Höhe, 60 % Breite, Maximieren-Icon), Buttons Close/Edit.
3. Aufklappen lädt die Previews per `RenderPreviewAction` (Mode `list`), markiert die Sections (`highlight`) und
   triggert `OpenCompositeEdit` auf `body` (KONAP mountet darauf React-Editoren). „Expand All“ öffnet/schließt eine
   Gruppe samt Untergruppen je Artikel.
4. Schließen: Close, Escape (nur Ansicht), Klick neben den Dialog (nur Ansicht).

## Ablauf Edit-Modus

- Edit-Button bzw. Ctrl/Alt+E / Ctrl/Alt+D (die Accesskeys der Seite sind solange entfernt). Nur möglich, wenn
  `canWriteAll` (Schreibrecht auf alle Referenz-Sections aller Elemente). `_EC.mode` ist dann `_CE`.
- Jede Preview mit `editanchor` wird zum `.editelement` (auch für später geladene Previews), **jetzt auch die
  Definitionen**; `canWriteAll` prüft daher Definitionen und Referenzen. Sollte dieselbe Section doch an mehreren
  Stellen stehen: nur ein Vorkommen ist editierbar, die anderen sind `.editelement-duplicate` (blass); ein Klick darauf
  editiert dort, solange die Section nicht schon offen ist, sonst wird die offene gezeigt.
- `InitEditModeAction` lädt (asynchron, gesammelt) Wikitext und Tool-Namespace; der Klick auf ein Element öffnet es
  über `_EC.editTools.resolve` (also wie Instant Edit mit CodeMirror bzw. Umschalter Text/Visuell) in einem
  `.editarea` mit Werkzeugleiste oben (`generateButtons`; Revert, Delete, Tool-eigene Buttons).
- **ID-Verleih:** Tools suchen ihre Section über `#<sectionId>`. Liegt die Section auch auf der aktuellen Seite, gibt
  das Seitenelement seine ID bis zum Schließen des Dialogs ab.
- **Speichern** (Save bzw. Ctrl+S): gesendet werden nur Sections, die **nicht ausgeblendet** sind (mindestens eins
  ihrer Elemente angehakt) und
  **tatsächlich geändert** sind (Text ≠ Wikitext und Text ≠ Text direkt nach dem Öffnen bzw. `unloadCondition`
  meldet Änderung – nötig, weil Tools beim Generieren umformatieren und z.B. das CBX-TestStep-Tool keine eigene
  `unloadCondition` hat), plus gelöschte Sections eingeblendeter Elemente; jede Section einmal. Danach Reload.
  Ohne Änderungen: zurück in die Ansicht mit Hinweis. Nur Ausgeblendetes geändert: Warnung, der Edit-Modus bleibt;
  die abgehakten Elemente mit solchen Änderungen bekommen in der Leiste einen Punkt.
- Cancel (Button, Ctrl+Q): schließt alle offenen Sections (Rückfrage nur bei Änderungen), zurück in die Ansicht.
  Ctrl+Q in einem Editor schließt nur diese Section.

## Öffentliche JS-API / Fremdnutzer

- `openCompositeEditDialog(identifier)`: KnowWE-Plugin-Core (Term-Suche), `KnowWEUtils`, d3web-basic
  (`ValueTooltipRenderer`, `SolutionPanelUtils`), KnowSEC `DialogRenderer`, KONAP `ShowObjectInfoToolProvider`,
  CBX (TSM-Übersicht im JS, 3D-Viewer `CBX3DViewer.ts`). Nimmt jetzt auch ein Array.
- `openDialog(identifier, action)`: CBX `ShowMeasurementsTool`.
- `closeOldDialog`, `resetVariables`: CBX `openMeasurementsDialog` (toter Code, ruft nicht exportierte Funktionen).
- Edit-Commons-Modus: `toolNameSpace`, `getSaveCancelDeleteButtons`, `getButtonsTable`, `reopenSection`,
  `getChangeNote`, `cancelSection`, `deleteSection`, `save`.
- Weitere Eigenschaften: `dialogDiv`, `enabled` (Dialog offen), `mode`/`ModeEnum`, `userCanWriteAllSections`,
  `editableSections`, `cancelCache`, `afterPreviewsLoad`, `enableCompositeEditModeByKey`, `disable`.
- Event `OpenCompositeEdit` (KONAP), `#compositeEdit` als View-Root (`KnowWE-Plugin-Core.js`, `ToolMenu.js`).
- `KNOWWE.plugin.compositeEditTool.wikiText` (`_CEWT`) gibt es nicht mehr (nur intern genutzt).

## Alte Fassung (vor dem Umbau) und behobene Fehler

Früher nur ein Term je Dialog; im Edit-Modus wurden **alle geöffneten** Sections gespeichert, geändert oder nicht,
mit eigenem Hover-Menü statt der Tool-Buttons und ohne `_EC.editTools`. Behoben:

- Edit-Buttons wechselten die Beschriftung nicht (`.ui-button-text` gibt es in jQuery UI 1.14 nicht mehr).
- Edit-Elemente wurden mehrfach initialisiert (`editableSections` doppelt); doppelte Preview-Sections ergaben doppelte
  `.editarea` und hätten doppelt gespeichert; dabei wurden Wikitexte mehrerer Sections zusammengeklebt.
- Sections der aktuellen Seite: Tools griffen über `#id` ins Seitenelement statt in den Dialog.
- Escape schloss den Dialog auch im Edit-Modus (Änderungen weg); Tastatur-Handler sammelten sich an, weil sie mit
  anonymen Funktionen registriert und nie wieder entfernt wurden.
- `postProcessHTML(id)` mit undefinierter Variable; `saveExpandState` warf beim ersten Zuklappen; `expandSavedStates`
  setzte die Pfeile am ersten statt am betroffenen Panel; Maximieren setzte die Breite auf die Fensterhöhe.
- `CompositeEditSaveAction` bekam die Texte als URL-Parameter (Längenlimit), jetzt im Request-Body.
- Die ungenutzte, teure Volltextsuche `renderPlainTextOccurrences` in der Open-Action entfällt.
