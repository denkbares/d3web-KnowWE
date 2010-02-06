/**
 * @author Franz Schwab
 */
 
 function refactoring() {
	var params = {
		action : 'RefactoringAction',
		formdata : document.refactoringform.refactoringselect.value,
	}
	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
		ids : ['refactoring-content']
		}
	}
	new _KA(options).send();
}