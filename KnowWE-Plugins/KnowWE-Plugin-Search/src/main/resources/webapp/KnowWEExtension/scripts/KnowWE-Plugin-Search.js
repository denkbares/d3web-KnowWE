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
 * The section search page.
 *
 * First iteration on purpose: breadcrumb plus highlighted text snippet, so the ranking can be judged against a real
 * wiki. The rendered section preview follows and replaces the snippet on this page; the snippet stays for the quick
 * search dropdown, where a preview would not fit.
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

	function item(hit) {
		var li = document.createElement('li');
		li.className = 'knowwe-search-hit';

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
		li.appendChild(link);

		var snippet = document.createElement('div');
		snippet.className = 'knowwe-search-snippet';
		// safe: the server escapes the body and only adds its own <mark> tags around the matches
		snippet.innerHTML = hit.snippet || '';
		li.appendChild(snippet);
		return li;
	}

	function parameter(name) {
		var match = new RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
		return match ? decodeURIComponent(match[1].replace(/\+/g, ' ')) : '';
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	}
	else {
		init();
	}
})();
