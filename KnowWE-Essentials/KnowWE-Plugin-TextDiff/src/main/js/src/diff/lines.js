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

/**
 * The line model of a diff, one entry per shown line with the 1-based old and new line numbers (0 for a missing
 * side) and the status "common", "removed" or "added". The empty line after a final newline is left out.
 */
export function lineModel(aLines, bLines, chunks) {
	const lines = [];
	const lastA = aLines.length - 1, lastB = bLines.length - 1;
	let ai = 0, bi = 0;
	const common = (untilA) => {
		while (ai < untilA) {
			if (ai < lastA) lines.push({old: ai + 1, new: bi + 1, status: "common", text: aLines[ai]});
			ai++;
			bi++;
		}
	};
	for (const [aPos, aLen, bPos, bLen] of chunks) {
		common(aPos);
		for (let i = 0; i < aLen; i++, ai++) {
			lines.push({old: ai + 1, new: 0, status: "removed", text: aLines[ai]});
		}
		for (let i = 0; i < bLen; i++, bi++) {
			lines.push({old: 0, new: bi + 1, status: "added", text: bLines[bi]});
		}
		if (bi !== bPos + bLen) throw new Error("chunks out of order");
	}
	common(lastA);
	return lines;
}
