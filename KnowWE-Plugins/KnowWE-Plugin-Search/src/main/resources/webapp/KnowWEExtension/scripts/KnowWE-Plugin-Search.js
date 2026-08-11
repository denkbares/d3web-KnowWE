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
	// Only from here on do we say that a search is running: at a 20 ms answer an indicator is a mere flicker.
	var BUSY_AFTER_MS = 300;

	/*
	 * The filters of the search page. The quick search sends none of them and gets everything, only sorted.
	 *
	 * "All Variants" is the only one that adds something: off means only what the current default compiler compiles
	 * -- the same thing the wiki greys out on foreign markup. The other two narrow the search down.
	 */
	var FILTERS = [
		{
			name: 'attachmentsOnly', label: 'Attachments',
			hint: 'Search the attachments instead of the pages themselves.'
		},
		{
			name: 'otherVariants', label: 'All Variants', on: true, onlyWithVariants: true,
			hint: 'Search every variant. Off: only what the current variant contains – the wiki greys the others '
				+ 'out anyway.'
		},
		{
			name: 'titleOnly', label: 'Page Name only',
			hint: 'Search the page names only, not the page content.'
		}
	];
	var DEBOUNCE_MS = 300;

	var state = { query: '', offset: 0, hits: [], total: 0, pending: null, loading: false, watcher: null };
	var nodes = {};

	function init() {
		var mount = document.getElementById('knowwe-search');
		if (!mount) return;

		mount.innerHTML =
			'<div class="knowwe-search-bar">' +
			'  <input type="search" id="knowwe-search-input" autocomplete="off" spellcheck="false"' +
			'         placeholder="' + mount.getAttribute('data-placeholder') + '" />' +
			'  <span class="knowwe-search-filters" id="knowwe-search-filters"></span>' +
			'</div>' +
			'<div class="knowwe-search-status" id="knowwe-search-status"></div>' +
			'<ol class="knowwe-search-results" id="knowwe-search-results"></ol>' +
			'<div class="knowwe-search-more" id="knowwe-search-more"></div>';

		nodes.input = document.getElementById('knowwe-search-input');
		nodes.root = mount;
		nodes.filters = document.getElementById('knowwe-search-filters');
		buildFilters();
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

	/*
	 * The way out when the search found no page of that name: create it.
	 *
	 * The old search had this and it is the more useful answer to a query that names something not there yet. Cloning
	 * takes the page the reader is on as the template, which is what "[{clone}]" means to the wiki's own editor -- see
	 * the wikiCloneUrl meta tag in commonheader.jsp.
	 */
	function createPageOffer(name) {
		var current = document.querySelector('meta[name="wikiPageName"]');
		var offer = document.createElement('div');
		offer.className = 'knowwe-search-create';

		var link = document.createElement('a');
		link.className = 'knowwe-search-create-link';
		link.href = 'Edit.jsp?page=' + encodeURIComponent(name);
		link.textContent = "Create page '" + name + "'";
		offer.appendChild(link);

		if (current && current.content) {
			var label = document.createElement('label');
			label.className = 'knowwe-search-create-clone tooltipster';
			label.title = "Start from the text of '" + current.content + "' instead of an empty page.";

			var box = document.createElement('input');
			box.type = 'checkbox';
			box.addEventListener('change', function () {
				link.href = 'Edit.jsp?page=' + encodeURIComponent(name)
						+ (box.checked ? '&clone=' + encodeURIComponent(current.content) : '');
			});
			// the label must not carry the click to the link beside it
			label.addEventListener('click', function (event) {
				event.stopPropagation();
			});
			label.appendChild(box);
			label.appendChild(document.createTextNode(' clone this page'));
			offer.appendChild(label);
		}
		if (window.jq$ && jq$.fn.tooltipster) jq$(offer).find('.tooltipster').tooltipster();
		return offer;
	}

	function buildFilters() {
		FILTERS.forEach(function (filter) {
			var label = document.createElement('label');
			label.className = 'knowwe-search-filter tooltipster';
			label.title = filter.hint;

			var box = document.createElement('input');
			box.type = 'checkbox';
			box.checked = !!filter.on;
			box.dataset.filter = filter.name;
			box.addEventListener('change', function () {
				run(state.query, 0, false);
			});
			label.appendChild(box);
			label.appendChild(document.createTextNode(' ' + filter.label));
			nodes.filters.appendChild(label);
		});
		// the explanations come from tooltipster, as everywhere else in the wiki
		if (window.jq$ && jq$.fn.tooltipster) jq$(nodes.filters).find('.tooltipster').tooltipster();

		// Only the server knows whether there are variants at all. Asked once, so that the filter for them does not
		// appear first and disappear again after the first search.
		jq$.ajax({ url: 'action/WikiSearchAction', data: { query: '' }, dataType: 'json', cache: false })
				.done(showApplicableFilters);
	}

	function showApplicableFilters(answer) {
		FILTERS.forEach(function (filter) {
			if (!filter.onlyWithVariants) return;
			var box = nodes.filters.querySelector('input[data-filter="' + filter.name + '"]');
			if (box) box.parentNode.hidden = answer.variants === false;
		});
	}

	function filterValues() {
		var values = {};
		nodes.filters.querySelectorAll('input[type="checkbox"]').forEach(function (box) {
			// a hidden filter must not narrow anything down, or something filters that nobody can see
			values[box.dataset.filter] = box.parentNode.hidden ? !!boxDefault(box) : box.checked;
		});
		return values;
	}

	function boxDefault(box) {
		for (var i = 0; i < FILTERS.length; i++) {
			if (FILTERS[i].name === box.dataset.filter) return FILTERS[i].on;
		}
		return false;
	}

	function run(query, offset, append) {
		state.query = query;
		state.offset = offset;
		if (!append && state.watcher) state.watcher.disconnect();
		if (!query.trim()) {
			render({ hits: [], total: 0 }, false);
			return;
		}
		nodes.status.textContent = 'Searching …';
		var busy = window.setTimeout(function () {
			nodes.root.classList.add('is-busy');
		}, BUSY_AFTER_MS);

		jq$.ajax({
			url: 'action/WikiSearchAction',
			data: Object.assign({ query: query, offset: offset, limit: PAGE_SIZE }, filterValues()),
			dataType: 'json',
			cache: false
		}).done(function (answer) {
			if (state.query !== query) return; // a newer query already won
			render(answer, append);
		}).fail(function () {
			nodes.status.textContent = 'The search is not reachable at the moment.';
		}).always(function () {
			window.clearTimeout(busy);
			nodes.root.classList.remove('is-busy');
			state.loading = false;
		});
	}

	function render(answer, append) {
		if (!append) nodes.results.innerHTML = '';
		nodes.more.innerHTML = '';

		var hits = answer.hits || [];
		if (append) state.hits = state.hits.concat(hits); else state.hits = hits;

		showApplicableFilters(answer);
		nodes.status.textContent = describe(answer);

		// the indicator stays while the attachments are still arriving and the search is filtered to them
		nodes.root.classList.toggle('is-busy',
				!!(answer.attachmentsIndexing && filterValues().attachmentsOnly));

		hits.forEach(function (hit) {
			nodes.results.appendChild(item(hit));
		});
		// only now are the previews laid out and can be measured
		measureSections(nodes.results);

		// above the hits, not below them: whoever searched for a page name that does not exist wants to read this
		// first. It scrolls away with the rest, so it is out of the way once the reader is in the list.
		if (answer.createPage) nodes.results.insertBefore(createPageOffer(answer.createPage), nodes.results.firstChild);
		if (answer.hasMore) watchForMore();
	}

	/*
	 * Loads the next pages while scrolling, as long as the server says there are more.
	 *
	 * The observer watches a marker below the last hit rather than the scroll position: it fires once, before the marker
	 * is actually reached, and it does not care which element scrolls -- the wiki's layout scrolls the page in some
	 * skins and a container in others. A button stays as the way out if a page is loaded and the marker never appears.
	 */
	function watchForMore() {
		var marker = document.createElement('div');
		marker.className = 'knowwe-search-more-marker';
		marker.textContent = 'Loading more results …';
		nodes.more.appendChild(marker);

		if (state.watcher) state.watcher.disconnect();
		if (!window.IntersectionObserver) {
			addMoreButton();
			return;
		}
		state.watcher = new IntersectionObserver(function (entries) {
			if (!entries[0].isIntersecting || state.loading) return;
			state.loading = true;
			state.watcher.disconnect();
			run(state.query, state.offset + PAGE_SIZE, true);
		}, { rootMargin: '400px' });
		state.watcher.observe(marker);
	}

	function addMoreButton() {
		var button = document.createElement('button');
		button.type = 'button';
		button.textContent = 'Load more';
		button.addEventListener('click', function () {
			run(state.query, state.offset + PAGE_SIZE, true);
		});
		nodes.more.innerHTML = '';
		nodes.more.appendChild(button);
	}

	function describe(answer) {
		if (answer.error) return answer.error;
		if (filterValues().attachmentsOnly && answer.attachmentsIndexed === false) {
			return answer.attachmentsIndexing
					? 'The attachments are being indexed – this list is still incomplete.'
					: 'The attachments are not indexed yet, so this filter finds nothing.';
		}
		if (answer.indexing) return 'The search index is being built, so this list is still incomplete.';
		if (!state.query.trim()) return '';
		if (!answer.total) {
			if (answer.unmatched && answer.unmatched.length) {
				return 'No results. Not found: ' + answer.unmatched.join(', ');
			}
			return 'No results.';
		}
		var text = answer.total + (answer.exact ? '' : '+')
				+ (answer.total === 1 ? ' result · ' : ' results · ') + answer.tookMs + ' ms';
		if (answer.relaxed) text += ' · not every word occurs, searching for them separately';
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

	/*
	 * The weaker sections of the same page, one click away. They are fetched rather than delivered with the entry:
	 * each one costs a rendering pass on the server, and most of them are never looked at.
	 */
	function expander(hit, sections) {
		var wrapper = document.createElement('div');
		wrapper.className = 'knowwe-search-folded';

		var button = document.createElement('button');
		button.type = 'button';
		button.className = 'knowwe-search-folded-toggle';
		button.textContent = hit.folded + (hit.folded === 1 ? ' more result' : ' more results') + ' on this page';
		wrapper.appendChild(button);

		var list = document.createElement('div');
		list.className = 'knowwe-search-folded-hits';
		list.hidden = true;
		// below the line, so that it keeps its place and the triangle points down, to where something opens up
		wrapper.appendChild(list);

		button.addEventListener('click', function () {
			if (list.childNodes.length) {
				// already fetched, so this is just the way back
				list.hidden = !list.hidden;
				button.classList.toggle('is-open', !list.hidden);
				return;
			}
			button.disabled = true;
			jq$.ajax({
				url: 'action/WikiSearchAction',
				data: { query: state.query, expand: hit.page || hit.title, limit: PAGE_SIZE },
				dataType: 'json',
				cache: false
			}).done(function (answer) {
				var seen = { path: null };
				(answer.hits || []).forEach(function (folded) {
					list.appendChild(section(folded, seen));
				});
				list.hidden = false;
				button.classList.add('is-open');
				measureSections(list);
			}).always(function () {
				button.disabled = false;
			});
		});
		return wrapper;
	}

	/** A button in the dropdown must not take the focus away from the field the user is typing in. */
	function keepFocus(event) {
		event.preventDefault();
	}

	/*
	 * A preview is capped so one long section cannot push the next hit off the screen. Where that cap actually cuts
	 * something off, a triangle lifts it -- measured after the preview is in the document, because a preview that is
	 * not laid out yet reports no height at all.
	 */
	/*
	 * The first measurement is not the last word: the wiki's own scripts lay out tables inside a preview after it is
	 * in the document, and a preview measured while that is still happening reports a width it will not keep. So the
	 * check is repeated a few times while things settle -- cheap, and it converges.
	 */
	function remeasure(root, selector) {
		[250, 800, 2000].forEach(function (delay) {
			window.setTimeout(function () {
				root.querySelectorAll(selector).forEach(markClipping);
			}, delay);
		});
	}

	function markClipping(preview) {
		if (!preview) return false;
		// a wide table or a long line is cut at the right edge just as a long section is cut at the bottom
		preview.classList.toggle('is-clipped-right', preview.scrollWidth > preview.clientWidth + 2);
		return preview.scrollHeight > preview.clientHeight + 4;
	}

	function addGrowToggle(preview, gutter) {
		var button = document.createElement('button');
		button.type = 'button';
		button.className = 'knowwe-search-grow';
		button.addEventListener('mousedown', keepFocus);
		gutter.appendChild(button);

		// a section whose preview fits keeps the mark, so the rows stay in line -- it just has nothing to lift
		if (!markClipping(preview)) {
			if (preview) preview.classList.remove('is-clipped');
			button.classList.add('is-static');
			button.disabled = true;
			return;
		}
		preview.classList.add('is-clipped');
		button.title = 'Show the whole section';
		button.addEventListener('click', function (event) {
			event.preventDefault();
			event.stopPropagation();
			var open = preview.classList.toggle('is-expanded');
			button.classList.toggle('is-open', open);
			// three times the cap does not always reach the end, so the fade stays wherever it still cuts off
			preview.classList.toggle('is-clipped', markClipping(preview));
			button.title = open ? 'Collapse the section again' : 'Show the whole section';
		});
	}

	/** The page name is the group's heading, so the sections below it only carry what comes after it. */
	function headingPath(hit) {
		var parts = (hit.breadcrumb || hit.title).split(' › ');
		return parts.length > 1 ? parts.slice(1) : [];
	}

	function pageLink(hit, className, query) {
		var link = document.createElement('a');
		link.className = className;
		// pageUrl comes from the server: for an attachment article it points at the article that renders it
		link.href = hit.pageUrl || ('Wiki.jsp?page=' + (hit.page || hit.title).replace(/ /g, '+'));
		link.appendChild(resultIcon());
		// a span rather than the link's own text, so setting the name cannot wipe the icon again
		var name = document.createElement('span');
		name.textContent = hit.page || hit.title;
		link.appendChild(name);
		markMatches(link, query);
		return link;
	}

	function item(hit) {
		var li = document.createElement('li');
		li.className = 'knowwe-search-hit';

		var body = document.createElement('div');
		body.className = 'knowwe-search-body';
		li.appendChild(body);
		body.appendChild(pageLink(hit, 'knowwe-search-page', state.query));

		var sections = document.createElement('div');
		sections.className = 'knowwe-search-sections';
		body.appendChild(sections);
		var seen = { path: null };
		sections.appendChild(section(hit, seen));
		(hit.sections || []).forEach(function (further) {
			sections.appendChild(section(further, seen));
		});

		if (hit.folded) body.appendChild(expander(hit, sections));
		return li;
	}

	/**
	 * One matching section of the page above it -- its heading path, then what the wiki makes of it.
	 * <p>
	 * Several blocks can sit under one heading (running text and a markup block are indexed apart), and repeating that
	 * heading for each of them reads as if the same section were found three times. The repeat is dropped instead, the
	 * same way the page name is only said once.
	 */
	function section(hit, seen) {
		var wrapper = document.createElement('div');
		wrapper.className = 'knowwe-search-section';

		var gutter = document.createElement('div');
		gutter.className = 'knowwe-search-gutter';
		wrapper.appendChild(gutter);

		var content = document.createElement('div');
		content.className = 'knowwe-search-section-body';
		wrapper.appendChild(content);

		var path = headingPath(hit);
		var repeated = seen && seen.path === path.join(' › ');
		if (seen) seen.path = path.join(' › ');
		if (repeated) wrapper.classList.add('is-continuation');

		if (path.length && !repeated) {
			var link = document.createElement('a');
			link.className = 'knowwe-search-breadcrumb';
			link.href = hit.url;
			path.forEach(function (part, index) {
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
			content.appendChild(link);
		}

		if (hit.stale) {
			var warning = document.createElement('div');
			warning.className = 'knowwe-search-stale';
			warning.textContent = 'The page has changed since it was indexed.';
			content.appendChild(warning);
		}

		if (hit.previewHtml) {
			// the wiki's own rendering of the section: real tables, real markup boxes
			var preview = document.createElement('div');
			preview.className = 'knowwe-search-preview';
			preview.innerHTML = hit.previewHtml;
			dropDuplicateHeading(preview, hit.breadcrumb);
			content.appendChild(preview);
			wrapper.growable = preview;
			markMatches(preview, state.query);
		}
		else {
			var snippet = document.createElement('div');
			snippet.className = 'knowwe-search-snippet';
			// safe: the server escapes the body and only adds its own <mark> tags around the matches
			snippet.innerHTML = hit.snippet || '';
			content.appendChild(snippet);
		}
		return wrapper;
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
	function measureSections(root) {
		loadAsynchronousParts(root);
		root.querySelectorAll('.knowwe-search-preview').forEach(pruneEmptyParagraphs);
		root.querySelectorAll('.knowwe-search-preview').forEach(pruneTables);
		remeasure(root, '.knowwe-search-preview');
		root.querySelectorAll('.knowwe-search-preview').forEach(revealFirstMatch);
		root.querySelectorAll('.knowwe-search-section').forEach(function (wrapper) {
			if (!wrapper.firstChild.childNodes.length) addGrowToggle(wrapper.growable, wrapper.firstChild);
		});
	}

	/*
	 * A preview can carry a part the wiki renders asynchronously -- a visualization, a wiring diagram. On a page, those
	 * placeholders are picked up when the page loads; a result arrives long after that, so nothing would ever replace
	 * them and the spinner would turn for good.
	 *
	 * Handing them over rather than loading them here, because the wiki fetches them only once they are on screen: what
	 * sits behind one can cost a graph layout, and a page of ten results must not order ten of those for content nobody
	 * has scrolled to yet.
	 */
	function loadAsynchronousParts(root) {
		if (!window.KNOWWE) return;
		if (KNOWWE.core && KNOWWE.core.rerendercontent && KNOWWE.core.rerendercontent.loadAsynchronousWhenVisible) {
			KNOWWE.core.rerendercontent.loadAsynchronousWhenVisible(root);
		}
		// Not every markup that fills itself later goes through AsynchronousRenderer. %%WiringViewer, for one, draws a
		// box of its own and has its plugin's script fetch the diagram -- and that script looks for its boxes when the
		// page loads, which is long before a result exists. "afterRerender" is the signal the wiki already uses for
		// "this part of the document is new", so whoever cares gets to hear about our previews too.
		if (KNOWWE.helper && KNOWWE.helper.observer) KNOWWE.helper.observer.notify('afterRerender', root);
	}

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
	/*
	 * The candidates a query offers, longest first: the whole thing, then every shorter run of neighbouring words, and
	 * single words last. Searching for "Unit A40" should mark "Unit A40" where it stands together, and fall back to the
	 * separate words only where it does not -- marking both words everywhere makes a page full of "unit" look like a
	 * hit when only the number matters.
	 */
	function matchCandidates(query) {
		var words = query.toLowerCase().split(/[^\p{L}\p{N}]+/u).filter(function (word) {
			// Two characters count: "Nr" and "24" are half the name in "Cable Nr-24", and dropping them leaves the
			// word sequence unmarkable. Single letters stay out, they would mark halves of words.
			return word.length > 1;
		});
		var candidates = [];
		for (var length = words.length; length >= 1; length--) {
			for (var start = 0; start + length <= words.length; start++) {
				// anything may sit between two words in the text, or nothing at all: a space, a hyphen, a slash --
				// or no separator, because half the names here are written TestCase and PageProvider
				candidates.push(new RegExp(words.slice(start, start + length).join('[^\\p{L}\\p{N}]{0,3}'), 'iu'));
			}
		}
		return candidates;
	}

	/*
	 * A table is rendered whole and pruned here, where the matches are known.
	 *
	 * No match anywhere in it: the table says nothing about this hit and goes. Otherwise every row without a match goes
	 * and the first row stays -- it is the header and tells the remaining rows what they mean. A match in the header
	 * alone therefore leaves exactly the header, which is the answer to "which table is this".
	 *
	 * Deciding this on the server would mean rendering per query and giving up the preview cache.
	 */
	/*
	 * Drops the paragraphs that carry nothing.
	 *
	 * The wiki's parser leaves a "<p />" wherever the source had a blank line, and CSS cannot help here: ":empty" does
	 * not match a paragraph holding a single space, and "<p />" arrives as an open paragraph rather than an empty one.
	 * Done here it also covers the dropdown, where the stylesheet rule never applied in the first place.
	 */
	function pruneEmptyParagraphs(container) {
		container.querySelectorAll('p').forEach(function (paragraph) {
			if (paragraph.textContent.trim()) return;
			if (paragraph.querySelector('img, table, input, select, canvas, svg, iframe')) return;
			paragraph.remove();
		});
	}

	function pruneTables(container) {
		if (container.dataset.pruned) return; // der Messdurchlauf wiederholt sich, das Aufraeumen nicht
		container.dataset.pruned = 'yes';
		container.querySelectorAll('table').forEach(function (table) {
			if (!table.isConnected) return; // an outer table may already have taken it
			if (!table.querySelector('mark')) {
				var wrapper = table.parentElement;
				table.remove();
				// KnowWE wraps its tables in a scrolling div; an empty one would leave a gap
				if (wrapper && wrapper.tagName === 'DIV' && !wrapper.textContent.trim()
					&& !wrapper.querySelector('table, img')) {
					wrapper.remove();
				}
				return;
			}
			var rows = table.querySelectorAll(':scope > tbody > tr, :scope > tr, :scope > thead > tr');
			rows.forEach(function (row, index) {
				if (index === 0) return;
				if (!row.querySelector('mark')) row.remove();
			});
		});
	}

	function markMatches(container, query) {
		var candidates = matchCandidates(query || state.query);
		if (!candidates.length) return;

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
			var found = null;
			for (var i = 0; i < candidates.length; i++) {
				found = candidates[i].exec(text);
				if (found) break;
			}
			if (!found) return;
			var mark = document.createElement('mark');
			mark.textContent = found[0];
			var after = node.splitText(found.index);
			after.nodeValue = after.nodeValue.substring(found[0].length);
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
	// pages, not results: each brings up to three sections of its own, so 10 become up to 30 lines
	var QUICK_LIMIT = 10;
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
		// capture phase: the wiki's own menus stop clicks from bubbling, and a click that never reaches the document
		// would leave the panel standing open
		document.addEventListener('mousedown', function (event) {
			if (!quick.panel.contains(event.target) && event.target !== quick.input) hideQuick();
		}, true);
		// on the document, not on the field: clicking a button in the panel moves the focus there, and Escape
		// would then never reach the field that used to listen for it
		document.addEventListener('keydown', function (event) {
			if (event.key === 'Escape' && !quick.panel.hidden) hideQuick();
		}, true);
	}

	function askQuick() {
		var query = quick.input.value;
		quick.query = query;
		if (query.trim().length < 2) {
			hideQuick();
			return;
		}
		var busy = window.setTimeout(function () {
			quick.panel.classList.add('is-busy');
			// on the first keystroke there is nothing to see yet -- then a line says that something is happening
			if (quick.panel.hidden) {
				quick.panel.innerHTML = '';
				var wait = document.createElement('div');
				wait.className = 'knowwe-quicksearch-busy';
				wait.textContent = 'Searching …';
				quick.panel.appendChild(wait);
				showQuick();
			}
		}, BUSY_AFTER_MS);

		jq$.ajax({
			url: 'action/WikiSearchAction',
			data: { query: query, limit: QUICK_LIMIT, partial: true, preview: true },
			dataType: 'json',
			cache: false
		}).done(function (answer) {
			if (quick.query !== query) return; // a newer keystroke already won
			renderQuick(answer);
		}).fail(hideQuick).always(function () {
			window.clearTimeout(busy);
			quick.panel.classList.remove('is-busy');
		});
	}

	function quickEntry(hit, seen) {
		// the triangle has to sit beside the link, not inside it: a button inside a link is neither valid nor clickable
		var wrapper = document.createElement('div');
		wrapper.className = 'knowwe-quicksearch-section';

		var gutter = document.createElement('div');
		gutter.className = 'knowwe-search-gutter';
		wrapper.appendChild(gutter);

		var entry = document.createElement('a');
		entry.className = 'knowwe-quicksearch-hit';
		entry.href = hit.url;
		wrapper.addEventListener('mouseenter', function () {
			highlightQuick(quickEntries().indexOf(entry));
		});

		wrapper.appendChild(entry);

		var qbody = document.createElement('div');
		qbody.className = 'knowwe-search-body';
		entry.appendChild(qbody);

		var path = headingPath(hit);
		var repeated = seen && seen.path === path.join(' › ');
		if (seen) seen.path = path.join(' › ');
		if (path.length && !repeated) {
			var crumb = document.createElement('div');
			crumb.className = 'knowwe-quicksearch-crumb';
			crumb.textContent = path.join(' › ');
			markMatches(crumb, quick.query);
			qbody.appendChild(crumb);
		}

		if (hit.previewHtml) {
			var preview = document.createElement('div');
			preview.className = 'knowwe-quicksearch-preview';
			preview.innerHTML = hit.previewHtml;
			dropDuplicateHeading(preview, hit.breadcrumb);
			qbody.appendChild(preview);
			markMatches(preview, quick.query);
			wrapper.growable = preview;
		}
		else {
			var snippet = document.createElement('div');
			snippet.className = 'knowwe-quicksearch-snippet';
			// safe: the server escapes the body and only adds its own <mark> tags around the matches
			snippet.innerHTML = hit.snippet || '';
			qbody.appendChild(snippet);
		}
		return wrapper;
	}

	function measureQuickSections(root) {
		loadAsynchronousParts(root);
		root.querySelectorAll('.knowwe-quicksearch-preview').forEach(pruneEmptyParagraphs);
		root.querySelectorAll('.knowwe-quicksearch-preview').forEach(pruneTables);
		remeasure(root, '.knowwe-quicksearch-preview');
		root.querySelectorAll('.knowwe-quicksearch-preview').forEach(revealFirstMatch);
		root.querySelectorAll('.knowwe-quicksearch-section').forEach(function (wrapper) {
			if (!wrapper.firstChild.childNodes.length) addGrowToggle(wrapper.growable, wrapper.firstChild);
		});
	}

	function quickEntries() {
		return Array.prototype.slice.call(quick.panel.querySelectorAll('.knowwe-quicksearch-hit'));
	}

	/*
	 * Same as on the search page, but the expander cannot live inside the entry: the entry is a link, and a button
	 * inside a link is neither valid nor clickable. So it is a sibling.
	 *
	 * Unfolding splices the new entries into quick.hits as well and folding takes them back out, because the arrow
	 * keys walk quick.hits by index while the highlight walks the entries in the panel -- the two have to describe
	 * the same list at all times.
	 */
	function quickExpander(hit) {
		// the unfolded ones join the list behind the group's sections, which is where they appear on screen too
		function position() {
			var at = quick.hits.indexOf(hit);
			return at < 0 ? -1 : at + 1 + (hit.sections ? hit.sections.length : 0);
		}

		var button = document.createElement('button');
		button.type = 'button';
		button.className = 'knowwe-quicksearch-more';
		button.addEventListener('mousedown', keepFocus);
		button.textContent = hit.folded + (hit.folded === 1 ? ' more result' : ' more results') + ' on this page';

		var loaded = null;
		var open = false;

		function insert() {
			var after = button;
			loaded.forEach(function (node) {
				after.parentNode.insertBefore(node, after.nextSibling);
				after = node;
			});
			var at = position();
			if (at >= 0) quick.hits.splice.apply(quick.hits, [at, 0].concat(loaded.map(function (node) {
				return node.searchHit;
			})));
			open = true;
			button.classList.add('is-open');
			measureQuickSections(quick.panel);
		}

		function collapse() {
			// the selection would otherwise point at a removed entry, and the next arrow key at the wrong one
			highlightQuick(-1);
			var at = position();
			if (at >= 0) quick.hits.splice(at, loaded.length);
			loaded.forEach(function (node) {
				node.remove();
			});
			open = false;
			button.classList.remove('is-open');
		}

		button.addEventListener('click', function (event) {
			event.preventDefault();
			if (open) {
				collapse();
				return;
			}
			if (loaded) {
				// fetched once, folding and unfolding again costs nothing
				insert();
				return;
			}
			button.disabled = true;
			jq$.ajax({
				url: 'action/WikiSearchAction',
				data: { query: quick.query, expand: hit.page || hit.title, limit: QUICK_LIMIT, partial: true },
				dataType: 'json',
				cache: false
			}).done(function (answer) {
				loaded = (answer.hits || []).map(function (folded) {
					var node = quickEntry(folded);
					node.searchHit = folded;
					return node;
				});
				insert();
			}).always(function () {
				button.disabled = false;
			});
		});
		return button;
	}

	function renderQuick(answer) {
		quick.hits = answer.hits || [];
		quick.active = -1;
		quick.panel.innerHTML = '';
		var list = document.createElement('div');
		list.className = 'knowwe-quicksearch-list';

		if (!quick.hits.length) {
			if (answer.createPage) quick.panel.appendChild(createPageOffer(answer.createPage));
			var empty = document.createElement('div');
			empty.className = 'knowwe-quicksearch-empty';
			empty.textContent = answer.indexing ? 'Building the index …' : 'No results';
			quick.panel.appendChild(empty);
			showQuick();
			return;
		}

		// at the top, inside the scrolling part: it is the answer to a page name that does not exist, and it gets out
		// of the way as soon as the reader scrolls into the hits
		if (answer.createPage) list.appendChild(createPageOffer(answer.createPage));

		// one block per page: its name once as a heading, the matching sections underneath it
		var groups = quick.hits;
		quick.hits = [];
		groups.forEach(function (hit) {
			var group = document.createElement('div');
			group.className = 'knowwe-quicksearch-group';
			group.appendChild(pageLink(hit, 'knowwe-quicksearch-page', quick.query));
			list.appendChild(group);

			var seen = { path: null };
			group.appendChild(quickEntry(hit, seen));
			quick.hits.push(hit);
			(hit.sections || []).forEach(function (further) {
				group.appendChild(quickEntry(further, seen));
				quick.hits.push(further);
			});
			if (hit.folded) group.appendChild(quickExpander(hit, group));
		});
		quick.panel.appendChild(list);

		var all = document.createElement('a');
		all.className = 'knowwe-quicksearch-all';
		all.href = 'Search.jsp?query=' + encodeURIComponent(quick.query);
		// "on the search page", because below the limit the dropdown already shows them all and "show all"
		// would promise nothing new
		all.textContent = 'Show all ' + answer.total + (answer.exact ? '' : '+') + ' results on the search page';
		quick.panel.appendChild(all);

		showQuick();
		measureQuickSections(quick.panel);
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
		// the marking sits on the whole section, not on the link inside it: the triangle stands beside the link, and
		// marking only the link would leave the line and the triangle differently shaded
		var entries = quickEntries().map(function (entry) {
			return entry.closest('.knowwe-quicksearch-section') || entry;
		});
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
