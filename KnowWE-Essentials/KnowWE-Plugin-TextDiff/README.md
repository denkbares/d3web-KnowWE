# KnowWE TextDiff Plugin

Reusable front and back end text diff components.

## Components

The plugin ships two web components from
`src/main/resources/webapp/KnowWEExtension/scripts/KnowWE-Plugin-TextDiff.js`:

- `<knowwe-text-diff>` displays a unified text diff.
- `<knowwe-file-change>` displays one GitHub-inspired file/article change entry and can wrap a
  `<knowwe-text-diff>` when the change includes content changes.

Styles live in
`src/main/resources/webapp/KnowWEExtension/css/KnowWE-Plugin-TextDiff.css`.

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

If `<knowwe-text-diff>` has no server-rendered shadow content, it posts `oldText`, `newText`, and
optional `contextLines` to `action/TextDiffAction` when it enters the viewport.

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

Supported changes are `added`, `deleted`, `modified`, `renamed`, and `renamed-modified`.
The component also accepts common aliases like `create`, `delete`, `removed`, `rename`, and
`rename-modified`.

The whole header toggles collapse. Links inside the header still navigate. `data-collapsed`
renders the body collapsed initially.

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
theming. Set `data-theme="light"` or `data-theme="dark"` for an explicit theme; otherwise CSS uses
`prefers-color-scheme`.

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
