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
						action : 'none',
						fn : function() {
							window.location.href = "Wiki.jsp?page=ObjectInfoPage&objectname="
									+ encodeURIComponent(replacement.value)
									+ "&renamedArticles="
									+ encodeURIComponent(this.responseText);
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
		}
	}
}();

/* ############################################################### */
/* ------------- Onload Events ---------------------------------- */
/* ############################################################### */
(function init() {

	window.addEvent('domready', _KL.setup);

	if (KNOWWE.helper.loadCheck([ 'Wiki.jsp' ])) {
		window.addEvent('domready', function() {
			KNOWWE.core.plugin.objectinfo.init();
		});
	}
	;
}());