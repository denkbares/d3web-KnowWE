var Interview = { };

Interview.startCase = function () {
	window.location = "/KnowWE/action/StartCase?KWikiWeb=default_web&lang="+Translate.getCurrentLanguage();
};

Interview.answer = function (id, value) {
	var qNode = jq$("#question_"+id)[0];
	var cNode = jq$("choice_"+id+"_"+value)[0];
	
	// mark this question as answered
	if (qNode) {
		Element.addClassName(qNode, "answered");
		LookAndFeel.deactivateAllSections(qNode);
	}
	if (cNode) {
		LookAndFeel.activateSection(cNode);
	}
	
	var infoObject = KBInfo.getInfoObject[id];
	var url = "/KnowWE/action/SetAnswer/"+(new Date().getTime())+"?"+id+"="+value;
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
			Interview.displayNextQuestion();
		},
		onFailure: function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
};

Interview.displayNextQuestion = function (refreshAll) {
	var url = "/KnowWE/action/GetInterview/"+(new Date().getTime());
	new Ajax.Request(url, {
		method: 'get',
		parameters: {
			lang: Translate.getCurrentLanguage(),
			history: refreshAll ? "true" : "false"
		},
		onSuccess: function(transport) {
			var xml = transport.responseXML;
			// render questions of response
			var his = xml.getElementsByTagName("history");
			var ask = xml.getElementsByTagName("questionnaire");
			var sol = xml.getElementsByTagName("solutions");
			// get all questions before inserting the new one
			var questions = Element.select(jq$("#interview")[0], ".question");
			var node = null;
			if (refreshAll && his && his[0]) {
				jq$("#interview")[0].innerHTML = "";
				var infoObjects = KBInfo.parseInfoObjects(his[0]);
				for (var i=0; i<infoObjects.length; i++) {
					var html = Interview.renderQuestion(infoObjects[i]);
					LookAndFeel.appendChildDIV("interview", html);
				}
			}
			if (ask && ask[0]) {
				// insert new question
				var infoObjects = KBInfo.parseInfoObjects(ask[0]);
				for (var i=0; i<infoObjects.length; i++) {
					var html = Interview.renderQuestion(infoObjects[i]);
					node = LookAndFeel.appendChildDIV("interview", html);
				}
				// and remove all previously unanswered questions
				for (var i=0; i<questions.length; i++) {
					var question = questions[i];
					if (!Element.hasClassName(question, "answered")) {
						LookAndFeel.removeChildDIV(question);
					}
				}
				// remove solutions if we have added a new (unanswered) question
				if (node) {
					jq$("#solutions")[0].innerHTML = "";
				}
			}
			// render solutions of response, if there are no questions!
			if (sol && sol[0] && !node) {
				var infoObjects = KBInfo.parseInfoObjects(sol[0]);
				jq$("#solutions")[0].innerHTML = LookAndFeel.renderSolutionSeparator();
				for (var i=0; i<infoObjects.length; i++) {
					// hack: ignore exported flowchart context solutions
					if (infoObjects[i].getName().indexOf("_KONTEXT_") >= 0) continue;
					var html = Interview.renderSolution(infoObjects[i]);
					node = LookAndFeel.appendChildDIV("solutions", html);
				}
			}
			// if no solution found, but end of interview reached,
			// add a no-solution message to the solutions-section
			if (!node) {
				jq$("#solutions")[0].innerHTML = LookAndFeel.renderSolutionSeparator();
				var html = Interview.renderNoSolution();
				node = LookAndFeel.appendChildDIV("solutions", html);
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
		onFailure: function() {
			CCMessage.warn('AJAX Verbindungs-Fehler', "");
		},
		onException: function(transport, exception) {
			CCMessage.warn('AJAX interner Fehler', exception);
		}
	}); 
};

Interview._isImageLink = function (link) {
	if (!link) return false;
	link = link.toLowerCase();
	var result =
		link.endsWith(".gif") ||
		link.endsWith(".png") ||
		link.endsWith(".jpg") ||
		link.endsWith(".jpeg") ||
		link.endsWith(".tif") ||
		link.endsWith(".tiff") ||
		link.endsWith(".bmp");
	return result;
};

Interview.renderSolution = function (infoObject) {
	var html = "<div class=solution>\n";
	
	// heading: solution name
	html += "\t<h3>";
	html += (infoObject.getText()) ? infoObject.getText() : infoObject.getName();
	html += "</h3>\n";
	
	// support knowledge
	var info = infoObject.getInfo();
	var multimedia = infoObject.getMultimedia();
	var link = infoObject.getLink();
	if (info || multimedia) html += "<p>";
	if (multimedia) {
		link = "../../../Multimedia/"+multimedia;
	}
	var isImage = link && link.match(/^.*\.(gif|jpg|jpeg|png|svg|tiff|tif|bmp)$/i); 
	if (link && isImage) {
		html += LookAndFeel.renderImage(link, false, 80);
	}
	if (info) {
		html += info + "\n";
	}
	if (info || multimedia) html += "</p>";	

	if (link && !isImage) {
		html += "<p><a target=ExternalLink href='" + link + "'>" + link + "</a></p>";
	}
	
	html += "\n</div>\n";
	return LookAndFeel.renderBox(html);
};

Interview.renderNoSolution = function () {
	var html = "<div class=solution>\n";
	html += "\t<h3>";
	html += Translate.get("no_result_title");
	html += "</h3>\n";
	html += "\n<p>";
	html += Translate.get("no_result_text");
	html += "</p>";
	html += "\n</div>\n";
	return LookAndFeel.renderBox(html);
};

Interview.renderQuestion = function (infoObject) {
	var className = "question";
	if (infoObject.getValues() && infoObject.getValues().length > 0) {
		className += " answered";
	}
	var html = "<div id='question_" + infoObject.getID() + "' class='"+className+"'>\n";
	
	// heading: ask question
	html += "\t<h3>";
	html += (infoObject.getText()) ? infoObject.getText() : infoObject.getName();
	html += "</h3>\n";
	
	// support knowledge
	var info = infoObject.getInfo();
	var multimedia = infoObject.getMultimedia();
	var link = infoObject.getLink();
	if (info || multimedia) html += "<p>";
	if (multimedia) {
		link = "../../../Multimedia/"+multimedia;
	}
	var isImage = link && link.match(/^.*\.(gif|jpg|jpeg|png|svg|tiff|tif|bmp)$/i); 
	if (link && isImage) {
		html += LookAndFeel.renderImage(link, false, 80);
	}
	if (info) {
		html += info + "\n";
	}
	if (info || multimedia) html += "</p>";	

	if (link && !isImage) {
		html += "<p><a target=ExternalLink href='" + link + "'>" + link + "</a></p>";
	}
	
	var choices = infoObject.getChoices();
	// content: checklist question: render different user interface
	if (choices && choices.length >= 2 && CheckList.isCheckChoice(choices[0])) {
		html += CheckList.renderAnswerArea(infoObject);
	}
	// content: answer choice question
	else {
		html += "<p style='clear:both;'>";
		for (var i=0; i<choices.length; i++) {
			var image = null;
			var text = choices[i].text ? choices[i].text : Translate.get("use_question_unknown");
			var link = choices[i].link;
			var ltext = text.toLowerCase();
			if (ltext == "weiter" || ltext == "verder" || ltext == "ok" || ltext == "ja" || ltext == "yes") image = "../image/check.png";
			if (ltext == "nein" || ltext == "no" || ltext == "nee") image = "../image/forbidden.png";
			if (Interview._isImageLink(link)) image = link;
			var active = infoObject.hasValue(choices[i]);
			var heightStyle = "";
			if (choices.length <= 5) {
				heightStyle = " class='big-button' ";
			}
			if (choices.length == 2) {
				html += LookAndFeel.renderSection2Elements(
				"<div id='choice_"+infoObject.getID()+"_"+choices[i].id+"'"+heightStyle+">" +
				LookAndFeel.renderImage(image, "25%", 48) +
				text +
				"</div>",
				" Interview.answer(\""+infoObject.getID()+"\", \""+choices[i].id+"\", this);",
				active
			);
			} else {
				html += LookAndFeel.renderSection(
				"<div id='choice_"+infoObject.getID()+"_"+choices[i].id+"'"+heightStyle+">" +
				LookAndFeel.renderImage(image, "25%", 48) +
				text +
				"</div>",
				" Interview.answer(\""+infoObject.getID()+"\", \""+choices[i].id+"\", this);",
				active
			);
			}
		}
		html += "</p>";
	}
	
	// content: answer textual questions (text, num, ...)
	if (infoObject.getType() == KBInfo.Question.TYPE_NUM 
			|| infoObject.getType() == KBInfo.Question.TYPE_TEXT) {
		html += "<p><input type=text";
		if (infoObject.getValues() && infoObject.getValues()[0]) {
			html += " value='"+String.escapeQuotes(infoObject.getValues()[0])+"'";
		}
		html += " onchange='Interview._typedAnswerChanged(\""+infoObject.getID()+"\", this);'";
		html += " onkeyup='Interview._typedAnswerAction(\""+infoObject.getID()+"\", this, event);'";
		html += "></input>";
		var min = infoObject.getMin();
		var max = infoObject.getMax();
		html += (min && max) ? "&nbsp;" + min + "&nbsp;..&nbsp;"  + max : "";
		var unit = infoObject.getUnit();
		html += unit ? "&nbsp;"+unit : "";
		html += "</p>";
	}
	
	html += "\n</div>\n";
	return LookAndFeel.renderBox(html);
};

Interview._typedAnswerAction = function (id, field, event) {
	if (!event) event = window.event;
	var code = (event.which) ? event.which : event.keyCode;
	var valid = Interview._checkValidInput(id, field);
	if (code == 13 && valid) {
		// blur automaticall answers the question 
		// due to registered event handler
		field.blur();
	}
};

Interview._typedAnswerChanged = function (id, field) {
	if (!Interview._checkValidInput(id, field)) return;
	var value = field.value;
	Interview.answer(id, value, field);
};

Interview._checkValidInput = function (id, field) {
	var question = KBInfo.getInfoObject(id);
	var isOK = true;
	if (question.getType() == KBInfo.Question.TYPE_NUM) {
		if (field.value) {
			isOK = /^\d+([.,]\d+)?$/g.test(field.value);
			if (isOK && question.getMin() && question.getMax()) {
				var num = Number(field.value);
				isOK = question.getMin() <= num && num <= question.getMax();
		}
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

