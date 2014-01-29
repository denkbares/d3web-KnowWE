/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.core == "undefined" || !KNOWWE.core) {
	KNOWWE.core = {};
}

/**
 * The KNOWWE.core global namespace object. If KNOWWE.core is already defined,
 * the existing KNOWWE.core object will not be overwritten so that defined
 * namespaces are preserved.
 */
if (typeof KNOWWE.core.plugin == "undefined" || !KNOWWE.core.plugin) {
	KNOWWE.core.plugin = {};
}

/**
 * Namespace: KNOWWE.core.plugin.objectinfo The KNOWWE object info namespace.
 */
KNOWWE.core.plugin.objectinfo = function() {
	return {

		init : function() {
			// init renaming form button
			jq$('#objectinfo-replace-button').click(
					KNOWWE.core.plugin.objectinfo.renameFunction);

			// we have to suspend the enter event to prevent multiple
			// confirm dialogs after when confirming the dialogs with enter...
			var suspend = false;
			jq$('#objectinfo-replacement').keyup(function(event) {
				if (event.keyCode == 13 && !suspend) {
					suspend = true;
					if (confirm("Are you sure you want to rename this term?")) {
						KNOWWE.core.plugin.objectinfo.renameFunction();
					} else {
						suspend = false;
					}
				}
			});

			KNOWWE.core.plugin.objectinfo.lookUp();
		},
		
		/**
		 * Load the ajax-previews
		 */
		loadPreviews : function(web, title, root) {
			var select = (root == undefined) 
					? jq$('.asynchronPreviewRenderer') 
					: jq$(root).find('.asynchronPreviewRenderer');
			var json = [];
			var ids = [];
			select.each(function() {
				json.push(this.getAttribute('rel'));
				ids.push(this.id);
			});
			jq$.ajax("action/RenderPreviewAction", {
				data: {
	                KWikiWeb: web,
	                KWiki_Topic: title,
	                data: JSON.stringify(json)
				},
				success: function(html) {
					KNOWWE.core.util.replaceElement(ids, html);
				    ToolMenu.decorateToolMenus();
				    KNOWWE.helper.observer.notify("previewsLoaded");
				}
			});
		},

		/**
		 * Function: createHomePage Used in the ObjectInfoToolProvider for
		 * creating homepages for KnowWEObjects
		 */
		createHomePage : function() {
			objectName = _KS('#objectinfo-src');
			if (objectName) {
				var params = {
					action : 'CreateObjectHomePageAction',
					objectname : objectName.innerHTML
				}

				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'none',
						fn : function() {
							window.location = "Wiki.jsp?page="
									+ objectName.innerHTML
						}
					}
				}
				new _KA(options).send();
			}

		},

		renameFunction : function() {
			KNOWWE.core.plugin.objectinfo.renameTerm(false);
		},

		/**
		 * Renames all occurrences of a specific term.
		 */
		renameTerm : function(forceRename) {
			if (forceRename == null)
				forceRename = false;
			// TODO shouldn't these 3 be vars?
			objectname = jq$('#objectinfo-target');
			replacement = jq$('#objectinfo-replacement');
			web = jq$('#objectinfo-web');
			if (objectname && replacement && web) {
				var changeNote = 'Renaming: "' + objectname.val() + '" -> "'
						+ replacement.val() + '"';
				var params = {
					action : jq$(replacement).attr('action'),
					termname : objectname.val(),
					termreplacement : replacement.val(),
					KWikiWeb : web.val(),
					KWikiChangeNote : changeNote,
					force : forceRename ? "true" : "false",
				}
				var options = {
					url : KNOWWE.core.util.getURL(params),
					response : {
						action : 'none',
						fn : function() {
							var jsonResponse = JSON.parse(this.responseText);
							var alreadyexists = jsonResponse.alreadyexists;
							var same = jsonResponse.same;
							if (same == 'true') {
								alert('The term has not changed.');
							} else {
								if (alreadyexists == 'true') {
									if (confirm('A term with this name already exists, are you sure you want to merge both terms?')) {
										KNOWWE.core.plugin.objectinfo
												.renameTerm(true);
									}
								} else {
									window.location.href = "Wiki.jsp?page=ObjectInfoPage&objectname="
											+ encodeURIComponent(jsonResponse.newObjectName)
											+ "&termIdentifier="
											+ encodeURIComponent(jsonResponse.newTermIdentifier)
											+ "&renamedArticles="
											+ encodeURIComponent(jsonResponse.renamedArticles);
								}
							}
							KNOWWE.core.util.updateProcessingState(-1);
						},
						onError : function() {
							KNOWWE.core.util.updateProcessingState(-1);
						}
					}
				}
				KNOWWE.core.util.updateProcessingState(1);
				new _KA(options).send();
			}

		},

		/**
		 * shows a list of similar terms
		 */
		lookUp : function() {
			var terms = jq$('#objectinfo-terms')
			if (terms.length == 0)
				return;
			var response = terms.text();
			var jsonResponse = JSON.parse(response);
			var a = jsonResponse.allTerms;
			jq$('#objectinfo-search').autocomplete({
				source : a
			});
			jq$('#objectinfo-search').on(
					"autocompleteselect",
					function(event, ui) {
						jq$('#objectinfo-search').val(ui.item.value);
						var t = jq$('#objectinfo-search').parent().children(
								'[type="submit"]');
						jq$('#objectinfo-search').parent().children(
								'[type="submit"]').click();
					});
		}
	}
}();

KNOWWE.core.plugin.renderKDOM = function() {

	jq$('.table_text').hover(function() {
		var that = this;
		setTimeout(function() {
			jq$(that).css('height', that.scrollHeight);
		}, 0);
		// alert(this.scrollHeight);
	}, function() {
		jq$(this).css('height', '18px');
	}

	);
};

KNOWWE.tooltips = {};

KNOWWE.tooltips.enrich = function() {
	jq$('.tooltipster').tooltipster({
		position : "top-left",
		interactive : true,
		delay : 1300,
		theme: ".tooltipster-knowwe",
		functionBefore : function (origin, continueTooltip) {
			// chech if we have an ajax-tooltip
			// and only do once for each tooltip
			var src = origin.data('tooltip-src');
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
				success: function(html) {
					origin.tooltipster('update', html).tooltipster('reposition');
				},
				error: function(request, status, error) {
					KNOWWE.notification.error("Cannot get tooltip content", error, src);
					origin.tooltipster('hide');
				}
			});
		}
	});
};

KNOWWE.treetable = {};

KNOWWE.treetable.setOverflow = function() {
	jq$('.table_text').hover(function() {
		var elem = jq$(this);
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

/**
 * Namespace: KNOWWE.core.plugin.pagination The KNOWWE plugin d3web namespace.
 */
KNOWWE.core.plugin.pagination = function() {

	function saveCookieAndUpdateNode(cookie, id) {
		var cookieStr = JSON.stringify(cookie);
		jq$.cookie("PaginationDecoratingRenderer-" + id, cookieStr);
		KNOWWE.plugin.d3webbasic.actions.updateNode(jq$("#" + id).first().attr(
				"id"), KNOWWE.helper.gup('page'), null);
	}

	function scrollToTopNavigation(id) {
		jq$('html, body').animate({
			scrollTop : jq$("#" + id).offset().top
		}, 0);
	}

	function getSortingSymbol(naturalOrder) {
		var file;
		if (naturalOrder) {
			file = "arrow_down.png";
		} else {
			file = "arrow_up.png";
		}
		return jq$('<img/>', {
			"src" : 'KnowWEExtension/images/' + file
		});
	}

	return {

		sort : function(th, id) {

			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-"
					+ id));
			var sorting = th.innerText;
			if (cookie) {
				if (cookie.sorting == sorting) {
					cookie.naturalOrder = !cookie.naturalOrder;
				} else {
					cookie.sorting = sorting;
					cookie.naturalOrder = "true";
				}
			} else {
				cookie = {};
				cookie.sorting = sorting;
				cookie.naturalOrder = "true";
			}
			saveCookieAndUpdateNode(cookie, id);
		},

		setCount : function(selected) {

			var id = jq$(selected).closest(".navigationPaginationWrapper")
					.attr('id');

			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-"
					+ id));
			if (cookie == null) {
				cookie = {};
			}
			var scrollToTop = false;
			if ((cookie.count)
					&& parseInt(selected.value, 10) < parseInt(cookie.count, 10)) {
				scrollToTop = true;
			}

			var count = selected.value;
			var startRow = jq$("#" + id + " .startRow").val();
			var search = /^\d+$/;
			var found = search.test(startRow);
			if (!(found)) {
				jq$('.navigationPaginationWrapper #startRow').val('');
				return;
			}
			if (startRow <= 0) {
				startRow = 1;
			}
			if (count == "Max") {
				cookie.startRow = 1;
				cookie.count = "Max";
			} else {
				cookie.startRow = startRow;
				cookie.count = count;
			}

			if (scrollToTop) {
				scrollToTopNavigation(id);
			}
			saveCookieAndUpdateNode(cookie, id);
		},

		navigate : function(id, direction){

			var count = jq$("#" + id + " .count").val();
			var startRow = jq$("#" + id + " .startRow").val();

			switch (direction) {
	            case "begin":
	        		startRow = 1;
	                break;
	            case "back":
					if (count == "Max") {
						startRow = 1;
					} 
					else {
						if (parseInt(startRow) - parseInt(count) < 1) {
							startRow = 1;
						} else {
							startRow = parseInt(startRow) - parseInt(count);
						}
					}
					break;
				case "forward":
					if (count == "Max") {
						startRow = 1;
					} else {
						startRow = parseInt(startRow) + parseInt(count);
					}
					break;
			}

			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-"+ id));
			if (cookie == null) {
				cookie = {};
			}
			cookie.startRow = startRow;
			cookie.count = count;
			saveCookieAndUpdateNode(cookie, id);
	        scrollToTopNavigation(id);	         
		},

		updateStartRow : function(selectedRow) {

			var id = jq$(selectedRow).closest(".navigationPaginationWrapper")
					.attr('id');
			var cookie = jq$.parseJSON(jq$.cookie("PaginationDecoratingRenderer-"
					+ id));
			if (cookie == null) {
				cookie = {};
			}
			var count = jq$("#" + id + " .count").val();
			var startRow = selectedRow.value;
			var search = /^\d+$/;
			var found = search.test(startRow);
			if (!(found)) {
				jq$("#" + id + " .startRow").val('');
				return;
			}
			if (startRow <= 0) {
				startRow = 1;
			}
			if (count == "Max") {
				cookie.startRow = 1;
				cookie.count = "Max";
			} else {
				cookie.startRow = startRow;
				cookie.count = count;
			}
			saveCookieAndUpdateNode(cookie, id);
			scrollToTopNavigation(id);
		},

		decorateTable : function() {
			jq$(".navigationPaginationWrapper").each(
					function() {
						var sectionId = jq$(this).attr('id');

						// register count selector
						jq$(this).find(".count").on('change', function() {
							KNOWWE.core.plugin.pagination.setCount(this);
						});

						// register start row change event
						jq$(this).find('.startRow').on('change', function() {
							KNOWWE.core.plugin.pagination.updateStartRow(this);
						});

						// make <th> clickable and therefore sortable except if
						// it's stated explicitly otherwise
						var tablePagination = jq$(this).find("table");
						jq$(tablePagination).attr('sectionid', sectionId);
						jq$(tablePagination).find("th").each(
								function(i) {
									if (!jq$(this).hasClass("notSortable")) {
										jq$(this).addClass("paginationHeader");
										this.addEventListener('click',
												function(event) {
													KNOWWE.core.plugin.pagination
															.sort(this,
																	sectionId);
												}, true);
									}
								});

						// render sorting symbol
						var cookie = jq$.parseJSON(jq$
								.cookie("PaginationDecoratingRenderer-"
										+ sectionId));
						if (cookie != null && cookie.sorting != null) {
							var thToGetSortingSymbol = jq$("#" + sectionId
									+ " th:contains('" + cookie.sorting + "')");
							jq$(thToGetSortingSymbol).append(
									getSortingSymbol(cookie.naturalOrder));
						}

					});
		}
	}
}();

// add clickable table headers to every table which is a sibling to a navigation
// bar,
// i.e. initialized by PaginationDecoratingRenderer
KNOWWE.helper.observer.subscribe("navigationPaginationRendered", function() {
	KNOWWE.core.plugin.pagination.decorateTable()
});



/* ############################################################### */
/* ------------- Onload Events ---------------------------------- */
/* ############################################################### */
(function init() {

	window.addEvent('domready', _KL.setup);
	if (KNOWWE.helper.loadCheck([ 'Wiki.jsp' ])) {
		window.addEvent('domready', function() {
			KNOWWE.tooltips.enrich();
			KNOWWE.core.plugin.objectinfo.init();
			KNOWWE.core.plugin.renderKDOM();
			KNOWWE.treetable.setOverflow();
			KNOWWE.core.plugin.pagination.decorateTable();
		});
	}
	;
}());