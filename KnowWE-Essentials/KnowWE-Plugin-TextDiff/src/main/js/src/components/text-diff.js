/**
 * <knowwe-text-diff> renders a text diff inside its own shadow DOM as a CodeMirror view built in the browser from
 * both texts. The texts come from one of three places:
 *
 *   1. Server rendered. The host carries a declarative shadow root with a placeholder and both texts as JSON:
 *
 *        <knowwe-text-diff data-layout="auto" data-context="page-history">
 *          <template shadowrootmode="open">
 *            <link rel="stylesheet" href=".../KnowWE-Plugin-TextDiff.css">
 *            <div class="diff-frame diff-placeholder"></div>
 *            <script type="application/json" class="diff-data">{"old": "...", "new": "..."}</script>
 *          </template>
 *        </knowwe-text-diff>
 *
 *   2. Attributes. data-old-text and data-new-text hold the texts, data-old-null and data-new-null mark a missing
 *      side, meaning a created or a deleted article. An absent attribute is the empty text.
 *
 *   3. Endpoint driven. data-action-url names a backend action that answers the same JSON document:
 *
 *        <knowwe-text-diff data-action-url="action/MyDiffAction?file=..."></knowwe-text-diff>
 *
 * Attributes:
 *   - data-layout:          unified (default) | split | auto. auto shows wide elements side by side and narrow ones
 *                           in one column. A layout the user chose for the context of the element takes precedence.
 *   - data-layouts:         the layouts the user may choose, "unified split" by default. With a single one the
 *                           layout is fixed and a frame around the element offers no switch.
 *   - data-context:         a name under which choices of the user, the layout and the whitespace mode, are
 *                           remembered and shared by all elements of the same context. Without one the element has
 *                           no choices to offer.
 *   - data-language:        wiki (default) | none, the highlighting
 *   - data-highlight-limit: texts above this many characters are shown without highlighting, default 200000
 *   - data-wrap:            false switches line wrapping off
 *   - data-context-lines:   number of unchanged lines to show around each change, -1 shows all. Defaults to 3.
 *   - data-status:          set by the component to loading | ready | error
 *   - data-theme:           optional explicit light | dark theme override
 *
 * Events:
 *   - knowwe-text-diff-stats: dispatched (bubbling) once the diff was computed, detail.added and detail.removed are
 *                             its line counts, detail.ignoreWhitespace the mode they count.
 *   - knowwe-text-diff-layout: dispatched (bubbling) when the element connects and whenever its layout changes,
 *                             detail.layout is the requested and detail.effective the shown layout, unified or
 *                             split, detail.layouts the choosable ones.
 *   - knowwe-text-diff-whitespace: dispatched (bubbling) when the element connects and whenever its whitespace mode
 *                             changes, detail.ignore tells whether whitespace is ignored.
 *
 * Properties: layout (the requested layout), layouts (the choosable ones), effectiveLayout (the shown layout),
 * ignoreWhitespace (the whitespace mode), stats (the line counts). Setting layout or ignoreWhitespace remembers the
 * choice for the context and applies it to every element of the context. The exported preferences object reads and
 * sets these choices for a context without an element at hand.
 *
 * The view is created when the element comes near the viewport and destroyed again when it leaves it by a wide
 * margin, keeping its height, so long lists of diffs stay light. When the texts cannot be loaded or the view cannot
 * be built, the element shows an error note. The bundle itself needs the CodeMirror bundle of KnowWE-Resources,
 * which loads before it on every page.
 *
 * Test seam: assign a function to window.knowweTextDiffFetch to intercept the backend call.
 */
import {computeDiff} from "../diff/index.js";
import {createDiffView} from "../renderer/codemirror.js";

const STYLESHEET_URL = "KnowWEExtension/css/KnowWE-Plugin-TextDiff.css";
const DEFAULT_CONTEXT_LINES = 3;
const DEFAULT_HIGHLIGHT_LIMIT = 200000;
// the distance from the viewport within which views exist
const VIEW_MARGIN = "600px";
// the width from which the auto layout shows both texts side by side
const SPLIT_MIN_WIDTH = 900;
const LAYOUTS = ["unified", "split"];
const WHITESPACE_MODES = ["show", "ignore"];
const STORAGE_PREFIX = "knowwe-text-diff.";
// how long and how often a rebuilt view is checked for the line that should stay at the top
const SETTLE_INTERVAL = 50;
const SETTLE_MAX_CHECKS = 20;
const SETTLE_STILL_CHECKS = 4;
// the connected elements, so a choice made on one reaches all elements of its context
const connectedElements = new Set();

// a choice of the user for a context, such as the layout, kept in local storage under knowwe-text-diff.<key>.<context>
function storedChoice(context, key, values) {
	if (!context) return null;
	try {
		const value = window.localStorage.getItem(STORAGE_PREFIX + key + "." + context);
		return values.includes(value) ? value : null;
	}
	catch (_) {
		return null;
	}
}

function storeChoice(context, key, value) {
	try {
		window.localStorage.setItem(STORAGE_PREFIX + key + "." + context, value);
	}
	catch (_) {
		// storage is unavailable, the choice lasts for the page
	}
}

const CHOICES = {layout: LAYOUTS, whitespace: WHITESPACE_MODES};

/**
 * The choices of the user per context. Keys are "layout" (unified or split) and "whitespace" (show or ignore).
 * Setting one stores it and applies it to every connected element of the context.
 */
export const preferences = {
	get(context, key) {
		return storedChoice(context, key, CHOICES[key] || []);
	},
	set(context, key, value) {
		if (!CHOICES[key] || !CHOICES[key].includes(value)) throw new Error("Unknown " + key + " " + value);
		storeChoice(context, key, value);
		for (const element of connectedElements) {
			if (element.context === context) element._applyChoice(key);
		}
	}
};

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
function withWikiParams(url) {
	try {
		const u = new URL(url, window.location.href);
		if (typeof Wiki !== "undefined" && Wiki.CsrfProtection) u.searchParams.set("X-XSRF-TOKEN", Wiki.CsrfProtection);
		const page = typeof KNOWWE !== "undefined" && KNOWWE.helper.getPagename();
		if (page && !u.searchParams.has("KWiki_Topic")) u.searchParams.set("KWiki_Topic", page);
		return u.toString();
	}
	catch (_) {
		return url;
	}
}

function displayMode() {
	return typeof KNOWWE !== "undefined" && KNOWWE.helper.getDisplayMode ? KNOWWE.helper.getDisplayMode() : "light";
}

export class KnowweTextDiff extends HTMLElement {
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
		this._announceWhitespace();
		const inline = this.shadowRoot && this.shadowRoot.querySelector("script.diff-data");
		if (inline) {
			try {
				this._data = JSON.parse(inline.textContent);
				inline.remove();
			}
			catch (err) {
				this._setStatus("error", "Invalid diff data");
				return;
			}
		}
		this._observe();
	}

	disconnectedCallback() {
		connectedElements.delete(this);
		this._unobserve();
		this._unobserveSize();
		this._destroyView();
	}

	/** The name under which the choices of the user are remembered, or null. */
	get context() {
		return this.getAttribute("data-context") || null;
	}

	/** The requested layout, the choice remembered for the context of the element or its data-layout attribute. */
	get layout() {
		const chosen = storedChoice(this.context, "layout", this.layouts);
		return chosen || this.getAttribute("data-layout") || "unified";
	}

	/** Chooses unified or split for the context of the element, which remembers it and applies it to every element. */
	set layout(value) {
		if (!this.layouts.includes(value)) throw new Error("Layout " + value + " is not available");
		if (this.context) preferences.set(this.context, "layout", value);
	}

	/** The layouts the user may choose, from the data-layouts attribute. */
	get layouts() {
		const named = (this.getAttribute("data-layouts") || "").split(/\s+/).filter(l => LAYOUTS.includes(l));
		return named.length ? named : LAYOUTS;
	}

	/** Whether whitespace is ignored, by the choice remembered for the context. */
	get ignoreWhitespace() {
		return storedChoice(this.context, "whitespace", WHITESPACE_MODES) === "ignore";
	}

	/** Switches the whitespace mode, remembered and applied like the layout. */
	set ignoreWhitespace(value) {
		if (this.context) preferences.set(this.context, "whitespace", value ? "ignore" : "show");
	}

	_applyChoice(key) {
		if (key === "layout") this._applyLayout();
		else this._applyWhitespace();
	}

	/** The shown layout, unified or split. */
	get effectiveLayout() {
		return this._resolveLayout();
	}

	/** The line counts of the shown diff, once it was computed. */
	get stats() {
		return this._diff ? this._diff.stats : null;
	}

	_hydrateTemplate() {
		const tpl = this.querySelector(":scope > template[shadowrootmode]");
		if (!tpl) return;
		const root = this.shadowRoot || this.attachShadow({mode: tpl.getAttribute("shadowrootmode") || "open"});
		root.appendChild(tpl.content);
		tpl.remove();
	}

	// renders when the element comes near the viewport and frees the view when it leaves
	_observe() {
		if (this._observer) return;
		if (!("IntersectionObserver" in window)) {
			this._render();
			return;
		}
		this._observer = new IntersectionObserver(entries => {
			const visible = entries.some(e => e.isIntersecting);
			if (visible && !this._rendered && !this._loading) this._render();
			else if (!visible && this._rendered) this._parkView();
		}, {rootMargin: VIEW_MARGIN});
		this._observer.observe(this);
	}

	_unobserve() {
		if (this._observer) {
			this._observer.disconnect();
			this._observer = null;
		}
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
		this._observeSize();
		if (this._editor && this._editor.layout !== this._resolveLayout()) {
			const anchor = this._topLine();
			this._showView();
			if (anchor) this._keepLine(anchor);
		}
		this._announceLayout();
	}

	_announceLayout() {
		const detail = {layout: this.layout, effective: this._resolveLayout(), layouts: this.layouts};
		if (this._announced && this._announced.layout === detail.layout && this._announced.effective === detail.effective) return;
		this._announced = detail;
		this.dispatchEvent(new CustomEvent("knowwe-text-diff-layout", {bubbles: true, detail}));
	}

	// diffs the kept texts anew in the current whitespace mode and shows the result where a view is shown
	_applyWhitespace() {
		const ignore = this.ignoreWhitespace;
		if (this._data && this._diff && this._diff.ignoreWhitespace !== ignore) {
			this._diff = this._computeDiff();
			if (this._rendered) {
				const anchor = this._editor ? this._topLine() : null;
				this._showView();
				if (anchor) this._keepLine(anchor);
			}
			this._dispatchStats();
		}
		this._announceWhitespace();
	}

	_announceWhitespace() {
		const ignore = this.ignoreWhitespace;
		if (this._announcedWhitespace === ignore) return;
		this._announcedWhitespace = ignore;
		this.dispatchEvent(new CustomEvent("knowwe-text-diff-whitespace", {bubbles: true, detail: {ignore}}));
	}

	_computeDiff() {
		const ignoreWhitespace = this.ignoreWhitespace;
		return {...computeDiff(this._data.old, this._data.new, {ignoreWhitespace}), ignoreWhitespace};
	}

	_dispatchStats() {
		const detail = {...this._diff.stats, ignoreWhitespace: this._diff.ignoreWhitespace};
		this.dispatchEvent(new CustomEvent("knowwe-text-diff-stats", {bubbles: true, detail}));
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
	// line held still for a few checks, when scrolling has no effect, when the view changes again or when the
	// user scrolls on.
	_keepLine(anchor) {
		const editor = this._editor && this._editor.editor;
		if (!editor) return;
		let still = 0, checks = 0;
		const check = () => {
			if (!this._editor || this._editor.editor !== editor || scrollTop(anchor.container) !== anchor.scrollTop) return;
			still = this._scrollToLine(anchor) ? still + 1 : 0;
			if (!anchor.stuck && still < SETTLE_STILL_CHECKS && ++checks < SETTLE_MAX_CHECKS) setTimeout(check, SETTLE_INTERVAL);
		};
		anchor.scrollTop = scrollTop(anchor.container);
		check();
	}

	// Scrolls until the line sits at its recorded place, measuring the view anew after each step, and tells
	// whether it was there already. Gives up, marking the anchor stuck, when a scroll step does not move the
	// container, which happens at its ends or when something else scrolls the element.
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
				const before = scrollTop(container);
				if (container === window) window.scrollBy(0, delta);
				else container.scrollTop += delta;
				if (scrollTop(container) === before) {
					anchor.stuck = true;
					break;
				}
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
			if (!this._data) this._data = await this._loadData();
			this._diff = this._computeDiff();
			this._showView();
			this._dispatchStats();
		}
		catch (err) {
			console.warn("knowwe-text-diff: cannot show the diff", err);
			// the error note is styled by the stylesheet in the shadow root and replaces the placeholder
			const frame = this._ensureStylesheet().querySelector(".diff-frame");
			if (frame) frame.remove();
			this._setStatus("error", err && err.message);
		}
		finally {
			this._loading = false;
		}
	}

	// the texts, from the attributes or the endpoint
	async _loadData() {
		this._ensureStylesheet();
		if (!this.hasAttribute("data-action-url")) {
			return {old: this._readTextAttribute("data-old-text", "data-old-null"),
				new: this._readTextAttribute("data-new-text", "data-new-null")};
		}
		this._setStatus("loading");
		const fetchImpl = window.knowweTextDiffFetch || window.fetch.bind(window);
		const res = await fetchImpl(withWikiParams(this.getAttribute("data-action-url")), {method: "POST"});
		if (!res.ok) throw new Error("HTTP " + res.status);
		return await res.json();
	}

	_readTextAttribute(textAttribute, nullAttribute) {
		if (this.hasAttribute(nullAttribute)) return null;
		return this.getAttribute(textAttribute) || "";
	}

	_showView() {
		const root = this._ensureStylesheet();
		this._destroyView();
		const frame = this._frame();
		frame.replaceChildren();
		frame.classList.remove("diff-placeholder");
		frame.style.removeProperty("min-height");
		this._rendered = true;
		if (this._diff.stats.added === 0 && this._diff.stats.removed === 0) {
			// nothing to build a view of, which deserves a word when whitespace changes are hidden
			const note = this._diff.ignoreWhitespace && this._data.old !== this._data.new
				? "Only whitespace changed." : "No changes.";
			frame.classList.remove("cm-diff");
			frame.innerHTML = '<p class="diff-note" part="note">' + note + '</p>';
			this._setStatus("ready");
			return;
		}
		const size = (this._diff.docA.length + this._diff.docB.length);
		const limit = parseInt(this.getAttribute("data-highlight-limit"), 10);
		const layout = this._resolveLayout();
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
		this._setStatus("ready");
	}

	// frees the view of an element far from the viewport, the frame keeps its height until the view is rebuilt
	_parkView() {
		const frame = this._frame();
		frame.style.minHeight = frame.getBoundingClientRect().height + "px";
		this._destroyView();
		frame.replaceChildren();
		this._rendered = false;
	}

	_destroyView() {
		if (this._editor) {
			this._editor.destroy();
			this._editor = null;
		}
	}

	_contextLines() {
		const n = parseInt(this.getAttribute("data-context-lines"), 10);
		return Number.isNaN(n) ? DEFAULT_CONTEXT_LINES : n;
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
}

if (!customElements.get("knowwe-text-diff")) customElements.define("knowwe-text-diff", KnowweTextDiff);
