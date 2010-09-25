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
 * The KNOWWE.plugin.solutionpanel global namespace object. If KNOWWE.plugin.solutionpanel is already defined, the
 * existing KNOWWE.plugin.solutionpanel object will not be overwritten so that defined namespaces
 * are preserved.
 */
KNOWWE.plugin.solutionpanel = function(){
    return {
    }
}();

/**
 * Namespace: KNOWWE.plugin.solutionpanel
 * The solutionpanel namespace.
 */
KNOWWE.plugin.solutionpanel = function(){
    return {
        /**
         * Function: init
         * Initializes the solutionstate functionality. Adds to the buttons the
         * correct action.
         */
        init : function(){
        	var el = _KS('#sstate-update');
            if( el ){
                _KE.add('click', el, this.updateSolutionstate);
            }
            
            el = _KS('#sstate-findings');
            if( el ){
                _KE.add('click', el, this.showFindings);
            }
            
            el = _KS('#sstate-clear'); 
            if( el ){
                _KE.add('click', el, this.clearSolutionstate);
            }
            KNOWWE.helper.observer.subscribe( 'update', this.updateSolutionstate );
        },
        /**
         * Function: updateSolutionstate
         * Updates the solutions in the solutionstate panel.
         */
        updateSolutionstate : function(){
            if(!_KS('#solutionPanelResults')) return;
            
            var params = {
                action : 'SolutionsPanelAction',
                KWikiWeb : 'default_web',
                ArticleSelection : (function () {
                    var box = document.getElementById('sdropdownbox');
                    
                    if(box.selectedIndex){
                        return box.selectedIndex;
                    }
                    return "";
                })()
            }

            var id = 'solutionPanelResults';
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'insert',
                    ids : [ id ],
                    fn : function(){
                        if(_KS('.sstate-show-explanation').length != 0){
                            _KS('.sstate-show-explanation').each(function(element){
                                _KE.add('click', element, KNOWWE.plugin.solutionpanel.showExplanation);
                            });
                        }
                        if(_KS('.show-solutions-log').length != 0){
                            _KS('.show-solutions-log').each(function(element){
                                _KE.add('click', element, KNOWWE.plugin.solutionpanel.showSolutionLog);
                            });
                        }
                    }
                }
            }
            new _KA( options ).send(); 
        },
        /**
         * Function: clearSolutionstate
         * Deletes the established solutions in the solutionstate panel.
         */
        clearSolutionstate : function(){
            var params = {
                action : 'ClearDPSSessionAction',
                KWikiWeb : 'default_web'
            }
            var id = 'solutionPanelResults';
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'insert',
                    ids : [ id ]
                }
            }
            if( _KS('#solutionPanelResults') ) {
                new _KA( options ).send();
                KNOWWE.core.rerendercontent.update();
                KNOWWE.plugin.d3web.rerenderquestionsheet.update();
                KNOWWE.plugin.quicki.showRefreshed();
                OneQuestionDialog.showRefreshed();
            }   
        },
        /**
         * Function: showFindings
         * Shows the findings in an extra popup window.
         */
        showFindings : function( event ){
            var params = {
                action : 'UserFindingsAction',
                KWikiWeb : 'default_web'
            }
            
            event = new Event( event );
            KNOWWE.helper.window.open({
                url : KNOWWE.core.util.getURL(params),
                left : event.page.x, 
                top : event.page.y,
                screenX : event.client.x,
                screenY : event.client.y
            });
        },
        /**
         * Functions: showExplanation
         * Shows detail information about an established solution.
         */
        showExplanation : function( event ){
            var rel = eval("(" + _KE.target( event ).parentNode.getAttribute('rel') + ")");
            if( !rel ) return false;
            
            var params = {
                action : 'XCLExplanationAction',
                KWikiTerm : rel.term,
                KWikisessionid : rel.session, 
                KWikiWeb : rel.web,
                KWikiUser : rel.user
            }
            
            event = new Event( event );
            KNOWWE.helper.window.open({
                url : KNOWWE.core.util.getURL(params),
                left : event.page.x, 
                top : event.page.y,
                screenX : event.client.x,
                screenY : event.client.y
            });
            return false;
        },
        /**
         * Functions: showSolutionLog
         * Shows detail information about ...
         */
        showSolutionLog : function( event ){
            var rel = eval("(" + _KE.target( event ).parentNode.getAttribute('rel') + ")");
            if( !rel ) return false;
            
            var params = {
                action : 'SolutionLogAction',
                KWikiTerm : rel.term,
                KWikiWeb : rel.web,
                KWikiUser : rel.user
            }
            
            event = new Event( event );
            KNOWWE.helper.window.open({
                url : KNOWWE.core.util.getURL(params),
                left : event.page.x, 
                top : event.page.y,
                screenX : event.client.x,
                screenY : event.client.y
            });
            return false;
        }        
    }   
}();

/**
 * Initializes the required JS functionality when DOM is readily loaded
 */
(function init(){ 
    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
        	KNOWWE.plugin.solutionpanel.init();
        });
    }
}());