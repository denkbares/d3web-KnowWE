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
    
    /**
     * Variable: questionnaireStates
     * Stores the state of visibility of each questionnaireStates in the dialog.
     * 
     * Type:
     *     String
     */
    var questionnaireStates = '';
    
    /**
     * Decodes the status information into a valid CSS attribute name.
     */
    function decodeStatus( status ){
        if(status == 1)
            return "visible";
        else if(status == 0)
            return "hidden";
    }
    /**
     * Toggles an image. The cases for questionnaires and follow-up
     * questions have to be distinguished as they need to be assigned different
     * css classes
     */
    function toogleImage( node , state ){
        
        // in case 'node' is displayed hidden
        if(state == "hidden"){              
            // and is a qcontainer
            if(node.className.substring(0,14)=='qcontainerName'){
                node.className ='qcontainerName pointer extend-htmlpanel-right';
            // or is a follow up question
            } else if(node.className.substring(0,6)=='follow')  {
                node.className ='follow pointer extend-htmlpanel-right-s';
            }
            
        // in case 'node' is displayed visible
        } else {
            // and is a qcontainer
            if(node.className.substring(0,14)=='qcontainerName'){
                node.className ='qcontainerName pointer extend-htmlpanel-down';
            // or is a follow up question
            } else if (node.className.substring(0,6)=='follow'){
                node.className ='follow  pointer extend-htmlpanel-down-s';
            }
        }
    } 
    /**
     * Finds the parent qcontainer if exists.
     */
    function findQContainer( node ){
        node = new _KN(node);
        while(!node._hasClass('qcontainer')){
            node = new _KN(node.parentNode);
        }
        return node;
    }
    
    return {
        /**
         * Function: init
         */
        init : function(){
               // here was the code for displaying the more-menu button - still needed?
        },
        test : function{
        	alert("hey");
        }
        /**
         * Function: initAction
         * Adds some events to the interview. Without this, it does not 
         * respond to any user action.
         */
        initAction : function(){
            _KS('.containerHeader').each(function(element){
                _KE.add('click', element, KNOWWE.plugin.quicki.test()));
                //var p = element.parentNode;
                //var tbl = _KS('#tbl'+p.id, p);
                //_KE.add('click', tbl, KNOWWE.plugin.d3web.dialog.answerClicked);                
            });
            
            if(_KS('#xcl-save-as')){
                _KE.add('click', _KS('#xcl-save-as'), KNOWWE.plugin.quicki.saveAsXCL);
            }
         
         //   _KS('.num-cell-down').each(function( element ){
         //       _KE.add('keydown', element, KNOWWE.plugin.d3web.dialog.numInput);
         //   });
         //   _KS('.num-cell-ok').each(function( element ){
         //       _KE.add('click', element, KNOWWE.plugin.d3web.dialog.numInput);
         //   });         
        },
        /**
         * Function: insert
         * Inserts the HTMLDialog into an article page. Also changes the HTMLDialog
         * menu button to remove the HTMLDialog when necessary.
         * 
         * Parameters:
         *     event - The event from the page actions top menu.
         */
        insert : function( event ){
            var params = {
                namespace : KNOWWE.helper.gup( 'page' ),
                action : 'QuickInterviewAction'
            }
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'create',
                    fn : function() {    
                        var el = _KS('a .dialog')[0];
                        el.innerHTML = bttn_name; 
                        
                        //_KE.removeEvents(_KS('#bttn-dialog'));
                        //_KE.add('click', _KS('#bttn-dialog'), KNOWWE.plugin.d3web.dialog.remove);
                        KNOWWE.plugin.quicki.initAction();
                    }
                },
                create : {
                    id : 'pagecontent',
                    fn : function(){
                        return new _KN('div', {'id' : 'quickinterview'});
                    }
                }
            }
            new _KA( options ).send();
        },
        /**
         * Function: getQContainerVisibilityStates
         * Stores the display status of each qcontainer element of the HTMLDialog.
         * Used to restore the view after a refresh occurred.
         */    
        getQContainerVisibilityStates : function(){
            var states = '';
            var tables = _KS('#dialog table');
                            
            for(var i = 0; i < tables.length; i++){
                var qContainerId = tables[i].parentNode.id;
                   
                if(tables[i].className == 'hidden'){
                    states = states.concat(0 + qContainerId);
                } else if (tables[i].className == 'visible') {
                    states = states.concat(1 + qContainerId);
                }
                states = states.concat(';');
            }
          
            return states;
        },
        /**
         * Function: getFollowUpRowsVisibilityStates
         * Stores the display status of each qcontainer element of the HTMLDialog.
         * Used to restore the view after a refresh occurred.
         */    
        getFollowUpRowsVisibilityStates : function(){
     
            var states = '';
            var trs = _KS('#dialog tr');
                          
            for(var i = 0; i < trs.length; i++){                
                var trid = trs[i].id;
                   
                if(trs[i].className == 'trf hidden'){
                    states = states.concat(0 + trid);;
                } else if(trs[i].className == 'trf') {
                    states = states.concat(1 + trid);
                }
                states = states.concat(';');
            }
       
            return states;
        },
        /**
         * Function: showElement
         * Sets an element of the HTMLDialog visible to the user.
         * 
         * Parameters:
         *     event - The qContainer click event.
         */
        showElement : function( event ){
            if(qContainerStates == ''){
                qContainerStates = KNOWWE.plugin.d3web.dialog.getQContainerVisibilityStates();
            }
            if(followUpStates == ''){
                followUpStates = KNOWWE.plugin.d3web.dialog.getFollowUpRowsVisibilityStates();
            }
            
            var el = _KE.target(event).parentNode;
            // questionnaire was clicked and should "react" accordingly
            if(el.className == 'qcontainer'){
                var id = el.id;
                var clazz = (el.className == 'qcontainer');
                if( !(id && clazz) ) return;
                
                // get table element with id of the clicked element
                var tbl = _KS('#' + id + ' table')[0];
                // get the clicked element which is stored in a h4 element
                var h4 = _KS('#' + id + ' h4')[0];
            
                if(tbl.className == 'visible'){     // if it was visible before
                    var bef = '1'+id+';';
                    var aft = '0'+id+';';
                    var test = qContainerStates.replace(bef, aft);
                    tbl.className = 'hidden';       // it should be hidden now
                    qContainerStates = test;
                } else if(tbl.className == 'hidden'){    
                    var bef = '0'+id+';';
                    var aft = '1'+id+';';
                    var test = qContainerStates.replace(bef, aft);
                    qContainerStates = test;
                    tbl.className = 'visible';      // it should be visible now
                }
                
                // display the right image for the questionnaire
                toogleImage( _KE.target(event) , tbl.className ); 
            }
            
            // if not questionnaire case, check if it's the follow-up extension case
            else {
                var id;
                var par;
                if(_KE.target(event).parentNode.className.substring(0,6)=='follow'){
                    id = _KE.target(event).parentNode.id;
                    par = true;
                } 
                if(_KE.target(event).className.substring(0,6)=='follow') {
                    id = _KE.target(event).id;
                    par = false;
                }
                
                // fetch all tr elements of the interview, first
                var trs = _KS('#dialog tr');
                // get the target id = id of the clicked element that is the root of 
                // the follow up elements
                var state;      
                
                for(var i = 0; i < trs.length; i++ ){               
                    if(trs[i].className == 'trf'){
                        var idTest = trs[i].id;
                        // follow up row must be hidden, if it was previously shown and
                        // if the clicked element is the root of the f-u --> which
                        // is the case if both have the same id
                        if(id == idTest){  
                            trs[i].className = 'trf hidden'; 
                            var bef = '1'+id+';';
                            var aft = '0'+id+';';
                            var test = followUpStates.replace(bef, aft);
                            followUpStates = test;  
                            state = 'hidden';
                        }
                    } else if(trs[i].className == 'trf hidden') {    
                        var idTest = trs[i].id; 
                        // follow up row must be shown, if it was previously hidden and
                        // if the clicked element is the root of the f-u --> which
                        // is the case if both have the same id
                        if(id == idTest){  
                            trs[i].className = 'trf'; 
                            var bef = '0'+id+';';
                            var aft = '1'+id+';';
                            var test = followUpStates.replace(bef, aft);
                            followUpStates = test;  
                            state = '';
                        }
                    }                
                } 
                
                var clickedQues;
                if(par){
                    // get the clicked question, i.e., the "root" of the follow up questions
                    clickedQues = _KE.target(event).parentNode;
                } else if (!par){
                    // get the clicked question, i.e., the "root" of the follow up questions
                    clickedQues = _KE.target(event);
                }
                
                // adapt image display for the root of the follow up questions
                toogleImage( clickedQues, state ); 
            }
        }, 
        /**
         * Function: refreshed
         * Shows the interview after a page refresh. Uses the questionnaireStates
         * variable to determine the state of the elements. The state is recovered
         * according to this.
         */
        refreshed : function(){
       
            var qs = questionnaireStates.split(';');
            var h4s = _KS('#dialog h4');
            var tables = _KS('#dialog table');
                
            for( var i = 0; i < qs.length-1; i++ ){
            
                var s = qs[i].substring(0,1);
                var id = qs[i].substring(1);
                var state = decodeStatus( s );
                
                var h4 = _KS('#' + id + ' h4')[0];
                toogleImage(h4, state);
               
                var tbl = _KS('#' + id + ' table')[0];
                tbl.className = state;
            }
            
            KNOWWE.plugin.quicki.initAction();
            KNOWWE.helper.observer.notify('update');
        },
        /**
         * Function: send
         * Stores the user input as single finding through an AJAX request.
         * 
         * Parameters:
         *     web - 
         *     namespace The name of the article
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
                    action: 'insert',
                    ids : ['quickinterview'],
                    fn : function(){
                        KNOWWE.plugin.d3web.dialog.refreshed();
                    }
                }
            }
                      
            new _KA( options ).send();         
        },
        /**
         * Function: saveAsXCL
         * Stores the selected findings in the HTMLDialog as an XCLRealtion.
         * Used as a simple XCLRelation editor.
         * @Depreacted
         */
        saveAsXCL : function(){
            var params = {
                action : 'SaveDialogAsXCLAction',
                KWiki_Topic : KNOWWE.helper.gup('page'),
                XCLSolution : _KS('#xcl-solution').value
            }
            
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    ids : [ ],
                    fn : window.location.reload
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
        	//KNOWWE.plugin.quicki.init();
        	KNOWWE.plugin.quicki.initAction();
        });
    }
}());