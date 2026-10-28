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
	const tabOrderStorageKey = "knowwe.rightPanel.tabOrder";
	// mirror of the user's choice for the server, see de.knowwe.core.rightpanel.RightPanel
	const openCookie = "knowwe-rightpanel-open";
	const tabCookie = "knowwe-rightpanel-tab";
	const defaultRightPanelWidth = 300;
	const minRightPanelWidth = 300;

	let rightPanel = null;

	let showSidebar = false;

	let globalFloatingTime = 500;

	let initScrolling = null;

	let windowWidth;

	let isOnBottom = false;

	let tabResizeObserver = null;

	let panelResizeObserver = null;

	let resizing = false;

	jq$(window).resize(function () {
		// content changing while the panel slides in may toggle the scrollbar of the page, which resizes the window;
		// placing the panel then would fight the animation frame by frame, so it is placed when the animation is done
		if (rightPanel && rightPanel.is(":animated")) return;
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
			height: '', // the height is only set on the right side
			top: 'auto',
			right: 'auto'
		});
		rightPanelScroll();
		updatePageForBottomPanel();
	}

	/**
	 * Leaves room for the panel below the content. A padding (instead of a fixed height) still lets
	 * the content grow and shrink.
	 */
	function updatePageForBottomPanel() {
		const $pageContent = jq$(KNOWWE.core.util.getPageContentSelector()).css('padding-bottom', '');
		const padding = parseFloat($pageContent.css('padding-bottom')) || 0;
		$pageContent.css('padding-bottom', (padding + rightPanel.outerHeight()) + 'px');
	}

	/**
	 * The height of the panel on the bottom changes with its content (switching tabs, collapsing
	 * tools, lazy loading), so the room left for it has to follow.
	 */
	function observePanelHeight() {
		if (typeof ResizeObserver === "undefined") return;
		if (panelResizeObserver) panelResizeObserver.disconnect();
		let lastHeight = null;
		panelResizeObserver = new ResizeObserver(function () {
			const height = rightPanel[0].getBoundingClientRect().height;
			if (height === lastHeight) return;
			lastHeight = height;
			if (showSidebar && isOnBottom) updatePageForBottomPanel();
		});
		panelResizeObserver.observe(rightPanel[0]);
	}

	function moveRightPanelToRight() {
		isOnBottom = false;
		rightPanel.removeClass("right-panel-bottom").addClass("right-panel-right");
		rightPanel.css({
			bottom: '0',
			right: '0'
		});
		// the page announces its own width changes as window resizes, which a drag causes on every move, the stored
		// width would then fight the dragged one until the drag ends
		if (!resizing) setRightPanelWidth(getStoredRightPanelWidth(), false);
		rightPanelScroll();
		jq$(KNOWWE.core.util.getPageContentSelector()).css('padding-bottom', '');
	}

	function makeRightPanelResizable() {
		if (rightPanel.find(".right-panel-resize-handle").length) return;

		const $handle = jq$("<div class=\"right-panel-resize-handle\"></div>").prependTo(rightPanel);
		let startX = 0;
		let startWidth = 0;

		$handle.on("mousedown", function(e) {
			if (isOnBottom) return;

			const panelRect = rightPanel[0].getBoundingClientRect();
			resizing = true;
			startX = e.clientX;
			startWidth = panelRect.width;
			jq$("body").addClass("right-panel-resizing");
			e.preventDefault();
		});

		jq$(document).off(".rightPanelResize");
		jq$(document).on("mousemove.rightPanelResize", function(e) {
			if (!resizing) return;

			const delta = startX - e.clientX;
			setRightPanelWidth(startWidth + delta, false);
			rightPanelScroll();
			e.preventDefault();
		});

		jq$(document).on("mouseup.rightPanelResize", function() {
			if (!resizing) return;

			resizing = false;
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

	function growPage(onHidden) {
		if (!isOnBottom) {
			jq$(KNOWWE.core.util.getPageContentSelector()).animate({'margin-right': '0'}, globalFloatingTime);
			rightPanel.animate({'right': -getRightPanelWidth() + 'px'}, globalFloatingTime, function () {
				removeRightPanel();
				onHidden();
			});
		} else {
			rightPanel.animate({'bottom': -rightPanel.height() + 'px'}, globalFloatingTime, function () {
				removeRightPanel();
				onHidden();
			});
		}
	}

	function hideMountedPanel() {
		jq$(document).off(".rightPanelResize");
		jq$("body").removeClass("right-panel-resizing");
		resizing = false;
		if (rightPanel && rightPanel.length) {
			rightPanel.find(".right-panel-resize-handle").remove();
			rightPanel.removeAttr("style").attr("hidden", "");
		}
	}

	function removeRightPanel() {
		hideMountedPanel();
		jq$(KNOWWE.core.util.getPageContentSelector()).css('padding-bottom', '');
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
		observePanelHeight();
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
			// the content is new, updates deferred before are already contained
			$body.removeAttr("data-rerender-pending");
			onDone();
		}).fail(_IE.onErrorBehavior);
	}

	function getStoredTabOrder() {
		let order = [];
		try {
			const raw = localStorage.getItem(tabOrderStorageKey);
			if (raw) order = JSON.parse(raw);
		} catch (e) { }
		if (!Array.isArray(order)) order = [];
		order = order.filter(id => typeof id === "string");
		return order;
	}

	function recordTabActivation(id) {
		const order = getStoredTabOrder().filter(x => x !== id);
		order.unshift(id);
		try {
			localStorage.setItem(tabOrderStorageKey, JSON.stringify(order));
		} catch (e) { }
		writeStateCookies();
	}

	function writeStateCookies() {
		KNOWWE.helper.cookie.create(openCookie, String(isRightPanelShown()), 365);
		KNOWWE.helper.cookie.create(tabCookie, getStoredTabOrder()[0] || "", 365);
	}

	/**
	 * Returns the id of the tab on this page that shows the specified tab: the tab itself, or a tab of its group (see
	 * RightPanelTabProvider.getGroup), or null if there is none.
	 */
	function availableTab(id) {
		if (tabButton(id).length) return id;
		const entry = (rightPanel.attr("data-groups") || "").split(" ").find(entry => entry.startsWith(id + "="));
		if (!entry) return null;
		const group = entry.substring(id.length + 1);
		const ids = (rightPanel.attr("data-groups") || "").split(" ")
				.filter(entry => entry.endsWith("=" + group))
				.map(entry => entry.substring(0, entry.length - group.length - 1));
		return ids.find(other => tabButton(other).length) || null;
	}

	/**
	 * Returns true if the tab the user activated most recently is contextual and missing on this page, and no tab of
	 * its group replaces it. The panel then stays collapsed on this page, without changing the user's choice to show
	 * it (see RightPanelTabProvider.isContextual).
	 */
	function isCollapsedOnThisPage() {
		const lastTab = getStoredTabOrder()[0];
		if (!lastTab || availableTab(lastTab)) return false;
		const contextual = (rightPanel.attr("data-contextual") || "").split(" ");
		return contextual.includes(lastTab);
	}

	function dispatchPanelEvent(shown) {
		document.dispatchEvent(new CustomEvent(shown ? "rightPanelShown" : "rightPanelHidden"));
	}

	function activateTab(id, userInitiated) {
		const $btn = tabButton(id);
		if (!$btn.length) return;
		const $body = tabBody(id);
		const lazy = $btn.is("[data-lazy]");

		function show() {
			rightPanel.find(".right-panel-tablist button").removeAttr("data-active");
			$btn.attr("data-active", "");
			// inactive tabs are not rendered again on updates, but when they are shown (see rerenderShown)
			rightPanel.find(".right-panel-tab").attr("hidden", "").attr("data-defer-rerender", "");
			$body.removeAttr("hidden").removeAttr("data-defer-rerender");
			KNOWWE.core.rerendercontent.rerenderPending($body[0]);
			if (userInitiated) recordTabActivation(id);
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

	function updateTabLabelVisibility(tablist) {
		tablist.classList.remove("icons-only");
		if (tablist.scrollWidth > tablist.clientWidth) {
			tablist.classList.add("icons-only");
		}
	}

	function setupResponsiveTabs(tablist) {
		if (!tablist) return;
		updateTabLabelVisibility(tablist);
		if (typeof ResizeObserver === "undefined") return;
		if (tabResizeObserver) tabResizeObserver.disconnect();
		tabResizeObserver = new ResizeObserver(function () {
			updateTabLabelVisibility(tablist);
		});
		tabResizeObserver.observe(tablist);
	}

	function initRightPanelTabs() {
		const $tablist = rightPanel.find(".right-panel-tablist");

		$tablist.off("click.rightPanelTabs").on("click.rightPanelTabs", "button[data-tab]", function () {
			activateTab(this.getAttribute("data-tab"), true);
		});

		// eagerly initialize every non-lazy body, even ones never opened (decision 7a)
		rightPanel.find(".right-panel-tab").each(function () {
			const id = this.getAttribute("data-tab");
			if (!tabButton(id).is("[data-lazy]")) {
				ensureInitialized(id, this);
			}
		});

		const order = getStoredTabOrder();
		let activeId = null;
		for (let i = 0; i < order.length; i++) {
			// a missing tab is replaced by a tab of its group, without changing the stored order
			activeId = availableTab(order[i]);
			if (activeId) break;
		}
		if (!activeId) {
			const $default = $tablist.find("button[data-active]").first();
			const $first = $default.length ? $default : $tablist.find("button[data-tab]").first();
			activeId = $first.attr("data-tab");
		}
		if (activeId) activateTab(activeId, false);

		setupResponsiveTabs($tablist[0]);
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
		writeStateCookies();
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
			// when restored on page load, the server already knows the panel is shown (see init)
			if (!fromCookie) dispatchPanelEvent(true);
		}
	}

	function terminateRightPanel() {
		if (!showSidebar) return;
		// notified when the panel has slid out, so its content does not change while it is still visible
		growPage(() => dispatchPanelEvent(false));
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

	function buildToolContent(pluginDiv) {
		const content = jq$('<div/>', {
				'class': 'right-panel-content'
			}
		);
		content.append(pluginDiv);
		return content;
	}

	function assembleTool(title, id, pluginDiv) {
		const tool = buildToolContainer(id);
		tool.append(buildTopBar(title));
		tool.append(buildToolContent(pluginDiv));
		return tool;
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
			// the panel may be collapsed on this page although the user chose to show it (isCollapsedOnThisPage)
			if (showSidebar) {
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

			const isShown = isRightPanelShown() && !isCollapsedOnThisPage();
			if (isShown) {
				initRightPanel(true);
			}
			initRightPanelToggleButton(isShown);
			// the server renders the page for the state of the cookies, which may be missing or outdated
			writeStateCookies();
			if (isShown !== (rightPanel.attr("data-open") === "true")) dispatchPanelEvent(isShown);
			jq$(window).scroll(function() {
				// wait for animation frame since we check for changes also done on scroll... we make sure we see these changes
				window.requestAnimationFrame(rightPanelScroll);
			});
			jq$('.header').on("transitionend", rightPanelScroll);
			setTimeout(rightPanelScroll, 0);
		},

		addToolToRightPanel: function (title, id, pluginDiv) {
			const tool = assembleTool(title, id, pluginDiv);
			const $active = rightPanel.find(".right-panel-tab:not([hidden])").first();
			($active.length ? $active : rightPanel).append(tool);
		},

		buildTool: function (title, id, pluginDiv) {
			return assembleTool(title, id, pluginDiv);
		},

		activateTab: function (id) {
			activateTab(id, true);
		},

		moveToBottom: function () {
			moveRightPanelToBottom();
		},

		moveToRight: function () {
			moveRightPanelToRight()
		},

		isShownOnRight: function () {
			return showSidebar && !isOnBottom;
		},

		setTabEnabled: function (id, enabled) {
			if (!rightPanel || !rightPanel.length) return;
			const $btn = tabButton(id);
			const $body = tabBody(id);
			if (enabled) {
				$btn.removeAttr("hidden");
			} else {
				$btn.attr("hidden", "");
				$body.attr("hidden", "").attr("data-defer-rerender", "");
			}
		}

	}

}
();

(function init() {

	window.addEvent('domready', _KL.setup);
	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function () {
			KNOWWE.core.plugin.rightPanel.init();
		});
	}
}());
