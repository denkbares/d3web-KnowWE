
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
		
		return (this.markup == "timeDB" ? ":= " : "= ") + result; 
		
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
	return this.markup == 'KnOffice' && this.valueString && this.valueString.startsWith('(') && this.valueString.endsWith(')') ||
	this.markup == 'timeDB' && this.valueString && this.valueString.startsWith('eval(') && this.valueString.endsWith(')');
}

Action.prototype.isDecision = function() {
	return this.markup == 'NOP';
}

Action.prototype.isFlowCall = function() {
	return this.markup == 'KnOffice' && this.expression.startsWith('CALL[');

}

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
	if (Action._isFormulaString(value)) {
		result = '"' + name.escapeQuote() + '" = ' + value;
	} 
	else {
		if (isKeyword){ // dont add quotes on right side if keyword is present
			result = '"' + name.escapeQuote() + '" = ' + value;
		} else {
			result = '"' + name.escapeQuote() + '" = "' + value.escapeQuote() + '"';
		}
		
		
	}
	
	return result;
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

	this.error = "The object's name can not be identified.";
	return null;
}

//extracts the value from the expression
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

	// second.II try with brackets -> Value
	nameExpr = /=\s*(\(.*\))\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];
	
	// second.III try with brackets -> Formula
	nameExpr = /=\s*(eval\(.*\))\s*$/i;
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
	
	
	nameExpr = /^\s*"?(.*)"?\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return 'ERFRAGE'; // we do have an implicit value	

	this.error = "The action can not be identified.";
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
		this.error = 'This object can not be asked.';
	}
	else if (this.valueString == '()' || this.valueString == 'eval()') {
		this.error = 'No formula has been entered yet.';
	}
	else if (clazz == KBInfo.Solution) {
		if (this.valueString && this.valueString.search(/^[NP][1234567]$/i) >= 0) return;
		this.error = '"'+this.valueString+'" is no valid value of a solution.';
	}
	else if (clazz == KBInfo.Question) {
		
		if (this.markup == "NOP")
			return;
		
		switch (infoObject.getType()) {
			case KBInfo.Question.TYPE_BOOL:
			case KBInfo.Question.TYPE_OC:
			case KBInfo.Question.TYPE_MC:
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
	var result = [];
	if (infoObject.getClassInstance() == KBInfo.Question) {
		if (!infoObject.isAbstract()) {
			result.push('Ask question');			
			// questions can be asked
			//removed conversion to json-string @20091102
			result.push(new Action('KnOffice', name));
			result.push(new Action('KnOffice', 'INSTANT['+name+']'));
			result.push(new Action('KnOffice', 'ALWAYS['+name+']'));
		}
		result.push('Use value');			
		result.push(new Action('NOP', '"'+name.escapeQuote()+'"'));			
		result.push('Assign value');			
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
				result.push(new Action('KnOffice', Action._createExpression(name, '()', true)));
				result.push(new Action('timeDB', Action._createExpression(name, 'eval()', true)));
				break;
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

	}
	else if (infoObject.getClassInstance() == KBInfo.Flowchart) {
		result.push('Call start node');			
		var options = infoObject.getStartNames();
		for (var i=0; i<options.length; i++) {
			result.push(new Action('KnOffice', 'CALL[' + name + '(' + options[i] + ')' + ']'));
		}
	}
	else if (infoObject.getClassInstance() == KBInfo.QSet) {
		result.push('Ask questionnaire');			
		//removed conversion to json-string @20091102
		result.push(new Action('KnOffice', name));

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
	//alert("object changed");
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
}

// Called after selecting an Action for the selected object
// checks if the input field has to be shown for the formula
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

ActionEditor.prototype.getFormulaString = function() {

	if (!this.selectedAction || !this.selectedAction.isFormula())
		return '()';
	
	var formula = this.selectedAction.getValueString();
	
	return Action._extractFormulaExpression(this.selectedAction.markup, this.selectedAction.valueString)
	
//	if (this.selectedAction.markup == 'KnOffice')
//		return formula.substring(1, formula.length-1);
//	else if (this.selectedAction.markup == 'timeDB')
//		return formula.substring(5, formula.length-1);
//	else
//		return '()';
	
}
	
ActionEditor.prototype.render = function() {
	var formula = this.getFormulaString();
	
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

CCEvents.addClassListener('keydown', 'ActionEditor',
	function(event) {
		this.__ActionEditor.handleKeyEvent(event);
	}
);

ActionEditor.prototype.handleKeyEvent = function(e) {
	switch(e.keyCode){
		case Event.KEY_ESC: 
			this.handleCancel();
			return;
		case Event.KEY_RETURN:
			if ($('choseQuestionText')) {
				this.askQuestionType();
			} else if ($('choseQuestionType')) {
				this.addAnswer();
			} else if ($('addAnswer')) {
				this.answerValue();
			} 
			return;
	}
	//default handling for cursor events
	e.defaultHandler();
}


// Helper
function getElementsByClassName(class_name)
{
  var all_obj,ret_obj=new Array(),j=0,teststr;

  if(document.all)all_obj=document.all;
  else if(document.getElementsByTagName && !document.all)
    all_obj=document.getElementsByTagName("*");

  for(i=0;i<all_obj.length;i++)
  {
    if(all_obj[i].className.indexOf(class_name)!=-1)
    {
      teststr=","+all_obj[i].className.split(" ").join(",")+",";
      if(teststr.indexOf(","+class_name+",")!=-1)
      {
        ret_obj[j]=all_obj[i];
        j++;
      }
    }
  }
  return ret_obj;
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
		if (Object.isString(action)) 
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
		if (Object.isString(action)) {
			html +=  '<optgroup label="--- ' + action + ' ---"></optgroup>';
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
	valueText = this.action.getDisplayText(); // zeigt ZusatzInfo an (fragen/ immer fragen,...)

	valueError = this.action.getError();

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


