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

KNOWWE = typeof KNOWWE === "undefined" ? {}: KNOWWE;
KNOWWE.core = KNOWWE.core || {};
KNOWWE.core.plugin = KNOWWE.core.plugin || {}

KNOWWE.core.plugin.objectinfo = function() {

	return {

		init: function() {
			// init renaming form button
			jq$('#objectinfo-replace-button').click(
				KNOWWE.core.plugin.objectinfo.renameFunction);

			// we have to suspend the enter event to prevent multiple
			// confirm dialogs after when confirming the dialogs with enter...
			let suspend = false;
			jq$('#objectinfo-replacement').keyup(function(event) {
				if (event.keyCode === 13 && !suspend) {
					suspend = true;
					if (confirm("Are you sure you want to rename this term?")) {
						KNOWWE.core.plugin.objectinfo.renameFunction();
					} else {
						suspend = false;
					}
				}
			});

			// highlight section navigation via anchors
			KNOWWE.core.plugin.objectinfo.highlighAnchor();
		},

		highlighAnchor: function() {
			if (!window.location.hash.startsWith("#section-")) return;
			jq$('.anchor-highlight').removeClass("highlight").removeClass("anchor-highlight");
			const name = window.location.hash.substring(1);
			const sectionId = name.substring(8);
			// first, try to find section directly
			const section = jq$('[sectionid="' + sectionId + '"]');
			if (section.exists()) {
				section.addClass('highlight');
				section.addClass('anchor-highlight');
				return;
			}
			// section not found, try highlighting from anchor to anchor-end
			const anchor = jq$('.anchor[name="' + name + '"]');
			if (!anchor.exists()) return;
			const endSelector = '.anchor,.anchor-end[name="' + name + '"]';
			if (!anchor.nextAll(endSelector).exists()) return;
			let next = anchor.next();
			while (!next.is(endSelector)) {
				next.addClass('highlight');
				next.addClass('anchor-highlight');
				next = next.next();
			}
		},

		/**
		 * Load the ajax-previews
		 */
		loadPreviews: function(root) {
			const select = (root === undefined)
				? jq$('.asynchronPreviewRenderer')
				: jq$(root).find('.asynchronPreviewRenderer');
			const json = [];
			const ids = [];
			select.each(function() {
				json.push(this.getAttribute('rel'));
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
					KNOWWE.core.plugin.objectinfo.highlightTermReferences(root, json);
					_TM.decorateToolMenus(root);
					_TM.animateDefaultMarkupMenu(root);
					_TM.adjustSingletonMenus(root);
					/**
					 * Trigger custome Event here to mount the React components so that the
					 * Item can be renderd on the users side. This even will be triggered
					 * multiple times since loadPreviews is called async. the react components
					 * will handle changes so that only new items will be rendered.
					 */
					jq$("body").trigger("OpenCompositeEdit");
				}
			});
		},

		highlightTermReferences: function(root, sectionIds) {
			let sectionIdSplit = [];
			sectionIds.forEach(s => sectionIdSplit = sectionIdSplit.concat(s.split(",")));
			sectionIdSplit.forEach(s => jq$(root).find("span[sectionid='" + s + "']").addClass("highlight"));
		},

		/**
		 * Function: createHomePage Used in the ObjectInfoToolProvider for
		 * creating homepages for KnowWEObjects
		 */
		createHomePage: function() {
			objectName = _KS('#objectinfo-src');
			if (objectName) {
				const params = {
					action: 'CreateObjectHomePageAction',
					objectname: objectName.innerHTML
				};

				const options = {
					url: KNOWWE.core.util.getURL(params),
					response: {
						action: 'none',
						fn: function() {
							window.location = "Wiki.jsp?page="
								+ objectName.innerHTML
						}
					}
				};
				new _KA(options).send();
			}

		},

		renameFunction: function() {
			KNOWWE.core.plugin.objectinfo.renameTerm(false);
		},

		/**
		 * Renames all occurrences of a specific term.
		 */
		renameTerm: function(forceRename) {
			if (forceRename == null)
				forceRename = false;
			// TODO shouldn't these 3 be vars?
			objectname = jq$('#objectinfo-target');
			replacement = jq$('#objectinfo-replacement');
			web = jq$('#objectinfo-web');
			if (objectname && replacement && web) {
				const changeNote = 'Renaming: "' + objectname.val() + '" -> "'
					+ replacement.val() + '"';
				const params = {
					action: jq$(replacement).attr('action'),
					termname: objectname.val(),
					termreplacement: replacement.val(),
					KWikiWeb: web.val(),
					KWikiChangeNote: changeNote,
					force: forceRename ? "true": "false"
				};
				const options = {
					url: KNOWWE.core.util.getURL(params),
					response: {
						action: 'none',
						fn: function() {
							const jsonResponse = JSON.parse(this.responseText);
							const alreadyexists = jsonResponse.alreadyexists;
							const same = jsonResponse.same;
							if (same === 'true') {
								alert('The term has not changed.');
							} else {
								if (alreadyexists === 'true') {
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
						onError: function() {
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
		lookUp: function(element) {
			if (!element) element = document;
			element = jq$(element);
			const terms = element.find('.objectinfo-terms');
			if (!terms.exists()) return;
			const response = terms.first().text();
			const termsJson = JSON.parse(response);
			const a = termsJson.allTerms;
			element.find('.objectinfo-search').autocomplete({
				source: a
			});
			element.find('.objectinfo-search').on(
				"autocompleteselect",
				function(event, ui) {
					KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(ui.item.value);
				});

			//Open "Show Info" on Enter key press only if term exists - otherwise do nothing
			element.find('.objectinfo-search').keyup(function(e) {
				if (e.keyCode === 13) {
					const val = jq$('.objectinfo-search').val();
					if (jq$.inArray(val, a) !== -1) {
						KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(val);
					}

				}
			});
		}
	}
}();

KNOWWE.plugin.renaming = function() {

	const sectionsCache = {};

	const otherOccurencesHashMap = {};

	let viewRoot = {};

	/**
	 * Renames all occurrences of a specific term.
	 */
	function renameTerms(oldValue, replacement, sectionId, forceRename) {
		if (forceRename == null)
			forceRename = false;
		if (oldValue && (replacement || replacement === "")) {
			const changeNote = 'Renaming: "' + oldValue + '" -> "'
				+ replacement + '"';
			const params = {
				action: "TermRenamingAction",
				termname: oldValue,
				termreplacement: replacement,
				sectionid: sectionId,
				force: forceRename ? "true": "false"
			};
			KNOWWE.core.util.updateProcessingState(1);
			jq$.ajax({
				type: "post", url: KNOWWE.core.util.getURL(params),
				success: function(data, text, request) {

					const jsonResponse = JSON.parse(data);
					const alreadyexists = jsonResponse.alreadyexists;
					const noForce = jsonResponse.noForce;
					const same = jsonResponse.same;
					if (same === 'true') {
						alert('The term has not changed.');
					} else {
						if (alreadyexists === 'true') {
							if (noForce === 'true') {
								alert('A term with this name already exists!');
								KNOWWE.core.util.reloadPage(request);
							} else if (confirm('A term with this name already exists, are you sure you want to merge both terms?')) {
								renameTerms(oldValue, replacement, sectionId, true);
							} else {
								KNOWWE.core.util.reloadPage(request);
							}
						} else {
							if (jsonResponse.objectinfopage === true) {
								window.location.href = "Wiki.jsp?page=ObjectInfoPage&objectname="
									+ encodeURIComponent(jsonResponse.newObjectName)
									+ "&termIdentifier="
									+ encodeURIComponent(jsonResponse.newTermIdentifier);
							} else {
								KNOWWE.helper.observer.notify("renamed");
								KNOWWE.core.util.reloadPage(request);
							}
						}
					}
				},

				error: function(request, status, error) {
					KNOWWE.core.util.updateProcessingState(-1);
					console.log(status, error);
					KNOWWE.core.util.reloadPage();
				}
			});
			//KNOWWE.core.util.updateProcessingState(1);
		}

	}

	function getRenamingInfo(sectionId, actionName, callback) {
		const params = {
			action: actionName,
			SectionID: sectionId
		};
		const options = {
			url: KNOWWE.core.util.getURL(params),
			response: {
				action: 'none',
				fn: function() {
					const jsonResponse = JSON.parse(this.responseText);
					callback(jsonResponse);


				},
				onError: function() {
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

		for (let occurence in otherOccurencesHashMap) {
			if (!otherOccurencesHashMap.hasOwnProperty(occurence)) continue;
			const section = sectionsCache[occurence];
			const parent = jq$(otherOccurencesHashMap[occurence]).parent();
			jq$(otherOccurencesHashMap[occurence]).parent().html(section);
			_TM.decorateToolMenus(parent);
		}

	}

	function afterCancelEdit(setting, original) {
		KNOWWE.tooltips.enrich(original);
		_TM.decorateToolMenus(jq$(original));
	}

	function showCurrentEditOnOtherOccurences(text) {
		for (let occurence in otherOccurencesHashMap) {
			if (!otherOccurencesHashMap.hasOwnProperty(occurence)) continue;
			jq$(otherOccurencesHashMap[occurence]).first().text(text);
		}
	}

	function saveOriginalsAndPrepareForEdit(lastPathElement) {
		for (let occurence in otherOccurencesHashMap) {
			if (!otherOccurencesHashMap.hasOwnProperty(occurence)) continue;
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
		} else if (jq$("#pagecontent").length > 0) {
			viewRoot = "#pagecontent ";
		} else {
			viewRoot = ".page-content "; // haddock template
		}

	}

	function initializeOtherOccurencesHashMap(sectionIds) {
		for (let i = 0; i < sectionIds.length; i++) {
			const sectionId = sectionIds[i];
			const toolMenuIdentifier = jq$(viewRoot + ".defaultMarkupFrame span[toolmenuidentifier=" + sectionId + "]");
			if (toolMenuIdentifier.length > 0) {
				otherOccurencesHashMap[sectionId] = toolMenuIdentifier[0].parentNode;
			}
		}
	}

	// noinspection JSUnusedGlobalSymbols
	return {
		renameTerm: function(sectionId, options) {
			setViewRoot();

			let actionName;
			if (options && options.actionName) {
				actionName = options.actionName;
			} else {
				actionName = "GetRenamingInfoAction"
			}

			getRenamingInfo(sectionId, actionName, function(jsonResponse) {

				if (!options) options = {
					toolMenuSection: sectionId,
					oldIdentifier: jsonResponse.termIdentifier,
				}

				const clickedTerm = jq$(viewRoot + "[toolmenuidentifier=" + options.toolMenuSection + "]").first().parent();
				clickedTerm.addClass("click");

				jq$(".click").editable(function(value) {
					renameTerms(jsonResponse.termIdentifier, value, sectionId, false);
					return value;
				}, {
					style: "inherit",
					onreset: cancelEdit,
					afterreset: afterCancelEdit,
					select: true

				});
				jq$('.click').trigger("click");
				//replace edit field value with sectionText for encoding reasons
				clickedTerm.find("input").val(jsonResponse.lastPathElement).select().autoGrowRenameField(5);

				initializeOtherOccurencesHashMap(jsonResponse.sectionIds);
				saveOriginalsAndPrepareForEdit(jsonResponse.lastPathElement);
				jq$(".click input").keyup(function() {
					showCurrentEditOnOtherOccurences(jq$(this).val());
				});
			});

		}

	}

}();

KNOWWE.core.plugin.renderKDOM = function() {

	jq$('.table_text').hover(function() {
			const that = this;
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
	const params = {
		action: 'SetMarkupActivationStatus',
		SectionID: id,
		status: status
	};
	const options = {
		url: KNOWWE.core.util.getURL(params),
		response: {
			action: 'none',
			fn: function() {
				window.location.reload();
			},
			onError: _EC.onErrorBehavior
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
		const anscestor = jq$(this).parents('.tooltipster');
		if (anscestor.exists()) {
			anscestor.removeAttr('title');
			anscestor.removeClass('tooltipster');
		}
	});
	element.find('.tooltipster').each(function() {
		let delay = jq$(this).attr('delay');
		if (!delay) delay = 1300;
		jq$(this).tooltipster({
			position: "top-left",
			interactive: true,
			multiple: true,
			delay: delay,
			contentAsHTML: true,
			updateAnimation: false,
			theme: ".tooltipster-knowwe",
			functionBefore: function(origin, continueTooltip) {
				// check if dom has changed since triggering... if source no longer exists, don't show tool tip
				if (!document.contains(origin[0])) {
					return;
				}
				// check if we have an ajax-tooltip
				// and only do once for each tooltip
				const src = origin.data('tooltip-src');
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
					success: function(json) {
						let html = json;
						if (_EC.isBlank(html)) {
							html = "No tooltip available";
						}
						try {
							const obj = jq$.parseJSON(json);
							if (jq$.isArray(obj)) html = obj[0];
						} catch (ignore) {
						}
						origin.tooltipster('update', html).tooltipster('reposition');
					},
					error: function(request, status, error) {
						KNOWWE.notification.error(error, "Cannot get tooltip content", src);
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
			expandable: true,
			clickableNodeNames: true,
			persist: true,
			article: jq$(this).closest(".defaultMarkupFrame").attr("id")
		});
	});
	KNOWWE.kdomtreetable.setOverflow();
};

KNOWWE.kdomtreetable.setOverflow = function() {
	jq$('.table_text').hover(function() {
		const elem = jq$(this);
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

KNOWWE.kdomtreetable.revealRenderKDOMTable = function(id) {
	const treetable = jq$('.renderKDOMTable.wikitable.treetable')[0];
	const markedtitle = jq$(treetable).find('td[style="color: rgb(0, 0, 255);"]');
	if (typeof markedtitle != "undefined" || markedtitle != null) {
		for (let i = 0; i < markedtitle.size(); i++) {
			jq$(markedtitle[i]).removeAttr("style");
		}
	}

	const tablerow = jq$(treetable).find('tr[data-tt-id="kdom-row-' + id + '"]')[0];
	jq$(treetable).treetable("reveal", "kdom-row-" + id);
	jq$(tablerow).find("td").first().css("color", "rgb(0, 0, 255)");
	jq$('html, body').animate({
		scrollTop: (jq$(tablerow).offset().top - 250)
	}, 400);
};

KNOWWE.kdomtreetable.collapseAll = function() {
	const treetable = jq$('.renderKDOMTable.wikitable.treetable')[0];
	jq$(treetable).treetable("collapseAll");
};

KNOWWE.core.plugin.attachment = function() {

	return {

		update: function(sectionId) {
			const params = {
				action: 'AttachmentUpdateAction',
				SectionID: sectionId
			};

			const options = {
				url: KNOWWE.core.util.getURL(params),
				fn: function() {
					window.location.reload();
				}
			};
			new _KA(options).send();
		}
	}
}();

/**
 * Namespace: KNOWWE.core.plugin.pagination The KNOWWE plugin d3web namespace.
 */
KNOWWE.core.plugin.pagination = function() {

	let windowHeight;

	function saveCookieAndUpdateNode(cookie, id) {
		saveCookie(cookie, id);
		updateNode(id);
	}

	function saveCookie(cookie, id) {
		const cookieStr = JSON.stringify(cookie);
		jq$.cookie("PaginationDecoratingRenderer-" + id, cookieStr);
	}

	function updateNode(id) {
		jq$('#' + id).rerender();
	}

	function scrollToTopNavigation(id) {
		jq$('html, body').animate({
			scrollTop: jq$("#" + id).offset().top
		}, 0);
	}

	function getSortingSymbol(naturalOrder, index) {
		let cssClass;
		if (naturalOrder) {
			cssClass = "fa fa-caret-down fa-lg";
		} else {
			cssClass = "fa fa-caret-up fa-lg";
		}
		if (index !== 0) cssClass += " secondary";
		return jq$('<i/>', {
			"class": cssClass
		});
	}

	function renderSortingSymbols(sectionId, sortingMode) {
		const cookie = jq$.cookie("PaginationDecoratingRenderer-" + sectionId);
		if (!cookie) return;
		const parsedCookie = jq$.parseJSON(cookie);
		if (!parsedCookie || !parsedCookie.sorting) return;
		let sortLength;
		if (sortingMode === 'multi') {
			sortLength = (parsedCookie.sorting).length;
		} else {
			sortLength = 1;
		}
		for (let i = 0; i < sortLength; i++) {
			const sortingSymbolParent = jq$("[pagination=" + sectionId + "] th:contains('" + parsedCookie.sorting[i].sort + "') span");
			const sortingSymbol = jq$(sortingSymbolParent).find("i");
			if (sortingSymbol.exists()) {
				sortingSymbol.replaceWith(getSortingSymbol(parsedCookie.sorting[i].naturalOrder, i));
			} else {
				jq$(sortingSymbolParent).append(getSortingSymbol(parsedCookie.sorting[i].naturalOrder, i));
			}
		}
	}

	function enableSorting(sectionId) {
		jq$(this.firstChild).wrap('<span></span>');
		if (!jq$(this).hasClass("notSortable")) {
			jq$(this).addClass("sortable");
			jq$(this).find("span").bind('click',
				function() {
					KNOWWE.core.plugin.pagination.sort(this, sectionId);
				}
			);
		}
	}

	function prepareFilterableElements() {
		const filterIcon = jq$('<i/>', {
			"class": 'fa fa-filter knowwe-filter'
		});
		jq$(this).prepend(filterIcon);
		const text = jq$(this).text();
		const preparedFilter = jq$(".paginationFilters div[filtername=" + text + "]").detach();
		jq$(filterIcon).tooltipster({
			content: jq$(preparedFilter),
			interactive: true,
			interactiveTolerance: 500,
			theme: "tooltipster-knowwe"
		});
	}

	function isShortTable() {
		return (jq$(this).height() < jq$(window).height());
	}

	function toggleLowerPagination(visibility) {
		const id = jq$(this).attr("pagination");
		jq$(".knowwe-paginationToolbar[pagination=" + id + "]").slice(2, 4).css("display", visibility);
	}

	function handlePaginationBelowTableVisibility() {
		if (isShortTable.call(this)) {
			toggleLowerPagination.call(this, "none");
		} else {
			toggleLowerPagination.call(this, "inline-block")
		}
	}

	jq$(window).resize(function() {
		if (windowHeight !== jq$(window).height()) {
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
			const sectionId = jq$(this).attr('pagination');

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

			const sortingMode = jq$(this).attr('sortable');

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

	function readCookie(id) {
		let cookieValue = jq$.cookie("PaginationDecoratingRenderer-" + id);
		if (!cookieValue) cookieValue = "{}";
		return cookie = jq$.parseJSON(cookieValue);
	}

	return {

		sort: function(element, id) {
			const cookie = readCookie(id);
			const columnName = jq$(element).text();
			let sorting;
			if (typeof cookie.sorting == "undefined") {
				sorting = [{sort: columnName, naturalOrder: true}];
			} else {
				sorting = cookie.sorting;
				let found = false;
				let remove = false;
				let i = 0
				for (; i < sorting.length; i++) {
					if (sorting[i].sort === columnName) {
						if (i === 0) { // we only toggle sorting when primary sort column
							if (sorting[i].naturalOrder) { // clicked second time, reverse order
								sorting[i].naturalOrder = false;
							} else { // clicked third time, remove (not sorting this column)
								remove = true;
							}
						} else {
							sorting.move(i, 0);
						}
						found = true;
						break;
					}
				}
				if (remove) {
					sorting.splice(i, 1);
				}
				if (!found) {
					sorting.unshift({sort: columnName, naturalOrder: true});
				}
			}
			cookie.sorting = sorting;
			saveCookieAndUpdateNode(cookie, id);
		},

		setCount: function(selected, id) {
			const $selected = jq$(selected);

			const cookie = readCookie(id);

			const lastCount = parseInt(cookie.count);
			const resultSize = parseInt(jq$('#' + id + " .resultSize").val());
			const count = $selected.val();
			$selected.data('current', count);
			let startRow = parseInt(jq$('div[pagination=' + id + '] .startRow').val());
			const search = /^\d+$/;
			const found = search.test(startRow);
			if (!(found)) {
				jq$('div[pagination=' + id + '] .startRow').val('');
				return;
			}

			if (count === "Max") {
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

		navigate: function(id, direction) {

			const count = jq$("#" + id + " .count").val();
			let startRow = jq$("#" + id + " .startRow").val();
			const resultSize = jq$("#" + id + " .resultSize").val();

			if (count === "All") {
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


			const cookie = readCookie(id);
			cookie.startRow = startRow;
			cookie.count = count;
			saveCookieAndUpdateNode(cookie, id);

		},

		updateStartRow: function(selectedRow, sectionId, preventRerender) {

			const id = sectionId;
			const cookie = readCookie(id);
			const count = jq$("#" + id + " .count").val();
			let startRow = selectedRow.value;
			const search = /^\d+$/;
			const found = search.test(startRow);
			if (!(found)) {
				jq$("#" + id + " .startRow").val('');
				return;
			}
			if (startRow <= 0) {
				startRow = 1;
			}
			if (count === "Max") {
				cookie.startRow = 1;
				cookie.count = "Max";
			} else {
				cookie.startRow = startRow;
				cookie.count = count;
			}
			saveCookie(cookie, id);
			if (!preventRerender) updateNode(id);
		},

		filter: function(checkbox, sectionId) {
			const key = jq$(checkbox).attr("filterkey");
			const value = jq$(checkbox).attr("filtervalue");
			const checked = checkbox.checked;

			const cookie = readCookie(id);
			if (typeof cookie.filters == "undefined") {
				cookie.filters = {};
				cookie.filters[key] = [];
				if (checked === true) {
					cookie.filters[key].push(value);
				} else {
					cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
				}
			} else if (typeof cookie.filters[key] == "undefined") {
				cookie.filters[key] = [];
				if (checked === true) {
					cookie.filters[key].push(value);
				} else {
					cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
				}
			} else {
				if (checked === true) {
					cookie.filters[key].push(value);
				} else {
					cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
				}
			}
			saveCookieAndUpdateNode(cookie, sectionId);
		},

		decorateTable: function() {
			handleNoResult(jq$(this));
			const id = jq$(this).find(".knowwe-paginationToolbar").first().attr('pagination');
			jq$(this).find("table").attr('pagination', id);
			jq$(this).find("table[pagination]").each(decorate());
			KNOWWE.helper.observer.notify("paginationTableDecorated");
		},

		decorateTables: function() {
			handleNoResult(jq$(this));
			const wrappers = jq$("div.knowwe-paginationWrapper");
			wrappers.each(function() {
				jq$(this).find("table").attr('pagination', jq$(this).attr('id'));
			});

			jq$("table[pagination]").each(decorate());
		}
	}
}();

KNOWWE.core.plugin.formatterAjax = function(id, actionClass) {

	const textarea = jq$("#defaultEdit" + id);
	const wikiText = textarea.val();

	jq$.ajax("action/" + actionClass, {
		data: {
			sectionID: id,
			wikiText: wikiText
		},
		type: 'post',
		cache: false,
		success: function(json) {
			textarea.val(json.wikiText);
		}
	});

	return;

}

KNOWWE.core.plugin.reloadNamespaceFile = function() {

	return {

		reloadFile: function(namespaceUrl, filename, title) {
			jq$.ajax("action/NamespaceFileReloadAction", {
				type: 'post',
				data: {
					namespaceUrl: namespaceUrl,
					filename: filename,
					title: title
				},
				success: function() {
					KNOWWE.notification.success("Success", "You successfully reloaded the namespace file in the attachment.", filename);
					//KNOWWE.core.util.reloadPage();

				},
				error: function() {
					KNOWWE.notification.loadNotifications();
				}
			});
		}
	}
}();

//jquery-autogrow for automatic input field resizing (customized for KnowWE renaming)

(function(jq$) {
	let inherit;
	inherit = ['font', 'letter-spacing'];
	return jq$.fn.autoGrowRenameField = function(options) {
		let comfortZone, remove, _ref;
		remove = (options === 'remove' || options === false) || !!(options != null ? options.remove: void 0);
		comfortZone = (_ref = options != null ? options.comfortZone: void 0) != null ? _ref: options;
		if (comfortZone != null) {
			comfortZone = +comfortZone;
		}
		return this.each(function() {
			let check, cz, input, growWithSpan, prop, styles, testSubject, _i, _j, _len, _len1;
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
					position: 'absolute',
					top: -99999,
					left: -99999,
					width: 'auto',
					visibility: 'hidden'
				};
				for (_j = 0, _len1 = inherit.length; _j < _len1; _j++) {
					prop = inherit[_j];
					styles[prop] = input.css(prop);
				}
				testSubject = jq$('<div class="autogrow"/>').css(styles);
				testSubject.insertAfter(input);
				cz = comfortZone != null ? comfortZone: 70;
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
})(typeof Zepto !== "undefined" && Zepto !== null ? Zepto: jQuery);


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
		});
	}
	jq$(window).on('hashchange', function() {
		KNOWWE.core.plugin.objectinfo.highlighAnchor();
	});
}());

KNOWWE.helper.observer.subscribe("afterRerender", function() {
	KNOWWE.tooltips.enrich(this);
	KNOWWE.core.plugin.objectinfo.lookUp(this);
	KNOWWE.core.plugin.pagination.decorateTable.call(this);
});

