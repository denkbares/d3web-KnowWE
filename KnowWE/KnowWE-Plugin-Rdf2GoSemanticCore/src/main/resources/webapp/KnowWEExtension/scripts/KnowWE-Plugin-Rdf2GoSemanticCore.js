/**
 * Title: KnowWE-Plugin-Semantic Contains all javascript functions concerning
 * the KnowWE-Plugin-SemanticCore.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	/**
	 * The KNOWWE global namespace object. If KNOWWE is already defined, the
	 * existing KNOWWE object will not be overwritten so that defined namespaces
	 * are preserved.
	 */
	var KNOWWE = {};
}
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
	/**
	 * The KNOWWE.plugin global namespace object. If KNOWWE.plugin is already
	 * defined, the existing KNOWWE.plugin object will not be overwritten so
	 * that defined namespaces are preserved.
	 */
	KNOWWE.plugin = function() {
		return {}
	}
}
/**
 * Namespace: KNOWWE.plugin.semantic The KNOWWE plugin d3web namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin.semantic = function() {
	return {}
}();

/**
 * Namespace: KNOWWE.plugin.semantic.action The namespace of the semantic things
 * in KNOWWE.
 */
KNOWWE.plugin.sparql = function() {


}();

(function init() {
	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		jq$(window).ready(function() {
			var ns = KNOWWE.plugin.semantic;
			for (var i in ns) {
				if (ns[i].init) {
					ns[i].init();
				}
			}
		});
		var initTreeFunc = function() {
			jq$(this).find(".sparqltreetable").agikiTreeTable({
				expandable : true,
				clickableNodeNames : true,
				persist : true,
				article : KNOWWE.helper.getPagename()
			});
		};
		jq$(document).ready(initTreeFunc);
		KNOWWE.helper.observer.subscribe('afterRerender', initTreeFunc);
	}
}());