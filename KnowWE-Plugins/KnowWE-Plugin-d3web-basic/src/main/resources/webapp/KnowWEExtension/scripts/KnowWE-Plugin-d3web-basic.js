/**
 * Title: KnowWE-Plugin-d3web-basic Contains all javascript functions concerning
 * the KnowWE-Plugin-d3web-basic.
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
 * Namespace: KNOWWE.plugin.d3webbasic The KNOWWE plugin d3web namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin.d3webbasic = function() {
	return {}
}();

/**
 * Namespace: KNOWWE.plugin.d3webbasic.actions some core actions of the D3Web
 * plugin for KNOWWE.
 */
KNOWWE.plugin.d3webbasic.actions = function() {
	return {
		/**
		 * Function: init Some function that are executed on page load. They
		 * initialize some core d3web plugin functionality.
		 */
		init : function() {
			// add highlight xcl
			if (_KS('.highlight-xcl-relation').length != 0) {
				_KS('.highlight-xcl-relation')
					.each(
						function(element) {
							_KE
								.add(
									'click',
									element,
									KNOWWE.plugin.d3webbasic.actions.highlightXCLRelation);
						});
			}
			// add highlight rule
			if (_KS('.highlight-rule').length != 0) {
				_KS('.highlight-rule')
					.each(
						function(element) {
							_KE
								.add(
									'click',
									element,
									KNOWWE.plugin.d3webbasic.actions.highlightRule);
						});
			}
		},
		/**
		 * Function: highlightXCLRelation
		 *
		 * Parameters: event - The event that was triggered from a DOM element.
		 */
		highlightXCLRelation : function(event) {
			var rel = eval("(" + _KE.target(event).getAttribute('rel') + ")");
			if (!rel)
				return;

			// Restore the Highlighting that was before
			var restore = document.getElementById('uniqueMarker');
			if (restore) {
				KNOWWE.plugin.d3webbasic.actions.updateNode(restore.className,
					rel.topic, null);
			}
			KNOWWE.plugin.d3webbasic.actions.highlightNode(rel.kdomid,
				rel.topic, rel.depth, rel.breadth, event);
		},
		/**
		 * Function: updateNode - Duplicated from KnowWE-Core: Calls
		 * rerendercontent.execute with 'replace' So highlighting 'yellow'
		 * functions more than once. TODO: After Highlighting there exist 2
		 * spans with the same attributes. Updates a node.
		 *
		 * Parameters: node - The node that should be updated. topic - The name
		 * of the page that contains the node.
		 */
		updateNode : function(node, topic, ajaxToHTML) {
			var params = {
				action : 'ReRenderContentPartAction',
				KWikiWeb : 'default_web',
				KdomNodeId : node,
				KWiki_Topic : topic,
				ajaxToHTML : ajaxToHTML,
				inPre : KNOWWE.helper.tagParent(_KS('#' + node), 'pre') != document
			}
			var url = KNOWWE.core.util.getURL(params);
			KNOWWE.core.rerendercontent.execute(url, node, 'replace');
		},
		/**
		 * Function: highlightRule
		 *
		 * Parameters: event - The event that was triggered from a DOM element.
		 */
		highlightRule : function(event) {
			var rel = eval("(" + _KE.target(event).getAttribute('rel') + ")");
			if (!rel)
				return;

			// Restore the Highlighting that was before
			var restore = document.getElementById('uniqueMarker');
			if (restore) {
				KNOWWE.plugin.d3webbasic.actions.updateNode(restore.className,
					rel.topic, null);
			}
			KNOWWE.plugin.d3webbasic.actions.highlightNode(rel.kdomid,
				rel.topic, rel.depth, rel.breadth, event);
		},
		/**
		 * Function: highlightNode Highlights an DOM node in the current wiki
		 * page. You need a span with an id to use this. There the uniqueMarker
		 * is located. *Note:* if a node has more than 1 element this function.
		 * Will not work because it cannot foresee how the html-tree is build.
		 *
		 * See Also: <highlighXCLRelation>
		 *
		 * Parameters: node - is the tag that has the marker under it topic -
		 * depth - means the tag that has the marker as firstchild. breadth -
		 * event - Used to stop event bubbling.
		 */
		highlightNode : function(node, topic, depth, breadth, event) {
			var event = new Event(event);
			event.stopPropagation();

			var params = {
				action : 'HighlightNodeAction',
				Kwiki_Topic : topic,
				KWikiJumpId : node
			}

			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					action : 'update',
					ids : [],
					fn : function() {
						// set the new Marker: Get the root node
						var element = document.getElementById(node);

						// get to the depth given.
						for (var i = 0; i < depth; i++) {
							element = element.firstChild;
						}

						// get to the given breadth
						for (var j = 0; j < breadth; j++) {
							if (element.nextSibling)
								element = element.nextSibling;
						}

						// TODO:Johannes:This is an ugly fix
						// for a highlighting problem
						if (element.firstChild.style == undefined) {
							element = element.firstChild.nextSibling;
						}

						if (element) {
							// for quoted questions and rules
							if (element.firstChild.style == undefined
								|| element.className == 'HIGHLIGHT_MARKER') {
								element.style.backgroundColor = "yellow";
								element.id = "uniqueMarker";
								element.className = node;
								element.scrollIntoView(true);
							} else {
								element.firstChild.style.backgroundColor = "yellow";
								element.firstChild.id = "uniqueMarker";
								element.firstChild.className = node;
								element.scrollIntoView(true);
							}
						}
					}
				}
			}
			new _KA(options).send();
		},

		/**
		 * Resets a d3web-Session using the SessionResetAction.
		 *
		 * The parameter kbarticlevalue can be used to specify the article
		 * compiling the knowledge base. If this parameter is not specified the
		 * SessionResetAction will use the title of the current page.
		 *
		 * @author Sebastian Furth (denkbares GmbH)
		 */
		resetSession : function(sectionId, fnAfter) {
			var params = {
				action : 'SessionResetAction',
				SectionID : sectionId
			};

			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					action : 'none',
					fn : function() {
						try {
							KNOWWE.helper.observer.notify('update', fnAfter);
						} catch (e) { /* ignore */
						}
						KNOWWE.core.util.updateProcessingState(-1);
					},
					onError : _EC.onErrorBehavior

				}
			};
			KNOWWE.core.util.updateProcessingState(1);
			new _KA(options).send();

		}

	}
}();

(function init() {
	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function() {

			var ns = KNOWWE.plugin.d3webbasic;
			for (var i in ns) {
				if (ns[i].init) {
					ns[i].init();
				}
			}

		});
	}
}());

KNOWWE.plugin.rule = {};

KNOWWE.plugin.rule.editTool = {};
jq$.extend(KNOWWE.plugin.rule.editTool, KNOWWE.plugin.defaultEditTool);

KNOWWE.plugin.rule.editTool.generateButtons = function(id) {
	KNOWWE.plugin.rule.editTool.format = function(id) {
		KNOWWE.core.plugin.formatterAjax(id, "RuleFormatAction");
	};
	return _EC.elements.getSaveCancelDeleteButtons(id,
		["<a class='action format' onclick='KNOWWE.plugin.rule.editTool.format(\"" + id + "\")'>" +
		"Format</a>"]);
};
