import {ExternalTokenizer} from "@lezer/lr";
import {DeclarationComment, bodyText, AnnotationName, AnnotationValue, space} from "./content.grammar.terms";

const NEWLINE = 10, RETURN = 13, SPACE = 32, TAB = 9, AT = 64, SLASH = 47, COLON = 58, EQUALS = 61;

function isBlank(ch) {
    return ch === SPACE || ch === TAB;
}

function isSpace(ch) {
    return isBlank(ch) || ch === NEWLINE || ch === RETURN;
}

function isWordChar(ch) {
    return (ch >= 48 && ch <= 57) || (ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122) || ch === 95 || ch >= 0xc0;
}

/** Returns the length of the annotation name "@name" with an optional ":" or "=" at the offset, or -1. */
function annotationNameLength(input, offset) {
    if (input.peek(offset) !== AT || !isWordChar(input.peek(offset + 1))) return -1;
    let i = offset + 2;
    while (isWordChar(input.peek(i))) i++;
    const ch = input.peek(i);
    if (ch >= 0 && ch !== COLON && ch !== EQUALS && !isSpace(ch)) return -1;
    let j = i;
    while (isBlank(input.peek(j))) j++;
    if (input.peek(j) === COLON || input.peek(j) === EQUALS) i = j + 1;
    return i - offset;
}

/** Returns whether the line starting at the offset is an annotation line. */
function isAnnotationLine(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    return annotationNameLength(input, i) > 0;
}

/** Returns the offset of the next annotation line start after the offset, or the end of the input. */
function nextAnnotationLine(input, offset) {
    for (let i = offset; ; i++) {
        const ch = input.peek(i);
        if (ch < 0) return i;
        if (ch === NEWLINE && isAnnotationLine(input, i + 1)) return i + 1;
    }
}

function countBlanks(input, offset) {
    let i = offset;
    while (isBlank(input.peek(i))) i++;
    return i - offset;
}

function lastNonSpace(input, from, to) {
    let end = to;
    while (end > from && isSpace(input.peek(end - 1))) end--;
    return end;
}

/** Returns whether the offset is in the indentation of an annotation line, which ends at its name. */
function inAnnotationIndent(input, offset) {
    let i = offset - 1;
    while (isBlank(input.peek(i))) i--;
    return input.peek(i) === NEWLINE && isAnnotationLine(input, offset);
}

export const bodyTokens = new ExternalTokenizer((input, stack) => {
    const ch = input.next;
    if (ch < 0) return;
    if (inAnnotationIndent(input, 0)) {
        const indent = countBlanks(input, 0);
        if (indent > 0) return input.acceptToken(space, indent);
        return input.acceptToken(AnnotationName, annotationNameLength(input, 0));
    }
    if (stack.canShift(DeclarationComment)) {
        // a comment on the declaration line, behind the markup name
        const indent = countBlanks(input, 0);
        if (input.peek(indent) === SLASH && input.peek(indent + 1) === SLASH) {
            if (indent > 0) return input.acceptToken(space, indent);
            let i = 0;
            while (input.peek(i) >= 0 && input.peek(i) !== NEWLINE && input.peek(i) !== RETURN) i++;
            return input.acceptToken(DeclarationComment, i);
        }
    }
    const end = nextAnnotationLine(input, 0);
    let blank = 0;
    while (blank < end && isSpace(input.peek(blank))) blank++;
    if (stack.canShift(bodyText)) return input.acceptToken(bodyText, end);
    if (blank > 0) return input.acceptToken(space, blank);
    // the value of the last annotation runs up to the next annotation line, without the trailing blanks
    input.acceptToken(AnnotationValue, lastNonSpace(input, 0, end));
}, {contextual: true});
