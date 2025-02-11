KNOWWE = typeof KNOWWE === "undefined" ? {} : KNOWWE;
KNOWWE.core = KNOWWE.core || {};
KNOWWE.core.plugin = KNOWWE.core.plugin || {}

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

  function getKey(operationID) {
    return 'knowwe-progress-visibility-' + operationID;
  }

  function showProgress(operationID) {
    localStorage.setItem(getKey(operationID), "true");
  }

  function hideProgress(operationID) {
    localStorage.setItem(getKey(operationID), "false");
  }

  function isProgressShown(operationID) {
    let visible = localStorage.getItem(getKey(operationID));
    return typeof visible === "undefined" || visible === null || visible === "true";
  }

  return {

    cache: {},

    startLongOperation: function(sectionID, operationID, parameters, onSuccessFunction) {

      if (typeof onSuccessFunction == 'function') {
        var onSuccessCaller = function() {
          onSuccessFunction(operationID);
          KNOWWE.helper.observer.unsubscribe("longOperationSuccessful", onSuccessCaller);
        };
        KNOWWE.helper.observer.subscribe("longOperationSuccessful", onSuccessCaller);
      }


      var progressID = new Date().getMilliseconds() + Math.floor((Math.random() * 10) + 1);

      var params = {
        action: 'StartOperationAction',
        SectionID: sectionID,
        OperationID: operationID,
        ProgressID: progressID
      };

      jq$.extend(params, parameters);

      // Start the long operation
      var options = {
        url: KNOWWE.core.util.getURL(params),
        loader: false,
        response: {
          fn: function() {
          },
          onError: handleErrResponse
        }
      };

      // remove old errors
      removeAllErrors();

      KNOWWE.helper.observer.notify('longOperationStarted', {
        sectionId: sectionID,
        opId: operationID
      });

      new _KA(options).send();

      // Start the progress updates for the longer operations. We need a separate action here, because the request
      // starting the actual operation does not return to the client until the operation is finished
      params = {
        action: 'StartProgressAction',
        SectionID: sectionID,
        OperationID: operationID,
        ProgressID: progressID
      };

      jq$.extend(params, parameters);

      options = {
        url: KNOWWE.core.util.getURL(params),
        loader: false,
        response: {
          fn: function() {
            showProgress(operationID);
            KNOWWE.core.plugin.progress.updateProgressBar(sectionID);
          },
          onError: handleErrResponse
        }
      };

      new _KA(options).send();


    },

    cancelLongOperation: function(sectionID, operationID) {

      var params = {
        action: 'CancelOperationAction',
        SectionID: sectionID,
        OperationID: operationID
      }

      var options = {
        url: KNOWWE.core.util.getURL(params),
        loader: false,
        response: {
          fn: function() {
          },
          onError: handleErrResponse
        }
      }

      new _KA(options).send();
    },

    removeLongOperation: function(sectionID, operationID) {
      jq$('#' + operationID).remove();
      hideProgress(operationID);
      removeAllErrors();

      var params = {
        action: 'RemoveOperationAction',
        SectionID: sectionID,
        OperationID: operationID
      }

      var options = {
        url: KNOWWE.core.util.getURL(params),
        loader: false,
        response: {
          fn: function() {
            KNOWWE.core.plugin.progress.updateProgressBar(sectionID);
          },
          onError: handleErrResponse
        }
      }

      new _KA(options).send();
    },

    // we remember which bars are hidden/removed, otherwise they can be added again accidentally, because the ajax
    // to remove and refresh could be timed unfortunately
    hiddenProgress: {},

    updateProgressBars: function() {
      jq$(document).find('.defaultMarkupFrame').each(function() {
        if (jq$(this).find('.long-op-progress-container').exists()) {
          KNOWWE.core.plugin.progress.updateProgressBar(jq$(this).attr('id'));
        }
      });
    },

    updateProgressBar: function(sectionId, refreshTilProgress) {
      if (!KNOWWE.helper.loadCheck(['Wiki.jsp'])) return;

      var params = {
        action: 'GetProgressAction',
        SectionID: sectionId
      };

      var options = {
        url: KNOWWE.core.util.getURL(params),
        loader: false,
        response: {
          fn: function() {
            var json = eval(this.responseText);
            var container = jq$("#" + sectionId + " .long-op-progress-container");
            var refresh = false;
            for (var i = 0; i < json.length; i++) {
              var opId = json[i].operationID;
              if (!isProgressShown(opId)) continue;
              var progress = json[i].progress;
              var runtime = json[i].runtime;
              var message = json[i].message;
              var report = json[i].report;
              var error = json[i].error;
              var running = json[i].running;
              if (!running && progress === 0) continue;
              var bar = container.find("#" + opId);
              if (bar.length === 0) {
                container.append(
                  "<div id='" + opId + "' class='long-progress'>" +
                    "<div class='long-progress-state'></div>" +
                    "<div class='long-progress-bar'>" +
                    "<span class='long-progress-bar-percent'>0 %</span>" +
                  "</div>" +
                  "<span class='long-progress-bar-message'></span>" +
                  "<div class='long-progress-report'></div></div>");
                bar = container.find("#" + opId);
              }
              bar.find(".long-progress-state").attr('title', "click to cancel").click(function() {
                KNOWWE.core.plugin.progress.cancelLongOperation(sectionId, jq$(this).parent().attr('id'));
              });
              bar.removeClass("long-progress-error long-progress-success");

              var percent = Math.floor(progress * 100);
              bar.find(".long-progress-bar").progressbar({value: percent});
              bar.find(".long-progress-bar-percent").text(percent + " % after " + runtime);
              bar.find(".long-progress-bar-message").text(message);
              bar.find(".long-progress-report").html(report);

              if (!running) {
                var closeFunction = function(event) {
                  KNOWWE.core.plugin.progress.removeLongOperation(sectionId, jq$(event.target).parent().attr('id'));
                };
                bar.find(".long-progress-state").unbind("click").click(closeFunction);
                if (!bar.find(".long-progress-close").exists()) {
                  bar.find(".long-progress-bar-message").after("<a class='long-progress-close'>[Hide]</a>");
                  bar.find(".long-progress-close").click(closeFunction);
                }
                if (error) {
                  bar.addClass("long-progress-error");
                  bar.find(".long-progress-report").html(error);
                  bar.find(".long-progress-state").attr("title", "aborted, click to hide");
                  KNOWWE.helper.observer.notify('longOperationAborted', {
                    sectionId: sectionId,
                    opId: opId
                  });
                } else {
                  bar.addClass("long-progress-success");
                  bar.find(".long-progress-state").attr("title", "succeeded, click to hide");
                  KNOWWE.helper.observer.notify('longOperationSuccessful', {
                    sectionId: sectionId,
                    opId: opId
                  });
                }
              }
              KNOWWE.tooltips.enrich(bar);
              refresh |= running;
            }
            KNOWWE.helper.observer.notify("contentChange");
            if (refresh) {
              window.setTimeout(function() {
                KNOWWE.core.plugin.progress.updateProgressBar(sectionId);
              }, 100);
            }
          },
          onError: handleErrResponse
        }
      }

      new _KA(options).send();
    }

  }
}();

KNOWWE.helper.observer.subscribe("afterRerender", KNOWWE.core.plugin.progress.updateProgressBars);

jq$(KNOWWE.core.plugin.progress.updateProgressBars);
