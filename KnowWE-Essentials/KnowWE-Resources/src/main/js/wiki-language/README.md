# KnowWE wiki language

This project builds `KnowWEExtension/scripts/KnowWE-WikiLanguage.js`, the CodeMirror language for JSPWiki pages
with KnowWE markups. The built file is committed and exposed as the global `KnowWEWikiLanguage`. It depends on the
vendored `codemirror.min.js` bundle and must be loaded after it.

## Contents

- `src/wiki.grammar` is the Lezer grammar for JSPWiki markup and KnowWE markups.
- `src/tokens.js` is the external tokenizer that produces all tokens of the grammar, and the context tracker that
  keeps the open elements.
- `src/markups.js` recognizes markup declarations like `DefaultMarkupType` does.
- `src/content.grammar` and `src/content-tokens.js` parse the default body of a markup, its content and annotations.
- `src/index.js` combines both parsers, adds highlighting tags and folding, and holds the registry of embedded
  languages.

## Syntax

The grammar follows `JSPWikiMarkupParser` rather than the JSPWiki documentation, since the parser defines how a page
renders.

- Line start marks only count in column zero. These are headings `!`, lists `*` and `#`, definitions `;term:text`,
  tables `|` and `||`, rules `----` and preformatted blocks `{{{`.
- A paragraph runs over all lines up to an empty line or a line that starts a block. A line of blanks is not empty.
  A list item continues on lines starting with a blank, a table on lines starting with `|`.
- Bold `__`, italic `''`, monospace `{{ }}` and styles `%%class ... /%` may span lines. A closing mark first ends the
  elements nested in its element. An element that is not closed ends with its paragraph, list item, table cell or
  heading. Like in JSPWiki, bold and italic then stay switched on, the next mark only switches them off, and a
  paragraph break continues them in the next paragraph.
- Links `[text|target|attributes]`, footnotes `[#1]` and `[1]`, plugins `[{Name param='value'}]` with a body after an
  empty line, variables `[{$name}]`, access rules `[{ALLOW ...}]`, metadata `[{SET ...}]` and preformatted text
  `{{{ }}}` are scanned up to their end like in JSPWiki, also across lines. If the end is missing, the opening bracket
  is marked as unclosed and the rest of the page is parsed as usual, while JSPWiki would show it as plain text.
- `%%Name` is a KnowWE markup if a markup of that name is installed, and a JSPWiki style otherwise. A markup is a
  block up to its closing line `/%` or `%` if nothing but a separator and a comment follows the name, a single line
  markup otherwise, or an inline markup up to `%%` or `/%`. KnowWE claims markups before JSPWiki sees the page, so a
  markup declaration line ends links and plugins that are still open. Only preformatted text hides markups.

## Markups and embedded languages

The editor page defines the installed markups before the editor is created, so the grammar tells KnowWE markups from
JSPWiki styles. Names are case insensitive regular expressions, like in KnowWE.

    KnowWEWikiLanguage.defineMarkups([{name: "Rule", inline: false}, {name: "Version", inline: true}]);

Without definitions every `%%Name` declaration in block form is a markup, and a single line declaration if a colon
or equals sign follows the name.

A plugin registers the Lezer parser for the body of its markup by name. The name is matched case insensitively
against the name in the `%%Name` declaration. The parser receives the text between the declaration and the closing
line, starting with the rest of the declaration line, or the content of a single line or inline markup.

    KnowWEWikiLanguage.registerLanguage("COOM:DSL", coomParser);

Register at the top level of a script declared as `module` parameter of the markup's Type extension in
`plugin.xml`. Module scripts run before the document is loaded and the editor is created on that event, so the
registration always precedes the first parse. Markups without a registered parser use the default body parser with
annotation highlighting. The registered parser must be built against the same `@lezer/lr` as the vendored bundle, so
plugin builds externalize `@lezer/lr` to `CodeMirror.lezer.lr` and the other packages likewise, see
`vite.config.mjs` for the mapping.

## Building and testing

    pnpm install
    pnpm build
    pnpm test

`pnpm test` checks the syntax trees of the examples in `test/*.txt`, and parses random documents made of markup
fragments. Every input must parse without error nodes, and incremental parses after edits must equal fresh parses.

    node test/corpus.mjs <wiki directory>...
    node test/fuzz.mjs 400 1 3000

The corpus run parses real wiki pages with the markups in `test/knowwe-markups.json` and applies random edits. The
fuzzer takes the number of documents, a seed and the document size in fragments.
