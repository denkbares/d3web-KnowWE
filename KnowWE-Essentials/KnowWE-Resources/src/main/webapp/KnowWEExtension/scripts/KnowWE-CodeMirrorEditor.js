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

/**
 * Mounts the CodeMirror page editor on the form rendered by editors/codemirror.jsp. The editor itself comes from
 * KnowWEEditor, this script connects it to the form, the toolbar and the status bar.
 */
(function () {
	"use strict";

	const WRAP_STORAGE_KEY = "knowwe.editor.wrap";

	/**
	 * Shows the plain textarea instead of the editor. The textarea is the form field, so saving keeps working.
	 */
	function fallback(root, error) {
		console.error("CodeMirror editor could not be started, falling back to the plain textarea", error);
		root.classList.add("knowwe-editor-fallback");
		const surface = root.querySelector(".knowwe-editor-surface");
		if (surface) surface.replaceChildren();
		const position = root.querySelector(".knowwe-editor-position");
		if (position) position.textContent = "The editor could not be started, editing as plain text";
		const dirtyLabel = root.querySelector(".knowwe-editor-dirty");
		if (dirtyLabel) dirtyLabel.hidden = true;
	}

	function isFallback(root) {
		return root.classList.contains("knowwe-editor-fallback");
	}

	function init(root) {
		if (typeof KnowWEEditor === "undefined" || !KnowWEEditor.isAvailable()) {
			throw new Error("CodeMirror bundle is not available");
		}

		const {undo, redo} = CodeMirror.commands;
		const {openSearchPanel, closeSearchPanel, searchPanelOpen} = CodeMirror.search;

		const form = root.closest("form");
		const source = root.querySelector(".knowwe-editor-source");
		const surface = root.querySelector(".knowwe-editor-surface");
		const position = root.querySelector(".knowwe-editor-position");
		const dirtyLabel = root.querySelector(".knowwe-editor-dirty");
		const page = form.querySelector("input[name=page]");

		let submitting = false;

		function updateStatus(update) {
			if (update.docChanged || update.selectionSet) {
				const head = update.state.selection.main.head;
				const line = update.state.doc.lineAt(head);
				position.textContent = "Ln " + line.number + ", Col " + (head - line.from + 1)
					+ "  ·  " + update.state.doc.lines + " lines";
			}
			const help = KnowWEEditor.isHelpOpen(update.state);
			if (help !== KnowWEEditor.isHelpOpen(update.startState)) {
				root.querySelector("[data-editor-cmd=help]").classList.toggle("active", help);
			}
			if (update.docChanged) {
				const dirty = !update.state.doc.eq(initialDoc);
				dirtyLabel.hidden = !dirty;
				root.classList.toggle("knowwe-editor-is-dirty", dirty);
			}
		}

		const view = KnowWEEditor.create(surface, {
			doc: source.value,
			article: page ? page.value : null,
			mode: "page",
			wrap: isWrapping(),
			onSave: save,
			onChange: updateStatus
		});
		const initialDoc = view.state.doc;

		function isDirty() {
			return !view.state.doc.eq(initialDoc);
		}

		// seed the status bar once, the update listener only fires on changes
		position.textContent = "Ln 1, Col 1  ·  " + view.state.doc.lines + " lines";

		function isWrapping() {
			try {
				return localStorage.getItem(WRAP_STORAGE_KEY) !== "false";
			} catch (e) {
				return true;
			}
		}

		function setWrapping(enabled) {
			KnowWEEditor.setWrapping(view, enabled);
			root.querySelector("[data-editor-cmd=wrap]").classList.toggle("active", enabled);
			try {
				localStorage.setItem(WRAP_STORAGE_KEY, String(enabled));
			} catch (e) {
				// storage is optional
			}
		}

		function save() {
			const ok = form.querySelector("button[name=ok]");
			if (ok) ok.click();
		}

		function syncSource() {
			source.value = view.state.doc.toString();
		}

		function fitHeight() {
			const top = surface.getBoundingClientRect().top + window.scrollY;
			const statusHeight = root.querySelector(".knowwe-editor-statusbar").offsetHeight;
			const available = window.innerHeight - top - statusHeight - 24;
			surface.style.height = Math.max(320, available) + "px";
		}

		root.querySelector("[data-editor-cmd=wrap]").classList.toggle("active", isWrapping());

		root.addEventListener("click", event => {
			const button = event.target.closest("[data-editor-cmd]");
			if (!button) return;
			event.preventDefault();
			switch (button.dataset.editorCmd) {
				case "undo":
					undo(view);
					break;
				case "redo":
					redo(view);
					break;
				case "find":
					if (searchPanelOpen(view.state)) {
						closeSearchPanel(view);
					} else {
						openSearchPanel(view);
					}
					break;
				case "switch":
					button.parentElement.classList.toggle("open");
					return;
				case "wrap":
					setWrapping(!isWrapping());
					break;
				case "help":
					KnowWEEditor.toggleHelp(view);
					break;
			}
			view.focus();
		});

		document.addEventListener("click", event => {
			const open = root.querySelector(".knowwe-editor-switch.open");
			if (open && !open.contains(event.target)) open.classList.remove("open");
		});

		root.querySelectorAll("a.editor-type").forEach(link => {
			link.addEventListener("click", event => {
				event.preventDefault();
				if (typeof Wiki !== "undefined" && typeof Wiki.prefs === "function") {
					if (isDirty() && !window.confirm("Discard the unsaved changes and switch the editor?")) return;
					Wiki.prefs("editor", link.dataset.editor);
					submitting = true;
					window.location.reload();
				}
			});
		});

		// after a late failure the textarea holds the text, which must not be overwritten by the editor
		form.addEventListener("submit", () => {
			submitting = true;
			if (!isFallback(root)) syncSource();
		});

		window.addEventListener("beforeunload", event => {
			const dirty = isFallback(root) ? source.value !== source.defaultValue : isDirty();
			if (!submitting && dirty) {
				event.preventDefault();
				event.returnValue = "";
			}
		});

		window.addEventListener("resize", fitHeight);
		fitHeight();
		view.focus();
	}

	function ready() {
		document.querySelectorAll(".knowwe-editor").forEach(root => {
			try {
				init(root);
			} catch (error) {
				fallback(root, error);
			}
		});
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", ready);
	} else {
		ready();
	}
})();
