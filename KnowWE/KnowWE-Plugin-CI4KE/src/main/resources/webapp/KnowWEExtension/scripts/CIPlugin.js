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

function fctGetBuildDetails(dashboardName, buildNr) {

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
}

/*
 * Cancels a running build, followed by a page reload.
 */
function stopRunningBuild(dashboardName, title, location) {

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
}

function CI_onErrorBehavior() {
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

/*
 * Triggers the start of a new build. Afterward just a page reload is called,
 * which then renders progress info html stuff.
 */
function fctExecuteNewBuild(dashboardName, title) {

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
			onError : CI_onErrorBehavior,
		}
	}

	new _KA(options).send();

}

/*
 * Repeatedly asks the state of the current build process and displays it. When
 * 'finished' is responed as progress message, the loop terminates and a page
 * reload is triggered.
 */
function refreshBuildProgress(dashboardName) {
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
}

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
			onError : CI_onErrorBehavior,
		}
	}

	new _KA(options).send();

}

/*
 * Fetches the list of build numbers/states when the left/right navigation
 * buttons for showing earlier/later builds are pressed. It also updates the
 * build details panel on the right correspondingly.
 * 
 */
function fctRefreshBuildList(dashboardName, indexFromBack, numberOfBuilds) {

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
}

/*
 * Starts the progress-refresh function if the progressInfo html or some
 * ci-deamon is rendered in the page (happens if someone opens a page with a
 * dashboard where currently a build is running).
 */
jq$(window).ready(function() {

	// trigger dashboard progress update
	jq$('.ci-progress-info').each(function() {
		var dashboardName = jq$(this).parents('.ci-title').attr('name');
		refreshBuildProgress(dashboardName);
	});

});
