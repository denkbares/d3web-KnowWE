# KnowWE wiki language

This project builds `KnowWEExtension/scripts/KnowWE-WikiLanguage.js`, the CodeMirror language for JSPWiki pages
with KnowWE markup blocks. The built file is committed and exposed as the global `KnowWEWikiLanguage`. It depends
on the vendored `codemirror.min.js` bundle and must be loaded after it.

## Contents

- `src/wiki.grammar` is the Lezer grammar for JSPWiki markup: headings, lists, definitions, tables, horizontal
  rules, bold, italic, monospace, preformatted blocks, links, plugins, escapes and line breaks.
- `src/line-tokens.js` is the external tokenizer for everything that is only valid at the start of a line,
  including the markup blocks `%%Name ... /%`. It follows the rules of KnowWE's `DefaultMarkupType`.
- `src/content.grammar` parses the default body of a markup block: annotation lines `@name: value` and text.
- `src/index.js` combines both, adds highlighting tags and folding, and holds the registry of embedded languages.

## Embedding a language for a markup

A plugin registers the Lezer parser for the body of its markup by name. The name is matched case insensitively
against the name in the `%%Name` declaration. The parser receives the text between the declaration line and the
closing line, or the rest of the declaration line for single line markups.

    KnowWEWikiLanguage.registerLanguage("COOM:DSL", coomParser);

Register at the top level of a script declared as `module` parameter of the markup's Type extension in
`plugin.xml`. Module scripts run before the document is loaded and the editor is created on that event, so
the registration always precedes the first parse.
Markups without a registered parser use the default body parser with annotation highlighting. The registered
parser must be built against the same `@lezer/lr` as the vendored bundle, so plugin builds externalize
`@lezer/lr` to `CodeMirror.lezer.lr` and the other packages likewise, see `vite.config.mjs` for the mapping.

## Building and testing

    pnpm install
    pnpm build
    pnpm test

`pnpm test` parses sample documents with the ES build in `dist/` and compares the syntax trees.
