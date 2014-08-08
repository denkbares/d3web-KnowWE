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

/**
 * Initialize cutting edge favorite scrolling
 */
DenkbaresSkin.initFavScroll = function() {
	if (DenkbaresSkin.isInitialized)
		return;
	var element = $("favorites");
	if (!element)
		return;
	DenkbaresSkin.isInitialized = true;
	DenkbaresSkin.originY = element.offsetTop;
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
	DenkbaresSkin.checkFavScroll();
};

/**
 * Get the height of the document independent of the used browser.
 */
DenkbaresSkin.getDocHeight = function() {
	var D = document;
	return Math.max(Math.max(D.body.scrollHeight,
		D.documentElement.scrollHeight), Math.max(D.body.offsetHeight,
		D.documentElement.offsetHeight), Math.max(D.body.clientHeight,
		D.documentElement.clientHeight));
};

/**
 * Adapt the left menu favorites to the screen so that the display size is
 * optimally used.
 */
DenkbaresSkin.checkFavScroll = function() {
	DenkbaresSkin.initFavScroll();
	var element = $("favorites");
	if (!element)
		return;
	var originY = DenkbaresSkin.originY;
	var wHeight = window.getHeight();
	var docHeight = DenkbaresSkin.getDocHeight();
	var favHeight = element.clientHeight;
	var favBottom = originY + favHeight;
	var scrollY = window.getScrollTop();
	var scrollMax = docHeight - wHeight;
	var favToScroll = favHeight - wHeight;
	var disableFixing = ($("actionsBottom") == null
		|| favHeight >= $("actionsBottom").offsetTop + $("actionsBottom").clientHeight);
	if (scrollY <= originY || disableFixing) {
		// when reaching top of page or if page height is made by leftMenu
		// align fav originally to page
		element.style.position = "static";
		element.style.top = originY + "px";
	} else if (scrollMax - scrollY <= favToScroll) {
		// when reaching end of page
		// align bottom of fav to bottom of page
		element.style.position = "absolute";
		element.style.top = (docHeight - favHeight) + "px";
		element.style.left = "0px";
	} else {
		// otherwise fix fav to the top of the viewport
		element.style.position = "fixed";
		element.style.top = "0px";
		element.style.left = "-" + window.getScrollLeft() + "px";
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
	for (var i = 0; i < breadcrumbs[0].childNodes.length; i++) {
		var childNode = breadcrumbs[0].childNodes[i];
		var tempValue = childNode.nodeValue;
		if ((lastNodeText == ", " || removeBecauseLeadingComma == true)
			&& tempValue == ", ") {
			childNode.nodeValue = "";
			removeBecauseLeadingComma = false;
		}
		lastNodeText = tempValue;

	}
	var text = jq$(breadcrumbs.first());
};

if (typeof jq$ != 'undefined') {
	jq$(window).ready(function() {
		DenkbaresSkin.cleanTrail();
	});
}

DenkbaresSkin.resizeFlows = function() {
	jq$('.Flowchart').each(function() {
		var newWidth = parseInt(jq$('#pagecontent').css('width'));
		newWidth = (Math.round(newWidth / 10) * 10) - 9;
		jq$(this).css('min-width', newWidth);
	});
};

KNOWWE.helper.observer.subscribe("flowchartrendered", DenkbaresSkin.resizeFlows);

jq$(window).scroll(DenkbaresSkin.checkFavScroll);
jq$(window).resize(DenkbaresSkin.checkFavScroll);
jq$(window).resize(DenkbaresSkin.resizeFlows);

// add auto-resize to edit page
if (KNOWWE.helper.loadCheck([ 'Edit.jsp' ])) {
	jq$(document).ready(function() {
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
	});
}

