var KnowWETextDiff = (function(exports, _codemirror_state, _codemirror_view, _codemirror_language, _codemirror_search, _codemirror_merge) {
	Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
	//#region src/diff/myers.js
	var GREEDY_MAX_D = 4e3;
	/**
	* The minimal diff of two integer arrays. Equal to the greedy Myers diff of java-diff-utils up to an edit
	* distance of a few thousand, and to a minimal diff with the same counts beyond.
	*/
	function myers(a, b) {
		return greedy(a, b, GREEDY_MAX_D) ?? linear(a, b);
	}
	/**
	* Classic greedy Myers. Keeps the furthest reaching points of every round and backtracks through them, which
	* reproduces the edit script of java-diff-utils. Returns null if the edit distance exceeds maxD.
	*/
	function greedy(a, b, maxD = Infinity) {
		const n = a.length, m = b.length;
		const limit = Math.min(n + m, maxD);
		const trace = [];
		let v = /* @__PURE__ */ new Int32Array(3);
		let found = false;
		for (let d = 0; d <= limit && !found; d++) {
			const next = new Int32Array(2 * d + 3);
			for (let k = -d; k <= d; k += 2) {
				let x;
				if (d === 0) x = 0;
				else if (k === -d || k !== d && v[d + k - 1] < v[d + k + 1]) x = v[d + k + 1];
				else x = v[d + k - 1] + 1;
				let y = x - k;
				while (x < n && y < m && a[x] === b[y]) {
					x++;
					y++;
				}
				next[d + 1 + k] = x;
				if (x >= n && y >= m) {
					found = true;
					break;
				}
			}
			trace.push(next);
			v = next;
		}
		if (!found) return null;
		const ops = [];
		let x = n, y = m;
		for (let d = trace.length - 1; d > 0; d--) {
			const prev = trace[d - 1], k = x - y;
			const pk = k === -d || k !== d && prev[d + k - 1] < prev[d + k + 1] ? k + 1 : k - 1;
			const px = prev[d + pk], py = px - pk;
			while (x > px && y > py) {
				x--;
				y--;
			}
			ops.push(x === px ? 0 : 1, px, py);
			x = px;
			y = py;
		}
		const chunks = [];
		let current = null;
		for (let i = ops.length - 3; i >= 0; i -= 3) {
			const removed = ops[i] === 1, ax = ops[i + 1], by = ops[i + 2];
			if (current && current[0] + current[1] === ax && current[2] + current[3] === by) {
				if (removed) current[1]++;
				else current[3]++;
			} else chunks.push(current = [
				ax,
				removed ? 1 : 0,
				by,
				removed ? 0 : 1
			]);
		}
		return chunks;
	}
	/**
	* Linear space Myers, which finds the middle snake of the shortest edit path and recurses on both halves. Minimal,
	* and bounded by the length of the inputs in memory.
	*/
	function linear(a, b) {
		const n = a.length, m = b.length;
		const size = 2 * (n + m) + 3;
		const vf = new Int32Array(size), vb = new Int32Array(size);
		const chunks = [];
		let current = null;
		const emit = (ax, aLen, by, bLen) => {
			if (!aLen && !bLen) return;
			if (current && current[0] + current[1] === ax && current[2] + current[3] === by) {
				current[1] += aLen;
				current[3] += bLen;
			} else chunks.push(current = [
				ax,
				aLen,
				by,
				bLen
			]);
		};
		const stack = [[
			0,
			n,
			0,
			m
		]];
		while (stack.length) {
			let [a0, a1, b0, b1] = stack.pop();
			while (a0 < a1 && b0 < b1 && a[a0] === b[b0]) {
				a0++;
				b0++;
			}
			while (a1 > a0 && b1 > b0 && a[a1 - 1] === b[b1 - 1]) {
				a1--;
				b1--;
			}
			if (a0 === a1) {
				emit(a0, 0, b0, b1 - b0);
				continue;
			}
			if (b0 === b1) {
				emit(a0, a1 - a0, b0, 0);
				continue;
			}
			const snake = middleSnake(a, b, a0, a1, b0, b1, vf, vb);
			stack.push([
				snake[2],
				a1,
				snake[3],
				b1
			]);
			stack.push([
				a0,
				snake[0],
				b0,
				snake[1]
			]);
		}
		return chunks;
	}
	function middleSnake(a, b, a0, a1, b0, b1, vf, vb) {
		const n = a1 - a0, m = b1 - b0;
		const delta = n - m, odd = (delta & 1) === 1;
		const off = n + m + 1;
		vf[off + 1] = 0;
		vb[off + 1] = 0;
		const max = Math.ceil((n + m) / 2);
		for (let d = 0; d <= max; d++) {
			for (let k = -d; k <= d; k += 2) {
				let x = k === -d || k !== d && vf[off + k - 1] < vf[off + k + 1] ? vf[off + k + 1] : vf[off + k - 1] + 1;
				let y = x - k;
				const xs = x, ys = y;
				while (x < n && y < m && a[a0 + x] === b[b0 + y]) {
					x++;
					y++;
				}
				vf[off + k] = x;
				if (odd && k >= delta - (d - 1) && k <= delta + (d - 1) && x + vb[off + delta - k] >= n) return [
					a0 + xs,
					b0 + ys,
					a0 + x,
					b0 + y
				];
			}
			for (let k = -d; k <= d; k += 2) {
				let x = k === -d || k !== d && vb[off + k - 1] < vb[off + k + 1] ? vb[off + k + 1] : vb[off + k - 1] + 1;
				let y = x - k;
				const xs = x, ys = y;
				while (x < n && y < m && a[a1 - 1 - x] === b[b1 - 1 - y]) {
					x++;
					y++;
				}
				vb[off + k] = x;
				if (!odd && delta - k >= -d && delta - k <= d && x + vf[off + delta - k] >= n) return [
					a1 - x,
					b1 - y,
					a1 - xs,
					b1 - ys
				];
			}
		}
		throw new Error("no middle snake");
	}
	/** Maps each distinct value of both lists to an integer, so the diff compares numbers. */
	function encode(as, bs) {
		const ids = /* @__PURE__ */ new Map();
		const enc = (list) => {
			const out = new Int32Array(list.length);
			for (let i = 0; i < list.length; i++) {
				let id = ids.get(list[i]);
				if (id === void 0) ids.set(list[i], id = ids.size);
				out[i] = id;
			}
			return out;
		};
		return [enc(as), enc(bs)];
	}
	/** The added and removed counts of a chunk list. */
	function countChunks(chunks) {
		let added = 0, removed = 0;
		for (const [, aLen, , bLen] of chunks) {
			added += bLen;
			removed += aLen;
		}
		return {
			added,
			removed
		};
	}
	//#endregion
	//#region src/diff/lines.js
	/** Appends a newline unless the text already ends with one. A missing text is the empty text. */
	function withTrailingNewline(text) {
		if (text == null || text.length === 0) return "";
		return text.endsWith("\n") ? text : text + "\n";
	}
	/** The lines of a document, the empty element after the final newline included. */
	function splitLines(doc) {
		return doc.split("\n");
	}
	/** Start offsets of each line in a document, plus the offset past the end. */
	function lineOffsets(lines) {
		const offsets = new Int32Array(lines.length + 1);
		for (let i = 0; i < lines.length; i++) offsets[i + 1] = offsets[i] + lines[i].length + 1;
		return offsets;
	}
	var WHITESPACE = /\s+/g;
	/** A line without any whitespace, which is how lines compare when whitespace is ignored. */
	function stripWhitespace(line) {
		return line.replace(WHITESPACE, "");
	}
	/**
	* The line chunks [aPos, aLen, bPos, bLen] of two line lists. With ignoreWhitespace lines that differ in whitespace
	* only are equal, as with git diff -w.
	*/
	function lineDiff(aLines, bLines, { ignoreWhitespace = false } = {}) {
		const [a, b] = ignoreWhitespace ? encode(aLines.map(stripWhitespace), bLines.map(stripWhitespace)) : encode(aLines, bLines);
		return myers(a, b);
	}
	/**
	* The line model of a diff, one entry per shown line with the 1-based old and new line numbers (0 for a missing
	* side) and the status "common", "removed" or "added". The empty line after a final newline is left out.
	*/
	function lineModel(aLines, bLines, chunks) {
		const lines = [], lastA = aLines.length - 1;
		bLines.length - 1;
		let ai = 0, bi = 0;
		const common = (untilA) => {
			while (ai < untilA) {
				if (ai < lastA) lines.push({
					old: ai + 1,
					new: bi + 1,
					status: "common",
					text: aLines[ai]
				});
				ai++;
				bi++;
			}
		};
		for (const [aPos, aLen, bPos, bLen] of chunks) {
			common(aPos);
			for (let i = 0; i < aLen; i++, ai++) lines.push({
				old: ai + 1,
				new: 0,
				status: "removed",
				text: aLines[ai]
			});
			for (let i = 0; i < bLen; i++, bi++) lines.push({
				old: 0,
				new: bi + 1,
				status: "added",
				text: bLines[bi]
			});
			if (bi !== bPos + bLen) throw new Error("chunks out of order");
		}
		common(lastA);
		return lines;
	}
	//#endregion
	//#region src/diff/tokens.js
	var TOKEN = /[\p{L}\p{N}_]+|[^\S\n]+|\n|./gsu;
	var WORD_START = /^[\p{L}\p{N}_]/u;
	var BLANK = /^\s/;
	var MAX_TOKEN_D = 2e3;
	var MAX_CHAR_D = 50;
	/** The tokens of a text, concatenating to the text again. */
	function tokenize(text) {
		return text.match(TOKEN) ?? [];
	}
	/**
	* The character changes {fromA, toA, fromB, toB} between two strings, relative to the strings. Tokens are diffed as
	* units, and a single replaced word is diffed by characters if both words are similar. With ignoreWhitespace,
	* whitespace tokens take no part in the comparison. Null if the strings are too different.
	*/
	function refine(a, b, { ignoreWhitespace = false } = {}) {
		const ta = tokenize(a), tb = tokenize(b);
		const pa = positions(ta), pb = positions(tb);
		const ia = indexes(ta, ignoreWhitespace), ib = indexes(tb, ignoreWhitespace);
		const [ea, eb] = encode(ia.map((i) => ta[i]), ib.map((i) => tb[i]));
		const chunks = greedy(ea, eb, MAX_TOKEN_D);
		if (!chunks) return null;
		const out = [];
		for (const [ci, cl, di, dl] of chunks) {
			const fromA = pa[ci < ia.length ? ia[ci] : ta.length], toA = cl ? pa[ia[ci + cl - 1] + 1] : fromA;
			const fromB = pb[di < ib.length ? ib[di] : tb.length], toB = dl ? pb[ib[di + dl - 1] + 1] : fromB;
			if (cl === 1 && dl === 1) {
				const wa = ta[ia[ci]], wb = tb[ib[di]];
				if (WORD_START.test(wa) && WORD_START.test(wb)) {
					const inner = charDiff(wa, wb);
					if (inner) {
						for (const c of inner) out.push({
							fromA: fromA + c.fromA,
							toA: fromA + c.toA,
							fromB: fromB + c.fromB,
							toB: fromB + c.toB
						});
						continue;
					}
				}
			}
			out.push({
				fromA,
				toA,
				fromB,
				toB
			});
		}
		return out;
	}
	function indexes(tokens, ignoreWhitespace) {
		const out = [];
		for (let i = 0; i < tokens.length; i++) if (!ignoreWhitespace || !BLANK.test(tokens[i])) out.push(i);
		return out;
	}
	function positions(tokens) {
		const p = new Int32Array(tokens.length + 1);
		for (let i = 0; i < tokens.length; i++) p[i + 1] = p[i] + tokens[i].length;
		return p;
	}
	function charDiff(a, b) {
		const ca = Array.from(a), cb = Array.from(b);
		const [ea, eb] = encode(ca, cb);
		const chunks = greedy(ea, eb, MAX_CHAR_D);
		if (!chunks) return null;
		let changed = 0;
		for (const [, al, , bl] of chunks) changed += Math.max(al, bl);
		if (changed * 2 > Math.max(ca.length, cb.length)) return null;
		const ua = utf16(ca), ub = utf16(cb);
		return chunks.map(([ai, al, bi, bl]) => ({
			fromA: ua[ai],
			toA: ua[ai + al],
			fromB: ub[bi],
			toB: ub[bi + bl]
		}));
	}
	function utf16(chars) {
		const p = new Int32Array(chars.length + 1);
		for (let i = 0; i < chars.length; i++) p[i + 1] = p[i] + chars[i].length;
		return p;
	}
	//#endregion
	//#region src/diff/moves.js
	var MIN_LINES = 3;
	/**
	* Blocks of lines that were removed in one place and added in another with the same content up to whitespace.
	* Returns {from: [aPos, len], to: [bPos, len]} pairs, in order of the removed side. Every line takes part in at
	* most one move.
	*/
	function findMoves(aLines, bLines, chunks) {
		const added = /* @__PURE__ */ new Map();
		const addedKeys = /* @__PURE__ */ new Map();
		for (const [, , bPos, bLen] of chunks) for (let i = bPos; i < bPos + bLen; i++) {
			const key = stripWhitespace(bLines[i]);
			addedKeys.set(i, key);
			if (!added.has(key)) added.set(key, []);
			added.get(key).push(i);
		}
		const usedB = /* @__PURE__ */ new Set();
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
				if (len >= MIN_LINES) {
					moves.push({
						from: [i, len],
						to: [bStart, len]
					});
					for (let j = 0; j < len; j++) usedB.add(bStart + j);
					i += len;
				} else i++;
			}
		}
		return moves;
	}
	function longestBlock(aLines, i, end, added, addedKeys, usedB) {
		const starts = added.get(stripWhitespace(aLines[i]));
		if (!starts) return null;
		let best = null;
		for (const bStart of starts) {
			if (usedB.has(bStart)) continue;
			let len = 1;
			while (i + len < end && !usedB.has(bStart + len) && addedKeys.get(bStart + len) === stripWhitespace(aLines[i + len])) len++;
			if (!best || len > best[1]) best = [bStart, len];
		}
		return best;
	}
	//#endregion
	//#region src/diff/index.js
	var MAX_REFINED_CHARS = 2e4;
	/**
	* Diffs two texts. Both get a trailing newline, a missing text is the empty text. Returns the documents, their
	* lines, the line chunks [aPos, aLen, bPos, bLen], the character changes {fromA, toA, fromB, toB} for the merge
	* view, which refine changed lines down to tokens where both sides of a chunk exist, and the line counts.
	*/
	function computeDiff(oldText, newText, { ignoreWhitespace = false, inline = true } = {}) {
		const docA = withTrailingNewline(oldText), docB = withTrailingNewline(newText);
		const aLines = splitLines(docA), bLines = splitLines(docB);
		const oa = lineOffsets(aLines), ob = lineOffsets(bLines);
		let chunks;
		if (oldText == null && newText == null) chunks = [];
		else if (oldText == null) chunks = [[
			0,
			0,
			0,
			bLines.length - 1
		]];
		else if (newText == null) chunks = [[
			0,
			aLines.length - 1,
			0,
			0
		]];
		else chunks = lineDiff(aLines.slice(0, -1), bLines.slice(0, -1), { ignoreWhitespace });
		const changes = [];
		for (const [aPos, aLen, bPos, bLen] of chunks) {
			const fromA = oa[aPos], toA = oa[aPos + aLen], fromB = ob[bPos], toB = ob[bPos + bLen];
			const inner = inline && aLen && bLen ? refineChunk(docA, docB, aLines, bLines, oa, ob, aPos, aLen, bPos, bLen, ignoreWhitespace) : null;
			if (inner && keepsChunk(inner, oa, ob, fromA, toA, fromB, toB)) changes.push(...inner);
			else changes.push({
				fromA,
				toA,
				fromB,
				toB
			});
		}
		return {
			docA,
			docB,
			aLines,
			bLines,
			chunks,
			changes,
			stats: countChunks(chunks)
		};
	}
	function refineChunk(docA, docB, aLines, bLines, oa, ob, aPos, aLen, bPos, bLen, ignoreWhitespace) {
		const fromA = oa[aPos], toA = oa[aPos + aLen], fromB = ob[bPos], toB = ob[bPos + bLen];
		const shift = (inner, a, b) => inner && inner.map((c) => ({
			fromA: c.fromA + a,
			toA: c.toA + a,
			fromB: c.fromB + b,
			toB: c.toB + b
		}));
		if (toA - fromA + toB - fromB <= MAX_REFINED_CHARS) return shift(refine(docA.slice(fromA, toA), docB.slice(fromB, toB), { ignoreWhitespace }), fromA, fromB);
		if (aLen !== bLen) return null;
		const out = [];
		for (let i = 0; i < aLen; i++) {
			const inner = shift(refine(aLines[aPos + i], bLines[bPos + i], { ignoreWhitespace }), oa[aPos + i], ob[bPos + i]);
			if (!inner) return null;
			out.push(...inner);
		}
		return out;
	}
	function lineIndex(offsets, pos) {
		let lo = 0, hi = offsets.length - 2;
		while (lo < hi) {
			const mid = lo + hi + 1 >> 1;
			if (offsets[mid] <= pos) lo = mid;
			else hi = mid - 1;
		}
		return lo;
	}
	function keepsChunk(changes, oa, ob, fromA, toA, fromB, toB) {
		if (!changes.length) return false;
		const endA = oa[oa.length - 1] - 1, endB = ob[ob.length - 1] - 1;
		const lineEnd = (offsets, i) => offsets[i + 1] - 1;
		const from = (c) => {
			const la = lineIndex(oa, c.fromA), lb = lineIndex(ob, c.fromB);
			return lineEnd(oa, la) === c.fromA && lineEnd(ob, lb) === c.fromB && c.fromA < endA && c.fromB < endB ? [c.fromA + 1, c.fromB + 1] : [oa[la], ob[lb]];
		};
		const to = (c) => {
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
	//#endregion
	//#region src/renderer/table.js
	var MIN_ELIDE = 2;
	function escapeHtml(text) {
		return text.replace(/[&<>"']/g, (c) => ({
			"&": "&amp;",
			"<": "&lt;",
			">": "&gt;",
			"\"": "&quot;",
			"'": "&apos;"
		})[c]);
	}
	function lineNumberMode(lines) {
		let hasOld = false, hasNew = false;
		for (const line of lines) {
			hasOld ||= line.old > 0;
			hasNew ||= line.new > 0;
			if (hasOld && hasNew) return {
				cssClass: "two-line-numbers",
				columns: 4,
				old: true,
				new: true
			};
		}
		if (hasOld && !hasNew) return {
			cssClass: "single-line-number old-only",
			columns: 3,
			old: true,
			new: false
		};
		if (hasNew && !hasOld) return {
			cssClass: "single-line-number new-only",
			columns: 3,
			old: false,
			new: true
		};
		return {
			cssClass: "two-line-numbers",
			columns: 4,
			old: true,
			new: true
		};
	}
	function visibility(lines, contextLines) {
		const visible = new Array(lines.length).fill(contextLines < 0);
		if (contextLines < 0) return visible;
		for (let i = 0; i < lines.length; i++) {
			if (lines[i].status === "common") continue;
			for (let j = Math.max(0, i - contextLines); j <= Math.min(lines.length - 1, i + contextLines); j++) visible[j] = true;
		}
		return visible;
	}
	function row(line, hidden, mode) {
		const sign = line.status === "added" ? "+" : line.status === "removed" ? "−" : "";
		const classes = line.status + (hidden ? " hidden" : "");
		let html = "<tr class=\"line " + classes + "\" part=\"line " + classes + "\">";
		if (mode.old) html += "<td class=\"num old\" part=\"num old\">" + (line.old > 0 ? line.old : "") + "</td>";
		if (mode.new) html += "<td class=\"num new\" part=\"num new\">" + (line.new > 0 ? line.new : "") + "</td>";
		html += "<td class=\"sign\" part=\"sign\">" + sign + "</td>";
		html += "<td class=\"text\" part=\"text\">" + escapeHtml(line.text) + "</td></tr>";
		return html;
	}
	/** The inner HTML of the diff frame as a table, with unchanged regions beyond the context lines folded. */
	function renderTable(diff, { contextLines = 3 } = {}) {
		if (diff.stats.added === 0 && diff.stats.removed === 0) return "<p class=\"diff-note\" part=\"note\">No changes.</p>";
		const lines = lineModel(diff.aLines, diff.bLines, diff.chunks);
		const mode = lineNumberMode(lines);
		const visible = visibility(lines, contextLines);
		let html = "<table class=\"diff " + mode.cssClass + "\"><tbody>";
		let i = 0;
		while (i < lines.length) {
			if (visible[i]) {
				html += row(lines[i++], false, mode);
				continue;
			}
			const start = i;
			while (i < lines.length && !visible[i]) i++;
			const hidden = i - start;
			if (hidden < MIN_ELIDE) {
				for (let j = start; j < i; j++) html += row(lines[j], false, mode);
				continue;
			}
			html += "<tr class=\"elided\" part=\"elided\" data-hidden-count=\"" + hidden + "\"><td colspan=\"" + mode.columns + "\"><button class=\"expand\" part=\"expand-button\" type=\"button\" aria-label=\"Show " + hidden + " unchanged lines\"><svg class=\"icon\" viewBox=\"0 0 16 16\" width=\"16\" height=\"16\" fill=\"currentColor\" aria-hidden=\"true\"><path fill-rule=\"evenodd\" d=\"M8.177.677l2.896 2.896a.25.25 0 0 1-.177.427H8.75v1.25a.75.75 0 0 1-1.5 0V4H5.104a.25.25 0 0 1-.177-.427L7.823.677a.25.25 0 0 1 .354 0zM7.25 10.75a.75.75 0 0 1 1.5 0V12h2.146a.25.25 0 0 1 .177.427l-2.896 2.896a.25.25 0 0 1-.354 0L4.927 12.427A.25.25 0 0 1 5.104 12H7.25v-1.25zm-5-2a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5zM6 8a.75.75 0 0 1-.75.75h-.5a.75.75 0 0 1 0-1.5h.5A.75.75 0 0 1 6 8zm2.25.75a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5zM12 8a.75.75 0 0 1-.75.75h-.5a.75.75 0 0 1 0-1.5h.5A.75.75 0 0 1 12 8zm2.25.75a.75.75 0 0 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5z\"/></svg><span class=\"label\" part=\"expand-label\">Show " + hidden + " unchanged lines</span></button></td></tr>";
			for (let j = start; j < i; j++) html += row(lines[j], true, mode);
		}
		return html + "</tbody></table>";
	}
	//#endregion
	//#region src/renderer/codemirror.js
	var theme = _codemirror_view.EditorView.theme({
		"&": {
			fontSize: "var(--diff-font-size, 12.5px)",
			backgroundColor: "var(--diff-bg, #fff)",
			color: "var(--diff-fg, #000)"
		},
		"&.cm-focused": { outline: "none" },
		".cm-scroller": {
			fontFamily: "var(--diff-font-family, monospace)",
			lineHeight: "var(--diff-line-height, 1.6)"
		},
		".cm-content": {
			tabSize: "var(--diff-tab-size, 4)",
			padding: "0"
		},
		".cm-line": { padding: "0 8px 0 6px" },
		".cm-gutters": {
			backgroundColor: "var(--diff-num-bg)",
			color: "var(--diff-num-fg)",
			borderRight: "none"
		},
		".cm-lineNumbers .cm-gutterElement, .cm-oldLineNumbers .cm-gutterElement": {
			boxSizing: "border-box",
			minWidth: "var(--diff-gutter-width)",
			padding: "0 4px 0 2px",
			textAlign: "right"
		},
		".cm-changeGutter": {
			width: "3px",
			paddingLeft: "1px"
		},
		"&.cm-merge-b .cm-changedLine, & .cm-insertedLine": { backgroundColor: "var(--diff-added-bg)" },
		"&.cm-merge-a .cm-changedLine, & .cm-deletedChunk": { backgroundColor: "var(--diff-removed-bg)" },
		"&.cm-merge-b .cm-changedText": { background: "var(--diff-added-emph-bg)" },
		"&.cm-merge-a .cm-changedText, & .cm-deletedChunk .cm-deletedText": { background: "var(--diff-removed-emph-bg)" },
		"& .cm-deletedChunk": { padding: "0" },
		"& .cm-deletedLine": {
			padding: "0 8px 0 6px",
			textDecoration: "none"
		},
		"& .cm-deletedLine del, & .cm-insertedLine, & .cm-deletedLine del *": { textDecoration: "none" },
		".cm-oldLineNumbers .cm-deletedNumbers": { backgroundColor: "var(--diff-removed-num-bg)" },
		"&.cm-merge-b .cm-changedLineGutter": { background: "var(--diff-added-num-bg)" },
		"&.cm-merge-a .cm-changedLineGutter, & .cm-deletedLineGutter": { background: "var(--diff-removed-num-bg)" },
		".cm-collapsedLines": {
			backgroundColor: "var(--diff-elide-bg)",
			color: "var(--diff-elide-fg)",
			padding: "4px 16px"
		},
		".cm-collapsedLines:hover": {
			backgroundColor: "var(--diff-elide-hover-bg)",
			color: "var(--diff-elide-hover-fg)"
		},
		".cm-panels": {
			backgroundColor: "var(--diff-num-bg)",
			color: "var(--diff-fg)"
		},
		".cm-panels.cm-panels-top": { borderBottom: "1px solid var(--diff-border)" },
		".cm-textfield, .cm-button": {
			font: "inherit",
			fontSize: "12px"
		}
	});
	var NumberMarker = class extends _codemirror_view.GutterMarker {
		constructor(number) {
			super();
			this.number = number;
		}
		eq(other) {
			return this.number === other.number;
		}
		toDOM() {
			return document.createTextNode(this.number);
		}
	};
	var DeletedNumbersMarker = class extends _codemirror_view.GutterMarker {
		constructor(first, count) {
			super();
			this.first = first;
			this.count = count;
		}
		eq(other) {
			return this.first === other.first && this.count === other.count;
		}
		toDOM() {
			const dom = document.createElement("div");
			dom.className = "cm-deletedNumbers";
			for (let i = 0; i < this.count; i++) {
				const row = dom.appendChild(document.createElement("div"));
				row.textContent = String(this.first + i);
			}
			return dom;
		}
	};
	function oldLineNumbers(chunks) {
		return (bLine) => {
			let lo = 0, hi = chunks.length;
			while (lo < hi) {
				const mid = lo + hi >> 1;
				if (chunks[mid][2] <= bLine) lo = mid + 1;
				else hi = mid;
			}
			if (lo === 0) return bLine + 1;
			const [aPos, aLen, bPos, bLen] = chunks[lo - 1];
			if (bLine < bPos + bLen) return 0;
			return bLine - (bPos + bLen) + aPos + aLen + 1;
		};
	}
	function deletedBlocks(diff) {
		const blocks = /* @__PURE__ */ new Map();
		let pos = 0, next = 0;
		for (let i = 0; i < diff.bLines.length; i++) {
			while (next < diff.chunks.length && diff.chunks[next][2] === i) {
				if (diff.chunks[next][1] > 0) blocks.set(pos, diff.chunks[next]);
				next++;
			}
			pos += diff.bLines[i].length + 1;
		}
		return blocks;
	}
	function oldLineNumberGutter(diff) {
		const numberOf = oldLineNumbers(diff.chunks);
		const blocks = deletedBlocks(diff);
		return (0, _codemirror_view.gutter)({
			class: "cm-oldLineNumbers",
			lineMarker(view, line) {
				const number = numberOf(view.state.doc.lineAt(line.from).number - 1);
				return number ? new NumberMarker(String(number)) : null;
			},
			lineMarkerChange: () => false,
			widgetMarker(view, widget, block) {
				const chunk = blocks.get(block.from);
				return chunk ? new DeletedNumbersMarker(chunk[0] + 1, chunk[1]) : null;
			}
		});
	}
	function deletedLineHeights() {
		return _codemirror_view.ViewPlugin.fromClass(class {
			constructor(view) {
				this.measure = {
					read: (v) => {
						return {
							heights: Array.from(v.contentDOM.querySelectorAll(".cm-deletedChunk")).map((el) => Array.from(el.querySelectorAll(".cm-deletedLine")).map((line) => line.getBoundingClientRect().height)),
							markers: Array.from(v.dom.querySelectorAll(".cm-oldLineNumbers .cm-deletedNumbers"))
						};
					},
					write: ({ heights, markers }) => {
						if (heights.length !== markers.length) return;
						for (let i = 0; i < markers.length; i++) {
							const rows = markers[i].children;
							for (let j = 0; j < rows.length && j < heights[i].length; j++) rows[j].style.height = heights[i][j] + "px";
						}
					}
				};
				view.requestMeasure(this.measure);
			}
			update(update) {
				if (update.viewportChanged || update.geometryChanged || update.docChanged || update.transactions.length) update.view.requestMeasure(this.measure);
			}
		});
	}
	function languageExtensions(options) {
		if (!options.highlight) return [];
		const extensions = [];
		if (typeof KnowWEEditor !== "undefined" && KnowWEEditor.highlightStyle) extensions.push((0, _codemirror_language.syntaxHighlighting)(KnowWEEditor.highlightStyle()));
		if ((options.language ?? "wiki") === "wiki" && typeof KnowWEWikiLanguage !== "undefined") extensions.push(KnowWEWikiLanguage.wiki());
		return extensions;
	}
	function sideExtensions(options) {
		return [
			_codemirror_state.EditorState.readOnly.of(true),
			_codemirror_view.EditorView.editable.of(false),
			_codemirror_view.EditorView.contentAttributes.of({ tabindex: "0" }),
			(0, _codemirror_view.highlightSpecialChars)(),
			(0, _codemirror_search.search)({ top: true }),
			_codemirror_view.keymap.of(_codemirror_search.searchKeymap),
			languageExtensions(options),
			options.wrap === false ? [] : _codemirror_view.EditorView.lineWrapping,
			theme
		];
	}
	function digits(diff) {
		return String(Math.max(diff.aLines.length, diff.bLines.length)).length;
	}
	/**
	* Creates the CodeMirror view of a computed diff in the parent element. Options: root (the shadow root the view is
	* mounted in), layout "unified" or "split", language, highlight (whether to color the text at all), wrap,
	* contextLines (-1 shows every line). Returns the view and a destroy function.
	*/
	function createDiffView(parent, diff, options = {}) {
		const changes = diff.changes.map((c) => new _codemirror_merge.Change(c.fromA, c.toA, c.fromB, c.toB));
		const diffConfig = { override: () => changes };
		const collapse = options.contextLines === -1 ? void 0 : {
			margin: options.contextLines ?? 3,
			minSize: 4
		};
		parent.style.setProperty("--diff-gutter-width", digits(diff) + 2 + "ch");
		let view;
		if (options.layout === "split") view = new _codemirror_merge.MergeView({
			a: {
				doc: diff.docA,
				extensions: [sideExtensions(options), (0, _codemirror_view.lineNumbers)()]
			},
			b: {
				doc: diff.docB,
				extensions: [sideExtensions(options), (0, _codemirror_view.lineNumbers)()]
			},
			parent,
			root: options.root,
			diffConfig,
			collapseUnchanged: collapse,
			highlightChanges: true,
			gutter: true
		});
		else view = new _codemirror_view.EditorView({
			parent,
			root: options.root,
			state: _codemirror_state.EditorState.create({
				doc: diff.docB,
				extensions: [
					oldLineNumberGutter(diff),
					(0, _codemirror_view.lineNumbers)(),
					sideExtensions(options),
					deletedLineHeights(),
					(0, _codemirror_merge.unifiedMergeView)({
						original: diff.docA,
						mergeControls: false,
						diffConfig,
						collapseUnchanged: collapse,
						gutter: true,
						syntaxHighlightDeletions: true
					})
				]
			})
		});
		return {
			view,
			destroy: () => view.destroy()
		};
	}
	//#endregion
	//#region src/components/text-diff.js
	/**
	* <knowwe-text-diff> renders a text diff inside its own shadow DOM, as a CodeMirror view built in the browser or as
	* a table.
	*
	* Usage modes, any one of them:
	*
	*   1. Server rendered. The host carries a declarative shadow root. With the table renderer it holds the complete
	*      table, with the CodeMirror renderer a placeholder and both texts as JSON:
	*
	*        <knowwe-text-diff data-renderer="codemirror">
	*          <template shadowrootmode="open">
	*            <link rel="stylesheet" href=".../KnowWE-Plugin-TextDiff.css">
	*            <div class="diff-frame diff-placeholder"></div>
	*            <script type="application/json" class="diff-data">{"old": "...", "new": "...", "stats": {...}}<\/script>
	*          </template>
	*        </knowwe-text-diff>
	*
	*   2. Slotted raw text. The texts are diffed in the browser with the CodeMirror renderer, or posted to the backend
	*      with the table renderer, once the element scrolls near the viewport:
	*
	*        <knowwe-text-diff data-context-lines="3">
	*          <script type="text/plain" slot="old">...<\/script>
	*          <script type="text/plain" slot="new">...<\/script>
	*        </knowwe-text-diff>
	*
	*   3. Programmatic. Set the oldText and newText properties, the diff renders once both are set, or call load().
	*
	*   4. Endpoint driven. data-action-url names a backend action that derives the diff from the url itself. The
	*      CodeMirror renderer requests it with format=json, the table renderer as HTML:
	*
	*        <knowwe-text-diff data-action-url="action/MyDiffHtmlAction?file=..."></knowwe-text-diff>
	*
	* Attributes:
	*   - data-renderer:        codemirror | table (default). The CodeMirror renderer falls back to a table built in the
	*                           browser if the view cannot be created.
	*   - data-layout:          unified (default) | split, CodeMirror renderer only
	*   - data-language:        wiki (default) | none, the highlighting of the CodeMirror renderer
	*   - data-highlight-limit: texts above this many characters are shown without highlighting, default 200000
	*   - data-wrap:            false switches line wrapping off, CodeMirror renderer only
	*   - data-context-lines:   number of unchanged lines to show around each change, -1 shows all. Defaults to 3.
	*   - data-action-url:      the backend endpoint, default action/TextDiffAction
	*   - data-old-null:        the old side is missing, meaning a created file or article
	*   - data-new-null:        the new side is missing, meaning a deleted file or article
	*   - data-status:          set by the component to loading | ready | error
	*   - data-theme:           optional explicit light | dark theme override
	*
	* Events:
	*   - knowwe-text-diff-stats: dispatched (bubbling) once the browser computed the diff, detail.added and
	*                             detail.removed are its line counts.
	*
	* The CodeMirror view is created when the element comes near the viewport and destroyed again when it leaves it by
	* a wide margin, keeping its height, so long lists of diffs stay light.
	*
	* Test seam: assign a function to window.knowweTextDiffFetch to intercept the backend call.
	*/
	var DEFAULT_ACTION_URL = "action/TextDiffAction";
	var STYLESHEET_URL$1 = "KnowWEExtension/css/KnowWE-Plugin-TextDiff.css";
	var DEFAULT_CONTEXT_LINES = 3;
	var DEFAULT_HIGHLIGHT_LIMIT = 2e5;
	var VIEW_MARGIN = "600px";
	function resourceUrl$1(path) {
		const base = window.knowweTextDiffResourceBase;
		return base ? new URL(path, base).toString() : path;
	}
	function withWikiParams(url, params = {}) {
		try {
			const u = new URL(url, window.location.href);
			if (typeof Wiki !== "undefined" && Wiki.CsrfProtection) u.searchParams.set("X-XSRF-TOKEN", Wiki.CsrfProtection);
			const page = typeof KNOWWE !== "undefined" && KNOWWE.helper.getPagename();
			if (page && !u.searchParams.has("KWiki_Topic")) u.searchParams.set("KWiki_Topic", page);
			for (const [name, value] of Object.entries(params)) u.searchParams.set(name, value);
			return u.toString();
		} catch (_) {
			return url;
		}
	}
	function displayMode$1() {
		return typeof KNOWWE !== "undefined" && KNOWWE.helper.getDisplayMode ? KNOWWE.helper.getDisplayMode() : "light";
	}
	function codeMirrorAvailable() {
		return typeof CodeMirror !== "undefined" && !!CodeMirror.merge;
	}
	var KnowweTextDiff = class extends HTMLElement {
		connectedCallback() {
			if (!this.hasAttribute("data-theme")) this.setAttribute("data-theme", displayMode$1());
			if (this._connected) {
				if (this._diff && !this._editor) this._observe();
				return;
			}
			this._connected = true;
			this._hydrateTemplate();
			const root = this.shadowRoot;
			const inline = root && root.querySelector("script.diff-data");
			if (inline) try {
				this._data = JSON.parse(inline.textContent);
				inline.remove();
				this._observe();
				return;
			} catch (err) {
				this._setStatus("error", "Invalid diff data");
				return;
			}
			if (root && root.querySelector(".diff-frame")) {
				this._wireExpanders();
				return;
			}
			if (this._hasLazyInputs()) this._observe();
		}
		disconnectedCallback() {
			this._unobserve();
			this._destroyView();
		}
		/** Set the "before" text. Once both oldText and newText are set, the diff renders. */
		set oldText(value) {
			this._oldText = value;
			this._maybeReload();
		}
		/** Set the "after" text. Once both oldText and newText are set, the diff renders. */
		set newText(value) {
			this._newText = value;
			this._maybeReload();
		}
		/** The line counts of the shown diff, once it was computed in the browser. */
		get stats() {
			return this._diff ? this._diff.stats : null;
		}
		/** Loads and renders the diff now. Returns a promise that resolves when the content is rendered. */
		load() {
			this._unobserve();
			this._data = null;
			this._diff = null;
			return this._render();
		}
		_renderer() {
			return this.getAttribute("data-renderer") === "codemirror" && codeMirrorAvailable() ? "codemirror" : "table";
		}
		_hasLazyInputs() {
			if (this.querySelector(":scope > [slot=\"old\"]") !== null) return true;
			if (this.querySelector(":scope > [slot=\"new\"]") !== null) return true;
			if (this.hasAttribute("data-action-url")) return true;
			return this.hasAttribute("data-old-text") || this.hasAttribute("data-new-text") || this.hasAttribute("data-old-null") || this.hasAttribute("data-new-null");
		}
		_hydrateTemplate() {
			const tpl = this.querySelector(":scope > template[shadowrootmode]");
			if (!tpl) return;
			(this.shadowRoot || this.attachShadow({ mode: tpl.getAttribute("shadowrootmode") || "open" })).appendChild(tpl.content);
			tpl.remove();
		}
		_maybeReload() {
			if (!this.isConnected) return;
			if (this._oldText !== void 0 && this._newText !== void 0) this.load();
		}
		_observe() {
			if (this._observer) return;
			if (!("IntersectionObserver" in window)) {
				this._render();
				return;
			}
			this._observer = new IntersectionObserver((entries) => {
				const visible = entries.some((e) => e.isIntersecting);
				if (visible && !this._editor && !this._loading) this._render();
				else if (!visible && this._editor) this._parkView();
			}, { rootMargin: VIEW_MARGIN });
			this._observer.observe(this);
		}
		_unobserve() {
			if (this._observer) {
				this._observer.disconnect();
				this._observer = null;
			}
		}
		async _render() {
			if (this._diff) {
				this._showView();
				return;
			}
			this._loading = true;
			try {
				if (this._renderer() === "codemirror") {
					if (!this._data) this._data = await this._loadData();
					this._diff = computeDiff(this._data.old, this._data.new);
					this._showView();
					this.dispatchEvent(new CustomEvent("knowwe-text-diff-stats", {
						bubbles: true,
						detail: { ...this._diff.stats }
					}));
				} else await this._renderServerTable();
			} catch (err) {
				this._setStatus("error", err && err.message);
			} finally {
				this._loading = false;
			}
		}
		async _loadData() {
			this._ensureStylesheet();
			if (!this.hasAttribute("data-action-url")) return {
				old: this._readTextInput("_oldText", "old", "data-old-text", "data-old-null"),
				new: this._readTextInput("_newText", "new", "data-new-text", "data-new-null")
			};
			this._setStatus("loading");
			const res = await (window.knowweTextDiffFetch || window.fetch.bind(window))(withWikiParams(this.getAttribute("data-action-url"), { format: "json" }), {
				method: "POST",
				headers: { "Content-Type": "application/json; charset=UTF-8" },
				body: JSON.stringify({ contextLines: this._contextLines() })
			});
			if (!res.ok) throw new Error("HTTP " + res.status);
			return await res.json();
		}
		_showView() {
			const root = this._ensureStylesheet();
			this._destroyView();
			const frame = this._frame();
			frame.replaceChildren();
			frame.classList.remove("diff-placeholder");
			frame.style.removeProperty("min-height");
			const size = this._diff.docA.length + this._diff.docB.length;
			const limit = parseInt(this.getAttribute("data-highlight-limit"), 10);
			try {
				this._editor = createDiffView(frame, this._diff, {
					root,
					layout: this.getAttribute("data-layout") || "unified",
					language: this.getAttribute("data-language") || "wiki",
					highlight: size <= (Number.isNaN(limit) ? DEFAULT_HIGHLIGHT_LIMIT : limit),
					wrap: this.getAttribute("data-wrap") !== "false",
					contextLines: this._contextLines()
				});
				frame.classList.add("cm-diff");
			} catch (err) {
				console.warn("knowwe-text-diff: CodeMirror view failed, showing a table", err);
				frame.classList.remove("cm-diff");
				frame.innerHTML = renderTable(this._diff, { contextLines: this._contextLines() });
				this._wireExpanders();
			}
			this._setStatus("ready");
		}
		_parkView() {
			const frame = this._frame();
			frame.style.minHeight = frame.getBoundingClientRect().height + "px";
			this._destroyView();
			frame.replaceChildren();
		}
		_destroyView() {
			if (this._editor) {
				this._editor.destroy();
				this._editor = null;
			}
		}
		async _renderServerTable() {
			const oldText = this._readTextInput("_oldText", "old", "data-old-text", "data-old-null");
			const newText = this._readTextInput("_newText", "new", "data-new-text", "data-new-null");
			const url = this.getAttribute("data-action-url") || DEFAULT_ACTION_URL;
			const fetchImpl = window.knowweTextDiffFetch || window.fetch.bind(window);
			this._ensureStylesheet();
			this._setStatus("loading");
			const res = await fetchImpl(withWikiParams(url), {
				method: "POST",
				headers: { "Content-Type": "application/json; charset=UTF-8" },
				body: JSON.stringify({
					oldText,
					newText,
					contextLines: this._contextLines()
				})
			});
			if (!res.ok) throw new Error("HTTP " + res.status);
			this.shadowRoot.innerHTML = await res.text();
			this._wireExpanders();
			this._setStatus("ready");
		}
		_contextLines() {
			const n = parseInt(this.getAttribute("data-context-lines"), 10);
			return Number.isNaN(n) ? DEFAULT_CONTEXT_LINES : n;
		}
		_readSlot(name) {
			const el = this.querySelector(":scope > [slot=\"" + name + "\"]");
			return el ? el.textContent : null;
		}
		_readTextInput(property, slotName, textAttribute, nullAttribute) {
			if (this[property] !== void 0) return this[property];
			if (this.hasAttribute(nullAttribute)) return null;
			const slotted = this._readSlot(slotName);
			if (slotted !== null) return slotted;
			const attributed = this.getAttribute(textAttribute);
			return attributed !== null ? attributed : "";
		}
		_ensureStylesheet() {
			const root = this.shadowRoot || this.attachShadow({ mode: "open" });
			if (!root.querySelector("link[rel=\"stylesheet\"]")) {
				const link = document.createElement("link");
				link.rel = "stylesheet";
				link.href = resourceUrl$1(STYLESHEET_URL$1);
				root.appendChild(link);
			}
			return root;
		}
		_frame() {
			const root = this.shadowRoot;
			let frame = root.querySelector(".diff-frame");
			if (!frame) {
				frame = document.createElement("div");
				frame.className = "diff-frame";
				frame.setAttribute("part", "frame");
				root.appendChild(frame);
			}
			return frame;
		}
		_setStatus(status, message) {
			this.setAttribute("data-status", status);
			if (message) this.setAttribute("data-status-message", message);
			else this.removeAttribute("data-status-message");
		}
		_wireExpanders() {
			(this.shadowRoot || this).querySelectorAll("tr.elided button.expand").forEach((btn) => {
				btn.addEventListener("click", () => this._expand(btn));
			});
		}
		_expand(btn) {
			const elidedRow = btn.closest("tr.elided");
			if (!elidedRow) return;
			let row = elidedRow.nextElementSibling;
			while (row && row.classList.contains("hidden")) {
				row.classList.remove("hidden");
				row = row.nextElementSibling;
			}
			elidedRow.remove();
		}
	};
	if (!customElements.get("knowwe-text-diff")) customElements.define("knowwe-text-diff", KnowweTextDiff);
	//#endregion
	//#region src/components/file-change.js
	/**
	* <knowwe-file-change> - a GitHub-inspired file/article change frame.
	*
	* The component owns the file header and operation styling. The default slot is intentionally
	* generic and can contain a <knowwe-text-diff> when the change has content differences.
	*
	* Attributes:
	*   - data-change:    added | deleted | modified | renamed | renamed-modified | added-deleted
	*   - data-old-name:  original file/article name
	*   - data-new-name:  final file/article name
	*   - data-url:       optional URL for the displayed file/article name
	*   - data-old-url:   optional URL for the original name in rename changes
	*   - data-new-url:   optional URL for the final name in rename changes
	*   - data-additions: optional number of added lines
	*   - data-deletions: optional number of removed lines
	*   - data-collapsed: optional boolean attribute; collapses the body when present
	*   - data-theme:     optional explicit 'light' | 'dark' theme override
	*
	* Slots:
	*   - (default):      the body, typically a <knowwe-text-diff>
	*   - actions:        extra controls shown in the header between the stats and the badge, for
	*                     example links to related views. Clicks on links inside the header never
	*                     toggle the body.
	*
	* Events:
	*   - toggle: dispatched (bubbling) when the user collapses or expands the body via the header.
	*             detail.collapsed reflects the new state. Not dispatched for programmatic
	*             attribute changes.
	*
	* The stats show the given additions and deletions until a <knowwe-text-diff> in the body reports the counts of
	* the diff it computed, which replace them.
	*/
	var STYLESHEET_URL = "KnowWEExtension/css/KnowWE-Plugin-TextDiff.css";
	var CHANGE_TYPES = {
		added: { label: "Added" },
		deleted: { label: "Deleted" },
		modified: { label: "Modified" },
		renamed: { label: "Renamed" },
		"renamed-modified": { label: "Renamed + modified" },
		"added-deleted": { label: "Added + deleted" }
	};
	function displayMode() {
		return typeof KNOWWE !== "undefined" && KNOWWE.helper.getDisplayMode ? KNOWWE.helper.getDisplayMode() : "light";
	}
	function resourceUrl(path) {
		const base = window.knowweTextDiffResourceBase;
		return base ? new URL(path, base).toString() : path;
	}
	function normalizeChange(value) {
		if (value === "create" || value === "created") return "added";
		if (value === "delete" || value === "removed") return "deleted";
		if (value === "rename") return "renamed";
		if (value === "rename-modified" || value === "renamed-with-modifications") return "renamed-modified";
		if (value === "add-delete" || value === "created-deleted" || value === "transient") return "added-deleted";
		return Object.prototype.hasOwnProperty.call(CHANGE_TYPES, value) ? value : "modified";
	}
	function positiveInteger(value) {
		const parsed = Number.parseInt(value, 10);
		return Number.isFinite(parsed) && parsed > 0 ? parsed : 0;
	}
	function hasVisibleNode(node) {
		return node.nodeType === Node.ELEMENT_NODE || node.nodeType === Node.TEXT_NODE && node.textContent.trim().length > 0;
	}
	var KnowweFileChange = class extends HTMLElement {
		constructor() {
			super();
			this.addEventListener("knowwe-text-diff-stats", (event) => {
				if (event.target.parentElement !== this) return;
				this.setAttribute("data-additions", String(event.detail.added));
				this.setAttribute("data-deletions", String(event.detail.removed));
			});
		}
		static get observedAttributes() {
			return [
				"data-change",
				"data-old-name",
				"data-new-name",
				"data-url",
				"data-old-url",
				"data-new-url",
				"data-additions",
				"data-deletions",
				"data-collapsed"
			];
		}
		connectedCallback() {
			if (!this.hasAttribute("data-theme")) this.setAttribute("data-theme", displayMode());
			if (!this.hasAttribute("role")) this.setAttribute("role", "group");
			this._ensureShadow();
			this._update();
			this._updateSlotState();
		}
		attributeChangedCallback() {
			if (this.isConnected) this._update();
		}
		_ensureShadow() {
			if (this.shadowRoot) return;
			const root = this.attachShadow({ mode: "open" });
			root.innerHTML = "<link rel=\"stylesheet\" href=\"" + resourceUrl(STYLESHEET_URL) + "\"><article class=\"file-change\" part=\"frame\">  <header class=\"file-change-header\" part=\"header\">    <button class=\"file-change-toggle\" part=\"toggle\" type=\"button\" aria-label=\"Collapse file change\" aria-expanded=\"true\">      <svg class=\"icon\" viewBox=\"0 0 16 16\" aria-hidden=\"true\" focusable=\"false\">  <path d=\"M4 6l4 4 4-4\"/></svg>    </button>    <span class=\"file-change-title\" part=\"title\">      <a class=\"file-change-name old\" part=\"old-name\"></a>      <span class=\"file-change-arrow\" part=\"rename-arrow\" aria-hidden=\"true\">&rarr;</span>      <a class=\"file-change-name new\" part=\"new-name\"></a>    </span>    <span class=\"file-change-stats\" part=\"stats\">      <span class=\"file-change-stat additions\" part=\"additions\"></span>      <span class=\"file-change-stat deletions\" part=\"deletions\"></span>    </span>    <span class=\"file-change-actions\" part=\"actions\"><slot name=\"actions\"></slot></span>    <span class=\"file-change-badge\" part=\"badge\"></span>  </header>  <div class=\"file-change-body\" part=\"body\"><slot></slot></div></article>";
			root.querySelector(".file-change-header").addEventListener("click", (event) => this._onHeaderClick(event));
			root.querySelector("slot:not([name])").addEventListener("slotchange", () => this._updateSlotState());
		}
		_update() {
			const change = normalizeChange(this.getAttribute("data-change") || "modified");
			const metadata = CHANGE_TYPES[change];
			const oldName = this.getAttribute("data-old-name") || "";
			const newName = this.getAttribute("data-new-name") || "";
			const url = this.getAttribute("data-url") || "";
			const oldUrl = this.getAttribute("data-old-url") || "";
			const newUrl = this.getAttribute("data-new-url") || "";
			const additions = positiveInteger(this.getAttribute("data-additions"));
			const deletions = positiveInteger(this.getAttribute("data-deletions"));
			const isRename = change === "renamed" || change === "renamed-modified";
			const displayName = newName || oldName || "Unnamed file";
			const collapsed = this.hasAttribute("data-collapsed");
			this.setAttribute("data-normalized-change", change);
			this.setAttribute("aria-label", this._ariaLabel(change, oldName, newName));
			const root = this.shadowRoot;
			if (!root) return;
			const frame = root.querySelector(".file-change");
			const badge = root.querySelector(".file-change-badge");
			const title = root.querySelector(".file-change-title");
			const oldNameEl = root.querySelector(".file-change-name.old");
			const newNameEl = root.querySelector(".file-change-name.new");
			const additionsEl = root.querySelector(".file-change-stat.additions");
			const deletionsEl = root.querySelector(".file-change-stat.deletions");
			const toggle = root.querySelector(".file-change-toggle");
			frame.setAttribute("data-change", change);
			title.classList.toggle("is-rename", isRename);
			badge.textContent = metadata.label;
			this._setNameLink(oldNameEl, isRename ? oldName || "Unnamed file" : displayName, isRename ? oldUrl : url || newUrl || oldUrl);
			this._setNameLink(newNameEl, isRename ? newName || "Unnamed file" : "", newUrl || url);
			additionsEl.textContent = additions > 0 ? "+" + additions : "";
			deletionsEl.textContent = deletions > 0 ? "-" + deletions : "";
			additionsEl.hidden = additions === 0;
			deletionsEl.hidden = deletions === 0;
			frame.classList.toggle("is-collapsed", collapsed);
			toggle.setAttribute("aria-expanded", String(!collapsed));
			toggle.setAttribute("aria-label", collapsed ? "Expand file change" : "Collapse file change");
		}
		_updateSlotState() {
			const slot = this.shadowRoot && this.shadowRoot.querySelector("slot:not([name])");
			const hasBody = slot && slot.assignedNodes({ flatten: true }).some(hasVisibleNode);
			this.toggleAttribute("data-empty-body", !hasBody);
			const toggle = this.shadowRoot && this.shadowRoot.querySelector(".file-change-toggle");
			if (toggle) toggle.hidden = !hasBody;
		}
		_toggleCollapsed() {
			if (this.hasAttribute("data-empty-body")) return;
			this.toggleAttribute("data-collapsed");
			this.dispatchEvent(new CustomEvent("toggle", {
				bubbles: true,
				detail: { collapsed: this.hasAttribute("data-collapsed") }
			}));
		}
		_onHeaderClick(event) {
			if (this.hasAttribute("data-empty-body")) return;
			if (event.target.closest("a")) return;
			this._toggleCollapsed();
		}
		_setNameLink(element, text, href) {
			element.textContent = text;
			element.title = text;
			if (href) element.setAttribute("href", href);
			else element.removeAttribute("href");
		}
		_ariaLabel(change, oldName, newName) {
			if (change === "renamed" || change === "renamed-modified") return CHANGE_TYPES[change].label + ": " + (oldName || "Unnamed file") + " to " + (newName || "Unnamed file");
			return CHANGE_TYPES[change].label + ": " + (newName || oldName || "Unnamed file");
		}
	};
	if (!customElements.get("knowwe-file-change")) customElements.define("knowwe-file-change", KnowweFileChange);
	//#endregion
	exports.computeDiff = computeDiff;
	exports.createDiffView = createDiffView;
	exports.findMoves = findMoves;
	exports.renderTable = renderTable;
	return exports;
})({}, CodeMirror.state, CodeMirror.view, CodeMirror.language, CodeMirror.search, CodeMirror.merge);
