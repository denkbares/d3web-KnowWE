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

KNOWWE.plugin.jspwikiConnector = {};

KNOWWE.plugin.jspwikiConnector.setReadOnlyCheckbox = function(checkbox) {
  var boxActivated = jq$(checkbox).prop("checked");

  KNOWWE.plugin.jspwikiConnector.setReadOnly(!boxActivated);
};

KNOWWE.plugin.jspwikiConnector.setReadOnly = function(active) {

  var readonly = !active;
  var params = {
    action: "ReadOnlyAction",
    readonly: readonly
  };

  var options = {
    url: KNOWWE.core.util.getURL(params),
    response: {
      action: "none",
      fn: function() {
        _EC.executeIfPrivileged(KNOWWE.plugin.jspwikiConnector.enableEditButtons, KNOWWE.plugin.jspwikiConnector.disableEditButtons);
        if (readonly) {
          jq$(".readOnlyMessage").css("display", "block");
        } else {
          jq$(".readOnlyMessage").css("display", "none");
        }
        location.reload();
      },
      onError: _EC.onErrorBehavior
    }
  };
  new _KA(options).send();
};

KNOWWE.plugin.jspwikiConnector.initCompileWarning = function() {

  const timeDisplay = document.getElementById("compile-warning");
  if (!timeDisplay) return;

  jq$.ajax("action/AwaitRecompilationAction", {cache: false}).done(function() {
    window.location.reload();
  });

  const timeValueSpan = document.getElementById('time-value');
  const dateStartedMsAgo = parseInt(timeValueSpan.getAttribute('data-started-ms-ago'), 10);
  const updateInterval = 1000; // Update alle 1 Sekunde

  function formatTime(milliseconds) {
    const totalSeconds = Math.round(milliseconds / 1000);

    const days = Math.floor(totalSeconds / 86400);
    const hours = Math.floor((totalSeconds % 86400) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const remainingSeconds = totalSeconds % 60;

    if (days > 0) {
      return `${days} days ${hours}:${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')} hours`;
    } else if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')} hours`;
    } else if (minutes > 0) {
      return `${minutes}:${remainingSeconds.toString().padStart(2, '0')} min`;
    } else {
      return `${totalSeconds}s`;
    }
  }

  function updateDisplay() {
    const currentTime = performance.now();
    const elapsedTime = currentTime + dateStartedMsAgo;
    timeValueSpan.textContent = formatTime(elapsedTime);
  }

  // Initiales Update und Setzen des Intervals
  updateDisplay();
  setInterval(updateDisplay, updateInterval);
};

KNOWWE.plugin.jspwikiConnector.enableEditButtons = function() {
  jq$("#actionsTop").find(".edit").parent().show();
  jq$("#edit").show();
  _IE.enableDefaultEditTool();
  if (typeof _EM !== 'undefined') _EM.changeActionMenu();
};

KNOWWE.plugin.jspwikiConnector.disableEditButtons = function() {
  jq$("#actionsTop").find(".edit").parent().hide();
  jq$("#edit").hide();
  _IE.disableDefaultEditTool();
};

function getSectionId(filterTool) {
  return jq$(filterTool).parents(".page").find(".type_RecentChanges").find("table").attr("section-id");

}

KNOWWE.plugin.jspwikiConnector.setPageFilter = function(filterCheckBox, filterType) {
  let table = jq$(filterCheckBox).parents(".page").find(".type_RecentChanges").find(".knowwe-paginationWrapper");
  KNOWWE.helper.setToLocalSectionStorage(table.attr("id"), filterType, filterCheckBox.checked);
  table.rerender({reason: "pagination"});
};

jq$(function() {
  if (KNOWWE.helper.loadCheck(["Wiki.jsp"])) {
    _EC.executeIfPrivileged(KNOWWE.plugin.jspwikiConnector.enableEditButtons, KNOWWE.plugin.jspwikiConnector.disableEditButtons);
  }
  KNOWWE.plugin.jspwikiConnector.initCompileWarning();
});


