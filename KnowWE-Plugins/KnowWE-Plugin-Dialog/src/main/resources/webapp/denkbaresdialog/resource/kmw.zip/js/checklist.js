var CheckList = { };

CheckList.question = null;
CheckList.stepIndex = 0;

CheckList.renderAnswerArea = function (infoObject) {
	var isActive = infoObject.getValues().length == 0;
	if (isActive) {
		CheckList.question = infoObject;
		CheckList.stepIndex = 0;
	}
	
	var html = "";
	html += "<div id='check_list_"+infoObject.getID()+"'>";
	html += CheckList.renderTable(infoObject);
	html += "</div>\n";

	// show buttons only for unanswered questions
	if (isActive) {
		html += "<div id='buttons_to_remove'>";
		html += 
			LookAndFeel.renderSection2Elements(
				"<div class='big-button'>" +
				LookAndFeel.renderImage("../image/check.png", "25%", 48) +
				Translate.get("check_button_next") +
				"</div>",
				" CheckList.onSuccess(CheckList.question);",
				false
			);
	
		html += 
			LookAndFeel.renderSection2Elements(
				"<div class='big-button'>" +
				LookAndFeel.renderImage("../image/forbidden.png", "25%", 48) +
				Translate.get("check_button_fail") +
				"</div>",
				" CheckList.onFailed(CheckList.question);",
				false
			);
		html += "</div>";
	}
	
	return html;
};

CheckList.renderTable = function (infoObject) {
	var html = "";
	html += "<table id='check_table_"+infoObject.getID()+"' border=1><tr>";
	html += "<th>"+Translate.get("check_title_pruefling");
	html += "<th>"+Translate.get("check_title_durchfuehrung");
	html += "<th>"+Translate.get("check_title_kdt");
	html += "<th>"+Translate.get("check_title_rs");
	html += "<th>"+Translate.get("check_title_ls");
	html += "<th>"+Translate.get("check_title_mkf");
	html += "</tr>\n";
	
	var choices = infoObject.getChoices();
	for (var i=0; i<choices.length; i++) {
		var choice = choices[i];
		if (!CheckList.isCheckChoice(choice)) continue;
		html += CheckList.renderLine(infoObject, choice);
	}
	html += "</table>";
	return html;
};

CheckList.getLineClass = function(infoObject, choice) {
	var className = "checkTodo";
	if (CheckList.isChoiceFailed(infoObject, choice)) {
		className = "checkFailed";
	}
	else if (CheckList.isChoiceActive(infoObject, choice)) {
		className = "checkActive";
	}
	else if (CheckList.isChoiceDone(infoObject, choice)) {
		className = "checkDone";
	}
	return className;
};

CheckList.renderLine = function (infoObject, choice) {
	var className = CheckList.getLineClass(infoObject, choice);
	var cells = choice.text.split(";");
	var index = infoObject.getChoices().indexOf(choice);
	var html = "";
	html += "<tr id='check_row_"+infoObject.getID()+"_nr_"+index+"'>\n";
	html += "<td class="+className+">" + cells[0];
	html += "<td class="+className+">" + cells[1];
	for (var c=0; c<cells[cells.length-1].length; c++) {
		var charac = cells[cells.length-1].charAt(c);
		html += "<td class="+className+" >" + "<center>" + charac + "</center>";
	}	
	html += "</tr>\n";
	return html;
};

CheckList.redrawLine = function (infoObject, choiceIndex) {
//	var row = $("check_row_"+infoObject.getID()+"_nr_"+choiceIndex);
//	var choice = infoObject.getChoices()[choiceIndex];
//	var html = CheckList.renderLine(infoObject, choice);
//	row.outerHTML = html;
	
//	var div = $("check_list_"+infoObject.getID());
//	var html = CheckList.renderTable(infoObject);
//	div.innerHTML = html;
	
	var row = jq$("#check_row_"+infoObject.getID()+"_nr_"+choiceIndex)[0];
	var tds = row.getElementsByTagName("td");
	var choice = infoObject.getChoices()[choiceIndex];
	var className = CheckList.getLineClass(infoObject, choice);
	for (var i=0; i<tds.length; i++) tds[i].className = className;
};

CheckList.isCheckChoice = function (choice) {
	return choice && choice.text && choice.text.match(/.*;[xX-][xX-][xX-][xX-]\s*$/);
};

CheckList.onSuccess = function (infoObject) {
	CheckList.stepIndex++;
	CheckList.redrawLine(infoObject, CheckList.stepIndex-1);
	var choice = infoObject.getChoices()[CheckList.stepIndex];
	if (CheckList.isCheckChoice(choice)) {
		CheckList.redrawLine(infoObject, CheckList.stepIndex);
	}
	else {
		// if we reached the unknown choice, use it!
		jq$(".buttons_to_remove").remove();
		Interview.answer(infoObject.getID(), choice.id);
	}
};

CheckList.onFailed = function (infoObject) {
	var choice = infoObject.getChoices()[CheckList.stepIndex];
	infoObject.values.push(choice);
	Interview.answer(infoObject.getID(), choice.id);
	CheckList.redrawLine(infoObject, CheckList.stepIndex);
	jq$(".buttons_to_remove").remove();
};

CheckList.isChoiceDone = function (infoObject, choice) {
	// either is the question is already answered and choice is above failed one
	var choiceIndex = infoObject.getChoices().indexOf(choice);
	var values = infoObject.getValues();
	if (values.length > 0) {
		var answeredIndex = infoObject.getChoices().indexOf(values[0]);
		return choiceIndex < answeredIndex;
	}
	// or if the question is the current one and current index is higher than choice-index
	if (CheckList.question && CheckList.question.getID() == infoObject.getID()) {
		return choiceIndex < CheckList.stepIndex;
	}
	return false;
};

CheckList.isChoiceFailed = function (infoObject, choice) {
	// if question is answered and choice is failed one
	return infoObject.hasValue(choice);
};

CheckList.isChoiceActive = function (infoObject, choice) {
	// only if question is the current one and index is equal
	if (CheckList.question && CheckList.question.getID() == infoObject.getID()) {
		var choiceIndex = infoObject.getChoices().indexOf(choice);
		return choiceIndex == CheckList.stepIndex;
	}
	return false;
};

