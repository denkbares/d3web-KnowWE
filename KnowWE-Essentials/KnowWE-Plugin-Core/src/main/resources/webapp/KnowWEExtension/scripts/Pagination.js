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

  let windowHeight;

  function saveCookieAndUpdateNode(cookie, id) {
    saveCookie(cookie, id);
    updateNode(id);
  }

  function saveCookie(cookie, id) {
    const cookieStr = JSON.stringify(cookie);
    jq$.cookie("PaginationDecoratingRenderer-" + id, cookieStr);
  }

  function updateNode(id) {
    jq$('#' + id).rerender();
  }

  function scrollToTopNavigation(id) {
    jq$('html, body').animate({
      scrollTop: jq$("#" + id).offset().top
    }, 0);
  }

  function getSortingSymbol(naturalOrder, index) {
    let cssClass;
    if (naturalOrder) {
      cssClass = "fa fa-caret-down fa-lg";
    } else {
      cssClass = "fa fa-caret-up fa-lg";
    }
    if (index !== 0) cssClass += " secondary";
    return jq$('<i/>', {
      "class": cssClass
    });
  }

  function renderSortingSymbols(sectionId, sortingMode) {
    const cookie = jq$.cookie("PaginationDecoratingRenderer-" + sectionId);
    if (!cookie) return;
    const parsedCookie = jq$.parseJSON(cookie);
    if (!parsedCookie || !parsedCookie.sorting) return;
    let sortLength;
    if (sortingMode === 'multi') {
      sortLength = (parsedCookie.sorting).length;
    } else {
      sortLength = 1;
    }
    for (let i = 0; i < sortLength; i++) {
      const sortingSymbolParent = jq$("[pagination=" + sectionId + "] th:contains('" + parsedCookie.sorting[i].sort + "') span");
      const sortingSymbol = jq$(sortingSymbolParent).find("i");
      if (sortingSymbol.exists()) {
        sortingSymbol.replaceWith(getSortingSymbol(parsedCookie.sorting[i].naturalOrder, i));
      } else {
        jq$(sortingSymbolParent).append(getSortingSymbol(parsedCookie.sorting[i].naturalOrder, i));
      }
    }
  }

  function enableSorting(sectionId) {
    jq$(this.firstChild).wrap('<span></span>');
    if (!jq$(this).hasClass("notSortable")) {
      jq$(this).addClass("sortable");
      jq$(this).find("span").bind('click',
        function() {
          KNOWWE.core.plugin.pagination.sort(this, sectionId);
        }
      );
    }
  }

  function prepareFilterableElements() {
    const filterIcon = jq$('<i/>', {
      "class": 'fa fa-filter knowwe-filter'
    });
    jq$(this).prepend(filterIcon);
    const text = jq$(this).text();
    const preparedFilter = jq$(".paginationFilters div[filtername=" + text + "]").detach();
    jq$(filterIcon).tooltipster({
      content: jq$(preparedFilter),
      interactive: true,
      interactiveTolerance: 500,
      theme: "tooltipster-knowwe"
    });
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

      jq$(this).find("th").each(
        function(i) {
          // make <th> clickable and therefore sortable except if
          // it's stated explicitly otherwise
          if (typeof sortingMode != 'undefined') {
            enableSorting.call(this, sectionId);
          }
          if (jq$(this).hasClass("filterable")) {
            prepareFilterableElements.call(this);
          }
        }
      );

      // render sorting symbol
      renderSortingSymbols(sectionId, sortingMode);

      handlePaginationBelowTableVisibility.call(this);
    }
  }

  function handleNoResult(pagination) {
    if (!pagination.find('table').exists()) {
      pagination.find(".knowwe-paginationToolbar").remove();
    }
  }

  function readCookie(id) {
    let cookieValue = jq$.cookie("PaginationDecoratingRenderer-" + id);
    if (!cookieValue) cookieValue = "{}";
    return cookie = jq$.parseJSON(cookieValue);
  }

  return {

    sort: function(element, id) {
      const cookie = readCookie(id);
      const columnName = jq$(element).parent().attr("sortname") || jq$(element).text();
      let sorting;
      if (typeof cookie.sorting == "undefined") {
        sorting = [{sort: columnName, naturalOrder: true}];
      } else {
        sorting = cookie.sorting;
        let found = false;
        let remove = false;
        let i = 0
        for (; i < sorting.length; i++) {
          if (sorting[i].sort === columnName) {
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
          sorting.unshift({sort: columnName, naturalOrder: true});
        }
      }
      cookie.sorting = sorting;
      saveCookieAndUpdateNode(cookie, id);
    },

    setCount: function(selected, id) {
      const $selected = jq$(selected);

      const cookie = readCookie(id);

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

      saveCookieAndUpdateNode(cookie, id);
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


      const cookie = readCookie(id);
      cookie.startRow = startRow;
      cookie.count = count;
      saveCookieAndUpdateNode(cookie, id);

    },

    updateStartRow: function(selectedRow, sectionId, preventRerender) {

      const id = sectionId;
      const cookie = readCookie(id);
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
      saveCookie(cookie, id);
      if (!preventRerender) updateNode(id);
    },

    filter: function(checkbox, sectionId) {
      const key = jq$(checkbox).attr("filterkey");
      const value = jq$(checkbox).attr("filtervalue");
      const checked = checkbox.checked;

      const cookie = readCookie(id);
      if (typeof cookie.filters == "undefined") {
        cookie.filters = {};
        cookie.filters[key] = [];
        if (checked === true) {
          cookie.filters[key].push(value);
        } else {
          cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
        }
      } else if (typeof cookie.filters[key] == "undefined") {
        cookie.filters[key] = [];
        if (checked === true) {
          cookie.filters[key].push(value);
        } else {
          cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
        }
      } else {
        if (checked === true) {
          cookie.filters[key].push(value);
        } else {
          cookie.filters[key].splice(cookie.filters[key].indexOf(value), 1)
        }
      }
      saveCookieAndUpdateNode(cookie, sectionId);
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
