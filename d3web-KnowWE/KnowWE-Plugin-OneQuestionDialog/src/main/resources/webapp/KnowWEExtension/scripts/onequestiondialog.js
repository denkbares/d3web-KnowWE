var OneQuestionDialog = {};

OneQuestionDialog.sendQuestion = function(element) {
	var div = OneQuestionDialog.findParentDiv(element);
	var question = div.firstChild.firstChild.firstChild.nodeValue;
	var questionId = div.firstChild.firstChild.lastChild.value;
	var tbody = OneQuestionDialog.findTbody(element);
	var trs = tbody.childNodes;
	
	var web = 'default_web';
	var namespace = 'OneQuestionDialog..OneQuestionDialog_KB'
	
	
	var answerId = '';
	
	for (var i = 0; i < trs.length; i++) {
		if (trs[i].firstChild.firstChild.checked) {
			answerId = trs[i].firstChild.lastChild.value;
			break;
		}
	}
	
	//answers = answers.substring(0, answers.lastIndexOf(',.,'));
	OneQuestionDialog.send(web, namespace, questionId, 'undefined', {ValueID: answerId});
	//OneQuestionDialog.sendAnswers(question, answers);
}

OneQuestionDialog.findParentDiv = function(element) {
	if (element.className === 'oneQuestionDialog' && element.tagName === 'DIV') {
		return element;
	} else {
		while (element.className !== 'oneQuestionDialog' && element.tagName !== 'DIV') {
			if (element.tagName == 'BODY') {
				return null;
			}
			element = element.parentNode;
		}
		return element;
	}
}

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

OneQuestionDialog.send = function( web, namespace, oid, termName, params){
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

OneQuestionDialog.sendAnswers = function(question, answers) {

    var params = {
        action : 'OneQuestionDialogAction',
        KWiki_Topic : KNOWWE.helper.gup('page'),
        question: question,
        answers: answers
    }

    var options = {
        url : KNOWWE.core.util.getURL ( params ),
        loader : true,
        response : {
            action : 'none',
            fn : function(){
    			
            }
        }
    }
    new _KA( options ).send();
}
