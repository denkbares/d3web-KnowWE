/*
 * Show dates as relative values.
 */
(function() {
	Timeline.GregorianDateLabeller = function(locale, timeZone) {
	    this._locale = locale;
	    this._timeZone = timeZone;
	};
	
	Timeline.GregorianDateLabeller.prototype.labelInterval = function(date, intervalUnit) {
		var unitName = ['ms', 's', 'min', 'h','d', 'w', 'mo', 'y', '10 y','100 y','1k y'];
	
		date = SimileAjax.DateTime.removeTimeZoneOffset(date, this._timeZone);
		var m = (date.getTime() % SimileAjax.DateTime.gregorianUnitLengths[intervalUnit+1]) / SimileAjax.DateTime.gregorianUnitLengths[intervalUnit];
		var emphasized = false;
		if(m === 0) {
			m = (date.getTime() % SimileAjax.DateTime.gregorianUnitLengths[intervalUnit+2]) / SimileAjax.DateTime.gregorianUnitLengths[intervalUnit+1];
			intervalUnit++;
			emphasized = true;
		}
		var text = m + unitName[intervalUnit];
	
		return { text: text, emphasized: emphasized };
	};
})();

/*
 * No caption for bands. -> Ignore text height for tape height calculation
 */
(function() {
	var oldPaint = Timeline.OriginalEventPainter.prototype.paint;
	Timeline.OriginalEventPainter.prototype.paint = function() {
		var oldGetLineHeight = SimileAjax.Graphics._FontRenderingContext.prototype.getLineHeight;
		SimileAjax.Graphics._FontRenderingContext.prototype.getLineHeight = function() {
			return 0;
		};
		oldPaint.apply(this);
		SimileAjax.Graphics._FontRenderingContext.prototype.getLineHeight = oldGetLineHeight;
	};
})();


/*
 * Draw tapes vertically centered.
 */
(function() {
	var oldPaintEventTape = Timeline.OriginalEventPainter.prototype._paintEventTape;
	Timeline.OriginalEventPainter.prototype._paintEventTape = function(
			evt, iconTrack, startPixel, endPixel, color,
			opacity, metrics, theme, tape_index) {
		// use old function for basic calculations
		res = oldPaintEventTape.call(this, evt, iconTrack,
				startPixel, endPixel, color, opacity, metrics,
				theme, tape_index);
		// adjust top attribute
		res.top = metrics.trackOffset + iconTrack * metrics.trackIncrement +
			metrics.trackHeight / 2 - 1;
		res.elmt.style.top = res.top + "px";
		return res;
	};
})();


/*
 * We have a fixed amount of tracks, so don't update their count.
 */
(function() {
	Timeline._Band.prototype.updateEventTrackInfo = function(tracks, increment) {
		this._eventTrackIncrement = increment; // doesn't vary for a specific band
	};
})();

/*
 * Adjust ether painter for different zoom levels.
 */
(function() {
	function getMultipleCount(band) {
		if(band._ether._pixelsPerInterval >= 45) {
			return 1;
		}
			
		var target = 45 / band._ether._pixelsPerInterval;
		var real = SimileAjax.DateTime.gregorianUnitLengths[band._zoomSteps[band._zoomIndex].unit + 1] / SimileAjax.DateTime.gregorianUnitLengths[band._zoomSteps[band._zoomIndex].unit];
		var divisors = [];
		switch(real)
		{
			case 10:
				divisors = [2,5];
				break;
			case 24:
				divisors = [2,3,4,6,8,12];
				break;
			case 60:
				divisors = [2,3,4,5,6,10,12,15,20,30];
				break;
			case 100:
				divisors =  [2,4,5,10,20,25,50];
				break;
			case 365:
				divisors = [5,73];
				break;
			case 1000:
				divisors = [2,4,5,8,10,20,25,40,50,100,125,200,250,500];
				break;
		}
		for(var i = divisors.length - 1; i >= 0; --i) {
			if(divisors[i] > target) {
				real = divisors[i];
			}
		}
		return real;
	}
	
	var oldInit = Timeline.GregorianEtherPainter.prototype.initialize;
	Timeline.GregorianEtherPainter.prototype.initialize = function(band, timeline) {
		oldInit.call(this, band, timeline);
		this._multiple = getMultipleCount(this._band);
	};
	var oldZoom = Timeline.GregorianEtherPainter.prototype.zoom;
	Timeline.GregorianEtherPainter.prototype.zoom = function(netIntervalChange) {
		oldZoom.call(this, netIntervalChange);
		this._multiple = getMultipleCount(this._band);
	};
})();

/*
 * Zoom all bands globally
 */
(function() {
	Timeline._Impl.prototype.zoom = function (zoomIn, x, y, target) {
		var matcher = new RegExp("^timeline-band-([0-9]+)$");
		var bandIndex = null;

		var result = matcher.exec(target.id);
		if (result) {
			bandIndex = parseInt(result[1]);
		}

		if (bandIndex !== null) {
			for(var i = 0; i < this._bands.length; ++i) {
				if(i != bandIndex) {
					var netIntervalChange = this._bands[i]._ether.zoom(zoomIn);
					this._bands[i]._etherPainter.zoom(netIntervalChange);
				}
			}
			
			this._bands[bandIndex].zoom(zoomIn, x, y, target);
		}   

		this.paint();
	};
})();

/*
 * Allow moving bands till edges are at the center
 */
(function() {
	Timeline._Band.prototype.getCenterVisibleDateAfterDelta = function(delta) {
		// Max date visible on band after delta px view change is applied 
		return this._ether.pixelOffsetToDate(this._viewLength / 2 + delta);
	};

	Timeline._Impl.prototype.shiftOK = function(index, shift) {
		// Returns true if the proposed shift is ok
		//
		// Positive shift means going back in time
		var going_back = shift > 0,
			going_forward = shift < 0;
		
		// Is there an edge?
		if ((going_back	&& this.timeline_start === null) ||
			(going_forward && this.timeline_stop  === null) ||
			(shift === 0)) {
			return (true);  // early return
		}
		
		// If any of the bands has noted that it is changing the others,
		// then this shift is a secondary shift in reaction to the real shift,
		// which already happened. In such cases, ignore it. (The issue is
		// that a positive original shift can cause a negative secondary shift, 
		// as the bands adjust.)
		var secondary_shift = false;
		for (var i = 0; i < this._bands.length && !secondary_shift; i++) {
			secondary_shift = this._bands[i].busy();
		}
		if (secondary_shift) {
			return(true); // early return
		}

		var newCenter = this._bands[index].getCenterVisibleDateAfterDelta(shift);

		return (!going_back || this.timeline_start <= newCenter)  &&
			(!going_forward || newCenter <= this.timeline_stop);
	};
})();

Timeline._Band.prototype.addDecorator = function(decorator) {
    this._decorators.push(decorator);
    decorator.initialize(this,this._timeline);
    decorator.paint();
};

Timeline._Band.prototype.removeAllDecorators = function() {
	for ( var i = 0, l = this._decorators.length; i < l; i++) {
		if(this._decorators[i] !== null) {
			this.removeLayerDiv(this._decorators[i]._layerDiv);
			this._decorators[i] = null;
		}
	}
};


/*==================================================
 *  Timeline Event Source
 *==================================================
 */

(function() {
	Timeline.DefaultEventSource.prototype.loadJSON = function(data, url, timeline) {
		var base = this._getBaseURL(url);

		var stoppuhr = this._resolveRelativeURL(
				'KnowWEExtension/images/timeline/icons/stoppuhr.png', base);
		var getStart = function(date) {
			return Timeline.NativeDateUnit.fromNumber(parseInt(date.time));
		};
		var events = this._events;
		jq$.each(data.dates, function(i, date) {
			var evt = new Timeline.DefaultEventSource.Event({
				start : getStart(date),
				instant : true,
				icon : stoppuhr,
				trackNum : data.queries.length,
				startId : i,
				timeline : timeline
			});
			events.add(evt);
			added = true;
		});

		var getDate = timeline.getDate;

		jq$.each(data.queries, function(queryNum, query) {
			jq$.each(query.events, function(eventnum, event) {
				var instant = (event.start === event.end);

				var evt = new Timeline.DefaultEventSource.Event({
					instant : instant,
					trackNum : queryNum,
					start : getDate(event.start),
					end : instant ? undefined : getDate(event.end),
					timeline : timeline,
					startId : event.start,
					endId : event.end
				});
				events.add(evt);
				added = true;
			});
		});

		if (added) {
			this._fire("onAddMany", []);
		}
	};
	
	var proto = Timeline.DefaultEventSource.Event.prototype;
	var oldEvent = Timeline.DefaultEventSource.Event;
	Timeline.DefaultEventSource.Event = function(args) {
		oldEvent.call(this, args);
		this._trackNum = args.trackNum;
		this._timeline = args.timeline;
		this._startId = args.startId;
		this._endId = args.endId;
	};
	
	proto.getTimeline = function() { return this._timeline; };
	proto.getStartId = function() { return this._startId; };
	proto.getEndId = function() { return this._endId; };
	proto.fillInfoBubble = function(elmt, theme, labeller) {
		var start = Timeline.NativeDateUnit.toNumber(this.getStart());
		var end = Timeline.NativeDateUnit.toNumber(this.getEnd());
		var timeline = this.getTimeline();
		var createRunToParagraph = function(caption, time) {
			var button = timeline.createRunCaseToButton(time);
			return jq$("<p></p>").append(button).append(
					"<b>" + caption + ":</b> " +
							TimelinePlugin.createTimeAsTimeStamp(time));
		};

		if (this.isInstant()) {
			jq$(elmt).append(createRunToParagraph("Time", start));
			var content = TimelinePlugin.printEvtData(timeline.getDataAtId(this.getStartId(), this.getTrackNum()));
			jq$(elmt).append(content);
		} else {
			jq$(elmt).append(createRunToParagraph("Start", start));
			jq$(elmt).append(createRunToParagraph("End", end));
			TimelinePlugin.createInfoBubbleToolbar(timeline, this, elmt);
		}
	};
	
	Timeline.DefaultEventSource.Event.prototype = proto;
})();

