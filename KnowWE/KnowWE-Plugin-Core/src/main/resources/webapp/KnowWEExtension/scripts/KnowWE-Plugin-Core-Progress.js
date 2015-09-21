/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

if (typeof KNOWWE.core == "undefined" || !KNOWWE.core) {
	KNOWWE.core = {};
}

if (typeof KNOWWE.core.plugin == "undefined" || !KNOWWE.core.plugin) {
	KNOWWE.core.plugin = {};
}

KNOWWE.helper.observer.subscribe("beforeRerender", function() {
	jq$('.progress-container').each(function() {
		KNOWWE.core.plugin.progress.cache[jq$(this).attr('id')] = this;
	});
});

KNOWWE.helper.observer.subscribe("afterRerender", function() {
	jq$('.progress-container').each(function() {
		jq$(this).replaceWith(KNOWWE.core.plugin.progress.cache[jq$(this).attr('id')]);
	});
});


/**
 * Namespace: KNOWWE.core.plugin.progress
 */
KNOWWE.core.plugin.progress = function() {

	function handleErrResponse() {
		var status = this.status;
		if (status == null) status = -1;
		switch (status) {
			case 404:
				KNOWWE.notification.error(null, "The page is outdated. Please reload first.", "lop_404");
				break;
			case 412:
				KNOWWE.notification.error(null, "The operation is already running, please stop it first.", "lop_412");
				break;
			default:
				KNOWWE.notification.error(null, "Error #" + status + ". Please reload the page.", "lop_unexpected");
				break;
		}
	}

	function removeAllErrors() {
		KNOWWE.notification.removeNotification("lop_412");
		KNOWWE.notification.removeNotification("lop_unexpected");
	}

	return {

		cache : {},

		startLongOperation : function(sectionID, operationID, parameters) {
			KNOWWE.core.util.updateProcessingState(1);
			var progressID = new Date().getMilliseconds() + Math.floor((Math.random() * 10) + 1);

			var params = {
				action : 'StartOperationAction',
				SectionID : sectionID,
				OperationID : operationID,
				ProgressID : progressID
			};

			jq$.extend(params, parameters);

			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : false,
				response : {
					fn : function() {
					},
					onError : handleErrResponse
				}
			};

			// remove old errors
			removeAllErrors();

			// remove old progress indicators (create a new one)
			var container = jq$("#" + sectionID + " .progress-container");
			container.find("#" + operationID).remove();

			new _KA(options).send();

			params = {
				action : 'StartProgressAction',
				SectionID : sectionID,
				OperationID : operationID,
				ProgressID : progressID
			};

			jq$.extend(params, parameters);

			options = {
				url : KNOWWE.core.util.getURL(params),
				loader : false,
				response : {
					fn : function() {
						KNOWWE.core.plugin.progress.updateProgressBar(sectionID);
					},
					onError : handleErrResponse
				}
			};

			new _KA(options).send();
		},

		cancelLongOperation : function(sectionID, operationID) {

			var params = {
				action : 'CancelOperationAction',
				SectionID : sectionID,
				OperationID : operationID
			}

			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : false,
				response : {
					fn : function() {
					},
					onError : handleErrResponse
				}
			}

			new _KA(options).send();
		},

		removeLongOperation : function(sectionID, operationID) {

			var params = {
				action : 'RemoveOperationAction',
				SectionID : sectionID,
				OperationID : operationID
			}

			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : false,
				response : {
					fn : function() {
						KNOWWE.core.plugin.progress.updateProgressBar(sectionID);
					},
					onError : handleErrResponse
				}
			}

			new _KA(options).send();
		},

		// we remember which bars are hidden/removed, otherwise they can be added again accidentally, because the ajax
		// to remove and refresh could be timed unfortunately
		hiddenProgress : {},

		updateProgressBar : function(sectionId, refreshTilProgress) {

			var params = {
				action : 'GetProgressAction',
				SectionID : sectionId
			};

			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : false,
				response : {
					fn : function() {
						var json = eval(this.responseText);
						var container = jq$("#" + sectionId + " .progress-container");
						var refresh = false;
						for (var i = 0; i < json.length; i++) {
							var opId = json[i].operationID;
							if (KNOWWE.core.plugin.progress.hiddenProgress[opId]) continue;
							var progress = json[i].progress;
							var message = json[i].message;
							var error = json[i].error;
							var running = json[i].running;
							var bar = container.find("#" + opId);
							if (bar.length == 0) {
								container.append("<div id='" + opId + "'>" +
									"<div class='progress-state'></div>" +
									"<div class='progress-bar'>" +
									"<span class='progress-bar-percent'>0 %</span>" +
									"</div>" +
									"<div class='progress-message'></div></div>");
								bar = container.find("#" + opId);
							}
							bar.find(".progress-state").attr('title', "click to cancel").click(function() {
								KNOWWE.core.plugin.progress.cancelLongOperation(sectionId, jq$(this).parent().attr('id'));
							});
							bar.removeClass("progress-error progress-success");
							var percent = Math.floor(progress * 100);
							bar.find(".progress-bar").progressbar({value : percent});
							bar.find(".progress-bar-percent").text(percent + " %");
							bar.find(".progress-message").html(message);
							var hasLineBreaks = /<\/?(br|p)\/?>|\\n/.test(message);
							if (hasLineBreaks) {
								bar.find(".progress-message").css('display', 'block');
							}
							if (!running) {
								var closeFunction = function(event) {
									var bar = jq$(event.target).parent();
									bar.remove();
									var barOpId = bar.attr('id');
									KNOWWE.core.plugin.progress.hiddenProgress[barOpId] = true;
									KNOWWE.core.plugin.progress.removeLongOperation(sectionId, barOpId);
									removeAllErrors();
								};
								bar.find(".progress-state").unbind("click").click(closeFunction);
								if (!bar.find(".progress-close").exists()) {
									var closeButton = "<a class='progress-close'>[Hide]</a>";
									if (hasLineBreaks) {
										bar.find('.progress-bar').after(closeButton);
									} else {
										bar.append(" " + closeButton);
									}
								}
								bar.find(".progress-close").unbind("click").click(closeFunction);
								if (error) {
									bar.addClass("progress-error");
									bar.find(".progress-message").html(error);
									bar.find(".progress-state").attr("title", "aborted, click to hide");
									KNOWWE.helper.observer.notify('longOperationAborted', {
										sectionId : sectionId,
										opId : opId
									});
								}
								else {
									bar.addClass("progress-success");
									bar.find(".progress-state").attr("title", "succeeded, click to hide");
									KNOWWE.helper.observer.notify('longOperationSuccessful', {
										sectionId : sectionId,
										opId : opId
									});
								}
							}
							refresh |= running;
						}
						if (refresh) {
							window.setTimeout(function() {
								KNOWWE.core.plugin.progress.updateProgressBar(sectionId);
							}, 100);
						}
						KNOWWE.core.util.updateProcessingState(-1);
					},
					onError : handleErrResponse
				}
			}

			new _KA(options).send();
		}
	}
}();