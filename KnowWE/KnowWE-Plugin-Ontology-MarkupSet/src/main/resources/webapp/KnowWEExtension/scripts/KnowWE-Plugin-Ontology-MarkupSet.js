/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

var toSelect;
/**
 * The KNOWWE.plugin global namespace object. If KNOWWE.plugin is already
 * defined, the existing KNOWWE.plugin object will not be overwritten so that
 * defined namespaces are preserved.
 */
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
	KNOWWE.plugin = function() {
		return {}
	}
}

/**
 * The KNOWWE.plugin.ontology global namespace object. If KNOWWE.plugin.ontology
 * is already defined, the existing KNOWWE.plugin.ontology object will not be
 * overwritten so that defined namespaces are preserved.
 */
KNOWWE.plugin.ontology = function() {
	return {
		commitOntology : function(sectionID) {
			new _KA({
				url : KNOWWE.core.util.getURL({
					action : 'CommitOntologyAction',
					SectionID : sectionID
				}),
				fn : function() {
					KNOWWE.core.util.hideProcessingIndicator();
					location.reload();
				}
			}).send();
			KNOWWE.core.util.showProcessingIndicator();
		},
		expandLazyReference : function(sectionID, newReferenceText, rerenderID) {
			var params = {
				action : 'ReplaceKDOMNodeAction',
				TargetNamespace : sectionID,
				KWikitext : newReferenceText,
			};
			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					fn : function() {
						// todo: rerender markup block
						location.reload();
					}
				}
			}
			new _KA(options).send();
		}
	}
}();
