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
if (typeof KNOWWE.core == "undefined" || !KNOWWE.core) {
	KNOWWE.core = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.core.plugin == "undefined" || !KNOWWE.core.plugin) {
	KNOWWE.core.plugin = {};
}

/**
 * Namespace: KNOWWE.core.plugin.objectinfo The KNOWWE object info namespace.
 */
KNOWWE.core.plugin.objectinfo = function() {
	return {

		init : function() {
			// init renaming form button
			jq$('#objectinfo-replace-button').click(
				KNOWWE.core.plugin.objectinfo.renameFunction);

			// we have to suspend the enter event to prevent multiple
			// confirm dialogs after when confirming the dialogs with enter...
			var suspend = false;
			jq$('#objectinfo-replacement').keyup(function(event) {
				if (event.keyCode == 13 && !suspend) {
					suspend = true;
					if (confirm("Are you sure you want to rename this term?")) {
						KNOWWE.core.plugin.objectinfo.renameFunction();
					} else {
						suspend = false;
					}
				}
			});

			KNOWWE.core.plugin.objectinfo.lookUp();
		},

		/**
		 * Load the ajax-previews
		 */
		loadPreviews : function(root) {
			var select = (root == undefined)
				? jq$('.asynchronPreviewRenderer')
				: jq$(root).find('.asynchronPreviewRenderer');
			var json = [];
			var ids = [];
			select.each(function() {
				json.push(this.getAttribute('rel'));
				ids.push(this.id);
			});
			jq$.ajax("action/RenderPreviewAction", {
				data : {
					data : JSON.stringify(json)
				},
				success : function(html) {
					KNOWWE.core.util.replaceElement(ids, html);
					if (jq$(root).parents('#compositeEdit').length) {
						_CE.afterPreviewsLoad(root);
						KNOWWE.core.actions.init();
					}
					_TM.decorateToolMenus(root);

				}
			});
		},

		/**
		 * Function: createHomePage Used in the ObjectInfoToolProvider for
		 * creating homepages for KnowWEObjects
		 */
		createHomePage : function() {
			objectName = _KS('#objectinfo-src');
			if (objectName) {
				var params = {
					action : 'CreateObjectHomePageAction',
					objectname : objectName.innerHTML
				}

				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'none',
						fn : function() {
							window.location = "Wiki.jsp?page="
								+ objectName.innerHTML
						}
					}
				}
				new _KA(options).send();
			}

		},

		renameFunction : function() {
			KNOWWE.core.plugin.objectinfo.renameTerm(false);
		},

		/**
		 * Renames all occurrences of a specific term.
		 */
		renameTerm : function(forceRename) {
			if (forceRename == null)
				forceRename = false;
			// TODO shouldn't these 3 be vars?
			objectname = jq$('#objectinfo-target');
			replacement = jq$('#objectinfo-replacement');
			web = jq$('#objectinfo-web');
			if (objectname && replacement && web) {
				var changeNote = 'Renaming: "' + objectname.val() + '" -> "'
					+ replacement.val() + '"';
				var params = {
					action : jq$(replacement).attr('action'),
					termname : objectname.val(),
					termreplacement : replacement.val(),
					KWikiWeb : web.val(),
					KWikiChangeNote : changeNote,
					force : forceRename ? "true" : "false"
				}
				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'none',
						fn : function() {
							var jsonResponse = JSON.parse(this.responseText);
							var alreadyexists = jsonResponse.alreadyexists;
							var same = jsonResponse.same;
							if (same == 'true') {
								alert('The term has not changed.');
							} else {
								if (alreadyexists == 'true') {
									if (confirm('A term with this name already exists, are you sure you want to merge both terms?')) {
										KNOWWE.core.plugin.objectinfo
											.renameTerm(true);
									}
								} else {
									window.location.href = "Wiki.jsp?page=ObjectInfoPage&objectname="
										+ encodeURIComponent(jsonResponse.newObjectName)
										+ "&termIdentifier="
										+ encodeURIComponent(jsonResponse.newTermIdentifier);
								}
							}
							KNOWWE.core.util.updateProcessingState(-1);
						},
						onError : function() {
							KNOWWE.core.util.updateProcessingState(-1);
						}
					}
				}
				KNOWWE.core.util.updateProcessingState(1);
				new _KA(options).send();
			}

		},

		/**
		 * shows a list of similar terms
		 */
		lookUp : function() {
			var terms = jq$('#objectinfo-terms')
			if (terms.length == 0)
				return;
			var response = terms.text();
			var jsonResponse = JSON.parse(response);
			var a = jsonResponse.allTerms;
			jq$('#objectinfo-search').autocomplete({
				source : a
			});
			jq$('#objectinfo-search').on(
				"autocompleteselect",
				function(event, ui) {
					KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(ui.item.value);
				});

			//Open "Show Info" on Enter key press only if term exists - otherwise do nothing
			jq$('#objectinfo-search').keyup(function(e) {
				if (e.keyCode == 13) {
					var val = jq$('#objectinfo-search').val();
					if (jq$.inArray(val, a) != -1) {
						KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(val);
					}

				}
			});
		}
	}
}();

KNOWWE.plugin.renaming = function() {

	var sectionsCache = new Object();

	var otherOccurencesHashMap = new Object();

	var viewRoot = new Object();

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
				action : "TermRenamingAction",
				termname : oldValue,
				termreplacement : replacement,
				force : forceRename ? "true" : "false"
			}
			KNOWWE.core.util.updateProcessingState(1);
			var request = jq$.ajax({
				type : "post", url : KNOWWE.core.util.getURL(params),
				success : function(data, text) {

					var jsonResponse = JSON.parse(data);
					var alreadyexists = jsonResponse.alreadyexists;
					var same = jsonResponse.same;
					if (same == 'true') {
						alert('The term has not changed.');
					} else {
						if (alreadyexists == 'true') {
							if (confirm('A term with this name already exists, are you sure you want to merge both terms?')) {
								renameTerms(oldValue, replacement, true);
							}
						}
						else {
							if (jsonResponse.objectinfopage === true) {
								window.location.href = "Wiki.jsp?page=ObjectInfoPage&objectname="
									+ encodeURIComponent(jsonResponse.newObjectName)
									+ "&termIdentifier="
									+ encodeURIComponent(jsonResponse.newTermIdentifier);
							}
							else {
								KNOWWE.core.util.reloadPage();
							}
						}
					}


				},

				error : function(request, status, error) {
					KNOWWE.core.util.updateProcessingState(-1);
					console.log(status, error);
					KNOWWE.core.util.reloadPage();
				}
			});
			//KNOWWE.core.util.updateProcessingState(1);
		}

	}

	function getOldTermIdentifierAndMatchingSections(sectionId, callback) {
		var params = {
			action : "GetInfosForInlineTermRenamingAction",
			SectionID : sectionId
		}
		var options = {
			url : KNOWWE.core.util.getURL(params),
			response : {
				action : 'none',
				fn : function() {
					var jsonResponse = JSON.parse(this.responseText);
					callback(jsonResponse);


				},
				onError : function() {
					KNOWWE.core.util.updateProcessingState(-1);
				}

			}

		}
		new _KA(options).send();
		KNOWWE.core.util.updateProcessingState(1);
		KNOWWE.core.util.updateProcessingIndicator();
	}

	function restoreOriginal(original) {
		jq$(original).removeClass("click");
		jq$(original).unbind();
		jq$(original).css("min-width", "");
		jq$(original).css("padding-right", "");
		jq$(original).css("width", "");
	}

	function cancelEdit(settings, original) {
		restoreOriginal(original);

		for (var occurence in otherOccurencesHashMap) {
			var section = sectionsCache[occurence];
			jq$(otherOccurencesHashMap[occurence]).replaceWith(section);
		}

	}

	function afterCancelEdit(setting, original) {
		_TM.decorateToolMenus(original);
	}

	function showCurrentEditOnOtherOccurences(text) {
		for (var occurence in otherOccurencesHashMap) {
			jq$(otherOccurencesHashMap[occurence]).first().text(text);
		}
	}

	function saveOriginalsAndPrepareForEdit(lastPathElement) {
		for (var occurence in otherOccurencesHashMap) {
			sectionsCache[occurence] = otherOccurencesHashMap[occurence].clone();
			jq$(otherOccurencesHashMap[occurence]).attr("sectionOccurenceId", occurence);
			jq$(otherOccurencesHashMap[occurence]).empty();
			jq$(otherOccurencesHashMap[occurence]).css("background-color", "yellow");
			jq$(otherOccurencesHashMap[occurence]).text(lastPathElement);
		}
		KNOWWE.core.util.updateProcessingState(-1);
	}

	function setViewRoot() {

		if (jq$("#compositeEdit").length > 0) {
			viewRoot = "#compositeEdit ";
		}
		else {
			viewRoot = "#pagecontent ";
		}

	}

	function initializeOtherOccurencesHashMap(sectionsIds) {
		for (var i = 0; i < sectionIds.length; i++) {
			var sectionId = sectionIds[i];
			var toolMenuIdentifier = jq$(viewRoot + ".defaultMarkupFrame span[toolmenuidentifier=" + sectionId + "]");
			if (toolMenuIdentifier.length > 0) {
				var toolMenuDecorated = toolMenuIdentifier[0].parentNode;
				otherOccurencesHashMap[sectionId] = toolMenuDecorated;
			}
		}
	}

	return {
		renameTerm : function(toolMenuIdentifier) {
			setViewRoot();
			var callback = function(jsonResponse) {
				var clickedTerm = jq$(viewRoot + "[toolmenuidentifier=" + toolMenuIdentifier + "]")[0].parentNode;

				//get edit field
				jq$(clickedTerm).addClass("click");
				//jq$(clickedTerm).css("display", "none");
				jq$(".click").editable(function(value, settings) {
					renameTerms(jsonResponse.termIdentifier, value, false);
					return(value)
				}, {
					style : "inherit",
					onreset : cancelEdit,
					afterreset : afterCancelEdit

				});
				jq$('.click').trigger("click");
				//replace edit field value with sectionText for encoding reasons
				var inputField = jq$(clickedTerm).find("input").val(jsonResponse.lastPathElement);
				jq$(inputField).autoGrow(5);


				sectionIds = jsonResponse.sectionIds;

				initializeOtherOccurencesHashMap(sectionIds);

				saveOriginalsAndPrepareForEdit(jsonResponse.lastPathElement);

				jq$(".click input").keyup(function() {
					showCurrentEditOnOtherOccurences(jq$(this).val());
				});
			}
			getOldTermIdentifierAndMatchingSections(toolMenuIdentifier, callback);

		}

	}

}();

KNOWWE.core.plugin.renderKDOM = function() {

	jq$('.table_text').hover(function() {
			var that = this;
			setTimeout(function() {
				jq$(that).css('height', that.scrollHeight);
			}, 0);
			// alert(this.scrollHeight);
		}, function() {
			jq$(this).css('height', '18px');
		}
	);
};

KNOWWE.core.plugin.setMarkupSectionActivationStatus = function(id, status) {
	var params = {
		action : 'SetMarkupActivationStatus',
		SectionID : id,
		status : status
	}

	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
			action : 'none',
			fn : function() {
				window.location.reload();
			}
		}
	}
	new _KA(options).send();
}

KNOWWE.tooltips = {};

KNOWWE.tooltips.enrich = function(element) {
	// first, we filter nested tooltiped objects
	// (e.g. a span with title contains another span with title)
	// this way, tooltipser behaves with nested tooltips the same way
	// normal tooltips behave: The most inner tooltips is shown exclusively
	if (element) {
		element = jq$(element);
	} else {
		element = jq$(document);
	}
	element.find('.tooltipster').each(function() {
		var anscestor = jq$(this).parents('.tooltipster');
		if (anscestor.exists()) {
			anscestor.removeAttr('title');
			anscestor.removeClass('tooltipster');
		}
	});
	element.find('.tooltipster').each(function() {
		var delay = jq$(this).attr('delay');
		if (!delay) delay = 1300;
		jq$(this).tooltipster({
			position : "top-left",
			interactive : true,
			multiple : true,
			delay : delay,
			contentAsHTML: true,
			theme : ".tooltipster-knowwe",
			functionBefore : function(origin, continueTooltip) {
				// check if we have an ajax-tooltip
				// and only do once for each tooltip
				var src = origin.data('tooltip-src');
				if (!src) {
					continueTooltip();
					return;
				}
				origin.data('tooltip-src', null);
				// show ajax-spinner until request is arriving
				origin.tooltipster('update', '<span class="ajaxLoader">loading tooltip...</span>');
				continueTooltip();
				// request new content
				jq$.ajax(src, {
					success : function(json) {
						var html = json;
						var obj = jq$.parseJSON(json);
						if (jq$.isArray(obj)) html = obj[0];
						origin.tooltipster('update', html).tooltipster('reposition');
					},
					error : function(request, status, error) {
						KNOWWE.notification.error("Cannot get tooltip content", error, src);
						origin.tooltipster('hide');
					}
				});
			}
		})
	});
};

KNOWWE.kdomtreetable = {};

KNOWWE.kdomtreetable.init = function() {
	jq$('.renderKDOMTable').each(function() {
		jq$(this).agikiTreeTable({expandable : true, clickableNodeNames : true, persist : true, article : jq$(this).closest(".defaultMarkupFrame").attr("id") });
	});
	KNOWWE.kdomtreetable.setOverflow();
}

KNOWWE.kdomtreetable.setOverflow = function() {
	jq$('.table_text').hover(function() {
		var elem = jq$(this);
		elem.data("stillin", "yes");
		setTimeout(function() {
			if (elem.data("stillin") === "yes") {
				elem.css("overflow", "auto");
			}
		}, 700);
	}, function() {
		jq$(this).data('stillin', "no");
		jq$(this).css("overflow", "hidden");
	});
};

/**
 * Namespace: KNOWWE.core.plugin.pagination The KNOWWE plugin d3web namespace.
 */
KNOWWE.core.plugin.pagination = function() {

	function saveCookieAndUpdateNode(cookie, id) {
		var cookieStr = JSON.stringify(cookie);
		jq$.cookie("PaginationDecoratingRenderer-" + id, cookieStr);
		KNOWWE.plugin.d3webbasic.actions.updateNode(jq$("#" + id).first().attr(
			"id"), KNOWWE.helper.gup('page'), null);

	}

	function scrollToTopNavigation(id) {
		jq$('html, body').animate({
			scrollTop : jq$("#" + id).offset().top
		}, 0);
	}

	function getSortingSymbol(naturalOrder) {
		var file;
		if (naturalOrder) {
			file = "arrow_down.png";
		} else {
			file = "arrow_up.png";
		}
		return jq$('<img/>', {
			"src" : 'KnowWEExtension/images/' + file,
			"class" : 'sorting'
		});
	}

	return {

		sort : function(element, id) {

			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-"
				+ id));
			var sorting = element.innerText;
			if (cookie) {
				if (cookie.sorting == sorting) {
					cookie.naturalOrder = !cookie.naturalOrder;
				} else {
					cookie.sorting = sorting;
					cookie.naturalOrder = "true";
				}
			} else {
				cookie = {};
				cookie.sorting = sorting;
				cookie.naturalOrder = "true";
			}
			saveCookieAndUpdateNode(cookie, id);
		},

		setCount : function(selected) {

			var id = jq$(selected).closest(".navigationPaginationWrapper")
				.attr('id');

			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-"
				+ id));
			if (cookie == null) {
				cookie = {};
			}
			var scrollToTop = false;
			if ((cookie.count)
				&& parseInt(selected.value, 10) < parseInt(cookie.count, 10)) {
				scrollToTop = true;
			}

			var count = selected.value;
			var startRow = jq$("#" + id + " .startRow").val();
			var search = /^\d+$/;
			var found = search.test(startRow);
			if (!(found)) {
				jq$('.navigationPaginationWrapper #startRow').val('');
				return;
			}
			if (startRow <= 0) {
				startRow = 1;
			}
			if (count == "Max") {
				cookie.startRow = 1;
				cookie.count = "Max";
			} else {
				cookie.startRow = startRow;
				cookie.count = count;
			}

			if (scrollToTop) {
				scrollToTopNavigation(id);
			}
			saveCookieAndUpdateNode(cookie, id);
		},

		navigate : function(id, direction) {

			var count = jq$("#" + id + " .count").val();
			var startRow = jq$("#" + id + " .startRow").val();

			switch (direction) {
				case "begin":
					startRow = 1;
					break;
				case "back":
					if (count == "Max") {
						startRow = 1;
					}
					else {
						if (parseInt(startRow) - parseInt(count) < 1) {
							startRow = 1;
						} else {
							startRow = parseInt(startRow) - parseInt(count);
						}
					}
					break;
				case "forward":
					if (count == "Max") {
						startRow = 1;
					} else {
						startRow = parseInt(startRow) + parseInt(count);
					}
					break;
			}

			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-" + id));
			if (cookie == null) {
				cookie = {};
			}
			cookie.startRow = startRow;
			cookie.count = count;
			saveCookieAndUpdateNode(cookie, id);
			scrollToTopNavigation(id);
		},

		updateStartRow : function(selectedRow) {

			var id = jq$(selectedRow).closest(".navigationPaginationWrapper")
				.attr('id');
			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-"
				+ id));
			if (cookie == null) {
				cookie = {};
			}
			var count = jq$("#" + id + " .count").val();
			var startRow = selectedRow.value;
			var search = /^\d+$/;
			var found = search.test(startRow);
			if (!(found)) {
				jq$("#" + id + " .startRow").val('');
				return;
			}
			if (startRow <= 0) {
				startRow = 1;
			}
			if (count == "Max") {
				cookie.startRow = 1;
				cookie.count = "Max";
			} else {
				cookie.startRow = startRow;
				cookie.count = count;
			}
			saveCookieAndUpdateNode(cookie, id);
			scrollToTopNavigation(id);
		},

		filter : function(checkbox, sectionId) {
			var key = jq$(checkbox).attr("filterkey");
			var value = jq$(checkbox).attr("filtervalue");
			var checked = checkbox.checked;

			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-" + sectionId));
			if (cookie == null) {
				cookie = {};
			}
			if (typeof cookie.filters == "undefined") {
				cookie.filters = new Object();
				cookie.filters[key] = new Array();
				if (checked === true) {
					cookie.filters[key].push(value);
				}
				else {
					cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
				}
			}
			else if (typeof cookie.filters[key] == "undefined") {
				cookie.filters[key] = new Array();
				if (checked === true) {
					cookie.filters[key].push(value);
				}
				else {
					cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
				}
			}
			else {
				if (checked === true) {
					cookie.filters[key].push(value);
				}
				else {
					cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
				}
			}
			saveCookieAndUpdateNode(cookie, sectionId);
		},

		decorateTable : function() {
			jq$(".navigationPaginationWrapper").each(
				function() {
					var sectionId = jq$(this).attr('id');

					// register count selector
					jq$(this).find(".count").on('change', function() {
						KNOWWE.core.plugin.pagination.setCount(this);
					});

					// register start row change event
					jq$(this).find('.startRow').on('change', function() {
						KNOWWE.core.plugin.pagination.updateStartRow(this);
					});

					// make <th> clickable and therefore sortable except if
					// it's stated explicitly otherwise
					var tablePagination = jq$(this).find("table");
					jq$(tablePagination).attr('sectionid', sectionId);
					jq$(tablePagination).find("th").each(
						function(i) {
							var text = jq$(this).text();
							jq$(this.firstChild).wrap('<span></span>');
							if (!jq$(this).hasClass("notSortable")) {
								jq$(this).find("span").bind('click',
									function(event) {
										KNOWWE.core.plugin.pagination
											.sort(this,
											sectionId);
									}
								);
							}
							if (jq$(this).hasClass("filterable")) {
								var filterIcon = jq$('<img/>', {
									"src" : 'KnowWEExtension/images/filter.png',
									"class" : 'filter'
								});
								jq$(this).prepend(filterIcon);
								var preparedFilter = jq$("#paginationFilters div[filtername=" + text + "]").detach();
								jq$(filterIcon).tooltipster({
									content : jq$(preparedFilter).html(),
									interactive : true,
									interactiveTolerance : 1000
								});
							}
						});

					// render sorting symbol
					var cookie = jq$.parseJSON(jq$
						.cookie("PaginationDecoratingRenderer-"
							+ sectionId));
					if (cookie != null && cookie.sorting != null) {
						var thToGetSortingSymbol = jq$("#" + sectionId
							+ " th:contains('" + cookie.sorting + "') span");
						jq$(thToGetSortingSymbol).append(
							getSortingSymbol(cookie.naturalOrder));
					}

				});
		}
	}
}();

// add clickable table headers to every table which is a sibling to a navigation
// bar,
// i.e. initialized by PaginationDecoratingRenderer
KNOWWE.helper.observer.subscribe("afterRerender", function() {
	KNOWWE.core.plugin.pagination.decorateTable();
	jq$("table.termReview").each(function() {
		KNOWWE.plugin.semanticservicecore.initReviewTable(this);
	});
});


//jquery-autogrow for automatic input field resizing (customized for KnowWE)
(function() {

	(function(jq$) {
		var inherit;
		inherit = ['font', 'letter-spacing'];
		return jq$.fn.autoGrow = function(options) {
			var comfortZone, remove, _ref;
			remove = (options === 'remove' || options === false) || !!(options != null ? options.remove : void 0);
			comfortZone = (_ref = options != null ? options.comfortZone : void 0) != null ? _ref : options;
			if (comfortZone != null) {
				comfortZone = +comfortZone;
			}
			return this.each(function() {
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
						check = function() {
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
						position : 'absolute',
						top : -99999,
						left : -99999,
						width : 'auto',
						visibility : 'hidden'
					};
					for (_j = 0, _len1 = inherit.length; _j < _len1; _j++) {
						prop = inherit[_j];
						styles[prop] = input.css(prop);
					}
					testSubject = jq$('<div class="autogrow"/>').css(styles);
					testSubject.insertAfter(input);
					cz = comfortZone != null ? comfortZone : 70;
					check = function() {
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


/* ############################################################### */
/* ------------- Onload Events ---------------------------------- */
/* ############################################################### */
(function init() {

	window.addEvent('domready', _KL.setup);
	if (KNOWWE.helper.loadCheck([ 'Wiki.jsp' ])) {
		window.addEvent('domready', function() {
			KNOWWE.tooltips.enrich();
			KNOWWE.core.plugin.objectinfo.init();
			KNOWWE.core.plugin.renderKDOM();
			KNOWWE.kdomtreetable.init();
			KNOWWE.core.plugin.pagination.decorateTable();
		});
	}
}());

KNOWWE.helper.observer.subscribe("afterRerender", function() {
	KNOWWE.tooltips.enrich(this);
});