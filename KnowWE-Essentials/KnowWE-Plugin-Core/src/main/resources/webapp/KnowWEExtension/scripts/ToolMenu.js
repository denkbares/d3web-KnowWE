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

function ToolMenu() {
  this.cache = {};
  this.lastMenuId = null;
}

ToolMenu.prototype.decorateToolMenus = function($parent) {
  if (!$parent) $parent = jq$('.toolMenuParent');
  if ($parent.attr("id") === "compositeEdit") _TM.adjustSingletonMenus();
  $parent.find('.toolsMenuDecorator, .toolsMenuDecorator2').each(function() {
    const $decorator = jq$(this);
    if ($decorator.data('toolMenuDecorated') === 'true') return;
    if ($decorator.is('.toolsMenuDecorator')) {
      $decorator.parent().hover(() => $decorator.css('visibility', 'visible'), () => $decorator.css('visibility', 'hidden'));
    } else { // toolsMenuDecorator2
      $decorator.parent().hover(() => $decorator.css("border-color", "grey"), () => $decorator.css("border-color", "transparent"));
    }
    $decorator.click(() => _TM.showToolPopupMenu($decorator));
    $decorator.dblclick(() => {
      _TM.hideToolsPopupMenu();
      _TM.selectTerm(this);
    });
    $decorator.data('toolMenuDecorated', 'true');
    //prevent "click through" in composite edit
    $decorator.click(event => event.stopPropagation());
  });
};

ToolMenu.prototype.selectTerm = function(element) {
  let clickableTerm = jq$(element).parent().find('.clickable-term')[0] || jq$(element).parents('.toolMenuDecorated')[0] || jq$(element)[0];
  const selection = window.getSelection();
  const range = document.createRange();
  range.selectNode(clickableTerm);
  selection.removeAllRanges();
  selection.addRange(range);
};

ToolMenu.mouseMoveEvent = 'mousemove.toolmenu';

/**
 * Distance in pixels the mouse may move out of a tool menu or one of its dropdowns without closing it.
 */
ToolMenu.mouseTolerance = 30;

/**
 * Returns whether the mouse of the given event is over one of the given elements or at most the given distance away
 * from it. Hidden elements (without size) are ignored.
 *
 * @param {Event} event the mouse event (also a wrapped mootools/jQuery event)
 * @param {Element[]} elements the elements to check
 * @param {number} [tolerance=ToolMenu.mouseTolerance] the distance in pixels
 */
ToolMenu.prototype.isMouseNear = function (event, elements, tolerance = ToolMenu.mouseTolerance) {
  event = event.event || event.originalEvent || event;
  return Array.from(elements).some(element => {
    if (!element) return false;
    const rect = element.getBoundingClientRect();
    if (rect.width === 0 && rect.height === 0) return false;
    return event.clientX >= rect.left - tolerance && event.clientX <= rect.right + tolerance
      && event.clientY >= rect.top - tolerance && event.clientY <= rect.bottom + tolerance;
  });
};

/**
 * @param {jQuery} $node HTML element for which to display the tool menu (used for positioning)
 * @param {Object} [args={}] optional set of additional arguments for the KnowWE action that returns the KnowWE tools
 */
ToolMenu.prototype.showToolPopupMenu = function ($node, args = {}) {
  // we hide all open tooltipster tool tips to reduce clutter
  jq$(".tooltipstered").tooltipster("hide");

  // hide tool menus opened for other terms
  const lastMenuId = this.lastMenuId;
  this.hideToolsPopupMenu();
  const currentMenuId = jq$($node).attr("id") || jq$($node).data("identifier");
  if (lastMenuId === currentMenuId) return;
  this.lastMenuId = currentMenuId;

  let node = $node[0];
  let windowHeight = window.getHeight();
  let windowWidth = window.getWidth();
  let nodeRect = node.getBoundingClientRect();
  let styles = {
    "z-index": "10000",
    position: "fixed",
  };
  const mouseMovePadding = 5;
  // the row of the node, extended to the width of the menu: moving the mouse from the node to a tool of the menu
  // (e.g. to the dropdown arrow at its right side), the mouse often passes this area outside both, the menu stays open
  const isInNodeRow = function (event) {
    event = event.event || event; // mootools events wrap the native one
    const menu = document.getElementById("toolPopupMenuID");
    const menuRect = menu ? menu.getBoundingClientRect() : nodeRect;
    return (
      event.clientY >= nodeRect.top &&
      event.clientY <= nodeRect.bottom &&
      event.clientX >= Math.min(nodeRect.left, menuRect.left) - mouseMovePadding &&
      event.clientX <= Math.max(nodeRect.right, menuRect.right) + mouseMovePadding
    );
  };
  // the mouse may leave the menu and its open dropdowns by this distance without closing the menu
  const isNearMenu = function (event) {
    const menu = document.getElementById("toolPopupMenuID");
    if (!menu) return false;
    return _TM.isMouseNear(event, [menu, ...menu.querySelectorAll(".tool-targets.open, .xcl-editor-dropdown")],
      ToolMenu.mouseTolerance);
  };
  // set as soon as the mouse left the menu, from then on the menu closes if the mouse is no longer near it
  let leftMenu = false;
  const showAbove = nodeRect.top > windowHeight - nodeRect.top + nodeRect.height;
  if (showAbove) {
    styles.bottom = windowHeight - nodeRect.top - 1 + "px";
  } else {
    styles.top = nodeRect.top + nodeRect.height + "px";
  }
  jq$(document.body)
    .unbind(ToolMenu.mouseMoveEvent)
    .bind(ToolMenu.mouseMoveEvent, function (event) {
      // beyond the node, on the side opposite to the menu, or beside the node and the menu
      const beyond = showAbove ? event.clientY > nodeRect.bottom : event.clientY < nodeRect.top;
      const besideNode = event.clientY >= nodeRect.top && event.clientY <= nodeRect.bottom && !isInNodeRow(event);
      const awayFromMenu = leftMenu && !isInNodeRow(event);
      if ((beyond || besideNode || awayFromMenu) && !isNearMenu(event)) {
        _TM.hideToolsPopupMenu();
        jq$(this).unbind(ToolMenu.mouseMoveEvent);
      }
    });
  if (nodeRect.left > windowWidth - nodeRect.left + nodeRect.width) {
    // expand to the left
    styles.right =
      Math.max(0, windowWidth - nodeRect.left - nodeRect.width) + "px";
  } else {
    // expand to the right
    styles.left = Math.max(0, nodeRect.left) + "px";
  }

  let parent = new Element("div", {
    id: "toolPopupMenuID",
    "data-menu-id": currentMenuId,
    styles: styles,
    events: {
      mouseleave: function (event) {
        // leaving the menu towards the node or just a little, the mouse move handler above closes the menu if necessary
        if (!isInNodeRow(event) && !isNearMenu(event)) _TM.hideToolsPopupMenu();
        else leftMenu = true;
      },
    },
  });

  document.body.appendChild(parent);
  parent.innerHTML =
    "<div class='toolMenuFrame'>" + this.getToolMenuHtml(node, args) + "</div>";
  this.decorateToolTargets(jq$(parent));
  // let plugins decorate the tools of the menu, e.g. with additional controls
  KNOWWE.helper.observer.notify("toolMenuShown", parent);

  const menu = jq$(parent).find(".markupMenu");
  let menuRect = menu[0].getBoundingClientRect();
  if (menuRect.top + menuRect.height > windowHeight) {
    menu.css("height", windowHeight - menuRect.top + "px");
  }
  if (menuRect.top < 0) {
    menu.css("height", menuRect.top + menuRect.height + "px");
  }
};

/**
 * @param {jQuery} node HTML element for which to display the tool menu (used for positioning)
 * @param {Object} [args={}] optional set of additional arguments for the KnowWE action that returns the KnowWE tools
 */
ToolMenu.prototype.getToolMenuHtml = function (node, args = {}) {
  let $node = jq$(node);

  let toolMenuIdentifier = $node.attr("toolMenuIdentifier");
  if (!toolMenuIdentifier) {
    toolMenuIdentifier = $node.data("identifier"); // some compatibility for saner attribute names
  }
  if (!this.cache[toolMenuIdentifier]) {
    let toolMenuAction = "GetToolMenuAction";
    let specialAction = $node.attr("toolMenuAction");
    if (!specialAction) {
      specialAction = $node.data("action");
    }
    if (specialAction) {
      toolMenuAction = specialAction;
    }

    let locationName = "default";
    if ($node.parents(".termbrowser").exists()) {
      locationName = "termbrowser";
    }

    const params = Object.assign(
      {
        action: toolMenuAction,
        identifier: toolMenuIdentifier,
        location: locationName,
      },
      args,
    );

    const options = {
      url: KNOWWE.core.util.getURL(params),
      async: false,
      response: {
        onError: _EC.onErrorBehavior,
      },
    };
    const ajaxCall = new _KA(options);
    ajaxCall.send();
    const parsedResponse = JSON.parse(ajaxCall.getResponse());
    this.cache[parsedResponse.sectionId] = parsedResponse.menuHTML;
    if (specialAction) {
      $node.removeAttr("toolMenuAction");
      $node.removeData("action");
      $node.attr("toolMenuIdentifier", parsedResponse.sectionId);
      toolMenuIdentifier = parsedResponse.sectionId;
    }
  }
  return this.cache[toolMenuIdentifier];
};

/**
 * Merged tools with several targets (see de.knowwe.tools.ToolTarget) have an arrow at their right side. Hovering or
 * clicking the arrow shows the list of the targets next to the tool, clicking a target executes the tool for it. The
 * list stays a DOM descendant of the tool (shown in the top layer as popover), so the surrounding menu stays open
 * while the mouse is over the list.
 *
 * @param {jQuery} $parent the element to decorate the tools in
 */
ToolMenu.prototype.decorateToolTargets = function($parent) {
  if (!$parent || $parent.length === 0) return;
  $parent.find(".tool-target-arrow").each(function() {
    const $arrow = jq$(this);
    if ($arrow.data("toolTargetsDecorated")) return;
    $arrow.data("toolTargetsDecorated", true);
    const $targets = $arrow.closest("a.markupMenuItem").siblings(".tool-targets").first();
    if ($targets.length === 0) return;

    // small delay before closing, so the mouse can move from the arrow into the list
    let closeTimeout = null;
    const cancelClose = function() {
      if (closeTimeout) clearTimeout(closeTimeout);
      closeTimeout = null;
    };
    const scheduleClose = function() {
      cancelClose();
      closeTimeout = setTimeout(() => _TM.closeToolTargets($targets), 200);
    };

    // while the list is open, it closes as soon as the mouse is no longer near the arrow or the list
    const watchMouse = function(event) {
      if (_TM.isMouseNear(event, [$arrow[0], $targets[0]])) cancelClose();
      else if (!closeTimeout) scheduleClose();
    };
    const open = function() {
      cancelClose();
      _TM.openToolTargets($arrow, $targets);
      jq$(document).off("mousemove.toolTargets").on("mousemove.toolTargets", watchMouse);
    };

    $arrow.on("mouseenter", open);
    // the arrow is inside the tool's link: don't execute the tool when clicking the arrow
    $arrow.on("click", function(event) {
      event.preventDefault();
      event.stopPropagation();
      open();
    });
    $targets.on("mouseenter", cancelClose);
    $targets.on("click", "a.tool-target", function() {
      cancelClose();
      _TM.closeToolTargets($targets);
    });
  });
};

ToolMenu.prototype.openToolTargets = function($arrow, $targets) {
  if ($targets.hasClass("open")) return;
  jq$(".tool-targets.open").each(function() {
    _TM.closeToolTargets(jq$(this));
  });

  const targets = $targets[0];
  $targets.addClass("open");
  $arrow.addClass("open");
  $targets.css({position: "fixed", top: -9999, left: -9999});
  if (typeof targets.showPopover === "function") {
    targets.showPopover();
  }

  // show the list below the arrow (like the editor selection of the XCL tools), so the mouse moves directly into it,
  // shifted to the left if there is not enough space, above the arrow if there is not enough space below
  const margin = 8;
  const arrowRect = $arrow[0].getBoundingClientRect();
  const viewportWidth = window.innerWidth;
  const viewportHeight = window.innerHeight;
  $targets.css({maxWidth: viewportWidth - 2 * margin, maxHeight: viewportHeight - 2 * margin});
  const width = $targets.outerWidth();
  const height = $targets.outerHeight();
  const left = Math.max(margin, Math.min(arrowRect.left, viewportWidth - width - margin));
  let top = arrowRect.bottom;
  if (top + height > viewportHeight - margin) {
    top = Math.max(margin, arrowRect.top - height);
  }
  $targets.css({left: left, top: top});
};

ToolMenu.prototype.closeToolTargets = function($targets) {
  const targets = $targets[0];
  if (!targets) return;
  if (typeof targets.hidePopover === "function" && targets.matches(":popover-open")) {
    targets.hidePopover();
  }
  $targets.removeClass("open");
  jq$(document).off("mousemove.toolTargets");
  $targets.siblings("a.markupMenuItem").find(".tool-target-arrow").removeClass("open");
};

ToolMenu.prototype.hideToolsPopupMenu = function() {
  this.lastMenuId = null;
  const old = jq$('#toolPopupMenuID')[0];
  if (old) {
    old.remove();
    jq$(document.body).unbind(ToolMenu.mouseMoveEvent)
  }
};

ToolMenu.prototype.animateDefaultMarkupMenu = function($parent) {
  if (!$parent) $parent = jq$('.defaultMarkupFrame');
  const $markups = $parent.is('.defaultMarkupFrame') ? $parent : $parent.find('.defaultMarkupFrame');
  $markups.each(function() {
    const markup = jq$(this);
    const header = markup.find('.headerMenu').first();
    const menu = markup.find('.markupMenu').first();
    if (menu.length === 0) {
      header.find('.markupMenuIndicator').hide();
    }

    header.off('mouseleave').on('mouseleave', function() {
      header.stop().animate({'max-width': 35, 'z-index': 1000, opacity: 0.3}, 200);
      if (menu) {
        menu.hide();
        menu.css("margin-top", "");
      }
    }).off('mouseenter').on('mouseenter', function() {
      header.stop().animate({'max-width': 250, 'z-index': 1500, opacity: 1}, 200);
      if (menu) {
        menu.show();
        let menuBottom = 0;
        if (menu.length > 0)
          menuBottom = menu[0].getBoundingClientRect().bottom;
        const windowHeight = jq$(window).height();
        let footerHeightVisible = 0;
        if (jq$('.footer').first()) {
          //Calculate user's scroll position from bottom
          const scrollPosition = window.pageYOffset;
          const windowSize = window.innerHeight;
          const bodyHeight = document.body.offsetHeight;
          const distToBottom = Math.max(bodyHeight - (scrollPosition + windowSize), 0);

          footerHeightVisible = Math.max(jq$('.footer').first().outerHeight() - distToBottom, 0);
        }
        if (menuBottom > (windowHeight - footerHeightVisible)) {
          menu.css("margin-top", "-" + (menuBottom - (windowHeight - footerHeightVisible)) + "px");
        }
        menu.stop().animate({opacity: 0.9}, 100);
      }
    });
  });
};

ToolMenu.prototype.adjustSingletonMenus = function($parent) {
  if (!$parent) return;
  const $markups = $parent.is('.defaultMarkupFrame') ? $parent : $parent.find('.defaultMarkupFrame');
  $markups.each(function() {
    const markup = jq$(this);
    const menu = markup.find('.markupMenu').first();
    const menuItems = menu.find('div.markupMenuItem');
    if (menuItems.length !== 1) return;
    const header = markup.find('.markupHeader').first();
    const menuItem = menuItems.first();
    let anchor = menuItem.find('a');
    if (anchor.length === 1) {
      const headerFrame = header.closest('.markupHeaderFrame');
      headerFrame.css("right", "0");
      const padding = header.css("padding");
      header.css("padding", "0");
      menuItem.css("padding", "0");
      anchor.first().css("margin", "0");
      anchor.first().css("padding", padding);
      anchor.first().css("border-radius", headerFrame.css("border-radius"));
    }
    header.html(menuItem);
    menu.remove();
  });
};

const _TM = new ToolMenu();

jq$(function() {
  _TM.animateDefaultMarkupMenu();
  _TM.decorateToolMenus();
  _TM.decorateToolTargets(jq$(document.body));
});

KNOWWE.helper.observer.subscribe("flowchartrendered", function() {
  _TM.decorateToolMenus(jq$('.Flowchart'))
});

KNOWWE.helper.observer.subscribe("afterRerender", function() {
  _TM.animateDefaultMarkupMenu(jq$(this));
  _TM.decorateToolMenus(jq$(this));
  _TM.decorateToolTargets(jq$(this));
});

