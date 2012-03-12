var TestCasePlayer = {};

TestCasePlayer.send = function(sessionid, casedate, name, topic) {
			
            var params = {
        		action : 'ExecuteCasesAction',
       			KWiki_Topic : topic,
       			id : sessionid,
        		date : casedate,
        		testCaseName : name
    		}
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                response : {
                	action : 'none',
                	fn : function(){
			        	try {
	                		KNOWWE.helper.observer.notify('update');
			        	}
			        	catch (e) { /*ignore*/ }
			        	KNOWWE.core.util.updateProcessingState(-1);
                	},
                    onError : function () {
			        	KNOWWE.core.util.updateProcessingState(-1);                    	
                    }
                }
            }
        	KNOWWE.core.util.updateProcessingState(1);
            new _KA( options ).send();         
        }
        
TestCasePlayer.change = function(key_sessionid, selectedvalue) {
 			var topic = KNOWWE.helper.gup('page');
			document.cookie = key_sessionid +"=" + encodeURI(selectedvalue);
           	KNOWWE.helper.observer.notify('update'); 

}

TestCasePlayer.addCookie = function(cookievalue) {
			var topic = KNOWWE.helper.gup('page');
			document.cookie = "additionalQuestions"+ encodeURI(topic) +"=" + encodeURI(cookievalue);
           	KNOWWE.helper.observer.notify('update');
}

TestCasePlayer.reset = function(kbidvalue) {
			var params = {
        		action : 'PlayerResetAction',
        		kbid : kbidvalue
    		}
            
            var options = {
                url : KNOWWE.core.util.getURL(params),
                response : {
                	action : 'none',
                	fn : function(){
			        	try {
	                		KNOWWE.helper.observer.notify('update');
			        	}
			        	catch (e) { /*ignore*/ }
			        	KNOWWE.core.util.updateProcessingState(-1);
                	},
                    onError : function () {
			        	KNOWWE.core.util.updateProcessingState(-1);                    	
                    }
                }
            }
        	KNOWWE.core.util.updateProcessingState(1);
            new _KA( options ).send();
}