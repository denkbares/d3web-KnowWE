KNOWWE.plugin.defaultEditTool = function() {
	
	function createTextAreaID(id) {
		return "defaultEdit" + id;
	}
	
    return {
    	
	    generateHTML : function(id) {
	    	return "<textarea id = " + createTextAreaID(id) + " class='defaultEditTool' style='height: " + $(id).clientHeight + "px;'>"
		    	+ _IE.getWikiText(id)
		    	+ "</textarea>"
		    	+ _IE.getSaveCancelDeleteButtons(id);iu
	    },
	    
	    postProcessHTML : function(id) {
	    	var textarea = $(createTextAreaID(id));
	    	if (typeof AutoComplete != "undefined") AutoComplete.initialize(textarea);
	        TextArea.initialize(textarea);
	        
	        textarea.focus();
	        while (textarea.clientHeight == textarea.scrollHeight) {
	        	var tempHeight = textarea.style.height; 
	        	textarea.style.height = textarea.clientHeight - 5 + "px";
	        	// abort if we are below minHeight and the height does not change anymore
	        	if (textarea.style.height == tempHeight) break;
	        }
	        textarea.style.height = textarea.scrollHeight + 15 + "px";
	    },
	    
	    unloadCondition : function(id) {
	    	var textArea = $(createTextAreaID(id));
			return textArea.defaultValue == textArea.value;
	    },
	    
	    generateWikiText : function(id) {
	    	return $(createTextAreaID(id)).value;
	    }
    }
}();