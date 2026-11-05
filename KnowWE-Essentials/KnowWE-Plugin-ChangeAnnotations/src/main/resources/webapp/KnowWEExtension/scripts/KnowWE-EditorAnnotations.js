/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

/**
 * Shows the change annotation of the Annotate tab in a gutter of the CodeMirror editors of KnowWEEditor: the version
 * that last changed a line, its author, date and change note. Consecutive lines of the same version form a group
 * that is labeled at its first line, newer versions are tinted stronger.
 *
 * The annotation describes the latest saved version of the page. It is fetched from AnnotatePageAction once the gutter
 * is shown and matched to the lines the editor started with by a hash of each line, so a line whose text differs from
 * the saved one has no annotation. Edits move the annotations with their lines, a line edited in the editor loses its
 * annotation and new lines have none.
 *
 * The gutter is off by default. The page editor switches it with a toolbar button, the choice is remembered in this
 * browser and also applies to the editors of sections.
 */
window.KnowWEEditorAnnotations = (function () {
	"use strict";

	const STORAGE_KEY = "knowwe.editor.annotations";

	/** The hash of a line as Java's String.hashCode() computes it, see PageAnnotationJson. */
	function hashLine(text) {
		let hash = 0;
		for (let i = 0; i < text.length; i++) {
			hash = (Math.imul(31, hash) + text.charCodeAt(i)) | 0;
		}
		return hash;
	}

	/** The lines of the document that still have the text the annotation describes, as ranges with their revision. */
	function matchLines(doc, data) {
		const entries = [];
		const count = Math.min(doc.lines, data.lines.length);
		for (let n = 1; n <= count; n++) {
			const line = doc.line(n);
			if (hashLine(line.text) === data.hashes[n - 1]) {
				entries.push({from: line.from, to: line.to, revision: data.lines[n - 1]});
			}
		}
		return entries;
	}

	/**
	 * Maps the annotated lines through changes. A line loses its annotation once a change removes or inserts text
	 * inside it, text inserted at its start or end leaves it intact, as does a line break before or after it. Such a
	 * line no longer starts or ends where its range does, so it shows no annotation until the text is removed again.
	 */
	function mapEntries(entries, changes) {
		const ranges = [];
		changes.iterChangedRanges((fromA, toA) => ranges.push(fromA, toA));
		const result = [];
		for (const entry of entries) {
			let touched = false;
			for (let i = 0; i < ranges.length && !touched; i += 2) {
				touched = ranges[i] < entry.to && ranges[i + 1] > entry.from;
			}
			if (touched) continue;
			const from = changes.mapPos(entry.from, 1);
			const to = changes.mapPos(entry.to, -1);
			if (from <= to) result.push({from, to, revision: entry.revision});
		}
		return result;
	}

	/** The annotated range of exactly this line, entries are sorted by their start. */
	function entryAt(entries, line) {
		let low = 0, high = entries.length;
		while (low < high) {
			const middle = (low + high) >> 1;
			if (entries[middle].from < line.from) low = middle + 1;
			else high = middle;
		}
		for (let i = low; i < entries.length && entries[i].from === line.from; i++) {
			if (entries[i].to === line.to) return entries[i];
		}
		return null;
	}

	function formatDate(iso, withTime) {
		const date = new Date(iso);
		if (isNaN(date.getTime())) return iso;
		return withTime ? date.toLocaleString() : date.toLocaleDateString();
	}

	/** Adds the label, the tooltip and the tint of each revision to the data of the action. */
	function prepare(data) {
		const versions = data.revisions.map(revision => revision.version);
		const newest = Math.max(...versions), oldest = Math.min(...versions);
		for (const revision of data.revisions) {
			const age = newest === oldest ? 1 : (revision.version - oldest) / (newest - oldest);
			revision.alpha = (0.05 + 0.25 * age).toFixed(3);
			revision.label = "v" + revision.version + " " + revision.author;
			revision.title = "Version " + revision.version + " by " + revision.author + ", "
				+ formatDate(revision.date, true)
				+ (revision.changeNote ? "\n" + revision.changeNote : "")
				+ (revision.diff ? "\nClick to show the changes of this version" : "");
		}
		return data;
	}

	// built on first use, so loading this script does not fail without the CodeMirror bundle
	let shared = null;

	function sharedParts() {
		if (shared) return shared;
		const {StateField, StateEffect, ChangeSet, Compartment, Facet, Prec} = CodeMirror.state;
		const {EditorView, ViewPlugin, GutterMarker, gutter} = CodeMirror.view;

		// the page and the section of the edited text, the options `article` and `section` of KnowWEEditor.create()
		const source = Facet.define({combine: values => values.length ? values[0] : {}});
		const loaded = StateEffect.define();

		// Until the annotation arrives, the field keeps the text the editor started with and the changes since then.
		// Afterwards it holds the data of the action and the annotated ranges in the current text.
		const field = StateField.define({
			create: state => ({initialDoc: state.doc, changes: ChangeSet.empty(state.doc.length), data: null, entries: null}),
			update(value, tr) {
				let next = value;
				if (tr.docChanged) {
					next = value.data
						? {...value, entries: mapEntries(value.entries, tr.changes)}
						: {...value, changes: value.changes.compose(tr.changes)};
				}
				for (const effect of tr.effects) {
					if (!effect.is(loaded) || next.data) continue;
					const entries = effect.value.error ? [] : mapEntries(matchLines(next.initialDoc, effect.value), next.changes);
					next = {initialDoc: null, changes: null, data: effect.value, entries};
				}
				return next;
			}
		});

		class AnnotationMarker extends GutterMarker {
			constructor(revision, first) {
				super();
				this.revision = revision;
				this.first = first;
			}

			eq(other) {
				return other.revision === this.revision && other.first === this.first;
			}

			toDOM() {
				const dom = document.createElement("div");
				dom.className = "cm-knowwe-annotation" + (this.first ? " cm-knowwe-annotation-first" : "");
				dom.style.backgroundColor = "rgba(66, 139, 230, " + this.revision.alpha + ")";
				if (this.first) dom.textContent = this.revision.label;
				dom.title = this.revision.title;
				return dom;
			}
		}

		// one marker per revision and position in its group, so unchanged lines keep their DOM
		const markers = new WeakMap();

		function markerFor(revision, first) {
			let pair = markers.get(revision);
			if (!pair) {
				pair = [new AnnotationMarker(revision, false), new AnnotationMarker(revision, true)];
				markers.set(revision, pair);
			}
			return pair[first ? 1 : 0];
		}

		function revisionAt(state, line) {
			const value = state.field(field);
			if (!value.data || value.data.error) return null;
			const entry = entryAt(value.entries, line);
			return entry ? value.data.revisions[entry.revision] : null;
		}

		const annotationGutter = gutter({
			class: "cm-knowwe-annotations",
			lineMarker(view, block) {
				const doc = view.state.doc;
				const line = doc.lineAt(block.from);
				const revision = revisionAt(view.state, line);
				if (!revision) return null;
				const first = line.number === 1 || revisionAt(view.state, doc.line(line.number - 1)) !== revision;
				return markerFor(revision, first);
			},
			lineMarkerChange: update => update.startState.field(field) !== update.state.field(field),
			domEventHandlers: {
				click(view, block) {
					const revision = revisionAt(view.state, view.state.doc.lineAt(block.from));
					if (!revision || !revision.diff) return false;
					window.open(revision.diff, "_blank", "noopener");
					return true;
				}
			}
		});

		// the views whose annotation was requested, each view asks the action once
		const requested = new WeakSet();

		function load(view) {
			const value = view.state.field(field);
			if (requested.has(view) || value.data) return;
			requested.add(view);
			const {article, section} = view.state.facet(source);
			const deliver = data => {
				if (view.dom.isConnected) view.dispatch({effects: loaded.of(data)});
			};
			if (!article) {
				deliver({error: "no page"});
				return;
			}
			const params = new URLSearchParams({page: article, format: "json"});
			if (section) {
				params.set("SectionID", section);
				params.set("lines", String(value.initialDoc.lines));
			}
			fetch("action/AnnotatePageAction?" + params, {credentials: "same-origin"}).then(response => {
				if (!response.ok) throw new Error(response.status + " " + response.statusText);
				return response.json();
			}).then(data => deliver(prepare(data))).catch(error => {
				console.warn("Line annotations are not available", error);
				deliver({error: error.message});
			});
		}

		const loader = ViewPlugin.define(view => {
			load(view);
			return {};
		});

		const theme = EditorView.baseTheme({
			".cm-gutter.cm-knowwe-annotations": {
				width: "15ch",
				fontSize: "12px",
				color: "var(--text-color-secondary, #888)"
			},
			".cm-knowwe-annotations .cm-gutterElement": {
				padding: "0"
			},
			".cm-knowwe-annotation": {
				boxSizing: "border-box",
				height: "100%",
				padding: "0 6px",
				overflow: "hidden",
				whiteSpace: "nowrap",
				textOverflow: "ellipsis",
				cursor: "default"
			},
			".cm-knowwe-annotation-first": {
				boxShadow: "inset 0 1px 0 var(--border-color-default, #e2e2e9)",
				color: "var(--text-color-primary, #000)"
			}
		});

		shared = {
			source, field, enabled: new Compartment(),
			// before the line numbers, gutters are ordered by precedence
			extension: [Prec.high(annotationGutter), loader, theme]
		};
		return shared;
	}

	function stored() {
		try {
			return localStorage.getItem(STORAGE_KEY) === "true";
		} catch (e) {
			return false;
		}
	}

	/** Shows or hides the annotation gutter of an editor created by KnowWEEditor, and remembers the choice. */
	function setEnabled(view, enabled) {
		const parts = sharedParts();
		view.dispatch({effects: parts.enabled.reconfigure(enabled ? parts.extension : [])});
		try {
			localStorage.setItem(STORAGE_KEY, String(enabled));
		} catch (e) {
			// storage is optional
		}
	}

	/** Returns whether the editor shows the annotation gutter. */
	function isEnabled(view) {
		return sharedParts().enabled.get(view.state) === sharedParts().extension;
	}

	function isAvailable() {
		return typeof KnowWEEditor !== "undefined" && KnowWEEditor.isAvailable();
	}

	if (isAvailable() && KnowWEEditor.registerExtension) {
		KnowWEEditor.registerExtension(options => {
			const parts = sharedParts();
			return [
				parts.source.of({article: options.article ?? null, section: options.section ?? null}),
				parts.field,
				parts.enabled.of(stored() ? parts.extension : [])
			];
		});
	}

	return {setEnabled, isEnabled, isAvailable};
})();
