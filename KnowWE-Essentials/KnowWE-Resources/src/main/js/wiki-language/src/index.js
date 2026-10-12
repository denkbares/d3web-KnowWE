import {parseMixed} from "@lezer/common";
import {styleTags, tags as t} from "@lezer/highlight";
import {foldNodeProp, foldService, LanguageSupport, LRLanguage, syntaxTree} from "@codemirror/language";
import {parser as wikiParser} from "./wiki.grammar";
import {parser as contentParser} from "./content.grammar";

export {defineMarkups} from "./markups.js";

/**
 * Registry of parsers for the bodies of KnowWE markup blocks, keyed by the markup name in lower case. Plugins
 * register the parser of their markup here before the editor is created.
 */
const registry = new Map();

/**
 * Registers the Lezer parser for the body of the markup with the given name. Call this at the top level of a
 * module script declared in the plugin.xml of the markup. Module scripts run before the document is loaded, and
 * the editor is created on that event, so every registration precedes the first parse.
 */
export function registerLanguage(markupName, parser) {
    registry.set(markupName.toLowerCase(), parser);
}

export function unregisterLanguage(markupName) {
    registry.delete(markupName.toLowerCase());
}

export function getLanguage(markupName) {
    return registry.get(markupName.toLowerCase()) ?? null;
}

/** Returns the name of the markup declared by the given opening, without "%%" and separator. */
export function markupName(open, input) {
    return input.read(open.from, open.to).trim().replace(/^%%/, "").replace(/[\s:=]*$/, "");
}

/** Parser for the default body of a markup block with its content, a declaration comment and annotations. */
export const markupContentParser = contentParser.configure({
    props: [
        styleTags({
            DeclarationComment: t.lineComment,
            AnnotationName: t.annotation,
            AnnotationValue: t.attributeValue,
        }),
    ],
});

// folds a block from the end of its first line to the start of its closing line, or to its end
function foldBlock(node, state) {
    const first = state.doc.lineAt(node.from);
    const last = node.lastChild;
    const closed = last && (last.name === "MarkupClose" || last.name === "StyleMark" || last.name === "CodeMark");
    const end = closed ? state.doc.lineAt(last.from).from - 1 : node.to;
    return end > first.to ? {from: first.to, to: end} : null;
}

/** Parser for JSPWiki markup, embedding registered parsers into the bodies of markup blocks. */
export const parser = wikiParser.configure({
    props: [
        styleTags({
            "Heading/...": t.heading,
            "HeadingMark ListMark DefinitionMark DescriptionMark": t.list,
            "BoldMark ItalicMark MonospaceMark CodeMark": t.processingInstruction,
            "Bold/...": t.strong,
            "Italic/...": t.emphasis,
            "Monospace/... InlineCode/... Preformatted/...": t.monospace,
            "TableHeader/...": t.strong,
            "CellMark HeaderMark": t.punctuation,
            "Link/... Footnote/... FootnoteReference/...": t.link,
            URL: t.url,
            LinkMark: t.punctuation,
            "LinkAttributes/...": t.meta,
            "Plugin/... Variable/... AccessRule/... Metadata/...": t.processingInstruction,
            PluginName: t.macroName,
            PluginMark: t.punctuation,
            "StyleMark StyleDeclaration": t.meta,
            StyleClass: t.className,
            Escape: t.escape,
            Entity: t.character,
            "LineBreak HorizontalRule": t.contentSeparator,
            "MarkupOpen MarkupClose": t.tagName,
            Unclosed: t.invalid,
        }),
        foldNodeProp.add({
            "Markup StyleBlock Preformatted": foldBlock,
            Plugin(node, state) {
                const first = state.doc.lineAt(node.from);
                return node.to - 2 > first.to ? {from: first.to, to: node.to - 2} : null;
            },
        }),
    ],
    wrap: parseMixed((node, input) => {
        if (node.name !== "MarkupContent") return null;
        const open = node.node.prevSibling;
        if (!open || open.name !== "MarkupOpen") return null;
        return {parser: getLanguage(markupName(open, input)) ?? markupContentParser};
    }),
});

export const wikiLanguage = LRLanguage.define({
    name: "jspwiki",
    parser,
    languageData: {
        // apostrophes and quotes are prose in wiki text, so only brackets close automatically
        closeBrackets: {brackets: ["(", "[", "{"]},
    },
});

/** Returns the level of the heading node, 3 for the largest heading "!!!" and 1 for the smallest "!". */
export function headingLevel(heading) {
    const mark = heading.getChild("HeadingMark");
    return mark ? mark.to - mark.from : 1;
}

/**
 * Returns the end of the section of the heading node, the start of the next heading of the same or a larger level in
 * the same block, or the end of that block. Returns -1 if the section may continue beyond the parsed text.
 */
export function sectionEnd(heading, treeLength, docLength) {
    const level = headingLevel(heading);
    for (let next = heading.nextSibling; next; next = next.nextSibling) {
        if (next.name === "Heading" && headingLevel(next) >= level) return next.from;
        // the closing mark of a style block
        if (next.name === "StyleMark") return next.from;
    }
    const end = heading.parent.to;
    return end >= treeLength && treeLength < docLength ? -1 : end;
}

// folds the section of a heading from the end of the heading up to the last non blank character
function foldSection(state, lineStart, lineEnd) {
    const tree = syntaxTree(state);
    let heading = tree.resolve(lineStart, 1);
    while (heading && heading.name !== "Heading") heading = heading.parent;
    if (!heading || heading.from !== lineStart) return null;
    let end = sectionEnd(heading, tree.length, state.doc.length);
    if (end < 0) {
        // the editor parses only a bit beyond the visible text, so the rest is searched for heading marks
        const mark = "!".repeat(headingLevel(heading));
        const first = state.doc.lineAt(tree.length);
        let pos = first.from;
        for (const lines = state.doc.iterLines(first.number); !lines.next().done;) {
            if (lines.value.startsWith(mark)) break;
            pos += lines.value.length + 1;
        }
        end = Math.min(pos, state.doc.length);
    }
    // a link or plugin in the heading may continue on the next lines
    const from = Math.max(lineEnd, state.doc.lineAt(heading.to).to);
    while (end > from && /\s/.test(state.doc.sliceString(end - 1, end))) end--;
    return end > from ? {from, to: end} : null;
}

/** The CodeMirror language support for JSPWiki pages. */
export function wiki() {
    return new LanguageSupport(wikiLanguage, foldService.of(foldSection));
}
