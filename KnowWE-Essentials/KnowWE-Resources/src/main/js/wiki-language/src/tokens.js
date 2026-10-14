import {ContextTracker, ExternalTokenizer} from "@lezer/lr";
import * as T from "./wiki.grammar.terms";
import {matchDeclaration, matchInlineDeclaration} from "./markups.js";

// The tokenizer follows JSPWikiMarkupParser, which reads the page character by character and toggles its elements
// without looking ahead. The context tracker keeps the open elements like the element stack of JSPWiki, so a closing
// mark first ends the elements nested in its element, like the popElement of JSPWiki. Line start marks only count in
// column zero. Links, plugins and preformatted text are scanned up to their end like JSPWiki does, but an opening
// bracket without an end is marked instead of turning the rest of the page into text.
//
// KnowWE claims its markups before JSPWiki sees the page, so a markup declaration line ends every construct around
// it except preformatted text, which KnowWE hides from markups.

const NEWLINE = 10, RETURN = 13, SPACE = 32, TAB = 9;
const EXCLAMATION = 33, QUOTE = 34, HASH = 35, DOLLAR = 36, PERCENT = 37, AMPERSAND = 38, APOSTROPHE = 39;
const OPEN_PAREN = 40, CLOSE_PAREN = 41, STAR = 42, DASH = 45, DOT = 46, SLASH = 47, COLON = 58, SEMICOLON = 59;
const EQUALS = 61, OPEN_BRACKET = 91, BACKSLASH = 92, CLOSE_BRACKET = 93, UNDERSCORE = 95, OPEN_BRACE = 123;
const PIPE = 124, CLOSE_BRACE = 125, TILDE = 126;

// characters that may start a token, plain text runs stop before them
const SPECIAL = new Set([NEWLINE, RETURN, UNDERSCORE, APOSTROPHE, OPEN_BRACE, CLOSE_BRACE, OPEN_BRACKET,
    CLOSE_BRACKET, TILDE, BACKSLASH, PERCENT, SLASH, AMPERSAND, COLON, PIPE]);

// characters that a tilde escapes together with their repetitions
const ESCAPABLE = new Set([PIPE, TILDE, BACKSLASH, STAR, HASH, DASH, EXCLAMATION, APOSTROPHE, UNDERSCORE,
    OPEN_BRACKET, OPEN_BRACE, CLOSE_BRACKET, CLOSE_BRACE, PERCENT]);

// the link targets JSPWiki treats as external, see LinkParsingOperations
const EXTERNAL = new RegExp("^(?:" + [
    "https?:", "ftp:", "mailto:", "news:", "file:", "rtsp:", "mms:", "ldap:", "gopher:", "nntp:", "telnet:", "wais:",
    "prospero:", "z39\\.50[sr]", "vemmi:", "imap:", "nfs:", "acap:", "tip:", "pop:", "dav:", "opaquelocktoken:",
    "sips?:", "tel:", "fax:", "modem:", "soap\\.beeps?", "xmlrpc\\.beeps?", "urn:", "go:", "h323:", "ipp:", "tftp:",
    "mupdate:", "pres:", "im:", "mtqp", "smb:",
].join("|") + ")");

// The context holds the kind of the current paragraph like unit, the stack of open elements and the bold and italic
// flags of JSPWiki. Lezer reuses a node of an earlier parse only where the context hash equals the one recorded at the
// end of the node. Every element ends with a mark or an end token, so the context at the end of a node is the one it
// started in, and a node is never reused where its marks would open or close differently.

// kinds of the current paragraph like unit, they decide whether a line break continues it and which marks end it
const PARAGRAPH = 0, LIST = 1, TABLE = 2, TERM = 3, OTHER = 4;

// types of the open elements, links, plugins and preformatted text are known to end when they open
const DIV = 1, STYLE = 2, MONOSPACE = 3, BOLD = 4, ITALIC = 5, LINK = 6, PLUGIN = 7, CODE = 8;
const END = [-1, T.divEnd, T.styleEnd, T.monospaceEnd, T.boldEnd, T.italicEnd];

// the deepest nesting of monospace and of styles, deeper nesting would exhaust the parse stack
const MAX_DEPTH = 48;

class Frame {
    constructor(type, parent) {
        this.type = type;
        this.parent = parent;
        this.monospace = (parent?.monospace ?? 0) + (type === MONOSPACE ? 1 : 0);
        this.styles = (parent?.styles ?? 0) + (type === STYLE || type === DIV ? 1 : 0);
        // a mixing hash, deep stacks of equal frames must not collide
        this.hash = Math.imul((parent?.hash ?? 0) ^ (type * 0x45d9f3b), 0x9e3779b1) ^ (parent ? parent.hash >>> 15 : 0);
        // equal stacks share their frames and contexts, so unchanged contexts stay identical
        this.children = [];
        this.contexts = [];
    }

    push(type) {
        return this.children[type] ??= new Frame(type, this);
    }
}

// bold or italic that is still on after the end of its element, and bold or italic that starts the next paragraph
const DANGLING_BOLD = 1, DANGLING_ITALIC = 2, PENDING_BOLD = 4, PENDING_ITALIC = 8, TAINTED = 16;
const DANGLING = DANGLING_BOLD | DANGLING_ITALIC, PENDING = PENDING_BOLD | PENDING_ITALIC;

class Context {
    constructor(kind, frame, flags) {
        this.kind = kind;
        this.frame = frame;
        this.flags = flags;
        // Lezer records the context at the end of a node and reuses the node where the current context has the same
        // hash. The flags carry over from one node into the following ones, so a node may start with them and end
        // without them, and would then be reused where they were never set. Once flags were set, the context is
        // tainted for the rest of the input and its hash is NaN, which equals no hash. Real hashes are not zero,
        // since nodes that record no hash count as zero.
        this.hash = flags ? NaN : (frame.hash ^ Math.imul(kind + 1, 0x27d4eb2d)) | 1;
    }
}

const ROOT = new Frame(0, null);

function context(kind, frame, flags = 0) {
    if (flags & (DANGLING | PENDING)) flags |= TAINTED;
    return frame.contexts[kind * 32 + flags] ??= new Context(kind, frame, flags);
}

/** Ends bold and italic, like JSPWiki at the start of a block level element. */
function startBlock(cx, kind) {
    return context(kind, cx.frame, cx.flags & TAINTED);
}

/**
 * Returns the context after a paragraph break, which ends bold and italic and continues them in the next paragraph,
 * unless the next line starts a block that cannot be inside of a paragraph.
 */
function paragraphBreak(cx, input, offset) {
    const restart = ((cx.flags & DANGLING) | ((cx.flags & PENDING) >> 2)) << 2;
    const ch = input.peek(offset);
    const block = ch === STAR || ch === HASH || ch === EXCLAMATION || ch === SEMICOLON || ch === PIPE
        || matches(input, offset, "{{{") || matches(input, offset, "----") || matches(input, offset, "%%")
        || (isBlank(ch) && declarationAt(input, offset));
    return context(lineKind(input, offset), cx.frame, (block ? 0 : restart) | (cx.flags & TAINTED));
}

/** Returns the innermost open element of one of the given types, inline elements are searched above blocks only. */
function openElement(cx, types) {
    for (let frame = cx.frame; frame !== ROOT; frame = frame.parent) {
        if (types.includes(frame.type)) return frame;
        if (frame.type === DIV) return null;
    }
    return null;
}

/** Returns the innermost element if it is inline, these end with their paragraph like unit. */
function openInline(cx) {
    const type = cx.frame.type;
    return type >= STYLE && type <= ITALIC ? cx.frame : null;
}

/** Returns the kind of the unit that the line at the offset starts after a line break. */
function lineKind(input, offset) {
    const ch = input.peek(offset);
    if ((ch === PERCENT || isBlank(ch)) && declarationAt(input, offset)) return OTHER;
    switch (ch) {
        case EXCLAMATION:
            return OTHER;
        case STAR:
        case HASH:
            return LIST;
        case PIPE:
            return TABLE;
        case SEMICOLON:
            return TERM;
        default:
            return PARAGRAPH;
    }
}

/**
 * Tracks the kind of the paragraph like unit and the open elements. Line breaks continue paragraphs on every line
 * that does not start a block, and list items on lines starting with a space.
 */
export const lineContext = new ContextTracker({
    start: context(PARAGRAPH, ROOT),
    shift(cx, term, stack, input) {
        switch (term) {
            case T.newline: {
                const next = afterLineBreak(input, 0);
                if (atLineStart(input)) return paragraphBreak(cx, input, next);
                const kind = lineKind(input, next);
                // a table starts like a block, its rows and cells keep bold and italic on
                return kind === TABLE ? startBlock(cx, TABLE) : context(kind, cx.frame, cx.flags);
            }
            case T.HorizontalRule:
                return startBlock(cx, PARAGRAPH);
            case T.PreOpen:
                return context(PARAGRAPH, cx.frame.push(CODE), cx.flags & TAINTED);
            case T.ListMark:
                return startBlock(cx, LIST);
            case T.itemBreak:
                return context(LIST, cx.frame, cx.flags);
            case T.CellMark:
            case T.HeaderMark:
            case T.rowBreak:
                return context(TABLE, cx.frame, cx.flags);
            case T.DefinitionMark:
                return startBlock(cx, TERM);
            case T.MarkupOpen:
            case T.LineMarkupOpen:
                return startBlock(cx, OTHER);
            case T.HeadingMark:
            case T.DescriptionMark:
                return context(OTHER, cx.frame, cx.flags);
            case T.DivOpen:
                return context(PARAGRAPH, cx.frame.push(DIV), cx.flags & TAINTED);
            case T.StyleOpen:
                return context(cx.kind, cx.frame.push(STYLE), cx.flags);
            case T.MonospaceOpen:
                return context(cx.kind, cx.frame.push(MONOSPACE), cx.flags);
            case T.BoldOpen:
                return context(cx.kind, cx.frame.push(BOLD), cx.flags);
            case T.ItalicOpen:
                return context(cx.kind, cx.frame.push(ITALIC), cx.flags);
            case T.boldRestart:
                return context(cx.kind, cx.frame.push(BOLD), cx.flags & ~PENDING_BOLD);
            case T.italicRestart:
                return context(cx.kind, cx.frame.push(ITALIC), cx.flags & ~PENDING_ITALIC);
            case T.BoldToggle:
                return context(cx.kind, cx.frame, cx.flags & ~DANGLING_BOLD);
            case T.ItalicToggle:
                return context(cx.kind, cx.frame, cx.flags & ~DANGLING_ITALIC);
            case T.boldEnd:
                return context(cx.kind, cx.frame.parent, cx.flags | DANGLING_BOLD);
            case T.italicEnd:
                return context(cx.kind, cx.frame.parent, cx.flags | DANGLING_ITALIC);
            case T.LinkOpen:
            case T.FootnoteOpen:
            case T.FootnoteReferenceOpen:
                return context(cx.kind, cx.frame.push(LINK), cx.flags);
            case T.PluginOpen:
            case T.VariableOpen:
            case T.AccessRuleOpen:
            case T.MetadataOpen:
                return context(cx.kind, cx.frame.push(PLUGIN), cx.flags);
            case T.CodeOpen:
                return context(cx.kind, cx.frame.push(CODE), cx.flags);
            case T.StyleClose:
            case T.MonospaceClose:
            case T.BoldClose:
            case T.ItalicClose:
            case T.divEnd:
            case T.styleEnd:
            case T.monospaceEnd:
            case T.LinkClose:
            case T.PluginClose:
            case T.CodeClose:
                // the text behind a closed style block is like a paragraph
                return context(cx.frame.type === DIV ? PARAGRAPH : cx.kind, cx.frame.parent, cx.flags);
            default:
                return cx;
        }
    },
    hash: cx => cx.hash,
});

function isBlank(ch) {
    return ch === SPACE || ch === TAB;
}

function isLineEnd(ch) {
    return ch === NEWLINE || ch === RETURN || ch < 0;
}

function isWordChar(ch) {
    return (ch >= 48 && ch <= 57) || (ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122) || ch === UNDERSCORE
        || ch >= 0xc0;
}

function isLetter(ch) {
    if ((ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122)) return true;
    return ch >= 0xc0 && /\p{L}/u.test(String.fromCharCode(ch));
}

function atLineStart(input, offset = 0) {
    return input.pos + offset === 0 || input.peek(offset - 1) === NEWLINE;
}

function matches(input, offset, string) {
    for (let i = 0; i < string.length; i++) {
        if (input.peek(offset + i) !== string.charCodeAt(i)) return false;
    }
    return true;
}

function countWhile(input, offset, predicate) {
    let i = offset;
    while (predicate(input.peek(i))) i++;
    return i - offset;
}

function lineText(input, offset) {
    let text = "";
    for (let i = offset; text.length < 4096; i++) {
        const ch = input.peek(i);
        if (isLineEnd(ch)) break;
        text += String.fromCharCode(ch);
    }
    return text;
}

/** Returns the offset of the next line start after the line break at the given offset. */
function afterLineBreak(input, offset) {
    return input.peek(offset) === RETURN && input.peek(offset + 1) === NEWLINE ? offset + 2 : offset + 1;
}

/** Returns the markup declaration of the line at the given offset, or null. */
function declarationAt(input, offset) {
    const indent = countWhile(input, offset, isBlank);
    if (input.peek(offset + indent) !== PERCENT || input.peek(offset + indent + 1) !== PERCENT) return null;
    return matchDeclaration(lineText(input, offset));
}

/** Returns the length of a markup closing line "/%" or "%" at the given offset, without the line break, or -1. */
function closingLineLength(input, offset) {
    let i = offset + countWhile(input, offset, isBlank);
    if (input.peek(i) === SLASH) i++;
    if (input.peek(i) !== PERCENT) return -1;
    i++;
    i += countWhile(input, i, isBlank);
    return isLineEnd(input.peek(i)) ? i - offset : -1;
}

/**
 * Returns the length of the class and style of a JSPWiki style, starting after "%%" or "/%". The class runs up to a
 * blank, line end or opening parenthesis and a backslash escapes the next character. The style is in balanced
 * parentheses and may span lines.
 */
function styleSpecLength(input, offset) {
    let i = offset;
    if (isLetter(input.peek(i))) i += classLength(input, i);
    if (input.peek(i) === OPEN_PAREN) i += declarationLength(input, i);
    return i - offset;
}

function classLength(input, offset) {
    let i = offset;
    for (;;) {
        const ch = input.peek(i);
        if (ch === BACKSLASH && !isLineEnd(input.peek(i + 1))) {
            i += 2;
        } else if (ch === OPEN_PAREN || isBlank(ch) || isLineEnd(ch)) {
            return i - offset;
        } else {
            i++;
        }
    }
}

function declarationLength(input, offset) {
    let depth = 0, i = offset;
    for (;;) {
        const ch = input.peek(i);
        if (ch < 0) return i - offset;
        i++;
        if (ch === OPEN_PAREN) depth++;
        else if (ch === CLOSE_PAREN && --depth === 0) return i - offset;
    }
}

/** Returns whether a style opening at the given offset is a block, since nothing follows its class and style. */
function isStyleBlock(input, offset) {
    let i = offset + 2;
    i += styleSpecLength(input, i);
    i += countWhile(input, i, isBlank);
    return isLineEnd(input.peek(i));
}

function opensStyle(input, offset) {
    const ch = input.peek(offset);
    return ch === OPEN_PAREN || isLetter(ch);
}

/** Returns the offset of the "}}}" that ends preformatted text, or -1. A tilde in front escapes it. */
function codeEnd(input, offset) {
    for (let i = offset; ; i++) {
        const ch = input.peek(i);
        if (ch < 0) return -1;
        if (ch === TILDE && matches(input, i + 1, "}}}")) i += 3;
        else if (ch === CLOSE_BRACE && matches(input, i + 1, "}}")) return i;
    }
}

/**
 * Scans to the given end of a bracket construct like JSPWiki, across line breaks. Returns the offset after the end,
 * or -1 if the input ends or a markup declaration line comes first.
 */
function scanBracket(input, offset, plugin) {
    let depth = 1;
    for (let i = offset; ;) {
        const ch = input.peek(i);
        if (ch < 0) return -1;
        if (ch === NEWLINE) {
            i++;
            if (declarationAt(input, i)) return -1;
        } else if (!plugin) {
            if (ch === CLOSE_BRACKET) return i + 1;
            i++;
        } else if (ch === OPEN_BRACKET && input.peek(i + 1) === OPEN_BRACE) {
            depth++;
            i += 2;
        } else if (ch === CLOSE_BRACE && input.peek(i + 1) === CLOSE_BRACKET) {
            i += 2;
            if (--depth === 0) return i;
        } else {
            i++;
        }
    }
}

// line kinds of the line after a line break
const EMPTY = 0, BLOCK = 1, INDENTED = 2, PLAIN = 3, LIST_LINE = 4, TABLE_LINE = 5;

function nextLineKind(input, offset) {
    const ch = input.peek(offset);
    if (ch < 0 || ch === NEWLINE || (ch === RETURN && input.peek(offset + 1) === NEWLINE)) return EMPTY;
    if ((ch === PERCENT || isBlank(ch)) && declarationAt(input, offset)) return BLOCK;
    switch (ch) {
        case SPACE:
            return INDENTED;
        case STAR:
        case HASH:
            return LIST_LINE;
        case PIPE:
            return TABLE_LINE;
        case EXCLAMATION:
        case SEMICOLON:
            return BLOCK;
        case DASH:
            return matches(input, offset, "----") ? BLOCK : PLAIN;
        case OPEN_BRACE:
            return matches(input, offset, "{{{") ? BLOCK : PLAIN;
        case PERCENT:
            return input.peek(offset + 1) === PERCENT && opensStyle(input, offset + 2) && isStyleBlock(input, offset)
                ? BLOCK : PLAIN;
        default:
            return PLAIN;
    }
}

function lineBreak(input, stack) {
    const length = input.next === RETURN ? 2 : 1, cx = stack.context;
    let term = T.newline;
    // a line break on an empty line separates paragraphs
    if (!atLineStart(input)) {
        const next = nextLineKind(input, length);
        // open inline elements end first, the break is checked again after them
        const inline = openInline(cx) !== null;
        if (next === LIST_LINE && cx.kind === LIST && (inline || stack.canShift(T.itemBreak))) {
            term = T.itemBreak;
        } else if (next === TABLE_LINE && cx.kind === TABLE && (inline || stack.canShift(T.rowBreak))) {
            term = T.rowBreak;
        } else if (next === INDENTED || (next === PLAIN && cx.kind === PARAGRAPH)) {
            // paragraphs continue on plain and indented lines, list items on indented lines only
            if ((cx.kind === PARAGRAPH || cx.kind === LIST) && stack.canShift(T.softBreak)) {
                return input.acceptToken(T.softBreak, length);
            }
        }
    }
    // the unit ends, and with it its inline elements
    const element = openInline(cx);
    if (element) return input.acceptToken(END[element.type], 0);
    input.acceptToken(term, length);
}

/**
 * Accepts the closing mark of the given element, or the end token of the innermost element if that is nested in it,
 * so that every element in between is closed first like the popElement of JSPWiki.
 */
function close(input, stack, element, term, length) {
    const top = stack.context.frame;
    if (top !== element) return input.acceptToken(END[top.type], 0);
    input.acceptToken(stack.canShift(term) ? term : T.text, length);
}

function mayNest(stack, monospace) {
    const frame = stack.context.frame;
    return (monospace ? frame.monospace : frame.styles) < MAX_DEPTH;
}

/** Produces the tokens that are only valid in column zero. Returns whether a token was accepted. */
function lineStart(input, stack) {
    const ch = input.next, flags = stack.context.flags;
    // bold and italic continue in a paragraph after a paragraph break, italic outside
    if (flags & PENDING && !isLineEnd(ch) && !declarationAt(input, 0)) {
        const restart = flags & PENDING_ITALIC ? T.italicRestart : T.boldRestart;
        if (stack.canShift(restart)) return input.acceptToken(restart, 0), true;
    }
    if ((ch === PERCENT || isBlank(ch)) && stack.canShift(T.MarkupOpen)) {
        const declaration = declarationAt(input, 0);
        if (declaration) {
            input.acceptToken(declaration.block ? T.MarkupOpen : T.LineMarkupOpen, declaration.length);
            return true;
        }
    }
    let term = -1, length = 0;
    switch (ch) {
        case EXCLAMATION:
            term = T.HeadingMark;
            length = Math.min(3, countWhile(input, 0, c => c === EXCLAMATION));
            break;
        case STAR:
        case HASH:
            term = T.ListMark;
            length = countWhile(input, 0, c => c === STAR || c === HASH);
            length += countWhile(input, length, c => c === SPACE);
            break;
        case SEMICOLON:
            term = T.DefinitionMark;
            length = 1;
            break;
        case DASH:
            length = countWhile(input, 0, c => c === DASH);
            if (length >= 4) term = T.HorizontalRule;
            break;
        case PIPE:
            term = input.peek(1) === PIPE ? T.HeaderMark : T.CellMark;
            length = term === T.HeaderMark ? 2 : 1;
            break;
        case OPEN_BRACE:
            if (matches(input, 0, "{{{") && codeEnd(input, 3) >= 0) {
                term = T.PreOpen;
                length = 3;
            }
            break;
        case PERCENT:
            if (input.peek(1) === PERCENT && opensStyle(input, 2) && isStyleBlock(input, 0) && mayNest(stack, false)) {
                term = T.DivOpen;
                length = 2;
            }
            break;
    }
    if (term < 0 || !stack.canShift(term)) return false;
    input.acceptToken(term, length);
    return true;
}

function markupContent(input, stack) {
    let length = 0;
    if (stack.canShift(T.blockContent)) {
        // the rest of the declaration line and all lines up to the closing line or the end of the input
        for (;;) {
            let ch = input.peek(length);
            while (!isLineEnd(ch)) ch = input.peek(++length);
            if (ch < 0) break;
            length = afterLineBreak(input, length);
            if (input.peek(length) < 0 || closingLineLength(input, length) >= 0) break;
        }
        if (length > 0) input.acceptToken(T.blockContent, length);
    } else if (stack.canShift(T.lineContent)) {
        while (!isLineEnd(input.peek(length))) length++;
        if (length > 0) input.acceptToken(T.lineContent, length);
    } else {
        length = inlineMarkupEnd(input, 0);
        if (length > 0) input.acceptToken(T.inlineContent, length);
        else input.acceptToken(T.InlineMarkupClose, 2);
    }
}

/** Returns the offset of the "%%" or "/%" that closes an inline markup, or -1. */
function inlineMarkupEnd(input, offset) {
    for (let i = offset; ; i++) {
        const ch = input.peek(i);
        if (ch < 0) return -1;
        if ((ch === PERCENT || ch === SLASH) && input.peek(i + 1) === PERCENT) return i;
        if (ch === NEWLINE && declarationAt(input, i + 1)) return -1;
    }
}

function code(input) {
    const end = codeEnd(input, 0);
    if (end > 0) input.acceptToken(T.CodeText, end);
    else input.acceptToken(T.CodeClose, 3);
}

function link(input, stack) {
    const ch = input.next;
    if (ch === CLOSE_BRACKET) return input.acceptToken(T.LinkClose, 1);
    if (ch === PIPE && stack.canShift(T.LinkSeparator)) return input.acceptToken(T.LinkSeparator, 1);
    if (stack.canShift(T.attributeText) || stack.canShift(T.attributeEquals)) {
        if (isWordChar(ch) && stack.canShift(T.AttributeName)) {
            const name = countWhile(input, 0, isWordChar);
            const blanks = countWhile(input, name, isBlank);
            if (input.peek(name + blanks) === EQUALS) return input.acceptToken(T.AttributeName, name);
        }
        if (stack.canShift(T.attributeEquals)) {
            const blanks = countWhile(input, 0, isBlank);
            if (input.peek(blanks) === EQUALS) {
                return input.acceptToken(T.attributeEquals, blanks + 1 + countWhile(input, blanks + 1, isBlank));
            }
        }
        if (ch === APOSTROPHE && stack.canShift(T.AttributeValue)) {
            let i = 1;
            while (input.peek(i) !== APOSTROPHE && input.peek(i) !== CLOSE_BRACKET) i++;
            return input.acceptToken(T.AttributeValue, input.peek(i) === APOSTROPHE ? i + 1 : i);
        }
        const length = Math.max(1, countWhile(input, 0, c => c !== CLOSE_BRACKET && c !== APOSTROPHE && c !== EQUALS
            && !isWordChar(c)));
        return input.acceptToken(T.attributeText, length);
    }
    let length = 0, text = "";
    for (let c = ch; c !== PIPE && c !== CLOSE_BRACKET; c = input.peek(++length)) text += String.fromCharCode(c);
    if (input.peek(length) === PIPE && stack.canShift(T.LinkText)) return input.acceptToken(T.LinkText, length);
    input.acceptToken(EXTERNAL.test(text.trim()) ? T.URL : T.LinkTarget, length);
}

/** Accepts the opening of a link, footnote or footnote reference at "[", if its end follows. */
function openLink(input) {
    const end = scanBracket(input, 1, false);
    if (end < 0) return input.acceptToken(T.Unclosed, 1);
    let content = "";
    for (let i = 1; i < end - 1; i++) content += String.fromCharCode(input.peek(i));
    const parts = content.split("|");
    const target = parts.length === 1 ? parts[0] : parts[1].trim();
    if (target.startsWith("#")) return input.acceptToken(T.FootnoteOpen, 1);
    if (/^-?\d+$/.test(target)) return input.acceptToken(T.FootnoteReferenceOpen, 1);
    input.acceptToken(T.LinkOpen, 1);
}

/** Accepts the opening of a plugin, variable, access rule or metadata at "[{", if its end follows. */
function openPlugin(input, unclosed) {
    if (scanBracket(input, 2, true) < 0) return input.acceptToken(unclosed, 2);
    if (input.peek(2) === DOLLAR) return input.acceptToken(T.VariableOpen, 3);
    if (matches(input, 2, "ALLOW") || matches(input, 2, "DENY")) return input.acceptToken(T.AccessRuleOpen, 2);
    if (matches(input, 2, "SET")) return input.acceptToken(T.MetadataOpen, 2);
    input.acceptToken(T.PluginOpen, 2);
}

/** Returns the length of a quoted parameter value, which nested plugins do not end, unlike the end of its plugin. */
function quotedLength(input, quote) {
    let depth = 0;
    for (let i = 1; ; i++) {
        const ch = input.peek(i);
        if (ch < 0) return i;
        if (ch === BACKSLASH && input.peek(i + 1) >= 0) {
            i++;
        } else if (ch === OPEN_BRACKET && input.peek(i + 1) === OPEN_BRACE) {
            depth++;
            i++;
        } else if (ch === CLOSE_BRACE && input.peek(i + 1) === CLOSE_BRACKET) {
            if (depth === 0) return i;
            depth--;
            i++;
        } else if (ch === quote && depth === 0) {
            return i + 1;
        }
    }
}

function atPluginEnd(input, offset) {
    return input.peek(offset) === CLOSE_BRACE && input.peek(offset + 1) === CLOSE_BRACKET;
}

function atPluginStart(input, offset) {
    return input.peek(offset) === OPEN_BRACKET && input.peek(offset + 1) === OPEN_BRACE;
}

/** Returns whether a plugin or a nested plugin ends or starts at the offset. */
function atPluginBracket(input, offset) {
    return atPluginEnd(input, offset) || atPluginStart(input, offset);
}

/** Returns the length of a run up to a blank, a line end or a plugin bracket. */
function wordLength(input, offset) {
    let i = offset;
    while (!isBlank(input.peek(i)) && !isLineEnd(input.peek(i)) && !atPluginBracket(input, i)) i++;
    return i - offset;
}

function plugin(input, stack) {
    const ch = input.next;
    if (atPluginEnd(input, 0)) return input.acceptToken(T.PluginClose, 2);
    if (atPluginStart(input, 0)) return openPlugin(input, stack.canShift(T.bodyText) ? T.bodyText : T.pluginText);
    if (stack.canShift(T.bodyText)) {
        let i = 0;
        while (input.peek(i) >= 0 && !atPluginEnd(input, i) && !atPluginStart(input, i)) i++;
        return input.acceptToken(T.bodyText, i);
    }
    if (ch === NEWLINE || ch === RETURN) {
        // the plugin body starts after the first line that holds nothing but blanks
        if (stack.canShift(T.bodyStart)) {
            let i = afterLineBreak(input, 0), blankLines = 0;
            for (;;) {
                const blanks = countWhile(input, i, isBlank);
                const end = input.peek(i + blanks);
                if (end !== NEWLINE && end !== RETURN) break;
                i = afterLineBreak(input, i + blanks);
                blankLines++;
            }
            if (blankLines > 0) return input.acceptToken(T.bodyStart, i);
        }
        return input.acceptToken(T.pluginSpace, afterLineBreak(input, 0));
    }
    if (isBlank(ch)) return input.acceptToken(T.pluginSpace, countWhile(input, 0, isBlank));
    if (stack.canShift(T.VariableName)) return input.acceptToken(T.VariableName, wordLength(input, 0));
    if (stack.canShift(T.AccessKeyword)) return input.acceptToken(T.AccessKeyword, matches(input, 0, "DENY") ? 4 : 5);
    if (stack.canShift(T.SetKeyword)) return input.acceptToken(T.SetKeyword, 3);
    if (stack.canShift(T.Permission)) return input.acceptToken(T.Permission, wordLength(input, 0));
    if (stack.canShift(T.Principal)) {
        if (ch === 44) return input.acceptToken(T.pluginText, 1);
        let i = 0, end = 0;
        for (let c = ch; c !== 44 && !isLineEnd(c) && !atPluginBracket(input, i); c = input.peek(++i)) {
            if (!isBlank(c)) end = i + 1;
        }
        return input.acceptToken(T.Principal, end);
    }
    if (stack.canShift(T.MetadataName)) {
        let i = 0, end = 0;
        for (let c = ch; c !== EQUALS && c >= 0 && !atPluginBracket(input, i); c = input.peek(++i)) {
            if (!isBlank(c) && !isLineEnd(c)) end = i + 1;
        }
        if (input.peek(i) === EQUALS && end > 0) return input.acceptToken(T.MetadataName, end);
        return input.acceptToken(T.pluginText, wordLength(input, 0));
    }
    if (stack.canShift(T.MetadataValue)) {
        let i = 0, end = 0;
        for (let c = ch; c >= 0 && !atPluginBracket(input, i); c = input.peek(++i)) {
            if (!isBlank(c) && !isLineEnd(c)) end = i + 1;
        }
        return input.acceptToken(T.MetadataValue, end);
    }
    if (ch === EQUALS && stack.canShift(T.parameterEquals)) return input.acceptToken(T.parameterEquals, 1);
    if (stack.canShift(T.ParameterValue)) {
        if (ch === APOSTROPHE || ch === QUOTE) return input.acceptToken(T.ParameterValue, quotedLength(input, ch));
        return input.acceptToken(T.ParameterValue, wordLength(input, 0));
    }
    if (isWordChar(ch)) {
        const word = countWhile(input, 0, c => isWordChar(c) || c === DOT);
        if (stack.canShift(T.PluginName)) {
            if (word === 6 && matches(input, 0, "INSERT") && stack.canShift(T.InsertKeyword)) {
                return input.acceptToken(T.InsertKeyword, 6);
            }
            return input.acceptToken(T.PluginName, word);
        }
        if (word === 5 && matches(input, 0, "WHERE") && stack.canShift(T.WhereKeyword)) {
            return input.acceptToken(T.WhereKeyword, 5);
        }
        const name = countWhile(input, 0, c => isWordChar(c) || c === DOT || c === DASH);
        if (input.peek(name + countWhile(input, name, isBlank)) === EQUALS && stack.canShift(T.ParameterName)) {
            return input.acceptToken(T.ParameterName, name);
        }
        return input.acceptToken(T.pluginText, word);
    }
    input.acceptToken(T.pluginText, 1);
}

function styleSpec(input, stack) {
    const ch = input.next;
    if (isLetter(ch) && stack.canShift(T.StyleClass)) {
        return input.acceptToken(T.StyleClass, classLength(input, 0)), true;
    }
    if (ch === OPEN_PAREN && stack.canShift(T.StyleDeclaration)) {
        return input.acceptToken(T.StyleDeclaration, declarationLength(input, 0)), true;
    }
    if (isBlank(ch) && stack.canShift(T.styleBlank)) {
        const length = countWhile(input, 0, isBlank);
        if (isLineEnd(input.peek(length))) return input.acceptToken(T.styleBlank, length), true;
    }
    return false;
}

function escape(input) {
    const next = input.peek(1);
    if (next === SPACE) return input.acceptToken(T.Escape, 2);
    if (ESCAPABLE.has(next)) return input.acceptToken(T.Escape, 1 + countWhile(input, 1, c => c === next));
    input.acceptToken(T.text, 1);
}

/** Accepts a mark that ends the current table cell or definition term, after the end of its inline elements. */
function unitMark(input, stack, term, length) {
    const element = openInline(stack.context);
    if (element) return input.acceptToken(END[element.type], 0);
    input.acceptToken(stack.canShift(term) ? term : T.text, length);
}

function inline(input, stack) {
    const ch = input.next, next = input.peek(1);
    switch (ch) {
        case UNDERSCORE:
            if (next === UNDERSCORE) {
                const bold = openElement(stack.context, [BOLD]);
                if (bold) return close(input, stack, bold, T.BoldClose, 2);
                return input.acceptToken(stack.context.flags & DANGLING_BOLD ? T.BoldToggle : T.BoldOpen, 2);
            }
            break;
        case APOSTROPHE:
            if (next === APOSTROPHE) {
                const italic = openElement(stack.context, [ITALIC]);
                if (italic) return close(input, stack, italic, T.ItalicClose, 2);
                return input.acceptToken(stack.context.flags & DANGLING_ITALIC ? T.ItalicToggle : T.ItalicOpen, 2);
            }
            break;
        case OPEN_BRACE:
            if (next === OPEN_BRACE) {
                if (input.peek(2) !== OPEN_BRACE) {
                    return input.acceptToken(mayNest(stack, true) ? T.MonospaceOpen : T.text, 2);
                }
                return input.acceptToken(codeEnd(input, 3) >= 0 ? T.CodeOpen : T.Unclosed, 3);
            }
            break;
        case CLOSE_BRACE:
            if (next === CLOSE_BRACE) {
                if (input.peek(2) === CLOSE_BRACE) return input.acceptToken(T.text, 3);
                const monospace = openElement(stack.context, [MONOSPACE]);
                return monospace ? close(input, stack, monospace, T.MonospaceClose, 2) : input.acceptToken(T.text, 2);
            }
            break;
        case OPEN_BRACKET:
            if (next === OPEN_BRACKET) {
                return input.acceptToken(T.Escape, countWhile(input, 0, c => c === OPEN_BRACKET));
            }
            if (next === OPEN_BRACE) return openPlugin(input, T.Unclosed);
            return openLink(input);
        case TILDE:
            return escape(input);
        case BACKSLASH:
            if (next === BACKSLASH) return input.acceptToken(T.LineBreak, input.peek(2) === BACKSLASH ? 3 : 2);
            break;
        case PERCENT:
            if (next === PERCENT) {
                const inlineMarkup = matchInlineDeclaration(lineText(input, 0));
                if (inlineMarkup > 0 && inlineMarkupEnd(input, inlineMarkup) >= 0) {
                    return input.acceptToken(T.InlineMarkupOpen, inlineMarkup);
                }
                if (opensStyle(input, 2)) return input.acceptToken(mayNest(stack, false) ? T.StyleOpen : T.text, 2);
                const style = openElement(stack.context, [STYLE, DIV]);
                return style ? close(input, stack, style, T.StyleClose, 2) : input.acceptToken(T.StrayStyleClose, 2);
            }
            break;
        case SLASH:
            if (next === PERCENT) {
                const style = openElement(stack.context, [STYLE, DIV]);
                if (!style) break;
                // like in JSPWiki, "/%" directly followed by a class or style opens a style instead
                if (opensStyle(input, 2) && mayNest(stack, false)) return input.acceptToken(T.StyleOpen, 2);
                return close(input, stack, style, T.StyleClose, 2);
            }
            break;
        case AMPERSAND: {
            const length = countWhile(input, 1, c => isWordChar(c) || c === HASH);
            if (length > 0 && input.peek(1 + length) === SEMICOLON) return input.acceptToken(T.Entity, length + 2);
            break;
        }
        case COLON:
            if (stack.context.kind === TERM) return unitMark(input, stack, T.DescriptionMark, 1);
            break;
        case PIPE:
            if (stack.context.kind === TABLE) {
                return next === PIPE ? unitMark(input, stack, T.HeaderMark, 2) : unitMark(input, stack, T.CellMark, 1);
            }
            break;
        default: {
            let length = 1;
            while (!SPECIAL.has(input.peek(length)) && input.peek(length) >= 0) length++;
            return input.acceptToken(T.text, length);
        }
    }
    input.acceptToken(T.text, 1);
}

/** Produces the tokens of markup bodies and of the class of style blocks. Returns whether a token was accepted. */
function raw(input, stack) {
    if (stack.canShift(T.blockContent) || stack.canShift(T.lineContent) || stack.canShift(T.InlineMarkupClose)) {
        markupContent(input, stack);
    } else if (stack.canShift(T.MarkupClose)) {
        const length = closingLineLength(input, 0);
        if (length < 0) return false;
        input.acceptToken(T.MarkupClose, length);
    } else {
        return styleSpec(input, stack);
    }
    return true;
}

export const wikiTokens = new ExternalTokenizer((input, stack) => {
    if (input.next < 0) {
        // the end of the input ends all open elements
        const top = stack.context.frame;
        if (top !== ROOT) input.acceptToken(END[top.type], 0);
        return;
    }
    switch (stack.context.frame.type) {
        case LINK:
            return link(input, stack);
        case PLUGIN:
            return plugin(input, stack);
        case CODE:
            return code(input);
    }
    // plain text is valid everywhere except in markup bodies and the classes of style blocks
    if (!stack.canShift(T.text) && raw(input, stack)) return;
    // the class and style of an inline style follow its opening mark, where text is valid too
    const ch = input.next;
    if ((ch === OPEN_PAREN || (input.peek(-1) === PERCENT && isLetter(ch))) && styleSpec(input, stack)) return;
    if (atLineStart(input) && lineStart(input, stack)) return;
    if (input.next === NEWLINE || (input.next === RETURN && input.peek(1) === NEWLINE)) return lineBreak(input, stack);
    inline(input, stack);
}, {contextual: true});

