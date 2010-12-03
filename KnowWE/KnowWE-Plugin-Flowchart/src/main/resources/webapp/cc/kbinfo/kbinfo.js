var KBInfo = { 
	
	Version: '0.0.1',
	REQUIRED_PROTOTYPE: '1.6.0.3',

	requireJS: function(libraryName) {
		// inserting via DOM fails in Safari 2.0, so brute force approach
		document.write('<script type="text/javascript" src="'+libraryName+'"><\/script>');
	},
	
	requireCSS: function(cssName) {
		// inserting via DOM fails in Safari 2.0, so brute force approach
		document.write('<link rel="stylesheet" type="text/css" href="'+cssName+'"><\/link>');
	},
	
	imagePath: "../image/kbinfo/",

	load: function() {
	    function convertVersionString(versionString) {
	      var v = versionString.replace(/_.*|\./g, '');
	      v = parseInt(v + '0'.times(4-v.length));
	      return versionString.indexOf('_') > -1 ? v-1 : v;
	    }
	    KBInfo.imagePath = "cc/image/kbinfo/";
	/*
		if((typeof Prototype=='undefined') ||
				(typeof Element == 'undefined') ||
				(typeof Element.Methods=='undefined') ||
				(convertVersionString(Prototype.Version) <
				convertVersionString(KBInfo.REQUIRED_PROTOTYPE)))
			throw("FlowPlugin requires the Prototype JavaScript framework >= " +
				KBInfo.REQUIRED_PROTOTYPE);
	  */
		/*
		var js = /kbinfo\.js(\?.*)?$/;
		$$('head script[src]').findAll(function(s) {
			return s.src.match(js);
		}).each(function(s) {
			var path = s.src.replace(js, '');
			('extensions,dropdownlist,objectselect,objecttree').split(',').each(function(include) { 
				KBInfo.requireJS(path+include+'.js');
				KBInfo.requireCSS(path+include+'.css');
			});
	    });
	    */
	}	
};

KBInfo._nodeText = function(node) {
	if (node.firstChild)
		return node.firstChild.nodeValue;
	else 
		return "";
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


KBInfo._updateCache = function(xmlDom) {
	//alert(xmlDom.childNodes.length);
	var changed = [];
	//showMessage(xmlDom.childNodes);
	if (xmlDom.nodeName.toLowerCase() != 'kbinfo') {
		// if we are not having the correct root element
		// search for it in the dom and process every one
		var infoDoms = xmlDom.getElementsByTagName('kbinfo');
		for (var i=0; i<infoDoms.length; i++) {
			KBInfo._updateCache(infoDoms[i]);
		}
		return;
	}
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
		else if (name == 'flowchart') {
			infoObject = new KBInfo.Flowchart(xmlNode);
		}
		else if (name == 'article') {
			infoObject = new KBInfo.Article(xmlNode);
		}
		// handle object
		if (infoObject) {
			KBInfo._addToChache(infoObject);
			changed.push(infoObject);	
		}
	}
	//alert(Object.toJSON(changed));
	if (changed.length > 0) KBInfo._fireCacheChangeEvent(changed);
}

KBInfo._cache = { byID: [], byName: [], listeners: {} };

KBInfo._addToChache = function(infoObject) {
	KBInfo._cache.byID[infoObject.getID().toLowerCase()] = infoObject;
	KBInfo._cache.byName[infoObject.getName().toLowerCase()] = infoObject;
};

/**
 * Gives the cache the hint that this info object(s) will be required later on.
 * The framework can prepare itself to download the information from the server
 * (if it is not available yet and put it into the cache when it is received.
 * The function will always return immediately.
 */
KBInfo.prepareInfoObject = function(nameOrIDOrArray) {
	if (!nameOrIDOrArray) return;
	if (Object.isString(nameOrIDOrArray)) nameOrIDOrArray = [nameOrIDOrArray];
	var ids = '';
	for (var i=0; i<nameOrIDOrArray.length; i++) {
		if (i>0) ids += ',';
		ids += nameOrIDOrArray[i];
	}
	var url = "KnowCC.jsp?action=GetInfoObjects&ids=" + ids;
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
			//alert(transport.responseXML);
			KBInfo._updateCache(transport.responseXML);
		},
		onFailure: function() {
			CCMessage.warn(
				'AJAX Verbindungs-Fehler', 
				'Eventuell werden einige Objekte anderer Wiki-Seiten nicht korrekt angezeigt. ' +
				'In spaeteren Aktionen koennte auch das Speichern der Aenderungen fehlschlagen.');
		},
		onException: function(transport, exception) {
			CCMessage.warn(
				'AJAX interner Fehler',
				exception
				);
		}
	}); 
};

KBInfo.searchInfoObject = function(phrase, classArray, maxCount, onResult) {
	var classesParam = '';
	if (classArray) {
		classesParam = '&classes=';
		for (var i=0; i<classArray.length; i++) {
			if (i>0) classesParam += ',';
			classesParam += classArray[i].getShortClassName();
		}
	}
	var phraseParam = '&phrase='+RegExp.escape(phrase);
	var maxCountParam = maxCount ? '&maxcount='+maxCount : '';
	var url = "KnowCC.jsp?action=SearchInfoObjects" 
			+ classesParam 
			+ phraseParam
			+ maxCountParam;
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
			// find root node
			var root = transport.responseXML;
			while (root && root.nodeName.toLowerCase() != 'matches') root = root.firstChild;
			// extract result
			var result = {
				count: root.getAttribute('count'),
				hasMore: (root.getAttribute('hasmore') == 'true'),
				matches: KBInfo._collectNodeValues(root, 'match')
			};
			// request the info objects for the result in advance
			KBInfo.prepareInfoObject(result.matches);
			// inform reciever on result
			if (onResult) onResult(result);
		},
		onFailure: function() {
			CCMessage.warn(
				'AJAX Verbindungs-Fehler', 
				'Eventuell werden einige Objekte anderer Wiki-Seiten nicht korrekt angezeigt. ' +
				'In spaeteren Aktionen koennte auch das Speichern der Aenderungen fehlschlagen.');
		},
		onException: function(transport, exception) {
			CCMessage.warn(
				'AJAX interner Fehler',
				exception
				);
		}
	}); 
};

/**
 * Looks in the cache and returns the desired object. 
 * If it is not available in the cache, null is returned 
 * (but the object will NOT be prepared automatically).
 * The function will always return immediately.
 */
KBInfo.lookupInfoObject = function(nameOrIDOrArray) {
	if (!nameOrIDOrArray) return null;
	return KBInfo._cache.byID[nameOrIDOrArray.toLowerCase()] 
		|| KBInfo._cache.byName[nameOrIDOrArray.toLowerCase()];
};

/**
 * Returns the desired object out of the cache. 
 * If it is not available in the cache, it is prepared
 * and the functions waits until the object has arrived from the server.
 * It returns only null if the object cannot be aquired.
 */
KBInfo.getInfoObject = function(nameOrIDOrArray) {
	if (!nameOrIDOrArray) return null;
};

/**
 * Iterates all currently cached KBInfo objects 
 * using the given iterator function. 
 */
KBInfo.iterateInfoObjects = function(iteratorFx) {
	for (var key in KBInfo._cache.byID) {
		var value = KBInfo._cache.byID[key];
		if (!value) continue;
		if (!value.getClassInstance) continue;
		iteratorFx(value);
	}
}

/**
 * Returns an array of all currently cached KBInfo objects, 
 * that meets the given condition function. 
 */
KBInfo.findInfoObjects = function(conditionFx, maxCount) {
	var result = [];
	var count = 0;
	for (var key in KBInfo._cache.byID) {
		var value = KBInfo._cache.byID[key];
		if (!value) continue;
		if (!value.getClassInstance) continue;
		if (conditionFx(value)) {
			result.push(value);
			count++;
			if (count >= maxCount) break;
		}
	}
	return result;
}

KBInfo._fireCacheChangeEvent = function(changedInfoObjects) {
	// call all general Listeners
	var listeners = KBInfo._cache.listeners['#_allKBInfoObjects'];
	if (listeners) {
		listeners = listeners.clone();
		for (var i=0; i<listeners.length; i++) {
			listeners[i](changedInfoObjects);
		}
	}
	// call all specific listeners for every changed info object
	for (var k=0; k<changedInfoObjects.length; k++) {
		var listeners = KBInfo._cache.listeners[changedInfoObjects[k].getID().toLowerCase()];
		if (listeners) {
			listeners = listeners.clone();
			for (var i=0; i<listeners.length; i++) {
				listeners[i]([changedInfoObjects[k]]);
			}
		}		
		var listeners = KBInfo._cache.listeners[changedInfoObjects[k].getName().toLowerCase()];
		if (listeners) {
			listeners = listeners.clone();
			for (var i=0; i<listeners.length; i++) {
				listeners[i]([changedInfoObjects[k]]);
			}
		}		
	}
}

/**
 * Registers a listener for cache update events. 
 * 
 * Optionally an object can be given. If done, the listener will
 * only be called for updates to this object.
 */
KBInfo.addCacheChangeListener = function(listener, nameOrID) {
	var key = nameOrID ? nameOrID.toLowerCase() : '#_allKBInfoObjects';
	if (KBInfo._cache.listeners[key]) {
		KBInfo._cache.listeners[key].push(listener);
	}
	else {
		KBInfo._cache.listeners[key] = [listener];
	}
}

/**
 * Removes a listener for cache update events. 
 * 
 * Optionally an object can be given. The method shall be called
 * with exactly the same signature as the listener has been registered.
 */
KBInfo.removeCacheChangeListener = function(listener, nameOrID) {
	var key = nameOrID ? nameOrID.toLowerCase() : '#_allKBInfoObjects';
	if (KBInfo._cache.listeners[key]) {
		KBInfo._cache.listeners[key].remove(listener);
	}
}


KBInfo.InfoObject = Class.create({
	initialize: function(xmlDom) {
		this.xmlDom = xmlDom;
		this.id = this.xmlDom.getAttribute('id');
		this.name = this.xmlDom.getAttribute('name');
		this.description = KBInfo._getNodeValueIfExists(this.xmlDom, 'description');
		//  we assume that every KBInfo may have child elements, 
		// even this is not true for flowcharts
		// For IE non-prototyype (non-extended) classes we use static method variant of Element.select
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
	 */
	getName: function() {
		return this.name;
	},
	
	/** 
	 * Returns the description of an InfoObject.
	 */
	getDescription: function() {
		return this.description;
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
		this.options = KBInfo._collectNodeValues(this.xmlDom, 'choice');
		this.text = KBInfo._getNodeValueIfExists(this.xmlDom, 'text');
		this.unit = KBInfo._getNodeValueIfExists(this.xmlDom, 'unit');
		
		var rangeNodes = this.xmlDom.getElementsByTagName('range');
		if (rangeNodes && rangeNodes.length>0) {
			this.range = [rangeNodes[0].getAttribute('min'), rangeNodes[0].getAttribute('max')];
		}
		else {
			this.range = null;
		}
	},
	
	getOptions: function() {
		return this.options;
	},
	
	isAbstract: function() {
		return this.is_abstract;
	},
	
	getText: function() {
		return this.text;
	},
	
	getType: function() {
		return this.type;
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
	

KBInfo.Flowchart = Class.create(
	KBInfo.InfoObject, {
	initialize: function($super, xmlDom) {
		$super(xmlDom);
		this.startNames = KBInfo._collectNodeValues(this.xmlDom, 'start');
		this.exitNames = KBInfo._collectNodeValues(this.xmlDom, 'exit');
	},
	
	getStartNames: function() {
		return this.startNames;
	},
	
	getExitNames: function() {
		return this.exitNames;
	},
	
	getClassInstance: function() {
		return KBInfo.Flowchart;
	},
	
	getIconURL: function() {
		return KBInfo.imagePath + 'flowchart.gif';
	}
	
});
KBInfo.Flowchart.getShortClassName = function() {
	return "Flowchart";
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
	

KBInfo.Article = Class.create(
	KBInfo.InfoObject, {
	initialize: function($super, xmlDom) {
		$super(xmlDom);
	},
	
	getClassInstance: function() {
		return KBInfo.Article;
	},

	getIconURL: function() {
		return KBInfo.imagePath + 'article.gif';
	}
});

KBInfo.Article.getShortClassName = function() {
	return "Article";
};





// and fianlly we load the sub-libraries
KBInfo.load();