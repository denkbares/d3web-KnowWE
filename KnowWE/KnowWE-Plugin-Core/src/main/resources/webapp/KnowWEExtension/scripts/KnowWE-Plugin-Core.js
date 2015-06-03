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
				type : 'post',
				data : JSON.stringify(json),
				contentType : 'application/json, UTF-8',
				success : function(html) {
					KNOWWE.core.util.replaceElement(ids, html);
					if (jq$(root).parents('#compositeEdit').length) {
						_CE.afterPreviewsLoad(root);
						KNOWWE.core.actions.init();
					}
					_TM.decorateToolMenus(root);
					_TM.animateDefaultMarkupMenu(root);

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
				};

				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'none',
						fn : function() {
							window.location = "Wiki.jsp?page="
							+ objectName.innerHTML
						}
					}
				};
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
				};
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
				};
				KNOWWE.core.util.updateProcessingState(1);
				new _KA(options).send();
			}

		},

		/**
		 * shows a list of similar terms
		 */
		lookUp : function(element) {
			if (!element) element = document;
			element = jq$(element);
			var terms = element.find('.objectinfo-terms');
			if (!terms.exists()) return;
			var response = terms.first().text();
			var termsJson = JSON.parse(response);
			var a = termsJson.allTerms;
			element.find('.objectinfo-search').autocomplete({
				source : a
			});
			element.find('.objectinfo-search').on(
				"autocompleteselect",
				function(event, ui) {
					KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(ui.item.value);
				});

			//Open "Show Info" on Enter key press only if term exists - otherwise do nothing
			element.find('.objectinfo-search').keyup(function(e) {
				if (e.keyCode == 13) {
					var val = jq$('.objectinfo-search').val();
					if (jq$.inArray(val, a) != -1) {
						KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(val);
					}

				}
			});
		}
	}
}();

KNOWWE.plugin.renaming = function() {

	var sectionsCache = {};

	var otherOccurencesHashMap = {};

	var viewRoot = {};

	/**
	 * Renames all occurrences of a specific term.
	 */
	function renameTerms(oldValue, replacement, sectionId, forceRename) {
		if (forceRename == null)
			forceRename = false;
		if (oldValue && replacement) {
			var changeNote = 'Renaming: "' + oldValue + '" -> "'
				+ replacement + '"';
			var params = {
				action : "TermRenamingAction",
				termname : oldValue,
				termreplacement : replacement,
				sectionid : sectionId,
				force : forceRename ? "true" : "false"
			};
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
								renameTerms(oldValue, replacement, sectionId, true);
							}
							else {
								KNOWWE.core.util.reloadPage();
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
		};
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

		};
		new _KA(options).send();
		KNOWWE.core.util.updateProcessingState(1);
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
			var parent = jq$(otherOccurencesHashMap[occurence]).parent();
			jq$(otherOccurencesHashMap[occurence]).parent().html(section);
			_TM.decorateToolMenus(parent);
		}

	}

	function afterCancelEdit(setting, original) {
		_TM.decorateToolMenus(jq$(original));
	}

	function showCurrentEditOnOtherOccurences(text) {
		for (var occurence in otherOccurencesHashMap) {
			jq$(otherOccurencesHashMap[occurence]).first().text(text);
		}
	}

	function saveOriginalsAndPrepareForEdit(lastPathElement) {
		for (var occurence in otherOccurencesHashMap) {
			sectionsCache[occurence] = jq$(otherOccurencesHashMap[occurence]).parent().html();
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
				var settings = {};
				settings.select = true;
				jq$(".click").editable(function(value, settings) {
					renameTerms(jsonResponse.termIdentifier, value, toolMenuIdentifier, false);
					return (value)
				}, {
					style : "inherit",
					onreset : cancelEdit,
					afterreset : afterCancelEdit,
					select : true

				});
				jq$('.click').trigger("click");
				//replace edit field value with sectionText for encoding reasons
				var inputField = jq$(clickedTerm).find("input").val(jsonResponse.lastPathElement).select();
				jq$(inputField).autoGrowRenameField(5);


				sectionIds = jsonResponse.sectionIds;

				initializeOtherOccurencesHashMap(sectionIds);

				saveOriginalsAndPrepareForEdit(jsonResponse.lastPathElement);

				jq$(".click input").keyup(function() {
					showCurrentEditOnOtherOccurences(jq$(this).val());
				});
			};
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
	};
	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
			action : 'none',
			fn : function() {
				window.location.reload();
			},
			onError : _EC.onErrorBehavior
		}
	};
	_KU.showProcessingIndicator();
	new _KA(options).send();
};

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
			contentAsHTML : true,
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
		jq$(this).agikiTreeTable({
			expandable : true,
			clickableNodeNames : true,
			persist : true,
			article : jq$(this).closest(".defaultMarkupFrame").attr("id")
		});
	});
	KNOWWE.kdomtreetable.setOverflow();
};

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

	var windowHeight;

	function saveCookieAndUpdateNode(cookie, id) {
		saveCookie(cookie, id);
		updateNode(id);
	}

	function saveCookie(cookie, id) {
		var cookieStr = JSON.stringify(cookie);
		jq$.cookie("PaginationDecoratingRenderer-" + id, cookieStr);
	}

	function updateNode(id) {
		KNOWWE.plugin.d3webbasic.actions.updateNode(id, KNOWWE.helper.gup('page'), null);
	}

	function scrollToTopNavigation(id) {
		jq$('html, body').animate({
			scrollTop : jq$("#" + id).offset().top
		}, 0);
	}

	function getSortingSymbol(naturalOrder) {
		var cssClass;
		if (naturalOrder) {
			cssClass = "fa fa-caret-up fa-lg";
		} else {
			cssClass = "fa fa-caret-down fa-lg";
		}
		return jq$('<i/>', {
			"class" : cssClass
		});
	}

	function renderSortingSymbols(sectionId, sortingMode) {
		var cookie = jq$.parseJSON(jq$
			.cookie("PaginationDecoratingRenderer-"
			+ sectionId));
		if (cookie != null && cookie.sorting != null) {
			var sortLength;
			if (sortingMode === 'multi') {
				sortLength = (cookie.sorting).length;
			}
			else {
				sortLength = 1;
			}
			for (var i = 0; i < sortLength; i++) {
				var sortingSymbolParent = jq$("[pagination=" + sectionId
				+ "] th:contains('" + cookie.sorting[i].sort + "') span");
				var sortingSymbol = jq$(sortingSymbolParent).find("i");
				if (sortingSymbol.length == 1) {
					sortingSymbol.replaceWith(
						getSortingSymbol(cookie.sorting[i].naturalOrder));
				}
				else {
					jq$(sortingSymbolParent).append(getSortingSymbol(cookie.sorting[i].naturalOrder));
				}
			}
		}
	}

	function enableSorting(sectionId) {
		jq$(this.firstChild).wrap('<span></span>');
		if (!jq$(this).hasClass("notSortable")) {
			jq$(this).addClass("sortable");
			jq$(this).find("span").bind('click',
				function(event) {
					KNOWWE.core.plugin.pagination
						.sort(this,
						sectionId);
				}
			);
		}
	}

	function prepareFilterableElements() {
		var filterIcon = jq$('<i/>', {
			"class" : 'fa fa-filter knowwe-filter'
		});
		jq$(this).prepend(filterIcon);
		var text = jq$(this).text();
		var preparedFilter = jq$("#paginationFilters div[filtername=" + text + "]").detach();
		jq$(filterIcon).tooltipster({
			content : jq$(preparedFilter),
			interactive : true,
			interactiveTolerance : 500,
			theme : "tooltipster-knowwe"
		});
	}

	function isShortTable() {
		return (jq$(this).height() < jq$(window).height());
	}

	function toggleLowerPagination(visibility) {
		var id = jq$(this).attr("pagination");
		jq$(".knowwe-paginationToolbar[pagination=" + id + "]").slice(2, 4).css("display", visibility);
	}

	function handlePaginationBelowTableVisibility() {
		if (isShortTable.call(this)) {
			toggleLowerPagination.call(this, "none");
		}
		else {
			toggleLowerPagination.call(this, "inline-block")
		}
	}

	jq$(window).resize(function() {
		if (windowHeight != jq$(window).height()) {
			//Do something
			windowHeight = jq$(window).height();
			jq$("table[pagination]").each(
				function() {
					handlePaginationBelowTableVisibility.call(this);
				}
			)
		}
	});

	function decorate() {

		windowHeight = jq$(window).height();

		return function() {
			var sectionId = jq$(this).attr('pagination');

			//for css purposes
			jq$(this).addClass('knowwe-pagination');

			// register count selector
			jq$('div[pagination=' + sectionId + '] .count').on('change', function() {
				KNOWWE.core.plugin.pagination.setCount(this, sectionId);
			});

			// register start row change event
			jq$('div[pagination=' + sectionId + '] .startRow').on('change', function() {
				KNOWWE.core.plugin.pagination.updateStartRow(this, sectionId);
			});

			var sortingMode = jq$(this).attr('sortable');

			jq$(this).find("th").each(
				function(i) {
					// make <th> clickable and therefore sortable except if
					// it's stated explicitly otherwise
					if (typeof sortingMode != 'undefined') {
						enableSorting.call(this, sectionId);
					}
					if (jq$(this).hasClass("filterable")) {
						prepareFilterableElements.call(this);
					}
				}
			);

			// render sorting symbol
			renderSortingSymbols(sectionId, sortingMode);

			handlePaginationBelowTableVisibility.call(this);
		}
	}

	function handleNoResult(pagination) {
		if (!pagination.find('table').exists()) {
			pagination.find(".knowwe-paginationToolbar").remove();
		}
	}

	return {

		sort : function(element, id) {
			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-" + id));
			if (cookie == null) {
				cookie = {};
			}
			var sort = element.innerText;
			var sorting;
			if (typeof cookie.sorting != 'undefined') {
				sorting = cookie.sorting;
				var found = false;
				for (var i = 0; i < sorting.length; i++) {
					if (sorting[i].sort == sort) {
						var naturalOrder = sorting[i].naturalOrder;
						sorting[i].naturalOrder = !naturalOrder;
						sorting.move(i, 0);
						found = true;
						break;
					}
				}
				if (found == false) {
					newSortObject = {sort : sort, naturalOrder : true};
					sorting.unshift(newSortObject);
				}
			}
			else {
				sorting = [{sort : sort, naturalOrder : true}];
			}
			cookie.sorting = sorting;
			saveCookieAndUpdateNode(cookie, id);
		},

		setCount : function(selected, id) {
			var $selected = jq$(selected);

			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-"
			+ id));
			if (cookie == null) {
				cookie = {};
			}

			var lastCount = parseInt(cookie.count);
			var resultSize = parseInt(jq$('#' + id + " .resultSize").val());
			var count = $selected.val();
			$selected.data('current', count);
			var startRow = parseInt(jq$('div[pagination=' + id + '] .startRow').val());
			var search = /^\d+$/;
			var found = search.test(startRow);
			if (!(found)) {
				jq$('div[pagination=' + id + '] .startRow').val('');
				return;
			}

			if (count == "Max") {
				cookie.startRow = 1;
				cookie.count = "Max";
			} else {
				if (startRow + lastCount === resultSize + 1) {
					startRow = resultSize - parseInt(count) + 1;
				}
				if (startRow <= 0) {
					startRow = 1;
				}
				cookie.startRow = startRow;
				cookie.count = count;
			}

			saveCookieAndUpdateNode(cookie, id);
		},

		navigate : function(id, direction) {

			var count = jq$("#" + id + " .count").val();
			var startRow = jq$("#" + id + " .startRow").val();
			var resultSize = jq$("#" + id + " .resultSize").val();

			if (count == "All") {
				startRow = 1;
			} else {
				switch (direction) {
					case "begin":
						startRow = 1;
						break;
					case "back":
						if (parseInt(startRow) - parseInt(count) < 1) {
							startRow = 1;
						} else {
							startRow = parseInt(startRow) - parseInt(count);
						}
						break;
					case "forward":
						startRow = parseInt(startRow) + parseInt(count);
						break;
					case "end":
						startRow = parseInt(resultSize) - parseInt(count) + 1;
						break;
				}
			}


			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-" + id));
			if (cookie == null) {
				cookie = {};
			}
			cookie.startRow = startRow;
			cookie.count = count;
			saveCookieAndUpdateNode(cookie, id);

		},

		updateStartRow : function(selectedRow, sectionId, preventRerender) {

			var id = sectionId;
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
			saveCookie(cookie, id);
			if (!preventRerender) updateNode(id);
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
				cookie.filters = {};
				cookie.filters[key] = [];
				if (checked === true) {
					cookie.filters[key].push(value);
				}
				else {
					cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
				}
			}
			else if (typeof cookie.filters[key] == "undefined") {
				cookie.filters[key] = [];
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
			handleNoResult(jq$(this));
			var id = jq$(this).find(".knowwe-paginationToolbar").first().attr('pagination');
			jq$(this).find("table").attr('pagination', id);
			jq$(this).find("table[pagination]").each(decorate());
		},

		decorateTables : function() {
			handleNoResult(jq$(this));
			var wrappers = jq$("div.knowwe-paginationWrapper");
			wrappers.each(function() {
				jq$(this).find("table").attr('pagination', jq$(this).attr('id'));
			});

			jq$("table[pagination]").each(decorate());
		}
	}
}();


/**
 * Namespace: KNOWWE.core.plugin.rightPanel for debugging D3web expressions in KnowWE
 *
 */
KNOWWE.core.plugin.rightPanel = function() {

	var rightPanelStorageKey = "rightPanel";

	var rightPanel = null;

	var showSidebar = false;

	var globalFloatingTime = 500;

	var initScrolling = null;

	var windowWidth;

	function getSelected() {
		var t = '';
		if (window.getSelection) {
			t = window.getSelection();
		} else if (document.getSelection) {
			t = document.getSelection();
		} else if (document.selection) {
			t = document.selection.createRange().text;
		}
		return t;

	}

	function rightPanelScroll() {
		var element = $("rightPanel");
		if (!element)
			return;
		var originY = initScrolling;
		var wHeight = window.getHeight();

		var docHeight = getDocHeight();
		var favHeight = element.clientHeight;
		var scrollY = window.getScrollTop();
		var scrollMax = docHeight - wHeight;
		var favToScroll = favHeight - wHeight;
		var actionsBottom = $("actionsBottom");
		var disableFixing = (actionsBottom == null
		|| favHeight >= actionsBottom.offsetTop + actionsBottom.clientHeight);
		if (scrollY <= originY || disableFixing) {
			// when reaching top of page or if page height is made by leftMenu
			// align fav originally to page
			element.style.position = "absolute";
			element.style.top = originY + "px";
		} else if (scrollMax - scrollY <= favToScroll) {
			// when reaching end of page
			// align bottom of fav to bottom of page
			element.style.position = "absolute";
			element.style.top = (docHeight - favHeight) + "px";
		} else {
			// otherwise fix fav to the top of the viewport
			element.style.position = "fixed";
			element.style.top = "0px";

		}

		function getDocHeight() {
			var D = document;
			return Math.max(Math.max(D.body.scrollHeight,
				D.documentElement.scrollHeight), Math.max(D.body.offsetHeight,
				D.documentElement.offsetHeight), Math.max(D.body.clientHeight,
				D.documentElement.clientHeight));
		}
	}


	function makeRightPanelResizable() {
		rightPanel.resizable({
			handles : "w",
			minWidth : 300,
			maxWidth : jq$(window).width()
			- (jq$(".tabmenu a:last-child").first().offset().left
			+ jq$(".tabmenu a:last-child").first().outerWidth()
			+ jq$("#actionsTop").outerWidth()
			+ jq$("#rightPanel .ui-resizable-w").width() + 20),
			alsoResize : "#watches textarea, #watches .watchlistline",
			resize : function(event, ui) {
				jq$(window).resize();
			}
		});
	}

	function restoreLayout() {
		var resize = jq$(window).width() - jq$("#favorites").outerWidth() - 300;
		jq$("#page").css("width", resize + "px");
		jq$(rightPanel).css("width", "300px");
		var pagesRightOffset = (jq$(window).width() - (jq$("#actionsTop").offset().left + jq$("#actionsTop").width()));
		rightPanel.css("left", (jq$(window).width() - pagesRightOffset) + "px");
	}

	function floatRightPanel() {

		showSidebar = true;

		var options = {right : '0px'};

		rightPanel.animate(options, globalFloatingTime, function() {
			//"left" is needed for resizable to work properly
			var pagesRightOffset = (jq$(window).width() - (jq$("#actionsTop").offset().left + jq$("#actionsTop").width()));
			rightPanel.css("left", (jq$(window).width() - pagesRightOffset) + "px");
		});
		initScrolling = jq$(".tabs").offset().top;
		jq$(window).scroll(function() {
			rightPanelScroll();
		});

		windowWidth = jq$(window).width();

		jq$(window).resize(function() {
			if (showSidebar) {
				windowWidth = jq$(window).width();

				var resize = jq$(window).width() - jq$('#rightPanel').width() - jq$("#page").offset().left;
				jq$("#page").css("width", resize + "px");

				var pagesRightOffset = (jq$(window).width() - (jq$("#actionsTop").offset().left + jq$("#actionsTop").width()));
				rightPanel.css("left", (jq$(window).width() - pagesRightOffset) + "px");
			}
		});

		//make sidebar resizable
		makeRightPanelResizable();
	}

	function shrinkPage() {
		jq$("#page").animate({
			'width' : "-=300px"
		}, globalFloatingTime);
		jq$("#pagecontent").css("margin-right", "5px");
		jq$("#actionsBottom").css("margin-right", "5px");
	}

	function growPage() {
		jq$("#morepopup").css("display", "none");
		var pageWidth = jq$('#page').width() + jq$('#rightPanel').width();
		jq$("#page").animate({
			'width' : pageWidth
		}, globalFloatingTime, function() {
			jq$('#page').css("width", "auto");
		});
		rightPanel.animate({
			left : (jq$(window).width() + "px")
		}, globalFloatingTime, function() {
			rightPanel.remove();
			jq$("#morepopup").css("display", "block");
		});
		jq$("#pagecontent").css("margin-right", "auto");
		jq$("#actionsBottom").css("margin-right", "auto");
	}

	function buildRightPanel() {
		var offsetTop = jq$(".tabs").offset().top;
		var scrollTop = jq$(window).scrollTop();
		rightPanel = jq$('<div/>', {
			'id' : 'rightPanel',
			'css' : {
				'position' : 'absolute',
				'top' : (offsetTop - scrollTop) + 'px',
				'right' : '-300px',
				'width' : "300px",
				'overflow-x' : 'hidden',
				'overflow-y' : 'hidden'
			}
		});

		var rightPanelHide = jq$('<div/>', {
			'class' : 'rightpanelhide'
		});


		var rightPanelHideText = jq$('<span/>', {
			'text' : 'Hide'

		});

		var rightPanelHideIcon = jq$('<img/>', {
			'src' : 'KnowWEExtension/images/arrow_right.png'

		});

		rightPanelHide.append(rightPanelHideText);
		rightPanelHide.append(rightPanelHideIcon);
		rightPanel.append(rightPanelHide);
		jq$("#content").append(rightPanel);

	}

	function initRightPanelTools() {
		KNOWWE.core.plugin.rightPanel.watches.initWatchesTool();
	}

	function setRightPanelCookie(b) {
		var storage = simpleStorage.get(rightPanelStorageKey);
		if (typeof storage == 'undefined') {
			storage = {}
		}

		else {
			simpleStorage.deleteKey(rightPanelStorageKey);
		}
		simpleStorage.set(rightPanelStorageKey, b);

	}

	function bindCollapseIcons() {
		jq$("#rightPanel").on("click", ".tool .topbar", function() {
			if (jq$(this).find("i").hasClass("fa-caret-down")) {
				jq$(this).find("i").removeClass("fa-caret-down").addClass("fa-caret-right");
				jq$(this).parent().find(".content").first().slideUp();
			}
			else {
				if (jq$(this).find("i").hasClass("fa-caret-right")) {
					jq$(this).find("i").removeClass("fa-caret-right").addClass("fa-caret-down");
					jq$(this).parent().find(".content").first().slideDown();
				}
			}
		});
	}


	function bindHideFunctions() {
		jq$("#morebutton .watches").unbind();
		bindHideInPanel();
		bindHideInMoreMenu();


		function bindHideInMoreMenu() {
			jq$("#morebutton .watches").prop("title", "Hide Right Panel");
			jq$("#morebutton .watches").on("click", function() {
				terminateRightPanel();
			});
			jq$("#morebutton .watches").text("Hide Right Panel");
		}


		function bindHideInPanel() {
			jq$("#rightPanel .rightpanelhide").on("click", function() {
				terminateRightPanel();
			})
		}
	}

	function changeHideToShow() {
		jq$("#morebutton .watches").prop("title", "Show Right Panel");
		jq$("#morebutton .watches").unbind();
		jq$("#morebutton .watches").on("click", function() {
			KNOWWE.plugin.core.rightPanel.showRightPanel();
		});
		jq$("#morebutton .watches").text("Show Right Panel");
	}

	function bindUiActions() {
		bindCollapseIcons();
		bindHideFunctions();
	}

	function initRightPanel(fromCookie) {
		if (!showSidebar) {
			showSidebar = true;
			if (fromCookie) {
				globalFloatingTime = 0;
			}
			else {
				globalFloatingTime = 500;
			}
			shrinkPage();
			buildRightPanel();
			floatRightPanel();
			setRightPanelCookie(true);
			bindUiActions();
			initRightPanelTools();
			globalFloatingTime = 500;
		}
	}

	function terminateRightPanel() {
		growPage();
		changeHideToShow();
		setRightPanelCookie(false);
		showSidebar = false;
	}

	function buildToolContainer(id) {
		return tool = jq$('<div/>', {
			'id' : id,
			'class' : 'tool',
			'css' : {
				'position' : 'relative'
			}
		});
	}

	function buildTopBar(title) {
		var toolTopbar = jq$('<div/>', {
			'class' : 'topbar'
		});
		var collapseIcon = jq$('<i/>', {
			'class' : 'collapseicon fa fa-fw fa-caret-down'
		});
		var toolTitle = jq$('<div/>', {
			'class' : 'title',
			'text' : title
		});
		toolTitle.prepend(collapseIcon);
		toolTopbar.append(toolTitle);
		return toolTopbar;
	}

	function appendNewToolToRightPanel(tool, topbar, div) {
		tool.append(topbar);
		tool.append(div);
		rightPanel.append(tool);
	}

	function buildToolContent(pluginDiv) {
		var content = jq$('<div/>', {
				'class' : 'content'
			}
		);
		content.append(pluginDiv);
		return content;
	}

	function isRightPanelShown() {
		return simpleStorage.get(rightPanelStorageKey) == true;
	}

	function initRightPanelToggleButton(isShown) {
		var orientation = (isShown ? "right" : "left");
		var status = (isShown ? "Hide" : "Show");
		jq$('#morebutton').after("<li><a id='rightPanel-toggle-button' title='" + status + " right panel'"
		+ " class='action fa fa-angle-double-" + orientation + "'></a></li>");
		jq$('#rightPanel-toggle-button').unbind('click').click(function() {
			var $this = jq$(this);
			if (isRightPanelShown()) {
				KNOWWE.core.plugin.rightPanel.hideRightPanel();
				$this.removeClass("fa-angle-double-right").addClass("fa-angle-double-left").attr("title", "Show right panel");
			} else {
				KNOWWE.core.plugin.rightPanel.showRightPanel();
				$this.removeClass("fa-angle-double-left").addClass("fa-angle-double-right").attr("title", "Hide right panel");
			}
		});
	}

	return {

		showRightPanel : function() {
			initRightPanel(false);
		},

		hideRightPanel : function() {
			terminateRightPanel();
		},

		init : function() {
			var isShown = isRightPanelShown();
			if (isShown) {
				initRightPanel(true);
			}
			initRightPanelToggleButton(isShown);
		},

		addToolToRightPanel : function(title, id, pluginDiv) {
			var tool = buildToolContainer(id);
			var topbar = buildTopBar(title);
			var content = buildToolContent(pluginDiv);
			appendNewToolToRightPanel(tool, topbar, content);
		}

	}
}();

KNOWWE.core.plugin.rightPanel.watches = function() {

	var watchesStorageKey = "watches";

	var watchesArray;

	var watches;

	var restorableEntries = {};

	var watchlist;

	function bindUiActions() {
		watches.on("click", ".watchlistentry", function(e) {
			editWatch(this);
		});
		watches.on("click", ".addwatch", function(e) {
			addWatch();
		});
		watches.on("click", ".fromselection", function(e) {
			addWatchFromSelection();
		});
		watches.on("keydown", "textarea", function(e) {
			handleTextarea(this, e);
		});
		watches.on("click", ".deletewatch", function(e) {
			e.stopPropagation();
			removeWatch(this);
		});
	}

	function handleResponse(data) {
		var parsed = JSON.parse(data);
		var expressionArrays = parsed.values;
		var oldEntries = watchlist.find(".watchlistentry");
		jq$.each(expressionArrays, function(index, value) {
			var newEntry = createNewEntry(watchesArray[index], value);
			jq$(oldEntries[index]).replaceWith(newEntry);
		});
		enableAddWatch();
	}

	function updateOldWatchesList(data) {
		handleResponse(data);
	}

	function updateWatches() {
		getExpressionValue(watchesArray).success(function(data) {
			updateOldWatchesList(data);
		});


	}

	function enableAddWatch() {
		if (watches.find(".addwatch").prop("disabled") == true && watches.find(".newwatch").length == 0) {
			watches.find(".addwatch").prop("disabled", false);
			watches.find(".fromselection").prop("disabled", false);
		}
	}

	function disableAddWatch() {
		if (watches.find(".addwatch").prop("disabled") == false) {
			watches.find(".addwatch").prop("disabled", true);
			watches.find(".fromselection").prop("disabled", true);
		}
	}

	function addWatch(text) {
		var textarea = createTextarea(null, text);
		watchlist.append(textarea);
		jq$(textarea).find("textarea").focus();
		//allow only one new textarea - disable Add Watch
		disableAddWatch();
	}

	function addWatchFromSelection() {
		var text = getSelectionText();
		addWatch(text);
	}

	function removeWatch(that) {
		var index = getWatchesIndex(jq$(that).parent());
		watchesArray.splice(index, 1);
		jq$(that).parent().remove();
		updateCookies();
	}

	function saveOldEntry(index, that) {
		var oldEntry = jq$(that);
		restorableEntries[index] = oldEntry;
	}

	function editWatch(that) {
		var index = getWatchesIndex(that);
		saveOldEntry(index, that);
		watchesArray.splice(index, 1);
		var textarea = createTextarea(that);
		jq$(that).replaceWith(textarea);
		jq$(textarea).find("textarea").focus();
		//allow only one new textarea - disable Add Watch
		disableAddWatch();
	}

	function restoreEntry(entry, watchesIndex) {
		var restorableEntry = restorableEntries[watchesIndex];
		var restorableExpression = jq$(restorableEntry).find(".expression").text();
		watchesArray.splice(watchesIndex, 0, restorableExpression);
		jq$(entry).replaceWith(restorableEntry);
		delete restorableEntries[watchesIndex];
	}

	function handleTextarea(that, e) {
		var entry = jq$(that).parent();
		var watchesIndex = getWatchesIndex(entry);

		//escape
		if (e.keyCode == 27) {
			//is it an old element?
			if (watchesIndex <= watchesArray.length) {
				//restore it
				restoreEntry(entry, watchesIndex);
			}
			else {
				entry.remove();
			}
			enableAddWatch();
		}

		if (jq$(that).data('ui-tooltip') && jq$(that).val().trim() != "") {
			jq$(that).tooltip("destroy");
			jq$(that).attr("title", null);
		}

		var trimmedValue = jq$(that).val().trim();
		//shift+enter = newline, enter=submit
		if (e.keyCode == 13 && !e.shiftKey) {
			if (trimmedValue == "") {
				jq$(that).tooltip({position : {my : "right bottom", at : "left top"}});
				jq$(that).attr("title", "Please enter an expression.");
				jq$(that).trigger("mouseover");
				// prevent default behavior
				e.preventDefault();
				//alert("ok");
			}
			else {
				if (watchesIndex < watchesArray.length) {
					addExpression(jq$(that), watchesIndex);
				}
				else {
					jq$(that).val(trimmedValue);
					addExpression(jq$(that));
				}

			}
		}

	}

	function getWatchesIndex(that) {
		return jq$(that).index();
	}

	function getExpressionValue(expr, id) {
		var data = {expressions : expr, page : KNOWWE.helper.gup('page'), id : id};
		return jq$.ajax({
			type : 'post',
			url : 'action/GetExpressionValueAction',
			data : JSON.stringify(data),
			cache : false,
			contentType : 'application/json, UTF-8'
		});
	}


	function createTextarea(that, text) {
		var watchesNewEntry = jq$('<div/>', {
			'class' : 'newwatch watchlistline'
		});
		var textarea = jq$('<textarea>', {});

		var textareaDom = textarea[0];

		if (typeof AutoComplete != "undefined") {
			new AutoComplete(textareaDom, function(callback, prefix) {
				var scope = "$d3web/condition";
				var data = {prefix : prefix, scope : scope};
				if (KNOWWE && KNOWWE.helper) {
					data.KWiki_Topic = KNOWWE.helper.gup('page');
				}
				jq$.ajax({
					url : 'action/CompletionAction',
					cache : false,
					data : data
				}).success(function(data) {
					callback(eval(data));
				});
			});
		}

		if (that) {
			var oldEntry = restorableEntries[getWatchesIndex(that)];
			var oldText = jq$(oldEntry).find(".expression").text();
			textarea.val(oldText);
		}

		if (typeof text != 'undefined') {
			textarea.val(text);
		}
		textarea.autosize({minHeight : "22px"});
		watchesNewEntry.append(textarea);
		return watchesNewEntry;
	}

	function createWatchesEntryValueSpan(value) {
		return jq$('<span/>', {
			'class' : 'value tooltip',
			'text' : value.value,
			'title' : value.kbname
		});
	}


	function createWatchesEntryHistoryValueSpan(title) {
		return jq$('<span/>', {
			'class' : 'value tooltip history',
			'title' : title
		});
	}

	function handleDefaultResponse(watchesEntry, responseObject) {

		jq$.each(responseObject.kbsEntries, function iterateValuesFromDifferentKbs(index, value) {
			var watchesEntryValue = createWatchesEntryValueSpan(value);
			var tooltipcontent = jq$('<span style="padding-right: 5px" class="fa fa-book"></span><span>' + value.kbname + '  </span>');
			jq$(watchesEntryValue).tooltipster({
				content : tooltipcontent,
				position : "top-left",
				delay : 300,
				theme : ".tooltipster-knowwe"
			});
			watchesEntry.append(watchesEntryValue);
		});

		return watchesEntry;

	}

	function handleHistoryResponse(watchesEntry, responseObject) {

		jq$.each(responseObject.kbsEntries, function iterateValuesFromDifferentKbs(index, value) {
			var watchesEntryValue = createWatchesEntryHistoryValueSpan(value.kbname);

			jq$.each(value.value, function iterateValuesInHistory(index, value) {
				var historyEntrySpan = jq$('<span/>', {
					'class' : 'value tooltip historyentry',
					'text' : value.value
				});
				createTimestampsToolTip.call(this, historyEntrySpan);
				watchesEntryValue.append(historyEntrySpan);

			});
			watchesEntry.append(watchesEntryValue);
		});

		function createTimestampsToolTip(historyEntrySpan) {
			var start = this.timestamps[0];
			var end = this.timestamps[1];
			var tooltipcontent;
			if (start != end) {
				tooltipcontent = jq$('<span>Start: ' + start + '</span><br><span>End: ' + end + '  </span>');
			}
			else {
				tooltipcontent = jq$('<span>Start: ' + start + '</span>');
			}
			jq$(historyEntrySpan).tooltipster({
				content : tooltipcontent,
				position : "top-left",
				delay : 300,
				theme : ".tooltipster-knowwe"
			});
		}

		return watchesEntry;
	}

	function createNewEntry(expression, responseObject) {

		var watchesEntry = jq$('<div/>', {
			'class' : 'watchlistline watchlistentry'

		});
		watchesEntry.uniqueId();
		var watchesEntryExpression = jq$('<span/>', {
			'class' : 'expression',
			'text' : expression
		});
		watchesEntry.append(watchesEntryExpression);


		var length = Object.keys(responseObject).length;
		if (length > 0) {
			switch (responseObject.info) {
				case 'history':
					handleHistoryResponse(watchesEntry, responseObject);
					break;
				default:
					handleDefaultResponse(watchesEntry, responseObject);
			}
		}
		else {
			var watchesEntryValue = jq$('<span/>', {
				'class' : 'value expressionerror',
				'text' : '<not a valid expression>'
			});
			watchesEntry.append(watchesEntryValue);
		}

		watchesEntry.append(createDeleteButton());
		return watchesEntry;
	}

	function addExpression(original, watchesIndex) {

		var expression = original.val();

		var watchesEntry = jq$('<div/>', {
			'class' : 'watchlistline watchlistentry',
			'text' : expression
		});
		jq$(original).parent().replaceWith(watchesEntry);

		if (typeof watchesIndex != 'undefined') {
			watchesArray.splice(watchesIndex, 0, expression);
		}
		else {
			watchesArray.push(expression);
		}
		updateCookies();

		getExpressionValue(watchesArray).success(function(data) {
			updateOldWatchesList(data);
		});
	}

	function buildBasicWatchesDiv() {

		var watchcontent = jq$('<div/>', {});

		var watchlist = jq$('<div/>', {
			'class' : 'watchlist'
		});

		var watchesAddEntry = jq$("<button class='addwatch'><i class='fa fa-plus-circle'></i>&nbsp;Add Watch</button>");


		var watchesAddEntryFromSelection = jq$("<button class='fromselection'><i class='fa fa-paragraph'></i>&nbsp;from Selection</button>");

		watchcontent.append(watchlist);
		watchcontent.append(watchesAddEntry);
		watchcontent.append(watchesAddEntryFromSelection);

		KNOWWE.core.plugin.rightPanel.addToolToRightPanel("Watches", "watches", watchcontent);
	}

	function createDeleteButton() {

		var deleteContainer = jq$('<div/>', {
			'class' : 'iconcontainer deletewatch select'
		});

		var deleteIcon = jq$("<a class=''><i class='fa fa-times-circle icon'></i></a>");


		return deleteContainer.append(deleteIcon);
	}

	function loadWatchesFromCookies() {
		watchesArray = simpleStorage.get(watchesStorageKey);
		if (typeof watchesArray != 'undefined') {
			getExpressionValue(watchesArray).success(function(data) {
				var parsed = JSON.parse(data);
				var expressionArrays = parsed.values;
				jq$.each(expressionArrays, function(index, value) {
					var newEntry = createNewEntry(watchesArray[index], value);
					watchlist.append(newEntry);
				});
			});
		}
		else {
			watchesArray = [];
		}
	}

	function updateCookies() {
		simpleStorage.set(watchesStorageKey, watchesArray);
	}

	function getSelectionText() {
		var text = "";
		if (window.getSelection) {
			text = window.getSelection().toString();
		} else if (document.selection && document.selection.type != "Control") {
			text = document.selection.createRange().text;
		}
		return text;
	}

	function initVariables() {
		watches = jq$("#watches");
		watchlist = watches.find(".watchlist");
	}

	return {
		initWatchesTool : function() {
			buildBasicWatchesDiv();
			loadWatchesFromCookies();
			initVariables();
			bindUiActions();
			KNOWWE.helper.observer.subscribe("update", function() {
				updateWatches();
			});
		},

		addToWatches : function(text) {
			KNOWWE.core.plugin.rightPanel.showRightPanel();
			addWatch(text);
		}
	}
}();

/**
 * Namespace: KNOWWE.core.plugin.rightPanel for debugging D3web expressions in KnowWE
 *
 */
KNOWWE.core.plugin.reloadNamespaceFile = function() {

	function doSomething() {


	}


	return {

		reloadFile : function(namespaceUrl, filename, title) {
			jq$.ajax("action/NamespaceFileReloadAction", {
				type : 'post',
				data : {
					namespaceUrl : namespaceUrl,
					filename : filename,
					title : title
				},
				success : function() {
					KNOWWE.notification.success("Success", "You successfully reloaded the namespace file in the attachment.", filename);
					//KNOWWE.core.util.reloadPage();

				},
				error : function() {
					KNOWWE.notification.loadNotifications();
				}
			});
		}
	}
}();

//jquery-autogrow for automatic input field resizing (customized for KnowWE renaming)
(function() {

	(function(jq$) {
		var inherit;
		inherit = ['font', 'letter-spacing'];
		return jq$.fn.autoGrowRenameField = function(options) {
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
	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function() {
			KNOWWE.tooltips.enrich();
			KNOWWE.core.plugin.objectinfo.init();
			KNOWWE.core.plugin.renderKDOM();
			KNOWWE.kdomtreetable.init();
			KNOWWE.core.plugin.pagination.decorateTables();
			KNOWWE.core.plugin.rightPanel.init();
		});
	}
}());

KNOWWE.helper.observer.subscribe("afterRerender", function() {
	KNOWWE.tooltips.enrich(this);
	KNOWWE.core.plugin.objectinfo.lookUp(this);
	KNOWWE.core.plugin.pagination.decorateTable.call(this);
	jq$("table.termReview").each(function() {
		KNOWWE.plugin.semanticservicecore.initReviewTable(this);
	});
});

Array.prototype.move = function(old_index, new_index) {
	if (new_index >= this.length) {
		var k = new_index - this.length;
		while ((k--) + 1) {
			this.push(undefined);
		}
	}
	this.splice(new_index, 0, this.splice(old_index, 1)[0]);
	return this; // for testing purposes
};