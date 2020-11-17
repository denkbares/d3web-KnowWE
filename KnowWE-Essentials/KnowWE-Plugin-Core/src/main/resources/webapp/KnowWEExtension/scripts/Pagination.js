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

  const initialState = "initial-state";
  const columnNameAttribute = "column-name";
  const filterTextQueryAttribute = "filter-text-query";
  const sortingIcon = "sorting-icon";
  const filterIcon = "filter-icon";
  const filterProviderActionAttribute = 'filter-provider-action';
  const filterTextsProperty = "filter-texts";
  const paginationClickEvent = "click.pagination-filter";

  let windowHeight;

  const getColumnName = $th => {
    return $th.attr(columnNameAttribute) || $th.text();
  };

  function setPaginationStateAndUpdateNode(cookie, id) {
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

  function getFilterSymbol(empty) {
    const $symbol = jq$('<i/>', {"class": 'fa-filter'});
    $symbol.addClass(filterIcon);
    $symbol.addClass(empty ? "far" : "fa");
    if (empty) $symbol.css("color", "grey");
    return $symbol;
  }

  function getPaginationState(sectionId) {
    return KNOWWE.helper.getLocalSectionStorage(sectionId).pagination || {};
  }

  function setPaginationState(id, state) {
    KNOWWE.helper.setToLocalSectionStorage(id, "pagination", state);
  }

  function isEmpty(columnState) {
    return !columnState || (columnState.selectAll && columnState.selectedTexts.length === 0 && columnState.selectedCustomTexts.length === 0);
  }

  function anyActiveFilter(filterState) {
    if (!filterState.columns) return false;
    for (let column in filterState.columns) {
      if (!filterState.columns.hasOwnProperty(column)) continue;
      if (!isEmpty(filterState.columns[column])) return true;
    }
    return false;
  }

  function renderIcons($table, sectionId, sortingMode) {
    const paginationState = getPaginationState(sectionId);

    $table.find("th").each(function() {
      jq$(this.firstChild).wrap('<span></span>');
    })

    let $paginationTable = jq$("table[pagination=" + sectionId + "]");
    let sorting = paginationState.sorting || [];
    for (let i = 0; i < (sortingMode === 'multi' ? sorting.length : 1); i++) {
      let sort = sorting[i].sort;
      const sortingSymbolParent = $paginationTable.find("th[column-name=" + sort + "] span");
      const sortingSymbol = jq$(sortingSymbolParent).find("." + sortingIcon);
      if (sortingSymbol.exists()) {
        sortingSymbol.replaceWith(getSortingSymbol(sorting[i].naturalOrder, i));
      } else {
        jq$(sortingSymbolParent).append(getSortingSymbol(sorting[i].naturalOrder, i));
      }
    }

    if (paginationState.filter && paginationState.filter.active) {
      let columns = paginationState.filter.columns || [];
      $table.find("th").each(function() {
        const $th = jq$(this);

        $th.prepend(getFilterSymbol(isEmpty(columns[getColumnName($th)])));
      });
    }
  }

  function enableSorting($thElement, sectionId) {
    if (!$thElement.hasClass("notSortable")) {
      $thElement.addClass("sortable");
      $thElement.find("span").bind('click', function(event) {
          if (event.altKey) return;
          KNOWWE.core.plugin.pagination.sort(this, sectionId);
        }
      );
    }
  }

  function getPaginationFilterState(paginationState) {
    if (!paginationState.filter) paginationState.filter = {};
    return paginationState.filter;
  }

  function enableFiltering($thElement, sectionId) {

    let filterProviderAction = $thElement.attr(filterProviderActionAttribute);
    if (!filterProviderAction) return; // if no action is defined, we cannot support filtering

    // init state
    const paginationState = getPaginationState(sectionId);
    const filterState = getPaginationFilterState(paginationState);
    if (!filterState.columns) filterState.columns = {};

    const columnName = getColumnName($thElement);
    if (!filterState.columns[columnName]) {
      filterState.columns[columnName] = {
        selectAll: true,
        customTexts: [],
        selectedCustomTexts: [],
        selectedTexts: [],
      };
    }
    const columnState = filterState.columns[columnName];
    $thElement.data(initialState, JSON.parse(JSON.stringify(columnState)));
    let filterTextsJson = {}; // will be initialized in ajaxFilterTexts
    let latestFilterTextQuery = ""; // will be initialized in ajaxFilterTexts
    let $tooltip = null; // will be initialized in initTooltip

    const saveAndCloseFilter = $filterIcon => {
      jq$(document).off(paginationClickEvent); // in case we close via buttons
      if (isValidState()) {
        $filterIcon.tooltipster("hide");
      } else {
        cancelFilter($filterIcon);
      }
      if ($tooltip && latestFilterTextQuery.length > 0) {
        const checked = $tooltip.find(".pagination-filter-list input:checked");
        if (checked.exists()) {
          columnState.selectAll = false;
          columnState.selectedTexts = [];
          checked.each(function() {
            getFilterTexts(jq$(this)).forEach(text => {
              columnState.selectedTexts.push(text);
            });
          });
          setPaginationState(sectionId, paginationState);
        }
      }
    };

    const cancelFilter = $filterIcon => {
      jq$(document).off(paginationClickEvent); // in case we close via buttons
      const $thElement = $filterIcon.parents('th');
      filterState.columns[getColumnName($thElement)] = $thElement.data(initialState);
      setPaginationState(sectionId, paginationState);
      $filterIcon.tooltipster("hide");
    };

    // function to generate the tooltip content on demand
    const generateTooltip = (origin, continueTooltip) => {
      if (!$thElement.attr(filterProviderActionAttribute)) {
        // nothing to do, we already retrieved the filter texts
        continueTooltip();
        return;
      }

      $thElement.removeAttr(filterProviderActionAttribute); // prevent second additional ajax calls

      ajaxFilterTexts("", () => {
        origin.tooltipster('content', getTooltipContent()).tooltipster('reposition');
        continueTooltip();
      });

    };

    // show little spinner at the filter text input
    const showSpinner = () => {
      if ($tooltip) {
        $tooltip.find('.filter-input-indicator-parent').addClass("loading-texts")
      }
    };

    // hide little spinner at the filter text input
    const hideSpinner = () => {
      if ($tooltip) {
        $tooltip.find('.filter-input-indicator-parent').removeClass("loading-texts")
      }
    };

    // function to get filter texts
    const ajaxFilterTexts = (filterTextQuery, callback) => {
      latestFilterTextQuery = filterTextQuery;
      showSpinner($tooltip);
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
            if (typeof callback === "function") {
              callback();
            }
            hideSpinner();
          }
        },
        error: function() {
          hideSpinner();
          _EC.onErrorBehavior();
        }
      });
    };

    // function to initialize functionality after tooltip content has be loaded and inserted
    const initTooltip = (origin, tooltip) => {
      $tooltip = jq$(tooltip); // init tooltip variable in closure
      initFilterInput($tooltip);
      initFilterList($tooltip);
      initButtons($tooltip);
      $tooltip.on("click", function(event) {
        event.stopPropagation(); // don't allow click to bubble up to document, closing tooltip again
      });
      $tooltip.on("keyup", function(event) {
        if (event.originalEvent.code === 'Enter') {
          $tooltip.find('.ok-button').click();
        } else if (event.originalEvent.code === 'Escape') {
          $tooltip.find('.cancel-button').click();
        }
      });
    };


    // open/close filter dialog/tooltip
    const $filterIcon = $thElement.find('.filter-icon');
    if (!$filterIcon.hasClass('tooltipstered')) {
      $filterIcon.tooltipster({
        interactive: true,
        updateAnimation: false,
        trigger: "custom",
        theme: "tooltipster-knowwe pagination-filter-tooltipster",
        content: "Loading...",
        functionBefore: generateTooltip,
        functionReady: initTooltip,
        functionAfter: () => updateNode(sectionId)
      });
      $filterIcon.click(function(event) {
        // hide other open filter tooltips
        $filterIcon.parents('tr').first().find('.filter-icon')
          .filter('.tooltipstered')
          .filter((i, e) => e !== $filterIcon[0])
          .each(function() {
            cancelFilter(jq$(this));
          })
        // open filter tooltip
        $filterIcon.tooltipster("show");
        // prevent closing it again immediately
        event.stopPropagation();
        // close tooltip when clicking outside of it
        jq$(document).off(paginationClickEvent); // just to be sure
        jq$(document).on(paginationClickEvent, function() {
          cancelFilter($filterIcon);
        })
      });
    }
    $filterIcon.addClass("tooltipstered");

    // generate html for filter list
    const getFilterList = () => {
      const selectAll = columnState.selectAll;
      const selectedTexts = columnState.selectedTexts;
      const filterTexts = filterTextsJson[filterTextsProperty];
      const isSelected = (text, array) => (selectAll && !array.includes(text)) || (!selectAll && array.includes(text));
      const encodeHTML = s => jq$("<div/>").text(s).html();

      return "<ul class='pagination-filter-list'>\n" +
        filterTexts.map((textsArray, i) => {
          let rendered = textsArray[0];
          let texts = textsArray.slice(1);
          let id = "filter" + i;
          return "<li class='query'>" +
            "<input type='checkbox' id='" + id + "' name='" + id + "' " + (isSelected(texts[0], selectedTexts) ? "checked" : "") + ">" +
            "<label for='" + id + "'>" + encodeHTML(rendered) + "</label><div style='display: none'>" + encodeHTML(JSON.stringify(texts)) + "</div>" +
            "</li>\n";
        }).join('') +
        "</ul>";
    };

    // update the filter list
    const updateFilterList = $tooltip => {
      $tooltip.find('.pagination-filter-list').replaceWith(getFilterList());
      initFilterList($tooltip);
    };

    // generate html for tooltip content
    const getTooltipContent = () => {
      return jq$("<div class='filter-parent'><label for='filter-input'>Filter:</label><input id='filter-input'>" +
        "<div class='filter-input-indicator-parent'><i class='filter-input-indicator fas fa-spinner fa-pulse'/></div>\n" +
        "<div class='toggle-box-parent'><input class='toggle-box' id='toggle-checkbox' type='checkbox'><label for='toggle-checkbox'>All</label></div>" +
        getFilterList() +
        "<div class='close-buttons'>" +
        "<button class='ok-button'>Ok</button>" +
        "<button class='cancel-button'>Cancel</button>" +
        "</div></div>");
    };

    // init events for "Filter: ..." text input
    const initFilterInput = $tooltip => {
      $tooltip.find('#filter-input').keyup(function(event) {
        if (event.originalEvent.code === 'Enter') {
          saveAndCloseFilter($filterIcon);
        } else {
          const filterText = jq$(this).val();
          columnState.filterText = filterText;
          setPaginationState(sectionId, paginationState);
          ajaxFilterTexts(filterText, () => {
            updateFilterList($tooltip);
          })
        }
      });
    };

    // update the state of buttons
    const updateButtonState = $tooltip => {
      const $toggleBox = $tooltip.find('.toggle-box');
      if (columnState.selectedTexts.length !== 0 || columnState.selectedCustomTexts.length !== 0) {
        $toggleBox.prop("indeterminate", true);
      } else {
        $toggleBox.prop("indeterminate", false);
        $toggleBox.prop("checked", columnState.selectAll);
      }

      $tooltip.find('.ok-button').prop("disabled", !isValidState());
    };

    const isValidState = () => columnState.selectAll || columnState.selectedTexts.length !== 0 || columnState.selectedCustomTexts.length !== 0;
    const getFilterTexts = $checkBox => JSON.parse($checkBox.parent().find('div').text());

    // init events for filter check boxes
    const initFilterList = function($tooltip) {
      const deleteFromArray = (selectedTexts, value) => {
        if (selectedTexts) {
          const index = selectedTexts.indexOf(value);
          if (index >= 0) selectedTexts.splice(index, 1);
        }
      };

      $tooltip.find('li input').change(function() {
        const $checkBox = jq$(this);
        const texts = getFilterTexts($checkBox);
        if (this.checked && !columnState.selectAll || !this.checked && columnState.selectAll) {
          texts.forEach(text => {
            if (!columnState.selectedTexts.includes(text)) {
              columnState.selectedTexts.push(text);
            }
          });
        } else {
          texts.forEach(text => {
            deleteFromArray(columnState.selectedTexts, text);
            deleteFromArray(columnState.selectedCustomTexts, text);
          });
        }
        updateButtonState($tooltip);
        setPaginationState(sectionId, paginationState);
      });
    };

    // init buttons
    const initButtons = $tooltip => {
      updateButtonState($tooltip);
      $tooltip.find('.toggle-box').click(function() {
        columnState.selectAll = this.checked;
        columnState.selectedTexts = [];
        columnState.selectedCustomTexts = [];
        updateButtonState($tooltip);
        setPaginationState(sectionId, paginationState);
        updateFilterList($tooltip);
      });
      $tooltip.find('.ok-button').click(function() {
        saveAndCloseFilter($filterIcon);
      });
      $tooltip.find('.cancel-button').click(function() {
        cancelFilter($filterIcon);
      });
    };
  }

  function isShortTable() {
    return (jq$(this).height() < jq$(window).height());
  }

  function toggleLowerPagination(visibility) {
    jq$(this).parents('.sparqlTable').nextAll().filter('.knowwe-paginationToolbar').css("display", visibility);
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

      // decorate "Filter" checkbox
      const paginationState = getPaginationState(sectionId);
      const filterState = getPaginationFilterState(paginationState);
      const filterActivator = jq$(this).parents('.knowwe-paginationWrapper').find('.filter-activator');
      if (filterState.active) {
        filterActivator.prop('checked', true);
      }
      filterActivator.change(function() {
        filterState.active = !!this.checked;
        setPaginationState(sectionId, paginationState);
        updateNode(sectionId)
      })
      const clearFilter = jq$(this).parents('.knowwe-paginationWrapper').find('.clear-filter');
      clearFilter.click(function() {
        filterState.columns = {}
        setPaginationState(sectionId, paginationState);
        updateNode(sectionId)
      })
      if (!filterState.active) {
        clearFilter.hide();
      } else {
        clearFilter.prop("disabled", !anyActiveFilter(filterState));
      }

      // render sorting symbol
      renderIcons(jq$(this), sectionId, sortingMode);

      jq$(this).find("th").each(function() {
          // make <th> clickable and therefore sortable except if
          // it's stated explicitly otherwise
          const $thElement = jq$(this);
          if (typeof sortingMode != 'undefined') {
            enableSorting($thElement, sectionId);
          }
          enableFiltering($thElement, sectionId);
        }
      );

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
      const sortingName = getColumnName(jq$(element).parent());
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
      setPaginationStateAndUpdateNode(cookie, id);
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

      setPaginationStateAndUpdateNode(cookie, id);
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
      setPaginationStateAndUpdateNode(cookie, id);

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
