DiaFlux.SubLink = {}

DiaFlux.SubLink.getLinks = function(){
	
	
	params = {
		action : 'GetSubflowLinksAction',
        kdomid: this.flow.kdomid,
	};
	
	var options = {
		url: KNOWWE.core.util.getURL( params ),
        response : {
            action: 'none',
            fn: DiaFlux.SubLink.setLinks
        }
    };
    new _KA(options).send();
		
} 

DiaFlux.SubLink.setLinks = function(response) {
	
	var doc = this.responseXML;
	
	var flowid = doc.getElementsByTagName('flow')[0].getAttribute('id');
	if (!flowid) {
		return
	}
	var prefix = doc.getElementsByTagName('flow')[0].getAttribute('prefix');
	var nodes = doc.getElementsByTagName('node');
	
	var flowDOM = $(flowid).getElementsBySelector('.FlowchartGroup')[0];
	
	var flowchart = flowDOM.__flowchart;
	
	for (var i = 0; i< nodes.length; i++) {
		var nodeInfo = nodes[i];
		var link = KBInfo._nodeText(nodeInfo); //TODO
		
		var colorNodeID = nodeInfo.getAttribute('id');
		var node = flowchart.findNode(colorNodeID);
		node.dom.innerHTML = "<a href='" + link +"'>" + node.dom.innerHTML +"</a>";
	}
	
}

KNOWWE.helper.observer.subscribe("flowchartrendered", DiaFlux.SubLink.getLinks);
