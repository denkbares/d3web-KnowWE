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

KNOWWE = typeof KNOWWE === "undefined" ? {} : KNOWWE;
KNOWWE.plugin = KNOWWE.plugin || {};

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

			KNOWWE.plugin.visualization.fetchToolMenuHTML(e, conceptName, sectionID)
		});
	},

	fetchToolMenuHTML : function(event, term, sectionID) {
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
						jq$("#" + sectionID).append(menudiv);
					}
					menudiv.empty();
					menudiv.append(htmlCode);

					// show menu at the mouse position
					let rect = event.target.getBoundingClientRect();
					menudiv.css({top : rect.bottom, left : rect.left, position : 'absolute', visibility : 'invisible'});

					_TM.decorateToolMenus(menudiv);
					_TM.showToolPopupMenu(jq$(menudiv).find(".toolsMenuDecorator"));

					jq$(document).click(function(e) {
						jq$("#toolPopupMenuID").css({visibility : 'hidden'});
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
		while (str.lastIndexOf('\r\n') === str.length - 2) {
			str = str.substring(0, str.length - 2);
		}
		return str;
	},

	closeContextMenu : function() {
		jq$('#vismenu').css({visibility : 'hidden'});
	},

};
