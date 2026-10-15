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
 * Factory for CodeMirror editors on KnowWE wiki text, shared by the page editor and the instant edit of sections.
 */
window.KnowWEEditor = (function () {
	"use strict";

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

	// built on first use, so loading this script does not fail without the CodeMirror bundle
	let shared = null;

	function sharedParts() {
		if (shared) return shared;
		const {RangeSetBuilder} = CodeMirror.state;
		const {EditorView, ViewPlugin, Decoration} = CodeMirror.view;
		const {HighlightStyle, syntaxTree} = CodeMirror.language;
		const {tags} = CodeMirror.lezer.highlight;

		const baseTheme = EditorView.theme({
			"&": {
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
			},
			".cm-tooltip": {
				backgroundColor: "var(--bg-color-primary, #fff)",
				color: "var(--text-color-primary, #000)",
				border: "1px solid var(--border-color-default, #e2e2e9)",
				borderRadius: "4px",
				boxShadow: "0 2px 8px rgba(0, 0, 0, 0.12)"
			},
			".cm-tooltip.cm-tooltip-autocomplete > ul": {
				fontFamily: "ui-monospace, 'JetBrains Mono', Menlo, Monaco, Consolas, 'Courier New', monospace"
			},
			".cm-tooltip.cm-tooltip-autocomplete > ul > li[aria-selected]": {
				backgroundColor: "var(--highlight-color-light, #d9edf7)",
				color: "var(--text-color-primary, #000)"
			},
			".cm-completionDetail": {
				color: "var(--text-color-secondary, #888)"
			},
			".cm-tooltip.cm-tooltip-no-completions": {
				padding: "2px 8px",
				color: "var(--text-color-secondary, #888)",
				fontStyle: "italic"
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
			{tag: [tags.processingInstruction, tags.escape, tags.character, tags.contentSeparator, tags.list], color: muted},
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

		const pageTheme = EditorView.theme({"&": {height: "100%"}});
		const sectionTheme = EditorView.theme({
			"&": {
				border: "1px solid var(--border-color-edit-blue, #9acbfb)",
				borderRadius: "3px"
			},
			"&.cm-focused": {
				outline: "none"
			},
			".cm-scroller": {
				minHeight: "60px"
			}
		});
		shared = {
			baseTheme, pageTheme, sectionTheme, highlightStyle, markupTint,
			explicitCompletion: explicitCompletion(),
			wrapping: new CodeMirror.state.Compartment()
		};
		return shared;
	}

	/**
	 * Starts completion on Ctrl-Space. If an explicitly started completion finds nothing, a hint says so until the
	 * caret or the text changes.
	 */
	function explicitCompletion() {
		const {StateField, StateEffect, Prec} = CodeMirror.state;
		const {keymap, showTooltip} = CodeMirror.view;
		const {startCompletion, completionStatus} = CodeMirror.autocomplete;

		const requested = StateEffect.define();
		// null, or the caret of a pending explicit request with the flag whether nothing was found
		const hint = StateField.define({
			create: () => null,
			update(value, tr) {
				if (tr.effects.some(effect => effect.is(requested))) {
					return {pos: tr.state.selection.main.head, empty: false};
				}
				if (!value || tr.docChanged || tr.selection) return null;
				const status = completionStatus(tr.state);
				if (status === "active") return null;
				// the sources of the request answered without any completion
				if (!value.empty && status === null && completionStatus(tr.startState) === "pending") {
					return {pos: value.pos, empty: true};
				}
				return value;
			},
			provide: field => showTooltip.compute([field], state => {
				const value = state.field(field);
				if (!value || !value.empty) return null;
				return {
					pos: value.pos,
					create() {
						const dom = document.createElement("div");
						dom.className = "cm-tooltip-no-completions";
						dom.textContent = "No completions";
						return {dom};
					}
				};
			})
		});

		function start(view) {
			if (!startCompletion(view)) return false;
			view.dispatch({effects: requested.of(null)});
			return true;
		}

		// above the keymap of the completion extension, which binds Ctrl-Space as well
		return [hint, Prec.highest(keymap.of([{key: "Ctrl-Space", run: start}]))];
	}

	/**
	 * Creates an editor on wiki text in the given parent element and returns its view. The editor brings highlighting
	 * of the wiki language and the registered markups, folding, search, history, completion and the KnowWE theme.
	 *
	 * Options are the text as `doc`, the title of the edited `article`, the `mode` "page" for an editor that fills
	 * its parent and tints markup blocks or "section" for one that grows with its content, whether to `wrap` lines,
	 * `onSave(view)` called on Mod-S, `onChange(update)` called on every view update and further `extensions`.
	 */
	function create(parent, options = {}) {
		const {EditorState} = CodeMirror.state;
		const {
			EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter, drawSelection,
			dropCursor, rectangularSelection, crosshairCursor, highlightSpecialChars
		} = CodeMirror.view;
		const {defaultKeymap, history, historyKeymap, indentWithTab} = CodeMirror.commands;
		const {searchKeymap, highlightSelectionMatches} = CodeMirror.search;
		const {bracketMatching, indentOnInput, indentUnit, foldGutter, foldKeymap, syntaxHighlighting} = CodeMirror.language;
		const {autocompletion, completionKeymap, closeBrackets, closeBracketsKeymap} = CodeMirror.autocomplete;
		const parts = sharedParts();

		const doc = options.doc ?? "";
		const mode = options.mode === "section" ? "section" : "page";

		// the wiki language is optional, so the editor still works if its script is missing
		const language = [];
		if (typeof KnowWEWikiLanguage !== "undefined") {
			language.push(
				KnowWEWikiLanguage.wiki(),
				KnowWEWikiLanguage.articleTitle.of(options.article ?? null),
				autocompletion({override: KnowWEWikiLanguage.completionSources()})
			);
		} else {
			console.warn("KnowWE wiki language is not available, editing without highlighting");
		}

		const keys = [];
		if (options.onSave) keys.push({key: "Mod-s", run: view => { options.onSave(view); return true; }});
		keys.push(
			...closeBracketsKeymap,
			...defaultKeymap,
			...searchKeymap,
			...historyKeymap,
			...foldKeymap,
			...completionKeymap,
			indentWithTab
		);

		const state = EditorState.create({
			doc,
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
				syntaxHighlighting(parts.highlightStyle),
				parts.explicitCompletion,
				language,
				mode === "page" ? parts.markupTint : [],
				EditorState.tabSize.of(4),
				indentUnit.of(detectIndentUnit(doc)),
				parts.wrapping.of(options.wrap === false ? [] : EditorView.lineWrapping),
				keymap.of(keys),
				parts.baseTheme,
				mode === "page" ? parts.pageTheme : parts.sectionTheme,
				options.onChange ? EditorView.updateListener.of(options.onChange) : [],
				options.extensions ?? []
			]
		});
		return new EditorView({parent, state});
	}

	/** Switches line wrapping of an editor created by this factory. */
	function setWrapping(view, enabled) {
		view.dispatch({effects: sharedParts().wrapping.reconfigure(enabled ? CodeMirror.view.EditorView.lineWrapping : [])});
	}

	/** Returns whether CodeMirror is available, so callers can fall back to a textarea otherwise. */
	function isAvailable() {
		return typeof CodeMirror !== "undefined";
	}

	return {create, setWrapping, isAvailable, detectIndentUnit};
})();
