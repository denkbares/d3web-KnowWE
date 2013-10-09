/*global Timeline:false, jq$: false, KNOWWE:false, Json:false, _KA:false, SimileAjax: false
 _KL:false, window:false, TestCasePlayer: false */

var TimelinePlugin = {};

TimelinePlugin.Timeline = function(element) {
	var that = {};
	var _eventSource;
	var _testCase;
	var _timeline;
	var _theme;
	var _data;
	var _noCaption = [];
	
	var id = jq$(element).closest(".defaultMarkupFrame").get(0).id;
	
	(function() {
		_createEventSource();
	
		_theme = _createTheme();
	
		var bandInfos = _createBands();
		_timeline = Timeline.create(element, bandInfos, Timeline.HORIZONTAL);
		_timeline.getBand(0).setCenterVisibleDate(new Date(0));
		_initNavigation();
		var testCaseSel = jq$(element).parent().children('.testCaseSelector');
		testCaseSel.change(function() {
			_changeTestCase(this.options[this.selectedIndex].value);
		});
		testCaseSel.change();
	}());

	
	function _createEventSource() {
		_eventSource = new Timeline.DefaultEventSource();
	}
	
	function _load(data) {
		_data = data;
		_timeline.getBand(0)._eventTracksNeeded = data.queries.length + 1;
		_initCaption(data.queries);

		_eventSource.clear();
		_eventSource.loadJSON(data, ".", that);
		
		for(var i = 0; i < _timeline.getBandCount(); ++i) {
			_timeline.getBand(i).removeAllDecorators();
			_timeline.getBand(i).addDecorator(new Timeline.SpanHighlightDecorator({
				startDate : _eventSource.getEarliestDate(),
				endDate : _eventSource.getLatestDate(),
				color : "#C0DD9F", // set color explicitly
				opacity : 30,
				startLabel : "Start",
				endLabel : "End",
				cssClass : "region"
			}));
		}
		
		jq$(element).parent().children('.errors').html(data.errors);
	}
	
	function _createTheme() {
		var theme = Timeline.ClassicTheme.create();
		theme.event.track.height = 16;
		theme.event.tape.height = 10;
		theme.autoWidth = true;
		theme.mousewheel = 'zoom';
		return theme;
	}
	
	function _createBands() {
		var bands = [ {
			layout : 'original',
			width : '60%'
		}, {
			layout : 'overview',
			width : '20%'
		}, {
			layout : 'overview',
			width : '20%'
		} ];
		var bandInfos = [];
		for ( var i = 0; i < bands.length; i++) {
			var band = bands[i];
			var zoomIndex = i * 2 + 5;
			band.zoomIndex = zoomIndex;
			band.zoomSteps = [];
			for ( var j = zoomIndex - 1; j < zoomIndex + 20; ++j) {
				band.zoomSteps.push(TimelinePlugin.getZoomLevel(j));
			}
			var zoomStep = band.zoomSteps[zoomIndex];
			band.intervalUnit = zoomStep.unit;
			band.intervalPixels = zoomStep.pixelsPerInterval;

			band.theme = _theme;
			if (i <= 1) {
				band.eventSource = _eventSource;
			}
			var bandInfo = Timeline.createBandInfo(band);
			if (i > 0) {
				bandInfo.syncWith = 0;
				bandInfo.highlight = true;
			}
			
			bandInfos.push(bandInfo);
		}
		return bandInfos;
	}
	
	function _runCaseTo(date) {
		var x = _testCase.split("/", 3);
		TestCasePlayer.send(_data.sourceId, date, x[1] + "/" + x[2], x[0]);
	}
	
	function _changeTestCase(testCase) {
		_testCase = testCase;
		_fetchData();
	}	

	function _fetchData() {
		var params, options;
		params = {
			action : "TimelineJSONExportAction",
			KWikiWeb : "default_web",
			kdomid : id,
			testCase : _testCase
		};

		// options for AJAX request
		options = {
			async : true,
			url : KNOWWE.core.util.getURL(params),
			response : {
				action : "none",
				fn : function () {	
					if (_timeline === undefined) {
						_create();
					}
					_load(Json.evaluate(this.responseText));
					_timeline.layout();
				}
			}
		};

		new _KA(options).send();
	}
	
	function _jumpToFirstDate() {
		_timeline.getBand(0).setCenterVisibleDate(_eventSource.getEarliestDate());
	}
	
	function _jumpToPrevDate() {
		var currentCenter = _timeline.getBand(0).getCenterVisibleDate();
		var unit = _timeline.getUnit();
		var iterator = _eventSource.getAllEventIterator();
		var matcher = _timeline.getBand(0).getEventPainter().getFilterMatcher();
		var date = null;
		while (iterator.hasNext()) {
			var evt = iterator.next();
			var evtDate = evt.getStart();
			if((matcher === null || matcher(evt)) &&
					unit.compare(evtDate, currentCenter) < 0) {
				if(date === null || unit.compare(evtDate, date) > 0) {
					date = evtDate;
				}
			}
		}
		if(date !== null) {
			_timeline.getBand(0).setCenterVisibleDate(date);
		}
	}
	
	function _jumpToNextDate() {
		var currentCenter = _timeline.getBand(0).getCenterVisibleDate();
		var unit = _timeline.getUnit();
		var iterator = _eventSource.getAllEventIterator();
		var date = null;
		var matcher = _timeline.getBand(0).getEventPainter().getFilterMatcher();
		while (iterator.hasNext()) {
			var evt = iterator.next();
			var evtDate = evt.getStart();
			if((matcher === null || matcher(evt)) && 
					unit.compare(evtDate, currentCenter) > 0) {
				if(date === null || unit.compare(evtDate, date) < 0) {
					date = evtDate;
				}
			}
		}
		if(date !== null) {
			_timeline.getBand(0).setCenterVisibleDate(date);
		}
	}
	
	function _jumpToLastDate() {
		_timeline.getBand(0).setCenterVisibleDate(_eventSource.getLatestDate());
	}
	
	function _initNavigation() {
		var buttons = {
			'begin' : {
				alt : "Begin",
				title : "Jump to first Event",
				action : _jumpToFirstDate
			},
			'prev' : {
				alt : "Previous",
				title : "Jump to previous Event",
				action : _jumpToPrevDate
			},
			'next' : {
				alt : "Next",
				title : "Jump to next Event",
				action : _jumpToNextDate
			},
			'end' : {
				alt : "End",
				title : "Jump to last Event",
				action : _jumpToLastDate
			}
		};

		jq$.each(buttons, function(k, v) {
			var img = jq$('<img src="KnowWEExtension/images/timeline/' + k +
					'.png" alt="' + v.alt + '" title="' + v.title + '" />');
			img.click(v.action);
			jq$(element).parent().children(".toolBar").append(img);
		});
	}
	
	function _getText(query) {
		return query.text + " [# = " + query.events.length + "]";
	}
	
	function _initFilter() {
		var filterMatcher = function(evt) {
			return _noCaption.indexOf(evt.getTrackNum()) === -1;
		};
		for (var i = 0; i < _timeline.getBandCount(); i++) {
			_timeline.getBand(i).getEventPainter().setFilterMatcher(filterMatcher);
		}
	}
	
	function _createOnOffButton(track, active) {
		var img = jq$("<img src=\"\" alt=\"Hide\" title=\"Hide/Show Events\"/>");
		var update = function() {
			if(active) {
				img.attr('src','KnowWEExtension/images/timeline/kreuz.png');
				_noCaption.push(track);
			}
			else {
				img.attr('src','KnowWEExtension/images/timeline/haken.png');
				_noCaption.splice(_noCaption.indexOf(track),1);
			}
		};
		
		img.click(function(){
				active = !active;
				update();
				_timeline.paint();
			}
		);
		update();
		return img;
	}
	
	function _initCaption(queries) {
		jq$(_timeline._containerDiv).children('.query-caption-holder').remove();
		var _div = jq$('<div class="query-caption-holder"></div>');
		_initFilter();
		
		var createOnOff = function(track, text, active) {
			var childParagraph = jq$("<p></p>");
			childParagraph.append(_createOnOffButton(track, active));
			childParagraph.append(text);
			var child = jq$("<div></div>");
			child.append(childParagraph);
			childParagraph.hover(function() {
				child.css('width', '100%');
				_div.css('width', '100%');
			}, function() {
				child.css('width', '');
				_div.css('width', '');
			});
			_div.append(child);
		};
		
		jq$.each(queries,
				function(k, v) {
					createOnOff(k, _getText(v), false);
				}
		);
		
		createOnOff(queries.length, "<img src='KnowWEExtension/images/timeline/icons/stoppuhr.png' />", true);
		jq$(_timeline._containerDiv).append(_div);
	}
	
	function _usesQuestion(trackId, question) {
		question = parseInt(question);
		if(trackId == _data.queries.length) {return true;}
		return _data.queries[trackId].questions.indexOf(question) !== -1;
	}
	
	that.fetchData = function(testCase) {
		_changeTestCase(testCase);
	};
	
	that.runCaseTo = _runCaseTo;
	
	that.getDate = function(i) {
		return Timeline.NativeDateUnit.fromNumber(parseInt(_data.dates[i].time));
	};
	
	that.getDataAtId =  function (i, trackId) {
		var data = [];
		jq$.each(_data.dates[i].values,
				function(question, value) {
					if(trackId === undefined || _usesQuestion(trackId, question)) {
						data.push({
							question: _data.questions[question],
							value: value
						});
					}
				}
		);
		
		return data;
	};
	
	that.createRunCaseToButton = function(date) {
		var button = jq$('<img style="cursor: pointer" src=\"KnowWEExtension/testcaseplayer/icon/runto.png\" alt=\"Run until here\"  title=\"Run testcase until here\">');
		button.click(
				function() {
					that.runCaseTo(date);
				}
		);
		return button;
	};

	return that;
};

TimelinePlugin.getZoomLevel = function (level) {
	var unit = SimileAjax.DateTime.MILLENNIUM;
	var pixel = 200 / Math.pow(2,level);
	while(unit > SimileAjax.DateTime.MILLISECOND) {
		var newPixel = pixel * SimileAjax.DateTime.gregorianUnitLengths[unit];
		if(newPixel < 200 && unit < SimileAjax.DateTime.MILLENNIUM) {
			pixel = newPixel;
			break;
		}
		--unit;
	}
	return {unit: unit, pixelsPerInterval: pixel};
};

TimelinePlugin.createInfoBubbleToolbar = function(timeline, evt, elmt) {
	var toolbar = jq$('<div class="toolBar" style="border-top: 1px solid rgb(170, 170, 170); margin-top: 5px; padding-top: 5px;"></div>');

	var startIndex = evt.getStartId();
	var endIndex = evt.getEndId();
	var index = startIndex;
	var buttons = {
		'begin' : {
			action : function() { index = startIndex; },
			enabled : function() { return index !== startIndex; }
		},
		'back' : {
			action : function() { index -= 1; },
			enabled : function() { return index !== startIndex; }
		},
		'forward' : {
			action : function() { index += 1; },
			enabled : function() { return index !== endIndex; }
		},
		'end' : {
			action : function() { index = endIndex; },
			enabled : function() { return index !== endIndex; }
		}
	};

	var update = function(action) {
		action();
		var data = timeline.getDataAtId(index, evt.getTrackNum());
		while(data.length === 0) {
			action();
			data = timeline.getDataAtId(index, evt.getTrackNum());
		}
		if (index < startIndex)
			index = startIndex;
		if (index >= endIndex)
			index = endIndex;

		jq$.each(buttons, function(k, v) {
			var isEnabled = v.enabled();
			var elm = v.element;
			if (!isEnabled && !elm.hasClass('disabled')) {
				elm.removeClass('enabled');
				elm.addClass('disabled');
				elm.children('img').attr('src', "KnowWEExtension/testcaseplayer/icon/" + k + "_deactivated.png");
			}
			else if (isEnabled && !elm.hasClass('enabled')) {
				elm.removeClass('disabled');
				elm.addClass('enabled');
				elm.children('img').attr('src', "KnowWEExtension/testcaseplayer/icon/" + k + ".png");
			}
		});

		data = timeline.getDataAtId(index, evt.getTrackNum());
		var date = timeline.getDate(index);
		jq$(elmt).children(".eventDataTable").remove();
		jq$(elmt).append(TimelinePlugin.printEvtData(data));
		jq$(toolbar).children(".currentTimeInTooltip").text(
				TimelinePlugin.createTimeAsTimeStamp(Timeline.NativeDateUnit.toNumber(date)));
	};
	

	jq$.each(buttons, function(k, v) {
		var element = jq$('<span class="toolButton"><img src="" /></span>');
		element.click(function() {  update(v.action); });
		v.element = element;
		jq$(toolbar).append(element);
	});

	jq$(toolbar).append("<div class=\"toolSeparator\"></div>");
	
	var date = timeline.getDate(index);
	jq$(toolbar).append(timeline.createRunCaseToButton(Timeline.NativeDateUnit.toNumber(date)));
	
	
	jq$(toolbar).append("<span class=\"currentTimeInTooltip\" style=\"font-weight:bold\">"+
			TimelinePlugin.createTimeAsTimeStamp(date)+"</span>");

	jq$(elmt).append(toolbar);

	update(function() {});
};

TimelinePlugin.printEvtData = function (data) {
	var content = "<table class=\"wikitable eventDataTable\"><thead><tr><th>Question</th><th>Value</th></tr></thead>";
	content += "<tbody>";
	jq$.each(data,
			function(i, v) {
					content += "<tr><td>" + v.question + "</td><td>" + v.value + "</td></tr>";
			}
	);
	content += "</tbody></table>";
	return content;
};

TimelinePlugin.createTimeAsTimeStamp = function (time) {
	if (time == 0) return "0s";

	var units =   ["ms",  "s",     "min",            "h",                 "d"];
	var factors = [   1, 1000, 60 * 1000, 60 * 60 * 1000, 24 * 60 * 60 * 1000];
	var t = "";
	for (var i = factors.length - 1; i >= 0; --i) {
		var factor = factors[i];
		var amount = Math.floor(time / factor);
		if (amount >= 1) {
			if (t !== "") t += " ";
			t += amount + units[i];
			time -= amount * factor;
		}
	}
	return t;
};

(function init() {
	window.addEvent("domready", _KL.setup);

	if (KNOWWE.helper.loadCheck([ "Wiki.jsp" ])) {
		window.addEvent("domready", function() {
			jq$(".timelineHolder").each(function () {
				var tl = new TimelinePlugin.Timeline(this);
			});
		}
		);
	}
}());