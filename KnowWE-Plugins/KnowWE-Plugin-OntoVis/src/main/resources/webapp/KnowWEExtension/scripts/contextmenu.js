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

KNOWWE.plugin.visualization = {

	addClickEventsToGraph : function(objectTag) {
		// get the corresponding section ID of this svg
		var sectionID = jq$(objectTag).parent().parent().parent().parent().attr('id');

		// get the parent div that has the overflow:scroll attribute (necessary for
		// finding the correct click position and for hiding the menu once this
		// div is scrolled)
		var scrollParent = jq$(objectTag).parent();
		scrollParent.scroll(function() {
			KNOWWE.plugin.visualization.closeContextMenu();
		});

		// get the SVG document inside the object tag and then find all g-nodes in it
		var svgDoc = objectTag.contentDocument;

		var nodes = jq$("g[id*='node']", svgDoc);

		nodes.css('cursor', 'pointer');
		nodes.off().on('click', function(e) {
			var conceptName = this.children[0].innerHTML;

			// menu deactivated for now, just go to definition...
			//KNOWWE.plugin.visualization.goToDef(sectionID, conceptName);

			var clickPosition = KNOWWE.plugin.visualization.getClickPosition(e, sectionID, scrollParent);

			KNOWWE.plugin.visualization.fetchToolMenuHTML(clickPosition.x, clickPosition.y, conceptName, sectionID)
		});
	},

	fetchToolMenuHTML : function(mouseX, mouseY, term, sectionID) {
		var params = {
			action : 'GetVisMenuAction',
			term : term,
			sectionID : sectionID
		};
		var options = {
			url : KNOWWE.core.util.getURL(params),
			response : {
				fn : function() {
					// insert new browser data
					var htmlCode = this.response;

					var menudiv = jq$("#vismenu");

					if (!menudiv.exists()) {
						menudiv = jq$('<div/>', {
							id : 'vismenu'
						});
						jq$("#content").append(menudiv);
					}
					menudiv.empty();
					menudiv.append(htmlCode);

					// show menu at the mouse position
					menudiv.css({top : mouseY, left : mouseX, position : 'absolute', visibility : 'invisible'});

					_TM.decorateToolMenus(menudiv);
					var decoratorSpan = jq$(menudiv).find(".toolsMenuDecorator");
					_TM.showToolPopupMenu(decoratorSpan);

					jq$(document).click(function(e) {
						var clickedElement = jq$(e.target);
						var toolPopupMenu = jq$("#toolPopupMenuID");
						toolPopupMenu.css({visibility : 'hidden'});
					});

				}
			}
		};

		new _KA(options).send();
	},

	reRenderGraph : function(newID, sectionID) {
		var params = {
			action : 'ReRenderContentPartAction',
			KdomNodeId : newID
		};

		var options = {
			url : KNOWWE.core.util.getURL(params),

			response : {
				fn : function() {
					if (this.response.toLowerCase().startsWith("not found")) {
						// if section can not be rerendered correctly for instance
						// because multiple sections have changed/been created
						location.reload();
					} else {
						// find old code block and replace with new one
						var jsonObject = jq$.parseJSON(this.response);
						var replaceContent = jsonObject['html'];

						var markupBlockOld = jq$('#' + sectionID);
						replaceContent = KNOWWE.plugin.visualization.cleanStringFromTrailingLinebreaks(replaceContent);
						markupBlockOld.replaceWith(replaceContent);
					}
				}
			}
		};
		new _KA(options).send();
	},

	cleanStringFromTrailingLinebreaks : function(str) {
		while (str.lastIndexOf('\r\n') == str.length - 2) {
			str = str.substring(0, str.length - 2);
		}
		return str;
	},

	closeContextMenu : function() {
		jq$('#vismenu').css({visibility : 'hidden'});
	},

	// Calculates the absolute click position of a click inside the svg-element (which
	// requires the click-event for the relative click position, the parent of the svg
	// with the overflow:scroll-attribute to eliminate scroll offsets if present and
	// it requires the recursive calculation of the absolute svg-position within the
	// whole document)
	getClickPosition : function(e, id, scrollParent) {
		// first we have to get the position of the click within the svg-element (e.page can
		// only return the relative position in this case - because of the object tag)
		var mouseX, mouseY;
		var is_firefox = navigator.userAgent.indexOf('Firefox') > -1;
		if (is_firefox) {
			mouseX = e.pageX;
			mouseY = e.pageY;
		} else {
			mouseX = e.pageX + document.body.scrollLeft + document.documentElement.scrollLeft;
			mouseY = e.pageY + document.body.scrollTop + document.documentElement.scrollTop;
		}
		// substract the possible overflow scroll of the parent
		mouseX -= scrollParent.scrollLeft();
		mouseY -= scrollParent.scrollTop();

		// we also have to get the position of the div the svg is in so we can add it
		// to the relative click position within the svg
		var svg = jq$("#content_" + id);
		var svgPos = KNOWWE.plugin.visualization.getPosition(svg[0]);

		// those two positions have to be added up to get the final result
		mouseX += svgPos.x;
		mouseY += svgPos.y;
		return { x : mouseX, y : mouseY };
	},

	getPosition : function(element) {
		var xPosition = 0;
		var yPosition = 0;

		while (element) {
			xPosition += (element.offsetLeft - element.scrollLeft + element.clientLeft);
			yPosition += (element.offsetTop - element.scrollTop + element.clientTop);
			element = element.offsetParent;
		}
		return { x : xPosition, y : yPosition };
	}
};