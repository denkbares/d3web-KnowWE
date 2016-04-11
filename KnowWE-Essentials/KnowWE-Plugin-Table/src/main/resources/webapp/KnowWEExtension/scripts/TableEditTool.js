/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    var KNOWWE = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.table == "undefined" || !KNOWWE.table) {
    KNOWWE.table = {};
}
 

/**
 * Utility methods
 */
KNOWWE.table.edit = {
		
	/**
	 * Determines the position of any cell-element (i.e. any child of a td or th) in the table.
	 * returns an object with 2 members, 'row' and 'column'.
	 */
	getPositionInTable : function(el) {
		var cell;
		if (el.tagName == 'TH' || el.tagName == 'TD'){
			cell = el;
		} else {
			cell = KNOWWE.table.edit.getParent(el, ['TH', 'TD']);
		}
		
		var tr = cell.parentNode;
		var result = {};
		
		for (var x = 0; x < tr.childNodes.length; x++){
			if (tr.childNodes[x] == cell) {
				result.column = x;
			}
		}
		
		var table = tr.parentNode;
		for (var y = 0; y < table.childNodes.length; y++){
			if (table.childNodes[y] == tr){
				result.row = y;
			}
		}
		
		return result;
		
	},
	
	/**
	 * finds the first parent node of el that has one of the tagnames supplied in tags
	 */
	getParent : function(el, tags) {
		while (true) {
			
			for (var i = 0; i < tags.length; i++){
				if (el.tagName == tags[i]) {
					return el;
				}
			}
			
			el = el.parentNode;
			if (el.tagName == 'BODY') {
				return null;
			}
		}
		return el;
	},
	
	/**
	 * returns the default actions for changing the table structure for the cell at row / col.
	 * 
	 */
	getDefaultActions : function(el, rowNo, colNo, editor) {
		var actions = [];
		
		if (rowNo == 0){
			actions.push({name: 'Delete column', f: (function(){editor.delCol(el)}) });
			actions.push({name: 'Add column left', f: (function(){editor.addCol(el, 0)})});
			actions.push({name: 'Add column right', f: (function(){editor.addCol(el, 1)})});
		}
		if (colNo == 0){
			actions.push({name: 'Delete row', f: (function(){editor.delRow(el)})});
			actions.push({name: 'Add row above', f: (function(){editor.addRow(el, 0)})});
			actions.push({name: 'Add row below', f: (function(){editor.addRow(el, 1)})});
		}
		
		return actions;
	}

}


KNOWWE.table.edit.Editor = (function(editProvider) {
 
	var wikicode;
	var areaId;

	function createTableID(id) {
		return "defaultEditTable" + id;
	}
	
	/**
	 * Displays a action menu, when clicking the menu dropdown
	 */
	function showActionMenu(event, el){
		
		var divContext = document.createElement("div");
		divContext.setAttribute("class", "actionMenu");
		
		var close = document.createElement('a');
		close.appendChild(document.createTextNode('(x)'));
		close.setAttribute('class', 'close');
		close.onclick = function() {document.body.removeChild(divContext)};
		divContext.appendChild(close);
		
		// document.body.scrollTop does not work in IE
		var scrollTop = document.body.scrollTop ? document.body.scrollTop :
			document.documentElement.scrollTop;
		var scrollLeft = document.body.scrollLeft ? document.body.scrollLeft :
			document.documentElement.scrollLeft;

		var left = event.clientX + scrollLeft;
		divContext.style.left = left + 'px';
		var top = event.clientY + scrollTop;
		divContext.style.top = top + 'px';
		
		var pos = KNOWWE.table.edit.getPositionInTable(el);
		var actions = editProvider.getActions(el, pos.row, pos.column, editor);
		
		var actionList = document.createElement("ul");
		divContext.appendChild(actionList);
		for (var i = 0; i < actions.length; i++){
			var item = document.createElement('li');
			item.appendChild(document.createTextNode(actions[i].name));
			item.onclick = (function(index){return (function(){actions[index].f();document.body.removeChild(divContext);})})(i);
			actionList.appendChild(item);
		}
		
		document.body.appendChild(divContext);
		
	}
	
	/**
	 * removes all  menu drop downs from the cells
	 */
	function removeMenuHandlers(table) {
		var handles = table.getElements('.menuSymbol');
		
		for (var i = 0; i < handles.length; i++){
			handles[i].parentNode.removeChild(handles[i]);
		}
	}

	/**
	 * resets the menu dropdowns after modifying the table structure
	 */
	function resetMenuHandlers(table) {
		removeMenuHandlers(table);
		addMenuHandlers(table);
	}
		
	/**
	 * adds the menu dropdowns to alls cells in first row / col
	 */
	function addMenuHandlers(table) {
		var rows = $(table).getElements('tr');
		var result = [];
		
		if (rows.length == 0)
			return;
		
		//all row headers
		for (var colNo = 0; colNo < rows[0].childNodes.length; colNo++) {
			result.push(rows[0].childNodes[colNo]);
		}

		//all col headers
		for (var rowNo = 1; rowNo < rows.length; rowNo++) {
			result.push(rows[rowNo].firstChild);
		}

		for (var i = 0; i < result.length; i++){
			var span = document.createElement("span");
			
			span.setAttribute('class','menuSymbol');
			span.onclick = (function(event){
				showActionMenu(event, this);
			}).bind(result[i]);
			
			result[i].appendChild(span);
			
		}
	}
	

	
	/**
	 * Takes a line of wiki markup and returns a tr with all the cells
	 */
	function parseLine(wikiLine, row) {
		
		var regex = /(?:\|(?!\|)([^|\r\n]*))|(?:\|\|([^|\r\n]*))/g;
		var result = document.createElement('tr');
		var col = 0;
		var cellType, cellValue;
		var match;
		
		while (match = regex.exec(wikiLine)) {
				
			if (match[1]) {
				cellValue = match[1];
				cellType = 'td';
			}
			else if (match[2]){
				cellValue = match[2];
				cellType = 'th';
			} else { //happens, if there is no space between cells, eg 3 cells like|||
				cellType = 'td';
				cellValue = '';
			}
			//
			result.appendChild(generateCell(cellType, row, col, cellValue));	
			col++;
		}
		
		return result;
		
	}
	
	

	/**
	 * Creates a new cell, when appending a row or column. 
	 * th is created for first row, td otherwise.
	 * calls getDefaultValue(...) of the editprovider to get a value for the display component.
	 */
	function createNewCell(row, col) {
		var value = editProvider.getDefaultValue(row, col);
		return generateCell(row == 0 ? 'th' : 'td' , row, col, value)
	}

	/**
	 * creates a cell a the specified position. It creates the display component by calling
	 * the getDisplayComponent(...) method in the editprovider. That version must also take 
	 * care about setting the correct initial value.
	 * type: 'th' or 'td'
	 * row / col: index in the table
	 * value: the value to display in the cell. Can be undefined, if no value should be set by default.
	 * 
	 */
	function generateCell(type, row, col, value) {
		var cell = document.createElement(type);
		//TODO convert to appendchild
		cell.innerHTML = editProvider.getDisplayComponent(row, col, value);
		return cell;
	}
	
	/**
	 * removes the surrounding defaultmarkup frame
	 */
	function removeDMFrame(editArea){
		var dmFrame = editArea.parentNode;
		
		while (dmFrame.className.indexOf('defaultMarkupFrame') == -1) {
			dmFrame = dmFrame.parentNode;
		}
		
		var dmParent = dmFrame.parentNode;
		dmParent.insertBefore(editArea, dmFrame);
		dmParent.removeChild(dmFrame);
		
	}
	

	var editor = {
    	
    	/**
    	 * inserts a new column to the left (offset = 0) or right (offSet = 1)
    	 * of the specified element el. 
    	 */
    	addCol : function(el, offSet) {
    		
    		var colNo = KNOWWE.table.edit.getPositionInTable(el).column;
    		colNo += offSet;
    		
    		var table = KNOWWE.table.edit.getParent(el, ['TABLE']);
    		var trs = table.getElementsByTagName("tr");

    		for (var rowNo = 0; rowNo < trs.length; rowNo++) {
    					
    			var newCell = createNewCell(rowNo, colNo);

    			if (colNo >= trs[rowNo].childNodes.length) {
    				trs[rowNo].appendChild(newCell);
    			} else {
    				trs[rowNo].insertBefore(newCell, trs[rowNo].childNodes[colNo]);
    			}
    		}

    		resetMenuHandlers(table);
			
			editProvider.columnAdded(colNo);
    	},
    	
    	/**
    	 * Deletes the column, that contains element el.
    	 */
    	delCol : function(el) {
    		
    		var table = KNOWWE.table.edit.getParent(el, ['TABLE']);
    		var trs = table.getElementsByTagName("tr");
    		
    		var nr = KNOWWE.table.edit.getPositionInTable(el).column;
    		for (var i = 0; i < trs.length; i++) {
    			if (nr < trs[i].childNodes.length) { 
    				trs[i].removeChild(trs[i].childNodes[nr]);
    			}
    		}
    		
    		//restore handlers, if first column has been removed
    		if (nr == 0)
    			KNOWWE.table.edit.resetMenuHandlers(table);
    	},

    	
    	/**
    	 * insertes a new row above (offset = 0) or below(offSet = 1)
    	 * of the specified element el. 
    	 */
    	addRow : function(el, offSet) {
    		var rowNo = KNOWWE.table.edit.getPositionInTable(el).row;
    		rowNo += offSet;

    		var table = KNOWWE.table.edit.getParent(el, ['TABLE']);	
    		var trs = table.getElementsByTagName("tr");
    		
    		var newtr = document.createElement("tr");
    		
    		//TODO Was, wenn die Tabelle nicht rechteckig ist? 
    		for (var colNo = 0; colNo < trs[0].childNodes.length; colNo++) {
    			var newtd = createNewCell(rowNo, colNo);
    			newtr.appendChild(newtd);
    		}
    		
    		if (rowNo < trs.length) {
    			trs[0].parentNode.insertBefore(newtr, trs[rowNo]);
    		} else {
    			trs[0].parentNode.appendChild(newtr);
    		}

    		resetMenuHandlers(table);
    		
    		editProvider.rowAdded(rowNo);

    		
    	},
    	

    	/**
    	 * Deletes the row, that contains element el
    	 * @param el
    	 */
    	delRow : function (el) {
    		var tr = KNOWWE.table.edit.getParent(el, ['TR']);
    		var first = KNOWWE.table.edit.getPositionInTable(el).row == 0;
    		
    		var table = tr.parentNode;
    		table.removeChild(tr);
    		
    		if (first) {
    			resetMenuHandlers(table);
    		}
    	},
    	
    	
    	/////////InstantEdit Interface
    	/**
    	 * Parses the wikimarkup and creates the editable table
    	 */
	    generateHTML : function(id) {
			editProvider.prepare(id);
			
			wikicode = _EC.getWikiText(id);
			areaId = id;
			
			var result = document.createElement('div')
			
//			var div = document.createElement('div')
//			div.setAttribute('class', 'defaultEditTool');
			
			var table = document.createElement('table');
			table.setAttribute('class','wikitable knowwetable');
			table.setAttribute('id', createTableID(id));

			var lines = wikicode.split(/\n/);
			for (var i = 0; i < lines.length; i++) {
				if (lines[i] =='') {
					continue;
				}
				
				table.appendChild(parseLine(lines[i], i));
				
			}
			
//			div.appendChild(table);
 			result.appendChild(table);
			
//			var buttons = document.createElement('div');
//			buttons.innerHTML = KNOWWE.plugin.instantEdit.getSaveCancelDeleteButtons(id); 
//			result.appendChild(buttons.firstChild);
//			
			return result.innerHTML;
	    },
	    
		/**
		 * Binds event listeners 
		 *
		 */
	    postProcessHTML : function(id) {
			var editarea = $(id);
			
			removeDMFrame(editarea);
			
			addMenuHandlers(editarea.getElementsByTagName("table")[0]);
			
			editProvider.postProcess(id);
	    },
	    
	    generateButtons : function(id) {
	    	return _EC.elements.getSaveCancelDeleteButtons(id)
	    },
	    
		/**
		* returns true, if no changes have been made
		**/
	    unloadCondition : function(id) {
	    	var wikiTable = KNOWWE.table.edit.createWikiMarkup(id);
			return wikicode == wikiTable;
	    },
	    
		/**
		Erstellt aus dem HTML Code das WikiMarkup
		**/
	    generateWikiText : function(id) {
	    	var wikiTable = '';
	    	
	    	var rows = document.getElementById(createTableID(id)).getElementsByTagName('tr');
	    	var cols;

	    	for (var rowNo = 0; rowNo < rows.length; rowNo++) {
	    		cols = rows[rowNo].childNodes;
	    		for (var colNo = 0; colNo < cols.length; colNo++) {
	    			wikiTable += '|';
	    			if (cols[colNo].tagName == 'TH')
	    				wikiTable += '|';
	    			
	    			var value = editProvider.getValue(cols[colNo].childNodes[0], rowNo, colNo);
	    			
	    			if (value == undefined || value == '') value = ' '; //separate cells
	    			
	    			wikiTable += value;
	    		}
	    		wikiTable += '\n';
	    	}
	    
	    	return wikiTable;
	    }
    };	
	
	return editor;
});


/**
 * This is the default edit provider.
 * It renders every table cell in a text field.
 * 
 */
KNOWWE.table.edit.defaultProvider = {

	/**
	 * Called once at start of edit session. The table is not yet appended to the dom
	 * id: id of the DOM Element that will contain the table
	 */
	prepare : function(id){
	},
	
	/**
	 * this method must return an HTML-String, that renders a component at the specified location
	 * (row, col) and displays the supplied value. 
	 * for non-editable values it can return a <span> or something similar.
	 * row / col: index in the table
	 * value: value to display, of undefined, if no value should be displayed
	 * 
	 */
	getDisplayComponent : function(row, col, value){
		var result = '<input type="text" size="10"';
		if (value != undefined){
			result += 'value="' + value + '"';
		}
		result += '>';
		
		return result;
	},
	
	/**
	 * This method returns the default value for newly created cells, 
	 * after inserting a row or column. This method can also return 'undefined', 
	 * e.g. to leave a textual input empty 
	 */
	getDefaultValue : function(row, col) {
		return undefined;
	},
	
	/**
	 * This method returns the value that has been entered at the supplied position in the table, as a string. 
	 * The returned string representation is written into the article.
	 * As a convenience, the first child of the cell at that position is supplied
	 * row/col: position in the table of the cell
	 * el: the first child of the td/th at (row, col)
	 */
	getValue : function(el, row, col) {
		return el.value;
	},

	/**
	 * Returns an array of actions, that are available for the cell at the specified coordinates.
	 * 
	 */
	getActions : function(el, ro, col, editor) {
		return KNOWWE.table.edit.getDefaultActions(el, ro, col, editor);
	},
	
	/**
	 * Called once after the table has been inserted into the DOM
	 */
	postProcess : function (id) {
		
	},
	
	/**
	 * Called after inserting a new column into the table
	 * colNr : index of new column
	 */
	columnAdded : function(colNr){
		
	},
	
	/**
	 * Called after inserting a new row into the table
	 * rowNr : index of new row
	 */
	rowAdded : function(rowNr){
		
	}
	
}
