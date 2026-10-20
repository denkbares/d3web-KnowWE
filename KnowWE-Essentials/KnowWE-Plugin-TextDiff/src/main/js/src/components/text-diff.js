/**
 * <knowwe-text-diff> renders a text diff inside its own shadow DOM, as a CodeMirror view built in the browser or as
 * a table.
 *
 * Usage modes, any one of them:
 *
 *   1. Server rendered. The host carries a declarative shadow root. With the table renderer it holds the complete
 *      table, with the CodeMirror renderer a placeholder and both texts as JSON:
 *
 *        <knowwe-text-diff data-renderer="codemirror">
 *          <template shadowrootmode="open">
 *            <link rel="stylesheet" href=".../KnowWE-Plugin-TextDiff.css">
 *            <div class="diff-frame diff-placeholder"></div>
 *            <script type="application/json" class="diff-data">{"old": "...", "new": "...", "stats": {...}}</script>
 *          </template>
 *        </knowwe-text-diff>
 *
 *   2. Slotted raw text. The texts are diffed in the browser with the CodeMirror renderer, or posted to the backend
 *      with the table renderer, once the element scrolls near the viewport:
 *
 *        <knowwe-text-diff data-context-lines="3">
 *          <script type="text/plain" slot="old">...</script>
 *          <script type="text/plain" slot="new">...</script>
 *        </knowwe-text-diff>
 *
 *   3. Programmatic. Set the oldText and newText properties, the diff renders once both are set, or call load().
 *
 *   4. Endpoint driven. data-action-url names a backend action that derives the diff from the url itself. The
 *      CodeMirror renderer requests it with format=json, the table renderer as HTML:
 *
 *        <knowwe-text-diff data-action-url="action/MyDiffHtmlAction?file=..."></knowwe-text-diff>
 *
 * Attributes:
 *   - data-renderer:        codemirror | table (default). The CodeMirror renderer falls back to a table built in the
 *                           browser if the view cannot be created.
 *   - data-layout:          unified (default) | split, CodeMirror renderer only
 *   - data-language:        wiki (default) | none, the highlighting of the CodeMirror renderer
 *   - data-highlight-limit: texts above this many characters are shown without highlighting, default 200000
 *   - data-wrap:            false switches line wrapping off, CodeMirror renderer only
 *   - data-context-lines:   number of unchanged lines to show around each change, -1 shows all. Defaults to 3.
 *   - data-action-url:      the backend endpoint, default action/TextDiffAction
 *   - data-old-null:        the old side is missing, meaning a created file or article
 *   - data-new-null:        the new side is missing, meaning a deleted file or article
 *   - data-status:          set by the component to loading | ready | error
 *   - data-theme:           optional explicit light | dark theme override
 *
 * Events:
 *   - knowwe-text-diff-stats: dispatched (bubbling) once the browser computed the diff, detail.added and
 *                             detail.removed are its line counts.
 *
 * The CodeMirror view is created when the element comes near the viewport and destroyed again when it leaves it by
 * a wide margin, keeping its height, so long lists of diffs stay light.
 *
 * Test seam: assign a function to window.knowweTextDiffFetch to intercept the backend call.
 */
import {computeDiff} from "../diff/index.js";
import {renderTable} from "../renderer/table.js";
import {createDiffView} from "../renderer/codemirror.js";

const DEFAULT_ACTION_URL = "action/TextDiffAction";
const STYLESHEET_URL = "KnowWEExtension/css/KnowWE-Plugin-TextDiff.css";
const DEFAULT_CONTEXT_LINES = 3;
const DEFAULT_HIGHLIGHT_LIMIT = 200000;
// the distance from the viewport within which views exist
const VIEW_MARGIN = "600px";

function resourceUrl(path) {
	const base = window.knowweTextDiffResourceBase;
	return base ? new URL(path, base).toString() : path;
}

// the backend checks the access of the reader against the page the component sits on, so the request carries it
function withWikiParams(url, params = {}) {
	try {
		const u = new URL(url, window.location.href);
		if (typeof Wiki !== "undefined" && Wiki.CsrfProtection) u.searchParams.set("X-XSRF-TOKEN", Wiki.CsrfProtection);
		const page = typeof KNOWWE !== "undefined" && KNOWWE.helper.getPagename();
		if (page && !u.searchParams.has("KWiki_Topic")) u.searchParams.set("KWiki_Topic", page);
		for (const [name, value] of Object.entries(params)) u.searchParams.set(name, value);
		return u.toString();
	}
	catch (_) {
		return url;
	}
}

function displayMode() {
	return typeof KNOWWE !== "undefined" && KNOWWE.helper.getDisplayMode ? KNOWWE.helper.getDisplayMode() : "light";
}

function codeMirrorAvailable() {
	return typeof CodeMirror !== "undefined" && !!CodeMirror.merge;
}

export class KnowweTextDiff extends HTMLElement {
	connectedCallback() {
		if (!this.hasAttribute("data-theme")) this.setAttribute("data-theme", displayMode());
		if (this._connected) {
			// moved in the document, the view is rebuilt from the kept diff when it comes into view again
			if (this._diff && !this._editor) this._observe();
			return;
		}
		this._connected = true;
		this._hydrateTemplate();
		const root = this.shadowRoot;
		const inline = root && root.querySelector("script.diff-data");
		if (inline) {
			try {
				this._data = JSON.parse(inline.textContent);
				inline.remove();
				this._observe();
				return;
			}
			catch (err) {
				this._setStatus("error", "Invalid diff data");
				return;
			}
		}
		if (root && root.querySelector(".diff-frame")) {
			this._wireExpanders();
			return;
		}
		// only hosts that opted into lazy loading fetch, server rendered ones must never surface a load error
		if (this._hasLazyInputs()) this._observe();
	}

	disconnectedCallback() {
		this._unobserve();
		this._destroyView();
	}

	/** Set the "before" text. Once both oldText and newText are set, the diff renders. */
	set oldText(value) {
		this._oldText = value;
		this._maybeReload();
	}

	/** Set the "after" text. Once both oldText and newText are set, the diff renders. */
	set newText(value) {
		this._newText = value;
		this._maybeReload();
	}

	/** The line counts of the shown diff, once it was computed in the browser. */
	get stats() {
		return this._diff ? this._diff.stats : null;
	}

	/** Loads and renders the diff now. Returns a promise that resolves when the content is rendered. */
	load() {
		this._unobserve();
		this._data = null;
		this._diff = null;
		return this._render();
	}

	_renderer() {
		return this.getAttribute("data-renderer") === "codemirror" && codeMirrorAvailable() ? "codemirror" : "table";
	}

	_hasLazyInputs() {
		if (this.querySelector(':scope > [slot="old"]') !== null) return true;
		if (this.querySelector(':scope > [slot="new"]') !== null) return true;
		if (this.hasAttribute("data-action-url")) return true;
		return this.hasAttribute("data-old-text") || this.hasAttribute("data-new-text")
			|| this.hasAttribute("data-old-null") || this.hasAttribute("data-new-null");
	}

	_hydrateTemplate() {
		const tpl = this.querySelector(":scope > template[shadowrootmode]");
		if (!tpl) return;
		const root = this.shadowRoot || this.attachShadow({mode: tpl.getAttribute("shadowrootmode") || "open"});
		root.appendChild(tpl.content);
		tpl.remove();
	}

	_maybeReload() {
		if (!this.isConnected) return;
		if (this._oldText !== undefined && this._newText !== undefined) this.load();
	}

	// renders when the element comes near the viewport, and for the CodeMirror renderer frees the view when it leaves
	_observe() {
		if (this._observer) return;
		if (!("IntersectionObserver" in window)) {
			this._render();
			return;
		}
		this._observer = new IntersectionObserver(entries => {
			const visible = entries.some(e => e.isIntersecting);
			if (visible && !this._editor && !this._loading) this._render();
			else if (!visible && this._editor) this._parkView();
		}, {rootMargin: VIEW_MARGIN});
		this._observer.observe(this);
	}

	_unobserve() {
		if (this._observer) {
			this._observer.disconnect();
			this._observer = null;
		}
	}

	async _render() {
		if (this._diff) {
			this._showView();
			return;
		}
		this._loading = true;
		try {
			if (this._renderer() === "codemirror") {
				if (!this._data) this._data = await this._loadData();
				this._diff = computeDiff(this._data.old, this._data.new);
				this._showView();
				this.dispatchEvent(new CustomEvent("knowwe-text-diff-stats", {bubbles: true, detail: {...this._diff.stats}}));
			}
			else {
				await this._renderServerTable();
			}
		}
		catch (err) {
			this._setStatus("error", err && err.message);
		}
		finally {
			this._loading = false;
		}
	}

	// the texts, from the inline data, the endpoint, or the slotted and programmatic inputs without a request
	async _loadData() {
		this._ensureStylesheet();
		if (!this.hasAttribute("data-action-url")) {
			return {old: this._readTextInput("_oldText", "old", "data-old-text", "data-old-null"),
				new: this._readTextInput("_newText", "new", "data-new-text", "data-new-null")};
		}
		this._setStatus("loading");
		const fetchImpl = window.knowweTextDiffFetch || window.fetch.bind(window);
		const res = await fetchImpl(withWikiParams(this.getAttribute("data-action-url"), {format: "json"}), {
			method: "POST",
			headers: {"Content-Type": "application/json; charset=UTF-8"},
			body: JSON.stringify({contextLines: this._contextLines()})
		});
		if (!res.ok) throw new Error("HTTP " + res.status);
		return await res.json();
	}

	// the CodeMirror view, or the table built in the browser if the view fails
	_showView() {
		const root = this._ensureStylesheet();
		this._destroyView();
		const frame = this._frame();
		frame.replaceChildren();
		frame.classList.remove("diff-placeholder");
		frame.style.removeProperty("min-height");
		const size = (this._diff.docA.length + this._diff.docB.length);
		const limit = parseInt(this.getAttribute("data-highlight-limit"), 10);
		try {
			this._editor = createDiffView(frame, this._diff, {
				root,
				layout: this.getAttribute("data-layout") || "unified",
				language: this.getAttribute("data-language") || "wiki",
				highlight: size <= (Number.isNaN(limit) ? DEFAULT_HIGHLIGHT_LIMIT : limit),
				wrap: this.getAttribute("data-wrap") !== "false",
				contextLines: this._contextLines()
			});
			frame.classList.add("cm-diff");
		}
		catch (err) {
			console.warn("knowwe-text-diff: CodeMirror view failed, showing a table", err);
			frame.classList.remove("cm-diff");
			frame.innerHTML = renderTable(this._diff, {contextLines: this._contextLines()});
			this._wireExpanders();
		}
		this._setStatus("ready");
	}

	// frees the view of an element far from the viewport, the frame keeps its height until the view is rebuilt
	_parkView() {
		const frame = this._frame();
		frame.style.minHeight = frame.getBoundingClientRect().height + "px";
		this._destroyView();
		frame.replaceChildren();
	}

	_destroyView() {
		if (this._editor) {
			this._editor.destroy();
			this._editor = null;
		}
	}

	async _renderServerTable() {
		const oldText = this._readTextInput("_oldText", "old", "data-old-text", "data-old-null");
		const newText = this._readTextInput("_newText", "new", "data-new-text", "data-new-null");
		const url = this.getAttribute("data-action-url") || DEFAULT_ACTION_URL;
		const fetchImpl = window.knowweTextDiffFetch || window.fetch.bind(window);
		// the loading and error placeholders are styled by the stylesheet inside the shadow root, so it has to be
		// present before the first response arrives (and stays if the response never arrives)
		this._ensureStylesheet();
		this._setStatus("loading");
		const res = await fetchImpl(withWikiParams(url), {
			method: "POST",
			headers: {"Content-Type": "application/json; charset=UTF-8"},
			body: JSON.stringify({oldText, newText, contextLines: this._contextLines()})
		});
		if (!res.ok) throw new Error("HTTP " + res.status);
		this.shadowRoot.innerHTML = await res.text();
		this._wireExpanders();
		this._setStatus("ready");
	}

	_contextLines() {
		const n = parseInt(this.getAttribute("data-context-lines"), 10);
		return Number.isNaN(n) ? DEFAULT_CONTEXT_LINES : n;
	}

	_readSlot(name) {
		const el = this.querySelector(':scope > [slot="' + name + '"]');
		return el ? el.textContent : null;
	}

	_readTextInput(property, slotName, textAttribute, nullAttribute) {
		if (this[property] !== undefined) return this[property];
		if (this.hasAttribute(nullAttribute)) return null;
		const slotted = this._readSlot(slotName);
		if (slotted !== null) return slotted;
		const attributed = this.getAttribute(textAttribute);
		return attributed !== null ? attributed : "";
	}

	_ensureStylesheet() {
		const root = this.shadowRoot || this.attachShadow({mode: "open"});
		if (!root.querySelector('link[rel="stylesheet"]')) {
			const link = document.createElement("link");
			link.rel = "stylesheet";
			link.href = resourceUrl(STYLESHEET_URL);
			root.appendChild(link);
		}
		return root;
	}

	_frame() {
		const root = this.shadowRoot;
		let frame = root.querySelector(".diff-frame");
		if (!frame) {
			frame = document.createElement("div");
			frame.className = "diff-frame";
			frame.setAttribute("part", "frame");
			root.appendChild(frame);
		}
		return frame;
	}

	_setStatus(status, message) {
		this.setAttribute("data-status", status);
		if (message) this.setAttribute("data-status-message", message);
		else this.removeAttribute("data-status-message");
	}

	_wireExpanders() {
		const root = this.shadowRoot || this;
		root.querySelectorAll("tr.elided button.expand").forEach(btn => {
			btn.addEventListener("click", () => this._expand(btn));
		});
	}

	_expand(btn) {
		const elidedRow = btn.closest("tr.elided");
		if (!elidedRow) return;
		let row = elidedRow.nextElementSibling;
		while (row && row.classList.contains("hidden")) {
			row.classList.remove("hidden");
			row = row.nextElementSibling;
		}
		elidedRow.remove();
	}
}

if (!customElements.get("knowwe-text-diff")) customElements.define("knowwe-text-diff", KnowweTextDiff);
