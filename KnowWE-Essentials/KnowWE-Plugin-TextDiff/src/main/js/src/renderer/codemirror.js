import {EditorState} from "@codemirror/state";
import {EditorView, GutterMarker, ViewPlugin, gutter, highlightSpecialChars, keymap, lineNumbers} from "@codemirror/view";
import {syntaxHighlighting} from "@codemirror/language";
import {search, searchKeymap} from "@codemirror/search";
import {Change, MergeView, unifiedMergeView} from "@codemirror/merge";

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
	".cm-changeGutter": {width: "3px", paddingLeft: "1px"},
	// changed lines, the inner changes of a line and the deleted blocks of the unified view
	"&.cm-merge-b .cm-changedLine, & .cm-insertedLine": {backgroundColor: "var(--diff-added-bg)"},
	"&.cm-merge-a .cm-changedLine, & .cm-deletedChunk": {backgroundColor: "var(--diff-removed-bg)"},
	"&.cm-merge-b .cm-changedText": {background: "var(--diff-added-emph-bg)"},
	"&.cm-merge-a .cm-changedText, & .cm-deletedChunk .cm-deletedText": {background: "var(--diff-removed-emph-bg)"},
	"& .cm-deletedChunk": {padding: "0"},
	"& .cm-deletedLine": {padding: "0 8px 0 6px", textDecoration: "none"},
	"& .cm-deletedLine del, & .cm-insertedLine, & .cm-deletedLine del *": {textDecoration: "none"},
	".cm-oldLineNumbers .cm-deletedNumbers": {backgroundColor: "var(--diff-removed-num-bg)"},
	"&.cm-merge-b .cm-changedLineGutter": {background: "var(--diff-added-num-bg)"},
	"&.cm-merge-a .cm-changedLineGutter, & .cm-deletedLineGutter": {background: "var(--diff-removed-num-bg)"},
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

// Deleted lines wrap like other lines, so the rows of their gutter numbers take the measured height of each line.
function deletedLineHeights() {
	return ViewPlugin.fromClass(class {
		constructor(view) {
			this.measure = {
				read: v => {
					const heights = Array.from(v.contentDOM.querySelectorAll(".cm-deletedChunk"))
						.map(el => Array.from(el.querySelectorAll(".cm-deletedLine")).map(line => line.getBoundingClientRect().height));
					const markers = Array.from(v.dom.querySelectorAll(".cm-oldLineNumbers .cm-deletedNumbers"));
					return {heights, markers};
				},
				write: ({heights, markers}) => {
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

function sideExtensions(options) {
	return [
		EditorState.readOnly.of(true),
		EditorView.editable.of(false),
		// the content stays reachable by keyboard, so search works in the read-only view
		EditorView.contentAttributes.of({tabindex: "0"}),
		highlightSpecialChars(),
		search({top: true}),
		keymap.of(searchKeymap),
		languageExtensions(options),
		options.wrap === false ? [] : EditorView.lineWrapping,
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
export function createDiffView(parent, diff, options = {}) {
	const changes = diff.changes.map(c => new Change(c.fromA, c.toA, c.fromB, c.toB));
	const diffConfig = {override: () => changes};
	const collapse = options.contextLines === -1 ? undefined : {margin: options.contextLines ?? 3, minSize: 4};
	parent.style.setProperty("--diff-gutter-width", (digits(diff) + 2) + "ch");
	let view;
	if (options.layout === "split") {
		view = new MergeView({
			a: {doc: diff.docA, extensions: [sideExtensions(options), lineNumbers()]},
			b: {doc: diff.docB, extensions: [sideExtensions(options), lineNumbers()]},
			parent,
			root: options.root,
			diffConfig,
			collapseUnchanged: collapse,
			highlightChanges: true,
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
					sideExtensions(options),
					deletedLineHeights(),
					unifiedMergeView({
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
	}
	return {view, destroy: () => view.destroy()};
}
