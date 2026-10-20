# KnowWE TextDiff Plugin

Reusable front and back end text diff components.

## Components

The plugin ships two web components in `KnowWEExtension/scripts/KnowWE-Plugin-TextDiff.js`:

- `<knowwe-text-diff>` displays a text diff as a CodeMirror view built in the browser from both texts.
- `<knowwe-file-change>` displays one GitHub-inspired file/article change entry and can wrap a
  `<knowwe-text-diff>` when the change includes content changes.

Styles live in `src/main/resources/webapp/KnowWEExtension/css/KnowWE-Plugin-TextDiff.css`. Both files are
registered at startup by the `TextDiffResources` instantiation.

The script is built from the Vite project in `src/main/js` and committed. CodeMirror resolves to the vendored global
bundle of KnowWE-Resources, which loads before it on every page, and the wiki language to the global
`KnowWEWikiLanguage`.

    cd src/main/js
    pnpm install
    pnpm build
    pnpm test

- `src/diff/` computes the diff: a minimal Myers line diff, in-line refinement by tokens, a whitespace insensitive
  mode. It has no DOM or CodeMirror dependency.
- `src/renderer/codemirror.js` builds the read-only merge view.
- `src/components/` holds the two web components.
- `test/` runs with Vitest. The Myers tests compare against the Java chunks of the shared fixtures.

## The Diff Host

`<knowwe-text-diff>` takes its texts from one of three places: a server rendered declarative shadow root holding
both texts as JSON, the attributes `data-old-text` and `data-new-text` (with `data-old-null` and `data-new-null`
for a missing side), or an endpoint named by `data-action-url` that answers the same JSON document. In the latter
two cases the diff is loaded once the element comes near the viewport.

Wiki and embedded markups are highlighted, changed tokens within a line are marked, unchanged regions are folded
and expand in place, and the view searches with Mod-F. Attributes: `data-layout="unified|split|auto"` (`auto`
shows elements of at least 900 px side by side), `data-layouts="unified"` fixes the layout for narrow places,
`data-context-lines` (default 3, -1 shows all), `data-language="wiki|none"`, `data-highlight-limit` (characters,
default 200000, larger diffs render without highlighting) and `data-wrap="false"`.

With `data-context="name"` the user may choose the layout and whether whitespace changes are hidden. Both choices
are stored in `localStorage` and applied to every element of that context, now and on later pages. A diff with a
context shows both switches in its top right corner, which stay in view while the diff scrolls. `data-controls`
names the switches to offer, `data-controls=""` shows none, and a diff with a fixed layout gets no layout switch.
The `layout` and `ignoreWhitespace` properties make the same choice from script, keeping the line at the top of
the viewport in place. Ignoring whitespace diffs the lines with all whitespace removed, as `git diff -w` does, and
a diff that then has no changes says so. The element dispatches `knowwe-text-diff-stats` with the line counts of
the current mode.

Changed words are emphasized only where that helps, in lines that exist on both sides and changed in a few places.
Lines that exist on one side only, rewritten lines (more than half of the text differs) and changes of whitespace
alone, such as a new indentation, show the line tint alone. Wrapped lines keep their indentation on the
continuation rows.

The view exists only while the element is near the viewport. When it scrolls far away, the view is destroyed and the
frame keeps its height, so a long list of diffs holds only the views on screen.

## Backend

The Java side lives in `src/main/java/com/denkbares/knowwe/textdiff/`:

- `TextDiff` computes the line model with java-diff-utils, for line counts and the height of the placeholder.
- `FileChange` describes one article/file operation.
- `DiffHtmlRenderer` renders both as web component HTML.
- `DiffTexts` holds both texts with normalized line endings and writes the JSON document the browser diffs.

```java
TextDiff diff = new TextDiff(oldText, newText);
String html = DiffHtmlRenderer.renderTextDiff(diff, DiffRenderOptions.defaults()
        .withView(DiffView.AUTO)
        .withContext("page-history"));
```

This emits a `<knowwe-text-diff>` with a declarative shadow root holding the stylesheet link, a placeholder of the
height the view will take and both texts as JSON. `DiffRenderOptions` names the view the diff opens in, the
context lines and the context of the user's choices. `null` for a side of `TextDiff` means a created or a deleted
file, `null` for both an empty diff.

Endpoints that feed a `<knowwe-text-diff>` through `data-action-url` answer with `new DiffTexts(oldText, newText).toJson()`:

```json
{"old": "full old text", "new": "full new text"}
```

Line endings are normalized to `\n`, a missing side is `null`. The output contains no `<` character and can be
inlined into a script element.

The fixtures in `src/test/resources/com/denkbares/knowwe/textdiff/fixtures/` pin the JSON, the line counts and the
line chunks of the diff for both the Java tests and the browser tests of the diff module.

## File Change Frames

`<knowwe-file-change>` owns the header, badge, rename display, optional article links, stats, and
collapse behavior. The switches of a diff in the body belong to the diff, the header holds the stats, the badge
and the `actions` slot.

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
(created and deleted again within the observed range). The whole header toggles collapse. Links and form controls
inside the header still work without toggling. `data-collapsed` renders the body collapsed initially. Once the
diff in the body computed its counts, they replace the given additions and deletions.

Extra controls such as links to related views go into the `actions` slot and appear in the header
between the stats and the badge:

```html
<knowwe-file-change data-change="modified" data-new-name="Article" data-collapsed>
  <a slot="actions" href="Diff.jsp?page=Article&r1=5&r2=3" title="Open page history diff">...</a>
  <knowwe-text-diff data-action-url="action/MyDiffAction?page=Article"></knowwe-text-diff>
</knowwe-file-change>
```

Server side:

```java
FileChange change = FileChange.modified("Article", oldText, newText);
String html = DiffHtmlRenderer.renderFileChange(change, options);
```

`FileChange.added(...)`, `deleted(...)`, `modified(...)`, `renamed(...)` and `renamedModified(...)` build the
model of one article operation with the article links, `new FileChange(...)` takes names and urls as they are.

## Styling And Errors

Both components use shadow DOM and expose CSS `part` attributes plus custom properties for theming. The components
follow the display mode of the wiki, `data-theme="light"` or `data-theme="dark"` overrides it.

`<knowwe-text-diff>` sets `data-status="loading"`, `ready`, or `error`. A failed request and a view that cannot be
built render as a small status placeholder. `<knowwe-file-change>` is forgiving:
unknown change types fall back to `modified`, invalid stats are hidden, and missing names render as `Unnamed file`.

## Demo

`src/test/java/com/denkbares/knowwe/textdiff/DemoPageGenerator.java` can generate a static HTML
demo. Run it from the IDE and pass an output file, for example:

```text
/path/to/KnowWE-Plugin-TextDiff/target/demo.html
```

The generator rewrites TextDiff CSS/JS URLs so the demo works outside the live KnowWE application.
