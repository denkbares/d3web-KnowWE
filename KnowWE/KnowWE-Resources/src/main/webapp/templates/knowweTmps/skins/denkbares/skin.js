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
	}, parent = $('page').getPosition();
	
	if (base == $('query')) {
		parent = {
			'x' : base.offsetWidth,
			'y' : base.offsetHeight
		};
	}

	for ( var z in prop) {
		var pos = corner[z] + offset[z] - parent[z]; /* top-left corner of base */
		if ((pos + popup[z] - scroll[z]) > win[z])
			pos = win[z] - popup[z] + scroll[z];
		el.setStyle(prop[z], pos);
	}
};
