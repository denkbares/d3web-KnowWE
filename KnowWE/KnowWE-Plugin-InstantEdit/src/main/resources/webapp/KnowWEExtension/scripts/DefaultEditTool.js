KNOWWE.plugin.defaultEditTool = function() {
	
	function createTextAreaID(id) {
		return "defaultEdit" + id;
	}
	
    return {
    	
	    generateHTML : function(id) {
	    	return "<textarea id = " + createTextAreaID(id) + " class='defaultEditTool' style='height: " + $(id).clientHeight + "px;'>" 
		    	+ KNOWWE.plugin.instantEdit.getWikiText(id)
		    	+ "</textarea>"
		    	+ KNOWWE.plugin.instantEdit.getSaveCancelDeleteButtons(id);
	    },
	    
	    postProcessHTML : function(id) {
	    	var textarea = $(createTextAreaID(id));
	    	if (typeof AutoComplete != "undefined") AutoComplete.initialize(textarea);
	        TextArea.initialize(textarea);
	        
	        KNOWWE.plugin.instantEdit.disableDefaultEditTool();
	        
	        textarea.focus();
	        textarea.style.height = (textarea.scrollHeight + 15) + "px";
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