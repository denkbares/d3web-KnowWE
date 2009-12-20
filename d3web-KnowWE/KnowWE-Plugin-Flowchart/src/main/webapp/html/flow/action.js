
/**
 * Action ist zur Behandlung einer beliebigen Regelaktion in allen bekannten
 * markups. Aktuell ist als markup 'KnOffice' und 'NOP' unterstuetzt.
 * 
 * Die Action dient nur zum Parsen der Aktion. Es werden keine Interpretationen 
 * gegen die Sematik vorgenommen (z.B. "ist der indizierte Text ueberhaupt eine 
 * Frage" oder "existiert das Zielobjekt ueberhaupt").
 */
function Action(markup, expression) {
	this.markup = markup;
	this.expression = expression;
	this.error = null;
	this.valueString = this._extractValueString(expression);
	this.infoObjectName = this._extractInfoObjectName(expression);
}

Action.prototype.setValueString = function(newValueString) {
	this.valueString = newValueString;
	this.expression = Action._createExpression(this.infoObjectName, this.valueString);
	this.error = null; // check semantics on first access of getError()
}

Action.prototype.getExpression = function() {
	return this.expression;
}

Action.prototype.getDisplayText = function() {
	if (this.markup == 'NOP')		return '---';
	if (this.valueString == "ERFRAGE")	return 'fragen';
	if (this.valueString == "INSTANT")	return 'immer fragen';
	if (this.valueString == "P7")		return 'etabliert';
	if (this.valueString == "N7")		return 'ausgeschlossen';
	if (this.valueString == "YES")		return 'ja';
	if (this.valueString == "NO")		return 'nein';
	if (this.valueString == "()")		return 'Formel: f(x)='; // nur leere Formel so anzeigen
	return this.valueString;
}

Action.prototype.getInfoObjectName = function() {
	return this.infoObjectName;
}

Action.prototype.getValueString = function() {
	return this.valueString;
}

Action.prototype.getMarkup = function() {
	return this.markup;
}

Action.prototype.getError = function() {
	this._checkSemantic();
	return this.error;
}

Action.prototype.isIndication = function() {
	return this.markup == 'KnOffice' && 
		(this.valueString == 'ERFRAGE' || this.valueString == 'INSTANT');
}

Action.prototype.isAssignment = function() {
	return this.markup == 'KnOffice' && this.valueString != 'ERFRAGE' && this.valueString != 'INSTANT';
}

Action.prototype.isFormula = function() {
	return this.markup == 'KnOffice' && this.valueString && this.valueString.startsWith('(') && this.valueString.endsWith(')');
}

Action.prototype.isDecision = function() {
	return this.markup == 'NOP';
}



Action._createExpression = function(name, value) {
	if (value.startsWith('(') && value.endsWith(')')) {
		return '"' + name.escapeQuote() + '" = ' + value;
	}
	else {
		return '"' + name.escapeQuote() + '" = "' + value.escapeQuote() + '"';
	}
}

Action.prototype._extractInfoObjectName = function(string) {
	if (this.markup == 'NOP') {
		var result = /^\s*"(.*)"\s*$/.exec(string);
		if (result) {
			return result[1];
		}
		else {
			return string;
		}
	}
	
	// first try with quotes
	var nameExpr = /^\s*(\w+)\s*=/i;
	var result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];
	
	// second try without quotes
	nameExpr = /^\s*"(.+)"\s*=/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];

	// third try 'KEYWORD' '[' <name> ']'
//	nameExpr = /^\s*[\w!]+\["?([^"]+)"?\]\s*$/i;
//	result = nameExpr.exec(string);
//	if (result && result.length > 1 && result[1]) return result[1];
	
	// third try 'KEYWORD' '[' <name> ']'
	nameExpr = /^\s*([\w!]+)\[([^\(\)]+)\]\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 2 && result[2]) return result[2];
	
	// 'KEYWORD' '[' <name> '(' <value> ')' ']'
	nameExpr = /^\s*([\w!]+)\[(.+)\((.+)\)\]\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 3 && result[2]) return result[2];
	
	// last try only ["] <name> ["]
	nameExpr = /^\s*"?(.*)"?\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];	

	this.error = "Der Name des Zielobjektes kann nicht identifiziert werden.";
	return null;
}

Action.prototype._extractValueString = function(string) {
	if (this.markup == 'NOP') {
		return "";
	}
	
	// first try without quotes
	var nameExpr = /=\s*(\w+)\s*$/i;
	var result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];
	
	// second try with quotes
	nameExpr = /=\s*"(.+)"\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];

	// second.II try with brackets
	nameExpr = /=\s*(\(.*\))\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];

	// third try 'KEYWORD' '[' <name> ']'
	nameExpr = /^\s*([\w!]+)\[([^\(\)]+)\]\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 2 && result[1]) return result[1];
	
	// 'KEYWORD' '[' <name> '(' <value> ')' ']'
//	nameExpr = /^\s*([\w!]+)\[(\w+)\((\w+)\)\]\s*$/i;
	nameExpr = /^\s*([\w!]+)\[(.+)\((.+)\)\]\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 3 && result[3]) return result[3];
	
	
//	nameExpr = /^\s*"?(.*)"?\s*$/i;
//	result = nameExpr.exec(string);
//	if (result && result.length > 1 && result[1]) return 'ERFRAGE'; // we do have an implicit value	

	this.error = "Die Aktion kann nicht identifiziert werden.";
	return null;
}

/**
 * Checks the semantic of the action against the KBInfo object.
 * If the KBInfo object is not available in the cache, the
 * semantic is not fully checked.
 */
Action.prototype._checkSemantic = function() {
	if (this.error) return;
	var infoObject = KBInfo.lookupInfoObject(this.getInfoObjectName());
	if (!infoObject) return;
	var clazz = infoObject.getClassInstance();
	if (this.isIndication()) {
		if (clazz == KBInfo.QSet) return;
		if (clazz == KBInfo.Question && !infoObject.isAbstract()) return;
		this.error = 'Dieser Objekttyp kann nicht erfragt werden';
	}
	else if (this.valueString == '()') {
		this.error = 'Es wurde noch keine Formel eingegeben';
	}
	else if (clazz == KBInfo.Solution) {
		if (this.valueString && this.valueString.search(/^[NP][1234567]$/i) >= 0) return;
		this.error = '"'+this.valueString+'" ist kein gueltiger Wert fuer eine Loesung';
	}
	else if (clazz == KBInfo.Question) {
		switch (infoObject.getType()) {
			case KBInfo.Question.TYPE_BOOL:
				// for now we receive also choices for boolean questions 
				// from the server so treat them similar to oc questions
				//if (this.valueString == "YES" || this.valueString == "NO" || this.isFormula()) break;
				//this.error = '"'+this.valueString+'" ist kein erlaubter Wert fuer eine Ja/Nein-Frage';
				//break;
			case KBInfo.Question.TYPE_OC:
			case KBInfo.Question.TYPE_MC:
				if (infoObject.getOptions().indexOf(this.valueString) >= 0) break;
				this.error = '"'+this.valueString+'" ist keine Antwortalternative dieser Frage';
				break;
			case KBInfo.Question.TYPE_NUM:
			case KBInfo.Question.TYPE_DATE:
			case KBInfo.Question.TYPE_TEXT:
				if (this.isFormula()) break;
				this.error = 'Nur Formeln als Wert fuer diese Frage zugelassen';
				break;
		}
	}
	else if (clazz == KBInfo.Flowchart) {
		if (infoObject.getStartNames().indexOf(this.valueString) >= 0) return;
		this.error = '"'+this.valueString+'" ist kein Startknoten dieses Flussdiagramms';
	}
}


Action.createPossibleActions = function(infoObject) {
	if (!infoObject) return null;
	var name = infoObject.getName();
	var result = [];
	if (infoObject.getClassInstance() == KBInfo.Question) {
		if (!infoObject.isAbstract()) {
			result.push('---- Frage stellen ----');			
			// questions can be asked
			//removed conversion to json-string @20091102
			result.push(new Action('KnOffice', 'ERFRAGE['+name+']'));
			result.push(new Action('KnOffice', 'INSTANT['+name+']'));
		}
		result.push('---- Wert abfragen ----');			
		result.push(new Action('NOP', '"'+name.escapeQuote()+'"'));			
		result.push('---- Wert zuweisen ----');			
		switch (infoObject.getType()) {
			//add yes/no value
			case KBInfo.Question.TYPE_BOOL:
				// for now we receive also choices for boolean questions 
				// from the server so treat them similar to oc questions
				//result.push(new Action('KnOffice', Action._createExpression(name, 'YES')));
				//result.push(new Action('KnOffice', Action._createExpression(name, 'NO')));
				//break;
			//add choices of oc/mc value
			case KBInfo.Question.TYPE_OC:
			case KBInfo.Question.TYPE_MC:
				var options = infoObject.getOptions();
				for (var i=0; i<options.length; i++) {
					result.push(new Action('KnOffice', Action._createExpression(name, options[i])));
				}
				break;
			// currently add no options for other values
			case KBInfo.Question.TYPE_NUM:
			case KBInfo.Question.TYPE_DATE:
			case KBInfo.Question.TYPE_TEXT:
				// no choices possible, use edit field instead!
				result.push(new Action('KnOffice', Action._createExpression(name, '()')));
				break;
		}
	}
	else if (infoObject.getClassInstance() == KBInfo.Solution) {
		result.push('---- Loesung bewerten ----');			
		result.push(new Action('KnOffice', Action._createExpression(name, 'P7')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'P6')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'P5')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'P4')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'P3')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'P2')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'P1')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'N1')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'N2')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'N3')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'N4')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'N5')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'N6')));
		result.push(new Action('KnOffice', Action._createExpression(name, 'N7')));
	}
	else if (infoObject.getClassInstance() == KBInfo.Flowchart) {
		result.push('---- Startknoten aufrufen ----');			
		var options = infoObject.getStartNames();
		for (var i=0; i<options.length; i++) {
//			result.push(new Action('KnOffice', Action._createExpression(name, options[i])));
			
			result.push(new Action('KnOffice', 'CALL[' + name + '(' + options[i] + ')' + ']'));
		}
	}
	else if (infoObject.getClassInstance() == KBInfo.QSet) {
		result.push('---- Fragebogen stellen ----');			
		//removed conversion to json-string @20091102
		result.push(new Action('KnOffice', 'ERFRAGE['+name+']'));
		result.push(new Action('KnOffice', 'INSTANT['+name+']'));
	}
	
	return result;
}



/**
 * ActionEditor is a class capable to provide an editor for actions.
 * 
 * It works on the Action class as a model and can produce an
 * edited Action every time as an result.
 *
 * It uses the KNInfo cache library to access information on the 
 * knowledgebase objects.
 */

function ActionEditor(parent, action, style) {
	this.parent = $(parent);
	this.style = style;
	this.selectedAction = action;

	this.dom = null;
	this.objectSelect = null;
	this.selectableActions = null;
	this.setVisible(true);
	
	this.answers = [];
}

ActionEditor.prototype.getDOM = function() {
	return this.dom;
}

ActionEditor.prototype.isVisible = function() {
	return (this.dom != null);
}

ActionEditor.prototype.getNodeModel = function() {
	return this.nodeModel;
}

ActionEditor.prototype.getAction = function() {
	this.handleValueSelected();
	if (this.selectedAction && this.selectedAction.isFormula()) {
		this.selectedAction.setValueString('('+this.dom.select('.input')[0].value+')');
	}
	return this.selectedAction;
}

ActionEditor.prototype.getAnswers = function() {
	return this.answers;
}

ActionEditor.prototype.handleObjectSelected = function() {
	//alert("object changed");
	this.refreshValueInput();
}

ActionEditor.prototype.handleValueSelected = function() {
	if (!this.isVisible()) return;
	var selects = this.dom.select('.value select');
	if (selects.length == 0) return;
	var value = selects[0].options[selects[0].options.selectedIndex].value;
	this.selectedAction = this.selectableActions[value];
	this.updateInputField();
}

ActionEditor.prototype.updateInputField = function() {
	var input = this.dom.select('.input')[0];
	if (this.selectedAction && this.selectedAction.isFormula()) {
		input.show();
		input.focus();
		input.select();
	}
	else {
		input.hide();
	}
}

ActionEditor.prototype.focus = function() {
	if (this.objectSelect) {
		this.objectSelect.focus();
	}
}

ActionEditor.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.parent.appendChild(this.dom);
		this.objectSelect = new ObjectSelect(
			this.dom.select('.object')[0], 
			[KBInfo.QSet, KBInfo.Question, KBInfo.Solution, KBInfo.Flowchart], 
			this.selectedAction ? this.selectedAction.getInfoObjectName() : '',
			function() {this.handleObjectSelected();}.bind(this));
		this.refreshValueInput();
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		this.objectSelect.destroy();
		this.objectSelect = null;
		this.parent.removeChild(this.dom);
		this.dom = null;
	}
}

ActionEditor.prototype.render = function() {
	var formula = (this.selectedAction && this.selectedAction.isFormula()) ? this.selectedAction.getValueString() : '()';
	formula = formula.substring(1, formula.length-1);
	var dom = Builder.node('div', {
		className: 'ActionEditor',
		style: this.style ? this.style : ''
	}, 
	[
		Builder.node('div', {className: 'object'}),	// ObjectSelect parent
		Builder.node('div', {className: 'value'}),	// value dropdown parent
		Builder.node('input', {
			className: 'input', type: 'text', value: formula, 
			style: 'display: none;'
		})
	]);
	dom.__ActionEditor = this;
	return dom;
}

//creates the Dropdown Menu, with the answer values
ActionEditor.createQuestionDropdown = function(addedQuestionText, addedQuestionType, possibleAnswers) {
	var name = addedQuestionText;	
	var result = [];
	
	result.push('---- Frage stellen ----');			
	// questions can be asked
	result.push(new Action('KnOffice', 'ERFRAGE['+name.toJSON()+']'));
	result.push(new Action('KnOffice', 'INSTANT['+name.toJSON()+']'));

	result.push('---- Wert abfragen ----');			
	result.push(new Action('NOP', '"'+name.escapeQuote()+'"'));			
	result.push('---- Wert zuweisen ----');			
	switch (addedQuestionType) {
			// add yes/no value
		case 'yn':
			result.push(new Action('KnOffice', Action._createExpression(name, 'yes')));
			result.push(new Action('KnOffice', Action._createExpression(name, 'no')));
			break;
			

		case 'oc':
		case 'mc':
		case 'num':
			for (var i = 0; i < possibleAnswers.length; i++) {
				result.push(new Action('KnOffice', Action._createExpression(name, possibleAnswers[i])));
			}
			break;
	}	
	return result;
}

// AJAX-Request for adding questions and their answers to the article
ActionEditor.updateQuestions = function(addedQuestionText, addedQuestionType, possibleAnswers) {

		var kdomID = window.location.search.substring(window.location.search.indexOf('kdomID=') + 7, window.location.search.indexOf('&'));
		var pageName = window.location.search.substring(window.location.search.indexOf('Wiki_Topic=') + 11);
		var answersToLine = "";
		for (var i = 0; i < possibleAnswers.length; i++) {
			if (i != possibleAnswers.length -1) {
				answersToLine += possibleAnswers[i] + ',';
			} else {
				answersToLine += possibleAnswers[i];
			}
		}
		var infos = '&infos=' + addedQuestionText + ',' + addedQuestionType + ',' + pageName + ',' + answersToLine;

		
		var url = "KnowCC.jsp?action=de.d3web.we.flow.kbinfo.UpdateQuestions" + infos;
		
		new Ajax.Request(url, {
			method: 'get',
			onSuccess: function(transport) {
			
			},
			onFailure: function() {
				CCMessage.warn(
					'AJAX Verbindungs-Fehler', 
					'Eventuell werden einige Objekte anderer Wiki-Seiten nicht korrekt angezeigt. ' +
					'In sp?teren Aktionen k?nnte auch das Speichern der ?nderungen fehlschlagen.');
			},
			onException: function(transport, exception) {
				CCMessage.warn(
					'AJAX interner Fehler',
					exception
					);
			}
		}); 
}

// creates the Dropwdown menu for solutions
ActionEditor.createSolutionDropdown = function(solutionText) {
	var name = solutionText;	
	var result = [];
	
	result.push('---- Loesung bewerten ----');			
	result.push(new Action('KnOffice', Action._createExpression(name, 'P7')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'P6')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'P5')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'P4')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'P3')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'P2')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'P1')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'N1')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'N2')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'N3')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'N4')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'N5')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'N6')));
	result.push(new Action('KnOffice', Action._createExpression(name, 'N7')));
	
	return result;
}


//AJAX-Request for adding solutions to the article
ActionEditor.updateSolutions = function(solutionText) {

	var kdomID = window.location.search.substring(window.location.search.indexOf('kdomID=') + 7, window.location.search.indexOf('&'));
	var pageName = window.location.search.substring(window.location.search.indexOf('Wiki_Topic=') + 11);
	var infos = '&infos=' + solutionText + ',' + pageName;

	var url = "KnowCC.jsp?action=de.d3web.we.flow.kbinfo.UpdateSolutions" + infos;
	
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
		
		},
		onFailure: function() {
			CCMessage.warn(
				'AJAX Verbindungs-Fehler', 
				'Eventuell werden einige Objekte anderer Wiki-Seiten nicht korrekt angezeigt. ' +
				'In sp?teren Aktionen k?nnte auch das Speichern der ?nderungen fehlschlagen.');
		},
		onException: function(transport, exception) {
			CCMessage.warn(
				'AJAX interner Fehler',
				exception
				);
		}
	}); 
}


ActionEditor.getQuestionType = function () {
	var qType;
	if (document.choseQuestionType.questionType[0].checked) {
		qType = document.choseQuestionType.questionType[0].value;
	} else if (document.choseQuestionType.questionType[1].checked) {
		qType = document.choseQuestionType.questionType[1].value;
	} else if (document.choseQuestionType.questionType[2].checked) {
		qType = document.choseQuestionType.questionType[2].value;
	} else if (document.choseQuestionType.questionType[3].checked) {
		qType = document.choseQuestionType.questionType[3].value;
	} else if (document.choseQuestionType.questionType[4].checked) {
		qType = document.choseQuestionType.questionType[4].value;
	} else if (document.choseQuestionType.questionType[5].checked) {
		qType = document.choseQuestionType.questionType[5].value;
	}
	return qType;
}

ActionEditor.addSubFlow = function(exitNodes) {

	var kdomID = window.location.search.substring(window.location.search.indexOf('kdomID=') + 7, window.location.search.indexOf('&'));
	var pageName = window.location.search.substring(window.location.search.indexOf('Wiki_Topic=') + 11);
	var name = document.choseQuestionText.questionText.value;
	
	var nodesToLine='';
	for (var i = 0; i < exitNodes.length; i++)  {
		nodesToLine += exitNodes[i];
		
		if (i != exitNodes.length -1) {
			nodesToLine += ',';
		} 
	}
	
	var infos = '&infos=' + pageName + ',' + name +  ',' + nodesToLine;

	var url = "KnowCC.jsp?action=de.d3web.we.flow.kbinfo.AddSubFlowchart" + infos;
	
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
		
		},
		onFailure: function() {
			CCMessage.warn(
				'AJAX Verbindungs-Fehler', 
				'Eventuell werden einige Objekte anderer Wiki-Seiten nicht korrekt angezeigt. ' +
				'In sp?teren Aktionen k?nnte auch das Speichern der ?nderungen fehlschlagen.');
		},
		onException: function(transport, exception) {
			CCMessage.warn(
				'AJAX interner Fehler',
				exception
				);
		}
	}); 
}

ActionEditor.prototype.createNewFlowchart = function() {
	var root = this.dom.select('.value')[0];
	var qText = document.choseQuestionText.questionText.value;
	var exitNodes = this.getAnswers();
	this.nodeModel = 'CALL[' + qText + '(Start' + qText + ')]';
	ActionEditor.addSubFlow(exitNodes);
}

// puts everything together, e.g. the question, the type and the answers
// also calls the AJAX request
ActionEditor.prototype.createNewQuestion = function() {
	var root = this.dom.select('.value')[0];
	var qText = document.choseQuestionText.questionText.value;
	var qType = ActionEditor.getQuestionType();
	var answers = this.getAnswers();
	var actions;
	
	// if the object is a question, do the right ajax-request and create the dropdown
	if (qType != "Solution") {
		ActionEditor.updateQuestions(qText, qType, answers);
		actions = ActionEditor.createQuestionDropdown(qText, qType, answers);
	
	// else it is a solution, do the right ajax-request and create the dropdown
	} else {
		ActionEditor.updateSolutions(qText);
		actions = ActionEditor.createSolutionDropdown(qText);
	}
	
	this.selectableActions = [];
	var valueToSelect = this.selectedAction ? this.selectedAction.getValueString() : '';
	var indexToSelect = -1; // select first one if no match available
	var isFormula = this.selectedAction ? this.selectedAction.isFormula() : false;
	this.selectedAction = null;

	if (!actions) {
		root.innerHTML = '<i>Keine Aktionen verfuegbar</i>';
		if (this.objectSelect) this.selectedAction = new Action('NOP', this.objectSelect.getValue());
		return;
	}

	for (var i=0; i<actions.length; i++) {
		var action = actions[i];
		if (!Object.isString(action)) {
			this.selectableActions.push(action);
			// ein Wert wird dann selektiert wenn der Wert gleich ist 
			// oder beides eine Formel ist (da dann der Wert in der Drop-Down-Liste leer ist)
			// Ausserdem wird die erste Action gemerkt, da eventuell keine passende mehr nachfolgt
			if ((indexToSelect == -1) 
					|| (action.getValueString() == valueToSelect)
					|| (action.getValueString() == '()' && isFormula)
					) {
				indexToSelect = i;
				// bei Formeln der Wert uebernehmen
				if (isFormula) {
					this.selectedAction = Object.clone(action);
					this.selectedAction.setValueString(valueToSelect);
				}
				else {
					this.selectedAction = action;
				}
			}
		}
	}

	var html = '<select ' +
			'onchange="this.parentNode.parentNode.__ActionEditor.handleValueSelected(this);this.blur();" ' +
			'onfocus="this.parentNode.parentNode.__ActionEditor.hasInputFocus = true;" ' +
			'onblur="this.parentNode.parentNode.__ActionEditor.hasInputFocus = false;">';
	for (var i=0, value=0; i<actions.length; i++) {
		var action = actions[i];
		if (Object.isString(action)) {
			html +=  '<optgroup label="'+action+'"></optgroup>';
		}
		else {
			html += '<option value=' + (value++) + 
				(indexToSelect == i ? ' selected' : '') + 
				'>' +
				action.getDisplayText() + 
				'</option>';
		}
	}
	html += '</select>';
	root.innerHTML = html + '<br \>';
	this.updateInputField();
}

// add additional answers to a question
ActionEditor.prototype.addAnswer = function() {
	var root = this.dom.select('.value')[0];
	var questionText = document.choseQuestionText.questionText.value;
	var questionType = ActionEditor.getQuestionType();
	
	// if its a yn question or a solution there is no need for custom answers
	if (questionType === "yn" || questionType === "num" || questionType ===  "Solution") {
		return this.createNewQuestion();
		
	// if a new Flowchart is to be created
	} else if (questionType === "newFlowchart") {
		var newAnswer = '<input type="text" size="30" name="answer"><br \>';
		var buttonWeiter = '<input type="button" value="Weiter" onclick="return this.parentNode.parentNode.parentNode.__ActionEditor.createNewFlowchart()"';
		var buttonOK = '<input type="button" value="Hinzufügen" onclick="return this.parentNode.parentNode.parentNode.__ActionEditor.answerValue()"';
		root.innerHTML = '<i>Exit-Knoten für</i><br\>' + questionText + '<br\><i>hinzufügen</i><br\><form name="addAnswer" id="addAnswer" method="get">' + newAnswer + buttonOK + buttonWeiter + '</form><i>bisherige Exit-Knoten:</i>';
		
	// else if its a question with custom answers
	} else {
		var newAnswer = '<input type="text" size="30" name="answer"><br \>';
		var buttonWeiter = '<input type="button" value="Weiter" onclick="return this.parentNode.parentNode.parentNode.__ActionEditor.createNewQuestion()"';
		var buttonOK = '<input type="button" value="Hinzufügen" onclick="return this.parentNode.parentNode.parentNode.__ActionEditor.answerValue()"';
		root.innerHTML = '<i>Lösungsvorschläge für</i><br\>' + questionText + ' [' + questionType + ']<br\><i>hinzufügen</i><br\><form name="addAnswer" id="addAnswer" method="get">' + newAnswer + buttonOK + buttonWeiter + '</form><i>bisherige Vorschläge:</i>';
	}
}




// TODO filter empty answers
ActionEditor.prototype.answerValue = function() {
	var root = this.dom.select('.value')[0];
	var answer = document.addAnswer.answer.value;
	if (!this.answers.contains(answer) && (answer !== '\s*')) {
		root.innerHTML += '<br \>' + answer;
		this.answers.push(answer);
	}
}
 

// asks the object type
ActionEditor.prototype.askQuestionType = function() {
	var root = this.dom.select('.value')[0];
	var questionText = document.choseQuestionText.questionText.value;
	var multipleChoice = '<input type="radio" id="mc" name="questionType" value="mc">Question [mc]<br>';
	var oneChoice = '<input type="radio" id="oc" name="questionType" value="oc">Question [oc]<br>';
	var yn = '<input type="radio" id="yn" name="questionType" value="yn">Question [yn]<br>';
	var num = '<input type="radio" id="num" name="questionType" value="num">Question [num]<br>';
	var solution = '<input type="radio" id="Solution" name="questionType" value="Solution">Solution<br>';
	var newFlowchart = '<input type="radio" id="newFlowchart" name="questionType" value="newFlowchart">new Flowchart<br>';
	var buttonOK = '<input type="button" value="Erstellen" onclick="return this.parentNode.parentNode.parentNode.__ActionEditor.addAnswer()"';
	root.innerHTML = '<i>Bitte Objekttyp auswählen für</i><br\>' + questionText
	+ '<br\><form name="choseQuestionType" id="choseQuestionType" method="get">' + multipleChoice + oneChoice + yn  + num + solution + newFlowchart + "<br\>" + buttonOK + "</form>";
	return;
}

ActionEditor.prototype.refreshValueInput = function() {
	if (!this.isVisible()) return;
	// remove existing input method
	var root = this.dom.select('.value')[0];
	
	// check if object is in cache
	var infoObject = (this.objectSelect) ? this.objectSelect.getMatchedItem() : null;
	if (!infoObject) {
		var buttonOK = '<button class="ok" onclick="this.parentNode.parentNode.__ActionEditor.askQuestionType()">Ok</button>';
		var question = '<form name="choseQuestionText" id="choseQuestionText" method="get"><input type="text" id="questionText" name="questionText"></form>';
		root.innerHTML = "<i>Objekt nicht bekannt<br\>Objekt erstellen?<br\></i>" + question + "<br\>" + buttonOK;
		if (this.objectSelect) this.selectedAction = new Action('NOP', this.objectSelect.getValue());
		return;
	}


	// iterate all actions	
	// create drop down list for existing info object
	// also look for a similar action to the current one
	// if no such exists, select the first one
	// if there are no selections possible (e.g. object does not exsist)
	// create empty Action
	var actions = Action.createPossibleActions(infoObject);
	this.selectableActions = [];
	var valueToSelect = this.selectedAction ? this.selectedAction.getValueString() : '';
	var indexToSelect = -1; // select first one if no match available
	var isFormula = this.selectedAction ? this.selectedAction.isFormula() : false;
	this.selectedAction = null;

	if (!actions) {
		root.innerHTML = '<i>Keine Aktionen verfuegbar</i>';
		if (this.objectSelect) this.selectedAction = new Action('NOP', this.objectSelect.getValue());
		return;
	}

	for (var i=0; i<actions.length; i++) {
		var action = actions[i];
		if (!Object.isString(action)) {
			this.selectableActions.push(action);
			// ein Wert wird dann selektiert wenn der Wert gleich ist 
			// oder beides eine Formel ist (da dann der Wert in der Drop-Down-Liste leer ist)
			// Ausserdem wird die erste Action gemerkt, da eventuell keine passende mehr nachfolgt
			if ((indexToSelect == -1) 
					|| (action.getValueString() == valueToSelect)
					|| (action.getValueString() == '()' && isFormula)
					) {
				indexToSelect = i;
				// bei Formeln der Wert uebernehmen
				if (isFormula) {
					this.selectedAction = Object.clone(action);
					this.selectedAction.setValueString(valueToSelect);
				}
				else {
					this.selectedAction = action;
				}
				//if (isFormula) this.dom.select('.input')[0].value = valueToSelect;
			}
		}
	}

	var html = '<select ' +
			'onchange="this.parentNode.parentNode.__ActionEditor.handleValueSelected(this);this.blur();" ' +
			'onfocus="this.parentNode.parentNode.__ActionEditor.hasInputFocus = true;" ' +
			'onblur="this.parentNode.parentNode.__ActionEditor.hasInputFocus = false;">';
	for (var i=0, value=0; i<actions.length; i++) {
		var action = actions[i];
		if (Object.isString(action)) {
			html +=  '<optgroup label="'+action+'"></optgroup>';
		}
		else {
			html += '<option value=' + (value++) + 
				(indexToSelect == i ? ' selected' : '') + 
				'>' +
				action.getDisplayText() + 
				'</option>';
		}
	}
	html += '</select>';
	root.innerHTML = html;
	this.updateInputField();
}

ActionEditor.prototype.destroy = function() {
	this.setVisible(false);
}


/**
 * ActionPane ist eine Klasse zur Anzeige einer Aktion ohne Editiermoeglichkeit. 
 * Neben dem Rendern der Inhalte ueberwacht ActionPane auch den KBInfo Cache.
 */
 
 function ActionPane(parent, action, style, onChange) {
	this.parent = $(parent);
	this.style = style;
	this.action = action;
	this.onChange = onChange;

	this.dom = null;
	this.cacheListener = function(changedInfoObjects) {this.handleCacheChange(changedInfoObjects);}.bind(this);
	this.setVisible(true);
}

ActionPane.prototype.getDOM = function() {
	return this.dom;
}

ActionPane.prototype.isVisible = function() {
	return (this.dom != null);
}

ActionPane.prototype.getAction = function() {
	return this.action;
}

ActionPane.prototype.handleCacheChange = function(changedInfoObjects) {
	if (this.isVisible()) {
		// due to be registered only to cache events for this object name
		// no further checks are required
		this.setVisible(false);
		this.setVisible(true);
	}
	if (this.onChange) {
		this.onChange(this);
	}
}

ActionPane.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.parent.appendChild(this.dom);
		if (this.action) {
			var name = this.action.getInfoObjectName();
			KBInfo.addCacheChangeListener(this.cacheListener, name);
//			if (!KBInfo.lookupInfoObject(name)) {
//				KBInfo.searchInfoObject(name, null, 100, null);
//			}		
		}
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		if (this.action) {
			var name = this.action.getInfoObjectName();
			KBInfo.removeCacheChangeListener(this.cacheListener, name);
		}
		this.parent.removeChild(this.dom);
		this.dom = null;
	}
}

ActionPane.prototype.render = function() {
	var childs = [];
	var name = this.action ? this.action.getInfoObjectName() : '---';
	var infoObject = KBInfo.lookupInfoObject(name);
	var icon = infoObject ? infoObject.getIconURL() : KBInfo.imagePath+'no-object.gif';
	if (icon) {
		childs.push(Builder.node('span', {className: 'icon'}, 
		[
			Builder.node('img', {src: icon})
		]));
	}
	childs.push((name && !name.blank()) ? name : '---');

	var valueText = null;
	var valueError = null;
	if (this.action && !this.action.isDecision()) {
//		valueText = this.action.getDisplayText();
		valueText = ' ';
		valueError = this.action.getError();
	}
	
	var object;
	var dom = Builder.node('div', {
		className: 'ActionPane',
		style: this.style ? this.style : ''
	}, 
	[
		object = Builder.node('div', {
			className: 'object'
		}, childs),
		Builder.node('div', {
			className: valueError ? 'value error' : 'value',
			title: valueError ? valueError : ''
		}, (valueText == null) ? [] : [valueText])
	]);
	dom.__ActionEditor = this;
	if (infoObject) object.title = infoObject.getToolTip();
	return dom;
}
 	

ActionPane.prototype.destroy = function() {
	this.setVisible(false);
}

