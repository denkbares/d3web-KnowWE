/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

/**
 * Shows the change annotation of the Annotate tab in a gutter of the CodeMirror editors of KnowWEEditor: the version
 * that last changed a line, its date and author, in the columns of the Annotate tab. Each line repeats them, a line
 * above the first line of a version separates it from the previous one, and newer versions are tinted stronger. Hover
 * shows the change note in a tooltip, a click opens the changes of the version.
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

	function escapeHtml(text) {
		return String(text).replace(/[&<>"']/g, c => ({"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"})[c]);
	}

	/** The content of the tooltip of a revision: version, date, author and change note. */
	function tooltipHtml(revision) {
		const version = "v" + revision.version;
		const rows = [
			["Version", revision.diff
				? "<a href='" + escapeHtml(revision.diff) + "' target='_blank' title='Show the changes of this version'>"
				+ version + "</a>"
				: version],
			["Date", escapeHtml(formatDate(revision.date, true))],
			["Author", escapeHtml(revision.author)]
		];
		if (revision.changeNote) rows.push(["Change note", escapeHtml(revision.changeNote)]);
		return "<table class='knowwe-annotation-tooltip'>"
			+ rows.map(([name, value]) => "<tr><th>" + name + "</th><td>" + value + "</td></tr>").join("")
			+ "</table>";
	}

	/**
	 * Adds the labels, the tooltip and the tint of each revision to the data of the action.
	 */
	function prepare(data) {
		const versions = data.revisions.map(revision => revision.version);
		const newest = Math.max(...versions), oldest = Math.min(...versions);
		for (const revision of data.revisions) {
			const age = newest === oldest ? 1 : (revision.version - oldest) / (newest - oldest);
			revision.alpha = (0.04 + 0.22 * age).toFixed(3);
			revision.versionLabel = "v" + revision.version;
			// the date column of the Annotate tab, the day in UTC
			revision.dateLabel = String(revision.date).slice(0, 10);
			revision.tooltip = tooltipHtml(revision);
		}
		return data;
	}

	// the widest author column, enough for usual names, longer ones end with an ellipsis and show in the tooltip
	const MAX_AUTHOR_WIDTH = 220;

	/**
	 * Sets the columns of the data as wide as their widest entry in the font of the gutter, so the gutter is no wider
	 * than it needs to be and every line shows its entries aligned.
	 */
	function measureColumns(view, data) {
		const probe = document.createElement("span");
		probe.className = "cm-knowwe-annotation-probe";
		view.dom.appendChild(probe);
		const width = text => {
			probe.textContent = text;
			return Math.ceil(probe.getBoundingClientRect().width) + 1;
		};
		let version = 0, date = 0, author = 0;
		for (const revision of data.revisions) {
			version = Math.max(version, width(revision.versionLabel));
			date = Math.max(date, width(revision.dateLabel));
			author = Math.max(author, width(revision.author));
		}
		probe.remove();
		data.columns = version + "px " + date + "px " + Math.min(author, MAX_AUTHOR_WIDTH) + "px";
		return data;
	}

	/** Shows the tooltip of a revision on hover, with tooltipster where KnowWE provides it, a title otherwise. */
	function attachTooltip(dom, revision) {
		if (typeof jq$ === "undefined" || !jq$.fn.tooltipster) {
			dom.title = revision.versionLabel + ", " + formatDate(revision.date, true) + ", " + revision.author
				+ (revision.changeNote ? "\n" + revision.changeNote : "");
			return;
		}
		jq$(dom).tooltipster({
			content: revision.tooltip,
			contentAsHTML: true,
			interactive: true,
			position: "right",
			delay: 400,
			updateAnimation: false,
			theme: ".tooltipster-knowwe"
		});
	}

	// the style of the tooltips, which tooltipster puts outside the editor
	function addTooltipStyle() {
		if (document.getElementById("knowwe-annotation-tooltip-style")) return;
		const style = document.createElement("style");
		style.id = "knowwe-annotation-tooltip-style";
		style.textContent = ".knowwe-annotation-tooltip { border-collapse: collapse; font-size: 13px; line-height: 18px; }"
			+ " .knowwe-annotation-tooltip th { padding: 1px 12px 1px 0; text-align: left; font-weight: normal;"
			+ " color: var(--text-color-secondary, #57606a); vertical-align: top; white-space: nowrap; }"
			+ " .knowwe-annotation-tooltip td { padding: 1px 0; white-space: pre-wrap; max-width: 420px; }";
		document.head.appendChild(style);
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
			constructor(revision, first, columns) {
				super();
				this.revision = revision;
				this.first = first;
				this.columns = columns;
			}

			eq(other) {
				return other.revision === this.revision && other.first === this.first;
			}

			toDOM() {
				const dom = document.createElement("div");
				dom.className = "cm-knowwe-annotation" + (this.first ? " cm-knowwe-annotation-first" : "");
				dom.style.gridTemplateColumns = this.columns;
				dom.style.setProperty("--knowwe-annotation-alpha", this.revision.alpha);
				for (const [className, text] of [["version", this.revision.versionLabel],
					["date", this.revision.dateLabel], ["author", this.revision.author]]) {
					const cell = document.createElement("span");
					cell.className = "cm-knowwe-annotation-" + className;
					cell.textContent = text;
					dom.appendChild(cell);
				}
				attachTooltip(dom, this.revision);
				return dom;
			}
		}

		// one marker per revision and position in its group, so unchanged lines keep their DOM
		const markers = new WeakMap();

		function markerFor(data, revision, first) {
			let pair = markers.get(revision);
			if (!pair) {
				pair = [new AnnotationMarker(revision, false, data.columns), new AnnotationMarker(revision, true, data.columns)];
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
				return markerFor(view.state.field(field).data, revision, first);
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
			}).then(data => deliver(measureColumns(view, prepare(data)))).catch(error => {
				console.warn("Line annotations are not available", error);
				deliver({error: error.message});
			});
		}

		const loader = ViewPlugin.define(view => {
			load(view);
			return {};
		});

		// the look of the Annotate tab: gray version and date, the author in the text color, hover in yellow
		const theme = EditorView.baseTheme({
			".cm-gutter.cm-knowwe-annotations": {
				fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif",
				// the size of the editor text, like the line numbers
				fontSize: "inherit",
				fontVariantNumeric: "tabular-nums",
				color: "var(--text-color-secondary, #57606a)",
				borderRight: "1px solid var(--border-color-default, #d0d7de)"
			},
			// measures the columns in the font of the gutter, see measureColumns()
			".cm-knowwe-annotation-probe": {
				position: "absolute",
				visibility: "hidden",
				whiteSpace: "nowrap",
				fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif",
				fontVariantNumeric: "tabular-nums"
			},
			".cm-knowwe-annotations .cm-gutterElement": {
				padding: "0"
			},
			".cm-knowwe-annotation": {
				display: "grid",
				columnGap: "10px",
				boxSizing: "border-box",
				height: "100%",
				padding: "0 10px",
				whiteSpace: "nowrap",
				backgroundColor: "rgba(66, 139, 230, var(--knowwe-annotation-alpha, 0))",
				cursor: "pointer"
			},
			".cm-knowwe-annotation > span": {
				overflow: "hidden",
				textOverflow: "ellipsis"
			},
			".cm-knowwe-annotation-version": {
				color: "var(--link-color, #0969da)"
			},
			".cm-knowwe-annotation-author": {
				color: "var(--text-color-primary, #1f2328)"
			},
			".cm-knowwe-annotation-first": {
				boxShadow: "inset 0 1px 0 var(--border-color-default, #d0d7de)"
			},
			".cm-knowwe-annotation:hover": {
				backgroundColor: "var(--highlight-color-faint-yellow, #fff8c5)"
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
		addTooltipStyle();
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
