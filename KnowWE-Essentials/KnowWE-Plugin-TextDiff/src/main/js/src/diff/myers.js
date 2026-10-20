// Minimal diffs over integer arrays. Chunks are [aPos, aLen, bPos, bLen], the positions in a and b of a run of
// removed and added elements, in order and never touching each other.

// Beyond this edit distance the greedy trace would exceed about 64 MB, so the linear space variant takes over.
const GREEDY_MAX_D = 4000;

/**
 * The minimal diff of two integer arrays. Equal to the greedy Myers diff of java-diff-utils up to an edit
 * distance of a few thousand, and to a minimal diff with the same counts beyond.
 */
export function myers(a, b) {
	return greedy(a, b, GREEDY_MAX_D) ?? linear(a, b);
}

/**
 * Classic greedy Myers. Keeps the furthest reaching points of every round and backtracks through them, which
 * reproduces the edit script of java-diff-utils. Returns null if the edit distance exceeds maxD.
 */
export function greedy(a, b, maxD = Infinity) {
	const n = a.length, m = b.length;
	const limit = Math.min(n + m, maxD);
	// round d keeps the diagonals -d..d at offset d + 1, the trace holds about d squared integers in total
	const trace = [];
	let v = new Int32Array(3);
	let found = false;
	for (let d = 0; d <= limit && !found; d++) {
		const next = new Int32Array(2 * d + 3);
		for (let k = -d; k <= d; k += 2) {
			let x;
			if (d === 0) x = 0;
			else if (k === -d || (k !== d && v[d + k - 1] < v[d + k + 1])) x = v[d + k + 1];
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

	// the edit operations in reverse, each as removed flag, position in a and position in b
	const ops = [];
	let x = n, y = m;
	for (let d = trace.length - 1; d > 0; d--) {
		const prev = trace[d - 1], k = x - y;
		const pk = (k === -d || (k !== d && prev[d + k - 1] < prev[d + k + 1])) ? k + 1 : k - 1;
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
		}
		else {
			chunks.push(current = [ax, removed ? 1 : 0, by, removed ? 0 : 1]);
		}
	}
	return chunks;
}

/**
 * Linear space Myers, which finds the middle snake of the shortest edit path and recurses on both halves. Minimal,
 * and bounded by the length of the inputs in memory.
 */
export function linear(a, b) {
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
		}
		else {
			chunks.push(current = [ax, aLen, by, bLen]);
		}
	};
	// explicit stack of subproblems, so deep recursions on long inputs cannot overflow
	const stack = [[0, n, 0, m]];
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
		// the second half is pushed first, so the chunks come out in order
		stack.push([snake[2], a1, snake[3], b1]);
		stack.push([a0, snake[0], b0, snake[1]]);
	}
	return chunks;
}

// the start and end of the middle snake of the region as [xStart, yStart, xEnd, yEnd]
function middleSnake(a, b, a0, a1, b0, b1, vf, vb) {
	const n = a1 - a0, m = b1 - b0;
	const delta = n - m, odd = (delta & 1) === 1;
	const off = n + m + 1;
	vf[off + 1] = 0;
	vb[off + 1] = 0;
	const max = Math.ceil((n + m) / 2);
	for (let d = 0; d <= max; d++) {
		for (let k = -d; k <= d; k += 2) {
			let x = (k === -d || (k !== d && vf[off + k - 1] < vf[off + k + 1])) ? vf[off + k + 1] : vf[off + k - 1] + 1;
			let y = x - k;
			const xs = x, ys = y;
			while (x < n && y < m && a[a0 + x] === b[b0 + y]) {
				x++;
				y++;
			}
			vf[off + k] = x;
			// a reverse path on the same forward diagonal k runs on reverse diagonal delta - k
			if (odd && k >= delta - (d - 1) && k <= delta + (d - 1) && x + vb[off + delta - k] >= n) {
				return [a0 + xs, b0 + ys, a0 + x, b0 + y];
			}
		}
		for (let k = -d; k <= d; k += 2) {
			// the reverse pass counts from the end of the region, its diagonal k is forward diagonal delta - k
			let x = (k === -d || (k !== d && vb[off + k - 1] < vb[off + k + 1])) ? vb[off + k + 1] : vb[off + k - 1] + 1;
			let y = x - k;
			const xs = x, ys = y;
			while (x < n && y < m && a[a1 - 1 - x] === b[b1 - 1 - y]) {
				x++;
				y++;
			}
			vb[off + k] = x;
			if (!odd && delta - k >= -d && delta - k <= d && x + vf[off + delta - k] >= n) {
				return [a1 - x, b1 - y, a1 - xs, b1 - ys];
			}
		}
	}
	throw new Error("no middle snake");
}

/** Maps each distinct value of both lists to an integer, so the diff compares numbers. */
export function encode(as, bs) {
	const ids = new Map();
	const enc = list => {
		const out = new Int32Array(list.length);
		for (let i = 0; i < list.length; i++) {
			let id = ids.get(list[i]);
			if (id === undefined) ids.set(list[i], id = ids.size);
			out[i] = id;
		}
		return out;
	};
	return [enc(as), enc(bs)];
}

/** The added and removed counts of a chunk list. */
export function countChunks(chunks) {
	let added = 0, removed = 0;
	for (const [, aLen, , bLen] of chunks) {
		added += bLen;
		removed += aLen;
	}
	return {added, removed};
}
