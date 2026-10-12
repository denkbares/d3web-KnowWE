/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/**
 * Mounts the CodeMirror page editor on the form rendered by editors/codemirror.jsp.
 */
(function () {
	"use strict";

	const WRAP_STORAGE_KEY = "knowwe.editor.wrap";

	function init(root) {
		if (typeof CodeMirror === "undefined") {
			console.error("CodeMirror bundle is not available, falling back to the plain textarea");
			root.classList.add("knowwe-editor-fallback");
			return;
		}

		const {Compartment, EditorState, RangeSetBuilder} = CodeMirror.state;
		const {
			EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter, drawSelection,
			dropCursor, rectangularSelection, crosshairCursor, highlightSpecialChars, ViewPlugin, Decoration
		} = CodeMirror.view;
		const {defaultKeymap, history, historyKeymap, indentWithTab, undo, redo} = CodeMirror.commands;
		const {searchKeymap, highlightSelectionMatches, openSearchPanel, closeSearchPanel, searchPanelOpen} = CodeMirror.search;
		const {
			bracketMatching, indentOnInput, indentUnit, foldGutter, foldKeymap, HighlightStyle, syntaxHighlighting,
			syntaxTree
		} = CodeMirror.language;
		const {closeBrackets, closeBracketsKeymap} = CodeMirror.autocomplete;
		const {tags} = CodeMirror.lezer.highlight;

		const form = root.closest("form");
		const source = root.querySelector(".knowwe-editor-source");
		const surface = root.querySelector(".knowwe-editor-surface");
		const position = root.querySelector(".knowwe-editor-position");
		const dirtyLabel = root.querySelector(".knowwe-editor-dirty");

		const wrapping = new Compartment();
		let submitting = false;

		/**
		 * Guesses the indentation unit from the indented lines of the text, so new indentation matches the page.
		 * Tabs win over spaces on a tie. Among space indented lines, the smallest common leading width wins, since
		 * deeper levels are multiples of the unit.
		 */
		function detectIndentUnit(text) {
			const widths = new Map();
			let tabs = 0, spaces = 0;
			for (const line of text.split("\n", 5000)) {
				if (line.charCodeAt(0) === 9) {
					tabs++;
				} else if (line.charCodeAt(0) === 32) {
					const width = line.length - line.trimStart().length;
					if (width === line.length || width > 8) continue;
					spaces++;
					widths.set(width, (widths.get(width) || 0) + 1);
				}
			}
			if (tabs >= spaces) return "\t";
			const threshold = Math.max(...widths.values()) / 4;
			let best = 4;
			for (const [width, count] of widths) {
				if (count >= threshold && width < best) best = width;
			}
			return " ".repeat(best);
		}

		const theme = EditorView.theme({
			"&": {
				height: "100%",
				// the selection layer is drawn at a negative z-index, so the root must be its own stacking context
				isolation: "isolate",
				fontSize: "14px",
				backgroundColor: "var(--bg-color-primary, #fff)",
				color: "var(--text-color-primary, #000)"
			},
			".cm-scroller": {
				fontFamily: "ui-monospace, 'JetBrains Mono', Menlo, Monaco, Consolas, 'Courier New', monospace",
				lineHeight: "1.5"
			},
			".cm-content": {
				padding: "8px 0",
				caretColor: "var(--text-color-primary, #000)"
			},
			".cm-cursor, .cm-dropCursor": {
				borderLeftColor: "var(--text-color-primary, #000)"
			},
			// the selectors mirror the base theme, which is more specific than a plain class selector
			".cm-selectionBackground": {
				background: "rgba(66, 139, 230, 0.22)"
			},
			"&.cm-focused > .cm-scroller > .cm-selectionLayer .cm-selectionBackground": {
				background: "rgba(66, 139, 230, 0.38)"
			},
			// must stay translucent, an opaque active line would cover the selection layer underneath
			".cm-activeLine": {
				backgroundColor: "rgba(66, 139, 230, 0.07)"
			},
			".cm-markup-line": {
				backgroundColor: "var(--syntax-block-bg, rgba(26, 55, 102, 0.022))"
			},
			// stacks the active line on top of the block tint, both set the background of the same line element
			".cm-markup-line.cm-activeLine": {
				backgroundImage: "linear-gradient(rgba(66, 139, 230, 0.07), rgba(66, 139, 230, 0.07))"
			},
			".cm-gutters": {
				backgroundColor: "var(--bg-color-secondary, #f6f6f6)",
				color: "var(--text-color-secondary, #888)",
				borderRight: "1px solid var(--border-color-default, #e2e2e9)"
			},
			".cm-foldGutter .cm-gutterElement": {
				padding: "0 4px",
				cursor: "pointer"
			},
			".cm-activeLineGutter": {
				backgroundColor: "var(--bg-color-secondary-shade, #eee)",
				color: "var(--text-color-primary, #000)"
			},
			".cm-lineNumbers .cm-gutterElement": {
				padding: "0 10px 0 14px",
				minWidth: "40px"
			},
			".cm-selectionMatch": {
				backgroundColor: "var(--highlight-color-faint-yellow, #fdf6e3)"
			},
			".cm-matchingBracket, &.cm-focused .cm-matchingBracket": {
				backgroundColor: "var(--highlight-color-light, #d9edf7)",
				outline: "1px solid var(--border-color-edit-blue, #9acbfb)"
			},
			".cm-panels": {
				backgroundColor: "var(--bg-color-secondary, #f6f6f6)",
				color: "var(--text-color-primary, #000)",
				borderBottom: "1px solid var(--border-color-default, #e2e2e9)"
			},
			".cm-panels.cm-panels-bottom": {
				borderTop: "1px solid var(--border-color-default, #e2e2e9)",
				borderBottom: "none"
			},
			".cm-panel.cm-search": {
				display: "flex",
				flexWrap: "wrap",
				alignItems: "center",
				gap: "4px 6px",
				padding: "5px 28px 5px 8px"
			},
			".cm-panel.cm-search br": {
				display: "none"
			},
			".cm-panel.cm-search input[type=text], .cm-panel.cm-search input:not([type])": {
				width: "180px"
			},
			".cm-panel.cm-search input, .cm-panel.cm-search button": {
				fontSize: "12px",
				height: "24px",
				lineHeight: "1",
				padding: "0 7px",
				margin: "0",
				borderRadius: "3px",
				border: "1px solid var(--border-color-default, #e2e2e9)",
				backgroundImage: "none",
				backgroundColor: "var(--bg-color-primary, #fff)",
				color: "var(--text-color-primary, #000)"
			},
			".cm-panel.cm-search label": {
				display: "inline-flex",
				alignItems: "center",
				gap: "3px",
				fontSize: "12px",
				margin: "0 2px",
				fontWeight: "normal"
			},
			".cm-panel.cm-search label input[type=checkbox]": {
				height: "auto",
				margin: "0"
			},
			".cm-panel.cm-search button[name=close]": {
				top: "4px",
				right: "8px",
				fontSize: "16px",
				color: "var(--text-color-secondary, #888)"
			},
			".cm-searchMatch": {
				backgroundColor: "var(--highlight-color-dark, rgba(248, 136, 25, 0.4))"
			},
			".cm-searchMatch.cm-searchMatch-selected": {
				backgroundColor: "var(--highlight-color-light, #d9edf7)",
				outline: "1px solid var(--border-color-edit-blue, #9acbfb)"
			}
		});

		// Colors for syntax nodes of the wiki language and of embedded markup languages, see KnowWE-WikiLanguage.js.
		// Embedded languages use the standard Lezer tags. Color marks markup, statement keywords, annotations,
		// definitions, values and comments, bold marks markup delimiters, block keywords and type definitions.
		// References and the remaining syntax keep the text color, secondary syntax is muted.
		const text = "var(--syntax-text, #000)";
		const muted = "var(--syntax-muted, #868c95)";
		const markup = "var(--syntax-markup, #1a3766)";
		const definition = "var(--syntax-definition, #45607f)";
		const annotation = "var(--syntax-annotation, #0f6b74)";
		const value = "var(--syntax-value, #357a28)";
		const highlightStyle = HighlightStyle.define([
			{tag: tags.heading, fontWeight: "bold", color: text},
			{tag: tags.strong, fontWeight: "bold"},
			{tag: tags.emphasis, fontStyle: "italic"},
			{tag: tags.monospace, color: text, backgroundColor: "var(--syntax-code-bg, #f1f3f5)", borderRadius: "3px"},
			{tag: tags.link, color: "var(--syntax-link, #173a64)"},
			{tag: [tags.processingInstruction, tags.escape, tags.contentSeparator, tags.list], color: muted},
			{tag: [tags.punctuation, tags.meta], color: muted},
			{tag: tags.tagName, color: markup, fontWeight: "600"},
			{tag: tags.annotation, color: annotation, fontWeight: "600"},
			{tag: tags.attributeName, color: annotation},
			{tag: tags.definitionKeyword, color: markup, fontWeight: "600"},
			{tag: tags.controlKeyword, color: markup},
			{tag: tags.definition(tags.typeName), color: definition, fontWeight: "600"},
			{tag: [tags.definition(tags.propertyName), tags.definition(tags.variableName)], color: definition},
			{tag: [tags.string, tags.number, tags.bool, tags.atom], color: value},
			// attribute values are strings in Lezer, but annotation values read as plain text next to their name
			{tag: tags.attributeValue, color: text},
			{tag: tags.comment, color: "var(--syntax-comment, #6b737d)", fontStyle: "italic"},
			{tag: tags.invalid, textDecoration: "underline wavy var(--syntax-invalid, #e81818)"}
		]);

		// faint background on every line of a markup block, so embedded languages read as their own region
		const markupLine = Decoration.line({class: "cm-markup-line"});
		const markupTint = ViewPlugin.fromClass(class {
			constructor(view) {
				this.decorations = this.build(view);
			}

			update(update) {
				if (update.docChanged || update.viewportChanged
					|| syntaxTree(update.startState) !== syntaxTree(update.state)) {
					this.decorations = this.build(update.view);
				}
			}

			build(view) {
				const builder = new RangeSetBuilder();
				const doc = view.state.doc;
				let last = -1;
				for (const {from, to} of view.visibleRanges) {
					syntaxTree(view.state).iterate({
						from, to, enter(node) {
							if (node.name !== "Markup") return;
							const end = doc.lineAt(Math.min(node.to, to)).number;
							for (let n = doc.lineAt(Math.max(node.from, from)).number; n <= end; n++) {
								const line = doc.line(n);
								if (line.from > last) {
									builder.add(line.from, line.from, markupLine);
									last = line.from;
								}
							}
							return false;
						}
					});
				}
				return builder.finish();
			}
		}, {decorations: plugin => plugin.decorations});

		// the wiki language is optional, so the editor still works if its script is missing
		const language = typeof KnowWEWikiLanguage !== "undefined" ? [KnowWEWikiLanguage.wiki()] : [];
		if (language.length === 0) console.warn("KnowWE wiki language is not available, editing without highlighting");

		const updateStatus = EditorView.updateListener.of(update => {
			if (update.docChanged || update.selectionSet) {
				const head = update.state.selection.main.head;
				const line = update.state.doc.lineAt(head);
				position.textContent = "Ln " + line.number + ", Col " + (head - line.from + 1)
					+ "  ·  " + update.state.doc.lines + " lines";
			}
			if (update.docChanged) {
				const dirty = !update.state.doc.eq(initialDoc);
				dirtyLabel.hidden = !dirty;
				root.classList.toggle("knowwe-editor-is-dirty", dirty);
			}
		});

		const state = EditorState.create({
			doc: source.value,
			extensions: [
				lineNumbers(),
				foldGutter(),
				highlightActiveLineGutter(),
				highlightSpecialChars(),
				history(),
				drawSelection(),
				dropCursor(),
				EditorState.allowMultipleSelections.of(true),
				indentOnInput(),
				bracketMatching(),
				closeBrackets(),
				rectangularSelection(),
				crosshairCursor(),
				highlightActiveLine(),
				highlightSelectionMatches(),
				syntaxHighlighting(highlightStyle),
				language,
				markupTint,
				EditorState.tabSize.of(4),
				indentUnit.of(detectIndentUnit(source.value)),
				wrapping.of(isWrapping() ? EditorView.lineWrapping : []),
				keymap.of([
					{key: "Mod-s", run: () => { save(); return true; }},
					...closeBracketsKeymap,
					...defaultKeymap,
					...searchKeymap,
					...historyKeymap,
					...foldKeymap,
					indentWithTab
				]),
				theme,
				updateStatus
			]
		});
		const initialDoc = state.doc;
		const view = new EditorView({parent: surface, state});

		function isDirty() {
			return !view.state.doc.eq(initialDoc);
		}

		// seed the status bar once, the update listener only fires on changes
		position.textContent = "Ln 1, Col 1  ·  " + view.state.doc.lines + " lines";

		function isWrapping() {
			try {
				return localStorage.getItem(WRAP_STORAGE_KEY) !== "false";
			} catch (e) {
				return true;
			}
		}

		function setWrapping(enabled) {
			view.dispatch({effects: wrapping.reconfigure(enabled ? EditorView.lineWrapping : [])});
			root.querySelector("[data-editor-cmd=wrap]").classList.toggle("active", enabled);
			try {
				localStorage.setItem(WRAP_STORAGE_KEY, String(enabled));
			} catch (e) {
				// storage is optional
			}
		}

		function save() {
			const ok = form.querySelector("button[name=ok]");
			if (ok) ok.click();
		}

		function syncSource() {
			source.value = view.state.doc.toString();
		}

		function fitHeight() {
			const top = surface.getBoundingClientRect().top + window.scrollY;
			const statusHeight = root.querySelector(".knowwe-editor-statusbar").offsetHeight;
			const available = window.innerHeight - top - statusHeight - 24;
			surface.style.height = Math.max(320, available) + "px";
		}

		root.querySelector("[data-editor-cmd=wrap]").classList.toggle("active", isWrapping());

		root.addEventListener("click", event => {
			const button = event.target.closest("[data-editor-cmd]");
			if (!button) return;
			event.preventDefault();
			switch (button.dataset.editorCmd) {
				case "undo":
					undo(view);
					break;
				case "redo":
					redo(view);
					break;
				case "find":
					if (searchPanelOpen(view.state)) {
						closeSearchPanel(view);
					} else {
						openSearchPanel(view);
					}
					break;
				case "switch":
					button.parentElement.classList.toggle("open");
					return;
				case "wrap":
					setWrapping(!isWrapping());
					break;
			}
			view.focus();
		});

		document.addEventListener("click", event => {
			const open = root.querySelector(".knowwe-editor-switch.open");
			if (open && !open.contains(event.target)) open.classList.remove("open");
		});

		root.querySelectorAll("a.editor-type").forEach(link => {
			link.addEventListener("click", event => {
				event.preventDefault();
				if (typeof Wiki !== "undefined" && typeof Wiki.prefs === "function") {
					if (isDirty() && !window.confirm("Discard the unsaved changes and switch the editor?")) return;
					Wiki.prefs("editor", link.dataset.editor);
					submitting = true;
					window.location.reload();
				}
			});
		});

		form.addEventListener("submit", () => {
			submitting = true;
			syncSource();
		});

		window.addEventListener("beforeunload", event => {
			if (!submitting && isDirty()) {
				event.preventDefault();
				event.returnValue = "";
			}
		});

		window.addEventListener("resize", fitHeight);
		fitHeight();
		view.focus();
	}

	function ready() {
		document.querySelectorAll(".knowwe-editor").forEach(init);
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", ready);
	} else {
		ready();
	}
})();
