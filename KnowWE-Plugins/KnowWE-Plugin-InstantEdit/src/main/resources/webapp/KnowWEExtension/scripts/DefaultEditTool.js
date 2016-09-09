KNOWWE.plugin.defaultEditTool = function() {
	
	function createTextAreaID(id) {
		return "defaultEdit" + id;
	}
	
    return {
    	
	    generateHTML : function(id) {
	    	return "<textarea id = " + createTextAreaID(id) + " class='defaultEditTool' style='height: " + jq$('#' + id)[0].clientHeight + "px;'>"
		    	+ _EC.encodeForHtml(_EC.getWikiText(id))
		    	+ "</textarea>";
	    },
	    
	    generateButtons : function(id) {
	    	return _EC.elements.getSaveCancelDeleteButtons(id);
	    },
	    
	    postProcessHTML : function(id) {
	    	var textarea = jq$('#' + createTextAreaID(id))[0];
	    	if (typeof AutoComplete != "undefined") {	    		
	    		new AutoComplete(textarea);
	    	}
	    	new TextArea(textarea);
	        
	        textarea.focus();
	        jq$(textarea).autosize({append: "\n"});
//	        while (textarea.clientHeight == textarea.scrollHeight) {
//	        	var tempHeight = textarea.style.height; 
//	        	textarea.style.height = textarea.clientHeight - 5 + "px";
//	        	// abort if we are below minHeight and the height does not change anymore
//	        	if (textarea.style.height == tempHeight) break;
//	        }
//	        textarea.style.height = textarea.scrollHeight + 15 + "px";
	    },
	    
	    unloadCondition : function(id) {
	    	var textArea = jq$('#' + createTextAreaID(id))[0];
			if (textArea) {
	    		return textArea.defaultValue == textArea.value;
	    	}
	    	return true;
	    },
	    
	    generateWikiText : function(id) {
	    	if (jq$('#' + createTextAreaID(id))[0]) {
	    		return jq$('#' + createTextAreaID(id))[0].value;
	    	} else {
	    		return _EC.getWikiText(id);
	    	}
	    }
    }
}();