/**
 * Title: KnowWE-Plugin-Semantic
 * Contains all javascript functions concerning the KnowWE-Plugin-SemanticCore.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    /**
     * The KNOWWE global namespace object.  If KNOWWE is already defined, the
     * existing KNOWWE object will not be overwritten so that defined
     * namespaces are preserved.
     */
    var KNOWWE = {};
}
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
 /**
     * The KNOWWE.plugin global namespace object. If KNOWWE.plugin is already defined, the
     * existing KNOWWE.plugin object will not be overwritten so that defined namespaces
     * are preserved.
     */
    KNOWWE.plugin = function(){
         return {  }
    }
}
/**
 * Namespace: KNOWWE.plugin.semantic
 * The KNOWWE plugin d3web namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin.semantic = function(){
    return { }
}();


/**
 * Namespace: KNOWWE.plugin.semantic
 * The namespace of the semantic things in KNOWWE.
 */
KNOWWE.plugin.semantic = function(){
    /**
     * Variable: sTimer
     * Stores an timer object. Used to remove the overlay question element after
     * a certain amount of time with no user action.
     * 
     * Type:
     *     Object
     */
    var sTimer = undefined;
    
    /**
     * Indicates if the current visible question is a multiple choice question.
     * if so the popup stays visible until the user closes it or moves the mouse
     * cursor out of the question overlay.
     */
    var isMC = false;
    
    /**
     * Stores the request URL of the MC questions.
     */
    var mcUrl = undefined;
    
    /**
     * 
     */
    function handleMC(){
        var mcStorage = new Array();
        _KS('.semano_mc').each(function(element){
            if( element.checked ){
                var rel = eval( "(" + element.getAttribute('rel') + ")");
                mcStorage.push( rel.ValueIDS );
            }
        });
        mcUrl.ValueIDS = mcStorage.join(',');
        KNOWWE.plugin.semantic.send( mcUrl, null );
        mcUrl = false;
        isMC = false;
    }
    
    return {
        /**
         * Function: init
         * Initializes the semantic popups.
         * Adds to every question of the questionsheet a popup action.
         */
        init : function(){
            if(_KS('.semLink').length != 0){
                _KS('.semLink').each(function(element) {
                    _KE.add('click', element, KNOWWE.plugin.semantic.showOverlayQuestion);
                });
            }
        },
        /**
         * Function: overlayActions
         * Contains all actions that can occur in an question overlay element.
         * Used to initialize the actions after the overlay is created.
         */
        overlayActions : function(){
            if(_KS('.semano_mc').length != 0){
                _KS('.semano_mc').each(function(element){
                    _KE.add('click', element, KNOWWE.plugin.semantic.handleForm);
                });
            }

            if(_KS('.semano_oc').length != 0){
                _KS('.semano_oc').each(function(element){
                    _KE.add('click', element, KNOWWE.plugin.semantic.handleOC);
                });         
            }
            if(_KS('.semano_num').length != 0){
                _KS('.semano_num').each(function(element){
                    _KE.add('keydown', element, KNOWWE.plugin.semantic.handleNum);
                });
                 _KS('.semano_ok').each(function(element){
                    _KE.add('click', element, KNOWWE.plugin.semantic.handleNum);
                });
            }
            if(_KS('#o-lay')){
                _KE.add('mouseout', _KS('#o-lay'), function(e){             
                    var father = _KE.target(e);
                    var e = father;
                    var id = e.getAttribute('id');
                    while(id != 'o-lay' ){
                        e = e.parentNode;
                        id = e.getAttribute('id');
                    }
                    if(e.getAttribute('id') == 'o-lay' || father == e){
                        clearTimeout( sTimer );
                        sTimer = setTimeout(function(){
                            if(isMC){
                                handleMC();
                            }
                            _KS('#o-lay')._remove();
                        }, 4000);
                    }
                });
                _KE.add('click', _KS('#o-lay-close'), function(){
                    if(isMC){
                        handleMC();
                    }
                    _KS('#o-lay')._remove();
                    clearTimeout( sTimer );
                });
            }
        },
        /**
         * Function: handleForm
         * Handles the selection of checkboxes.
         * 
         * Parameters:
         *     e - The current occurred event.
         */
        handleForm : function(e){
            var el = new _KN(_KE.target(e));
            var rel = eval( "(" + el.getAttribute('rel') + ")");
            
            isMC = true;
            mcUrl = rel;
        },
        /**
         * Function: handleNum
         * Handles the input in an HTMLInput element
         * 
         * Parameters:
         *      e - The current occurred event
         */
        handleNum : function(e){            
            var bttn = (_KE.target( e ).value == 'ok');
            var key = (e.keyCode == 13);
            if( !( key || bttn ) ) return false;
            
            var rel = null, el = null;
            el = new _KN(_KE.target(e));
            if( el.value=="ok" ){
                el = el.previousSibling;
            }
            rel = eval("(" + el.getAttribute('rel') + ")");

            if( !rel ) return;            
            KNOWWE.plugin.semantic.send( rel, {ValueNum : el.value} );
        },
        /**
         * Function: handleOC
         * Handles the selection within an one choice question.
         * 
         * Parameters:
         *     e - The current occurred event
         */
        handleOC : function(e){
            var el = _KE.target(e);
            var rel = eval( "(" + el.getAttribute('rel') + ")");
            
            KNOWWE.plugin.semantic.send( rel, null );
        },
        /**
         * Function: send
         * Sends the user selection and stores it. Used in the other handleXXX
         * functions to send an AJAX request in order to store the users choice.
         *  
         * Parameters:
         *     url - The URL of the request
         *     values - The selected value
         */
        send : function( url, values ){
            var tokens = ['action=SetFindingAction'];
            for( keys in url ){
                if(keys == 'url') continue;
                tokens.push(keys + "=" + encodeURIComponent( url[keys] ));
            }
            
            if(values) {
                for( keys in values ){
                    tokens.push(keys + "=" + encodeURIComponent( values[keys] ));
                }
            }
            var options = {
                url : url.url + "?" + tokens.join('&'),
                response : {
                    action: 'none',
                    fn : function(){KNOWWE.helper.observer.notify('update')}
                }
            }
            new _KA( options ).send();
            
            if(!isMC){
                _KS('#o-lay')._remove();
                clearTimeout(sTimer);
            }
        },
        /**
         * Function: showOverlayQuestion
         * Gets the data that is shown as an overlay over the current question.
         * 
         * Parameters:
         *     e - The latest event object
         */
        showOverlayQuestion : function(e){
            
            if(isMC){
                clearTimeout( sTimer );
                handleMC();
                _KS('#o-lay')._remove();
            }
            
            var el = _KE.target( e );
            if(!el.getAttribute('rel')) return;
            
            var rel = eval( "(" + el.getAttribute('rel') + ")");
            
            var params = {
                action : 'SemanticAnnotationAction',
                namespace : KNOWWE.helper.gup( 'page' )+'..'+KNOWWE.helper.gup( 'page' )+'_KB',
                ObjectID : rel.objectID,
                TermName : rel.termName,
                KWikiWeb : 'default_web',
                TermType : 'symptom',
                KWikiUser : rel.user,
                sendToUrl : 'KnowWE.jsp'
            }
            var mousePos = KNOWWE.helper.mouseCoords( e );
            var mouseOffset = KNOWWE.helper.getMouseOffset( el, e );
            
            var olay = new KNOWWE.helper.overlay({
                title : 'Interview',
                cursor : {
                    top : mousePos.y - mouseOffset.y,
                    left : mousePos.x - mouseOffset.x
                },
                url : KNOWWE.core.util.getURL( params ),
                fn : KNOWWE.plugin.semantic.overlayActions
             });
        }
    }
}();

(function init(){ 
    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
            
            var ns = KNOWWE.plugin.semantic;
            for(var i in ns){
                if(ns[i].init){
                    ns[i].init();
                }
            }
            
        });
    }
}());