
if (!KNOWWE.plugin.abstractionTable) KNOWWE.plugin.abstractionTable = {};

jq$(document).ready(function() {
	// Prepare for instant table editor with custom auto-complete 
	KNOWWE.plugin.abstractionTable.editTool = KNOWWE.plugin.tableEditTool.create(
			function (callback, prefix, spreadsheet, row, col) {
				var ajaxFun, ajaxPrefix = prefix;
				// prepare object name
				var colName = spreadsheet.getCellTextTrimmed(0, col);
				colName = AutoComplete.unquoteTermIdentifier(colName);
				if (AutoComplete.termRequiresQuotes(colName)) colName = '"' +colName + '"';
				// prepare ajax call
				if (row == 0) {
					ajaxFun = AutoComplete.sendD3webValueObjectCompletionAction;
				}
				else if (col == spreadsheet.size.cols-1) {
					ajaxFun = AutoComplete.sendD3webActionCompletionAction;
					ajaxPrefix = colName + ' = ' + prefix;
				} 
				else {
					ajaxFun = AutoComplete.sendD3webConditionCompletionAction;
					ajaxPrefix = colName + ' = ' + prefix;
				}
				ajaxFun(function(byAjax) {
					AutoComplete.unquoteTermIdentifiers(byAjax);
					callback(byAjax);
				}, ajaxPrefix);
			});
});