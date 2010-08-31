var OneQuestionDialog = {};


/**
 * The function, which is called after a click on the button.
 * Evaluates which input was selected, and sends the selected
 * value via sendInput to appropiate action.
 * Next it gets the new Question and Answers and replaces the
 * old.
 */
OneQuestionDialog.sendQuestion = function(element) {
	var div = OneQuestionDialog.findParentDiv(element);
	var question = div.firstChild.firstChild.firstChild.textContent;
	var questionId = div.firstChild.firstChild.lastChild.value;
	var tbody = OneQuestionDialog.findTbody(element);
	var trs = tbody.childNodes;
	
	var web = 'default_web';
	var namespace = 'OneQuestionDialog..OneQuestionDialog_KB'
	
	
	var answerId = '';
	
	var type = trs[1].firstChild.firstChild.type;
	
	if (type == 'checkbox') {
		for (var i = 0; i < trs.length; i++) {
			if (trs[i].firstChild.firstChild.checked) {
				answerId = trs[i].firstChild.lastChild.value;
				break;
			}
		}
	}
	
	OneQuestionDialog.sendInput(web, namespace, questionId, 'undefined', {ValueID: answerId});
	OneQuestionDialog.getNewQuestion(question);
}

/**
 * returns the parentDiv of an element or null
 */
OneQuestionDialog.findParentDiv = function(element) {
	if (element.className === 'oneQuestionDialog' && element.tagName === 'DIV') {
		return element;
	} else {
		while (!(element.className === 'oneQuestionDialog' && element.tagName === 'DIV')) {
			if (element.tagName == 'BODY') {
				return null;
			}
			element = element.parentNode;
		}
		return element;
	}
}

/**
 * return the parentTbody of an element or null
 */
OneQuestionDialog.findTbody = function(element) {
	if (element.tagName === 'TBODY') {
		return element;
	} else {
		while (element.tagName !== 'TBODY') {
			if (element.tagName == 'BODY') {
				return null;
			}
			element = element.parentNode;
		}
		return element;
	}
}


/**
 * sends an input to SetSingleFindingAction
 */
OneQuestionDialog.sendInput = function( web, namespace, oid, termName, params){
    var pDefault = {
            action : 'SetSingleFindingAction',
            KWikiWeb : web,
            namespace : namespace,
            ObjectID : oid,
            TermName : termName             
        }
        pDefault = KNOWWE.helper.enrich( params, pDefault );
        
        var options = {
            url : KNOWWE.core.util.getURL( pDefault ),
            response : {
                action: 'insert',
                ids : ['dialog-panel'],
                fn : function(){
                    KNOWWE.plugin.d3web.dialog.refreshed();
                }
            }
        }
                  
        new _KA( options ).send();         
}

/**
 * gets the next question from OneQuestionDialogAction
 */
OneQuestionDialog.getNewQuestion = function(question) {

    var params = {
        action : 'OneQuestionDialogAction',
        KWiki_Topic : KNOWWE.helper.gup('page'),
        question: question
    }

    var options = {
        url : KNOWWE.core.util.getURL ( params ),
        loader : true,
        response : {
            action : 'none',
            fn : function(){
    			OneQuestionDialog.insertNewQuestion(this);
            }
        }
    }
    new _KA( options ).send();
}

/**
 * replaces the old question with the next new one
 * from the response of an ajax request
 */
OneQuestionDialog.insertNewQuestion = function(response) {
	var root = $('onequestiondialog');
	var div = root.getElement('div');
	
	if (div.hasChildNodes()){
	    while (div.childNodes.length >= 1){
	    	div.removeChild(div.firstChild);       
	    } 
	}
	
	div.innerHTML = response.responseText;
}
