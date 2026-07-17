/**
 * <knowwe-text-diff> — a self-hydrating web component that renders a unified text diff
 * inside its own shadow DOM.
 *
 * Usage modes (any one of them):
 *
 *   1. Server-rendered (preferred for static page loads). The server emits the host element
 *      with a declarative shadow root already attached:
 *
 *        <knowwe-text-diff>
 *          <template shadowrootmode="open">
 *            <link rel="stylesheet" href="…/KnowWE-Plugin-TextDiff.css">
 *            <table class="diff">…</table>
 *          </template>
 *        </knowwe-text-diff>
 *
 *      The component just wires up the "expand elided region" buttons.
 *
 *   2. Slotted raw text (lazy). The host has no shadow content; the diff is computed on the
 *      backend the first time the element scrolls near the viewport:
 *
 *        <knowwe-text-diff data-context-lines="3">
 *          <script type="text/plain" slot="old">…raw old text…</script>
 *          <script type="text/plain" slot="new">…raw new text…</script>
 *        </knowwe-text-diff>
 *
 *   3. Programmatic (lazy). Set the JS properties; a fetch is triggered automatically once
 *      both are set:
 *
 *        const el = document.querySelector('knowwe-text-diff');
 *        el.oldText = '…';
 *        el.newText = '…';
 *
 *      Or call el.load() to force a refresh.
 *
 *   4. Endpoint driven (lazy). Set data-action-url to a backend action that derives the diff
 *      from the URL itself (query parameters) and returns the rendered shadow content. No old
 *      or new text is needed on the host:
 *
 *        <knowwe-text-diff data-action-url="action/MyDiffHtmlAction?file=..."></knowwe-text-diff>
 *
 *      Like the other lazy modes, the fetch is deferred until the element becomes visible near
 *      the viewport. An element inside a collapsed (display none) container therefore loads
 *      when it is expanded.
 *
 * Attributes:
 *   - data-context-lines: number of unchanged lines to show around each hunk; -1 disables
 *                         elision. Defaults to 3.
 *   - data-action-url:    override the backend endpoint (default: 'action/TextDiffAction.action').
 *   - data-old-null:      send oldText as null in lazy mode, meaning "created file/article".
 *   - data-new-null:      send newText as null in lazy mode, meaning "deleted file/article".
 *   - data-status:        set by the component to 'loading' | 'ready' | 'error' so host pages
 *                         can style the loading and error placeholders via ::part or :host().
 *   - data-theme:         optional explicit 'light' | 'dark' theme override; otherwise CSS uses
 *                         prefers-color-scheme as fallback.
 *
 * Theming: every visual element exposes a CSS shadow part — see ::part(line added),
 * ::part(line removed), ::part(num old|new), ::part(sign), ::part(text), ::part(elided),
 * ::part(expand-button). CSS custom properties (--diff-added-bg etc.) pierce the shadow
 * boundary too.
 *
 * Test seam: assign a function to window.knowweTextDiffFetch to intercept the backend call
 * (used by the demo page to mock responses).
 */
(function () {
	if (customElements.get('knowwe-text-diff')) return;

	const DEFAULT_ACTION_URL = 'action/TextDiffAction';

	function withCsrf(url) {
		try {
			const u = new URL(url, window.location.href);
			if (typeof Wiki !== 'undefined' && Wiki.CsrfProtection) {
				u.searchParams.set('X-XSRF-TOKEN', Wiki.CsrfProtection);
			}
			return u.toString();
		}
		catch (_) {
			return url;
		}
	}

	class KnowweTextDiff extends HTMLElement {
		connectedCallback() {
			if (this._hasShadowContent()) {
				this._wireExpanders();
				return;
			}
			// Only auto-fetch when the host actually opted into lazy mode (slots or data-old/new-text/null).
			// Server-rendered components without these inputs must never trigger a network call — that would
			// surface as a "Failed to load diff" error overlay even when the page already shows a valid diff
			// (e.g. when the declarative shadow DOM hydrated through a path our heuristic missed).
			if (!this._hasLazyInputs()) return;
			this._scheduleLazyLoad();
		}

		_hasLazyInputs() {
			if (this.querySelector(':scope > [slot="old"]') !== null) return true;
			if (this.querySelector(':scope > [slot="new"]') !== null) return true;
			// a custom action url means the endpoint derives the diff from the url itself,
			// so the component is lazily loadable without any old or new text inputs
			if (this.hasAttribute('data-action-url')) return true;
			return this.hasAttribute('data-old-text')
					|| this.hasAttribute('data-new-text')
					|| this.hasAttribute('data-old-null')
					|| this.hasAttribute('data-new-null');
		}

		disconnectedCallback() {
			if (this._observer) {
				this._observer.disconnect();
				this._observer = null;
			}
		}

		/** Set the "before" text. Once both oldText and newText are set, a fetch is triggered. */
		set oldText(value) { this._oldText = value; this._maybeReload(); }
		/** Set the "after" text. Once both oldText and newText are set, a fetch is triggered. */
		set newText(value) { this._newText = value; this._maybeReload(); }

		/** Force a reload from the backend. Returns a promise that resolves when content is rendered. */
		load() { return this._fetchAndRender(); }

		_hasShadowContent() {
			if (this.shadowRoot && this.shadowRoot.firstElementChild) return true;
			const tpl = this.querySelector(':scope > template[shadowrootmode]');
			if (tpl) return this._hydrateFromTemplate(tpl);
			return false;
		}

		_hydrateFromTemplate(tpl) {
			const mode = tpl.getAttribute('shadowrootmode') || 'open';
			const root = this.shadowRoot || this.attachShadow({ mode });
			root.appendChild(tpl.content);
			tpl.remove();
			return true;
		}

		_scheduleLazyLoad() {
			if (!('IntersectionObserver' in window)) {
				this._fetchAndRender();
				return;
			}
			this._observer = new IntersectionObserver((entries) => {
				if (entries.some((e) => e.isIntersecting)) {
					this._observer.disconnect();
					this._observer = null;
					this._fetchAndRender();
				}
			}, { rootMargin: '200px' });
			this._observer.observe(this);
		}

		_maybeReload() {
			if (!this.isConnected) return;
			if (this._oldText !== undefined && this._newText !== undefined) {
				this._fetchAndRender();
			}
		}

		async _fetchAndRender() {
			const oldText = this._readTextInput('_oldText', 'old', 'data-old-text', 'data-old-null');
			const newText = this._readTextInput('_newText', 'new', 'data-new-text', 'data-new-null');
			const contextLinesAttr = this.getAttribute('data-context-lines');
			const url = this.getAttribute('data-action-url') || DEFAULT_ACTION_URL;
			const fetchImpl = window.knowweTextDiffFetch || window.fetch.bind(window);

			const payload = { oldText, newText };
			if (contextLinesAttr != null) {
				const n = parseInt(contextLinesAttr, 10);
				if (!Number.isNaN(n)) payload.contextLines = n;
			}

			this._setStatus('loading');
			try {
				const res = await fetchImpl(withCsrf(url), {
					method: 'POST',
					headers: { 'Content-Type': 'application/json; charset=UTF-8' },
					body: JSON.stringify(payload),
				});
				if (!res.ok) throw new Error('HTTP ' + res.status);
				const html = await res.text();
				this._writeShadow(html);
				this._wireExpanders();
				this._setStatus('ready');
			}
			catch (err) {
				this._setStatus('error', err && err.message);
			}
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
			return attributed !== null ? attributed : '';
		}

		_writeShadow(html) {
			const root = this.shadowRoot || this.attachShadow({ mode: 'open' });
			root.innerHTML = html;
		}

		_setStatus(status, message) {
			this.setAttribute('data-status', status);
			if (message) this.setAttribute('data-status-message', message);
			else this.removeAttribute('data-status-message');
		}

		_wireExpanders() {
			const root = this.shadowRoot || this;
			root.querySelectorAll('tr.elided button.expand').forEach((btn) => {
				btn.addEventListener('click', () => this._expand(btn));
			});
		}

		_expand(btn) {
			const elidedRow = btn.closest('tr.elided');
			if (!elidedRow) return;
			let row = elidedRow.nextElementSibling;
			while (row && row.classList.contains('hidden')) {
				row.classList.remove('hidden');
				row = row.nextElementSibling;
			}
			elidedRow.remove();
		}
	}

	customElements.define('knowwe-text-diff', KnowweTextDiff);
})();

/**
 * <knowwe-file-change> - a GitHub-inspired file/article change frame.
 *
 * The component owns the file header and operation styling. The default slot is intentionally
 * generic and can contain a <knowwe-text-diff> when the change has content differences.
 *
 * Attributes:
 *   - data-change:    added | deleted | modified | renamed | renamed-modified
 *   - data-old-name:  original file/article name
 *   - data-new-name:  final file/article name
 *   - data-url:       optional URL for the displayed file/article name
 *   - data-old-url:   optional URL for the original name in rename changes
 *   - data-new-url:   optional URL for the final name in rename changes
 *   - data-additions: optional number of added lines
 *   - data-deletions: optional number of removed lines
 *   - data-collapsed: optional boolean attribute; collapses the body when present
 *
 * Events:
 *   - toggle: dispatched (bubbling) when the user collapses or expands the body via the header.
 *             detail.collapsed reflects the new state. Not dispatched for programmatic
 *             attribute changes.
 */
(function () {
	if (customElements.get('knowwe-file-change')) return;

	const STYLESHEET_URL = 'KnowWEExtension/css/KnowWE-Plugin-TextDiff.css';
	const CHEVRON_DOWN_ICON =
		'<svg class="icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false">' +
		'  <path d="M4 6l4 4 4-4"/>' +
		'</svg>';
	const CHANGE_TYPES = {
		added: { label: 'Added' },
		deleted: { label: 'Deleted' },
		modified: { label: 'Modified' },
		renamed: { label: 'Renamed' },
		'renamed-modified': { label: 'Renamed + modified' },
	};

	function resourceUrl(path) {
		const base = window.knowweTextDiffResourceBase;
		return base ? new URL(path, base).toString() : path;
	}

	function normalizeChange(value) {
		if (value === 'create' || value === 'created') return 'added';
		if (value === 'delete' || value === 'removed') return 'deleted';
		if (value === 'rename') return 'renamed';
		if (value === 'rename-modified' || value === 'renamed-with-modifications') return 'renamed-modified';
		return Object.prototype.hasOwnProperty.call(CHANGE_TYPES, value) ? value : 'modified';
	}

	function positiveInteger(value) {
		const parsed = Number.parseInt(value, 10);
		return Number.isFinite(parsed) && parsed > 0 ? parsed : 0;
	}

	function hasVisibleNode(node) {
		return node.nodeType === Node.ELEMENT_NODE ||
			(node.nodeType === Node.TEXT_NODE && node.textContent.trim().length > 0);
	}

	// noinspection HtmlUnknownAttribute
	class KnowweFileChange extends HTMLElement {
		static get observedAttributes() {
			return [
				'data-change',
				'data-old-name',
				'data-new-name',
				'data-url',
				'data-old-url',
				'data-new-url',
				'data-additions',
				'data-deletions',
				'data-collapsed',
			];
		}

		connectedCallback() {
			if (!this.hasAttribute('role')) this.setAttribute('role', 'group');
			this._ensureShadow();
			this._update();
			this._updateSlotState();
		}

		attributeChangedCallback() {
			if (this.isConnected) this._update();
		}

		_ensureShadow() {
			if (this.shadowRoot) return;
			const root = this.attachShadow({ mode: 'open' });
			root.innerHTML =
				'<link rel="stylesheet" href="' + resourceUrl(STYLESHEET_URL) + '">' +
				'<article class="file-change" part="frame">' +
				'  <header class="file-change-header" part="header">' +
				'    <button class="file-change-toggle" part="toggle" type="button" aria-label="Collapse file change" aria-expanded="true">' +
				'      ' + CHEVRON_DOWN_ICON +
				'    </button>' +
				'    <span class="file-change-title" part="title">' +
				'      <a class="file-change-name old" part="old-name"></a>' +
				'      <span class="file-change-arrow" part="rename-arrow" aria-hidden="true">&rarr;</span>' +
				'      <a class="file-change-name new" part="new-name"></a>' +
				'    </span>' +
				'    <span class="file-change-stats" part="stats">' +
				'      <span class="file-change-stat additions" part="additions"></span>' +
				'      <span class="file-change-stat deletions" part="deletions"></span>' +
				'    </span>' +
				'    <span class="file-change-badge" part="badge"></span>' +
				'  </header>' +
				'  <div class="file-change-body" part="body"><slot></slot></div>' +
				'</article>';
			root.querySelector('.file-change-header').addEventListener('click', (event) => this._onHeaderClick(event));
			root.querySelector('slot').addEventListener('slotchange', () => this._updateSlotState());
		}

		_update() {
			const change = normalizeChange(this.getAttribute('data-change') || 'modified');
			const metadata = CHANGE_TYPES[change];
			const oldName = this.getAttribute('data-old-name') || '';
			const newName = this.getAttribute('data-new-name') || '';
			const url = this.getAttribute('data-url') || '';
			const oldUrl = this.getAttribute('data-old-url') || '';
			const newUrl = this.getAttribute('data-new-url') || '';
			const additions = positiveInteger(this.getAttribute('data-additions'));
			const deletions = positiveInteger(this.getAttribute('data-deletions'));
			const isRename = change === 'renamed' || change === 'renamed-modified';
			const displayName = newName || oldName || 'Unnamed file';
			const collapsed = this.hasAttribute('data-collapsed');

			this.setAttribute('data-normalized-change', change);
			this.setAttribute('aria-label', this._ariaLabel(change, oldName, newName));

			const root = this.shadowRoot;
			if (!root) return;
			const frame = root.querySelector('.file-change');
			const badge = root.querySelector('.file-change-badge');
			const title = root.querySelector('.file-change-title');
			const oldNameEl = root.querySelector('.file-change-name.old');
			const newNameEl = root.querySelector('.file-change-name.new');
			const additionsEl = root.querySelector('.file-change-stat.additions');
			const deletionsEl = root.querySelector('.file-change-stat.deletions');
			const toggle = root.querySelector('.file-change-toggle');

			frame.setAttribute('data-change', change);
			title.classList.toggle('is-rename', isRename);
			badge.textContent = metadata.label;
			this._setNameLink(oldNameEl, isRename ? oldName || 'Unnamed file' : displayName, isRename ? oldUrl : url || newUrl || oldUrl);
			this._setNameLink(newNameEl, isRename ? newName || 'Unnamed file' : '', newUrl || url);
			additionsEl.textContent = additions > 0 ? '+' + additions : '';
			deletionsEl.textContent = deletions > 0 ? '-' + deletions : '';
			additionsEl.hidden = additions === 0;
			deletionsEl.hidden = deletions === 0;
			frame.classList.toggle('is-collapsed', collapsed);
			toggle.setAttribute('aria-expanded', String(!collapsed));
			toggle.setAttribute('aria-label', collapsed ? 'Expand file change' : 'Collapse file change');
		}

		_updateSlotState() {
			const slot = this.shadowRoot && this.shadowRoot.querySelector('slot');
			const hasBody = slot && slot.assignedNodes({ flatten: true }).some(hasVisibleNode);
			this.toggleAttribute('data-empty-body', !hasBody);
			const toggle = this.shadowRoot && this.shadowRoot.querySelector('.file-change-toggle');
			if (toggle) toggle.hidden = !hasBody;
		}

		_toggleCollapsed() {
			if (this.hasAttribute('data-empty-body')) return;
			this.toggleAttribute('data-collapsed');
			this.dispatchEvent(new CustomEvent('toggle', {
				bubbles: true,
				detail: { collapsed: this.hasAttribute('data-collapsed') },
			}));
		}

		_onHeaderClick(event) {
			if (this.hasAttribute('data-empty-body')) return;
			if (event.target.closest('a')) return;
			this._toggleCollapsed();
		}

		_setNameLink(element, text, href) {
			element.textContent = text;
			element.title = text;
			if (href) element.setAttribute('href', href);
			else element.removeAttribute('href');
		}

		_ariaLabel(change, oldName, newName) {
			if (change === 'renamed' || change === 'renamed-modified') {
				return CHANGE_TYPES[change].label + ': ' + (oldName || 'Unnamed file') + ' to ' + (newName || 'Unnamed file');
			}
			return CHANGE_TYPES[change].label + ': ' + (newName || oldName || 'Unnamed file');
		}
	}

	customElements.define('knowwe-file-change', KnowweFileChange);
})();
