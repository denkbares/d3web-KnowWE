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

KNOWWE.plugin.compositeEditTool = function() {

	var recentlyDragged = false;

	function initCompositeEdit(id) {

		if (!id) {
			id = jq$('.objectInfoPanel');
		}

		if (_IE.enabled) {
			alert("You are already editing the page. Please finish your current edit before entering composite edit.");
			return;
		}
		_EC.showAjaxLoader();

		initSections(id);
		if (_CE.mode == _CE.ModeEnum.EDIT) {
			addEventListenerForEdit(id);
		}
		// TODO: replace with dialog specific function
		// bindUnloadFunctions();


		if (_CEWT.locked) {
			var message = "Another user has started to edit this page, but "
				+ "hasn't yet saved it. You are allowed to further edit this page, but be "
				+ "aware that the other user will not be pleased if you do so!";
			KNOWWE.notification.warn("Locked Article", message);
		}

		_EC.mode = _CE;
		_CE.enabled = true;

		_EC.hideAjaxLoader();
	}

	function initSections(id) {
		createEditElements(id);
		prepareEditElementContents(id);
		initWikiText();
		enableCompositeViewToolMenus(jq$(id));
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
		jq$(id).contents().filter(function() {
			return this.nodeType === 3 && !/^\s*$/.test(this.nodeValue);
		}).wrap('<div/>');
		// add id, sectionId, and class attributes to correct html elements
		jq$(id).find('.editanchor').each(
			function() {
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
					_CE.editableSections.push(sectionId);

				// we no longer need the anchor
				editAnchor.remove();
			});
		jq$(id).find("p:empty").remove();
	}

	function prepareEditElementContents(id) {
		var beforeFirstElement = true;
		var afterLastElement = false;
		jq$(id).find('.editelement').each(function() {
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
			editElement.children().each(function() {
				var align = jq$(this).attr('align');
				if (align != null) {
					jq$(this).attr('align', null);
				}
			});
		});
	}

	function initWikiText() {
		var ids = _CE.editableSections.join(";");

		var params = {
			action : 'InitEditModeAction'
		};

		var options = {
			url : KNOWWE.core.util.getURL(params),
			async : false,
			data : ids,
			response : {
				action : 'none'
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
				_CE.toolNameSpace[section.id] = eval(section.namespace);
				_EC.wikiText[section.id] = section.wikitext;
			}
		}

		// for inline sections we combine the text of the other inlined
		// sections into the id of the first/main id
		var lastEditElementId = null;
		var combinedText = "";
		for (var i = 0; i < _CE.editableSections.length; i++) {
			var sectionId = _CE.editableSections[i]
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

		_CEWT.order = [];
		jq$('.editelement').each(function() {
			var sectionId = jq$(this).attr('sectionId');
			if (sectionId)
				_CEWT.order.push(sectionId);
		});
		_CEWT.rootID = json.root;
		_CEWT.text = json.wikitext;
		_CEWT.locked = json.locked;
	}

	function addEventListenerForEdit(root) {

		var found = jq$(root).find('.editelement');
		jq$(root).find('.editelement').each(function() {
			var editElement = jq$(this);
			var sectionID = editElement.attr('sectionid');

			if (jq$.inArray(sectionID, _CE.editableSections) > -1) {
				editElement.unbind('click').click(function() {
					if (_CE.enabled && !recentlyDragged) {
						removeMarker('marker_' + sectionID);
						enableEditArea(sectionID);
					}
				});
			}

			editElement.unbind('mouseover').mouseover(function() {
				if (_CE.enabled && !this.hasClass('editarea')) {
					appendMarker('marker_' + sectionID, this);
				}
			});

			editElement.unbind('mouseout').mouseout(function() {
				if (_CE.enabled) {
					removeMarker('marker_' + sectionID);
				}
			});
		});

	}

	function removeEventListenersForEdit(root) {
		var found = jq$(root).find('.editelement');
		jq$(root).find('.editelement').each(function() {
			var editElement = jq$(this);
			editElement.unbind('click');
			var sectionID = editElement.attr('sectionid');
			editElement.unbind('mouseout');
			editElement.unbind('mouseover');
		});
	}

	function isEditSourceKey(event) {
		// E, but not with alt gr (= alt + ctrl) to allow for € in windows
		return event.which == 69 && (!event.altKey || !event.ctrlKey);
	}

	function getSection(sectionId) {
		return jq$('#compositeEdit div[sectionId="' + sectionId + '"]').filter('.editelement');
	}

	function enableEditArea(sectionId) {

		_EC.showAjaxLoader();

		var params = {
			action : 'InstantEditEnableAction',
			KdomNodeId : sectionId
		};

		var options = {
			url : KNOWWE.core.util.getURL(params),
			response : {
				action : 'none',
				fn : function() {

					// store the current version of this section for restoring
					_CE.cancelCache[sectionId] = getSection(sectionId);

					var toolNameSpace = _CE.toolNameSpace[sectionId];

					// show edit area
					var json = JSON.parse(this.responseText);
					var locked = json.locked;
					var html = toolNameSpace.generateHTML(sectionId);
					html = wrapHTML(sectionId, locked, html);
					getSection(sectionId).replaceWith(html);

					_EC.registerSaveCancelEvents(getSection(sectionId),
						_CE.save, _CE.cancelSection, sectionId);

					// show toolmenu
					appendDefaultMarkupFrameToolMenu(sectionId);

					postProcessHTML(sectionId);

					jq$('#compositeEdit div.defaultMarkupFrame[compositeedit=' + sectionId + ']').each(function(index, frame) {
						_TM.animateDefaultMarkupMenu(jq$(frame));
					});
				},
				onError : _EC.onErrorBehavior
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
			new AutoComplete(textarea, function(callback, prefix) {
				var scope = "root";
				var data = {sectionId : id, prefix : prefix, scope : scope};
				if (KNOWWE && KNOWWE.helper) {
					data.KWiki_Topic = KNOWWE.helper.gup('page');
				}
				jq$.ajax({
					url : 'action/CompositeEditCompletionAction',
					cache : false,
					data : data
				}).success(function(data) {
					callback(eval(data));
				});
			});
		}
		new TextArea(textarea);

		textarea.focus();
		jq$(textarea).autosize({append : "\n"});
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
		var openingDiv = "<div sectionid='" + id + "' class='editarea'><div class='defaultMarkupFrame' compositeEdit='" + id + "'>";
		var closingDiv = "</div></div>";

		return openingDiv + lockedHTML + html + closingDiv;
	}

	function bindUnloadFunctions() {

		window.onbeforeunload = function() {
			if (_CEWT.hasChanged()) {
				return "edit.areyousure".localize();
			}
		};

		window.onunload = function() {
			_IE.disable(_CEWT.rootID, true);
		};
	}

	function unbindUnloadFunctions() {
		window.onbeforeunload = null;
		window.onunload = null;
	}

	function appendDefaultMarkupFrameToolMenu(sectionId) {
		var editField = jq$(".defaultMarkupFrame[compositeEdit=" + sectionId + "]");
		editField.css('position', 'relative');
		var headerMenu = jq$('<div/>', {
			'class' : 'ueedittoolbar markupHeaderFrame headerMenu'
		});
		var markupHeader = generateMarkUpHeader(sectionId)
		var markUpMenu = generateMarkUpMenu(sectionId);
		headerMenu.append(markupHeader);
		headerMenu.append(markUpMenu);
		jq$(editField).prepend(headerMenu);
	}

	function generateMarkUpHeader(sectionId) {
		var markupHeader = jq$('<div/>', {
			"class" : 'markupHeader'
		});
		var img = jq$('<img/>', {
			"src" : 'KnowWEExtension/images/arrow_down_lines.png'
		});
		markupHeader.append(img);
		return markupHeader;
	}

	function generateMarkUpMenu(sectionId) {
		var markupMenu = jq$('<div/>', {
			"class" : 'markupMenu'
		});
		var cancel = generateMarkUpmenuItem("_CE.cancelSection(\'" + sectionId + "\')", "Revert", "undo.png");
		var del = generateMarkUpmenuItem("_CE.deleteSection(\'" + sectionId + "\')", "Delete this section", "delete.png");
		markupMenu.append(cancel);
		markupMenu.append(del);
		return markupMenu;
	}

	function generateMarkUpmenuItem(js, text, icon) {
		var markupMenuItemDiv = jq$('<div/>', {
			"class" : 'markupMenuItem'
		});
		var varMenuItemA = jq$('<a/>', {
			"class" : 'markupMenuItem',
			"href" : "javascript:" + js
		});
		var img = jq$('<img/>', {
			"src" : "KnowWEExtension/images/" + icon
		});
		var span = jq$('<span/>', {
			"text" : text
		});
		varMenuItemA.append(img);
		varMenuItemA.append(span);
		markupMenuItemDiv.append(varMenuItemA);
		return markupMenuItemDiv;
	}

	function appendMarker(id, reference, color) {
		var marker = new Element('div', {
			'id' : id,
			'class' : 'ueeditmarker',
			'styles' : {
				'height' : reference.offsetHeight
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

	function closeOldDialog() {
		if (typeof _CE.dialogDiv !== 'undefined') {
			jq$(_CE.dialogDiv).dialog("close");
			jq$(_CE.dialogDiv).dialog("destroy").remove();
		}
	}

	function resetVariables() {
		_CE.cancelCache = {};
		_CE.editableSections = [];
		_CE.enabled = true;
	}

	function buildDefaultMarkupStructure() {
		_CE.dialogDiv = jq$('<div/>', {
			id : 'compositeEdit',
			"class" : 'defaultMarkup',
			text : ''
		});
		var divMarkupText = jq$('<div/>', {
			"class" : 'markupText',
			text : ''
		});
		_CE.dialogDiv.append(divMarkupText);
		return divMarkupText;
	}

	function appendDialogToHtmlBody() {
		jq$("body").append(_CE.dialogDiv);
	}

	function disableCompositeEditMode() {
		for (var section in _CE.cancelCache) {
			_CE.cancelSection(section);
		}
		removeEventListenersForEdit(jq$("#compositeEdit"));
	}

	function enableCompositeViewMode() {
		disableCompositeEditMode();
		changeDefaultKeybindings();
		_CE.mode = _CE.ModeEnum.VIEW;
		changeButtons();
		registerButtonEvents();
	}

	function registerButtonEvents() {
		var pane = jq$("#compositeEdit").next("div.ui-dialog-buttonpane");
		if (_CE.mode == _CE.ModeEnum.VIEW) {
			unregisterSaveCancelEvents(document, _CE.save, enableCompositeViewMode);
			jq$(pane).find(".closeButton").unbind().on("click", function() {
				_CE.disable();
			});
			jq$(pane).find(".editButton").unbind().on("click", function() {
				enableCompositeEditMode();
			});
		}
		else {
			_EC.registerSaveCancelEvents(document, _CE.save, enableCompositeViewMode);
			jq$(pane).find(".saveButton").unbind().on("click", function() {
				_CE.save();
			});
			jq$(pane).find(".cancelEditButton").unbind().on("click", function() {
				enableCompositeViewMode();
			});
		}
	}

	function unregisterSaveCancelEvents(element, saveFunction, cancelFunction) {
		jq$(element).off("keydown", saveFunction);
		jq$(element).off("keydown", cancelFunction);
	}

	function restoreDefaultKeybindings() {
		jq$("#edit-source-button").attr("accesskey", "e");
		jq$("#edit-mode-button").attr("accesskey", "d");
		jq$(document).off('keydown', _CE.enableCompositeEditModeByKey)
			.on('keydown', _EM.enableEditModes)
			.on('keydown', _EM.enableSourceEdit);
	}

	function changeDefaultKeybindings() {
		jq$("#edit-source-button").removeAttr("accesskey");
		jq$("#edit-mode-button").removeAttr("accesskey");
		jq$(document).off('keydown', _EM.enableEditModes)
			.off('keydown', _EM.enableSourceEdit)
			.on('keydown', _CE.enableCompositeEditModeByKey);
	}

	function enableInitialDialogState() {
		KNOWWE.core.actions.init();
		if (!_CE.userCanWriteAllSections) {
			var pane = jq$("#compositeEdit").next("div.ui-dialog-buttonpane");
			jq$(pane).find(".editButton").attr('disabled', true).attr('title', "You can't edit these sections, because you don't have write permissions on all sections.");
		}
		document.body.style.overflow = 'hidden';
		appendMaximizeButton();
		if (_CE.mode == _CE.ModeEnum.EDIT) {
			enableCompositeEditMode();
		}
		else {
			enableCompositeViewMode();
		}
		_TM.decorateToolMenus(jq$('#compositeEdit'));
		jq$('#compositeEdit .defaultMarkupFrame').click(function(e) {
			e.stopPropagation();
		});
		_TM.animateDefaultMarkupMenu(jq$('#compositeEdit'));
		jq$(".extend-panel-right").click(function(e) {
			KNOWWE.core.plugin.objectinfo.loadPreviews(jq$(this).next('div'));
			saveExpandState(this);
		});
		jq$(document).on('keydown', escapeFunction);
		expandSavedStates();

		KNOWWE.tooltips.enrich();
	}

	function escapeFunction(event) {
		if (event.keyCode === jq$.ui.keyCode.ESCAPE) {
			_CE.disable();
		}
	}

	function createCompositeEditDialog(identifier, divMarkupText) {
		var params = {
			action : 'CompositeEditOpenDialogAction',
			termIdentifier : identifier
		};

		var options = {
			url : KNOWWE.core.util.getURL(params),
			response : {
				action : 'none',
				fn : function() {
					if (this.responseText) {
						var parsed = JSON.parse(this.responseText);
						divMarkupText.append(parsed.result);
						_CE.userCanWriteAllSections = parsed.canWriteAll;


						_CE.dialogDiv.dialog({
							//fix for strange behaviour (=scrolling to top) of dialog at first "mousedown" event
							open : function(event, ui) {
								jq$(this).mousedown(function(event) {
									event.stopPropagation();
								});
							},
							closeOnEscape : false,
							dialogClass : "no-close",
							height : (jq$(window).height() * .9),
							width : jq$(document).width() * .6,
							title : parsed.header,
							resizable : true,
							draggable : true,
							modal : true,
							buttons : [
								{
									text : "Close",
									'class' : 'closeButton'
								},
								{
									text : "Edit",
									'class' : 'editButton'
								}
							]
						});
					}
					enableInitialDialogState();
					_EC.hideAjaxLoader();
				},
				onError : _EC.onErrorBehavior

			}
		};
		return options;
	}

	function enableCompositeViewToolMenus(id) {

		_TM.animateDefaultMarkupMenu(jq$(id));
		_TM.decorateToolMenus(jq$(id));
		jq$(id).find("a").click(function(e) {
			e.stopPropagation();
		});
	}

	function saveExpandState(node) {
		var prefix = "CompositeEditCollapseState-";
		var type = jq$(node).find("strong").text();
		var term = jq$("#objectinfo-src").text();
		var typeArray;
		if (jq$(node).hasClass("extend-panel-down")) {
			typeArray = simpleStorage.get(prefix + term);
			if (typeof typeArray == "undefined") {
				typeArray = new Array(type);
				simpleStorage.set(prefix + term, typeArray);
			}
			if (jq$.inArray(type, typeArray) == -1) {
				typeArray.push(type);
				simpleStorage.set(prefix + term, typeArray);
			}
		}
		else {
			typeArray = simpleStorage.get(prefix + term);
			typeArray.splice(typeArray.indexOf(type), 1);
			simpleStorage.set(prefix + term, typeArray);
		}
	}


	function expandSavedStates() {
		var prefix = "CompositeEditCollapseState-";
		var term = jq$("#objectinfo-src").text();
		var typeArray = simpleStorage.get(prefix + term);
		if (typeof typeArray !== "undefined") {
			for (var i = 0; i < typeArray.length; i++) {
				var type = typeArray[i];
				var p = jq$(".extend-panel-right strong").filter(function() {
					return jq$(this).text() === type;
				}).parent().removeClass("extend-panel-right").addClass("extend-panel-down");
				var section = jq$(p).next('div');
				jq$(section).css("display", "inline");
				KNOWWE.core.plugin.objectinfo.loadPreviews(section);
			}
		}
	}

	function registerMaximizeEvent(image) {
		var image = jq$(image);
		jq$(image).on("click", function() {
			if (image.hasClass("minimized")) {
				image.removeClass("minimized").addClass("maximized");
				jq$(_CE.dialogDiv).parents(".ui-dialog:first").animate({
					width : window.innerWidth,
					height : window.innerHeight
				}, {
					duration : 500,
					step : function() {
						jq$(_CE.dialogDiv).dialog('option', 'position', 'center');
					}
				});
				jq$(_CE.dialogDiv).dialog("option", "height", window.innerHeight);
				jq$(_CE.dialogDiv).dialog("option", "width", window.innerHeight);
			}
			else {
				image.removeClass("maximized").addClass("minimized");
				jq$(_CE.dialogDiv).parents(".ui-dialog:first").animate({
					width : window.innerWidth * .6,
					height : window.innerHeight * .9
				}, {
					duration : 500,
					step : function() {
						jq$(_CE.dialogDiv).dialog('option', 'position', 'center');
					}
				});
				jq$(_CE.dialogDiv).dialog("option", "height", window.innerHeight * .9);
				jq$(_CE.dialogDiv).dialog("option", "width", window.innerHeight * .6);
			}
		});
	}

	function appendMaximizeButton() {
		var image = jq$('<span/>', {
			"id" : 'maximizeCompositeView',
			"class" : 'minimized'
		});
		var titlebar = jq$(".ui-dialog-titlebar");
		jq$(titlebar).append(image);
		registerMaximizeEvent(image);
	}

	function changeButtons() {
		var pane = jq$("#compositeEdit").next("div.ui-dialog-buttonpane");
		if (_CE.mode == _CE.ModeEnum.EDIT) {
			jq$(pane).find(".closeButton").addClass("saveButton").removeClass("closeButton").find(".ui-button-text").text("Save");
			jq$(pane).find(".editButton").addClass("cancelEditButton").removeClass("editButton").find(".ui-button-text").text("Cancel");
		}
		else {
			jq$(pane).find(".saveButton").addClass("closeButton").removeClass("saveButton").find(".ui-button-text").text("Close");
			jq$(pane).find(".cancelEditButton").addClass("editButton").removeClass("cancelEditButton").find(".ui-button-text").text("Edit");
		}
	}

	function enableCompositeEditMode() {
		if (_CE.userCanWriteAllSections) {
			_CE.mode = _CE.ModeEnum.EDIT;
			initCompositeEdit();
			changeButtons();
			registerButtonEvents();
			addEventListenerForEdit(jq$("#compositeEdit"));
			bindUnloadFunctions()
		}
	}

	return {

		dialogDiv : null,

		/** Indicates whether edit mode is enabled */
		enabled : false,

		/** Caches the old sections for reuse */
		cancelCache : {},

		/** The namespace for each section */
		toolNameSpace : {},

		/** All sections that are compatible with edit mode */
		editableSections : [],

		/** User has write rights for all sections */
		userCanWriteAllSections : true,

		ModeEnum : {
			VIEW : "view",
			EDIT : "edit"
		},

		mode : "view",

		disable : function() {
			_CE.enabled = false;
			unbindUnloadFunctions();
			_IE.disable(_CEWT.rootID, false);
			_CE.dialogDiv.dialog("close");
			_CE.dialogDiv.dialog('destroy').remove();
			_CE.dialogDiv = undefined;
			document.body.style.overflow = 'auto';
			restoreDefaultKeybindings();
			unregisterSaveCancelEvents(document, _CE.save, enableCompositeViewMode);
			jq$(document).off('keydown', escapeFunction);
		},

		save : function() {

			jsonObj = [];
			jq$('#compositeEdit .editarea').each(function() {

				var id = jq$(this).attr("sectionid");
				var text = _CEWT.getSectionText(id);

				jsonObj.push({
					id : id,
					text : text
				});
			});

			jq$.each(_CEWT.deletes, function(index, id) {

				jsonObj.push({
					id : id,
					text : ""
				});
			});

			var params = {
				action : 'CompositeEditSaveAction',
				replaceSections : jsonObj
			};

			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					action : 'none',
					fn : function() {
						$(window).removeEvents('beforeunload');
						$(window).removeEvents('unload');
						_EC.hideAjaxLoader();
						_CE.disable();
						_IE.disable(_CEWT.rootID, true);
					},
					onError : _EC.onErrorBehavior
				}
			};
			new _KA(options).send();

		},

		cancelSection : function(sectionId) {
			var sectionId = sectionId; // we need this -> JS closure
			var fn = function() {
				var cached = _CE.cancelCache[sectionId];
				jq$('#compositeEdit div[sectionid=' + sectionId + ']').replaceWith(cached);
				addEventListenerForEdit(jq$("#compositeEdit div[sectionid=" + sectionId + "]").parent());
			};
			_IE.disable(sectionId, false, fn)
			_TM.decorateToolMenus('#compositeEdit div[sectionid=' + sectionId + '] .defaultMarkupFrame');
			enableCompositeViewToolMenus(jq$('#compositeEdit div[sectionid=' + sectionId + ']'));
		},

		deleteSection : function(id) {
			var del = confirm("Do you really want to delete this content?");
			if (del) {
				_CE.cancelSection(id);
				_CEWT.deleteSection(id);
				getSection(id).remove();
			}
		},

		openCompositeEditDialog : function(identifier) {
			closeOldDialog();
			resetVariables();
			var divMarkupText = buildDefaultMarkupStructure();
			var options = createCompositeEditDialog(identifier, divMarkupText);
			new _KA(options).send();
			appendDialogToHtmlBody();
		},

		afterPreviewsLoad : function(id) {
			if (_CE.mode == _CE.ModeEnum.EDIT) {
				initCompositeEdit(id);
				jq$(".toolsMenuDecorator").click(function(e) {
					e.stopPropagation();
				});
			}
		},

		enableCompositeEditModeByKey : function(event) {
			event = jq$.event.fix(event);
			if (_EC.isModifier(event) || _EC.isDoubleModifier(event)) {
				if (event.which == 68) { // D
					jq$(document).off('keydown', _CE.enableCompositeEditModeByKey);
					event.stopPropagation();
					event.preventDefault();
					enableCompositeEditMode();
				}
				else if (isEditSourceKey(event)) { // E
					event.stopPropagation();
					event.preventDefault();
					enableCompositeEditMode();
				}
			}
		}


	}
}();

KNOWWE.plugin.compositeEditTool.wikiText = function() {

	function fixLineBreaks(i, newText) {
		var order = _CEWT.getOriginalOrder();
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
		var sectionOrder = [];
		jq$('.editelement, .editarea').each(function() {
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
		rootID : null,

		/** Original Wiki-Text */
		text : null,

		/** Original ordering of the sections */
		order : null,

		/** Deleted sections */
		deletes : [],

		/** is the article locked? */
		locked : null,

		hasChanged : function() {
			if (_CEWT.deletes.length > 0) {
				return true;
			}
			if (JSON.stringify(_CEWT.order) != JSON
					.stringify(getSectionOrder())) {
				return true;
			}
			for (var i = 0; i < _CE.editableSections.length; i++) {
				var id = _CE.editableSections[i];
				var unloadCondition = _CE.toolNameSpace[id].unloadCondition;
				if (unloadCondition) {
					var hasToolChanged = true;
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

		getSectionText : function(id) {

			if (jq$.inArray(id, _CEWT.deletes) > -1) {
				return "";
			}
			var toolNameSpace = _CE.toolNameSpace[id];
			var wikiText = null;
			try {
				wikiText = toolNameSpace.generateWikiText(id);
			} catch (err) {
				// the tool might not be initialized or has
				// other errors
				wikiText = _EC.getWikiText(id)
			}
			return wikiText;
		},

		getOriginalText : function() {
			return _CEWT.text;
		},

		getOriginalOrder : function() {
			return _CEWT.order;
		},

		isLocked : function() {
			return _CEWT.locked;
		},

		deleteSection : function(id) {
			_CEWT.deletes.push(id);
		}
	}
}();

var _CE = KNOWWE.plugin.compositeEditTool;
var _CEWT = KNOWWE.plugin.compositeEditTool.wikiText;

//fixing jquery(version 2.1) bug where HTML code is not parsed in titlebar
jq$.widget("ui.dialog", jq$.extend({}, jq$.ui.dialog.prototype, {
	_title : function(title) {
		if (!this.options.title) {
			title.html("&#160;");
		} else {
			title.html(this.options.title);
		}
	}
}));

