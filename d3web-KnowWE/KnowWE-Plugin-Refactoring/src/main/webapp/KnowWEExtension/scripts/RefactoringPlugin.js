/**
 * @author Franz Schwab
 */
function sendRefactoringRequest() {
	//alert(document.refactoringform.refactoringselect.value);
	var params = {
		action : 'RefactoringAction',
		KnowledgeElement : document.refactoringform.refactoringselect.value
	//        TargetNamespace : _KS('#renameInputField').value,
	//        KWikiFocusedTerm : _KS('#replaceInputField').value,
	//        ContextPrevious : _KS('#renamePreviousInputContext').value,
	//        ContextAfter : _KS('#renameAfterInputContext').value,
	//        CaseSensitive : _KS('#search-sensitive').checked
	}

	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
		// ids : ['refactoring-result']
		}
	}
	new _KA(options).send();
}