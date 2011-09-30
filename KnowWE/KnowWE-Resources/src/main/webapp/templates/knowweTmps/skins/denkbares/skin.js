/**
 * * SkinQute javascript extensions * needed to initialise RoundedCorner
 * elements. *
 */

if (RoundedCorners) {
	var r = RoundedCorners;
	// r.register( "header", ['yyyy', 'lime', 'lime' ] );
	// r.register( "footer", ['yyyy', 'lime', 'lime' ] );

}

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
		for ( var anchor = $(el.parentNode); anchor && anchor != document; anchor = $(anchor.parentNode)) {
			var cssPosition = anchor.getStyle('position');
			if (cssPosition == 'absolute' || cssPosition == 'relative') {
				parent = anchor.getPosition();
				break;
			}
		}
	}

	for ( var z in prop) {
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
};


var DenkbaresSkin = {};

/**
 * Initialize cutting edge favorite scrolling
 */
DenkbaresSkin.initFavScroll = function () {
	if (DenkbaresSkin.isInitialized) return;
	var element = $("favorites");
	if (!element) return;
	DenkbaresSkin.isInitialized = true;
	DenkbaresSkin.originY = element.offsetTop;
	// initialize some additional events
	document.body.onclick = DenkbaresSkin.checkDocSizeScroll;
};

/**
 * Quick convenience function to be called every time
 * the document size may have changed.
 * Unfortunately this cannot be traced by an event. 
 */
DenkbaresSkin.checkDocSizeScroll = function () {
	//alert("check");
	var docHeight = window.getScrollHeight();
	if (DenkbaresSkin.docHeight == docHeight) return;
	DenkbaresSkin.docHeight = docHeight;
	DenkbaresSkin.checkFavScroll();
};

/**
 * Adapt the left menu favorites to the screen
 * so that the display size is optimally used.
 */
DenkbaresSkin.checkFavScroll = function () {
	DenkbaresSkin.initFavScroll();
	var element = $("favorites");
	if (!element) return;
	var originY = DenkbaresSkin.originY;
	var docHeight = window.getScrollHeight();
	var favHeight = element.clientHeight;
	var favBottom = originY + favHeight; 
	var scrollY = window.getScrollTop();
	var scrollMax = window.getScrollHeight() - window.getHeight();
	var favToScroll = favHeight - window.getHeight();
	if (scrollY <= originY) {
		// when reaching top of page
		// align fav originally to page
		element.style.position = "absolute";
		element.style.top = originY+"px";
	}
	else if (scrollMax - scrollY <= favToScroll) {
		// when reaching end of page
		// align bottom of fav to bottom of page
		element.style.position = "absolute";
		element.style.top = (docHeight - favHeight)+"px";
	}
	else {
		// otherwise fix fav to the top of the viewport
		element.style.position = "fixed";
		element.style.top = "0px";
	}
};


window.onresize = DenkbaresSkin.checkFavScroll;
window.onscroll = DenkbaresSkin.checkFavScroll;