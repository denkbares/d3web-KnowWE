/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
    var KNOWWE = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
    KNOWWE.plugin = {};
}


/**
 * Namespace: KNOWWE.core.plugin.instantedit The KNOWWE instant edit namespace.
 */
KNOWWE.plugin.ci4ke = function() {
	
	/*
	 * Fetches the ci state bubble html code for a deamon/dashboard and insertes it.
	 * This is called after some build process has been finished on the server to
	 * update the view correspondingly.
	 */
	function refreshCIDeamonBubble(dashboardName) {
	
		var params = {
			action : 'CIAction',
			task : 'refreshBubble',
			name : dashboardName,
		}
	
		var options = {
			url : KNOWWE.core.util.getURL(params),
			loader : true,
			response : {
				ids : [ 'state_' + dashboardName ],
				action : 'replace',
				onError : onErrorBehavior,
			}
		}
	
		new _KA(options).send();
	
	}
	
	function onErrorBehavior() {
		if (this.status == null)
			return;
		switch (this.status) {
		case 0:
			// server not running, do nothing.
			break;
		case 666:
			alert("There already is a build running for this dashbaord. Please abort the running build before starting a new one.");
			break;
		case 404:
			alert("This page no longer exists. Please reload.");
			break;
		default:
			// alert("Error " + this.status + ". Please reload the page.");
			break;
		}
	}
	
	return {
	
		getBuildDetails : function(dashboardName, buildNr) {

			var params = {
				action : 'CIAction',
				task : 'getBuildDetails',
				name : dashboardName,
				nr : buildNr
			}

			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : true,
				response : {
					ids : [ dashboardName + '-build-details-wrapper' ],
					action : 'insert',
				}
			}

			new _KA(options).send();
		},
		
		/*
		 * Cancels a running build, followed by a page reload.
		 */
		stopRunningBuild : function(dashboardName, title, location) {
		
			var params = {
				action : 'CIStopBuildAction',
				name : dashboardName,
				topic : title
			}
		
			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : true,
				response : {
					fn : function() {
						window.location = location;
					}
				}
			}
		
			new _KA(options).send();
		},

		
		/*
		 * Triggers the start of a new build. Afterward just a page reload is called,
		 * which then renders progress info html stuff.
		 */
		executeNewBuild : function(dashboardName, title) {
		
			var params = {
				action : 'CIAction',
				task : 'executeNewBuild',
				name : dashboardName
			}
		
			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : true,
				response : {
					fn : function() {
						window.location.reload();
					},
					onError : onErrorBehavior,
				}
			}
		
			new _KA(options).send();
		},
		
		/*
		 * Repeatedly asks the state of the current build process and displays it. When
		 * 'finished' is responed as progress message, the loop terminates and a page
		 * reload is triggered.
		 */
		refreshBuildProgress : function(dashboardName) {
			var error = false;
			var params = {
				action : 'CIGetProgressAction',
				name : dashboardName,
			};
			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					action : 'none',
					fn : function() {
						if (error)
							return;
						var percent = JSON.parse(this.responseText).progress;
						var message = JSON.parse(this.responseText).message;
		
						var progressValue = jq$("#" + dashboardName + "_progress-value");
						if (progressValue)
							progressValue.text(percent + " %");
						var progressText = jq$("#" + dashboardName + "_progress-text");
						if (progressText)
							progressText.text(" " + message);
						if (message != 'Finished') {
							setTimeout(function() {
								new _KA(options).send()
							}, 500);
						} else {
							// lets just refresh...
							// refreshCIDeamonBubble(dashboardName);
							window.location.reload();
						}
					},
					onError : function() {
						var progressText = jq$("#" + dashboardName + "_progress-text");
						if (progressText)
							progressText
									.text(" Exception while updating progress. Please reload manually.");
					}
				}
			}
			new _KA(options).send();
		},
		
		/*
		 * Repeatedly whether the current build process is still running.
		 * When 'finished' is responded as progress message, 
		 * the loop terminates and a refresh call of the state bubble is called.
		 */
		refreshBuildProgressDeamon : function(dashboardName) {
			
			var params = {
					action : 'CIGetProgressAction',
					name   : dashboardName,
		    }; 
			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					action : 'none',
					fn : function() {
		
						var message = JSON.parse(this.responseText).message;
						
						if(message != 'Finished'){
							setTimeout(function() {
								new _KA(options).send()
							}, 1000);
						} else {
							refreshCIDeamonBubble(dashboardName);
						}
		
					}
				},
				onError : function() {
				}
			}
			
			 new _KA(options).send();
		},
		
		
		/*
		 * Fetches the list of build numbers/states when the left/right navigation
		 * buttons for showing earlier/later builds are pressed. It also updates the
		 * build details panel on the right correspondingly.
		 * 
		 */
		refreshBuildList : function(dashboardName, indexFromBack, numberOfBuilds) {
		
			var params = {
				action : 'CIAction',
				task : 'refreshBuildList',
				name : dashboardName,
				indexFromBack : indexFromBack,
				numberOfBuilds : numberOfBuilds
			}
		
			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					ids : [ dashboardName + '-build-table' ],
					action : 'insert'
				}
			}
		
			new _KA(options).send();
		},
	}
}();

/**
 * Alias for some to reduce typing.
 */
var _CI = KNOWWE.plugin.ci4ke;

/*
 * Starts the progress-refresh function if the progressInfo html or some
 * ci-deamon is rendered in the page (happens if someone opens a page with a
 * dashboard where currently a build is running).
 */
jq$(window).ready(function() {

	// trigger dashboard progress update
	jq$('.ci-progress-info').each(function() {
		var dashboardName = jq$(this).parents('.ci-title').attr('name');
		_CI.refreshBuildProgress(dashboardName);
	});
	
	// trigger request loop asking whether build is finished to stop daemon on LeftMenu
	jq$('.ci-state').each(
			function() {
				var dashboardName = jq$(this).attr('dashboardName');
				var runs = jq$(this).attr('running');
				if(runs) {
					_CI.refreshBuildProgressDeamon(dashboardName);
				}
			}
	)

});
