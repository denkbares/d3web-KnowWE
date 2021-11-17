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
  let KNOWWE = {};
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

  let recentlyDragged = false;

  function initCompositeEdit(id) {

    if (!id) {
      id = jq$('.objectInfoPanel');
    }

    if (_IE.enabled) {
      alert("You are already editing the page. Please finish your current edit before entering composite edit.");
      return;
    }
    _KU.showProcessingIndicator();

    initSections(id);
    if (_CE.mode === _CE.ModeEnum.EDIT) {
      addEventListenerForEdit(id);
    }
    // TODO: replace with dialog specific function
    // bindUnloadFunctions();


    if (_CEWT.locked) {
      const message = "Another user has started to edit this page, but "
        + "hasn't yet saved it. You are allowed to further edit this page, but be "
        + "aware that the other user will not be pleased if you do so!";
      KNOWWE.notification.warn("Locked Article", message);
    }

    _EC.mode = _CE;
    _CE.enabled = true;

    _KU.hideProcessingIndicator();
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
        const editAnchor = jq$(this);
        const sectionId = editAnchor.attr('sectionid');
        let topLvlAnchorElement = editAnchor;

        topLvlAnchorElement = topLvlAnchorElement.prev("");

        let editElement = topLvlAnchorElement;

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
        if (sectionId && jq$('#' + sectionId).length === 0
          && editElement.attr('sectionid') === sectionId) {
          editElement.attr('id', sectionId);
        }

        if (sectionId) {
          _CE.editableSections.push(sectionId);
        }

        // we no longer need the anchor
        editAnchor.remove();
      });
    jq$(id).find("p:empty").remove();
  }

  function prepareEditElementContents(id) {
    let beforeFirstElement = true;
    let afterLastElement = false;
    jq$(id).find('.editelement').each(function() {
      const editElement = jq$(this);
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
      let next = editElement.next();
      while (next.length === 1 && !next.is('.editelement')) {
        next.remove();
        editElement.append(next);
        next = editElement.next();
      }
      // if elements are aligned, they are not properly selectable
      // for editing
      editElement.children().each(function() {
        const align = jq$(this).attr('align');
        if (align != null) {
          jq$(this).attr('align', null);
        }
      });
    });
  }

  function initWikiText() {
    const ids = _CE.editableSections.join(";");

    const params = {
      action: 'InitEditModeAction'
    };

    const options = {
      url: KNOWWE.core.util.getURL(params),
      async: false,
      data: ids,
      response: {
        action: 'none'
      }
    };

    const ajaxCall = new _KA(options);
    ajaxCall.send();
    const response = ajaxCall.getResponse();
    const json = JSON.parse(response);

    // store toolNameSpace and wikiText for each section
    const sections = json.sections;
    if (sections != null) {
      for (let i = 0; i < sections.length; i++) {
        let section = sections[i];
        _CE.toolNameSpace[section.id] = eval(section.namespace);
        _EC.wikiText[section.id] = section.wikitext;
      }
    }

    // for inline sections we combine the text of the other inlined
    // sections into the id of the first/main id
    let lastEditElementId = null;
    let combinedText = "";
    for (let i = 0; i < _CE.editableSections.length; i++) {
      let sectionId = _CE.editableSections[i]
      let section = getSection(sectionId);
      if (section.length === 1) {
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
      const sectionId = jq$(this).attr('sectionId');
      if (sectionId)
        _CEWT.order.push(sectionId);
    });
    _CEWT.rootID = json.root;
    _CEWT.text = json.wikitext;
    _CEWT.locked = json.locked;
  }

  function addEventListenerForEdit(root) {

    const found = jq$(root).find('.editelement');
    jq$(root).find('.editelement').each(function() {
      const editElement = jq$(this);
      const sectionID = editElement.attr('sectionid');

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
    const found = jq$(root).find('.editelement');
    jq$(root).find('.editelement').each(function() {
      const editElement = jq$(this);
      editElement.unbind('click');
      const sectionID = editElement.attr('sectionid');
      editElement.unbind('mouseout');
      editElement.unbind('mouseover');
    });
  }

  function isEditSourceKey(event) {
    // E, but not with alt gr (= alt + ctrl) to allow for € in windows
    return event.which === 69 && (!event.altKey || !event.ctrlKey);
  }

  function getSection(sectionId) {
    return jq$('#compositeEdit div[sectionId="' + sectionId + '"]').filter('.editelement');
  }

  function enableEditArea(sectionId) {

    _KU.showProcessingIndicator();

    const params = {
      action: 'InstantEditEnableAction',
      KdomNodeId: sectionId
    };

    const options = {
      url: KNOWWE.core.util.getURL(params),
      response: {
        action: 'none',
        fn: function() {

          // store the current version of this section for restoring
          _CE.cancelCache[sectionId] = getSection(sectionId);

          const toolNameSpace = _CE.toolNameSpace[sectionId];

          // show edit area
          const json = JSON.parse(this.responseText);
          const locked = json.locked;
          let html = toolNameSpace.generateHTML(sectionId);
          html = wrapHTML(sectionId, locked, html);
          getSection(sectionId).replaceWith(html);

          _EC.registerSaveCancelEvents(getSection(sectionId),
            _CE.save, _CE.cancelSection, sectionId);

          // show toolmenu
          appendDefaultMarkupFrameToolMenu(sectionId);

          toolNameSpace.postProcessHTML(sectionId);
          try {
            // this is a workaround to make auto completion work for default edit tool
            // we should handle auto completion and composite edit in general in the tool namespaces
            postProcessHTML(id);
          } catch (e) {
            console.log(e);
          }

          jq$('#compositeEdit div.defaultMarkupFrame[compositeedit=' + sectionId + ']').each(function(index, frame) {
            _TM.animateDefaultMarkupMenu(jq$(frame));
          });
          _KU.hideProcessingIndicator();
        },
        onError: _EC.onErrorBehavior
      }
    };
    new _KA(options).send();
  }

  function createTextAreaID(id) {
    return "defaultEdit" + id;
  }

  function postProcessHTML(id) {
    const textarea = jq$('#' + createTextAreaID(id))[0];
    if (typeof AutoComplete != "undefined") {
      new AutoComplete(textarea, function(callback, prefix) {
        const scope = "root";
        const data = {sectionId: id, prefix: prefix, scope: scope};
        if (KNOWWE && KNOWWE.helper) {
          data.KWiki_Topic = KNOWWE.helper.gup('page');
        }
        jq$.ajax({
          url: 'action/CompositeEditCompletionAction',
          cache: false,
          data: data
        }).success(function(data) {
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
    let lockedHTML = "";
    if (locked) {
      lockedHTML = "<div class=\"error\">Another user has started to edit this page, but " + "hasn't yet saved it. You are allowed to further edit this page, but be " + "aware that the other user will not be pleased if you do so!</div>"
    }
    const openingDiv = "<div sectionid='" + id + "' class='editarea'><div class='defaultMarkupFrame' compositeEdit='" + id + "'>";
    const closingDiv = "</div></div>";

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
    const editField = jq$(".defaultMarkupFrame[compositeEdit=" + sectionId + "]");
    editField.css('position', 'relative');
    const headerMenu = jq$('<div/>', {
      'class': 'ueedittoolbar markupHeaderFrame headerMenu'
    });
    const markupHeader = generateMarkUpHeader(sectionId);
    const markUpMenu = generateMarkUpMenu(sectionId);
    headerMenu.append(markupHeader);
    headerMenu.append(markUpMenu);
    jq$(editField).prepend(headerMenu);
  }

  function generateMarkUpHeader(sectionId) {
    const markupHeader = jq$('<div/>', {
      "class": 'markupHeader'
    });
    const img = jq$('<i/>', {
      "class": 'fa fa-fw fa-lg fa-angle-down'
    });
    markupHeader.append(img);
    return markupHeader;
  }

  function generateMarkUpMenu(sectionId) {
    const markupMenu = jq$('<div/>', {
      "class": 'markupMenu'
    });
    const cancel = generateMarkUpmenuItem("_CE.cancelSection(\'" + sectionId + "\')", "Revert", "fa-undo");
    const del = generateMarkUpmenuItem("_CE.deleteSection(\'" + sectionId + "\')", "Delete this section", "fa-times");
    markupMenu.append(cancel);
    markupMenu.append(del);
    return markupMenu;
  }

  function generateMarkUpmenuItem(js, text, icon) {
    const markupMenuItemDiv = jq$('<div/>', {
      "class": 'markupMenuItem'
    });
    const varMenuItemA = jq$('<a/>', {
      "class": 'markupMenuItem',
      "href": "javascript:" + js
    });
    const img = jq$('<i/>', {
      "class": "fa fa-fw " + icon
    });
    const span = jq$('<span/>', {
      "text": text
    });
    varMenuItemA.append(img);
    varMenuItemA.append(span);
    markupMenuItemDiv.append(varMenuItemA);
    return markupMenuItemDiv;
  }

  function appendMarker(id, reference, color) {
    const marker = new Element('div', {
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
      id: 'compositeEdit',
      "class": 'defaultMarkup',
      text: ''
    });
    const divMarkupText = jq$('<div/>', {
      "class": 'markupText',
      text: ''
    });
    _CE.dialogDiv.append(divMarkupText);
    return divMarkupText;
  }

  function appendDialogToHtmlBody() {
    jq$("body").append(_CE.dialogDiv);
  }

  function disableCompositeEditMode() {
    for (let section in _CE.cancelCache) {
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
    const pane = jq$("#compositeEdit").next("div.ui-dialog-buttonpane");
    if (_CE.mode === _CE.ModeEnum.VIEW) {
      unregisterSaveCancelEvents(document, _CE.save, enableCompositeViewMode);
      jq$(pane).find(".closeButton").unbind().on("click", function() {
        _CE.disable();
      });
      jq$(pane).find(".editButton").unbind().on("click", function() {
        enableCompositeEditMode();
      });
    } else {
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
      const pane = jq$("#compositeEdit").next("div.ui-dialog-buttonpane");
      jq$(pane).find(".editButton").attr('disabled', true).attr('title', "You can't edit these sections, because you don't have write permissions on all sections.");
    }
    document.body.style.overflow = 'hidden';
    appendMaximizeButton();
    if (_CE.mode === _CE.ModeEnum.EDIT) {
      enableCompositeEditMode();
    } else {
      enableCompositeViewMode();
    }
    _TM.decorateToolMenus(jq$('#compositeEdit'));
    jq$('#compositeEdit .defaultMarkupFrame').click(function(e) {
      e.stopPropagation();
    });
    _TM.animateDefaultMarkupMenu(jq$('#compositeEdit'));
    _TM.adjustSingletonMenus(jq$('#compositeEdit'));
    jq$(".extend-panel-right").click(function(e) {
      KNOWWE.core.util.form.showExtendedPanel.call(new KNOWWE.helper.element(this)); // TODO: Try to not use this ancient stuff
      loadPreviews(jq$(this).next('div'));
      saveExpandState(this);
    });
    jq$(document).on('keydown', escapeFunction);
    expandSavedStates();

    KNOWWE.tooltips.enrich();
  }

  /**
   * Load the ajax-previews
   */
  function loadPreviews(root) {
    const select = (root === undefined)
      ? jq$('.asynchronPreviewRenderer')
      : jq$(root).find('.asynchronPreviewRenderer');
    const json = [];
    const ids = [];
    select.filter((i, e) => e.parentElement.style.display === "inline").each(function() {
      let attribute = this.getAttribute('rel');
      json.push(attribute);
      ids.push(this.id);
    });
    jq$.ajax("action/RenderPreviewAction", {
      type: 'post',
      data: JSON.stringify(json),
      contentType: 'application/json, UTF-8',
      success: function(html) {
        KNOWWE.core.util.replaceElement(ids, html);
        if (jq$(root).parents('#compositeEdit').length) {
          _CE.afterPreviewsLoad(root);
          KNOWWE.core.actions.init();
        }
        highlightTermReferences(root, json);
        _TM.decorateToolMenus(root);
        _TM.animateDefaultMarkupMenu(root);
        _TM.adjustSingletonMenus(root);
        /**
         * Trigger custom Event here to mount the React components so that the
         * Item can be renderd on the users side. This even will be triggered
         * multiple times since loadPreviews is called async. the react components
         * will handle changes so that only new items will be rendered.
         */
        jq$("body").trigger("OpenCompositeEdit");
      }
    });
  }

  function highlightTermDefinitions() {
    const edit = jq$('#compositeEdit');
    const rel = edit.find('.relevantSections').attr('rel');
    if (rel) {
      rel.split(',').forEach(s => edit.find("[sectionid='" + s + "']").addClass("highlight"));
    }
  }

  function highlightTermReferences(root, sectionIds) {
    let sectionIdSplit = [];
    sectionIds.forEach(s => sectionIdSplit = sectionIdSplit.concat(s.split(",")));
    sectionIdSplit.forEach(s => jq$(root).find("[sectionid='" + s + "']").addClass("highlight"));
  }


  function initExpandAllButtons() {
    resetButtonTexts();

    jq$('#compositeEdit').find('.extend-panel').click(function() {
      resetButtonTexts();
    });
    jq$('#compositeEdit').find('.show.extend').click(function() {
      resetButtonTexts();
    });

    jq$('#compositeEdit').find('.expandPanelButton').click(function(event) {
      let parent = jq$(this).closest('.pointer');
      let expanded = isParentExpanded(jq$(this));
      if (isOpen(parent) && !expanded) event.stopPropagation();
      const toggleClassName = expanded ? '.extend-panel-down' : '.extend-panel-right';
      parent.siblings('.objectInfoPanel').find(toggleClassName).click();
      setExpandButtonText(jq$(this));
    });

    function resetButtonTexts() {
      jq$('#compositeEdit').find('.expandPanelButton').each(function() {
        setExpandButtonText(jq$(this));
      });
    }

    function setExpandButtonText(button) {
      button.html(isParentExpanded(button) ? "collapse all" : "expand all");
    }

    function isParentExpanded(button) {
      return isExpanded(button.closest('.pointer'));
    }

    function isExpanded(panel) {
      return isOpen(panel) && panel.next('.objectInfoPanel').find('.extend-panel-right').length === 0;
    }

    function isOpen(panel) {
      return panel.hasClass('extend-panel-down');
    }
  }

  function escapeFunction(event) {
    if (event.keyCode === jq$.ui.keyCode.ESCAPE) {
      _CE.disable();
    }
  }

  function createCompositeEditDialog(identifier, divMarkupText, action) {
    const params = {
      action: action,
      termIdentifier: identifier
    };

    const options = {
      url: KNOWWE.core.util.getURL(params),
      response: {
        action: 'none',
        fn: function() {
          if (this.responseText) {
            const parsed = JSON.parse(this.responseText);
            divMarkupText.append(parsed.result);
            _CE.userCanWriteAllSections = parsed.canWriteAll;


            _CE.dialogDiv.dialog({
              //fix for strange behaviour (=scrolling to top) of dialog at first "mousedown" event
              open: function(event, ui) {
                jq$(this).mousedown(function(event) {
                  event.stopPropagation();
                });
                jq$("body").trigger("OpenCompositeEdit");
              },
              closeOnEscape: false,
              dialogClass: "no-close",
              height: (jq$(window).height() * .9),
              width: jq$(document).width() * .6,
              title: parsed.header,
              resizable: true,
              draggable: true,
              modal: true,
              buttons: [
                {
                  text: "Close",
                  'class': 'closeButton'
                },
                {
                  text: "Edit",
                  'class': 'editButton'
                }
              ]
            });
          }
          enableInitialDialogState();
          highlightTermDefinitions();
          initExpandAllButtons();
          _KU.hideProcessingIndicator();
        },
        onError: _EC.onErrorBehavior

      }
    };
    return options;
  }

  function enableCompositeViewToolMenus(id) {

    _TM.animateDefaultMarkupMenu(jq$(id));
    _TM.decorateToolMenus(jq$(id));
    _TM.adjustSingletonMenus(jq$(id));
    jq$(id).find("a").click(function(e) {
      e.stopPropagation();
    });
  }

  function saveExpandState(node) {
    const prefix = "CompositeEditCollapseState-";
    const type = jq$(node).find("strong").text();
    const term = jq$("#objectinfo-src").text();
    let typeArray;
    if (jq$(node).hasClass("extend-panel-down")) {
      typeArray = simpleStorage.get(prefix + term);
      if (typeof typeArray == "undefined") {
        typeArray = new Array(type);
        simpleStorage.set(prefix + term, typeArray);
      }
      if (jq$.inArray(type, typeArray) === -1) {
        typeArray.push(type);
        simpleStorage.set(prefix + term, typeArray);
      }
    } else {
      typeArray = simpleStorage.get(prefix + term);
      typeArray.splice(typeArray.indexOf(type), 1);
      simpleStorage.set(prefix + term, typeArray);
    }
  }


  function expandSavedStates() {
    const prefix = "CompositeEditCollapseState-";
    const term = jq$("#objectinfo-src").text();
    const typeArray = simpleStorage.get(prefix + term);
    if (typeof typeArray !== "undefined") {
      for (let i = 0; i < typeArray.length; i++) {
        const type = typeArray[i];
        const p = jq$(".extend-panel-right strong").filter(function() {
          return jq$(this).text() === type;
        }).parent().removeClass("extend-panel-right").addClass("extend-panel-down");
        const section = jq$(p).next('div');
        jq$(section).css("display", "inline");
        loadPreviews(section);
      }
    }
  }

  function registerMaximizeEvent(image) {
    var image = jq$(image);
    jq$(image).on("click", function() {
      if (image.hasClass("minimized")) {
        image.removeClass("minimized").addClass("maximized");
        jq$(_CE.dialogDiv).parents(".ui-dialog:first").animate({
          width: window.innerWidth,
          height: window.innerHeight
        }, {
          duration: 500,
          step: function() {
            jq$(_CE.dialogDiv).dialog('option', 'position', {my: 'center', at: 'center', of: window});
          }
        });
        jq$(_CE.dialogDiv).dialog("option", "height", window.innerHeight);
        jq$(_CE.dialogDiv).dialog("option", "width", window.innerHeight);
      } else {
        image.removeClass("maximized").addClass("minimized");
        jq$(_CE.dialogDiv).parents(".ui-dialog:first").animate({
          width: window.innerWidth * .6,
          height: window.innerHeight * .9
        }, {
          duration: 500,
          step: function() {
            jq$(_CE.dialogDiv).dialog('option', 'position', {my: 'center', at: 'center', of: window});
          }
        });
        jq$(_CE.dialogDiv).dialog("option", "height", window.innerHeight * .9);
        jq$(_CE.dialogDiv).dialog("option", "width", window.innerHeight * .6);
      }
    });
  }

  function appendMaximizeButton() {
    const image = jq$('<span/>', {
      "id": 'maximizeCompositeView',
      "class": 'minimized'
    });
    const titlebar = jq$(".ui-dialog-titlebar");
    jq$(titlebar).append(image);
    registerMaximizeEvent(image);
  }

  function changeButtons() {
    const pane = jq$("#compositeEdit").next("div.ui-dialog-buttonpane");
    if (_CE.mode === _CE.ModeEnum.EDIT) {
      jq$(pane).find(".closeButton").addClass("saveButton").removeClass("closeButton").find(".ui-button-text").text("Save");
      jq$(pane).find(".editButton").addClass("cancelEditButton").removeClass("editButton").find(".ui-button-text").text("Cancel");
    } else {
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

    dialogDiv: null,

    /** Indicates whether edit mode is enabled */
    enabled: false,

    /** Caches the old sections for reuse */
    cancelCache: {},

    /** The namespace for each section */
    toolNameSpace: {},

    /** All sections that are compatible with edit mode */
    editableSections: [],

    /** User has write rights for all sections */
    userCanWriteAllSections: true,

    ModeEnum: {
      VIEW: "view",
      EDIT: "edit"
    },

    mode: "view",

    disable: function() {
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

    save: function() {

      jsonObj = [];
      jq$('#compositeEdit .editarea').each(function() {

        const id = jq$(this).attr("sectionid");
        const text = _CEWT.getSectionText(id);

        jsonObj.push({
          id: id,
          text: text
        });
      });

      jq$.each(_CEWT.deletes, function(index, id) {

        jsonObj.push({
          id: id,
          text: ""
        });
      });

      const params = {
        action: 'CompositeEditSaveAction',
        replaceSections: jsonObj
      };

      const options = {
        url: KNOWWE.core.util.getURL(params),
        response: {
          action: 'none',
          fn: function() {
            window.onbeforeunload = null;
            window.onunload = null;
            _KU.hideProcessingIndicator();
            _CE.disable();
            _IE.disable(_CEWT.rootID, true);
          },
          onError: _EC.onErrorBehavior
        }
      };
      _KU.showProcessingIndicator();
      new _KA(options).send();

    },

    cancelSection: function(sectionId) {
      let id = sectionId; // we need this for the JS closure
      const fn = function() {
        const cached = _CE.cancelCache[id];
        jq$('#compositeEdit div[sectionid=' + id + ']').replaceWith(cached);
        addEventListenerForEdit(jq$("#compositeEdit div[sectionid=" + id + "]").parent());
      };
      _IE.disable(id, false, fn)
      _TM.decorateToolMenus(jq$('#compositeEdit div[sectionid=' + id + '] .defaultMarkupFrame'));
      enableCompositeViewToolMenus(jq$('#compositeEdit div[sectionid=' + id + ']'));
    },

    deleteSection: function(id) {
      const del = confirm("Do you really want to delete this content?");
      if (del) {
        _CE.cancelSection(id);
        _CEWT.deleteSection(id);
        getSection(id).remove();
      }
    },

    openCompositeEditDialog: function(identifier) {
      this.openDialog(identifier, 'CompositeEditOpenDialogAction');
    },

    openDialog: function(identifier, action) {
      closeOldDialog();
      resetVariables();
      _KU.showProcessingIndicator();
      const divMarkupText = buildDefaultMarkupStructure();
      const options = createCompositeEditDialog(identifier, divMarkupText, action);
      new _KA(options).send();
      appendDialogToHtmlBody();
    },

    afterPreviewsLoad: function(id) {
      if (_CE.mode === _CE.ModeEnum.EDIT) {
        initCompositeEdit(id);
        jq$(".toolsMenuDecorator").click(function(e) {
          e.stopPropagation();
        });
      }
    },

    enableCompositeEditModeByKey: function(event) {
      event = jq$.event.fix(event);
      if (_EC.isModifier(event) || _EC.isDoubleModifier(event)) {
        if (event.which === 68) { // D
          jq$(document).off('keydown', _CE.enableCompositeEditModeByKey);
          event.stopPropagation();
          event.preventDefault();
          enableCompositeEditMode();
        } else if (isEditSourceKey(event)) { // E
          event.stopPropagation();
          event.preventDefault();
          enableCompositeEditMode();
        }
      }
    },

    closeOldDialog: function() {
      if (typeof _CE.dialogDiv !== 'undefined') {
        jq$(_CE.dialogDiv).dialog("close");
        jq$(_CE.dialogDiv).dialog("destroy").remove();
      }
    },

    resetVariables: function() {
      _CE.cancelCache = {};
      _CE.editableSections = [];
      _CE.enabled = true;
    },


  }
}();

KNOWWE.plugin.compositeEditTool.wikiText = function() {

  function fixLineBreaks(i, newText) {
    const order = _CEWT.getOriginalOrder();
    if (i === 0)
      return;
    const lastId = order[i - 1];
    const id = order[i];
    let last = newText[lastId];
    let current = newText[id];
    const lastOriginalText = getSectionText(lastId);

    // moved sections should end with a line break if they are
    // followed by an line break at the start of the new section
    // if possible and needed remove a line break from the
    // current (lower or following) section and add it
    // to the start of the last (upper) section
    const endingLineBreak = /\r?\n$/;
    const startingLineBreak = /^\r?\n/;
    if (!endingLineBreak.test(last) && startingLineBreak.test(current)) {
      last += current.match(startingLineBreak)[0];
      current = current.replace(startingLineBreak, "");
    }
    // if we have the last section, make sure it ends with a
    // line break in case it got moved
    if (i === order.length - 1) {
      const newOrder = getSectionOrder();
      const newPos = jq$.inArray(id, newOrder);
      if (newPos !== -1 && newPos !== newOrder.length - 1) {
        current += "\n";
      }
    }
    // if the last version of the section ended with an line break,
    // we force the new version also to have a line break at the end
    // to avoid users accidentally merging section because of the missing
    // line break
    const currentOriginalText = _EC.getWikiText(id);
    if (endingLineBreak.test(currentOriginalText)
      && !endingLineBreak.test(current)) {
      current += currentOriginalText.match(endingLineBreak)[0];
    }

    newText[lastId] = last;
    newText[id] = current;
  }

  function getSectionOrder() {
    const sectionOrder = [];
    jq$('.editelement, .editarea').each(function() {
      let id = jq$(this).attr('id');
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
    deletes: [],

    /** is the article locked? */
    locked: null,

    hasChanged: function() {
      if (_CEWT.deletes.length > 0) {
        return true;
      }
      if (JSON.stringify(_CEWT.order) !== JSON
        .stringify(getSectionOrder())) {
        return true;
      }
      for (let i = 0; i < _CE.editableSections.length; i++) {
        const id = _CE.editableSections[i];
        const unloadCondition = _CE.toolNameSpace[id].unloadCondition;
        if (unloadCondition) {
          let hasToolChanged = true;
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

    getSectionText: function(id) {

      if (jq$.inArray(id, _CEWT.deletes) > -1) {
        return "";
      }
      const toolNameSpace = _CE.toolNameSpace[id];
      let wikiText = null;
      try {
        wikiText = toolNameSpace.generateWikiText(id);
      } catch (err) {
        // the tool might not be initialized or has
        // other errors
        wikiText = _EC.getWikiText(id)
      }
      return wikiText;
    },

    getOriginalText: function() {
      return _CEWT.text;
    },

    getOriginalOrder: function() {
      return _CEWT.order;
    },

    isLocked: function() {
      return _CEWT.locked;
    },

    deleteSection: function(id) {
      _CEWT.deletes.push(id);
    }
  }
}();

const _CE = KNOWWE.plugin.compositeEditTool;
const _CEWT = KNOWWE.plugin.compositeEditTool.wikiText;

//fixing jquery(version 2.1) bug where HTML code is not parsed in titlebar
jq$.widget("ui.dialog", jq$.extend({}, jq$.ui.dialog.prototype, {
  _title: function(title) {
    if (!this.options.title) {
      title.html("&#160;");
    } else {
      title.html(this.options.title);
    }
  }
}));

