if (typeof DiaFlux == "undefined" || !DiaFlux) {
    var DiaFlux = {};
}

DiaFlux.Trace = {}

DiaFlux.Trace.getTrace = function(){
	DiaFlux.Highlight.getHighlights.call(this, 'GetTraceHighlightAction', {});
	
}

DiaFlux.Trace.refreshAllTraces = function() {
	
	var flows = document.querySelectorAll('.FlowchartGroup');
	
	for ( var i = 0; i < flows.length; i++) {
		DiaFlux.Trace.getTrace.call({flow: flows[i].__flowchart});
	}
		
} 

KNOWWE.helper.observer.subscribe("flowchartrendered", DiaFlux.Trace.getTrace);
KNOWWE.helper.observer.subscribe("update", DiaFlux.Trace.refreshAllTraces);