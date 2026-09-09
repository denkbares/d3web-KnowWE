/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
  let KNOWWE = {};
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

  function onErrorBehavior() {
    if (this.status == null) return;
    switch (this.status) {
      case 0:
        // server not running, do nothing.
        KNOWWE.notification.error(
          null,
          "Server appears to be offline.",
          "offline"
        );
        break;
      case 409:
        KNOWWE.notification.error(
          null,
          "There already is a build running for this dashboard. Please abort the running build before starting a new one.",
          status
        );
        break;
      case 404:
        KNOWWE.notification.error(
          null,
          "This page no longer exists. Please reload.",
          status
        );
        break;
      default:
        // alert("Error " + this.status + ". Please reload the page.");
        break;
    }
  }

  // consecutive connection failures of the progress stream after which the page stops reconnecting
  const MAX_STREAM_FAILURES = 5;

  // never use Array.from here, MooTools on the wiki pages replaces it with a version that wraps a Set into [set]
  // the single progress stream of this page: its EventSource and the dashboard names it follows
  let pageStream = null;
  // last reported build state per dashboard name and callbacks to run once a dashboard's build finished
  const lastStates = {};
  const finishCallbacks = {};

  function setText(id, text) {
    const element = document.getElementById(id);
    if (element) element.textContent = text;
  }

  function setHtml(id, html) {
    const element = document.getElementById(id);
    if (element) element.innerHTML = html;
  }

  function progressInfo(dashboardName) {
    return jq$("[name=\"" + dashboardName + "\"]").find(".ci-progress-info");
  }

  // state bubbles of a dashboard within headers or daemons, matching the container elements themselves as well
  function stateBubbles(dashboardName, container) {
    return jq$(container).find(".ci-state").addBack(".ci-state")
      .filter("[dashboardName=\"" + dashboardName + "\"]");
  }

  function isName(value) {
    return typeof value === "string" && value.length > 0;
  }

  function pageDashboardNames() {
    const names = new Set();
    jq$(".ci-header, .ci-daemon").find(".ci-state").addBack(".ci-state").each(function() {
      const name = jq$(this).attr("dashboardName");
      if (isName(name)) names.add(name);
    });
    return names;
  }

  function closeStream() {
    if (!pageStream) return;
    pageStream.source.close();
    pageStream = null;
  }

  /*
   * Opens the progress stream for the given dashboard names, replacing a stream that is already open. Events are
   * dispatched per dashboard, the stream is closed once every followed dashboard reported a finished build.
   */
  function openStream(names) {
    closeStream();
    const url = KNOWWE.core.util.getURL({
      action: "CIGetProgressAction",
      names: [...names]
    });
    const source = new EventSource(url);
    pageStream = { source: source, names: new Set(names) };
    let failures = 0;

    source.addEventListener("progress", function(event) {
      failures = 0;
      handleProgress(JSON.parse(event.data));
    });

    // the server announces that no followed dashboard has a build any more, closing prevents a reconnect
    source.addEventListener("end", function() {
      if (pageStream && pageStream.source === source) closeStream();
    });

    source.onerror = function() {
      // the browser retries on its own while connecting, give up after repeated failures without any event
      failures++;
      if (source.readyState !== EventSource.CLOSED && failures < MAX_STREAM_FAILURES) return;
      source.close();
      if (pageStream && pageStream.source === source) pageStream = null;
      names.forEach(name => {
        if (lastStates[name] && lastStates[name] !== "FINISHED") {
          setText(name + "_progress-text", " Connection to the server lost. Please reload manually.");
        }
      });
    };
  }

  function handleProgress(status) {
    const name = status.dashboard;
    const previous = lastStates[name];
    lastStates[name] = status.state;
    const showsRunning = stateBubbles(name, ".ci-header, .ci-daemon").filter("[running='true']").length > 0;

    if (status.state !== "FINISHED") {
      setText(name + "_progress-value", status.progress + "%");
      setHtml(name + "_progress-text", status.message);
      setText(name + "_progress-duration", status.elapsedDuration);
      progressInfo(name).show();
      if (!showsRunning) {
        // a build started while the page is open, switch header and bubble to their running appearance
        if (stateBubbles(name, ".ci-header").length > 0) _CI.refreshBuildStatus(name);
        if (stateBubbles(name, ".ci-daemon").length > 0) _CI.refreshCIDaemonBubble(name);
      }
      return;
    }

    const callbacks = finishCallbacks[name] || [];
    delete finishCallbacks[name];
    callbacks.forEach(callback => callback(name));

    // a delivered bubble is always the current state, apply it even if nothing was shown as running
    const bubbleDelivered = !!status.bubbleHtml;
    if (bubbleDelivered) {
      stateBubbles(name, ".ci-header, .ci-daemon").replaceWith(status.bubbleHtml);
    }

    const wasActive = previous !== undefined && previous !== "FINISHED";
    if (!wasActive && !showsRunning) return;

    progressInfo(name).fadeOut(500);
    const modifiedWarning = document.getElementById("modified-warning_" + name);
    if (modifiedWarning) {
      modifiedWarning.parentElement.remove();
    }
    if (stateBubbles(name, ".ci-header").length > 0) {
      _CI.refreshBuildDetails(name);
      _CI.refreshBuildList(name);
      _CI.refreshBuildStatus(name);
    }
    // older servers deliver no bubble, then the daemon bubble is fetched as before
    if (!bubbleDelivered && stateBubbles(name, ".ci-daemon").length > 0) {
      _CI.refreshCIDaemonBubble(name);
    }
  }

  return {

    expandAllMessages: function(button) {
      let expandButtons = jq$(button).parents(".ci-column-middle")
        .find(".expandCIMessage:visible")
        .filter((i, el) => jq$(el).find(".knowwe-error, .knowwe-warning").exists());
      if (expandButtons.length === 0) {
        expandButtons = jq$(button).parents(".ci-column-middle")
          .find(".expandCIMessage:visible");
      }
      expandButtons.each((i, el) => KNOWWE.plugin.ci4ke.expandMessage(el));
    },

    collapseAllMessages: function(button) {
      jq$(button).parents(".ci-column-middle")
        .find(".collapseCIMessage:visible")
        .each((i, el) => KNOWWE.plugin.ci4ke.collapseMessage(el));
    },

    expandMessage: function(button) {
      const $expandButton = jq$(button);
      const $collapseButton = $expandButton
        .parent()
        .children(".collapseCIMessage");
      const $message = $expandButton.parent().children(".ci-message");
      $message.show("fast", function() {
        $expandButton.hide();
        $collapseButton.show();
      });
    },

    collapseMessage: function(button) {
      const $collapseButton = jq$(button);
      const $expandButton = $collapseButton
        .parent()
        .children(".expandCIMessage");
      const $message = $collapseButton.parent().children(".ci-message");
      $message.hide("fast", function() {
        $collapseButton.hide();
        $expandButton.show();
      });
    },

    refreshBuildDetails: function(dashboardName, buildNr) {
      const params = {
        action: "CIAction",
        task: "refreshBuildDetails",
        name: dashboardName
      };

      if (buildNr != null) {
        params["nr"] = buildNr;
      }

      const options = {
        url: KNOWWE.core.util.getURL(params),
        loader: true,
        response: {
          ids: [dashboardName + "-build-details-wrapper"],
          action: "insert",
          fn: function() {
            // (re-)activate incoming script tags for collapsing
            let result = null;
            const rePattern = /<script>(.*)<\/script>/gi;
            while ((result = rePattern.exec(this.responseText))) {
              const script = result[1];
              eval(script);
            }
          }
        }
      };

      new _KA(options).send();
    },

    /*
     * Cancels a running build, followed by a page reload.
     */
    stopRunningBuild: function(dashboardName, title, location) {
      const params = {
        action: "CIStopBuildAction",
        name: dashboardName,
        topic: title
      };

      const options = {
        url: KNOWWE.core.util.getURL(params),
        loader: true,
        response: {
          fn: function() {
            window.location = location;
          }
        }
      };

      new _KA(options).send();
    },

    /*
     * Triggers the start of a new build and follows its progress afterward.
     */
    executeNewBuild: function(dashboardName, title) {
      const params = {
        action: "CIAction",
        task: "executeNewBuild",
        name: dashboardName
      };

      const options = {
        url: KNOWWE.core.util.getURL(params),
        loader: true,
        response: {
          fn: function() {
            _CI.refreshCIDaemonBubble(dashboardName);
            _CI.watchBuild(dashboardName);

            // make not-up-to-date warning disappear
            jq$(".ci-title").each(function() {
              if (jq$(this).attr("name") === dashboardName) {
                jq$(this).find(".warning").hide();
              }
            });
          },
          onError: onErrorBehavior
        }
      };

      new _KA(options).send();
    },

    /**
     * Follows the current build of a dashboard through the progress stream of this page. Progress bar, message and
     * elapsed time in the dashboard header are updated as events arrive. Once the build is finished, the dashboard
     * header and daemon bubble present on the page are refreshed. The stream also covers every other dashboard shown
     * on the page, so builds starting for them in the meantime are picked up as well.
     */
    watchBuild: function(dashboardName, onFinish = undefined) {
      if (!isName(dashboardName)) return;
      if (onFinish) {
        (finishCallbacks[dashboardName] = finishCallbacks[dashboardName] || []).push(onFinish);
      }
      const names = pageDashboardNames();
      names.add(dashboardName);
      if (pageStream && [...names].every(name => pageStream.names.has(name))) return;
      openStream(names);
    },

    /**
     * Brings every dashboard header and daemon bubble on the page up to date through the progress stream. The
     * server reports each dashboard once, delivering the current bubble for finished builds, and keeps following
     * builds that are running. Nothing happens while a stream is already open, it already delivers all changes.
     */
    syncBuilds: function() {
      if (pageStream) return;
      const names = pageDashboardNames();
      if (names.size === 0) return;
      openStream(names);
    },

    /**
     * Opens the progress stream of this page if any dashboard header or daemon bubble shows a running build.
     */
    watchRunningBuilds: function() {
      const running = jq$(".ci-header, .ci-daemon").find(".ci-state").addBack(".ci-state").filter("[running='true']");
      if (running.length === 0) return;
      const names = pageDashboardNames();
      if (pageStream && [...names].every(name => pageStream.names.has(name))) return;
      openStream(names);
    },

    /**
     * @deprecated: use watchBuild instead
     */
    refreshBuildProgress: function(dashboardName) {
      this.watchBuild(dashboardName);
    },

    /**
     * @deprecated: use watchBuild instead
     */
    refreshBuildProgressDeamon: function(dashboardName, onFinish = undefined) {
      this.watchBuild(dashboardName, onFinish);
    },

    /**
     * @deprecated: use watchBuild instead
     */
    refreshBuildProgressDaemon: function(dashboardName, onFinish = undefined) {
      this.watchBuild(dashboardName, onFinish);
    },

    /**
     * @deprecated: use refreshCIDaemonBubble instead
     */
    refreshCIDeamonBubble: function(dashboardName, onFinish = undefined) {
      this.refreshCIDaemonBubble(dashboardName, onFinish);
    },

    /**
     * Fetches the ci state bubble html code for a daemon/dashboard and inserts it. This is called after some build
     * process has been finished on the server to update the view correspondingly.
     *
     * @param dashboardName the name of the dashboard to display the daemon for
     * @param {undefined | (({dashboardName: string}) => void)} onFinish an optional callback to be called after
     * successfully refreshing
     */
    refreshCIDaemonBubble: function(dashboardName, onFinish = undefined) {
      const params = {
        action: "CIAction",
        task: "refreshBubble",
        name: dashboardName
      };

      const options = {
        url: KNOWWE.core.util.getURL(params),
        loader: true,
        response: {
          fn: function() {
            if (this.status === 200) {
              jq$(".ci-header,.ci-daemon")
                .find(".ci-state")
                .filter("[dashboardName=\"" + dashboardName + "\"]")
                .replaceWith(this.response);
              if (onFinish) {
                onFinish(dashboardName);
              }
            }
          },
          onError: onErrorBehavior
        }
      };

      new _KA(options).send();
    },

    /*
     * Fetches the list of build numbers/states when the left/right navigation
     * buttons for showing earlier/later builds are pressed. It also updates the
     * build details panel on the right correspondingly.
     *
     */
    refreshBuildList: function(
      dashboardName,
      clickedIndex,
      indexFromBack,
      numberOfBuilds
    ) {
      const params = {
        action: "CIAction",
        task: "refreshBuildList",
        name: dashboardName
      };

      if (clickedIndex != null) {
        params["nr"] = clickedIndex;
      }

      if (indexFromBack != null) {
        params["indexFromBack"] = indexFromBack;
      }

      if (numberOfBuilds != null) {
        params["numberOfBuilds"] = numberOfBuilds;
      }

      const options = {
        url: KNOWWE.core.util.getURL(params),
        response: {
          ids: [dashboardName + "-build-table"],
          action: "insert"
        }
      };

      new _KA(options).send();
    },

    refreshBuildStatus: function(dashboardName) {
      const params = {
        action: "CIAction",
        task: "refreshBuildStatus",
        name: dashboardName
      };

      const options = {
        url: KNOWWE.core.util.getURL(params),
        response: {
          ids: ["ci-header_" + dashboardName],
          action: "replace"
        }
      };

      new _KA(options).send();
    }
  };
}();

/**
 * Alias for some to reduce typing.
 */
const _CI = KNOWWE.plugin.ci4ke;

/*
 * Follows running builds once the page is loaded (happens if someone opens a page with a dashboard or daemon
 * where currently a build is running). On focus all dashboards of the page are synchronized through one stream,
 * which also picks up builds started while the tab was in the background.
 */
jq$(function() {
  _CI.watchRunningBuilds();
}).on("focus", () => {
  _CI.syncBuilds();
});

KNOWWE.plugin.ci4ke.FreezeFailedTests = function() {

  async function run(sectionId, dashboardName, title) {

    var url = KNOWWE.core.util.getURL({
      action: "CIFreezeFailedTestsAction",
      SectionID: sectionId,
      dashboardName: dashboardName
    });

    await fetch(url, { credentials: "same-origin" });

    KNOWWE.plugin.ci4ke.executeNewBuild(dashboardName, title);

    window.location.reload();
  }

  return {
    run: run
  };

}();
