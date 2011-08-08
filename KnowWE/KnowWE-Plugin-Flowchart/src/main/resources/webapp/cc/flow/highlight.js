if (typeof DiaFlux == "undefined" || !DiaFlux) {
    var DiaFlux = {};
}

DiaFlux.Highlight = {};

DiaFlux.Highlight.getHighlights = function(actionName, master){
	
	params = {
		action : actionName,
        kdomid: this.flow.kdomid,
        master: master
	};
	
	var options = {
		url: KNOWWE.core.util.getURL( params ),
        response : {
            action: 'none',
            fn: DiaFlux.Highlight.highlight
        }
    };
    new _KA(options).send();
	
		
} 


DiaFlux.Highlight.highlight = function(response) {
	
	var doc = this.responseXML;
	
	var flowid = doc.getElementsByTagName('flow')[0].getAttribute('id');
	if (!flowid) {
		return
	}
	var prefix = doc.getElementsByTagName('flow')[0].getAttribute('prefix');
	var nodes = doc.getElementsByTagName('node');
	var edges = doc.getElementsByTagName('edge');
	
	var flowDOM = $(flowid).getElementsBySelector('.FlowchartGroup')[0];
	
	var flowchart = flowDOM.__flowchart;
	
	for (var i = 0; i< nodes.length; i++) {
		var colorNode = nodes[i];
		var className = KBInfo._nodeText(colorNode); //TODO
		
		var colorNodeID = colorNode.getAttribute('id');
		var node = flowchart.findNode(colorNodeID);
		DiaFlux.Highlight.setHighlightClass(node.getDOM(), className, prefix);
	}
	
	
	for (var j = 0; j< edges.length; j++) {
		var colorEdge = edges[j];
		var className = KBInfo._nodeText(colorEdge); //TODO
		var colorEdgeID = colorEdge.getAttribute('id');
		var edge = flowchart.findRule(colorEdgeID);
		var lines = edge.getDOM().getElementsBySelector('.h_line, .v_line');
		
		for ( var k = 0; k < lines.length; k++) {
			DiaFlux.Highlight.setHighlightClass(lines[k], className, prefix);
			
		}
	}
	
}

//removes all classes starting with the prefix from elem and adds the class clazz
DiaFlux.Highlight.setHighlightClass = function(elem, clazz, prefix) {
	var clazzes = elem.className;
	var newClass = clazzes.replace(new RegExp(prefix + '\\w*','i'), '');
	elem.className = newClass + ' ' + clazz;
	
}




