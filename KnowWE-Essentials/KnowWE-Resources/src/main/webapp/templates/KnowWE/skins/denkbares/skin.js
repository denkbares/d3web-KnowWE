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
	if (base == $('query')) {
		parent = {
			'x' : base.offsetWidth,
			'y' : base.offsetHeight
		};
	} else {
		// search for parent defining it own coordinate system
		for (var anchor = $(el.parentNode); anchor && anchor != document; anchor = $(anchor.parentNode)) {
			var cssPosition = anchor.getStyle('position');
			if (cssPosition == 'absolute' || cssPosition == 'relative') {
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
			$(this.query.form).removeEvents("mouseout");
			$(this.query.form).removeEvents("mouseover");
			// and add focus events instead
			$(this.query).addEvent("blur", function() {
				this.hover.start(0);
			}.bind(this)).addEvent("focus", function() {
				Wiki.locatemenu(this.query, $("searchboxMenu"));
				this.hover.start(0.9);
			}.bind(this));
		}
	};

	SearchBox.ajaxQuickSearch = function() {
		// capture original text before first search
		if (!SearchBox.noSearchTargetText_original) {
			SearchBox.noSearchTargetText_original = $('searchTarget').innerHTML;
		}
		SearchBox.ajaxQuickSearch_original();
		// if search is empty, restore original text and relocate menu
		var a = this.query.value.stripScripts();
		if ((a == null) || (a.trim() == "") || (a == this.query.defaultValue)) {
			$('searchTarget').innerHTML = SearchBox.noSearchTargetText_original;
			Wiki.locatemenu($("query"), $("searchboxMenu"));
		}
	}
}

var DenkbaresSkin = {};

// we set
DenkbaresSkin.scrollTransitionDuration = {'transition' : 'left', '-webkit-transition' : 'left'};
DenkbaresSkin.toggleTransitionDuration = {'transition' : 'left 400ms', '-webkit-transition' : 'left 400ms'};

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
	if (DenkbaresSkin.docHeight == docHeight)
		return;
	DenkbaresSkin.docHeight = docHeight;
	DenkbaresSkin.scrollFavorites();
};


/**
 * if there is a TOC in the left menu, highlight the current visible chapter.
 */
DenkbaresSkin.highlightActiveTOC = function() {
	var tocItems = jq$("#favorites > .leftmenu > .toc li");
	if (tocItems.length == 0) return;

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
	var favorites = jq$("#favorites");
	if (!favorites) return;
	var wHeight = jq$(window).height();
	var docHeight = jq$(document).height();
	var favHeight = favorites.outerHeight();
	var scrollY = DenkbaresSkin.scrollTop();
	var scrollMax = docHeight - wHeight;
	var favToScroll = favHeight - wHeight;
	var actionsBottom = jq$("#actionsBottom");
	var disableFixing = (!actionsBottom.exists()
	|| favHeight >= actionsBottom.offset().top + actionsBottom.height());
	if (!DenkbaresSkin.favoriteStatus) return;
	var favLeft = DenkbaresSkin.favoriteStatus.status == 'expanded' ?
		DenkbaresSkin.favoriteStatus.favLeftExpanded : DenkbaresSkin.favoriteStatus.favLeftCollapsed;
	jq$("#favorites").css(DenkbaresSkin.scrollTransitionDuration);
	jq$("#page").css(DenkbaresSkin.scrollTransitionDuration);
	jq$('#favorites-toggle').css(DenkbaresSkin.scrollTransitionDuration);
	if (scrollY <= jq$('#header').outerHeight() || disableFixing) {
		// when reaching top of page or if page height is made by leftMenu
		// align fav originally to page
		favorites.css({
			position : "relative",
			top : "auto",
			left : favLeft + "px"
		});
	} else if (scrollMax - scrollY <= favToScroll) {
		// when reaching end of page
		// align bottom of fav to bottom of page
		favorites.css({
			position : "absolute",
			top : (docHeight - favHeight) + "px",
			left : favLeft + "px"
		});
	} else {
		// otherwise fix fav to the top of the viewport
		favorites.css({
			position : "fixed",
			top : "0px",
			left : (favLeft - DenkbaresSkin.scrollLeft()) + "px"
		});
	}
	var minHeight = wHeight - favorites.offset().top - (favHeight - favorites.height()) + scrollY;
	favorites.css({'min-height' : minHeight + "px"});
};

DenkbaresSkin.initPageScroll = function() {
	DenkbaresSkin.originalPageOffset = jq$("#page").offset().top;
};

DenkbaresSkin.scrollPage = function() {
	var page = jq$("#page");
	var body = jq$('body');
	var scrollTop = DenkbaresSkin.scrollTop();
	var windowHeight = jq$(window).height();
	var actionHeight = jq$('#actionsTop').height();
	var pageLeft = DenkbaresSkin.favoriteStatus.status == 'expanded' ?
		DenkbaresSkin.favoriteStatus.pageLeftExpanded : DenkbaresSkin.favoriteStatus.pageLeftCollapsed;
	if (page.height() < windowHeight
		&& page.offset().top - scrollTop < -actionHeight) {
		// setting the page position fixed will cause the document width to be reduced
		// we set it manually to avoid odd behavior
		body.width(jq$(document).width());
		page.css({
			position : 'fixed',
			top : "0",
			left : (pageLeft - DenkbaresSkin.scrollLeft()) + "px"
		});
	}
	if (scrollTop < DenkbaresSkin.originalPageOffset + actionHeight) {
		page.css({
			position : 'absolute',
			top : "auto",
			left : pageLeft + "px"
		});
		body.width("auto");
	}
};

DenkbaresSkin.cleanTrail = function() {
	var breadcrumbs = jq$('.breadcrumbs');
	if (breadcrumbs.length == 0)
		return;
	var crumbs = breadcrumbs.find('a.wikipage');
	if (crumbs.length == 0)
		return;
	var crumbsCheck = {};
	var removeBecauseLeadingComma = false;
	// remove duplicate entries
	for (var i = crumbs.length - 1; i >= 0; i--) {
		var crumb = crumbs[i];
		var crumbHtml = jq$(crumb).clone().wrap('<p>').parent().html();
		var existingEntry = crumbsCheck[crumbHtml];
		if (typeof existingEntry == "undefined") {
			crumbsCheck[crumbHtml] = i;
		} else {
			jq$(crumb).remove();
			if (i == 0)
				removeBecauseLeadingComma = true;
		}
	}
	// remove superfluous commas
	var lastNodeText = "";
	for (i = 0; i < breadcrumbs[0].childNodes.length; i++) {
		var childNode = breadcrumbs[0].childNodes[i];
		var tempValue = childNode.nodeValue;
		if ((lastNodeText == ", " || removeBecauseLeadingComma == true)
			&& tempValue == ", ") {
			childNode.nodeValue = "";
			removeBecauseLeadingComma = false;
		}
		lastNodeText = tempValue;

	}
};

DenkbaresSkin.resizeFlows = function() {
	jq$('.Flowchart').each(function() {
		var newWidth = parseInt(jq$('#pagecontent').css('width'));
		newWidth = (Math.round(newWidth / 10) * 10) - 9;
		jq$(this).css('min-width', newWidth);
	});
};

DenkbaresSkin.toggleFavorites = function() {
	var favorites = jq$('#favorites');
	var page = jq$('#page');
	var toggle = jq$('#favorites-toggle');
	var toggleButton = jq$('#favorites-toggle-button');
	favorites.css(DenkbaresSkin.toggleTransitionDuration);
	page.css(DenkbaresSkin.toggleTransitionDuration);
	toggle.css(DenkbaresSkin.toggleTransitionDuration);
	var status = DenkbaresSkin.favoriteStatus;
	if (DenkbaresSkin.favoriteStatus.status == 'expanded') {
		favorites.css({left : status.favLeftCollapsed + "px"});
		page.css({left : status.pageLeftCollapsed + "px"});
		toggle.css({cursor : 'e-resize', left : status.pageLeftCollapsed + "px"});
		toggleButton.attr('title', 'Show left menu');
		toggleButton.find('i').removeClass('fa-angle-double-left').addClass('fa-angle-double-right');
		DenkbaresSkin.favoriteStatus.status = 'collapsed';
	} else {
		favorites.css({left : status.favLeftExpanded + "px"});
		page.css({left : status.pageLeftExpanded + "px"});
		toggle.css({cursor : 'w-resize', left : status.pageLeftExpanded + "px"});
		toggleButton.attr('title', 'Hide left menu');
		toggleButton.find('i').removeClass('fa-angle-double-right').addClass('fa-angle-double-left');
		DenkbaresSkin.favoriteStatus.status = 'expanded';
	}
	jq$(page).bind('transitionend', function() {
		jq$(window).resize();
	});
};

// does not return "elastic scroll" values from OSX.
DenkbaresSkin.scrollLeft = function() {
	var maxScroll = jq$(document).width() - jq$(window).width();
	var minScroll = 0;
	return Math.min(Math.max(jq$(window).scrollLeft(), 0), maxScroll);
};

DenkbaresSkin.scrollTop = function() {
	var maxScroll = jq$(document).height() - jq$(window).height();
	var minScroll = 0;
	return Math.min(Math.max(jq$(window).scrollTop(), 0), maxScroll);
};

DenkbaresSkin.addFavoriteToggle = function() {
	jq$('#page').before("<div id='favorites-toggle'></div>");
	jq$('#menu-pagecontent').before("<div id='favorites-toggle-button' title='Hide left menu'>" +
	"<i class='fa fa-angle-double-left'></i></div>");
	var setTogglePosition = function() {
		jq$('#favorites-toggle').css({
			'left' : (jq$('#page').offset().left - DenkbaresSkin.scrollLeft()) + "px"
		});
	};
	DenkbaresSkin.favoriteStatus = {
		status : 'expanded',
		favLeftExpanded : jq$('#favorites').offset().left,
		pageLeftExpanded : jq$('#page').offset().left,
		toggleLeftExpanded : jq$('#favorites-toggle').offset().left,
		favLeftCollapsed : -(jq$('#page').offset().left - 5),
		pageLeftCollapsed : 5,
		toggleLeftCollapsed : 5
	};
	jq$(window).scroll(setTogglePosition);
	jq$(window).resize(setTogglePosition);
	setTogglePosition();
	jq$('#favorites-toggle').unbind('click').click(DenkbaresSkin.toggleFavorites);
	jq$('#favorites-toggle-button').unbind('click').click(DenkbaresSkin.toggleFavorites);
};


jq$(document).ready(function() {
	DenkbaresSkin.addFavoriteToggle();
	DenkbaresSkin.cleanTrail();
	//DenkbaresSkin.initPageScroll();

	// workaround, because sometimes we are too early
	window.setTimeout(function() {
		DenkbaresSkin.initFavoritesScroll();
		DenkbaresSkin.scrollFavorites();
	});

	// add auto-resize to edit page
	if (KNOWWE.helper.loadCheck(['Edit.jsp'])) {
		var editPane = jq$('form #editorarea');
		editPane.autosize();
		for (var time = 100; time <= 500; time += 100) {
			window.setTimeout(function() {
				editPane.trigger('autosize.resize');
				if (Wiki && Wiki.prefs && Wiki.prefs.set) {
					Wiki.prefs.set("EditorSize", null);
				}
			}, time);
		}
	}
});


KNOWWE.helper.observer.subscribe("flowchartrendered", DenkbaresSkin.resizeFlows);

jq$(window).scroll(DenkbaresSkin.scrollFavorites);
jq$(window).scroll(DenkbaresSkin.highlightActiveTOC);
//jq$(window).scroll(DenkbaresSkin.scrollPage);
jq$(window).resize(DenkbaresSkin.scrollFavorites);
jq$(window).resize(DenkbaresSkin.resizeFlows);