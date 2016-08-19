/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

//var _KE = KNOWWE.helper.event;    /* Alias KNOWWE event. */
//var _KA = KNOWWE.helper.ajax;     /* Alias KNOWWE ajax. */
//var _KS = KNOWWE.helper.selector; /* Alias KNOWWE ElementSelector */
//var _KL = KNOWWE.helper.logger;   /* Alias KNOWWE logger */
//var _KN = KNOWWE.helper.element   /* Alias KNOWWE.helper.element */
//var _KH = KNOWWE.helper.hash      /* Alias KNOWWE.helper.hash */


/**
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

var toSelect;
/**
 * The KNOWWE.plugin global namespace object. If KNOWWE.plugin is already
 * defined, the existing KNOWWE.plugin object will not be overwritten so that
 * defined namespaces are preserved.
 */
if (typeof KNOWWE.plugin == "undefined" || !KNOWWE.plugin) {
	KNOWWE.plugin = function() {
		return {}
	}
}

/**
 * The KNOWWE.plugin.quicki global namespace object. If KNOWWE.plugin.quicki is
 * already defined, the existing KNOWWE.plugin.quicki object will not be
 * overwritten so that defined namespaces are preserved.
 */
KNOWWE.plugin.quicki = function() {
	return {}
}();


/**
 * Namespace: KNOWWE.plugin.quicki The quick interview (quicki) namespace.
 */
KNOWWE.plugin.quicki = function() {


	var mcanswervals = '';      // for collecting the values of MC answers
	var quickiruns = false;     // flag whether QuickI runs a session
	var questionnaireVis = new Object(); // for storing questionnaire
	// visibility states
	var questionVis = '';		// for storing question visibility states

	function sectionId(event) {
		var el = _KE.target(event);
		return jq$(el).parents('.quickinterview').attr('sectionId');
	}

	return {
		applyProcessingStateToEventHandler : function(fun, event) {
			try {
				fun(event);
			}
			catch (e) { /* ignore */
			}
		},
		/**
		 * Function: initialize add the click events and corresponding functions
		 * to interview elments
		 */
		initialize : function() {

			// select all elements with class="answer"
			jq$('.answer').click(function(event) {
				KNOWWE.plugin.quicki.answerClicked(event);
			});
			// select all elements with class="answerClicked"
			jq$('.answerClicked').click(function(event) {
				KNOWWE.plugin.quicki.answerClicked(event);
			});

			// select all elements with class="(.*)answerunknown(.*)"
			// ---> class="answerunknown" and class="answerunknownClicked"
			jq$('.answerunknown').click(function(event) {
				KNOWWE.plugin.quicki.answerUnknownClicked(event);
			});

			// select all elements with class="answerMC"
			jq$('.answerMC').click(function(event) {
				KNOWWE.plugin.quicki.answerMCCollect(event);
			});
			// select all elements with class="answerMCClicked"
			jq$('.answerMCClicked').click(function(event) {
				KNOWWE.plugin.quicki.answerMCCollect(event);
			});

			// select all elements with class="(.*)questionnaire(.*)"
			// ---> class="questionnaire" and class="emptyQuestionnaire"
			jq$('.questionnaire').click(function(event) {
				KNOWWE.plugin.quicki.toggleQuestionnaireVisibility(event);
			});

			// add click-event for divs with class='num-ok' to submit numValues
			// select all elements with class="num-ok"
			jq$('.num-ok').click(function(event) {
				KNOWWE.plugin.quicki.numAnswerClicked(event);
			});
			// select all input fields
			jq$('.numinput, .inputdate, .inputtextvalue').blur(function(event) {
				KNOWWE.plugin.quicki.focusLost(event);
			}).focus(function(event) {
				KNOWWE.plugin.quicki.focusGained(event);
			});
			// select all elements with class="numinput"
			jq$('.numinput').change(function(event) {
				KNOWWE.plugin.quicki.numAnswerClicked(event);
			});
			// select all elements with class="inputdate"
			jq$('.inputdate').change(function(event) {
				KNOWWE.plugin.quicki.dateAnswerClicked(event);
			});

			// select all elements with class="inputtextvalue"
			jq$('.inputtextvalue').change(function(event) {
				KNOWWE.plugin.quicki.submitTextValue(event);
			});

			/*
			 * restore visibility states of elements after reloading the page
			 * (e.g. after sending answer val via AJAX)
			 */
			KNOWWE.plugin.quicki.restoreQuestionnaireVis();

		},

		currentFocus : null,

		focusGained : function(event) {
			var $target = jq$(event.target);
			KNOWWE.plugin.quicki.currentFocus = $target.attr('qid');
		},

		focusLost : function(event) {
			KNOWWE.plugin.quicki.currentFocus = null;
		},

		/**
		 * Function: restoreQuestionnaireVis restores the visibility states of
		 * questionnaires after reloading the page, eg. after an automatic AJAX
		 * refresh
		 */
		restoreQuestionnaireVis : function() {
			try {
				for (var qid in questionnaireVis) {

					var qvis = questionnaireVis[qid];

					var groupEl = _KS('#group_' + qid);
					var questionnaire = _KS('#' + qid);
					var indicated = $(questionnaire).hasClass('indicated');

					// 0 means set style and image to invisible if questionnaire
					// is not indicated
					if (qvis == 0) {
						if (!indicated) {
							groupEl.style.display = 'none';
							KNOWWE.plugin.quicki.toggleImage(1, questionnaire);
						}
					}
					// 1 means set style and image to be visible = unfolded
					else if (qvis == 1) {
						groupEl.style.display = 'block';
						KNOWWE.plugin.quicki.toggleImage(0, questionnaire);
					}
					// 2 means set style and image to be invisible although
					// the questionnaire is indicated
					else if (qvis == 2) {
						if (!indicated) {
							questionnaireVis[qid] = 0;
						}
						groupEl.style.display = 'none';
						KNOWWE.plugin.quicki.toggleImage(1, questionnaire);
					}
				}
			}
			catch (e) { /* ignore */
			}
		},
		/**
		 * Function: restoreQuestionVis restores the visibility states of
		 * questions
		 */
		restoreQuestionVis : function() {

			KNOWWE.core.util.updateProcessingState(1);
			try {
				// split questionVis storage
				var qs = questionVis.split('###');

				for (var i = 0; i < qs.length; i++) {

					// split into question id and visibility
					var qsplit = qs[i].split(';');
					var qid = qsplit[0];
					qid = qid.replace(/ /g, ''); // remove spaces
					var qvis = qsplit[1];

					// TODO check what we need to get here: the table???
					var groupEl = _KS('#group_' + qid);
					var questionnaire = _KS('#' + qid);

					// 0 means set style and image to invisible
					if (qvis == 0) {
						groupEl.style.display = 'none';
						KNOWWE.plugin.quicki.toggleImage(1, questionnaire);
					}
					// 1 means set style and image to be visible = unfolded
					else if (qvis == 1) {
						groupEl.style.display = 'block';
						KNOWWE.plugin.quicki.toggleImage(0, questionnaire);
					}
				}
			}
			catch (e) { /* ignore */
			}
			KNOWWE.core.util.updateProcessingState(-1);
		},
		/**
		 * Function: answerMCCollect collects given mc answer vals into a
		 * variable for sending them later as ONE MultipleChoiceValue
		 *
		 * Parameters: event - the event fired by the mc answer val that was
		 * clicked
		 */
		answerMCCollect : function(event) {

			/*
			 * This is a Workaround, because the mcanswervals are reset
			 * somewhere in the js even if there are set values you can see in
			 * the Quick-Interview! Johannes recollect all mcanswervals
			 * belonging to question
			 */
			var el = _KE.target(event); 	// get the clicked element
			_KE.cancel(event);

			var clickedRel = eval("(" + el.getAttribute('rel') + ")");
			var questionID = clickedRel.qid;
			mcanswervals = '';
			_KS('.answerMCClicked').each(function(element) {
				var rel = eval("(" + element.getAttribute('rel') + ")");
				if (rel.qid == questionID) {
					mcanswervals += rel.choice;
					mcanswervals += "#####"
				}
			});

			// TODO new algorithm for mc vals:
			// if a not highlighted answer is clicked it is
			// - highlighted
			// - we fetch all already highlighted answers and set those as one
			// new mc fact
			// - during each setting, the last-set-fact is stored
			// if an answer already answered is clicked it is
			// - de-highlighted
			// - again all selected answers are fetched (i.e. minus the clicked
			// one)
			// - and set as a mc fact
			// if mcanswervals is empty, retract the last fact

			var rel = eval("(" + el.getAttribute('rel') + ")");
			if (!rel) return;
			var oid = rel.choice;
			var toreplace = oid + "#####";

			/*
			 * not yet clicked, thus highlight, store, collect all highlighted,
			 * and send
			 */
			if (el.className == 'answerMC') {
				el.className = 'answerMCClicked';

				// if not already contained, attach value
				if (mcanswervals.indexOf("#####" + toreplace) == -1) {
					mcanswervals += oid;
					mcanswervals += "#####";
				}
				// get the newly assembled, complete mc fact without the last
				// "#####"
				mcvals = mcanswervals.substring(0, mcanswervals.length - 5);
				// and send it
				KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
					{action : 'SetSingleFindingAction', ValueID : mcvals});

			}
			/* already clicked mc answer */
			else if (el.className == 'answerMCClicked') {
				el.className = 'answerMC';

				// save mcvalues
				var mcvalsOld = mcanswervals.substring(0, mcanswervals.length - 5);

				// if value is alerady contained, remove it
				if (mcanswervals.indexOf(toreplace) != -1) {
					mcanswervals = mcanswervals.replace(toreplace, '');
				}

				// if mcanswerval storage is empty after last removal
				if (mcanswervals == "") {
					// we need to call a retract action
					KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
						{action : 'RetractSingleFindingAction', ValueID : mcvalsOld});
				} else {
					// get the newly assembled, complete mc fact
					mcvals = mcanswervals.substring(0, mcanswervals.length - 5);
					// and send it
					KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
						{action : 'SetSingleFindingAction', ValueID : mcvals});
				}
			}
		},
		/**
		 * Function: answerClicked Stores the user selected answer of the
		 * HTMLDialog.
		 *
		 * Parameters: event - The user click event on an answer.
		 */
		answerClicked : function(event) {
			var el = _KE.target(event); 	// get the clicked element
			if (el.className.toLowerCase() == "answerunknown") return;
			if (el.className.toLowerCase() == "answerunknownclicked") return;
			if (el.className.toLowerCase() == "answermc") return;
			if (el.className.toLowerCase() == "answermcclicked") return;
			var retract = false;

			// if already clicked, it needs to be de-highlighted and val
			// retracted
			if (el.className == 'answerClicked') {
				retract = true;
			}
			_KE.cancel(event);

			var rel = eval("(" + el.getAttribute('rel') + ")");
			if (!rel) return;
			var type = rel.type;

			KNOWWE.plugin.quicki.toggleAnswerHighlighting(el, type, retract);

			// if it is already highlighted it should now be deactivated and
			// value retracted
			if (retract) {
				KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
					{action : 'RetractSingleFindingAction', ValueID : rel.choice});
			}
			// otherwise send the value
			else {
				KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
					{action : 'SetSingleFindingAction', ValueID : rel.choice});
			}
		},
		/**
		 * Function: answerUnknownClicked Sets value unknown for the clicked
		 * quesstion and toggles all already highlighted answers
		 *
		 * Parameters: event - The user click event on an answer.
		 */
		answerUnknownClicked : function(event) {

			var el = _KE.target(event); 	// get the clicked element
			var rel = eval("(" + el.getAttribute('rel') + ")");
			var questionID = rel.qid;

			// handle num input fields
			if (rel.type == 'num') {
				var numfield = _KS('#input_' + rel.qid);
				// clear input field
				if (numfield) {
					numfield.value = "";
				}
			}

			KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
				{action : 'SetSingleFindingAction', ValueID : 'MaU'});

			el.className = "answerunknownClicked"; // change the highlighting
			mcanswervals = ""; // reset the mcanswerval storage

			KNOWWE.plugin.quicki.toggleAnswerHighlightingAfterUnknown(questionID);

		},
		/**
		 * Function: numAnswerClicked Handles the input of num-values
		 *
		 * Parameters: event - the event firing the action
		 */
		numAnswerClicked : function(event) {
			event.stopPropagation();
			var rel = eval("(" + _KE.target(event).getAttribute('rel') + ")");

			// get the provided value if any is provided
			var inputtext = jq$(event.target).val();

			// empty values should not be sent!
			if (inputtext == '') {
				KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
					{action : 'SetSingleFindingAction', ValueID : 'MaU'});
				return;
			}

			// enabling float value input also with "," instead of "."
			if (inputtext.indexOf(",") != -1) {
				inputtext = inputtext.replace(",", ".");
			}
			if (!inputtext.match(/^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$/)) {
				var errormessage = 'Input needs to be a number!';
				_KS('#' + rel.oid + "_errormsg").className = 'errormsg';
				_KS('#' + rel.oid + "_errormsg").innerHTML = errormessage;
				return;
			}
			// if range is given, validate range
			if (rel.rangeMin != 'NaN' && rel.rangeMax != 'NaN') {

				var min = parseFloat(rel.rangeMin);
				var max = parseFloat(rel.rangeMax);
				// compare with range
				if (parseFloat(inputtext) >= min && parseFloat(inputtext) <= max) {

					if (_KS('#' + rel.oid + "_errormsg")) {
						_KS('#' + rel.oid + "_errormsg").className = 'invisible';
						_KS('#' + rel.oid + "_errormsg").innerHTML = '';
					}

					// send KNOWWE request as SingleFindingAction with given
					// value
					KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
						{action : 'SetSingleFindingAction', ValueNum : inputtext});

				} else {
					_KE.target(event).value = inputtext;

					// and display error message
					var errormessage = 'Input needs to be a number between ' + rel.rangeMin + ' and ' + rel.rangeMax + '!';
					_KS('#' + rel.oid + "_errormsg").className = 'errormsg';
					_KS('#' + rel.oid + "_errormsg").innerHTML = errormessage;
				}
			}
			// else just try to get the value and set it as finding
			else {
				// send KNOWWE request as SingleFindingAction with given
				// value
				KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
					{action : 'SetSingleFindingAction', ValueNum : inputtext});
			}
		},
		/**
		 * Function: dateAnswerClicked Handles the input of date-values
		 *
		 * Parameters: event - the event firing the action
		 */
		dateAnswerClicked : function(event) {
			event = new Event(event).stopPropagation();

			var rel = eval("(" + _KE.target(event).getAttribute('rel') + ")");
			if (!rel) return;

			var inputtext = jq$(event.target).val();

			if (!inputtext) {
				KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
					{action : 'SetSingleFindingAction', ValueID : 'MaU'});
				return;
			}
			// either yyyy-MM-dd or dd.MM.yyyy, separator either - or .
			var dateRegex = "(\\d\\d\\d\\d(-|\\.)\\d\\d(-|\\.)\\d\\d|\\d\\d(-|\\.)\\d\\d(-|\\.)\\d\\d\\d\\d)";
			// HH-mm-ss-SSS, but seconds and milliseconds are optional, separator either - or :
			var timeRegex = "\\d\\d(-|\\:)\\d\\d((-|\\:)\\d\\d((-|\\:|\\.)\\d\\d\\d)?)?";
			// like UTC or GMT+8:00 or "Pacific Standard Time"
			var timeZoneRegex = "((?:[a-zA-Z]\s*)+|GMT[+-]\d?\d:\d\d)";
			// time is optional
			var dateTimeRegex = new RegExp("^\\s*" + dateRegex + "((\\s|-)" + timeRegex + ")?\\s*(" + timeZoneRegex + ")?$");
			if (!(inputtext.match(dateTimeRegex))) {
				var errormessage = 'Input has wrong format!';
				_KS('#' + rel.oid + "_errormsg").className = 'errormsg';
				_KS('#' + rel.oid + "_errormsg").innerHTML = errormessage;
				return;
			}
			// send KNOWWE request as SingleFindingAction with given value
			KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
				{action : 'SetSingleFindingAction', ValueDate : inputtext});
		},
		/**
		 * submits the value
		 */
		submitTextValue : function(event) {
			toFocus = null;
			var rel = eval("(" + _KE.target(event).getAttribute('rel') + ")");
			if (!rel) return;

			var inputtext = jq$(event.target).val();

			if (!inputtext) {
				KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
					{action : 'SetSingleFindingAction', ValueID : 'MaU'});
				return;
			}
			// send KNOWWE request as SingleFindingAction with given value
			KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
				{action : 'SetSingleFindingAction', ValueText : inputtext});
		},
		/**
		 * Function: toggleImage Toggles the image display for questionnaire
		 * headings
		 *
		 * Parameter: flag - either 1 or 0; 1 means, image is actually displayed
		 * and needs to be hidden, 0 vice versa questionnaire - the element, the
		 * image is attached to
		 */
		toggleImage : function(flag, questionnaire) {

			if (flag == 1) {
				// questionnaire is visible and should be hidden
				// thus image needs to be the triangle indicating extensibility
				questionnaire.className = 'questionnaire pointRight';
			} else if (flag == 0) {
				questionnaire.className = 'questionnaire pointDown';
			}
		},
		/**
		 * Function: toggleAnswerHighlighting Hightlights an answer if clicked
		 * or unhighlights an highlighted answer if clicked
		 *
		 * Parameters: answerEl - The clicked answer-element type - flag that
		 * tells whether OC or MC question for appropriate highlighting retract -
		 * flag that tells whether question is to be set or retracted
		 */
		toggleAnswerHighlighting : function(answerEl, type, retract) {

			if (answerEl.className.toLowerCase() == "answerunknownclicked") return;
			if (answerEl.className.toLowerCase() == "answerunknown") return;

			var relClicked = eval("(" + answerEl.getAttribute('rel') + ")");

			// if clicked q is a oc q, already clicked answer alternatives need
			// to be de-highlighted
			if (type == "oc") {
				_KS('.answerClicked').each(function(element) {
					var relElement = eval("(" + element.getAttribute('rel') + ")");
					if (relElement.qid == relClicked.qid) {
						element.className = 'answer';
					}
				});

				// if a oc question is clicked, also answer unknown needs to be
				// reset
				_KS('.answerunknownClicked').each(function(element) {
					var relElement = eval("(" + element.getAttribute('rel') + ")");
					if (relElement.qid == relClicked.qid) {
						element.className = 'answerunknown';
					}
				});

				// all oc answers are now per default un-highlighted by above
				// code
				// thus if un-highlighting is not correct, highlight again here
				if (!retract) {
					answerEl.className = 'answerClicked';
				}
			}

			// otherwise just toggle highlighting
			else {
				// to highlight/unhighlight an answer if clicked on, generally
				if (answerEl.className == 'answerClicked') {
					answerEl.className = 'answer';
				} else if (answerEl.className == 'answer') {
					answerEl.className = 'answerClicked';
				}
			}
		},
		/**
		 * Function toggleAnswerHighlightingAfterUnknown process the correct
		 * highlighting of answer elements after unknown is clicked i.e., remove
		 * highlighting
		 *
		 * Parameters: questionID - id of the question that was clicked with
		 * unknown
		 */
		toggleAnswerHighlightingAfterUnknown : function(questionID) {

			_KS('.answerClicked').each(function(element) {

				var relElement = eval("(" + element.getAttribute('rel') + ")");
				if (relElement.qid == questionID) {

					if (element.className != "answerunknownClicked") {
						element.className = "answer";
					}
				}
			});
		},
		/**
		 * Function: updateQuestionnaireVisibility Toggles the visibility of
		 * questionnaire-contents on click: visible ones are hidden, hidden ones
		 * are displayed
		 *
		 * Parameters: event - The fired click event
		 */
		toggleQuestionnaireVisibility : function(event) {

			// get the clicked element, i.e., the questionnaire
			var questionnaire = _KE.target(event);
			var group = _KS('#group_' + questionnaire.id);

			if (group.style.display == 'block') {
				var indicated = $(questionnaire).hasClass('indicated');
				if (indicated) {
					questionnaireVis[questionnaire.id] = 2;
				} else {
					questionnaireVis[questionnaire.id] = 0;
				}
				group.style.display = 'none';

				KNOWWE.plugin.quicki.toggleImage(1, questionnaire);

			} else if (group.style.display == 'none') {

				group.style.display = 'block';

				questionnaireVis[questionnaire.id] = 1;

				KNOWWE.plugin.quicki.toggleImage(0, questionnaire);
			}
		},
		/**
		 * Function: toggleQuestionVisibility Toggles the visibility of
		 * question-contents on click: visible ones are hidden, hidden ones are
		 * displayed
		 *
		 * Parameters: event - The fired the click event
		 */
		toggleQuestionVisibility : function(event) {

			// get the clicked element, i.e., the questionnaire
			/*
			 * var question = _KE.target(event); var group = _KS('#group_' +
			 * question.id);
			 * 
			 * if(group.style.display=='block'){ group.style.display = 'none'; }
			 * else if (group.style.display=='none'){ group.style.display =
			 * 'block'; } KNOWWE.plugin.quicki.showRefreshed;
			 */
		},
		/**
		 * Function: send Stores the user input as single finding through an
		 * AJAX request.
		 *
		 * Parameters: web - the web context namespace - The name of the article
		 * oid - The id of the question termName - The question text params -
		 * Some parameter depending on the HTMLInputElement
		 */
		send : function(sectionId, web, namespace, oid, termName, params) {

			var pDefault = {
				action : 'QuickInterviewAction',
				SectionID : sectionId,
				KWikiWeb : web,
				namespace : namespace,
				ObjectID : oid,
				TermName : termName
			};

			pDefault = KNOWWE.helper.enrich(params, pDefault);

			var options = {
				url : KNOWWE.core.util.getURL(pDefault),
				response : {
					action : 'none',
					fn : function() {
						try {
							KNOWWE.helper.observer.notify('update');
						}
						catch (e) { /* ignore */
						}
						KNOWWE.core.util.updateProcessingState(-1);
					},
					onError : function() {
						KNOWWE.core.util.updateProcessingState(-1);
					}
				}
			};
			KNOWWE.core.util.updateProcessingState(1);
			new _KA(options).send();
		},
		/**
		 * Function: showRefreshed send the request and render the interview
		 * newly via QuickInterviewAction
		 */
		showRefreshed : function(sectionId) {

			// needed to avoid endless calls in case quicki is reloaded
			// due to a session clearing from solution panel
			if (!_KS('.quickinterview'))
				return;

			var params = {
				namespace : KNOWWE.helper.gup('page'),
				action : 'QuickInterviewAction',
				SectionID : sectionId
			};


			// also submit config parameters defined in the markup
			var resetPointer = _KS('#quickireset');
			if (resetPointer) {
				var relations = eval("(" + resetPointer.getAttribute('rel') + ")");
				params = KNOWWE.helper.enrich(relations, params);

			}
			// we need to get this temp variable here, because on rerender, we also lose focus
			var currentFocus = KNOWWE.plugin.quicki.currentFocus;
			var currentCaret = jq$('#' + sectionId + ' [qid="' + currentFocus + '"]').caret();
			var options = {
				url : KNOWWE.core.util.getURL(params),
				response : {
					action : 'insert',
					ids : ["quickinterview_" + sectionId],
					fn : function() {
						try {
							KNOWWE.plugin.quicki.initialize();

							/* recollect all mcanswervals */
							mcanswervals = '';
							_KS('.answerMCClicked').each(function(element) {
								var rel = eval("(" + element.getAttribute('rel') + ")");
								mcanswervals += rel.choice;
								mcanswervals += "#####"
							});

							if (currentFocus) {
								var $input = jq$('#' + sectionId + ' [qid="' + currentFocus + '"]');
								$input.focus();
								$input.caret(currentCaret.begin, currentCaret.end);
								//$input.prop("selectionEnd", currentFocus.selectionEnd);
							}
						}
						catch (e) { /* ignore */
						}
						KNOWWE.tooltips.enrich();
						//TODO
						//is there a possibility to only call this once after the final rerender?
						//at the moment it is call multiple times
						//update watches
						KNOWWE.helper.observer.notify("watches");
						KNOWWE.core.util.updateProcessingState(-1);
					},
					onError : function() {
						KNOWWE.core.util.updateProcessingState(-1);
					}
				}
			};
			KNOWWE.core.util.updateProcessingState(1);
			new _KA(options).send();
		}
	}
}();


/**
 * Initializes the required JS functionality when DOM is readily loaded
 */
(function init() {
	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function() {
			KNOWWE.plugin.quicki.initialize();
			var fn = function() {
				jq$('.quickinterview').each(function() {
					KNOWWE.plugin.quicki.showRefreshed(jq$(this).attr('sectionId'));
				})
			};
			KNOWWE.helper.observer.subscribe('update', fn);
		});
	}
}());