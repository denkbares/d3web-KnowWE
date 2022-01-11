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
Wiki.locatemenu = function (base, el) {
	const win = {
		'x': window.getWidth(),
		'y': window.getHeight()
	}, scroll = {
		'x': window.getScrollLeft(),
		'y': window.getScrollTop()
	}, corner = base.getPosition(), offset = {
		'x': base.offsetWidth - el.offsetWidth,
		'y': base.offsetHeight
	}, popup = {
		'x': el.offsetWidth,
		'y': el.offsetHeight
	}, prop = {
		'x': 'left',
		'y': 'top'
	};
	let parent = {
		'x': 0,
		'y': 0
	};

	// some special treatment for search to avoid annoying menu position
	if (base === jq$('#query')[0]) {
		parent = {
			'x': base.offsetWidth,
			'y': base.offsetHeight
		};
	} else {
		// search for parent defining it own coordinate system
		for (let anchor = el.parentNode; anchor && anchor !== document; anchor = anchor.parentNode) {
			const cssPosition = anchor.getStyle('position');
			if (cssPosition === 'absolute' || cssPosition === 'relative') {
				parent = anchor.getPosition();
				break;
			}
		}
	}

	for (let z in prop) {
		// top-left corner of base
		let pos = corner[z] + offset[z] - parent[z];
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

	SearchBox.onPageLoadQuickSearch = function () {
		// call original first, doing lots of other stuff
		SearchBox.onPageLoadQuickSearch_original();
		// remove hover events
		if (this.query) {
			jq$(this.query.form).off("mouseout");
			jq$(this.query.form).off("mouseover");
			// and add focus events instead
			jq$(this.query).blur(function () {
				this.hover.start(0);
			}.bind(this)).focus(function () {
				Wiki.locatemenu(this.query, jq$("#searchboxMenu")[0]);
				this.hover.start(0.9);
			}.bind(this));
		}
	};

	SearchBox.ajaxQuickSearch = function () {
		// capture original text before first search
		if (!SearchBox.noSearchTargetText_original) {
			SearchBox.noSearchTargetText_original = jq$('#searchTarget')[0].innerHTML;
		}
		SearchBox.ajaxQuickSearch_original();
		// if search is empty, restore original text and relocate menu
		const a = this.query.value.stripScripts();
		if ((a === null) || (a.trim() === "") || (a === this.query.defaultValue)) {
			jq$('#searchTarget')[0].innerHTML = SearchBox.noSearchTargetText_original;
			Wiki.locatemenu(jq$("#query")[0], jq$("#searchboxMenu")[0]);
		}
	}
}

const DenkbaresSkin = {};


// we set
DenkbaresSkin.narrowPageWidth = 550;
DenkbaresSkin.mediumPageWidth = 825;

/**
 * Initialize cutting edge favorite scrolling
 */
DenkbaresSkin.initFavoritesScroll = function () {
	// initialize some additional events
	document.body.onclick = DenkbaresSkin.checkDocSizeScroll;
};

/**
 * Quick convenience function to be called every time the document size may have
 * changed. Unfortunately this cannot be traced by an event.
 */
DenkbaresSkin.checkDocSizeScroll = function () {
	// alert("check");
	const docHeight = window.getScrollHeight();
	if (DenkbaresSkin.docHeight === docHeight)
		return;
	DenkbaresSkin.docHeight = docHeight;
};


/**
 * if there is a TOC in the left menu, highlight the current visible chapter.
 */
DenkbaresSkin.highlightActiveTOC = function () {
	const tocItems = jq$(".sidebar > .leftmenu > .toc li");
	if (tocItems.length === 0) return;

	// find the first section that is visible on the screen, if there is no such section
	// find the last section that is above the middle of the screen
	const sections = Wiki.getSections();
	let index = -1;
	const top = window.getScrollTop();
	const height = jq$(window).height();
	const middle = top + height / 2;
	const bottom = top + height;
	for (let i = 0; i <= sections.length; i++) {
		// get position of the section
		// (and add position for below document end at last element)
		const pos = (i < sections.length)
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

DenkbaresSkin.initPageScroll = function () {
	DenkbaresSkin.originalPageOffset = jq$(".page").offset().top;
};

DenkbaresSkin.adjustPageHeight = function () {
	jq$('.page').css('min-height', jq$('.sidebar').outerHeight());
};

DenkbaresSkin.cleanTrail = function () {
	const breadcrumbs = jq$('.breadcrumb');
	if (breadcrumbs.length === 0)
		return;
	const crumbs = breadcrumbs.find('a.wikipage');
	if (crumbs.length === 0)
		return;
	const crumbsCheck = {};
	// remove duplicate entries
	for (let i = crumbs.length - 1; i >= 0; i--) {
		const crumb = crumbs[i];
		const crumbText = jq$(crumb).text();
		if (crumbsCheck[crumbText]) {
			if (jq$(crumb).prev().hasClass('divider')) {
				jq$(crumb).prev().remove();
			}
			jq$(crumb).remove();
		} else {
			crumbsCheck[crumbText] = true;
		}
	}
	const firstRemainingCrumb = jq$('.breadcrumb a.wikipage')[0];
	if (jq$(firstRemainingCrumb).prev().hasClass('divider')) {
		jq$(firstRemainingCrumb).prev().remove();
	}
};

DenkbaresSkin.resizeFlows = function () {
	jq$('.Flowchart').each(function () {
		let newWidth = jq$('.page-content').width();
		newWidth = (Math.round(newWidth / 10) * 10) - 9;
		jq$(this).css('min-width', newWidth);
	});
};

DenkbaresSkin.showSidebar = function () {
	jq$('.content').addClass('active');
};

DenkbaresSkin.hideSidebar = function () {
	jq$('.content').removeClass('active');
};

DenkbaresSkin.toggleSidebar = function () {
	if (DenkbaresSkin.isSidebarShown()) {
		DenkbaresSkin.hideSidebar();
	} else {
		DenkbaresSkin.showSidebar();
	}
};

DenkbaresSkin.toggleFavorites = function () {
	DenkbaresSkin.toggleSidebar();
};

DenkbaresSkin.isSidebarShown = function () {
	return jq$('.content').hasClass('active');
};

// does not return "elastic scroll" values from OSX.
DenkbaresSkin.scrollLeft = function () {
	const maxScroll = jq$(document).width() - jq$(window).width();
	return Math.min(Math.max(jq$(window).scrollLeft(), 0), maxScroll);
};

DenkbaresSkin.scrollTop = function () {
	const maxScroll = jq$(document).height() - jq$(window).height();
	return Math.min(Math.max(jq$(window).scrollTop(), 0), maxScroll);
};

jq$(document).ready(function () {
	DenkbaresSkin.cleanTrail();
	DenkbaresSkin.lastPageWidth = jq$(window).width();

	// add ID #favorites to sidebar
	jq$(jq$('.sidebar')[0]).attr('id', 'favorites');

	// add auto-resize to edit page
	if (KNOWWE.helper.loadCheck(['Edit.jsp'])) {
		window.setTimeout(function () {
			const editPanes = jq$('.editor');
			for (let i = 0; i < editPanes.length; i++) {
				jq$(editPanes[i]).trigger('autosize.resize');
			}
			const ajaxpreview = jq$('.ajaxpreview');
			if (ajaxpreview) {
				ajaxpreview.height(jq$(editPanes[0]).height());
			}
		}, 0);

	}

});


KNOWWE.helper.observer.subscribe("flowchartrendered", DenkbaresSkin.resizeFlows);

jq$(window).scroll(DenkbaresSkin.highlightActiveTOC);
jq$(window).scroll(AutoComplete.adjustPosition);
jq$(window).resize(DenkbaresSkin.resizeFlows);
jq$(window).resize(DenkbaresSkin.adjustPageHeight);
jq$(document).on('quickSearchResult', DenkbaresSkin.resizeQuickSearchBox);
