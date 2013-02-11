var TestCasePlayer = {};

TestCasePlayer.init = function() {
	jq$(".type_TestCasePlayer").find(".wikitable").find("th").click(TestCasePlayer.registerClickableColumnHeaders);
}

TestCasePlayer.registerClickableColumnHeaders = function() {
	var th = jq$(this);
	if (th.find("input").length > 0) return;
	var column = th.attr("column");
	
	var isCollapsed = th.hasClass("collapsedcolumn");
	var tds = th.parents(".wikitable").first().find('[column="' + column + '"]');
	if (isCollapsed) {
		tds.removeClass("collapsedcolumn");
		th.attr("title", "Collapse");
		tds.filter("td").each(function() {
			jq$(this).removeAttr("title");
		});
	}
	
	var collapsed = "";
	th.siblings().each(function() {
		if (jq$(this).hasClass("collapsedcolumn")) {
			collapsed += jq$(this).attr("column") + "#";
		}
	});
	if (!isCollapsed) collapsed += column;
	
	var id = th.parents(".type_TestCasePlayer").first().attr("id");
	var testCase = jq$("#" + id).find("select").find('[selected="selected"]').attr("value");
	document.cookie = "columnstatus_" + id + "_" + testCase + "=" + collapsed;
	
	if (!isCollapsed) {
		tds.addClass("collapsedcolumn");
		th.attr("title", "Expand " + jq$(this).text());
		tds.filter("td").each(function() {
			jq$(this).attr("title", jq$(this).text());
		});
	}
	
}

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
            //KNOWWE.helper.observer.notify('update');
           	TestCasePlayer.update();
}

TestCasePlayer.addCookie = function(cookievalue)  {
			var topic = KNOWWE.helper.gup('page');
			document.cookie = "additionalQuestions"+ TestCasePlayer.encodeCookieValue(topic) +"=" + TestCasePlayer.encodeCookieValue(cookievalue);
           	//KNOWWE.helper.observer.notify('update');
           	TestCasePlayer.update();
}

TestCasePlayer.encodeCookieValue = function(cookievalue) {
			var temp = escape(cookievalue);
			temp = temp.replace('@', '%40');
			temp = temp.replace('+', '%2B');
			return temp;
}

TestCasePlayer.update = function() {
	jq$(".ReRenderSectionMarker").each(function() {
		var id = eval("(" + jq$(this).attr("rel") + ")" ).id;
		var params = {
            action: 'ReRenderContentPartAction',
            KdomNodeId: id,
        };

        var options = {
            url: KNOWWE.core.util.getURL(params),
            response: {
                action: 'none',
                fn: function() {
                	var tableDiv = jq$("#" + id).find('.' + "wikitable").parent();
                	var scrollLeft = tableDiv.scrollLeft();
                	var scrollWidth = tableDiv[0].scrollWidth;
                	
                    jq$("#" + id).replaceWith(this.responseText);
                    
                    tableDiv = jq$("#" + id).find('.' + "wikitable").parent();
                    var scrollWidthAfter = tableDiv[0].scrollWidth;
                    if (scrollWidth < scrollWidthAfter) {
                    	scrollLeft += scrollWidthAfter - scrollWidth;
                    }
                    tableDiv.scrollLeft(scrollLeft);
                    
                    tableDiv.find("th").click(TestCasePlayer.registerClickableColumnHeaders);
                    
		        	KNOWWE.core.util.updateProcessingState(-1);
                },
            }
        };
        new _KA(options).send();
    	KNOWWE.core.util.updateProcessingState(1);
	});
}

jq$(document).ready(function() {
	TestCasePlayer.init();
});