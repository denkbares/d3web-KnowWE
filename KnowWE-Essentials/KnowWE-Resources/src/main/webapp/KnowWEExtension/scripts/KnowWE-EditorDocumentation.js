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
 * Quick documentation of markups and annotations in the CodeMirror editors of KnowWE, in the page editor as well as
 * in the edit mode. F1 shows the documentation for the caret: for a markup name like `%%Cable` or an annotation name
 * like `@Plug` at the caret, else for the annotation of the line, else for the markup around the caret. The documentation comes as HTML from the MarkupDocumentationAction,
 * the same the completion shows next to suggested markups and annotations. Its examples are highlighted like the
 * editor highlights wiki text, see upgrade().
 */
window.KnowWEEditorDocumentation = (function () {
	"use strict";

	const ACTION = "action/MarkupDocumentationAction";

	function isAvailable() {
		return typeof CodeMirror !== "undefined" && typeof KnowWEEditor !== "undefined"
			&& typeof KnowWEWikiLanguage !== "undefined";
	}

	// the replies of the server per markup and annotation, as promises of the HTML or null
	const cache = new Map();

	/** Fetches the documentation of the markup or of its annotation as HTML, resolves to null if there is none. */
	function fetchDocumentation(markup, annotation) {
		const key = markup.toLowerCase() + "\n" + (annotation ?? "").toLowerCase();
		if (!cache.has(key)) {
			const params = new URLSearchParams({markup: markup});
			if (annotation) params.set("annotation", annotation);
			const reply = fetch(ACTION + "?" + params, {credentials: "same-origin"})
				.then(response => response.ok ? response.text() : null)
				.catch(() => {
					// a failed request is asked again next time
					cache.delete(key);
					return null;
				});
			cache.set(key, reply);
		}
		return cache.get(key);
	}

	// built on first use, so loading this script does not fail without the CodeMirror bundle
	let shared = null;

	function sharedParts() {
		if (shared) return shared;
		const {EditorView} = CodeMirror.view;
		shared = {
			exampleTheme: EditorView.theme({
				"&": {
					fontSize: "12px",
					backgroundColor: "var(--bg-color-secondary, #f6f6f6)",
					border: "1px solid var(--border-color-default, #e2e2e9)",
					borderRadius: "3px"
				},
				"&.cm-focused": {outline: "none"},
				".cm-content": {padding: "4px 0"},
				".cm-line": {padding: "0 8px"},
				".cm-scroller": {
					fontFamily: "ui-monospace, 'JetBrains Mono', Menlo, Monaco, Consolas, 'Courier New', monospace",
					lineHeight: "1.4"
				}
			})
		};
		addStyle();
		return shared;
	}

	/**
	 * Styles of the documentation, global because the completion shows it in its info panel as well, which is
	 * created by the completion and not by this script.
	 */
	function addStyle() {
		const style = document.createElement("style");
		style.textContent = `
.cm-tooltip.cm-knowwe-doc-tooltip, .cm-tooltip.cm-completionInfo:has(.knowwe-doc) {
	box-sizing: border-box; max-width: min(560px, 90vw); max-height: min(420px, 60vh); overflow: auto; padding: 8px 12px;
	font-family: var(--font-family, system-ui, sans-serif); font-size: 13px; line-height: 1.45; white-space: normal;
}
.knowwe-doc code {
	padding: 0 3px; border-radius: 3px; background-color: var(--syntax-code-bg, #f1f3f5); color: inherit;
	font-family: ui-monospace, 'JetBrains Mono', Menlo, Monaco, Consolas, monospace; font-size: 12px;
}
.knowwe-doc-title { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; margin-bottom: 4px; font-weight: 600; }
.knowwe-doc-title > code { font-size: 13px; color: var(--syntax-markup, #1a3766); }
.knowwe-doc-icon { width: 16px; height: 16px; object-fit: contain; }
.knowwe-doc-context { font-weight: normal; color: var(--text-color-secondary, #888); }
.knowwe-doc-badge {
	padding: 0 5px; border-radius: 8px; font-size: 11px; font-weight: normal; line-height: 16px;
	color: var(--text-color-secondary, #666); border: 1px solid var(--border-color-default, #e2e2e9);
}
.knowwe-doc-required { color: var(--syntax-annotation, #0f6b74); border-color: currentColor; }
.knowwe-doc-deprecated { margin: 2px 0 4px; color: var(--syntax-invalid, #c0392b); }
.knowwe-doc-text ul { margin: 4px 0; padding-left: 18px; }
.knowwe-doc-heading {
	margin: 10px 0 3px; font-size: 11px; font-weight: 600; letter-spacing: 0.04em; text-transform: uppercase;
	color: var(--text-color-secondary, #888);
}
.knowwe-doc pre.knowwe-doc-example {
	margin: 0 0 6px; padding: 4px 8px; overflow: auto; font-size: 12px; white-space: pre;
	background-color: var(--bg-color-secondary, #f6f6f6); border: 1px solid var(--border-color-default, #e2e2e9);
	border-radius: 3px;
}
.knowwe-doc-example-view { margin: 0 0 6px; }
.knowwe-doc-annotations, .knowwe-doc-values { margin: 0; padding: 0; list-style: none; }
.knowwe-doc-annotations > li, .knowwe-doc-values > li { padding: 2px 4px; margin: 0 -4px; border-radius: 3px; }
.knowwe-doc-annotations > li.knowwe-doc-current { background-color: var(--highlight-color-light, #d9edf7); }
.cm-knowwe-doc-tooltip .knowwe-doc-annotations > li, .cm-completionInfo .knowwe-doc-annotations > li { cursor: pointer; }
.cm-knowwe-doc-tooltip .knowwe-doc-annotations > li:hover, .cm-completionInfo .knowwe-doc-annotations > li:hover { background-color: var(--bg-color-secondary-shade, #eee); }
.knowwe-doc-annotations .knowwe-doc-badge, .knowwe-doc-values .knowwe-doc-summary { margin-left: 6px; }
.knowwe-doc-summary { color: var(--text-color-secondary, #666); }
.knowwe-doc-annotations .knowwe-doc-summary { display: block; margin-left: 2px; font-size: 12px; }
.knowwe-doc-links:not(:empty) { margin-top: 8px; padding-top: 6px; border-top: 1px solid var(--border-color-default, #e2e2e9); }
.knowwe-doc-links a { margin-right: 12px; }
.knowwe-doc-links a::before { content: "↗ "; }
.knowwe-doc-hint { margin-top: 6px; font-size: 11px; color: var(--text-color-secondary, #888); }
`;
		document.head.appendChild(style);
	}

	/**
	 * Replaces the examples in the documentation element by read-only editors that highlight the wiki text like the
	 * editor does. Returns a function that destroys these editors.
	 */
	function upgrade(element) {
		if (!isAvailable()) return () => {};
		const {EditorState} = CodeMirror.state;
		const {EditorView} = CodeMirror.view;
		const {syntaxHighlighting} = CodeMirror.language;
		const parts = sharedParts();
		const views = [];
		for (const pre of element.querySelectorAll("pre.knowwe-doc-example")) {
			const wrapper = document.createElement("div");
			wrapper.className = "knowwe-doc-example-view";
			pre.replaceWith(wrapper);
			views.push(new EditorView({
				parent: wrapper,
				state: EditorState.create({
					doc: pre.textContent,
					extensions: [
						KnowWEWikiLanguage.wiki(),
						syntaxHighlighting(KnowWEEditor.highlightStyle()),
						EditorState.readOnly.of(true),
						EditorView.editable.of(false),
						parts.exampleTheme
					]
				})
			}));
		}
		return () => views.forEach(view => view.destroy());
	}

	/**
	 * Creates the element of a documentation, `html` as sent by the server. Clicking an annotation of a markup or the
	 * markup of an annotation shows its documentation in place.
	 */
	function documentationElement(html, hint) {
		const dom = document.createElement("div");
		let destroyExamples = () => {};

		function show(content) {
			destroyExamples();
			dom.innerHTML = content;
			destroyExamples = upgrade(dom);
			if (hint) {
				const hintElement = document.createElement("div");
				hintElement.className = "knowwe-doc-hint";
				hintElement.textContent = hint;
				dom.appendChild(hintElement);
			}
		}

		dom.addEventListener("mousedown", event => {
			// keeps the focus and the selection of the editor
			if (!event.target.closest("a[target]")) event.preventDefault();
		});
		dom.addEventListener("click", event => {
			const doc = dom.querySelector(".knowwe-doc");
			if (!doc) return;
			const markupLink = event.target.closest("a[data-markup]");
			const annotationItem = event.target.closest(".knowwe-doc-annotations > li[data-annotation]");
			if (!markupLink && !annotationItem) return;
			event.preventDefault();
			const markup = doc.dataset.markup;
			fetchDocumentation(markup, annotationItem ? annotationItem.dataset.annotation : null)
				.then(content => content && dom.isConnected && show(content));
		});
		show(html);
		return {dom, destroy: () => destroyExamples()};
	}

	/**
	 * Returns what to document at the position: the markup name or annotation name at the position with its range, or
	 * with `context`, also the annotation of the line or the markup around the position. Returns null if there is
	 * nothing to document.
	 */
	function targetAt(state, pos, side, context) {
		const {syntaxTree} = CodeMirror.language;
		for (let node = syntaxTree(state).resolveInner(pos, side); node; node = node.parent) {
			if (node.name === "MarkupOpen") {
				const text = state.sliceDoc(node.from, node.to);
				const match = /^(\s*%%)([\w:-]+)/u.exec(text);
				if (!match) return null;
				const from = node.from + match[1].length - 2;
				const to = from + 2 + match[2].length;
				if (!context && (pos < from || pos > to)) return null;
				return {markup: match[2], from, to};
			}
		}

		const block = KnowWEWikiLanguage.markupBlock(state, pos);
		if (!block) return null;
		// annotations of block markups start a line, those of inline markups follow a blank
		const line = state.doc.lineAt(pos);
		const annotation = /(^|\s)@([\w-]+)/gu;
		let match, found = null;
		while ((match = annotation.exec(line.text))) {
			const from = line.from + match.index + match[1].length;
			const to = from + 1 + match[2].length;
			if (from < block.from || to > block.to) continue;
			if (pos >= from && pos <= to) return {markup: block.name, annotation: match[2], from, to};
			if (context && from <= pos) found = {markup: block.name, annotation: match[2], from, to};
		}
		if (!context) return null;
		return found ?? {markup: block.name, from: pos, to: pos};
	}

	/** Returns a promise of the tooltip for the target, or of null if the server has no documentation for it. */
	function tooltipFor(target, hint) {
		return fetchDocumentation(target.markup, target.annotation).then(html => {
			if (!html) return null;
			return {
				pos: target.from,
				end: target.to,
				above: false,
				create() {
					const element = documentationElement(html, hint);
					element.dom.classList.add("cm-knowwe-doc-tooltip");
					return element;
				}
			};
		});
	}

	/** The documentation shown on F1 until the text or the selection changes, Escape closes it. */
	function keyTooltip() {
		const {StateField, StateEffect, Prec} = CodeMirror.state;
		const {keymap, showTooltip} = CodeMirror.view;
		const set = StateEffect.define();
		const field = StateField.define({
			create: () => null,
			update(tooltip, tr) {
				for (const effect of tr.effects) {
					if (effect.is(set)) return effect.value;
				}
				return tr.docChanged || tr.selection ? null : tooltip;
			},
			provide: field => showTooltip.from(field)
		});

		function show(view) {
			const {state} = view;
			const head = state.selection.main.head;
			const target = targetAt(state, head, -1, true);
			if (!target) return true;
			tooltipFor(target, "Esc closes the documentation.").then(tooltip => {
				// the caret moved away while the server answered
				if (!tooltip || view.state.selection.main.head !== head || view.state.doc !== state.doc) return;
				view.dispatch({effects: set.of({...tooltip, pos: head, end: undefined})});
			});
			return true;
		}

		function close(view) {
			if (!view.state.field(field, false)) return false;
			view.dispatch({effects: set.of(null)});
			return true;
		}

		return [field, Prec.high(keymap.of([
			{key: "F1", run: show, preventDefault: true},
			{key: "Escape", run: close}
		]))];
	}

	function extensions() {
		sharedParts();
		return keyTooltip();
	}

	if (isAvailable() && KnowWEEditor.registerExtension) {
		// one instance of the extensions for all editors, so CodeMirror sees the same ones
		let instance = null;
		KnowWEEditor.registerExtension(() => instance ??= extensions());
	}

	return {isAvailable, fetchDocumentation, documentationElement, upgrade, targetAt};
})();
