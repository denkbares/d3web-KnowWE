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

		const {Compartment, EditorState} = CodeMirror.state;
		const {
			EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter, drawSelection,
			dropCursor, rectangularSelection, crosshairCursor, highlightSpecialChars
		} = CodeMirror.view;
		const {defaultKeymap, history, historyKeymap, indentWithTab, undo, redo} = CodeMirror.commands;
		const {searchKeymap, highlightSelectionMatches, openSearchPanel, closeSearchPanel, searchPanelOpen} = CodeMirror.search;
		const {bracketMatching, indentOnInput, foldGutter, foldKeymap, HighlightStyle, syntaxHighlighting} = CodeMirror.language;
		const {closeBrackets, closeBracketsKeymap} = CodeMirror.autocomplete;
		const {tags} = CodeMirror.lezer.highlight;

		const form = root.closest("form");
		const source = root.querySelector(".knowwe-editor-source");
		const surface = root.querySelector(".knowwe-editor-surface");
		const position = root.querySelector(".knowwe-editor-position");
		const dirtyLabel = root.querySelector(".knowwe-editor-dirty");

		const initialText = source.value;
		const wrapping = new Compartment();
		let submitting = false;

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
		// Embedded languages use the standard Lezer tags, so the common ones are styled here as well.
		const highlightStyle = HighlightStyle.define([
			{tag: tags.heading, fontWeight: "bold", color: "var(--text-color-default-head, #036)"},
			{tag: tags.strong, fontWeight: "bold"},
			{tag: tags.emphasis, fontStyle: "italic"},
			{tag: tags.monospace, backgroundColor: "var(--bg-color-secondary, #f6f6f6)", color: "var(--color-pink, #b95d71)"},
			{tag: tags.link, color: "var(--text-color-primary-link, #173a64)", textDecoration: "underline"},
			{tag: tags.processingInstruction, color: "var(--color-purple, #6f42c1)"},
			{tag: tags.escape, color: "var(--text-color-secondary, #888)"},
			{tag: tags.contentSeparator, color: "var(--text-color-secondary, #888)"},
			{tag: tags.list, color: "var(--color-orange, #e08a00)", fontWeight: "bold"},
			{tag: tags.punctuation, color: "var(--text-color-secondary, #888)"},
			{tag: tags.keyword, color: "var(--color-blue, #1a56b0)", fontWeight: "bold"},
			{tag: tags.annotation, color: "var(--color-green-dark, #006400)", fontWeight: "bold"},
			{tag: tags.attributeValue, color: "var(--color-green, #008000)"},
			{tag: tags.attributeName, color: "var(--color-green-dark, #006400)"},
			{tag: tags.comment, color: "var(--text-color-secondary, #888)", fontStyle: "italic"},
			{tag: tags.string, color: "var(--color-green, #008000)"},
			{tag: tags.number, color: "var(--color-pink, #b95d71)"},
			{tag: tags.bool, color: "var(--color-pink, #b95d71)"},
			{tag: tags.operator, color: "var(--text-color-primary-light, #333)"},
			{tag: tags.typeName, color: "var(--color-purple, #6f42c1)"},
			{tag: tags.definition(tags.variableName), color: "var(--color-blue, #1a56b0)"},
			{tag: tags.variableName, color: "var(--text-color-primary, #000)"},
			{tag: tags.invalid, textDecoration: "underline wavy var(--color-red, #c00)"}
		]);

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
				const dirty = update.state.doc.toString() !== initialText;
				dirtyLabel.hidden = !dirty;
				root.classList.toggle("knowwe-editor-is-dirty", dirty);
			}
		});

		const view = new EditorView({
			parent: surface,
			state: EditorState.create({
				doc: initialText,
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
			})
		});

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
			if (!submitting && view.state.doc.toString() !== initialText) {
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
