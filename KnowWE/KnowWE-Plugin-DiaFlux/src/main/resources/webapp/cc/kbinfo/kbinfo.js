var KBInfo = { 
		
	imagePath: "cc/image/kbinfo/",
	cachePrefix : "#DiaFlux_"
	
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
	if (!xmlDom) return;
	var changed = [];
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
};

KBInfo._cache = { byID: [], byName: [], listeners: {} };

KBInfo._addToChache = function(infoObject) {
	KBInfo._cache.byID[KBInfo.createKey(infoObject.getID())] = infoObject;
	if (infoObject.getClassInstance() != KBInfo.Article) {
		KBInfo._cache.byName[KBInfo.createKey(infoObject.getName())] = infoObject;
	}
};

/**
 * creates the key for for name/InfoObject pairs in cache
 */
KBInfo.createKey = function(name) {
	return KBInfo.cachePrefix + name.toLowerCase();
};

/**
 * Gives the cache the hint that this info object(s) will be required later on.
 * The framework can prepare itself to download the information from the server
 * (if it is not available yet and put it into the cache when it is received.
 * The function will always return immediately.
 */
KBInfo.prepareInfoObject = function(nameOrIDOrArray) {
	if (!nameOrIDOrArray) return;
	if (DiaFluxUtils.isString(nameOrIDOrArray)) nameOrIDOrArray = [nameOrIDOrArray];
	var url = "KnowWE.jsp?action=GetInfoObjects";
	new Ajax.Request(url, {
		method: 'post',
		parameters: {ids: JSON.stringify(nameOrIDOrArray), sectionID: nodeID},
		onSuccess: function(transport) {
		
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
	var phraseParam = '&phrase='+encodeURIComponent(phrase);
	var maxCountParam = maxCount ? '&maxcount='+maxCount : '';
	var sectionID = '&sectionID='+nodeID;
	var url = "KnowWE.jsp?action=SearchInfoObjects" 
			+ classesParam 
			+ phraseParam
			+ maxCountParam
			+ sectionID;
	new Ajax.Request(url, {
		method: 'get',
		onSuccess: function(transport) {
			// find root node
			var root = transport.responseXML;
			while (root && root.nodeName.toLowerCase() != 'matches') {
				root = root.firstChild;
			}
			// extract result
			var result = {
				count: root ? root.getAttribute('count') : 0,
				hasMore: root ? (root.getAttribute('hasmore') == 'true') : false,
				matches: root ? KBInfo._collectNodeValues(root, 'match') : []
			};
			// request the info objects for the result in advance
			KBInfo.prepareInfoObject(nodeID, result.matches);
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
	return KBInfo._cache.byID[KBInfo.createKey(nameOrIDOrArray)] 
		|| KBInfo._cache.byName[KBInfo.createKey(nameOrIDOrArray)];
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
 * Returns an array of all currently cached KBInfo objects, 
 * that meets the given condition function. 
 */
KBInfo.findInfoObjects = function(conditionFx, searchedValue, maxCount) {
	var result = [];
	var count = 0;
	for (var key in KBInfo._cache.byID) {
		var value = KBInfo._cache.byID[key];
		if (!value) continue;
		if (!value.getClassInstance) continue;
		
		//Even if we have enough hits according to maxCount
		//we have to make sure that the searched InfoObject is included if it comes later
		//e.g. looking for question 'f' is not possible, if many objects contain an f
		if (count >= maxCount)  {
			if (searchedValue.toLowerCase() == value.name.toLowerCase()) {
				result.push(value);
				break;
			}
		}
		else if (conditionFx(value)) {
			var isNew = true;
			for (var i=0; i<result.length; i++) {
				var old = result[i];
				// check if it is the same object class
				if (old.name != value.name) continue;
				if (old.getClassInstance() != value.getClassInstance()) continue;
				// also test for question if they are of the same type 
				// for all other objects it is undefined an therefore '=='
				if (old.type != value.type) continue;
				// if all is same, ignore the new item
				isNew = false;
				break;
			}
			if (isNew) {
				result.push(value);
				count++;
			}
		}
	}
	result.sort(function (a, b) {
		var n1 = a.name.toLowerCase();
		var n2 = b.name.toLowerCase();
		if (n1 > n2) return 1;
		if (n1 < n2) return -1;
		if (a.id > b.id) return 1;
		if (a.id < b.id) return -1;
		return 0;
	});
	return result;
};

KBInfo._fireCacheChangeEvent = function(changedInfoObjects) {
	// call all general Listeners
	var listeners = KBInfo._cache.listeners['#_allKBInfoObjects'];
	if (listeners) {
		listeners = listeners.slice();
		for (var i=0; i<listeners.length; i++) {
			listeners[i](changedInfoObjects);
		}
	}
	// call all specific listeners for every changed info object
	for (var k=0; k<changedInfoObjects.length; k++) {
		var listeners = KBInfo._cache.listeners[KBInfo.createKey(changedInfoObjects[k].getID())];
		if (listeners) {
			listeners = listeners.slice();
			for (var i=0; i<listeners.length; i++) {
				listeners[i]([changedInfoObjects[k]]);
			}
		}		
		var listeners = KBInfo._cache.listeners[KBInfo.createKey(changedInfoObjects[k].getName())];
		if (listeners) {
			listeners = listeners.slice();
			for (var i=0; i<listeners.length; i++) {
				listeners[i]([changedInfoObjects[k]]);
			}
		}		
	}
};

/**
 * Registers a listener for cache update events. 
 * 
 * Optionally an object can be given. If done, the listener will
 * only be called for updates to this object.
 */
KBInfo.addCacheChangeListener = function(listener, nameOrID) {
	var key = nameOrID ? KBInfo.createKey(nameOrID) : '#_allKBInfoObjects';
	if (KBInfo._cache.listeners[key]) {
		KBInfo._cache.listeners[key].push(listener);
	}
	else {
		KBInfo._cache.listeners[key] = [listener];
	}
};

/**
 * Removes a listener for cache update events. 
 * 
 * Optionally an object can be given. The method shall be called
 * with exactly the same signature as the listener has been registered.
 */
KBInfo.removeCacheChangeListener = function(listener, nameOrID) {
	var key = nameOrID ? KBInfo.createKey(nameOrID) : '#_allKBInfoObjects';
	if (KBInfo._cache.listeners[key]) {
		KBInfo._cache.listeners[key].remove(listener);
	}
};



KBInfo.InfoObject = function(xmlDom) {
	if (!xmlDom)
		return;
	
	this.xmlDom = xmlDom;
	this.id = this.xmlDom.getAttribute('id');
	this.name = this.xmlDom.getAttribute('name');
	this.description = KBInfo._getNodeValueIfExists(this.xmlDom, 'description');
	//  we assume that every KBInfo may have child elements, 
	// even this is not true for flowcharts
	// For IE non-prototyype (non-extended) classes we use static method variant of Element.select
	this.childs = KBInfo._collectNodeValues(this.xmlDom, 'child');
};
	
/** 
 * Returns the class instance of this KBInfo object.
 */
KBInfo.InfoObject.prototype.getClassInstance = function() {
	//throw("KBInfo.InfoObject.getClassInstance() must be overwritten");
};
	
/** 
 * Returns the id of the referenced knowledgebase object.
 */
KBInfo.InfoObject.prototype.getID = function() {
	return this.id;
};
	
/** 
 * Returns the short name of an InfoObject.
 */
KBInfo.InfoObject.prototype.getName = function() {
	return this.name;
};
	
/** 
 * Returns the description of an InfoObject.
 */
KBInfo.InfoObject.prototype.getDescription = function() {
	return this.description;
};
	
/** 
 * Returns the child objects of an InfoObject.
 */
KBInfo.InfoObject.prototype.getChilds = function() {
	return this.childs;
};
	
/** 
 * Returns the url to the icon of this KBInfo object.
 */
KBInfo.InfoObject.prototype.getIconURL = function() {
	return null;
};
	
/** 
 * Returns the desired ToolTip-Text of an InfoObject.
 */
KBInfo.InfoObject.prototype.getToolTip = function() {
	return this.name+' (#'+this.id+')'+
		(this.text || this.description ? ': ' : '') + 
		(this.text ? (' \n' + this.text) : '') + 
		(this.description ? (' \n' + this.description) : '');
};



KBInfo.Question = function(xmlDom) {
	KBInfo.InfoObject.call(this, xmlDom);
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
};

/***/
KBInfo.Question.prototype = new KBInfo.InfoObject();  
	
KBInfo.Question.prototype.getOptions = function() {
	return this.options;
},

KBInfo.Question.prototype.isAbstract = function() {
	return this.is_abstract;
};

KBInfo.Question.prototype.getText = function() {
	return this.text;
};

KBInfo.Question.prototype.getType = function() {
	return this.type;
};

KBInfo.Question.prototype.getClassInstance = function() {
	return KBInfo.Question;
};

KBInfo.Question.prototype.getIconURL = function() {
	var file = 
		(this.type == KBInfo.Question.TYPE_OC) ? 'single' :
		(this.type == KBInfo.Question.TYPE_MC) ? 'multiple' :
		(this.type == KBInfo.Question.TYPE_BOOL) ? 'yesno' :
		(this.type == KBInfo.Question.TYPE_NUM) ? 'num' :
		(this.type == KBInfo.Question.TYPE_DATE) ? 'date' :
		'text';
	if (this.isAbstract()) file += '-abstract';
	return KBInfo.imagePath + file + '.gif';
};

KBInfo.Question.getShortClassName = function() {
	return "Question";
};

KBInfo.Question.TYPE_OC =	"oc";
KBInfo.Question.TYPE_MC =	"mc";
KBInfo.Question.TYPE_BOOL =	"bool";
KBInfo.Question.TYPE_NUM =	"num";
KBInfo.Question.TYPE_DATE =	"date";
KBInfo.Question.TYPE_TEXT =	"text";




KBInfo.Solution = function(xmlDom) {
	KBInfo.InfoObject.call(this, xmlDom);

};
/****/
KBInfo.Solution.prototype = new KBInfo.InfoObject();  
	
KBInfo.Solution.prototype.getClassInstance = function() {
	return KBInfo.Solution;
};

KBInfo.Solution.prototype.getIconURL = function() {
	return KBInfo.imagePath + 'diagnosis.gif';
};

KBInfo.Solution.getShortClassName = function() {
	return "Solution";
};
	


KBInfo.Flowchart = function(xmlDom) {
	KBInfo.InfoObject.call(this, xmlDom);
	this.startNames = KBInfo._collectNodeValues(this.xmlDom, 'start');
	this.exitNames = KBInfo._collectNodeValues(this.xmlDom, 'exit');
	this.icon = this.xmlDom.getAttribute('icon');
};

/***/
KBInfo.Flowchart.prototype = new KBInfo.InfoObject();  

KBInfo.Flowchart.prototype.getStartNames = function() {
	return this.startNames;
};
	
KBInfo.Flowchart.prototype.getExitNames = function() {
	return this.exitNames;
};
	
KBInfo.Flowchart.prototype.getClassInstance = function() {
	return KBInfo.Flowchart;
};
	
KBInfo.Flowchart.prototype.getIconURL = function() {
	var iconName = this.icon || 'flowchart.gif';
	return KBInfo.imagePath + iconName;
};
	
KBInfo.Flowchart.getShortClassName = function() {
	return "Flowchart";
};



KBInfo.QSet = function(xmlDom) {
	KBInfo.InfoObject.call(this, xmlDom);
};

/***/
KBInfo.QSet.prototype = new KBInfo.InfoObject(); 

KBInfo.QSet.prototype.getClassInstance = function() {
	return KBInfo.QSet;
};

KBInfo.QSet.prototype.getIconURL = function() {
	return KBInfo.imagePath + 'qset.gif';
};

KBInfo.QSet.getShortClassName = function() {
	return "QSet";
};
	


KBInfo.Article = function(xmlDom) {
	KBInfo.InfoObject.call(this, xmlDom);

};

/****/
KBInfo.Article.prototype = new KBInfo.InfoObject(); 
	
KBInfo.Article.prototype.getClassInstance = function() {
	return KBInfo.Article;
};

KBInfo.Article.prototype.getIconURL = function() {
	return KBInfo.imagePath + 'article.gif';
};

KBInfo.Article.getShortClassName = function() {
	return "Article";
};


