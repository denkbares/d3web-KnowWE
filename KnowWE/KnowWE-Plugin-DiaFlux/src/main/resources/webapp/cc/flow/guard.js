/**
 * Guard
 * @param {String} markup defines the used markup language
 * @param {String} conditionString textual rule condition on KnOffice format to be fulfilled, it may contain placeholders like ${num} or ${question:num}
 * @param {String} displayHTML (optional) name for pretty printing the guard
 * @param {String} unit (optional) the unit of the values of guard
 */
function Guard(markup, conditionString, displayHTML, unit) {
	this.markup = markup;
	this.conditionString = conditionString;
	this.displayHTML = displayHTML;
	this.unit = unit || '';
}

Guard.prototype.isPatternFor = function(other, skipQuotes) {
	// matches is having same markup
	if (this.markup != other.markup) return false;
	// and the conditionString patterns matches
	var regexp = this.getConditionString();
	if (skipQuotes) regexp = regexp.replace(/"/g, "");
	regexp = regexp.replace(/\$\{[:\w]*\}/gi, '__ANY__');
	regexp = DiaFluxUtils.escapeRegex(regexp);

	if (this.isFormula()) {
		regexp = regexp.replace(/__ANY__/g, '(.*)');
	} else {
		regexp = regexp.replace(/__ANY__/g, '([^\\s]*)');
	}

	var string = '^\\s*' + regexp + '\\s*$';
	regexp = new RegExp(string);
	var otherCondition = other.getConditionString();
	if (skipQuotes) otherCondition = otherCondition.replace(/"/g, "");
	return regexp.test(otherCondition);
};

Guard.prototype.getMarkup = function() {
	return this.markup;
};

Guard.prototype.getConditionString = function() {
	return this.conditionString;
};

Guard.prototype.getDisplayHTML = function(values) {
	var text = this.displayHTML ? this.displayHTML :
		(this.conditionString ? this.conditionString.escapeHTML() : "");
	if (values) {
		text = Guard.inject(text, values, true);
	}
	return text;
};

Guard.prototype.lookupDisplayHTML = function(guardPatterns) {
	if (!guardPatterns) return;
	var skipQuotes = [false, true];
	for (var k = 0; k < skipQuotes.length; k++) {
		for (var i = 0; i < guardPatterns.length; i++) {
			var guard = guardPatterns[i];
			if (DiaFluxUtils.isString(guard)) continue;
			if (guard.isPatternFor(this, skipQuotes[k])) {
				// extrahiere Wert
				var values = this.getValues(guard, skipQuotes);
				// und erzeuge auf Werte passendes displayHTML
				this.displayHTML = guard.getDisplayHTML(values);
				this.unit = guard.unit;
				return;
			}
		}
	}
	// not found, than we want to have the original expression a display name
	this.displayHTML = this.getConditionString();
};

Guard.prototype.countVariables = function() {
	return this.getVariableTypes().length;
};

Guard.prototype.getVariableTypes = function() {
	var regexp = /\$\{([:\w]*)\}/gi;
	var result = [];
	var text = this.getConditionString();
	var match;

	while ((match = regexp.exec(text)) != null) {
		result.push(match[1]);
	}
	return result;
};

Guard.prototype.getValues = function(patternGuard, skipQuotes) {
	var pattern = patternGuard.getConditionString();
	if (skipQuotes) pattern = pattern.replace(/"/g, "");
	pattern = pattern.replace(/\$\{[:\w]*\}/gi, '__ANY__');
	pattern = DiaFluxUtils.escapeRegex(pattern);

	if (this.isFormula()) {
		pattern = pattern.replace(/__ANY__/g, '(.*)');
	} else {
		pattern = pattern.replace(/__ANY__/g, '([^\\s]*)');
	}

	var regexp = new RegExp(pattern, 'gi');
	var condString = this.conditionString;
	if (skipQuotes) condString = condString.replace(/"/g, "");
	var result = regexp.exec(condString);
	if (!result) {
		return "";
	} else {
		return result.slice(1);
	}
};

Guard.prototype.inject = function(values) {
	this.conditionString = Guard.inject(this.conditionString, values);
	this.displayHTML = Guard.inject(this.displayHTML, values, true);
};

Guard.prototype.isFormula = function() {
	if (this.markup == 'timeDB') {
		return true;
	} else if (this.markup == 'KnOffice') {
		return this.conditionString && this.conditionString.startsWith('(') && this.conditionString.endsWith(')');
	}

	return false;
};

Guard.inject = function(text, values, escapeHtml) {
	var regexp = /\$\{[:\w]*\}/i;
	for (var i = 0; i < values.length; i++) {
		var item = values[i];
		if (escapeHtml) item = item.escapeHTML();
		text = text.replace(regexp, item);
	}
	return text;
};

Guard.createFromXML = function(flowchart, xmlDom, pasteOptions, sourceNode) {

	if (!xmlDom || xmlDom.length == 0) {
		return new Guard('NOP', ' ', ' ');
	}

	var markup = xmlDom[0].getAttribute('markup') || 'KnOffice';
	var conditionString = KBInfo._nodeText(xmlDom[0]);

	//removes lhs from binary operation
	if (markup == 'timeDB') {

		var infoObject = sourceNode.getBaseObject();
		if (infoObject) {
			var name = DiaFluxUtils.escapeRegex(infoObject.getName());
			var quotedName = DiaFluxUtils.escapeRegex(IdentifierUtils.quote(infoObject.getName()));
			var regexString = "^eval\\((?:" + name + "|" + quotedName + ")\\s*(<|<=|>|>=|!=|=)(.*)\\)";
			var regex = new RegExp(regexString, "i");
			var match = regex.exec(conditionString);

			if (match) {
				conditionString = "eval(" + match[1] + "" + match[2] + ")";
			}
		}
	}

	guard = new Guard(markup, conditionString);
	guard.lookupDisplayHTML(sourceNode.getPossibleGuards());

	return guard;
};


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
				for (var i = 0; i < options.length; i++) {
					result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" = "' + options[i] + '"', options[i]));
				}
				result.push('Exclude value');
				for (var i = 0; i < options.length; i++) {
					result.push(new Guard('KnOffice', 'NOT("' + infoObject.getName() + '" = "' + options[i] + '")', '&ne; ' + options[i]));
				}
				break;
			// currently add no options for other values
			case KBInfo.Question.TYPE_DATE:
				result.push('Time');
				result.push(new Guard('timeDB', 'eval((now - ${duration}) > ' + infoObject.getName() + ')', '&gt; ${duration} ago'));
				result.push(new Guard('timeDB', 'eval((now - ${duration}) < ' + infoObject.getName() + ')', '&lt; ${duration} ago'));
				result.push(new Guard('timeDB', 'eval((now - ${duration}) >= ' + infoObject.getName() + ')', '&ge; ${duration} ago'));
				result.push(new Guard('timeDB', 'eval((now - ${duration}) <= ' + infoObject.getName() + ')', '&le; ${duration} ago'));
				break;
			case KBInfo.Question.TYPE_TEXT:
				break;
			case KBInfo.Question.TYPE_NUM:
				result.push('Test value');
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" = ${num}', '= ${num}', unit));
				result.push(new Guard('KnOffice', 'NOT("' + infoObject.getName() + '" = ${num})', '&ne; ${num}', unit));
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" < ${num}', '&lt; ${num}', unit));
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" > ${num}', '&gt; ${num}', unit));
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" <= ${num}', '&le; ${num}', unit));
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" >= ${num}', '&ge; ${num}', unit));
				result.push('Test interval');
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" >= ${num} AND "' + infoObject.getName() + '" <= ${num}', '[ ${num} .. ${num} ]', unit));
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" >= ${num} AND "' + infoObject.getName() + '" < ${num}', '[ ${num} .. ${num} [', unit));
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" > ${num} AND "' + infoObject.getName() + '" <= ${num}', '] ${num} .. ${num} ]', unit));
				result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" > ${num} AND "' + infoObject.getName() + '" < ${num}', '] ${num} .. ${num} [', unit));
		}
		result.push('Formula');
		result.push(new Guard('timeDB', 'eval(${formula})', '${formula}'));
		result.push('Common');
		result.push(new Guard('KnOffice', 'KNOWN["' + infoObject.getName() + '"]', 'known'));
		result.push(new Guard('KnOffice', 'NOT(KNOWN["' + infoObject.getName() + '"])', 'unknown'));
		result.push(new Guard('NOP', ' ', ' '));
	}
	else if (infoObject.getClassInstance() == KBInfo.Solution) {
		result.push('User');
		result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" = confirmed', "confirmed"));
		result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" = rejected', "rejected"));
		result.push('Derived Value');
		result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" = established', "established"));
		result.push(new Guard('KnOffice', '"' + infoObject.getName() + '" = excluded', "excluded"));
		result.push('Negated Value');
		result.push(new Guard('KnOffice', 'NOT("' + infoObject.getName() + '" = established)', "&ne; established"));
		result.push(new Guard('KnOffice', 'NOT("' + infoObject.getName() + '" = excluded)', "&ne; excluded"));
		result.push('Formula');
		result.push(new Guard('timeDB', 'eval(${formula})', '${formula}'));
		result.push('Common');
		result.push(new Guard('NOP', ' ', ' '));
	}
	else if (infoObject.getClassInstance() == KBInfo.Flowchart) {
		var options = infoObject.getExitNames();
		if (options.length > 0) {
			result.push('Use result');
			for (var i = 0; i < options.length; i++) {
				var opt = options[i];
				if (IdentifierUtils.needQuotes(opt)) {
					opt = IdentifierUtils.quote(opt);
				}
				result.push(new Guard('KnOffice', 'IS_ACTIVE["' + infoObject.getName() + '"(' + opt + ')]', options[i]));
			}
		}
		result.push('Common');
		result.push(new Guard('KnOffice', 'PROCESSED[' + infoObject.getName() + ']', 'processed'));
	}
	else if (infoObject.getClassInstance() == KBInfo.QSet) {
		result.push('Formula');
		result.push(new Guard('timeDB', 'eval(${formula})', '${formula}'));
		result.push('Common');
		result.push(new Guard('NOP', ' ', ' '));
	}

	return result;
};


/**
 * GuardPane
 *
 * Class for displaying an existing guard.
 */

function GuardPane(parent, guard, rule) {
	this.parent = $(parent);
	this.guard = guard;
	this.problem = null;

	if (DiaFluxUtils.isString(this.guard)) {
		this.guard = new Guard('KnOffice', this.guard, this.guard);
	}

	this.dom = null;
	this.checkProblems(rule);
	this.setVisible(true);
}

GuardPane.prototype.getDOM = function() {
	return this.dom;
};

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

	if (this.guard) {
		if (this.guard.isFormula() && (this.guard.getConditionString() == '()' || this.guard.getConditionString() == 'eval()')) {
			this.problem = 'empty formula';
		}

		if (sourceModel.action) {
			var action = new Action(sourceModel.action.markup, sourceModel.action.expression);
			var infoObject = KBInfo.lookupInfoObject(action.getInfoObjectName());
			if (infoObject && infoObject.getClassInstance() == KBInfo.Question) {
				switch (infoObject.getType()) {

					case KBInfo.Question.TYPE_DATE:
						var allGuards = Guard.createPossibleGuards(sourceModel);

						for (var i = 0; i < allGuards.length; i++) {
							if (DiaFluxUtils.isString(allGuards[i])) continue;
							if (allGuards[i].isPatternFor(this.guard)) {
								var types = allGuards[i].getVariableTypes();
								if (types.length == 1 && types[0] == 'duration') {
									var duration = this.guard.getValues(allGuards[i]);
									if (duration == '') {
										this.problem = 'missing duration string';
										break;
									}
									if (!/^\s*(:?\d+\s*(:?ms|s|min|h|d)\s*)$/i.exec(duration)) {
										this.problem = 'invalid duration string: ' + duration;
									}
								}
							}
						}

						break;
				}

			}
		}
	}


	// TODO: check for problems with the guard for the source node here
	if (this.isVisible()) {
		this.setVisible(false);
		this.setVisible(true);
	}
};

GuardPane.prototype.isVisible = function() {
	return (this.dom != null);
};

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
};

GuardPane.prototype.render = function() {
	var childs = [];

	if (this.problem) {
		childs.push(Builder.node('img', {
			src : Flowchart.imagePath + 'warning.png',
			title : this.problem
		}));
	}

	if (this.guard) {
		var textNode = Builder.node('div');
		textNode.innerHTML = this.guard.getDisplayHTML() + ' ' + this.guard.unit;
		childs.push(textNode);
	}
	var dom = Builder.node('div', {
			className : 'GuardPane'
		},
		childs);
	return dom;
};

GuardPane.prototype.destroy = function() {
	this.setVisible(false);
};

