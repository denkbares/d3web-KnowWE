import {encode, greedy} from "./myers.js";

// words are runs of letters, digits and underscores, whitespace runs stay one token, anything else is one character
const TOKEN = /[\p{L}\p{N}_]+|[^\S\n]+|\n|./gsu;
const WORD_START = /^[\p{L}\p{N}_]/u;
const BLANK = /^\s/;

// beyond these edit distances the sides are too different for a useful in-line view
const MAX_TOKEN_D = 2000;
const MAX_CHAR_D = 50;

/** The tokens of a text, concatenating to the text again. */
export function tokenize(text) {
	return text.match(TOKEN) ?? [];
}

/**
 * The character changes {fromA, toA, fromB, toB} between two strings, relative to the strings. Tokens are diffed as
 * units, and a single replaced word is diffed by characters if both words are similar. With ignoreWhitespace,
 * whitespace tokens take no part in the comparison. Null if the strings are too different.
 */
export function refine(a, b, {ignoreWhitespace = false} = {}) {
	const ta = tokenize(a), tb = tokenize(b);
	const pa = positions(ta), pb = positions(tb);
	// indexes of the tokens that are compared, all of them or the non blank ones
	const ia = indexes(ta, ignoreWhitespace), ib = indexes(tb, ignoreWhitespace);
	const [ea, eb] = encode(ia.map(i => ta[i]), ib.map(i => tb[i]));
	const chunks = greedy(ea, eb, MAX_TOKEN_D);
	if (!chunks) return null;
	const out = [];
	for (const [ci, cl, di, dl] of chunks) {
		// a chunk of compared tokens maps back to a range of all tokens, blanks at its edges excluded
		const fromA = pa[ci < ia.length ? ia[ci] : ta.length], toA = cl ? pa[ia[ci + cl - 1] + 1] : fromA;
		const fromB = pb[di < ib.length ? ib[di] : tb.length], toB = dl ? pb[ib[di + dl - 1] + 1] : fromB;
		if (cl === 1 && dl === 1) {
			const wa = ta[ia[ci]], wb = tb[ib[di]];
			if (WORD_START.test(wa) && WORD_START.test(wb)) {
				const inner = charDiff(wa, wb);
				if (inner) {
					for (const c of inner) {
						out.push({fromA: fromA + c.fromA, toA: fromA + c.toA, fromB: fromB + c.fromB, toB: fromB + c.toB});
					}
					continue;
				}
			}
		}
		out.push({fromA, toA, fromB, toB});
	}
	return out;
}

function indexes(tokens, ignoreWhitespace) {
	const out = [];
	for (let i = 0; i < tokens.length; i++) {
		if (!ignoreWhitespace || !BLANK.test(tokens[i])) out.push(i);
	}
	return out;
}

function positions(tokens) {
	const p = new Int32Array(tokens.length + 1);
	for (let i = 0; i < tokens.length; i++) p[i + 1] = p[i] + tokens[i].length;
	return p;
}

// the character changes of two words if they share at least half of the longer one
function charDiff(a, b) {
	const ca = Array.from(a), cb = Array.from(b);
	const [ea, eb] = encode(ca, cb);
	const chunks = greedy(ea, eb, MAX_CHAR_D);
	if (!chunks) return null;
	let changed = 0;
	for (const [, al, , bl] of chunks) changed += Math.max(al, bl);
	if (changed * 2 > Math.max(ca.length, cb.length)) return null;
	const ua = utf16(ca), ub = utf16(cb);
	return chunks.map(([ai, al, bi, bl]) => ({fromA: ua[ai], toA: ua[ai + al], fromB: ub[bi], toB: ub[bi + bl]}));
}

// code point indexes to UTF-16 offsets
function utf16(chars) {
	const p = new Int32Array(chars.length + 1);
	for (let i = 0; i < chars.length; i++) p[i + 1] = p[i] + chars[i].length;
	return p;
}
