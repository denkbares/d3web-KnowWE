import {encode, myers} from "./myers.js";

/** Appends a newline unless the text already ends with one. A missing text is the empty text. */
export function withTrailingNewline(text) {
	if (text == null || text.length === 0) return "";
	return text.endsWith("\n") ? text : text + "\n";
}

/** The lines of a document, the empty element after the final newline included. */
export function splitLines(doc) {
	return doc.split("\n");
}

/** Start offsets of each line in a document, plus the offset past the end. */
export function lineOffsets(lines) {
	const offsets = new Int32Array(lines.length + 1);
	for (let i = 0; i < lines.length; i++) offsets[i + 1] = offsets[i] + lines[i].length + 1;
	return offsets;
}

const WHITESPACE = /\s+/g;

/** A line without any whitespace, which is how lines compare when whitespace is ignored. */
export function stripWhitespace(line) {
	return line.replace(WHITESPACE, "");
}

/**
 * The line chunks [aPos, aLen, bPos, bLen] of two line lists. With ignoreWhitespace lines that differ in whitespace
 * only are equal, as with git diff -w.
 */
export function lineDiff(aLines, bLines, {ignoreWhitespace = false} = {}) {
	const [a, b] = ignoreWhitespace
		? encode(aLines.map(stripWhitespace), bLines.map(stripWhitespace))
		: encode(aLines, bLines);
	return myers(a, b);
}
