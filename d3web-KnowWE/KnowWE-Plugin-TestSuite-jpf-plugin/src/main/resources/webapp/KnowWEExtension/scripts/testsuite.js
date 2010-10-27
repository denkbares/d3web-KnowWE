function runTestSuite() {
	
	if (!(_KS('#testsuite-result') && _KS('#testsuite-topic'))) return;
	
	testSuiteName = _KS('#testsuite-topic').innerHTML;
	
	// default params
	params = {
		action : 'TestSuiteRunAction',
        KWikiWeb : 'default_web',
        testsuite : testSuiteName
	};
	
	// options for AJAX request
    options = {
        url : KNOWWE.core.util.getURL( params ),
        response : {
            action : 'insert',
            ids : [ 'testsuite-result' ]
        }
    };
    
    // send AJAX request
    new _KA( options ).send();
	
}

function extendTestSuiteFailed() {
	img = _KS('testsuite-failed-extend-img');
	extend = _KS('testsuite-failed-extend');
	panel = _KS('testsuite-detail-panel');
	

	if (panel.style.display == "none") {
		img.src = "KnowWEExtension/images/arrow_down.png";
		panel.style.display = "block";
		extend.style.marginLeft = "0px";
	} else {
		img.src = "KnowWEExtension/images/arrow_right.png";
		panel.style.display = "none";
		extend.style.marginLeft = "7px";
	}
	
}