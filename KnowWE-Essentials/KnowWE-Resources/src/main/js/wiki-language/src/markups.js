// Recognition of KnowWE markup declarations, following the patterns of DefaultMarkupType.
//
// KnowWE only claims "%%Name" for registered markups, any other name is a JSPWiki style. The names arrive through
// defineMarkups. Until then every declaration in block form counts as a markup, and single line declarations count
// if a colon or equals sign separates the name.

const FALLBACK_NAME = "[A-Za-z][\\w.\\-]*(?::[\\w.\\-]+)*";

let blockPattern = null;
let inlinePattern = null;
let fallbackPattern = namePattern([FALLBACK_NAME]);

function namePattern(names) {
    if (names.length === 0) return null;
    // longer names first, so a name never matches as the prefix of another one
    const sorted = [...names].sort((a, b) => b.length - a.length);
    return new RegExp("^%%(?:" + sorted.join("|") + ")(?=[ \\t:=]|$)", "i");
}

/**
 * Defines the markups of the wiki. Each entry has the markup name, which KnowWE treats as a case insensitive regular
 * expression, and whether the markup is inline. Passing null restores the default recognition.
 */
export function defineMarkups(markups) {
    if (markups == null) {
        blockPattern = inlinePattern = null;
        return;
    }
    const block = [], inline = [];
    for (const markup of markups) {
        const entry = typeof markup === "string" ? {name: markup, inline: false} : markup;
        (entry.inline ? inline : block).push(entry.name);
    }
    blockPattern = namePattern(block);
    inlinePattern = namePattern(inline);
}

/** Returns whether markups were defined, so declarations are matched against known names only. */
export function markupsDefined() {
    return blockPattern !== null || inlinePattern !== null;
}

const BLOCK_REST = /^[ \t]*[:=]?[ \t]*(?:\/\/.*)?$/;
const OPEN_REST = /^[ \t]*[:=]?/;

/**
 * Matches a markup declaration against a whole line. Returns the length of the opening "%%Name" including the
 * separator and whether the markup is a block running to its closing line or a single line markup, or null.
 */
export function matchDeclaration(line) {
    let indent = 0;
    while (line.charCodeAt(indent) === 32 || line.charCodeAt(indent) === 9) indent++;
    const declaration = indent ? line.slice(indent) : line;
    const name = markupsDefined() ? blockPattern?.exec(declaration) : fallbackPattern.exec(declaration);
    if (!name) return null;
    const rest = declaration.slice(name[0].length);
    if (BLOCK_REST.test(rest)) {
        return {block: true, length: indent + name[0].length + OPEN_REST.exec(rest)[0].length};
    }
    // single line form, the name is separated by one blank, colon or equals sign, the content does not start with "/"
    let i = 0;
    while (rest.charCodeAt(i) === 32 || rest.charCodeAt(i) === 9) i++;
    const separator = rest.charCodeAt(i);
    if (separator === 58 || separator === 61) {
        i++;
    } else if (i === 0 || !markupsDefined()) {
        return null;
    }
    let content = i;
    while (rest.charCodeAt(content) === 32 || rest.charCodeAt(content) === 9) content++;
    if (content >= rest.length || rest.charCodeAt(content) === 47) return null;
    return {block: false, length: indent + name[0].length + i};
}

/**
 * Matches an inline markup at the start of the given text. Returns the length of the opening "%%Name" including the
 * separator, or -1. The caller checks that a closing "%%" or "/%" follows.
 */
export function matchInlineDeclaration(text) {
    const name = inlinePattern?.exec(text);
    if (!name) return -1;
    return name[0].length + OPEN_REST.exec(text.slice(name[0].length))[0].length;
}
