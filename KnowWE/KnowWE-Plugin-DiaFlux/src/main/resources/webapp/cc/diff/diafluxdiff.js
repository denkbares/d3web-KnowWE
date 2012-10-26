if (typeof DiaFlux == "undefined" || !DiaFlux) {
    var DiaFlux = {};
}

DiaFlux.Diff = {
	renderedFlows: {}
}

DiaFlux.Diff.getHighlight = function(){

	//parentid structure: <id>-<side>, with side: LEFT|RGHT
	var parentid = this.flow.parent.id;
	var id = parentid.slice(0, -5);
	var side = parentid.slice(-4);
	
	if (!DiaFlux.Diff.renderedFlows[id]){
		DiaFlux.Diff.renderedFlows[id] = {};
	}
	var renderStatus = DiaFlux.Diff.renderedFlows[id];
	
	
	renderStatus[side + 'kdomid'] = this.flow.kdomid;

	// after both sides have been rendered, call the highlighting function
	// and tell it the ids of the sections, that have been registered when 
	// creating the flowchart renderer from the old versions
	if (renderStatus.LEFTkdomid && renderStatus.RGHTkdomid){
		DiaFlux.Highlight.getHighlights.call(this, 'GetDiffHighlightAction',renderStatus);
	}
	
} 

KNOWWE.helper.observer.subscribe("flowchartrendered", DiaFlux.Diff.getHighlight);