
function Rollup(titleElement, contentElement) {
	this.titleElement = $(titleElement);
	this.contentElement = $(contentElement);
	this.rollupGroup = null;
	
	var rollup = this;
	this.onClickHandler = function(event) {rollup.onClick(event);};
	Event.observe(this.titleElement, 'click', this.onClickHandler);
}

Rollup.prototype.onClick = function() {
	if (this.rollupGroup) {
		this.rollupGroup.expandRollup(this);
	}
	else {
		this.toggle();
	}
}

Rollup.prototype.toggle = function() {
	Effect.toggle(this.contentElement, 'blind', { duration: 0.3 });
}

Rollup.prototype.isCollapsed = function() {
	return this.contentElement.style.display == 'none';
}

Rollup.prototype.destroy = function() {
	Event.stopObserving(this.titleElement, 'click', this.onClickHandler);
}



function RollupGroup(rollupArray) {
	this.rollups = rollupArray;
	for (var i=0; i<this.rollups.length; i++) {
		this.rollups[i].rollupGroup = this;
	}
}

RollupGroup.prototype.expandRollup = function(rollupToExpand) {
	for (var i=0; i<this.rollups.length; i++) {
		var rollup = this.rollups[i];
		var collapsed = rollup.isCollapsed();
		if (rollup == rollupToExpand) {
			if (collapsed) rollup.toggle();
		}
		else {
			if (!collapsed) rollup.toggle();
		}
	}
}