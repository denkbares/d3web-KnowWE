import {ExternalTokenizer} from "@lezer/lr";
import {
    MarkupOpen, MarkupContent, MarkupClose, HeadingMark, ListMark, DefinitionMark, HorizontalRule, TableMark
} from "./wiki.grammar.terms";

// Produces the tokens that are only valid at the start of a line. Markup blocks follow KnowWE's DefaultMarkupType:
// the declaration "%%Name" starts a line, a block body runs until a line holding only "/%" or "%", a single line
// body ends with its line.

const NEWLINE = 10, RETURN = 13, SPACE = 32, TAB = 9, PERCENT = 37, SLASH = 47, COLON = 58, EQUALS = 61;
const EXCLAMATION = 33, STAR = 42, HASH = 35, SEMICOLON = 59, DASH = 45, PIPE = 124;

function isBlank(ch) {
    return ch === SPACE || ch === TAB;
}

function isNameStart(ch) {
    return (ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122);
}

function isNameChar(ch) {
    return isNameStart(ch) || (ch >= 48 && ch <= 57) || ch === 95 || ch === 45 || ch === 46 || ch === COLON;
}

function isLineEnd(ch) {
    return ch === NEWLINE || ch === RETURN || ch < 0;
}

function atLineStart(input) {
    return input.pos === 0 || input.peek(-1) === NEWLINE;
}

// Returns the length of a closing line "/%" or "%" starting at the given offset, or -1.
function closingLineLength(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    if (input.peek(i) === SLASH) i++;
    if (input.peek(i) !== PERCENT) return -1;
    i++;
    while (isBlank(input.peek(i))) i++;
    return isLineEnd(input.peek(i)) ? i - offset : -1;
}

// Returns the length of a declaration "%%Name" with optional ":" or "=" starting at the given offset, or -1.
function declarationLength(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    if (input.peek(i) !== PERCENT || input.peek(i + 1) !== PERCENT) return -1;
    i += 2;
    if (!isNameStart(input.peek(i))) return -1;
    while (isNameChar(input.peek(i))) i++;
    let j = i;
    while (isBlank(input.peek(j))) j++;
    if (input.peek(j) === COLON || input.peek(j) === EQUALS) i = j + 1;
    return i - offset;
}

// A declaration is a block if only blanks or a line comment follow it on its line.
function isBlockDeclaration(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    const ch = input.peek(i);
    return isLineEnd(ch) || (ch === SLASH && input.peek(i + 1) === SLASH);
}

function countWhile(input, predicate, max) {
    let i = 0;
    while (i < max && predicate(input.peek(i))) i++;
    return i;
}

export const lineStart = new ExternalTokenizer((input, stack) => {
    if (stack.canShift(MarkupContent)) {
        let length = 0;
        if (isBlockDeclaration(input, 0)) {
            // consume whole lines until a closing line or the end of the input
            for (;;) {
                let ch = input.peek(length);
                while (!isLineEnd(ch)) ch = input.peek(++length);
                if (ch < 0) break;
                if (ch === RETURN) length++;
                if (input.peek(length) === NEWLINE) length++;
                if (closingLineLength(input, length) >= 0 || input.peek(length) < 0) break;
            }
        } else {
            while (!isLineEnd(input.peek(length))) length++;
        }
        if (length > 0) input.acceptToken(MarkupContent, length);
        return;
    }
    if (!atLineStart(input)) return;
    if (stack.canShift(MarkupClose)) {
        const length = closingLineLength(input, 0);
        if (length > 0) {
            input.acceptToken(MarkupClose, length);
            return;
        }
    }
    if (stack.canShift(MarkupOpen)) {
        const length = declarationLength(input, 0);
        if (length > 0) {
            input.acceptToken(MarkupOpen, length);
            return;
        }
    }
    const ch = input.next;
    if (ch === EXCLAMATION && stack.canShift(HeadingMark)) {
        input.acceptToken(HeadingMark, countWhile(input, c => c === EXCLAMATION, 3));
    } else if ((ch === STAR || ch === HASH) && stack.canShift(ListMark)) {
        input.acceptToken(ListMark, countWhile(input, c => c === STAR || c === HASH, 20));
    } else if (ch === SEMICOLON && stack.canShift(DefinitionMark)) {
        input.acceptToken(DefinitionMark, 1);
    } else if (ch === DASH && stack.canShift(HorizontalRule)) {
        const length = countWhile(input, c => c === DASH, 1000);
        if (length >= 4) input.acceptToken(HorizontalRule, length);
    } else if (ch === PIPE && stack.canShift(TableMark)) {
        input.acceptToken(TableMark, input.peek(1) === PIPE ? 2 : 1);
    }
}, {contextual: true});
