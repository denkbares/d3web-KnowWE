/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.core == "undefined" || !KNOWWE.core) {
	KNOWWE.core = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.core.plugin == "undefined" || !KNOWWE.core.plugin) {
	KNOWWE.core.plugin = {};
}

/**
 * Namespace: KNOWWE.core.plugin.objectinfo The KNOWWE object info namespace.
 */
KNOWWE.core.plugin.objectinfo = function() {
	return {
		init : function(){       
            //init renaming form button
			button = _KS('#objectinfo-replace-button');
			if (button) _KE.add('click', button, KNOWWE.core.plugin.objectinfo.renameTerm);             
        },
		
		/**
		 * Function: createHomePage Used in the ObjectInfoToolProvider for
		 * creating homepages for KnowWEObjects
		 */
		createHomePage : function() {
			objectName = _KS('#objectinfo-src');
			if (objectName) {
				var params = {
					action : 'CreateObjectHomePageAction',
					objectname : objectName.innerHTML
				}

				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'none',
						fn : function() {
							window.location = "Wiki.jsp?page=" + objectName.innerHTML
						}
					}
				}
				new _KA(options).send();
			}

		},
		
		/**
		 * Renames all occurrences of a specific term.
		 */
		renameTerm : function() {
			objectname = _KS('#objectinfo-target');
			replacement = _KS('#objectinfo-replacement');
			web = _KS('#objectinfo-web');
			if (objectname && replacement && web) {
				var params = {
					action : 'TermRenamingAction',
					termname : objectname.value,
					termreplacement : replacement.value,
					KWikiWeb : web.value
				}

				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'insert',
	                    ids : ['objectinfo-rename-result']
					}
				}
				new _KA(options).send();
			}
		}
	}
}();

/**
 * Namespace: KNOWWE.core.plugin.renaming
 * The KNOWWE renaming tool plugin object.
 */
KNOWWE.core.plugin.renaming = function(){
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
                _KE.add('click', bttn, KNOWWE.core.plugin.renaming.preview );
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
            }
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                //method getSelectedSections() is located in TreeView.js (TreeView.js should be included here eventually)
                data : "SelectedSections=" + JSON.stringify(getSelectedSections()),
                response : {
                    ids : ['rename-result'],
                    fn : function(){
                        if(_KS('.check-select')) {
                             _KS('.check-select').each(function(element){
                                _KE.add('click', element, KNOWWE.core.plugin.renaming.selectPerSection );     
                             });
                        }
                            
                        if(_KS('.check-deselect')){
                            _KS('.check-deselect').each(function(element){
                                _KE.add('click', element, KNOWWE.core.plugin.renaming.deselectPerSection );     
                             });
                        }
                        if(_KS('#renaming-replace'))
                            _KE.add('click', _KS('#renaming-replace'), KNOWWE.core.plugin.renaming.replace );
                        
                        var imgs = _KS('.show-additional-text-renaming');
                        for( var i = imgs.length - 1; i > -1 ; i--){
                            _KE.add('click', imgs[i], KNOWWE.core.plugin.renaming.getAdditionalMatchText);
                        }
                        
                        //init sortable table
                        var tableID = _KS('#rename-result table')[0].id;
                        var tblHeader = _KS('#' + tableID + ' thead')[0].getElementsByTagName('th');
                        for( var i = 0; i < tblHeader.length; i++){
                            if(myColumns[i].sortable == "true"){ 
                                var text = tblHeader[i].innerHTML; 
                                tblHeader[i].innerHTML = '<a href="#" onclick="javascript:KNOWWE.tablesorter.sort(' 
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
                        _KE.add('click', _KS('#'+id), KNOWWE.core.plugin.renaming.getAdditionalMatchText);
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

/* ############################################################### */
/* ------------- Onload Events  ---------------------------------- */
/* ############################################################### */
(function init(){
    
    window.addEvent( 'domready', _KL.setup );

    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
        	KNOWWE.core.plugin.objectinfo.init();
        	KNOWWE.core.plugin.renaming.init();
        });
    };
}());