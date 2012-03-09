if (typeof DiaFlux == "undefined" || !DiaFlux) {
    var DiaFlux = {};
}

DiaFlux.Highlight = {};

DiaFlux.Highlight.getHighlights = function(actionName, infos){
	
	var params = {
		action : actionName,
        kdomid: this.flow.kdomid
	};
	
	for ( var prop in infos) {
		params[prop] = infos[prop];
	}
	
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
	
	var flowDOM = $(flowid).getElementsBySelector('.FlowchartGroup')[0];
	var flowchart = flowDOM.__flowchart;
	
	var nodes = doc.getElementsByTagName('node');
	DiaFlux.Highlight.highlightNodes(flowchart, prefix, nodes);
	
	var edges = doc.getElementsByTagName('edge');
	DiaFlux.Highlight.highlightEdges(flowchart, prefix, edges);
	
	
}

DiaFlux.Highlight.highlightNodes = function(flowchart, prefix, nodes){

	for (var i = 0; i< nodes.length; i++) {
		var node = flowchart.findNode(nodes[i].getAttribute('id'));

		DiaFlux.Highlight.addAttributesToDOM([node.getDOM()], nodes[i].attributes, prefix);
		
	}
	
}

DiaFlux.Highlight.highlightEdges = function(flowchart, prefix, edges){
	
	for (var i = 0; i < edges.length; i++) {
		var edge = flowchart.findRule(edges[i].getAttribute('id'));
		var lines = edge.getDOM().getElementsBySelector('.h_line, .v_line');
		
		DiaFlux.Highlight.addAttributesToDOM(lines, edges[i].attributes, prefix);
		
	}
	
}

DiaFlux.Highlight.addAttributesToDOM = function(doms, attributes, prefix){
	for ( var j = 0; j < doms.length; j++) {
		
		for (var k = 0; k < attributes.length; k++){
			if (attributes[k].name == "id") {
				continue; //ignore, is id of object in flowchart
			} else if (attributes[k].name == "class") {
				DiaFlux.Highlight.setHighlightClass(doms[j], attributes[k].value, prefix);
			} else {
				DiaFlux.Highlight.setAttribute(doms[j], attributes[k]);
			}
		}
	}
}

//removes all classes starting with the prefix from elem and adds the class clazz
DiaFlux.Highlight.setHighlightClass = function(elem, clazz, prefix) {
	var clazzes = elem.className;
	var newClass = clazzes.replace(new RegExp(prefix + '\\w*','i'), '');
	elem.className = newClass + ' ' + clazz;
	
}

DiaFlux.Highlight.setAttribute = function(elem, attribute) {
	elem[attribute.name] = attribute.value;
}
