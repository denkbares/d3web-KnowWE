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

	function rerender(id, parameters) {
		if (!parameters) parameters = {};
		parameters.SectionID = id;
		jq$.ajax({
			url : KNOWWE.core.util.getURL({
				action : 'RefreshSparqlAction'
			}),
			data : parameters,
			cache : false,
			type : 'post'
		}).success(function(result) {
			jq$('#sparqlTable_' + id).replaceWith(result.trim());
		});
	}

	return {

		refresh : function(id, paremeters) {

			var root = jq$.parseJSON(jq$.cookie("SparqlRenderer-" + id));
			if (root == null) {
				root = {};
			}
			var showLines = jq$('#showLines' + id).val();
			var fromLine = jq$('#fromLine' + id).val();
			var search = /^\d+$/;
			var found = search.test(fromLine);
			if (jq$('#fromLine' + id).exists() && !(found)) {
				jq$('#fromLine').val('');
				return;
			}
			if (fromLine <= 0) {
				fromLine = 1;
			}
			if (showLines == "All") {
				root.navigationOffset = 1;
				root.navigationLimit = "All";
			}
			else {
				root.navigationOffset = fromLine;
				root.navigationLimit = showLines;
			}
			var cookieStr = JSON.stringify(root);
			jq$.cookie("SparqlRenderer-" + id, cookieStr);
			rerender(id, paremeters);
		},

		forward : function(id, element) {
			var showInput = jq$('#showLines' + id);
			var show = showInput.val();
			var fromInput = jq$('#fromLine' + id);
			if (show == "All") {
				fromInput.val(1);
			}
			else {
				fromInput.val(parseInt(fromInput.val()) + parseInt(show));
			}
			KNOWWE.plugin.sparql.refresh(id);
		},

		end : function(id, maximum) {
			var showInput = jq$('#showLines' + id);
			var show = showInput.val();
			var fromInput = jq$('#fromLine' + id);
			if (show == "All") {
				fromInput.val(1);
			}
			else {
				fromInput.val(maximum - show + 1);
			}
			KNOWWE.plugin.sparql.refresh(id);
		},

		back : function(id) {
			var showInput = jq$('#showLines' + id);
			var show = showInput.val();
			var fromInput = jq$('#fromLine' + id);
			var from = fromInput.val();
			if (parseInt(from) - parseInt(show) < 1) {
				fromInput.val(1);
			}
			else {
				fromInput.val(parseInt(from) - parseInt(show));
			}
			KNOWWE.plugin.sparql.refresh(id);
		},

		begin : function(id) {
			jq$('#fromLine' + id).val(1);
			KNOWWE.plugin.sparql.refresh(id);
		},

		sortResultsBy : function(newSorting, id) {

			var root = jq$.parseJSON(jq$.cookie("SparqlRenderer-" + id));

			if (root) {

				if (root.sorting) {

					var firstKey = Object.keys(root.sorting[0]);
					if (firstKey == newSorting) {
						if (root.sorting[0][firstKey] == "ASC") {
							root.sorting[0][firstKey] = "DESC";
						}
						else {
							root.sorting[0][firstKey] = "ASC";
						}
					}
					else {
						for (var i = 0; i < root.sorting.length; i++) {
							key = Object.keys(root.sorting[i]);
							if (key == newSorting) {
								root.sorting.splice(i, 1);
							}
						}
						newOrder = [];
						var tempKeyValue = {};
						tempKeyValue[newSorting] = "ASC";
						root.sorting.unshift(tempKeyValue);

					}
				}
				else {
					root.sorting = [];
					var keyValueString = {};
					keyValueString[newSorting] = "ASC";
					root.sorting.push(keyValueString);
				}
			}
			else {
				root = {};
				root.sorting = [];
				var keyValueString = {};
				root.sorting.push(keyValueString);
			}
			var cookieStr = JSON.stringify(root);
			jq$.cookie("SparqlRenderer-" + id, cookieStr);
			rerender(id);
		}
	}
}();

(function init() {
	if (KNOWWE.helper.loadCheck([ 'Wiki.jsp' ])) {
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