/**
 * Namespace: KNOWWE.plugin.d3web.solutionstate
 * The solutionstate namespace.
 */
KNOWWE.plugin.d3web.solutionstate = function(){
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
            if(!_KS('#sstate-result')) return;
            
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

            var id = 'sstate-result';
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'insert',
                    ids : [ id ],
                    fn : function(){
                        if(_KS('.sstate-show-explanation').length != 0){
                            _KS('.sstate-show-explanation').each(function(element){
                                _KE.add('click', element, KNOWWE.plugin.d3web.solutionstate.showExplanation);
                            });
                        }
                        if(_KS('.show-solutions-log').length != 0){
                            _KS('.show-solutions-log').each(function(element){
                                _KE.add('click', element, KNOWWE.plugin.d3web.solutionstate.showSolutionLog);
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
            var id = 'sstate-result';
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'insert',
                    ids : [ id ]
                }
            }
            if( _KS('#sstate-result') ) {
                new _KA( options ).send();
                KNOWWE.core.rerendercontent.update();
                KNOWWE.plugin.d3web.rerenderquestionsheet.update();
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