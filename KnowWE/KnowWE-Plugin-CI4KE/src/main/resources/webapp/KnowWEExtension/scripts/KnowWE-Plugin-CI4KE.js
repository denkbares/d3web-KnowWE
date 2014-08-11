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
				fn : function() {
					jq$('.ci-header,.ci-daemon').find('.ci-state')
					        .filter('[dashboardName="' + dashboardName + '"]')
					        .replaceWith(this.response);
				},
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
			KNOWWE.notification.error(null, "Server appears to be offline.", status);
			break;
		case 409:
			KNOWWE.notification.error(null, "There already is a build running for this dashbaord. Please abort the running build before starting a new one.", status);
			break;
		case 404:
			KNOWWE.notification.error(null, "This page no longer exists. Please reload.", status);
			break;
		default:
			// alert("Error " + this.status + ". Please reload the page.");
			break;
		}
	}

	return {

		 expandMessage : function(button) {
			 var $expandButton = jq$(button);
			 var $collapseButton = $expandButton.parent().children('.collapseCIMessage');
			 var $message = $expandButton.parent().children('.ci-message');
			 $message.show('fast', function() {
				$expandButton.hide();
				$collapseButton.show();
			 });
		},

		collapseMessage : function(button) {
			 var $collapseButton = jq$(button);
			 var $expandButton = $collapseButton.parent().children('.expandCIMessage');
			 var $message = $collapseButton.parent().children('.ci-message');
			 $message.hide('fast', function() {
				$collapseButton.hide();
				$expandButton.show();
			 });
		},

		refreshBuildDetails : function(dashboardName, buildNr) {

			var params = {
				action : 'CIAction',
				task : 'refreshBuildDetails',
				name : dashboardName,
			}

			if (buildNr != null) {
				params["nr"] = buildNr;
			}

			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : true,
				response : {
					ids : [ dashboardName + '-build-details-wrapper' ],
					action : 'insert',
					fn : function() {
						// (re-)activate incoming script tags for collapsing
						var result = null;
						var rePattern = /<script>(.*)<\/script>/gi;
						while (result = rePattern.exec(this.responseText)) {
							var script = result[1];
							eval(script);
						}
					}
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
						_CI.refreshBuildProgress(dashboardName);
						refreshCIDeamonBubble(dashboardName);
						_CI.refreshBuildProgressDeamon(dashboardName);

						// make not-up-to-date warning disappear
						jq$('.ci-title').each(function() {
							if(jq$(this).attr('name') == dashboardName) {
								jq$(this).find('.warning').hide();
							}
						});
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

						var pv = document.getElementById(dashboardName + "_progress-value");
						if(pv) pv.innerHTML = percent + " %";
						var pt = document.getElementById(dashboardName + "_progress-text");
						if(pt) pt.innerHTML = message;

						if (message != 'Finished') {
							jq$('[name="' + dashboardName +'"]').find('.ci-progress-info').show();
							setTimeout(function() {
								new _KA(options).send()
							}, 500);
						} else {
							jq$('[name="' + dashboardName +'"]').find('.ci-progress-info').fadeOut(500);
							jq$('#modified-warning_' + dashboardName).parents('.warning').remove();
							_CI.refreshBuildDetails(dashboardName);
							_CI.refreshBuildList(dashboardName);
							_CI.refreshBuildStatus(dashboardName);
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
		 * Repeatedly check whether the current build process is still running.
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
		refreshBuildList : function(dashboardName, clickedIndex, indexFromBack, numberOfBuilds) {

			var params = {
				action : 'CIAction',
				task : 'refreshBuildList',
				name : dashboardName,
			}

			if (clickedIndex != null) {
				params["nr"] = clickedIndex;
			}

			if (indexFromBack != null) {
				params["indexFromBack"] = indexFromBack;
			}

			if (numberOfBuilds != null) {
				params["numberOfBuilds"] = numberOfBuilds;
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

		refreshBuildStatus : function(dashboardName) {

			var params = {
				action : 'CIAction',
				task : 'refreshBuildStatus',
				name : dashboardName,
			}

			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					ids : [ 'ci-header_' + dashboardName ],
					action : 'replace'
				}
			}

			new _KA(options).send();
		}
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
	jq$('.ci-header').find('.ci-state').each(function() {
		var runs = jq$(this).attr('running');
		if(runs) {
			var dashboardName = jq$(this).attr('dashboardName');
			_CI.refreshBuildProgress(dashboardName);
		}
	});

	// trigger request loop asking whether build is finished to stop daemon on LeftMenu
	jq$('.ci-daemon').find('.ci-state').each(
			function() {
				var runs = jq$(this).attr('running');
				if (runs) {
					var dashboardName = jq$(this).attr('dashboardName');
					_CI.refreshBuildProgressDeamon(dashboardName);
				}
			}
	)

});
