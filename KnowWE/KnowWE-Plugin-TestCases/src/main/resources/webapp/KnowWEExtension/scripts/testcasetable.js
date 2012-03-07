var Testcase = {};

Testcase.testcaseSkipped = "testcaseSkipped";
Testcase.testcaseExecuted = "testcaseExecuted";

/**
 * returns the tablelines of the currently edited table
 */
Testcase.getTableLines = function(table) {
	var lines = table.getElement('tbody').childNodes;
	var tableLines = [];
	for (var i = 0 ; i < lines.length; i++) {
		if (lines[i].nodeName === 'TR') {
			tableLines.push(lines[i]);
		}
	}
	return tableLines;
}


Testcase.findParentWikiTable = function(element) {
	while (true) {
		if (element.className == "wikitable knowwetable") {
			break;
		}
		element = element.parentNode;
		if (element.tagName == 'BODY') {
			return null;
		}
	}
	return element;
}


Testcase.findLineOfElement = function(element) {
	var e = $(element);
	while (e.tagName != 'BODY') {
		if (e.tagName == 'TR') {
			return e;
		} else {
			e = e.parentNode;
		}
	}
}


/**
 * resets the css back to standard
 */
Testcase.resetTableCSS = function(sectionID) {
	var sec = $(sectionID);
	var tables = $$('table.wikitable');
	var tds;
	
	for (var i = 0; i < tables.length; i++) {
		trs = tables[i].getElements('tr');
		for (var j = 1; j < trs.length; j++) {
			trs[j].removeClass(Testcase.testcaseExecuted);
			trs[j].removeClass(Testcase.testcaseSkipped);
		}
	}
}


Testcase.importTestcase = function(sectionID) {
	
	var form = new Element('form', {
		id: 'testcaseImport',
		method:'post',
		enctype: 'multipart/form-data',
		action: 'KnowWE.jsp?action=TestcaseImportAction'
	});
	
	var chooser = new Element('input', {
		type: 'file',
		name: 'testcasefile',
		size: 50
	});
	
	var section = new Element('input', {
		type: 'hidden',
		name: 'kdomid',
		value: sectionID
	});
	
	chooser.inject(form);
	section.inject(form);
	
	var action = function() {
		$('testcaseImport').submit();
	}
	
	KNOWWE.helper.message.showOKCancelDialog(form,'Import testcase' ,action);
	
}
