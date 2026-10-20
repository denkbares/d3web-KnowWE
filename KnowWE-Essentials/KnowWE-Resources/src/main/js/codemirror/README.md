# Vendored CodeMirror bundle

This project builds `KnowWEExtension/scripts/codemirror.min.js`, which exposes CodeMirror 6 and Lezer as the
global `CodeMirror` object with one namespace per package:

- `CodeMirror.state`, `CodeMirror.view`, `CodeMirror.language`, `CodeMirror.commands`, `CodeMirror.search`,
  `CodeMirror.autocomplete`, `CodeMirror.lint`, `CodeMirror.merge`
- `CodeMirror.lezer.common`, `CodeMirror.lezer.highlight`, `CodeMirror.lezer.lr`

The built file is committed and registered in `ResourceLoader`, like the React scripts. Plugins must not bundle
their own copy, since CodeMirror facets and state fields are identity based. In a plugin's Vite build, mark the
packages as external and map them to the namespaces, for example `"@codemirror/state": "CodeMirror.state"`.

Plugins should pin the same package versions as this project for type checking. Upgrade versions here and rebuild:

    pnpm install
    pnpm build

## Versions

CodeMirror relies on the native `Array.from`, for example to validate the indent unit. The MooTools bundled in the
JSPWiki haddock template therefore omits its 1.5 compat alias of `Array.from`, see the JSPWiki fork.

`@lezer/lr` must stay at 1.4.10 or later. Earlier versions move skipped comments into the preceding node when more
than 25 characters are skipped, which produces malformed trees in the COOM grammar.

## Patches

`patches/` holds changes to the packages, applied by pnpm on install.

`@codemirror/merge` is patched so that a diff supplied through `DiffConfig.override` is used as it is. Without the
patch `Chunk.build` still runs the override result through `makePresentable`, which merges changes separated by
fewer than three characters, expands them to word boundaries and slides them, and `toChunks` merges chunks that are
separated by a single line. Both change the line counts of a diff computed elsewhere. With the patch
`presentableDiff` returns the override result unchanged and `toChunks` merges chunks only when they touch.
