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
		/**
		 * Function: createHomePage Used in the ObjectInfoToolProvider for
		 * creating homepages for KnowWEObjects
		 * 
		 * Parameters: e - The occurred event.
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

		}
	}
}();