KNOWWE = typeof KNOWWE === "undefined" ? {} : KNOWWE;
KNOWWE.core = KNOWWE.core || {};
KNOWWE.core.plugin = KNOWWE.core.plugin || {};

/**
 * The KNOWWE.plugin.ontology global namespace object. If KNOWWE.plugin.ontology
 * is already defined, the existing KNOWWE.plugin.ontology object will not be
 * overwritten so that defined namespaces are preserved.
 */
KNOWWE.plugin.ontology = function() {
  // noinspection JSUnusedGlobalSymbols
  return {
    commitOntology: function(sectionID) {
      new _KA({
        url: KNOWWE.core.util.getURL({
          action: "CommitOntologyAction",
          SectionID: sectionID
        }),
        fn: function() {
          KNOWWE.core.util.hideProcessingIndicator();
          location.reload();
        }
      }).send();
      KNOWWE.core.util.showProcessingIndicator();
    },
    expandLazyReference: function(sectionID, newReferenceText) {
      var params = {
        action: "ReplaceKDOMNodeAction",
        TargetNamespace: sectionID,
        KWikitext: newReferenceText
      };
      var options = {
        url: KNOWWE.core.util.getURL(params),
        response: {
          fn: function() {
            // todo: rerender markup block
            location.reload();
          }
        }
      };
      new _KA(options).send();
    }
  };
}();

/**
 * Instant edit tool of SPARQL markups, the CodeMirror edit tool with a button to indent the query, see
 * sparql-language.js. The base tool is looked up on each call, so the load order of the scripts does not matter.
 */
KNOWWE.plugin.sparql.codeMirrorEditTool = function() {
  const base = () => KNOWWE.plugin.codeMirrorEditTool;
  return {
    generateHTML: id => base().generateHTML(id),
    generateButtons: function(id) {
      const indent = "<a class='action indent' title='Indent the SPARQL query by its brackets (Shift-Alt-I)'"
        + " onclick=\"KNOWWE.plugin.sparql.codeMirrorEditTool.indent('" + id + "')\">Indent</a>";
      return base().generateButtons(id, window.SparqlLanguage ? [indent] : []);
    },
    postProcessHTML: id => base().postProcessHTML(id),
    unloadCondition: id => base().unloadCondition(id),
    generateWikiText: id => base().generateWikiText(id),
    indent: function(id) {
      const view = base().getView(id);
      if (!view || !window.SparqlLanguage) return;
      // the caret may still be on the declaration line, before the body of the markup
      if (!SparqlLanguage.indentBlock(view)) SparqlLanguage.indentBlock(view, view.state.doc.line(1).to);
      view.focus();
    }
  };
}();

KNOWWE.plugin.turtle = {};
KNOWWE.plugin.turtle.editTool = {};
jq$.extend(KNOWWE.plugin.turtle.editTool, KNOWWE.plugin.defaultEditTool);

KNOWWE.plugin.sparql.downloadExcel = function(id, paramFilename, actionClass, filtered) {
  let sectionMarker = jq$("#" + id).find(".knowwe-paginationWrapper");
  let localSectionStorage = KNOWWE.helper.getLocalSectionStorage(sectionMarker.attr("id"), true);
  jq$.ajax("action/" + actionClass, {
    data: {
      SectionID: id,
      filename: paramFilename,
      localSectionStorage: localSectionStorage,
      download: false,
      filtered: filtered
    },
    type: "post",
    cache: false,
    success: function(json) {
      window.location = "action/SparqlDownloadAction?SectionID=" + id +
        "&filename=" + paramFilename +
        "&download=true" +
        "&downloadFile=" + json["downloadFile"] +
        "&filtered=" + filtered;
    }
  });
};

KNOWWE.plugin.sparql.initDownloadExcelTools = function() {


  KNOWWE.helper.observer.subscribe("filterChanged", function() {
    let $elements = jq$(`.ReRenderSectionMarker[sectionid="${(this.sectionId)}"]`).closest(".defaultMarkupFrame").find(".markupMenu .markupMenuItem, button")
      .filter((_, elem) => jq$(elem).find(".fa-arrow-down-to-bracket").exists());
    $elements.css("display", this.filteringActive ? "inline-block" : "none");
  });

  KNOWWE.helper.observer.notify("registeredFilterChangedListener");
};

KNOWWE.plugin.turtle.editTool.generateButtons = function(id) {
  KNOWWE.plugin.turtle.editTool.format = function() {
    KNOWWE.core.plugin.formatterAjax(id, "TurtleFormatAction");
  };
  return _EC.elements.getSaveCancelDeleteButtons(id,
    ["<a class='action format' onclick='KNOWWE.plugin.turtle.editTool.format(\"" + id + "\")'>" +
    "Format</a>"]);
};

(function init() {

  window.addEvent("domready", _KL.setup);
  if (KNOWWE.helper.loadCheck(["Wiki.jsp"])) {
    window.addEvent("domready", function() {
      jq$(document).find("type_HierarchyTable .wikitable.knowwetable").each(function() {
        KNOWWE.plugin.hierarchy.declare(this);
        KNOWWE.plugin.hierarchy.eval(this);
      });
      KNOWWE.plugin.hierarchy.build();
      KNOWWE.plugin.hierarchy.init();
    });
  }
}());


KNOWWE.plugin.hierarchy = {};
KNOWWE.plugin.hierarchy.declare = function(table) {
  jq$(table.rows).filter(function() {
    return table.rows[0] !== this;
  }).each(function() {
    this.setAttribute("data-tt-id", "hierarchy-row-" + this.id);
  });
};

KNOWWE.plugin.hierarchy.list = [];

KNOWWE.plugin.hierarchy.eval = function(table) {
  var highest = -1, lowest = -1;
  jq$(table.rows).filter(function() {
    return this.hasAttribute("data-tt-id");
  }).each(function() {
    var hierarchy = this.cells[0].innerText.trim();
    if (highest === -1 || parseInt(hierarchy) < highest) {
      highest = parseInt(hierarchy);
    }
    if (lowest === -1 || parseInt(hierarchy) > lowest) {
      lowest = parseInt(hierarchy);
    }
    this.className = "hierarchy" + hierarchy;
    KNOWWE.plugin.hierarchy.list.push(
      KNOWWE.plugin.hierarchy.object(
        this.id,
        hierarchy
      )
    );
  });
};

KNOWWE.plugin.hierarchy.build = function() {
  KNOWWE.plugin.hierarchy.list.forEach(function(obj) {
    obj.setParent(KNOWWE.plugin.hierarchy.getParent(obj));
  });
  KNOWWE.plugin.hierarchy.list.forEach(function(obj) {
    if (obj.children.length) {
      var dom = document.getElementById(obj.id);
      dom.classList.add("collapsed");
      dom.setAttribute("data-tt-branch", "true");
    }
  });
};

KNOWWE.plugin.hierarchy.getParent = function(hierarchyObject) {
  var $child = jq$(document.getElementById(hierarchyObject.id));
  while ($child.prev().length) {
    if ($child.prev().hasClass("hierarchy" + (parseInt(hierarchyObject.hierarchy) - 1))) {
      return jq$(KNOWWE.plugin.hierarchy.list).filter(function() {
        return this.id === $child.prev()[0].id;
      })[0];
    }
    $child = jq$($child.prev()[0]);
  }
  return null;
};

KNOWWE.plugin.hierarchy.object = function(id, hierarchy) {
  return {
    id: id,
    hierarchy: hierarchy,
    data_tt_id: "hierarchy-row-" + id,
    parent: null,
    children: [],
    setParent: function(parent) {
      if (parent == null) return;
      this.parent = parent;
      document.getElementById(this.id).setAttribute("data-tt-parent-id", parent.data_tt_id);
      parent.addChild(this);
    },
    addChild: function(child) {
      if (child == null) return;
      this.children.push(child);
    }
  };
};

KNOWWE.plugin.hierarchy.init = function() {
  jq$(".type_ClassHierarchy .wikitable.knowwetable").each(function() {
    jq$(this).agikiTreeTable({
      expandable: true,
      clickableNodeNames: true,
      persist: true,
      article: jq$(this.id).closest(".defaultMarkupFrame").attr("id")
    });
  });

  jq$(".type_HierarchyTable .wikitable.knowwetable").each(function() {
    jq$(this).agikiTreeTable({
      expandable: true,
      clickableNodeNames: true,
      persist: true,
      article: jq$(this.id).closest(".defaultMarkupFrame").attr("id")
    });
  });
};

KNOWWE.plugin.sparqlConsole = function() {

  return {
    init: function() {
      jq$(".type_SparqlConsole textarea.sparqlEditor").each(function() {
        const editor = jq$(this);
        editor.autosize({append: ""});
        new TextArea(editor);
        editor.on("keydown", function(event) {
          if ((event.metaKey || event.altKey || event.ctrl) && event.key === "Enter") { // render sparql when cmd + enter is pressed
            KNOWWE.plugin.sparqlConsole.updateConsole(jq$(this).attr("sectionid"));
          }
        });
      });
    },

    updateConsole: function(sectionID) {
      var query = jq$("textarea.sparqlEditor[sectionid=\"" + sectionID + "\"]").val();
      jq$.cookie("sparqlConsole_" + sectionID, query);
      jq$("#" + sectionID).rerender();
    }
  };
}

(function init() {
  if (KNOWWE.helper.loadCheck(["Wiki.jsp"])) {
    window.addEvent("domready", function() {
      KNOWWE.plugin.sparqlConsole.init();
      KNOWWE.plugin.sparql.initDownloadExcelTools();
    });
  }
}());