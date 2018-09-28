KNOWWE.plugin.tableEditTool = function() {

	const originalWikiText = {};
	const spreadsheet = {};
	const supportLinks = {};

	function createRootID(id) {
		return "tableEdit" + id;
	}

	function createButton(image, ext) {
		return "<a id='" + image + "' class='action'>" +
			"<img src='KnowWEExtension/images/table/" + image + "." + ext + "'></a>";
	}

	return {

		supportLinks : function(id, support) {
			supportLinks[id] = support;
		},

		getWikiText : function(id) {
			return _EC.getWikiText(id);
		},

		generateHTML : function(id) {
			let toolNameSpace = _EM.toolNameSpace[id];
			if (!toolNameSpace) toolNameSpace = _IE.toolNameSpace[id];
			originalWikiText[id] = toolNameSpace.getWikiText(id);
			return "<div id='" + createRootID(id) + "' style='position: relative; overflow: auto'></div>";
		},

		generateButtons : function(id) {
			return _EC.elements.getSaveCancelDeleteButtons(id, [
				createButton("table_insert_col_before", "gif"),
				createButton("table_insert_col_after", "gif"),
				createButton("table_insert_row_before", "gif"),
				createButton("table_insert_row_after", "gif"),
				createButton("table_delete_col", "png"),
				createButton("table_delete_row", "png"),
				createButton("toggle_header", "png")]);
		},

		getEventInfo : function($event) {
			return {
				target : $event.target,
				rect : $event.target.getBoundingClientRect(),
				clientX : $event.clientX,
				clientY : $event.clientY,
				pageX : $event.pageX,
				pageY : $event.pageY

			};
		},

		handleEventInfo : function(id, eventInfo) {
			let $td = jq$(eventInfo.target);
			if (!$td.is("td")) {
				$td = $td.parents('td').first();
			}
			if (!$td.exists()) return;
			const column = $td.index() + 1;
			const $tr = $td.parents("tr");
			const row = $tr.index() + 1;

			const $newTable = spreadsheet[id].element.parent();
			const $newTr = $newTable.find("tr:nth-of-type(" + row + ")");
			if (!$newTr.exists()) return;
			const $newTd = $newTr.find("td:nth-of-type(" + column + ")");
			if (!$newTd.exists()) return;
			const newRect = $newTd[0].getBoundingClientRect();

			const diffY = newRect.top - eventInfo.rect.top;
			const diffX = newRect.left - eventInfo.rect.left;
			jq$(window).scrollTop(jq$(window).scrollTop() + diffY);
			jq$(window).scrollLeft(jq$(window).scrollLeft() + diffX);

			const data = $newTd.data("cellInfo");
			data.spreadsheet.selectCell(data.row, data.col);

		},

		postProcessHTML : function(id) {
			spreadsheet[id] = new Spreadsheet(createRootID(id), function() {
				_IE.save(id)
			}, function() {
				_IE.cancel(id)
			});
			spreadsheet[id].setSupportLinks(supportLinks[id] == null ? true : supportLinks[id]);
			spreadsheet[id].setWikiMarkup(originalWikiText[id]);
			originalWikiText[id] = spreadsheet[id].getWikiMarkup();

			const root = spreadsheet[id].element.parent();
			root.find("#table_insert_row_before").click(function(event) {
				if (spreadsheet[id].selected) spreadsheet[id].addRow(spreadsheet[id].selected.row);
				event.preventDefault();
			});
			root.find("#table_insert_row_after").click(function(event) {
				if (spreadsheet[id].selected) spreadsheet[id].addRow(spreadsheet[id].selected.row + 1);
				event.preventDefault();
			});
			root.find("#table_delete_row").click(function(event) {
				spreadsheet[id].removeSelectedRows();
				event.preventDefault();
			});

			root.find("#table_insert_col_before").click(function(event) {
				if (spreadsheet[id].selected) spreadsheet[id].addCol(spreadsheet[id].selected.col);
				event.preventDefault();
			});
			root.find("#table_insert_col_after").click(function(event) {
				if (spreadsheet[id].selected) spreadsheet[id].addCol(spreadsheet[id].selected.col + 1);
				event.preventDefault();
			});
			root.find("#table_delete_col").click(function(event) {
				spreadsheet[id].removeSelectedCols();
				event.preventDefault();
			});

			root.find("#toggle_header").click(function(event) {
				if (!spreadsheet[id].selected) return;
				let header = spreadsheet[id].getCell(spreadsheet[id].selected.row, spreadsheet[id].selected.col).hasClass("header");
				spreadsheet[id].forEachSelected(function(cell, row, col) {
					spreadsheet[id].setHeader(row, col, !header);
				});
				spreadsheet[id].snapshot();
				event.preventDefault();
			});
		},

		unloadCondition : function(id) {
			return (!originalWikiText[id] && !spreadsheet[id])
				|| originalWikiText[id] === spreadsheet[id].getWikiMarkup();
		},

		generateWikiText : function(id) {
			spreadsheet[id].stopEditCell();
			return spreadsheet[id].getWikiMarkup();
		},

		/**
		 * Creates a new table editor. Optionally you can specify the function
		 * used for auto-completion. The function to be specified will get the
		 * following parameters:
		 * <ul>
		 * <li>callback: a function to be called with the auto-completion suggestions
		 * as they are prepard (see below)
		 * <li>prefix: text before cursor
		 * <li>table: the spreadsheet instance currently edited
		 * <li>row: the edited cell's row index (0..)
		 * <li>col: the edited cell's column index (0..)
		 * <ul>
		 * The method shall call the specified callback funtion with a javascript
		 * array of auto-completion suggestions. Each suggestion is a javascript
		 * object with the following parameters:
		 * <ul>
		 * <li>insertTest: the text to be inserted
		 * <li>title: (optional) title of the completion
		 * <li>description: (optional) description of the completion
		 * <li>iconPath: (optional) url to the icon of the completion
		 * <li>replaceLength: (optional) how many chars before the cursor shall be replaced
		 * <li>cursorPosition: (optional) where shall the cursor be positioned, relative to the start of the inserted text
		 * </ul>
		 */
		create : function(autoCompleteFun) {
			const created = jq$.extend({}, KNOWWE.plugin.tableEditTool);
			const super_postProcessHTML = created.postProcessHTML;
			created.postProcessHTML = function(id) {
				super_postProcessHTML(id);
				if (autoCompleteFun) {
					spreadsheet[id].setAutoCompleteFunction(autoCompleteFun);
				}
			};
			return created;
		}
	}
}();


function SpreadsheetModel(wikiText, supportLinks) {
	this.width = 1;
	this.height = 1;
	this.cells = [];

	if (wikiText) {
		// prepend and append returns for easier regex
		wikiText = ("\n" + wikiText + "\n");
		let firstTableLine = wikiText.search(/\n[ \t]*\|/); // pipe is first char after return
		if (firstTableLine === -1) {
			firstTableLine = wikiText.indexOf("\n", wikiText.search(/\S/) + 1);
		}
		if (firstTableLine > 1) {
			this.textBeforeTable = wikiText.substring(1, firstTableLine);
			wikiText = "\n" + wikiText.substring(firstTableLine + 1);
		}
		const lastTableLineEnd = wikiText.search(/(\n([^\|][^\n]*)?)*$/);
		if (lastTableLineEnd >= 0 && lastTableLineEnd < wikiText.length - 2) {
			this.textAfterTable = wikiText.substring(lastTableLineEnd + 1, wikiText.length - 1);
			wikiText = wikiText.substring(0, lastTableLineEnd + 1);
		}

		// normalize returns, remove multiples
		wikiText = wikiText.replace(/[\r\n]+/g, "\n");

		// avoid conflicting with already coded entities
		wikiText = wikiText.replace(/\&/g, "&amp;");

		// special treatment of multiple "~" 
		// followed by a potentially escaped character (by insert a required space)
		wikiText = wikiText.replace(/\~\~(\[|\\|\|)/g, "~~ $1");

		// replace "~" by entities
		// handle multiple "~", odd, but like jsp-wiki does
		// (by this code single "~" remain untouched!)
		while (wikiText.search(/\~\~\~/) !== -1) {
			wikiText = wikiText.replace(/\~\~\~/, "&#126;~~");
		}
		wikiText = wikiText.replace(/\~\~/g, "&#126;");

		// replace escaped opening "[" by html entity
		wikiText = wikiText.replace(/\~\[/g, "&#126;&#91;");

		// replace in-link pipes and escapes-characters by html entity
		if (supportLinks) {
			let oldText;
			do {
				oldText = wikiText;
				wikiText = wikiText.replace(/(\[[^\]]*)\|/g, "$1&#124;");	// "|"
				wikiText = wikiText.replace(/(\[[^\]]*)\~/g, "$1&#126;");	// "~"
				wikiText = wikiText.replace(/(\[[^\]]*)\\/g, "$1&#92;");	// "\"
			}
			while (oldText !== wikiText);
		}

		// replace remaining escaped "|" and "\" by html entity (outside links)
		wikiText = wikiText.replace(/\~\|/g, "&#124;");
		wikiText = wikiText.replace(/\~\\/g, "&#92;");
		wikiText = wikiText.replace(/\~\]/g, "&#93;");

		// and now parse the table structure,
		// remaining | are now surely cell separators (!)
		const lines = wikiText.match(/\n[ \t]*\|[^\n]*/g);
		if (lines == null) return;
		let row = 0;
		for (let i = 0; i < lines.length; i++) {
			let line = lines [i];
			line = line.replace(/\n/g, "");
			if (line.match(/^\s*$/g)) continue;
			const cells = line.match(/\|\|?[^\|]*/g);
			for (let col = 0; col < cells.length; col++) {
				const cell = cells[col];
				let text = "";
				let header = false;
				if (cell.charAt(1) === '|') {
					text = cell.substr(2);
					header = true;
				}
				else {
					text = cell.substr(1);
				}
				// within the cells, revert our html encoding
				text = text.replace(/\\u00A0/g, " ");
				text = jq$.trim(text);
				text = text.replace(/\\\\/g, "\n");
				text = text.replace(/\&\#124;/g, "|");
				text = text.replace(/\&\#126;/g, "~");
				text = text.replace(/\&\#91;/g, "[");
				text = text.replace(/\&\#92;/g, "\\");
				text = text.replace(/\&\#93;/g, "]");
				// reveal our initial "&" encoding
				text = text.replace(/\&amp;/g, "&");
				this.setCell(row, col, text, header);
			}
			row++;
		}
	}
}

SpreadsheetModel.prototype.setCell = function(row, col, text, isHeader) {
	this.ensureSize(row + 1, col + 1);
	const key = row + "_" + col;
	this.cells[key] = {row : row, col : col, text : text, isHeader : isHeader};
};

SpreadsheetModel.prototype.ensureSize = function(rowCount, colCount) {
	this.height = Math.max(this.height, rowCount);
	this.width = Math.max(this.width, colCount);
};

SpreadsheetModel.prototype.getCellText = function(row, col) {
	const key = row + "_" + col;
	const data = this.cells[key];
	return data ? data.text : "";
};

SpreadsheetModel.prototype.isHeader = function(row, col) {
	// use header attribute of the cell
	const key = row + "_" + col;
	const data = this.cells[key];
	if (data) return data.isHeader;
	return false;
};

SpreadsheetModel.prototype.toWikiMarkup = function(supportLinks) {
	let wikiText = this.textBeforeTable ? this.textBeforeTable : "";
	for (let row = 0; row < this.height; row++) {
		for (let col = 0; col < this.width; col++) {
			let cellText = this.getCellText(row, col);

			// avoid conflicting with already coded entities
			cellText = cellText.replace(/\&/g, "&amp;");

			// recognize single escaped opening "["
			cellText = cellText.replace(/\~\[/g, "&#126;&#91;");

			// remain contents of links unchanged
			if (supportLinks) {
				let oldText;
				// fix unclosed links by adding escape character
				// and use html entities to prevent link treatment
				do {
					oldText = cellText;
					cellText = cellText.replace(/^(.*)\[([^\]]*)$/g, "$1&#126;&#91;$2");
				}
				while (oldText !== cellText);

				// prevent inner-link-text to be escaped by using html entities
				do {
					oldText = cellText;
					cellText = cellText.replace(/(\[[^\]]*)\|/g, "$1&#124;");	// "|"
					cellText = cellText.replace(/(\[[^\]]*)\~/g, "$1&#126;");	// "~"
					cellText = cellText.replace(/(\[[^\]]*)\\/g, "$1&#92;");	// "\"
				}
				while (oldText !== cellText);
			}

			// special handling: "~~|" will be recognized 
			// through jsp-wiki as new cell --> avoid this 
			// by handling ~| directly and insert required space
			cellText = cellText.replace(/\~\|/g, "&#126; &#124;");

			// escape special characters (outside links only)
			cellText = cellText.replace(/(\~+)/g, "~$1");
			cellText = cellText.replace(/\|/g, "~|");

			// escape remaining special characters
			cellText = cellText.replace(/\r?\n/g, "\\\\");	// RETURN --> "\\"
			cellText = cellText.replace(/\\u00A0/g, " ");	// &nbsp; --> " "

			// reveal our used html entities
			cellText = cellText.replace(/\&\#124;/g, "|");
			cellText = cellText.replace(/\&\#126;/g, "~");
			cellText = cellText.replace(/\&\#91;/g, "[");
			cellText = cellText.replace(/\&\#92;/g, "\\");

			// reveal our initial "&" encoding
			cellText = cellText.replace(/\&amp;/g, "&");

			// create cell markup
			wikiText += this.isHeader(row, col) ? "|| " : "|  ";
			wikiText += cellText;
			wikiText += "\t";
		}
		wikiText += "\n";
	}
	if (this.textAfterTable) wikiText += this.textAfterTable;
	return wikiText;

};

function Spreadsheet(elementID, saveFun, cancelFun) {
	this.elementID = elementID;
	this.saveFunction = saveFun;
	this.cancelFunction = cancelFun;
	this.supportLinks = true;
	this.element = jq$("#" + elementID);
	this.undoHistory = [];
	this.redoHistory = [];

	this.createTable();
	this.selectCell(0, 0);
}
Spreadsheet.prototype.setWikiMarkup = function(wikiText) {
	this.setModel(new SpreadsheetModel(wikiText, this.supportLinks));
	this.snapshot();
};

Spreadsheet.prototype.setSupportLinks = function(support) {
	this.supportLinks = support;
};

Spreadsheet.prototype.setModel = function(model) {
	this.element.html("");
	this.createTable(model);
	this.selectCell(0, 0);
	this.textBeforeTable = model.textBeforeTable;
	this.textAfterTable = model.textAfterTable;
};

Spreadsheet.prototype.getWikiMarkup = function() {
	return this.getModel().toWikiMarkup(this.supportLinks);
};

Spreadsheet.prototype.getModel = function() {
	const model = new SpreadsheetModel();
	this.element.find("tr").each(function(rowIndex) {
		jq$(this).find("td").each(function(colIndex) {
			const isHeader = jq$(this).hasClass("header");
			const text = jq$(this).find("div > a").text();
			model.setCell(rowIndex, colIndex, text, isHeader);
		});
	});
	model.textBeforeTable = this.textBeforeTable;
	model.textAfterTable = this.textAfterTable;
	return model;
};

Spreadsheet.prototype.createTable = function(model) {
	if (!model) model = new SpreadsheetModel();
	this.size = {rows : model.height, cols : model.width};
	this.selected = null;
	this.selectedRange = null;
	this.copied = null;

	let html = "";
	html += "<table class='wikitable spreadsheet'>\n";
	html += "</tr>\n";
	let row, col, cell;
	for (row = 0; row < this.size.rows; row++) {
		html += "<tr>\n";
		for (col = 0; col < this.size.cols; col++) {
			const isHeader = model.isHeader(row, col);
			const text = _EC.encodeForHtml(model.getCellText(row, col));
			html += "<";
			html += isHeader ? "td class=header" : "td";
			html += " id='" + this.getCellID(row, col) + "'><div><a href='#'>";
			html += text ? text : "&nbsp;";
			html += "</a></div>";
		}
		html += "</tr>\n";
	}
	html += "</table>";
	this.element.append(html);

	for (row = 0; row < this.size.rows; row++) {
		for (col = 0; col < this.size.cols; col++) {
			cell = this.getCell(row, col);
			cell.data("cellInfo", {spreadsheet : this, row : row, col : col});
		}
	}

	// add click event to select data
	this.element.find(" tr > td").click(function(event) {
		const cell = jq$(this);
		const data = cell.data("cellInfo");
		data.spreadsheet.selectCell(data.row, data.col, event.shiftKey || event.metaKey);
		event.preventDefault();
	});

	// double click event to enter edit mode
	this.element.find(" tr > td").dblclick(function(event) {
		const cell = jq$(this);
		const data = cell.data("cellInfo");
		data.spreadsheet.editCell(data.row, data.col);
		event.preventDefault();
	});

	// add keyboard event to select/edit data
	this.element.find(" tr > td > div > a").keydown(function(event) {
		const cell = jq$(this).parents(" tr > td");
		const data = cell.data("cellInfo");
		const multi = event.shiftKey;
		const command = event.ctrlKey || event.metaKey;
		const alt = event.altKey;

		// toplevel handle undo/redo
		if (_EC.isModifier(event)) {
			if (event.keyCode === 89 || (event.keyCode === 90 && event.shiftKey)) { // Y
				event.stop();
				data.spreadsheet.redo();
				return;
			}
			if (event.keyCode === 90) { // Z
				event.stop();
				data.spreadsheet.snapshot();
				data.spreadsheet.undo();
				return;
			}
		}
		// otherwise handle event normally
		const handled = data.spreadsheet.handleKeyDown(cell, event.keyCode, multi, command, alt);
		if (handled) {
			event.preventDefault();
		}
	});

	this.element.find(" tr > td > div > a").blur(function(event) {
		const cell = jq$(this).parents(" tr > *");
		cell.removeClass("selected");
	});
};

/**
 * Handles key down event end returns boolean if the key was handled or not
 */
Spreadsheet.prototype.handleKeyDown = function(cell, keyCode, multiSelect, command, alt) {
	const row = this.selected.row;
	const col = this.selected.col;
	let toCol, roRow;
	// Backspace + ENTF
	if (keyCode === 8 || keyCode === 46) {
		this.clearSelectedCells();
	}
	// return, or F2 (Excel Windows), or Ctrl+u (Excel Mac) 
	else if (keyCode === 13 || keyCode === 113 || (keyCode === 85 && command)) {
		this.editCell(row, col);
	}
	// Ctrl+Space for edit mode and auto-completion (if available)
	else if (keyCode === 32 && command) {
		this.editCell(row, col);
		this.showAutoComplete(this.createCellAreaID(row, col));
	}
	// left
	else if (keyCode === 37 && !alt) {
		toCol = command ? 0 : col - 1;
		if (toCol >= 0) this.selectCell(row, toCol, multiSelect);
	}
	// up
	else if (keyCode === 38 && !alt) {
		toRow = command ? 0 : row - 1;
		if (toRow >= 0) this.selectCell(toRow, col, multiSelect);
	}
	// right
	else if (keyCode === 39 && !alt) {
		toCol = command ? this.size.cols - 1 : col + 1;
		if (toCol <= this.size.cols - 1) this.selectCell(row, toCol, multiSelect);
	}
	// down
	else if (keyCode === 40 && !alt) {
		toRow = command ? this.size.rows - 1 : row + 1;
		if (toRow <= this.size.rows - 1) this.selectCell(toRow, col, multiSelect);
	}
	// shift cells (command for not selecting full row/col)
	// left + alt 
	else if (keyCode === 37 && alt) {
		if (!command) this.extendSelection(true, false);
		this.shiftSelectedCells(0, -1)
	}
	// up + alt
	else if (keyCode === 38 && alt) {
		if (!command) this.extendSelection(false, true);
		this.shiftSelectedCells(-1, 0)
	}
	// right + alt
	else if (keyCode === 39 && alt) {
		if (!command) this.extendSelection(true, false);
		this.shiftSelectedCells(0, 1)
	}
	// down + alt
	else if (keyCode === 40 && alt) {
		if (!command) this.extendSelection(false, true);
		this.shiftSelectedCells(1, 0)
	}
	// tab
	else if (keyCode === 9 && !multiSelect) {
		if (col < this.size.cols - 1) {
			this.selectCell(row, col + 1);
		}
		else if (row < this.size.rows - 1) {
			this.selectCell(row + 1, 0);
		}
		else {
			// if we reached end, we will add an additional row
			this.addRow(row + 1);
			this.selectCell(row + 1, 0);
		}
	}
	// backward tab
	else if (keyCode === 9 && multiSelect) {
		if (col > 0) {
			this.selectCell(row, col - 1);
		}
		else if (row > 0) {
			this.selectCell(row - 1, this.size.cols - 1);
		}
	}
	// cut + copy
	else if ((keyCode === 88 || keyCode === 67) && command) {
		this.copySelectedCells(keyCode === 88);
	}
	// paste
	else if (keyCode === 86 && command) {
		this.pasteCopiedCells();
	}
	// save: 's'
	else if (keyCode === 83 && command && this.saveFunction) {
		this.stopEditCell();
		return false;
//		this.saveFunction();
	}
	// cancel: 'q'
	else if (keyCode === 81 && command && this.cancelFunction) {
		this.stopEditCell(true);
//		this.cancelFunction();
		return false;
	}
	// delete line: command+'d' or alt+'d'
	else if (keyCode === 68 && ((command && !alt) || (!command && alt))) {
		this.removeSelectedRows();
	}
	// ESC
	else if (keyCode === 27) {
		this.uncopyCopiedCells();
	}
	// normal typing
	else if (!command &&
		((keyCode >= 32 && keyCode <= 90)
		|| (keyCode >= 96 && keyCode <= 106)
		|| (keyCode >= 186 && keyCode <= 222))) {
		this.editCell(row, col);
		// return false to use first key pressed as input
		return false;
	}
	// ignore all others
	else {
		return false;
	}
	return true;
};

Spreadsheet.prototype.stopEditCell = function(cancel) {
	if (this.stopEditCellFunction) {
		this.stopEditCellFunction(cancel);
	}
};

Spreadsheet.prototype.createCellAreaID = function(row, col) {
	return "cellEditArea_" + this.elementID + "_" + row + col;
};

Spreadsheet.prototype.editCell = function(row, col) {
	this.stopEditCell();
	this.uncopyCopiedCells();
	this.selectCell(row, col);
	const contentElement = this.getSelectedCell().find("div > a");
	const pos = contentElement.parent().parent().position();
	const textAreaID = this.createCellAreaID(row, col);
	let cellText = this.getCellText(row, col);
	if (cellText.match(/^[ \t\u00A0\u200B]*$/g)) cellText = "";
	let html = "";
	html += "<div class='cellEdit' style='";
	html += "left:" + (pos.left - 3) + "px;top:" + (pos.top - 3) + "px;";
	html += "'>";
	html += "<textarea id='" + textAreaID + "' style='";
	html += "width:" + (contentElement.width() + 16) + "px";
	html += "'>";
	html += _EC.encodeForHtml(cellText);
	html += "</textarea>";
	html += "</div>";
	this.element.prepend(html);
	const editDiv = this.element.children("div");
	const editArea = editDiv.children("textarea");
	editArea.focus();
	editArea.select();
	jq$(editArea).autosize();
	// then adding our key and event handling
	const spreadsheet = this;
	let closing = false; // flag to avoid multiple closing (detach forces focusout-event)
	const keyDownFunction = function (event) {
		if (closing) return;
		const keyCode = event.which;
		const command = event.ctrlKey || event.metaKey;
		// ignore return key if auto-complete is on
		if ((keyCode === 13 || keyCode === 27) && spreadsheet.isAutoCompleteFocused(textAreaID)) return;
		if ((keyCode === 13 && !event.altKey && !event.shiftKey) || (keyCode === 9 && !event.altKey)) {
			spreadsheet.setCellText(row, col, editArea.val());
			spreadsheet.snapshot();

		}
		// save: 's'
		else if (keyCode === 83 && command && spreadsheet.saveFunction) {
			spreadsheet.stopEditCell();
			spreadsheet.saveFunction();
			return;
		}
		// cancel: 'q'
		else if (keyCode === 81 && command && spreadsheet.cancelFunction) {
			spreadsheet.stopEditCell(true);
			spreadsheet.cancelFunction();
			return;
		}
		// ESC
		else if (keyCode === 27) {
		}
		else {
			return;
		}
		closing = true;
		event.preventDefault();
		editDiv.detach();
		spreadsheet.selectCell(row, col);
		// on tab or return, select next cell as well
		if (keyCode === 9) {
			spreadsheet.handleKeyDown(spreadsheet.getSelectedCell(), keyCode, event.shiftKey, false, false);
		}
		if (keyCode === 13) {
			spreadsheet.handleKeyDown(spreadsheet.getSelectedCell(), 40, false, false, false);
		}
	};
	editArea.keydown(keyDownFunction);
	this.stopEditCellFunction = function(cancel) {
		if (closing) return;
		closing = true;
		spreadsheet.uninstallAutoComplete(textAreaID);
		if (!cancel) spreadsheet.setCellText(row, col, editArea.val());
		editDiv.detach();
		spreadsheet.selectCell(row, col);
	};
	//editArea.focusout(this.stopEditCellFunction);	

	// install auto-complete finally
	this.installAutoComplete(textAreaID, row, col);
};

Spreadsheet.prototype.isAutoCompleteFocused = function(id) {
	if (typeof AutoComplete !== "undefined") {
		return jq$('#' + id)[0].autocompletion.hasFocus();
	} else {
		return false;
	}
};

Spreadsheet.prototype.showAutoComplete = function(id) {
	if (typeof AutoComplete !== "undefined") {
		jq$('#' + id)[0].autocompletion.requestFocus();
		jq$('#' + id)[0].autocompletion.requestCompletions();
	}
};

Spreadsheet.prototype.uninstallAutoComplete = function(id) {
	if (typeof AutoComplete !== "undefined") {
		jq$('#' + id)[0].autocompletion.showCompletions(null);
	}
};

Spreadsheet.prototype.installAutoComplete = function(textAreaID, row, col) {
	// enable auto-completion if available
	// but we require some special functionality because only editing part of table
	const spreadsheet = this;
	const completeFun = this.customAutoCompleteFunction
		? jq$.proxy(function (callback, prefix) {
			this.customAutoCompleteFunction(callback, prefix, spreadsheet, row, col);
		}, this)
		: function (callback, prefix) {
			callback(spreadsheet.getColumnCellCompletionSuggestions(prefix, col));
		};
	const textarea = jq$('#' + textAreaID)[0];
	new TextArea(textarea, true);
	if (typeof AutoComplete !== "undefined") {
		new AutoComplete(textarea, completeFun);
	}
};

/**
 * creates completion suggestions according to the other cells of the same column.
 */
Spreadsheet.prototype.getColumnCellCompletionSuggestions = function(prefix, col) {
	const trimPrefix = prefix.trim();
	const json = [];
	const textCache = {};
	for (let r = 0; r < this.size.rows; r++) {
		// use each text once
		const text = this.getCellText(r, col).trim();
		if (textCache[text]) continue;
		textCache[text] = "done";
		// check if it can be used for completion
		if (text.length === 0) continue;
		if (text.length < trimPrefix.length) continue;
		if (text.substring(0, trimPrefix.length) !== trimPrefix) continue;
		json.push({
			insertText : text,
			replaceLength : prefix.length
		});
	}
	return json;
};

/**
 * Sets the function used for autocompletion. The function to be specified will get the
 * following parameters:
 * <ul>
 * <li>callback: a function to be called with the auto-completion suggestions as they are prepard (see below)
 * <li>prefix: text before cursor
 * <li>table: the spreadsheet instance currently edited
 * <li>row: the edited cell's row index (0..)
 * <li>col: the edited cell's column index (0..)
 * <ul>
 * The method shall cann the specified cabblack funtion with a javascript array of auto-completion suggestions.
 * Each suggestion is a javascript object with the following parameters:
 * <ul>
 * <li>title: title of the completion
 * <li>insertTest: the text to be inserted
 * <li>replaceLength: (optional) how many chars before the cursor shall be replaced
 * <li>cursorPosition: (optional) where shall the cursor be positioned, relative to the start of the inserted text
 * </ul>
 */
Spreadsheet.prototype.setAutoCompleteFunction = function(fun) {
	this.customAutoCompleteFunction = fun;
};

Spreadsheet.prototype.setCellText = function(row, col, text) {
	this.stopEditCell();
	const elem = this.getCell(row, col).find("div > a");
	if (!text || text.match(/^\s+$/g)) {
		elem.html("&nbsp;"); // avoid cell collapsing
	}
	else {
		elem.text(text);
	}
};

Spreadsheet.prototype.getCellTextTrimmed = function(row, col) {
	return jq$.trim(this.getCellText(row, col).replace(/\\u00A0/g, " "));
};

Spreadsheet.prototype.getCellText = function(row, col) {
	const elem = this.getCell(row, col).find("div > a");
	return elem.text();
};

/**
 * Extends the selection to complete rows / cols including the selected cells.
 */
Spreadsheet.prototype.extendSelection = function(extendRows, extendCols) {
	if (!this.selected) return;

	// get the selected area
	let r1 = this.selected.row;
	let c1 = this.selected.col;
	let r2 = this.selectedRange ? this.selectedRange.toRow : r1;
	let c2 = this.selectedRange ? this.selectedRange.toCol : c1;

	// extend selection
	if (extendRows) {
		r1 = 0;
		r2 = this.size.rows - 1;
	}
	if (extendCols) {
		c1 = 0;
		c2 = this.size.cols - 1;
	}

	this.selectCell(r2, c2, false);
	this.selectCell(r1, c1, true);
};

/**
 * Selects the specified cell. Use this method without arguments
 * to deselect all cells.
 */
Spreadsheet.prototype.selectCell = function(row, col, multiSelect) {
	this.stopEditCell();
	let cell = this.getSelectedCell();
	if (cell) {
		cell.removeClass("selected");
	}
	if (row === undefined && col === undefined) {
		this.selected = null;
		if (cell) cell.find("div > a").blur();
		return;
	}
	// deselect multi-selected cells
	this.forEachSelected(function(cell) {
		cell.removeClass("multiSelected");
	});
	// if multi-select is requested, select from previous selected cell to requested cell
	if (multiSelect) {
		if (!this.selectedRange) {
			const toRow = this.selected.row;
			const toCol = this.selected.col;
			this.selectedRange = {toRow : toRow, toCol : toCol};
		}
	}
	else {
		this.selectedRange = null;
	}
	// do normal selection as well
	this.selected = {row : row, col : col};
	cell = this.getSelectedCell();
	cell.addClass("selected");
	this.forEachSelected(function(cell) {
		cell.addClass("multiSelected");
	});
	cell.find("div > a").focus();
};

Spreadsheet.prototype.forEachSelected = function(fun) {
	if (!this.selected) return;
	let row, col;
	if (!this.selectedRange) {
		row = this.selected.row;
		col = this.selected.col;
		fun(this.getCell(row, col), row, col);
		return;
	}
	const r1 = this.selected.row;
	const c1 = this.selected.col;
	const r2 = this.selectedRange.toRow;
	const c2 = this.selectedRange.toCol;
	for (row = Math.min(r1, r2); row <= Math.max(r1, r2); row++) {
		for (col = Math.min(c1, c2); col <= Math.max(c1, c2); col++) {
			fun(this.getCell(row, col), row, col);
		}
	}
};

Spreadsheet.prototype.clearSelectedCells = function() {
	const sheet = this;
	this.forEachSelected(function(cell, row, col) {
		sheet.setCellText(row, col, "");
	});
	this.snapshot();
};

/**
 * Moves the selected cells in the appropriate direction. The cells to be overwritten
 * are flowing around the shifted cells and will be appended on the other side.
 * The selected cells are shiftey be dRow and dCol realtively. by specifying
 * wrapAround you can specify if the shift stops at the end of the table borders
 * or wraps around these border.
 */
Spreadsheet.prototype.shiftSelectedCells = function(dRow, dCol, wrapAround) {
	// prepare selection
	if (!this.selected) return;
	if (!this.selectedRange) this.selectCell(this.selected.row, this.selected.col, true);
	this.uncopyCopiedCells();

	// get the selected area
	const r1 = this.selected.row;
	const c1 = this.selected.col;
	const r2 = this.selectedRange.toRow;
	const c2 = this.selectedRange.toCol;
	let areaRow = Math.min(r1, r2);
	let areaCol = Math.min(c1, c2);
	let areaHeight = Math.abs(r1 - r2) + 1;
	let areaWidth = Math.abs(c1 - c2) + 1;

	// check wrap around
	if (!wrapAround) {
		if (areaRow + dRow < 0) {
			dRow = -areaRow;
		}
		if (areaCol + dCol < 0) {
			dCol = -areaCol;
		}
		if (areaRow + areaHeight + dRow > this.size.rows) {
			dRow = this.size.rows - areaRow - areaHeight;
		}
		if (areaCol + areaWidth + dCol > this.size.cols) {
			dCol = this.size.cols - areaCol - areaWidth;
		}
	}
	// check if there is something to do at all
	if (dRow === 0 && dCol === 0) return;

	// extend the area by dRow and dCol
	areaHeight += Math.abs(dRow);
	areaWidth += Math.abs(dCol);
	if (dRow < 0) areaRow += dRow;
	if (dCol < 0) areaCol += dCol;


	// be careful if ranges overlapping
	// therefore first copy to array
	const data = [];
	let index = 0;
	let row, col;
	for (row = areaRow; row < areaRow + areaHeight; row++) {
		for (col = areaCol; col < areaCol + areaWidth; col++) {
			data[index++] = this.getCellText(row % this.size.rows, col % this.size.cols);
		}
	}
	// and then insert copied texts ba shifting the coordinares
	// flowing around the area borders
	index = 0;
	for (row = areaRow; row < areaRow + areaHeight; row++) {
		for (col = areaCol; col < areaCol + areaWidth; col++) {
			const text = data[index++];
			// let them flow inside the area
			const toRow = (row - areaRow + dRow + areaHeight) % areaHeight + areaRow;
			const toCol = (col - areaCol + dCol + areaWidth) % areaWidth + areaCol;
			// and set to new position
			this.setCellText(toRow % this.size.rows, toCol % this.size.cols, text);
		}
	}

	this.selectCell(
		Math.min(Math.max(0, r2 + dRow), this.size.rows - 1),
		Math.min(Math.max(0, c2 + dCol), this.size.cols - 1), false);
	this.selectCell(
		Math.min(Math.max(0, r1 + dRow), this.size.rows - 1),
		Math.min(Math.max(0, c1 + dCol), this.size.cols - 1), true);
	this.snapshot();
};

Spreadsheet.prototype.pasteCopiedCells = function() {
	if (!this.copied) return;
	if (!this.selected) return;
	if (!this.selectedRange) this.selectCell(this.selected.row, this.selected.col, true);
	const r1 = this.selected.row;
	const c1 = this.selected.col;
	const r2 = this.selectedRange.toRow;
	const c2 = this.selectedRange.toCol;
	const destRow = Math.min(r1, r2);
	const destCol = Math.min(c1, c2);
	let destHeight = Math.abs(r1 - r2) + 1;
	let destWidth = Math.abs(c1 - c2) + 1;
	const srcRow = this.copied.row;
	const srcCol = this.copied.col;
	const srcHeight = this.copied.toRow - srcRow + 1;
	const srcWidth = this.copied.toCol - srcCol + 1;
	if (destHeight === 1 && destWidth === 1) {
		destHeight = srcHeight;
		destWidth = srcWidth;
		this.selectCell(Math.min(destRow + destHeight - 1, this.size.rows), Math.min(destCol + destWidth - 1, this.size.cols));
		this.selectCell(destRow, destCol, true);
	}
	else if (destHeight !== srcHeight || destWidth !== srcWidth) {
		// beep();
		return;
	}

	// be careful if ranges overlapping
	const data = [];
	let index = 0;
	for (row = srcRow; row < srcRow + srcHeight; row++) {
		for (col = srcCol; col < srcCol + srcWidth; col++) {
			data[index++] = this.getCellText(row, col);
			if (this.copied.doCut) this.setCellText(row, col, "");
		}
	}
	index = 0;
	for (row = destRow; row < destRow + destHeight; row++) {
		for (col = destCol; col < destCol + destWidth; col++) {
			if (row >= this.size.rows) continue;
			if (col >= this.size.cols) continue;
			this.setCellText(row, col, data[index++]);
		}
	}

	if (this.copied.doCut) {
		this.uncopyCopiedCells();
	}
	this.snapshot();
};

Spreadsheet.prototype.copySelectedCells = function(doCut) {
	if (!this.selected) return;
	if (!this.selectedRange) this.selectCell(this.selected.row, this.selected.col, true);
	this.uncopyCopiedCells();
	const r1 = this.selected.row;
	const c1 = this.selected.col;
	const r2 = this.selectedRange.toRow;
	const c2 = this.selectedRange.toCol;
	this.copied = {
		row : Math.min(r1, r2), col : Math.min(c1, c2),
		toRow : Math.max(r1, r2), toCol : Math.max(c1, c2),
		doCut : doCut
	};
	this.forEachCopied(function(cell) {
		cell.addClass("copied");
	});
};

Spreadsheet.prototype.uncopyCopiedCells = function() {
	this.forEachCopied(function(cell) {
		cell.removeClass("copied");
	});
	this.copied = null;
};

Spreadsheet.prototype.forEachCopied = function(fun) {
	if (!this.copied) return;
	for (let row = this.copied.row; row <= this.copied.toRow; row++) {
		for (let col = this.copied.col; col <= this.copied.toCol; col++) {
			fun(this.getCell(row, col), row, col);
		}
	}
};

Spreadsheet.prototype.getSelectedCell = function() {
	if (!this.selected) return null;
	return this.getCell(this.selected.row, this.selected.col);
};

Spreadsheet.prototype.getCell = function(row, col) {
	return this.element.find("#" + this.getCellID(row, col));
};

Spreadsheet.prototype.getCellID = function(row, col) {
	return "cell_" + row + "_" + col;
};

Spreadsheet.prototype.addRow = function(row) {
	this.stopEditCell();
	let sr = this.selected ? this.selected.row : 0;
	const sc = this.selected ? this.selected.col : 0;
	if (sr >= row) sr++;
	// copy model, adding empty row before "row"
	const srcModel = this.getModel();
	const destModel = new SpreadsheetModel();
	let destRow = 0;
	for (let srcRow = 0; srcRow < srcModel.height; srcRow++) {
		if (srcRow === row) destRow++; // add line
		let destCol = 0;
		for (let srcCol = 0; srcCol < srcModel.width; srcCol++) {
			let isHeader = srcModel.isHeader(srcRow, srcCol);
			const text = srcModel.getCellText(srcRow, srcCol);
			destModel.setCell(destRow, destCol, text, isHeader);
			destCol++;
		}
		destRow++;
	}
	// if we add last row, make sure that size increases
	destModel.ensureSize(srcModel.height + 1, srcModel.width);
	// copy format from selected row
	for (let col = 0; col < destModel.width; col++) {
		let isHeader = destModel.isHeader(sr, col);
		destModel.setCell(row, col, "", isHeader);
	}
	// set new Model and restore selection
	destModel.textBeforeTable = srcModel.textBeforeTable;
	destModel.textAfterTable = srcModel.textAfterTable;
	this.setModel(destModel);
	this.selectCell(sr, sc);
	this.snapshot();
};

Spreadsheet.prototype.removeSelectedRows = function() {
	if (!this.selected) return;
	if (!this.selectedRange) this.selectCell(this.selected.row, this.selected.col, true);
	const r1 = this.selected.row;
	const r2 = this.selectedRange.toRow;
	const upperRow = Math.min(r1, r2);
	const lowerRow = Math.max(r1, r2);
	this.removeRow(upperRow, lowerRow);
};

Spreadsheet.prototype.removeSelectedCols = function() {
	if (!this.selected) return;
	if (!this.selectedRange) this.selectCell(this.selected.row, this.selected.col, true);
	const c1 = this.selected.col;
	const c2 = this.selectedRange.toCol;
	const leftCol = Math.min(c1, c2);
	const rightCol = Math.max(c1, c2);
	this.removeCol(leftCol, rightCol);
};

Spreadsheet.prototype.removeRow = function(upperRow, lowerRow) {
	this.stopEditCell();
	if (this.size.rows <= 1) return;
	let sr = this.selected ? Math.min(this.selected.row, this.size.rows - 2) : 0;
	const sc = this.selected ? this.selected.col : 0;
	if (sr > upperRow) sr--;
	// copy model, removing "row"
	const srcModel = this.getModel();
	const destModel = new SpreadsheetModel();
	let destRow = 0;
	for (let srcRow = 0; srcRow < srcModel.height; srcRow++) {
		// ignore lines to delete
		if (upperRow <= lowerRow && srcRow >= upperRow && srcRow <= lowerRow) continue;
		if (!lowerRow && srcRow === upperRow) continue; // no range is  given, just single row
		let destCol = 0;
		for (let srcCol = 0; srcCol < srcModel.width; srcCol++) {
			const isHeader = srcModel.isHeader(srcRow, srcCol);
			const text = srcModel.getCellText(srcRow, srcCol);
			destModel.setCell(destRow, destCol, text, isHeader);
			destCol++;
		}
		destRow++;
	}
	// set new Model and restore selection
	destModel.textBeforeTable = srcModel.textBeforeTable;
	destModel.textAfterTable = srcModel.textAfterTable;
	this.setModel(destModel);
	this.selectCell(sr, sc);
	this.snapshot();
};

Spreadsheet.prototype.addCol = function(col) {
	this.stopEditCell();
	const sr = this.selected ? this.selected.row : 0;
	let sc = this.selected ? this.selected.col : 0;
	if (sc >= col) sc++;
	// copy model, adding empty row before "row"
	const srcModel = this.getModel();
	const destModel = new SpreadsheetModel();
	let destRow = 0;
	for (let srcRow = 0; srcRow < srcModel.height; srcRow++) {
		let destCol = 0;
		for (let srcCol = 0; srcCol < srcModel.width; srcCol++) {
			if (srcCol === col) destCol++; // add column
			let isHeader = srcModel.isHeader(srcRow, srcCol);
			const text = srcModel.getCellText(srcRow, srcCol);
			destModel.setCell(destRow, destCol, text, isHeader);
			destCol++;
		}
		destRow++;
	}
	// if we add last column, make sure that size increases
	destModel.ensureSize(srcModel.height, srcModel.width + 1);
	// copy format from selected col
	for (let row = 0; row < destModel.height; row++) {
		let isHeader = destModel.isHeader(row, sc);
		destModel.setCell(row, col, "", isHeader);
	}
	// set new Model and restore selection
	destModel.textBeforeTable = srcModel.textBeforeTable;
	destModel.textAfterTable = srcModel.textAfterTable;
	this.setModel(destModel);
	this.selectCell(sr, sc);
	this.snapshot();
};

Spreadsheet.prototype.removeCol = function(leftCol, rightCol) {
	this.stopEditCell();
	if (this.size.cols <= 1) return;
	const sr = this.selected ? this.selected.row : 0;
	let sc = this.selected ? Math.min(this.selected.col, this.size.cols - 2) : 0;
	if (sc > leftCol) sc--;
	// copy model, removing "row"
	const srcModel = this.getModel();
	const destModel = new SpreadsheetModel();
	let destRow = 0;
	for (let srcRow = 0; srcRow < srcModel.height; srcRow++) {
		let destCol = 0;
		for (let srcCol = 0; srcCol < srcModel.width; srcCol++) {
			// ignore cols to delete
			if (leftCol <= rightCol && srcCol >= leftCol && srcCol <= rightCol) continue;
			if (!rightCol && srcCol === leftCol) continue; // no range is  given, just single col
			const isHeader = srcModel.isHeader(srcRow, srcCol);
			const text = srcModel.getCellText(srcRow, srcCol);
			destModel.setCell(destRow, destCol, text, isHeader);
			destCol++;
		}
		destRow++;
	}
	// set new Model and restore selection
	destModel.textBeforeTable = srcModel.textBeforeTable;
	destModel.textAfterTable = srcModel.textAfterTable;
	this.setModel(destModel);
	this.selectCell(sr, sc);
	this.snapshot();
};

Spreadsheet.prototype.setHeader = function(row, col, isHeader) {
	this.stopEditCell();
	const sr = this.selected ? this.selected.row : 0;
	const sc = this.selected ? this.selected.col : 0;
	const tr = this.selectedRange ? this.selectedRange.toRow : sr;
	const tc = this.selectedRange ? this.selectedRange.toCol : sc;
	const cell = this.getCell(row, col);
	if (isHeader) {
		cell.addClass("header");
	}
	else {
		cell.removeClass("header");
	}
	this.selectCell(tr, tc, false);
	this.selectCell(sr, sc, true);
};

Spreadsheet.prototype.snapshot = function(row, col, isHeader) {
	const model = this.getModel();
	const undo = this.undoHistory, redo = this.redoHistory;
	if (undo.length > 0 && Spreadsheet.areEqual(undo[undo.length - 1].model, model)) {
		return;
	}
	if (redo.length > 0 && Spreadsheet.areEqual(redo[redo.length - 1].model, model)) {
		return;
	}
	// if we have a redo history, we take the recent element back to the undo history,
	// because it was the text fields original state before editing again
	if (redo.length > 0) {
		const shot = redo.pop();
		undo.push(shot);
		this.redoHistory = [];
	}

	const snap = {
		model: jq$.extend(true, {}, model),
		selected: null,
		selectedRange: null
	};
	if (this.selected) snap.selected = jq$.extend(true, {}, this.selected);
	if (this.selectedRange) snap.selectedRange = jq$.extend(true, {}, this.selectedRange);
	this.undoHistory.push(snap);
};

Spreadsheet.prototype.undo = function() {
	const model = this.getModel();
	while (true) {
		if (this.undoHistory.length === 0) return;
		let shot = this.undoHistory.pop();
		this.redoHistory.push(shot);
		if (!Spreadsheet.areEqual(shot.model, model)) break;
	}
	this.restoreSnapshot(shot);
};

Spreadsheet.prototype.redo = function() {
	const model = this.getModel();
	while (true) {
		if (this.redoHistory.length === 0) return;
		let shot = this.redoHistory.pop();
		this.undoHistory.push(shot);
		if (!Spreadsheet.areEqual(shot.model, model)) break;
	}
	this.restoreSnapshot(shot);
};

Spreadsheet.prototype.restoreSnapshot = function(snapshot) {
	this.uncopyCopiedCells();
	this.setModel(snapshot.model);
	const sel = snapshot.selected;
	const range = snapshot.selectedRange;
	if (sel) {
		if (range) this.selectCell(range.toRow, range.toCol, false);
		this.selectCell(sel.row, sel.col, range);
	}
};

Spreadsheet.areEqual = function(x, y) {
	for (let p in y) {
		if (!y.hasOwnProperty(p)) continue;
		if (typeof(y[p]) !== typeof(x[p])) return false;
		if ((y[p] === null) !== (x[p] === null)) return false;
		switch (typeof(y[p])) {
			case 'undefined':
				if (typeof(x[p]) !== 'undefined') return false;
				break;
			case 'object':
				if (y[p] !== null && x[p] !== null &&
					(y[p].constructor.toString() !== x[p].constructor.toString()
					|| !Spreadsheet.areEqual(y[p], x[p]))) return false;
				break;
			case 'function':
				if (p !== 'equals' && y[p].toString() !== x[p].toString()) return false;
				break;
			default:
				if (y[p] !== x[p]) return false;
		}
	}
	return true;
};

