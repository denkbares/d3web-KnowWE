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
				article : KNOWWE.helper.getPagename(),
				onNodeExpand : nodeExpand,
				onNodeCollapse : nodeCollapse
			});
		};

		function nodeExpand() {
			var sectionID = jq$(jq$("tr[data-tt-id='" + this.id + "']").closest('.sparqlTable')[0]).attr('sparqlsectionid');
			getNodeViaAjax(this.id, sectionID);
			_TM.decorateToolMenus();
		};

		function nodeCollapse() {
		};

		function getNodeViaAjax(parentNodeID, sectionID) {

			jq$.ajax({
				type : 'post',
				url : 'action/LoadTreeChildrenSparqlAction',
				data : {
					ParentNodeID : parentNodeID,
					SectionID : sectionID
				},
				success : function(data) {
					var children = jq$(data);
					var parentID = jq$(data).attr("data-tt-parent-id");
					var parentNode = jq$("tr[data-tt-id='" + parentID + "']");
					var table = parentNode.parents("table");
					var parent = table.treetable("node", parentID);

					for (var i = 0; i < children.length; i++) {
						var child = children[i];
						var nodeToAdd = table.treetable("node", jq$(child).attr("data-tt-id"));

						// Check if node already exists. If not, add it to parent node
						if (!nodeToAdd) {
							table.treetable("loadBranch", parent, child);
						}

					}
				}
			})
		}

		jq$(document).ready(initTreeFunc);
		KNOWWE.helper.observer.subscribe('afterRerender', initTreeFunc);
	}
}());