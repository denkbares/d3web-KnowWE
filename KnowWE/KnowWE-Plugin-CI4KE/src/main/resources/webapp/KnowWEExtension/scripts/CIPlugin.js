/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

function fctGetBuildDetails( dashboardID , buildNr ) {

    var params = {
           action : 'CIAction',
           task   : 'getBuildDetails',
           id     : dashboardID,
           nr     : buildNr
       }
   
    var options = {
           url : KNOWWE.core.util.getURL( params ),
           loader: true,
           response : {
               ids : [ dashboardID + '-build-details-wrapper'],
               action : 'insert',
               fn : function(){
    				makeCIBoxesCollapsible( dashboardID );
    		   }
           }
    }
    
    new _KA( options ).send();
}

/*
 * Starts the progress-refresh function if the progressInfo html is rendered in the page 
 * (happens if someone opens a page with a dashboard where currently a build is running).
 */
jq$(window).ready(function(){
	 
	jq$('.progressInfo').each(
			function() {
				var dashboardID = jq$(this).attr('dashboardname');
				var title = jq$(this).attr('dashboardarticle');
				refreshBuildProgress(dashboardID , title);
			}
	);
	
});

/*
 * Cancels a running build, followed by a page reload.
 */

function stopRunningBuild( dashboardID , title , location ) {
	
    var params = {
           action : 'CIStopBuildAction',
           id     : dashboardID,
           topic     : title
       }
   
    var options = {
           url : KNOWWE.core.util.getURL( params ),
           loader: true,
           response : {
               fn : function(){
    				window.location = location;
    		   }
           }
    }
    
    new _KA( options ).send();
}

function helper(options){
	new _KA(options).send();
}


function CI_onErrorBehavior() {
	if (this.status == null) return;
	switch (this.status) {
	  case 0:
		// server not running, do nothing.
		break;
	  case 666:
  	    alert("There is currently already a build running for that dashbaord. Please wait.");
	    break;
	  case 404:
    	alert("This page no longer exists. Please reload.");
	    break;
	  default:
	    //alert("Error " + this.status + ". Please reload the page.");
	    break;
	}
}

/*
 * Triggers the start of a new build. Further rerenders and inserts the dashboard header including the html for the progress update.
 * After response the progress-refresh function is called. 
 */

function fctExecuteNewBuild( dashboardID,title ) {

	var params = {
            action : 'CIAction',
            task   : 'executeNewBuild',
            id     : dashboardID
        }
    
     var options = {
            url : KNOWWE.core.util.getURL( params ),
            loader: true,
            response : {
                ids : [ 'top_'+dashboardID ],
                action : 'insert',
                fn : function() {
                	refreshBuildProgress( dashboardID,title );
				},
				onError : CI_onErrorBehavior,
            }
     }
	
    new _KA(options).send();
     
}


/*
 * Repeatedly asks the state of the current build process and displays it.
 * When 'finished' is responed as progress message, the loop terminates and a page reload is triggered.
 */

function refreshBuildProgress(dashboardID , title) {
	
	var params2 = {
			action : 'CIGetProgressAction',
			id     : dashboardID,
    }; 
	var options2 = {
		url : KNOWWE.core.util.getURL(params2),
		response : {
			action : 'none',
			fn : function() {

				var percent = JSON.parse(this.responseText).progress;
				var message = JSON.parse(this.responseText).message;
				
				var progressValue = document.getElementById("progress_value");
				if(progressValue) progressValue.innerHTML = percent+" %";
				var progressText = document.getElementById("progress_text"); 
				if(progressText) progressText.innerHTML = " "+""+message+"";
				if(message != 'finished'){
					jq$.delay(helper(options2),2000);
				} else {
					window.location.reload();
				}

			}
		},
		onError : function() {
		}
	}
	
	 new _KA(options2).send();
}

function fctRefreshBuildList( dashboardID, indexFromBack, numberOfBuilds ) {

	var params = {
            action 			: 'CIAction',
            task			: 'refreshBuildList',
            id				: dashboardID,
            indexFromBack	: indexFromBack,
            numberOfBuilds	: numberOfBuilds
        }
    
     var options = {
            url : KNOWWE.core.util.getURL( params ),
            response : {
                ids : [ dashboardID + '-build-table'],
                action : 'insert'
            }
     }
     
     new _KA( options ).send();
}


/*
 * function not working?!
 */
function makeCIBoxesCollapsible( dashboardID ){
    var selector = "div .ci-collapsible-box";
    if ( dashboardID ) {
    	selector = "#" + dashboardID + selector;
    }    	
    
    var panels = _KS( selector );
    if( panels.length < 1 ) return;
    if( !panels.length ) panels = new Array(panels);
    
    for(var i = 0; i < panels.length; i++){
        var span = new _KN('span');
        span._setText('+ ');
        
        var heading = panels[i].getElementsByTagName('h4')[0];
        if (!heading) continue;
        if(!heading.innerHTML.startsWith('<span>')){
             span._injectTop( heading );
        }
        _KE.add('click', heading , function(){
            var el = new _KN( this );
            var style = el._next()._getStyle('display');
            style = (style == 'block') ? 'none' : ((style == '') ? 'none' : 'block');                    
            
            el._getChildren()[0]._setText( (style == 'block')? '- ' : '+ ' );
            el._next()._setStyle('display', style);
        });
    }
}

/* KNOWWE.helper.observer.subscribe( 'onload', makeCIBoxesCollapsible ); */
window.addEvent("domready", makeCIBoxesCollapsible);
