/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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
KNOWWE.core = KNOWWE.core || {};
KNOWWE.core.plugin = KNOWWE.core.plugin || {};

/**
 * Namespace: KNOWWE.core.plugin.rightPanel for debugging d3web expressions in KnowWE
 */
KNOWWE.core.plugin.rightPanel = function () {

	const rightPanelStorageKey = "rightPanel";
	const rightPanelWidthStorageKey = "knowwe.rightPanel.width";
	const activeTabStorageKey = "knowwe.rightPanel.activeTab";
	const defaultRightPanelWidth = 300;
	const minRightPanelWidth = 300;

	let rightPanel = null;

	let showSidebar = false;

	let globalFloatingTime = 500;

	let initScrolling = null;

	let windowWidth;

	let isOnBottom = false;

	jq$(window).resize(function () {
		if (showSidebar) {
			windowWidth = jq$(window).width();

			if (windowWidth < 600) {
				KNOWWE.core.plugin.rightPanel.moveToBottom();
				jq$(KNOWWE.core.util.getPageContentSelector()).css("margin-right", "0");
			} else {
				KNOWWE.core.plugin.rightPanel.moveToRight();
			}
			rightPanelScroll();
		}
	});

	function getRightPanelMaxWidth() {
		const maxWidth = jq$(window).width() / 2;
		return Math.max(minRightPanelWidth, Math.floor(maxWidth));
	}

	function clampRightPanelWidth(width) {
		return Math.max(minRightPanelWidth, Math.min(getRightPanelMaxWidth(), width));
	}

	function getStoredRightPanelWidth() {
		const storedWidth = parseInt(localStorage.getItem(rightPanelWidthStorageKey), 10);
		if (!isNaN(storedWidth)) return clampRightPanelWidth(storedWidth);
		return defaultRightPanelWidth;
	}

	function getRightPanelWidth() {
		if (rightPanel && rightPanel.length && !isOnBottom) {
			const currentWidth = rightPanel[0].getBoundingClientRect().width;
			if (currentWidth) return clampRightPanelWidth(currentWidth);
		}
		return getStoredRightPanelWidth();
	}

	function updatePageForRightPanelWidth(width) {
		if (!isOnBottom) {
			jq$(KNOWWE.core.util.getPageContentSelector()).css("margin-right", width + "px");
		}
	}

	function setRightPanelWidth(width, persist) {
		const clampedWidth = clampRightPanelWidth(width);
		rightPanel.css("width", clampedWidth + "px");
		updatePageForRightPanelWidth(clampedWidth);
		if (persist) {
			localStorage.setItem(rightPanelWidthStorageKey, clampedWidth);
		}
		return clampedWidth;
	}

	function getSelected() {
		let t = '';
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
		if (showSidebar) {
			//Calculate user's scroll position from bottom
			const scrollPosition = window.pageYOffset;
			const windowSize = window.innerHeight;
			const bodyHeight = document.body.offsetHeight;
			const distToBottom = Math.max(bodyHeight - (scrollPosition + windowSize), 0);
			const footerHeightVisible = Math.max(jq$('.footer').first().outerHeight() - distToBottom, 0);

			if (isOnBottom) {
				//Haddock Template on the bottom
				jq$('#rightPanel').css({
					bottom: footerHeightVisible + 'px'
				});
			} else {
				// HaddockTemplate on the right
				let $header = jq$('.header');
				let $navigation = $header.find('.navigation');
				let isFixedHeader = $header.hasClass("scrolling-down");
				let position = isFixedHeader ? "fixed" : "absolute";
				let top = isFixedHeader ? $navigation.height() : 0;
				let height = document.documentElement.clientHeight - ($navigation.position().top + $navigation.height() + footerHeightVisible);
				jq$('#rightPanel').css({position: position, top: top + 'px', height: height + 'px'});
			}
		}
	}


	function moveRightPanelToBottom() {
		isOnBottom = true;
		rightPanel.removeClass("right-panel-right").addClass("right-panel-bottom");
		rightPanel.css({
			position: 'fixed',
			width: '100%',
			top: 'auto',
			right: 'auto'
		});
		rightPanelScroll();
		jq$(KNOWWE.core.util.getPageContentSelector()).css('height', 'auto');
		const currentHeight = jq$(KNOWWE.core.util.getPageContentSelector).height();
		const rightPanelHeight = jq$('#rightPanel').height();
		jq$(KNOWWE.core.util.getPageContentSelector()).css('height', (currentHeight + rightPanelHeight) + 'px');
	}

	function moveRightPanelToRight() {
		isOnBottom = false;
		rightPanel.removeClass("right-panel-bottom").addClass("right-panel-right");
		rightPanel.css({
			bottom: '0',
			right: '0'
		});
		setRightPanelWidth(getStoredRightPanelWidth(), false);
		rightPanelScroll();
		jq$(KNOWWE.core.util.getPageContentSelector()).css('height', 'auto');
	}

	function makeRightPanelResizable() {
		if (rightPanel.find(".right-panel-resize-handle").length) return;

		const $handle = jq$("<div class=\"right-panel-resize-handle\"></div>").prependTo(rightPanel);
		let dragging = false;
		let startX = 0;
		let startWidth = 0;

		$handle.on("mousedown", function(e) {
			if (isOnBottom) return;

			const panelRect = rightPanel[0].getBoundingClientRect();
			dragging = true;
			startX = e.clientX;
			startWidth = panelRect.width;
			jq$("body").addClass("right-panel-resizing");
			e.preventDefault();
		});

		jq$(document).off(".rightPanelResize");
		jq$(document).on("mousemove.rightPanelResize", function(e) {
			if (!dragging) return;

			const delta = startX - e.clientX;
			setRightPanelWidth(startWidth + delta, false);
			rightPanelScroll();
			e.preventDefault();
		});

		jq$(document).on("mouseup.rightPanelResize", function() {
			if (!dragging) return;

			dragging = false;
			jq$("body").removeClass("right-panel-resizing");
			setRightPanelWidth(rightPanel[0].getBoundingClientRect().width, true);
		});
	}

	function floatRightPanel() {

		showSidebar = true;

		if (!isOnBottom) {
			rightPanelScroll();
			rightPanel.animate({'right': '0'}, globalFloatingTime, function () {
				jq$(window).resize();
			});
		} else {
			rightPanel.animate({'bottom': '0'}, globalFloatingTime, function () {
				jq$(window).resize();
			});
		}

		initScrolling = jq$('.navigation').offset().top;

		//make sidebar resizable
		makeRightPanelResizable();
	}

	function shrinkPage() {
		isOnBottom = jq$(window).width() < 600;
		const rightPanelWidth = getStoredRightPanelWidth();
		if (!isOnBottom) {
			jq$(KNOWWE.core.util.getPageContentSelector()).animate({'margin-right': rightPanelWidth + "px"}, globalFloatingTime);
		}
	}

	function growPage() {
		if (!isOnBottom) {
			jq$(KNOWWE.core.util.getPageContentSelector()).animate({'margin-right': '0'}, globalFloatingTime);
			rightPanel.animate({'right': -getRightPanelWidth() + 'px'}, globalFloatingTime, function () {
				removeRightPanel();
			});
		} else {
			rightPanel.animate({'bottom': -rightPanel.height() + 'px'}, globalFloatingTime, function () {
				removeRightPanel();
			});
		}
	}

	function hideMountedPanel() {
		jq$(document).off(".rightPanelResize");
		jq$("body").removeClass("right-panel-resizing");
		if (rightPanel && rightPanel.length) {
			rightPanel.find(".right-panel-resize-handle").remove();
			rightPanel.removeAttr("style").attr("hidden", "");
		}
	}

	function removeRightPanel() {
		hideMountedPanel();
		isOnBottom = false;
		jq$(window).resize();
	}

	function locateScaffold() {
		const $all = jq$('#rightPanel');
		if ($all.length > 1) {
			$all.slice(1).remove();
		}
		return jq$('#rightPanel').first();
	}

	function addHideBar() {
		if (rightPanel.find('.rightpanelhide').length) return;
		const rightPanelHide = jq$('<div/>', {'class': 'rightpanelhide'});
		rightPanelHide.append(jq$('<span/>', {'text': 'Hide'}));
		rightPanelHide.append(jq$('<img/>', {'src': 'KnowWEExtension/images/arrow_right.png'}));
		rightPanel.prepend(rightPanelHide);
	}

	function mountRightPanel() {
		rightPanel = locateScaffold();
		if (!rightPanel.length) return false;

		const $parent = jq$(KNOWWE.core.util.getPageSelector());
		if ($parent.length && !rightPanel.parent().is($parent)) {
			rightPanel.appendTo($parent);
		}

		const scrollTop = jq$(window).scrollTop();
		const panelWidth = getStoredRightPanelWidth();
		const width = isOnBottom ? '100%' : panelWidth + 'px';
		const right = isOnBottom ? 'auto' : -panelWidth + 'px';
		const position = isOnBottom ? 'fixed' : 'absolute';
		rightPanel
			.removeClass('right-panel-right right-panel-bottom')
			.addClass(isOnBottom ? 'right-panel-bottom' : 'right-panel-right')
			.css({
				'position': position,
				'right': right,
				'width': width,
				'overflow-x': 'hidden',
				'overflow-y': 'hidden'
			});

		addHideBar();

		if (isOnBottom) {
			rightPanel.css('bottom', '-' + jq$('#rightPanel').height());
		} else {
			rightPanel.css('top', (-scrollTop) + 'px');
		}

		rightPanel.removeAttr('hidden');
		return true;
	}

	function tabButton(id) {
		return rightPanel.find(".right-panel-tablist button").filter(function () {
			return this.getAttribute("data-tab") === id;
		});
	}

	function tabBody(id) {
		return rightPanel.find(".right-panel-tab").filter(function () {
			return this.getAttribute("data-tab") === id;
		});
	}

	function dispatchTabEvent(name, id, body) {
		document.dispatchEvent(new CustomEvent(name, {detail: {id: id, body: body}}));
	}

	function ensureInitialized(id, body) {
		if (!body || body.getAttribute("data-initialized") === "true") return;
		body.setAttribute("data-initialized", "true");
		dispatchTabEvent("rightPanelTabInitialized", id, body);
	}

	function fetchLazyBody(id, $body, onDone) {
		jq$.ajax({
			url: KNOWWE.core.util.getURL({action: 'GetRightPanelTabContentAction', tab: id}),
			cache: false,
			type: 'post'
		}).done(function (html) {
			$body.html(html);
			$body.attr("data-loaded", "true");
			onDone();
		}).fail(_IE.onErrorBehavior);
	}

	function activateTab(id) {
		const $btn = tabButton(id);
		if (!$btn.length) return;
		const $body = tabBody(id);
		const lazy = $btn.is("[data-lazy]");

		function show() {
			rightPanel.find(".right-panel-tablist button").removeAttr("data-active");
			$btn.attr("data-active", "");
			rightPanel.find(".right-panel-tab").attr("hidden", "");
			$body.removeAttr("hidden");
			localStorage.setItem(activeTabStorageKey, id);
			dispatchTabEvent("rightPanelTabShown", id, $body[0]);
		}

		if (lazy && $body.attr("data-loaded") !== "true") {
			fetchLazyBody(id, $body, function () {
				ensureInitialized(id, $body[0]);
				show();
			});
		} else {
			ensureInitialized(id, $body[0]);
			show();
		}
	}

	function initRightPanelTabs() {
		const $tablist = rightPanel.find(".right-panel-tablist");

		$tablist.off("click.rightPanelTabs").on("click.rightPanelTabs", "button[data-tab]", function () {
			activateTab(this.getAttribute("data-tab"));
		});

		// eagerly initialize every non-lazy body, even ones never opened (decision 7a)
		rightPanel.find(".right-panel-tab").each(function () {
			const id = this.getAttribute("data-tab");
			if (!tabButton(id).is("[data-lazy]")) {
				ensureInitialized(id, this);
			}
		});

		// restore the persisted active tab if it still exists, else fall back to the scaffold default
		const stored = localStorage.getItem(activeTabStorageKey);
		let activeId = (stored && tabBody(stored).length) ? stored : null;
		if (!activeId) {
			const $default = $tablist.find("button[data-active]").first();
			const $first = $default.length ? $default : $tablist.find("button[data-tab]").first();
			activeId = $first.attr("data-tab");
		}
		if (activeId) activateTab(activeId);
	}

	function setRightPanelCookie(b) {
		let storage = simpleStorage.get(rightPanelStorageKey);
		if (typeof storage == 'undefined') {
			storage = {}
		}

		else {
			simpleStorage.deleteKey(rightPanelStorageKey);
		}
		simpleStorage.set(rightPanelStorageKey, b);

	}

	function bindCollapseIcons() {
		jq$("#rightPanel").off("click.rightPanelCollapse").on("click.rightPanelCollapse", ".tool .topbar", function () {
			if (jq$(this).find("i").hasClass("fa-caret-down")) {
				jq$(this).find("i").removeClass("fa-caret-down").addClass("fa-caret-right");
				jq$(this).parent().find(".right-panel-content").first().slideUp();
			}
			else {
				if (jq$(this).find("i").hasClass("fa-caret-right")) {
					jq$(this).find("i").removeClass("fa-caret-right").addClass("fa-caret-down");
					jq$(this).parent().find(".right-panel-content").first().slideDown();
				}
			}
		});
	}


	function bindHideFunctions() {
		jq$(KNOWWE.core.util.getMoreButtonSelector() + " .watches").unbind();
		bindHideInPanel();
		bindHideInMoreMenu();


		function bindHideInMoreMenu() {
			jq$(KNOWWE.core.util.getMoreButtonSelector() + " .watches").prop("title", "Hide Right Panel");
			jq$(KNOWWE.core.util.getMoreButtonSelector() + " .watches").on("click", function () {
				terminateRightPanel();
			});
			jq$(KNOWWE.core.util.getMoreButtonSelector() + " .watches").text("Hide Right Panel");
		}


		function bindHideInPanel() {
			jq$("#rightPanel").off("click.rightPanelHide").on("click.rightPanelHide", ".rightpanelhide", function () {
				terminateRightPanel();
			})
		}
	}

	function changeHideToShow() {
		jq$(KNOWWE.core.util.getMoreButtonSelector() + " .watches").prop("title", "Show Right Panel");
		jq$(KNOWWE.core.util.getMoreButtonSelector() + " .watches").unbind();
		jq$(KNOWWE.core.util.getMoreButtonSelector() + " .watches").on("click", function () {
			KNOWWE.plugin.core.rightPanel.showRightPanel();
		});
		jq$(KNOWWE.core.util.getMoreButtonSelector() + " .watches").text("Show Right Panel");
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
			if (!mountRightPanel()) {
				showSidebar = false;
				globalFloatingTime = 500;
				return;
			}
			floatRightPanel();
			setRightPanelCookie(true);
			bindUiActions();
			initRightPanelTabs();
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
			'id': id,
			'class': 'tool',
			'css': {
				'position': 'relative'
			}
		});
	}

	function buildTopBar(title) {
		const toolTopbar = jq$('<div/>', {
			'class': 'topbar'
		});
		const collapseIcon = jq$('<i/>', {
			'class': 'collapseicon fa fa-fw fa-caret-down'
		});
		const toolTitle = jq$('<div/>', {
			'class': 'title',
			'text': title
		});
		toolTitle.prepend(collapseIcon);
		toolTopbar.append(toolTitle);
		return toolTopbar;
	}

	function appendNewToolToRightPanel(tool, topbar, div) {
		tool.append(topbar);
		tool.append(div);
		const $active = rightPanel.find(".right-panel-tab:not([hidden])").first();
		($active.length ? $active : rightPanel).append(tool);
	}

	function buildToolContent(pluginDiv) {
		const content = jq$('<div/>', {
				'class': 'right-panel-content'
			}
		);
		content.append(pluginDiv);
		return content;
	}

	function isRightPanelShown() {
		return simpleStorage.get(rightPanelStorageKey) === true;
	}

	function initRightPanelToggleButton(isShown) {
		const orientation = (isShown ? "right" : "left");
		const status = (isShown ? "Hide" : "Show");
		jq$(KNOWWE.core.util.getMoreButtonSelector()).after("<li class='rightPanel-li'><a id='rightPanel-toggle-button' title='" + status + " right panel'"
			+ " class='action " + (KNOWWE.helper.isFontAwesomeProAvailable() ? "fa-regular" : "fa-solid") + " fa-angles-" + orientation + "'></a></li>");
		bindRightPanelToggleButton();
	}

	function bindRightPanelToggleButton() {
		jq$('#rightPanel-toggle-button').unbind('click').click(function () {
			const $this = jq$(this);
			if (isRightPanelShown()) {
				KNOWWE.core.plugin.rightPanel.hideRightPanel();
				$this.removeClass("fa-angles-right").addClass("fa-angles-left").attr("title", "Show right panel");
			} else {
				KNOWWE.core.plugin.rightPanel.showRightPanel();
				$this.removeClass("fa-angles-left").addClass("fa-angles-right").attr("title", "Hide right panel");
			}
		});
	}

	return {

		showRightPanel: function () {
			initRightPanel(false);
		},

		hideRightPanel: function () {
			terminateRightPanel();
		},

		init: function () {
			rightPanel = locateScaffold();
			if (!rightPanel.length) return;

			const isShown = isRightPanelShown();
			if (isShown) {
				initRightPanel(true);
			}
			initRightPanelToggleButton(isShown);
			jq$(window).scroll(function() {
				// wait for animation frame since we check for changes also done on scroll... we make sure we see these changes
				window.requestAnimationFrame(rightPanelScroll);
			});
			jq$('.header').on("transitionend", rightPanelScroll);
			setTimeout(rightPanelScroll, 0);
		},

		addToolToRightPanel: function (title, id, pluginDiv) {
			const tool = buildToolContainer(id);
			const topbar = buildTopBar(title);
			const content = buildToolContent(pluginDiv);
			appendNewToolToRightPanel(tool, topbar, content);
		},

		moveToBottom: function () {
			moveRightPanelToBottom();
		},

		moveToRight: function () {
			moveRightPanelToRight()
		},

		isShownOnRight: function () {
			return isRightPanelShown() && !isOnBottom;
		},

		setTabEnabled: function (id, enabled) {
			if (!rightPanel || !rightPanel.length) return;
			const $btn = tabButton(id);
			const $body = tabBody(id);
			if (enabled) {
				$btn.removeAttr("hidden");
			} else {
				$btn.attr("hidden", "");
				$body.attr("hidden", "");
			}
		}

	}

}
();

// The Custom tab is server-rendered inline by CustomContentTabProvider (decision H), so it needs no
// client hydration: any interactive markup inside the RightPanel article is wired up by the normal
// page-load init. The former GetRightPanelContentAction AJAX module was removed with the action.

KNOWWE.core.plugin.rightPanel.watches = function () {

	const watchesStorageKey = "watches";

	let watchesArray;

	let watches;

	const restorableEntries = {};

	let watchlist;

	function bindUiActions() {
		watches.on("click", ".watchlistentry", function (e) {
			editWatch(this);
		});
		watches.on("click", ".addwatch", function (e) {
			addWatch();
		});
		watches.on("click", ".fromselection", function (e) {
			addWatchFromSelection();
		});
		watches.on("keydown", "textarea", function (e) {
			handleTextarea(this, e);
		});
		watches.on("click", ".deletewatch", function (e) {
			e.stopPropagation();
			removeWatch(this);
		});
	}

	function handleResponse(data) {
		const expressionArrays = data.values;
		const oldEntries = watchlist.find(".watchlistentry");
		jq$.each(expressionArrays, function (index, value) {
			const newEntry = createNewEntry(watchesArray[index], value);
			jq$(oldEntries[index]).replaceWith(newEntry);
		});
		enableAddWatch();
	}

	function updateOldWatchesList(data) {
		handleResponse(data);
	}

	function updateWatches() {
		getExpressionValue(watchesArray).done(function (data) {
			updateOldWatchesList(data);
		});


	}

	function enableAddWatch() {
		if (watches.find(".addwatch").prop("disabled") === true && watches.find(".newwatch").length === 0) {
			watches.find(".addwatch").prop("disabled", false);
			watches.find(".fromselection").prop("disabled", false);
		}
	}

	function disableAddWatch() {
		if (watches.find(".addwatch").prop("disabled") === false) {
			watches.find(".addwatch").prop("disabled", true);
			watches.find(".fromselection").prop("disabled", true);
		}
	}

	function addWatch(text) {
		const textarea = createTextarea(null, text);
		watchlist.append(textarea);
		jq$(textarea).find("textarea").focus();
		//allow only one new textarea - disable Add Watch
		disableAddWatch();
	}

	function addWatchFromSelection() {
		const text = getSelectionText();
		addWatch(text);
	}

	function removeWatch(that) {
		const index = getWatchesIndex(jq$(that).parent());
		watchesArray.splice(index, 1);
		jq$(that).parent().remove();
		updateCookies();
	}

	function saveOldEntry(index, that) {
		const oldEntry = jq$(that);
		restorableEntries[index] = oldEntry;
	}

	function editWatch(that) {
		const index = getWatchesIndex(that);
		saveOldEntry(index, that);
		watchesArray.splice(index, 1);
		const textarea = createTextarea(that);
		jq$(that).replaceWith(textarea);
		jq$(textarea).find("textarea").focus();
		//allow only one new textarea - disable Add Watch
		disableAddWatch();
	}

	function restoreEntry(entry, watchesIndex) {
		const restorableEntry = restorableEntries[watchesIndex];
		const restorableExpression = jq$(restorableEntry).find(".expression").text();
		watchesArray.splice(watchesIndex, 0, restorableExpression);
		jq$(entry).replaceWith(restorableEntry);
		delete restorableEntries[watchesIndex];
	}

	function handleTextarea(that, e) {
		const entry = jq$(that).parent();
		const watchesIndex = getWatchesIndex(entry);

		//escape
		if (e.keyCode === 27) {
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

		if (jq$(that).data('ui-tooltip') && jq$(that).val().trim() !== "") {
			jq$(that).tooltip("destroy");
			jq$(that).attr("title", null);
		}

		const trimmedValue = jq$(that).val().trim();
		//shift+enter = newline, enter=submit
		if (e.keyCode === 13 && !e.shiftKey) {
			if (trimmedValue === "") {
				jq$(that).tooltip({position: {my: "right bottom", at: "left top"}});
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
		const data = {expressions: expr, page: KNOWWE.helper.gup('page'), id: id};
		return jq$.ajax({
			type: 'post',
			url: 'action/GetExpressionValueAction',
			data: JSON.stringify(data),
			cache: false,
			contentType: 'application/json; charset=UTF-8'
		});
	}


	function createTextarea(that, text) {
		const watchesNewEntry = jq$('<div/>', {
			'class': 'newwatch watchlistline'
		});
		const textarea = jq$('<textarea>', {});

		const textareaDom = textarea[0];

		if (typeof AutoComplete != "undefined") {
			new AutoComplete(textareaDom, function (callback, prefix) {
				const scope = "$d3web/condition";
				const data = {prefix: prefix, scope: scope};
				if (KNOWWE && KNOWWE.helper) {
					data.KWiki_Topic = KNOWWE.helper.gup('page');
				}
				jq$.ajax({
					url: 'action/CompletionAction',
					cache: false,
					data: data
				}).done(function (data) {
					callback(eval(data));
				});
			});
		}

		if (that) {
			const oldEntry = restorableEntries[getWatchesIndex(that)];
			const oldText = jq$(oldEntry).find(".expression").text();
			textarea.val(oldText);
		}

		if (typeof text != 'undefined') {
			textarea.val(text);
		}
		textarea.autosize({minHeight: "22px"});
		watchesNewEntry.append(textarea);
		return watchesNewEntry;
	}

	function createWatchesEntryValueSpan(value) {
		return jq$('<span/>', {
			'class': 'value tooltip',
			'text': value.value,
			'title': value.kbname
		});
	}


	function createWatchesEntryHistoryValueSpan(title) {
		return jq$('<span/>', {
			'class': 'value tooltip history',
			'title': title
		});
	}

	function handleDefaultResponse(watchesEntry, responseObject) {

		jq$.each(responseObject.kbsEntries, function iterateValuesFromDifferentKbs(index, value) {
			const watchesEntryValue = createWatchesEntryValueSpan(value);
			const tooltipcontent = jq$('<span style="padding-right: 5px" class="fa fa-book"></span><span>' + value.kbname + '  </span>');
			jq$(watchesEntryValue).tooltipster({
				content: tooltipcontent,
				position: "top-left",
				delay: 300,
				theme: ".tooltipster-knowwe"
			});
			watchesEntry.append(watchesEntryValue);
		});

		return watchesEntry;

	}

	function handleHistoryResponse(watchesEntry, responseObject) {

		jq$.each(responseObject.kbsEntries, function iterateValuesFromDifferentKbs(index, value) {
			const watchesEntryValue = createWatchesEntryHistoryValueSpan(value.kbname);

			jq$.each(value.value, function iterateValuesInHistory(index, value) {
				const historyEntrySpan = jq$('<span/>', {
					'class': 'value tooltip historyentry',
					'text': value.value
				});
				createTimestampsToolTip.call(this, historyEntrySpan);
				watchesEntryValue.append(historyEntrySpan);

			});
			watchesEntry.append(watchesEntryValue);
		});

		function createTimestampsToolTip(historyEntrySpan) {
			const start = this.timestamps[0];
			const end = this.timestamps[1];
			let tooltipcontent;
			if (start !== end) {
				tooltipcontent = jq$('<span>Start: ' + start + '</span><br><span>End: ' + end + '  </span>');
			}
			else {
				tooltipcontent = jq$('<span>Start: ' + start + '</span>');
			}
			jq$(historyEntrySpan).tooltipster({
				content: tooltipcontent,
				position: "top-left",
				delay: 300,
				theme: ".tooltipster-knowwe"
			});
		}

		return watchesEntry;
	}

	function createNewEntry(expression, responseObject) {

		const watchesEntry = jq$('<div/>', {
			'class': 'watchlistline watchlistentry'

		});
		watchesEntry.uniqueId();
		const watchesEntryExpression = jq$('<span/>', {
			'class': 'expression',
			'text': expression
		});
		watchesEntry.append(watchesEntryExpression);


		const length = Object.keys(responseObject).length;
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
			const watchesEntryValue = jq$('<span/>', {
				'class': 'value expressionerror',
				'text': '<not a valid expression>'
			});
			watchesEntry.append(watchesEntryValue);
		}

		watchesEntry.append(createDeleteButton());
		return watchesEntry;
	}

	function addExpression(original, watchesIndex) {

		const expression = original.val();

		const watchesEntry = jq$('<div/>', {
			'class': 'watchlistline watchlistentry',
			'text': expression
		});
		jq$(original).parent().replaceWith(watchesEntry);

		if (typeof watchesIndex != 'undefined') {
			watchesArray.splice(watchesIndex, 0, expression);
		}
		else {
			watchesArray.push(expression);
		}
		updateCookies();

		getExpressionValue(watchesArray).done(function (data) {
			updateOldWatchesList(data);
		});
	}

	function buildBasicWatchesDiv() {

		const watchcontent = jq$('<div/>', {});

		const watchlist = jq$('<div/>', {
			'class': 'watchlist'
		});

		const watchesAddEntry = jq$("<button class='addwatch'><i class='fa fa-circle-plus'></i>&nbsp;Add Watch</button>");


		const watchesAddEntryFromSelection = jq$("<button class='fromselection'><i class='fa fa-paragraph'></i>&nbsp;from Selection</button>");

		watchcontent.append(watchlist);
		watchcontent.append(watchesAddEntry);
		watchcontent.append(watchesAddEntryFromSelection);

		KNOWWE.core.plugin.rightPanel.addToolToRightPanel("Watches", "watches", watchcontent);
	}

	function createDeleteButton() {

		const deleteContainer = jq$('<div/>', {
			'class': 'iconcontainer deletewatch select'
		});

		const deleteIcon = jq$("<a class=''><i class='fa fa-circle-xmark icon'></i></a>");


		return deleteContainer.append(deleteIcon);
	}

	function loadWatchesFromCookies() {
		watchesArray = simpleStorage.get(watchesStorageKey);
		if (typeof watchesArray != 'undefined') {
			getExpressionValue(watchesArray).done(function (data) {
				const expressionArrays = data.values;
				jq$.each(expressionArrays, function (index, value) {
					const newEntry = createNewEntry(watchesArray[index], value);
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
		let text = "";
		if (window.getSelection) {
			text = window.getSelection().toString();
		} else if (document.selection && document.selection.type !== "Control") {
			text = document.selection.createRange().text;
		}
		return text;
	}

	function initVariables() {
		watches = jq$("#watches");
		watchlist = watches.find(".watchlist");
	}

	return {
		initWatchesTool: function () {
			buildBasicWatchesDiv();
			loadWatchesFromCookies();
			initVariables();
			bindUiActions();
			KNOWWE.helper.observer.subscribe("update", function () {
				updateWatches();
			});
		},

		addToWatches: function (text) {
			KNOWWE.core.plugin.rightPanel.showRightPanel();
			addWatch(text);
		},

		moveToBottom: function () {
			moveRightPanelToBottom();
		},

		moveToRight: function () {
			moveRightPanelToRight();
		}
	}
}();

(function init() {

	window.addEvent('domready', _KL.setup);
	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function () {
			KNOWWE.core.plugin.rightPanel.init();
		});
	}
}());
