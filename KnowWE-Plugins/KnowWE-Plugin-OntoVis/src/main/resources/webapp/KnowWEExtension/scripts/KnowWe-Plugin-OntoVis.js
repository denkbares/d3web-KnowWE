/**
 * Title: KnowWE-Plugin-OntoVis Contains all javascript functions concerning
 * the KnowWE-Plugin-OntoVis.
 */

KNOWWE.plugin.ontovis = {};

KNOWWE.plugin.ontovis.retry = function(id) {
	jq$.ajax({
		url : 'action/OntoVisReRenderAction',
		type : 'post',
		cache : false,
		data : {SectionID : id}
	}).success(function(data) {
		jq$('#' + id).rerender();
	});
};