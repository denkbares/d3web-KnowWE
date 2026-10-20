/** Contributions of markups to the editor, keyed by the markup name in lower case. */
const markups = new Map();

/**
 * Registers what the markup with the given name contributes to the editor. The name is matched case insensitively
 * against the name in the `%%Name` declaration. All fields are optional.
 *
 * The `parser` is the Lezer parser for the body of the markup. It receives the text between the declaration and the
 * closing line, starting with the rest of the declaration line, or the content of a single line or inline markup.
 * Markups without a parser use the default body parser with annotation highlighting.
 *
 * The function `complete(block, context)` offers completions in the body of the markup. It returns a
 * `CompletionResult`, null or a promise of either. The block describes the markup at the caret as `markupBlock`
 * does. The context holds `pos`, `explicit`, `aborted`, `addEventListener` and `matchBefore` like a
 * `CompletionContext`. Positions in the context and in the result are relative to the start of the body. Only the
 * `apply` functions of the options get document positions, since they act on the editor.
 *
 * The `highlight` style colors the nodes of the parser in place of the editor's style. Scope it to the language
 * (`HighlightStyle.define(specs, {scope: language})`) and register `language.parser`, which marks the embedded trees
 * with that language. Properties the style leaves out still come from the editor's style.
 *
 * The `language` is a `LanguageSupport` for the body on its own, outside of a page. Read-only views use it with the
 * `highlight` style to show snippets of the markup's content, for example code blocks in a chat.
 *
 * Call this at the top level of a module script declared in the plugin.xml of the markup. Module scripts run before
 * the document is loaded, and editors are created on that event or later, so every registration precedes the first
 * parse. A second registration for the same name replaces the first.
 */
export function registerMarkup(name, {parser = null, complete = null, highlight = null, language = null} = {}) {
    markups.set(name.toLowerCase(), {parser, complete, highlight, language});
}

export function unregisterMarkup(name) {
    markups.delete(name.toLowerCase());
}

/** Returns the contribution registered for the markup name, or null. */
export function getMarkup(name) {
    return markups.get(name.toLowerCase()) ?? null;
}

/** Returns the distinct highlight styles of all registered markups. */
export function markupHighlightStyles() {
    return [...new Set([...markups.values()].map(markup => markup.highlight).filter(Boolean))];
}

/** Returns the name of the markup declared by the given opening, without "%%" and separator. */
export function markupName(open, input) {
    return input.read(open.from, open.to).trim().replace(/^%%/, "").replace(/[\s:=]*$/, "");
}

/** Completion sources for the whole page, in the order of registration. */
const completions = [];

/**
 * Registers a CodeMirror completion source for the whole page. It applies wherever the caret is not in the body of a
 * markup that registered its own `complete` function, which takes precedence. The source receives the usual
 * `CompletionContext` with document positions, and finds the article title in the `articleTitle` facet of the state.
 *
 * Register before editors are created, like markups. Registering the same source again has no effect.
 */
export function registerCompletion(source) {
    if (!completions.includes(source)) completions.push(source);
}

export function unregisterCompletion(source) {
    const index = completions.indexOf(source);
    if (index >= 0) completions.splice(index, 1);
}

/** Returns the registered completion sources for the whole page. */
export function pageCompletions() {
    return [...completions];
}
