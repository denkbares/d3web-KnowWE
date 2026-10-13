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
 * Instant edit tool that edits the full text of a section in the CodeMirror editor of the page editor. Markups opt in
 * by naming this namespace in their InstantEditTool. Without the CodeMirror bundle it behaves like the default tool.
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

		generateHTML: function (id) {
			if (!available()) return fallback().generateHTML(id);
			return "<div id='" + createContainerID(id) + "' class='codeMirrorEditTool'></div>";
		},

		generateButtons: function (id) {
			return _EC.elements.getSaveCancelDeleteButtons(id);
		},

		postProcessHTML: function (id) {
			if (!available()) return fallback().postProcessHTML(id);
			const view = KnowWEEditor.create(document.getElementById(createContainerID(id)), {
				doc: _EC.getWikiText(id),
				article: KNOWWE.helper.getPagename(),
				mode: "section",
				wrap: isWrapping()
			});
			editors[id] = {view: view, initialDoc: view.state.doc};
			view.focus();
		},

		unloadCondition: function (id) {
			const editor = editors[id];
			if (!editor) return fallback().unloadCondition(id);
			return editor.view.state.doc.eq(editor.initialDoc);
		},

		generateWikiText: function (id) {
			const editor = editors[id];
			if (!editor) return fallback().generateWikiText(id);
			return editor.view.state.doc.toString();
		}
	};
}();
