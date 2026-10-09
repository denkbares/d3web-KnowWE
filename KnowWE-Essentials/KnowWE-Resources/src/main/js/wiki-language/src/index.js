import {parseMixed} from "@lezer/common";
import {styleTags, tags as t} from "@lezer/highlight";
import {foldNodeProp, LanguageSupport, LRLanguage} from "@codemirror/language";
import {parser as wikiParser} from "./wiki.grammar";
import {parser as contentParser} from "./content.grammar";

/**
 * Registry of parsers for the bodies of KnowWE markup blocks, keyed by the markup name in lower case. Plugins
 * register the parser of their markup here before the editor is created.
 */
const registry = new Map();

export function registerLanguage(markupName, parser) {
    registry.set(markupName.toLowerCase(), parser);
}

export function unregisterLanguage(markupName) {
    registry.delete(markupName.toLowerCase());
}

export function getLanguage(markupName) {
    return registry.get(markupName.toLowerCase()) ?? null;
}

function markupName(open, input) {
    return input.read(open.from, open.to).trim().replace(/^%%/, "").replace(/\s*[:=]$/, "").trim();
}

/** Parser for the default body of a markup block: annotation lines and plain text. */
export const markupContentParser = contentParser.configure({
    props: [
        styleTags({
            AnnotationName: t.annotation,
            AnnotationValue: t.attributeValue,
        }),
    ],
});

/** Parser for JSPWiki markup, embedding registered parsers into the bodies of markup blocks. */
export const parser = wikiParser.configure({
    props: [
        styleTags({
            "Heading/...": t.heading,
            HeadingMark: t.processingInstruction,
            "Bold/...": t.strong,
            "Italic/...": t.emphasis,
            Monospace: t.monospace,
            Preformatted: t.monospace,
            Link: t.link,
            LinkContent: t.link,
            "LinkOpen LinkClose LinkSeparator": t.punctuation,
            Plugin: t.processingInstruction,
            Escape: t.escape,
            LineBreak: t.contentSeparator,
            HorizontalRule: t.contentSeparator,
            ListMark: t.list,
            DefinitionMark: t.list,
            TableMark: t.punctuation,
            MarkupOpen: t.keyword,
            MarkupClose: t.keyword,
        }),
        foldNodeProp.add({
            Markup(node) {
                const open = node.firstChild;
                const close = node.lastChild;
                if (!open || !close || close.name !== "MarkupClose") return null;
                return {from: open.to, to: close.from};
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
});

/** The CodeMirror language support for JSPWiki pages. */
export function wiki() {
    return new LanguageSupport(wikiLanguage);
}
