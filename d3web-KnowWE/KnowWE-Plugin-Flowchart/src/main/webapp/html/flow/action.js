
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
	if (this.valueString == "ERFRAGE")	return 'ask';
	if (this.valueString == "INSTANT")	return 'always ask';
	if (this.valueString == "P7")		return 'established';
	if (this.valueString == "N7")		return 'excluded';
	if (this.valueString == "YES")		return 'yes';
	if (this.valueString == "NO")		return 'no';
	if (this.valueString == "()")		return 'Formula: f(x)='; // nur leere Formel so anzeigen
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
	nameExpr = /^\s*[\w!]+\["?([^"]+)"?\]\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1];
	
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
	nameExpr = /^\s*([\w!]+)\[.+\]\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return result[1].toUpperCase();

	nameExpr = /^\s*"?(.*)"?\s*$/i;
	result = nameExpr.exec(string);
	if (result && result.length > 1 && result[1]) return 'ERFRAGE'; // we do have an implicit value	

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
			result.push(new Action('KnOffice', 'ERFRAGE['+name.toJSON()+']'));
			result.push(new Action('KnOffice', 'INSTANT['+name.toJSON()+']'));
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
			result.push(new Action('KnOffice', Action._createExpression(name, options[i])));
		}
	}
	else if (infoObject.getClassInstance() == KBInfo.QSet) {
		result.push('---- Fragebogen stellen ----');			
		result.push(new Action('KnOffice', 'ERFRAGE['+name.toJSON()+']'));
		result.push(new Action('KnOffice', 'INSTANT['+name.toJSON()+']'));
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

ActionEditor.prototype.refreshValueInput = function() {
	if (!this.isVisible()) return;
	// remove existing input method
	var root = this.dom.select('.value')[0];
	
	// check if object is in cache
	var infoObject = (this.objectSelect) ? this.objectSelect.getMatchedItem() : null;
	if (!infoObject) {
		root.innerHTML = "<i>Objekt nicht bekannt</i>" 
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
 * ActionPane ist eine Klasse zur Anzeige einer Aktion ohne Editierm�glichkeit. 
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
		valueText = this.action.getDisplayText();
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

