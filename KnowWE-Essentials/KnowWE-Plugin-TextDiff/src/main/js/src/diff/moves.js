import {stripWhitespace} from "./lines.js";

// a block counts as moved if it has this many lines, or fewer lines with this much content
const MIN_LINES = 3;
const MIN_CHARS = 30;

/**
 * Blocks of lines that were removed in one place and added in another with the same content up to whitespace.
 * Returns {from: [aPos, len], to: [bPos, len]} pairs, in order of the removed side. Every line takes part in at
 * most one move.
 */
export function findMoves(aLines, bLines, chunks) {
	// every added line by its content, so a removed block can be looked up in the added lines
	const added = new Map();
	const addedKeys = new Map();
	for (const [, , bPos, bLen] of chunks) {
		for (let i = bPos; i < bPos + bLen; i++) {
			const key = stripWhitespace(bLines[i]);
			addedKeys.set(i, key);
			if (!added.has(key)) added.set(key, []);
			added.get(key).push(i);
		}
	}
	const usedB = new Set();
	const moves = [];
	for (const [aPos, aLen] of chunks) {
		let i = aPos;
		while (i < aPos + aLen) {
			const match = longestBlock(aLines, i, aPos + aLen, added, addedKeys, usedB);
			if (!match) {
				i++;
				continue;
			}
			const [bStart, len] = match;
			if (significant(aLines, i, len)) {
				moves.push({from: [i, len], to: [bStart, len]});
				for (let j = 0; j < len; j++) usedB.add(bStart + j);
				i += len;
			}
			else {
				i++;
			}
		}
	}
	return moves;
}

// the longest run of added lines equal to the removed lines starting at i, as [bStart, length]
function longestBlock(aLines, i, end, added, addedKeys, usedB) {
	const starts = added.get(stripWhitespace(aLines[i]));
	if (!starts) return null;
	let best = null;
	for (const bStart of starts) {
		if (usedB.has(bStart)) continue;
		let len = 1;
		while (i + len < end && !usedB.has(bStart + len)
			&& addedKeys.get(bStart + len) === stripWhitespace(aLines[i + len])) {
			len++;
		}
		if (!best || len > best[1]) best = [bStart, len];
	}
	return best;
}

function significant(lines, start, len) {
	if (len >= MIN_LINES) return true;
	let chars = 0;
	for (let i = 0; i < len; i++) chars += stripWhitespace(lines[start + i]).length;
	return chars >= MIN_CHARS;
}
