/**
 * Fills the Annotate tab of the page info view with the change annotation of the page.
 *
 * The markup contributes nothing but an empty element carrying data-annotate-page with the page name. The
 * annotation is fetched from AnnotatePageAction once that element is shown, because computing it reads every
 * version of the page and must not be paid on a page info view that most readers never open.
 */
(function () {
	'use strict';

	var ATTRIBUTE = 'data-annotate-page';

	/**
	 * Attaches a shadow root for every declarative shadow root template below the given element.
	 *
	 * The HTML parser turns a template carrying shadowrootmode into a shadow root only while it parses a document.
	 * Content assigned through innerHTML leaves the template inert and therefore invisible, so it has to be
	 * hydrated by hand. attachShadow is much older than the parser feature, so doing it this way works in every
	 * browser that could ever have displayed this tab.
	 */
	function hydrateShadowRoots(element) {
		var templates = element.querySelectorAll('template[shadowrootmode]');
		for (var i = 0; i < templates.length; i++) {
			var template = templates[i];
			var host = template.parentNode;
			host.attachShadow({ mode: template.getAttribute('shadowrootmode') }).appendChild(template.content);
			template.remove();
		}
	}

	function load(container) {
		container.textContent = 'Loading annotations...';
		fetch('action/AnnotatePageAction?page=' + encodeURIComponent(container.getAttribute(ATTRIBUTE)), {
			credentials: 'same-origin'
		}).then(function (response) {
			if (!response.ok) throw new Error(response.status + ' ' + response.statusText);
			return response.text();
		}).then(function (html) {
			container.innerHTML = html;
			hydrateShadowRoots(container);
		}).catch(function (error) {
			container.textContent = '';
			var warning = document.createElement('p');
			warning.className = 'warning';
			warning.textContent = 'No annotation available: ' + error.message;
			container.appendChild(warning);
		});
	}

	/**
	 * Loads the annotation once the tab carrying the container is opened.
	 *
	 * Both templates keep the tab's content hidden until then, so the container gaining a width is the one signal
	 * that means the same thing in both. A hidden element is laid out at zero width, which is what separates the
	 * two states, and a tab that is already open on arrival reports a real width straight away. Browsers without
	 * the observer load immediately, which is the behaviour the tab had before it became lazy.
	 */
	function loadWhenShown(container) {
		if (typeof ResizeObserver !== 'function') {
			load(container);
			return;
		}
		var observer = new ResizeObserver(function (entries) {
			for (var i = 0; i < entries.length; i++) {
				if (entries[i].contentRect.width > 0) {
					observer.disconnect();
					load(container);
					return;
				}
			}
		});
		observer.observe(container);
	}

	function init() {
		var containers = document.querySelectorAll('[' + ATTRIBUTE + ']');
		for (var i = 0; i < containers.length; i++) {
			loadWhenShown(containers[i]);
		}
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', init);
	}
	else {
		init();
	}
})();
