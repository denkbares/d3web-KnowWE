/* global _EC, _EM, _IE, _KA, jq$ */
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
KNOWWE.SIDEBAR_STORAGE_KEY = "knowwe.sidebar.width";

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

KNOWWE.plugin.jspwikiConnector.restartWebApp = function() {
  if (confirm("Are you sure you want to restart the web app? This may take a while...")) {
    KNOWWE.editCommons.showAjaxLoader();
    setTimeout(() => jq$.ajax({url: "action/AdministrationToolProvider?type=restart-webapp"}).done(() => window.location.reload()), 500);
  }
};

KNOWWE.plugin.jspwikiConnector.restartTomcat = function() {
  if (!confirm("Are you sure you want to restart tomcat? This may take a while...")) return;

  const messageId = "tomcat-restart";
  const pollInterval = 3000;
  // give up waiting for tomcat to come back after this time
  const maxWaitMillis = 10 * 60 * 1000;
  const started = Date.now();
  let jvmStartTime = null;

  const getStatus = function(type) {
    return jq$.ajax({
      url: "action/AdministrationToolProvider?type=" + type,
      dataType: "json",
      cache: false,
      timeout: 10000
    });
  };

  const stop = function(showMessage) {
    KNOWWE.editCommons.hideAjaxLoader();
    KNOWWE.notification.removeNotification(messageId);
    showMessage();
  };

  const handleStatus = function(status) {
    if (jvmStartTime !== null && status.jvmStartTime !== jvmStartTime) {
      // a new JVM answers, so the restart was successful
      window.location.reload();
      return;
    }
    jvmStartTime = status.jvmStartTime;
    if (status.pending) {
      KNOWWE.notification.warn("Restarting Tomcat", status.message, messageId);
      poll();
    }
    else {
      stop(() => KNOWWE.notification.error("Tomcat restart failed", status.message
        + " Details are in the wiki log (Download recent logs).", messageId));
    }
  };

  const poll = function() {
    if (Date.now() - started > maxWaitMillis) {
      stop(() => KNOWWE.notification.error("Tomcat restart failed",
        "Tomcat did not come back within " + (maxWaitMillis / 60000) + " minutes. Please contact the server administrator.",
        messageId));
      return;
    }
    setTimeout(() => getStatus("restart-tomcat-status").done(handleStatus).fail(() => {
      // tomcat is shutting down or starting, wait for it to be available again
      KNOWWE.notification.warn("Restarting Tomcat", "The server is currently not available, waiting for it to come back...", messageId);
      poll();
    }), pollInterval);
  };

  KNOWWE.editCommons.showAjaxLoader();
  getStatus("restart-tomcat").done(handleStatus).fail(jqXHR => stop(() => KNOWWE.notification.error_jqXHR(jqXHR)));
};

KNOWWE.plugin.jspwikiConnector.initCompileWarning = function() {

  const timeDisplay = document.getElementById("compile-warning");
  if (!timeDisplay) return;

  jq$.ajax("action/AwaitRecompilationAction", { cache: false }).done(function() {
    window.location.reload();
  });

  const timeValueSpan = document.getElementById("time-value");
  const dateStartedMsAgo = parseInt(timeValueSpan.getAttribute("data-started-ms-ago"), 10);
  const updateInterval = 1000; // Update alle 1 Sekunde

  function formatTime(milliseconds) {
    const totalSeconds = Math.round(milliseconds / 1000);

    const days = Math.floor(totalSeconds / 86400);
    const hours = Math.floor((totalSeconds % 86400) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const remainingSeconds = totalSeconds % 60;

    if (days > 0) {
      return `${days} days ${hours}:${minutes.toString().padStart(2, "0")}:${remainingSeconds.toString().padStart(2, "0")} hours`;
    } else if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, "0")}:${remainingSeconds.toString().padStart(2, "0")} hours`;
    } else if (minutes > 0) {
      return `${minutes}:${remainingSeconds.toString().padStart(2, "0")} min`;
    } else {
      return `${totalSeconds}s`;
    }
  }

  function updateDisplay() {
    const currentTime = performance.now();
    const elapsedTime = currentTime + dateStartedMsAgo;
    timeValueSpan.textContent = formatTime(elapsedTime);
  }

  updateDisplay();
  setInterval(updateDisplay, updateInterval);
};

KNOWWE.plugin.jspwikiConnector.enableEditButtons = function() {
  jq$("#actionsTop").find(".edit").parent().show();
  jq$("#edit").show();
  _IE.enableDefaultEditTool();
  if (typeof _EM !== "undefined") _EM.changeActionMenu();
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
  table.rerender({ reason: "pagination" });
};

jq$(function() {
  if (KNOWWE.helper.loadCheck(["Wiki.jsp"])) {
    _EC.executeIfPrivileged(KNOWWE.plugin.jspwikiConnector.enableEditButtons, KNOWWE.plugin.jspwikiConnector.disableEditButtons);
  }
  KNOWWE.plugin.jspwikiConnector.initCompileWarning();
});

(function() {
  // read and load the stored sidebar width asap
  const value = localStorage.getItem(KNOWWE.SIDEBAR_STORAGE_KEY);
  if (value) {
    document.documentElement.style.setProperty("--sidebar-width", value + "px");
  }
})();

jq$(function SidebarResizer() {
  "use strict";

  const $sidebar = jq$("#sidebar, .sidebar").first();
  if (!$sidebar.length) return;

  // read default values from css
  const style = getComputedStyle($sidebar[0]);
  const minWidth = parseInt(style.minWidth, 10);
  const maxWidth = parseInt(style.maxWidth, 10);

  // append the resize handler to the sidebar
  const $handle = jq$("<div class=\"sidebar-handle\"></div>").appendTo($sidebar);

  let dragging = false;
  let startX = 0;
  let startWidth = 0;

  $handle.on("mousedown", function(e) {
    const sidebarRect = $sidebar[0].getBoundingClientRect();

    dragging = true;
    startX = sidebarRect.right ?? 0;
    startWidth = sidebarRect.width ?? 0;
    jq$("body").addClass("sidebar-resizing");
  });

  jq$(document).on("mousemove", function(e) {
    if (!dragging) return;

    const delta = e.clientX - startX;
    let newWidth = startWidth + delta;
    newWidth = Math.max(minWidth, Math.min(maxWidth, newWidth));
    $sidebar.css("width", newWidth + "px");
  });

  jq$(document).on("mouseup", function() {
    if (!dragging) return;

    dragging = false;
    jq$("body").removeClass("sidebar-resizing");
    localStorage.setItem(KNOWWE.SIDEBAR_STORAGE_KEY, $sidebar[0].getBoundingClientRect().width ?? "0");
  });
});

document.addEventListener("DOMContentLoaded", () => {

  document.querySelectorAll("pre").forEach(pre => {

    if (pre.dataset.copyButtonAdded) {
      return;
    }

    pre.dataset.copyButtonAdded = "true";

    const wrapper = document.createElement("div");
    wrapper.className = "codeblock-wrapper";

    const button = document.createElement("button");
    button.className = "copy-button";

    // Match the button background to the codeblock's actual background
    button.style.backgroundColor = window.getComputedStyle(pre).backgroundColor;

    button.innerHTML = '<i class="fa fa-copy"></i>';

    // Custom tooltip
    const tooltip = document.createElement("div");
    tooltip.className = "copy-tooltipster";
    tooltip.innerText = "Copy";

    button.appendChild(tooltip);

    // Hover handling
    button.addEventListener("mouseenter", () => {
      tooltip.classList.add("visible");
    });

    button.addEventListener("mouseleave", () => {
      tooltip.classList.remove("visible");
    });

    // Copy action
    button.addEventListener("click", async () => {

      try {

        await navigator.clipboard.writeText(pre.innerText);

        tooltip.innerText = "Copied!";

        button.classList.add("copied");

        setTimeout(() => {

          button.classList.remove("copied");

          tooltip.innerText = "Copy";

        }, 1500);

      } catch (err) {
        console.error("Copy failed:", err);
      }
    });

    pre.parentNode.insertBefore(wrapper, pre);

    wrapper.appendChild(pre);

    wrapper.appendChild(button);
  });

});