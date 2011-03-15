function runTestCase() {
	
	if (!(_KS('#testcase-result') && _KS('#testcase-topic'))) return;
	
	testCaseName = _KS('#testcase-topic').innerHTML;
	
	// default params
	params = {
		action : 'TestCaseRunAction',
        KWikiWeb : 'default_web',
        testcase : testCaseName
	};
	
	// options for AJAX request
    options = {
        url : KNOWWE.core.util.getURL( params ),
        response : {
            action : 'insert',
            ids : [ 'testcase-result' ]
        }
    };
    
    // send AJAX request
    new _KA( options ).send();
	
}


function debugTestCase() {
	if (!(_KS('#testcase-result') && _KS('#testcase-topic'))) return;
	
	testCaseName = _KS('#testcase-topic').innerHTML;
	
	// default params
	params = {
		action : 'TestCaseDebugAction',
        KWikiWeb : 'default_web',
        testcase : testCaseName
	};
	
	// options for AJAX request
    options = {
        url : KNOWWE.core.util.getURL( params ),
        response : {
            action : 'insert',
            ids : [ 'testcase-result' ]
        }
    };
    
    // send AJAX request
    new _KA( options ).send();
}

function extendTestCaseFailed() {
	img = _KS('testcase-failed-extend-img');
	extend = _KS('testcase-failed-extend');
	panel = _KS('testcase-detail-panel');
	

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