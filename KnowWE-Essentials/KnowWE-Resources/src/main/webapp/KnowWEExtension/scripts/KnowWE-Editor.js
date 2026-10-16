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
			},
			".cm-knowwe-help": {
				padding: "6px 12px 10px",
				fontSize: "13px",
				lineHeight: "1.4",
				outline: "none"
			},
			".cm-knowwe-help-header": {
				display: "flex",
				alignItems: "center",
				justifyContent: "space-between",
				marginBottom: "6px",
				fontWeight: "600"
			},
			".cm-knowwe-help-header button": {
				padding: "0 4px",
				border: "none",
				background: "none",
				fontSize: "16px",
				lineHeight: "1",
				color: "var(--text-color-secondary, #888)",
				cursor: "pointer"
			},
			".cm-knowwe-help-body": {
				display: "grid",
				gridTemplateColumns: "repeat(auto-fill, minmax(290px, 1fr))",
				gap: "10px 28px",
				maxHeight: "40vh",
				overflowY: "auto"
			},
			".cm-knowwe-help-group-title": {
				margin: "0 0 3px",
				fontSize: "11px",
				fontWeight: "600",
				letterSpacing: "0.04em",
				textTransform: "uppercase",
				color: "var(--text-color-secondary, #888)"
			},
			".cm-knowwe-help-row": {
				display: "flex",
				alignItems: "baseline",
				justifyContent: "space-between",
				gap: "12px",
				padding: "1px 0"
			},
			".cm-knowwe-help-keys": {
				flexShrink: "0",
				textAlign: "right",
				color: "var(--text-color-secondary, #888)"
			},
			".cm-knowwe-help kbd": {
				display: "inline-block",
				minWidth: "1.5em",
				padding: "0 4px",
				fontFamily: "inherit",
				fontSize: "11px",
				fontWeight: "normal",
				lineHeight: "17px",
				textAlign: "center",
				color: "var(--text-color-primary, #000)",
				backgroundColor: "var(--bg-color-primary, #fff)",
				border: "1px solid var(--border-color-table, #ccc)",
				borderRadius: "3px",
				boxShadow: "none"
			},
			".cm-knowwe-help kbd + kbd": {
				marginLeft: "2px"
			},
			".cm-knowwe-help-plus": {
				padding: "0 2px"
			},
			".cm-knowwe-help-or": {
				padding: "0 6px"
			},
			".cm-knowwe-help code": {
				padding: "0 3px",
				color: "inherit",
				backgroundColor: "var(--syntax-code-bg, #f1f3f5)",
				borderRadius: "3px"
			},
			".cm-knowwe-help-tips": {
				gridColumn: "1 / -1"
			},
			".cm-knowwe-help-tips ul": {
				margin: "0",
				paddingLeft: "18px"
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
			help: helpPanel(),
			textareaEditing: textareaEditing(),
			wrapping: new CodeMirror.state.Compartment()
		};
		return shared;
	}

	// the platform the way CodeMirror keymaps tell it, which also decides whether Mod is Cmd or Ctrl
	const platform = typeof navigator === "undefined" ? "key"
		: /Mac/.test(navigator.platform) ? "mac"
			: /Win/.test(navigator.platform) ? "win"
				: /Linux|X11/.test(navigator.platform) ? "linux" : "key";

	/**
	 * The shortcuts shown in the help panel, which must follow the keymaps of create(). A binding uses the notation of
	 * CodeMirror keymaps, `key` holds it for all platforms, `mac`, `win` or `linux` replace it on that platform, and an
	 * array lists alternatives. Mouse gestures are written like keys. An entry with `modes` only shows in those modes.
	 */
	const HELP_SHORTCUTS = [
		{
			title: "General", entries: [
				{text: "Save", key: "Mod-s"},
				{text: "Cancel", key: "Alt-q", mac: "Ctrl-q", modes: ["section"]},
				{text: "Undo", key: "Mod-z"},
				{text: "Redo", key: "Mod-y", mac: "Mod-Shift-z", win: ["Mod-y", "Ctrl-Shift-z"], linux: ["Mod-y", "Ctrl-Shift-z"]},
				{text: "Show completions", key: "Ctrl-Space", mac: ["Ctrl-Space", "Alt-`"]},
				{text: "Close completion, search or help", key: "Escape"},
				{text: "Let Tab move the focus (toggle)", key: "Ctrl-m", mac: "Shift-Alt-m"}
			]
		},
		{
			title: "Search", entries: [
				{text: "Find and replace", key: "Mod-f"},
				{text: "Next match", key: ["F3", "Mod-g"]},
				{text: "Previous match", key: ["Shift-F3", "Shift-Mod-g"]},
				{text: "Go to line", key: "Mod-Alt-g"},
				{text: "Select next occurrence", key: "Mod-d"},
				{text: "Select all occurrences", key: "Mod-Shift-l"}
			]
		},
		{
			title: "Lines", entries: [
				{text: "Move up / down", key: ["Alt-ArrowUp", "Alt-ArrowDown"]},
				{text: "Duplicate up / down", key: ["Shift-Alt-ArrowUp", "Shift-Alt-ArrowDown"]},
				{text: "Delete", key: ["Alt-d", "Shift-Mod-k"]},
				{text: "Toggle // comment", key: "Mod-/"},
				{text: "Insert blank line below", key: "Mod-Enter"},
				{text: "Indent / unindent", key: ["Tab", "Shift-Tab"]}
			]
		},
		{
			title: "Selection", entries: [
				{text: "Add a cursor", key: "Mod-Click"},
				{text: "Rectangular selection", key: "Alt-Drag"},
				{text: "Back to a single cursor", key: "Escape"},
				{text: "Select line", key: "Alt-l", mac: "Ctrl-l"},
				{text: "Select enclosing syntax", key: "Mod-i"},
				{text: "Go to matching bracket", key: "Shift-Mod-\\"},
				{text: "Select all", key: "Mod-a"}
			]
		},
		{
			title: "Folding", entries: [
				{text: "Fold", key: "Ctrl-Shift-[", mac: "Cmd-Alt-["},
				{text: "Unfold", key: "Ctrl-Shift-]", mac: "Cmd-Alt-]"},
				{text: "Fold all", key: "Ctrl-Alt-["},
				{text: "Unfold all", key: "Ctrl-Alt-]"}
			]
		}
	];

	/**
	 * Hints below the shortcuts, `{binding}` shows a key like the shortcuts do and backquotes mark code. A hint with
	 * `modes` only shows in those modes.
	 */
	const HELP_TIPS = [
		{text: "Completion suggests markups after `%%` and annotations after `@` while you type, {Ctrl-Space} opens it anywhere."},
		{text: "With several lines selected, typing `-`, `#`, `*` or a space puts it in front of each line, {Shift-Space} removes the first such character."},
		{text: "The arrows in the gutter fold markups, style and preformatted blocks and the section below a heading."},
		{text: "The wrap button of the toolbar switches line wrapping, this browser remembers it for all editors.", modes: ["page"]},
		{text: "Preview shows the rendered page with the current text, without saving it.", modes: ["page"]},
		{text: "Line wrapping follows the setting of the wrap button in the page editor.", modes: ["section"]}
	];

	const MODIFIERS = ["Ctrl", "Alt", "Shift", "Cmd"];
	const KEY_LABELS = {
		Ctrl: {pc: "Ctrl", mac: "⌃", title: "Control"},
		Alt: {pc: "Alt", mac: "⌥", title: "Option"},
		Shift: {pc: "Shift", mac: "⇧", title: "Shift"},
		Cmd: {pc: "Cmd", mac: "⌘", title: "Command"},
		ArrowUp: {pc: "↑", mac: "↑"},
		ArrowDown: {pc: "↓", mac: "↓"},
		Escape: {pc: "Esc", mac: "Esc"},
		Enter: {pc: "Enter", mac: "↩", title: "Return"}
	};

	function element(tag, className, ...children) {
		const node = document.createElement(tag);
		if (className) node.className = className;
		node.append(...children);
		return node;
	}

	/** Renders a binding in CodeMirror notation as keys of the current platform, e.g. Mod-s as Ctrl + S or ⌘S. */
	function renderBinding(binding) {
		const mac = platform === "mac";
		const parts = binding.split(/-(?!$)/);
		let key = parts.pop();
		const modifiers = parts
			.map(part => part === "Mod" ? (mac ? "Cmd" : "Ctrl") : part === "Meta" ? "Cmd" : part)
			.sort((a, b) => MODIFIERS.indexOf(a) - MODIFIERS.indexOf(b));
		if (key.length === 1) key = key.toUpperCase();
		const span = element("span", "cm-knowwe-help-binding");
		[...modifiers, key].forEach((name, index) => {
			if (index > 0 && !mac) span.append(element("span", "cm-knowwe-help-plus", "+"));
			const label = KEY_LABELS[name];
			const kbd = element("kbd", null, label ? label[mac ? "mac" : "pc"] : name);
			if (label && label.title && mac) kbd.title = label.title;
			span.append(kbd);
		});
		return span;
	}

	function renderBindings(entry) {
		let bindings = entry[platform] ?? entry.key;
		if (!Array.isArray(bindings)) bindings = [bindings];
		const keys = element("span", "cm-knowwe-help-keys");
		bindings.forEach((binding, index) => {
			if (index > 0) keys.append(element("span", "cm-knowwe-help-or", "/"));
			keys.append(renderBinding(binding));
		});
		return keys;
	}

	/** Renders a hint text, `{binding}` becomes keys and `code` becomes code. */
	function renderTip(text) {
		const item = element("li");
		for (const part of text.split(/(\{[^}]+\}|`[^`]+`)/)) {
			if (part.startsWith("{")) {
				item.append(renderBinding(part.slice(1, -1)));
			} else if (part.startsWith("`")) {
				item.append(element("code", null, part.slice(1, -1)));
			} else if (part) {
				item.append(part);
			}
		}
		return item;
	}

	/**
	 * The help panel above the text with the shortcuts and hints for the mode of the editor, toggled by the effect.
	 * It closes on Escape, in the editor and in the panel.
	 */
	function helpPanel() {
		const {StateField, StateEffect, Facet} = CodeMirror.state;
		const {showPanel} = CodeMirror.view;

		const mode = Facet.define({combine: values => values.length ? values[0] : "page"});
		const toggle = StateEffect.define();
		const field = StateField.define({
			create: () => false,
			update(open, tr) {
				for (const effect of tr.effects) {
					if (effect.is(toggle)) open = effect.value;
				}
				return open;
			},
			provide: field => showPanel.from(field, open => open ? create : null)
		});

		function close(view) {
			view.dispatch({effects: toggle.of(false)});
			view.focus();
		}

		function visible(item, editorMode) {
			return !item.modes || item.modes.includes(editorMode);
		}

		function create(view) {
			const editorMode = view.state.facet(mode);
			const closeButton = element("button", null, "×");
			closeButton.type = "button";
			closeButton.title = "Close";
			closeButton.setAttribute("aria-label", "Close");
			closeButton.addEventListener("click", () => close(view));

			const body = element("div", "cm-knowwe-help-body");
			for (const group of HELP_SHORTCUTS) {
				const section = element("div", "cm-knowwe-help-group",
					element("div", "cm-knowwe-help-group-title", group.title));
				for (const entry of group.entries.filter(entry => visible(entry, editorMode))) {
					section.append(element("div", "cm-knowwe-help-row",
						element("span", null, entry.text), renderBindings(entry)));
				}
				body.append(section);
			}
			body.append(element("div", "cm-knowwe-help-group cm-knowwe-help-tips",
				element("div", "cm-knowwe-help-group-title", "Tips"),
				element("ul", null, ...HELP_TIPS.filter(tip => visible(tip, editorMode)).map(tip => renderTip(tip.text)))));

			const dom = element("div", "cm-knowwe-help",
				element("div", "cm-knowwe-help-header", element("span", null, "Editor shortcuts"), closeButton), body);
			// focusable, so a click into the panel keeps the keys within the editor
			dom.tabIndex = -1;
			dom.setAttribute("role", "dialog");
			dom.setAttribute("aria-label", "Editor shortcuts");
			dom.addEventListener("keydown", event => {
				if (event.key !== "Escape") return;
				event.preventDefault();
				close(view);
			});
			return {dom, top: true};
		}

		return {
			mode, toggle, field,
			// closes an open help on Escape, otherwise Escape keeps its other meanings
			closeKey: {
				key: "Escape", run(view) {
					if (!view.state.field(field, false)) return false;
					view.dispatch({effects: toggle.of(false)});
					return true;
				}
			}
		};
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
	 * Editing behavior known from the textarea of the instant edit: Alt-D deletes lines, Ctrl-Shift-Z redoes on Windows
	 * as well, typing "-", "#", "*" or a space while several lines are selected puts it in front of each of these lines,
	 * and Shift-Space removes such a first character again. Line comments start with "//", which other languages of the
	 * text, e.g. embedded in markups, may override.
	 */
	function textareaEditing() {
		const {EditorState, EditorSelection, Prec} = CodeMirror.state;
		const {EditorView, keymap} = CodeMirror.view;
		const {deleteLine, redo} = CodeMirror.commands;
		const PREFIXES = ["-", "#", "*", " "];

		// the lines of a single selection over several lines, a selection ending at the start of a line excludes it
		function selectedLines(state) {
			if (state.selection.ranges.length !== 1) return null;
			const {from, to} = state.selection.main;
			const first = state.doc.lineAt(from);
			let last = state.doc.lineAt(to);
			if (to === last.from && to > from) last = state.doc.line(last.number - 1);
			return last.number > first.number ? {first, last} : null;
		}

		// applies the change of each selected line and selects the lines, false without a selection over several lines
		function changeLines(view, change) {
			const lines = selectedLines(view.state);
			if (!lines) return false;
			const changes = [];
			for (let n = lines.first.number; n <= lines.last.number; n++) {
				const lineChange = change(view.state.doc.line(n));
				if (lineChange) changes.push(lineChange);
			}
			const changeSet = view.state.changes(changes);
			view.dispatch({
				changes: changeSet,
				selection: EditorSelection.single(changeSet.mapPos(lines.first.from, -1), changeSet.mapPos(lines.last.to, 1)),
				scrollIntoView: true,
				userEvent: "input"
			});
			return true;
		}

		const prefixLines = EditorView.inputHandler.of((view, from, to, text) =>
			PREFIXES.includes(text) && changeLines(view, line => ({from: line.from, insert: text})));
		const unprefixLines = view => changeLines(view, line =>
			/^[-#* \t]/.test(line.text) ? {from: line.from, to: line.from + 1} : null);

		return [
			keymap.of([
				{key: "Alt-d", run: deleteLine},
				{win: "Ctrl-Shift-z", run: redo, preventDefault: true},
				{key: "Shift-Space", run: unprefixLines}
			]),
			prefixLines,
			Prec.low(EditorState.languageData.of(() => [{commentTokens: {line: "//"}}]))
		];
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
			parts.help.closeKey,
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
				parts.textareaEditing,
				parts.help.field,
				parts.help.mode.of(mode),
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

	/** Opens or closes the help panel with the shortcuts of an editor created by this factory, toggles without `open`. */
	function toggleHelp(view, open) {
		const help = sharedParts().help;
		view.dispatch({effects: help.toggle.of(open ?? !isHelpOpen(view.state))});
	}

	/** Returns whether the help panel of the editor state is open. */
	function isHelpOpen(state) {
		return state.field(sharedParts().help.field, false) === true;
	}

	/** Returns whether CodeMirror is available, so callers can fall back to a textarea otherwise. */
	function isAvailable() {
		return typeof CodeMirror !== "undefined";
	}

	return {create, setWrapping, toggleHelp, isHelpOpen, isAvailable, detectIndentUnit};
})();
