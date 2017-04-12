var KBInfo = {};


KBInfo._infoObjectByID = {};


KBInfo.getInfoObject = function (id) {
	return KBInfo._infoObjectByID[id];
};

KBInfo.parseInfoObjects = function (xmlDom) {
	var parsedInfoObjects = [];
	for (var xmlNode = xmlDom.firstChild; xmlNode; xmlNode = xmlNode.nextSibling) {
		var name = xmlNode.nodeName.toLowerCase();
		var infoObject = null;
		// create object
		if (name == 'qset') {
			infoObject = new KBInfo.QSet(xmlNode);
		}
		else if (name == 'question') {
			infoObject = new KBInfo.Question(xmlNode);
		}
		else if (name == 'solution') {
			infoObject = new KBInfo.Solution(xmlNode);
		}
		// handle object
		if (infoObject) {
			parsedInfoObjects.push(infoObject);
			KBInfo._infoObjectByID[infoObject.getID()] = infoObject;
		}
	}
	return parsedInfoObjects;
}

KBInfo._nodeText = function(node) {
	return node.firstChild ? node.firstChild.nodeValue : null;
};

KBInfo._getNodeValueIfExists = function(xmlDom, selector) {
	var node = xmlDom.getElementsByTagName(selector);
	if (node && node.length >= 1) {
		return KBInfo._nodeText(node[0]);
	}
	else {
		return null;
	}
};

KBInfo._collectNodeValues = function(xmlDom, selector) {
	var node = xmlDom.getElementsByTagName(selector);
	var result = [];
	if (node) for (var i=0; i<node.length; i++) {
		result.push(KBInfo._nodeText(node[i]));
	}
	return result;
};

KBInfo._collectChoices = function(xmlDom) {
	var node = xmlDom.getElementsByTagName("choice");
	var result = [];
	if (node) for (var i=0; i<node.length; i++) {
		result.push({
			id: node[i].getAttribute('id'),
			link: node[i].getAttribute('link'),
			abnormality: node[i].getAttribute('abnormality'),
			selected: (node[i].getAttribute('selected') === 'true'),
			text: KBInfo._nodeText(node[i])
		});
	}
	return result;
};

KBInfo._collectRegions = function(xmlDom) {
	var node = xmlDom.getElementsByTagName("region");
	var result = [];
	if (node) for (var i=0; i<node.length; i++) {
		result.push({
			value: node[i].getAttribute('value'),
			coordinates: KBInfo._nodeText(node[i])
		});
	}
	return result;
};

KBInfo._collectValues = function(choices, xmlDom) {
	var node = xmlDom.getElementsByTagName("value");
	var result = [];
	if (node) for (var i=0; i<node.length; i++) {
		// alle values durchgehen
		// und diejenigen, die choices entsprechen, als choices hinzuf�gen
		// die anderen einfach als Text
		var id = KBInfo._nodeText(node[i]);
		var value = id;
		if (id) {
			for (var k=0; k<choices.length; k++) {
				if (choices[k].id == id) value = choices[k];
			}
			result.push(value)
		}
	}
	return result;
};



KBInfo.InfoObject = Class.create({
	initialize: function(xmlDom) {
		this.xmlDom = xmlDom;
		this.id = this.xmlDom.getAttribute('id');
		this.name = this.xmlDom.getAttribute('name');
		this.description = KBInfo._getNodeValueIfExists(this.xmlDom, 'description');
		this.text = KBInfo._getNodeValueIfExists(this.xmlDom, 'text');
		this.info = KBInfo._getNodeValueIfExists(this.xmlDom, 'info');
		this.multimedia = KBInfo._getNodeValueIfExists(this.xmlDom, 'multimedia');
		this.link = KBInfo._getNodeValueIfExists(this.xmlDom, 'link');
		//  we assume that every KBInfo may have child elements, 
		// even this is not true for flowcharts
		// For IE non-prototype (non-extended) classes we use static method variant of Element.select
		this.childs = KBInfo._collectNodeValues(this.xmlDom, 'child');
	},
	
	/** 
	 * Returns the class instance of this KBInfo object.
	 */
	getClassInstance: function() {
		throw("KBInfo.InfoObject.getClassInstance() must be overwritten");
	},
	
	/** 
	 * Returns the id of the referenced knowledgebase object.
	 */
	getID: function() {
		return this.id;
	},
	
	/** 
	 * Returns the short name of an InfoObject. 
	 * this is the non-localized name, normally unique in knowledge base.
	 */
	getName: function() {
		return this.name;
	},
	
	/** 
	 * Returns the localized descriptive title or questioning text of an InfoObject.
	 */
	getText: function() {
		return this.text;
	},
		
	/** 
	 * Returns the localized description text of an InfoObject.
	 */
	getDescription: function() {
		return this.description;
	},
		
	/** 
	 * Returns the description of an InfoObject.
	 */
	getInfo: function() {
		return this.info;
	},
	
	/** 
	 * Returns the multimedia of an InfoObject.
	 */
	getMultimedia: function() {
		return this.multimedia;
	},
	
	/**
	 * Returns the external link of an InfoObject
	 */
	getLink: function() {
		return this.link;
	},
	
	/** 
	 * Returns the child objects of an InfoObject.
	 */
	getChilds: function() {
		return this.childs;
	},
	
	/** 
	 * Returns the url to the icon of this KBInfo object.
	 */
	getIconURL: function() {
		return null;
	},
	
	/** 
	 * Returns the desired ToolTip-Text of an InfoObject.
	 */
	getToolTip: function() {
		return this.name+' (#'+this.id+')'+
			(this.text || this.description ? ': ' : '') + 
			(this.text ? (' \n' + this.text) : '') + 
			(this.description ? (' \n' + this.description) : '');
	}
});

KBInfo.Question = Class.create(
	KBInfo.InfoObject, {

	initialize: function($super, xmlDom) {
		$super(xmlDom);
		this.is_abstract = (this.xmlDom.getAttribute('abstract') == 'true');
		this.type = this.xmlDom.getAttribute('type');
		this.min = Number(KBInfo._getNodeValueIfExists(this.xmlDom, 'min'));
		this.max = Number(KBInfo._getNodeValueIfExists(this.xmlDom, 'max'));
		this.unit = KBInfo._getNodeValueIfExists(this.xmlDom, 'unit');
		this.choices = KBInfo._collectChoices(this.xmlDom);
		this.regions = KBInfo._collectRegions(this.xmlDom);
		this.values = KBInfo._collectValues(this.choices, this.xmlDom);
		var rangeNodes = this.xmlDom.getElementsByTagName('range');
		if (rangeNodes && rangeNodes.length>0) {
			this.range = [rangeNodes[0].getAttribute('min'), rangeNodes[0].getAttribute('max')];
		}
		else {
			this.range = null;
		}
	},
	
	getChoices: function() {
		return this.choices;
	},
	
	getRegions: function() {
		return this.regions;
	},
	
	isAbstract: function() {
		return this.is_abstract;
	},
	
	getType: function() {
		return this.type;
	},
	
	getUnit: function() {
		return this.unit;
	},
	
	getMin: function() {
		return this.min;
	},
	
	getMax: function() {
		return this.max;
	},
	
	getValues: function() {
		return this.values;
	},
	
	hasValue: function(choice) {
		//return this.values.indexOf(choice) >= 0;
		var len = this.values.length;
		for(i=0; i<len; i++){
			if(this.values[i] == choice.text) return true;
		}
		return false;
	},
	
	getClassInstance: function() {
		return KBInfo.Question;
	},

	getIconURL: function() {
		var file = 
			(this.type == KBInfo.Question.TYPE_OC) ? 'single' :
			(this.type == KBInfo.Question.TYPE_MC) ? 'multiple' :
			(this.type == KBInfo.Question.TYPE_BOOL) ? 'yesno' :
			(this.type == KBInfo.Question.TYPE_NUM) ? 'num' :
			(this.type == KBInfo.Question.TYPE_DATE) ? 'date' :
			'text';
		if (this.isAbstract()) file += '-abstract';
		return KBInfo.imagePath + file + '.gif';
	}
});
KBInfo.Question.getShortClassName = function() {
	return "Question";
};	

KBInfo.Question.TYPE_OC =	"oc";
KBInfo.Question.TYPE_MC =	"mc";
KBInfo.Question.TYPE_BOOL =	"bool";
KBInfo.Question.TYPE_NUM =	"num";
KBInfo.Question.TYPE_DATE =	"date";
KBInfo.Question.TYPE_TEXT =	"text";


KBInfo.Solution = Class.create(
	KBInfo.InfoObject, {
	initialize: function($super, xmlDom) {
		$super(xmlDom);
	},
	
	getClassInstance: function() {
		return KBInfo.Solution;
	},

	getIconURL: function() {
		return KBInfo.imagePath + 'diagnosis.gif';
	}
});
KBInfo.Solution.getShortClassName = function() {
	return "Solution";
};


KBInfo.QSet = Class.create(
	KBInfo.InfoObject, {
	initialize: function($super, xmlDom) {
		$super(xmlDom);
	},
	
	getClassInstance: function() {
		return KBInfo.QSet;
	},

	getIconURL: function() {
		return KBInfo.imagePath + 'qset.gif';
	}
});
KBInfo.QSet.getShortClassName = function() {
	return "QSet";
};
