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

ToolMenu.prototype.showToolPopupMenu = function($node) {
  // we hide all open tooltipster tool tips to reduce clutter
  jq$('.tooltipstered').tooltipster('hide');

  // hide tool menus opened for other terms
  const lastMenuId = this.lastMenuId;
  this.hideToolsPopupMenu();
  const currentMenuId = jq$($node).attr('id') || jq$($node).data('identifier');
  if (lastMenuId === currentMenuId) return;
  this.lastMenuId = currentMenuId;

  let node = $node[0];
  let windowHeight = window.getHeight();
  let windowWidth = window.getWidth();
  let nodeRect = node.getBoundingClientRect();
  let styles = {
    'z-index': '10000',
    'position': 'fixed'
  };
  const mouseMovePadding = 5;
  if (nodeRect.top > windowHeight - nodeRect.top + nodeRect.height) {
    // show above
    styles.bottom = windowHeight - nodeRect.top - 1 + 'px';
    jq$(document.body).unbind(ToolMenu.mouseMoveEvent).bind(ToolMenu.mouseMoveEvent, function(event) {
      if (event.clientY > nodeRect.bottom
        || (event.clientY > nodeRect.top && (event.clientX > nodeRect.right + mouseMovePadding || event.clientX < nodeRect.left - mouseMovePadding))) {
        _TM.hideToolsPopupMenu();
        jq$(this).unbind(ToolMenu.mouseMoveEvent);
      }
    });
  } else {
    // show below
    styles.top = nodeRect.top + nodeRect.height + 'px';
    jq$(document.body).unbind(ToolMenu.mouseMoveEvent).bind(ToolMenu.mouseMoveEvent, function(event) {
      if (event.clientY < nodeRect.top ||
        (event.clientY < nodeRect.bottom && (event.clientX > nodeRect.right + mouseMovePadding || event.clientX < nodeRect.left - mouseMovePadding))) {
        _TM.hideToolsPopupMenu();
        jq$(this).unbind(ToolMenu.mouseMoveEvent);
      }
    });
  }
  if (nodeRect.left > windowWidth - nodeRect.left + nodeRect.width) {
    // expand to the left
    styles.right = Math.max(0, windowWidth - nodeRect.left - nodeRect.width) + 'px';
  } else {
    // expand to the right
    styles.left = Math.max(0, nodeRect.left) + 'px';
  }

  let parent = new Element('div', {
    'id': 'toolPopupMenuID',
    'data-menu-id': currentMenuId,
    'styles': styles,
    'events': {
      'mouseleave': _TM.hideToolsPopupMenu
    }
  });

  document.body.appendChild(parent);
  parent.innerHTML = "<div class='toolMenuFrame'>" + this.getToolMenuHtml(node) + "</div>";

  const menu = jq$(parent).find(".markupMenu");
  let menuRect = menu[0].getBoundingClientRect();
  if (menuRect.top + menuRect.height > windowHeight) {
    menu.css("height", windowHeight - menuRect.top + "px");
  }
  if (menuRect.top < 0) {
    menu.css("height", menuRect.top + menuRect.height + "px");
  }
};

ToolMenu.prototype.getToolMenuHtml = function(node) {
  let $node = jq$(node);

  let toolMenuIdentifier = $node.attr('toolMenuIdentifier');
  if (!toolMenuIdentifier) {
    toolMenuIdentifier = $node.data('identifier'); // some compatibility for saner attribute names
  }
  if (!this.cache[toolMenuIdentifier]) {
    let toolMenuAction = 'GetToolMenuAction';
    let specialAction = $node.attr('toolMenuAction');
    if (!specialAction) {
      specialAction = $node.data('action');
    }
    if (specialAction) {
      toolMenuAction = specialAction;
    }

    let locationName = "default";
    if ($node.parents(".termbrowser").exists()) {
      locationName = "termbrowser";
    }

    const params = {
      action: toolMenuAction,
      identifier: toolMenuIdentifier,
      location: locationName,
    };

    const options = {
      url: KNOWWE.core.util.getURL(params),
      async: false,
      response: {
        onError: _EC.onErrorBehavior
      }
    };
    const ajaxCall = new _KA(options);
    ajaxCall.send();
    const parsedResponse = JSON.parse(ajaxCall.getResponse());
    this.cache[parsedResponse.sectionId] = parsedResponse.menuHTML;
    if (specialAction) {
      $node.removeAttr('toolMenuAction');
      $node.removeData('action');
      $node.attr('toolMenuIdentifier', parsedResponse.sectionId);
      toolMenuIdentifier = parsedResponse.sectionId;
    }
  }
  return this.cache[toolMenuIdentifier];
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

    header.unbind('mouseout').on('mouseout', function() {
      header.stop().animate({'max-width': 35, 'z-index': 1000, opacity: 0.3}, 200);
      if (menu) {
        menu.hide();
        menu.css("margin-top", "");
      }
    }).unbind('mouseover').on('mouseover', function() {
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

jq$(window).ready(function() {
  _TM.animateDefaultMarkupMenu();
  _TM.decorateToolMenus();
});

KNOWWE.helper.observer.subscribe("flowchartrendered", function() {
  _TM.decorateToolMenus(jq$('.Flowchart'))
});

KNOWWE.helper.observer.subscribe("afterRerender", function() {
  _TM.animateDefaultMarkupMenu(jq$(this));
  _TM.decorateToolMenus(jq$(this));
});

