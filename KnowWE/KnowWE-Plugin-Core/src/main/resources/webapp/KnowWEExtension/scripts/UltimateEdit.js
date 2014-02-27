/*
 * Copyright (C) 2014 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

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

KNOWWE.plugin.renaming = function () {

	var sectionIds = [];

	/**
	 * Renames all occurrences of a specific term.
	 */
	function renameTerms(oldValue, replacement, forceRename) {
		if (forceRename == null)
			forceRename = false;
		if (oldValue && replacement) {
			var changeNote = 'Renaming: "' + oldValue + '" -> "'
				+ replacement + '"';
			var params = {
				action: "TermRenamingAction",
				termname: oldValue,
				termreplacement: replacement,
				force: forceRename ? "true" : "false"
			}
			var options = {
				url: KNOWWE.core.util.getURL(params),
				response: {
					action: 'none',
					fn: function () {
						var jsonResponse = JSON.parse(this.responseText);
						var alreadyexists = jsonResponse.alreadyexists;
						var same = jsonResponse.same;
						if (same == 'true') {
							alert('The term has not changed.');
						} else {
							if (alreadyexists == 'true') {
								if (confirm('A term with this name already exists, are you sure you want to merge both terms?')) {
									renameTerms(oldValue, replacement, true);
								}
								else {
									_IE.disable(_UEWT.rootID, true);
								}
							} else {
								_IE.disable(_UEWT.rootID, true);
							}
						}
						KNOWWE.core.util.updateProcessingState(-1);

					},
					onError: function () {
						KNOWWE.core.util.updateProcessingState(-1);
					}
				}
			}
			KNOWWE.core.util.updateProcessingState(1);
			new _KA(options).send();
		}

	}

	function getOldTermIdentifierAndMatchingSections(sectionId, callback) {
		var params = {
			action: "GetInfosForInlineTermRenamingAction",
			sectionId: sectionId
		}
		var options = {
			url: KNOWWE.core.util.getURL(params),
			response: {
				action: 'none',
				fn: function () {
					var jsonResponse = JSON.parse(this.responseText);
					callback(jsonResponse);
				},
				onError: function () {
					KNOWWE.core.util.updateProcessingState(-1);
				}
			}
		}

		new _KA(options).send();
	}

	function cancelEdit(settings, original) {
		jq$(original).removeClass("click");
		jq$(original).prev(".toolsMenuDecorator").css("display", "block");
		jq$(original).closest("span.toolMenuDecorated").css("padding-right", "0px");
		for (var i = 0; i < sectionIds.length; i++) {
			var occurence = jq$(".defaultMarkupFrame span[toolmenuidentifier=" + sectionIds[i] + "]").siblings("span");
			jq$(occurence).text(original.revert);
			jq$(occurence).css("background-color", "transparent");
		}

	}

	function showCurrentEditOnOtherOccurences(text) {

		for (var i = 0; i < sectionIds.length; i++) {
			var sectionId = sectionIds[i];
			var occurence = jq$(".defaultMarkupFrame span[toolmenuidentifier=" + sectionId + "]").siblings("span");
			jq$(occurence).first().text(text);
			jq$(occurence).first().css("background-color", "yellow");
		}

	}

	return {
		renameTerm: function (toolmenuidentifier) {
			var callback = function (jsonResponse) {

				var clickedTerm = jq$("span[toolmenuidentifier=" + toolmenuidentifier + "]").siblings('span:contains("' + jsonResponse.lastPathElement + '")');

				//get edit field
				jq$(clickedTerm).addClass("click");
				jq$(clickedTerm).prev(".toolsMenuDecorator").css("display", "none");
				jq$(".click").editable(function (value, settings) {
					renameTerms(jsonResponse.termIdentifier, value, false);
					return(value)
				}, {
					style: "inherit",
					onreset: cancelEdit
				});
				jq$('.click').trigger("click");
				//replace edit field value with sectionText for encoding reasons
				var inputField = jq$(clickedTerm).find("input").val(jsonResponse.sectionText);
				jq$(inputField).autoGrow(5);

				sectionIds = jsonResponse.sectionIds;
				showCurrentEditOnOtherOccurences(jsonResponse.sectionText);

				jq$(".click input").keyup(function () {
					showCurrentEditOnOtherOccurences(jq$(this).val());
				});


			}
			getOldTermIdentifierAndMatchingSections(toolmenuidentifier, callback);

		}

	}

}();


KNOWWE.plugin.ultimateEditTool = function () {

	var recentlyDragged = false;

	function initSections(id) {
		createEditElements(id);
		prepareEditElementContents(id);
		initWikiText();
	}

	/**
	 * The editanchors are added to the dom in different ways by JSPWiki. In
	 * this method we associate the actual html output of the section with the
	 * correct anchor.
	 */
	function createEditElements(id) {

		// some of the child nodes in the page content are just text, but we
		// need
		// actual html elements
		jq$(id).contents().filter(function () {
			return this.nodeType === 3 && !/^\s*$/.test(this.nodeValue);
		}).wrap('<div/>');
		// add id, sectionId, and class attributes to correct html elements
		jq$(id + ' .editanchor').each(
			function () {
				var editAnchor = jq$(this);
				var sectionId = editAnchor.attr('sectionid');
				var topLvlAnchorElement = editAnchor;

				topLvlAnchorElement = topLvlAnchorElement.prev("");

				var editElement = topLvlAnchorElement;

				// if we do not already have an editelement, we create one
				topLvlAnchorElement.before('<div></div>');
				if (!editElement.is('.editelement')) {
					editElement = topLvlAnchorElement.prev();
					editElement.addClass('editelement');
					if (sectionId)
						editElement.attr('sectionid', sectionId);
				}

				// we mark the first (and closing) elements to not delete
				// page
				// appends in the next step
				if (editAnchor.hasClass('first')) {
					editElement.addClass('first')
				} else if (editAnchor.hasClass('closing')) {
					editElement.addClass('closing')
				}

				// if the attribute id with this section's id is not yet
				// used, we set it,
				// otherwise InstantEdit will not work
				if (sectionId && jq$('#' + sectionId).length == 0
					&& editElement.attr('sectionid') == sectionId) {
					editElement.attr('id', sectionId);
				}

				if (sectionId)
					_UE.editableSections.push(sectionId);

				// we no longer need the anchor
				editAnchor.remove();
			});
		jq$(id + " > p:empty").remove();
	}

	function prepareEditElementContents(id) {
		var beforeFirstElement = true;
		var afterLastElement = false;
		jq$(id + ' .editelement').each(function () {
			var editElement = jq$(this);
			if (editElement.hasClass('first')) {
				beforeFirstElement = false;
			}
			// we skip before the first and after the closing elements
			// to not delete page appends
			if (beforeFirstElement)
				return;
			if (editElement.hasClass('closing')) {
				afterLastElement = true;
			}
			if (afterLastElement)
				return;
			// we add all elements between this editelement
			// and the next to this editelement
			var next = editElement.next();
			while (next.length == 1 && !next.is('.editelement')) {
				next.remove();
				editElement.append(next);
				next = editElement.next();
			}
			// if elements are aligned, they are not properly selectable
			// for editing
			editElement.children().each(function () {
				var align = jq$(this).attr('align');
				if (align != null) {
					jq$(this).attr('align', null);
				}
			});
		});
	}

	function initWikiText() {
		var ids = _UE.editableSections.join(";")

		var params = {
			action: 'InitEditModeAction'
		};

		var options = {
			url: KNOWWE.core.util.getURL(params),
			async: false,
			data: ids,
			response: {
				action: 'none'
			}
		};

		var ajaxCall = new _KA(options);
		ajaxCall.send();
		var response = ajaxCall.getResponse();
		var json = JSON.parse(response);

		// store toolNameSpace and wikiText for each section
		var sections = json.sections;
		if (sections != null) {
			for (var i = 0; i < sections.length; i++) {
				var section = sections[i];
				_UE.toolNameSpace[section.id] = eval(section.namespace);
				_EC.wikiText[section.id] = section.wikitext;
			}
		}

		// for inline sections we combine the text of the other inlined
		// sections into the id of the first/main id
		var lastEditElementId = null;
		var combinedText = "";
		for (var i = 0; i < _UE.editableSections.length; i++) {
			var sectionId = _UE.editableSections[i]
			var section = getSection(sectionId);
			if (section.length == 1) {
				if (lastEditElementId != null) {
					_EC.wikiText[lastEditElementId] = combinedText;
				}
				lastEditElementId = sectionId;
				combinedText = "";
			}
			combinedText += _EC.wikiText[sectionId];
		}
		_EC.wikiText[lastEditElementId] = combinedText;

		_UEWT.order = new Array();
		jq$('.editelement').each(function () {
			var sectionId = jq$(this).attr('sectionId');
			if (sectionId)
				_UEWT.order.push(sectionId);
		});
		_UEWT.rootID = json.root;
		_UEWT.text = json.wikitext;
		_UEWT.locked = json.locked;
	}

	function isEditAnchorElement(element) {
		return element.is('.editanchor')
			|| element.find('.editanchor').length != 0;
	}

	function addDragAndDrop() {
		jq$('#pagecontent').sortable({
			placeholder: "dropplaceholder",
			items: ".editarea, .editelement",
			stop: function (event, ui) {
				// needed for FF to stop enabling edit tools after sorting
				// stopping the event in here will also cancel the sorting
				// not pretty but works...
				recentlyDragged = true;
				setTimeout(function () {
					recentlyDragged = false;
				}, 200)
			}
		});
	}

	function addEventListener() {

		jq$('.editelement').each(function () {
			var editElement = jq$(this);
			var sectionID = editElement.attr('sectionid');

			if (jq$.inArray(sectionID, _UE.editableSections) > -1) {
				editElement.unbind('click').click(function () {
					if (_UE.enabled && !recentlyDragged) {
						removeMarker('marker_' + sectionID);
						enableEditArea(sectionID);
					}
				});
			}

			editElement.unbind('mouseover').mouseover(function () {
				if (_UE.enabled && !this.hasClass('editarea')) {
					appendMarker('marker_' + sectionID, this);
				}
			});

			editElement.unbind('mouseout').mouseout(function () {
				if (_UE.enabled) {
					removeMarker('marker_' + sectionID);
				}
			});
		});
		var enableSourceEdit = function (event) {
			event = jq$.event.fix(event.originalEvent || event);
			if (_EC.isModifier(event) || _EC.isDoubleModifier(event)) {
				if (isEditSourceKey(event)) { // E
					_UE.editSource();
				}
			}
		};
		jq$(document).keydown(enableSourceEdit);
	}

	function isEditSourceKey(event) {
		// E, but not with alt gr (= alt + ctrl) to allow for € in windows
		if (event.which == 69 && (!event.altKey || !event.ctrlKey)) {
			return true;
		}
		return false;
	}

	function getSection(sectionId) {
		return jq$('[sectionId="' + sectionId + '"]').filter('.editelement');
	}

	function enableEditArea(sectionId) {

		_EC.showAjaxLoader();

		var params = {
			action: 'InstantEditEnableAction',
			KdomNodeId: sectionId
		};

		var options = {
			url: KNOWWE.core.util.getURL(params),
			response: {
				action: 'none',
				fn: function () {

					// store the current version of this section for restoring
					_UE.cancelCache[sectionId] = getSection(sectionId);

					var toolNameSpace = _UE.toolNameSpace[sectionId];

					// show edit area
					var json = JSON.parse(this.responseText);
					var locked = json.locked;
					var html = toolNameSpace.generateHTML(sectionId);
					html = wrapHTML(sectionId, locked, html);
					getSection(sectionId).replaceWith(html);

					_EC.registerSaveCancelEvents(getSection(sectionId),
						_UE.save, _UE.cancelSection, sectionId);

					_EC.hideTools();

					// show tool bar
					appendToolBar(sectionId, generateHeaderMenu(sectionId));

					postProcessHTML(sectionId);
					jq$('#ultimateEdit .defaultMarkupFrame').each(function (index, frame) {
						KNOWWE.core.rerendercontent.animateDefaultMarkupMenu(jq$(frame));
					});
				},
				onError: _EC.onErrorBehavior
			}
		};
		new _KA(options).send();

		_EC.hideAjaxLoader();
	}

	function createTextAreaID(id) {
		return "defaultEdit" + id;
	}

	function postProcessHTML(id) {
		var textarea = $(createTextAreaID(id));
		if (typeof AutoComplete != "undefined") {
			new AutoComplete(textarea, function (callback, prefix) {
				var scope = "root";
				var data = {sectionId: id, prefix: prefix, scope: scope};
				if (KNOWWE && KNOWWE.helper) {
					data.KWiki_Topic = KNOWWE.helper.gup('page');
				}
				jq$.ajax({
					url: 'action/CompositeEditCompletionAction',
					cache: false,
					data: data
				}).success(function (data) {
					callback(eval(data));
				});
			});
		}
		new TextArea(textarea);

		textarea.focus();
		jq$(textarea).autosize({append: "\n"});
//	        while (textarea.clientHeight == textarea.scrollHeight) {
//	        	var tempHeight = textarea.style.height;
//	        	textarea.style.height = textarea.clientHeight - 5 + "px";
//	        	// abort if we are below minHeight and the height does not change anymore
//	        	if (textarea.style.height == tempHeight) break;
//	        }
//	        textarea.style.height = textarea.scrollHeight + 15 + "px";
	}

	function wrapHTML(id, locked, html) {
		var lockedHTML = "";
		if (locked) {
			lockedHTML = "<div class=\"error\">Another user has started to edit this page, but " + "hasn't yet saved it. You are allowed to further edit this page, but be " + "aware that the other user will not be pleased if you do so!</div>"
		}
		var openingDiv = "<div id='" + id + "' class='editarea'><div class='defaultMarkupFrame'>";
		var closingDiv = "</div></div>";

		return openingDiv + lockedHTML + html + closingDiv;
	}

	function bindUnloadFunctions() {

		$(window).addEvent('beforeunload', function () {
			if (_UEWT.hasChanged()) {
				return "edit.areyousure".localize();
			}
		});

		$(window).addEvent('unload', function () {
			_IE.disable(_UEWT.rootID, true);
		});
	}

	function appendMainToolBar() {
		var buttons = _EC.elements.getSaveButton("_UE.save()");
		buttons += _EC.elements
			.getCancelButton("KNOWWE.plugin.ultimateEditTool.disable()");
		buttons += "&nbsp;&nbsp;&nbsp;";
		buttons += _UE.getDeleteArticleButton();
		var toolbar = new Element('div', {
			'id': 'ueeditmaintoolbar'
		});
		toolbar.innerHTML = buttons;

		var mainToolbarDiv = jq$('<div/>', {
			"class": 'ui-dialog-content ui-widget-content',
			text: '',
			css: {
			}
		});

		mainToolbarDiv.append(toolbar);

		return mainToolbarDiv;

	}

	function appendToolBar(sectionId, content) {
		var toolbar = new Element('div', {
			'class': 'ueedittoolbar markupHeaderFrame headerMenu'
		});
		toolbar.innerHTML = content;
		var test = jq$(sectionId);
		var editor = jq$('#' + sectionId + " .defaultMarkupFrame");
		editor.css('position', 'relative');
		jq$(editor).prepend(toolbar);
	}

	function generateHeaderMenu(sectionId) {
		var html = "<div class='markupHeader'><img src='KnowWEExtension/images/arrow_down_lines.png'></span></div><div class='markupMenu'>";
		html += generateButtons(sectionId);
		html += "</div>";
		return html;
	}

	function generateButtons(sectionId) {
		return getCancelButton("_UE.cancelSection(\'" + sectionId + "\')") + getDeleteButton("_UE.deleteSection(\'" + sectionId + "\')");
	}


	function getDeleteButton(jsFunction) {
		return "<div class='markupMenuItem'><a class='markupMenuItem' href=\"javascript:" + jsFunction + "\"" + "><img src='KnowWEExtension/images/delete.png'><span>Delete this section</span></a></div>";
	}

	function getCancelButton(jsFunction) {
		return "<div class='markupMenuItem'><a class='markupMenuItem' href=\"javascript:" + jsFunction + "\"" + "><img src='KnowWEExtension/images/undo.png'><span>Undo all changes</span></a></div>";
	}

	function appendMarker(id, reference, color) {
		var marker = new Element('div', {
			'id': id,
			'class': 'ueeditmarker',
			'styles': {
				'height': reference.offsetHeight
			}
		});
		if (color) {
			marker.setStyle('background-color', color);
		}
		getSection(jq$(reference).attr('sectionid')).append(marker);
	}

	function removeMarker(id) {
		jq$('#' + id).remove();
	}

	return {

		dialogDiv: null,

		/** Indicates whether edit mode is enabled */
		enabled: false,

		/** Caches the old sections for reuse */
		cancelCache: new Object(),

		/** The namespace for each section */
		toolNameSpace: new Object(),

		/** The offset of each section in the original article */
		offset: new Object(),

		/** All sections that are compatible with edit mode */
		editableSections: new Array(),

		editSource: function () {
			var changes = _UEWT.hasChanged();
			if (!changes || (changes && confirm("edit.areyousure".localize()))) {
				$(window).removeEvents('beforeunload');
				_IE.disable(_UEWT.rootID, false, _UE.goToEditSource);
			}
		},

		goToEditSource: function () {
			var newLocation = window.location.href.replace("Wiki.jsp",
				"Edit.jsp");
			window.location = newLocation;
		},

		changeActionMenu: function () {
			if (jq$('#edit-source-button').exists())
				return;
			// change old edit button
			var pageActions = $('actionsTop').getElementsByTagName('ul')[0];
			var editAction = pageActions.getElementsByTagName('li')[0];
			var editActionA = $(editAction.getElementsByTagName('a')[0]);
			editActionA.id = "edit-source-button";

			// add instant edit button
			var instantEditAction = new Element('a', {
				'class': 'action edit',
				'title': 'Activate edit mode for this article [ d ]',
				'id': 'edit-mode-button',
				'accessKey': 'd',
				'events': {
					'click': function () {
						_UE.enable('#pagecontent');
					}
				}
			});
			instantEditAction.innerHTML = "E<span class='accesskey'>d</span>it Mode";
			var instantEditLI = new Element('li');
			instantEditLI.appendChild(instantEditAction);
			instantEditLI.injectBefore(editAction);

			var enableEditModes = function (event) {
				event = jq$.event.fix(event);
				if (_EC.isModifier(event) || _EC.isDoubleModifier(event)) {
					if (event.which == 68) { // D
						_UE.enable('#pagecontent');
						jq$(document).unbind('keydown', enableEditModes);
						event.stopPropagation();
						event.preventDefault();
					} else if (isEditSourceKey(event)) { // E
						_UE.goToEditSource();
						event.stopPropagation();
						event.preventDefault();
					}
				}
			};
			jq$(document).keydown(enableEditModes);
		},

		changeSourceEditButtons: function () {
			var buttons = jq$('#submitbuttons');
			buttons.find('[name="preview"]').remove();
			var save = buttons.find('[name="ok"]');
			save.attr('accesskey', null);
			var cancel = buttons.find('[name="cancel"]');
			cancel.attr('accesskey', null);
			_EC.registerSaveCancelEvents(document, function () {
				save.click();
			}, function () {
				cancel.click();
			});
		},

		enable: function (id) {

			if (_IE.enabled) {
				alert("You are already editing the page. Please finish your current edit before entering the instant edit mode.");
				return;
			}
			_EC.showAjaxLoader();

			initSections(id);
			addEventListener();
			addDragAndDrop();
			appendMainToolBar();
			bindUnloadFunctions();
			_EC.registerSaveCancelEvents(document, _UE.save, _UE.disable);
			_IE.disableDefaultEditTool();

			// $('header').getChildren().addClass('greyout');
			// $('favorites').addClass('greyout');
			// $('edit-mode-button').setStyle("display", "none");
			// $$('.tablePopupIcon').setStyle("display", "none");
			// $('edit-source-button').href = "javascript:_UE.editSource()";

			if (_UEWT.locked) {
				var message = "Another user has started to edit this page, but "
					+ "hasn't yet saved it. You are allowed to further edit this page, but be "
					+ "aware that the other user will not be pleased if you do so!";
				KNOWWE.notification.warn("Locked Article", message);
			}

			_EC.mode = _UE;
			_UE.enabled = true;

			_EC.hideAjaxLoader();
		},

		disable: function () {
			_UE.enabled = false;
			_UE.cancelCache = new Object();
			_UE.editableSections = new Array();
			window.onbeforeunload = null;
			window.onunload = null;
			$(window).removeEvents('beforeunload');
			$(window).removeEvents('unload');
			jq$(_UE.dialogDiv).dialog('destroy').remove();
			_UE.dialogDiv = null;
			_IE.disable(_UEWT.rootID, false);

		},

		save: function () {

			jsonObj = [];
			jq$('#ultimateEdit .editarea').each(function () {

				var id = jq$(this).attr("id");
				var text = _UEWT.getSectionText(id);

				item = {}
				item["id"] = id;
				item["text"] = text;

				jsonObj.push(item);
			});

			var params = {
				action: 'UltimateEditSaveAction',
				replaceSections: jsonObj
			}

			var options = {
				url: KNOWWE.core.util.getURL(params),
				response: {
					action: 'none',
					fn: function () {
						// TODO: Remove?
						window.onbeforeunload = null;
						window.onunload = null;
						$(window).removeEvents('beforeunload');
						$(window).removeEvents('unload');
						_EC.hideAjaxLoader();
						_IE.disable(_UEWT.rootID, true);
					},
					onError: _EC.onErrorBehavior
				}
			}
			new _KA(options).send();

		},

		cancelSection: function (sectionId) {
			var sectionId = sectionId; // we need this -> JS closure
			var fn = function () {
				var cached = _UE.cancelCache[sectionId];
				jq$('#ultimateEdit #' + sectionId).replaceWith(cached);
				addEventListener();
			};
			_IE.disable(sectionId, false, fn)
			ToolMenu.decorateToolMenus();
		},

		deleteSection: function (id) {
			// var del = confirm("Do you really want to delete this content?");
			// if (del) {
			_UE.cancelSection(id);
			_UEWT.deleteSection(id);
			getSection(id).remove();
			// }
		},

		deleteArticle: function (id) {
			var del = confirm("Do you really want to delete this article?");
			if (del) {
				var params = {
					action: 'InstantEditSaveAction',
					KdomNodeId: _UEWT.rootID
				}
				_EC.sendChanges("", params, _EC.reloadPage);
			}
		},

		getDeleteArticleButton: function (jsFunction) {
			return "<a class=\"action delete\" href=\"javascript:_UE.deleteArticle()\">Delete</a>";
		},

		getSaveCancelDeleteButtons: function (id, additionalButtonArray) {
			var array = new Array();
			array.push(_EC.elements.getCancelButton("_UE.cancelSection(\'" + id
				+ "\')"));
			array
				.push(_EC.elements
					.getDeleteSectionButton("_UE.deleteSection(\'" + id
						+ "\')"));

			// add additional buttons
			if (additionalButtonArray) {
				array.push("       ");
				array = array.concat(additionalButtonArray);
			}
			return array;
		},

		getButtonsTable: function (buttons) {
			var table = "";
			for (var i = 0; i < buttons.length; i++) {
				table += buttons[i];
			}
			return table;
		},

		createDialogDiv: function (identifier) {

			if (_UE.dialogDiv) {
				_UE.cancelCache = new Object();
				_UE.editableSections = new Array();
				jq$(_UE.dialogDiv).dialog("close");
				jq$(_UE.dialogDiv).dialog("destroy").remove();
			}

			_UE.dialogDiv = jq$('<div/>', {
				id: 'ultimateEdit',
				"class": 'defaultMarkup',
				text: ''
			});

			var divMarkupText = jq$('<div/>', {
				"class": 'markupText',
				text: ''
			});


			var params = {
				action: 'UltimateEditAction',
				termIdentifier: identifier
			}

			var options = {
				url: KNOWWE.core.util.getURL(params),
				response: {
					action: 'none',
					fn: function () {
						if (this.responseText) {
							var parsed = JSON.parse(this.responseText);
							divMarkupText.append(parsed.result);

							_UE.dialogDiv.dialog({
								//fix for strange behaviour (=scrolling to top) of dialog at first "mousedown" event
								open: function (event, ui) {
									jq$(this).mousedown(function (event) {
										event.stopPropagation();
									});
								},
								closeOnEscape: false,
								dialogClass: "no-close",
								height: jq$(window).height() * .9,
								width: jq$(document).width() * .6,
								title: parsed.header,
								resizable: false,
								draggable: false,
								modal: true,
								buttons: [
									{
										text: "Save",
										click: function () {
											jq$(this).dialog("close");
											_UE.save();
										}
									},
									{
										text: "Cancel",
										click: function () {
											jq$(this).dialog("close");
											_UE.disable();
										}
									}
								]
							});
						}
						_EC.hideAjaxLoader();

					},

					onError: _EC.onErrorBehavior

				}
			};
			new _KA(options).send();


			KNOWWE.helper.observer.subscribe("previewsLoaded", function () {
				KNOWWE.plugin.ultimateEditTool.enable('#ultimateEdit');
				jq$(".toolsMenuDecorator").click(function (e) {
					e.stopPropagation();
				});
			});

			_UE.dialogDiv.append(divMarkupText);

			jq$("body").append(_UE.dialogDiv);


		}

	}
}();

KNOWWE.plugin.ultimateEditTool.wikiText = function () {

	function fixLineBreaks(i, newText) {
		var order = _UEWT.getOriginalOrder();
		if (i == 0)
			return;
		var lastId = order[i - 1];
		var id = order[i];
		var last = newText[lastId];
		var current = newText[id];
		var lastOriginalText = getSectionText(lastId);

		// moved sections should end with a line break if they are
		// followed by an line break at the start of the new section
		// if possible and needed remove a line break from the
		// current (lower or following) section and add it
		// to the start of the last (upper) section
		var endingLineBreak = /\r?\n$/;
		var startingLineBreak = /^\r?\n/;
		if (!endingLineBreak.test(last) && startingLineBreak.test(current)) {
			last += current.match(startingLineBreak)[0];
			current = current.replace(startingLineBreak, "");
		}
		// if we have the last section, make sure it ends with a
		// line break in case it got moved
		if (i == order.length - 1) {
			var newOrder = getSectionOrder();
			var newPos = jq$.inArray(id, newOrder);
			if (newPos != -1 && newPos != newOrder.length - 1) {
				current += "\n";
			}
		}
		// if the last version of the section ended with an line break,
		// we force the new version also to have a line break at the end
		// to avoid users accidentally merging section because of the missing
		// line break
		var currentOriginalText = _EC.getWikiText(id);
		if (endingLineBreak.test(currentOriginalText)
			&& !endingLineBreak.test(current)) {
			current += currentOriginalText.match(endingLineBreak)[0];
		}

		newText[lastId] = last;
		newText[id] = current;
	}

	function getSectionOrder() {
		var sectionOrder = new Array();
		jq$('.editelement, .editarea').each(function () {
			var id = jq$(this).attr('id');
			if (id == null) {
				id = jq$(this).attr('sectionId');
			}

			sectionOrder.push(id);
		});
		return sectionOrder;
	}

	return {

		/** ID of the root section */
		rootID: null,

		/** Original Wiki-Text */
		text: null,

		/** Original ordering of the sections */
		order: null,

		/** Deleted sections */
		deletes: new Array(),

		/** is the article locked? */
		locked: null,

		hasChanged: function () {
			if (_UEWT.deletes.length > 0) {
				return true;
			}
			if (JSON.stringify(_UEWT.order) != JSON
				.stringify(getSectionOrder())) {
				return true;
			}
			for (var i = 0; i < _UE.editableSections.length; i++) {
				var id = _UE.editableSections[i];
				var unloadCondition = _UE.toolNameSpace[id].unloadCondition
				if (unloadCondition) {
					var hasToolChanged = false;
					try {
						hasToolChanged = !unloadCondition(id);
					} catch (err) {
						// maybe the tool is not correctly initialized
						// or as another problem
						// in case of an error we don't think anythin
						// has changed for the tool
					}
					if (hasToolChanged)
						return true;
				}
			}
			return false;
		},

		// getText: function() {
		// // collect the new text first to fix issues with line breaks
		// var newText = new Object();
		// var order = _UEWT.getOriginalOrder();
		// for (var i = 0; i < order.length; i++) {
		// var id = order[i];
		// newText[id] = getSectionText(id);
		// fixLineBreaks(i, newText);
		// }

		// // assemble the text
		// var newTextString = "";
		// var newOrder = getSectionOrder();
		// for (var i = 0; i < newOrder.length; i++) {
		// var id = newOrder[i];
		// if (!id) continue; // e.g. deleted sections
		// newTextString += newText[id];
		// }
		// return newTextString;
		// },

		getSectionText: function (id) {

			if (jq$.inArray(id, _UEWT.deletes) > -1) {
				return "";
			}
			var toolNameSpace = _UE.toolNameSpace[id];
			var wikiText = null;
			try {
				wikiText = toolNameSpace.generateWikiText(id);
			} catch (err) {
				// the tool might not be initialized or has
				// other errors
				wikiText = _EC.getWikiText(id)
			}
			return wikiText;

			return getNonKDomSectionText(id);

		},

		getOriginalText: function () {
			return _UEWT.text;
		},

		getOriginalOrder: function () {
			return _UEWT.order;
		},

		isLocked: function () {
			return _UEWT.locked;
		},

		deleteSection: function (id) {
			_UEWT.deletes.push(id);
		}
	}
}();

var _UE = KNOWWE.plugin.ultimateEditTool;
var _UEWT = KNOWWE.plugin.ultimateEditTool.wikiText;

(function init() {

	if (KNOWWE.helper.loadCheck([ 'Wiki.jsp' ])) {
		window.addEvent('domready', function () {
			KNOWWE.core.init();
		});
	}
	if (KNOWWE.helper.loadCheck([ 'Wiki.jsp' ])) {
		window.addEvent('domready', function () {
			_EC.executeIfPrivileged(_UE.changeActionMenu, null);
		});
	}
	if (KNOWWE.helper.loadCheck([ 'Edit.jsp' ])) {
		window.addEvent('domready', function () {
			_UE.changeSourceEditButtons();
		});
	}
}());

//fixing jquery(version 2.1) bug where HTML code is not parsed in titlebar
jq$.widget("ui.dialog", jq$.extend({}, jq$.ui.dialog.prototype, {
	_title: function (title) {
		if (!this.options.title) {
			title.html("&#160;");
		} else {
			title.html(this.options.title);
		}
	}
}));

//jquery-autogrow for automatic input field resizing
(function () {

	(function (jq$) {
		var inherit;
		inherit = ['font', 'letter-spacing'];
		return jq$.fn.autoGrow = function (options) {
			var comfortZone, remove, _ref;
			remove = (options === 'remove' || options === false) || !!(options != null ? options.remove : void 0);
			comfortZone = (_ref = options != null ? options.comfortZone : void 0) != null ? _ref : options;
			if (comfortZone != null) {
				comfortZone = +comfortZone;
			}
			return this.each(function () {
				var check, cz, input, growWithSpan, prop, styles, testSubject, _i, _j, _len, _len1;
				input = jq$(this);
				growWithSpan = input.closest("span.toolMenuDecorated");
				testSubject = input.next().filter('div.autogrow');
				if (testSubject.length && remove) {
					input.unbind('input.autogrow');
					return testSubject.remove();
				} else if (testSubject.length) {
					styles = {};
					for (_i = 0, _len = inherit.length; _i < _len; _i++) {
						prop = inherit[_i];
						styles[prop] = input.css(prop);
					}
					testSubject.css(styles);
					if (comfortZone != null) {
						check = function () {
							testSubject.text(input.val());
							growWithSpan.width(testSubject.width() + comfortZone);
							return input.width(testSubject.width() + comfortZone);
						};
						input.unbind('input.autogrow');
						input.bind('input.autogrow', check);
						return check();
					}
				} else if (!remove) {

					input.css('min-width', '15px');
					growWithSpan.css('min-width', '15px');
					growWithSpan.css('padding-right', '10px');

					styles = {
						position: 'absolute',
						top: -99999,
						left: -99999,
						width: 'auto',
						visibility: 'hidden'
					};
					for (_j = 0, _len1 = inherit.length; _j < _len1; _j++) {
						prop = inherit[_j];
						styles[prop] = input.css(prop);
					}
					testSubject = jq$('<div class="autogrow"/>').css(styles);
					testSubject.insertAfter(input);
					cz = comfortZone != null ? comfortZone : 70;
					check = function () {
						testSubject.text(input.val());
						growWithSpan.width(testSubject.width() + cz);
						return input.width(testSubject.width() + cz);
					};
					input.bind('input.autogrow', check);
					return check();
				}
			});
		};
	})(typeof Zepto !== "undefined" && Zepto !== null ? Zepto : jQuery);

}).call(this);