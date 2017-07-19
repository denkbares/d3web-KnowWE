var Interview = {};

Interview.startCase = function() {
	window.location = "../../../Restart?KWikiWeb=default_web&lang=" + Translate.getCurrentLanguage();
};

Interview.downloadProtocol = function() {
	window.location = "../../../DownloadProtocol";
};

/**
 * Decides if a question should be answered or a given answer should be retracted.
 */
Interview.dispatch = function(id, value) {

	var cNode = $("choice_" + id + "_" + value);
	if (cNode) {
		var questionSection = Element.up(cNode, ".section");
		var choiceIsAnswered = Element.hasClassName(questionSection, "sectionactive");

		var qNode = Element.up(cNode, ".question");
		var isMultipleChoiceQuestion = Element.hasClassName(qNode, "mc");

		if (choiceIsAnswered && isMultipleChoiceQuestion) {
			Interview.retract(id, value);
		}
		else {
			Interview.answer(id, value);
		}
	}
};

Interview.retract = function(id, value) {
	var qNode = $$("'div[id=" + "\"question_" + id + "\"]'").last();
	var cNode = $$("'div[id=" + "\"choice_" + id + "_" + value + "\"]'").last();

	if (cNode) {
		LookAndFeel.deactivateSection(cNode);
	}
	if (qNode) {
		var activeSectionsLeft = Element.select(qNode, ".sectionactive").length;
		if (activeSectionsLeft == 0) {
			Element.removeClassName(qNode, "answered");
		}
	}
	var url = "../../../RetractAnswer/" + (new Date().getTime()) + "?" + encodeURIComponent(unescape(id)) + "=" + encodeURIComponent(unescape(value));
	CCAjaxIndicator.show();
	new Ajax.Request(url, {
		method : 'get',
		onSuccess : function(transport) {
			//Interview.displayNextQuestion();
			CCAjaxIndicator.hide();
			Interview.loadNotifications();
		},
		onFailure : function() {
			CCAjaxIndicator.hide();
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException : function(transport, exception) {
			CCAjaxIndicator.hide();
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	});
};

Interview.answer = function(id, value) {
	var qNode = $$("'div[id=" + "\"question_" + id + "\"]'").last();
	var cNode = $$("'div[id=" + "\"choice_" + id + "_" + value + "\"]'").last();

	if (qNode) {
		//mark question as answered
		Element.addClassName(qNode, "answered");
	}
	if (qNode && cNode) {
		//check if question is a multiple-choice-question
		var isMultipleChoiceQuestion = Element.hasClassName(qNode, "mc");
		//if question is not a MC-question:
		if (!isMultipleChoiceQuestion) {
			//deactivate (unmark) all oder choices
			LookAndFeel.deactivateAllSections(qNode);
		}
		//activate the choice (mark as answered)
		LookAndFeel.activateSection(cNode);
	}
	//var infoObject = KBInfo.getInfoObject[id];
	var url = "../../../SetAnswer/" + (new Date().getTime()) + "?" + encodeURIComponent(unescape(id)) + "=" + encodeURIComponent(unescape(value));
	CCAjaxIndicator.show();
	new Ajax.Request(url, {
		method : 'get',
		onSuccess : function(transport) {
			Interview.displayNextQuestion();
			CCAjaxIndicator.hide();
			Interview.loadNotifications();
		},
		onFailure : function() {
			CCAjaxIndicator.hide();
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException : function(transport, exception) {
			CCAjaxIndicator.hide();
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	});
};

Interview.loadNotifications = function() {
	if (KNOWWE && KNOWWE.notification) {
		KNOWWE.notification.loadNotifications();
	}
}
//	// mark this question as answered
//	if (qNode) {
//		var isMultipleChoiceQuestion = Element.hasClassName(qNode, "mc");
//		
//		Element.addClassName(qNode, "answered");
//		if(!isMultipleChoiceQuestion){
//			LookAndFeel.deactivateAllSections(qNode);
//		}
//	}
//	if (cNode) {
//		LookAndFeel.activateSection(cNode);
//	}


Interview.displayNextQuestion = function(refreshAll) {
	var url = "../../../GetInterview/" + (new Date().getTime());
	new Ajax.Request(url, {
		method : 'get',
		parameters : {
			lang : Translate.getCurrentLanguage(),
			history : refreshAll ? "true" : "false"
		},
		onSuccess : function(transport) {
			var xml = transport.responseXML;
			// render questions of response
			var his = xml.getElementsByTagName("history");
			var ask = xml.getElementsByTagName("questionnaire");
			var sol = xml.getElementsByTagName("solutions");
			var cb = xml.getElementsByTagName("showAlternatives");

			var button = $("alternativeButton");
			if (button) {
				button.style.display = cb.length != 0 ? "inline-block" : "none";
			}
			// get all questions before inserting the new one
			var questions = Element.select($("interview"), ".question");
			var node = null;
			if (refreshAll && his && his[0]) {
				$("interview").innerHTML = "";
				var infoObjects = KBInfo.parseInfoObjects(his[0]);
				for (var i = 0; i < infoObjects.length; i++) {
					var html = Interview.renderQuestion(infoObjects[i]);
					LookAndFeel.appendChildDIV("interview", html);
				}
			}
			if (ask && ask[0]) {
				// insert new question
				var infoObjects = KBInfo.parseInfoObjects(ask[0]);
				for (var i = 0; i < infoObjects.length; i++) {
					var html = Interview.renderQuestion(infoObjects[i]);
					node = LookAndFeel.appendChildDIV("interview", html);
				}
				// and remove all previously unanswered questions
				for (var i = 0; i < questions.length; i++) {
					var question = questions[i];
					if (!Element.hasClassName(question, "answered")) {
						LookAndFeel.removeChildDIV(question);
					}
				}
				// remove solutions if we have added a new (unanswered) question
				if (node) {
					$("solutions").innerHTML = "";
				}
			}
			// render solutions of response, if there are no questions!
			if (sol && sol[0] && !node) {
				var infoObjects = KBInfo.parseInfoObjects(sol[0]);
				$("solutions").innerHTML = LookAndFeel.renderSeparator(
					Translate.get("result_title"),
					LookAndFeel.renderButton("../image/new.gif", Translate.get("use_button_new"), "Interview.startCase();") +
					LookAndFeel.renderButton("../image/save.gif", Translate.get("use_button_save"), "Interview.downloadProtocol();")
				);
				for (var i = 0; i < infoObjects.length; i++) {
					// hack: ignore exported flowchart context solutions
					if (infoObjects[i].getName().indexOf("_KONTEXT_") >= 0) continue;
					var html = Interview.renderSolution(infoObjects[i]);
					node = LookAndFeel.appendChildDIV("solutions", html);
				}
			}
			// scroll to newly added elements
			// focus newly added input fields
			if (node) {
				Effect.ScrollTo(node);
				var fields = Element.select(node, "input");
				if (fields && fields[0]) {
					// only select if unanswered question
					var answeredElement = Element.up(fields[0], ".answered");
					if (!answeredElement || !answeredElement[0]) {
						fields[0].select();
					}
				}
			}
		},
		onFailure : function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException : function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	});
};

Interview._isImageLink = function(link) {
	if (!link) return false;
	link = link.toLowerCase().trim();
	var result =
		link.endsWith(".gif") ||
		link.endsWith(".png") ||
		link.endsWith(".jpg") ||
		link.endsWith(".jpeg") ||
		link.endsWith(".tif") ||
		link.endsWith(".tiff") ||
		link.endsWith(".bmp");
	result = result && link.indexOf(';') == -1;
	return result;
};

Interview._isMultiImageLink = function(link) {
	return Interview._getImageLinks(link).length > 0
};

Interview._getImageLinks = function(link) {
	var result = [];
	var links = link ? link.split(";") : [];
	for (var i = 0; i < links.length; i++) {
		var url = links[i];
		if (Interview._isImageLink(url)) {
			result.push(url);
		}
	}
	return result;
};

Interview._makeUrlLink = function(linkOrPath) {
	if (!linkOrPath) return null;
	var hasParam = linkOrPath.indexOf('?') > 0;
	linkOrPath += (hasParam ? "&" : "?") + "lang=" + Translate.getCurrentLanguage();
	if (linkOrPath.match(/^[\w]*:.*$/g)) return linkOrPath;
	return "../../../Multimedia/" + linkOrPath;
};

Interview.renderSolution = function(infoObject) {
	var html = "<div class=solution>\n";

	// heading: solution name
	var name = infoObject.getName();
	var text = infoObject.getText();
	if (text) {
		var alt = name.replace(/[<>]/g, '-').replace(/[']/g, '"');
		html += "\t<h3 title=" + alt + ">" + text + "</h3>\n";
	}
	else {
		html += "\t<h3>" + name + "</h3>\n";
	}

	// description
	var description = infoObject.getDescription();
	if (description) html += "<p>" + Interview._renderDescription(description);

	// support knowledge
	var info = infoObject.getInfo();
	var multimedia = infoObject.getMultimedia();
	var link = infoObject.getLink();
	if (info || multimedia) html += "<p>";
	if (Interview._isImageLink(multimedia)) {
		html += LookAndFeel.renderImage(Interview._makeUrlLink(multimedia), "100%", "40%");
	}
	else if (Interview._isMultiImageLink(multimedia)) {
		var links = Interview.getImageLinks(multimedia);
		var width = 95 / links.length;
		for (var i = 0; i < links.length; i++) {
			html += LookAndFeel.renderImage(Interview._makeUrlLink(links[i]), width + "%", "40%");
		}
	}
	else if (multimedia && link) {
		link = multimedia + ";" + link;
	}
	else if (multimedia) {
		link = multimedia;
	}
	if (info) {
		html += info + "\n";
	}
	if (info || multimedia) html += "</p>";

	if (link) {
		var urls = link.split(";");
		for (var i = 0; i < urls.length; i++) {
			html += "<p><a href='" +
				Interview._makeUrlLink(urls[i]) + "'>" + urls[i] + "</a></p>";
		}
	}

	html += "\n</div>\n";
	return LookAndFeel.renderBox(html);
};

Interview.renderQuestion = function(infoObject) {
	var className = "question";
	if (infoObject.getValues() && infoObject.getValues().length > 0) {
		className += " answered";
	}
	if (infoObject.getType() == 'mc') {
		//add marker for multiple choice questions
		className += " mc";
	}
	var html = "<div id='question_" + escape(infoObject.getID()) + "' class='" + className + "'>\n";

	// heading: ask question
	var name = infoObject.getName();
	var text = infoObject.getText();
	if (text) {
		var alt = name.replace(/[<>]/g, '-').replace(/[']/g, '"');
		html += "\t<h3 title=" + alt + ">" + text + "</h3>\n";
	}
	else {
		html += "\t<h3>" + name + "</h3>\n";
	}

	// description
	var description = infoObject.getDescription();
	if (description) html += "<p>" + Interview._renderDescription(description);

	// support knowledge
	var info = infoObject.getInfo();
	var multimedia = infoObject.getMultimedia();
	var link = infoObject.getLink();
	if (info || multimedia) html += "<p>";
	if (Interview._isImageLink(multimedia)) {
		var regions = infoObject.getRegions();
		var mapID = null;
		if (regions && regions.length > 0) {
			mapID = "map_" + escape(infoObject.getID());
		}
		html += LookAndFeel.renderImage(Interview._makeUrlLink(multimedia), "100%", "40%", mapID);
		if (mapID) {
			html += "<map name='" + mapID + "'>";
			// render regions in reverse order, 
			// beacuse image map has higher priority on first regions
			// (in contrast to image map markup)
			for (var i = regions.length - 1; i >= 0; i--) {
				var regValue = regions[i].value;
				var regCoords = regions[i].coordinates;
				var command = "Interview.dispatch(\"" + escape(infoObject.getID()) + "\", \"" + escape(regValue) + "\")";
				html += "<area" +
					" onclick='" + command + ";'" +
					" alt='" + regValue + "'" +
					" coords='" + regCoords + "'" +
					" shape='poly'>";
			}
			html += "</map>";
		}
	}
	else if (multimedia && link) {
		link = multimedia + ";" + link;
	}
	else if (multimedia) {
		link = multimedia;
	}
	if (info) {
		html += info + "\n";
	}
	if (info || multimedia) html += "</p>";

	if (link) {
		var urls = link.split(";");
		var width = 95 / urls.length;
		for (var i = 0; i < urls.length; i++) {
			var url = urls[i];
			if (Interview._isImageLink(url)) {
				html += LookAndFeel.renderImage(Interview._makeUrlLink(url), width + "%", "40%");
			}
		}
		for (var i = 0; i < urls.length; i++) {
			var url = urls[i];
			if (!Interview._isImageLink(url)) {
				html += "<p><a href='" +
					Interview._makeUrlLink(url) + "'>" + url + "</a></p>";
			}
		}
	}

	// content: answer choice question
	var choices = infoObject.getChoices();
	for (var i = 0; i < choices.length; i++) {
		var image = null;
		var text = choices[i].text ? choices[i].text : Translate.get("use_question_unknown");
		var link = choices[i].link;
		var abnormality = choices[i].abnormality;
		var imageClass = "";
		var answerClass = "answer-choice-section choice-count-" + choices.length;
		if (!abnormality) {
			var ltext = text.toLowerCase();
			if (ltext == "weiter" || ltext == "verder" || ltext == "ok" || ltext == "ja" || ltext == "yes") image = "../image/check.png";
			if (ltext == "nein" || ltext == "no" || ltext == "nee") image = "../image/forbidden.png";
		}
		else {
			if (abnormality == "0.0") {
				image = "../image/check.png";
				imageClass = "answer-image-normal";
				answerClass += " answer-normal";
			}
			else {
				image = "../image/forbidden.png";
				imageClass = "answer-image-abnormal";
				answerClass += " answer-abnormal";
			}
		}
		if (Interview._isImageLink(link)) {
			image = link;
			imageClass = "answer-image-multimedia";
		}
		var onClick = "Interview.dispatch(\"" + escape(infoObject.getID()) + "\", \"" + escape(choices[i].id) + "\")";
		html += LookAndFeel.renderSection(
			"<div id='choice_" + escape(infoObject.getID()) + "_" + escape(choices[i].id) + "'" +
			" class='answer-choice'>" +
			LookAndFeel.renderImage(image, "25%", 48, null, imageClass) +
			text + "</div>", onClick, choices[i].selected,
			answerClass
		);
	}

	// content: answer textual questions (text, num, ...)
	if (infoObject.getType() == KBInfo.Question.TYPE_NUM
		|| infoObject.getType() == KBInfo.Question.TYPE_TEXT) {
		html += "<p><input type=text class=answer";
		if (infoObject.getValues() && infoObject.getValues()[0]) {
			html += " value='" + String.escapeQuotes(infoObject.getValues()[0]) + "'";
		}
		html += " onblur='Interview._typedAnswerChanged(\"" + escape(infoObject.getID()) + "\", this);'";
		html += " onkeyup='Interview._typedAnswerAction(\"" + escape(infoObject.getID()) + "\", this, event);'";
		html += "></input>";
		var min = infoObject.getMin();
		var max = infoObject.getMax();
		html += (min && max) ? "&nbsp;" + min + "&nbsp;..&nbsp;" + max : "";
		var unit = infoObject.getUnit();
		html += unit ? "&nbsp;" + unit : "";
		html += "</p>";
	}

	html += "\n</div>\n";
	return LookAndFeel.renderBox(html, "question-section question-type-" + infoObject.getType());
};

Interview._renderDescription = function(text) {
	return text.replace(/<\?\#([^>]+)>/gi,
		"<a href='javascript:Interview._openViewer(&quot;$1&quot;);'>" +
		"<span style='position:relative'><img style='position:absolute;top:-2px;' src='../image/help16.gif'></span>" +
		"</a>");
};

Interview._openViewer = function(file) {
	var url = "../../../ExternalViewer/" + file + "?" + (new Date().getTime());
	new Ajax.Request(url, {
		method : 'get',
		parameters : {}
	});
};

Interview._typedAnswerAction = function(id, field, event) {
	if (!event) event = window.event;
	var code = (event.which) ? event.which : event.keyCode;
	var valid = Interview._checkValidInput(id, field);
	if (code == 13 && valid) {
		// blur automatically answers the question
		// due to registered event handler
		field.blur();
	}
};

Interview._typedAnswerChanged = function(id, field) {
	if (!Interview._checkValidInput(id, field)) return;
	var value = field.value;
	Interview.answer(id, value, field);
};

Interview._checkValidInput = function (id, field) {
	var question = KBInfo.getInfoObject(unescape(id));
	var isOK = true;
	if (question.getType() == KBInfo.Question.TYPE_NUM) {
		if (field.value) {
			isOK = /^-?\d+([.,]\d+)?$/.test(field.value);
			if (isOK && question.getMin() && question.getMax()) {
				var num = Number(field.value);
				isOK = question.getMin() <= num && num <= question.getMax();
			}
		} else {
			// no error, but also not a valid input
			Element.removeClassName(field, "error");
			return false;
		}
	}
	if (isOK) {
		Element.removeClassName(field, "error");
	}
	else {
		Element.addClassName(field, "error");
	}
	return isOK;
}

