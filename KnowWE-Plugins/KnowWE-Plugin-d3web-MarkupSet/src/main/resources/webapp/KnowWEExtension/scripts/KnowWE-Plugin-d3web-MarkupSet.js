KNOWWE = KNOWWE || {};
KNOWWE.plugin = KNOWWE.plugin || {}
KNOWWE.plugin.abstractionTable = {}
KNOWWE.plugin.propertyTable = {}

jq$(document).ready(function () {
	// Prepare for instant table editor with custom auto-complete for abstraction table
	KNOWWE.plugin.abstractionTable.editTool = KNOWWE.plugin.tableEditTool.create(
		function (callback, prefix, spreadsheet, row, col) {
			var ajaxFun, ajaxPrefix = prefix;
			// prepare object name
			var colName = spreadsheet.getCellTextTrimmed(0, col);
			colName = AutoComplete.unquoteTermIdentifier(colName);
			if (AutoComplete.termRequiresQuotes(colName)) colName = '"' + colName + '"';
			// prepare ajax call
			if (row === 0) {
				ajaxFun = AutoComplete.sendD3webValueObjectCompletionAction;
			}
			else if (col === spreadsheet.size.cols - 1) {
				ajaxFun = AutoComplete.sendD3webActionCompletionAction;
				ajaxPrefix = colName + ' = ' + prefix;
			}
			else {
				ajaxFun = AutoComplete.sendD3webConditionCompletionAction;
				ajaxPrefix = colName + ' = ' + prefix;
			}
			ajaxFun(function (byAjax) {
				AutoComplete.unquoteTermIdentifiers(byAjax);
				callback(byAjax);
			}, ajaxPrefix);
		});

	// Prepare for instant table editor with custom auto-complete for property table
	KNOWWE.plugin.propertyTable.editTool = KNOWWE.plugin.tableEditTool.create(
		function (callback, prefix, spreadsheet, row, col) {
			var ajaxFun, ajaxPrefix = prefix;
			var scope;
			if (row === 0) {
				if (col >= 0) {
					ajaxFun = AutoComplete.sendCompletionAction
					scope = 'PropertyTable'
				}
			}
			else if (col === 0) {
				ajaxFun = AutoComplete.sendD3webValueObjectCompletionAction;
			}
			ajaxFun(function (byAjax) {
				AutoComplete.unquoteTermIdentifiers(byAjax);
				callback(byAjax);
			}, ajaxPrefix, scope);
		});

	KNOWWE.plugin.propertyTable.editTool.getWikiText = function (id) {
		var wikiText = _EC.getWikiText(id);
		if (!/%%PropertyTable\s*\|/i.test(wikiText)) {
			wikiText = "|| Name\t\t|| prompt\t\t|| description \n| Your Question\t| The Question Prompt\t| The Description of the Question " +
				wikiText.substring(wikiText.indexOf("\n")).trim();
		}
		return wikiText;
	};
});
