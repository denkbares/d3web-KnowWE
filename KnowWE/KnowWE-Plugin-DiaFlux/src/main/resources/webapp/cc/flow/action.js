
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
	var parameters = this._extractParameters(expression);
	this.valueString = parameters.value;
	this.infoObjectName = parameters.object;
}

Action.prototype.setValueString = function(newValueString) {
	this.valueString = newValueString;
	this.expression = Action._createExpression(this.infoObjectName, this.valueString);
	this.error = null; // check semantics on first access of getError()
}

Action.prototype.getExpression = function() {
	return this.expression;
}

Action.prototype.getDisplayHtml= function() {
	var text = this.getDisplayText();
	if (text) text = text.escapeHTML();
	return text;
}

Action.prototype.getDisplayText = function() {
	if (this.markup == 'NOP')		return '';
	if (this.valueString == "ERFRAGE")	return 'ask';
	if (this.valueString == "ALWAYS")	return 'always ask';
	if (this.valueString == "INSTANT")	return 'ask instantly';
	if (this.valueString == "P7")		return 'established';
	if (this.valueString == "N7")		return 'excluded';
	if (this.valueString == "YES")		return 'Yes';
	if (this.valueString == "NO")		return 'No';
	if (this.valueString == "P4")		return 'suggested';
	if (this.valueString == "()" && this.markup == "KnOffice")		return 'Value:'; // nur leere Formel so anzeigen
	if (this.valueString == "eval()" && this.markup == "timeDB")		return 'Formula: f(x)='; // nur leere Formel so anzeigen
	
	if (this.isFormula()) { // nichtleere Formel
		var result = Action._extractFormulaExpression(this.markup, this.valueString);
		return "= " + result; 
	}
	
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
		(this.valueString == 'ERFRAGE' || this.valueString == 'INSTANT' || this.valueString == 'ALWAYS');
}

Action.prototype.isAssignment = function() {
	return this.markup == 'KnOffice' && this.valueString != 'ERFRAGE' && this.valueString != 'INSTANT' && this.valueString != 'ALWAYS';
}

Action.prototype.isFormula = function() {
	// we cannot have formulas for some special expressions
	if (this.expression && this.expression.startsWith('CALL[')) return false;
	if (this.expression && this.expression.startsWith('ALWAYS[')) return false;
	if (this.expression && this.expression.startsWith('INSTANT[')) return false;
	return this.markup == 'KnOffice' && this.valueString && this.valueString.startsWith('(') && this.valueString.endsWith(')') ||
	this.markup == 'timeDB' && this.valueString && this.valueString.startsWith('eval(') && this.valueString.endsWith(')');
}

Action.prototype.isDecision = function() {
	return this.markup == 'NOP';
}

Action.prototype.isFlowCall = function() {
	return this.markup == 'KnOffice' && this.expression.startsWith('CALL[');

};

Action._isFormulaString = function(string){
	if (string.startsWith('(') && string.endsWith(')')) {
		return true;
	} else if (string.startsWith('eval(') && string.endsWith(')')) {
		return true;
	} else {
		return false;
	}
	
}

Action._extractFormulaExpression = function(markup, formExpr){
	
	if (markup == 'KnOffice')
		return formExpr.substring(1, formExpr.length-1);
	else if (markup == 'timeDB')
		return formExpr.substring(5, formExpr.length-1);
	else
		return '()';
	
}

Action._createExpression = function(name, value, isKeyword) {
	var result;
	name = IdentifierUtils.quoteIfNeeded(name);
	if (Action._isFormulaString(value)) {
		result = name + ' = ' + value;
	} 
	else {
		if (isKeyword){ // dont add quotes on right side if keyword is present
			result = name + ' = ' + value;
		} else {
			result = name + ' = ' + IdentifierUtils.quoteIfNeeded(value);
		}
		
		
	}
	
	return result;
}


Action.prototype._extractInfoObjectName_deprecated = function(string) {
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

	// for graceful behavior try out known keywords first
	// 'ALWAYS[' <name> ']'
	// 'INSTANT[' <name> ']'
	nameExpr = /^\s*(ALWAYS|INSTANT)\[(.+)\]\s*$/;
	result = nameExpr.exec(string);
	if (result && result.length > 2 && result[2]) return result[2];

	// 'CALL[' <name> '("' <value> '")' ']'
	nameExpr = /^\s*(CALL)\[(.+)\(("[^\\"]+((\\"|\\\\)[^\\"]*)*")\)\]\s*$/;
	result = nameExpr.exec(string);
	if (result && result.length > 3 && result[2]) return result[2];

	// 'CALL[' <name> '(' <value> ')' ']'
	nameExpr = /^\s*(CALL)\[(.+)\(([^\(]+|"[^"]+(\\"[^"]+)*")\)\]\s*$/;
	result = nameExpr.exec(string);
	if (result && result.length > 3 && result[2]) return result[2];
	
	// last try only ["] <name> ["]
	nameExpr = /^\s*"?(.*)"?\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];	

	this.error = "The object's name can not be identified.";
	return "";
}

//extracts the value from the expression
Action.prototype._extractValueString_deprecated = function(string) {
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

	// for graceful behavior try out known keywords first
	// 'ALWAYS[' <name> ']'
	// 'INSTANT[' <name> ']'
	nameExpr = /^\s*(ALWAYS|INSTANT)\[(.+)\]\s*$/;
	result = nameExpr.exec(string);
	if (result && result.length > 2 && result[1]) return result[1];

	// 'CALL[' <name> '("' <value> '")' ']'
	nameExpr = /^\s*(CALL)\[(.+)\(("[^\\"]+((\\"|\\\\)[^\\"]*)*")\)\]\s*$/;
	result = nameExpr.exec(string);
	if (result && result.length > 3 && result[3]) return IdentifierUtils.unquote(result[3]);

	// 'CALL[' <name> '(' <value> ')' ']'
	nameExpr = /^\s*(CALL)\[(.+)\(([^\(]+|"[^"]+(\\"[^"]+)*")\)\]\s*$/;
	result = nameExpr.exec(string);
	if (result && result.length > 3 && result[3]) return result[3];
	
	// second.II try with brackets -> Value
	nameExpr = /=\s*(\(.*\))\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];
	
	// second.III try with brackets -> Formula
	nameExpr = /=\s*(eval\(.*\))\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];

	nameExpr = /^\s*"?(.*)"?\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return 'ERFRAGE'; // we do have an implicit value	

	this.error = "The action can not be identified.";
	return "";
}


Action.prototype._extractParameters = function(string) {
	string = string.trim();
	
	// decision node
	if (this.markup == 'NOP') {
		return {
			object: IdentifierUtils.unquote(string),
			value: ''
		};
	}
	
	// only correctly encoded object name --> ask for it
	if (IdentifierUtils.isIdentifier(string)) {
		return {
			object: IdentifierUtils.unquote(string),
			value: 'ERFRAGE'
		};
	}
	
	// correctly encoded assignment
	var regex = new RegExp("^("+IdentifierUtils.IDENTIFIER_STRING+")\\s*=\\s*("+IdentifierUtils.IDENTIFIER_STRING+")$");
	var matches = regex.exec(string);
	if (matches) return {
		object: IdentifierUtils.unquote(matches[1]),
		value: IdentifierUtils.unquote(matches[2])
	};
	
	// correctly encoded and not encoded keyword actions
	// because not encoded is also unambiguous
	// 'ALWAYS[' <name> ']'
	// 'INSTANT[' <name> ']'
	regex = /^(ALWAYS|INSTANT)\[(.+)\]$/i;
	matches = regex.exec(string);
	if (matches) return {
		object: IdentifierUtils.unquote(matches[2]),
		value: matches[1].toUpperCase()
	};
	
	// keyword actions for calling start nodes of other flows
	// where at least the value has to be encoded correctly
	// 'CALL[' <name> '(' <value> ')]'
	regex = new RegExp("^CALL\\[(.+)\\(("+IdentifierUtils.IDENTIFIER_STRING+")\\)\\]$", "i");
	matches = regex.exec(string);
	if (matches) return {
		object: IdentifierUtils.unquote(matches[1]),
		value: IdentifierUtils.unquote(matches[2])
	};

	// for compatibility reasons, try with identifier string that did not yet consider white spaces
	var compatibilityIdentifierString = IdentifierUtils.IDENTIFIER_STRING.replace(/ /, "");
	var regex = new RegExp("^("+ compatibilityIdentifierString+")\\s*=\\s*("+compatibilityIdentifierString+")$");
	var matches = regex.exec(string);
	if (matches) return {
		object: IdentifierUtils.unquote(matches[1]),
		value: IdentifierUtils.unquote(matches[2])
	};
	
	// otherwise go for backward compatibility 
	// (use old and deprecated methods)
	return {
		object: this._extractInfoObjectName_deprecated(string).trim(),
		value: this._extractValueString_deprecated(string).trim()
	};
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
		this.error = 'This object can not be asked.';
	}
	else if (this.valueString == '()' || this.valueString == 'eval()') {
		this.error = 'No formula has been entered yet.';
	}
	else if (clazz == KBInfo.Solution) {
		if (this.isDecision() || (this.valueString && this.valueString.search(/^[NP][1234567]$/i) >= 0)) return;
		this.error = '"'+this.valueString+'" is no valid value of a solution.';
	}
	else if (clazz == KBInfo.Question) {
		
		if (this.markup == "NOP")
			return;
		
		switch (infoObject.getType()) {
			case KBInfo.Question.TYPE_BOOL:
			case KBInfo.Question.TYPE_OC:
			case KBInfo.Question.TYPE_MC:
				if (this.isFormula()) break;
				if (infoObject.getOptions().indexOf(this.valueString) >= 0) break;
				this.error = '"'+this.valueString+'" is no answer of this question.';
				break;
			case KBInfo.Question.TYPE_NUM:
			case KBInfo.Question.TYPE_DATE:
			case KBInfo.Question.TYPE_TEXT:
				if (this.isFormula()) break;
				this.error = 'Only formulas are allow for this object';
				break;
		}
	}
	else if (clazz == KBInfo.Flowchart) {
		if (infoObject.getStartNames().indexOf(this.valueString) >= 0) return;
		this.error = '"'+this.valueString+'" is no start node of this flowchart';
	}
}


Action.createPossibleActions = function(infoObject) {
	if (!infoObject) return null;
	var name = infoObject.getName();
	var quoted = IdentifierUtils.quoteIfNeeded(name);
	var result = [];
	if (infoObject.getClassInstance() == KBInfo.Question) {
		if (!infoObject.isAbstract()) {
			result.push('Ask question');			
			// questions can be asked
			result.push(new Action('KnOffice', quoted));
			result.push(new Action('KnOffice', 'INSTANT['+quoted+']'));
			result.push(new Action('KnOffice', 'ALWAYS['+quoted+']'));
		}
		result.push('Use value');			
		result.push(new Action('NOP', quoted));			
		result.push('Assign value');			
		switch (infoObject.getType()) {
			//add yes/no value
			case KBInfo.Question.TYPE_OC:
			case KBInfo.Question.TYPE_MC:
			case KBInfo.Question.TYPE_BOOL:
				var options = infoObject.getOptions();
				for (var i=0; i<options.length; i++) {
					result.push(new Action('KnOffice', Action._createExpression(name, options[i])));
				}
			// currently add no options for other values
			case KBInfo.Question.TYPE_DATE:
			case KBInfo.Question.TYPE_TEXT:
				// no choices possible, use edit field instead!
				result.push(new Action('timeDB', Action._createExpression(name, 'eval()', true)));
				break;
			case KBInfo.Question.TYPE_NUM:
				result.push(new Action('timeDB', Action._createExpression(name, 'eval()', true)));
				result.push(new Action('KnOffice', Action._createExpression(name, '()', true)));
		}
	}
	else if (infoObject.getClassInstance() == KBInfo.Solution) {
		result.push('Rate solution');
		for (var i = 7; i > 1; i--){
			result.push(new Action('KnOffice', Action._createExpression(name, 'P' + i, true)));
		}
		for (var i = 1; i <= 7; i++){
			result.push(new Action('KnOffice', Action._createExpression(name, 'N' + i, true)));
		}
		result.push('Use value');			
		result.push(new Action('NOP', quoted));	
	}
	else if (infoObject.getClassInstance() == KBInfo.Flowchart) {
		result.push('Call start node');			
		var options = infoObject.getStartNames();
		for (var i=0; i<options.length; i++) {
			var opt = IdentifierUtils.quoteIfNeeded(options[i]);
			result.push(new Action('KnOffice', 'CALL[' + quoted + '(' + opt + ')' + ']'));
		}
	}
	else if (infoObject.getClassInstance() == KBInfo.QSet) {
		result.push('Ask questionnaire');			
		result.push(new Action('KnOffice', quoted));
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

function ActionEditor(parent, action) {
	this.parent = $(parent);
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
		
		if (this.selectedAction.markup == 'timeDB') {
			this.selectedAction.setValueString('eval('+this.dom.select('.input')[0].value+')');
		} else {
			this.selectedAction.setValueString('('+this.dom.select('.input')[0].value+')');
			
		}
		
	}
	return this.selectedAction;
}

ActionEditor.prototype.getAnswers = function() {
	return this.answers;
}

ActionEditor.prototype.handleObjectSelected = function() {
	this.refreshValueInput();
}

// Called after an Object has been selected in ObjectSelect
ActionEditor.prototype.handleValueSelected = function() {
	if (!this.isVisible()) return;
	var selects = this.dom.select('.value select');
	if (selects.length == 0) return;
	var value = selects[0].options[selects[0].options.selectedIndex].value;
	this.selectedAction = this.selectableActions[value];
	this.updateInputField();
	var toFocus = this.dom.select('.input')[0] || selects[0];
	(function() {this.focus()}).bind(toFocus).defer();
}

// Called after selecting an Action for the selected object
// checks if the input field has to be shown for the formula
ActionEditor.prototype.updateInputField = function() {
	var input = this.dom.select('.input')[0];
	if (this.selectedAction && this.selectedAction.isFormula()) {
		input.show();
		input.focus();
		input.select();
		if (this.selectedAction.markup == 'timeDB') {
			this.autocomplete = new AutoComplete(input, AutoComplete.sendD3webFormulaCompletionAction);
			jq$(input).blur(jq$.proxy(function() {this.autocomplete.showCompletions(null)}, this));
		}
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
		if (this.autocomplete) {
			this.autocomplete.showCompletions(null);
			this.autocomplete = null;
		}
		this.objectSelect.destroy();
		this.objectSelect = null;
		this.parent.removeChild(this.dom);
		this.dom = null;
	}
}

ActionEditor.prototype.getFormulaString = function() {

	if (!this.selectedAction || !this.selectedAction.isFormula())
		return '()';
	
	var formula = this.selectedAction.getValueString();
	
	return Action._extractFormulaExpression(this.selectedAction.markup, this.selectedAction.valueString)
	

}
	
ActionEditor.prototype.render = function() {
	var formula = this.getFormulaString();
	
	var dom = Builder.node('div', {
		className: 'ActionEditor'
	}, 
	[
		Builder.node('div', {className: 'object'}),	// ObjectSelect parent
		Builder.node('div', {className: 'value'}),	// value dropdown parent
		Builder.node('textarea', {
			className: 'input formula', type: 'text', style: 'display: none;'
		},[formula])
	]);
	dom.__ActionEditor = this;
	return dom;
}


ActionEditor.prototype.refreshValueInput = function() {
	if (!this.isVisible()) return;
	// remove existing input method
	var root = this.dom.select('.value')[0];
	
	// check if object is in cache
	var infoObject = (this.objectSelect) ? this.objectSelect.getMatchedItem() : null;
	if (!infoObject) {
//		var buttonOK = '<button class="ok" onclick="this.parentNode.parentNode.__ActionEditor.askQuestionType()">Ok</button>';
//		var question = '<form name="choseQuestionText" id="choseQuestionText" method="get"><input type="text" id="questionText" name="questionText"></form>';
//		root.innerHTML = "<i>Objekt nicht bekannt<br\>Objekt erstellen?<br\></i>" + question + "<br\>" + buttonOK;
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
	var markupToSelect = this.selectedAction ? this.selectedAction.getMarkup() : '';
	var indexToSelect = -1; // select first one if no match available
	var isSelectFormula = this.selectedAction ? this.selectedAction.isFormula() : false;
	this.selectedAction = null;

	if (!actions) {
		root.innerHTML = '<i>no action available</i>';
		if (this.objectSelect) this.selectedAction = new Action('NOP', this.objectSelect.getValue());
		return;
	}

	for (var i=0; i<actions.length; i++) {
		var action = actions[i];
		if (DiaFluxUtils.isString(action)) 
			continue;
		
		var actionValueString = action.getValueString();
		var actionMarkup = action.getMarkup();
		this.selectableActions.push(action);
		// ein Wert wird dann selektiert wenn der Wert gleich ist 
		// oder beides eine Formel ist (da dann der Wert in der Drop-Down-Liste leer ist)
		// Ausserdem wird die erste Action gemerkt, da eventuell keine passende mehr nachfolgt
		
		if (indexToSelect == -1 // noch keine Aktion ausgewählt -> nimm die erste
			|| (actionValueString == valueToSelect // sonst muss markup und wert übereinstimmen
				&& markupToSelect == actionMarkup)) {
			
			indexToSelect = i;
		
			this.selectedAction = action;
		
		
		} else if (markupToSelect == actionMarkup 
				&& action.isFormula() && isSelectFormula) {

			indexToSelect = i;
			
			// bei Formeln der Wert uebernehmen
			this.selectedAction = Object.clone(action);
			this.selectedAction.setValueString(valueToSelect);

		}
				
			
	}

	var html = '<select ' +
			'onchange="this.parentNode.parentNode.__ActionEditor.handleValueSelected(this);this.blur();" ' +
			'onfocus="this.parentNode.parentNode.__ActionEditor.hasInputFocus = true;" ' +
			'onblur="this.parentNode.parentNode.__ActionEditor.hasInputFocus = false;">';
	for (var i=0, value=0; i<actions.length; i++) {
		var action = actions[i];
		if (DiaFluxUtils.isString(action)) {
			html +=  '<optgroup label="--- ' + action + ' ---"></optgroup>';
		}
		else {
			html += '<option value=' + (value++) + 
				(indexToSelect == i ? ' selected' : '') + 
				'>' +
				action.getDisplayHtml() + 
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
 
 function ActionPane(parent, action, onChange, flowname, fcid) {
	this.parent = $(parent);
	this.action = action;
	this.onChange = onChange;
	this.flowname = flowname;
	this.fcid = fcid;

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
		
		//TODO disabled: test for disappearing editor when inserting newly created object into cache
//		this.setVisible(false);
//		this.setVisible(true);
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

ActionPane.WORD_WRAP_POINTS = ['[', ']', '-', '_','#','(',')'];

//inserts a Zero-width space after special chars
//NB: those have to be removed, if this string is re-used, eg to search for an object
ActionPane.insertWordWrapPoints = function(s) {
	for (var i = 0; i < ActionPane.WORD_WRAP_POINTS.length; i++){
		var char = ActionPane.WORD_WRAP_POINTS[i];
		s = s.replace(new RegExp("\\" + char, 'g'), char + "\u200B");
	}
	return s;
}

ActionPane.prototype.render = function() {
	var name = this.action ? this.action.getInfoObjectName() : '---';
	var infoObject = KBInfo.lookupInfoObject(name);
	var iconURL = infoObject ? infoObject.getIconURL() : KBInfo.imagePath+'no-object.gif';
	if (!name) name = '';

	name = ActionPane.insertWordWrapPoints(name);
	
	var valueText = null;
	var valueError = null;
	valueText = this.action.getDisplayText(); // zeigt ZusatzInfo an (fragen/ immer fragen,...)
	valueError = this.action.getError();

	var objectNode = Builder.node('span', {
		className: 'object',
		style: 'background-image: url(' + iconURL + ');'
	}, [name]);
	
	var dom = Builder.node('div', {
		className: 'ActionPane'
	}, 
	[
		Node.wrapToolMenu(this.flowname, this.fcid, objectNode),
		Builder.node('div', {
			className: valueError ? 'value error' : 'value',
			title: valueError ? valueError : ''
		}, (valueText == null) ? [] : [ActionPane.insertWordWrapPoints(valueText)])
	]);
	dom.__ActionEditor = this;
	return dom;
}
 	

ActionPane.prototype.destroy = function() {
	this.setVisible(false);
}

