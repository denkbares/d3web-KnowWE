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
 * The KNOWWE global namespace object. If KNOWWE is already defined, the
 * existing KNOWWE object will not be overwritten so that defined namespaces are
 * preserved.
 */
if (typeof KNOWWE == "undefined" || !KNOWWE) {
	var KNOWWE = {};
}

/**
 * Namespace: KNOWWE.core.plugin.instantedit The KNOWWE instant edit namespace.
 */
KNOWWE.editCommons = function () {

	return {

		mode: null,

		wikiText: new Object(),

		sleep: function (ms) {
			var startTime = new Date().getTime();
			while (new Date().getTime() < startTime + ms);
		},

		wrapHTML: function (id, locked, html) {
			var lockedHTML = "";
			if (locked) {
				lockedHTML = "<div class=\"error\">Another user has started to edit this page, but " + "hasn't yet saved it. You are allowed to further edit this page, but be " + "aware that the other user will not be pleased if you do so!</div>"
			}
			var openingDiv = "<div id='" + id + "' class='editarea'>";
			var closingDiv = "</div>\n";

			return openingDiv + lockedHTML + html + closingDiv;
		},

		encodeForHtml: function (text) {
			if (text) return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
			return "";
		},

		hideTools: function () {
			$$('.markupTools').setStyle("display", "none");
		},

		showAjaxLoader: function () {
			KNOWWE.core.util.updateProcessingState(1);
		},

		hideAjaxLoader: function () {
			KNOWWE.core.util.updateProcessingState(-1);
		},

		reloadPage: function () {
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

		// Maybe return given messages instead
		onErrorBehavior: function () {
			_EC.hideAjaxLoader();
			var status = this.status;
			if (status == null) return;
			switch (status) {
				case 0:
					KNOWWE.notification.error(null, "Server appears to be offline.", status);
					break;
				case 403:
					KNOWWE.notification.error(null, "You are not authorized to change this page.", status);
					break;
				case 404:
					KNOWWE.notification.error(null, "This page no longer exists. Please reload.", status);
					break;
				case 409:
					KNOWWE.notification.error(null, "This section has changed since you loaded this page. Please reload the page.", status);
					break;
				default:
					KNOWWE.notification.error(null, "Error " + status + ". Please reload the page.", status);
					break;
			}
		},

		executeIfPrivileged: function (grantedFN, forbiddenFN) {
			var params = {
				action: 'CheckCanEditPageAction'
			}

			var options = {
				url: KNOWWE.core.util.getURL(params),
				method: 'GET',
				response: {
					action: 'none',
					fn: function () {
						var canedit = JSON.parse(this.responseText).canedit;

						if (canedit) {
							grantedFN();
						} else if (forbiddenFN) {
							forbiddenFN();
						}
					},
					onError: _EC.onErrorBehavior,
				}
			}
			new _KA(options).send();
		},

		sendChanges: function (newWikiText, params, fn) {
			_EC.showAjaxLoader();
			var options = {
				url: KNOWWE.core.util.getURL(params),
				data: newWikiText,
				response: {
					action: 'none',
					fn: function () {
						// TODO: Remove?
						window.onbeforeunload = null;
						window.onunload = null;
						$(window).removeEvents('beforeunload');
						$(window).removeEvents('unload');
						if (fn) fn();
						_EC.hideAjaxLoader();
					},
					onError: _EC.onErrorBehavior
				}
			}
			new _KA(options).send();
		},

		registerSaveCancelEvents: function (element, saveFunction, cancelFunction, argument) {
			jq$(element).keydown(function (event) {
				event = _EC.toJQueryEvent(event);
				if (_EC.isSaveKey(event)) {
					event.stopPropagation();
					event.preventDefault();
					saveFunction(argument);
				}
				else if (_EC.isCancelKey(event)) {
					event.stopPropagation();
					event.preventDefault();
					cancelFunction(argument);
				}
			});
		},

		isSaveKey: function (event) {
			event = _EC.toJQueryEvent(event);
			if (_EC.isModifier(event) || _EC.isDoubleModifier(event)) {
				if (event.which == 83) { // S
					return true;
				}
			}
			return false;
		},

		toJQueryEvent: function (event) {
			return jq$.event.fix(event.originalEvent || event.event || event);
		},

		isCancelKey: function (event) {
			event = _EC.toJQueryEvent(event);
			if (_EC.isModifier(event) || _EC.isDoubleModifier(event)) {
				// Q, but not with alt gr (= alt + ctrl)  to allow for @ in windows
				if (event.which == 81 && (!event.altKey || !event.ctrlKey)) {
					return true
				}
				if (event.which == 27) { // ESC
					return true;
				}
			}
			return false;
		},

		isModifier: function (event) {
			event = _EC.toJQueryEvent(event);
			if ((!event.metaKey && event.ctrlKey && !event.altKey)
				|| (!event.metaKey && !event.ctrlKey && event.altKey)
				|| (event.metaKey && !event.ctrlKey && !event.altKey)) {
				return true;
			}
			return false;
		},

		isDoubleModifier: function (event) {
			event = _EC.toJQueryEvent(event);
			var mods = 0;
			if (event.metaKey) mods++;
			if (event.ctrlKey) mods++;
			if (event.altKey) mods++;
			if (event.shiftKey) mods++;
			if (mods == 2) {
				return true;
			}
			return false;
		},

		getWikiText: function (id, actionName) {

			var tempWikiText = _EC.wikiText[id];

			if (tempWikiText != null) return tempWikiText;

			if (actionName == null) actionName = 'GetWikiTextAction';

			var params = {
				action: actionName,
				KdomNodeId: id
			};

			var options = {
				url: KNOWWE.core.util.getURL(params),
				async: false,
				response: {
					action: 'none',
					// for FF 3.6 compatibility, we can't use the function fn
					// in synchronous call (no onreadystatechange event fired)
					onError: _EC.onErrorBehavior
				}
			};
			var ajaxCall = new _KA(options);
			ajaxCall.send();
			_EC.wikiText[id] = JSON.parse(ajaxCall.getResponse()).text;
			return _EC.wikiText[id];
		},

		isKDomID: function (id) {
			if (!id) {
				return false;
			}
			// TODO: Check RegExp
			var validID = new RegExp("^[0123456789abcdef]{1,8}$");
			if (validID.test(id)) {
				// TODO: Check also server-side
				return true;
			}
			return false;
		},

		isEmpty: function (str) {
			return (!str || 0 === str.length);
		}

	}
}();


KNOWWE.editCommons.elements = function () {

	return {

		getSaveButton: function (jsFunction) {
			return "<a class=\"action save\" " + "href=\"javascript:" + jsFunction + "\"" + ">Save</a>";
		},

		getCancelButton: function (jsFunction) {
			return "<a class=\"action cancel\" href=\"javascript:" + jsFunction + "\"" + ">Cancel</a>";
		},

		getDeleteSectionButton: function (jsFunction) {
			return "<a class=\"action delete\" href=\"javascript:" + jsFunction + "\"" + ">Delete</a>";
		},

		getSaveCancelDeleteButtons: function (id, additionalButtonArray) {
			var buttons = _EC.mode.getSaveCancelDeleteButtons(id, additionalButtonArray);
			return _EC.mode.getButtonsTable(buttons);
		},

	}

}();

var _EC = KNOWWE.editCommons;