/**
 * Title: KnowWE-Plugin-Semantic Contains all javascript functions concerning
 * the KnowWE-Plugin-SemanticCore.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
	KNOWWE.plugin = function() {
		return {}
	}
}
KNOWWE.plugin.sparql = {};

KNOWWE.plugin.sparql.retry = function(id) {
	jq$.ajax({
		url : 'action/ClearCachedSparqlAction',
		type : 'post',
		cache : false,
		data : {SectionID : id}
	}).success(function(data) {
		jq$('#' + id).rerender();
	});
};

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