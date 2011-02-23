var FlowEditor = {

	imagePath: "cc/image/"
};

//register select and edit click events for node
CCEvents.addClassListener('click', 'Node', 
	function(event) {
		var ctrlKey = event.ctrlKey;
		var altKey = event.altKey;
		var metaKey = event.metaKey;
		if (this.__node) this.__node.select(ctrlKey | altKey | metaKey);
	}
);

CCEvents.addClassListener('dblclick', 'Node', 
	function(event) {
	//avoids error when dblclick on prototype
		if (this.__node)
			this.__node.edit();
	}
);

//register click-listener for that class
CCEvents.addClassListener('click', 'ArrowTool', function(event) {/*NOP, but avoid bubbling*/});


//register select click events for flowchart
CCEvents.addClassListener('click', 'Rule', 
	function(event) {
		this.__rule.select();
	}
);

