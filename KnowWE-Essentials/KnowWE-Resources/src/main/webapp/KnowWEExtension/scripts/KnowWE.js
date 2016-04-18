/*
 * Copyright (C) 2014 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/**
 * Title: KnowWE-core
 * Contains javascript functions the KnowWE core needs to functions properly.
 * The functions are based upon some KnowWE helper functions and need the
 * KNOWWE-helper.js in order to work correct.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	/**
	 * The KNOWWE global namespace object.  If KNOWWE is already defined, the
	 * existing KNOWWE object will not be overwritten so that defined
	 * namespaces are preserved.
	 */
	var KNOWWE = {};
}
/**
 * Namespace: KNOWWE.core
 * The KNOWWE core namespace.
 * Contains some init functions.
 */
KNOWWE.core = function() {
	return {
		/**
		 * Function: init
		 * Core init functions.
		 */
		init : function() {
			KNOWWE.core.util.init();
			KNOWWE.core.actions.init();
			KNOWWE.core.rerendercontent.init();
			setTimeout(function() {
				KNOWWE.helper.observer.notify('onload')
			}, 50);
		},

		restoreThisVersion : function() {
			var version = document.getElementById('version').value;
			var params = {
				restoreThisVersion : version,
				action : 'RestoreAction'
			};
			var options = {
				url : KNOWWE.core.util.getURL(params),
				loader : true,
				response : {
					fn : function() {
						window.location = "Wiki.jsp?page=" + this.responseText
					},
					onError : _EC.onErrorBehavior
				}
			};
			new _KA(options).send();
		}
	}
}();
/**
 * Namespace: KNOWWE.core.actions
 * The KNOWWE actions namespace object.
 * Contains all actions that can be triggered in KnowWE per javascript.
 */
KNOWWE.core.actions = function() {
	return {
		/**
		 * Function: init
		 * Core KnowWE actions.
		 */
		init : function() {
			//init show extend panel
			els = _KS('.show-extend');
			if (els) {
				els.each(function(element) {
					_KE.add('click', element, KNOWWE.core.util.form.showExtendedPanel);
				});
			}

			//enable clearHTML
			_KS('.clear-element').each(function(element) {
				_KE.add('click', element, KNOWWE.core.actions.clearHTML);
			});

			_KS('.js-cell-change').each(function(element) {
				_KE.add('change', element, KNOWWE.core.actions.cellChanged);
			});
		},
		/**
		 * Function: clearHTML
		 * Clears the inner HTML of a given element.
		 *
		 * Parameters:
		 *     e - The occurred event.
		 */
		clearHTML : function(e) {
			var el = KNOWWE.helper.event.target(e);
			if (el.id) {
				_KS(el.id)._clear();
			}
		},
		/**
		 * Function: cellChanged
		 *
		 * Parameters:
		 *     e - The occurred event.
		 */
		cellChanged : function(e) {
			var el = KNOWWE.helper.event.target(e);
			var rel = el.getAttribute('rel');

			if (!rel) return;
			rel = rel.parseToObject();

			var nodeID = rel.id;
			var topic = rel.title;

			el = _KS('#' + nodeID);
			if (el) {
				var selectedOption = el.options[el.selectedIndex].value;

				var params = {
					action : 'ReplaceKDOMNodeAction',
					TargetNamespace : nodeID,
					KWikitext : selectedOption,
					KWiki_Topic : topic
				};
				var options = {
					url : KNOWWE.helper.getURL(params),
					response : {
						action : none,
						fn : null
					}
				};
				new _KA(options).send();
			}
		}
	}
}();

/**
 * Namespace: KNOWWE.core.util
 * The KNOWWE core util namespace object.
 * Contains some helper functions. For detailed information read the comments
 * above each function.
 */
KNOWWE.core.util = function() {

	var activityCounter = 0;

	return {

		init : function() {
			KNOWWE.core.util.addCollabsiblePluginHeader();
		},

		/**
		 * Function updateProcessingState
		 *
		 * Updates the hidden element in the page to contain
		 * the current processing state
		 */
		updateProcessingState : function(delta) {
			activityCounter += delta;
			var indicator = jq$('#KnowWEProcessingIndicator');
			if (activityCounter > 0) {
				// to reduce flicker, we wait a bit
				window.setTimeout(function() {
					// if counter still positive after timeout, show indicator...
					if (activityCounter > 0) {
						indicator.attr('state', 'processing');
						indicator.show();
					}
				}, 200);
			}
			else {
				indicator.hide();
				indicator.attr('state', 'idle');
			}
		},
		showProcessingIndicator : function() {
			_KU.updateProcessingState(1);
		},
		hideProcessingIndicator : function() {
			_KU.updateProcessingState(-1);
		},
		/**
		 * Function: addCollabsiblePluginHeader
		 * Extends the headings of the KnowWEPlugin DIVs with collabs ability.
		 * The function searches for all DIVs with an ".panel" class attribute and
		 * extends them. The plugin DIV should have the following structure in order
		 * to work properly:
		 * (start code)
		 * <div class='panel'><h3>Pluginname</h3><x>some plugin content</x></div>
		 * (end)
		 *
		 * Parameters:
		 *     id - Optional id attribute. Specifies the DOM element, the collabsible
		 *          functionality should be applied to.
		 */
		addCollabsiblePluginHeader : function(id) {
			var selector = "div .panel";
			if (id) {
				selector = id;
			}

			var panels = _KS(selector);
			if (panels.length < 1) return;
			if (!panels.length) panels = new Array(panels);

			for (var i = 0; i < panels.length; i++) {
				var span = new _KN('span');
				span._setText('- ');

				var heading = panels[i].getElementsByTagName('h3')[0];
				if (!heading.innerHTML.startsWith('<span>')) {
					span._injectTop(heading);
				}
				_KE.add('click', heading, function() {
					var el = new _KN(this);
					var style = el._next()._getStyle('display');
					style = (style == 'block') ? 'none' : ((style == '') ? 'none' : 'block');

					el._getChildren()[0]._setText((style == 'block') ? '- ' : '+ ');
					el._next()._setStyle('display', style);
				});
			}
		},
		/**
		 * Function: getURL
		 * Returns an URL created out of the given parameters.
		 * e.g.:
		 * (start code)
		 *  var params = {
         *      renderer : 'KWiki_dpsSolutions',
         *      KWikiWeb : 'default_web'
         *  }
		 * KNOWWE.util.getURL( params ) --> KnowWE.jsp?renderer=KWiki_dpsSolutions&KWikiWeb=default_web
		 * (end)
		 *
		 * Parameters:
		 *     params - The parameter for the URL.
		 *
		 * Returns:
		 *     The URL containing the elements of the params array.
		 */
		getURL : function(params) {
			var baseURL = 'KnowWE.jsp';
			var tokens = [];

			if (!params && typeof params != 'object') return baseURL;

			for (keys in params) {
				var value = params[keys];
				if (typeof value != 'string') value = JSON.stringify(params[keys]);
				tokens.push(keys + "=" + escape(encodeURIComponent(value)));
			}

			//parse the url to add special token like debug etc.
			var p = document.location.search.replace('?', '').split('&');
			for (var i = 0; i < p.length; i++) {
				if (p[i].length == 0) continue;
				var t = p[i].split('=');
				if (!KNOWWE.helper.containsArr(tokens, t[0])) {
					tokens.push(t[0] + "=" + encodeURIComponent(t[1]));
				}
			}
			tokens.push('tstamp=' + new Date().getTime());
			return baseURL + '?' + tokens.join('&');
		},
		/**
		 * Function: getWindowParams
		 * Returns an URL which is used as the target URL for a popup window.
		 *
		 * Parameters:
		 *     params - The parameter for the popup window
		 * Returns:
		 *     The url for the popup window
		 */
		getWindowParams : function(params) {
			if (!params && typeof params != 'object') return '';
			var tokens = [];
			for (keys in params) {
				if (keys == 'url') continue;
				tokens.push(keys + "=" + params[keys]);
			}
			return tokens.join(',');
		},
		/**
		 * Function: replace
		 * Used to replace elements in the DOM tree. The parameter 'elements'
		 * contains the HTML of the element one wants to replace. Multiple
		 * elements can easily replaced since the function not only replaces the
		 * root element in the elements HTML string but every element found on
		 * the top level. For example:
		 *
		 * <ul>
		 * <li><div id='replaceMe'>lorem ipsum</div> replaces an element in the DOM
		 * with id equal 'replaceMe'</li>
		 * <li><div id='replaceMe'>lorem ipsum</div><div id='replaceMe2'>lorem
		 * ipsum</div>: replaces both elements in the DOM if found</li>
		 * </ul>
		 *
		 * The value for 'elements' often is a result from an AJAX query. So make
		 * sure to validate the response properly before handling it here.
		 *
		 * Parameters:
		 *     htmlText - The html text of elements used for replacement.
		 */
		replace : function(htmlText) {
			var newDOMwrapper = document.createElement("div");
			newDOMwrapper.innerHTML = htmlText;

			var domChildNodes = newDOMwrapper.children;

			for (var j = 0; j < domChildNodes.length; j++) {
				var newDOM = domChildNodes[j];
				oldDOM = document.getElementById(newDOM.id);
				if (oldDOM) {
					oldDOM.parentNode.replaceChild(newDOM, oldDOM);
				}
			}
			KNOWWE.helper.observer.notify("afterRerender", newDOM);
		},
		/**
		 * Function: replaceElement
		 * Used to replace elements in the DOM tree. The parameter 'elements'
		 * contains the HTML of the element one wants to replace. Multiple
		 * elements can easily replaced since the function not only replaces the
		 * root element in the elements HTML string but every element found on
		 * the top level.
		 *
		 * In contrast to "replace", this method does not use any ids of the html
		 * to be replaced. Instead it replaces the specified ids with the root
		 * elements of the specified html text. (first id with first element,
		 * second with second, and so on).
		 *
		 * The value for 'elements' often is a result from an AJAX query. So make
		 * sure to validate the response properly before handling it here.
		 *
		 * Parameters:
		 *       ids - array of ids to be replaced.
		 *     htmlText - The html text of the elements used for replacement.
		 */
		replaceElement : function(ids, htmlText) {
			var domChildNodes = null;
			var jsonArray = null;
			try {
				jsonArray = JSON.parse(htmlText);
				if (!jq$.isArray(jsonArray)) throw EventException;
			} catch (e) {
				temp = document.createElement("div");
				temp.innerHTML = htmlText;
				domChildNodes = temp.children;
			}
			// execute script tags that came in with the content
			var evalAddedScripts = function(element) {
				jq$(element).find('script').each(function() {
					eval(this.innerHTML);
				});
			};

			for (var j = ids.length - 1; j >= 0; j--) {
				var oldDOM = jq$('#' + ids[j]);
				var parent = oldDOM.parent();
				if (oldDOM) {
					if (jsonArray) {
						temp = document.createElement("div");
						temp.innerHTML = jsonArray[j];
						jq$(oldDOM).replaceWith(temp.children);
					} else if (domChildNodes) {
						var node = domChildNodes[j];
						jq$(oldDOM).replaceWith(domChildNodes[j]);
					}
					evalAddedScripts(parent);
					KNOWWE.helper.observer.notify("afterRerender", parent[0]);
				}
			}
		},

		reloadPage : function() {
			// reload page. remove version attribute if there
			var hrefSplit = window.location.href.split('?');
			if (hrefSplit.length == 1) {
				window.location.reload();
				return;
			}
			var path = hrefSplit[0];
			var args = hrefSplit[1].split('&');
			var newLocation = path;
			for (var i = 0; i < args.length; i++) {
				if (args[i].indexOf('version=') == 0) continue;
				newLocation += i == 0 ? '?' : '&';
				newLocation += args[i];
			}
			window.location = newLocation;
			window.location.reload(true);
		},

		isIE : function() {
			var ua = window.navigator.userAgent;
			var msie = ua.indexOf('MSIE ');
			if (msie > 0) {
				// IE 10 or older => return version number
				return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
			}

			var trident = ua.indexOf('Trident/');
			if (trident > 0) {
				// IE 11 => return version number
				var rv = ua.indexOf('rv:');
				return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
			}

			var edge = ua.indexOf('Edge/');
			if (edge > 0) {
				// IE 12 => return version number
				return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
			}
			// other browser
			return false;
		}
	}
}();

/**
 * Namespace: KNOWWE.core.util.form
 * Some helper functions concerning HTML form elements.
 */
KNOWWE.core.util.form = function() {
	return {
		/**
		 * Function: getCursorPositionInTextArea
		 * Does get the current position of the cursor inside a textarea.
		 *
		 * Parameters:
		 *     textarea - The textarea
		 *
		 * Returns:
		 *     The position of the cursor inside the textarea.
		 */
		getCursorPositionInTextArea : function(textarea) {
			if (document.selection) {
				var range = document.selection.createRange();
				var stored_range = range.duplicate();
				stored_range.moveToElementText(textarea);
				stored_range.setEndPoint('EndToEnd', range);
				textarea.selectionStart = stored_range.text.length - range.text.length;
				return textarea.selectionStart + range.text.length;
			}
			else {
				if (textarea.selectionEnd) {
					textarea.focus();
					return textarea.selectionEnd;
				}
			}
		},
		/**
		 * Function: insertAtCursor
		 * Inserts an text element at the current cursor position in a textarea, etc.
		 *
		 * Parameters:
		 *     element - The textarea, etc.
		 *     value - The text string
		 */
		insertAtCursor : function(element, value) {
			if (document.selection) {
				element.focus();
				sel = document.selection.createRange();
				sel.text = value;
			} else if (element.selectionStart || element.selectionStart == '0') {
				var startPos = element.selectionStart;
				var endPos = element.selectionEnd;
				element.value = element.value.substring(0, startPos) + value
					+ element.value.substring(endPos, element.value.length);
				element.setSelectionRange(endPos + value.length, endPos + value.length);
			} else {
				element.value = value;
			}
			element.focus();
		},
		/**
		 * Function: addFormHints
		 * Shows a small overlay text containing additional information about an
		 * input HTMLElement. Used for e.g. in the KnofficeUploader.
		 *
		 * Parameters:
		 *     name - The name of the HTMLElement
		 */
		addFormHints : function(name) {
			if (!_KS('#' + name)) return;

			var els = document.getElementById(name + '-extend').getElementsByTagName("input");
			for (var i = 0; i < els.length; i++) {
				var tag = els[i].nextSibling.tagName;
				if (!tag) continue;

				if (tag.toLowerCase() == 'span') {
					_KE.add('focus', els[i], function(e) {
						var el = _KE.target(e);
						el.nextSibling.style.display = "inline";
					});
					_KE.add('blur', els[i], function(e) {
						var el = _KE.target(e);
						el.nextSibling.style.display = "none";
					});
				}
			}
		},
		/**
		 * Function: showExtendedPanel
		 * Shows a panel in certain plugin with additional options.
		 */
		showExtendedPanel : function() {
			var el = this;

			var style = el._next().style;
			el.removeAttribute('class');

			if (style['display'] == 'inline') {
				style['display'] = 'none';
				//el.setAttribute('class', 'show extend pointer extend-panel-down');
				el.setAttribute('class', 'show extend pointer extend-panel-right');
			} else {
				style['display'] = 'inline';
				el.setAttribute('class', 'show extend pointer extend-panel-down');
			}
		}
	}
}();

/**
 * Namespace: KNOWWE.core.rerendercontent
 * Rerenders parts of the article.
 */
KNOWWE.core.rerendercontent = function() {


	return {
		/**
		 * Function: init
		 */
		init : function() {
			KNOWWE.helper.observer.subscribe('update', function() {
				var parameters = {reason : "updateEvent"};
				if (typeof this == "object" && this != window) {
					jq$.extend(parameters, this);
				}
				jq$('.ReRenderSectionMarker').rerender(null, parameters);
			});
			jq$('.asynchronRenderer').rerender(null, {reason : "asynchronRenderer", globalProcessingState : false});
		},
		/**
		 * Function: updateNode
		 * Updates a node.
		 *
		 * Parameters:
		 *     node - The node that should be updated.
		 *     topic - The name of the page that contains the node.
		 */
		updateNode : function(node, topic, ajaxToHTML) {
			var params = {
				action : 'ReRenderContentPartAction',
				KWikiWeb : 'default_web',
				KdomNodeId : node,
				KWiki_Topic : topic,
				ajaxToHTML : ajaxToHTML

			};
			var url = KNOWWE.core.util.getURL(params);
			KNOWWE.core.rerendercontent.execute(url, node, 'insert');
		},
		/**
		 * Function: update
		 */
		update : function(elements, action, callback, indicateProcess) {
			if (elements == undefined) elements = _KS('.ReRenderSectionMarker');
			if (action == undefined) action = 'replace';
			if (!callback && typeof this == "function") callback = this;

			if (elements.length != 0) {
				for (var i = 0; i < elements.length; i++) {
					var rel = elements[i].getAttribute('rel');
					if (!rel) continue;
					rel = eval("(" + rel + ")");

					var params = {
						action : 'ReRenderContentPartAction',
						KWikiWeb : 'default_web',
						SectionID : rel.id,
						ajaxToHTML : "render",
						inPre : KNOWWE.helper.tagParent(_KS('#' + rel.id), 'pre') != document
					};
					if (this.wikiStatus) {
						params.status = this.wikiStatus;
					}
					var url = KNOWWE.core.util.getURL(params);
					KNOWWE.core.rerendercontent.execute(url, rel.id, action, callback, indicateProcess);
				}
			}
		},
		/**
		 * Function: execute
		 * Sends the rerendercontent AJAX request.
		 *
		 * Parameters:
		 *     url - The URL for the AJAX request.
		 *     id - The id of the node that should be updated.
		 */
		execute : function(url, id, action, fn, indicateProcess) {
			if (indicateProcess == undefined) indicateProcess = true;
			var msgId = "executeAjax";
			var options = {
				url : url,
				response : {
					ids : [id],
					action : action,
					fn : function() {
						try {
							KNOWWE.core.actions.init();
							Collapsible.render(_KS('#page'), KNOWWE.helper.gup('page'));
							if (typeof(fn) == "function") {
								fn();
							}
							jq$('#knowWEInfoStatus').val(JSON.parse(this.response).status);
						}
						catch (e) { /*ignore*/
						}
						if (indicateProcess) KNOWWE.core.util.updateProcessingState(-1);
						if (action) {
							// afterRerender event thrown in respective action
						} else {
							// we throw event here
							KNOWWE.helper.observer.notify("afterRerender");
						}

						KNOWWE.notification.removeNotification(msgId);

					},
					onError : function() {
						if (this.status == 304) {
							// 304 means not change in status, so it is to be expected
							KNOWWE.core.util.hideProcessingIndicator();
							return;
						}
						_EC.onErrorBehavior.call(this, msgId);
					}
				}
			};
			if (indicateProcess) KNOWWE.core.util.updateProcessingState(1);
			KNOWWE.helper.observer.notify("beforeRerender");
			new _KA(options).send();
		}
	}
}();

/**
 * Namespace: KNOWWE.plugin
 * The KNOWWE plugin namespace.
 * Initialized empty to ensure existence.
 */
KNOWWE.plugin = function() {
	return {}
}();

/**
 * Aliases for some often used namespaced function to reduce typing.
 */
/* Alias KNOWWE event. */
var _KE = KNOWWE.helper.event;
/* Alias KNOWWE ajax. */
var _KA = KNOWWE.helper.ajax;
/* Alias KNOWWE ElementSelector */
var _KS = KNOWWE.helper.selector;
/* Alias KNOWWE logger */
var _KL = KNOWWE.helper.logger;
/* Alias KNOWWE.helper.element */
var _KN = KNOWWE.helper.element;
/* Alias KNOWWE.helper.hash */
var _KH = KNOWWE.helper.hash;
/* Alias KNOWWE.core */
var _KU = KNOWWE.core.util;


/* ############################################################### */
/* ------------- Onload Events  ---------------------------------- */
/* ############################################################### */
(function init() {

	window.addEvent('domready', _KL.setup);

	if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function() {
			KNOWWE.core.init();
		});
		jq$(window).focus(function() {
			var message = null;
			if (window.onbeforeunload) {
				message = window.onbeforeunload();
			}
			if (!message) {
				KNOWWE.helper.observer.notify('update', {status : jq$('#knowWEInfoStatus').val()});
			}
		});
		if (KNOWWE.core.util.isIE()) {
			// the following lines are for IE compatibility, they trigger the change event if the user presses return
			var ieInputCompatibility = function($element) {
				$element.find("input[type=text]").keyup(function(e) {
					if (e.which === 13) {
						jq$(this).trigger('change');
					}
				});
			};
			jq$(document).ready(function() {
				ieInputCompatibility(jq$(document));
			});
			KNOWWE.helper.observer.subscribe("afterRerender", function() {
				ieInputCompatibility(jq$(this));
			});
		}
	}
}());
