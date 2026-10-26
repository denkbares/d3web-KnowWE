# RightPanel

Seitenpanel rechts neben dem Artikel (unter 600 px Fensterbreite unten), mit Tabs. Jeder Tab kommt von einem
`RightPanelTabProvider`.

## Bausteine

| Baustein | Ort | Aufgabe |
|---|---|---|
| `RightPanelTabProvider` | KnowWE-core, `de.knowwe.core.rightpanel` | ein Tab: Titel, Beschreibung, Icon, `isAvailable`, `isLazy`, `isContextual`, `renderContent` |
| `RightPanelTab` | KnowWE-core | Provider plus ID und Prioritaet |
| `RightPanel` | KnowWE-core | Server-Sicht: verfuegbare Tabs der Seite, `isOpen(UserContext)` |
| `Plugins.getRightPanelTabs` | KnowWE-core | alle Tabs, sortiert nach Prioritaet, dann ID |
| `RightPanelAppendHandler` | dieses Plugin | rendert das Geruest hinter den Artikel |
| `GetRightPanelTabContentAction` | dieses Plugin | liefert den Inhalt eines Lazy-Tabs beim ersten Aktivieren |
| `CustomContentTabProvider` | dieses Plugin | Tab "Custom": rendert den Artikel `RightPanel` |
| `KnowWE-Plugin-RightPanel.js` | dieses Plugin | Ein-/Ausblenden, Resize, Tabs, Zustand |

## Tab registrieren

Deklarativ in der `plugin.xml`; die Extension-`id` ist die Tab-ID (DOM `data-tab`, Storage-Key, Parameter der
Lazy-Action):

```xml
<extension plugin-id="KnowWEExtensionPoints" point-id="RightPanelTab" id="watches">
    <parameter id="class" value="de.d3web.we.watches.WatchesTabProvider" />
    <parameter id="name" value="WatchesTabProvider" />
    <parameter id="description" value="..." />
    <parameter id="version" value="1.0" />
    <parameter id="priority" value="5" />
    <parameter id="script" value="KnowWE-Plugin-d3web-basic.js" />
    <parameter id="css" value="KnowWE-Plugin-d3web-basic.css" />
</extension>
```

Oder programmatisch per `Plugins.registerRightPanelTabProvider(environment, id, provider, priority)`. Gleiche ID
deklarativ und programmatisch: die deklarative gewinnt.

- `isAvailable(UserContext)` wird bei jedem Seitenaufbau ausgewertet. Ist kein Tab verfuegbar, gibt es weder Panel
  noch Toggle-Button.
- Nicht-lazy Tabs werden bei jedem Seitenaufbau gerendert, auch bei zugeklapptem Panel. Lazy Tabs werden erst
  geholt, wenn das Panel offen ist und der Tab aktiv wird.

## Zustand

Im Browser (`localStorage`):

- `rightPanel` (ueber `simpleStorage`): Der Nutzer will das Panel sehen.
- `knowwe.rightPanel.width`: Breite in px.
- `knowwe.rightPanel.tabOrder`: Tab-IDs, zuletzt vom Nutzer aktivierte zuerst. Beim Laden wird der erste vorhandene
  Tab aktiviert.

Fuer den Server spiegelt das JS zwei Cookies (Pfad `/`, ein Jahr):

- `knowwe-rightpanel-open`: `true`/`false`;
- `knowwe-rightpanel-tab`: zuletzt vom Nutzer aktivierter Tab.

`RightPanel.isOpen(UserContext)` wertet sie aus. Damit kann ein Markup Inhalte entweder im Artikel oder in einem Tab
rendern, ohne sie doppelt im DOM zu haben.

## Kontextuelle Tabs

Ein Tab mit `isContextual() == true` zeigt Inhalt der Seite, auf der er verfuegbar ist (wie das Media-Panel in
ServiceMate/ILME). War er der zuletzt gewaehlte Tab und fehlt auf der aktuellen Seite, bleibt das Panel dort zu. Die
Nutzerwahl "offen" bleibt erhalten; auf der naechsten Seite mit dem Tab geht das Panel wieder mit ihm auf. Fehlt ein
nicht-kontextueller Tab, wird wie bisher ein anderer vorhandener Tab gezeigt.

Das Geruest traegt dafuer `data-contextual` (IDs aller kontextuellen Tabs) und `data-open` (was der Server mit
`RightPanel.isOpen` angenommen hat).

## Events

Auf `document`, als `CustomEvent`:

| Event | Wann | `detail` |
|---|---|---|
| `rightPanelTabInitialized` | einmal je Tab, nicht-lazy beim Aufbau, lazy beim ersten Laden | `{id, body}` |
| `rightPanelTabShown` | bei jeder Aktivierung eines Tabs | `{id, body}` |
| `rightPanelShown` | Panel wird eingeblendet; beim Laden nur, wenn der Server es als zu angenommen hat | – |
| `rightPanelHidden` | Panel ist hinausgeglitten (nach der Animation); beim Laden nur, wenn der Server es als offen angenommen hat | – |

Die Cookies sind beim Feuern von `rightPanelShown`/`rightPanelHidden` schon aktualisiert, ein Rerender in der
Behandlung sieht also den neuen Zustand.

Waehrend das Panel animiert, ignoriert es `resize`-Events des Fensters und platziert sich erst danach neu. Aendert
sich beim Ein- oder Ausblenden der Seiteninhalt, kann die Scrollleiste der Seite erscheinen oder verschwinden, und das
Fenster meldet ein Resize; das Panel sprang sonst Frame fuer Frame zwischen Animation und Endposition.

## JS-API

`KNOWWE.core.plugin.rightPanel`: `showRightPanel`, `hideRightPanel`, `activateTab(id)`, `setTabEnabled(id, enabled)`,
`isShownOnRight`, `moveToBottom`, `moveToRight`, aelter: `addToolToRightPanel(title, id, div)`, `buildTool`.
