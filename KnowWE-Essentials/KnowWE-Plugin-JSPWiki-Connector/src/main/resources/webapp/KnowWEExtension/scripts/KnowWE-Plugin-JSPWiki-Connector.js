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
  var boxActivated = jq$(checkbox).prop('checked');

  KNOWWE.plugin.jspwikiConnector.setReadOnly(!boxActivated);
};

KNOWWE.plugin.jspwikiConnector.setReadOnly = function(active) {

  var readonly = !active;
  var params = {
    action: 'ReadOnlyAction',
    readonly: readonly
  };

  var options = {
    url: KNOWWE.core.util.getURL(params),
    response: {
      action: 'none',
      fn: function() {
        _EC.executeIfPrivileged(KNOWWE.plugin.jspwikiConnector.enableEditButtons, KNOWWE.plugin.jspwikiConnector.disableEditButtons);
        if (readonly) {
          jq$('.readOnlyMessage').css('display', 'block');
        } else {
          jq$('.readOnlyMessage').css('display', 'none');
        }
        location.reload();
      },
      onError: _EC.onErrorBehavior
    }
  };
  new _KA(options).send();
};

KNOWWE.plugin.jspwikiConnector.enableEditButtons = function() {
  jq$('#actionsTop').find('.edit').parent().show();
  jq$('#edit').show();
  _IE.enableDefaultEditTool();
  _EM.changeActionMenu();
};

KNOWWE.plugin.jspwikiConnector.disableEditButtons = function() {
  jq$('#actionsTop').find('.edit').parent().hide();
  jq$('#edit').hide();
  _IE.disableDefaultEditTool();
};

function getSectionId(filterTool) {
  return jq$(filterTool).parents('.page').find('.type_RecentChanges').find("table").attr("section-id");

}

KNOWWE.plugin.jspwikiConnector.setPageFilter = function(filterCheckBox, filterType){
  let table = jq$(filterCheckBox).parents('.page').find('.type_RecentChanges').find(".knowwe-paginationWrapper");
  KNOWWE.helper.setToLocalSectionStorage(table.attr("id"), filterType, filterCheckBox.checked);
  table.rerender({reason: "pagination"});
}

jq$(document).ready(function() {
  if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
    _EC.executeIfPrivileged(KNOWWE.plugin.jspwikiConnector.enableEditButtons, KNOWWE.plugin.jspwikiConnector.disableEditButtons);
  }
});


