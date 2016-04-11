if (typeof DiaFlux == "undefined" || !DiaFlux) {
    var DiaFlux = {};
}

DiaFlux.Error = {}

DiaFlux.Error.getErrors = function(){
	DiaFlux.Highlight.getHighlights.call(this, 'GetErrorHighlightAction', {});
	
}

KNOWWE.helper.observer.subscribe("flowchartrendered", DiaFlux.Error.getErrors);
