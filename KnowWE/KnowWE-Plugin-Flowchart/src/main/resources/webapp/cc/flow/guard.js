
/**
 * Guard
 * @param {String} markup defines the used markup language
 * @param {String} conditionString textual rule condition on KnOffice format to be fulfilled, it may contain placeholders like ${num} or ${question:num}
 * @param {String} displayHTML (optional) name for pretty printing the guard
 */
function Guard(markup, conditionString, displayHTML, unit) {
	this.markup = markup;
	this.conditionString = conditionString;
	this.displayHTML = displayHTML;
	this.unit = unit || '';
}

Guard.prototype.isPatternFor = function(other) {
	// matches is having same markup
	if (this.markup != other.markup) return false;
	// and the conditionString patterns matches
	var regexp = this.getConditionString();
	regexp = regexp.replace(/\$\{[:\w]*\}/gi, '__ANY__');
	regexp = RegExp.escape(regexp);
	regexp = regexp.replace(/__ANY__/g, '.*');
	var string = '/^\s*'+regexp+'\s*$/';
	regexp = eval(string);
	var test = regexp.test(other.getConditionString());
	return test;
}

Guard.prototype.getMarkup = function() {
	return this.markup;
}

Guard.prototype.getConditionString = function() {
	return this.conditionString;
}

Guard.prototype.getDisplayHTML = function(values) {
	var text = this.displayHTML ? this.displayHTML : this.conditionString.escapeHTML();
	if (values) {
		text = Guard.inject(text, values);
	}
	return text;
}

Guard.prototype.lookupDisplayHTML = function(guardPatterns) {
	if (!guardPatterns) return;
	for (var i=guardPatterns.length - 1; i>0; --i) {
		var guard = guardPatterns[i];
		if (Object.isString(guard)) continue;
		if (guard.isPatternFor(this)) {
			// extrahiere Wert
			var values = this.getValues(guard);
			// und erzeuge auf Werte passendes displayHTML
			this.displayHTML = guard.getDisplayHTML(values);
			return;
		}
	}
	// not found, than we want to have the original expression a display name
	this.displayHTML = this.getConditionString();
}

Guard.prototype.countVariables = function() {
	var regexp = /\$\{[:\w]*\}/i;
	var count = 0;
	var text = this.getConditionString();
	while (text.search(regexp) >= 0) {
		text = text.replace(regexp, '');
		count++;
	}
	return count;
}

Guard.prototype.getValues = function(patternGuard) {
	var pattern = patternGuard.getConditionString();
	if (this.isFormula()){
		
		pattern = pattern.replace(/\$\{[:\w]*\}/gi, '__ANY__');
		pattern = RegExp.escape(pattern);
		pattern = pattern.replace(/__ANY__/g, '(.*)');
		
	} else {
		pattern = pattern.replace(/\$\{[:\w]*\}/gi, '__ANY__');
		pattern = RegExp.escape(pattern);
		pattern = pattern.replace(/__ANY__/g, '([^\\s]*)');
		
	}
	
	var regexp = eval('/'+pattern+'/gi')
	var result = regexp.exec(this.conditionString);
	var slice = result.slice(1);
	return slice;
}

Guard.prototype.inject = function(values) {
	this.conditionString = Guard.inject(this.conditionString, values);
	this.displayHTML = Guard.inject(this.displayHTML, values);
}

Guard.prototype.isFormula = function() {
	if (this.markup == 'timeDB'){
		return true;
	} else if (this.markup == 'KnOffice'){
		return this.conditionString.startsWith('(') && this.conditionString.endsWith(')');
	}
	
	return false;
}

Guard.inject = function(text, values) {
	var regexp = /\$\{[:\w]*\}/i;
	for (var i=0; i<values.length; i++) {
		text = text.replace(regexp, values[i]);
	}
	return text;
}


Guard.createPossibleGuards = function(nodeModel) {
	if (!nodeModel) return null;
	// no guards for start/exit allowed
	if (nodeModel.start) 
		return null;
	
	if (nodeModel.exit) 
		return null;
	
	// create action and lookup info object
	if (!nodeModel.action)
		return null;
	
	var action = new Action(nodeModel.action.markup, nodeModel.action.expression);
	var infoObject = KBInfo.lookupInfoObject(action.getInfoObjectName());
	
	// if no info Object is available, no guards can be provided
	if (!infoObject) 
		return null;
	
	var unit = infoObject.unit;
		
	// ok, so now we build the possible guards
	var result = [];
	if (infoObject.getClassInstance() == KBInfo.Question) {
		switch (infoObject.getType()) {
		
			case KBInfo.Question.TYPE_BOOL:
			case KBInfo.Question.TYPE_OC:
			case KBInfo.Question.TYPE_MC:
				result.push('Test value');
				var options = infoObject.getOptions();
				for (var i=0; i<options.length; i++) {
					result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" = "'+options[i]+'"', options[i]));
				}
				result.push('Exclude value');
				for (var i=0; i<options.length; i++) {
					result.push(new Guard('KnOffice', 'NOT("'+infoObject.getName()+'" = "'+options[i]+'")', '&ne; ' + options[i]));
				}
				result.push('Formula');
				result.push(new Guard('timeDB', 'eval(${formula})', '${formula}'));
				break;
			// currently add no options for other values
			case KBInfo.Question.TYPE_DATE:
			case KBInfo.Question.TYPE_TEXT:
				result.push('Formula');
				result.push(new Guard('timeDB', 'eval(${formula})', '${formula}'));
				break
			case KBInfo.Question.TYPE_NUM:
				result.push('Test value');
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" = ${num}', '= ${num}', unit));
				result.push(new Guard('KnOffice', 'NOT("'+infoObject.getName()+'" = ${num})', '&ne; ${num}', unit));
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" < ${num}', '&lt; ${num}', unit));
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" > ${num}', '&gt; ${num}', unit));
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" <= ${num}', '&le; ${num}', unit));
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" >= ${num}', '&ge; ${num}', unit));
				result.push('Test interval');
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" >= ${num} AND "'+infoObject.getName()+'" <= ${num}', '[ ${num} .. ${num} ]', unit));
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" >= ${num} AND "'+infoObject.getName()+'" < ${num}', '[ ${num} .. ${num} [', unit));
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" > ${num} AND "'+infoObject.getName()+'" <= ${num}', '] ${num} .. ${num} ]', unit));
				result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" > ${num} AND "'+infoObject.getName()+'" < ${num}', '] ${num} .. ${num} [', unit));
				result.push('Formula');
				result.push(new Guard('timeDB', 'eval(${formula})', '${formula}'));
				break;
		}
		result.push('Common');
		result.push(new Guard('KnOffice', 'KNOWN["'+infoObject.getName()+'"]', 'known'));
		result.push(new Guard('KnOffice', 'NOT(KNOWN["'+infoObject.getName()+'"])', 'unknown'));
		result.push(new Guard('NOP', ' ', ' '));
	}
	else if (infoObject.getClassInstance() == KBInfo.Solution) {
		result.push('User');
		result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" = confirmed', "confirmed"));
		result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" = rejected', "rejected"));		
		result.push('Derivation');
		result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" = established', "establisehd"));
		result.push(new Guard('KnOffice', '"'+infoObject.getName()+'" = excluded', "excluded"));
		result.push('Common');
		result.push(new Guard('NOP', ' ', ' '));
	}
	else if (infoObject.getClassInstance() == KBInfo.Flowchart) {
		var options = infoObject.getExitNames();
		if (options.length > 0) {
			result.push('Use result');
			for (var i=0; i<options.length; i++) {
				result.push(new Guard('KnOffice', 'IS_ACTIVE[' + infoObject.getName()+'('+options[i]+')]', options[i]));
			}
			//result.push('Exclude result');
			//for (var i=0; i<options.length; i++) {
			//	result.push(new Guard('KnOffice', 'NICHT(IS_ACTIVE[' + infoObject.getName()+'('+options[i]+')])', '&ne; ' + options[i]));
			//}
		}
		result.push('Common');
		result.push(new Guard('KnOffice', 'PROCESSED[' + infoObject.getName()+']', 'processed'));
		//result.push(new Guard('NOP', ' ', ' ')); //does this make sense for FCs?
	}
	else if (infoObject.getClassInstance() == KBInfo.QSet) {
		result.push('Common');
		result.push(new Guard('NOP', ' ', ' '));
		result.push('Formula');
		result.push(new Guard('timeDB', 'eval(${formula})', '${formula}'));
	}
	
	return result;
}


/**
 * GuardEditor
 * 
 * Class for editing an existing guard with a given set of 
 * possible guards. 
 */

 function GuardEditor(parent, initialGuard, possibleGuards, onChangeListener) {
 	this.parent = $(parent);
 	this.initialGuard = initialGuard;
 	this.possibleGuards = possibleGuards || [];
 	this.onChangeListener = onChangeListener;
 	this.values = [];
 
 	this.dom = null;
 	this.selectedIndex = -1;
 	
 	if (this.initialGuard) {
	 	for (var i=0; i<this.possibleGuards.length; i++) {
	 		var guard = this.possibleGuards[i];
	 		if (Object.isString(guard)) continue;
	 		// das erste selektieren, dass fuer den Guard Vorlage ist 
	 		// (bzw. einfach nur das erste wenn wir keinen Guard haben)
	 		if (guard.isPatternFor(this.initialGuard)) {
	 			this.selectedIndex = i;
		 		this.values = this.initialGuard.getValues(guard);
	 		}
	 	}
	 	if (this.selectedIndex == -1) {
	 		if (Object.isString(this.initialGuard)) {
	 			this.initialGuard = new Guard('KnOffice', this.initialGuard, this.initialGuard);
	 		}
	 		this.possibleGuards.unshift(this.initialGuard);
	 		this.selectedIndex = 0;
	 	}
 	}
 	 	
	this.setVisible(true);
	this.handleValueSelected();
}

GuardEditor.prototype.getDOM = function() {
	return this.dom;
}

GuardEditor.prototype.isVisible = function() {
	return (this.dom != null);
}

/**
 * Returns the selected guard. 
 * 
 * If the guard has any variables inside, the current values are injected.
 */
GuardEditor.prototype.getGuard = function() {
	// check value of select is user has selected with cursor keys
	this.updateFromUI();
	var guard = this.getSelectedGuard();
	if (guard) {
		guard = new Guard(guard.getMarkup(), guard.getConditionString(), guard.getDisplayHTML(), guard.unit);
		// update input fields and inject them into the copy of selected guard
		guard.inject(this.values);
	}
	return guard;
}

/**
 * Returns the selected guard. 
 * 
 * In contrast to "getGuard()" you will get the selected guard prototype
 * as it is, instead of a guard with the edited values injected instead.
 * (e.g. for numeric comparisons)
 */
GuardEditor.prototype.getSelectedGuard = function() {
	return this.possibleGuards[this.selectedIndex];
}

GuardEditor.prototype.handleValueSelected = function() {
	if (!this.isVisible()) return;
	this.updateFromUI();
	this.updateInputField();
	if (this.onChangeListener) this.onChangeListener(this.getGuard());
}

GuardEditor.prototype.updateFromUI = function() {
	if (!this.isVisible()) return;
	var select = this.dom.select('.value select')[0];
	this.selectedIndex = select.value;
	var inputs = this.dom.select('.input input');
	var guard = this.getSelectedGuard();
	var count = guard ? guard.countVariables() : 0;
	this.values = [];
	var allValues = [];
	for (var i=0; i<inputs.length; i++) {
		if (i<count) this.values.push(inputs[i].value);
		allValues.push(inputs[i].value);
	}
	//also update selection entries if they need an inject
	var optIndex = 0;
	for (var i=0; i<this.possibleGuards.length; i++) {
		if (Object.isString(this.possibleGuards[i])) continue;
		select.options[optIndex++].innerHTML = 
			this.possibleGuards[i].getDisplayHTML(allValues);
	}
}

GuardEditor.prototype.updateInputField = function() {
	var guard = this.getSelectedGuard();
	var inputs = this.dom.select('.input input');
	var count = guard ? guard.countVariables() : 0;
	var formula = this.getSelectedGuard() ? this.getSelectedGuard().isFormula() : false;
	for (var i=0; i<inputs.length; i++) {
		if (i<count) {
			if (i ==0){
				if (formula){ // enlarge input field for formulas
					inputs[0].addClassName("formula");
					
				}else {
					inputs[0].removeClassName("formula");
					
				}
			}
			inputs[i].show();
		}
		else {
			inputs[i].hide();
		}
	}
}

GuardEditor.prototype.focus = function() {
	var select = this.dom.select('select')[0];
	var inputFocuses = this.dom.select('.inputFocus');
	if (select.visible()) {
		select.focus();
	}
	else if (inputFocuses.length > 0) {
		inputFocuses[0].focus();
	}
}

GuardEditor.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.parent.appendChild(this.dom);
		this.updateInputField();
		this.focus();
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		this.parent.removeChild(this.dom);
		this.dom = null;
	}
}

GuardEditor.prototype.render = function() {
	var select = 
		'<select class="guard " ' +
		(this.possibleGuards.length==0 ? 'style="display:none;" ' : '') +
		'onchange="this.parentNode.parentNode.__GuardEditor.handleValueSelected();">';
	for (var i=0; i<this.possibleGuards.length; i++) {
		var guard = this.possibleGuards[i];
 		if (Object.isString(guard)) {
 			select += '<optgroup label="--- '+guard+' ---"></optgroup>';
 		}
 		else {
			select += 
				'<option value=' + i + 
				(i == this.selectedIndex ? ' selected': '') +
				'>' + guard.getDisplayHTML() + '</option>';
 		}
	}
	select += '</select>';
	
	if (this.possibleGuards.length==0) {
		select += '<a href="#" class="inputFocus"></a>';
	}
	
	var selectParent, inputParent;
	var inputclass = this.getGuard() ? (this.getGuard().isFormula() ? 'formula' : '') : '';
	var dom = Builder.node('div', {
		className: 'GuardEditor '
	}, 
	[
		selectParent = Builder.node('div', {className: 'value'}),	// value dropdown parent
		inputParent = Builder.node('div', {className: 'input GuardEditorEventHandler'}, [
			Builder.node('input', {	type: 'text', className: inputclass, style: 'display: none;', onBlur: 'this.parentNode.parentNode.__GuardEditor.handleValueSelected();', value: this.values.length > 0 ? this.values[0] : ''}),
			Builder.node('input', {	type: 'text', style: 'display: none;', onBlur: 'this.parentNode.parentNode.__GuardEditor.handleValueSelected();', value: this.values.length > 1 ? this.values[1] : ''})
		])
	]);
	dom.__GuardEditor = this;
	selectParent.innerHTML = select;
	return dom;
}

// avoid handling key events while editing the edit fields or the select
CCEvents.addClassListener('keydown', 'GuardEditorEventHandler', function(event) {
	
	switch(event.keyCode){
	case Event.KEY_RETURN:
		this.parentNode.__GuardEditor.handleValueSelected();
	case Event.KEY_ESC: 
		//if the editor is not hidden before setting the selection to null, the values are set by onBlurHandler
		this.parentNode.__GuardEditor.setVisible(false); 
		theFlowchart.setSelection(null);
		return;
	}
	
	event.defaultHandler();	
});



GuardEditor.prototype.destroy = function() {
	if (this._destroyed) return;
	this._destroyed = true;
	this.setVisible(false);
}
 
 
/**
 * GuardPane
 * 
 * Class for displaying an existing guard. 
 */

 function GuardPane(parent, guard, rule) {
 	this.parent = $(parent);
 	this.guard = guard;
 	this.problem = null;
 
	if (Object.isString(this.guard)) {
		this.guard = new Guard('KnOffice', this.guard, this.guard);
	}
	
 	this.dom = null;
 	this.checkProblems(rule);
	this.setVisible(true);
}

GuardPane.prototype.getDOM = function() {
	return this.dom;
}

GuardPane.prototype.checkProblems = function(rule) {
	this.problem = null;
	if (!rule) {
		this.problem = 'the edge can not be checked';
		return;
	}
	var sourceModel = rule.getSourceNode().getNodeModel();
	var targetModel = rule.getTargetNode().getNodeModel();
	if (sourceModel && sourceModel.exit) {
		this.problem = 'no edges must leave an exit node';
	}
	if (targetModel && targetModel.start) {
		this.problem = 'no edge must enter a start node';
	}
	
	if (this.guard){
		if (this.guard.isFormula() && (this.guard.getConditionString() == '()' ||  this.guard.getConditionString() == 'eval()')){
			this.problem = 'empty formula';
		}
		
	}

	
	// TODO: check for problems with the guard for the source node here
	if (this.isVisible) {
		this.setVisible(false);
		this.setVisible(true);
	}
}

GuardPane.prototype.isVisible = function() {
	return (this.dom != null);
}

GuardPane.prototype.setVisible = function(visible) {
	if (!this.isVisible() && visible) {
		// ==> show Node
		this.dom = this.render();
		this.parent.appendChild(this.dom);
	}
	else if (this.isVisible() && !visible) {
		// ==> hide Node
		this.parent.removeChild(this.dom);
		this.dom = null;
	}
}

GuardPane.prototype.render = function() {
	var childs = [];
	
	if (this.problem) {
		childs.push(Builder.node('img', {
			src: FlowEditor.imagePath+'warning.gif',
			title: this.problem
		}));
	}
	
	if (this.guard) {
		var textNode = Builder.node('div');
		textNode.innerHTML = this.guard.getDisplayHTML() + ' ' + this.guard.unit;
		childs.push(textNode);
	}
	var dom = Builder.node('div', {
		className: 'GuardPane'
	}, 
	childs);
	return dom;
}

GuardPane.prototype.destroy = function() {
	this.setVisible(false);
}

