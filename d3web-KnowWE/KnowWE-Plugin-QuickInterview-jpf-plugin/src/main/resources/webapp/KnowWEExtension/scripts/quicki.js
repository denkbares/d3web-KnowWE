/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

//var _KE = KNOWWE.helper.event;    /* Alias KNOWWE event. */
//var _KA = KNOWWE.helper.ajax;     /* Alias KNOWWE ajax. */
//var _KS = KNOWWE.helper.selector; /* Alias KNOWWE ElementSelector */
//var _KL = KNOWWE.helper.logger;   /* Alias KNOWWE logger */
//var _KN = KNOWWE.helper.element   /* Alias KNOWWE.helper.element */
//var _KH = KNOWWE.helper.hash      /* Alias KNOWWE.helper.hash */


/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces
 * are preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    var KNOWWE = {};
}

/**
 * The KNOWWE.plugin global namespace object. If KNOWWE.plugin is already defined, the
 * existing KNOWWE.plugin object will not be overwritten so that defined namespaces
 * are preserved.
 */
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
	    KNOWWE.plugin = function(){
	         return {  }
	    }
}

/**
 * The KNOWWE.plugin.solutionpanel global namespace object. If KNOWWE.plugin.quicki is already defined, the
 * existing KNOWWE.plugin.quicki object will not be overwritten so that defined namespaces
 * are preserved.
 */
KNOWWE.plugin.quicki = function(){
    return {
    }
}();



/**
 * Namespace: KNOWWE.plugin.quicki
 * The quick interview (quicki) namespace.
 */
KNOWWE.plugin.quicki = function(){
    
    return {
        /**
         * Function: init
         */
        init : function(){
               // here was the code for displaying the more-menu button - still needed?
        },
        /**
         * Function: initAction
         * 		add the click events and corresponding functions to interview elments
         */
        initAction : function (){
        	_KS('.answer').each(function(element){
        		_KE.add('click', element, KNOWWE.plugin.quicki.answerClicked);                
            });
        	_KS('.questionnaire').each(function(element){
                _KE.add('click', element, KNOWWE.plugin.quicki.updateQuestionnaireVisibility);
        	});
        	 _KS('.num-ok').each(function( element ){
                 _KE.add('click', element, KNOWWE.plugin.quicki.numAnswer);
             });   
        }, 
        /**
         * Function: answerClicked
         * Stores the user selected answer of the HTMLDialog.
         * 
         * Parameters:
         *     event - The user click event on an answer.
         */
        answerClicked : function( event ) {
            var el = _KE.target(event); 	// get the clicked element
            if(el.tagName.toLowerCase() == "input") return; // TODO check
            _KE.cancel( event );
            
            var rel = eval("(" + el.getAttribute('rel') + ")");
            if( !rel ) return;
            var type = rel.type;
            
            KNOWWE.plugin.quicki.toggleAnswerHighlighting(el, type);
            
            // get the currently indicated question
            var q = _KS('#' + rel.qid)
            KNOWWE.plugin.quicki.toggleIndicationHighlighting(q);
                       
            if(type=="mc"){
            	alert("mc clicked");
            	KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', 
                    	{ action : 'SetSingleFindingAction', ValueID: rel.oid});
            } else {
            	KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', 
                	{ action : 'SetSingleFindingAction', ValueID: rel.oid});
            }
        },
        /**
         * Function: numAnswer
         * 		Handles the input of num-values
         * 
         * Parameters:
         * 		event - the event firing the action
         */
        numAnswer : function (event) {
        	event = new Event( event ).stopPropagation();
            var bttn = (_KE.target( event ).className == 'num-ok');            
            var key = (event.code == 13);
            
            // check, if either button was clicked or enter was pressed
            if( !(key || bttn) ) return false;
            
            var rel = null;
            if(key){				// if enter was pressed
                rel = eval("(" + _KE.target( event ).getAttribute('rel') + ")");
            } else {				// if button was clicked
                rel = eval("(" + _KE.target( event ).previousSibling.getAttribute('rel') + ")");
            }
            if( !rel ) return;
            
            var inputtext = 'inputTextNotFound';	// default input
            
            // if an input was given in the field
            if(_KS('#input_' + rel.oid)) {
                    inputtext = _KS('#input_' + rel.oid).value; 
            }
            
            // send KNOWWE request as SingleFindingAction with given value
            KNOWWE.plugin.quicki.send(rel.web, rel.ns, rel.oid, rel.qtext, 
            		{action : 'SetSingleFindingAction', ValueNum: inputtext});
        },
        /**
         * Function: toggleImage(int)
         * 		Toggles the image display for questionnaire headings
         * 
         * Parameter:
         * 		flag - either 1 or 0; 1 means, image is actually displayed
         * 		  and needs to be hidden, 0 vice versa
         * 		questionnaire - the element, the image is attached to
         */	
        toggleImage : function (flag, questionnaire) {
        	
        	if(flag==1){
        		// questionnaire is visible and should be hidden
        		// thus image needs to be the triangle indicating extensibility
        		questionnaire.className = 'questionnaire pointRight';     
        	} else if (flag==0){
        		questionnaire.className = 'questionnaire pointDown';   
        	}
        },
        /**
         * Function: toggleAnswerHighlighting
         * Hightlights an answer if clicked or unhighlights an highlighted answer if clicked
         *
         * Parameters:
         * 		answerEl - The clicked answer-element
         * 		type - flag that tells whether OC or MC question for appropriate
         * 				highlighting
         */
        toggleAnswerHighlighting : function( answerEl, type ){
        	
        	var relClicked = eval("(" +  answerEl.getAttribute('rel') + ")");
        	if(type=="oc"){	
        		_KS('.answerClicked').each(function(element){
        			var relElement =  eval("(" +  element.getAttribute('rel') + ")");
        			if (relElement.qid==relClicked.qid){
        				alert("found");
        				element.className = 'answer';
        			}
                });
        	} else if (type=="mc"){
        		
        	}
        	
        	// to highlight/unhighlight an answer if clicked on
        	if(answerEl.className=='answerClicked'){
        		answerEl.className = 'answer';
        	} else if (answerEl.className=='answerunknownClicked'){
        		answerEl.className = 'answerunknown';
        	} else if (answerEl.className=='answer'){
        		answerEl.className = 'answerClicked';
        	} else {
        		answerEl.className = 'answerunknownClicked';
        	}
        }, 
        toggleIndicationHighlighting : function ( element ) {
        	if(element.className=='question indicated'){
        		element.className = 'question';
        	}
        },
        /**
         * Function: updateQuestionnaireVisibility 
         * Toggles the visibility of questionnaire-contents on click:
         * 		visible ones are hidden, hidden ones are displayed
         * 
         * Parameters: 
         *  	event - The fired the click event  
         */
        updateQuestionnaireVisibility : function( event ){
        	      
        	// get the clicked element, i.e., the questionnaire
        	var questionnaire = _KE.target(event); 	
        	var group = _KS('#group_' + questionnaire.id);
        	
        	if(group.style.display=='block'){
            	group.style.display = 'none';     
            	KNOWWE.plugin.quicki.toggleImage(1, questionnaire);            	
            } else if (group.style.display=='none'){     
            	group.style.display = 'block';
            	KNOWWE.plugin.quicki.toggleImage(0, questionnaire);   
            } 
          	KNOWWE.plugin.quicki.showRefreshed();
        },
        /**
         * Function: send
         * Stores the user input as single finding through an AJAX request.
         * 
         * Parameters:
         *     web - the web context
         *     namespace - The name of the article
         *     oid - The id of the question   
         *     termName - The question text
         *     params - Some parameter depending on the HTMLInputElement
         */
        send : function( web, namespace, oid, termName, params){
            var pDefault = {
                KWikiWeb : web,
                namespace : namespace,
                ObjectID : oid,
                TermName : termName,
            }
            
            pDefault = KNOWWE.helper.enrich( params, pDefault );
            
            var options = {
                url : KNOWWE.core.util.getURL( pDefault ),
                response : {
                    fn : function(){
                    	 KNOWWE.plugin.quicki.showRefreshed();
                    }
                }
            }
            new _KA( options ).send();         
        },
        /**
         * Function: showRefreshed
         * 		send the request and render the interview newly via
         * 		QuickInterviewAction 
         */
        showRefreshed : function ( ){
        	 var params = {
                     namespace : KNOWWE.helper.gup( 'page' ),
                     action : 'QuickInterviewAction'
             }
             var options = {
                 url : KNOWWE.core.util.getURL( params ),
                 response : {
                	 fn : function(){
                		 KNOWWE.plugin.quicki.initAction();
                		 KNOWWE.helper.observer.notify('update');
                	 }	
                 }
        	 }
             new _KA( options ).send();
        }
    }
}();



/**
 * Initializes the required JS functionality when DOM is readily loaded
 */
(function init(){ 
    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
        	KNOWWE.plugin.quicki.initAction();
        });
    }
}());