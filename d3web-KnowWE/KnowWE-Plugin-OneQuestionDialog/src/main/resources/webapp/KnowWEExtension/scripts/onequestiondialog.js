/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

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
	var question = div.getElement('p').textContent;;
	var questionId = div.getElement('p').getElement('input').value;
	var tbody = OneQuestionDialog.findTbody(element);
	var trs = tbody.childNodes;
	
	var web = 'default_web';
	var namespace = KNOWWE.helper.gup('page');
	
	
	var answerId = '';
	var answerValue = '';
	
	var type = trs[0].getElement('input').type;
	
	if (type == 'radio') {
		for (var i = 0; i < trs.length; i++) {
			var td = trs[i].getElement('td');
			if (td.firstChild.checked) {
				answerId = td.lastChild.value;
				break;
			}
		}
	} else if (type == 'checkbox') {
		for (var i = 0; i < trs.length; i++) {
			var td = trs[i].getElement('td');
			if (td.firstChild.checked) {
				answerId += td.lastChild.value + '#####';
			}
		}
		answerId = answerId.substring(0, answerId.lastIndexOf('#####'));
	} else if (type == 'text') {
		answerValue = trs[0].getElement('td').lastChild.value;
	}
		
	if (answerId != '') {
		OneQuestionDialog.sendInput(web, namespace, questionId, 'undefined', question, questionId, {ValueID: answerId});
	} else {
		OneQuestionDialog.sendInput(web, namespace, questionId, 'undefined', question, questionId,{ValueNum: answerValue});	
	}
	
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
OneQuestionDialog.sendInput = function( web, namespace, oid, termName, question, questionId, params){
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
                action: 'none',
                fn : function(){
        			KNOWWE.helper.observer.notify('update');
                }
            }
        }
                  
        new _KA( options ).send();         
}

/**
 * gets the next question from OneQuestionDialogAction
 */
OneQuestionDialog.getNewQuestion = function(question, questionId, type) {

    var params = {
        action : 'OneQuestionDialogAction',
        KWiki_Topic : KNOWWE.helper.gup('page'),
        question: question,
        questionId: questionId,
        type: type
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
	var newText = response.responseText;
	if (!newText) {
		return;
	}
	
	var root = $('onequestiondialog');
	var div = root.getElement('div');
	
	if (div.hasChildNodes()){
	    while (div.childNodes.length >= 1){
	    	div.removeChild(div.firstChild);       
	    } 
	}
	
	div.innerHTML = newText;
}


/**
 * replaces the current question with the previous one
 */
OneQuestionDialog.getPrevious = function(element) {
	var div = OneQuestionDialog.findParentDiv(element);
	var question = div.getElement('p').textContent;;
	var questionId = div.getElement('p').getElement('input').value;
	
	return OneQuestionDialog.getNewQuestion(question, questionId, 'previous');
}


/**
 * the function which is called, after a question is answered
 */
OneQuestionDialog.newQuestionAfterUpdate = function() {
	var element = $('oqdbutton');
	if (!element)
		return; 
	
	var div = OneQuestionDialog.findParentDiv(element);
	var question = div.getElement('p').textContent;;
	var questionId = div.getElement('p').getElement('input').value;
	
	OneQuestionDialog.getNewQuestion(question, questionId, 'next');
}

OneQuestionDialog.submitOnEnter = function(element, e) {
	  var keyCode=(e)? e.which :event.keyCode;
	  if(keyCode==13) {
		  OneQuestionDialog.sendQuestion(element);
	  }
}

window.addEvent('domready', KNOWWE.helper.observer.subscribe( 'update', OneQuestionDialog.newQuestionAfterUpdate));
