/**
 * @author Franz Schwab
 */
 
function refactoring() {
 	var formElements = _KS('.refactoring');
 	var formMap = new Object();
 	for (var i = 0; i < formElements.length; i++){
 		with (formElements[i]) {
 			if (formMap[name] == undefined) {
	 			formMap[name] = new Array();
 			}
	 		if (type == "radio" || type == "checkbox") {
				if (checked) {
					formMap[name].push(value);
				}
	 		} else if (type == "select-one" || type == "select-multiple") {
	 			for (var j = 0; j < options.length; j++) {
	 				if(options[j].selected == true) {
	 					formMap[name].push(options[j].value);
	 					if (type == "select-one") {
	 					 	break;	
	 					}
	 				}
	 			}
	 		}
	 		else {
	 			formMap[name].push(value);
	 		}
 		}
	}
	var params = {
		action : 'de.d3web.we.refactoring.action.RefactoringAction',
		jsonFormMap : JSON.stringify(formMap),
	}
	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
		ids : ['refactoring-content']
		}
	}
	new _KA(options).send();
}