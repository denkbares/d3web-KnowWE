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

ToolMenu.prototype.decorateToolMenus = function ($parent) {
	if (!$parent) $parent = jq$('.toolMenuParent');
	var decorators = $parent.find('.toolsMenuDecorator');
	decorators.each(function () {
		var a = jq$(this);
		if (a.data('toolMenuDecorated') === 'true') return;
		a.parent().mouseenter(function () {
			a.css('visibility', 'visible');
		});
		a.parent().mouseleave(function () {
			a.css('visibility', 'hidden');
		});
		a.click(function () {
			_TM.showToolPopupMenu(a);
		});
		a.dblclick(function () {
			_TM.hideToolsPopupMenu();
			_TM.selectTerm(this);
		});
		a.data('toolMenuDecorated', 'true');
		//prevent "click through" in composite edit
		a.click(function (e) {
			e.stopPropagation();
		});
	});
};

ToolMenu.prototype.selectTerm = function (element) {
	element = jq$(element).parent().find('.clickable-term')[0];
	var selection = window.getSelection();
	var range = document.createRange();
	range.selectNodeContents(element);
	selection.removeAllRanges();
	selection.addRange(range);
};

ToolMenu.prototype.showToolPopupMenu = function ($node) {
	// we hide all open tooltipster tool tips to reduce clutter
	jq$('.tooltipstered').tooltipster('hide');

	// hide tool menus opened for other terms
	var lastMenuId = this.lastMenuId;
	this.hideToolsPopupMenu();
	var currentMenuId = jq$($node).attr('id');
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
	if (nodeRect.top > windowHeight - nodeRect.top + nodeRect.height) {
		// show above
		styles.bottom = windowHeight - nodeRect.top - 1 + 'px';
		$node.unbind('mouseleave').mouseleave(function (event) {
			if (event.clientY >= nodeRect.top + 1) _TM.hideToolsPopupMenu();
			jq$(this).unbind('mouseleave');
		});
	}
	else {
		// show below
		styles.top = nodeRect.top + nodeRect.height + 'px';
		$node.unbind('mouseleave').mouseleave(function (event) {
			if (event.clientY <= nodeRect.bottom - 1) _TM.hideToolsPopupMenu();
			jq$(this).unbind('mouseleave');
		});
	}
	if (nodeRect.left > windowWidth - nodeRect.left + nodeRect.width) {
		// expand to the left
		styles.right = Math.max(0, windowWidth - nodeRect.left - nodeRect.width) + 'px';
	}
	else {
		// expand to the right
		styles.left = Math.max(0, nodeRect.left) + 'px';
	}

	let parent = new Element('div', {
		'id': 'toolPopupMenuID',
		'styles': styles,
		'events': {
			'mouseleave': _TM.hideToolsPopupMenu
		}
	});

	document.body.appendChild(parent);
	parent.innerHTML = "<div class='toolMenuFrame'>" + this.getToolMenuHtml(node) + "</div>";

	var menu = jq$(parent).find(".markupMenu");
	let menuRect = menu[0].getBoundingClientRect();
	if (menuRect.top + menuRect.height > windowHeight) {
		menu.css("height", windowHeight - menuRect.top + "px");
	}
	if (menuRect.top < 0) {
		menu.css("height", menuRect.top + menuRect.height + "px");
	}
}
;

ToolMenu.prototype.getToolMenuHtml = function (node) {


	var toolMenuIdentifier = jq$(node).attr('toolMenuIdentifier');
	if (!this.cache[toolMenuIdentifier]) {
		var toolMenuAction = 'GetToolMenuAction';
		var specialAction = jq$(node).attr('toolMenuAction');
		if (specialAction) {
			toolMenuAction = specialAction;
		}

		var locationName = "default";
		if (jq$(node).parents(".termbrowser").exists()) {
			locationName = "termbrowser";
		}

		var params = {
			action: toolMenuAction,
			identifier: toolMenuIdentifier,
			location: locationName
		};

		var options = {
			url: KNOWWE.core.util.getURL(params),
			async: false,
			response: {
				onError: _EC.onErrorBehavior
			}
		};
		var ajaxCall = new _KA(options);
		ajaxCall.send();
		var parsedResponse = JSON.parse(ajaxCall.getResponse());
		this.cache[parsedResponse.sectionId] = parsedResponse.menuHTML;
		if (specialAction) {
			jq$(node).removeAttr('toolMenuAction');
			jq$(node).attr('toolMenuIdentifier', parsedResponse.sectionId);
			toolMenuIdentifier = parsedResponse.sectionId;
		}
	}
	return this.cache[toolMenuIdentifier];
};

ToolMenu.prototype.hideToolsPopupMenu = function () {
	this.lastMenuId = null;
	var old = jq$('#toolPopupMenuID')[0];
	if (old) {
		old.remove();
	}
};

ToolMenu.prototype.animateDefaultMarkupMenu = function ($parent) {
	if (!$parent) $parent = jq$('.defaultMarkupFrame');
	var $markups = $parent.is('.defaultMarkupFrame') ? $parent : $parent.find('.defaultMarkupFrame');
	$markups.each(function () {
		var markup = jq$(this);
		var header = markup.find('.headerMenu').first();
		var menu = markup.find('.markupMenu').first();
		if (menu.length === 0) {
			header.find('.markupMenuIndicator').hide();
		}

		header.unbind('mouseout').on('mouseout', function () {
			header.stop().animate({'max-width': 35, 'z-index': 1000, opacity: 0.3}, 200);
			if (menu) {
				menu.hide();
				menu.css("margin-top", "");
			}
		}).unbind('mouseover').on('mouseover', function () {
			header.stop().animate({'max-width': 250, 'z-index': 1500, opacity: 1}, 200);
			if (menu) {
				menu.show();
				var menuBottom = menu[0].getBoundingClientRect().bottom;
				var windowHeight = jq$(window).height();
				var footerHeightVisible = 0;
				if (jq$('.footer').first()) {
					//Calculate user's scroll position from bottom
					var scrollPosition = window.pageYOffset;
					var windowSize = window.innerHeight;
					var bodyHeight = document.body.offsetHeight;
					var distToBottom = Math.max(bodyHeight - (scrollPosition + windowSize), 0);

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

var _TM = new ToolMenu();

jq$(window).ready(function () {
	_TM.animateDefaultMarkupMenu();
	_TM.decorateToolMenus();
});

KNOWWE.helper.observer.subscribe("flowchartrendered", function () {
	_TM.decorateToolMenus(jq$('.Flowchart'))
});

KNOWWE.helper.observer.subscribe("afterRerender", function () {
	_TM.animateDefaultMarkupMenu(jq$(this));
	_TM.decorateToolMenus(jq$(this));
});

