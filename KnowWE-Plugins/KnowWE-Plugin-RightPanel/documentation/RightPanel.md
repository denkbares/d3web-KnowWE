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
| `CustomContentTabProvider` | dieses Plugin | Tab "Custom": rendert den Artikel `RightPanel`; nur verfuegbar, wenn der Artikel Inhalt ausserhalb deaktivierter Markups (`%%Off:…`) hat |
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
  noch Toggle-Button. Auch "Custom" ist nicht immer verfuegbar (s.o.): Ohne Inhalt im Artikel `RightPanel`
  und ohne andere Tabs gibt es also kein Panel; der Artikel wird dann ueber die normale Wiki-Seite angelegt.
- Nicht-lazy Tabs werden bei jedem Seitenaufbau gerendert, auch bei zugeklapptem Panel. Lazy Tabs werden erst
  geholt, wenn das Panel offen ist und der Tab aktiv wird.

## Zustand

Im Browser (`localStorage`):

- `rightPanel` (ueber `simpleStorage`): Der Nutzer will das Panel sehen.
- `knowwe.rightPanel.width`: Breite in px.
- `knowwe.rightPanel.tabOrder`: Tab-IDs, zuletzt vom Nutzer aktivierte zuerst. Beim Laden wird der erste vorhandene
  Tab aktiviert.
- `knowwe.rightPanel.leftMenuHidden`: `true`, solange das Panel das LeftMenu eingeklappt hat (siehe unten).

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

## Breite und LeftMenu

Rechts laesst sich das Panel am linken Rand zwischen 300 px und 80 % der Fensterbreite ziehen. Wird es dabei (oder
beim Einblenden) breiter als 50 %, klappt das JS das LeftMenu ein; wird es wieder schmaler als 50 % oder ausgeblendet,
klappt es das LeftMenu wieder aus, aber nur, wenn es selbst es eingeklappt hat (`knowwe.rightPanel.leftMenuHidden`).

- Geschaltet wird ueber den Haddock-Toggle (`li#menu`, `data-toggle` an `.content`, Klasse `active`). Der speichert den
  Zustand in der Preference `Sidebar`, der Server rendert die naechste Seite also schon mit eingeklapptem LeftMenu,
  ohne Springen beim Laden.
- Der Skin blendet die Sidebar per `display:none` aus, ohne Transition. Das JS schiebt sie deshalb per negativem
  `margin-left` (inline, `!important`) hinaus und toggelt danach, bzw. toggelt und schiebt sie im selben Task aus dem
  Bild und dann hinein. Die Seite waechst und schrumpft dabei stetig mit. Kreuzt die Breite waehrend der Animation die
  Grenze erneut, gleicht das JS nach der Animation ab.
- Eingeklappt wird nur beim Ueberschreiten der Grenze. Klappt der Nutzer das LeftMenu bei breitem Panel selbst wieder
  aus (oder vorher selbst ein), entfaellt die Markierung (Klick-Listener in der Capture-Phase, Haddock stoppt das
  Event) und das Panel laesst das LeftMenu in Ruhe.
- Beim Laden schaltet das JS nicht ein (das LeftMenu ist schon im gespeicherten Zustand); es klappt nur aus, wenn das
  Panel inzwischen schmaler als 50 % ist (z.B. groesseres Fenster). Auf Seiten, auf denen das Panel zu bleibt (z.B.
  kontextueller Tab fehlt), bleibt auch das LeftMenu eingeklappt, damit es beim Seitenwechsel nicht springt.
- Unten (Fenster unter 600 px) wird das LeftMenu nicht angefasst.

## Inaktive Tabs

Inaktive Tabs tragen `hidden` und `data-defer-rerender`. `KNOWWE.core.rerendercontent.rerenderShown` (in
`KnowWE.js`, genutzt vom `update`-Event) rendert `ReRenderSectionMarker` darin nicht neu, sondern setzt am Tab
`data-rerender-pending`; beim Aktivieren rendert `rerenderPending` sie einmal nach. Plugins, die eigene Inhalte bei
`update` laden (z.B. das CBX-Wiring), pruefen `closest("[data-defer-rerender]")` und laden bei `rightPanelTabShown`
nach.

## JS-API

`KNOWWE.core.plugin.rightPanel`: `showRightPanel`, `hideRightPanel`, `activateTab(id)`, `setTabEnabled(id, enabled)`,
`isShownOnRight`, `moveToBottom`, `moveToRight`, aelter: `addToolToRightPanel(title, id, div)`, `buildTool`.
