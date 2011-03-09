//Rule event handlers
//register click-listener for that class
CCEvents.addClassListener('click', 'ArrowTool', function(event) {/*NOP, but avoid bubbling*/});


//register select click events for flowchart
CCEvents.addClassListener('click', 'Rule', 
	function(event) {
		this.__rule.select();
	}
);


Rule.prototype.createDraggable = function() {
	var newDrag = new Draggable(this.getDOM(), {
		ghosting: false,
		revert: true, 
		starteffect: null,
		endeffect: null
	});
	newDrag.__rule = this;
	return newDrag;	
}



Rule.prototype.toXML = function() {
	var xml = '\t<edge' +
			(this.fcid ? ' fcid="'+this.fcid+'"' : '')+
			'>\n';
	xml += '\t\t<origin>'+this.sourceNode.getNodeModel().fcid+'</origin>\n';
	xml += '\t\t<target>'+this.targetNode.getNodeModel().fcid+'</target>\n';
	if (this.guard && this.guard.getMarkup() != 'NOP') {
		if (DiaFluxUtils.isString(this.guard)) {
			xml += '\t\t<guard>' + this.guard + '</guard>\n';
		}
		else {
			xml += '\t\t<guard markup="'+this.guard.getMarkup()+'">' +
					this.guard.getConditionString()+
					'</guard>\n';
		}
	}
	xml += '\t</edge>\n';
	return xml;
}


