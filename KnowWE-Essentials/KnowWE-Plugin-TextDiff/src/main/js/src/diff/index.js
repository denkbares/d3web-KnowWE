// The diff of two documents as the renderers consume it. Independent of CodeMirror and the DOM.
import {countChunks} from "./myers.js";
import {lineDiff, lineOffsets, splitLines, withTrailingNewline} from "./lines.js";
import {refine} from "./tokens.js";

export {withTrailingNewline, splitLines, lineModel} from "./lines.js";

// chunks beyond this size are refined line by line, or not at all if their line counts differ
const MAX_REFINED_CHARS = 20000;
// a chunk whose refinement changes more of its text than this is a rewrite and gets no emphasis
const MAX_EMPHASIS_RATIO = 0.5;
const BLANK = /^\s*$/;

/**
 * Diffs two texts. Both get a trailing newline, a missing text is the empty text. Returns the documents, their
 * lines, the line chunks [aPos, aLen, bPos, bLen], the character changes {fromA, toA, fromB, toB} for the merge
 * view, which refine changed lines down to tokens where both sides of a chunk exist, the emphasis ranges (those
 * refinements worth marking, since the lines changed in a few places and not only in their whitespace) and the
 * line counts.
 */
export function computeDiff(oldText, newText, {ignoreWhitespace = false, inline = true} = {}) {
	const docA = withTrailingNewline(oldText), docB = withTrailingNewline(newText);
	const aLines = splitLines(docA), bLines = splitLines(docB);
	const oa = lineOffsets(aLines), ob = lineOffsets(bLines);
	let chunks;
	if (oldText == null && newText == null) chunks = [];
	else if (oldText == null) chunks = [[0, 0, 0, bLines.length - 1]];
	else if (newText == null) chunks = [[0, aLines.length - 1, 0, 0]];
	// the empty element after the final newline is common by construction and must not match a blank line
	else chunks = lineDiff(aLines.slice(0, -1), bLines.slice(0, -1), {ignoreWhitespace});

	const changes = [], refined = [];
	for (const [aPos, aLen, bPos, bLen] of chunks) {
		const fromA = oa[aPos], toA = oa[aPos + aLen], fromB = ob[bPos], toB = ob[bPos + bLen];
		const inner = inline && aLen && bLen
			? refineChunk(docA, docB, aLines, bLines, oa, ob, aPos, aLen, bPos, bLen, ignoreWhitespace) : null;
		if (inner && keepsChunk(inner, oa, ob, fromA, toA, fromB, toB)) {
			changes.push(...inner);
			if (changedShare(inner) <= MAX_EMPHASIS_RATIO * (toA - fromA + toB - fromB)) {
				// a changed run of whitespace, such as a new indentation, is not worth a mark of its own
				refined.push(...inner.filter(c => !BLANK.test(docA.slice(c.fromA, c.toA)) || !BLANK.test(docB.slice(c.fromB, c.toB))));
			}
		}
		else {
			changes.push({fromA, toA, fromB, toB});
		}
	}
	return {docA, docB, aLines, bLines, chunks, changes, emphasis: refined, stats: countChunks(chunks)};
}

function changedShare(changes) {
	let n = 0;
	for (const c of changes) n += c.toA - c.fromA + c.toB - c.fromB;
	return n;
}

function refineChunk(docA, docB, aLines, bLines, oa, ob, aPos, aLen, bPos, bLen, ignoreWhitespace) {
	const fromA = oa[aPos], toA = oa[aPos + aLen], fromB = ob[bPos], toB = ob[bPos + bLen];
	const shift = (inner, a, b) => inner && inner.map(c => ({fromA: c.fromA + a, toA: c.toA + a, fromB: c.fromB + b, toB: c.toB + b}));
	if (toA - fromA + toB - fromB <= MAX_REFINED_CHARS) {
		return shift(refine(docA.slice(fromA, toA), docB.slice(fromB, toB), {ignoreWhitespace}), fromA, fromB);
	}
	if (aLen !== bLen) return null;
	const out = [];
	for (let i = 0; i < aLen; i++) {
		const inner = shift(refine(aLines[aPos + i], bLines[bPos + i], {ignoreWhitespace}), oa[aPos + i], ob[bPos + i]);
		if (!inner) return null;
		out.push(...inner);
	}
	return out;
}

function lineIndex(offsets, pos) {
	let lo = 0, hi = offsets.length - 2;
	while (lo < hi) {
		const mid = (lo + hi + 1) >> 1;
		if (offsets[mid] <= pos) lo = mid;
		else hi = mid - 1;
	}
	return lo;
}

// Whether the merge view groups the changes into exactly the given line chunk. It extends a change that starts at
// a line end on both sides to the next line, and one that ends inside a line to the end of that line, and merges
// changes whose line ranges touch, see toChunks in @codemirror/merge.
function keepsChunk(changes, oa, ob, fromA, toA, fromB, toB) {
	if (!changes.length) return false;
	const endA = oa[oa.length - 1] - 1, endB = ob[ob.length - 1] - 1;
	const lineEnd = (offsets, i) => offsets[i + 1] - 1;
	const from = c => {
		const la = lineIndex(oa, c.fromA), lb = lineIndex(ob, c.fromB);
		return lineEnd(oa, la) === c.fromA && lineEnd(ob, lb) === c.fromB && c.fromA < endA && c.fromB < endB
			? [c.fromA + 1, c.fromB + 1] : [oa[la], ob[lb]];
	};
	const to = c => {
		const la = lineIndex(oa, c.toA), lb = lineIndex(ob, c.toB);
		return oa[la] === c.toA && ob[lb] === c.toB ? [c.toA, c.toB] : [lineEnd(oa, la) + 1, lineEnd(ob, lb) + 1];
	};
	const [startA, startB] = from(changes[0]);
	let [stopA, stopB] = to(changes[0]);
	for (let i = 1; i < changes.length; i++) {
		const [nextA, nextB] = from(changes[i]);
		if (nextA > stopA || nextB > stopB) return false;
		[stopA, stopB] = to(changes[i]);
	}
	return startA === fromA && startB === fromB && Math.max(startA, stopA) === toA && Math.max(startB, stopB) === toB;
}
