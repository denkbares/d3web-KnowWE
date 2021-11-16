/*
 * Copyright (C) 2014 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

KNOWWE = typeof KNOWWE === "undefined" ? {} : KNOWWE;
KNOWWE.core = KNOWWE.core || {};
KNOWWE.core.plugin = KNOWWE.core.plugin || {}

KNOWWE.core.plugin.objectinfo = function() {

  return {

    /**
     * shows a list of similar terms
     */
    lookUp: function(element) {
      if (!element) element = document;
      element = jq$(element);
      const terms = element.find('.objectinfo-terms');
      if (!terms.exists()) return;
      const response = terms.first().text();
      const termsJson = JSON.parse(response);
      const a = termsJson.allTerms;
      element.find('.objectinfo-search').autocomplete({
        source: a
      });
      element.find('.objectinfo-search').on(
        "autocompleteselect",
        function(event, ui) {
          KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(ui.item.value);
        });

      //Open "Show Info" on Enter key press only if term exists - otherwise do nothing
      element.find('.objectinfo-search').keyup(function(e) {
        if (e.keyCode === 13) {
          const val = jq$('.objectinfo-search').val();
          if (jq$.inArray(val, a) !== -1) {
            KNOWWE.plugin.compositeEditTool.openCompositeEditDialog(val);
          }

        }
      });
    }
  }
}();

/**
 * Handle anchors
 * @type {{init: KNOWWE.plugin.anchor.init}}
 */
KNOWWE.plugin.anchor = function() {


  function getFloatingTableHeaderHeight($element) {
    let $tableHeader = $element.parents("table.sticky-header").find("thead");
    if ($tableHeader.exists()) {
      return $tableHeader.height();
    } else {
      return 0;
    }
  }

  function scrollIntoView($element) {
    let headerHeight = jq$('.navigation').height();
    let elementOffset = $element.offset().top;
    let scrollTop = elementOffset
      // header will be fixed to the top, so we have to scroll a bit further
      - headerHeight
      // 10 additional margin
      - 10
      // if the hash anchor is inside a table, scroll additional 100px to also account for fixed table header
      - getFloatingTableHeaderHeight($element);
    jq$('html, body').animate({
      scrollTop: scrollTop
    }, 0);
  }


  return {

    init: function() {
      if (!window.location.hash) return;

      jq$('.anchor-highlight').removeClass("highlight").removeClass("anchor-highlight");
      const name = window.location.hash.substring(1);
      const sectionId = name.substr(name.lastIndexOf("-") + 1).toLowerCase();
      // first, try to find section directly
      let section = jq$('[sectionid="' + sectionId + '"]');
      if (section.exists()) {
        section.addClass('highlight');
        section.addClass('anchor-highlight');
        scrollIntoView(section);
        return;
      }

      // section not found, try highlighting from anchor to anchor-end
      const anchor = jq$('.anchor[name="' + name + '"]');
      if (anchor.exists()) {
        scrollIntoView(anchor.parent());
        const endSelector = '.anchor,.anchor-end[name="' + name + '"]';
        if (!anchor.nextAll(endSelector).exists()) return;
        let next = anchor.next();
        while (!next.is(endSelector)) {
          next.addClass('highlight');
          next.addClass('anchor-highlight');
          next = next.next();
        }
      }

      section = jq$('#' + sectionId);
      if (section.exists()) {
        scrollIntoView(section);
      }

      section = jq$('#' + name); // ordinary JSPWiki hash anchor links
      if (section.exists()) {
        setTimeout(function() { // deferred, so normal hash anchor scroll has already happened
          scrollIntoView(section);
        }, 0);
      }
    }
  }
}();

KNOWWE.plugin.renaming = function() {

  const sectionsCache = {};

  const otherOccurencesHashMap = {};

  let viewRoot = {};

  /**
   * Renames all occurrences of a specific term.
   */
  function renameTerms(options) {
    let forceRename = options.forceRename || false;
    let termName = options.termName;
    let termReplacement = options.termReplacement;
    let sectionId = options.sectionId;
    if (termName && (termReplacement || termReplacement === "")) {

      const changeNote = 'Renaming: "' + termName + '" -> "' + termReplacement + '"';
      const params = {
        action: options.action,
        termName: termName,
        termReplacement: termReplacement,
        sectionId: sectionId,
        changeNote: changeNote,
        force: forceRename
      };

      KNOWWE.core.util.updateProcessingState(1);

      jq$.ajax({
        type: "post", url: KNOWWE.core.util.getURL(params),

        success: function(jsonResponse, text, request) {
          if (jsonResponse.same) {
            alert('The term has not changed.');
          } else {
            if (jsonResponse.alreadyExists) {
              if (jsonResponse.noForce) {
                alert('A term with this name already exists!');
                KNOWWE.core.util.reloadPage(request);
              } else if (confirm('A term with this name already exists, are you sure you want to merge both terms?')) {
                options.forceRename = true;
                renameTerms(options);
              } else {
                KNOWWE.core.util.reloadPage(request);
              }
            } else {
              KNOWWE.helper.observer.notify("renamed");
              KNOWWE.core.util.reloadPage(request);
            }
          }
        },

        error: function(request, status, error) {
          KNOWWE.core.util.updateProcessingState(-1);
          console.log(status, error);
          KNOWWE.core.util.reloadPage();
        }
      });
      //KNOWWE.core.util.updateProcessingState(1);
    }

  }

  function getRenamingInfo(sectionId, actionName, callback) {
    const params = {
      action: actionName,
      SectionID: sectionId
    };
    const options = {
      url: KNOWWE.core.util.getURL(params),
      response: {
        action: 'none',
        fn: function() {
          const jsonResponse = JSON.parse(this.responseText);
          callback(jsonResponse);
        },
        onError: function() {
          KNOWWE.core.util.updateProcessingState(-1);
        }
      }
    };
    new _KA(options).send();
    KNOWWE.core.util.updateProcessingState(1);
  }

  function restoreOriginal(original) {
    jq$(original).removeClass("click");
    jq$(original).unbind();
    jq$(original).css("min-width", "");
    jq$(original).css("padding-right", "");
    jq$(original).css("width", "");
  }

  function cancelEdit(settings, original) {
    restoreOriginal(original);

    for (let occurence in otherOccurencesHashMap) {
      if (!otherOccurencesHashMap.hasOwnProperty(occurence)) continue;
      const section = sectionsCache[occurence];
      const parent = jq$(otherOccurencesHashMap[occurence]).parent();
      jq$(otherOccurencesHashMap[occurence]).parent().html(section);
      _TM.decorateToolMenus(parent);
    }

  }

  function afterCancelEdit(setting, original) {
    KNOWWE.tooltips.enrich(original);
    _TM.decorateToolMenus(jq$(original));
  }

  function showCurrentEditOnOtherOccurences(text) {
    for (let occurence in otherOccurencesHashMap) {
      if (!otherOccurencesHashMap.hasOwnProperty(occurence)) continue;
      jq$(otherOccurencesHashMap[occurence]).first().text(text);
    }
  }

  function saveOriginalsAndPrepareForEdit(lastPathElement) {
    for (let occurence in otherOccurencesHashMap) {
      if (!otherOccurencesHashMap.hasOwnProperty(occurence)) continue;
      sectionsCache[occurence] = jq$(otherOccurencesHashMap[occurence]).parent().html();
      jq$(otherOccurencesHashMap[occurence]).attr("sectionOccurenceId", occurence);
      jq$(otherOccurencesHashMap[occurence]).empty();
      jq$(otherOccurencesHashMap[occurence]).css("background-color", "yellow");
      jq$(otherOccurencesHashMap[occurence]).text(lastPathElement);
    }
    KNOWWE.core.util.updateProcessingState(-1);
  }

  function setViewRoot() {

    if (jq$("#compositeEdit").length > 0) {
      viewRoot = "#compositeEdit ";
    } else if (jq$("#pagecontent").length > 0) {
      viewRoot = "#pagecontent ";
    } else {
      viewRoot = ".page-content "; // haddock template
    }

  }

  function initializeOtherOccurencesHashMap(sectionIds) {
    for (let i = 0; i < sectionIds.length; i++) {
      const sectionId = sectionIds[i];
      const toolMenuIdentifier = jq$(viewRoot + ".defaultMarkupFrame span[toolmenuidentifier=" + sectionId + "]");
      if (toolMenuIdentifier.length > 0) {
        otherOccurencesHashMap[sectionId] = toolMenuIdentifier[0].parentNode;
      }
    }
  }

  // noinspection JSUnusedGlobalSymbols
  return {
    renameTerm: function(sectionId, opts) {
      setViewRoot();
      let options = opts || {};
      let infoActionName = options.infoActionName || "GetRenamingInfoAction";
      let renamingActionName = options.renamingActionName || "TermRenamingAction";

      getRenamingInfo(sectionId, infoActionName, function(jsonResponse) {

        const clickedTerm = jq$(viewRoot + "[toolmenuidentifier=" + sectionId + "]").first().parent();
        clickedTerm.addClass("tool-menu-click");

        jq$(".tool-menu-click").editable(function(value) {
          renameTerms({
            sectionId: sectionId,
            termName: jsonResponse.termIdentifier,
            termReplacement: value,
            action: renamingActionName,
            forceRename: false
          });
          return value;
        }, {
          style: "inherit",
          height: "20px",
          onreset: cancelEdit,
          afterreset: afterCancelEdit,
          select: true
        });

        jq$('.tool-menu-click').trigger("click");
        // replace edit field value with sectionText for encoding reasons
        clickedTerm.find("input").val(jsonResponse.lastPathElement).select().autoGrowRenameField(5);

        initializeOtherOccurencesHashMap(jsonResponse.sectionIds);
        saveOriginalsAndPrepareForEdit(jsonResponse.lastPathElement);
        jq$(".tool-menu-click input").keyup(function() {
          showCurrentEditOnOtherOccurences(jq$(this).val());
        });
      });

    }

  }

}();

KNOWWE.core.plugin.renderKDOM = function() {

  jq$('.table_text').hover(function() {
      const that = this;
      setTimeout(function() {
        jq$(that).css('height', that.scrollHeight);
      }, 0);
      // alert(this.scrollHeight);
    }, function() {
      jq$(this).css('height', '18px');
    }
  );
};

KNOWWE.core.plugin.setMarkupSectionActivationStatus = function(id, status) {
  const params = {
    action: 'SetMarkupActivationStatus',
    SectionID: id,
    status: status
  };
  const options = {
    url: KNOWWE.core.util.getURL(params),
    response: {
      action: 'none',
      fn: function() {
        window.location.reload();
      },
      onError: _EC.onErrorBehavior
    }
  };
  _KU.showProcessingIndicator();
  new _KA(options).send();
};

KNOWWE.tooltips = {};

KNOWWE.tooltips.enrich = function(element) {
  // first, we filter nested tooltiped objects
  // (e.g. a span with title contains another span with title)
  // this way, tooltipser behaves with nested tooltips the same way
  // normal tooltips behave: The most inner tooltips is shown exclusively
  if (element) {
    element = jq$(element);
  } else {
    element = jq$(document);
  }
  element.find('.tooltipster').each(function() {
    const anscestor = jq$(this).parents('.tooltipster');
    if (anscestor.exists()) {
      anscestor.removeAttr('title');
      anscestor.removeClass('tooltipster');
    }
  });
  element.find('.tooltipster').each(function() {
    let $this = jq$(this);
    let delay = $this.attr('delay') || 1300;
    let trigger = $this.attr('trigger') || "hover";
    $this.tooltipster({
      position: "top-left",
      interactive: true,
      multiple: true,
      delay: delay,
      contentAsHTML: true,
      updateAnimation: false,
      trigger: trigger,
      theme: ".tooltipster-knowwe",
      functionBefore: function(origin, continueTooltip) {
        // check if dom has changed since triggering... if source no longer exists, don't show tool tip
        if (!document.contains(origin[0])) {
          return;
        }
        // check if we have an ajax-tooltip
        // and only do once for each tooltip
        const src = origin.data('tooltip-src');
        if (!src) {
          continueTooltip();
          return;
        }
        origin.data('tooltip-src', null);
        // show ajax-spinner until request is arriving
        origin.tooltipster('update', '<span class="ajaxLoader">loading tooltip...</span>');
        continueTooltip();
        // request new content
        jq$.ajax(src, {
          success: function(json) {
            let html = json;
            if (_EC.isBlank(html)) {
              html = "No tooltip available";
            }
            try {
              const obj = jq$.parseJSON(json);
              if (jq$.isArray(obj)) html = obj[0];
            } catch (ignore) {
            }
            origin.tooltipster('update', html).tooltipster('reposition');
          },
          error: function(request, status, error) {
            KNOWWE.notification.error(error, "Cannot get tooltip content", src);
            origin.tooltipster('hide');
          }
        });
      }
    })
  });
};

KNOWWE.kdomtreetable = {};

KNOWWE.kdomtreetable.init = function() {
  jq$('.renderKDOMTable').each(function() {
    jq$(this).agikiTreeTable({
      expandable: true,
      clickableNodeNames: true,
      persist: true,
      article: jq$(this).closest(".defaultMarkupFrame").attr("id")
    });
  });
  KNOWWE.kdomtreetable.setOverflow();
};

KNOWWE.kdomtreetable.setOverflow = function() {
  jq$('.table_text').hover(function() {
    const elem = jq$(this);
    elem.data("stillin", "yes");
    setTimeout(function() {
      if (elem.data("stillin") === "yes") {
        elem.css("overflow", "auto");
      }
    }, 700);
  }, function() {
    jq$(this).data('stillin', "no");
    jq$(this).css("overflow", "hidden");
  });
};

KNOWWE.kdomtreetable.revealRenderKDOMTable = function(id) {
  const treetable = jq$('.renderKDOMTable.wikitable.treetable').not(".floatThead-table");
  const markedtitle = treetable.find('td[style="color: rgb(0, 0, 255);"]');
  if (typeof markedtitle != "undefined" || markedtitle != null) {
    for (let i = 0; i < markedtitle.size(); i++) {
      jq$(markedtitle[i]).removeAttr("style");
    }
  }

  const tablerow = treetable.find('tr[data-tt-id="kdom-row-' + id + '"]');
  treetable.treetable("reveal", "kdom-row-" + id);
  tablerow.find("td").first().css("color", "rgb(0, 0, 255)");
  jq$('html, body').animate({
    scrollTop: (tablerow.offset().top - 250)
  }, 400);
};

KNOWWE.kdomtreetable.collapseAll = function() {
  const treetable = jq$('.renderKDOMTable.wikitable.treetable')[0];
  jq$(treetable).treetable("collapseAll");
};

KNOWWE.core.plugin.attachment = function() {

  return {

    update: function(sectionId) {
      const params = {
        action: 'AttachmentUpdateAction',
        SectionID: sectionId
      };

      const options = {
        url: KNOWWE.core.util.getURL(params),
        fn: function() {
          window.location.reload();
        }
      };
      new _KA(options).send();
    }
  }
}();

KNOWWE.core.plugin.formatterAjax = function(id, actionClass) {

  const textarea = jq$("#defaultEdit" + id);
  const wikiText = textarea.val();

  jq$.ajax("action/" + actionClass, {
    data: {
      sectionID: id,
      wikiText: wikiText
    },
    type: 'post',
    cache: false,
    success: function(json) {
      textarea.val(json.wikiText);
    }
  });

  return;

}

KNOWWE.core.plugin.reloadNamespaceFile = function() {

  return {

    reloadFile: function(namespaceUrl, filename, title) {
      jq$.ajax("action/NamespaceFileReloadAction", {
        type: 'post',
        data: {
          namespaceUrl: namespaceUrl,
          filename: filename,
          title: title
        },
        success: function() {
          KNOWWE.notification.success("Success", "You successfully reloaded the namespace file in the attachment.", filename);
          //KNOWWE.core.util.reloadPage();

        },
        error: function() {
          KNOWWE.notification.loadNotifications();
        }
      });
    }
  }
}();

KNOWWE.core.plugin.recompile = function() {

  return {

    init: function() {
      jq$(document).keyup(function(event) {
          let command = null;
          const isCMD = event.ctrlKey || event.metaKey;
          if (isCMD && event.altKey && event.shiftKey && (event.key === 'R' || event.key === '¸')) {
            command = "recompileAll";
            KNOWWE.notification.success("Full Recompile", "Performing recompilation of the current page and all involved compilers.");
          } else if (isCMD && event.altKey && (event.key === 'r' || event.key === '®')) {
            command = "recompile"
            KNOWWE.notification.success("Recompile", "Recompiling the current page.");
          } else {
            return;
          }

          event.preventDefault();
          event.stopPropagation();

          jq$.ajax({
            url: "action/RecompileAction",
            data: {
              command: command,
              title: KNOWWE.helper.getPagename()
            },
          }).success(function() {
            // just refresh, server will wait for compilation to finish before rendering
            window.location.reload();
          });
        }
      );
    }
  }
}();


KNOWWE.core.plugin.switchCompiler = function() {

  function addClickAction(link) {
    jq$(link).click(function(event) {
      var parentSelector = jq$(this).attr('data-click-parent');
      var parent = jq$(parentSelector);
      var openParent = jq$('.open-click-parent');
      // Close already open parents
      if (!openParent.is(parent)) {
        openParent.removeClass('open open-click-parent');
      }
      if (parent.hasClass('open')) {
        parent.removeClass('open open-click-parent');
      } else {
        parent.addClass('open open-click-parent');
        if (parent.find('input').length > 0) {
          parent.find("input:first").focus();
        }
      }
      event.preventDefault();
      event.stopPropagation();
      return false;
    });
  }

  let defaultCompilerPrefixKey = "default-compiler";

  function getCompilerListContent(compilers, defaultCompiler) {
    let listInnerText = "";
    for (compiler of compilers) {
      let icon = "far fa-circle";
      if (compiler === defaultCompiler) {
        icon = "far fa-dot-circle";
      }
      listInnerText = listInnerText + '<li><a onclick="KNOWWE.core.plugin.switchCompiler.setDefaultCompiler(\'' + compiler + '\')"><span class="' + icon + '" style="padding-right: 5px"></span><span>' + compiler + '</span></a></li>';
    }
    return listInnerText;
  }

  return {
    init: function() {
      if (!jq$('.navigation').exists()) return;

      let pullRight = jq$('.navigation').children(".nav.nav-pills.pull-right").first();
      let compilerSwitch = new Element('li', {
        'id': 'compilerSwitch'
      });
      let link = new Element('a', {
        'id': 'selectedCompiler',
        'href': '#',
        'data-click-parent': '#compilerSwitch'
      });
      let list = new Element('ul', {
        'id': 'compilerList',
        'class': 'dropdown-menu pull-right'
      });
      // get the inner html
      jq$.ajax("action/GetCompilerSwitchContentAction", {
        cache: false,
        dataType: 'json',
        success: function(response) {
          if (Object.keys(response).length === 0) return;
          let storedDefaultCompiler = localStorage.getItem(defaultCompilerPrefixKey);
          let defaultCompiler = response.defaultCompiler;
          let compilers = response.compilers;
          if (storedDefaultCompiler === "null" || !compilers.contains(storedDefaultCompiler)) {
            // If default compiler is not stored yet or the stored compiler is not contained in all given compilers,
            // store the default compiler from the server also for the client.
            storedDefaultCompiler = defaultCompiler;
            localStorage.setItem(defaultCompilerPrefixKey, defaultCompiler);
          }
          if (storedDefaultCompiler === defaultCompiler) {
            // If the default compiler of the client and server (for the user) are the same, add the compiler switch.
            let linkContent = '<span class="far fa-microchip"></span>' + '<span>' + defaultCompiler + '</span>';
            if (compilers.length > 1) {
              linkContent = linkContent + '<span class="caret"></span>';
            }
            link.innerHTML = linkContent;

            if (compilers.length > 1) {
              // only add dropdown to select
              addClickAction(link);
              list.innerHTML = getCompilerListContent(compilers, defaultCompiler);
              compilerSwitch.append(list);
            }
            compilerSwitch.appendChild(link);
            pullRight.prepend(compilerSwitch);
          } else {
            // If the stored default compiler is not the same than the default compiler from the server,
            // set the stored default compiler also for the server.
            // This case should only happen after a restart of the server and the first page is rendered twice.
            KNOWWE.core.plugin.switchCompiler.setDefaultCompiler(storedDefaultCompiler);
          }
        }
      });
    },

    setDefaultCompiler: function(name) {
      jq$.ajax("action/SetDefaultCompilerAction?name=" + name, {
        cache: false,
        dataType: 'json',
        success: function() {
          localStorage.setItem(defaultCompilerPrefixKey, name);
          window.location.reload();
        }
      });
    }
  };
}();

KNOWWE.core.plugin.stickyTableHeaders = function() {
  return {
    init: function() {
      jq$(".haddock table.sticky-header, .haddock .wikitable").not('.renderKDOMTable').floatThead({
        top: function() {
          let $header = jq$(".header .navigation");
          let rect = $header[0].getBoundingClientRect();
          return rect.top + rect.height;
        },
        responsiveContainer: function($table) {
          return $table.closest('.zebra-table, .scroll-parent');
        },
        zIndex: 400, // has to be < than wiki header, which is 1001
      });

      function update() {
        window.dispatchEvent(new CustomEvent('resize'));
        window.dispatchEvent(new CustomEvent('scroll'));
      }

      // trigger scroll and resize event to make sure sticky position gets recalculated when page height changes
      KNOWWE.helper.observer.subscribe("afterRerender", update);
      jq$(".haddock .header").on("transitionend", update);

      // special handling for sidebar, we want to continuously update while animating
      let transitionOngoing = false
      jq$(".haddock .sidebar").on("transitionstart", function() {
        transitionOngoing = true;

        function continuousUpdate() {
          if (!transitionOngoing) return;
          window.dispatchEvent(new CustomEvent('scroll'));
          window.requestAnimationFrame(continuousUpdate);
        }

        window.requestAnimationFrame(continuousUpdate);
      });
      jq$(".haddock .sidebar").on("transitionend transitioncancel", function() {
        transitionOngoing = false;
        update();
      });
    }
  }
}();

/* ############################################################### */
/* ------------- Onload Events ---------------------------------- */
/* ############################################################### */
(function init() {
  window.addEvent('domready', _KL.setup);

  if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
    window.addEvent('domready', function() {
      KNOWWE.tooltips.enrich();
      KNOWWE.core.plugin.renderKDOM();
      KNOWWE.kdomtreetable.init();
      KNOWWE.core.plugin.recompile.init();
      KNOWWE.plugin.anchor.init();
    });
    jq$(window).on('hashchange', function() {
      KNOWWE.plugin.anchor.init();
    });
  }
}());

KNOWWE.helper.observer.subscribe("afterRerender", function() {
  KNOWWE.tooltips.enrich(this);
  KNOWWE.core.plugin.objectinfo.lookUp(this);
  KNOWWE.core.plugin.stickyTableHeaders.init();
});

jq$(document).ready(function() {
  KNOWWE.core.plugin.switchCompiler.init();
  KNOWWE.core.plugin.stickyTableHeaders.init();
});
