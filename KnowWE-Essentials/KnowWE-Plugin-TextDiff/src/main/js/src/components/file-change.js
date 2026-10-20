/**
 * <knowwe-file-change> - a GitHub-inspired file/article change frame.
 *
 * The component owns the file header and operation styling. The default slot is intentionally
 * generic and can contain a <knowwe-text-diff> when the change has content differences.
 *
 * Attributes:
 *   - data-change:    added | deleted | modified | renamed | renamed-modified | added-deleted
 *   - data-old-name:  original file/article name
 *   - data-new-name:  final file/article name
 *   - data-url:       optional URL for the displayed file/article name
 *   - data-old-url:   optional URL for the original name in rename changes
 *   - data-new-url:   optional URL for the final name in rename changes
 *   - data-additions: optional number of added lines
 *   - data-deletions: optional number of removed lines
 *   - data-collapsed: optional boolean attribute; collapses the body when present
 *   - data-controls:  the header buttons a diff in the body may get, "layout whitespace" by default, an empty
 *                     value shows none
 *   - data-theme:     optional explicit 'light' | 'dark' theme override
 *
 * Slots:
 *   - (default):      the body, typically a <knowwe-text-diff>
 *   - actions:        extra controls shown in the header between the stats and the badge, for
 *                     example links to related views. Clicks on links inside the header never
 *                     toggle the body.
 *
 * Events:
 *   - toggle: dispatched (bubbling) when the user collapses or expands the body via the header.
 *             detail.collapsed reflects the new state. Not dispatched for programmatic
 *             attribute changes.
 *
 * The stats show the given additions and deletions until a <knowwe-text-diff> in the body reports the counts of
 * the diff it computed, which replace them. A diff with a context gets two buttons in the header, one that switches
 * between one column and two and one that hides whitespace changes, unless data-controls leaves them out or the
 * diff has a fixed layout.
 */
const STYLESHEET_URL = 'KnowWEExtension/css/KnowWE-Plugin-TextDiff.css';
const CHEVRON_DOWN_ICON =
	'<svg class="icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false">' +
	'  <path d="M4 6l4 4 4-4"/>' +
	'</svg>';
// the layout each button click switches to, shown as its icon
const LAYOUT_SWITCH = {
	unified: {
		next: 'split',
		label: 'Show side by side',
		icon: '<svg class="icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false">' +
			'<rect x="1.5" y="2.5" width="13" height="11" rx="1.5"/><path d="M8 2.5v11"/></svg>',
	},
	split: {
		next: 'unified',
		label: 'Show in one column',
		icon: '<svg class="icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false">' +
			'<rect x="1.5" y="2.5" width="13" height="11" rx="1.5"/><path d="M4.5 6h7M4.5 8.5h7M4.5 11h4"/></svg>',
	},
};
const WHITESPACE_ICON =
	'<svg class="icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false">' +
	'<path d="M13 2H6.5a3 3 0 0 0 0 6H9M9 2v12M12 2v12"/></svg>';
const LAYOUTS_ALL = ['unified', 'split'];
const CHANGE_TYPES = {
	added: { label: 'Added' },
	deleted: { label: 'Deleted' },
	modified: { label: 'Modified' },
	renamed: { label: 'Renamed' },
	'renamed-modified': { label: 'Renamed + modified' },
	'added-deleted': { label: 'Added + deleted' },
};

function displayMode() {
	return typeof KNOWWE !== 'undefined' && KNOWWE.helper.getDisplayMode ? KNOWWE.helper.getDisplayMode() : 'light';
}

function resourceUrl(path) {
	const base = window.knowweTextDiffResourceBase;
	return base ? new URL(path, base).toString() : path;
}

function normalizeChange(value) {
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
	constructor() {
		super();
		this.addEventListener('knowwe-text-diff-stats', (event) => {
			if (event.target.parentElement !== this) return;
			this.setAttribute('data-additions', String(event.detail.added));
			this.setAttribute('data-deletions', String(event.detail.removed));
		});
		this.addEventListener('knowwe-text-diff-layout', (event) => {
			if (event.target.parentElement === this) this._updateLayoutButton(event.detail.effective, event.detail.layouts);
		});
		this.addEventListener('knowwe-text-diff-whitespace', (event) => {
			if (event.target.parentElement === this) this._updateWhitespaceButton(event.detail.ignore);
		});
	}

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
			'data-controls',
		];
	}

	connectedCallback() {
		if (!this.hasAttribute('data-theme')) this.setAttribute('data-theme', displayMode());
		if (!this.hasAttribute('role')) this.setAttribute('role', 'group');
		this._ensureShadow();
		this._update();
		this._updateSlotState();
	}

	attributeChangedCallback(name) {
		if (!this.isConnected) return;
		if (name === 'data-controls') this._updateSlotState();
		else this._update();
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
			'    <span class="file-change-actions" part="actions"><slot name="actions"></slot></span>' +
			'    <button class="file-change-button file-change-whitespace" part="whitespace-button" type="button" hidden' +
			' aria-pressed="false">' + WHITESPACE_ICON + '</button>' +
			'    <button class="file-change-button file-change-layout" part="layout-button" type="button" hidden></button>' +
			'    <span class="file-change-badge" part="badge"></span>' +
			'  </header>' +
			'  <div class="file-change-body" part="body"><slot></slot></div>' +
			'</article>';
		root.querySelector('.file-change-header').addEventListener('click', (event) => this._onHeaderClick(event));
		root.querySelector('.file-change-layout').addEventListener('click', () => this._switchLayout());
		root.querySelector('.file-change-whitespace').addEventListener('click', () => this._switchWhitespace());
		root.querySelector('slot:not([name])').addEventListener('slotchange', () => this._updateSlotState());
	}

	_textDiff() {
		return this.querySelector(':scope > knowwe-text-diff');
	}

	_controls() {
		const value = this.getAttribute('data-controls');
		return value === null ? ['layout', 'whitespace'] : value.split(/\s+/);
	}

	// a choice is only offered for a diff that remembers it, which needs a context
	_choosing() {
		const diff = this._textDiff();
		return diff && 'effectiveLayout' in diff && diff.context !== null;
	}

	// the button shows the layout a click switches to, and only for a diff that has layouts to choose from
	_updateLayoutButton(effective, layouts = LAYOUTS_ALL) {
		const button = this.shadowRoot && this.shadowRoot.querySelector('.file-change-layout');
		if (!button) return;
		const current = LAYOUT_SWITCH[effective];
		button.hidden = !current || layouts.length < 2 || !this._choosing() || !this._controls().includes('layout');
		if (!current) return;
		button.innerHTML = LAYOUT_SWITCH[current.next].icon;
		button.title = current.label;
		button.setAttribute('aria-label', current.label);
		button.setAttribute('data-layout', effective);
	}

	_switchLayout() {
		const diff = this._textDiff();
		const current = diff && LAYOUT_SWITCH[diff.effectiveLayout];
		if (current) diff.layout = current.next;
	}

	// the button is pressed while whitespace changes are hidden, and absent for a diff without the mode
	_updateWhitespaceButton(ignore) {
		const button = this.shadowRoot && this.shadowRoot.querySelector('.file-change-whitespace');
		if (!button) return;
		button.hidden = typeof ignore !== 'boolean' || !this._choosing() || !this._controls().includes('whitespace');
		if (button.hidden) return;
		button.setAttribute('aria-pressed', String(ignore));
		button.title = ignore ? 'Show whitespace changes' : 'Hide whitespace changes';
		button.setAttribute('aria-label', button.title);
	}

	_switchWhitespace() {
		const diff = this._textDiff();
		if (diff && typeof diff.ignoreWhitespace === 'boolean') diff.ignoreWhitespace = !diff.ignoreWhitespace;
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
		const slot = this.shadowRoot && this.shadowRoot.querySelector('slot:not([name])');
		const hasBody = slot && slot.assignedNodes({ flatten: true }).some(hasVisibleNode);
		this.toggleAttribute('data-empty-body', !hasBody);
		const toggle = this.shadowRoot && this.shadowRoot.querySelector('.file-change-toggle');
		if (toggle) toggle.hidden = !hasBody;
		// a diff that was upgraded before this frame has announced its layout already
		const diff = this._textDiff();
		const upgraded = diff && 'effectiveLayout' in diff;
		this._updateLayoutButton(upgraded ? diff.effectiveLayout : null, upgraded ? diff.layouts : undefined);
		this._updateWhitespaceButton(upgraded ? diff.ignoreWhitespace : null);
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
		if (event.target.closest('a, .file-change-button')) return;
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

if (!customElements.get('knowwe-file-change')) customElements.define('knowwe-file-change', KnowweFileChange);
