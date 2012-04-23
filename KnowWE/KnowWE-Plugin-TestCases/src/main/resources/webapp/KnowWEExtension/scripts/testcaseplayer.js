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
			        	if (this.status == null) return;
        	switch (this.status) {
        	  case 0:
        		// server not running, do nothing.
        		break;
        	  case 409:
          	    alert("The section has changed since you " 
          	    		+ "loaded this page. Please reload the page.");
        	    break;
        	  default:
        	    alert("Error " + this.status + ". Please reload the page.");
        	    break;
        	}                	
                    }
                }
            }
        	KNOWWE.core.util.updateProcessingState(1);
            new _KA( options ).send();         
        }
        
TestCasePlayer.change = function(key_sessionid, selectedvalue) {
 			var topic = KNOWWE.helper.gup('page');
			document.cookie = key_sessionid +"=" + TestCasePlayer.encodeCookieValue(selectedvalue);
           	KNOWWE.helper.observer.notify('update'); 

}

TestCasePlayer.addCookie = function(cookievalue) {
			var topic = KNOWWE.helper.gup('page');
			document.cookie = "additionalQuestions"+ TestCasePlayer.encodeCookieValue(topic) +"=" + TestCasePlayer.encodeCookieValue(cookievalue);
           	KNOWWE.helper.observer.notify('update');
}

TestCasePlayer.encodeCookieValue = function(cookievalue) {
			var temp = escape(cookievalue);
			temp = temp.replace('@', '%40');
			temp = temp.replace('+', '%2B');
			return temp;
}