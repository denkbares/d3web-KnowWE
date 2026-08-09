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
 * The section search: the full page, and the quick search in the header.
 *
 * Both talk to the same endpoint, which is the point of doing it this way -- the dropdown is not a second, weaker
 * search but the same ranking with a smaller window. The page shows breadcrumb plus highlighted snippet for now; the
 * rendered section preview replaces the snippet there later, while the dropdown keeps the snippet because a preview
 * would not fit into it.
 */
(function () {
	'use strict';

	var PAGE_SIZE = 10;
	var DEBOUNCE_MS = 300;

	var state = { query: '', offset: 0, hits: [], total: 0, pending: null };
	var nodes = {};

	function init() {
		var mount = document.getElementById('knowwe-search');
		if (!mount) return;

		mount.innerHTML =
			'<div class="knowwe-search-bar">' +
			'  <input type="search" id="knowwe-search-input" autocomplete="off" spellcheck="false"' +
			'         placeholder="' + mount.getAttribute('data-placeholder') + '" />' +
			'</div>' +
			'<div class="knowwe-search-status" id="knowwe-search-status"></div>' +
			'<ol class="knowwe-search-results" id="knowwe-search-results"></ol>' +
			'<div class="knowwe-search-more" id="knowwe-search-more"></div>';

		nodes.input = document.getElementById('knowwe-search-input');
		nodes.status = document.getElementById('knowwe-search-status');
		nodes.results = document.getElementById('knowwe-search-results');
		nodes.more = document.getElementById('knowwe-search-more');

		nodes.input.addEventListener('input', function () {
			schedule(nodes.input.value);
		});
		nodes.input.addEventListener('keydown', function (event) {
			if (event.key === 'Escape') {
				nodes.input.value = '';
				schedule('');
			}
		});

		var initial = parameter('query');
		if (initial) {
			nodes.input.value = initial;
			run(initial, 0, false);
		}
		nodes.input.focus();
	}

	function schedule(query) {
		window.clearTimeout(state.pending);
		state.pending = window.setTimeout(function () {
			run(query, 0, false);
			// keep the query in the address bar so a search can be shared or reloaded
			var url = window.location.pathname + (query ? '?query=' + encodeURIComponent(query) : '');
			window.history.replaceState(null, '', url);
		}, DEBOUNCE_MS);
	}

	function run(query, offset, append) {
		state.query = query;
		state.offset = offset;
		if (!query.trim()) {
			render({ hits: [], total: 0 }, false);
			return;
		}
		nodes.status.textContent = 'Suche …';

		jq$.ajax({
			url: 'action/WikiSearchAction',
			data: { query: query, offset: offset, limit: PAGE_SIZE },
			dataType: 'json',
			cache: false
		}).done(function (answer) {
			if (state.query !== query) return; // a newer query already won
			render(answer, append);
		}).fail(function () {
			nodes.status.textContent = 'Die Suche ist gerade nicht erreichbar.';
		});
	}

	function render(answer, append) {
		if (!append) nodes.results.innerHTML = '';
		nodes.more.innerHTML = '';

		var hits = answer.hits || [];
		if (append) state.hits = state.hits.concat(hits); else state.hits = hits;

		nodes.status.textContent = describe(answer);

		hits.forEach(function (hit) {
			nodes.results.appendChild(item(hit));
		});
		// only now are the previews laid out and can be measured
		nodes.results.querySelectorAll('.knowwe-search-preview').forEach(revealFirstMatch);

		if (state.hits.length < (answer.total || 0)) {
			var button = document.createElement('button');
			button.type = 'button';
			button.textContent = 'Mehr laden';
			button.addEventListener('click', function () {
				run(state.query, state.offset + PAGE_SIZE, true);
			});
			nodes.more.appendChild(button);
		}
	}

	function describe(answer) {
		if (answer.error) return answer.error;
		if (answer.indexing) return 'Der Suchindex wird gerade aufgebaut, die Trefferliste ist noch unvollständig.';
		if (!state.query.trim()) return '';
		if (!answer.total) {
			if (answer.unmatched && answer.unmatched.length) {
				return 'Keine Treffer. Nicht gefunden: ' + answer.unmatched.join(', ');
			}
			return 'Keine Treffer.';
		}
		var text = answer.total + (answer.exact ? '' : '+') + ' Treffer · ' + answer.tookMs + ' ms';
		if (answer.relaxed) text += ' · nicht alle Wörter kommen vor, es wird nach einzelnen gesucht';
		return text;
	}

	/*
	 * A document mark in front of every hit. It gives the list a fixed left edge to indent against, so where one
	 * result ends and the next begins is visible at a glance instead of having to be read.
	 *
	 * The markup is a constant, so assigning it is safe -- nothing from the query or the wiki goes in here.
	 */
	function resultIcon() {
		var icon = document.createElement('span');
		icon.className = 'knowwe-search-icon';
		icon.setAttribute('aria-hidden', 'true');
		icon.innerHTML = '<svg viewBox="0 0 24 24" width="18" height="18">'
			+ '<path fill="currentColor" d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h5.2a6 6 0 0 1-1-2H6V4h7v5h5v1.3'
			+ 'a6 6 0 0 1 2 1.5V8l-6-6z"/>'
			+ '<path fill="currentColor" d="M16.5 12a4.5 4.5 0 1 0 2.6 8.2l2.1 2.1 1.4-1.4-2.1-2.1A4.5 4.5 0 0 0 '
			+ '16.5 12zm0 2a2.5 2.5 0 1 1 0 5 2.5 2.5 0 0 1 0-5z"/>'
			+ '</svg>';
		return icon;
	}

	function item(hit) {
		var li = document.createElement('li');
		li.className = 'knowwe-search-hit';
		li.appendChild(resultIcon());

		var body = document.createElement('div');
		body.className = 'knowwe-search-body';
		li.appendChild(body);

		var link = document.createElement('a');
		link.className = 'knowwe-search-breadcrumb';
		link.href = hit.url;
		(hit.breadcrumb || hit.title).split(' › ').forEach(function (part, index) {
			if (index > 0) {
				var separator = document.createElement('span');
				separator.className = 'knowwe-search-separator';
				separator.textContent = '›';
				link.appendChild(separator);
			}
			var span = document.createElement('span');
			span.textContent = part;
			link.appendChild(span);
		});
		markMatches(link, state.query);
		body.appendChild(link);

		if (hit.stale) {
			var warning = document.createElement('div');
			warning.className = 'knowwe-search-stale';
			warning.textContent = 'Die Seite wurde seit der Indizierung geändert.';
			body.appendChild(warning);
		}

		if (hit.previewHtml) {
			// the wiki's own rendering of the section: real tables, real markup boxes
			var preview = document.createElement('div');
			preview.className = 'knowwe-search-preview';
			preview.innerHTML = hit.previewHtml;
			dropDuplicateHeading(preview, hit.breadcrumb);
			body.appendChild(preview);
			markMatches(preview, state.query);
		}
		else {
			var snippet = document.createElement('div');
			snippet.className = 'knowwe-search-snippet';
			// safe: the server escapes the body and only adds its own <mark> tags around the matches
			snippet.innerHTML = hit.snippet || '';
			body.appendChild(snippet);
		}
		return li;
	}

	/**
	 * Removes the preview's own heading when the breadcrumb above it already says the same.
	 *
	 * The preview renderer includes the heading, which is right on the object info page but here it repeats the last
	 * breadcrumb segment -- and in the dropdown that costs one of seven lines. Only an exact match is dropped, so a
	 * heading that says something else stays.
	 */
	function dropDuplicateHeading(container, breadcrumb) {
		var heading = container.querySelector('h1, h2, h3, h4, h5, h6');
		if (!heading) return;
		var parts = (breadcrumb || '').split(' › ');
		var last = parts[parts.length - 1].trim().toLowerCase();
		if (!last || headingText(heading) !== last) return;
		heading.remove();
	}

	/**
	 * The heading's text without the anchor link the wiki appends to it -- its content is a "#", so comparing the
	 * raw textContent never matches and the heading would always survive.
	 */
	function headingText(heading) {
		var copy = heading.cloneNode(true);
		copy.querySelectorAll('a').forEach(function (anchor) {
			anchor.remove();
		});
		return copy.textContent.replace(/[#\s]+$/, '').trim().toLowerCase();
	}

	/**
	 * Brings the first match into view inside a capped preview.
	 *
	 * A preview cut at a fixed height shows the beginning of the section, which is not necessarily where the match is
	 * -- a result would then show everything except the reason it is a result. Scrolling to the first mark fixes that;
	 * the class tells the styling to fade at the top as well, so it stays visible that something is above.
	 */
	function revealFirstMatch(container) {
		var margin = 12;
		// bounding rects, not offsetTop: the offset parent is not necessarily the container
		var box = container.getBoundingClientRect();
		var target = null;
		var delta = 0;

		// not simply the first mark: a match can sit in a tooltip or another element outside the flow, which then
		// measures far above the container and scrolling to it does nothing
		var marks = container.querySelectorAll('mark');
		for (var i = 0; i < marks.length; i++) {
			var offset = marks[i].getBoundingClientRect().top - box.top + container.scrollTop;
			if (offset >= 0 && offset <= container.scrollHeight) {
				target = marks[i];
				delta = offset - container.scrollTop;
				break;
			}
		}
		if (!target) return;
		if (delta >= 0 && delta + target.getBoundingClientRect().height <= container.clientHeight - margin) return;
		container.scrollTop += delta - margin;
		if (container.scrollTop > 0) container.classList.add('is-scrolled');
	}

	/**
	 * Highlights the query words inside a rendered preview. Done here rather than on the server because wrapping
	 * matches in already rendered HTML would cut through tags; walking text nodes cannot.
	 */
	function markMatches(container, query) {
		var words = (query || state.query).toLowerCase().split(/[^\p{L}\p{N}]+/u).filter(function (word) {
			return word.length > 2;
		});
		if (!words.length) return;

		// Not inside form controls and the like: a mark in an <option> is never visible, and its position cannot be
		// measured, so scrolling to it would fail silently.
		var skip = /^(OPTION|SELECT|TEXTAREA|SCRIPT|STYLE|INPUT)$/;
		var walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT, {
			acceptNode: function (node) {
				for (var p = node.parentElement; p && p !== container; p = p.parentElement) {
					if (skip.test(p.tagName)) return NodeFilter.FILTER_REJECT;
				}
				return NodeFilter.FILTER_ACCEPT;
			}
		});
		var nodes = [];
		while (walker.nextNode()) nodes.push(walker.currentNode);

		nodes.forEach(function (node) {
			var text = node.nodeValue;
			var lower = text.toLowerCase();
			var hitAt = -1, hitWord = null;
			words.forEach(function (word) {
				var at = lower.indexOf(word);
				if (at >= 0 && (hitAt < 0 || at < hitAt)) { hitAt = at; hitWord = word; }
			});
			if (hitAt < 0) return;
			var mark = document.createElement('mark');
			mark.textContent = text.substr(hitAt, hitWord.length);
			var after = node.splitText(hitAt);
			after.nodeValue = after.nodeValue.substring(hitWord.length);
			node.parentNode.insertBefore(mark, after);
		});
	}

	function parameter(name) {
		var match = new RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
		return match ? decodeURIComponent(match[1].replace(/\+/g, ' ')) : '';
	}

	/* ---------------------------------------------------------------- quick search in the header */

	// the whole panel scrolls, footer included, so it can hold more than fits on screen. Each preview still costs
	// a rendering pass on the server, which is what limits this rather than the space.
	var QUICK_LIMIT = 20;
	var QUICK_DEBOUNCE_MS = 200;

	var quick = { input: null, panel: null, pending: null, hits: [], active: -1, query: '' };

	function initQuickSearch() {
		quick.input = document.querySelector('.knowwe-quicksearch-input');
		quick.panel = document.getElementById('knowwe-quicksearch-panel');
		if (!quick.input || !quick.panel) return;

		quick.input.addEventListener('input', function () {
			window.clearTimeout(quick.pending);
			quick.pending = window.setTimeout(askQuick, QUICK_DEBOUNCE_MS);
		});
		quick.input.addEventListener('keydown', onQuickKey);
		quick.input.addEventListener('focus', function () {
			if (quick.hits.length) showQuick();
		});
		document.addEventListener('click', function (event) {
			if (!quick.panel.contains(event.target) && event.target !== quick.input) hideQuick();
		});
	}

	function askQuick() {
		var query = quick.input.value;
		quick.query = query;
		if (query.trim().length < 2) {
			hideQuick();
			return;
		}
		jq$.ajax({
			url: 'action/WikiSearchAction',
			data: { query: query, limit: QUICK_LIMIT, partial: true, preview: true },
			dataType: 'json',
			cache: false
		}).done(function (answer) {
			if (quick.query !== query) return; // a newer keystroke already won
			renderQuick(answer);
		}).fail(hideQuick);
	}

	function renderQuick(answer) {
		quick.hits = answer.hits || [];
		quick.active = -1;
		quick.panel.innerHTML = '';
		var list = document.createElement('div');
		list.className = 'knowwe-quicksearch-list';

		if (!quick.hits.length) {
			var empty = document.createElement('div');
			empty.className = 'knowwe-quicksearch-empty';
			empty.textContent = answer.indexing ? 'Index wird aufgebaut …' : 'Keine Treffer';
			quick.panel.appendChild(empty);
			showQuick();
			return;
		}

		quick.hits.forEach(function (hit, position) {
			var entry = document.createElement('a');
			entry.className = 'knowwe-quicksearch-hit';
			entry.href = hit.url;
			entry.addEventListener('mouseenter', function () {
				highlightQuick(position);
			});
			entry.appendChild(resultIcon());

			var qbody = document.createElement('div');
			qbody.className = 'knowwe-search-body';
			entry.appendChild(qbody);

			var crumb = document.createElement('div');
			crumb.className = 'knowwe-quicksearch-crumb';
			crumb.textContent = hit.breadcrumb || hit.title;
			markMatches(crumb, quick.query);
			qbody.appendChild(crumb);

			if (hit.previewHtml) {
				var preview = document.createElement('div');
				preview.className = 'knowwe-quicksearch-preview';
				preview.innerHTML = hit.previewHtml;
				dropDuplicateHeading(preview, hit.breadcrumb);
				qbody.appendChild(preview);
				markMatches(preview, quick.query);
			}
			else {
				var snippet = document.createElement('div');
				snippet.className = 'knowwe-quicksearch-snippet';
				// safe: the server escapes the body and only adds its own <mark> tags around the matches
				snippet.innerHTML = hit.snippet || '';
				qbody.appendChild(snippet);
			}

			list.appendChild(entry);
		});
		quick.panel.appendChild(list);

		var all = document.createElement('a');
		all.className = 'knowwe-quicksearch-all';
		all.href = 'Search.jsp?query=' + encodeURIComponent(quick.query);
		// "auf Suchseite", because below the limit the dropdown already shows them all and "alle anzeigen"
		// would promise nothing new
		all.textContent = 'Alle ' + answer.total + (answer.exact ? '' : '+') + ' Treffer auf Suchseite anzeigen';
		quick.panel.appendChild(all);

		showQuick();
		quick.panel.querySelectorAll('.knowwe-quicksearch-preview').forEach(revealFirstMatch);
	}

	function onQuickKey(event) {
		if (event.key === 'Escape') {
			hideQuick();
			return;
		}
		if (!quick.hits.length || quick.panel.hidden) return;

		if (event.key === 'ArrowDown') {
			event.preventDefault();
			highlightQuick(quick.active + 1 >= quick.hits.length ? 0 : quick.active + 1);
		}
		else if (event.key === 'ArrowUp') {
			event.preventDefault();
			highlightQuick(quick.active <= 0 ? quick.hits.length - 1 : quick.active - 1);
		}
		else if (event.key === 'Enter' && quick.active >= 0) {
			// without a selection Enter submits the form and lands on the full search page
			event.preventDefault();
			window.location.href = quick.hits[quick.active].url;
		}
	}

	function highlightQuick(position) {
		var entries = quick.panel.querySelectorAll('.knowwe-quicksearch-hit');
		if (quick.active >= 0 && entries[quick.active]) entries[quick.active].classList.remove('active');
		quick.active = position;
		if (entries[position]) {
			entries[position].classList.add('active');
			entries[position].scrollIntoView({ block: 'nearest' });
		}
	}

	function showQuick() {
		quick.panel.hidden = false;
	}

	function hideQuick() {
		quick.panel.hidden = true;
		quick.active = -1;
	}

	function start() {
		init();
		initQuickSearch();
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', start);
	}
	else {
		start();
	}
})();
