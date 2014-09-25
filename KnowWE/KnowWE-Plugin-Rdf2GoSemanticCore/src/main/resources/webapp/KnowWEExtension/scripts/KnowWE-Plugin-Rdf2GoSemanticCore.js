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
KNOWWE.plugin.semantic.actions = function() {

	return {
		refreshSparqlRenderer : function(id, element) {

			var root = jq$.parseJSON(jq$.cookie("SparqlRenderer-" + id));
			if (root == null) {
				root = {};
			}
			var showLines = jq$('#showLines' + id).val();
			var fromLine = jq$('#fromLine' + id).val();
			var search = /^\d+$/;
			var found = search.test(fromLine);
			if (!(found)) {
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
			KNOWWE.plugin.d3webbasic.actions.updateNode(jq$(element).parents('.type_Sparql').first().attr("id"), KNOWWE.helper.gup('page'), null);

		},

		forward : function(id, element) {

			var root = jq$.parseJSON(jq$.cookie("SparqlRenderer-" + id));
			if (root == null) {
				root = {};
			}
			var showLines = jq$('#showLines' + id).val();
			var fromLine = jq$('#fromLine' + id).val();

			var newFromLine;
			if (showLines == "All") {
				newFromLine = 1;
			}
			else {
				newFromLine = parseInt(fromLine) + parseInt(showLines);
			}
			root.navigationOffset = newFromLine;
			root.navigationLimit = showLines;
			var cookieStr = JSON.stringify(root);
			jq$.cookie("SparqlRenderer-" + id, cookieStr);
			KNOWWE.plugin.d3webbasic.actions.updateNode(jq$(element).parents('.type_Sparql').first().attr("id"), KNOWWE.helper.gup('page'), null);
		},

		end : function(id, maximum, element) {
			var root = jq$.parseJSON(jq$.cookie("SparqlRenderer-" + id));
			if (root == null) {
				root = {};
			}
			var showLines = jq$('#showLines' + id).val();

			if (showLines == "All") {
				root.navigationOffset = 1;
				root.navigationLimit = "All";

			}
			else {
				root.navigationLimit = showLines;
				root.navigationOffset = maximum - showLines + 1;
			}
			var cookieStr = JSON.stringify(root);
			jq$.cookie("SparqlRenderer-" + id, cookieStr);
			KNOWWE.plugin.d3webbasic.actions.updateNode(jq$(element).parents('.type_Sparql').first().attr("id"), KNOWWE.helper.gup('page'), null);
		},

		back : function(id, element) {

			var root = jq$.parseJSON(jq$.cookie("SparqlRenderer-" + id));
			if (root == null) {
				root = {};
			}
			var showLines = jq$('#showLines' + id).val();

			var fromLine = jq$('#fromLine' + id).val();
			var newFromLine;
			if (showLines == "All") {
				newFromLine = 1;
			}
			else {
				if (parseInt(fromLine) - parseInt(showLines) < 1) {
					newFromLine = 1;
				}
				else {
					newFromLine = parseInt(fromLine) - parseInt(showLines);
				}
			}

			root.navigationOffset = newFromLine;
			root.navigationLimit = showLines;
			var cookieStr = JSON.stringify(root);
			jq$.cookie("SparqlRenderer-" + id, cookieStr);
			KNOWWE.plugin.d3webbasic.actions.updateNode(jq$(element).parents('.type_Sparql').first().attr("id"), KNOWWE.helper.gup('page'), null);
		},

		begin : function(id, element) {
			var root = jq$.parseJSON(jq$.cookie("SparqlRenderer-" + id));
			if (root == null) {
				root = {};
			}
			var showLines = jq$('#showLines' + id).val();

			var fromLine = 1;
			root.navigationOffset = fromLine;
			root.navigationLimit = showLines;
			var cookieStr = JSON.stringify(root);
			jq$.cookie("SparqlRenderer-" + id, cookieStr);
			KNOWWE.plugin.d3webbasic.actions.updateNode(jq$(element).parents('.type_Sparql').first().attr("id"), KNOWWE.helper.gup('page'), null);
		},

		sortResultsBy : function(newSorting, id, element) {

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
					var keyValueString = {}
					keyValueString[newSorting] = "ASC";
					root.sorting.push(keyValueString);
				}
			}
			else {
				root = {};
				root.sorting = [];
				var keyValueString = {}
				root.sorting.push(keyValueString);
			}
			var cookieStr = JSON.stringify(root);
			jq$.cookie("SparqlRenderer-" + id, cookieStr);
			KNOWWE.plugin.d3webbasic.actions.updateNode(jq$(element).parents('.type_Sparql').first().attr("id"), KNOWWE.helper.gup('page'), null);
		}
	}
}();

(function init() {
	if (KNOWWE.helper.loadCheck([ 'Wiki.jsp' ])) {
		window.addEvent('domready', function() {

			var ns = KNOWWE.plugin.semantic;
			for (var i in ns) {
				if (ns[i].init) {
					ns[i].init();
				}
			}

		});
	}
}());