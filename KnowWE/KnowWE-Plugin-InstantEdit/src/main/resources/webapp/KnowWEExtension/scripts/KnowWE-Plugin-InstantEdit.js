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
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
    KNOWWE.plugin = {};
}

/**
 * Namespace: KNOWWE.core.plugin.instantedit The KNOWWE instant edit namespace.
 */
KNOWWE.plugin.instantEdit = function() {
    
	 function bindUnloadFunctions(id) {
        window.onbeforeunload = (function() {
        	var toolsUnloadCondition = KNOWWE.plugin.instantEdit.toolNameSpace[id].unloadCondition;
			if (toolsUnloadCondition != null && !toolsUnloadCondition(id)) {
				return "edit.areyousure".localize();
			}
		}).bind(this);
        
        window.onunload = (function() {
            KNOWWE.plugin.instantEdit.disable(id, false);
		}).bind(this);
	}

	function wrapHTML(id, locked, html) {
		var lockedHTML = "";
		if (locked) {
			lockedHTML = "<div class=\"error\">Another user has started to edit this page, but "
				 + "hasn't yet saved it. You are allowed to further edit this page, but be "
				 + "aware that the other user will not be pleased if you do so!</div>"
		}
		var openingDiv = "<div id='" + id + "' class='editarea'>";
		var closingDiv = "</div>\n";
		
		return openingDiv + lockedHTML + html + closingDiv;
	}
	 
    return {
    	
    	toolNameSpace : new Object(),
    	
        enable : function(id, toolNameSpace) {
        	
        	KNOWWE.plugin.instantEdit.toolNameSpace[id] = toolNameSpace;
        	
            var params = {
                action : 'InstantEditEnableAction',
                KdomNodeId : id,
            };        
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                response : {
                    action : 'none',
                    fn : function() {
                    	
                    	enableResponse = JSON.parse(this.responseText);
                    	if (enableResponse.success) {

                        	var html = toolNameSpace.generateHTML(id);
                            html = wrapHTML(id, enableResponse.locked, html);
                            
                            KNOWWE.core.util.replace(html);
                            
                            toolNameSpace.postProcessHTML(id);
                            bindUnloadFunctions(id);
                    	}
                    },
                    onError : KNOWWE.plugin.instantEdit.onErrorBehavior,
                }
            };
            new _KA(options).send();
        },
        
        
        disable : function(id, reload) {
            var params = {
                action : 'InstantEditDisableAction',
                KdomNodeId : id,
            }           
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                async : false, // hotfix for chrome (onunload does not wait for
								// async call)
                response : {
                    action : 'none',
                    fn : function() {
                        var enabled = JSON.parse(this.responseText);
                        enabled = enabled.success;
                        
                        if(enabled && reload) {
                        	window.location.reload();
                        }
                    },
	                onError : KNOWWE.plugin.instantEdit.onErrorBehavior,
                }
            }
            new _KA(options).send();            
        },
        
        /**
		 * Save the changes to the article.
		 * 
		 * @param String
		 *            id The id of the DOM element
		 * @param String
		 *            value The old text of the section
		 */
        save : function(id){
                
        	var newWikiText =  KNOWWE.plugin.instantEdit.toolNameSpace[id].generateWikiText(id);
        	
            var params = {
                action : 'InstantEditSaveAction',
                KdomNodeId : id,
            }           

            var options = {
                url : KNOWWE.core.util.getURL(params),
                data : newWikiText,
                response : {
                    action : 'none',
                    fn : function() {
                        var enabled = JSON.parse(this.responseText);
                        enabled = enabled.success;

                        if( enabled ) {
                        	window.onbeforeunload = null;
                            window.onunload = null;
                            KNOWWE.plugin.instantEdit.disable(id, true);
                        }
                    },
                    onError : KNOWWE.plugin.instantEdit.onErrorBehavior,
                }
            }
            new _KA(options).send();  
        },
        /**
		 * Cancel the instant edit action. Restore the original text.
		 * 
		 * @param String
		 *            id The id of the DOM element
		 */        
        cancel : function(id) {
            KNOWWE.plugin.instantEdit.disable(id, true);
        },
        
        
    	wikiText : new Object(),
    	
        getWikiText : function(id) {
        	
        	var tempWikiText = KNOWWE.plugin.instantEdit.wikiText[id];
        	
        	if (tempWikiText != null) return tempWikiText;
        	
        	var params = {
                action : 'GetWikiTextAction',
                KdomNodeId : id,
            };        
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                async : false,
                response : {
                   action : 'none',
                   fn : function() {
                	   // var cleanedUpWikiText = this.responseText.replace(
                	   // new RegExp( "\\r", "g" ),
                	   // ""
                	   // );
                	   KNOWWE.plugin.instantEdit.wikiText[id] = this.responseText;
                   },
                   onError : KNOWWE.plugin.instantEdit.onErrorBehavior,
               }
            };
            new _KA(options).send();
            
            return KNOWWE.plugin.instantEdit.wikiText[id];
        },
        
        onErrorBehavior : function() {
        	if (this.status == null) return;
        	switch (this.status) {
        	  case 403:
          	    alert("You are not authorized to change this page.");
        	    break;
        	  case 404:
            	alert("This page no longer exists. Please reload.");
        	    break;
        	  case 409:
          	    alert("This section has changed since you " 
          	    		+ "loaded this page. Please reload the page.");
        	    break;
        	  default:
        	    alert("Unknown error. Please reload the page.");
        	    break;
        	}
        },
        
    	getButtons : function(id) {
    		var saveButton = "<a class=\"action save\" href=\"javascript:KNOWWE.plugin.instantEdit.save('"
    			+ id
    			+ "')\">Save</a>\n";
    		var cancelButton = "<a class=\"action cancel\" href=\"javascript:KNOWWE.plugin.instantEdit.cancel('"
    			+ id
    			+ "')\">Cancel</a>\n";
    		
    		// maybe return a object with these two buttons
    		return "<div class='saveCancelButtons'><table><tr><td>" + saveButton + "</td><td>" +  cancelButton + "</td></tr></table></div>";
    	},
        
        handleEditToolButtonVisibility : function() {
    		
    		var params = {
    				action : 'CheckCanEditPageAction',
    				KWiki_Topic : KNOWWE.helper.gup('page'),
    		}           
    		
    		var options = {
    				url : KNOWWE.core.util.getURL(params),
    				response : {
    					action : 'none',
    					fn : function() {
    						var show = JSON.parse(this.responseText);
    						show = show.success;
    						
    						if(!show) {
    							KNOWWE.plugin.instantEdit.disableDefaultEditTool();
    						} else {
    							KNOWWE.plugin.instantEdit.enableDefaultEditTool();
    						}
    					},
    					onError : KNOWWE.plugin.instantEdit.onErrorBehavior,
    				}
    		}
    		new _KA(options).send();  
    	},
    	
    	disableDefaultEditTool : function() {
			$$('div.DefaultEditTool').setStyle("display", "none");
    	},
    	
    	enableDefaultEditTool : function() {
    		$$('div.DefaultEditTool').setStyle("display", null);
    	}
    }
}();

KNOWWE.plugin.instantEdit.handleEditToolButtonVisibility();
