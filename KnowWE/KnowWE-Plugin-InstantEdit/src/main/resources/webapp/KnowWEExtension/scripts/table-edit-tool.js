KNOWWE.plugin.tableEditTool = function() {
	
	var originalWikiText = null;
	var spreadsheet = null;
	
	function createRootID(id) {
		return "tableEdit" + id;
	}
	
	function createButton(image, ext) {
		return "<a id='"+image+"' class='action'>" +
				"<img src='KnowWEExtension/images/table/"+image+"."+ext+"'></a>";
	}
	
    return {
    	
	    generateHTML : function(id) {
	    	originalWikiText = _IE.getWikiText(id);
	    	return "<div id='"+createRootID(id)+"' style='position: relative;'></div>" +
	    			_IE.getSaveCancelDeleteButtons(id, new Array(
	    	    			createButton("table_insert_col_before", "gif"),
	    	    			createButton("table_insert_col_after", "gif"),
	    	    			createButton("table_delete_col", "gif"),
	    	    			createButton("table_insert_row_before", "gif"),
	    	    			createButton("table_insert_row_after", "gif"),
	    	    			createButton("table_delete_row", "gif"),
	    	    			createButton("toggle_header", "png")
	    					));
	    },
	    
	    postProcessHTML : function(id) {
	    	spreadsheet = new Spreadsheet(createRootID(id));
	    	spreadsheet.setWikiMarkup(originalWikiText);
	    	originalWikiText = spreadsheet.getWikiMarkup();

	    	var root = spreadsheet.element.parent();
	    	root.find("#table_insert_row_before").click(function(event) {
	    		if (spreadsheet.selected) spreadsheet.addRow(spreadsheet.selected.row);
	    		event.preventDefault();
	    	}); 
	    	root.find("#table_insert_row_after").click(function(event) {
	    		if (spreadsheet.selected) spreadsheet.addRow(spreadsheet.selected.row+1);
	    		event.preventDefault();
	    	});
	    	root.find("#table_delete_row").click(function(event) {
	    		if (spreadsheet.selected) spreadsheet.removeRow(spreadsheet.selected.row);
	    		event.preventDefault();
	    	});
	    	
	    	root.find("#table_insert_col_before").click(function(event) {
	    		if (spreadsheet.selected) spreadsheet.addCol(spreadsheet.selected.col);
	    		event.preventDefault();
	    	});
	    	root.find("#table_insert_col_after").click(function(event) {
	    		if (spreadsheet.selected) spreadsheet.addCol(spreadsheet.selected.col+1);
	    		event.preventDefault();
	    	});
	    	root.find("#table_delete_col").click(function(event) {
	    		if (spreadsheet.selected) spreadsheet.removeCol(spreadsheet.selected.col);
	    		event.preventDefault();
	    	});

	    	root.find("#toggle_header").click(function(event) {
	    		if (!spreadsheet.selected) return;
	    		var header = spreadsheet.getCell(spreadsheet.selected.row, spreadsheet.selected.col).hasClass("header");
	    		spreadsheet.forEachSelected(function(cell, row, col) {
	    			spreadsheet.setHeader(row, col, !header);
	    		});
	    		event.preventDefault();
	    	});
	    },
	    
	    unloadCondition : function(id) {
			return originalWikiText == spreadsheet.getWikiMarkup();
	    },
	    
	    generateWikiText : function(id) {
	    	return spreadsheet.getWikiMarkup();
	    }
    }
}();


function SpreadsheetModel(wikiText) {
	this.width = 1;
	this.height = 1;
	this.cells = new Array();

	if (wikiText) {
		// normalize text returns
		wikiText = ("\n"+wikiText+"\n").replace(/[\n\r]+/g, "\n").replace(/\~\|/g, "&#124;");
		var lines = wikiText.match(/\n\|[^\n]*/g);
		var row = 0;
		for (var i = 0; i<lines.length; i++) {
			var line = lines [i];
			line = line.replace(/\n/g, "");
			if (line.match(/^\s*$/g)) continue;
			var cells = line.match(/\|\|?[^\|]*/g);
			var col = 0;
			for (var j = 0; j<cells.length; j++) {
				var cell = cells[j];
				var text = "";
				if (cell.charAt(1) == '|') {
					text = jq$.trim(cell.substr(2).replace(/\\\\/g, "\n"));
					this.setCell(row, col, text, true);
				}
				else { 
					text = jq$.trim(cell.substr(1).replace(/\\\\/g, "\n"));
					this.setCell(row, col, text, false);
				}
				col++;
			}
			row++;
		}
	}
}

SpreadsheetModel.prototype.setCell = function(row, col, text, isHeader) {
	this.ensureSize(row+1, col+1);
	var key = row + "_"+col;
	this.cells[key] = {row: row, col: col, text: text, isHeader: isHeader};
}

SpreadsheetModel.prototype.ensureSize = function(rowCount, colCount) {
	this.height = Math.max(this.height, rowCount);
	this.width = Math.max(this.width, colCount);
}

SpreadsheetModel.prototype.getCellText = function(row, col) {
	var key = row + "_"+col;
	var data = this.cells[key];
	return data ? data.text : "";
}

SpreadsheetModel.prototype.isHeader = function(row, col) {
	var key = row + "_"+col;
	var data = this.cells[key];
	// if not specified, inherit header state from left cell
	if (!data && col > 0) return this.isHeader(row, col-1);
	return data ? data.isHeader : false;
}

SpreadsheetModel.prototype.toWikiMarkup = function() {
	var wikiText = "";
	for (var row = 0; row < this.height; row++) {
		for (var col = 0; col < this.width; col++) {
			wikiText += this.isHeader(row, col) ? "|| " : "|  ";
			wikiText += this.getCellText(row, col).replace(/\|/g, "~|").replace(/\r?\n\r?/g, "\\\\");
			wikiText += "\t";
		}
		wikiText += "\n";
	}
	return wikiText;

}

function Spreadsheet(elementID) {
	this.elementID = elementID;
	this.element = jq$("#"+elementID);
	
	this.createTable();
	this.selectCell(0,0);
}

Spreadsheet.prototype.setWikiMarkup = function(wikiText) {
	this.setModel(new SpreadsheetModel(wikiText));
}

Spreadsheet.prototype.setModel = function(model) {
	this.element.html("");
	this.createTable(model);
	this.selectCell(0,0);
}

Spreadsheet.prototype.getWikiMarkup = function() {
	return this.getModel().toWikiMarkup();
}

Spreadsheet.prototype.getModel = function() {
	var model = new SpreadsheetModel();
	this.element.find("tr").each(function(rowIndex) {
		jq$(this).find("td").each(function(colIndex) {
			var isHeader = jq$(this).hasClass("header");
			var text = jq$(this).find("div > a").text();
			model.setCell(rowIndex, colIndex, text, isHeader);
		});
	});
	return model;
}

Spreadsheet.prototype.createTable = function(model) {
	if (!model) model = new SpreadsheetModel();
	this.size = {rows: model.height, cols: model.width};
	this.selected = null;
	this.selectedRange = null;
	this.copied = null;
	
	var html = "";
	html += "<table class='wikitable spreadsheet'>\n";
	html += "</tr>\n";
	for (var row=0; row<this.size.rows; row++) {
		html += "<tr>\n";
		for (var col=0; col<this.size.cols; col++) {
			var isHeader = model.isHeader(row, col);
			var text = model.getCellText(row, col);
			html += "<";
			html += isHeader ? "td class=header" : "td";
			html += " id='"+this.getCellID(row, col)+"'><div><a href='#'>";
			html += text ? text : " ";
			html += "</a></div>";
		}
		html += "</tr>\n";
	}
	html += "</table>";
	this.element.append(html);
	
	for (var row=0; row<this.size.rows; row++) {
		for (var col=0; col<this.size.cols; col++) {
			var cell = this.getCell(row, col);
			cell.data("cellInfo", {spreadsheet: this, row: row, col: col});
		}
	}
	
	// add click event to select data
	this.element.find(" tr > td").click(function(event) {
		var cell = jq$(this);
		var data = cell.data("cellInfo");
		data.spreadsheet.selectCell(data.row, data.col, event.shiftKey || event.metaKey);
		event.preventDefault();
	});

	// add keyboard event to select/edit data
	this.element.find(" tr > td > div > a").keydown(function(event) {
		var cell = jq$(this).parents(" tr > td");
		var data = cell.data("cellInfo");
		var multi = event.shiftKey;
		var command = event.ctrlKey || event.metaKey;
		var handled = data.spreadsheet.handleKeyDown(cell, event.which, multi, command);
		if (handled) event.preventDefault();
	});
	
	this.element.find(" tr > td > div > a").blur(function(event) {
		var cell = jq$(this).parents(" tr > *");
		var data = cell.data("cellInfo");
		cell.removeClass("selected");
	});
};

/**
 * Handles key down event end returns boolean if the key was handled or not
 */
Spreadsheet.prototype.handleKeyDown = function(cell, keyCode, multiSelect, command) {
	var row = this.selected.row;
	var col = this.selected.col;
	// Backspace + ENTF
	if (keyCode == 8 || keyCode == 46) {
		this.clearSelectedCells();
	}
	// return
	else if (keyCode == 13) {
		this.editCell(row, col);
	}
	// left
	else if (keyCode == 37) {
		var toCol = command ? 0 : col - 1;
		if (toCol >= 0) this.selectCell(row, toCol, multiSelect);
	}
	// up
	else if (keyCode == 38) {
		var toRow = command ? 0 : row - 1;
		if (toRow >= 0) this.selectCell(toRow, col, multiSelect);
	}
	// right
	else if (keyCode == 39) {
		var toCol = command ? this.size.cols-1 : col + 1;
		if (toCol <= this.size.cols-1) this.selectCell(row, toCol, multiSelect);
	}
	// down
	else if (keyCode == 40) {
		var toRow = command ? this.size.rows-1 : row + 1;
		if (toRow <= this.size.rows-1) this.selectCell(toRow, col, multiSelect);
	}
	// tab
	else if (keyCode == 9 && !multiSelect) {
		if (col < this.size.cols-1) {
			this.selectCell(row, col + 1);
		}
		else if (row < this.size.rows-1) {
			this.selectCell(row + 1, 0);
		}
	}
	// backward tab
	else if (keyCode == 9 && multiSelect) {
		if (col > 0) {
			this.selectCell(row, col - 1);
		}
		else if (row > 0) {
			this.selectCell(row - 1, this.size.cols-1);
		}
	}
	// cut + copy
	else if ((keyCode == 88 || keyCode == 67) && command) {
		this.copySelectedCells(keyCode == 88);
	}
	// paste
	else if (keyCode == 86 && command) {
		this.pasteCopiedCells();
	}
	// save
	else if (keyCode == 83 && command) {
		alert(this.getWikiMarkup());
	}
	// ESC
	else if (keyCode == 27) {
		this.uncopyCopiedCells();
	}	
	// normal typing
	else if (((keyCode >= 32 && keyCode <= 90) || (keyCode >= 186 && keyCode <= 222))  && !command) {
		this.editCell(row,col);
		return false; // return false to start edit
	}
	// ignore all others
	else { return false; }
	return true;
}

Spreadsheet.prototype.editCell = function(row, col) {
	this.uncopyCopiedCells();
	this.selectCell(row,col);
	var contentElement = this.getSelectedCell().find("div > a");
	var pos = contentElement.parent().position();
	var html = "";
	html += "<div class='cellEdit' style='";
	html += "left:"+(pos.left-3)+"px;top:"+(pos.top-3)+"px;";
	html += "'>";
	html += "<textarea style='";
	html += "width:"+(contentElement.width()+16)+"px;height:"+(contentElement.height()+6)+"px;";
	html += "'>";
	html += this.getCellText(row, col);
	html += "</textarea>";
	html += "</div>";
	this.element.prepend(html);
	var editDiv = this.element.children("div");
	var editArea = editDiv.children("textarea");
	editArea.focus();
	editArea.select();
	var spreadsheet = this;
	var closing = false; // flag to avoid multiple closing (detach forces focusout-event)
	editArea.keydown(function(event) {
		if (closing) return;
		var keyCode = event.which;
		if ((keyCode == 13 || keyCode == 9) && !event.altKey && !event.shiftKey) {
			spreadsheet.setCellText(row, col, editArea.val());
		}
		// ESC
		else if (keyCode == 27) {
		}
		else {return;}
		closing = true;
		event.preventDefault();
		editDiv.detach();
		spreadsheet.selectCell(row,col);
		// on tab or return, select next cell as well
		if (keyCode == 9) {
			spreadsheet.handleKeyDown(spreadsheet.getSelectedCell(), keyCode, event.shiftKey, false);
		}
		if (keyCode == 13) {
			spreadsheet.handleKeyDown(spreadsheet.getSelectedCell(), 40, false, false);
		}
	});
	editArea.focusout(function(event) {
		if (closing) return;
		spreadsheet.setCellText(row, col, editArea.val());
		closing = true;
		editDiv.detach();
		spreadsheet.selectCell(row,col);			
	});
}

Spreadsheet.prototype.setCellText = function(row, col, text) {
	var elem = this.getCell(row, col).find("div > a");
	if (!text || text.match(/^\s+$/g)) {
		elem.html(" "); // avoid cell collapsing
	}
	else {
		elem.text(text);
	}
}

Spreadsheet.prototype.getCellText = function(row, col) {
	var elem = this.getCell(row, col).find("div > a");
	return elem.text();
}

/**
 * Selects the specified cell. Use this method without arguments
 * to deselect all cells.
 */
Spreadsheet.prototype.selectCell = function(row, col, multiSelect) {
	var cell = this.getSelectedCell();
	if (cell) {
		cell.removeClass("selected");
	}
	if (row == undefined && col == undefined) {
		this.selected = null;
		if (cell) cell.find("div > a").blur();
		return;
	}
	// deselect multi-selected cells
	this.forEachSelected(function(cell) {cell.removeClass("multiSelected");});
	// if multi-select is requested, select from previous selected cell to requested cell
	if (multiSelect) {
		if (!this.selectedRange) {
			var toRow = this.selected.row;
			var toCol = this.selected.col;
			this.selectedRange = { toRow: toRow, toCol: toCol };
		}
	}
	else {
		this.selectedRange = null;
	}
	// do normal selection as well
	this.selected = {row: row, col: col};
	cell = this.getSelectedCell();
	cell.addClass("selected");
	this.forEachSelected(function(cell) {cell.addClass("multiSelected");});
	cell.find("div > a").focus();
}

Spreadsheet.prototype.forEachSelected = function(fun) {
	if (!this.selected) return;
	if (!this.selectedRange) {
		var row = this.selected.row;
		var col = this.selected.col;
		fun(this.getCell(row,col), row, col);
		return;
	}
	var r1 = this.selected.row;
	var c1 = this.selected.col;
	var r2 = this.selectedRange.toRow;
	var c2 = this.selectedRange.toCol;
	for (var row = Math.min(r1,r2); row <= Math.max(r1,r2); row++) {
		for (var col = Math.min(c1,c2); col <= Math.max(c1,c2); col++) {
			fun(this.getCell(row,col), row, col);
		}
	}
}

Spreadsheet.prototype.clearSelectedCells = function() {
	var sheet = this;
	this.forEachSelected(function(cell, row, col) {sheet.setCellText(row, col, "");});
}

Spreadsheet.prototype.pasteCopiedCells = function() {
	if (!this.copied) return;
	if (!this.selected) return;
	if (!this.selectedRange) this.selectCell(this.selected.row,this.selected.col,true);
	var r1 = this.selected.row;
	var c1 = this.selected.col;
	var r2 = this.selectedRange.toRow;
	var c2 = this.selectedRange.toCol;	
	var destRow = Math.min(r1,r2);
	var destCol = Math.min(c1,c2);
	var destHeight = Math.abs(r1-r2)+1;
	var destWidth = Math.abs(c1-c2)+1;
	var srcRow = this.copied.row;
	var srcCol = this.copied.col;
	var srcHeight = this.copied.toRow - srcRow + 1;
	var srcWidth = this.copied.toCol - srcCol + 1;
	if (destHeight == 1 && destWidth == 1) {
		destHeight = srcHeight;
		destWidth = srcWidth;
		this.selectCell(Math.min(destRow + destHeight - 1, this.size.rows), Math.min(destCol + destWidth - 1, this.size.cols));
		this.selectCell(destRow, destCol, true);
	}
	else if (destHeight != srcHeight || destWidth != srcWidth) {
		// beep();
		return;
	}
	
	// be careful if ranges overlapping
	var data = new Array();
	var index = 0;
	for (var row = srcRow; row < srcRow + srcHeight; row ++) {
		for (var col = srcCol; col < srcCol + srcWidth; col ++) {
			var text = this.getCellText(row, col);
			data[index++] = text;
			if (this.copied.doCut) this.setCellText(row, col, "");
		}
	}
	index = 0;
	for (var row = destRow; row < destRow + destHeight; row ++) {
		for (var col = destCol; col < destCol + destWidth; col ++) {
			var text = data[index++];
			if (row >= this.size.rows) continue;
			if (col >= this.size.cols) continue;
			this.setCellText(row, col, text);
		}
	}
	
	if (this.copied.doCut) {
		this.uncopyCopiedCells();
	}
}

Spreadsheet.prototype.copySelectedCells = function(doCut) {
	if (!this.selected) return;
	if (!this.selectedRange) this.selectCell(this.selected.row,this.selected.col,true);
	this.uncopyCopiedCells();
	var r1 = this.selected.row;
	var c1 = this.selected.col;
	var r2 = this.selectedRange.toRow;
	var c2 = this.selectedRange.toCol;
	this.copied = {
		row: Math.min(r1,r2), col: Math.min(c1,c2), 
		toRow: Math.max(r1,r2), toCol: Math.max(c1,c2),
		doCut: doCut };
	this.forEachCopied(function(cell) {cell.addClass("copied");});
}

Spreadsheet.prototype.uncopyCopiedCells = function() {
	this.forEachCopied(function(cell) {cell.removeClass("copied");});
	this.copied = null;
}

Spreadsheet.prototype.forEachCopied = function(fun) {
	if (!this.copied) return;
	for (var row = this.copied.row; row <= this.copied.toRow; row++) {
		for (var col = this.copied.col; col <= this.copied.toCol; col++) {
			fun(this.getCell(row,col), row, col);
		}
	}
}


Spreadsheet.prototype.getSelectedCell = function() {
	if (!this.selected) return null;
	return this.getCell(this.selected.row, this.selected.col);
}

Spreadsheet.prototype.getCell = function(row, col) {
	return this.element.find("#"+this.getCellID(row, col));
}

Spreadsheet.prototype.getCellID = function(row, col) {
	return "cell_"+row+"_"+col;
}

Spreadsheet.prototype.addRow = function(row) {
	var sr = this.selected ? this.selected.row : 0;
	var sc = this.selected ? this.selected.col : 0;
	if (sr >= row) sr++;
	// copy model, adding empty row before "row"
	var srcModel = this.getModel();
	var destModel = new SpreadsheetModel();
	var destRow = 0;
	for (var srcRow = 0; srcRow < srcModel.height; srcRow++) {
		if (srcRow == row) destRow++; // add line
		var destCol = 0;
		for (var srcCol = 0; srcCol < srcModel.width; srcCol++) {
			var isHeader = srcModel.isHeader(srcRow, srcCol);
			var text = srcModel.getCellText(srcRow, srcCol);
			destModel.setCell(destRow, destCol, text, isHeader);
			destCol++;
		}
		destRow++;
	}
	// if we add last row, make sure that size increases
	destModel.ensureSize(srcModel.height + 1, srcModel.width);
	// set new Model and restore selection
	this.setModel(destModel);
	this.selectCell(sr, sc);
}

Spreadsheet.prototype.removeRow = function(row) {
	if (this.size.rows <= 1) return;
	var sr = this.selected ? Math.min(this.selected.row, this.size.rows-2) : 0;
	var sc = this.selected ? this.selected.col : 0;
	if (sr > row) sr--;
	// copy model, removing "row"
	var srcModel = this.getModel();
	var destModel = new SpreadsheetModel();
	var destRow = 0;
	for (var srcRow = 0; srcRow < srcModel.height; srcRow++) {
		if (srcRow == row) continue; // ignore line to delete
		var destCol = 0;
		for (var srcCol = 0; srcCol < srcModel.width; srcCol++) {
			var isHeader = srcModel.isHeader(srcRow, srcCol);
			var text = srcModel.getCellText(srcRow, srcCol);
			destModel.setCell(destRow, destCol, text, isHeader);
			destCol++;
		}
		destRow++;
	}
	// set new Model and restore selection
	this.setModel(destModel);
	this.selectCell(sr, sc);
}

Spreadsheet.prototype.addCol = function(col) {
	var sr = this.selected ? this.selected.row : 0;
	var sc = this.selected ? this.selected.col : 0;
	if (sc >= col) sc++;
	// copy model, adding empty row before "row"
	var srcModel = this.getModel();
	var destModel = new SpreadsheetModel();
	var destRow = 0;
	for (var srcRow = 0; srcRow < srcModel.height; srcRow++) {
		var destCol = 0;
		for (var srcCol = 0; srcCol < srcModel.width; srcCol++) {
			if (srcCol == col) destCol++; // add column
			var isHeader = srcModel.isHeader(srcRow, srcCol);
			var text = srcModel.getCellText(srcRow, srcCol);
			destModel.setCell(destRow, destCol, text, isHeader);
			destCol++;
		}
		destRow++;
	}
	// if we add last column, make sure that size increases
	destModel.ensureSize(srcModel.height, srcModel.width + 1);
	// set new Model and restore selection
	this.setModel(destModel);
	this.selectCell(sr, sc);
}

Spreadsheet.prototype.removeCol = function(col) {
	if (this.size.cols <= 1) return;
	var sr = this.selected ? this.selected.row : 0;
	var sc = this.selected ? Math.min(this.selected.col, this.size.cols-2) : 0;
	if (sc > col) sc--;
	// copy model, removing "row"
	var srcModel = this.getModel();
	var destModel = new SpreadsheetModel();
	var destRow = 0;
	for (var srcRow = 0; srcRow < srcModel.height; srcRow++) {
		var destCol = 0;
		for (var srcCol = 0; srcCol < srcModel.width; srcCol++) {
			if (srcCol == col) continue; // ignore col to delete
			var isHeader = srcModel.isHeader(srcRow, srcCol);
			var text = srcModel.getCellText(srcRow, srcCol);
			destModel.setCell(destRow, destCol, text, isHeader);
			destCol++;
		}
		destRow++;
	}
	// set new Model and restore selection
	this.setModel(destModel);
	this.selectCell(sr, sc);
}

Spreadsheet.prototype.setHeader = function(row, col, isHeader) {
	var sr = this.selected ? this.selected.row : 0;
	var sc = this.selected ? this.selected.col : 0;
	var tr = this.selectedRange ? this.selectedRange.toRow : sr;
	var tc = this.selectedRange ? this.selectedRange.toCol : sc;
	var cell = this.getCell(row, col);
	if (isHeader) {
		cell.addClass("header");
	}
	else {
		cell.removeClass("header");
	}
	this.selectCell(tr, tc, false);
	this.selectCell(sr, sc, true);
}

