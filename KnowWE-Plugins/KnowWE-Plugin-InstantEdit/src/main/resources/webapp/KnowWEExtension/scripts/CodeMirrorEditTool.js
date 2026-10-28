/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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
KNOWWE.plugin = KNOWWE.plugin || {};

/**
 * Instant edit tool that edits the full text of a section in the CodeMirror editor of the page editor. Sections edited
 * with the textarea tool get this editor instead, see KNOWWE.editCommons.editTools, markups can also name this
 * namespace in their InstantEditTool. Without the CodeMirror bundle it behaves like the default tool.
 */
KNOWWE.plugin.codeMirrorEditTool = function () {

	const WRAP_STORAGE_KEY = "knowwe.editor.wrap";

	/** The editors by section id, each with its view and the text it started with. */
	const editors = {};

	function available() {
		return typeof KnowWEEditor !== "undefined" && KnowWEEditor.isAvailable();
	}

	function fallback() {
		return KNOWWE.plugin.defaultEditTool;
	}

	/**
	 * Returns the editor of the section, or null if there is none. An editor whose section was closed again, e.g. by
	 * canceling it in the edit mode, is dropped, so the section falls back to its wiki text.
	 */
	function editorOf(id) {
		const editor = editors[id];
		if (!editor) return null;
		if (editor.view.dom.isConnected) return editor;
		editor.view.destroy();
		delete editors[id];
		return null;
	}

	function createContainerID(id) {
		return "codeMirrorEdit" + id;
	}

	// shares the wrapping preference of the page editor
	function isWrapping() {
		try {
			return localStorage.getItem(WRAP_STORAGE_KEY) !== "false";
		} catch (e) {
			return true;
		}
	}

	return {

		/** Marks this tool and the ones built on it as CodeMirror based, see KNOWWE.editCommons.editTools. */
		codeMirror: true,

		generateHTML: function (id) {
			if (!available()) return fallback().generateHTML(id);
			return "<div id='" + createContainerID(id) + "' class='codeMirrorEditTool'></div>";
		},

		/**
		 * Returns the buttons of the tool bar. Tools that extend this one pass their own `buttons` as HTML, they are
		 * left out without the CodeMirror bundle.
		 */
		generateButtons: function (id, buttons = []) {
			if (!available()) return fallback().generateButtons(id);
			return _EC.elements.getSaveCancelDeleteButtons(id, buttons);
		},

		postProcessHTML: function (id) {
			if (!available()) return fallback().postProcessHTML(id);
			const container = document.getElementById(createContainerID(id));
			editorOf(id); // drops the editor of an earlier opening of the section
			try {
				const view = KnowWEEditor.create(container, {
					doc: _EC.getWikiText(id),
					article: KNOWWE.helper.getPagename(),
					mode: "section",
					wrap: isWrapping()
				});
				editors[id] = {view: view, initialDoc: view.state.doc};
				view.focus();
			} catch (error) {
				// any failure ends in the textarea of the default tool, which the other functions use without an editor
				console.error("CodeMirror editor could not be started, falling back to the plain textarea", error);
				delete editors[id];
				container.outerHTML = fallback().generateHTML(id);
				fallback().postProcessHTML(id);
			}
		},

		unloadCondition: function (id) {
			const editor = editorOf(id);
			if (!editor) return fallback().unloadCondition(id);
			return editor.view.state.doc.eq(editor.initialDoc);
		},

		/** Returns the editor view of the section, or null if it is not edited or edited in the plain textarea. */
		getView: function (id) {
			return editorOf(id)?.view ?? null;
		},

		/** Closes the editor of the section, e.g. before the section is edited with another tool. */
		destroy: function (id) {
			const editor = editors[id];
			if (!editor) return;
			editor.view.destroy();
			delete editors[id];
		},

		generateWikiText: function (id) {
			const editor = editorOf(id);
			if (!editor) return fallback().generateWikiText(id);
			return editor.view.state.doc.toString();
		}
	};
}();
