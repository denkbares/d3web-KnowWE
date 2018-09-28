const TestCasePlayer = {};

TestCasePlayer.init = function () {
	TestCasePlayer.setLastSelected();
	jq$(".type_TestCasePlayer").find(".wikitable").find("th").unbind('click')
		.click(TestCasePlayer.registerClickableColumnHeaders);
	jq$(".type_TestCasePlayer").find(".wikitable").find(".collapsedcolumn")
		.each(function () {
			TestCasePlayer.addToolTip(jq$(this));
		});
};

TestCasePlayer.toggleFindings = function (id, action) {
	jq$('#' + id).find('.wikitable').find('th').filter('[type="finding"]')
		.each(function () {
			let collapse = action === "collapse";
			const th = jq$(this);
			if (collapse && !th.hasClass("collapsedcolumn")) {
				TestCasePlayer.collapseColumn(th);
			} else if (!collapse && th.hasClass("collapsedcolumn")) {
				TestCasePlayer.expandColumn(th);
			}
		});
};

TestCasePlayer.registerClickableColumnHeaders = function () {
	TestCasePlayer.toggleColumnStatus(jq$(this));
};

TestCasePlayer.toggleColumnStatus = function (th) {

	const isCollapsed = th.hasClass("collapsedcolumn");

	if (isCollapsed) {
		TestCasePlayer.expandColumn(th);
	} else {
		TestCasePlayer.collapseColumn(th);
	}

};

TestCasePlayer.downloadCase = function (sectionID) {
	const options = {
		url: KNOWWE.core.util.getURL({
			playerid: sectionID,
			action: 'DownloadCaseAction'
		}),
		response: {
			action: 'none',
			fn: function () {
				const parsed = JSON.parse(this.responseText);
				if (parsed.path) {
					window.location = 'action/DownloadFileAction?file='
						+ parsed.path + '&name=' + parsed.file;
				} else {
					KNOWWE.notification.error(null, parsed.error, "tcp-error");
				}
				KNOWWE.core.util.updateProcessingState(-1);
			},
			onError: _EC.onErrorBehavior
		}
	};
	KNOWWE.core.util.updateProcessingState(1);
	new _KA(options).send();
};

TestCasePlayer.getCollapseStatus = function (th) {
	let collapsed = "";
	th.siblings().each(function () {
		if (jq$(this).hasClass("collapsedcolumn")) {
			collapsed += jq$(this).attr("column") + "#";
		}
	});
	return collapsed;
};

TestCasePlayer.writeCollapseStatus = function (th, collapsed) {
	const id = th.parents(".TestCasePlayerContent").attr("id");
	let testCase = jq$("#selector" + id).find('[selected="selected"]').attr(
		"value");
	if (!testCase)
		testCase = jq$("#selector" + id).val();
	testCase = TestCasePlayer.encodeCookieValue(testCase);
	document.cookie = "columnstatus_" + id + "_" + testCase + "=" + collapsed;
};

TestCasePlayer.collapseColumn = function (th, animated) {
	if (th.find("input").length > 0)
		return;

	const column = th.attr("column");

	const tds = th.parents(".wikitable").first()
		.find('[column="' + column + '"]');

	let collapsed = TestCasePlayer.getCollapseStatus(th);
	collapsed += column;
	TestCasePlayer.writeCollapseStatus(th, collapsed);

	tds.addClass("collapsedcolumn");

	TestCasePlayer.addToolTip(th);
	tds.filter("td").each(function () {
		TestCasePlayer.addToolTip(jq$(this));
	});

};

TestCasePlayer.addToolTip = function (element) {
	if (element.is('th')) {
		TestCasePlayer.setToolTip(element, "Expand " + element.text().trim());
	} else if (element.is('td')) {
		const th = element.parents('table').find('th').filter('[column="' + element.attr('column') + '"]');
		const data = element.clone();
		data.find('script').remove();
		data.find('br').replaceWith('\n');
		title = th.text() + "\n" + data.text().trim().replace(
			/[ \t]*(\r?\n)[ \t]*/g, '$1');
		if (title) {
			TestCasePlayer.setToolTip(element, title);
		}
	}
};

TestCasePlayer.setToolTip = function (element, tooltip) {
	if (element.data('hasToolTip') === 'enabled') {
		element.tooltipster('update', tooltip);
	} else {
		element.attr('title', tooltip);
		element.tooltipster({
			delay: 100,
			theme: '.tcpTooltipser'
		});
		element.data('hasToolTip', 'enabled');
	}
};

TestCasePlayer.removeToolTip = function (element) {
	element.tooltipster('destroy');
	element.attr('title', null);
	element.data('hasToolTip', 'disabled');
};

TestCasePlayer.expandColumn = function (th) {
	if (th.find("input").length > 0)
		return;

	const column = th.attr("column");

	const tds = th.parents(".wikitable").first()
		.find('[column="' + column + '"]');

	tds.removeClass("collapsedcolumn");

	TestCasePlayer.setToolTip(th, "Collapse");
	tds.filter("td").each(function () {
		TestCasePlayer.removeToolTip(jq$(this));
	});

	const collapsed = TestCasePlayer.getCollapseStatus(th);

	TestCasePlayer.writeCollapseStatus(th, collapsed);

};

TestCasePlayer.send = function (providerId, casedate, name, topic, element) {

	const params = {
		action: 'ExecuteCasesAction',
		KWiki_Topic: topic,
		providerId: providerId,
		date: casedate,
		testCaseName: name,
		playerId: jq$(element).parents('.type_TestCasePlayer').attr('id')
	};

	const options = {
		url: KNOWWE.core.util.getURL(params),
		response: {
			action: 'none',
			fn: function () {
				try {
					TestCasePlayer.update();
				} catch (e) { /* ignore */
				}
				KNOWWE.core.util.updateProcessingState(-1);
			},
			onError: _EC.onErrorBehavior
		}
	};
	KNOWWE.core.util.updateProcessingState(1);
	new _KA(options).send();
};

TestCasePlayer.lastSelected = {};

TestCasePlayer.setLastSelected = function () {
	jq$('.type_TestCasePlayer').find(".ReRenderSectionMarker").each(function () {
		const id = jq$(this).children().first().attr('id');
		TestCasePlayer.lastSelected[id] = jq$('#selector' + id).val();
	});
};

TestCasePlayer.change = function (key_sessionid, selectedvalue, sectionID) {
	// reset pagination for other test case
	if (sectionID) {
		const $startRow = jq$("#" + sectionID + " .startRow");
		$startRow.val(1);
		KNOWWE.core.plugin.pagination.updateStartRow($startRow[0], sectionID, true);
	}
	document.cookie = key_sessionid + "="
		+ TestCasePlayer.encodeCookieValue(selectedvalue);
	TestCasePlayer.update();
};

TestCasePlayer.addCookie = function (cookievalue) {
	const topic = KNOWWE.helper.getPagename();
	document.cookie = "additionalQuestions"
		+ TestCasePlayer.encodeCookieValue(topic) + "="
		+ TestCasePlayer.encodeCookieValue(cookievalue);
	TestCasePlayer.update(true);
};

TestCasePlayer.encodeCookieValue = function (cookievalue) {
	let temp = escape(cookievalue);
	temp = temp.replace('@', '%40');
	temp = temp.replace('+', '%2B');
	return temp;
};

TestCasePlayer.update = function (adjustLeft) {
	const scrollInfos = {};
	jq$('.type_TestCasePlayer .ReRenderSectionMarker').each(function () {
		const id = jq$(this).children().first().attr('id');
		const selected = jq$('#selector' + id).val();
		const scrollInfo = {};
		if (selected !== TestCasePlayer.lastSelected[id]) {
			TestCasePlayer.lastSelected[id] = selected;
		} else {
			const tableDiv = jq$("#" + id).find('.wikitable').parent();
			scrollInfo.left = tableDiv.scrollLeft();
			scrollInfo.width = tableDiv[0].scrollWidth;
			scrollInfo.restoreScroll = true;
		}
		scrollInfos[id] = scrollInfo;
	});

	const fn = function () {
		for (let id in scrollInfos) {
			if (!scrollInfos.hasOwnProperty(id)) continue;
			const scrollInfo = scrollInfos[id];
			if (scrollInfo.restoreScroll) {
				const tableDiv = jq$("#" + id).find('.' + "wikitable").parent();
				const scrollWidthAfter = tableDiv[0].scrollWidth;
				if (adjustLeft && scrollInfo.width < scrollWidthAfter) {
					scrollInfo.left += scrollWidthAfter - scrollInfo.width;
				}
				tableDiv.scrollLeft(scrollInfo.left);
			}

		}
	};
	KNOWWE.helper.observer.notify('update', fn);
};

if (!KNOWWE.plugin.testCases)
	KNOWWE.plugin.testCases = {};
if (!KNOWWE.plugin.testCases.testCaseTable)
	KNOWWE.plugin.testCases.testCaseTable = {};

jq$(document)
	.ready(
		function () {
			// Prepare auto-complete for table editor
			let autoCompleteFun = function (callback, prefix, spreadsheet, row, col) {
				let ajaxFun, ajaxPrefix = prefix;
				const otherItems = [];
				// prepare object name
				let colName = spreadsheet.getCellTextTrimmed(0, col);
				// prepare ajax
				if (row === 0) {
					ajaxFun = AutoComplete.sendD3webValueObjectCompletionAction;
					if (prefix
						.match(/^\s*"?(t(i(m(e)?)?)?)?"?\s*$/i))
						otherItems
							.push({
								insertText: "Time",
								replaceLength: prefix.length,
								description: "Column for entering the reasoning time of the specific row."
							});
					if (prefix
						.match(/^\s*"?(c(h(e(c(k(s)?)?)?)?)?)?"?\s*$/i))
						otherItems
							.push({
								insertText: "Checks",
								replaceLength: prefix.length,
								description: "Column for entering some conditions. The condition must be true after executing the row. Otherwise the test case fails."
							});
				} else if (colName === 'Time') {
					ajaxFun = function (callback, prefix) {
						callback([]);
					};
				} else if (colName === 'Checks') {
					ajaxFun = AutoComplete.sendD3webConditionCompletionAction;
				} else {
					colName = AutoComplete
						.unquoteTermIdentifier(colName);
					if (AutoComplete
						.termRequiresQuotes(colName))
						colName = '"' + colName + '"';
					ajaxFun = AutoComplete.sendD3webActionCompletionAction;
					ajaxPrefix = colName + ' = ' + prefix;
				}
				ajaxFun(
					function (byAjax) {
						AutoComplete
							.unquoteTermIdentifiers(byAjax);
						callback(otherItems.concat(byAjax));
					}, ajaxPrefix);
			};

			KNOWWE.plugin.testCases.testCaseTable.editTool = KNOWWE.plugin.tableEditTool.create(autoCompleteFun);
			KNOWWE.plugin.testCases.testCaseTable.editTool.getWikiText = function (id) {
				let wikiText = _EC.getWikiText(id);
				if (!/%%TestCaseTable\s*\|/i.test(wikiText)) {
					wikiText = "%%TestCaseTable\n\n" +
						"|| Name || Time || Checks				|| My Question \n" +
						"| Start |    0s | My Question = Answer | Answer" +
						wikiText.substring(wikiText.indexOf("\n")).trim();
				}
				return wikiText;
			};

			// init test case player
			TestCasePlayer.init();
			KNOWWE.helper.observer.subscribe("afterRerender", TestCasePlayer.init);
		});
