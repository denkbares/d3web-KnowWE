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
 * Namespace: KNOWWE.core.plugin.include
 */
KNOWWE.plugin.include = {};

KNOWWE.plugin.include.updateVersion = function(sectionID, newVersion) {
	var params = {
		action : 'ReplaceKDOMNodeAction',
		TargetNamespace : sectionID,
		KWikitext : newVersion,
	};
	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
			fn : function() {
				location.reload();
			},
			onError : function() {
				if (this.status == null) return;
				switch (this.status) {
				case 0:
					KNOWWE.notification.error(null, 
							"Server appears to be offline.", status);
					break;
				case 404:
					KNOWWE.notification.error(null, 
							"This page no longer exists. Please reload.", status);
					break;
				case 403:
					KNOWWE.notification.error(null,
							"You do not have the permission to edit this page.", status);
					break;
				default:
					KNOWWE.notification.error(null,
							"Unexpected error " + this.status + ". Please reload the page.", status);
					break;
				}
			}
		}
	}
	new _KA(options).send();
};