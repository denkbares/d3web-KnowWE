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
        	var toolsUnloadCondition = _IE.toolNameSpace[id].unloadCondition;
			if (toolsUnloadCondition != null && !toolsUnloadCondition(id)) {
				return "edit.areyousure".localize();
			}
		}).bind(this);
        
        window.onunload = (function() {
            _IE.disable(id, false);
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
	
	function hideTools() {
		$$('.markupTools').setStyle("display", "none");
	}
	
	function showAjaxLoader(id) {
		var ajaxLoaderGif = new Element("img", {
			'id' : 'instantedit_loader',
			'src' : 'KnowWEExtension/images/ajax-100.gif',
			'class' : 'ajaxloader',
		});
		$(id).appendChild(ajaxLoaderGif);
	}
	
	function hideAjaxLoader() {
		var ajaxLoaderGif = $('instantedit_loader');
		if (ajaxLoaderGif) {
			ajaxLoaderGif.parentNode.removeChild(ajaxLoaderGif);
		}
	}
	
	function enabledWarning() {
    	if (_IE.enabled) {
    		alert("You can only edit the page once at a time.")
    		return;
    	}
	}
	
    return {
    	
    	toolNameSpace : new Object(),
    	
    	enabled : false,
    	
        enable : function(id, toolNameSpace) {
        	
        	if (_IE.enabled) {
        		enabledWarning();
        		return;
        	}
        	
        	showAjaxLoader(id);
        	
        	_IE.toolNameSpace[id] = toolNameSpace;
        	
            var params = {
                action : 'InstantEditEnableAction',
                KdomNodeId : id,
            };        
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                response : {
                    action : 'none',
                    fn : function() {
                    	if (_IE.enabled) {
                    		enabledWarning();
                    		return;
                    	}
                    	_IE.enabled = true;
                    	
                    	var locked = JSON.parse(this.responseText).locked;
                    	var html = toolNameSpace.generateHTML(id);
                        html = wrapHTML(id, locked, html);
                        
                        KNOWWE.core.util.replace(html);
                        
                        toolNameSpace.postProcessHTML(id);
                        bindUnloadFunctions(id);
                        hideTools();
                    },
                    onError : _IE.onErrorBehavior,
                }
            };
            new _KA(options).send();
            
        	hideAjaxLoader();
        },
        
        
        disable : function(id, reload) {
            var params = {
                action : 'InstantEditDisableAction',
                KdomNodeId : id,
            }           
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                async : false,
                response : {
                    action : 'none',
	                onError : _IE.onErrorBehavior,
                }
            }
            new _KA(options).send(); 
            
            if(reload) {
            	window.location = window.location.href.split('&')[0];
            }
        },
        
        /**
		 * Save the changes to the article.
		 * 
		 * @param id is the id of the DOM element
		 */
        save : function(id, newWikiText) {
        	
        	showAjaxLoader(id);
            
        	if (newWikiText == null) {
        		newWikiText =  _IE.toolNameSpace[id].generateWikiText(id);        		
        	}
        	
            var params = {
                action : 'InstantEditSaveAction',
                KdomNodeId : id,
            }

            _IE.sendChanges(id, params, newWikiText); 
        },
        
        /**
		 * Adds a new article with the given articleText. The title of the new 
		 * article is given by the current tools function getNewArticleTitle();
		 * 
		 * @param id is the id of the source DOM element
		 */
        add : function(id, title, newWikiText) {
        	
        	showAjaxLoader(id);
            
        	if (newWikiText == null) {
        		newWikiText =  _IE.toolNameSpace[id].generateWikiText(id);        		
        	}
        	if (title == null) {        		
        		var title = _IE.toolNameSpace[id].getNewArticleTitle(id); 	
        	}
        	
            var params = {
                action : 'InstantEditAddArticleAction',
                KdomNodeId : id,
				KWiki_Topic : title,
            }
            
            _IE.sendChanges(id, params, newWikiText);
            
        },
        
        sendChanges : function(id, params, newWikiText) {

            var options = {
                url : KNOWWE.core.util.getURL(params),
                data : newWikiText,
                response : {
                    action : 'none',
                    fn : function() {
                    	window.onbeforeunload = null;
                        window.onunload = null;
                        _IE.disable(id, true);
                    },
                    onError : _IE.onErrorBehavior,
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
    		_IE.disable(id, true);
        },
        
        deleteSection : function(id) {
        	var del = confirm("Do you really want to delete this content?");
        	if (del) {  
        		_IE.save(id, "");
        	}
        }, 
        
        deleteArticle : function(id) {
        	var del = confirm("Do you really want to delete this content?");
        	if (del) {
        		var title = _IE.toolNameSpace[id].getCurrentArticleTitle(id); 	
        		_IE.add(id, title, "");
        	}
        }, 
        
    	wikiText : new Object(),
    	
        getWikiText : function(id, actionName) {
        	
        	var tempWikiText = _IE.wikiText[id];
        	
        	if (tempWikiText != null) return tempWikiText;
        	
        	if (actionName == null) actionName = 'GetWikiTextAction';
        	
        	var params = {
                action : actionName,
                KdomNodeId : id,
            };         
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                async : false,
                response : {
                   action : 'none',
                   // for FF 3.6 compatibility, we can't use the function fn
                   // in synchronous call (no onreadystatechange event fired)
                   onError : _IE.onErrorBehavior,
               }
            };
            var ajaxCall = new _KA(options);
            ajaxCall.send();
            _IE.wikiText[id] = ajaxCall.getResponse();
            return _IE.wikiText[id];
        },
        
        // Maybe return given messages instead
        onErrorBehavior : function() {
        	hideAjaxLoader();
        	if (this.status == null) return;
        	switch (this.status) {
        	  case 0:
        		// server not running, do nothing.
        		break;
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
        	    alert("Error " + this.status + ". Please reload the page.");
        	    break;
        	}
        },
        
    	getSaveButton : function(id) {
    		return "<a class=\"action save\" " 
    			+ "href=\"javascript:_IE.save('"
    			+ id
    			+ "')\">Save</a>";
    	},
    	
    	getAddArticleButton : function(id) {
    		return "<a class=\"action add\" " 
    			+ "href=\"javascript:_IE.add('"
    			+ id
    			+ "')\">Save</a>";
    	},
    	
    	getCancelButton : function(id) {
    		return "<a class=\"action cancel\" "
    			+ "href=\"javascript:_IE.cancel('"
    			+ id
    			+ "')\">Cancel</a>";
    	},
    	
    	getDeleteSectionButton : function(id) {
    		return "<a class=\"action delete\"" 
    			+ "href=\"javascript:_IE.deleteSection('"
    			+ id
    			+ "')\">Delete</a>";
    	},
    	
    	getDeleteArticleButton : function(id) {
    		return "<a class=\"action delete\"" 
    			+ "href=\"javascript:_IE.deleteArticle('"
    			+ id
    			+ "')\">Delete</a>";
    	},
    	
    	getButtonsTable : function(buttons) {
    		var table = "<div class='buttons'><table><tr>";
    		for (var i = 0; i < buttons.length; i++) {
    			table += "<td>" + buttons[i] + "</td>\n";
    		}
    		table += "</tr></table></div>";
    		return table;
    	},
    	
    	getSaveCancelDeleteButtons : function(id, additionalButtonArray) {
    		var array = new Array(
    				_IE.getSaveButton(id),
    				_IE.getCancelButton(id),
    				"       ",
    				_IE.getDeleteSectionButton(id));

    		if (additionalButtonArray) {
    			array.push("       ");
    			array = array.concat(additionalButtonArray);
    		}
    		return _IE.getButtonsTable(array);
    	},
        
        handleEditToolButtonVisibility : function() {
    		
    		var params = {
    				action : 'CheckCanEditPageAction',
    		}           
    		
    		var options = {
    				url : KNOWWE.core.util.getURL(params),
    				response : {
    					action : 'none',
    					fn : function() {
    						var canedit = JSON.parse(this.responseText).canedit;
    						
    						if(canedit) {
    							_IE.enableDefaultEditTool();
    						} else {
    							_IE.disableDefaultEditTool();
    						}
    					},
    					onError : _IE.onErrorBehavior,
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

/**
 * Alias for some to reduce typing.
 */
var _IE = KNOWWE.plugin.instantEdit; 

_IE.handleEditToolButtonVisibility();
