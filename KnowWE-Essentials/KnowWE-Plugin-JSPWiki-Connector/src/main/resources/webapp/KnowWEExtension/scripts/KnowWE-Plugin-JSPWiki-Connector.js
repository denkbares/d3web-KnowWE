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

/**
 * Asks for confirmation in a dialog of the page and calls onConfirm if confirmed. Used instead of confirm(), which
 * embedded browsers may suppress: they answer "no" without showing anything. The severity "warning" (yellow) or
 * "danger" (red) colors the confirming button.
 */
KNOWWE.plugin.jspwikiConnector.confirmAction = function(title, question, confirmText, severity, onConfirm) {
  // the dialog is attached to the document of its content, so create it in this one
  const box = jq$("<div class='admin-tool-question'></div>", document).text(question);
  box.dialog({
    title: title,
    modal: true,
    resizable: false,
    width: Math.min(480, jq$(window).width() * 0.9),
    classes: { "ui-dialog": "admin-tool-dialog" },
    close: () => box.dialog("destroy").remove(),
    buttons: [
      {
        text: confirmText, "class": "admin-tool-" + severity, click: () => {
          box.dialog("close");
          onConfirm();
        }
      },
      { text: "Cancel", click: () => box.dialog("close") }
    ]
  });
};

KNOWWE.plugin.jspwikiConnector.restartWebApp = function() {
  KNOWWE.plugin.jspwikiConnector.confirmAction("Restart web app",
    "Are you sure you want to restart the web app? This may take a while...", "Restart web app", "warning", () => {
      KNOWWE.editCommons.showAjaxLoader();
      setTimeout(() => jq$.ajax({url: "action/AdministrationToolProvider?type=restart-webapp"}).done(() => window.location.reload()), 500);
    });
};

KNOWWE.plugin.jspwikiConnector.restartTomcat = function() {
  KNOWWE.plugin.jspwikiConnector.confirmAction("Restart Tomcat",
    "Are you sure you want to restart Tomcat? This may take a while...", "Restart Tomcat", "warning",
    KNOWWE.plugin.jspwikiConnector.performTomcatRestart);
};

KNOWWE.plugin.jspwikiConnector.performTomcatRestart = function() {

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
    setTimeout(() => getStatus("restart-tomcat-status").done(handleStatus).fail(jqXHR => {
      if (jqXHR.status === 401 || jqXHR.status === 403) {
        // a server answers, but does not know our session any more (it was lost by the restart), so reload to log in again
        window.location.reload();
        return;
      }
      // tomcat is shutting down or starting, wait for it to be available again
      KNOWWE.notification.warn("Restarting Tomcat", "The server is currently not available, waiting for it to come back...", messageId);
      poll();
    }), pollInterval);
  };

  KNOWWE.editCommons.showAjaxLoader();
  getStatus("restart-tomcat").done(handleStatus).fail(jqXHR => stop(() => KNOWWE.notification.error_jqXHR(jqXHR)));
};

KNOWWE.plugin.jspwikiConnector.deletePages = function() {

  const statusLabels = {
    NOT_FOUND: "not found",
    NO_PERMISSION: "no permission",
    DUPLICATE: "listed more than once",
    ALREADY_CLEARED: "already cleared",
    FAILED: "failed"
  };
  const okLabel = function(result) {
    const verb = result.mode === "CLEAR" ? "cleared" : "deleted";
    return result.preview ? "will be " + verb : verb;
  };

  const dialog = jq$(
    "<div class='delete-pages-dialog'>" +
    "<p>One page name per line, the names have to match exactly (upper and lower case do not matter).</p>" +
    "<textarea class='delete-pages-names' spellcheck='false'></textarea>" +
    "<div class='delete-pages-mode'>" +
    "<label><input type='radio' name='delete-pages-mode' value='CLEAR' checked> " +
    "Clear pages (the content can be restored from the page history)</label>" +
    "<label><input type='radio' name='delete-pages-mode' value='DELETE'> " +
    "Delete pages including their attachments and revision history (cannot be undone)</label>" +
    "</div>" +
    "<div class='delete-pages-report'></div>" +
    "</div>", document); // the dialog is attached to the document of its content, so create it in this one
  const namesArea = dialog.find(".delete-pages-names");
  const report = dialog.find(".delete-pages-report");
  const getMode = () => dialog.find("input[name=delete-pages-mode]:checked").val();


  // while a request runs (deleting a thousand pages takes a while), the input is locked and the action button is
  // disabled and tells what happens, so it cannot be clicked twice
  const request = function(preview, busyLabel) {
    const button = actionButton();
    const label = button.button("option", "label");
    const inputs = dialog.find("textarea, input");
    button.button("option", "label", busyLabel).button("disable");
    inputs.prop("disabled", true);
    KNOWWE.editCommons.showAjaxLoader();
    return jq$.ajax({
      url: "action/DeletePagesAction",
      method: "POST",
      dataType: "json",
      data: { pages: namesArea.val(), mode: getMode(), preview: preview }
    }).always(() => {
      KNOWWE.editCommons.hideAjaxLoader();
      inputs.prop("disabled", false);
      button.button("option", "label", label).button("enable");
    }).fail(jqXHR => KNOWWE.notification.error_jqXHR(jqXHR));
  };

  const countOk = result => result.results.filter(entry => entry.status === "OK").length;
  const pages = count => count + (count === 1 ? " page" : " pages");

  const renderReport = function(result) {
    report.empty();
    if (result.results.length === 0) {
      report.append(jq$("<p>").text("No page names entered."));
      return;
    }
    const groups = {};
    result.results.forEach(entry => (groups[entry.status] = groups[entry.status] || []).push(entry));
    report.append(jq$("<p class='delete-pages-report-title'></p>", document).text(result.preview ? "Preview:" : "Done:"));
    // the pages actually cleared resp. deleted get a check mark, the summary line as well as each page
    const success = () => result.preview ? [] : [" ", jq$("<i class='fa-regular fa-circle-check delete-pages-success'></i>", document)];
    ["OK", "FAILED", "NOT_FOUND", "NO_PERMISSION", "ALREADY_CLEARED", "DUPLICATE"].forEach(status => {
      const entries = groups[status];
      if (!entries) return;
      const label = status === "OK" ? okLabel(result) : statusLabels[status];
      const details = jq$("<details>").addClass("delete-pages-" + status.toLowerCase());
      // problems are shown expanded, the (possibly long) list of pages that are fine is collapsed
      if (status !== "OK") details.attr("open", "open");
      const done = status === "OK" ? success : () => [];
      details.append(jq$("<summary>").text(pages(entries.length) + " " + label).append(done()));
      const list = jq$("<ul>");
      entries.forEach(entry => list.append(jq$("<li>").text(entry.page + (entry.message ? ": " + entry.message : "")).append(done())));
      details.append(list);
      report.append(details);
    });
  };

  // The action button first shows the preview together with the question, and only a second click clears or deletes
  // the previewed pages. The question is asked in the dialog instead of with confirm(): embedded browsers may suppress
  // native dialogs and silently answer "no".
  // the number of previewed pages to confirm, 0 if there is no preview to confirm
  let confirming = 0;
  const actionButton = () => dialog.dialog("widget").find(".delete-pages-action");

  const reset = function() {
    confirming = 0;
    report.empty();
    const verb = getMode() === "CLEAR" ? "Clear" : "Delete";
    actionButton().button("option", "label", verb + " (Preview)")
      .removeClass("admin-tool-warning admin-tool-danger");
  };

  const askToConfirm = function(result) {
    const count = countOk(result);
    const clear = result.mode === "CLEAR";
    if (count === 0) {
      if (result.results.length > 0) report.append(jq$("<p></p>", document).text(clear ? "Nothing to clear." : "Nothing to delete."));
      return;
    }
    const their = count === 1 ? "its" : "their";
    const question = clear
      ? "Really clear " + pages(count) + "? The content can be restored from the page history."
      : "Really delete " + pages(count) + " including " + their + " attachments and revision history? This cannot be undone!";
    const box = jq$("<div class='delete-pages-confirm'></div>", document)
      .addClass(clear ? "delete-pages-confirm-warning" : "delete-pages-confirm-danger")
      .text(question);
    report.append(box);
    box[0].scrollIntoView({ block: "nearest" });
    actionButton().button("option", "label", (clear ? "Clear " : "Delete ") + pages(count))
      .addClass(clear ? "admin-tool-warning" : "admin-tool-danger")
      .trigger("focus");
    confirming = count;
  };

  const act = function() {
    if (confirming) {
      const busyLabel = (getMode() === "CLEAR" ? "Clearing " : "Deleting ") + pages(confirming) + " ...";
      request(false, busyLabel).done(result => {
        reset();
        renderReport(result);
        // the pages are done, the report tells what happened to them (setting the value fires no input event)
        namesArea.val("");
      });
    }
    else {
      request(true, "Checking pages ...").done(result => {
        renderReport(result);
        askToConfirm(result);
      });
    }
  };

  // what was previewed and confirmed must be what gets changed, so every change of the input starts over
  namesArea.on("input", reset);
  dialog.find("input[name=delete-pages-mode]").on("change", reset);

  dialog.dialog({
    title: "Delete multiple pages",
    modal: true,
    resizable: true,
    width: Math.min(800, jq$(window).width() * 0.9),
    maxHeight: jq$(window).height() * 0.9,
    classes: { "ui-dialog": "admin-tool-dialog" },
    close: () => dialog.dialog("destroy").remove(),
    buttons: [
      { text: "Clear (Preview)", "class": "delete-pages-action", click: act },
      { text: "Close", click: () => dialog.dialog("close") }
    ]
  });
  reset();
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