/**
 * Title: KnowWE-plugin-d3web
 * Contains all javascript functions concerning the KnowWE plugin d3web.
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
 * Namespace: KNOWWE.plugin.d3web
 * The KNOWWE plugin d3web namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin.d3web = function(){
    return {
    }
}();

/**
 * Namespace: KNOWWE.plugin.d3web.actions
 * some core actions of the D3Web plugin for KNOWWE.
 */
KNOWWE.plugin.d3web.actions = function(){
    return {
        /**
         * Function: init
         * Some function that are executed on page load. They initialize some core
         * d3web plugin functionality.
         */ 
        init : function(){           
            //init KnowledgeBasesGenerator
            if(_KS('#KnowledgeBasesGenerator')) {
                var els = _KS('#KnowledgeBasesGenerator input[type=button]');
                for (var i = 0; i < els.length; i++){
                    _KE.add('click', els[i], KNOWWE.plugin.d3web.actions.doKbGenerating); 
                }
            }
            //add generate kb button action
            if(_KS('.generate-kb').length != 0){
                _KS('.generate-kb').each(function( element ){
                    _KE.add('click', element, KNOWWE.plugin.d3web.actions.doKbGenerating);
                });
            }
            //add highlight xcl
            if(_KS('.highlight-xcl-relation').length != 0){
                _KS('.highlight-xcl-relation').each(function( element ){
                    _KE.add('click', element, KNOWWE.plugin.d3web.actions.highlightXCLRelation);
                });
            }  
            //add highlight rule
            if(_KS('.highlight-rule').length != 0){
                _KS('.highlight-rule').each(function( element ){
                    _KE.add('click', element, KNOWWE.plugin.d3web.actions.highlightXCLRelation);
                });
            } 
        },
        /**
         * Function: doKbGenerating
         * Adds a specified knowledgebase in jar format.
         * 
         * Parameters:
         *     event - The event from the create knowledgebase button. 
         */
        doKbGenerating : function( event ) {
            var jarfile = eval( "(" + _KE.target(event).getAttribute('rel') + ")").jar;

            var params = {
                action : 'GenerateKBAction',
                NewKBName : _KS('#' + jarfile ).value,
                AttachmentName : jarfile
            }
            
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    ids : ['GeneratingInfo']
                }
            }
            new _KA( options ).send();
        },
        /**
         * Function: doTiRexToXCL
         * 
         * Parameters:
         *     topicname - The name of the page
         */
        doTiRexToXCL : function(topicname) {
            var params = {
                action : 'TirexToXCLRenderer',
                TopicForXCL : topicname
            }
        
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    ids : ['GeneratingTiRexToXCLInfo']
                }
            }
            new _KA( options ).send();
        },         
        /**
         * Function: highlightXCLRelation
         * 
         * Parameters:
         *     event - The event that was triggered from a DOM element.
         */
        highlightXCLRelation : function( event ) {
            var rel = eval( "(" + _KE.target(event).getAttribute('rel') + ")");
            if( !rel ) return;
            
            // Restore the Highlighting that was before
            var restore = document.getElementById('uniqueMarker');
            if (restore) {
                KNOWWE.core.rerendercontent.updateNode(restore.className, rel.topic, null);
            }
            KNOWWE.plugin.d3web.actions.highlightNode(rel.kdomid, rel.topic, rel.depth, rel.breadth, event);
        },
        /**
         * Function: highlightRule
         * 
         * Parameters:
         *     event - The event that was triggered from a DOM element.
         */
        highlightRule : function( event ) {
            var rel = eval( "(" + _KE.target(event).getAttribute('rel') + ")");
            if( !rel ) return;
            
            // Restore the Highlighting that was before
            var restore = document.getElementById('uniqueMarker');
            if (restore) {
                KNOWWE.core.rerendercontent.updateNode(restore.className, rel.topic, null);
            }
            KNOWWE.plugin.d3web.actions.highlightNode(rel.kdomid, rel.topic, rel.depth, rel.breadth, event);
        },
        /**
         * Function: highlightNode
         * Highlights an DOM node in the current wiki page.
         * You need a span with an id to use this. There the uniqueMarker is located.
         * *Note:* if a node has more than 1 element this function.
         * Will not work because it cannot foresee how the html-tree is build.
         * 
         * See Also:
         *     <highlighXCLRelation>
         * 
         * Parameters:
         *     node - is the tag that has the marker under it
         *     topic - 
         *     depth - means the tag that has the marker as firstchild.
         *     breadth -
         *     event - Used to stop event bubbling.
         */
        highlightNode : function(node, topic, depth, breadth, event){
            var event = new Event(event);
            event.stopPropagation();
             
            var params = {
                action : 'HighlightNodeAction',
                Kwiki_Topic : topic,
                KWikiJumpId : node
            }
            
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'update',
                    ids : [],
                    fn : function() {                       
                        // set the new Marker: Get the root node
                        var element = document.getElementById(node);
                          
                        // get to the depth given.
                        for (var i = 0; i < depth; i++) {
                            element = element.firstChild;
                        }
                            
                        // get to the given breadth
                        for (var j = 0; j < breadth; j++) {                       
                            if (element.nextSibling)
                                element = element.nextSibling;
                        }
                                                  
                        if (element) {
                            element.firstChild.style.backgroundColor = "yellow";
                            element.firstChild.id = "uniqueMarker";
                            element.firstChild.className = node;
                            element.scrollIntoView(true);
                        }
                    }
                }
            }
            new _KA( options ).send();
        }
    }
}();


/**
 * Namespace: KNOWWE.plugin.d3web.dialog
 * The KNOWWE HTMLDialog plugin namespace.
 */
KNOWWE.plugin.d3web.dialog = function(){
    /**
     * Variable: bttn_name
     * The name of the add/remove dialog button in the page actions tops menue.
     * 
     * Type:
     *     String
     */
    var bttn_name = "Interview";
    
    /**
     * Variable: qContainerStates
     * Stores the state of visibility of each qcontainer in the dialog.
     * 
     * Type:
     *     String
     */
    var qContainerStates = '';
    var followUpStates = '';
    
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
     * Toggles an image. The cases for qcontainer (questionnaires) and follow-up
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
         * Creates an button in the top actions menu in the WIKI. This button
         * is use to insert an HTMLDialog into the current article.
         */
        init : function(){
            if( !_KS('#actionsTop') ) return;
            var li = new _KN('li', {'id': 'moreKnowWE'});
            var a = new _KN('a', {'id': 'bttn-dialog','href' : '#', 'class' : 'action dialog'});
            a._setText( bttn_name );
            li.appendChild(a);
            _KS('#actionsTop ul')[0].appendChild(li);
            _KE.add('click', _KS('#bttn-dialog'), KNOWWE.plugin.d3web.dialog.insert);        
        },
        /**
         * Function: initAction
         * Adds some events to the HTMLDialog. Without this the Dialog does not 
         * respond to any user action.
         */
        initAction : function(){
            _KS('.qcontainerName').each(function(element){
                _KE.add('click', element, KNOWWE.plugin.d3web.dialog.showElement);
                var p = element.parentNode;
                var tbl = _KS('#tbl'+p.id, p);
                _KE.add('click', tbl, KNOWWE.plugin.d3web.dialog.answerClicked);                
            });
               
            _KS('.follow').each(function(element){
                _KE.add('click', element, KNOWWE.plugin.d3web.dialog.showElement);
               // var p = element.parentNode;
               // var tbl = _KS('#tbl'+p.id, p);
               // _KE.add('click', tbl, KNOWWE.plugin.d3web.dialog.answerClicked);                
            });
            
            if(_KS('#xcl-save-as')){
                _KE.add('click', _KS('#xcl-save-as'), KNOWWE.plugin.d3web.dialog.saveAsXCL);
            }
                        
            /*
             _KS('span .fieldcell').each(function( element ){
                _KE.add('click', element, KNOWWE.plugin.d3web.dialog.answerClicked);
            });
            * */
            
            _KS('.num-cell-down').each(function( element ){
                _KE.add('keydown', element, KNOWWE.plugin.d3web.dialog.numInput);
            });
            _KS('.num-cell-ok').each(function( element ){
                _KE.add('click', element, KNOWWE.plugin.d3web.dialog.numInput);
            });         
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
                action : 'RefreshHTMLDialogAction'
            }
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'create',
                    fn : function() {    
                        var el = _KS('a .dialog')[0];
                        el.innerHTML = bttn_name; 
                        
                        _KE.removeEvents(_KS('#bttn-dialog'));
                        _KE.add('click', _KS('#bttn-dialog'), KNOWWE.plugin.d3web.dialog.remove);
                        KNOWWE.plugin.d3web.dialog.initAction();
                    }
                },
                create : {
                    id : 'pagecontent',
                    fn : function(){
                        return new _KN('div', {'id' : 'dialog-panel'});
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
         * Function: remove
         * Removes the HTMLDialog from the article and changes the HTMLDialog menu
         * button to its default.
         */
        remove : function(){
            _KS('#dialog-panel')._remove();            
            _KE.removeEvents(_KS('#bttn-dialog'));
            _KE.add('click', _KS('#bttn-dialog'), KNOWWE.plugin.d3web.dialog.insert);       
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
         * Shows the HTMLDialog after a page refresh. Uses the qContainerStates
         * variable to determine the state of the elements. The state is recovered
         * according to this.
         */
        refreshed : function(){
       
            var qContainers = qContainerStates.split(';');
            var h4s = _KS('#dialog h4');
            var tables = _KS('#dialog table');
                
            for( var i = 0; i < qContainers.length-1; i++ ){
            
                var s = qContainers[i].substring(0,1);
                var id = qContainers[i].substring(1);
                var state = decodeStatus( s );
                
                var h4 = _KS('#' + id + ' h4')[0];
                toogleImage(h4, state);
               
                var tbl = _KS('#' + id + ' table')[0];
                tbl.className = state;
            }
           
            var followUps = followUpStates.split(';');           
            var trs = _KS('#dialog tr');
          
            for(var j = 0; j < followUps.length-1; j++) {
                var s = followUps[j].substring(0,1);
                var id =followUps[j].substring(1);
                var state = decodeStatus( s );
                
                for(var k=0; k<trs.length; k++){
                    if(trs[k].className.substring(0,3) == 'trf'){
                        if(trs[k].id == id){
                            if(s==0){
                                trs[k].className = 'trf hidden';
                            } else {
                                trs[k].className = 'trf'
                            }
                        }
                    }
                }
            }
            
            KNOWWE.plugin.d3web.dialog.initAction();
            KNOWWE.helper.observer.notify('update');
        },
        /**
         * Function: answerClicked
         * Stores the user selected answer of the HTMLDialog.
         * 
         * Parameters:
         *     event - The user click event on an answer.
         */
        answerClicked : function( e ) {
            var el = _KE.target(e);
            if(el.tagName.toLowerCase() == "input") return;
            
            var rel = eval("(" + el.getAttribute('rel') + ")");
            _KE.cancel( e );
            if( !rel ) return;
            var answerID = rel.oid;
            KNOWWE.plugin.d3web.dialog.send( rel.web, rel.ns, rel.qid, 'undefined', {ValueID: answerID});
        },
        /**
         * Function: numInput
         * Handles input fields in the HTMLDialog.
         * 
         * Parameters:
         *     event - The event that occurs when the users enters a numeric value into an input field of the dialog.
         */
        numInput : function( event ){
            event = new Event( event ).stopPropagation();
            var bttn = (_KE.target( event ).className == 'num-cell-ok');            
            var key = (event.code == 13);
            if( !(key || bttn) ) return false;
            
            var rel = null;
            if(key){
                rel = eval("(" + _KE.target( event ).getAttribute('rel') + ")");
            } else {
                rel = eval("(" + _KE.target( event ).previousSibling.getAttribute('rel') + ")");
            }
            if( !rel ) return;
            
            var inputtext = 'inputTextNotFound';
            if(_KS('#' + rel.inputid)) {
                    inputtext = _KS('#' + rel.inputid).value; 
            }
            KNOWWE.plugin.d3web.dialog.send(rel.web, rel.ns, rel.oid, rel.qtext, {ValueNum: inputtext});            
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
                    ids : ['dialog-panel'],
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
 * Namespace: KNOWWE.plugin.d3web.adminconsole
 * The KNOWWE plugin admin console.
 */
KNOWWE.plugin.d3web.adminconsole = function(){
    return {
        /**
         * Function: init
         * Adds events to the buttons in the admin console. Without this there is
         * no action in the admin console.
         */
        init : function(){
            if(_KS('#admin-summarizer'))
                _KE.add('click', _KS('#admin-summarizer'), this.doSumAll);
            if(_KS('#admin-reInit'))
                _KE.add('click', _KS('#admin-reInit'), this.doReInit);
            if(_KS('#admin-parseWeb'))
                _KE.add('click', _KS('#admin-parseWeb'), this.doParseWeb);
        },
        /**
         * Function: doReInit
         */
        doReInit : function(){
            var params = {
                action : 'ReInitDPSEnvironmentAction',
                KWikiWeb : 'default_web'
            };
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    ids : [ 'reInit' ],
                    fn : function(){
                        _KE.add('click', _KS('#reInit'), function(event){
                            event = new Event(event);
                            event.stopPropagation();                            
                            _KS('#reInit')._clear();
                        });
                    }
                }
            };
            new _KA( options ).send();
        },
        /**
         * Function: doParseWeb
         * 
         */
        doParseWeb : function(){
            var params = {
                action : 'ParseWebOfflineRenderer',
                KWikiWeb : 'default_web'
            };
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    ids : [ 'parseWeb' ],
                    fn : function(){
                        _KE.add('click', _KS('#parseWeb'), function(event){
                            event = new Event(event);
                            event.stopPropagation();
                            _KS('#parseWeb')._clear();
                        });
                    }
                }
            };
            new _KA( options ).send();
        },
        /**
         * Function: doSumAll
         */
        doSumAll : function(){
            var params = {
                action : 'KnowledgeSummerizeAction',
                KWikiWeb : 'default_web'
            };
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    ids : [ 'sumAll' ],
                    fn : function(){
                        _KE.add('click', _KS('#sumAll'), function(event){
                            var event = new Event(event);
                            event.stopPropagation();                                
                            _KS('#sumAll')._clear();
                        });
                    }
                }
            };
            new _KA( options ).send();
        }
    }
}();


/**
 * Namespace: KNOWWE.plugin.d3web.semantic
 * The namespace of the semantic things in KNOWWE.
 */
KNOWWE.plugin.d3web.semantic = function(){
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
        KNOWWE.plugin.d3web.semantic.send( mcUrl, null );
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
                    _KE.add('click', element, KNOWWE.plugin.d3web.semantic.showOverlayQuestion);
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
                    _KE.add('click', element, KNOWWE.plugin.d3web.semantic.handleForm);
                });
            }

            if(_KS('.semano_oc').length != 0){
                _KS('.semano_oc').each(function(element){
                    _KE.add('click', element, KNOWWE.plugin.d3web.semantic.handleOC);
                });         
            }
            if(_KS('.semano_num').length != 0){
                _KS('.semano_num').each(function(element){
                    _KE.add('keydown', element, KNOWWE.plugin.d3web.semantic.handleNum);
                });
                 _KS('.semano_ok').each(function(element){
                    _KE.add('click', element, KNOWWE.plugin.d3web.semantic.handleNum);
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
            KNOWWE.plugin.d3web.semantic.send( rel, {ValueNum : el.value} );
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
            
            KNOWWE.plugin.d3web.semantic.send( rel, null );
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
                fn : KNOWWE.plugin.d3web.semantic.overlayActions
             });
        }
    }
}();
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
                action : 'DPSSolutionsAction',
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

/**
 * Namespace: KNOWWE.plugin.d3web.qcvalues
 * The qcvalues namespace.
 */
KNOWWE.plugin.d3web.qcvalues = function(){
    return {
        /**
         * Function: init
         * Initializes the QuestionnaireValues functionality. 
         */
        init : function(){
            KNOWWE.helper.observer.subscribe( 'update', this.updateQuestionnaireValues );
        },
        /**
         * Function: updateQuestionnaireValues
         * Updates the values of the questions in the questionnaires values panel.
         */
        updateQuestionnaireValues : function(){
            
            // There can be more than one QuestionnaireValuesViewHandler
            // TODO: this is more or less a hack, because it limits the amount of taghandlers to 100
            for (i = 0; i < 100; i++) {
                
                // Create the unique id of each panel
                var panelID = '#qcvalues-panel' + i;
                
                // Check the existence of the panel
                if(!_KS(panelID)) continue;
                
                // Find the childNode containing the name of the Questionnaire
                for (j = 0; j < _KS(panelID).childNodes.length; j++) {
                    if(_KS(panelID).childNodes[j].className == 'qcname') {
                        
                        var questionnaireName = _KS(panelID).childNodes[j].innerHTML;
                        
                        var params = {
                                action : 'QuestionnaireValuesViewAction',
                                questionnaire: questionnaireName,
                                KWikiWeb : 'default_web'
                        }

                        // Create the unique id of each result p
                        var resultID = 'qcvalues-result' + i;
                        
                        if (!_KS('#' + resultID)) break;
                        
                        var options = {
                            url : KNOWWE.core.util.getURL( params ),
                            response : {
                                action : 'insert',
                                ids : [ resultID ]
                            }
                        }
                        
                        new _KA( options ).send(); 
                        
                        // If we have done this, we can leave THIS loop!
                        break;
                    
                    
                    }
                }
            }
        }
    }   
}();


/**
 * Namespace: KNOWWE.plugin.d3web.rerenderquestionsheet
 * Contains some functions to update the question sheet after the user selected
 * an answer in the dialog or in the question sheet itself.
 */
KNOWWE.plugin.d3web.rerenderquestionsheet = function() {
    return {
        /**
         * Function: init
         */
        init : function(){
            KNOWWE.helper.observer.subscribe( 'update', this.update );
        },      
        /**
         * Function: update
         * Updates the question sheet after the user selected an answer in the
         * pop-up window.
         */
        update : function( ) {
            var topic = KNOWWE.helper.gup('page');
            var params = {
                action : 'ReRenderQuestionSheetAction',
                KWikiWeb : 'default_web',
                KWiki_Topic : topic
            }
            var url = KNOWWE.core.util.getURL( params );
            KNOWWE.plugin.d3web.rerenderquestionsheet.execute(url, 'questionsheet');
        },
        /**
         * Function: execute
         * Executes the update question sheet AJAX request
         * 
         * Parameters:
         *     url - The URL for the AJAX request
         *     id - The id of the DOM Element that should be updated.
         */
        execute : function( url, id ) {
            if(!_KS('#questionsheet-panel')) return ;
            var options = {
                url : url,
                response : {
                    action : 'insert',
                    ids : [ id ],
                    fn : function(){
                        KNOWWE.core.util.addCollabsiblePluginHeader('#questionsheet-panel');
                        KNOWWE.plugin.d3web.semantic.init();
                    }
                }
            }
            new _KA( options ).send();
        }
    }
}();
/**
 * Namespace: KNOWWE.plugin.d3web.dt
 * DecisionTree things
 */
KNOWWE.plugin.d3web.dt = function(){
    
    /**
     * Shows a ul element containing the answer possibilities of the question.
     */
    function showUL( ul ){
        ul = new _KN( ul );
        //ul._show();
        
        var c = ul.childNodes;
        if(!c || !c.length) return;
        
        for( var i = 0; i < c.length; i++){
            if( c[i].nodeName.toLowerCase() == 'li'){
                c[i].style.visibility = 'visible';
                c[i].style.display = 'block';
                var z = new _KN( c[i] );
                z._getChildren().each(function(element){
                    if( element.nodeName.toLowerCase() == 'ul' ){
                        element._getChildren().each(function( el ){ el._hide();});
                        //element._hide();
                    }
                });
            }
        }
    }
    /**
     * Function: getCertainChild
     * Returns to a given element the first child element with tagname 
     * equals tag.
     */
    function getCertainChild ( father, tag ){
        var c = father.childNodes;
        for(var i = 0; i < c.length; i++){
            if(c[i].nodeName.toLowerCase() == tag)
                return c[i];
        }
        return null;
    }    
     /**
     * Function: createMenu
     * Creates the action menu that is inserted into the dt pre element.
     */   
    function createMenu( node ) {
        var divs, pres, l;
        divs = _KS(':div#QuestionTree');
        //pres = document.getElementsByTagName('pre');
        //if( !node && pres.length == 0 ) node = _KS('.collapsible-questiontree')[0];
        //l = pres.length || 1;
        l = divs.length;
            
        for(var i = 0; i < l; i++){
            var father = divs[i];
            //var father = pres[i] ? pres[i].parentNode : node;
            if(!father) return;
            if(father.id.indexOf('QuestionTree')){
                var div = new _KN('div', {'style' : 'float:right'})
                div._setHTML("<span class=\"dt-ajax pointer\" rel=\"{dt : '', KdomNodeId : '"+father.id+"'}\">default</span> | "
                        + "<span class=\"dt-ajax pointer\" rel=\"{dt : 'custom', KdomNodeId : '"+father.id+"'}\">dialog</span>");
                
                pres = divs[i].getElementsByTagName('pre');
                if( pres.length != 0) {
                    div._injectTop( pres[0] );
                } else {
                    div._injectTop( divs[i] );
                }
                //divs[i] ? div._injectTop( divs[i] ) : div._injectTop( node ); //pres[i]
            }
        }
    }    
    
    return {
        /**
         * Function: init
         * Initialises the additional DT features like menu etc.
         */
        init : function( node ){        
            createMenu();
            var el = _KS('.dt-ajax');
            if(el.length != 0){
              el.each(function( element ){
                 _KE.removeEvents( element );
                 _KE.add('click', element, function(){
                    var params = eval( "(" + element.getAttribute('rel') + ")");
                    KNOWWE.plugin.d3web.dt.setCustomRenderer( params.dt, element );                    
                 });
              });
            }
            if(node) return;
            var trees = _KS('.collapsible-questiontree');
            for(var i = 0; i < trees.length; i++){
                _KS('li', trees[i]).each(function(element){
                    element._hide();
                });
                KNOWWE.plugin.d3web.dt.initView( trees[i] );
                _KE.add('click', trees[i], KNOWWE.plugin.d3web.dt.next);
            }
        },
        /**
         * Function: initView
         * The start view of the DT dialog.
         * 
         * Parameters:
         *     root - The root div element of the DT.
         */
         initView : function( root ){
            var c = null, pos = 0, head = null;
            c = root.childNodes; //document.getElementsByTagName('pre')[0].childNodes;
            
            for(var i = 0; i < c.length; i++){
                if( c[i].nodeName.toLowerCase() == 'ul'){
                    var li = c[i].childNodes;            
                    for(var j = 0; j < li.length; j++){
                        if( li[j].nodeName.toLowerCase() == 'li'){
                            head = new _KN( li[j] );
                            head._show();
                            break;
                        }
                    }
                   //head._next()._show(); not needed when in answer mode
                   showUL( getCertainChild( head, 'ul' )); //head.next() when question should be visible
                }
            }
            var infos = _KS('.collapsible-info');
            for(var i = 0; i < infos.length; i++){
                _KE.add('click', infos[i], KNOWWE.plugin.d3web.dt.showHint);
            }
            _KS('#collapsible-questiontree-info')._hide();
         },
         /**
          * Function: setCustomRenderer
          * Sets some custom renderer for the decision tree. 
          * 
          * Parameters:
          *     custom - Sets if the renderer are set or removed.
          */         
         setCustomRenderer : function( custom, element ){
              var params = { rm : custom,  
                  types : {
                      'de.d3web.we.kdom.questionTreeNew.dialog.QuestionDashTreeOnlyAnswersRenderer'     : 'de.d3web.we.kdom.questionTreeNew.QuestionDashTree',
                      'de.d3web.we.kdom.questionTreeNew.dialog.QuestionTreeRootTypeRenderer'     : 'de.d3web.we.kdom.questionTreeNew.QuestionTreeRootType',
                      'de.d3web.we.kdom.questionTreeNew.dialog.DashesPrefixRenderer'             : 'de.d3web.we.kdom.dashTree.DashesPrefix',
                      'de.d3web.we.kdom.questionTreeNew.dialog.QuestionTreeAnswerDefRenderer'    : 'de.d3web.we.kdom.objects.QuestionTreeAnswerDef'
                  }
             };
             
             params.action =  'RenderManagerAction';
             
             var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'none',
                    fn : function(){
                        KNOWWE.plugin.d3web.dt.loadView( element );
                    }
                }
            }
            new _KA( options ).send();    
         },
         /**
          * Function: loadView
          * Loads per Ajax the view of the DT dialog. 
          * 
          * Parameters:
          *     e - The user event.
          */
         loadView : function( element ){
            var rel = element.getAttribute('rel');
            if (!rel) return;
            
            var params = eval( "(" + rel + ")");
            params.action = 'ReRenderContentPartAction';
            params.KWikiWeb = 'default_web';
            params.topic = KNOWWE.helper.gup('page');
            
            var root = _KS('#'+params.KdomNodeId);
            _KE.removeEvents( root ); 
                       
            var fn = function(){
                var root = _KS('#'+params.KdomNodeId);
                KNOWWE.plugin.d3web.dt.init( root );
                if(!root || !params.dt) return;
                _KE.add('click', root, KNOWWE.plugin.d3web.dt.next);
                _KS('li', root).each(function(element){
                    element._hide();
                });
                KNOWWE.plugin.d3web.dt.initView( root );  
            };
            
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'replace',
                    ids : [ params.KdomNodeId ],
                    fn : fn
                }
            }
            new _KA( options ).send();          
         },
         /**
          * Function: next
          * Shows the next question.
          * 
          * Parameters:
          *     e - An occured event.
          */
         next : function( e ){
            var next = _KE.target( e );
            if (next.className.toLowerCase() === 'dt-ajax') return;
            next = KNOWWE.helper.tagParent( next, 'li' ); 
                  
            if( next.className === "head" ){ 
                next = new _KN( next )._next();
            } else if(KNOWWE.plugin.d3web.dt.alreadyChoosen(next)){
                next = next.parentNode.parentNode; //only node in question mode
            } else {
                KNOWWE.plugin.d3web.dt.hideAnswers( next );
                next = KNOWWE.plugin.d3web.dt.getNextQuestion( next );
            } 
            KNOWWE.plugin.d3web.dt.showQuestion( next );
         },
        /**
         * Function: getNextQuestion.
         * Checks were to find the next question and returns the question
         * element for further handling.
         * 
         * Parameters:
         *     answer - The answer the user choose.
         */
        getNextQuestion : function( answer ){          
            var q;
            if( q = getCertainChild( answer, 'ul')){ //follow up question
                q = getCertainChild( q, 'li' );
                return q;
            } else { //get father
                q = answer.parentNode;
                if( !q ) return answer;
                if( q = q.parentNode ) {
                    q = new _KN( q );
                    return q.parentNode.parentNode;
                    //while( !q._next().nodeName ){   //used in question mode
                    //    q = new _KN( q.parentNode.parentNode );
                    //}                
                    //return q._next();
                } else {
                    q = answer;
                    do {
                        q = q.parentNode;
                    } while(q.parentNode.nodeName.toLowerCase() != 'div');
                    return q;
                }
            }
        },
        /**
         * Function: showQuestion
         * Shows the next question in the dialog.
         * 
         * Parameters:
         *     question: The to show question.
         */
        showQuestion : function( question ){
            if(!question || !question.nodeName) return;
            var tagName = question.nodeName.toLowerCase();
                       
            if( tagName == 'li' ) {
                question = new _KN( question );
                question._show();
                showUL( getCertainChild( question, 'ul' ));
            } else if (tagName = 'ul'){ //does not occur in answer mode
                showUL( question );
                try { question = question.getElementsByTagName('li')[0].getElementsByTagName('ul')[0];
                showUL( question ); } catch(e){ };
            }
        },
        /**
         * Function: hideAnswers
         * hides the not choosen answers from the DT.
         */
        hideAnswers : function( answer ) {
            var p = new _KN( answer.parentNode );
            var c = p._getChildren();
            for(var i = 0; i < c.length; i++){
                var child = c[i];
                if (child.innerHTML !== answer.innerHTML){
                    child._hide();
                }
            }
        },
        /**
         * Function: alreadyChoosen
         * Checks if the user hag already selcted the question.
         */
        alreadyChoosen : function( question ){
            question = getCertainChild(question, 'ul');
            if (!question) return false;

            var q = new _KN( question );
            //q._hide();            
            var c = q._getChildren();
            var choosen = false;
              
            for( var i = 0; i < c.length; i++){             
                if(c[i]._isVisible()){
                    c[i]._hide();
                    choosen = true;
                }
            }
            return choosen;
        },
        /**
         * Function: showHint
         * Shows additional information of an answer
         */
        showHint : function( e ){
            var o, i = '';
            
            _KE.cancel( e );
            e = _KE.target( e );
            
            //get stored information from element, element has token rel:info
            //add showHint event to root QuestionTreeDiv
            o = e.getAttribute('rel');
            if( !o ) return;
            i = eval( "(" + o + ")");
 
            el = _KS('#collapsible-questiontree-info');
            el._show();
            el.innerHTML = i.info;
        }
    }
}();

(function init(){ 
    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
            
            var ns = KNOWWE.plugin.d3web;
            for(var i in ns){
                if(ns[i].init){
                    ns[i].init();
                }
            }
            
            if(_KS('#dialog-panel')){
                KNOWWE.plugin.d3web.dialog.initAction();
                var xy = KNOWWE.helper.findXY( _KS('#dialog-panel') );
            }
        });
    }
}());