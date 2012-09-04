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
 * Starts the progress-refresh function if the progressInfo html or some ci-deamon is rendered in the page 
 * (happens if someone opens a page with a dashboard where currently a build is running).
 */
jq$(window).ready(function(){
	 
	// trigger dashboard progress update
	jq$('.progressInfo').each(
			function() {
				var dashboardID = jq$(this).attr('dashboardname');
				var title = jq$(this).attr('dashboardarticle');
				refreshBuildProgress(dashboardID , title);
			}
	);
	
	// trigger request loop asking whether build is finished
	jq$('.cideamon_state').each(
			function() {
				var dashboardID = jq$(this).attr('dashboardname');
				var runs = jq$(this).attr('running');
				if(runs) {
					refreshBuildProgressDeamon(dashboardID);
				}
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
 * Triggers the start of a new build. 
 * Afterward just a page reload is called, which then renders progress info html stuff. 
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
                fn : function() {
                	window.location.reload();
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
	
	var params = {
			action : 'CIGetProgressAction',
			id     : dashboardID,
    }; 
	var options = {
		url : KNOWWE.core.util.getURL(params),
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
					jq$.delay(helper(options),2000);
				} else {
					window.location.reload();
				}
			}
		},
		onError : function() {
		}
	}
	
	 new _KA(options).send();
}


/*
 * Repeatedly whether the current build process is still running.
 * When 'finished' is responded as progress message, 
 * the loop terminates and a refresh call of the state bubble is called.
 */
function refreshBuildProgressDeamon(dashboardID) {
	
	var params = {
			action : 'CIGetProgressAction',
			id     : dashboardID,
    }; 
	var options = {
		url : KNOWWE.core.util.getURL(params),
		response : {
			action : 'none',
			fn : function() {

				var message = JSON.parse(this.responseText).message;
				
				if(message != 'finished'){
					jq$.delay(helper(options2),2000);
				} else {
					refreshCIDeamonBubble(dashboardID);
				}

			}
		},
		onError : function() {
		}
	}
	
	 new _KA(options).send();
}

/*
 * Fetches the ci state bubble html code for a deamon/dashboard and insertes it.
 * This is called after some build process 
 * has been finished on the server to update the view correspondingly.
 */
function refreshCIDeamonBubble( dashboardID) {

	var params = {
            action : 'CIAction',
            task   : 'refreshBubble',
            id     : dashboardID
        }
    
     var options = {
            url : KNOWWE.core.util.getURL( params ),
            loader: true,
            response : {
                ids : [ 'state_'+dashboardID ],
                action : 'replace',
				onError : CI_onErrorBehavior,
            }
     }
	
    new _KA(options).send();
     
}

/*
 * Fetches the list of build numbers/states when the left/right navigation buttons 
 * for showing earlier/later builds are pressed.
 * It also updates the build details panel on the right correspondingly.
 * 
 */
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
