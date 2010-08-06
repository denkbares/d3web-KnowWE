/**
 * Title: KnowWE-core
 * Contains javascript functions the KnowWE core needs to functions properly.
 * The functions are based upon some KnowWE helper functions and need the
 * KNOWWE-helper.js in order to work correct.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    /**
     * The KNOWWE global namespace object.  If KNOWWE is already defined, the
     * existing KNOWWE object will not be overwritten so that defined
     * namespaces are preserved.
     */
    var KNOWWE = {};
}
/**
 * Namespace: KNOWWE.core
 * The KNOWWE core namespace.
 * Contains some init functions.
 */
KNOWWE.core = function(){
    return {
        /**
         * Function: init
         * Core init functions.
         */
        init : function(){
            KNOWWE.core.util.addCollabsiblePluginHeader();
            KNOWWE.core.util.form.addFormHints('knoffice-panel');
            KNOWWE.core.actions.init();
        }
    }
}();
/**
 * Namespace: KNOWWE.core.actions
 * The KNOWWE actions namespace object.
 * Contains all actions that can be triggered in KnowWE per javascript.
 */
KNOWWE.core.actions = function(){
    return {
        /**
         * Function: init
         * Core KnowWE actions.
         */
        init : function(){
            //init quickedit actions
            var els = _KS('.quickedit');
            for (var i = 0; i < els.length; i++){
                _KE.removeEvents(els[i]);
                if( els[i]._hasClass( 'table' )){
                    _KE.add('click', els[i], function(e){
                        var el = _KE.target(e);
                        var id = el.parentNode.id;
                        KNOWWE.core.actions.enableQuickEdit( KNOWWE.core.table.init, id, null );
                    });
                    //Due to problems with refresh, so that table functionality is still guaranteed:
                    KNOWWE.core.table.init();
                } else if( els[i]._hasClass( 'default') ) {
                    _KE.add('click', els[i], function(e){
                        var el = _KE.target(e);
                        var rel = eval("(" + el.getAttribute('rel') + ")");
                        KNOWWE.core.actions.enableQuickEdit( KNOWWE.core.edit.init, rel.id, "render" );
                    });
                }
                //check for save button in case the user reloads the page during quick edit
                rel = eval("(" + els[i].getAttribute('rel') + ")");
                if(rel) {
                    bttns = _KS('#'+rel.id + ' input[type=submit]');
                    if( bttns.length != 0 ){
                        _KE.add('click', bttns[0], KNOWWE.core.edit.onSave );
                    }
                }        
            }
            
            //init show extend panel
            els = _KS('.show-extend'); 
            if( els ){
                els.each(function(element){
                    _KE.add('click', element, KNOWWE.core.util.form.showExtendedPanel); 
                });
            }
            
            //init parseAll action
            _KS('.parseAllButton').each(function(element){
                    _KE.add('click', element, KNOWWE.core.actions.parseAll); 
            });
            
            //enable clearHTML
            _KS('.clear-element').each(function(element){
                _KE.add('click', element, KNOWWE.core.actions.clearHTML);
            });
            
            _KS('.js-cell-change').each(function(element){
                _KE.add('change', element, KNOWWE.core.actions.cellChanged);
            });
        },
        /**
         * Function: clearHTML
         * Clears the inner HTML of a given element.
         * 
         * Parameters:
         *     e - The occurred event.
         */
        clearHTML : function( e ){
            var el = KNOWWE.helper.event.target( e );
            if( el.id ){
                _KS(el.id)._clear();
            }
        },
        /**
         * Function: cellChanged
         * 
         * Parameters:
         *     e - The occurred event.
         */
        cellChanged : function( e ) {
            var el = KNOWWE.helper.event.target( e );
            var rel = el.getAttribute('rel');
            
            if(!rel) return;
            rel = rel.parseToObject();
            
            var nodeID = rel.id;
            var topic = rel.title;
            
            el = _KS('#' + nodeID);
            if( el ) {
                var selectedOption = el.options[el.selectedIndex].value; 
             
                var params = {
                    action : 'ReplaceKDOMNodeAction',
                    TargetNamespace : nodeID,
                    KWikitext : selectedOption,
                    KWiki_Topic : topic
                }
                var options = {
                    url : KNOWWE.helper.getURL( params ),
                    response : {
                        action : none,
                        fn : null
                    }
                }
                new _KA( options ).send();
            }
        },
        /**
         * Function: enableQuickEdit
         * Sets the quick-edit flag to the given element.
         * 
         * Parameters:
         *     fn - The function that should be executed afterwards.
         *     id - The id of the element the quick edit flag should set to.
         */        
        enableQuickEdit : function( fn, id, view){
            var params = {
                action : 'SetQuickEditFlagAction',
                TargetNamespace : id,
                KWiki_Topic : KNOWWE.helper.gup('page'),
                ajaxToHTML : view,
                inPre : KNOWWE.helper.tagParent(_KS('#' + id), 'pre') != document
            }   
            
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'string',
                    ids : [id],
                    fn : function(){
                        fn.call();
                        Collapsible.render( _KS('#page'), KNOWWE.helper.gup('page'));
                        if(view === "render"){
                            KNOWWE.helper.observer.notify('quick-edit');
                        }
                    }
                }
            }
            new _KA( options ).send();
            if (id.substring(id.length - 13) === 'TestcaseTable') {
            	(function() {Testcase.addNewAnswers($(id));}).delay(500);
            }
        },
        
        /**
         * Function: enableQuickEdit
         * parses all pages
         */  
        parseAll : function(){
            var params = {
                action : 'ParseWebOfflineRenderer',
                KWiki_Topic : KNOWWE.helper.gup('page')
            }   
            
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'insert',
                    ids : ['parseAllResult']
                }
            }
            new _KA( options ).send();
        }
    }
}();

/**
 * Namespace: KNOWWE.core.util
 * The KNOWWE core util namespace object.
 * Contains some helper functions. For detailed information read the comments
 * above each function.
 */
KNOWWE.core.util = function(){
    return {
        /**
         * Function: addCollabsiblePluginHeader
         * Extends the headings of the KnowWEPlugin DIVs with collabs ability.
         * The function searches for all DIVs with an ".panel" class attribute and
         * extends them. The plugin DIV should have the following structure in order
         * to work properly:
         * (start code)
         * <div class='panel'><h3>Pluginname</h3><x>some plugin content</x></div>
         * (end)
         * 
         * Parameters: 
         *     id - Optional id attribute. Specifies the DOM element, the collabsible
         *          functionality should be applied to.
         */
        addCollabsiblePluginHeader : function( id ){
            var selector = "div .panel";
            if( id ) {
                selector = id;
            }
            
            var panels = _KS( selector );
            if( panels.length < 1 ) return;
            if( !panels.length ) panels = new Array(panels);
            
            for(var i = 0; i < panels.length; i++){
                var span = new _KN('span');
                span._setText('- ');
                
                var heading = panels[i].getElementsByTagName('h3')[0];
                if(!heading.innerHTML.startsWith('<span>')){
                     span._injectTop( heading );
                }
                _KE.add('click', heading , function(){
                    var el = new _KN( this );
                    var style = el._next()._getStyle('display');
                    style = (style == 'block') ? 'none' : ((style == '') ? 'none' : 'block');                    
                    
                    el._getChildren()[0]._setText( (style == 'block')? '- ' : '+ ' );
                    el._next()._setStyle('display', style);
                });
            }
        },
        /**
         * Function: getURL
         * Returns an URL created out of the given parameters.
         * e.g.: 
         * (start code)
         *  var params = {
         *      renderer : 'KWiki_dpsSolutions',
         *      KWikiWeb : 'default_web'
         *  }
         * KNOWWE.util.getURL( params ) --> KnowWE.jsp?renderer=KWiki_dpsSolutions&KWikiWeb=default_web
         * (end)
         * 
         * Parameters:
         *     params - The parameter for the URL.
         * 
         * Returns:
         *     The URL containing the elements of the params array.
         */
         getURL : function( params ){
            var baseURL = 'KnowWE.jsp';
            var tokens = [];
        
            if( !params && typeof params != 'object') return baseURL;
                        
            for( keys in params ){
                var value = params[keys] ; 
                if(typeof value != 'string') value = JSON.stringify( params[keys] ); 
                tokens.push(keys + "=" + escape(encodeURIComponent( value )));
            }
            
            //parse the url to add special token like debug etc.
            var p = document.location.search.replace('?','').split('&');
            for(var i = 0; i < p.length; i++){
                var t = p[i].split('=');
                if(!KNOWWE.helper.containsArr(tokens,t[0])){
                    tokens.push( t[0] + "=" + encodeURIComponent( t[1] ));
                }
            }
            tokens.push('tstamp='+new Date().getTime());            
            return baseURL + '?' + tokens.join('&');
        },       
        /**
         * Function: getWindowParams
         * Returns an URL which is used as the target URL for a popup window.
         * 
         * Parameters:
         *     params - The parameter for the popup window
         * Returns:
         *     The url for the popup window
         */
        getWindowParams : function( params ){
            if( !params && typeof params != 'object') return '';
            var tokens = [];
            for( keys in params ){
                if(keys == 'url') continue;
                tokens.push(keys + "=" + params[keys]);
            }
            return tokens.join(',');
        },
        /**
         * Function: replace
         * Used to replace elements in the DOM tree. All elements given in the ids
         * array are replaced with the value of the second parameter. If one
         * element is not found, nothing happens.
         * 
         * Parameters:
         *     ids - The ids of the elements that should be replaced.
         *     value - The value used for replacement.
         */
        replace : function(ids, value){
            for (var i in ids ) {
                if( typeof ids[i] != 'string' ) return;
                
                var tmpDiv = new _KN('div', {
                    'styles': {
                        'display': 'hidden' 
                    },
                    'id' : 'KnowWE-temp'
                });
                var old = _KS('#' + ids[i]);
                tmpDiv._injectBefore( old );
                tmpDiv._setHTML( value );
                old._remove();
                var old2 =_KS('#' + ids[i]);
                var tmp = _KS('#KnowWE-temp'); 
                old2._injectAfter(tmp );
                tmpDiv._remove();
            }
        }
    }
}();

/**
 * Namespace: KNOWWE.core.util.form
 * Some helper functions concerning HTML form elements.
 */
KNOWWE.core.util.form = function(){
    return {
        /**
         * Function: getCursorPositionInTextArea
         * Does get the current position of the cursor inside a textarea.
         * 
         * Parameters:
         *     textarea - The textarea
         * 
         * Returns: 
         *     The position of the cursor inside the textarea.
         */
        getCursorPositionInTextArea : function( textarea ){
            if( document.selection ){
                var range = document.selection.createRange();
                var stored_range = range.duplicate();
                stored_range.moveToElementText( textarea );           
                stored_range.setEndPoint( 'EndToEnd', range );
                textarea.selectionStart = stored_range.text.length - range.text.length;            
                return textarea.selectionStart + range.text.length;
            } 
            else {
                if(textarea.selectionEnd){
                    textarea.focus();
                    return textarea.selectionEnd;
                }
            }
        },          
        /**
         * Function: insertAtCursor
         * Inserts an text element at the current cursor position in a textarea, etc.
         * 
         * Parameters:
         *     element - The textarea, etc.
         *     value - The text string
         */
        insertAtCursor : function(element, value) {
            if (document.selection) { 
                element.focus();
                sel = document.selection.createRange();
                sel.text = value;
            } else if(element.selectionStart || element.selectionStart == '0'){ 
                 var startPos = element.selectionStart;
                 var endPos = element.selectionEnd;
                 element.value = element.value.substring(0, startPos) + value
                     + element.value.substring(endPos, element.value.length);
                 element.setSelectionRange(endPos + value.length, endPos + value.length);
            } else {
                element.value = value;
            }
            element.focus();
        },
        /**
         * Function: addFormHints
         * Shows a small overlay text containing additional information about an
         * input HTMLElement. Used for e.g. in the KnofficeUploader.
         * 
         * Parameters:
         *     name - The name of the HTMLElement
         */
        addFormHints : function( name ){
            if(!_KS('#' + name)) return;
            
            var els = document.getElementById(name + '-extend').getElementsByTagName("input");
            for (var i = 0; i < els.length; i++){
                var tag = els[i].nextSibling.tagName;
                if( !tag) continue;

              if(tag.toLowerCase() == 'span'){
                _KE.add('focus', els[i], function (e) {
                   var el = _KE.target( e );
                   el.nextSibling.style.display = "inline";});
                _KE.add('blur', els[i], function (e) {
                   var el = _KE.target( e );
                   el.nextSibling.style.display = "none";});
              }
            }
        },
        /**
         * Function: showExtendedPanel
         * Shows a panel in certain plugin with additional options.
         */
        showExtendedPanel : function(){
            var el = this;

            var style = el._next().style;
            el.removeAttribute('class');           
         
            if(style['display'] == 'inline'){
                style['display'] = 'none';
                el.setAttribute('class', 'show extend pointer extend-panel-down');
            }else{
                style['display'] = 'inline';
                el.setAttribute('class', 'show extend pointer extend-panel-up');
            }
        }
    }    
}();

/**
 * Namespace: KNOWWE.core.util.tablesorter
 * The KNOWWE table sorter namespace.
 * Contains functions to sort HTMLTables.
 */
KNOWWE.core.util.tablesorter = function(){
    /*sorting function for strings */
    function stringSort(el1, el2){
        var cellOne = el1.getElementsByTagName("td")[col].innerHTML;
        var cellTwo = el2.getElementsByTagName("td")[col].innerHTML;       
                
        return (cellOne > cellTwo) ? direction :(cellOne < cellTwo)? -direction : 0;
    }
    
    /*sorting function for integers */
    function intSort(el1, el2){
        var cellOne = parseInt(el1.getElementsByTagName("td")[col].innerHTML);
        var cellTwo = parseInt(el2.getElementsByTagName("td")[col].innerHTML);
                
        return (cellOne > cellTwo) ? direction :(cellOne < cellTwo)? -direction : 0;
    }   
    return {
        /**
         * Function: init
         * Initializes the sort ability.
         * 
         * Parameters:
         *     columns - The columns of the to sort table.
         *     tableID - The id of the table.
         */
        init : function(columns, tableID){
            if(!_KS('#' + tableID)) return;
            var tblHeader = document.getElementById(tableID).getElementsByTagName('thead')[0].getElementsByTagName('th');
            for( var i = 0; i < tblHeader.length; i++){
                if(columns[i].sortable == "true"){
                    var text = tblHeader[i].innerHTML;
                    _KE.add('click', tblHeader[i], function(){
                        KNOWWE.core.util.tablesorter.sort(i, tableID);
                    });
                }
            }
        },
        /**
         * Function: sort
         * Sorts the table according to the selected column.
         * 
         * Parameters:
         *     columns - The columns of the to sort table.
         *     tableID - The id of the table.
         */
        sort : function(columnID, tableID){
            var tblHeader = document.getElementById(tableID).getElementsByTagName('thead')[0].getElementsByTagName('th');
            var tbody = document.getElementById(tableID).getElementsByTagName('tbody');
                    
            var sortingType; var direction;
            var rowsSort = [];
            
            /* choose sorting type [asc desc]*/
            if(tblHeader[columnID].classname == "asc"){
                sortingType = "des";
                direction = -1;
            }else if(tblHeader[columnID].classname == "des"){
                sortingType = "asc";
                direction = 1;
            }else{
                sortingType = "asc";
                direction = 1;
            }       
                
            /* for each tbody if query is found in more than one article*/
            for(var i = 0; i < tbody.length; i++){
                var rows = tbody[i].getElementsByTagName('tr');
                col = columnID;
                
                /* clone original nodes´, necessary for comparision. */
                for(var j = 0; j < rows.length; j++){
                    rowsSort[j] = rows[j].cloneNode(true);
                }
                
                /* sort the table*/
                rowsSort.sort(stringSort);
                    
                /* replace old table with new sorted one */
                for(var k = 0; k < rows.length; k++){
                    rows[k].parentNode.replaceChild(rowsSort[k], rows[k]);
                }
            }
            
            /* store current sorting type */
            tblHeader[columnID].classname = sortingType;
        }       
    }   
}();
/**
 * Namespace: KNOWWE.core.edit
 * The KNOWWE quick edit namespace.
 */
KNOWWE.core.edit = function(){
    return {
        /**
         * Function: init
         * Initializes some wuick edit default functionality.
         */     
        init : function(){
            var elements = _KS('.quickedit .default');
            for(var i = 0; i < elements.length; i++){
                var rel, bttns;
                
                _KE.removeEvents(elements[i]);
                rel = eval("(" + elements[i].getAttribute('rel') + ")");
                bttns = _KS('#'+rel.id + ' input[type=submit]');
                if( bttns.length != 0 ){
                    _KE.add('click', bttns[0], KNOWWE.core.edit.onSave );
                    _KE.add('click', elements[i], function(e){
                        var el = _KE.target(e);
                        var rel = eval("(" + el.getAttribute('rel') + ")");
                        KNOWWE.core.actions.enableQuickEdit( KNOWWE.core.edit.init, rel.id, "render");
                    });
                }  else {               
                    _KE.add('click', elements[i], function(e){
                        var el = _KE.target(e);
                        var rel = eval("(" + el.getAttribute('rel') + ")");
                        KNOWWE.core.actions.enableQuickEdit( KNOWWE.core.edit.init, rel.id, null);
                    });
                }
            }               
        },
        /**
         * Function: onSave
         * Triggered when the changes to the quick edit element in edit mode should be saved.
         * 
         * Parameters:
         *     e - The occurred event.
         */     
        onSave : function( e ){
            var el = _KE.target(e);
            var rel = eval("(" + el.getAttribute('rel') + ")");
            var params = {
                action : 'UpdateKDOMNodeAction',
                SectionID :  rel.id,
                KWiki_Topic : KNOWWE.helper.gup('page')
            }

            var options = {
                url : KNOWWE.core.util.getURL ( params ),
                data : 'TargetNamespace='+encodeURIComponent(_KS('#' + rel.id + '/default-edit-area').value),
                loader : true,
                response : {
                    action : 'none',
                    fn : function(){ 
                        KNOWWE.core.actions.enableQuickEdit( KNOWWE.core.edit.init, rel.id, "render");
                        Collapsible.render( _KS('#page'), KNOWWE.helper.gup('page'));
                    }
                }
            }
            new _KA( options ).send();          
        }
    }
}();

/**
 * Namespace: KNOWWE.core.table
 * The KNOWWE table tag namespace.
 */
KNOWWE.core.table = function(){
    var map = new KNOWWE.helper.hash(); 
    return {  
        /**
         * Function: init
         * Initializes some table functionality.
         */
        init : function(){                  
            if( _KS('.table-edit').length != 0 ){
                var elements = _KS(".table-edit input[type=submit]");
                for(var i = 0; i < elements.length; i++){
                    _KE.add('click', elements[i], KNOWWE.core.table.onSave );
                }
                elements = _KS('.table-edit-node');
                for(var i = 0; i < elements.length; i++){
                    _KE.add('change', elements[i], KNOWWE.core.table.onChange );
                }
                
                elements = _KS('.quickedit .table');
                for(var i = 0; i < elements.length; i++){
                    _KE.removeEvents(elements[i]);
                    _KE.add('click', elements[i], function(e){
                        var el = _KE.target(e);
                        var id = el.parentNode.id;
                        KNOWWE.core.actions.enableQuickEdit( KNOWWE.core.table.init, id, null);
                    });
                }                
            }
        },
        /**
         * Function: map
         * Returns the map of the KnowWETable. Stores the changed cells in edit mode.
         * 
         * Returns:
         *     The changed cells stored as map.
         */
        getMap : function(){
            return map;
        },
        /**
         * Function: onChange
         * Triggered when the cell content changes. Stores the new value together 
         * with the old one in the table map.
         * 
         * Parameters:
         *     e - The occurred cell change event.
         */
        onChange : function(e){
            var el = _KE.target( e );
            KNOWWE.core.table.getMap().set(el.id, el.value);
        },
        /**
         * Function: onSave
         * Triggered when the changes to the table in edit mode should be saved.
         * 
         * Parameters:
         *     e - The occurred event.
         */
        onSave : function( e ){
            var el = _KE.target(e);
            var id = el.id;

            var n = '';
            KNOWWE.core.table.getMap().forEach(function(key, value){
                n += key + ";-;" + value + "::";
            });
            n = n.substring(0, n.lastIndexOf('::'));

            var params = {
                action : 'UpdateTableKDOMNodesAction',
                TargetNamespace : n,
                KWiki_Topic : KNOWWE.helper.gup('page')
            }

            var options = {
                url : KNOWWE.core.util.getURL ( params ),
                loader : true,
                response : {
                    action : 'none',
                    fn : function(){ 
                        KNOWWE.core.actions.enableQuickEdit( KNOWWE.core.table.init, id, "render");
                    }
                }
            }
            new _KA( options ).send();
        }
    }
}();


/**
 * Namespace: KNOWWE.core.renaming
 * The KNOWWE renaming tool plugin object.
 */
KNOWWE.core.renaming = function(){
    var myColumns = [{key:'match'  , label:'Match'   , type:'string' , sortable:'false'},
                {key:'section', label:'Section' , type:'string' , sortable:'true'},
                {key:'replace', label:'Replace?', type:'string' , sortable:'false'},
                {key:'preview', label:'Preview' , type:'string' , sortable:'false'}];
    return {
        /**
         * Function:init
         * The init function for the renaming tool. Enables the preview button.
         */
        init : function(){                    
            var bttn = _KS( '#rename-panel input[type=button]')[0];
            if( bttn ){
                _KE.add('click', bttn, KNOWWE.core.renaming.preview );
            }
        },
        /**
         * Function: selectPerSelection
         * Select all checkboxes within a certain section.
         * If section has value "undefined", select all checkboxes.
         *
         * Parameters:
         *     e - The occurred event. 
         */
        selectPerSection : function( e ) {
            var renameForm = this.form;
            var el = _KE.target( e );
            var rel = el.getAttribute('rel');
            if(!rel) return;  
                       
            rel = eval("(" + rel + ")");
            var section = rel.section;
            
            var  l = renameForm.length;
            for(i = 0; i < l; i++){
                if(renameForm[i].type == 'checkbox' && renameForm[i].id != ''){
                    if(section == undefined || renameForm[i].id.search(section) != -1)
                        renameForm[i].checked = true;
                }
            }
        },
        /**
         * Function: deselectPerSelection
         * Deselects all chechboxes in the renaming form.  
         * If section has value "undefined", deselect all checkboxes.
         *
         * Parameters:
         *     e - The occurred event. 
         */
         deselectPerSection : function( e ){
            var renameForm = this.form;
            var el = KNOWWE.helper.event.target( e );
            
            var rel = el.getAttribute('rel');
            if(!rel) return;             
            
            rel = eval("(" + rel + ")");
            var section = rel.section;            
            
            var l = renameForm.length;
            for(i = 0; i < l; i++){
                if(renameForm[i].type == 'checkbox' && renameForm[i].id != ''){
                    if(section == undefined || renameForm[i].id.search(section) != -1)
                        renameForm[i].checked = false;
                }
            }
         },
        /**
         * Function: preview
         * Sends an request with the entered values and shows the result of the
         * renaming request in a preview table.
         */
        preview : function(){
            if(!_KS('#rename-panel')) return;
            
            var params = {
                action : 'WordBasedRenamingAction',
                TargetNamespace : _KS('#renameInputField').value,
                KWikiFocusedTerm : _KS('#replaceInputField').value,
                ContextPrevious : _KS('#renamePreviousInputContext').value,
                ContextAfter : _KS('#renameAfterInputContext').value,
                CaseSensitive :_KS('#search-sensitive').checked
                //method getSelectedSections() is located in TreeView.js (TreeView.js should be included here eventually)
                //crashes request
                //SelectedSections : JSON.stringify(getSelectedSections())
            }
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                response : {
                    ids : ['rename-result'],
                    fn : function(){
                        if(_KS('.check-select')) {
                             _KS('.check-select').each(function(element){
                                _KE.add('click', element, KNOWWE.core.renaming.selectPerSection );     
                             });
                        }
                            
                        if(_KS('.check-deselect')){
                            _KS('.check-deselect').each(function(element){
                                _KE.add('click', element, KNOWWE.core.renaming.deselectPerSection );     
                             });
                        }
                        if(_KS('#renaming-replace'))
                            _KE.add('click', _KS('#renaming-replace'), KNOWWE.core.renaming.replace );
                        
                        var imgs = _KS('.show-additional-text-renaming');
                        for( var i = imgs.length - 1; i > -1 ; i--){
                            _KE.add('click', imgs[i], KNOWWE.core.renaming.getAdditionalMatchText);
                        }
                        
                        //init sortable table
                        var tableID = _KS('#rename-result table')[0].id;
                        var tblHeader = _KS('#' + tableID + ' thead')[0].getElementsByTagName('th');
                        for( var i = 0; i < tblHeader.length; i++){
                            if(myColumns[i].sortable == "true"){ 
                                var text = tblHeader[i].innerHTML; 
                                tblHeader[i].innerHTML = '<a href="#" onclick="javascript:KNOWWE.core.util.tablesorter.sort(' 
                                    + i + ",'" + tableID + "'" + ');">' + text + '</a>';
                            }
                        }                        
                    }
                }
            }
            new _KA( options ).send();
        },
        /**
         * Function: replace
         * Replace action. Replaces the given string in all the selceted articles.
         */
        replace : function(){
            var codeReplacements = '';
            var inputs = _KS('input');

            for(var i = 0; i < inputs.length; i++) {
                var inputID = inputs[i].id;
                if(inputID.substring(0,11) == 'replaceBox_') {
                    if(inputs[i].checked) {
                        var code = inputID.substring(11);
                        codeReplacements += code + "__";
                    }
                }
            } 
         
            var params = {
                TargetNamespace : _KS('#renameInputField').value,
                action : 'GlobalReplaceAction',
                KWikiFocusedTerm : _KS('#replaceInputField').value
            }
             
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                method : 'post',
                data : 'KWikitext='+codeReplacements,
                response : {
                    action : 'insert',
                    ids : [ 'rename-result' ],
                    fn : function(){ setTimeout ( 'document.location.reload()', 5000 ); }
                }
            }
            new _KA( options ).send();
        },
        /**
         * Function: getAdditionalMatchText
         * Get additional context in which the string occurs. This text is shown
         * in the match column of the renaming tool.
         * 
         * Parameters:
         *     atmUrl - A string containing information which context to expand.
         */ 
        getAdditionalMatchText : function( event ){   
            var el = KNOWWE.helper.event.target( event );
            var rel = el.getAttribute('rel');
            if(!rel) return;             
            rel = eval("(" + rel + ")" );
            
            var atmUrl = rel.article + ":" + rel.section + ":" + rel.index + ":" 
                + rel.words + ":" + rel.direction;
            
            var params = {
                action : 'WordBasedRenamingAction',
                ATMUrl : atmUrl,
                KWikiFocusedTerm : _KS('#replaceInputField').value,
                TargetNamespace : _KS('#renameInputField').value,
                ContextAfter : _KS('#renameAfterInputContext').value,
                ContextPrevious : _KS('#renamePreviousInputContext').value,
                CaseSensitive : _KS('#search-sensitive').value
            }
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                method : 'post',
                response : {
                    action : 'none',
                    fn : function(){ 
                        var id = rel.direction + rel.index;
                        _KS('#'+id).setHTML( request.getResponse() );
                        request = null;
                        _KE.add('click', _KS('#'+id), KNOWWE.core.renaming.getAdditionalMatchText);
                    }
                }
            }
            var request = new _KA( options );
            request.send();
        },
        /**
         * Function: getColumns
         * Returns the columns used in the renaming result table.
         * 
         * Returns:
         *     The columns used in the renaming result table.
         */
        getColumns : function(){
            return myColumns;
        }
    }
}();

/**
 * Namespace: KNOWWE.core.template
 * The KNOWWE template namespace.
 */
KNOWWE.core.template = function(){
    return {
        /**
         * Function: init
         * The template init function. Enables the
         * templatetaghandler and templatetaghandler
         * buttons.
         */
        init : function(){
            //init TemplateTagHandler
            if(_KS('#TemplateTagHandler')) {
                var els = _KS('#TemplateTagHandler input[type=button]');
                for (var i = 0; i < els.length; i++){
                    _KE.add('click', els[i], KNOWWE.core.template.doTemplate); 
                }
            }
            //add generate template button action
            if(_KS('.generate-template').length != 0){
                _KS('.generate-template').each(function( element ){
                    _KE.add('click', element, KNOWWE.core.template.doTemplate);
                });
            }          
        },
        
        /**
         * Function: doTemplate
         * Creates a new Wikipage out of a templateType.
         * 
         * Parameters:
         *     event - The event from the create knowledgebase button. 
         */
        doTemplate : function( event ) {
            var pageName = eval( "(" + _KE.target(event).getAttribute('rel') + ")").jar;

            var params = {
                action : 'TemplateGenerationAction',
                NewPageName : _KS('#' + pageName ).value,
                TemplateName : pageName
            }
            
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    ids : ['TemplateGeneratingInfo']
                }
            }
            new _KA( options ).send();
        }
    }
}();

/**
 * Namespace: KNOWWE.core.typebrowser
 * The KNOWWE typebrowser namespace.
 */
KNOWWE.core.typebrowser = function(){
    return {
        /**
         * Function: init
         * The typebrowser init function. Enables the typebrowser and type 
         * activator buttons.
         */
        init : function(){
            var bttn = _KS('#KnowWEObjectTypeBrowser input[type=button]')[0];
            if( bttn )_KE.add('click', bttn, KNOWWE.core.typebrowser.searchTypes );
            
            bttn = _KS('#KnowWEObjectTypeActivator input[type=button]')[0];   
            if( bttn ) _KE.add('click', bttn, KNOWWE.core.typebrowser.switchTypeActivation );
        },
        /**
         * Function: getAdditionalMatchTextTypeBrowser
         * 
         * Gets additional context around the typebrowser finding. Used to view the
         * context in which the type occurs.
         * 
         * Parameters:
         *     atmUrl - An special URL. Used o transport the position of the finding and how many context elements should be displayed.
         *     query - The query string  for the TypeBrowser action
         */
        getAdditionalMatchTextTypeBrowser : function( event ){
            var el = KNOWWE.helper.event.target( event );
            var rel = el.getAttribute('rel');
            if(!rel) return;             
            rel = eval("(" + rel + ")" );

            var id = rel.direction + rel.index;
            var atmUrl = rel.article + ":" + rel.section + ":" + rel.index + ":" + rel.words + ":"
                + rel.direction + ":" + rel.wordCount;
            
            var params = {
                TypeBrowserQuery : rel.queryLength, //queryLength
                action : 'KnowWEObjectTypeBrowserAction',
                ATMUrl :  atmUrl
            };
            var options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'insert',
                    ids : [ id ]
                }
            }
            var request = new _KA( options );
            request.send();
        },
        /**
         * Function: switchTypeActivation
         * Switches the status of the selected type. 
         * It either enables or disables one type.
         */
        switchTypeActivation : function() {
            if(!_KS('#KnowWEObjectTypeActivator')) return;           
            var params = {
                action : 'KnowWEObjectTypeActivationAction',
                KnowWeObjectType : (function () {
                    var ob = _KS('#KnowWEObjectTypeActivator select')[0];
                    if(ob.selectedIndex){
                        return ob[ob.selectedIndex].value;
                    }
                    return "";
                })()
            }
                
            var options = {
                url : KNOWWE.core.util.getURL ( params ),
                response : {
                    action : 'update',
                    fn : function() {
                            var ob = _KS('#KnowWEObjectTypeActivator select')[0];
                            ob = ob[ob.selectedIndex];
                            if ( ob.style.color == "red") {
                                ob.style.color = "green";
                            } else {
                                ob.style.color = "red";
                            }
                         }
                }
            }
            new _KA( options ).send();
        },
        /**
         * Function: searchTypes
         * Searches for selected type and returns the result that can be viewed
         * in the Wiki page.
         */
        searchTypes : function() {
            if(!document.typebrowser) return;
            var params = {
                action : 'KnowWEObjectTypeBrowserAction',
                TypeBrowserQuery : (function () {
                    var box = document.typebrowser.Auswahl;
                    
                    if(box.selectedIndex){
                        return box.options[box.selectedIndex].value;
                    }
                    return "";
                })()
            }
            var options = {
                url : KNOWWE.core.util.getURL ( params ),
                response : {
                    action : 'insert',
                    ids : ['TypeSearchResult'],
                    fn : function(){
                        _KS('.show-additional-text').each(function(){
                             _KE.add('click', this, KNOWWE.core.typebrowser.getAdditionalMatchTextTypeBrowser);
                        });
                    }
                }
            }
            new _KA( options ).send();
        }        
    }
}();
/**
 * Namespace: KNOWWE.core.codecompletition
 * The KNOWWE code completion tool namespace.
 * Do not use! Will be replaced soon :)
 */
KNOWWE.core.codecompletion = function(){
    return {
        /**
         * Function: init
         * Initializes the code completion functionality.
         * Enables the keydown handler in the editor and insert the auto complete button.
         */
        init : function(){            
            var span = new _KN('span', { 'id' : 'autoCompleteMenu' });
            span.inject(_KS('#submitbuttons'));

            _KE.add('keydown', _KS('#editorarea'), KNOWWE.core.codecompletion.handleKeyEvent );
        },
        /**
         * Function: handleKeyEvent
         * Triggers the codecomplition action after the user pressed STRG + Space.
         * 
         * Parameters:
         *     event - The triggered event
         */
        handleKeyEvent : function( event ){
            _KS('#autoCompleteMenu').style['display'] = 'inline';
            if (event.ctrlKey && event.keyCode == 32) {        
                var textarea = _KS('#editorarea');
                var endPos = KNOWWE.core.util.form.getCursorPositionInTextArea( textarea );         
                
                var tmp = textarea.value.substring(0, endPos);
                var startPos = 0; var i = endPos;
                while( startPos == 0 ) {
                    if(tmp.charAt(i) == '\n' || tmp.charAt(i) == '\r' ||tmp.charAt(i) == ' ') {
                         startPos = i;
                    }
                    i--;
                }
                var encodedData = tmp.substring(startPos + 1);
                
                var params = {
                    action : 'CodeCompletionAction',
                    KWikiWeb : 'default_web',
                    CompletionText : encodedData
                };

                var options = {
                    url : KNOWWE.core.util.getURL( params ),
                    response : {
                        ids : [ 'autoCompleteMenu' ],
                        fn : function(){
                            var el = _KS('#codeCompletion');
                            _KE.add('keydown', el, function(event){
                                var event = event|| window.event;
                                if( event.code == 13){
                                    if( el.selectedIndex == 1) {
                                        el.fireEvent('change');
                                    }
                                }
                            });
                            _KE.add('click', el.options[1], function(){
                                  el.fireEvent('change');
                            });                
                            _KE.add('change', el, function(){
                                    index = el.selectedIndex;
                                    value = el.options[index].value;
                                    KNOWWE.core.util.form.insertAtCursor(_KS('#editorarea'), value);
                            });
                            el.focus();
                        } 
                    }
                }
                new _KA( options ).send();
            }   
        }
    }
}();

/**
 * Namespace: KNOWWE.core.rerendercontent
 * Rerenders parts of the article.
 */
KNOWWE.core.rerendercontent = function(){
    return {
        /**
         * Function: init
         */
        init : function(){
            KNOWWE.helper.observer.subscribe( 'update', KNOWWE.core.rerendercontent.update );
        },
        /**
         * Function: updateNode
         * Updates a node.
         * 
         * Parameters:
         *     node - The node that should be updated.
         *     topic - The name of the page that contains the node.
         */
        updateNode : function(node, topic, ajaxToHTML) {
            var params = {
                action : 'ReRenderContentPartAction',
                KWikiWeb : 'default_web',
                KdomNodeId : node,
                KWiki_Topic : topic,
                ajaxToHTML : ajaxToHTML

            }
            var url = KNOWWE.core.util.getURL( params );
            KNOWWE.core.rerendercontent.execute(url, node, 'insert');
        },
        /**
         * Function: update
         * 
         */
        update : function() {
            var classlist = _KS('.ReRenderSectionMarker');             
            
            if ( classlist.length != 0 ) {
                for (var i = 0; i < classlist.length; i++) {
                    var rel = classlist[i].getAttribute('rel');
                    if(!rel) continue;
                    rel = eval("(" + rel + ")" );
                    
                    var params = {
                        action : 'ReRenderContentPartAction',
                        KWikiWeb : 'default_web',
                        KdomNodeId : rel.id,
                        KWiki_Topic : KNOWWE.helper.gup('page'),
                        ajaxToHTML : "render",
                        inPre : KNOWWE.helper.tagParent(_KS('#' + rel.id), 'pre') != document 
                    }           
                    var url = KNOWWE.core.util.getURL( params );
                    KNOWWE.core.rerendercontent.execute(url, rel.id, 'replace');
                }
            }
        },
        /**
         * Function: execute
         * Sends the rerendercontent AJAX request.
         * 
         * Parameters:
         *     url - The URL for the AJAX request.
         *     id - The id of the node that should be updated.
         */
        execute : function( url, id, action) {
            var options = {
                url : url,
                action : action,
                response : {
                    ids : [ id ],
                    fn : function(){
                        KNOWWE.core.actions.init();
                        Collapsible.render( _KS('#page'), KNOWWE.helper.gup('page'));
                    }
                }
            }
            new _KA( options ).send();
        }
    }
}();

/**
 * Namespace: KNOWWE.plugin
 * The KNOWWE plugin namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin = function(){
    return {
    }
}();

/**
 * Aliases for some often used namespaced function to reduce typing.
 */
var _KE = KNOWWE.helper.event;    /* Alias KNOWWE event. */
var _KA = KNOWWE.helper.ajax;     /* Alias KNOWWE ajax. */
var _KS = KNOWWE.helper.selector; /* Alias KNOWWE ElementSelector */
var _KL = KNOWWE.helper.logger;   /* Alias KNOWWE logger */
var _KN = KNOWWE.helper.element   /* Alias KNOWWE.helper.element */
var _KH = KNOWWE.helper.hash      /* Alias KNOWWE.helper.hash */

/* ############################################################### */
/* ------------- Onload Events  ---------------------------------- */
/* ############################################################### */
(function init(){
    
    window.addEvent( 'domready', _KL.setup );

    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
            KNOWWE.core.init();
            KNOWWE.core.renaming.init(); 
            KNOWWE.core.util.tablesorter.init();
            KNOWWE.core.typebrowser.init();
            KNOWWE.core.rerendercontent.init();
            KNOWWE.core.template.init();

            if(_KS('#testsuite-show-extend')){
                _KE.add('click', _KS('#testsuite-show-extend'), KNOWWE.core.util.form.showExtendedPanel);
            }
            if(_KS('#testsuite2-show-extend')){
                _KE.add('click', _KS('#testsuite2-show-extend'), KNOWWE.core.util.form.showExtendedPanel);
            }
            setTimeout(function(){KNOWWE.helper.observer.notify('onload')}, 50);
            setTimeout(function(){KNOWWE.helper.observer.notify('update')}, 50);
        });
    };
}());
