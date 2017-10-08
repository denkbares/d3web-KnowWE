/*
 * Copyright (C) 2017 denkbares GmbH, Germany
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

// overwrite "Wiki.locatemenu" to correct bug
// if menu ist relative to a non-document element
Wiki.locatemenu = function(base, el) {
	var win = {
		'x' : window.getWidth(),
		'y' : window.getHeight()
	}, scroll = {
		'x' : window.getScrollLeft(),
		'y' : window.getScrollTop()
	}, corner = base.getPosition(), offset = {
		'x' : base.offsetWidth - el.offsetWidth,
		'y' : base.offsetHeight
	}, popup = {
		'x' : el.offsetWidth,
		'y' : el.offsetHeight
	}, prop = {
		'x' : 'left',
		'y' : 'top'
	}, parent = {
		'x' : 0,
		'y' : 0
	};

	// some special treatment for search to avoid annoying menu position
	if (base === jq$('#query')[0]) {
		parent = {
			'x' : base.offsetWidth,
			'y' : base.offsetHeight
		};
	} else {
		// search for parent defining it own coordinate system
		for (var anchor = el.parentNode; anchor && anchor !== document; anchor = anchor.parentNode) {
			var cssPosition = anchor.getStyle('position');
			if (cssPosition === 'absolute' || cssPosition === 'relative') {
				parent = anchor.getPosition();
				break;
			}
		}
	}

	for (var z in prop) {
		// top-left corner of base
		var pos = corner[z] + offset[z] - parent[z];
		if ((pos + popup[z] - scroll[z]) > win[z])
			pos = win[z] - popup[z] + scroll[z];
		el.setStyle(prop[z], pos);
	}
};

// hack into search menu
// 1) to avoid popup at mouseover but having it on focus instead
// 2) locate search-popup, even if text is selected
if (SearchBox) {

	SearchBox.onPageLoadQuickSearch_original = SearchBox.onPageLoadQuickSearch;
	SearchBox.ajaxQuickSearch_original = SearchBox.ajaxQuickSearch;
	SearchBox.noSearchTargetText_original = null;

	SearchBox.onPageLoadQuickSearch = function() {
		// call original first, doing lots of other stuff
		SearchBox.onPageLoadQuickSearch_original();
		// remove hover events
		if (this.query) {
			jq$(this.query.form).off("mouseout");
			jq$(this.query.form).off("mouseover");
			// and add focus events instead
			jq$(this.query).blur(function() {
				this.hover.start(0);
			}.bind(this)).focus(function() {
				Wiki.locatemenu(this.query, jq$("#searchboxMenu")[0]);
				this.hover.start(0.9);
			}.bind(this));
		}
	};

	SearchBox.ajaxQuickSearch = function() {
		// capture original text before first search
		if (!SearchBox.noSearchTargetText_original) {
			SearchBox.noSearchTargetText_original = jq$('#searchTarget')[0].innerHTML;
		}
		SearchBox.ajaxQuickSearch_original();
		// if search is empty, restore original text and relocate menu
		var a = this.query.value.stripScripts();
		if ((a === null) || (a.trim() === "") || (a === this.query.defaultValue)) {
			jq$('#searchTarget')[0].innerHTML = SearchBox.noSearchTargetText_original;
			Wiki.locatemenu(jq$("#query")[0], jq$("#searchboxMenu")[0]);
		}
	}
}

var DenkbaresSkin = {};


DenkbaresSkin.jspwikiSideBarButtonWasPressed = function() {
	try {
		return JSON.parse(jq$.cookie("JSPWikiUserPrefs")).Sidebar === "";
	} catch (err) {
		console.warn("Unable to read side bar state:", err);
		return false;
	}
}

// we set
DenkbaresSkin.scrollTransitionDuration = {'transition' : 'left', '-webkit-transition' : 'left'};
DenkbaresSkin.toggleTransitionDuration = {'transition' : 'left 400ms', '-webkit-transition' : 'left 400ms'};
DenkbaresSkin.sideBarButtonWasPressed = DenkbaresSkin.jspwikiSideBarButtonWasPressed();
DenkbaresSkin.narrowPageWidth = 550;
DenkbaresSkin.mediumPageWidth = 825;

/**
 * Initialize cutting edge favorite scrolling
 */
DenkbaresSkin.initFavoritesScroll = function() {
	// initialize some additional events
	document.body.onclick = DenkbaresSkin.checkDocSizeScroll;
};

/**
 * Quick convenience function to be called every time the document size may have
 * changed. Unfortunately this cannot be traced by an event.
 */
DenkbaresSkin.checkDocSizeScroll = function() {
	// alert("check");
	var docHeight = window.getScrollHeight();
	if (DenkbaresSkin.docHeight === docHeight)
		return;
	DenkbaresSkin.docHeight = docHeight;
	DenkbaresSkin.scrollFavorites();
};


/**
 * if there is a TOC in the left menu, highlight the current visible chapter.
 */
DenkbaresSkin.highlightActiveTOC = function() {
	var tocItems = jq$(".sidebar > .leftmenu > .toc li");
	if (tocItems.length === 0) return;

	// find the first section that is visible on the screen, if there is no such section
	// find the last section that is above the middle of the screen
	var sections = Wiki.getSections();
	var index = -1;
	var top = window.getScrollTop();
	var height = jq$(window).height();
	var middle = top + height / 2;
	var bottom = top + height;
	for (var i = 0; i <= sections.length; i++) {
		// get position of the section
		// (and add position for below document end at last element)
		var pos = (i < sections.length)
			? jq$(sections[i]).offset().top
			: jq$(document).height() + height;

		// use first one visible on screen
		if (top <= pos + 10 && pos + 20 <= bottom) {
			index = i;
			break;
		}
		// use the one that comes before the first one that is below the screen middle
		if (pos >= middle) {
			index = i - 1;
			break;
		}
	}

	// highlight the TOC entry that represents this section
	tocItems.removeClass("active");
	if (index >= 0) tocItems.eq(index).addClass("active");
};

/**
 * Adapt the left menu favorites to the screen so that the display size is
 * optimally used.
 */
DenkbaresSkin.scrollFavorites = function() {
	if (DenkbaresSkin.isSidebarShown() && jq$(window).width() >= DenkbaresSkin.narrowPageWidth) {
		var sidebar = jq$('.sidebar');
		var sidebarTop = 0
		if (sidebar.exists()) {
			sidebarTop = sidebar.offset().top;
			var sidebarHeight = sidebar.outerHeight();
		}
		var footer = jq$('.footer');
		var footerTop = 0;
		if (footer.exists()) {
			footerTop = footer.offset().top;
		}
		var limit = footerTop - sidebarHeight;
		var stickyMenuHeight = jq$('.sticky').outerHeight();
		var windowTop = jq$(window).scrollTop();

		// when header is visible, place sidebar beneath it
		if (window.pageYOffset <= jq$('.header').outerHeight()) {
			sidebar.css({
				position : "absolute",
				top : "0"
			});
			// keep sidebar fixed on the left when header is not visible
		} else if (sidebarTop - stickyMenuHeight < windowTop) {
			sidebar.css({
				position : 'fixed',
				top : stickyMenuHeight + 'px'
			});
		}
		// if footer is visible align bottom of sidebar with footer's top
		if (limit - stickyMenuHeight < windowTop) {
			var diff = limit - (windowTop);
			sidebar.css({
				position : 'fixed',
				top : diff + 'px'
			})
		}
	} else {
		jq$('.sidebar').css({
			position : "absolute",
			top : "0"
		})
	}
};

DenkbaresSkin.adjustSidebarVisibility = function() {
	var windowWidth = jq$(window).width();
	if (DenkbaresSkin.sideBarButtonWasPressed) {
		DenkbaresSkin.sideBarButtonWasPressed = !DenkbaresSkin.windowSizePassedThreshold(windowWidth);
	}
	if (!DenkbaresSkin.sideBarButtonWasPressed) {
		if (DenkbaresSkin.jspwikiSideBarButtonWasPressed() || windowWidth < DenkbaresSkin.mediumPageWidth) {
			DenkbaresSkin.hideSidebar();
		} else {
			DenkbaresSkin.showSidebar();
		}
	}
	DenkbaresSkin.lastPageWidth = windowWidth;
}

DenkbaresSkin.windowSizePassedThreshold = function(newWindowWidth) {
	if (DenkbaresSkin.lastPageWidth < DenkbaresSkin.narrowPageWidth
		&& newWindowWidth > DenkbaresSkin.narrowPageWidth) {
		return true;
	}
	if (DenkbaresSkin.lastPageWidth > DenkbaresSkin.narrowPageWidth
		&& newWindowWidth < DenkbaresSkin.narrowPageWidth) {
		return true;
	}
	if (DenkbaresSkin.lastPageWidth < DenkbaresSkin.mediumPageWidth
		&& newWindowWidth > DenkbaresSkin.mediumPageWidth) {
		return true;
	}
	//noinspection RedundantIfStatementJS
	if (DenkbaresSkin.lastPageWidth > DenkbaresSkin.mediumPageWidth
		&& newWindowWidth < DenkbaresSkin.mediumPageWidth) {
		return true;
	}
	return false;
}

DenkbaresSkin.initPageScroll = function() {
	DenkbaresSkin.originalPageOffset = jq$(".page").offset().top;
};

DenkbaresSkin.adjustPageHeight = function() {
	jq$('.page').css('min-height', jq$('.sidebar').outerHeight());
}

DenkbaresSkin.cleanTrail = function() {
	var breadcrumbs = jq$('.breadcrumb');
	if (breadcrumbs.length === 0)
		return;
	var crumbs = breadcrumbs.find('a.wikipage');
	if (crumbs.length === 0)
		return;
	var crumbsCheck = {};
	// remove duplicate entries
	for (var i = crumbs.length - 1; i >= 0; i--) {
		var crumb = crumbs[i];
		var crumbText = jq$(crumb).text();
		if (crumbsCheck[crumbText]) {
			if (jq$(crumb).prev().hasClass('divider')) {
				jq$(crumb).prev().remove();
			}
			jq$(crumb).remove();
		} else {
			crumbsCheck[crumbText] = true;
		}
	}
	var firstRemainingCrumb = jq$('.breadcrumb a.wikipage')[0];
	if (jq$(firstRemainingCrumb).prev().hasClass('divider')) {
		jq$(firstRemainingCrumb).prev().remove();
	}
};

DenkbaresSkin.resizeFlows = function() {
	jq$('.Flowchart').each(function() {
		var newWidth = jq$('.page-content').width();
		newWidth = (Math.round(newWidth / 10) * 10) - 9;
		jq$(this).css('min-width', newWidth);
	});
};

DenkbaresSkin.showSidebar = function() {
	jq$('.content').addClass('active');
	DenkbaresSkin.onShowSidebar();
}

DenkbaresSkin.hideSidebar = function() {
	jq$('.content').removeClass('active');
	DenkbaresSkin.onHideSidebar();
}

DenkbaresSkin.toggleSidebar = function() {
	if (DenkbaresSkin.isSidebarShown()) {
		DenkbaresSkin.hideSidebar();
	} else {
		DenkbaresSkin.showSidebar();
	}
}

DenkbaresSkin.toggleFavorites = function() {
	DenkbaresSkin.toggleSidebar();
}

DenkbaresSkin.isSidebarShown = function() {
	return jq$('.content').hasClass('active');
}

DenkbaresSkin.onHideSidebar = function() {
	jq$('.sidebar').css('width', '275px');
	jq$('.sidebar').css('left', '-275px');
	jq$('.page').css('display', 'block');
	jq$('.content').css('min-height', 'auto');
}

DenkbaresSkin.onShowSidebar = function() {
	DenkbaresSkin.scrollFavorites();
	jq$('.sidebar').css('left', '0');
	if (jq$(window).width() < DenkbaresSkin.narrowPageWidth) {
		jq$('.sidebar').css('width', '100vw');
		jq$('.page').css('display', 'none');
		jq$('.content').css('min-height', jq$('.sidebar').outerHeight() + 'px');
	} else {
		jq$('.sidebar').css({
			'width' : '275px'
		});
	}
}

// does not return "elastic scroll" values from OSX.
DenkbaresSkin.scrollLeft = function() {
	var maxScroll = jq$(document).width() - jq$(window).width();
	return Math.min(Math.max(jq$(window).scrollLeft(), 0), maxScroll);
};

DenkbaresSkin.scrollTop = function() {
	var maxScroll = jq$(document).height() - jq$(window).height();
	return Math.min(Math.max(jq$(window).scrollTop(), 0), maxScroll);
};

jq$(document).ready(function() {
	DenkbaresSkin.cleanTrail();
	DenkbaresSkin.lastPageWidth = jq$(window).width();
	DenkbaresSkin.adjustPageHeight();

	// workaround, because sometimes we are too early
	window.setTimeout(function() {
		DenkbaresSkin.initFavoritesScroll();
		DenkbaresSkin.adjustSidebarVisibility();
		DenkbaresSkin.adjustPageHeight();
		DenkbaresSkin.scrollFavorites();
	});

	// add ID #favorites to sidebar
	jq$(jq$('.sidebar')[0]).attr('id', 'favorites');

	// add auto-resize to edit page
	if (KNOWWE.helper.loadCheck(['Edit.jsp'])) {
		window.setTimeout(function() {
			var editPanes = jq$('.editor');
			for (var i = 0; i < editPanes.length; i++) {
				jq$(editPanes[i]).trigger('autosize.resize');
			}
			var ajaxpreview = jq$('.ajaxpreview');
			if (ajaxpreview) {
				ajaxpreview.height(jq$(editPanes[0]).height());
			}
		}, 0);

	}

	jq$('#menu').click(function() {
		DenkbaresSkin.sideBarButtonWasPressed = true;
		if (DenkbaresSkin.isSidebarShown()) {
			DenkbaresSkin.onShowSidebar();
		} else {
			DenkbaresSkin.onHideSidebar();
		}
	});

});


KNOWWE.helper.observer.subscribe("flowchartrendered", DenkbaresSkin.resizeFlows);

jq$(window).scroll(DenkbaresSkin.scrollFavorites);
jq$(window).scroll(DenkbaresSkin.highlightActiveTOC);
jq$(window).scroll(AutoComplete.adjustPosition);
jq$(window).resize(DenkbaresSkin.resizeFlows);
jq$(window).resize(DenkbaresSkin.adjustSidebarVisibility);
jq$(window).resize(DenkbaresSkin.scrollFavorites);
jq$(window).resize(DenkbaresSkin.adjustPageHeight);
//jq$(document).on('rightPanelResize', DenkbaresSkin.scrollFavorites);
//jq$(document).on('rightPanelResize', DenkbaresSkin.resizeFlows);
jq$(document).on('quickSearchResult', DenkbaresSkin.resizeQuickSearchBox);
