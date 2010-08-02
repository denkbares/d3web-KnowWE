if(!window.console) var console = {};
if(!console.log) console.log = function(){};
if(!console.warn) console.warn = console.log;
if(!console.error) console.error = console.warn;
MooTools.upgradeLogLevel = "warn";

MooTools.upgradeLog = function() {
	if (console[this.upgradeLogLevel]) console[this.upgradeLogLevel].apply(console, arguments);
};

(function(){
	oldA = $A;
	window.$A = function(iterable, start, length){
		if (start != undefined && length != undefined) {
			MooTools.upgradeLog('1.1 > 1.2: $A no longer takes start and length arguments.');
			if (Browser.Engine.trident && $type(iterable) == 'collection'){
				start = start || 0;
				if (start < 0) start = iterable.length + start;
				length = length || (iterable.length - start);
				var array = [];
				for (var i = 0; i < length; i++) array[i] = iterable[start++];
				return array;
			}
			start = (start || 0) + ((start < 0) ? iterable.length : 0);
			var end = ((!$chk(length)) ? iterable.length : length) + start;
			return Array.prototype.slice.call(iterable, start, end);
		}
		return oldA(iterable);
	};


	var strs = ['Array', 'Function', 'String', 'RegExp', 'Number', 'Window', 'Document', 'Element', 'Elements'];
	for (var i = 0, l = strs.length; i < l; i++) {
		var type = strs[i];
		var natv = window[type];
		if (natv) {
			var extend = natv.extend;
			natv.extend = function(props){
				MooTools.upgradeLog('1.1 > 1.2: native types no longer use .extend to add methods to prototypes but instead use .implement. NOTE: YOUR METHODS WERE NOT IMPLEMENTED ON THE NATIVE ' + type.toUpperCase() + ' PROTOTYPE.');
				return extend.apply(this, arguments);
			};
		}
	}
})();

window.onDomReady = function(fn){
	MooTools.upgradeLog('1.1 > 1.2: window.onDomReady is no longer supported. Use window.addEvent("domready") instead');
	return this.addEvent('domready', fn);
};if (Browser.__defineGetter__) {
	Browser.__defineGetter__('hasGetter',function(){
		return true;
	});
}

if(Browser.hasGetter){ // webkit, gecko, opera support
	
	window.__defineGetter__('ie',function(){
		MooTools.upgradeLog('1.1 > 1.2: window.ie is deprecated. Use Browser.Engine.trident');
		return (Browser.Engine.name == 'trident') ? true : false;
	});
	window.__defineGetter__('ie6',function(){
		MooTools.upgradeLog('1.1 > 1.2: window.ie6 is deprecated. Use Browser.Engine.trident and Browser.Engine.version');
		return (Browser.Engine.name == 'trident' && Browser.Engine.version == 4) ? true : false;
	});
	window.__defineGetter__('ie7',function(){
		MooTools.upgradeLog('1.1 > 1.2: window.ie7 is deprecated. Use Browser.Engine.trident and Browser.Engine.version');
		return (Browser.Engine.name == 'trident' && Browser.Engine.version == 5) ? true : false;
	});
	window.__defineGetter__('gecko',function(){
		MooTools.upgradeLog('1.1 > 1.2: window.gecko is deprecated. Use Browser.Engine.gecko');
		return (Browser.Engine.name == 'gecko') ? true : false;
	});
	window.__defineGetter__('webkit',function(){
		MooTools.upgradeLog('1.1 > 1.2: window.webkit is deprecated. Use Browser.Engine.webkit');
		return (Browser.Engine.name == 'webkit') ? true : false;
	});
	window.__defineGetter__('webkit419',function(){
		MooTools.upgradeLog('1.1 > 1.2: window.webkit is deprecated. Use Browser.Engine.webkit and Browser.Engine.version');
		return (Browser.Engine.name == 'webkit' && Browser.Engine.version == 419) ? true : false;
	});
	window.__defineGetter__('webkit420',function(){
		MooTools.upgradeLog('1.1 > 1.2: window.webkit is deprecated. Use Browser.Engine.webkit and Browser.Engine.version');
		return (Browser.Engine.name == 'webkit' && Browser.Engine.version == 420) ? true : false;
	});
	window.__defineGetter__('opera',function(){
		MooTools.upgradeLog('1.1 > 1.2: window.opera is deprecated. Use Browser.Engine.presto');
		return (Browser.Engine.name == 'presto') ? true : false;
	});
} else {
	window[Browser.Engine.name] = window[Browser.Engine.name + Browser.Engine.version] = true;
	window.ie = window.trident;
	window.ie6 = window.trident4;
	window.ie7 = window.trident5;	
}
Array.implement({

	copy: function(start, length){
		MooTools.upgradeLog('1.1 > 1.2: Array.copy is deprecated. Use Array.splice');
		return $A(this, start, length);
	},

	remove : function(item){
		MooTools.upgradeLog('1.1 > 1.2: Array.remove is deprecated. Use Array.erase');
		return this.erase(item);
	},

	merge : function(array){
		MooTools.upgradeLog('1.1 > 1.2: Array.merge is deprecated. Use Array.combine');
		return this.combine(array);
	}

});
Function.implement({

	bindAsEventListener: function(bind, args){
		MooTools.upgradeLog('1.1 > 1.2: Function.bindAsEventListener is deprecated. Use bindWithEvent.');
		return this.bindWithEvent.call(this, bind, args);
	}

});

Function.empty = function(){
	MooTools.upgradeLog('1.1 > 1.2: Function.empty is now just $empty.');
};Hash.implement({

	keys : function(){
		MooTools.upgradeLog('1.1 > 1.2: Hash.keys is deprecated. Use Hash.getKeys');
		return this.getKeys();
	},

	values : function(){
		MooTools.upgradeLog('1.1 > 1.2: Hash.values is deprecated. Use Hash.getValues');
		return this.getValues();
	},

	hasKey : function(item){
		MooTools.upgradeLog('1.1 > 1.2: Hash.hasKey is deprecated. Use Hash.has');
		return this.has(item);
	},

	merge : function(properties){
		MooTools.upgradeLog('1.1 > 1.2: Hash.merge is deprecated. Use Hash.combine');
		return this.extend(properties);
	},

	remove: function(key){
		MooTools.upgradeLog('1.1 > 1.2: Hash.remove is deprecated. use Hash.erase');
		return this.erase(key);
	}

});

Object.toQueryString = function(obj){
	MooTools.upgradeLog('1.1 > 1.2: Object.toQueryString() is deprecated. use Hash.toQueryString() instead');
	$H(obj).each(function(item, key){
		if ($type(item) == 'object' || $type(item) == 'array'){
			obj[key] = item.toString();
		}
	});
	return Hash.toQueryString(obj);
};

var Abstract = function(obj){
	MooTools.upgradeLog('1.1 > 1.2: Abstract is deprecated. Use Hash');
	return new Hash(obj);
};Class.empty = function(){ 
	MooTools.upgradeLog('1.1 > 1.2: replace Class.empty with $empty');
	return $empty;
};

//legacy .extend support

(function(){
	var proto = function(obj) {
		var f = function(){
			return this;
		};
		f.prototype = obj;
		return f;
	};

	Class.prototype.extend = function(properties){
		MooTools.upgradeLog('1.1 > 1.2: Class.extend is deprecated. See the class Extend mutator.');
		var maker = proto(properties);
		var made = new maker();
		made.Extends = this;
		return new Class(made);
	};

	var __implement = Class.prototype.implement;
	Class.prototype.implement = function(){
		if (arguments.length > 1 && Array.every(arguments, Object.type)){
			MooTools.upgradeLog('1.1 > 1.2: Class.implement no longer takes more than one thing at a time, either MyClass.implement(key, value) or MyClass.implement(object) but NOT MyClass.implement(new Foo, new Bar, new Baz). See also: the class Implements mutator.');
			Array.each(arguments, function(argument){
				__implement.call(this, argument);
			}, this);
			return this;
		}
		return __implement.apply(this, arguments);
	};
})();(function(){

	var getPosition = Element.prototype.getPosition;
	var getCoordinates = Element.prototype.getCoordinates;

	function isBody(element){
		return (/^(?:body|html)$/i).test(element.tagName);
	};

	var getSize = Element.prototype.getSize;

	Element.implement({
	
		getSize: function(){
			MooTools.upgradeLog('1.1 > 1.2: NOTE: getSize is different in 1.2; it no longer returns values for size, scroll, and scrollSize, but instead just returns x/y values for the dimensions of the element.');
			var size = getSize.apply(this, arguments);
			return $merge(size, {
				size: size,
				scroll: this.getScroll(),
				scrollSize: this.getScrollSize()
			});
		},

		getPosition: function(relative){
			if (relative && $type(relative) == "array") {
				MooTools.upgradeLog('1.1 > 1.2: Element.getPosition no longer accepts an array of overflown elements but rather, optionally, a single element to get relative coordinates.');
				relative = null;
			}
			return getPosition.apply(this, [relative]);
		},

		getCoordinates: function(relative){
			if (relative && $type(relative) == "array") {
				MooTools.upgradeLog('1.1 > 1.2: Element.getCoordinates no longer accepts an array of overflown elements but rather, optionally, a single element to get relative coordinates.');
				relative = null;
			}
			return getCoordinates.apply(this, [relative]);
		}
	
	});

	Native.implement([Document, Window], {

		getSize: function(){
			MooTools.upgradeLog('1.1 > 1.2: NOTE: getSize is different in 1.2; it no longer returns values for size, scroll, and scrollSize, but instead just returns x/y values for the dimensions of the element.');
			var size;
			var win = this.getWindow();
			var doc = this.getDocument();
			doc = (!doc.compatMode || doc.compatMode == 'CSS1Compat') ? doc.html : doc.body;
			if (Browser.Engine.presto || Browser.Engine.webkit){
				size =  {x: win.innerWidth, y: win.innerHeight};
			} else {
				size = {x: doc.clientWidth, y: doc.clientHeight};
			}
			return $extend(size, {
				size: size,
				scroll: {x: win.pageXOffset || doc.scrollLeft, y: win.pageYOffset || doc.scrollTop},
				scrollSize: {x: Math.max(doc.scrollWidth, size.x), y: Math.max(doc.scrollHeight, size.y)}
			});
		}

	});

})();Event.keys = Event.Keys; // TODO
(function(){

	var toQueryString = Element.prototype.toQueryString;

	Element.implement({

		getFormElements: function(){
			MooTools.upgradeLog('1.1 > 1.2: Element.getFormElements is deprecated, use Element.getElements("input, textarea, select");'); 
			return this.getElements('input, textarea, select');
		},

		replaceWith: function(el){
			MooTools.upgradeLog('1.1 > 1.2: Element.replaceWith is deprecated, use Element.replaces instead.'); 
			el = $(el);
			this.parentNode.replaceChild(el, this);
			return el;
		},

		remove: function() {
			MooTools.upgradeLog('1.1 > 1.2: Element.remove is deprecated - use Element.dispose.');
			return this.dispose.apply(this, arguments);
		},

		getText: function(){
			MooTools.upgradeLog('1.1 > 1.2: Element.getText is deprecated - use Element.get("text").'); 
			return this.get('text');
		},

		setText: function(text){
			MooTools.upgradeLog('1.1 > 1.2: Element.setText is deprecated - use Element.set("text", text).'); 
			return this.set('text', text);
		},

		setHTML: function(){
			MooTools.upgradeLog('1.1 > 1.2: Element.setHTML is deprecated - use Element.set("html", HTML).'); 
			return this.set('html', arguments);
		},

		getHTML: function(){
			MooTools.upgradeLog('1.1 > 1.2: Element.getHTML is deprecated - use Element.get("html").'); 
			return this.get('html');
		},

		getTag: function(){
			MooTools.upgradeLog('1.1 > 1.2: Element.getTag is deprecated - use Element.get("tag").'); 
			return this.get('tag');
		},
	
		getValue: function(){
			MooTools.upgradeLog('1.1 > 1.2: Element.getValue is deprecated - use Element.get("value").');
			switch(this.getTag()){
				case 'select':
					var values = [];
					$each(this.options, function(option){
						if (option.selected) values.push($pick(option.value, option.text));
					});
					return (this.multiple) ? values : values[0];
				case 'input': if (!(this.checked && ['checkbox', 'radio'].contains(this.type)) && !['hidden', 'text', 'password'].contains(this.type)) break;
				case 'textarea': return this.value;
			}
			return false;
		},

		toQueryString: function(){
			MooTools.upgradeLog('1.1 > 1.2: warning Element.toQueryString is slightly different; inputs without names are excluded, inputs with type == submit, reset, and file are excluded, and inputs with undefined values are excluded.');
			return toQueryString.apply(this, arguments);
		}
	});
})();

Element.Properties.properties = {
	
	set: function(props){
		MooTools.upgradeLog('1.1 > 1.2: Element.set({properties: {}}) is deprecated; instead of properties, just name the values at the root of the object (Element.set({src: url})).');
		$H(props).each(function(value, property){
			this.set(property, value);
		}, this);
	}
	
};
Element.implement({

	setOpacity: function(op){
		MooTools.upgradeLog('1.1 > 1.2: Element.setOpacity is deprecated; use Element.setStyle("opacity", value).');
		return this.setStyle('opacity', op);
	}

});

Element.Properties.styles = {
	
	set: function(styles){
		MooTools.upgradeLog('1.1 > 1.2: Element.set("styles") no longer accepts a string as an argument. Pass an object instead.');
		if ($type(styles) == 'string'){
			styles.split(";").each(function(style){
				this.setStyle(style.split(":")[0], style.split(":")[1]);
			}, this);
		} else {
			this.setStyles(styles);
		}
	}
	
};Fx.implement({

	custom: function(from, to){
		MooTools.upgradeLog('1.1 > 1.2: Fx.custom is deprecated. use Fx.start.');
		return this.start(from, to);
	},

	clearTimer: function(){
		MooTools.upgradeLog('1.1 > 1.2: Fx.clearTimer is deprecated. use Fx.cancel.');
		return this.cancel();
	},

	stop: function(){
		MooTools.upgradeLog('1.1 > 1.2: Fx.stop is deprecated. use Fx.cancel.');
		return this.cancel();
	}

});

Fx.Base = new Class({
	Extends: Fx,
	initialize: function(){
		MooTools.upgradeLog('1.1 > 1.2: Fx.Base is deprecated. use Fx.');
		this.parent.apply(this, arguments);
	}
});
Fx.Style = new Class({
	Extends: Fx.Tween,
	initialize: function(element, property, options){
		MooTools.upgradeLog('1.1 > 1.2: Fx.Style is deprecated. use Fx.Tween.');
		this.property = property;
		this.parent(element, options);
	},
	
	start: function(from, to) {
		return this.parent(this.property, from, to);
	},
	
	set: function(to) {
		return this.parent(this.property, to);
	},
	
	hide: function(){
		MooTools.upgradeLog('1.1 > 1.2: Fx.Style .hide() is deprecated; use Fx.Tween .set(0) instead');
		return this.set(0);
	}

});

Element.implement({

	effect: function(property, options){
		MooTools.upgradeLog('1.1 > 1.2: Element.effect is deprecated; use Fx.Tween or Element.tween.');
		return new Fx.Style(this, property, options);
	}

});
Fx.Styles = new Class({
	Extends: Fx.Morph,
	initialize: function(){
		MooTools.upgradeLog('1.1 > 1.2: Fx.Styles is deprecated. use Fx.Morph.');
		this.parent.apply(this, arguments);
	}
});

Element.implement({

	effects: function(options){
		MooTools.upgradeLog('1.1 > 1.2: Element.effects is deprecated; use Fx.Morph or Element.morph.');
		return new Fx.Morph(this, options);
	}

});Fx.Scroll.implement({

	scrollTo: function(y, x){
		MooTools.upgradeLog('1.1 > 1.2: Fx.Scroll\'s .scrollTo is deprecated; use .start.');
		return this.start(y, x);
	}

});Request.implement({
	//1.11 passed along the response text and xml to onComplete
	onStateChange: function(){
		if (this.xhr.readyState != 4 || !this.running) return;
		this.running = false;
		this.status = 0;
		$try(function(){
			this.status = this.xhr.status;
		}.bind(this));
		this.xhr.onreadystatechange = $empty;
		this.response = {text: this.xhr.responseText, xml: this.xhr.responseXML};
		if (this.options.isSuccess.call(this, this.status)) this.success(this.response.text, this.response.xml);
		else this.failure(this.response.text, this.response.xml);
	},
	
	failure: function(){
		this.onFailure.apply(this, arguments);
	},

	onFailure: function(){
		MooTools.upgradeLog('1.1 > 1.2: Note that onComplete does not receive arguments in 1.2. Also note that onComplete is invoked on BOTH success and failure (while in 1.1 it was only invoked on success). Use the onSuccess event instead if you wish to limit this invocation to success.');
		this.fireEvent('complete', arguments).fireEvent('failure', this.xhr);
	}

});

var XHR = new Class({

	Extends: Request,

	options: {
		update: false
	},

	initialize: function(options){
		MooTools.upgradeLog('1.1 > 1.2: XHR is deprecated. Use Request.');
		this.parent(options);
		this.transport = this.xhr;
	},

	request: function(data){
		MooTools.upgradeLog('1.1 > 1.2: XHR.request() is deprecated. Use Request.send() instead.');
		return this.send(this.url, data || this.options.data);
	},

	send: function(url, data){
		if (!this.check(arguments.callee, url, data)) return this;
		return this.parent({url: url, data: data});
	},

	success: function(text, xml){
		text = this.processScripts(text);
		if (this.options.update) $(this.options.update).empty().set('html', text);
		this.onSuccess(text, xml);
	},

	failure: function(){
		this.fireEvent('failure', this.xhr);
	}

});


var Ajax = new Class({

	Extends: XHR,

	initialize: function(url, options){
		MooTools.upgradeLog('1.1 > 1.2: Ajax is deprecated. Use Request.');
		this.url = url;
		this.parent(options);
	},

	success: function(text, xml){
		// This version processes scripts *after* the update element is updated, like Mootools 1.1's Ajax class
		// Partially from Remote.Ajax.success
		this.processScripts(text);
		response = this.response;
		response.html = text.stripScripts(function(script){
				response.javascript = script;
		});
		if (this.options.update) $(this.options.update).empty().set('html', response.html);
		if (this.options.evalScripts) $exec(response.javascript);
		this.onSuccess(text, xml);
	}

});

(function(){
	var send = Element.prototype.send;
	Element.implement({
		send: function(url) {
			if ($type(url) == "string") return send.apply(this, arguments);
			if ($type(url) == "object") {
				MooTools.upgradeLog('1.1 > 1.2: Element.send no longer takes an options argument as its object but rather a url. See docs.');
				this.set('send', url);
				send.call(this);
			}
			return this;
		}
	});
})();JSON.Remote = new Class({

	options: {
		key: 'json'
	},

	Extends: Request.JSON,

	initialize: function(url, options){
		MooTools.upgradeLog('JSON.Remote is deprecated. Use Request.JSON');
		this.parent(options);
		this.onComplete = $empty;
		this.url = url;
	},

	send: function(data){
		if (!this.check(arguments.callee, data)) return this;
		return this.parent({url: this.url, data: {json: Json.encode(data)}});
	},

	failure: function(){
		this.fireEvent('failure', this.xhr);
	}

});

Cookie.set = function(key, value, options){
	MooTools.upgradeLog('1.1 > 1.2: Cookie.set is deprecated. Use Cookie.write');
	return new Cookie(key, options).write(value);
};

Cookie.get = function(key){
	MooTools.upgradeLog('1.1 > 1.2: Cookie.get is deprecated. Use Cookie.read');
	return new Cookie(key).read();
};

Cookie.remove = function(key, options){
	MooTools.upgradeLog('1.1 > 1.2: Cookie.remove is deprecated. Use Cookie.dispose');
	return new Cookie(key, options).dispose();
};
JSON.toString = function(obj){ 
	MooTools.upgradeLog('1.1 > 1.2: JSON.toString is deprecated. Use JSON.encode');
	return JSON.encode(obj); 
};
JSON.evaluate = function(str){
	MooTools.upgradeLog('1.1 > 1.2: JSON.evaluate is deprecated. Use JSON.decode');
	return JSON.decode(str); 
};
var Json = JSON;

Native.implement([Element, Document], {

	getElementsByClassName: function(className){
		MooTools.upgradeLog('1.1 > 1.2: Element.filterByTag is deprecated.');
		
		return this.getElements('.' + className);
	},

	getElementsBySelector: function(selector){
		MooTools.upgradeLog('1.1 > 1.2: Element.getElementsBySelector is deprecated. Use getElements()');
		return this.getElements(selector);
	}

});

Elements.implement({

	filterByTag: function(tag){
		MooTools.upgradeLog('1.1 > 1.2: Elements.filterByTag is deprecated. Use Elements.filter.');
		return this.filter(tag);
	},

	filterByClass: function(className){
		MooTools.upgradeLog('1.1 > 1.2: Elements.filterByClass is deprecated. Use Elements.filter.');
		return this.filter('.' + className);
	},

	filterById: function(id){
		MooTools.upgradeLog('1.1 > 1.2: Elements.filterById is deprecated. Use Elements.filter.');
		return this.filter('#' + id);
	},

	filterByAttribute: function(name, operator, value){
		MooTools.upgradeLog('1.1 > 1.2: Elements.filterByAttribute is deprecated. Use Elements.filter.');
		var filtered = this.filter('[' + name + (operator || '') + (value || '') + ']');
		if (value) filtered = filtered.filter('[' + name + ']');
		return filtered;
	}

});

var $E = function(selector, filter){
	MooTools.upgradeLog('1.1 > 1.2: $E is deprecated, use document.getElement.');
	return ($(filter) || document).getElement(selector);
};

var $ES = function(selector, filter){
	MooTools.upgradeLog('1.1 > 1.2: $ES is deprecated. Use $$.');
	return ($(filter) || document).getElements(selector);
};(function(){
	if (!window.Tips) return;

	Tips.implement({

		initialize: function(){
			MooTools.upgradeLog('1.1 > 1.2: Tips DOM element layout has changed and your CSS classes may need to change.');
			var params = Array.link(arguments, {options: Object.type, elements: $defined});
			this.setOptions(params.options);
			if (this.options.offsets) {
				MooTools.upgradeLog('1.1 > 1.2: Tips no longer have an "offsets" option; use "offset".');
				this.options.offset = this.options.offsets;
			}
			document.id(this);
			this.addEvent('show', function(){
				this.tip.addClass('tool-tip');
				this.tip.getElement('.tip-title').addClass('tool-title');
				this.tip.getElement('.tip-text').addClass('tool-text');
			});
			this.parseTitle(params.elements);
			if (params.elements) this.attach(params.elements);
		},

		parseTitle: function(elements){
			elements.each(function(element){
			var title = element.get('title');
				if (title.test('::')) {
					MooTools.upgradeLog('1.1 > 1.2: Tips no longer parse the title attribute for "::" for title/caption; use title and rel attributes instead.');
					element.store('tip:title', title.split('::')[0]);
					element.store('tip:text', title.split('::')[1]);
					element.set('title', '');
				}
			});
		}

	});

})();