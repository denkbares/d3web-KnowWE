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
 * The KNOWWE.plugin.quicki global namespace object. If KNOWWE.plugin.quicki is already defined, the
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
	

	var mcanswervals = '';      // for collecting the values of MC answers
	var quickiruns = false;     // flag whether QuickI runs a session
	var questionnaireVis = ''; // for storing questionnaire visibility states
	var questionVis = '';		// for storing question visibility states
	
	return {
        applyProcessingStateToEventHandler : function (fun, event) {
        	KNOWWE.core.util.updateProcessingState(1);
        	try {
        		fun(event);
        	}
        	catch (e) { /*ignore*/ }
        	KNOWWE.core.util.updateProcessingState(-1);
        },
		/**
         * Function: initialize
         * 		add the click events and corresponding functions to interview elments
         */
        initialize : function (){
        	
        	_KS('.answer').each(function(element){
        		_KE.add('click', element, 
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.answerClicked, event);
        		});
            });
        	
        	_KS('.answerunknown').each(function(element){
                _KE.add('click', element,  
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.answerUnknownClicked, event);
        		});
        	});
        	
        	_KS('.answerMC').each(function(element){
                _KE.add('click', element,  
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.answerMCCollect, event);
        		});
        	});
        	
        	_KS('.answerMCClicked').each(function(element){
                _KE.add('click', element,  
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.answerMCCollect, event);
        		});
        	});
        	
        	_KS('.questionnaire').each(function(element){
                _KE.add('click', element,  
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.toggleQuestionnaireVisibility, event);
        		});
        	});
        	
        	
        	// add click-event for divs with class='num-ok' to submit numValues
        	_KS('.num-ok').each(function( element ){
        		_KE.add('click', element,  
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.numAnswerClicked, event);
        		});
            });
        	
        	_KS('.numinput').each(function( element ){
        		_KE.add('keydown', element,  
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.numAnswerClicked, event);
        		});
            });  
        	
        	_KS('.inputdate').each(function( element ){
        		_KE.add('keydown', element,  
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.dateAnswerClicked, event);
        		});
            });
            
            _KS('.inputtextvalue').each(function( element ){
        		_KE.add('keydown', element,  
        		function(event) {
        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.textAnswerClicked, event);
        		});
            });
        	
        	/* removing indicated elements from the storages; this ensures that indicated
        	 * questions are always displayed expanded, even after e.g. the user previously
        	 * collapsed a (part of) such a questionnaire tree. */
        	_KS('.indicated').each(function( element ){
	        	KNOWWE.core.util.updateProcessingState(1);
	        	try {
	        		var ids = questionnaireVis.indexOf(element.id);
	        		var torep = questionnaireVis.substring(ids, ids + element.id.length + 1 + 5);
	        		if (questionnaireVis.indexOf(torep)!=-1){
	        			questionnaireVis = questionnaireVis.replace(torep, "");
	        		}
	          	}
	        	catch (e) { /*ignore*/ }
	        	KNOWWE.core.util.updateProcessingState(-1);
            }); 
        	
        	if ($('quickireset')) {
	            _KE.add('click', _KS('#quickireset'),  
	        		function(event) {
	        			KNOWWE.plugin.quicki.applyProcessingStateToEventHandler(KNOWWE.plugin.quicki.quickIReset, event);
	        		});
        	}
            
        	/* restore visibility states of elements after reloading
        	 * the page (e.g. after sending answer val via AJAX)
        	 */
        	KNOWWE.plugin.quicki.restoreQuestionnaireVis();
        },
        /**
         * Function: restoreQuestionnaireVis
         * 		restores the visibility states of questionnaires 
         * 		after reloading the page, eg. after an automatic
         * 		AJAX refresh
         */
        restoreQuestionnaireVis : function(){
        	
        	KNOWWE.core.util.updateProcessingState(1);
        	try {
	        	// split questionnaireVis storage into questionnaire;vis
	        	// elments
	        	var qs = questionnaireVis.split('###');
	        	
	        	for (var i = 0; i < qs.length; i++) {
	        	
	        		// split into questionnaire id and visibility
	        		var qsplit = qs[i].split(';');
	        		var qid = qsplit[0];
	        		qid = qid.replace(/ /g, ''); // remove spaces
	        		var qvis = qsplit[1];
	        		        		
	        		var groupEl = _KS('#group_' + qid);
	        		var questionnaire = _KS('#'+qid);
	        		
	        		// 0 means set style and image to invisible
	        		if(qvis==0){
	        			groupEl.style.display = 'none'; 
	        			KNOWWE.plugin.quicki.toggleImage(1, questionnaire);       
	        		} 
	        		// 1 means set style and image to be visible = unfolded
	        		else if (qvis ==1 ){
	        			groupEl.style.display = 'block'; 
	        			KNOWWE.plugin.quicki.toggleImage(0, questionnaire);       
	        		}
	        	}
        	}
        	catch (e) { /*ignore*/ }
        	KNOWWE.core.util.updateProcessingState(-1);
        },
        /**
         * Function: restoreQuestionVis
         * 		restores the visibility states of questions 
         */
        restoreQuestionVis : function(){
        	
        	KNOWWE.core.util.updateProcessingState(1);
        	try {
	        	// split questionVis storage
	        	var qs = questionVis.split('###');
	        	
	        	for (var i = 0; i < qs.length; i++) {
	        	
	        		// split into question id and visibility
	        		var qsplit = qs[i].split(';');
	        		var qid = qsplit[0];
	        		qid = qid.replace(/ /g, ''); // remove spaces
	        		var qvis = qsplit[1];
	        		        	
	        		// TODO check what we need to get here: the table???
	        		var groupEl = _KS('#group_' + qid);
	        		var questionnaire = _KS('#'+qid);
	        		
	        		// 0 means set style and image to invisible
	        		if(qvis==0){
	        			groupEl.style.display = 'none'; 
	        			KNOWWE.plugin.quicki.toggleImage(1, questionnaire);       
	        		} 
	        		// 1 means set style and image to be visible = unfolded
	        		else if (qvis ==1 ){
	        			groupEl.style.display = 'block'; 
	        			KNOWWE.plugin.quicki.toggleImage(0, questionnaire);       
	        		}
	        	}
        	}
        	catch (e) { /*ignore*/ }
        	KNOWWE.core.util.updateProcessingState(-1);
        },
         /**
         * Function: answerMCCollect
         * 		collects given mc answer vals into a variable for sending them later
         * 		as ONE MultipleChoiceValue
         * 
         * Parameters:
         * 		event - the event fired by the mc answer val that was clicked
         */
        answerMCCollect : function( event ) {
            var el = _KE.target(event); 	// get the clicked element
            _KE.cancel( event );
            
            // TODO new algorithm for mc vals:
        	// if a not highlighted answer is clicked it is
        	// 	- highlighted
        	//  - we fetch all already highlighted answers and set those as one new mc fact
        	//	- during each setting, the last-set-fact is stored
        	// if an answer already answered is clicked it is
        	// 	- de-highlighted
        	// 	- again all selected answers are fetched (i.e. minus the clicked one)
        	//	- and set as a mc fact
        	// if mcanswervals is empty, retract the last fact
           
            var rel = eval("(" + el.getAttribute('rel') + ")");
            if( !rel ) return;
            var oid = rel.oid;
            var toreplace = oid + "#####";
        	            
            /* not yet clicked, thus highlight, store, collect all highlighted, and send */
            if(el.className=='answerMC'){
            	el.className='answerMCClicked';
            	
            	// if not already contained, attach value
            	if(mcanswervals.indexOf(toreplace)==-1){
            		mcanswervals += oid;
                    mcanswervals += "#####"
            	}
            	// get the newly assembled, complete mc fact without the last "#####"
            	mcvals = mcanswervals.substring(0, mcanswervals.length-5);
            	// and send it
            	KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', 
                    	{action: 'SetSingleFindingAction', ValueID: mcvals});
            	
            } 
            /* already clicked mc answer */
            else if (el.className=='answerMCClicked'){
            	el.className='answerMC';
            	
            	// save mcvalues
            	var mcvalsOld = mcanswervals.substring(0, mcanswervals.length-5);
            	
            	// if value is alerady contained, remove it
            	if(mcanswervals.indexOf(toreplace)!=-1){
            		mcanswervals = mcanswervals.replace(toreplace, '');
            	}
            	
            	// if mcanswerval storage is empty after last removal
            	if(mcanswervals == ""){
            		// we need to call a retract action 
                	KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', 
                        	{action: 'RetractSingleFindingAction', ValueID: mcvalsOld});
            	} else {
            		// get the newly assembled, complete mc fact
                	mcvals = mcanswervals.substring(0, mcanswervals.length-5);
                	// and send it
                	KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', 
                        	{action: 'SetSingleFindingAction', ValueID: mcvals});
            	}
        	}
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
            if(el.className.toLowerCase() == "answerunknown") return;
            if(el.className.toLowerCase() == "answerunknownclicked") return;
            if(el.className.toLowerCase() == "answermc") return;
            if(el.className.toLowerCase() == "answermcclicked") return;
            var retract = false;
            
            // if already clicked, it needs to be de-highlighted and val retracted
            if(el.className == 'answerClicked'){
            	retract = true;
            }
            _KE.cancel( event );
            
            var rel = eval("(" + el.getAttribute('rel') + ")");
            if( !rel ) return;
            var type = rel.type;
            
            KNOWWE.plugin.quicki.toggleAnswerHighlighting(el, type, retract);
            
            // if it is already highlighted it should now be deactivated and value retracted
            if(retract){
            	KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', 
                    	{action: 'RetractSingleFindingAction', ValueID: rel.oid});
            }
            // otherwise send the value
            else {
            	KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', 
                    	{action: 'SetSingleFindingAction', ValueID: rel.oid});
            }
        },
        /**
         * Function: answerUnknownClicked
         * 		Sets value unknown for the clicked quesstion and toggles all
         * 		already highlighted answers
         * 
         * Parameters:
         *     event - The user click event on an answer.
         */
        answerUnknownClicked : function( event ) {
        	
            var el = _KE.target(event); 	// get the clicked element
            var rel = eval("(" + el.getAttribute('rel') + ")");
            var questionID = rel.qid;
          
            // handle num input fields
            if( rel.type=='num') {
            	var numfield = _KS('#input_' + rel.qid);
            	// clear input field
            	if(numfield) {
                    numfield.value = "";
            	}      
            } 
         	
            // do nothing caus Unknown can only be taken back if another val is set
			if(el.className=='answerunknownClicked'){
				
			} else if (el.className=="answerunknown"){
				KNOWWE.plugin.quicki.send( rel.web, rel.ns, rel.qid, 'undefined', 
			             	{action: 'SetSingleFindingAction', ValueID: 'MaU'});
				
				// TODO: check if those are correct and needed at all
				el.className="answerunknownClicked"; // change the highlightung
				mcanswervals = ""; // reset the mcanswerval storage
			}
            KNOWWE.plugin.quicki.toggleAnswerHighlightingAfterUnknown(questionID);  
               
         },
        /**
         * Function: numAnswerClicked
         * 		Handles the input of num-values
         * 
         * Parameters:
         * 		event - the event firing the action
         */
        numAnswerClicked : function (event) {
        	event = new Event( event ).stopPropagation();
        	//this (hidden) div is for submitting numValues by click-event
        	var div = (_KE.target( event ).className == 'num-ok');
        	var key = (event.code == 13);
            
            // check, if either the div was clicked or enter was pressed
        	if( !(key || div) ) return false;
            
            if(key){				// if enter was pressed
                rel = eval("(" + _KE.target( event ).getAttribute('rel') + ")");
            } else {				// if div was clicked
            	// split the ID of the <div> by "_"
            	var arrSplittedID = _KE.target( event ).getAttribute('id').split('_');
            	// use the second part of the splitted ID to get the <input> element
            	var inputID = 'input_' + arrSplittedID[1];
                rel = eval("(" + $(inputID).getAttribute('rel') + ")");
            }
            if( !rel ) return;            
  
            var inputtext = 'inputTextNotFound';	// default input
            
            // get the provided value if any is provided
            if(_KS('#input_' + rel.oid)) {
                inputtext = _KS('#input_' + rel.oid).value; 
                
                // enabling float value input also with "," instead of "."
                if(inputtext.indexOf(",")!=-1){
                	inputtext = inputtext.replace(",", ".");
                }
    	 		
                // if range is given, validate range
                if(rel.rangeMin!='NaN' && rel.rangeMax!='NaN'){
            	            	
                	var min = parseInt(rel.rangeMin);
                	var max = parseInt(rel.rangeMax);
            	 	// compare with range
                	if(parseInt(inputtext) >= min && parseInt(inputtext) <= max){
            		 	 	                		
                        if(_KS('#' + rel.oid + "_errormsg")){
                        	_KS('#' + rel.oid + "_errormsg").className='invisible';	
                        	_KS('#' + rel.oid + "_errormsg").innerHTML='';
                        }
                	 	
            	 		// send KNOWWE request as SingleFindingAction with given value
                    	KNOWWE.plugin.quicki.send(rel.web, rel.ns, rel.oid, rel.qtext, 
                    		{action: 'SetSingleFindingAction', ValueNum: inputtext});

            	 	} else {
            		 	_KE.target( event ).value = inputtext;
            		 	
            		 	// and display error message
            		 	var errormessage = 'Input needs to be a number between ' + rel.rangeMin + ' and ' + rel.rangeMax + '!';
            		 	_KS('#' + rel.oid + "_errormsg").className='errormsg';
            		 	_KS('#' + rel.oid + "_errormsg").innerHTML=errormessage;
            	 	} 
            	} 
            	// else just try to get the value and set it as finding
            	else {
            		// send KNOWWE request as SingleFindingAction with given value
                	KNOWWE.plugin.quicki.send(rel.web, rel.ns, rel.oid, rel.qtext, 
                		{action: 'SetSingleFindingAction', ValueNum: inputtext});
            	}
            }
        },
        /**
         * Function: dateAnswerClicked
         * 		Handles the input of date-values
         * 
         * Parameters:
         * 		event - the event firing the action
         */
        dateAnswerClicked : function (event) {
        	event = new Event( event ).stopPropagation();         
            var key = (event.code == 13);
            
            // check, if either button was clicked or enter was pressed
            if( !key) return false;
            
            var rel = eval("(" + _KE.target( event ).getAttribute('rel') + ")");
            if( !rel ) return;
            
            var inputtext = 'inputTextNotFound';	// default input
            
            // if an input was given in the field
            if(_KS('#input_' + rel.oid)) {
                    inputtext = _KS('#input_' + rel.oid).value; 
            }
            if( !inputtext ) return;  
            // send KNOWWE request as SingleFindingAction with given value
            KNOWWE.plugin.quicki.send(rel.web, rel.ns, rel.oid, rel.qtext, 
            		{action: 'SetSingleFindingAction', ValueDate: inputtext});
        },
        /**
         * Function: textAnswerClicked
         * 		Handles the input of text-values
         * 
         * Parameters:
         * 		event - the event firing the action
         */
        textAnswerClicked : function (event) {
        	event = new Event( event ).stopPropagation();         
            var key = (event.code == 13);
            
            // check, if either button was clicked or enter was pressed
            if( !key) return false;
            
            var rel = eval("(" + _KE.target( event ).getAttribute('rel') + ")");
            if( !rel ) return;
            
            var inputtext;	// default input
            
            // if an input was given in the field
            if(_KS('#input_' + rel.oid)) {
                    inputtext = _KS('#input_' + rel.oid).value; 
            }
            if( !inputtext ) return;  
            // send KNOWWE request as SingleFindingAction with given value
            KNOWWE.plugin.quicki.send(rel.web, rel.ns, rel.oid, rel.qtext, 
            		{action: 'SetSingleFindingAction', ValueText: inputtext});
        },
        /**
         * Function: toggleImage
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
         * 		retract - flag that tells whether question is to be set or retracted
         */
        toggleAnswerHighlighting : function( answerEl, type, retract ){
        	
        	if(answerEl.className.toLowerCase() == "answerunknownclicked") return;
        	if(answerEl.className.toLowerCase() == "answerunknown") return;               
        	
        	var relClicked = eval("(" +  answerEl.getAttribute('rel') + ")");        	
        	
        	// if clicked q is a oc q, already clicked answer alternatives need to be de-highlighted
        	if(type=="oc"){
        		_KS('.answerClicked').each(function(element){
    				var relElement =  eval("(" +  element.getAttribute('rel') + ")");
    				if (relElement.qid==relClicked.qid){
    					element.className = 'answer';
    				}
            	});
        		
        		// if a oc question is clicked, also answer unknown needs to be reset
        		_KS('.answerunknownClicked').each(function(element){
    				var relElement =  eval("(" +  element.getAttribute('rel') + ")");
    				if (relElement.qid==relClicked.qid){
    					element.className = 'answerunknown';
    				}
            	});
        		
        		// all oc answers are now per default un-highlighted by above code
        		// thus if un-highlighting is not correct, highlight again here
        		if(!retract){
        			answerEl.className = 'answerClicked';
        		}
        	} 
        	
        	// otherwise just toggle highlighting 
        	else {
        		// to highlight/unhighlight an answer if clicked on, generally
            	if(answerEl.className=='answerClicked'){
            		answerEl.className = 'answer';
            	} else if (answerEl.className=='answer'){
            		answerEl.className = 'answerClicked';
            	} 
        	}
        }, 
        /**
         * 	Function toggleAnswerHighlightingAfterUnknown
         * 		process the correct highlighting of answer elements after unknown is clicked
         *		i.e., remove highlighting 
         *
         *	Parameters:
         *		questionID - id of the question that was clicked with unknown
         */
        toggleAnswerHighlightingAfterUnknown : function( questionID){

        	_KS('.answerClicked').each(function(element){
        		
    			var relElement =  eval("(" +  element.getAttribute('rel') + ")");
    			if (relElement.qid==questionID){
    					
    				if (element.className!="answerunknownClicked"){
    					element.className="answer";
    				}
    			}
            });
        }, 
        /**
         * Function: updateQuestionnaireVisibility 
         * Toggles the visibility of questionnaire-contents on click:
         * 		visible ones are hidden, hidden ones are displayed
         * 
         * Parameters: 
         *  	event - The fired the click event  
         */
        toggleQuestionnaireVisibility : function( event ){
        	      
        	// get the clicked element, i.e., the questionnaire
        	var questionnaire = _KE.target(event); 	
        	var group = _KS('#group_' + questionnaire.id);
        	
        	if(group.style.display=='block'){ 
        		
            	group.style.display = 'none';    
            	var replacer = questionnaire.id + ';0###';
            	var containsIndex = questionnaireVis.indexOf(questionnaire.id);
            		
            	if(containsIndex!=-1){
            		var toreplace = questionnaireVis.substring(containsIndex, 
                		containsIndex + questionnaire.id.length + 5);
                	questionnaireVis = questionnaireVis.replace(toreplace, replacer);
                }            		
            	else {
            		questionnaireVis = questionnaireVis + replacer;
            	}
            
            	KNOWWE.plugin.quicki.toggleImage(1, questionnaire);       
            	
            } else if (group.style.display=='none'){   
          		
            	group.style.display = 'block';	
          		
            	var replacer = questionnaire.id + ';1###';
            	var containsIndex = questionnaireVis.indexOf(questionnaire.id);
            		
            	if(containsIndex!=-1){
            		var toreplace = questionnaireVis.substring(containsIndex, 
                		containsIndex + questionnaire.id.length + 5);
                	questionnaireVis = questionnaireVis.replace(toreplace, replacer);
                }            		
            	else {
            		questionnaireVis = questionnaireVis + replacer;
            	}
            	
            	KNOWWE.plugin.quicki.toggleImage(0, questionnaire);   
            } 
        	KNOWWE.plugin.quicki.showRefreshed;
        },
        /**
         * TODO
         * 
         * Function: toggleQuestionVisibility 
         * Toggles the visibility of question-contents on click:
         * 		visible ones are hidden, hidden ones are displayed
         * 
         * Parameters: 
         *  	event - The fired the click event  
         */
        toggleQuestionVisibility : function( event ){
        	      
        	// get the clicked element, i.e., the questionnaire
        	/*var question = _KE.target(event); 	
        	var group = _KS('#group_' + question.id);
        	
        	if(group.style.display=='block'){
            	group.style.display = 'none';   	
            } else if (group.style.display=='none'){     
            	group.style.display = 'block';
            } 
          	KNOWWE.plugin.quicki.showRefreshed;*/
        },
        /**
         * Function quickIReset 
         * 		Reset the whole interview, i.e., call reset action and
         * 		clear highlighting and fields
         *  
         *  Parameters:
         *  	event: the triggering event
         */
        quickIReset : function ( event ) {
        	
        	var resetBttn = _KE.target(event); 	// get the clicked element 
            var rel = eval("(" + resetBttn.getAttribute('rel') + ")");
            mcanswervals = '';
        	
            // call reset action with necessary parameters
        	var params = {
                    action : 'QuickInterviewResetAction',
                    KWikiWeb : rel.web,
                    namespace : rel.ns,
            }
        	
        	// insert the String/HTML returned by the QuickIntervewResetAction at
        	// the div with id "auickinterview"
        	var id = 'quickinterview';
            var options = {
        		url : KNOWWE.core.util.getURL( params ),
        		response : {
        			action : 'insert',
                    ids : [ id ],					// to re-insert a freshly created interview
                    fn : function(){
			        	try {
	                    	KNOWWE.plugin.quicki.initialize();
                    		KNOWWE.core.rerendercontent.update(); //Clear new SolutionPanel
			        	}
			        	catch (e) { /*ignore*/ }
			        	KNOWWE.core.util.updateProcessingState(-1);
                    },
                    onError : function () {
			        	KNOWWE.core.util.updateProcessingState(-1);                    	
                    }
        		}
        	}
        	KNOWWE.core.util.updateProcessingState(1);
            new _KA( options ).send();
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
                TermName : termName
            }
            
            pDefault = KNOWWE.helper.enrich( params, pDefault );
             
            var options = {
                url : KNOWWE.core.util.getURL( pDefault ),
                response : {
                	action : 'none',
                	fn : function(){
			        	try {
	                		KNOWWE.helper.observer.notify('update');
			        	}
			        	catch (e) { /*ignore*/ }
			        	KNOWWE.core.util.updateProcessingState(-1);
                	},
                    onError : function () {
			        	KNOWWE.core.util.updateProcessingState(-1);                    	
                    }
                }
            }
        	KNOWWE.core.util.updateProcessingState(1);
            new _KA( options ).send();         
        },
        /**
         * Function: showRefreshed
         * 		send the request and render the interview newly via
         * 		QuickInterviewAction 
         */
        showRefreshed : function ( ){
        	
        	// needed to avoid endless calls in case quicki is reloaded
        	// due to a session clearing from solution panel
        	if(!_KS('#quickinterview'))
        		return;
        	
        	 var params = {
                     namespace : KNOWWE.helper.gup( 'page' ),
                     action : 'QuickInterviewAction'
             }
        	 
        	 var id = 'quickinterview';
             var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                	action : 'insert',
                    ids : [ id ],	
                	fn : function(){
			        	try {
	                		KNOWWE.plugin.quicki.initialize();
			        	}
			        	catch (e) { /*ignore*/ }
			        	KNOWWE.core.util.updateProcessingState(-1);
                	},
                    onError : function () {
			        	KNOWWE.core.util.updateProcessingState(-1);                    	
                    }
                }
        	 }
        	 KNOWWE.core.util.updateProcessingState(1);
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
        	KNOWWE.plugin.quicki.initialize();
        	KNOWWE.helper.observer.subscribe( 'update', KNOWWE.plugin.quicki.showRefreshed);
        });
    }
}());