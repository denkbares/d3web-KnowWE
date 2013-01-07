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
		init : function() {
			// init renaming form button
			button = _KS('#objectinfo-replace-button');
			if (button)
				_KE.add('click', button,
						KNOWWE.core.plugin.objectinfo.renameTerm);
			jq$('#objectinfo-search').keyup(function() {
				KNOWWE.core.plugin.objectinfo.lookUp();
			});
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
							window.location = "Wiki.jsp?page="
									+ objectName.innerHTML
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
			
			//TODO shouldn't these 3 be vars?
			objectname = jq$('#objectinfo-target');
			replacement = jq$('#objectinfo-replacement');
			web = jq$('#objectinfo-web');
			if (objectname && replacement && web) {
				var changeNote = 'Renaming: "' + objectname.val() + '" -> "' + replacement.val() +'"';
				var params = {
					action : jq$(replacement).attr('action'),
					termname : objectname.val(),
					termreplacement : replacement.val(),
					KWikiWeb : web.val(),
					KWikiChangeNote: changeNote
				}

				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'none',
						fn : function() {
							var jsonResponse = JSON.parse(this.responseText);
							window.location.href = "Wiki.jsp?page=ObjectInfoPage&objectname="
									+ encodeURIComponent(jsonResponse.newObjectName)
									+ "&termIdentifier="
									+ encodeURIComponent(jsonResponse.newTermIdentifier)
									+ "&renamedArticles="
									+ encodeURIComponent(jsonResponse.renamedArticles);
							KNOWWE.core.util.updateProcessingState(-1);
						},
						onError : function() {
							KNOWWE.core.util.updateProcessingState(-1);
						}
					}
				}
				KNOWWE.core.util.updateProcessingState(1);
				new _KA(options).send();
			}
		},
		
		/**
		 * shows a list of similar terms
		 */
		lookUp : function() {
			
			web = jq$('#objectinfo-web');
			if (web) {
				var params = {
				    searchstring : jq$('#objectinfo-search').val(),
					action : jq$('#objectinfo-search').attr('action'),
					KWikiWeb : web.val(),
				}

				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'none',
						fn : function() {
							var jsonResponse = JSON.parse(this.responseText);
							var a = jsonResponse.allTerms;
							jq$('#objectinfo-search').autocomplete({source:a});
						}
					}
				}
				new _KA(options).send();
			}
		}
	}
}();

KNOWWE.core.plugin.renderKDOM = function() {

jq$('.table_text').hover(
    function() {
    var that = this;
    setTimeout( function(){
        jq$(that).css('height', that.scrollHeight);
},0);
    	//alert(this.scrollHeight);
    },
    function() {
        jq$(this).css('height', '18px');
    }
    

);
};

/* ############################################################### */
/* ------------- Onload Events ---------------------------------- */
/* ############################################################### */
(function init() {
	
	window.addEvent('domready', _KL.setup);
	if (KNOWWE.helper.loadCheck([ 'Wiki.jsp' ])) {
		window.addEvent('domready', function() {
			KNOWWE.core.plugin.objectinfo.init();
			KNOWWE.core.plugin.renderKDOM();

		});
	}
	;
}());