if (typeof DiaFlux == "undefined" || !DiaFlux) {
    var DiaFlux = {};
}

DiaFlux.Highlight = {};

DiaFlux.Highlight.getHighlights = function(actionName, infos){
	
	infos = infos || {};
	var parentid = this.flow.parent.id;
	
	var params = {
		action : actionName,
        SectionID: this.flow.kdomid,
        parentid: parentid
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

DiaFlux.Highlight.highlight = function() {
	
	var doc = this.responseXML;
	var flows = doc.getElementsByTagName('flow');
	
	for (var i = 0; i < flows.length; i++){
		DiaFlux.Highlight.highlightFlow(flows[i]);
	}
}

DiaFlux.Highlight.highlightFlow = function(flowXML){
	var flowid = flowXML.getAttribute('id');
	if (!flowid) {
		return
	}
	var prefix = flowXML.getAttribute('cssprefix');
	
	var flowDOM = document.getElementById(flowid).getElementsByClassName('FlowchartGroup')[0];
	var flowchart = flowDOM.__flowchart;
	
	var nodes = flowXML.getElementsByTagName('node');
	DiaFlux.Highlight.highlightNodes(flowchart, prefix, nodes);
	
	var edges = flowXML.getElementsByTagName('edge');
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
		var lines = edge.getDOM().querySelectorAll('.h_line, .v_line');
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
	//style contains coordinates, so concatenate
	if (attribute.name == 'style'){
		elem.setAttribute(attribute.name, elem.getAttribute('style') + attribute.value);
	} else {
		elem.setAttribute(attribute.name, attribute.value);
	}
}
