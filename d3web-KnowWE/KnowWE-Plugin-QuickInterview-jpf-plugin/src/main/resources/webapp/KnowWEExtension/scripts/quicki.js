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
        initAction : function (){
        	_KS('.answer').each(function(element){
        		_KE.add('click', element, KNOWWE.plugin.quicki.answerClicked);                
            });
        	_KS('.questionnaire').each(function(element){
                _KE.add('click', element, KNOWWE.plugin.quicki.updateQuestionnaireVisibility);
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
            KNOWWE.plugin.quicki.toggleAnswerHighlighting(el);
            KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', {ValueID: rel.oid});
        }, 
        /**
         * Function: toggleAnswerHighlighting
         * Hightlights an answer if clicked or unhighlights an highlighted answer if clicked
         *
         * Parameters:
         * 	element - The clicked answer-element
         */
        toggleAnswerHighlighting: function( element ){
        	if(element.className=='answerClicked'){
        		element.className = 'answer';
        	} else if (element.className=='answerunknownClicked'){
        		element.className = 'answerunknown';
        	} else if (element.className=='answer'){
        		element.className = 'answerClicked';
        	} else {
        		element.className = 'answerunknownClicked';
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
        	alert(group);
        	// for each qablock toggle visibility
        	
        	if(group.getAttribute('style') == 'display: block;'){
            	alert("block");
            	group.setAttribute('style', 'display: none;');        
            	
            } else if (group.getAttribute('style') == 'display: none;'){
            	alert("none");
            	group.setAttribute('style', 'display: block;');            	
            } 
            
        	KNOWWE.plugin.d3web.dialog.initAction();
        	KNOWWE.helper.observer.notify('update');
                // display the right image for the questionnaire
                //toogleImage( _KE.target(event) , tbl.className ); 
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
                    fn : function(){
                    	KNOWWE.plugin.d3web.dialog.initAction();
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