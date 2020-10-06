/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

/**
 * Namespace: KNOWWE.core.plugin.pagination The KNOWWE plugin d3web namespace.
 */
KNOWWE.core.plugin.pagination = function() {

  const columnNameAttribute = "column-name";
  const filterTextQueryAttribute = "filter-text-query";
  const sortingIcon = "sorting-icon";
  const filterIcon = "filter-icon";
  const filterProviderActionAttribute = 'filter-provider-action';
  const filterTextsProperty = "filter-texts";

  let windowHeight;

  function setPaginaionStateAndUpdateNode(cookie, id) {
    setPaginationState(id, cookie);
    updateNode(id);
  }

  function updateNode(id) {
    jq$('#' + id).rerender();
  }

  function getSortingSymbol(naturalOrder, index) {
    let cssClass;
    if (naturalOrder) {
      cssClass = "fa fa-caret-down fa-lg " + sortingIcon;
    } else {
      cssClass = "fa fa-caret-up fa-lg " + sortingIcon;
    }
    if (index !== 0) cssClass += " secondary";
    return jq$('<i/>', {
      "class": cssClass
    });
  }

  function getFilterSymbol() {
    return jq$('<i/>', {
      "class": 'fa fa-filter ' + filterIcon
    });
  }

  function getPaginationState(sectionId) {
    return KNOWWE.helper.getLocalSectionStorage(sectionId).pagination || {};
  }

  function setPaginationState(id, state) {
    KNOWWE.helper.setToLocalSectionStorage(id, "pagination", state);
  }

  function isEmpty(columnState) {
    return columnState.selectAll && columnState.selectedTexts.length === 0 && columnState.selectedCustomTexts.length === 0;
  }

  function renderIcons(sectionId, sortingMode) {
    const paginationState = getPaginationState(sectionId);

    let $paginationTable = jq$("table[pagination=" + sectionId + "]");
    let sorting = paginationState.sorting || [];
    for (let i = 0; i < (sortingMode === 'multi' ? sorting.length : 1); i++) {
      let sort = sorting[i].sort;
      const sortingSymbolParent = $paginationTable.find("th:contains('" + sort + "') span");
      const sortingSymbol = jq$(sortingSymbolParent).find("." + sortingIcon);
      if (sortingSymbol.exists()) {
        sortingSymbol.replaceWith(getSortingSymbol(sorting[i].naturalOrder, i));
      } else {
        jq$(sortingSymbolParent).append(getSortingSymbol(sorting[i].naturalOrder, i));
      }
    }

    let columns = (paginationState.filter || {}).columns || [];
    for (let column in columns) {
      if (!columns.hasOwnProperty(column)) continue;
      if (isEmpty(columns[column])) continue;
      $paginationTable.find("th:contains('" + column + "') span").prepend(getFilterSymbol());
    }
  }

  function enableSorting(sectionId) {
    jq$(this.firstChild).wrap('<span></span>');
    if (!jq$(this).hasClass("notSortable")) {
      jq$(this).addClass("sortable");
      jq$(this).find("span").bind('click', function(event) {
          if (event.altKey) return;
          KNOWWE.core.plugin.pagination.sort(this, sectionId);
        }
      );
    }
  }

  function enableFiltering(sectionId) {
    let $thElement = jq$(this);

    let filterProviderAction = $thElement.attr(filterProviderActionAttribute);
    if (!filterProviderAction) return; // if no action is defined, we cannot support filtering

    const paginationState = getPaginationState(sectionId);
    if (!paginationState.filter) paginationState.filter = {};
    const filterState = paginationState.filter;
    if (!filterState.columns) filterState.columns = {};

    const columnName = $thElement.attr(columnNameAttribute);
    if (!filterState.columns[columnName]) {
      filterState.columns[columnName] = {
        selectAll: true,
        customTexts: [],
        selectedCustomTexts: [],
        selectedTexts: [],
      };
    }
    const columnState = filterState.columns[columnName];
    let filterTextsJson = {};
    let latestFilterTextQuery = "";

    const ajaxFilterTexts = (filterTextQuery, callback) => {
      latestFilterTextQuery = filterTextQuery;
      jq$.ajax({
        url: KNOWWE.core.util.getURL({
          action: filterProviderAction
        }),
        type: 'post',
        data: function() {
          let data = {SectionID: sectionId};
          data[columnNameAttribute] = columnName;
          data[filterTextQueryAttribute] = filterTextQuery;
          return data;
        }(),
        success: function(json) {
          if (json[filterTextQueryAttribute] === latestFilterTextQuery) { // if not equal, response is outdated...
            filterTextsJson = json;
            if (typeof callback === "function") callback();
          }
        },
        error: _EC.onErrorBehavior
      });
    };

    const generateTooltip = (origin, continueTooltip) => {
      if (!$thElement.attr(filterProviderActionAttribute)) {
        // nothing to do, we already retrieved the filter texts
        continueTooltip();
        return;
      }

      continueTooltip();

      $thElement.removeAttr(filterProviderActionAttribute); // prevent second additional ajax calls

      ajaxFilterTexts("", () => {
        origin.tooltipster('content', getTooltipContent()).tooltipster('reposition');
      });

    };

    $thElement.find("span").bind('click', function(event) {
      if (!event.altKey) return;

      if (!$thElement.hasClass('tooltipstered')) {

        $thElement.tooltipster({
          interactive: true,
          interactiveTolerance: 500,
          updateAnimation: false,
          trigger: "click",
          theme: "tooltipster-knowwe",
          functionBefore: generateTooltip,
          functionReady: initTooltip,
          functionAfter: () => updateNode(sectionId)
        });
      }
      $thElement.addClass("tooltipstered");
      $thElement.tooltipster('show');
    });

    const getFilterList = () => {
      const selectAll = columnState.selectAll;
      const customTexts = columnState.customTexts;
      const selectedCustomTexts = columnState.selectedCustomTexts;
      const selectedTexts = columnState.selectedTexts.concat(customTexts);

      const isSelected = (text, array) => (selectAll && !array.includes(text)) || (!selectAll && array.includes(text));

      return "<ul class='pagination-filter-list'>\n" +
        customTexts.concat(filterTextsJson[filterTextsProperty]).map((text, i) => {
          let id = "filter" + i;
          return "<li class='" + (isSelected(text, selectedCustomTexts) ? "custom" : "query") + "'>" +
            "<input type='checkbox' id='" + id + "' name='" + id + "' " + (isSelected(text, selectedTexts) ? "checked" : "") + ">" +
            "<label for='" + id + "'>" + text + "</label>" +
            "</li>\n";
        }).join('') +
        "</ul>";
    };

    const updateFilterList = $tooltip => {
      $tooltip.find('.pagination-filter-list').replaceWith(getFilterList());
      initFilterList($tooltip);
    };

    const getTooltipContent = () => {
      return jq$("<div class='filter-parent'><label for='filter-input'>Filter:</label><input id='filter-input'>\n" +
        "<div><button class='toggle-button'>Toggle All</button></div>" +
        getFilterList() +
        "</div>");
    };

    const initFilterInput = $tooltip => {
      $tooltip.find('#filter-input').keyup(function() {
        const filterText = jq$(this).val();
        filterState.filterText = filterText;
        setPaginationState(sectionId, paginationState);
        ajaxFilterTexts(filterText, () => {
          updateFilterList($tooltip);
        })
      });
    };

    const initFilterList = function($tooltip) {
      const deleteFromArray = (selectedTexts, value) => {
        if (selectedTexts) {
          const index = selectedTexts.indexOf(value);
          if (index >= 0) selectedTexts.splice(index, 1);
        }
      };

      $tooltip.find('li input').change(function() {
        const text = jq$(this).parent().find('label').text();
        if (this.checked && !columnState.selectAll || !this.checked && columnState.selectAll) {
          if (!columnState.selectedTexts.includes(text)) {
            columnState.selectedTexts.push(text);
          }
        } else {
          deleteFromArray(columnState.selectedTexts, text);
          deleteFromArray(columnState.selectedCustomTexts, text);
        }
        setPaginationState(sectionId, paginationState);
      });
    };

    const initButtons = $tooltip => {
      $tooltip.find('.toggle-button').click(function() {
        if (columnState.selectedTexts.length === 0 && columnState.selectedCustomTexts.length === 0) {
          columnState.selectAll = !columnState.selectAll;
        } else {
          columnState.selectAll = true;
          columnState.selectedTexts = [];
          columnState.selectedCustomTexts = [];
        }
        setPaginationState(sectionId, paginationState);
        updateFilterList($tooltip);
      });
    };


    const initTooltip = (origin, tooltip) => {
      let $tooltip = jq$(tooltip);
      initFilterInput($tooltip);
      initFilterList($tooltip);
      initButtons($tooltip);
    };
  }

  function isShortTable() {
    return (jq$(this).height() < jq$(window).height());
  }

  function toggleLowerPagination(visibility) {
    const id = jq$(this).attr("pagination");
    jq$(".knowwe-paginationToolbar[pagination=" + id + "]").slice(2, 4).css("display", visibility);
  }

  function handlePaginationBelowTableVisibility() {
    if (isShortTable.call(this)) {
      toggleLowerPagination.call(this, "none");
    } else {
      toggleLowerPagination.call(this, "inline-block")
    }
  }

  jq$(window).resize(function() {
    if (windowHeight !== jq$(window).height()) {
      //Do something
      windowHeight = jq$(window).height();
      jq$("table[pagination]").each(
        function() {
          handlePaginationBelowTableVisibility.call(this);
        }
      )
    }
  });

  function decorate() {

    windowHeight = jq$(window).height();

    return function() {
      const sectionId = jq$(this).attr('pagination');

      //for css purposes
      jq$(this).addClass('knowwe-pagination');

      // register count selector
      jq$('div[pagination=' + sectionId + '] .count').on('change', function() {
        KNOWWE.core.plugin.pagination.setCount(this, sectionId);
      });

      // register start row change event
      jq$('div[pagination=' + sectionId + '] .startRow').on('change', function() {
        KNOWWE.core.plugin.pagination.updateStartRow(this, sectionId);
      });

      const sortingMode = jq$(this).attr('sortable');

      jq$(this).find("th").each(function() {
          // make <th> clickable and therefore sortable except if
          // it's stated explicitly otherwise
          if (typeof sortingMode != 'undefined') {
            enableSorting.call(this, sectionId);
          }
          enableFiltering.call(this, sectionId);
        }
      );

      // render sorting symbol
      renderIcons(sectionId, sortingMode);

      handlePaginationBelowTableVisibility.call(this);
    }
  }

  function handleNoResult(pagination) {
    if (!pagination.find('table').exists()) {
      pagination.find(".knowwe-paginationToolbar").remove();
    }
  }

  return {

    sort: function(element, id) {
      const cookie = getPaginationState(id);
      const sortingName = jq$(element).parent().attr(columnNameAttribute) || jq$(element).text();
      let sorting;
      if (typeof cookie.sorting == "undefined") {
        sorting = [{sort: sortingName, naturalOrder: true}];
      } else {
        sorting = cookie.sorting;
        let found = false;
        let remove = false;
        let i = 0
        for (; i < sorting.length; i++) {
          if (sorting[i].sort === sortingName) {
            if (i === 0) { // we only toggle sorting when primary sort column
              if (sorting[i].naturalOrder) { // clicked second time, reverse order
                sorting[i].naturalOrder = false;
              } else { // clicked third time, remove (not sorting this column)
                remove = true;
              }
            } else {
              sorting.move(i, 0);
            }
            found = true;
            break;
          }
        }
        if (remove) {
          sorting.splice(i, 1);
        }
        if (!found) {
          sorting.unshift({sort: sortingName, naturalOrder: true});
        }
      }
      cookie.sorting = sorting;
      setPaginaionStateAndUpdateNode(cookie, id);
    },

    setCount: function(selected, id) {
      const $selected = jq$(selected);

      const cookie = getPaginationState(id);

      const lastCount = parseInt(cookie.count);
      const resultSize = parseInt(jq$('#' + id + " .resultSize").val());
      const count = $selected.val();
      $selected.data('current', count);
      let startRow = parseInt(jq$('div[pagination=' + id + '] .startRow').val());
      const search = /^\d+$/;
      const found = search.test(startRow);
      if (!(found)) {
        jq$('div[pagination=' + id + '] .startRow').val('');
        return;
      }

      if (count === "Max") {
        cookie.startRow = 1;
        cookie.count = "Max";
      } else {
        if (startRow + lastCount === resultSize + 1) {
          startRow = resultSize - parseInt(count) + 1;
        }
        if (startRow <= 0) {
          startRow = 1;
        }
        cookie.startRow = startRow;
        cookie.count = count;
      }

      setPaginaionStateAndUpdateNode(cookie, id);
    },

    navigate: function(id, direction) {

      const count = jq$("#" + id + " .count").val();
      let startRow = jq$("#" + id + " .startRow").val();
      const resultSize = jq$("#" + id + " .resultSize").val();

      if (count === "All") {
        startRow = 1;
      } else {
        switch (direction) {
          case "begin":
            startRow = 1;
            break;
          case "back":
            if (parseInt(startRow) - parseInt(count) < 1) {
              startRow = 1;
            } else {
              startRow = parseInt(startRow) - parseInt(count);
            }
            break;
          case "forward":
            startRow = parseInt(startRow) + parseInt(count);
            break;
          case "end":
            startRow = parseInt(resultSize) - parseInt(count) + 1;
            break;
        }
      }


      const cookie = getPaginationState(id);
      cookie.startRow = startRow;
      cookie.count = count;
      setPaginaionStateAndUpdateNode(cookie, id);

    },

    updateStartRow: function(selectedRow, sectionId, preventRerender) {

      const id = sectionId;
      const cookie = getPaginationState(id);
      const count = jq$("#" + id + " .count").val();
      let startRow = selectedRow.value;
      const search = /^\d+$/;
      const found = search.test(startRow);
      if (!(found)) {
        jq$("#" + id + " .startRow").val('');
        return;
      }
      if (startRow <= 0) {
        startRow = 1;
      }
      if (count === "Max") {
        cookie.startRow = 1;
        cookie.count = "Max";
      } else {
        cookie.startRow = startRow;
        cookie.count = count;
      }
      setPaginationState(id, cookie);
      if (!preventRerender) updateNode(id);
    },

    decorateTable: function() {
      handleNoResult(jq$(this));
      const id = jq$(this).find(".knowwe-paginationToolbar").first().attr('pagination');
      jq$(this).find("table").attr('pagination', id);
      jq$(this).find("table[pagination]").each(decorate());
      KNOWWE.helper.observer.notify("paginationTableDecorated");
    },

    decorateTables: function() {
      handleNoResult(jq$(this));
      const wrappers = jq$("div.knowwe-paginationWrapper");
      wrappers.each(function() {
        jq$(this).find("table").attr('pagination', jq$(this).attr('id'));
      });

      jq$("table[pagination]").each(decorate());
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
      KNOWWE.core.plugin.pagination.decorateTables();
    });
  }
}());

KNOWWE.helper.observer.subscribe("afterRerender", function() {
  KNOWWE.core.plugin.pagination.decorateTable.call(this);
});
