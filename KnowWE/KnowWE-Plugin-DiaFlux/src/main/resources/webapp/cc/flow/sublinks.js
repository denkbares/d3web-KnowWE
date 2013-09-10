DiaFlux.SubLink = {}

DiaFlux.SubLink.getLinks = function(){
	
	var flow = this.flow;
	
	params = {
		action : 'GetSubflowLinksAction',
        kdomid: flow.kdomid,
	};
	
	var options = {
		url: KNOWWE.core.util.getURL( params ),
        response : {
            action: 'none',
            fn: function(){DiaFlux.SubLink.setLinks(flow, this.responseXML);}
        }
    };
    new _KA(options).send();
		
} 

DiaFlux.SubLink.setLinks = function(flow, xml) {
	
	var flowid = xml.getElementsByTagName('flow')[0].getAttribute('id');
	if (!flowid) {
		return
	}
	var prefix = xml.getElementsByTagName('flow')[0].getAttribute('prefix');
	var nodes = xml.getElementsByTagName('node');
	
	var flowDOM = $(flowid).getElementsBySelector('.FlowchartGroup')[0];
	
	var flowchart = flowDOM.__flowchart;
	
	for (var i = 0; i< nodes.length; i++) {
		var nodeInfo = nodes[i];
		var link = KBInfo._nodeText(nodeInfo); //TODO
		
		var colorNodeID = nodeInfo.getAttribute('id');
		var node = flowchart.findNode(colorNodeID);
		node.dom.innerHTML = "<a href='" + link +"'>" + node.dom.innerHTML +"</a>";
	}
	
	KNOWWE.helper.observer.notify("flowchartlinked", {flow: flow});
}

KNOWWE.helper.observer.subscribe("flowchartrendered", DiaFlux.SubLink.getLinks);
