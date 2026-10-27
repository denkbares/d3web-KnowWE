import {EditorState, RangeSetBuilder} from "@codemirror/state";
import {Decoration, EditorView, GutterMarker, ViewPlugin, gutter, highlightSpecialChars, keymap, lineNumbers} from "@codemirror/view";
import {syntaxHighlighting} from "@codemirror/language";
import {search, searchKeymap} from "@codemirror/search";
import {Change, MergeView, unifiedMergeView} from "@codemirror/merge";
import {lineOffsets} from "../diff/lines.js";

// The colors and metrics of KnowWE-Plugin-TextDiff.css, so the editor and the table look alike and the custom
// properties theme both. The merge view's own light and dark rules are overridden by these.
const theme = EditorView.theme({
	"&": {
		fontSize: "var(--diff-font-size, 12.5px)",
		backgroundColor: "var(--diff-bg, #fff)",
		color: "var(--diff-fg, #000)"
	},
	"&.cm-focused": {outline: "none"},
	".cm-scroller": {
		fontFamily: "var(--diff-font-family, monospace)",
		lineHeight: "var(--diff-line-height, 1.6)"
	},
	".cm-content": {tabSize: "var(--diff-tab-size, 4)", padding: "0"},
	".cm-line": {padding: "0 8px 0 6px"},
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
	// the line tints tell the changed lines already
	".cm-changeGutter": {display: "none"},
	// changed lines, the inner changes of a line and the deleted blocks of the unified view
	"&.cm-merge-b .cm-changedLine, & .cm-insertedLine": {backgroundColor: "var(--diff-added-bg)"},
	"&.cm-merge-a .cm-changedLine, & .cm-deletedChunk": {backgroundColor: "var(--diff-removed-bg)"},
	"&.cm-merge-b .cm-changedText": {background: "var(--diff-added-emph-bg)"},
	"&.cm-merge-a .cm-changedText, & .cm-deletedChunk .cm-deletedText": {background: "var(--diff-removed-emph-bg)"},
	"& .cm-deletedChunk": {padding: "0"},
	"& .cm-deletedLine": {padding: "0 8px 0 6px", textDecoration: "none"},
	"& .cm-deletedLine del, & .cm-insertedLine, & .cm-deletedLine del *": {textDecoration: "none"},
	".cm-collapsedLines": {
		backgroundColor: "var(--diff-elide-bg)",
		color: "var(--diff-elide-fg)",
		padding: "4px 16px"
	},
	".cm-collapsedLines:hover": {
		backgroundColor: "var(--diff-elide-hover-bg)",
		color: "var(--diff-elide-hover-fg)"
	},
	".cm-panels": {backgroundColor: "var(--diff-num-bg)", color: "var(--diff-fg)"},
	".cm-panels.cm-panels-top": {borderBottom: "1px solid var(--diff-border)"},
	".cm-textfield, .cm-button": {font: "inherit", fontSize: "12px"}
});

class NumberMarker extends GutterMarker {
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
}

// the old line numbers of a deleted block, one row per deleted line
class DeletedNumbersMarker extends GutterMarker {
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
}

// the old line number of each line of the new document, 0 for an added line
function oldLineNumbers(chunks) {
	return bLine => {
		let lo = 0, hi = chunks.length;
		while (lo < hi) {
			const mid = (lo + hi) >> 1;
			if (chunks[mid][2] <= bLine) lo = mid + 1;
			else hi = mid;
		}
		if (lo === 0) return bLine + 1;
		const [aPos, aLen, bPos, bLen] = chunks[lo - 1];
		if (bLine < bPos + bLen) return 0;
		return bLine - (bPos + bLen) + aPos + aLen + 1;
	};
}

// the chunks with removed lines by the document offset of the deleted block the unified view shows for them
function deletedBlocks(diff) {
	const blocks = new Map();
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

// a gutter with the old line numbers next to the new ones, for the unified view
function oldLineNumberGutter(diff) {
	const numberOf = oldLineNumbers(diff.chunks);
	const blocks = deletedBlocks(diff);
	return gutter({
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

const emphasisMark = Decoration.mark({class: "cm-changedText"});

// a wrapped line keeps its indentation on the continuation rows, up to this many columns
const MAX_HANGING_COLUMNS = 24;
const LINE_PADDING_LEFT = "6px";

// the indentation of a line in columns, or 0 for a blank line
function indentColumns(text, tabSize) {
	let columns = 0;
	for (let i = 0; i < text.length; i++) {
		const ch = text.charCodeAt(i);
		if (ch === 9) columns += tabSize - columns % tabSize;
		else if (ch === 32) columns++;
		else return Math.min(columns, MAX_HANGING_COLUMNS);
	}
	return 0;
}

function hangingIndentStyle(columns) {
	return "padding-left: calc(" + LINE_PADDING_LEFT + " + " + columns + "ch); text-indent: -" + columns + "ch";
}

// line decorations that indent the continuation rows of wrapped lines like their first row
function hangingIndent(lines, tabSize) {
	const offsets = lineOffsets(lines);
	const builder = new RangeSetBuilder();
	for (let i = 0; i < lines.length; i++) {
		const columns = indentColumns(lines[i], tabSize);
		if (columns) builder.add(offsets[i], offsets[i], Decoration.line({attributes: {style: hangingIndentStyle(columns)}}));
	}
	return EditorView.decorations.of(builder.finish());
}

// the emphasis of one side as mark decorations, the merge view's own highlighting being off
function emphasisMarks(diff, side) {
	const builder = new RangeSetBuilder();
	const ranges = (diff.emphasis || [])
		.map(c => side === "a" ? [c.fromA, c.toA] : [c.fromB, c.toB])
		.filter(([from, to]) => to > from)
		.sort((x, y) => x[0] - y[0]);
	for (const [from, to] of ranges) builder.add(from, to, emphasisMark);
	return EditorView.decorations.of(builder.finish());
}

// the emphasis ranges of each line of the old text, in offsets within the line
function emphasisByLine(diff) {
	const offsets = lineOffsets(diff.aLines);
	const byLine = new Map();
	for (const c of diff.emphasis || []) {
		for (let pos = c.fromA; pos < c.toA;) {
			const line = lineAt(offsets, pos), end = Math.min(c.toA, offsets[line + 1]);
			if (!byLine.has(line)) byLine.set(line, []);
			byLine.get(line).push([pos - offsets[line], end - offsets[line]]);
			pos = end;
		}
	}
	for (const ranges of byLine.values()) ranges.sort((x, y) => x[0] - y[0]);
	return byLine;
}

function lineAt(offsets, pos) {
	let lo = 0, hi = offsets.length - 2;
	while (lo < hi) {
		const mid = (lo + hi + 1) >> 1;
		if (offsets[mid] <= pos) lo = mid;
		else hi = mid - 1;
	}
	return lo;
}

// wraps the given ranges of the text of a row in emphasis spans, splitting text nodes at the bounds
function emphasizeRow(row, ranges) {
	const walker = document.createTreeWalker(row, NodeFilter.SHOW_TEXT);
	const nodes = [];
	let at = 0;
	for (let node = walker.nextNode(); node; node = walker.nextNode()) {
		nodes.push([node, at]);
		at += node.length;
	}
	for (const [node, start] of nodes) emphasizeNode(node, start, ranges);
}

// wraps the first range that reaches into the node and goes on with the remainder of the node
function emphasizeNode(node, nodeStart, ranges) {
	const nodeEnd = nodeStart + node.length;
	for (const [from, to] of ranges) {
		const a = Math.max(from, nodeStart) - nodeStart, b = Math.min(to, nodeEnd) - nodeStart;
		if (a >= b) continue;
		let piece = node;
		if (a > 0) piece = piece.splitText(a);
		const rest = b - a < piece.length ? piece.splitText(b - a) : null;
		const span = document.createElement("span");
		span.className = "cm-deletedText";
		piece.parentNode.insertBefore(span, piece);
		span.appendChild(piece);
		if (rest) emphasizeNode(rest, nodeStart + b, ranges);
		return;
	}
}

// Deleted lines wrap like other lines, so the rows of their gutter numbers take the measured height of each line.
// The rows are built by the merge view, so what document lines get as decorations, the emphasis and the hanging
// indent, is put into them here.
function deletedLineRows(diff, options) {
	const blocks = deletedBlocks(diff);
	const emphasis = emphasisByLine(diff);
	const hanging = options.wrap !== false;
	return ViewPlugin.fromClass(class {
		constructor(view) {
			this.measure = {
				read: v => {
					const chunks = Array.from(v.contentDOM.querySelectorAll(".cm-deletedChunk"));
					const heights = chunks.map(el => Array.from(el.querySelectorAll(".cm-deletedLine")).map(line => line.getBoundingClientRect().height));
					const markers = Array.from(v.dom.querySelectorAll(".cm-oldLineNumbers .cm-deletedNumbers"));
					const unmarked = chunks.filter(el => !el.hasAttribute("data-emphasized")).map(el => [el, blocks.get(v.posAtDOM(el))]);
					return {heights, markers, unmarked};
				},
				write: ({heights, markers, unmarked}) => {
					for (const [el, chunk] of unmarked) {
						el.setAttribute("data-emphasized", "");
						if (!chunk) continue;
						el.querySelectorAll(".cm-deletedLine").forEach((row, i) => {
							const ranges = emphasis.get(chunk[0] + i);
							if (ranges) emphasizeRow(row, ranges);
							const columns = hanging ? indentColumns(diff.aLines[chunk[0] + i] || "", options.tabSize) : 0;
							if (columns) row.style.cssText += ";" + hangingIndentStyle(columns);
						});
					}
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
			if (update.viewportChanged || update.geometryChanged || update.docChanged || update.transactions.length) {
				update.view.requestMeasure(this.measure);
			}
		}
	});
}

function languageExtensions(options) {
	if (!options.highlight) return [];
	const extensions = [];
	if (typeof KnowWEEditor !== "undefined" && KnowWEEditor.highlightStyle) {
		extensions.push(syntaxHighlighting(KnowWEEditor.highlightStyle()));
	}
	if ((options.language ?? "wiki") === "wiki" && typeof KnowWEWikiLanguage !== "undefined") {
		extensions.push(KnowWEWikiLanguage.wiki());
	}
	return extensions;
}

function sideExtensions(options, lines) {
	return [
		EditorState.readOnly.of(true),
		EditorView.editable.of(false),
		// the content stays reachable by keyboard, so search works in the read-only view
		EditorView.contentAttributes.of({tabindex: "0"}),
		highlightSpecialChars(),
		search({top: true}),
		keymap.of(searchKeymap),
		languageExtensions(options),
		options.wrap === false ? [] : [EditorView.lineWrapping, hangingIndent(lines, options.tabSize)],
		theme
	];
}

function digits(diff) {
	return String(Math.max(diff.aLines.length, diff.bLines.length)).length;
}

/**
 * Creates the CodeMirror view of a computed diff in the parent element. Options: root (the shadow root the view is
 * mounted in), layout "unified" or "split", language, highlight (whether to color the text at all), wrap (wrapped
 * lines keep their indentation on the continuation rows), tabSize, contextLines (-1 shows every line). Returns the
 * view, the editor showing the new text, an align function that brings the heights of the view up to date at once,
 * and a destroy function.
 */
export function createDiffView(parent, diff, options = {}) {
	options = {tabSize: 4, ...options};
	const changes = diff.changes.map(c => new Change(c.fromA, c.toA, c.fromB, c.toB));
	const diffConfig = {override: () => changes};
	const collapse = options.contextLines === -1 ? undefined : {margin: options.contextLines ?? 3, minSize: 10};
	parent.style.setProperty("--diff-gutter-width", (digits(diff) + 2) + "ch");
	let view;
	if (options.layout === "split") {
		view = new MergeView({
			a: {doc: diff.docA, extensions: [sideExtensions(options, diff.aLines), lineNumbers(), emphasisMarks(diff, "a")]},
			b: {doc: diff.docB, extensions: [sideExtensions(options, diff.bLines), lineNumbers(), emphasisMarks(diff, "b")]},
			parent,
			root: options.root,
			diffConfig,
			collapseUnchanged: collapse,
			highlightChanges: false,
			gutter: true
		});
	}
	else {
		view = new EditorView({
			parent,
			root: options.root,
			state: EditorState.create({
				doc: diff.docB,
				extensions: [
					oldLineNumberGutter(diff),
					lineNumbers(),
					sideExtensions(options, diff.bLines),
					deletedLineRows(diff, options),
					emphasisMarks(diff, "b"),
					unifiedMergeView({
						original: diff.docA,
						mergeControls: false,
						diffConfig,
						collapseUnchanged: collapse,
						// the emphasis is drawn by emphasisMarks and deletedLineRows, only where it helps
						highlightChanges: false,
						gutter: true,
						syntaxHighlightDeletions: true
					})
				]
			})
		});
	}
	const split = options.layout === "split";
	return {
		view,
		editor: split ? view.b : view,
		// the split view pads its sides in a frame callback, which callers measuring right away cannot wait for
		align: () => split && typeof view.measure === "function" && view.measure(),
		destroy: () => view.destroy()
	};
}
