import {describe, expect, it} from "vitest";
import {Text} from "@codemirror/state";
import {Change, Chunk} from "@codemirror/merge";
import {encode, greedy, linear, myers} from "../src/diff/myers.js";
import {lineDiff, lineModel, splitLines, withTrailingNewline} from "../src/diff/lines.js";
import {refine, tokenize} from "../src/diff/tokens.js";
import {findMoves} from "../src/diff/moves.js";
import {computeDiff} from "../src/diff/index.js";
import {fixtures, normalized} from "./fixtures.js";

// the lines of both sides without the empty element after the final newline
const encodedLines = f => encode(splitLines(withTrailingNewline(normalized(f.old))).slice(0, -1), splitLines(withTrailingNewline(normalized(f.new))).slice(0, -1));
const bothSides = fixtures.filter(f => f.old != null && f.new != null);

describe("greedy Myers", () => {
	it.each(bothSides.map(f => [f.name, f]))("equals the Java chunks of %s", (name, f) => {
		const [a, b] = encodedLines(f);
		expect(greedy(a, b)).toEqual(f.chunks);
	});

	it("gives up beyond the distance limit", () => {
		expect(greedy([1, 2, 3], [4, 5, 6], 2)).toBeNull();
		expect(greedy([1, 2, 3], [4, 5, 6], 6)).toEqual([[0, 3, 0, 3]]);
	});
});

describe("linear space Myers", () => {
	it.each(bothSides.map(f => [f.name, f]))("has the counts of the Java chunks of %s", (name, f) => {
		const [a, b] = encodedLines(f);
		const count = chunks => chunks.reduce(([r, ad], [, al, , bl]) => [r + al, ad + bl], [0, 0]);
		expect(count(linear(a, b))).toEqual(count(f.chunks));
	});

	it("produces a valid edit script on random inputs", () => {
		let seed = 42;
		const random = n => (seed = (seed * 1103515245 + 12345) & 0x7fffffff) % n;
		for (let round = 0; round < 300; round++) {
			const a = Array.from({length: random(40)}, () => random(5));
			const b = Array.from({length: random(40)}, () => random(5));
			const chunks = linear(a, b);
			const g = greedy(a, b);
			// the same distance as the greedy diff, and applying the chunks to a yields b
			expect(chunks.reduce((d, [, al, , bl]) => d + al + bl, 0)).toBe(g.reduce((d, [, al, , bl]) => d + al + bl, 0));
			const out = [];
			let ai = 0;
			for (const [aPos, aLen, bPos, bLen] of chunks) {
				while (ai < aPos) out.push(a[ai++]);
				ai += aLen;
				for (let i = 0; i < bLen; i++) out.push(b[bPos + i]);
			}
			while (ai < a.length) out.push(a[ai++]);
			expect(out).toEqual(b);
		}
	});

	it("takes over from the greedy diff on large distances", () => {
		const a = Array.from({length: 6000}, (_, i) => i), b = Array.from({length: 6000}, (_, i) => i + 10000);
		expect(myers(a, b)).toEqual([[0, 6000, 0, 6000]]);
	});
});

describe("computeDiff", () => {
	it.each(fixtures.map(f => [f.name, f]))("matches the fixture %s", (name, f) => {
		const diff = computeDiff(normalized(f.old), normalized(f.new));
		expect(diff.chunks).toEqual(f.chunks);
		expect(diff.stats).toEqual(f.stats);
	});

	it.each(fixtures.map(f => [f.name, f]))("is shown chunk for chunk by the merge view for %s", (name, f) => {
		const diff = computeDiff(normalized(f.old), normalized(f.new));
		const a = Text.of(diff.aLines), b = Text.of(diff.bLines);
		const shown = Chunk.build(a, b, {override: () => diff.changes.map(c => new Change(c.fromA, c.toA, c.fromB, c.toB))});
		const lines = shown.map(c => {
			const aPos = a.lineAt(c.fromA).number - 1, bPos = b.lineAt(c.fromB).number - 1;
			const aLen = c.toA > c.fromA ? a.lineAt(Math.min(c.toA - 1, a.length)).number - aPos : 0;
			const bLen = c.toB > c.fromB ? b.lineAt(Math.min(c.toB - 1, b.length)).number - bPos : 0;
			return [aPos, aLen, bPos, bLen];
		});
		expect(lines).toEqual(f.chunks);
	});

	it("refines a changed line to its changed tokens", () => {
		const diff = computeDiff("| _2017003\t| _Stoff3\t| _Grau\n", "| _2017003\t| _Stoff3\t| _Anthrazit\n");
		expect(diff.changes).toEqual([{fromA: 23, toA: 28, fromB: 23, toB: 33}]);
	});

	it("builds the line model", () => {
		const diff = computeDiff("a\nb\nc\n", "a\nB\nc\n");
		expect(lineModel(diff.aLines, diff.bLines, diff.chunks)).toEqual([
			{old: 1, new: 1, status: "common", text: "a"},
			{old: 2, new: 0, status: "removed", text: "b"},
			{old: 0, new: 2, status: "added", text: "B"},
			{old: 3, new: 3, status: "common", text: "c"},
		]);
	});

	it("builds the line model of a created file", () => {
		const diff = computeDiff(null, "a\nb");
		expect(lineModel(diff.aLines, diff.bLines, diff.chunks).map(l => l.status + l.new)).toEqual(["added1", "added2"]);
	});
});

describe("whitespace mode", () => {
	const f = fixtures.find(x => x.name === "whitespace-only");

	it("ignores lines that differ in whitespace only", () => {
		const diff = computeDiff(f.old, f.new, {ignoreWhitespace: true});
		expect(diff.chunks).toEqual([]);
		expect(diff.stats).toEqual({added: 0, removed: 0});
	});

	it("still finds changed content", () => {
		const diff = computeDiff("a  =  1\nb = 2\n", "a = 1\nb = 3\n", {ignoreWhitespace: true});
		expect(diff.chunks).toEqual([[1, 1, 1, 1]]);
	});

	it("skips whitespace tokens in the in-line changes", () => {
		expect(refine("x  = 1", "x = 2", {ignoreWhitespace: true})).toEqual([{fromA: 5, toA: 6, fromB: 4, toB: 5}]);
	});

	it("compares lines without whitespace", () => {
		expect(lineDiff(["a b", "c"], ["ab", "c"], {ignoreWhitespace: true})).toEqual([]);
	});
});

describe("tokens", () => {
	it("splits words, blanks and punctuation", () => {
		expect(tokenize("_BraunBeige\t<-> _Grau, 12")).toEqual(["_BraunBeige", "\t", "<", "-", ">", " ", "_Grau", ",", " ", "12"]);
	});

	it("keeps identifiers whole in tab aligned tables", () => {
		const changes = refine("_BraunBeige\t_BraunBeige", "_BraunBeigeDunkel\t_BraunBeigeHell");
		expect(changes).toEqual([{fromA: 11, toA: 11, fromB: 11, toB: 17}, {fromA: 23, toA: 23, fromB: 29, toB: 33}]);
	});

	it("diffs a single similar word by characters", () => {
		expect(refine("Volantrollo im VM", "Volantrollos im VM")).toEqual([{fromA: 11, toA: 11, fromB: 11, toB: 12}]);
	});

	it("does not split a replaced word that is unlike the old one", () => {
		expect(refine("_Grau", "_PVC")).toEqual([{fromA: 0, toA: 5, fromB: 0, toB: 4}]);
	});

	it("removes whole identifiers from an id list", () => {
		expect(refine("_2017691, _2018120", "_2018120")).toEqual([{fromA: 0, toA: 10, fromB: 0, toB: 0}]);
	});

	it("handles surrogate pairs", () => {
		expect(refine("Stoff 🎨 blau", "Stoff 🎨 rot")).toEqual([{fromA: 9, toA: 13, fromB: 9, toB: 12}]);
	});
});

describe("moves", () => {
	it("pairs a removed block with the same added block", () => {
		const f = fixtures.find(x => x.name === "coom-moved-block");
		const diff = computeDiff(f.old, f.new);
		const moves = findMoves(diff.aLines, diff.bLines, diff.chunks);
		// a line was added inside the moved block, so it is found in two parts
		expect(moves).toEqual([{from: [1, 2], to: [8, 2]}, {from: [3, 4], to: [11, 4]}]);
		expect(diff.aLines.slice(1, 3)).toEqual(diff.bLines.slice(8, 10));
		expect(diff.aLines.slice(3, 7)).toEqual(diff.bLines.slice(11, 15));
	});

	it("ignores small blocks of little content", () => {
		const diff = computeDiff("{\n}\na\n", "a\n{\n}\n");
		expect(findMoves(diff.aLines, diff.bLines, diff.chunks)).toEqual([]);
	});

	it("matches up to whitespace", () => {
		const diff = computeDiff("x\n  one two three four five six seven eight\ny\n", "x\ny\none two three four five six seven eight\n");
		expect(findMoves(diff.aLines, diff.bLines, diff.chunks)).toEqual([{from: [1, 1], to: [2, 1]}]);
	});
});
