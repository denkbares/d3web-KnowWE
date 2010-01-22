/**
 * @author Franz Schwab
 */
function selectRefactoring() {
	//alert(document.refactoringform.refactoringselect.value);
	var params = {
		action : 'ShowRefactoringAction',
		refactoringElement : document.refactoringform.refactoringselect.value
	//        TargetNamespace : _KS('#renameInputField').value,
	//        KWikiFocusedTerm : _KS('#replaceInputField').value,
	//        ContextPrevious : _KS('#renamePreviousInputContext').value,
	//        ContextAfter : _KS('#renameAfterInputContext').value,
	//        CaseSensitive : _KS('#search-sensitive').checked
	}

	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
		ids : ['refactoring-content']
		}
	}
	new _KA(options).send();
}

function refactoring(refactoringElement) {
	//alert(document.refactoringform.refactoringselect.value);
	var params = {
		action : 'RefactoringAction',
		knowledgeElement : document.refactoringform.refactoringselect.value,
		refactoringElement : refactoringElement
	//        TargetNamespace : _KS('#renameInputField').value,
	//        KWikiFocusedTerm : _KS('#replaceInputField').value,
	//        ContextPrevious : _KS('#renamePreviousInputContext').value,
	//        ContextAfter : _KS('#renameAfterInputContext').value,
	//        CaseSensitive : _KS('#search-sensitive').checked
	}

	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
		ids : ['refactoring-content']
		}
	}
	new _KA(options).send();
}