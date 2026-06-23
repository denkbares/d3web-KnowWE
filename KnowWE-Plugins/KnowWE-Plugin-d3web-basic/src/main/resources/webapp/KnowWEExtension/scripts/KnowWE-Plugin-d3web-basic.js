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
						KNOWWE.notification.removeNotification(sectionId);
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

/**
 * Namespace: KNOWWE.plugin.d3webbasic.watches
 *
 * The Watches debugging tool, contributed as the "watches" right-panel tab (WatchesTabProvider). It
 * resolves term identifiers to their current knowledge-base value.
 */
KNOWWE.plugin.d3webbasic.watches = function () {

	const watchesStorageKey = "watches";

	let watchesArray;

	let watches;

	const restorableEntries = {};

	let watchlist;

	function bindUiActions() {
		watches.on("click", ".watchlistentry", function (e) {
			editWatch(this);
		});
		watches.on("click", ".addwatch", function (e) {
			addWatch();
		});
		watches.on("click", ".fromselection", function (e) {
			addWatchFromSelection();
		});
		watches.on("keydown", "textarea", function (e) {
			handleTextarea(this, e);
		});
		watches.on("click", ".deletewatch", function (e) {
			e.stopPropagation();
			removeWatch(this);
		});
	}

	function handleResponse(data) {
		const expressionArrays = data.values;
		const oldEntries = watchlist.find(".watchlistentry");
		jq$.each(expressionArrays, function (index, value) {
			const newEntry = createNewEntry(watchesArray[index], value);
			jq$(oldEntries[index]).replaceWith(newEntry);
		});
		enableAddWatch();
	}

	function updateOldWatchesList(data) {
		handleResponse(data);
	}

	function updateWatches() {
		getExpressionValue(watchesArray).done(function (data) {
			updateOldWatchesList(data);
		});


	}

	function enableAddWatch() {
		if (watches.find(".addwatch").prop("disabled") === true && watches.find(".newwatch").length === 0) {
			watches.find(".addwatch").prop("disabled", false);
			watches.find(".fromselection").prop("disabled", false);
		}
	}

	function disableAddWatch() {
		if (watches.find(".addwatch").prop("disabled") === false) {
			watches.find(".addwatch").prop("disabled", true);
			watches.find(".fromselection").prop("disabled", true);
		}
	}

	function addWatch(text) {
		const textarea = createTextarea(null, text);
		watchlist.append(textarea);
		jq$(textarea).find("textarea").focus();
		//allow only one new textarea - disable Add Watch
		disableAddWatch();
	}

	function addWatchFromSelection() {
		const text = getSelectionText();
		addWatch(text);
	}

	function removeWatch(that) {
		const index = getWatchesIndex(jq$(that).parent());
		watchesArray.splice(index, 1);
		jq$(that).parent().remove();
		updateCookies();
	}

	function saveOldEntry(index, that) {
		const oldEntry = jq$(that);
		restorableEntries[index] = oldEntry;
	}

	function editWatch(that) {
		const index = getWatchesIndex(that);
		saveOldEntry(index, that);
		watchesArray.splice(index, 1);
		const textarea = createTextarea(that);
		jq$(that).replaceWith(textarea);
		jq$(textarea).find("textarea").focus();
		//allow only one new textarea - disable Add Watch
		disableAddWatch();
	}

	function restoreEntry(entry, watchesIndex) {
		const restorableEntry = restorableEntries[watchesIndex];
		const restorableExpression = jq$(restorableEntry).find(".expression").text();
		watchesArray.splice(watchesIndex, 0, restorableExpression);
		jq$(entry).replaceWith(restorableEntry);
		delete restorableEntries[watchesIndex];
	}

	function handleTextarea(that, e) {
		const entry = jq$(that).parent();
		const watchesIndex = getWatchesIndex(entry);

		//escape
		if (e.keyCode === 27) {
			//is it an old element?
			if (watchesIndex <= watchesArray.length) {
				//restore it
				restoreEntry(entry, watchesIndex);
			}
			else {
				entry.remove();
			}
			enableAddWatch();
		}

		if (jq$(that).data('ui-tooltip') && jq$(that).val().trim() !== "") {
			jq$(that).tooltip("destroy");
			jq$(that).attr("title", null);
		}

		const trimmedValue = jq$(that).val().trim();
		//shift+enter = newline, enter=submit
		if (e.keyCode === 13 && !e.shiftKey) {
			if (trimmedValue === "") {
				jq$(that).tooltip({position: {my: "right bottom", at: "left top"}});
				jq$(that).attr("title", "Please enter an expression.");
				jq$(that).trigger("mouseover");
				// prevent default behavior
				e.preventDefault();
				//alert("ok");
			}
			else {
				if (watchesIndex < watchesArray.length) {
					addExpression(jq$(that), watchesIndex);
				}
				else {
					jq$(that).val(trimmedValue);
					addExpression(jq$(that));
				}

			}
		}

	}

	function getWatchesIndex(that) {
		return jq$(that).index();
	}

	function getExpressionValue(expr, id) {
		const data = {expressions: expr, page: KNOWWE.helper.gup('page'), id: id};
		return jq$.ajax({
			type: 'post',
			url: 'action/GetExpressionValueAction',
			data: JSON.stringify(data),
			cache: false,
			contentType: 'application/json; charset=UTF-8'
		});
	}


	function createTextarea(that, text) {
		const watchesNewEntry = jq$('<div/>', {
			'class': 'newwatch watchlistline'
		});
		const textarea = jq$('<textarea>', {});

		const textareaDom = textarea[0];

		if (typeof AutoComplete != "undefined") {
			new AutoComplete(textareaDom, function (callback, prefix) {
				const scope = "$d3web/condition";
				const data = {prefix: prefix, scope: scope};
				if (KNOWWE && KNOWWE.helper) {
					data.KWiki_Topic = KNOWWE.helper.gup('page');
				}
				jq$.ajax({
					url: 'action/CompletionAction',
					cache: false,
					data: data
				}).done(function (data) {
					callback(eval(data));
				});
			});
		}

		if (that) {
			const oldEntry = restorableEntries[getWatchesIndex(that)];
			const oldText = jq$(oldEntry).find(".expression").text();
			textarea.val(oldText);
		}

		if (typeof text != 'undefined') {
			textarea.val(text);
		}
		textarea.autosize({minHeight: "22px"});
		watchesNewEntry.append(textarea);
		return watchesNewEntry;
	}

	function createWatchesEntryValueSpan(value) {
		return jq$('<span/>', {
			'class': 'value tooltip',
			'text': value.value,
			'title': value.kbname
		});
	}


	function createWatchesEntryHistoryValueSpan(title) {
		return jq$('<span/>', {
			'class': 'value tooltip history',
			'title': title
		});
	}

	function handleDefaultResponse(watchesEntry, responseObject) {

		jq$.each(responseObject.kbsEntries, function iterateValuesFromDifferentKbs(index, value) {
			const watchesEntryValue = createWatchesEntryValueSpan(value);
			const tooltipcontent = jq$('<span style="padding-right: 5px" class="fa fa-book"></span><span>' + value.kbname + '  </span>');
			jq$(watchesEntryValue).tooltipster({
				content: tooltipcontent,
				position: "top-left",
				delay: 300,
				theme: ".tooltipster-knowwe"
			});
			watchesEntry.append(watchesEntryValue);
		});

		return watchesEntry;

	}

	function handleHistoryResponse(watchesEntry, responseObject) {

		jq$.each(responseObject.kbsEntries, function iterateValuesFromDifferentKbs(index, value) {
			const watchesEntryValue = createWatchesEntryHistoryValueSpan(value.kbname);

			jq$.each(value.value, function iterateValuesInHistory(index, value) {
				const historyEntrySpan = jq$('<span/>', {
					'class': 'value tooltip historyentry',
					'text': value.value
				});
				createTimestampsToolTip.call(this, historyEntrySpan);
				watchesEntryValue.append(historyEntrySpan);

			});
			watchesEntry.append(watchesEntryValue);
		});

		function createTimestampsToolTip(historyEntrySpan) {
			const start = this.timestamps[0];
			const end = this.timestamps[1];
			let tooltipcontent;
			if (start !== end) {
				tooltipcontent = jq$('<span>Start: ' + start + '</span><br><span>End: ' + end + '  </span>');
			}
			else {
				tooltipcontent = jq$('<span>Start: ' + start + '</span>');
			}
			jq$(historyEntrySpan).tooltipster({
				content: tooltipcontent,
				position: "top-left",
				delay: 300,
				theme: ".tooltipster-knowwe"
			});
		}

		return watchesEntry;
	}

	function createNewEntry(expression, responseObject) {

		const watchesEntry = jq$('<div/>', {
			'class': 'watchlistline watchlistentry'

		});
		watchesEntry.uniqueId();
		const watchesEntryExpression = jq$('<span/>', {
			'class': 'expression',
			'text': expression
		});
		watchesEntry.append(watchesEntryExpression);


		const length = Object.keys(responseObject).length;
		if (length > 0) {
			switch (responseObject.info) {
				case 'history':
					handleHistoryResponse(watchesEntry, responseObject);
					break;
				default:
					handleDefaultResponse(watchesEntry, responseObject);
			}
		}
		else {
			const watchesEntryValue = jq$('<span/>', {
				'class': 'value expressionerror',
				'text': '<not a valid expression>'
			});
			watchesEntry.append(watchesEntryValue);
		}

		watchesEntry.append(createDeleteButton());
		return watchesEntry;
	}

	function addExpression(original, watchesIndex) {

		const expression = original.val();

		const watchesEntry = jq$('<div/>', {
			'class': 'watchlistline watchlistentry',
			'text': expression
		});
		jq$(original).parent().replaceWith(watchesEntry);

		if (typeof watchesIndex != 'undefined') {
			watchesArray.splice(watchesIndex, 0, expression);
		}
		else {
			watchesArray.push(expression);
		}
		updateCookies();

		getExpressionValue(watchesArray).done(function (data) {
			updateOldWatchesList(data);
		});
	}

	function buildBasicWatchesDiv(body) {

		const watchcontent = jq$('<div/>', {});

		const watchlist = jq$('<div/>', {
			'class': 'watchlist'
		});

		const watchesAddEntry = jq$("<button class='addwatch'><i class='fa fa-circle-plus'></i>&nbsp;Add Watch</button>");


		const watchesAddEntryFromSelection = jq$("<button class='fromselection'><i class='fa fa-paragraph'></i>&nbsp;from Selection</button>");

		watchcontent.append(watchlist);
		watchcontent.append(watchesAddEntry);
		watchcontent.append(watchesAddEntryFromSelection);

		// build the standard collapsible tool chrome (.tool#watches > .topbar + .right-panel-content) and
		// mount it into this tab's own body (passed by the rightPanelTabInitialized event), not via
		// addToolToRightPanel - eager init fires while a different tab may be active
		const tool = KNOWWE.core.plugin.rightPanel.buildTool("Watches", "watches", watchcontent);
		jq$(body).append(tool);
	}

	function createDeleteButton() {

		const deleteContainer = jq$('<div/>', {
			'class': 'iconcontainer deletewatch select'
		});

		const deleteIcon = jq$("<a class=''><i class='fa fa-circle-xmark icon'></i></a>");


		return deleteContainer.append(deleteIcon);
	}

	function loadWatchesFromCookies() {
		watchesArray = simpleStorage.get(watchesStorageKey);
		if (typeof watchesArray != 'undefined') {
			getExpressionValue(watchesArray).done(function (data) {
				const expressionArrays = data.values;
				jq$.each(expressionArrays, function (index, value) {
					const newEntry = createNewEntry(watchesArray[index], value);
					watchlist.append(newEntry);
				});
			});
		}
		else {
			watchesArray = [];
		}
	}

	function updateCookies() {
		simpleStorage.set(watchesStorageKey, watchesArray);
	}

	function getSelectionText() {
		let text = "";
		if (window.getSelection) {
			text = window.getSelection().toString();
		} else if (document.selection && document.selection.type !== "Control") {
			text = document.selection.createRange().text;
		}
		return text;
	}

	function initVariables() {
		watches = jq$("#watches");
		watchlist = watches.find(".watchlist");
	}

	return {
		initWatchesTool: function (body) {
			buildBasicWatchesDiv(body);
			loadWatchesFromCookies();
			initVariables();
			bindUiActions();
			KNOWWE.helper.observer.subscribe("update", function () {
				updateWatches();
			});
		},

		addToWatches: function (text) {
			KNOWWE.core.plugin.rightPanel.showRightPanel();
			KNOWWE.core.plugin.rightPanel.activateTab("watches");
			addWatch(text);
		}
	}
}();

// Hydrate the watches tab body when the panel initializes it (decision 7a/7b). Subscribing on the
// document needs no eval-time dependency on the RightPanel module having loaded first.
document.addEventListener("rightPanelTabInitialized", function (event) {
	if (event.detail && event.detail.id === "watches") {
		KNOWWE.plugin.d3webbasic.watches.initWatchesTool(event.detail.body);
	}
});
