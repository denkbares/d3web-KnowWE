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
 * Shows the tables of the page in the CodeMirror page editor as the spreadsheet of the table edit tool, see
 * DefaultTableEditTool.js. The rest of the page stays text. Tables are the JSPWiki tables of the wiki text and the
 * consecutive lines starting with "|" in the body of markups without a language of their own, e.g. %%Cable.
 *
 * Every change in a spreadsheet replaces the table in the text in one transaction, so saving, preview and the undo
 * history of the editor see it like a change of the text. Rows that did not change keep their text. Undo and redo in
 * the grid run the history of the editor, so there is one history for the page and the grid follows the text.
 * A table switches to text while the text cursor is in it after typing, so a table typed in the editor does not turn
 * into a grid under the cursor, and while the search panel is open, so matches in tables stay visible. Each table can
 * also be switched to text and back by a tool next to it. Like in the view of the page, the header rows of a long table
 * stay at the upper edge of the editor while it scrolls.
 */
window.KnowWETableEditor = (function () {
	"use strict";

	const STORAGE_KEY = "knowwe.editor.tables";
	// where the tools of a grid show, "left" of the table or "above" it over the text, a switch for trying both
	const TOOLS_KEY = "knowwe.editor.tableTools";
	const BUTTONS = [
		"table_insert_col_before", "table_insert_col_after", "table_insert_row_before", "table_insert_row_after",
		"table_delete_col", "table_delete_row", "toggle_header"
	];

	let nextTableId = 1;
	let nextElementId = 1;

	// ---- the table text ----

	// the cells of a row as [header, trimmed text] without trailing empty cells, which grids add to short rows
	function rowSignature(model, row) {
		const cells = [];
		for (let col = 0; col < model.width; col++) {
			cells.push([model.isHeader(row, col), model.getCellText(row, col).trim()]);
		}
		while (cells.length && !cells[cells.length - 1][0] && !cells[cells.length - 1][1]) cells.pop();
		return JSON.stringify(cells);
	}

	// the cells of a row as wiki text, escaped by SpreadsheetModel and written like "|| Head || Head" or "| a | b"
	function formatCells(model, row, fromCol, toCol) {
		const single = new SpreadsheetModel();
		for (let col = fromCol; col < toCol; col++) {
			single.setCell(0, col - fromCol, model.getCellText(row, col).replace(/\t/g, " "), model.isHeader(row, col));
		}
		if (toCol <= fromCol) return "";
		// toWikiMarkup ends each cell with a tab
		return single.toWikiMarkup(true).replace(/\n$/, "").split("\t").slice(0, -1).map(cell => {
			const [, mark, text] = /^(\|\|?) *([\s\S]*)$/.exec(cell);
			return text ? mark + " " + text : mark;
		}).join(" ");
	}

	function formatRow(model, row) {
		return formatCells(model, row, 0, model.width).trimEnd();
	}

	/** The wiki text of the model, rows with the same cells as a row of the old text keep the text of that row. */
	function tableText(model, oldText) {
		const oldRows = new Map();
		for (const line of oldText.split("\n")) {
			const parsed = new SpreadsheetModel(line, true);
			if (parsed.isEmpty()) continue;
			const signature = rowSignature(parsed, 0);
			if (!oldRows.has(signature)) oldRows.set(signature, []);
			oldRows.get(signature).push(line);
		}
		const rows = [];
		for (let row = 0; row < model.height; row++) {
			const same = oldRows.get(rowSignature(model, row));
			rows.push(same && same.length ? same.shift() : formatRow(model, row));
		}
		return rows.join("\n");
	}

	// ---- finding tables ----

	function wholeLines(doc, from, to) {
		return {from: doc.lineAt(from).from, to: doc.lineAt(Math.max(from, to - 1)).to};
	}

	// consecutive lines starting with "|" in the body of a markup without a language of its own
	function markupTables(state, markup, ranges) {
		const open = markup.getChild("MarkupOpen");
		if (!open) return;
		const name = KnowWEWikiLanguage.markupName(open, {read: (from, to) => state.sliceDoc(from, to)});
		if (KnowWEWikiLanguage.getMarkup(name)?.parser) return;
		// the body is mounted with the tree of its own parser, so it is taken as the range between open and close
		const close = markup.getChild("MarkupClose");
		const content = {from: open.to, to: close ? close.from : markup.to};
		const doc = state.doc;
		let table = null;
		for (let n = doc.lineAt(content.from).number, last = doc.lineAt(content.to).number; n <= last; n++) {
			const line = doc.line(n);
			if (line.from >= content.from && line.to <= content.to && /^[ \t]*\|/.test(line.text)) {
				if (table) table.to = line.to;
				else table = {from: line.from, to: line.to, markup: true};
			} else if (table) {
				ranges.push(table);
				table = null;
			}
		}
		if (table) ranges.push(table);
	}

	/** The ranges of the tables in the parsed part of the document, each covering whole lines. */
	function tableRanges(state) {
		const {syntaxTree} = CodeMirror.language;
		const tree = syntaxTree(state);
		const complete = tree.length >= state.doc.length;
		const ranges = [];
		tree.iterate({
			enter(node) {
				// a table at the end of a partial tree may still grow
				if (!complete && node.to >= tree.length) return false;
				switch (node.name) {
					case "Table":
						ranges.push(wholeLines(state.doc, node.from, node.to));
						return false;
					case "Markup":
						markupTables(state, node.node, ranges);
						return false;
					case "InlineMarkup":
					case "Preformatted":
					case "Plugin":
						return false;
				}
			}
		});
		return ranges;
	}

	// gives each range the id of the old table it overlaps, so a grid survives changes of its table
	function identify(ranges, oldTables) {
		const free = [...oldTables];
		return ranges.map(range => {
			const index = free.findIndex(old => old.from <= range.to && old.to >= range.from);
			const id = index >= 0 ? free.splice(index, 1)[0].id : nextTableId++;
			return {id, from: range.from, to: range.to, markup: range.markup === true};
		});
	}

	// ---- the editor extension ----

	let shared = null;

	function sharedParts() {
		if (shared) return shared;
		const {StateField, StateEffect, Annotation, Compartment} = CodeMirror.state;
		const {EditorView, Decoration, WidgetType, ViewPlugin, GutterMarker, lineNumberWidgetMarker} = CodeMirror.view;
		const {syntaxTree, ensureSyntaxTree} = CodeMirror.language;
		const {searchPanelOpen} = CodeMirror.search;

		// marks the changes written by a grid
		const gridChange = Annotation.define();
		// shows the table with the `id` as text or as grid, by `text`
		const showAsText = StateEffect.define();

		class TableWidget extends WidgetType {
			constructor(id, text, markup) {
				super();
				this.id = id;
				this.text = text;
				this.markup = markup;
			}

			eq(other) {
				return other.id === this.id && other.text === this.text && other.markup === this.markup;
			}

			toDOM(view) {
				const dom = grid(view, this.id, this.text).dom;
				// the tint of the markup lines around, see KnowWE-Editor.js
				dom.classList.toggle("cm-markup-line", this.markup);
				return dom;
			}

			updateDOM(dom, view) {
				const controller = dom.knowweTable;
				if (!controller || controller.id !== this.id || controller.view !== view) return false;
				controller.update(this.text);
				dom.classList.toggle("cm-markup-line", this.markup);
				return true;
			}

			get estimatedHeight() {
				return 28 + 24 * this.text.split("\n").length;
			}

			ignoreEvent() {
				return true;
			}
		}

		// the line numbers of the rows next to a grid, each row is a line of the text, see TableGrid.layoutNumbers()
		class RowNumbers extends GutterMarker {
			constructor(id) {
				super();
				this.id = id;
			}

			eq(other) {
				return other.id === this.id;
			}

			toDOM(view) {
				const dom = document.createElement("div");
				dom.className = "cm-knowwe-table-linenumbers";
				numbersOf(view).set(this.id, dom);
				const controller = gridsOf(view).get(this.id);
				if (controller) controller.layoutNumbers();
				return dom;
			}
		}

		const rowNumbers = lineNumberWidgetMarker
			? lineNumberWidgetMarker.of((view, widget) => widget instanceof TableWidget ? new RowNumbers(widget.id) : null)
			: [];

		function decorations(state, tables, editing, asText) {
			if (searchPanelOpen(state)) return Decoration.none;
			return Decoration.set(tables.filter(table => table.id !== editing && !asText.has(table.id)).map(table => Decoration.replace({
				widget: new TableWidget(table.id, state.sliceDoc(table.from, table.to), table.markup),
				block: true
			}).range(table.from, table.to)), true);
		}

		function tableAt(tables, pos) {
			return tables.find(table => table.from <= pos && pos <= table.to);
		}

		const field = StateField.define({
			create(state) {
				ensureSyntaxTree(state, state.doc.length, 100);
				const tables = identify(tableRanges(state), []);
				const asText = new Set();
				return {tables, editing: null, asText, decorations: decorations(state, tables, null, asText)};
			},
			update(value, tr) {
				let {tables, editing, asText} = value;
				for (const effect of tr.effects) {
					if (!effect.is(showAsText)) continue;
					asText = new Set(asText);
					if (effect.value.text) {
						asText.add(effect.value.id);
					} else {
						asText.delete(effect.value.id);
						if (editing === effect.value.id) editing = null;
					}
				}
				const treeChanged = syntaxTree(tr.state) !== syntaxTree(tr.startState);
				if (tr.docChanged || treeChanged) {
					const mapped = tables.map(table => ({
						id: table.id,
						from: tr.changes.mapPos(table.from, 1),
						to: tr.changes.mapPos(table.to, -1)
					}));
					tables = identify(tableRanges(tr.state), mapped);
					if ([...asText].some(id => !tables.some(table => table.id === id))) {
						asText = new Set([...asText].filter(id => tables.some(table => table.id === id)));
					}
				}
				const head = tr.state.selection.main.head;
				if (tr.docChanged && !tr.annotation(gridChange) && (tr.isUserEvent("input") || tr.isUserEvent("delete"))) {
					const typedIn = tableAt(tables, head);
					if (typedIn) editing = typedIn.id;
				}
				if (editing !== null) {
					const table = tables.find(table => table.id === editing);
					if (!table || head < table.from || head > table.to) editing = null;
				}
				if (tables === value.tables && editing === value.editing && asText === value.asText
					&& searchPanelOpen(tr.state) === searchPanelOpen(tr.startState)) {
					return value;
				}
				return {tables, editing, asText, decorations: decorations(tr.state, tables, editing, asText)};
			},
			provide: field => EditorView.decorations.from(field, value => value.decorations)
		});

		// drops the grids of tables that are no longer shown as grid
		const cleanup = ViewPlugin.fromClass(class {
			constructor(view) {
				this.view = view;
			}

			update(update) {
				const grids = gridsOf(update.view);
				if (!grids.size) return;
				// lines added or removed above a grid change the numbers of its rows
				if (update.docChanged || update.geometryChanged) grids.forEach(controller => controller.layoutNumbers());
				const shown = new Set();
				update.state.field(field).decorations.between(0, update.state.doc.length, (from, to, deco) => {
					shown.add(deco.spec.widget.id);
				});
				for (const [id, controller] of [...grids]) {
					if (shown.has(id)) continue;
					controller.destroy();
					grids.delete(id);
				}
			}

			destroy() {
				gridsOf(this.view).forEach(controller => controller.destroy());
				views.delete(this.view);
			}
		});

		/**
		 * The tool next to a table shown as text, to show it as grid again. It shows while the mouse is over the table
		 * or while the text cursor is in it, left of the text like the tools of a grid, and at the upper edge of the
		 * editor while a long table scrolls.
		 */
		const textTools = ViewPlugin.fromClass(class {
			constructor(view) {
				this.view = view;
				this.hoverId = null;
				this.overTool = false;
				this.shownId = null;
				this.tool = document.createElement("div");
				this.tool.className = "knowwe-table-tools-floating";
				this.tool.style.display = "none";
				this.tool.append(iconButton("table_show_grid", "fa-table", "show as grid"));
				// keeps the focus and the selection in the editor
				this.tool.addEventListener("mousedown", event => event.preventDefault());
				this.tool.addEventListener("click", event => {
					event.preventDefault();
					if (this.shownId !== null) this.showGrid(this.shownId);
				});
				this.tool.addEventListener("mouseenter", () => this.overTool = true);
				this.tool.addEventListener("mouseleave", () => {
					this.overTool = false;
					this.later();
				});
				const signal = (this.listeners = new AbortController()).signal;
				view.contentDOM.addEventListener("mousemove", event => this.track(event), {signal});
				view.contentDOM.addEventListener("mouseleave", () => {
					this.hoverId = null;
					this.later();
				}, {signal});
				view.scrollDOM.addEventListener("scroll", () => this.schedule(), {passive: true, signal});
				window.addEventListener("resize", () => this.schedule(), {signal});
			}

			update(update) {
				if (update.docChanged || update.selectionSet || update.focusChanged || update.geometryChanged
					|| update.state.field(field) !== update.startState.field(field)) {
					this.schedule();
				}
			}

			destroy() {
				this.listeners.abort();
				this.tool.remove();
			}

			// the tables shown as text, the search panel shows all tables as text and has no tools
			textTable(id) {
				const state = this.view.state;
				const value = state.field(field, false);
				if (id === null || !value || searchPanelOpen(state)) return null;
				const table = value.tables.find(table => table.id === id);
				return table && (table.id === value.editing || value.asText.has(table.id)) ? table : null;
			}

			track(event) {
				const pos = this.view.posAtCoords({x: event.clientX, y: event.clientY});
				const value = this.view.state.field(field, false);
				const table = pos === null || !value ? null : tableAt(value.tables, pos);
				const id = table && this.textTable(table.id) ? table.id : null;
				if (id === this.hoverId) return;
				this.hoverId = id;
				// leaving the table for the tool passes the gutter, the delay keeps the tool meanwhile
				if (id === null) {
					this.later();
				} else {
					this.schedule();
				}
			}

			later() {
				setTimeout(() => this.schedule(), 100);
			}

			schedule() {
				this.view.requestMeasure({key: this, read: () => this.measure(), write: place => this.place(place)});
			}

			measure() {
				const view = this.view;
				let table = this.overTool ? this.textTable(this.shownId) : this.textTable(this.hoverId);
				if (!table && view.hasFocus) {
					const value = view.state.field(field, false);
					const atCursor = value && tableAt(value.tables, view.state.selection.main.head);
					table = atCursor ? this.textTable(atCursor.id) : null;
				}
				if (!table) return null;
				const top = view.lineBlockAt(table.from).top + view.documentTop;
				const bottom = view.lineBlockAt(table.to).bottom + view.documentTop;
				const editor = view.scrollDOM.getBoundingClientRect();
				if (bottom < editor.top || top > editor.bottom) return null;
				return {id: table.id, top, bottom, editorTop: editor.top, left: view.contentDOM.getBoundingClientRect().left};
			}

			place(place) {
				const tool = this.tool;
				// the button stays under the mouse while the mouse is on it
				if (this.overTool && place && place.id === this.shownId) return;
				this.shownId = place ? place.id : null;
				if (!place) {
					tool.style.display = "none";
					return;
				}
				if (!tool.isConnected) document.body.append(tool);
				tool.style.display = "";
				const height = tool.offsetHeight;
				tool.style.top = Math.min(Math.max(place.top, place.editorTop), Math.max(place.top, place.bottom - height)) + "px";
				tool.style.left = (place.left - tool.offsetWidth) + "px";
			}

			showGrid(id) {
				this.view.dispatch({effects: showAsText.of({id, text: false})});
				this.view.focus();
			}
		});

		const theme = EditorView.baseTheme({
			".cm-knowwe-table": {
				position: "relative",
				padding: "2px 0 6px 6px",
				whiteSpace: "normal",
				cursor: "default",
				// a wide table scrolls in its grid instead of widening the text
				contain: "inline-size"
			},
			".cm-knowwe-table-toolbar": {
				visibility: "hidden"
			},
			// floats over the text above the table, so the table takes no extra space
			".cm-knowwe-table-tools-above .cm-knowwe-table-toolbar": {
				position: "absolute",
				left: "6px",
				bottom: "100%",
				zIndex: "5",
				display: "flex",
				alignItems: "center",
				gap: "4px",
				padding: "2px 4px",
				backgroundColor: "var(--bg-color-primary, #fff)",
				border: "1px solid var(--border-color-default, #e2e2e9)",
				borderRadius: "4px",
				boxShadow: "0 2px 8px rgba(0, 0, 0, 0.12)"
			},
			".cm-knowwe-table:hover .cm-knowwe-table-toolbar, .cm-knowwe-table:focus-within .cm-knowwe-table-toolbar": {
				visibility: "visible"
			},
			".cm-knowwe-table-toolbar a": {
				display: "inline-flex",
				padding: "3px 5px",
				borderRadius: "4px",
				cursor: "pointer",
				opacity: "0.75"
			},
			".cm-knowwe-table-toolbar a:hover": {
				opacity: "1",
				backgroundColor: "var(--bg-color-secondary-shade, #eee)"
			},
			".cm-knowwe-table-toolbar svg": {
				height: "22px",
				width: "auto"
			},
			".cm-knowwe-table-toolbar i": {
				width: "22px",
				height: "22px",
				fontSize: "16px",
				lineHeight: "22px",
				textAlign: "center"
			},
			".cm-knowwe-table-grid": {
				overflow: "auto",
				maxWidth: "100%",
				// the editor of a cell starts 3px above and left of the cell, the scrolling grid would clip it in the first
				// row and column
				padding: "3px"
			},
			".cm-knowwe-table-sheet": {
				position: "relative"
			},
			".cm-knowwe-table-linenumbers": {
				position: "relative",
				height: "0"
			},
			".cm-knowwe-table-linenumbers > div": {
				position: "absolute",
				right: "0",
				whiteSpace: "nowrap"
			},
			// the header rows at the upper edge of the editor cover the rows scrolling below them, see stickHeader()
			".cm-knowwe-table-grid table.spreadsheet tr.cm-knowwe-table-stuck": {
				position: "relative",
				zIndex: "2"
			},
			// the collapsed borders are drawn by the table and stay behind, each moved cell draws an opaque ring instead
			".cm-knowwe-table-grid table.spreadsheet tr.cm-knowwe-table-stuck > td": {
				position: "relative"
			},
			".cm-knowwe-table-grid table.spreadsheet tr.cm-knowwe-table-stuck > td::after": {
				content: "''",
				position: "absolute",
				inset: "-1px",
				border: "1px solid var(--border-color-default)",
				pointerEvents: "none"
			},
			".cm-knowwe-table-linenumbers > div.cm-knowwe-table-stuck": {
				zIndex: "1",
				left: "-14px",
				right: "-10px",
				paddingRight: "10px",
				textAlign: "right",
				backgroundColor: "var(--bg-color-secondary, #f6f6f6)"
			},
			".cm-knowwe-table-grid table.spreadsheet": {
				margin: "0"
			},
			// the cells keep their lines, a wide table scrolls instead
			".cm-knowwe-table-grid table.spreadsheet tr td > div": {
				whiteSpace: "pre",
				overflowWrap: "normal",
				wordBreak: "normal"
			}
		});

		shared = {field, gridChange, showAsText, cleanup, textTools, rowNumbers, theme, enabled: new Compartment()};
		return shared;
	}

	// ---- the grids ----

	// the grids of each editor by table id, a grid is reused when the editor draws its table again
	const views = new WeakMap();

	function gridsOf(view) {
		if (!views.has(view)) views.set(view, new Map());
		return views.get(view);
	}

	// the elements in the line number gutter of each editor by table id
	const numberViews = new WeakMap();

	function numbersOf(view) {
		if (!numberViews.has(view)) numberViews.set(view, new Map());
		return numberViews.get(view);
	}

	function grid(view, id, text) {
		const grids = gridsOf(view);
		let controller = grids.get(id);
		if (!controller || controller.dom.isConnected) {
			if (controller) controller.destroy();
			controller = new TableGrid(view, id, text);
			grids.set(id, controller);
		} else {
			controller.update(text);
		}
		return controller;
	}

	function iconButton(name, icon, title) {
		const button = document.createElement("a");
		button.className = name;
		button.title = title;
		button.innerHTML = "<i class='fa " + icon + "'></i>";
		return button;
	}

	function svgButton(iconId) {
		const button = document.createElement("a");
		button.className = iconId;
		button.title = iconId.replace(/_/g, " ").replace("table ", "");
		button.innerHTML = "<svg height='22' viewBox='0 0 149 124' fill='none'>"
			+ "<use xlink:href='KnowWEExtension/images/table/svg/table-edit-icons.svg#" + iconId + "'></use></svg>";
		return button;
	}

	class TableGrid {
		constructor(view, id, text) {
			this.view = view;
			this.id = id;
			this.text = text;

			this.side = toolsSide();
			this.dom = document.createElement("div");
			this.dom.className = "cm-knowwe-table cm-knowwe-table-tools-" + this.side;
			this.dom.contentEditable = "false";
			this.dom.knowweTable = this;

			const toolbar = document.createElement("div");
			toolbar.className = this.side === "left" ? "knowwe-table-tools-floating" : "cm-knowwe-table-toolbar";
			this.toolbar = toolbar;
			toolbar.append(...BUTTONS.map(svgButton), iconButton("table_show_text", "fa-code", "show as text"));
			// keeps the focus and the selected cells in the grid
			toolbar.addEventListener("mousedown", event => event.preventDefault());
			toolbar.addEventListener("click", event => {
				const button = event.target.closest("a");
				if (button) this.runButton(button.className);
				event.preventDefault();
			});

			// the editor of a cell sticks out of its sheet, the padding of the scrolling grid makes room for it
			const host = document.createElement("div");
			host.className = "cm-knowwe-table-grid";
			const sheet = document.createElement("div");
			sheet.className = "cm-knowwe-table-sheet";
			sheet.id = "knowweTableGrid" + nextElementId++;
			host.append(sheet);
			if (this.side === "left") {
				this.dom.append(host);
			} else {
				this.dom.append(toolbar, host);
			}

			this.sheet = new Spreadsheet(sheet);
			this.sheet.setWikiMarkup(text);
			this.clearSelection();
			this.sheet.setAutoCompleteFunction((callback, prefix, sheet, row, col) => this.complete(callback, prefix, row, col));
			this.sheet.setChangeListener(() => this.commit());

			// capture, so the keys reach neither the spreadsheet nor the editor
			this.dom.addEventListener("keydown", event => this.handleKey(event), true);
			this.listeners = new AbortController();
			const follow = () => {
				if (this.isToolbarShown()) this.placeToolbar();
			};
			// the row numbers and the header rows follow the rows of the grid
			this.rowResizes = new ResizeObserver(() => {
				this.layoutNumbers();
				this.stickHeader();
			});
			this.rowResizes.observe(host);
			if (this.side === "left") {
				// the toolbar is outside the editor, so it is shown while the grid or the toolbar is hovered or focused
				this.hovered = new Set();
				for (const element of [this.dom, toolbar]) {
					element.addEventListener("mouseenter", () => this.hover(element, true));
					element.addEventListener("mouseleave", () => this.hover(element, false));
				}
				this.dom.addEventListener("focusin", () => this.showToolbar());
				this.dom.addEventListener("focusout", () => setTimeout(() => this.showToolbar(), 0));
				// the number of tool rows follows the height of the table
				this.resizes = new ResizeObserver(follow);
				this.resizes.observe(host);
			} else {
				this.dom.addEventListener("mouseenter", () => this.placeToolbar());
				this.dom.addEventListener("focusin", () => this.placeToolbar());
			}
			// the toolbar of a long table stays at the upper edge of the editor while scrolling
			view.scrollDOM.addEventListener("scroll", follow, {passive: true, signal: this.listeners.signal});
			window.addEventListener("resize", follow, {signal: this.listeners.signal});
			view.scrollDOM.addEventListener("scroll", () => this.stickHeader(), {passive: true, signal: this.listeners.signal});
			this.dom.addEventListener("focusout", event => {
				// a cell still being edited is taken over when the focus leaves the grid, e.g. to save
				if (!this.dom.contains(event.relatedTarget)) this.leave(event.relatedTarget);
			});
		}

		// the spreadsheet selects the first cell whenever it shows a table
		clearSelection() {
			this.sheet.selectCell();
			this.sheet.element.find(".multiSelected").removeClass("multiSelected");
		}

		destroy() {
			this.listeners.abort();
			this.rowResizes.disconnect();
			if (this.resizes) this.resizes.disconnect();
			if (this.side === "left") this.toolbar.remove();
		}

		isToolbarShown() {
			if (this.side === "left") return this.dom.isConnected && (this.hovered.size > 0 || this.hasFocus());
			return this.dom.isConnected && this.dom.matches(":hover, :focus-within");
		}

		// leaving the grid for the toolbar or back passes no gap, the delay only bridges the order of the events
		hover(element, entered) {
			if (entered) {
				this.hovered.add(element);
			} else {
				this.hovered.delete(element);
			}
			setTimeout(() => this.showToolbar(), entered ? 0 : 100);
		}

		showToolbar() {
			if (!this.isToolbarShown()) {
				this.toolbar.style.display = "none";
				return;
			}
			if (!this.toolbar.isConnected) document.body.append(this.toolbar);
			this.toolbar.style.display = "";
			this.placeToolbar();
		}

		/**
		 * Left of the table, the tools take as many rows as fit beside the table and more columns otherwise. They float
		 * over the line numbers and beyond the editor, at the top of the table or at the upper edge of the editor while
		 * a long table scrolls. Above the table, the toolbar is over the text. Without room above it in the editor, it
		 * goes below a table that is visible as a whole, and otherwise to the upper edge of the editor, but not below
		 * the table.
		 */
		placeToolbar() {
			const toolbar = this.toolbar;
			if (this.side === "left") {
				// the buttons stay under the mouse while their changes resize the table, until the mouse leaves them
				if (this.hovered.has(toolbar) && toolbar.style.top) return;
				const box = this.sheet.element[0].getBoundingClientRect();
				const editor = this.view.scrollDOM.getBoundingClientRect();
				const hidden = box.bottom < editor.top || box.top > editor.bottom;
				toolbar.style.display = hidden ? "none" : "";
				if (hidden) return;
				const button = toolbar.firstElementChild.offsetHeight + 1;
				const rows = Math.max(1, Math.min(toolbar.children.length, Math.floor(box.height / button)));
				toolbar.style.gridTemplateRows = "repeat(" + rows + ", auto)";
				const top = Math.min(Math.max(box.top, editor.top), Math.max(box.top, box.bottom - toolbar.offsetHeight));
				toolbar.style.top = top + "px";
				toolbar.style.left = (box.left - toolbar.offsetWidth) + "px";
				return;
			}
			const height = toolbar.offsetHeight;
			const box = this.dom.getBoundingClientRect();
			const editor = this.view.scrollDOM.getBoundingClientRect();
			let top = -height;
			if (box.top - height < editor.top) {
				top = box.top >= editor.top && box.bottom + height <= editor.bottom
					? box.height
					: Math.min(editor.top - box.top, box.height - height);
			}
			toolbar.style.top = top + "px";
			toolbar.style.bottom = "auto";
		}

		hasFocus() {
			return this.dom.contains(document.activeElement);
		}

		/** Writes the line number of each row into the line number gutter, next to the row. */
		layoutNumbers() {
			if (!this.numbersKey) this.numbersKey = {};
			this.view.requestMeasure({
				key: this.numbersKey,
				read: () => {
					const dom = numbersOf(this.view).get(this.id);
					const range = this.range();
					if (!dom || !dom.isConnected || !range || !this.dom.isConnected) return null;
					const base = this.dom.getBoundingClientRect().top;
					const first = this.view.state.doc.lineAt(range.from).number;
					const rows = [...this.sheet.element[0].querySelectorAll("tr")].map((row, index) => {
						const box = row.getBoundingClientRect();
						return {number: first + index, top: box.top - base, height: box.height};
					});
					return {dom, rows};
				},
				write: layout => {
					if (!layout) return;
					layout.dom.replaceChildren(...layout.rows.map(row => {
						const number = document.createElement("div");
						number.textContent = row.number;
						number.style.top = row.top + "px";
						number.style.height = number.style.lineHeight = row.height + "px";
						return number;
					}));
					this.moveStuckNumbers();
				}
			});
		}

		/**
		 * Keeps the header rows, the rows from the top whose cells are all headers, at the upper edge of the editor while
		 * the rest of the table scrolls below them, but not beyond the end of the table. Their line numbers move along.
		 */
		stickHeader() {
			if (!this.stickKey) this.stickKey = {};
			this.view.requestMeasure({
				key: this.stickKey,
				read: () => {
					const table = this.dom.isConnected ? this.sheet.element[0].querySelector("table") : null;
					if (!table) return null;
					const rows = [];
					for (const row of table.rows) {
						if (!row.cells.length || ![...row.cells].every(cell => cell.classList.contains("table-header"))) break;
						rows.push(row);
					}
					const box = table.getBoundingClientRect();
					const editor = this.view.scrollDOM.getBoundingClientRect();
					const height = rows.reduce((sum, row) => sum + row.getBoundingClientRect().height, 0);
					// the first row starts half a collapsed border below the table, measured without its current offset
					const top = rows.length ? rows[0].getBoundingClientRect().top - Spreadsheet.rowOffset(rows[0]) : 0;
					const offset = rows.length ? Math.max(0, Math.min(editor.top - top, box.bottom - top - height)) : 0;
					return {rows, offset};
				},
				write: stick => {
					if (!stick) return;
					for (const row of this.stuckRows || []) {
						if (stick.rows.includes(row)) continue;
						row.style.transform = "";
						row.classList.remove("cm-knowwe-table-stuck");
					}
					for (const row of stick.rows) {
						row.style.transform = stick.offset ? "translateY(" + stick.offset + "px)" : "";
						row.classList.toggle("cm-knowwe-table-stuck", stick.offset > 0);
					}
					this.stuckRows = stick.rows;
					this.stuckOffset = stick.offset;
					this.moveStuckNumbers();
				}
			});
		}

		moveStuckNumbers() {
			const dom = numbersOf(this.view).get(this.id);
			if (!dom) return;
			const count = this.stuckRows ? this.stuckRows.length : 0;
			[...dom.children].forEach((number, index) => {
				const stuck = index < count && this.stuckOffset > 0;
				number.style.transform = stuck ? "translateY(" + this.stuckOffset + "px)" : "";
				number.classList.toggle("cm-knowwe-table-stuck", stuck);
			});
		}

		/** The current range of the table in the document, null if it is gone. */
		range() {
			const value = this.view.state.field(sharedParts().field, false);
			return value ? value.tables.find(table => table.id === this.id) ?? null : null;
		}

		/** Shows the given table text, unless it is the text of the grid anyway. */
		update(text) {
			if (text === this.text) return;
			this.text = text;
			const active = document.activeElement;
			const focused = this.hasFocus();
			const selected = this.sheet.selected;
			// selects and focuses the first cell
			this.sheet.setWikiMarkup(text);
			this.stickHeader();
			if (focused && selected) {
				this.sheet.selectCell(Math.min(selected.row, this.sheet.size.rows - 1),
					Math.min(selected.col, this.sheet.size.cols - 1));
				return;
			}
			this.clearSelection();
			if (focused) {
				this.view.focus();
			} else if (active && active !== document.activeElement && active.isConnected) {
				active.focus({preventScroll: true});
			}
		}

		/** Writes the table of the grid into the document. */
		commit() {
			const text = tableText(this.sheet.getModel(), this.text);
			if (text === this.text) return;
			const range = this.range();
			if (!range) return;
			const focused = this.hasFocus();
			this.text = text;
			this.view.dispatch({
				changes: {from: range.from, to: range.to, insert: text},
				annotations: sharedParts().gridChange.of(true),
				userEvent: "table.edit"
			});
			// rows added, removed or turned into header rows are drawn anew
			this.stickHeader();
			// in case the editor had to draw the grid anew
			if (focused && !this.hasFocus()) this.restoreFocus();
		}

		restoreFocus() {
			const current = gridsOf(this.view).get(this.id);
			if (!current || !current.dom.isConnected) return;
			const selected = this.sheet.selected;
			if (selected) current.sheet.selectCell(selected.row, selected.col);
		}

		leave(target) {
			if (!this.dom.querySelector(".cellEdit")) return;
			this.sheet.stopEditCell();
			// stopping the edit selects the cell again, the focus belongs where it went
			if (target && target.isConnected) {
				target.focus();
			} else if (this.hasFocus()) {
				document.activeElement.blur();
			}
		}

		runButton(name) {
			const sheet = this.sheet;
			const selected = sheet.selected;
			if (name === "table_show_text") {
				this.showAsText();
				return;
			}
			if (!selected && name !== "table_delete_row" && name !== "table_delete_col") return;
			switch (name) {
				case "table_insert_row_before":
					sheet.addRow(selected.row);
					break;
				case "table_insert_row_after":
					sheet.addRow(selected.row + 1);
					break;
				case "table_insert_col_before":
					sheet.addCol(selected.col);
					break;
				case "table_insert_col_after":
					sheet.addCol(selected.col + 1);
					break;
				case "table_delete_row":
					sheet.removeSelectedRows();
					break;
				case "table_delete_col":
					sheet.removeSelectedCols();
					break;
				case "toggle_header": {
					const header = sheet.getCell(selected.row, selected.col).hasClass("table-header");
					sheet.forEachSelected((cell, row, col) => sheet.setHeader(row, col, !header));
					sheet.snapshot();
					break;
				}
			}
		}

		/** Shows the table as text with the text cursor at its start, the tool next to it shows it as grid again. */
		showAsText() {
			// takes over a cell still being edited
			this.sheet.stopEditCell();
			const range = this.range();
			if (!range) return;
			this.view.dispatch({
				effects: sharedParts().showAsText.of({id: this.id, text: true}),
				selection: {anchor: range.from}
			});
			this.view.focus();
		}

		handleKey(event) {
			const command = event.ctrlKey || event.metaKey;
			if (!command || event.altKey) return;
			const key = event.key.toLowerCase();
			const inCell = event.target.tagName === "TEXTAREA";
			if (key === "s") {
				event.preventDefault();
				event.stopPropagation();
				this.sheet.stopEditCell();
				KnowWEEditor.save(this.view);
				return;
			}
			// the text area of a cell keeps its own undo
			if (inCell) return;
			const {undo, redo} = CodeMirror.commands;
			if (key === "z" && !event.shiftKey) {
				undo(this.view);
			} else if (key === "y" || (key === "z" && event.shiftKey)) {
				redo(this.view);
			} else {
				return;
			}
			event.preventDefault();
			event.stopPropagation();
		}

		/**
		 * Asks the server for completions of a cell, with the page up to the cell as prefix like the completion of the
		 * text, and the rest of the table and of its markup as suffix.
		 */
		complete(callback, prefix, row, col) {
			const range = this.range();
			if (!range || typeof AutoComplete === "undefined") return callback([]);
			const state = this.view.state;
			const model = this.sheet.getModel();
			const before = [];
			for (let r = 0; r < row; r++) before.push(formatRow(model, r));
			const mark = model.isHeader(row, col) ? "||" : "|";
			const rowStart = formatCells(model, row, 0, col);
			before.push((rowStart ? rowStart + " " : "") + mark + " " + prefix);

			const after = [formatCells(model, row, col + 1, model.width)];
			for (let r = row + 1; r < model.height; r++) after.push(formatRow(model, r));
			const block = KnowWEWikiLanguage.markupBlock(state, range.from);
			const data = {
				prefix: state.sliceDoc(0, range.from) + before.join("\n"),
				scope: "root",
				suffix: after.join("\n") + state.sliceDoc(range.to, block ? block.to : range.to)
			};
			const article = state.facet(KnowWEWikiLanguage.articleTitle);
			if (article) data.KWiki_Topic = article;
			AutoComplete.sendCompletionActionData(data, response => {
				const items = Array.isArray(response) ? response : response && response.completions || [];
				// annotations, plugins and blocks of the markup or page do not fit into a cell
				callback(items.filter(item => !/^(@|\[\{)|\n/.test(item.insertText || "")));
			});
		}
	}

	// the completion menu of a cell is outside the grid, clicking it must not end the edit of the cell
	document.addEventListener("mousedown", event => {
		const target = event.target;
		if (target.closest && target.closest("#findSuggestionMenu")
			&& document.activeElement && document.activeElement.closest(".cm-knowwe-table")) {
			event.preventDefault();
		}
	}, true);

	// the left toolbar is a child of the body, above the menus of the page, so the theme of the editor does not reach it
	function addFloatingStyle() {
		const style = document.createElement("style");
		style.textContent = `
.knowwe-table-tools-floating {
	position: fixed; z-index: 1000; display: grid; grid-auto-flow: column; gap: 1px 2px; padding: 2px;
	background-color: var(--bg-color-primary, #fff); border: 1px solid var(--border-color-default, #e2e2e9);
	border-radius: 4px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}
.knowwe-table-tools-floating a {
	display: inline-flex; padding: 3px 5px; border-radius: 4px; cursor: pointer; opacity: 0.75;
}
.knowwe-table-tools-floating a:hover {
	opacity: 1; background-color: var(--bg-color-secondary-shade, #eee);
}
.knowwe-table-tools-floating svg {
	height: 22px; width: auto;
}
.knowwe-table-tools-floating i {
	width: 22px; height: 22px; font-size: 16px; line-height: 22px; text-align: center;
}`;
		document.head.append(style);
	}

	// ---- public ----

	function toolsSide() {
		try {
			return localStorage.getItem(TOOLS_KEY) === "above" ? "above" : "left";
		} catch (e) {
			return "left";
		}
	}

	function stored() {
		try {
			return localStorage.getItem(STORAGE_KEY) !== "false";
		} catch (e) {
			return true;
		}
	}

	function extension() {
		const parts = sharedParts();
		return [parts.field, parts.cleanup, parts.textTools, parts.rowNumbers, parts.theme];
	}

	/** Shows the tables of an editor created by KnowWEEditor as grids or as text, and remembers the choice. */
	function setEnabled(view, enabled) {
		view.dispatch({effects: sharedParts().enabled.reconfigure(enabled ? extension() : [])});
		try {
			localStorage.setItem(STORAGE_KEY, String(enabled));
		} catch (e) {
			// storage is optional
		}
	}

	/** Returns whether the tables of the editor show as grids. */
	function isEnabled(view) {
		return view.state.field(sharedParts().field, false) !== undefined;
	}

	function isAvailable() {
		return typeof KnowWEEditor !== "undefined" && typeof KnowWEWikiLanguage !== "undefined"
			&& typeof Spreadsheet !== "undefined" && KnowWEEditor.isAvailable();
	}

	if (isAvailable() && KnowWEEditor.registerExtension) {
		addFloatingStyle();
		// only the page editor, the instant edit of a table has the table edit tool
		KnowWEEditor.registerExtension(options => options.mode === "page"
			? sharedParts().enabled.of(stored() ? extension() : []) : []);
	}

	return {setEnabled, isEnabled, isAvailable, tableText};
})();
