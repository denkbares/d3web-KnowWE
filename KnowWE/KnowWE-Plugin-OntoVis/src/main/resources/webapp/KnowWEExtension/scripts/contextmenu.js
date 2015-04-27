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

	addHTMLMenu : function() {
		// if the menu hasn't been created yet create it!
		if (jq$("#vismenu").length <= 0) {
			var menudiv = jq$('<div/>', {id : 'vismenu'});

			var literalsdiv = jq$('<div/>', {id: 'literals'})
				.appendTo(menudiv)
				.append(jq$('<span/>', {id: 'literalHeading', text:'Literals'}))
				.append(jq$('<table/>', {id: 'literalsTable'}));

			var menulist = jq$('<ul/>', {class : 'menuitems'})
				.appendTo(menudiv)
				.append(jq$('<li/>', {id : 'expand', text : 'Expand Concept'}))
				.append(jq$('<li/>', {id : 'exclude', text : 'Exclude Concept'}))
				.append(jq$('<li/>', {id : 'gotodef', text : 'Go to Definition'}))
				.append(jq$('<li/>', {id : 'newvis', text : 'New Visualization'}));

			jq$("#content").append(menudiv);
		}
	},

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
			var clickPosition = KNOWWE.plugin.visualization.getClickPosition(e, sectionID, scrollParent);

			KNOWWE.plugin.visualization.fillAndShowMenu(clickPosition.x, clickPosition.y, conceptName, sectionID)
		});
	},

	fillAndShowMenu : function(mouseX, mouseY, conceptName, sectionID) {
		// add the literals for the given concept to the menu
		KNOWWE.plugin.visualization.addLiteralsToMenu(conceptName, sectionID);

		// add click-functionality to the menu
		jq$("#vismenu #expand").off('click').on('click', function(e) {
			var params = {
				action : 'ExpandCurrentConceptAction',
				kdomid : sectionID,
				concept : conceptName
			};

			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					fn : function() {
						location.reload();
						//var newID =  this.response.trim();
						//KNOWWE.plugin.visualization.reRenderGraph(newID, sectionID);
					}
				}
			};
			new _KA(options).send();
		});

		jq$("#vismenu #exclude").off('click').on('click', function(e) {
			var params = {
				action : 'ExcludeCurrentConceptAction',
				kdomid : sectionID,
				concept : conceptName
			};

			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					fn : function() {
						location.reload();
						//var newID =  this.response.trim();
						//KNOWWE.plugin.visualization.reRenderGraph(newID, sectionID);
					}
				}
			};
			new _KA(options).send();
		});

		jq$("#vismenu #gotodef").off().on('click', function(e) {
			var params = {
				action : 'GoToDefinitionAction',
				kdomid : sectionID,
				concept : conceptName
			};

			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					fn : function() {
						var url = this.response.trim();
						window.location.href = url;
					}
				}
			};
			new _KA(options).send();
		});

		jq$("#vismenu #newvis").off().on('click', function(e) {
			var params = {
				action : 'MakeNewVisualizationOfConceptAction',
				kdomid : sectionID,
				concept : conceptName
			};

			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					fn : function() {
						location.reload();

						//var newID =  this.response.trim();
						//KNOWWE.plugin.visualization.reRenderGraph(newID, sectionID);
					}
				}
			};
			new _KA(options).send();
		});

		// show menu at the mouse position
		jq$('#vismenu').css({top : mouseY, left : mouseX, position : 'absolute', visibility : 'visible'});
	},

	addLiteralsToMenu : function(conceptName, sectionID) {
		var params = {
			action : 'FindLiteralsForConceptAction',
			kdomid : sectionID,
			concept : conceptName
		};

		var options = {
			url : KNOWWE.core.util.getURL(params),
			response : {
				fn : function() {
					var jsonLiteralsArray = jq$.parseJSON(this.response);

					if (jsonLiteralsArray.length > 0) {
						// show literals-part of the menu
						jq$('#vismenu #literals').css({display : 'block'});

						// change heading
						jq$('#literalHeading').text(conceptName);

						// add each literal to the table (after emptying it)
						var table = jq$('#vismenu #literalsTable').empty();

						for (var i = 0; i < jsonLiteralsArray.length; i++) {
							var jsonLiteralArray = jq$.makeArray(jsonLiteralsArray[i]);
							if (jsonLiteralArray.length != 2) continue;

							// create a new table row for the literal
							var literalsdiv = jq$('<tr/>')
								.appendTo(table)
								.append(jq$('<td/>', {text: jsonLiteralArray[0]}))
								.append(jq$('<td/>', {text: jsonLiteralArray[1]}));
						}
					} else {
						// hide literals-part of the menu
						jq$('#vismenu #literals').css({display : 'none'});
					}
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

// close open menus by clicking somewhere outside of them
jq$(document).click(function(e) {
	var clickedElement = jq$(e.target);
	if (!clickedElement.is('#vismenu')) {
		KNOWWE.plugin.visualization.closeContextMenu();
	}
});

KNOWWE.helper.observer.subscribe("afterRerender", function() {
	// add the menu-div to the page
	KNOWWE.plugin.visualization.addHTMLMenu();
});