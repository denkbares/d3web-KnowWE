// SilverIPE: A simple In Place Editor Class
// By: Jean-Nicolas Jolivet (http://www.silverscripting.com)
// Released under the MIT License
// Version: 0.2

var SilverIPE = function (eb,el, url, options) {
	this.url = url;
	var that = this;
	
	// If el is a string, we get the element by this id, if not, assume it's an
	// element
	this.el = (typeof el === 'string') ? document.getElementById(el) : el;
	this.eb = (typeof eb === 'string') ? document.getElementById(eb) : eb;
	this.el.title = (this.el.title === '') ? 'Click to edit...' : this.el.title;
	// Remember the original BG color if it's not set, default to transparent
	this.originalBg = (this.el.style.backgroundColor === '') ? 'transparent' : this.el.style.backgroundColor;
	
	// define the default options...
	this.options = {
		parameterName: 'value',
		method: 'POST',
		highlightColor: '#FFFFBF',
		borderColor: '#000',
		savingText: 'saving...',
		saveButtonLabel: 'save',
		cancelButtonLabel: 'cancel',
		textWidth: 20,
		textHeight: 4,
		additionalParameters: {}
	};
	
	this.options = this.mergeObjects(this.options, options || {});
	this.options.method = this.options.method.toUpperCase();
	this.el.onmouseover = function () {
		// that.el.style.backgroundColor = that.options.highlightColor;
	};
	this.eb.onmouseover = function () {
		that.el.style.backgroundColor = that.options.highlightColor;
	};

	this.el.onmouseout = function () {
		// that.el.style.backgroundColor = that.originalBg;
	};
	this.eb.onmouseout = function () {
		that.el.style.backgroundColor = that.originalBg;
	};
	
	this.el.onclick = function () {	
		// that.elClicked.call(that);
	};
	this.eb.onclick = function () {	
		that.elClicked.call(that);
	};

	this.buildElements();
};

SilverIPE.prototype.buildElements = function () {
	var parentEl = this.el.parentNode;
	var that = this;
	
	// For inline elements, we use a text input
	if (this.el.tagName.toLowerCase() === 'span' || ((this.el.tagName.toLowerCase() === 'div' || this.el.tagName.toLowerCase() === 'p') && this.el.style.display === 'inline')) {
		this.inputEl = document.createElement("input");
		this.inputEl.type = "text";
		this.inputEl.size = this.options.textWidth;
		this.originalDisplay = 'inline';
	}
	// For block elements, use a textarea
	else if ((this.el.tagName.toLowerCase() === 'div' || this.el.tagName.toLowerCase() === 'p') || 
	(this.el.tagName.toLowerCase() && this.el.style.display === 'block')) 
	{
		this.inputEl = document.createElement("textarea");
		this.inputEl.cols = this.options.textWidth;
		this.inputEl.rows = this.options.textHeight;
		this.originalDisplay = (this.el.style.display === '') ? 'block' : this.el.style.display;
	}
	
	this.inputEl.style.display = 'none';
	this.inputEl.style.border = '1px dashed ' + this.options.borderColor;
	this.inputEl.style.backgroundColor = this.options.highlightColor;
	this.inputEl.onkeypress = function (e){
		var characterCode 
		if(e && e.which){ 
		e = e
		characterCode = e.which 
		}
		else{
		e = event
		characterCode = e.keyCode 
		}
		if(characterCode == 13){ 
			that.saveClicked.call(that);
		return false
		}
		else{
		return true
		}
		}
 
	parentEl.insertBefore(this.inputEl, this.el.nextSibling);
	this.saveButton = document.createElement('a');
	this.saveButton.innerHTML = this.options.saveButtonLabel;
	this.setCommonStyles(this.saveButton);	
	this.saveButton.onclick = function () {
		that.saveClicked.call(that);
		return false;
	};
	


	
	parentEl.insertBefore(this.saveButton, this.inputEl.nextSibling);
	
	this.cancelButton = document.createElement('a');
	this.cancelButton.innerHTML = this.options.cancelButtonLabel;
	this.setCommonStyles(this.cancelButton);
	this.cancelButton.onclick = function () {
		that.cancelClicked.call(that);
		return false;
	};
	parentEl.insertBefore(this.cancelButton, this.saveButton.nextSibling);
};

SilverIPE.prototype.cancelClicked = function () {
	this.hideIpe();

};


SilverIPE.prototype.saveClicked = function () {
	if (this.inputEl.value !== this.lastValue)
	{
		this.el.innerHTML = this.options.savingText;
		this.hideIpe();
		this.request = this.requestFactory();
		var that = this;
		var additionalData = this.extractParams();
		var data = this.options.parameterName + '=' + this.inputEl.value + additionalData;
		var url = this.options.method === "POST" ? this.url : this.url + "?" + data;
		
		this.request.onreadystatechange = function () {
			that.handleRequest.call(that);
		};
		
		this.request.open(this.options.method, url, true);
		this.request.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
		this.request.setRequestHeader('Accept', 'text/javascript, text/html, application/xml, text/xml, */*');
		if(this.options.method === "POST") {
			this.request.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
			this.request.setRequestHeader("Content-length", data.length);
			this.request.setRequestHeader("Connection", "close");
		}
		this.request.send(data);
		

	}
	else {
		this.cancelClicked();
	}
	
};

SilverIPE.prototype.extractParams = function() {
	var params = "";
	for(param in this.options.additionalParameters) {
		if(typeof param !== "function") {
			params = params + "&" + param + "=" + this.options.additionalParameters[param];
		}
	}
	return params;
}


SilverIPE.prototype.hideIpe = function () {
	this.inputEl.style.display = 'none';
	this.saveButton.style.display = 'none';
	this.cancelButton.style.display = 'none';
	this.el.style.display = this.originalDisplay;
};


SilverIPE.prototype.setCommonStyles = function (el) {
	el.href = '#';
	el.style.fontFamily = 'arial, sans-serif';
	el.style.fontSize = '11px';
	el.style.display = 'none';
	el.style.margin = '0 4px';
};

function stripHTML(string) {
	return string.replace(/<(.|\n)*?>/g, '');
}

SilverIPE.prototype.elClicked = function (test) {
	var strValue = this.trimString(this.el.innerHTML);
	strValue = stripHTML(strValue);
	this.inputEl.value = strValue;
	
	this.lastValue = this.inputEl.value;
	this.showIpe();
	this.inputEl.focus();
};

SilverIPE.prototype.showIpe = function () {
	this.el.style.display = 'none';
	this.inputEl.style.display = 'inline';
	this.saveButton.style.display = 'inline';
	this.cancelButton.style.display = 'inline';
};

SilverIPE.prototype.highlight = function (el, origColor, highColor) {
	var that = this;
	setTimeout(function () {
		that.doHighlight.call(that, el, highColor);
	}, 75);
	setTimeout(function () {
		that.doHighlight.call(that, el, origColor);
	}, 150);
	setTimeout(function () {
		that.doHighlight.call(that, el, highColor);
	}, 225);
	setTimeout(function () {
		that.doHighlight.call(that, el, origColor);
	}, 300);
};

SilverIPE.prototype.doHighlight = function (el, color) {
	el.style.backgroundColor = color;
};

// Internal function to merge 2 objects...
SilverIPE.prototype.mergeObjects = function (orig, ext) {
	var name;
	for (name in ext || {}) {
		if (typeof name !== "function") {
			orig[name] = ext[name];
		}
	}
	return orig;
};

SilverIPE.prototype.trimString = function(str) {
	return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
};


// XMLHttpRequest Related Functions
SilverIPE.prototype.requestFactory = function () {
	// Thanks to WikiPedia for the XMLHttpRequest factory snippet...
	if (typeof XMLHttpRequest === "undefined")
	{
		XMLHttpRequest = function () {
			try { 
				return new ActiveXObject("Msxml2.XMLHTTP.6.0"); 
			} 
			catch (e) {}
			try { 
				return new ActiveXObject("Msxml2.XMLHTTP.3.0"); 
			} catch (e) {}
			try { 
				return new ActiveXObject("Msxml2.XMLHTTP"); 
			} catch (e) {}
			try { 
				return new ActiveXObject("Microsoft.XMLHTTP"); 
			}  catch (e) {}
			throw new Error("This browser does not support XMLHttpRequest or XMLHTTP.");
		};
	}
	var request = new XMLHttpRequest();
	return request;
};

SilverIPE.prototype.handleRequest = function () {
	if (this.request.readyState === 4) {
		if (this.request.status === 200) {
			// We got a successfull response...
			this.el.innerHTML = this.request.responseText;
			this.hideIpe();
			this.highlight(this.el, this.originalBg, this.options.highlightColor);
		}
		else { // an error...
			this.el.innerHTML = 'Error ' + this.request.status + ": " + this.request.statusText;
			
			this.highlight(this.el, this.originalBg, '#FF8282');
		}
	}
};

