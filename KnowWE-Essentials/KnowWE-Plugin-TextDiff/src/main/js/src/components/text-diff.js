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
 *   - data-layout:          unified (default) | split | auto, CodeMirror renderer only. auto shows wide elements
 *                           side by side and narrow ones in one column. A layout the user chose for the context
 *                           of the element takes precedence.
 *   - data-context:         a name under which choices of the user, such as the layout, are remembered and shared
 *                           by all elements of the same context
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
 *   - knowwe-text-diff-layout: dispatched (bubbling) by the CodeMirror renderer when the element connects and
 *                             whenever its layout changes, detail.layout is the requested and detail.effective the
 *                             shown layout, unified or split.
 *
 * Properties: layout (the requested layout, setting it remembers the choice for the context and applies it to
 * every element of the context), effectiveLayout (the shown layout, null with the table renderer).
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
// the width from which the auto layout shows both texts side by side
const SPLIT_MIN_WIDTH = 900;
const LAYOUTS = ["unified", "split"];
const LAYOUT_STORAGE_PREFIX = "knowwe-text-diff.layout.";
// how long and how often a rebuilt view is checked for the line that should stay at the top
const SETTLE_INTERVAL = 50;
const SETTLE_MAX_CHECKS = 20;
const SETTLE_STILL_CHECKS = 4;
// the connected elements, so a choice made on one reaches all elements of its context
const connectedElements = new Set();

function storedLayout(context) {
	if (!context) return null;
	try {
		const layout = window.localStorage.getItem(LAYOUT_STORAGE_PREFIX + context);
		return LAYOUTS.includes(layout) ? layout : null;
	}
	catch (_) {
		return null;
	}
}

function storeLayout(context, layout) {
	try {
		window.localStorage.setItem(LAYOUT_STORAGE_PREFIX + context, layout);
	}
	catch (_) {
		// storage is unavailable, the choice lasts for the page
	}
}

// the nearest scrolling ancestor, also across shadow roots, or the window
function scrollParent(element) {
	let node = element;
	while (node) {
		const parent = node.parentElement || (node.getRootNode() instanceof ShadowRoot ? node.getRootNode().host : null);
		if (!parent || parent === document.body || parent === document.documentElement) break;
		const overflow = getComputedStyle(parent).overflowY;
		if ((overflow === "auto" || overflow === "scroll") && parent.scrollHeight > parent.clientHeight) return parent;
		node = parent;
	}
	return window;
}

function scrollTop(container) {
	return container === window ? window.scrollY : container.scrollTop;
}

// where the top of the line at pos is on screen, measured by the view where the line is rendered
function lineTop(editor, pos) {
	const coords = editor.coordsAtPos(pos);
	return coords ? coords.top : editor.lineBlockAt(pos).top + editor.documentTop;
}

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
	static get observedAttributes() {
		return ["data-layout"];
	}

	connectedCallback() {
		if (!this.hasAttribute("data-theme")) this.setAttribute("data-theme", displayMode());
		connectedElements.add(this);
		if (this._connected) {
			// moved in the document, the view is rebuilt from the kept diff when it comes into view again
			if (this._diff && !this._editor) this._observe();
			this._observeSize();
			return;
		}
		this._connected = true;
		this._hydrateTemplate();
		this._observeSize();
		this._announceLayout();
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
		connectedElements.delete(this);
		this._unobserve();
		this._unobserveSize();
		this._destroyView();
	}

	attributeChangedCallback(name) {
		if (name === "data-layout" && this._connected) {
			this._observeSize();
			this._applyLayout();
		}
	}

	/** The requested layout, the choice remembered for the context of the element or its data-layout attribute. */
	get layout() {
		return storedLayout(this._context()) || this.getAttribute("data-layout") || "unified";
	}

	/**
	 * Chooses unified or split. With a context the choice is remembered and applied to every element of the
	 * context, without one it becomes the data-layout attribute of this element.
	 */
	set layout(value) {
		if (!LAYOUTS.includes(value)) throw new Error("Unknown layout " + value);
		const context = this._context();
		if (!context) {
			this.setAttribute("data-layout", value);
			return;
		}
		storeLayout(context, value);
		for (const element of connectedElements) {
			if (element._context() === context) element._applyLayout();
		}
	}

	/** The shown layout, unified or split, or null when the diff is a table. */
	get effectiveLayout() {
		return this._renderer() === "codemirror" ? this._resolveLayout() : null;
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

	_context() {
		return this.getAttribute("data-context") || null;
	}

	// the auto layout follows the width of the element
	_observeSize() {
		const auto = this.layout === "auto" && "ResizeObserver" in window;
		if (!auto) {
			this._unobserveSize();
			return;
		}
		if (this._sizeObserver) return;
		this._sizeObserver = new ResizeObserver(() => this._applyLayout());
		this._sizeObserver.observe(this);
	}

	_unobserveSize() {
		if (this._sizeObserver) {
			this._sizeObserver.disconnect();
			this._sizeObserver = null;
		}
	}

	_resolveLayout() {
		const layout = this.layout;
		if (layout === "split") return "split";
		if (layout === "auto") return this.getBoundingClientRect().width >= SPLIT_MIN_WIDTH ? "split" : "unified";
		return "unified";
	}

	// rebuilds an existing view when the shown layout changed, keeping the line at the top of the viewport in place
	_applyLayout() {
		if (this._editor && this._editor.layout !== this._resolveLayout()) {
			const anchor = this._topLine();
			this._showView();
			if (anchor) this._keepLine(anchor);
		}
		this._announceLayout();
	}

	_announceLayout() {
		if (this._renderer() !== "codemirror") return;
		const detail = {layout: this.layout, effective: this._resolveLayout()};
		if (this._announced && this._announced.layout === detail.layout && this._announced.effective === detail.effective) return;
		this._announced = detail;
		this.dispatchEvent(new CustomEvent("knowwe-text-diff-layout", {bubbles: true, detail}));
	}

	// the line of the new text at the top of the scrolling area and where its top is on screen
	_topLine() {
		try {
			const editor = this._editor.editor;
			const container = scrollParent(this);
			const containerTop = container === window ? 0 : container.getBoundingClientRect().top;
			const y = Math.max(this.getBoundingClientRect().top, containerTop);
			const block = editor.lineBlockAtHeight(y - editor.documentTop);
			return {container, pos: block.from, top: lineTop(editor, block.from), scrollTop: scrollTop(container)};
		}
		catch (_) {
			return null;
		}
	}

	// Puts the line back where it was, now and again while the new view settles, since line heights away from the
	// viewport are estimates until measured and the split view pads its sides in later passes. Stops once the
	// line held still for a few checks, when the view changes again or when the user scrolls on.
	_keepLine(anchor) {
		const editor = this._editor && this._editor.editor;
		if (!editor) return;
		let still = 0, checks = 0;
		const check = () => {
			if (!this._editor || this._editor.editor !== editor || scrollTop(anchor.container) !== anchor.scrollTop) return;
			still = this._scrollToLine(anchor) ? still + 1 : 0;
			if (still < SETTLE_STILL_CHECKS && ++checks < SETTLE_MAX_CHECKS) setTimeout(check, SETTLE_INTERVAL);
		};
		anchor.scrollTop = scrollTop(anchor.container);
		check();
	}

	// scrolls until the line sits at its recorded place, measuring the view anew after each step, and tells
	// whether it was there already
	_scrollToLine(anchor) {
		const editor = this._editor.editor;
		const {container, pos, top} = anchor;
		let moved = false;
		try {
			const from = Math.min(pos, editor.state.doc.length);
			for (let i = 0; i < 8; i++) {
				this._editor.align();
				editor.requestMeasure();
				const delta = lineTop(editor, from) - top;
				if (Math.abs(delta) < 1) break;
				moved = true;
				if (container === window) window.scrollBy(0, delta);
				else container.scrollTop += delta;
			}
		}
		catch (_) {
			// the line is not reachable in the new view, the scroll position stays
		}
		anchor.scrollTop = scrollTop(container);
		return !moved;
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
		const layout = this._resolveLayout();
		try {
			this._editor = createDiffView(frame, this._diff, {
				root,
				layout,
				language: this.getAttribute("data-language") || "wiki",
				highlight: size <= (Number.isNaN(limit) ? DEFAULT_HIGHLIGHT_LIMIT : limit),
				wrap: this.getAttribute("data-wrap") !== "false",
				contextLines: this._contextLines()
			});
			this._editor.layout = layout;
			frame.classList.add("cm-diff");
			frame.setAttribute("data-layout", layout);
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
