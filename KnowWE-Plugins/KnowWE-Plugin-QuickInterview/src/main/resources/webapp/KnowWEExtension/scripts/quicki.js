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


KNOWWE = typeof KNOWWE === "undefined" ? {} : KNOWWE;
KNOWWE.plugin = KNOWWE.plugin || {};

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
    applyProcessingStateToEventHandler: function(fun, event) {
      try {
        fun(event);
      } catch (e) { /* ignore */
      }
    },
    /**
     * Function: initialize add the click events and corresponding functions
     * to interview elments
     */
    initialize: function() {

      // select all elements with class="answer"
      jq$('.answer:not(.answerDisabled)').unbind('click').click(function(event) {
        KNOWWE.plugin.quicki.answerClicked(event);
      });
      // select all elements with class="answerClicked"
      jq$('.answerClicked:not(.answerDisabled)').unbind('click').click(function(event) {
        KNOWWE.plugin.quicki.answerClicked(event);
      });
      jq$('.answerDropdown').unbind('change').change(function(event) {
        KNOWWE.plugin.quicki.answerDropDownSelected(event);
      });
      jq$('.answerDropdown').unbind('focus').focus(function(event) {
        KNOWWE.plugin.quicki.updateDropDownChoices(event);
      });

      // select all elements with class="(.*)answerunknown(.*)"
      // ---> class="answerunknown" and class="answerunknownClicked"
      jq$('.answerunknown').unbind('click').click(function(event) {
        KNOWWE.plugin.quicki.answerUnknownClicked(event);
      });

      // select all elements with class="answerMC"
      jq$('.answerMC:not(.answerDisabled)').unbind('click').click(function(event) {
        KNOWWE.plugin.quicki.answerMCCollect(event);
      });
      // select all elements with class="answerMCClicked"
      jq$('.answerMCClicked:not(.answerDisabled)').unbind('click').click(function(event) {
        KNOWWE.plugin.quicki.answerMCCollect(event);
      });

      // select all elements with class="(.*)questionnaire(.*)"
      // ---> class="questionnaire" and class="emptyQuestionnaire"
      jq$('.questionnaire').unbind('click').click(function(event) {
        KNOWWE.plugin.quicki.toggleQuestionnaireVisibility(event);
      });

      // add click-event for divs with class='num-ok' to submit numValues
      // select all elements with class="num-ok"
      jq$('.num-ok').unbind('click').click(function(event) {
        KNOWWE.plugin.quicki.numAnswerClicked(event);
      });
      // select all elements with class="numinput"
      jq$('.numinput').unbind('change').change(function(event) {
        KNOWWE.plugin.quicki.numAnswerClicked(event);
      });
      // select all elements with class="inputdate"
      jq$('.inputdate').unbind('change').change(function(event) {
        KNOWWE.plugin.quicki.dateAnswerClicked(event);
      });

      // select all elements with class="inputtextvalue"
      jq$('.inputtextvalue').unbind('change').change(function(event) {
        KNOWWE.plugin.quicki.submitTextValue(event);
      });

      /*
       * restore visibility states of elements after reloading the page
       * (e.g. after sending answer val via AJAX)
       */
      KNOWWE.plugin.quicki.restoreQuestionnaireVis();

      KNOWWE.plugin.quicki.initDataDrop();

    },

    /**
     * Function: restoreQuestionnaireVis restores the visibility states of
     * questionnaires after reloading the page, eg. after an automatic AJAX
     * refresh
     */
    restoreQuestionnaireVis: function() {
      try {
        for (var qid in questionnaireVis) {

          var qvis = questionnaireVis[qid];

          var groupEl = _KS('#group_' + qid);
          var questionnaire = _KS('#' + qid);
          var indicated = questionnaire.hasClass('indicated');

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
      } catch (e) { /* ignore */
      }
    },
    /**
     * Function: restoreQuestionVis restores the visibility states of
     * questions
     */
    restoreQuestionVis: function() {

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
      } catch (e) { /* ignore */
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
    answerMCCollect: function(event) {

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
        if (rel.qid === questionID) {
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
      if (el.className === 'answerMC') {
        el.className = 'answerMCClicked answerSelected';

        // if not already contained, attach value
        if (mcanswervals.indexOf("#####" + toreplace) === -1) {
          mcanswervals += oid;
          mcanswervals += "#####";
        }
        // get the newly assembled, complete mc fact without the last
        // "#####"
        mcvals = mcanswervals.substring(0, mcanswervals.length - 5);
        // and send it
        KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
          {action: 'SetSingleFindingAction', ValueID: mcvals});

      }
      /* already clicked mc answer */
      else if (el.classList.contains('answerMCClicked')) {
        var wasUserSelected = el.classList.contains('answerSelected');
        el.className = 'answerMC';

        // save mcvalues
        var mcvalsOld = mcanswervals.substring(0, mcanswervals.length - 5);

        // if value is already contained, remove it (if it was user selected, otherwise the value is confirmed)
        if (wasUserSelected && mcanswervals.indexOf(toreplace) !== -1) {
          mcanswervals = mcanswervals.replace(toreplace, '');
        }

        // if mcanswerval storage is empty after last removal
        if (mcanswervals === "") {
          // we need to call a retract action
          KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
            {action: 'RetractSingleFindingAction', ValueID: mcvalsOld});
        } else {
          // get the newly assembled, complete mc fact
          mcvals = mcanswervals.substring(0, mcanswervals.length - 5);
          // and send it
          KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
            {action: 'SetSingleFindingAction', ValueID: mcvals});
        }
      }
    },
    /**
     * Function: answerClicked Stores the user selected answer of the
     * HTMLDialog.
     *
     * Parameters: event - The user click event on an answer.
     */
    answerClicked: function(event) {
      var el = _KE.target(event); 	// get the clicked element
      if (el.classList.contains("answerunknown")) return;
      if (el.classList.contains("answerunknownClicked")) return;
      if (el.classList.contains("answerMC")) return;
      if (el.classList.contains("answerMCClicked")) return;
      var retract = false;

      // if already clicked, it needs to be de-highlighted and val
      // retracted
      if (el.classList.contains('answerSelected')) {
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
          {action: 'RetractSingleFindingAction', ValueID: rel.choice});
      }
      // otherwise send the value
      else {
        KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
          {action: 'SetSingleFindingAction', ValueID: rel.choice});
      }
    },

    /**
     * Function: called when a select is focused, to update the availability of the contained choice options
     */
    updateDropDownChoices: function(event) {
      var select = _KE.target(event); 	// get the clicked element
      var qid = select.getAttribute("data-qid");
      if (!qid) return;

      jq$.ajax({
        url: 'action/QuickInterviewGetChoiceAvailability',
        type: 'post',
        cache: false,
        async: false, // wait for the result to avoid popup-flickering
        data: {"SectionID": sectionId(event), "ObjectID": qid}
      }).success(function(response) {
        console.info("QuickInterviewGetChoiceAvailability: ", response);
        if (!response) return;
        for (let option of select.options) {
          const choiceName = option.getAttribute("data-cid");
          if (!choiceName) continue;
          if (!(choiceName in response)) continue;

          // check if not available, the hide
          const available = response[choiceName];
          if (!available) {
            // never hide the selected choice
            if (option.defaultSelected) {
              option.setAttribute("hidden", "");
            }
            // additionally set disabled
            // 1.: to show but disable currently selected (defaultSelected) option
            // 2.: forces the popup to update, where setting only "hidden" does not
            option.setAttribute("disabled", "");
          }
        }
      });
    },

    /**
     * Function: called when a choice is selected from a drow-down field
     */
    answerDropDownSelected: function(event) {
      var select = _KE.target(event); 	// get the clicked element
      var retract = false;

      // if already clicked, it needs to be de-highlighted and val
      // retracted
      // if (el.classList.contains('answerSelected')) {
      //   retract = true;
      // }
      _KE.cancel(event);

      var option = select.selectedOptions[0];
      if (!option) return;

      var rel = eval("(" + option.getAttribute('rel') + ")");
      if (!rel) return;
      var type = rel.type;

      if (rel.choice) {
        KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
          {action: 'SetSingleFindingAction', ValueID: rel.choice});
      } else {
        KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
          {action: 'RetractSingleFindingAction'});
      }
    },
    /**
     * Function: answerUnknownClicked Sets value unknown for the clicked
     * quesstion and toggles all already highlighted answers
     *
     * Parameters: event - The user click event on an answer.
     */
    answerUnknownClicked: function(event) {

      var el = _KE.target(event); 	// get the clicked element
      var rel = eval("(" + el.getAttribute('rel') + ")");
      var questionID = rel.qid;

      // handle num input fields
      if (rel.type === 'num') {
        var numfield = _KS('#input_' + rel.qid);
        // clear input field
        if (numfield) {
          numfield.value = "";
        }
      }

      KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.qid, rel.qid,
        {action: 'SetSingleFindingAction', ValueID: 'MaU'});

      el.className = "answerunknownClicked"; // change the highlighting
      mcanswervals = ""; // reset the mcanswerval storage

      KNOWWE.plugin.quicki.toggleAnswerHighlightingAfterUnknown(questionID);

    },
    /**
     * Function: numAnswerClicked Handles the input of num-values
     *
     * Parameters: event - the event firing the action
     */
    numAnswerClicked: function(event) {
      event.stopPropagation();
      var rel = eval("(" + _KE.target(event).getAttribute('rel') + ")");

      // get the provided value if any is provided
      var inputtext = jq$(event.target).val();

      // empty values should not be sent!
      if (inputtext === '') {
        KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
          {action: 'RetractSingleFindingAction'});
        return;
      }

      // enabling float value input also with "," instead of "."
      if (inputtext.indexOf(",") !== -1) {
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
            {action: 'SetSingleFindingAction', ValueNum: inputtext});

        } else {
          _KE.target(event).value = inputtext;

          // and display error message
          errormessage = 'Input needs to be a number between ' + rel.rangeMin + ' and ' + rel.rangeMax + '!';
          _KS('#' + rel.oid + "_errormsg").className = 'errormsg';
          _KS('#' + rel.oid + "_errormsg").innerHTML = errormessage;
        }
      }
      // else just try to get the value and set it as finding
      else {
        // send KNOWWE request as SingleFindingAction with given
        // value
        KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
          {action: 'SetSingleFindingAction', ValueNum: inputtext});
      }
    },
    /**
     * Function: dateAnswerClicked Handles the input of date-values
     *
     * Parameters: event - the event firing the action
     */
    dateAnswerClicked: function(event) {
      event = new Event(event).stopPropagation();

      var rel = eval("(" + _KE.target(event).getAttribute('rel') + ")");
      if (!rel) return;

      var inputtext = jq$(event.target).val();

      if (!inputtext) {
        KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
          {action: 'RetractSingleFindingAction'});
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
        {action: 'SetSingleFindingAction', ValueDate: inputtext});
    },
    /**
     * submits the value
     */
    submitTextValue: function(event) {
      toFocus = null;
      var rel = eval("(" + _KE.target(event).getAttribute('rel') + ")");
      if (!rel) return;

      var inputtext = jq$(event.target).val();

      if (!inputtext) {
        KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
          {action: 'RetractSingleFindingAction'});
        return;
      }
      // send KNOWWE request as SingleFindingAction with given value
      KNOWWE.plugin.quicki.send(sectionId(event), rel.web, rel.ns, rel.oid, rel.qtext,
        {action: 'SetSingleFindingAction', ValueText: inputtext});
    },
    /**
     * Function: toggleImage Toggles the image display for questionnaire
     * headings
     *
     * Parameter: flag - either 1 or 0; 1 means, image is actually displayed
     * and needs to be hidden, 0 vice versa questionnaire - the element, the
     * image is attached to
     */
    toggleImage: function(flag, questionnaire) {

      if (flag === 1) {
        // questionnaire is visible and should be hidden
        // thus image needs to be the triangle indicating extensibility
        questionnaire.className = 'questionnaire pointRight';
      } else if (flag === 0) {
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
    toggleAnswerHighlighting: function(answerEl, type, retract) {

      if (answerEl.classList.contains("answerunknownClicked")) return;
      if (answerEl.classList.contains("answerunknown")) return;

      var relClicked = eval("(" + answerEl.getAttribute('rel') + ")");

      // if clicked q is a oc q, already clicked answer alternatives need
      // to be de-highlighted
      if (type === "oc") {
        _KS('.answerClicked').each(function(element) {
          var relElement = eval("(" + element.getAttribute('rel') + ")");
          if (relElement.qid === relClicked.qid) {
            element.className = 'answer';
          }
        });

        // if a oc question is clicked, also answer unknown needs to be
        // reset
        _KS('.answerunknownClicked').each(function(element) {
          var relElement = eval("(" + element.getAttribute('rel') + ")");
          if (relElement.qid === relClicked.qid) {
            element.className = 'answerunknown';
          }
        });

        // all oc answers are now per default un-highlighted by above
        // code
        // thus if un-highlighting is not correct, highlight again here
        if (!retract) {
          answerEl.className = 'answerClicked answerSelected';
        }
      }

      // otherwise just toggle highlighting
      else {
        // to highlight/unhighlight an answer if clicked on, generally
        if (answerEl.classList.contains('answerClicked')) {
          answerEl.className = 'answer';
        } else if (answerEl.className === 'answer') {
          answerEl.className = 'answerClicked  answerSelected';
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
    toggleAnswerHighlightingAfterUnknown: function(questionID) {

      _KS('.answerClicked').each(function(element) {

        var relElement = eval("(" + element.getAttribute('rel') + ")");
        if (relElement.qid === questionID) {

          if (!element.classList.contains("answerunknownClicked")) {
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
    toggleQuestionnaireVisibility: function(event) {

      // get the clicked element, i.e., the questionnaire
      var questionnaire = _KE.target(event);
      var group = _KS('#group_' + questionnaire.id);

      if (group.style.display === 'block') {
        var indicated = questionnaire.hasClass('indicated');
        if (indicated) {
          questionnaireVis[questionnaire.id] = 2;
        } else {
          questionnaireVis[questionnaire.id] = 0;
        }
        group.style.display = 'none';

        KNOWWE.plugin.quicki.toggleImage(1, questionnaire);

      } else if (group.style.display === 'none') {

        group.style.display = 'block';

        questionnaireVis[questionnaire.id] = 1;

        KNOWWE.plugin.quicki.toggleImage(0, questionnaire);
      }
    },
    /**
     * Function: send Stores the user input as single finding through an
     * AJAX request.
     *
     * Parameters: web - the web context namespace - The name of the article
     * oid - The id of the question termName - The question text params -
     * Some parameter depending on the HTMLInputElement
     */
    send: function(sectionId, web, namespace, oid, termName, params) {

      var pDefault = {
        action: 'QuickInterviewAction',
        SectionID: sectionId,
        KWikiWeb: web,
        namespace: namespace,
        ObjectID: oid,
        TermName: termName
      };

      pDefault = KNOWWE.helper.enrich(params, pDefault);

      var options = {
        url: KNOWWE.core.util.getURL(pDefault),
        response: {
          action: 'none',
          fn: function() {
            try {
              KNOWWE.helper.observer.notify('update');
            } catch (e) { /* ignore */
            }
            KNOWWE.core.util.updateProcessingState(-1);
          },
          onError: function() {
            KNOWWE.core.util.updateProcessingState(-1);
          }
        }
      };
      KNOWWE.core.util.updateProcessingState(1);
      new _KA(options).send();
    },

    /**
     * Adds a representation of the interview to the attachments
     *
     * @param sectionId id of interview's section
     */
    saveAsAttachment: function(sectionId) {
      console.log("Saving to attachment");
      var params = {
        action: 'QuickInterviewSaveAction',
        SectionID: sectionId,
        download: false
      }

      var options = {
        url: KNOWWE.core.util.getURL(params),
        loader: true,
        response: {
          fn: function() {
            window.location.reload()
          },
        }
      }
      new _KA(options).send();
    },

    initDataDrop: function() {
      if (KNOWWE.core.plugin.dropZone) {
        KNOWWE.core.plugin.dropZone.addDropZoneTo('.quickinterview', "Drop protocol here", KNOWWE.plugin.quicki.handleDrop, null, "replace")
      }
    },

    resetDataDrop: function(element) {
      if (KNOWWE.core.plugin.dropZone) {
        KNOWWE.core.plugin.dropZone.resetDropZoneStyle(element, "Drop protocol here");
      }
    },

    handleDrop: function(event) {
      event.stopPropagation();
      event.preventDefault();
      if (KNOWWE.core.plugin.dropZone) {
        KNOWWE.core.plugin.dropZone.setDropZoneStyleUploading(event.target);
      }
      const data = event.dataTransfer.files;

      if (data.length !== 1) {
        KNOWWE.notification.error(null, "Please drop only one file.");
        KNOWWE.plugin.quicki.resetDataDrop(event.target);
        return;
      }
      KNOWWE.plugin.quicki.handleUploadFile(data, sectionId(event));
    },


    /**
     * Initiates load dialogue to let user upload representation of a previously saved interview from their drive
     */
    loadFromFile: function() {
      jq$('#file-input').trigger('click');
    },

    handleUploadFile: function(files, sectionId) {
      const file = files[0];
      if (file.type !== 'text/xml') {
        KNOWWE.notification.error(null, "The given file could not be uploaded since it has the wrong format.");
        KNOWWE.plugin.quicki.resetDataDrop();
        return;
      }
      const reader = new FileReader();

      reader.addEventListener("load", function() {
        sendData = reader.result;

        var params = {
          action: "QuickInterviewLoadAction",
          fromFile: true,
          SectionID: sectionId
        }

        var options = {
          url: KNOWWE.core.util.getURL(params),
          loader: true,
          data: sendData,
          response: {
            fn: function() {
              window.location.reload();
            },
            onError: function(data) {
              KNOWWE.notification.error(data.responseText);
              KNOWWE.plugin.quicki.resetDataDrop();
            }
          }
        }
        new _KA(options).send();
      });
      reader.readAsText(file);
    },

    /**
     * Loads a representation of an interview that has previously added to the attachments
     *
     * @param sectionId id of interview's section
     */
    loadFromAttachment: function(sectionId) {
      jq$("#dialog-message").dialog({
        modal: true,
        minWidth: 400,
        buttons: {
          Load: function() {
            var name = 'quicki-session-record';
            var inputValue = jq$('input[name=' + name + ']:checked', '#' + sectionId + '-form').val();
            var params = {
              action: "QuickInterviewLoadAction",
              fromFile: false,
              SectionID: sectionId,
              loadname: inputValue
            }

            var options = {
              url: KNOWWE.core.util.getURL(params),
              loader: true,
              response: {
                fn: function() {
                  window.location.reload();
                }
              }
            }
            new _KA(options).send();

            jq$(this).dialog("close");
          }
        }
      }).parent().find('.ui-dialog-titlebar-close').css('top', '.3em');
      ;
    }
  }
}();


/**
 * Initializes the required JS functionality when DOM is readily loaded
 */
(function init() {
  if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
    jq$(window).ready(function() {
      KNOWWE.plugin.quicki.initialize();
      KNOWWE.helper.observer.subscribe('update', function() {
        var quickiId = null;
        var qid = null;
        var caret = null;
        let scrollX = window.scrollX;
        let scrollY = window.scrollY;
        jq$('.type_QuickInterview').rerender({
          beforeReplace: function($element) {
            if (!$element) return;
            var focusElement = jq$(document.activeElement);
            var quickinterview = focusElement.parents('.type_QuickInterview');
            if (quickinterview.exists() && quickinterview[0] === $element[0]) {
              quickiId = quickinterview.attr('id');
              qid = focusElement.attr('qid');
              caret = focusElement.caret();
            } else {
              quickiId = null;
              qid = null;
              caret = null;
            }
          },
          callback: function() {
            KNOWWE.tooltips.enrich();
            KNOWWE.helper.observer.notify("watches");
            KNOWWE.core.util.updateProcessingState(-1);
            KNOWWE.plugin.quicki.initialize();
            var $input = jq$('#' + quickiId + ' [qid="' + qid + '"]');
            $input.focus();
            if (caret) $input.caret(caret.begin, caret.end);
            window.scroll(scrollX, scrollY);
          }
        });
      });
    });
  }
}());
