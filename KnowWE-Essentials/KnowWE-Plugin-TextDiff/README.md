# KnowWE TextDiff Plugin

Reusable front and back end text diff components.

## Components

The plugin ships two web components in `KnowWEExtension/scripts/KnowWE-Plugin-TextDiff.js`:

- `<knowwe-text-diff>` displays a text diff, as a CodeMirror view built in the browser or as a table.
- `<knowwe-file-change>` displays one GitHub-inspired file/article change entry and can wrap a
  `<knowwe-text-diff>` when the change includes content changes.

Styles live in `src/main/resources/webapp/KnowWEExtension/css/KnowWE-Plugin-TextDiff.css`.

The script is built from the Vite project in `src/main/js` and committed. CodeMirror resolves to the vendored global
bundle of KnowWE-Resources, which loads on every page, and the wiki language to the global `KnowWEWikiLanguage`.

    cd src/main/js
    pnpm install
    pnpm build
    pnpm test

- `src/diff/` computes the diff: a minimal Myers line diff, in-line refinement by tokens, a whitespace insensitive
  mode. It has no DOM or CodeMirror dependency.
- `src/renderer/codemirror.js` builds the read-only merge view, `src/renderer/table.js` the table.
- `src/components/` holds the two web components.
- `test/` runs with Vitest. The Myers tests compare against the Java chunks of the shared fixtures.

## Renderers

`data-renderer="codemirror"` switches a `<knowwe-text-diff>` to the view built in the browser: wiki and embedded
markups are highlighted, changed tokens within a line are marked, unchanged regions are folded and expand in place,
and the view searches with Mod-F. Without the attribute, or when the CodeMirror bundle is missing, the table of the
server is shown. If the view cannot be created, the component builds the table in the browser from the same texts.

Further attributes of the CodeMirror renderer: `data-layout="unified|split|auto"` (`auto` shows elements of at
least 900 px side by side), `data-language="wiki|none"`, `data-highlight-limit` (characters, default 200000, larger
diffs render without highlighting) and `data-wrap="false"`. `data-context-lines` applies to both renderers.

The `layout` property switches between `unified` and `split` while the view is shown, keeping the line at the top
of the viewport in place. `data-whitespace="ignore"` and the `ignoreWhitespace` property diff the lines with all
whitespace removed, as `git diff -w` does, and leave whitespace tokens unmarked. A diff that then has no changes
says so instead of showing an empty view. With `data-context="name"` both choices are stored in `localStorage` and
applied to every element of that context, now and on later pages. The element dispatches `knowwe-text-diff-layout`
and `knowwe-text-diff-whitespace` on connect and on every change, and `knowwe-text-diff-stats` carries the counts
of the current mode.

`data-layouts="unified"` fixes the layout for places too narrow for two columns, and the exported
`KnowWETextDiff.preferences` reads and sets the choices of a context (`get(context, "whitespace")`,
`set(context, "layout", "split")`) for controls that sit outside the diffs.

Changed words are emphasized only where that helps, in lines that exist on both sides and changed in a few places.
Lines that exist on one side only, rewritten lines (more than half of the text differs) and changes of whitespace
alone, such as a new indentation, show the line tint alone. Wrapped lines keep their indentation on the
continuation rows.

The view exists only while the element is near the viewport. When it scrolls far away, the view is destroyed and the
frame keeps its height, so a long list of diffs holds only the views on screen.

Once the browser computed a diff, the component dispatches `knowwe-text-diff-stats` with the line counts, and a
`<knowwe-file-change>` around it replaces its additions and deletions by them.

## Backend Diff

The Java side lives in `src/main/java/com/denkbares/knowwe/textdiff/`:

- `TextDiff` computes the line model.
- `FileChange` describes one article/file operation and can infer article changes from the live wiki.
- `DiffHtmlRenderer` renders that model as `<knowwe-text-diff>` HTML.
- `TextDiffAction` computes lazy diffs requested by the browser component.

Preferred server-side text diff usage:

```java
TextDiff diff = new TextDiff(oldText, newText);
String html = DiffHtmlRenderer.renderTextDiff(diff);
```

This emits a `<knowwe-text-diff>` with declarative shadow DOM and a linked stylesheet. The browser
component then only hydrates the shadow root and wires the collapsed-context expand buttons.

## Lazy Diff Mode

If `<knowwe-text-diff>` has no server-rendered shadow content, it loads its diff when it enters the viewport. The
table renderer posts `oldText`, `newText`, and optional `contextLines` to `action/TextDiffAction`. The CodeMirror
renderer diffs slotted and programmatic texts in the browser without a request, and asks an action url for the
texts with `format=json`.

```html
<knowwe-text-diff data-context-lines="3">
  <script type="text/plain" slot="old">old content</script>
  <script type="text/plain" slot="new">new content</script>
</knowwe-text-diff>
```

Programmatic usage is also supported:

```js
const diff = document.querySelector('knowwe-text-diff');
diff.oldText = oldText;
diff.newText = newText;
```

Use `data-action-url` to point at a different backend endpoint.

## JSON Format

The browser side renderer (opt in, see below) needs both texts rather than rendered HTML. `DiffJsonRenderer` writes
them as one JSON document together with the line counts, and `TextDiffAction` returns that document instead of HTML
when called with the parameter `format=json`. Endpoints of other plugins that feed `<knowwe-text-diff>` should
support the same parameter.

```json
{"old": "full old text", "new": "full new text", "stats": {"added": 7, "removed": 10}}
```

Line endings are normalized to `\n`, a missing side is `null`. The counts are those of the browser diff, which
gives both texts a trailing newline, so a newline that only one side ends with is not counted. The output contains
no `<` character and can be inlined into a script element.

`DiffRenderOptions.withRenderer(DiffRenderer.CODEMIRROR)` makes the server render a placeholder of a plausible height
plus the JSON in a `<script type="application/json" class="diff-data">` inside the shadow root, and marks the host
with `data-renderer="codemirror"`. The default renderer stays the server rendered table.

The fixtures in `src/test/resources/com/denkbares/knowwe/textdiff/fixtures/` pin the JSON and the line chunks of the
Java diff for both the Java tests and the browser tests of the diff module.

## Created And Deleted Files

`TextDiff` uses `null` for a missing side:

- `new TextDiff(null, newText)` means created/all-added.
- `new TextDiff(oldText, null)` means deleted/all-removed.
- `new TextDiff(null, null)` means empty diff.

The renderer detects created/deleted files and emits a compact single-line-number layout:
`single-line-number new-only` or `single-line-number old-only`.

In lazy mode, use explicit null attributes:

```html
<knowwe-text-diff data-old-null>
  <script type="text/plain" slot="new">created content</script>
</knowwe-text-diff>

<knowwe-text-diff data-new-null>
  <script type="text/plain" slot="old">deleted content</script>
</knowwe-text-diff>
```

An absent slot without one of these null attributes is treated as an empty string.

## File Change Frames

`<knowwe-file-change>` owns the header, badge, rename display, optional article links, stats, and
collapse behavior.

```html
<knowwe-file-change
  data-change="renamed-modified"
  data-old-name="OldArticle"
  data-new-name="NewArticle"
  data-old-url="Wiki.jsp?page=OldArticle"
  data-new-url="Wiki.jsp?page=NewArticle"
  data-additions="4"
  data-deletions="2">
  <knowwe-text-diff>...</knowwe-text-diff>
</knowwe-file-change>
```

Supported changes are `added`, `deleted`, `modified`, `renamed`, `renamed-modified`, and `added-deleted`
(created and deleted again within the observed range).
The component also accepts common aliases like `create`, `delete`, `removed`, `rename`, and
`rename-modified`.

The whole header toggles collapse. Links inside the header still navigate. `data-collapsed`
renders the body collapsed initially. A `<knowwe-text-diff>` with the CodeMirror renderer in the body adds two
buttons to the header, one that switches the diff between one column and two (part `layout-button`) and one that
hides whitespace changes (part `whitespace-button`, pressed while they are hidden). `data-controls` names the
buttons to offer, `data-controls=""` shows none, and a diff with a fixed layout gets no layout button.

Extra controls such as links to related views go into the `actions` slot and appear in the header
between the stats and the badge:

```html
<knowwe-file-change data-change="modified" data-new-name="Article" data-collapsed>
  <a slot="actions" href="Diff.jsp?page=Article&r1=5&r2=3" title="Open page history diff">...</a>
  <knowwe-text-diff data-action-url="action/MyDiffAction?page=Article"></knowwe-text-diff>
</knowwe-file-change>
```

Preferred server-side file/article change usage:

```java
FileChange change = FileChange.fromArticle("OldArticle", "NewArticle", newArticleText);
if (change == null) return "";

String html = DiffHtmlRenderer.renderFileChange(change);
```

`FileChange.fromArticle(...)` uses `Environment.getInstance().getArticleManager()` to load the
current article text and infer the operation. It returns `null` when there is no visible change.

Convenience forms:

```java
FileChange changed = FileChange.fromArticle("Article", newContent);
FileChange deleted = FileChange.fromArticle("Article", null);
FileChange created = FileChange.fromArticle(null, "NewArticle", newContent);
```

Manual construction remains available through `new FileChange(...)` or helpers such as
`FileChange.added(...)`, `FileChange.deleted(...)`, `FileChange.modified(...)`,
`FileChange.renamed(...)`, and `FileChange.renamedModified(...)`.

Additions/deletions are computed from the diff model by `DiffHtmlRenderer` and written as component
attributes.

## Styling And Errors

Both components use shadow DOM and expose CSS `part` attributes plus custom properties for
theming. Set `data-theme="light"` or `data-theme="dark"` to explicitly overwrite the theme.

`<knowwe-text-diff>` sets `data-status="loading"`, `ready`, or `error`. Fetch/backend errors render
as a small status placeholder. `<knowwe-file-change>` is forgiving: unknown change types fall back
to `modified`, invalid stats are hidden, and missing names render as `Unnamed file`.

## Demo

`src/test/java/com/denkbares/knowwe/textdiff/DemoPageGenerator.java` can generate a static HTML
demo. Run it from the IDE and pass an output file, for example:

```text
/path/to/KnowWE-Plugin-TextDiff/target/demo.html
```

The generator rewrites TextDiff CSS/JS URLs so the demo works outside the live KnowWE application.
