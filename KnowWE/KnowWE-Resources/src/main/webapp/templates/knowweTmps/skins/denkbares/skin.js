/**
 * * SkinQute javascript extensions * needed to initialise RoundedCorner
 * elements. *
 */

if (RoundedCorners) {
	var r = RoundedCorners;
	// r.register( "header", ['yyyy', 'lime', 'lime' ] );
	// r.register( "footer", ['yyyy', 'lime', 'lime' ] );

}

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
	}
	else {
		// search for parent defining it own coordinate system
		for (var anchor = el.parentNode; anchor; anchor = anchor.parentNode) {
			var cssPosition = anchor.getStyle('position');
			if (cssPosition == 'absolute' || cssPosition == 'relative') {
				parent = $('page').getPosition();
				break;
			}
		}
	}

	for ( var z in prop) {
		var pos = corner[z] + offset[z] - parent[z]; /*
														 * top-left corner of
														 * base
														 */
		if ((pos + popup[z] - scroll[z]) > win[z])
			pos = win[z] - popup[z] + scroll[z];
		el.setStyle(prop[z], pos);
	}
};
