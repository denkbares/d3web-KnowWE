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
KNOWWE.core.plugin = KNOWWE.core.plugin || {};

/**
 * Namespace: KNOWWE.core.plugin.pagination The KNOWWE plugin d3web namespace.
 */
KNOWWE.core.plugin.pagination = function() {

  const columnNameAttribute = "column-name";
  const filterTextQueryAttribute = "filter-text-query";
  const sortingIcon = "sorting-icon";
  const filterIcon = "filter-icon";
  const filterProviderActionAttribute = "filter-provider-action";
  const filterTextsProperty = "filter-texts";
  const paginationClickEvent = "click.pagination-filter";

  const getColumnName = $el => {
    return $el.attr(columnNameAttribute) || $el.text();
  };

  function setPaginationStateAndUpdateNode(paginationState, id) {
    setPaginationState(id, paginationState);
    updateNode(id);
  }

  function updateNode(id) {
    jq$("#" + id).rerender({reason: "pagination"});
  }

  function getSortingSymbol(naturalOrder, index) {
    let cssClass;
    if (naturalOrder) {
      cssClass = "fa fa-caret-down fa-lg " + sortingIcon;
    } else {
      cssClass = "fa fa-caret-up fa-lg " + sortingIcon;
    }
    if (index !== 0) cssClass += " secondary";
    return jq$("<i/>", {
      "class": cssClass
    });
  }

  function getFilterSymbol(empty) {
    const $symbol = jq$("<i/>", {"class": "fa-filter"});
    $symbol.addClass(filterIcon);
    $symbol.addClass(empty ? "far" : "fa");
    if (empty) $symbol.css("color", "grey");
    return $symbol;
  }

  function getFilterState(paginationState) {
    const filterState = getPaginationFilterState(paginationState);
    if (!filterState.columns) filterState.columns = {};
    return filterState;
  }

  function getColumnState(paginationState, columnName) {
    let filterState = getFilterState(paginationState);
    if (!filterState.columns[columnName]) {
      filterState.columns[columnName] = {
        selectAll: true,
        customTexts: [],
        selectedCustomTexts: [],
        selectedTexts: [],
        datepicker: {active: false, from: "", to: ""},
        hidden: false
      };
    }
    return filterState.columns[columnName];
  }

  function paginationKey(backup) {
    return "pagination" + (backup ? "-backup" : "");
  }

  function setPaginationState(id, paginationState, backup = false) {
    let key = paginationKey(backup);
    KNOWWE.helper.setToLocalSectionStorage(id, key, paginationState);
  }

  function getPaginationState(sectionId, backup = false) {
    let sectionStorage = KNOWWE.helper.getLocalSectionStorage(sectionId);
    let key = paginationKey(backup);
    return sectionStorage[key] || {};
  }

  function isEmpty(columnState) {
    if (!columnState) return true;
    if (columnState.hidden) return false;
    return columnState.selectAll && columnState.selectedTexts.length === 0 && columnState.selectedCustomTexts.length === 0 && !columnState.datepicker.active;
  }

  function anyActiveFilter(filterState) {
    if (!filterState.columns) return false;
    for (let column in filterState.columns) {
      if (!filterState.columns.hasOwnProperty(column)) continue;
      if (!isEmpty(filterState.columns[column])) return true;
    }
    return false;
  }

  function renderIcons($table, $columnFilterButton, sectionId, sortingMode, filteringActive) {

    $table.find("th").each(function() {
      jq$(this.firstChild).wrap("<span></span>");
    });

    let paginationState = getPaginationState(sectionId);

    if (sortingMode !== "off") {
      let sorting = paginationState.sorting || [];
      for (let i = 0; i < sorting.length; i++) {
        let sort = sorting[i].sort;
        const sortingSymbolParent = $table.find("th[column-name='" + sort + "'] span");
        const sortingSymbol = jq$(sortingSymbolParent).find("." + sortingIcon);
        if (sortingSymbol.exists()) {
          sortingSymbol.replaceWith(getSortingSymbol(sorting[i].naturalOrder, i));
        } else {
          jq$(sortingSymbolParent).append(getSortingSymbol(sorting[i].naturalOrder, i));
        }
      }
    }

    if (filteringActive && paginationState.filter && paginationState.filter.active) {
      let columns = paginationState.filter.columns || [];
      $table.find("th").each(function() {
        const $th = jq$(this);
        $th.prepend(getFilterSymbol(isEmpty(columns[getColumnName($th)])));
      });

      let empty = Object.entries(columns).filter(v => v[1].hidden).length === 0;
      $columnFilterButton.prepend(getFilterSymbol(empty));
    }
  }

  function enableSorting($thElement, sectionId) {
    if (!$thElement.hasClass("notSortable")) {
      $thElement.addClass("sortable");
      $thElement.find("span").bind("click", function(event) {
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

  function initTooltipster($element, generateTooltip, initTooltip, sectionId) {
    // noinspection JSUnusedGlobalSymbols
    $element.tooltipster({
      interactive: true,
      updateAnimation: false,
      trigger: "custom",
      theme: "tooltipster-knowwe pagination-filter-tooltipster",
      content: "Loading...",
      functionBefore: generateTooltip,
      functionReady: initTooltip
    });
  }

  function setBackupPaginationState(sectionId) {
    setPaginationState(sectionId, getPaginationState(sectionId), true);
  }


  function loadBackupPaginationState(sectionId) {
    return setPaginationState(sectionId, getPaginationState(sectionId, true));
  }

  function closeFilter(sectionId, reset = false) {
    let $openFilters = jq$(".pagination-column-filter.showing-tooltip, .sparqltable .filter-icon.showing-tooltip");
    if ($openFilters.exists()) {
      $openFilters.removeClass("showing-tooltip").tooltipster("hide");
      if (reset) loadBackupPaginationState(sectionId);
    }
  }

  function enableFiltering($thElement, sectionId) {

    let filterProviderAction = $thElement.attr(filterProviderActionAttribute);
    if (!filterProviderAction) return; // if no action is defined, we cannot support filtering

    const columnName = getColumnName($thElement);
    let filterTextsJson = {}; // will be initialized in ajaxFilterTexts
    let latestFilterTextQuery = ""; // will be initialized in ajaxFilterTexts
    let $tooltip = null; // will be initialized in initTooltip

    const saveAndCloseFilter = (sectionId) => {
      let paginationState = getPaginationState(sectionId);
      let columnState = getColumnState(paginationState, columnName);
      jq$(document).off(paginationClickEvent); // in case we close via buttons
      if (isValidState(columnState)) {
        closeFilter(sectionId);
      } else {
        cancelFilter(sectionId);
      }
      const filterCheckbox = document.getElementById("filter-checkbox");
      if ($tooltip && (latestFilterTextQuery.length > 0 || filterCheckbox?.checked)) {
        const datepickerFromText = document.getElementById("datepickerFrom")?.value;
        const datepickerToText = document.getElementById("datepickerTo")?.value;
        const checked = $tooltip.find(".pagination-filter-list input:checked");
        if (filterCheckbox?.checked && (datepickerFromText.length > 0 && datepickerToText.length > 0)) {
          const selectedFromDate = new Date(datepickerFromText);
          const selectedToDate = new Date(datepickerToText);
          selectedToDate.setDate(selectedToDate.getDate() + 1);
          let paginationState = getPaginationState(sectionId);
          const columnState = getColumnState(paginationState, columnName);
          columnState.selectAll = false;
          columnState.selectedTexts = [];
          columnState.datepicker = {active: true, from: selectedFromDate, to: selectedToDate};
          Array.from(document.querySelectorAll(".pagination-filter-list .query label"))
            .map(label => label.innerText)
            .filter(date => {
              const parsedDate = new Date(date);
              return parsedDate >= selectedFromDate && parsedDate <= selectedToDate;
            })
            .map(value => columnState.selectedTexts.push(value));
          setPaginationState(sectionId, paginationState);
        } else if (checked.exists()) {
          let paginationState = getPaginationState(sectionId);
          const columnState = getColumnState(paginationState, columnName);
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
      updateNode(sectionId);
      notifyFilterChanged(sectionId);
    };

    const cancelFilter = sectionId => {
      jq$(document).off(paginationClickEvent); // in case we close via buttons
      closeFilter(sectionId, true);
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
        origin.tooltipster("content", getTooltipContent(getFilterList)).tooltipster("reposition");
        continueTooltip();
      });

    };

    // show little spinner at the filter text input
    const showSpinner = () => {
      if ($tooltip) {
        $tooltip.find(".filter-input-indicator-parent").addClass("loading-texts");
      }
    };

    // hide little spinner at the filter text input
    const hideSpinner = () => {
      if ($tooltip) {
        $tooltip.find(".filter-input-indicator-parent").removeClass("loading-texts");
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
        type: "post",
        data: function() {
          let data = {SectionID: sectionId};
          data[columnNameAttribute] = columnName;
          data[filterTextQueryAttribute] = filterTextQuery;
          data["localSectionStorage"] = KNOWWE.helper.getLocalSectionStorage(sectionId, true);
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
      initFilterSearch($tooltip);
      initFilterCheckboxes($tooltip);
      initButtons($tooltip);
      $tooltip.on("click", function(event) {
        event.stopPropagation(); // don't allow click to bubble up to document, closing tooltip again
      });
      $tooltip.on("keyup", function(event) {
        if (event.originalEvent.code === "Enter") {
          $tooltip.find(".ok-button").click();
        } else if (event.originalEvent.code === "Escape") {
          $tooltip.find(".cancel-button").click();
        }
      });
    };


    // open/close filter dialog/tooltip
    const $filterIcon = $thElement.find(".filter-icon");
    if (!$filterIcon.hasClass("tooltipstered")) {
      initTooltipster($filterIcon, generateTooltip, initTooltip, sectionId);
      $filterIcon.click(function(event) {
        // prevent closing it again immediately
        event.stopPropagation();
        // close tooltip when clicking outside of it
        jq$(document).off(paginationClickEvent); // just to be sure
        jq$(document).on(paginationClickEvent, function() {
          cancelFilter(sectionId);
        });

        // hide other open filter tooltips
        closeFilter(sectionId, true);

        // open filter tooltip
        let $button = jq$(event.target);
        $button.tooltipster("show");
        setBackupPaginationState(sectionId);
        $button.addClass("showing-tooltip");
      });
    }
    $filterIcon.addClass("tooltipstered");

    // generate html for filter list
    const getFilterList = () => {
      let paginationState = getPaginationState(sectionId);
      let columnState = getColumnState(paginationState, columnName);
      const selectAll = columnState.selectAll;
      const selectedTexts = columnState.selectedTexts;
      const filterTexts = filterTextsJson[filterTextsProperty];
      const isSelected = (text, array) => (selectAll && !array.includes(text)) || (!selectAll && array.includes(text));
      const encodeHTML = s => jq$("<div/>").text(s).html();

      // check last element if it is a date so we are sure all values should be dates. First values can be "empty" so we have to check the last one
      const isDate = !isNaN(Date.parse(filterTexts[filterTexts.length - 1][0]));
      let datePicker = "";
      if (isDate) {
        const firstDate = filterTexts.map(array => new Date(array[0])).find(date => !isNaN(date));
        let firstTimeStrippedDate = firstDate.toISOString().split("T")[0];
        let lastTimeStrippedDate = new Date(filterTexts[filterTexts.length - 1][0]).toISOString().split("T")[0];
        if (columnState.datepicker?.to) {
          firstTimeStrippedDate = columnState.datepicker?.from.split("T")[0];
          lastTimeStrippedDate = columnState.datepicker?.to.split("T")[0];
        }
        datePicker = "<div class='filter-datepicker'>" +
          "<input type='checkbox' id='filter-checkbox' " + (!selectAll && columnState.datepicker?.active ? "checked" : "") + ">" +
          "<span class='filter-datepicker-outer'><span class='filter-datepicker-span'><label for='datepickerFrom'>From:</label><input type='date' id='datepickerFrom' class='filter-datepicker-input' value='" + firstTimeStrippedDate + "'></span>\n" +
          "<span class='filter-datepicker-span'><label for='datepickerTo'>To:</label><input type='date' id='datepickerTo' class='filter-datepicker-input' value='" + lastTimeStrippedDate + "'></span></span>" +
          "</div>";
      }

      return "<ul class='pagination-filter-list'>\n" +
        datePicker +
        filterTexts.map((textsArray, i) => {
          let rendered = textsArray[0];
          let texts = textsArray.slice(1);
          let id = "filter" + i;
          return "<li class='query'>" +
            "<input type='checkbox' id='" + id + "' name='" + id + "' " + (isSelected(texts[0], selectedTexts) ? "checked" : "") + ">" +
            "<label for='" + id + "'>" + encodeHTML(rendered) + "</label><div style='display: none'>" + encodeHTML(JSON.stringify(texts)) + "</div>" +
            "</li>\n";
        }).join("") +
        "</ul>";
    };

    // update the filter list
    const updateFilterList = $tooltip => {
      $tooltip.find(".pagination-filter-list").replaceWith(getFilterList());
      initFilterCheckboxes($tooltip);
    };

    // init events for "Filter: ..." text input
    const initFilterSearch = $tooltip => {
      $tooltip.find("#filter-input").keyup(function(event) {
        if (event.originalEvent.code === "Enter") {
          saveAndCloseFilter(sectionId);
        } else {
          let paginationState = getPaginationState(sectionId);
          let columnState = getColumnState(paginationState);
          const filterText = jq$(this).val();
          columnState.filterText = filterText;
          setPaginationState(sectionId, paginationState);
          ajaxFilterTexts(filterText, () => {
            updateFilterList($tooltip);
          });
        }
      });
    };

    // update the state of buttons
    const updateButtonState = $tooltip => {
      let paginationState = getPaginationState(sectionId);
      let columnState = getColumnState(paginationState, columnName);
      const $toggleBox = $tooltip.find(".toggle-box");
      if (columnState.selectedTexts.length !== 0 || columnState.selectedCustomTexts.length !== 0) {
        $toggleBox.prop("indeterminate", true);
      } else {
        $toggleBox.prop("indeterminate", false);
        $toggleBox.prop("checked", columnState.selectAll);
      }

      $tooltip.find(".ok-button").prop("disabled", !isValidState(columnState));
    };

    const isValidState = columnState => columnState.selectAll || columnState.selectedTexts.length !== 0 || columnState.selectedCustomTexts.length !== 0 || columnState.datepicker.active;
    const getFilterTexts = $checkBox => JSON.parse($checkBox.parent().find("div").text());

    // init events for filter check boxes
    const initFilterCheckboxes = function($tooltip) {
      const deleteFromArray = (selectedTexts, value) => {
        if (selectedTexts) {
          const index = selectedTexts.indexOf(value);
          if (index >= 0) selectedTexts.splice(index, 1);
        }
      };

      $tooltip.find("li input").change(function() {
        const $checkBox = jq$(this);
        let paginationState = getPaginationState(sectionId);
        let columnState = getColumnState(paginationState, columnName);
        const texts = getFilterTexts($checkBox);
        columnState.datepicker.active = false;
        $tooltip.find("#filter-checkbox")?.attr("checked", false);
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
        paginationState.startRow = 1;
        setPaginationState(sectionId, paginationState);
        updateButtonState($tooltip);
      });

      $tooltip.find("#filter-checkbox")?.change(function() {
        let paginationState = getPaginationState(sectionId);
        let columnState = getColumnState(paginationState, columnName);
        columnState.datepicker.active = this.checked;

        paginationState.startRow = 1;
        setPaginationState(sectionId, paginationState);
        updateButtonState($tooltip);
      });
    };

    // init buttons
    const initButtons = $tooltip => {
      updateButtonState($tooltip);
      $tooltip.find(".toggle-box").click(function() {
        let paginationState = getPaginationState(sectionId);
        let columnState = getColumnState(paginationState, columnName);
        columnState.selectAll = this.checked;
        columnState.selectedTexts = [];
        columnState.selectedCustomTexts = [];
        setPaginationState(sectionId, paginationState);
        updateFilterList($tooltip);
        updateButtonState($tooltip);
      });
      $tooltip.find(".ok-button").click(function() {
        saveAndCloseFilter(sectionId);
      });
      $tooltip.find(".cancel-button").click(function() {
        cancelFilter(sectionId);
      });
    };
  }

  function isShortTable(paginationWrapper) {
    return jq$(paginationWrapper).find("table").height() < jq$(window).height();
  }

  function toggleLowerPagination(paginationWrapper, visibility) {
    jq$(paginationWrapper).find(".sparqlTable").nextAll().filter(".knowwe-paginationToolbar").css("display", visibility);
  }

  function handlePaginationBelowTableVisibility(paginationWrapper) {
    if (isShortTable(paginationWrapper)) {
      toggleLowerPagination(paginationWrapper, "none");
    } else {
      toggleLowerPagination(paginationWrapper, "inline-block");
    }
  }

  function notifyFilterChanged(sectionId) {
    let paginationState = getPaginationState(sectionId);
    let configuredFilters = anyActiveFilter(getFilterState(paginationState));
    let filteringActive = paginationState.filter.active && configuredFilters;
    KNOWWE.helper.observer.notify("filterChanged", {
      filteringActive: filteringActive,
      sectionId: sectionId
    });
  }

  function decoratePagination($paginationWrapper, rerenderIfFilterActive) {

    const sectionId = $paginationWrapper.attr("id");

    const $table = $paginationWrapper.find("table");
    if (!$table.exists()) {
      $paginationWrapper.find(".knowwe-paginationToolbar").remove();
      $paginationWrapper.find(".download-tools").remove();
      return;
    }

    //for css purposes
    $table.addClass("knowwe-pagination");

    // register count selector
    $paginationWrapper.find("select.count").on("change", function() {
      KNOWWE.core.plugin.pagination.setCount(this, sectionId);
    });

    // register start row change event
    $paginationWrapper.find("input.startRow").on("change", function() {
      KNOWWE.core.plugin.pagination.updateStartRow(this, sectionId);
    });

    const filterActivator = $paginationWrapper.find(".filter-activator");
    const sortingMode = $paginationWrapper.attr("sorting-mode"); // from markup (not user specific)
    const filteringActive = $paginationWrapper.attr("filtering") === "true"; // from markup/annotation (not user specific)

    // decoratePagination "Filter" checkbox
    const paginationState = getPaginationState(sectionId);
    const filterState = getPaginationFilterState(paginationState);
    if (filterState.active) {
      filterActivator.prop("checked", true);
    }
    filterActivator.change(function() {
      const paginationState = getPaginationState(sectionId);
      const filterState = getPaginationFilterState(paginationState);
      filterState.active = !!this.checked;
      setPaginationState(sectionId, paginationState);
      updateNode(sectionId);
      notifyFilterChanged(sectionId);
    });
    const clearFilter = $paginationWrapper.find(".clear-filter");
    const filterTools = $paginationWrapper.find(".filter-tools");
    clearFilter.click(function() {
      let paginationState = getPaginationState(sectionId);
      let filterState = getFilterState(paginationState);
      filterState.columns = {};
      setPaginationState(sectionId, paginationState);
      updateNode(sectionId);
      notifyFilterChanged(sectionId);
    });
    if (!filterState.active) {
      filterTools.hide();
    } else {
      clearFilter.prop("disabled", !anyActiveFilter(filterState));
    }

    const $columnFilterButton = $paginationWrapper.find(".pagination-column-filter");

    // render sorting symbol
    renderIcons($table, $columnFilterButton, sectionId, sortingMode, filteringActive);


    $table.find("th").each(function() {
        // make <th> clickable and therefore sortable except if
        // it's stated explicitly otherwise
        const $thElement = jq$(this);
        if (sortingMode !== "off") {
          enableSorting($thElement, sectionId);
        }
        if (filteringActive) {
          enableFiltering($thElement, sectionId);
        }
      }
    );

    initColumnFiltering($columnFilterButton, $table, sectionId);

    handlePaginationBelowTableVisibility($paginationWrapper);

    KNOWWE.helper.observer.notify("paginationTableDecorated");

    // when loading filters initially, rerender to apply filter to result on server
    if (rerenderIfFilterActive && filterState.active) {
      updateNode(sectionId);
    }

    //on first load
    notifyFilterChanged(sectionId);

    KNOWWE.helper.observer.subscribe("registeredFilterChangedListener", function() {
      notifyFilterChanged(sectionId);
    });
  }

  function initColumnFiltering($columnFilterButton, $table, sectionId) {

    let latestFilterTextQuery = "";

    const columns = $table.parents(".sparqlTable").find("table").first().data("columns");

    const getColumnFilterList = () => {
      return "<ul class='pagination-filter-list'>\n" +
        columns.map((column, i) => {
          let id = "filter" + i;
          let columnName = column[0];
          let columnState = getColumnState(getPaginationState(sectionId), columnName);
          let columnLabel = column[1];
          return "<li class='column' " + columnNameAttribute + "='" + columnName + "'>" +
            "<input type='checkbox' id='" + id + "' name='" + id + "' " + (columnState.hidden ? "" : "checked") + ">" +
            "<label for='" + id + "'>" + columnLabel + "</label>" +
            "</li>\n";
        }).join("") +
        "</ul>";
    };

    let generateTooltip = (origin, continueTooltip) => {
      origin.tooltipster("content", getTooltipContent(getColumnFilterList)).tooltipster("reposition");
      continueTooltip();
    };

    const initColumnFilterSearch = ($tooltip) => {
      let paginationState = getPaginationState(sectionId);
      $tooltip.find("#filter-input").keyup(function(event) {
        if (event.originalEvent.code === "Enter") {
          saveAndCloseFilter($tooltip, paginationState);
        } else {
          const filterText = jq$(this).val().toLowerCase();
          latestFilterTextQuery = filterText;
          $tooltip.find("li.column").each((i, li) => {
            let $li = jq$(li);
            let columnName = getColumnName($li);
            if (columnName.toLowerCase().contains(filterText)) {
              $li.removeClass("hidden");
            } else {
              $li.addClass("hidden");
            }
          });
        }
      });
    };

    const initTooltip = (origin, tooltip) => {
      $tooltip = jq$(tooltip); // init tooltip variable in closure
      initColumnFilterSearch($tooltip);
      initFilterCheckBoxes($tooltip);
      initButtons($tooltip);
      $tooltip.on("click", function(event) {
        event.stopPropagation(); // don't allow click to bubble up to document, closing tooltip again
      });
      $tooltip.on("keyup", function(event) {
        if (event.originalEvent.code === "Enter") {
          $tooltip.find(".ok-button").click();
        } else if (event.originalEvent.code === "Escape") {
          $tooltip.find(".cancel-button").click();
        }
      });
    };

    initTooltipster($columnFilterButton, generateTooltip, initTooltip, sectionId);
    $columnFilterButton.click(function(event) {
      // prevent closing it again immediately
      event.stopPropagation();
      // close tooltip when clicking outside of it
      jq$(document).off(paginationClickEvent); // just to be sure
      jq$(document).on(paginationClickEvent, function() {
        cancelFilter(sectionId);
      });
      // hide other open filter tooltips
      closeFilter(sectionId, true);
      // open filter tooltip
      let $button = jq$(event.target).closest(".pagination-column-filter");
      $button.tooltipster("show");
      setBackupPaginationState(sectionId);
      $button.addClass("showing-tooltip");
    });

    // init events for filter check boxes
    const initFilterCheckBoxes = function($tooltip) {
      $tooltip.find("li input").change(function() {
        let paginationState = getPaginationState(sectionId);
        const $checkBox = jq$(this);
        let columnName = getColumnName($checkBox.parents("li"));
        let columnFilterState = getColumnState(paginationState, columnName);
        columnFilterState.hidden = !this.checked;
        setPaginationState(sectionId, paginationState);
        updateButtonState($tooltip);
      });
    };

    // init buttons
    const initButtons = $tooltip => {
      updateButtonState($tooltip);
      $tooltip.find(".toggle-box").click(function() {
        let paginationState = getPaginationState(sectionId);
        let filterState = getFilterState(paginationState);
        let checkBoxes = $tooltip.find(".pagination-filter-list input");
        let checked = this.checked;
        checkBoxes.prop("checked", checked);
        checkBoxes.each((i, checkbox) => {
          let columnName = getColumnName(jq$(checkbox).parents("li"));
          let columnFilterState = getColumnState(paginationState, columnName);
          columnFilterState.hidden = !checked;
        });
        setPaginationState(sectionId, paginationState);
        updateButtonState($tooltip);
      });
      $tooltip.find(".ok-button").click(function() {
        saveAndCloseFilter($tooltip, getPaginationState(sectionId));
      });
      $tooltip.find(".cancel-button").click(function() {
        cancelFilter(sectionId);
      });
    };

    const cancelFilter = sectionId => {
      jq$(document).off(paginationClickEvent); // in case we close via buttons
      closeFilter(sectionId, true);
    };

    // update the state of buttons
    const updateButtonState = $tooltip => {
      const $toggleBox = $tooltip.find(".toggle-box");
      let $checkBoxes = $toggleBox.parents(".filter-parent").find(".pagination-filter-list input");
      let $checked = $checkBoxes.filter(":checked");
      let $notChecked = $checkBoxes.filter(":not(:checked)");
      if ($checked.length > 0 && $notChecked.length > 0) {
        $toggleBox.prop("indeterminate", true);
      } else {
        $toggleBox.prop("indeterminate", false);
        $toggleBox.prop("checked", $checked.length > 0);
      }
    };

    const saveAndCloseFilter = ($tooltip, paginationState) => {
      jq$(document).off(paginationClickEvent); // in case we close via buttons
      $columnFilterButton.tooltipster("hide");

      if ($tooltip && latestFilterTextQuery.length > 0) {
        $tooltip.find(".pagination-filter-list input").each((i, checkbox) => {
          let $checkbox = jq$(checkbox);
          let $li = $checkbox.parents("li");
          let columnName = getColumnName($li);
          let columnState = getColumnState(paginationState, columnName);
          let checked = $checkbox.prop("checked");
          let visible = !$li.is(".hidden");
          columnState.hidden = !(checked && visible);
        });
        setPaginationState(sectionId, paginationState);
      }
      updateNode(sectionId);
      notifyFilterChanged(sectionId);
    };


  }

  // generate html for tooltip content
  function getTooltipContent(getFilterList) {
    return jq$("<div class='filter-parent'><label for='filter-input'>Filter:</label><input id='filter-input'>" +
      "<div class='filter-input-indicator-parent'><i class='filter-input-indicator fa-solid fa-spinner fa-spin-pulse'></i></div>\n" +
      "<div class='toggle-box-parent'><input class='toggle-box' id='toggle-checkbox' type='checkbox'><label for='toggle-checkbox'>All</label></div>" +
      getFilterList() +
      "<div class='close-buttons'>" +
      "<button class='ok-button'>Ok</button>" +
      "<button class='cancel-button'>Cancel</button>" +
      "</div></div>");
  };

  return {

    sort: function(element, id) {
      const paginationState = getPaginationState(id);
      const sortingName = getColumnName(jq$(element).parent());
      let sorting;
      if (typeof paginationState.sorting == "undefined") {
        sorting = [{sort: sortingName, naturalOrder: true}];
      } else {
        sorting = paginationState.sorting;
        let found = false;
        let remove = false;
        let i = 0;
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
      paginationState.sorting = sorting;
      setPaginationStateAndUpdateNode(paginationState, id);
    },

    setCount: function(selected, id) {
      const $selected = jq$(selected);

      const paginationState = getPaginationState(id);

      const lastCount = parseInt(paginationState.count);
      const resultSize = parseInt(jq$("#" + id + " .resultSize").val());
      const count = $selected.val();
      $selected.data("current", count);
      let startRow = parseInt(jq$("#" + id + " .startRow").val());
      const search = /^\d+$/;
      const found = search.test(startRow);
      if (!(found)) {
        jq$("#" + id + " .startRow").val("");
        return;
      }

      if (count === "Max") {
        paginationState.startRow = 1;
        paginationState.count = "Max";
      } else {
        if (startRow + lastCount === resultSize + 1) {
          startRow = resultSize - parseInt(count) + 1;
        }
        if (startRow <= 0) {
          startRow = 1;
        }
        paginationState.startRow = startRow;
        paginationState.count = count;
      }

      setPaginationStateAndUpdateNode(paginationState, id);
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


      const paginationState = getPaginationState(id);
      paginationState.startRow = startRow;
      paginationState.count = count;
      setPaginationStateAndUpdateNode(paginationState, id);

    },

    updateStartRow: function(selectedRow, sectionId, preventRerender) {

      const id = sectionId;
      const paginationState = getPaginationState(id);
      const count = jq$("#" + id + " .count").val();
      let startRow = selectedRow.value;
      const search = /^\d+$/;
      const found = search.test(startRow);
      if (!(found)) {
        jq$("#" + id + " .startRow").val("");
        return;
      }
      if (startRow <= 0) {
        startRow = 1;
      }
      if (count === "Max") {
        paginationState.startRow = 1;
        paginationState.count = "Max";
      } else {
        paginationState.startRow = startRow;
        paginationState.count = count;
      }
      setPaginationState(id, paginationState);
      if (!preventRerender) updateNode(id);
    },

    decorateTable: function($paginationWrapper) {
      if ($paginationWrapper.is(".knowwe-paginationWrapper")) {
        // is() requires only one of the elements to match, so we also filter...
        $paginationWrapper = $paginationWrapper.filter(".knowwe-paginationWrapper");
      } else {
        // select correct elements
        $paginationWrapper = $paginationWrapper.find(".knowwe-paginationWrapper");
      }
      decoratePagination($paginationWrapper);
    },

    initialDecorateTables: function() {
      jq$(".knowwe-paginationWrapper").each(function() {
        decoratePagination(jq$(this), true);
      });

    }
  };
}();


/* ############################################################### */
/* ------------- Onload Events ---------------------------------- */
/* ############################################################### */
(function init() {
  window.addEvent("domready", _KL.setup);
  if (KNOWWE.helper.loadCheck(["Wiki.jsp"])) {
    window.addEvent("domready", function() {
      KNOWWE.core.plugin.pagination.initialDecorateTables();
    });
  }
}());

KNOWWE.helper.observer.subscribe("afterRerender", function() {
  KNOWWE.core.plugin.pagination.decorateTable(jq$(this));
});
