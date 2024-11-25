/*
 * Copyright (C) 2022 denkbares GmbH, Germany
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

KNOWWE = typeof KNOWWE === "undefined" ? {} : KNOWWE;

/**
 * Namespace: KNOWWE.core.plugin.instantedit The KNOWWE instant edit namespace.
 */
KNOWWE.editCommons = function () {

	return {

		mode: null,

		wikiText: {},

		appendToolBar : function(sectionId, content) {
			const toolbar = new Element('div', {
				'class' : 'edittoolbar'
			});
			toolbar.innerHTML = content;
			const editor = jq$('#' + sectionId);
			editor.css('position', 'relative');
			editor.append(toolbar);
		},

		wrapHTML: function (id, locked, html) {
			let lockedHTML = "";
			if (locked) {
				lockedHTML = "<div class=\"error\">Another user has started to edit this page, but " + "hasn't yet saved it. You are allowed to further edit this page, but be " + "aware that the other user will not be pleased if you do so!</div>";
			}
			let openingDiv = "<div id='" + id + "' class='editarea'>";
			let closingDiv = "</div>\n";

			return openingDiv + lockedHTML + html + closingDiv;
		},

		encodeForHtml: function (text) {
			if (text) return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
			return "";
		},

		hideTools: function () {
			jq$('.markupTools, .markupMenu, .headerMenu').css("display", "none");
		},

		showAjaxLoader: function () {
			_KU.showProcessingIndicator();
		},

		hideAjaxLoader: function () {
			_KU.hideProcessingIndicator();
		},

		errorMessageId: "defaultErrorMessageId",

		// Maybe return given messages instead
		onErrorBehavior: function (messageId) {
			if (!messageId) messageId = _EC.errorMessageId;
			_EC.hideAjaxLoader();
			const status = this.status;
			if (status == null) return;
			KNOWWE.notification.removeNotification(messageId);
			switch (status) {
				case 0:
					KNOWWE.notification.error(null, "Wiki appears to be offline.", messageId);
					break;
				case 403:
					KNOWWE.notification.error(null, "You are not authorized to do this.", messageId);
					break;
				case 404:
					KNOWWE.notification.error(null, "This page no longer exists. Please try <a href='javascript:window.location.reload();'>reloading</a> the page or go back to an existing one.", messageId);
					break;
				case 409:
					KNOWWE.notification.error(null, "This section has changed since you loaded this page. Please try <a href='javascript:window.location.reload();'>reloading</a> the page and contact your administrator if the error remains.", messageId);
					break;
				default:
					KNOWWE.notification.error(null, "Error " + status + ". Please try <a href='javascript:window.location.reload();'>reloading</a> the page and contact your administrator if the error remains.", messageId);
					break;
			}
		},

		/**
		 * Check if the user has write permissions on the article. If yes, execute function "grantedFN", if not, "forbiddenFN".
		 *
		 * @see KNOWWE.core.util.canWrite and KNOWWE.core.util.canView and so forth, which might be faster but less rigorous
		 *
		 * @param grantedFN the function to execute if the user has write privileges
		 * @param forbiddenFN the function to execute of the user does NOT have write privileges
		 */
		executeIfPrivileged: function (grantedFN, forbiddenFN) {
			const params = {
				action: 'CheckCanEditPageAction'
			};

			const options = {
				url: KNOWWE.core.util.getURL(params),
				method: 'GET',
				response: {
					action: 'none',
					fn: function () {
						const canedit = JSON.parse(this.responseText).canedit;

						if (canedit) {
							grantedFN();
						} else if (forbiddenFN) {
							forbiddenFN();
						}
					},
					onError: _EC.onErrorBehavior
				}
			};
			new _KA(options).send();
		},

		sendChanges: function (sectionData, params, fn, async) {
			if (typeof async === "undefined") async = true;
			_EC.showAjaxLoader();
			let xhr = new XMLHttpRequest();
			let url = _KA.appendXSRFToken(KNOWWE.core.util.getURL(params));
			xhr.open("POST", url, async);
			xhr.setRequestHeader("Content-Type", "application/json");
			xhr.onreadystatechange = function () {
				if (xhr.readyState === 4) {
					if (xhr.status === 200) {
						window.onbeforeunload = null;
						window.onunload = null;
						KNOWWE.helper.observer.notify('saved');
						if (fn) fn();
						_EC.hideAjaxLoader();
					} else {
						_EC.onErrorBehavior.call(xhr);
					}
				}
			};
			xhr.send(JSON.stringify(sectionData));
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
				if (event.which === 83) { // S
					return true;
				}
			}
			return false;
		},

		toJQueryEvent: function (event) {
			return jq$.event.fix(event.originalEvent || event.event || event);
		},

		isBlank: function (text) {
			return /^\s*$/.test(text);
		},

		isCancelKey: function (event) {
			event = _EC.toJQueryEvent(event);
			if (_EC.isModifier(event) || _EC.isDoubleModifier(event)) {
				// Q, but not with alt gr (= alt + ctrl)  to allow for @ in windows
				if (event.which === 81 && (!event.altKey || !event.ctrlKey)) {
					return true;
				}
				if (event.which === 27) { // ESC
					return true;
				}
			}
			return false;
		},

		isModifier: function (event) {
			event = _EC.toJQueryEvent(event);
			return (!event.metaKey && event.ctrlKey && !event.altKey)
				|| (!event.metaKey && !event.ctrlKey && event.altKey)
				|| (event.metaKey && !event.ctrlKey && !event.altKey);

		},

		isDoubleModifier: function (event) {
			event = _EC.toJQueryEvent(event);
			let mods = 0;
			if (event.metaKey) mods++;
			if (event.ctrlKey) mods++;
			if (event.altKey) mods++;
			if (event.shiftKey) mods++;
			return mods === 2;

		},
		getWikiArticleText: function (articleName, actionName) {
			if (actionName == null) actionName = "GetWikiArticleTextAction";

			const params = {
				action: actionName,
				articleName: articleName
			};

			const options = {
				url: KNOWWE.core.util.getURL(params),
				async: false,
				response: {
					action: 'none',
					onError: function () {
						console.log("Article does not exist");
					}
				}
			};
			const ajaxCall = new _KA(options);
			ajaxCall.send();
			return JSON.parse(ajaxCall.getResponse()).text;
		},

		getWikiText: function (id, actionName) {

			const tempWikiText = _EC.wikiText[id];

			if (tempWikiText != null) return tempWikiText;

			if (actionName == null) actionName = 'GetWikiTextAction';

			const params = {
				action: actionName,
				KdomNodeId: id
			};

			const options = {
				url: KNOWWE.core.util.getURL(params),
				async: false,
				response: {
					action: 'none',
					// for FF 3.6 compatibility, we can't use the function fn
					// in synchronous call (no onreadystatechange event fired)
					onError: _EC.onErrorBehavior
				}
			};
			const ajaxCall = new _KA(options);
			ajaxCall.send();
			_EC.wikiText[id] = JSON.parse(ajaxCall.getResponse()).text;
			return _EC.wikiText[id];
		},

		isKDomID: function (id) {
			if (!id) {
				return false;
			}
			// TODO: Check RegExp
			const validID = new RegExp("^[0123456789abcdef]{1,8}$");
			return validID.test(id);

		},

		isEmpty: function (str) {
			return (!str || 0 === str.length);
		}

	};
}();


KNOWWE.editCommons.elements = function () {
	function newSection(n) {
		return "<div class='new-section' id='new-section-"+n+"' sectionid='new-section-"+n+"' data-new-section-number='"+n+"'>New Section</div>";
	}

	return {

		getSaveButton: function (jsFunction) {
			return "<a class=\"action save\" " + "onclick=\"" + jsFunction + "\"" + ">Save</a>";
		},

		getCancelButton: function (jsFunction) {
			return "<a class=\"action cancel\" onclick=\"" + jsFunction + "\"" + ">Cancel</a>";
		},

		getDeleteSectionButton: function (jsFunction) {
			return "<a class=\"action delete\" onclick=\"" + jsFunction + "\"" + ">Delete</a>";
		},

		getSaveCancelDeleteButtons: function (id, additionalButtonArray) {
			const buttons = _EC.mode.getSaveCancelDeleteButtons(id, additionalButtonArray);
			return _EC.mode.getButtonsTable(buttons);
		},

		newSectionDragger: function (n) {
			return "<div class=\"new-element-drag-drop\">" + newSection(n) + "</div>";
		},

		newSection: newSection
	};

}();


/**
 * Class to instantiate for any editor that wants to implement undo / redo. By using this class,
 * undo functionality is always provided by generating a persistent representation from the current
 * state of the edit widget (e.g., nut not necessarily, creating the markup text). This persistent
 * representation is called the content-data. Undo/Redo is implemented by restoring the widget's
 * state from these data.
 *
 * Additionally the undo-/redoable widget should also provide a method to obtain the so-called
 * meta-data. This is (in contrast to the content-data) information only relevant for user display
 * and/or interaction, but have no influence on the content. This is e.g. scroll-positions,
 * selections, etc. The reason is that for each snapshoted state of content-data, two states of the
 * meta-data are created and stored, one right before and one right after the undoable action. When
 * performing an undo, this class will restore the meta-data right before the action, while when
 * performing a redo, we will restore the meta-data right after the action. This is the normal
 * behaviour most user interface implementations will show.
 *
 * @param restoreDataFun a function(contentData, metaData) that shall restore the widgets state
 * according to the specified content- and meta-data, given as function arguments.
 * @param getContentDataFun a function() that returns the content-data of the widget as an object
 * @param getMetaDataFun a function() that returns the meta-data of the widget as an object
 * @constructor
 */
KNOWWE.editCommons.UndoSupport = function (restoreDataFun, getContentDataFun, getMetaDataFun) {
	this.restoreDataFun = restoreDataFun;
	this.getContentDataFun = getContentDataFun;
	this.getMetaDataFun = getMetaDataFun;

	this.snaps = [];
	this.currentIndex = -1;
	this.recording = false;
};

/**
 * Method that will execute the specified action as a function without any arguments. When the
 * action is performed, this method makes sure that the changes are recorded, updating the undo/redo
 * stacks. So the next undo operation will revert the changes of the action. The method will return
 * the result returned by the action.
 *
 * The method also makes sure that nested undoable actions will be handled correctly. This means,
 * having an outer 'withUndo' action, all inner 'withUndo' will be ignored, as they are combined to
 * the outer undo action.
 *
 * @param name the name of the user action (e.g. to be shown later in a undo/redo menu)
 * @param action the user action to be performed
 * @param combineId (optional) if specified, multiple recorded actions of the same combineId will be unified
 * into one undo/redo operation. This is often useful when performing minimal changes by keyboard.
 */
KNOWWE.editCommons.UndoSupport.prototype.withUndo = function (name, action, combineId) {
	// check for nested undoable actions
	if (this.recording) return action();

	// if called the first time, make an initial snapshot
	if (this.currentIndex === -1) {
		const meta = this.getMetaDataFun();
		const content = this.getContentDataFun();
		this.snaps.push({name: "", metaDataBefore: meta, metaDataAfter: meta, contentData: content});
		this.currentIndex = 0;
	}

	const snap = {name: name, combineId: combineId};
	const lastSnap = this.snaps[this.currentIndex];
	let canCombine = combineId && combineId === lastSnap.combineId;
	try {
		this.recording = true;
		// metadata before operation if only required if we will not combine
		if (!canCombine) snap.metaDataBefore = this.getMetaDataFun();
		return action();
	}
	finally {
		// prepare data and remove all redo operations
		snap.metaDataAfter = this.getMetaDataFun();
		snap.contentData = this.getContentDataFun();
		// if nothing has changed, ignore this snapshot, only perform if there are changes
		if (lastSnap.contentData !== snap.contentData) {
			// remove future redo actions
			this.snaps.splice(this.currentIndex + 1, this.snaps.length - (this.currentIndex + 1));
			// update the snaps list
			if (canCombine) {
				// if we shall combine we update last snap
				lastSnap.name = snap.name;
				lastSnap.contentData = snap.contentData;
				lastSnap.metaDataAfter = snap.metaDataAfter;
			}
			else {
				// otherwise we add a the new snap
				this.currentIndex++;
				this.snaps.push(snap);
			}
		}
		this.recording = false;
	}
};

/**
 * Records a given state for an undo point. In contrast to method {@link withUndo} this method
 * cannot handle meta data well. So usually use {@link withUndo} is possible and this method only
 * if the undoable operation cannot be stated in a single function.
 *
 * @param name the name of the user action (e.g. to be shown later in a undo/redo menu)
 * @param combineId (optional) if specified, multiple recorded actions of the same combineId will be unified
 * into one undo/redo operation. This is often useful when performing minimal changes by keyboard.
 */
KNOWWE.editCommons.UndoSupport.prototype.recordUndo = function (name, combineId) {
	this.withUndo(name, function () {
	}, combineId);
};

KNOWWE.editCommons.UndoSupport.prototype.canUndo = function () {
	return this.currentIndex > 0;
};

KNOWWE.editCommons.UndoSupport.prototype.canRedo = function () {
	return this.currentIndex < this.snaps.length - 1;
};

KNOWWE.editCommons.UndoSupport.prototype.undo = function (count) {
	// check if undo is possible
	if (this.recording) throw "Must not call undo during recording undoable action";
	if (!this.canUndo()) return;
	// check count parameter
	if (typeof count === 'undefined') count = 1;
	if (count <= 0) return;
	// move currentIndex to new selected snap
	this.currentIndex -= count;
	if (this.currentIndex < 0) this.currentIndex = 0;
	// perform the undo operation
	this.restoreDataFun(
		this.snaps[this.currentIndex].contentData,
		this.snaps[this.currentIndex + 1].metaDataBefore);
};

KNOWWE.editCommons.UndoSupport.prototype.redo = function (count) {
	// check if redo is possible
	if (this.recording) throw "Must not call redo during recording undoable action";
	if (!this.canRedo()) return;
	// check count parameter and move count-1 items between the stacks, before performing the redo
	if (typeof count === 'undefined') count = 1;
	if (count <= 0) return;
	// move currentIndex to new selected snap
	this.currentIndex += count;
	if (this.currentIndex >= this.snaps.length) this.currentIndex = this.snaps.length - 1;
	// perform the redo operation
	this.restoreDataFun(
		this.snaps[this.currentIndex].contentData,
		this.snaps[this.currentIndex].metaDataAfter);
};

KNOWWE.editCommons.UndoSupport.prototype.getUndoNames = function (maxNames) {
	let from = 1;
	const to = this.currentIndex + 1;
	if (typeof maxNames !== 'undefined' && to - from > maxNames) from = to - maxNames;
	return jq$.map(this.snaps.slice(from, to), function (snap) {
		return snap.name;
	}).reverse();
};

KNOWWE.editCommons.UndoSupport.prototype.getRedoNames = function (maxNames) {
	const from = this.currentIndex + 1;
	let to = this.snaps.length + 1;
	if (typeof maxNames !== 'undefined' && to - from > maxNames) to = from + maxNames;
	return jq$.map(this.snaps.slice(from, to), function (snap) {
		return snap.name;
	});
};

/**
 * Returns if the content of the supported editor has been changed since it's initial state.
 */
KNOWWE.editCommons.UndoSupport.prototype.hasContentChanged = function () {
	if (this.currentIndex <= 0) return false;
	return this.snaps[0].contentData !== this.snaps[this.currentIndex].contentData;
};

var _EC = KNOWWE.editCommons;
