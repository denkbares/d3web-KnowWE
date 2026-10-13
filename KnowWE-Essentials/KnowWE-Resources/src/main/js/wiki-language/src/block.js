import {Facet} from "@codemirror/state";
import {IterMode, NodeProp} from "@lezer/common";
import {syntaxTree} from "@codemirror/language";
import {parser as contentParser} from "./content.grammar";
import {getMarkup, markupName, pageCompletions} from "./registry.js";

/** The title of the article the edited text belongs to, null if unknown. */
export const articleTitle = Facet.define({combine: values => values.length ? values[0] : null});

// reads the annotations of a default markup body, with positions relative to the body
function annotations(body, text) {
    const result = [];
    if (!body || body.type.name !== "MarkupBody") body = contentParser.parse(text);
    for (let node = body.topNode.firstChild; node; node = node.nextSibling) {
        if (node.name !== "Annotation") continue;
        const name = node.getChild("AnnotationName");
        const value = node.getChild("AnnotationValue");
        result.push({
            name: text.slice(name.from, name.to).replace(/^@/, "").replace(/[\s:=]*$/, ""),
            value: value ? text.slice(value.from, value.to) : "",
            from: node.from,
            to: node.to,
            valueFrom: value ? value.from : node.to,
            valueTo: value ? value.to : node.to,
        });
    }
    return result;
}

/**
 * Describes the markup block whose body contains the position, or returns null. The description holds the markup
 * `name` as declared, the body `text` with its document range `from` and `to`, the `annotations` of the body (name,
 * value and the ranges `from`, `to`, `valueFrom`, `valueTo`), the Lezer `tree` of the body if the markup registered
 * a parser, and the `article` title of the editor. Positions of annotations and tree are relative to the body.
 */
export function markupBlock(state, pos) {
    let markup = syntaxTree(state).resolveInner(pos, -1);
    while (markup && markup.name !== "Markup" && markup.name !== "InlineMarkup") markup = markup.parent;
    if (!markup) return null;

    // the body node hosts the embedded tree, so it is only visible without entering mounted trees
    const cursor = markup.cursor(IterMode.IgnoreMounts);
    if (!cursor.firstChild() || cursor.name !== "MarkupOpen") return null;
    const name = markupName(cursor, {read: (from, to) => state.sliceDoc(from, to)});
    let from = cursor.to, to = markup.to, mounted = null;
    while (cursor.nextSibling()) {
        if (cursor.name === "MarkupContent") {
            from = cursor.from;
            to = cursor.to;
            mounted = cursor.tree?.prop(NodeProp.mounted)?.tree ?? null;
        } else if (cursor.name === "MarkupClose") {
            to = Math.min(to, cursor.from);
        }
    }
    if (pos < from || pos > to) return null;

    const text = state.sliceDoc(from, to);
    const parser = getMarkup(name)?.parser;
    return {
        name,
        text,
        from,
        to,
        annotations: annotations(mounted, text),
        tree: parser && mounted ? mounted : null,
        article: state.facet(articleTitle),
    };
}

// moves the positions of a completion result by the offset, from the body into the document
function shiftResult(result, offset) {
    if (!result) return null;
    // a map function of the plugin would receive document changes, so the result is rebuilt on every change instead
    const {map, ...shifted} = result;
    shifted.from = result.from + offset;
    if (result.to != null) shifted.to = result.to + offset;
    if (typeof result.validFor === "function") {
        const validFor = result.validFor;
        shifted.validFor = (text, from, to, state) => validFor(text, from - offset, to - offset, state);
    }
    return shifted;
}

function anchorAtEnd(expr) {
    return expr.source.endsWith("$") ? expr : new RegExp("(?:" + expr.source + ")$", expr.flags);
}

/**
 * Completion source that offers the completions of the markup at the caret. It hands the markup block and a body
 * relative context to the `complete` function the markup registered and moves the result into the document.
 */
export function completeMarkup(context) {
    const block = markupBlock(context.state, context.pos);
    if (!block) return null;
    const complete = getMarkup(block.name)?.complete;
    if (!complete) return null;

    const offset = block.from;
    const blockContext = {
        pos: context.pos - offset,
        explicit: context.explicit,
        get aborted() {
            return context.aborted;
        },
        addEventListener: (type, listener, options) => context.addEventListener(type, listener, options),
        // like CompletionContext.matchBefore, but the text before the body never matches
        matchBefore(expr) {
            const line = context.state.doc.lineAt(context.pos);
            const start = Math.max(line.from, offset, context.pos - 250);
            const text = context.state.sliceDoc(start, context.pos);
            const found = text.search(anchorAtEnd(expr));
            return found < 0 ? null : {from: start + found - offset, to: context.pos - offset, text: text.slice(found)};
        },
    };

    const result = complete(block, blockContext);
    if (result && typeof result.then === "function") {
        return result.then(resolved => context.aborted ? null : shiftResult(resolved, offset));
    }
    return shiftResult(result, offset);
}

// whether the markup at the position offers its own completions, which replace those of the page
function markupCompletes(state, pos) {
    const block = markupBlock(state, pos);
    return Boolean(block && getMarkup(block.name)?.complete);
}

/**
 * Returns the completion sources of an editor, the markup completion first and then the registered page completions,
 * which stay silent where a markup offers its own.
 */
export function completionSources() {
    const page = pageCompletions()
        .map(source => context => markupCompletes(context.state, context.pos) ? null : source(context));
    return [completeMarkup, ...page];
}
