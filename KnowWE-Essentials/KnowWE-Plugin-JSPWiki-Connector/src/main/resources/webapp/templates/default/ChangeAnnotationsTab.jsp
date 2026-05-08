<%--
   Tab content for the Annotate ("blame") view, included by InfoContent.jsp.
   Renders a placeholder + a small lazy-load script that fetches the server-rendered
   <knowwe-page-annotate> on first activation. The actual computation happens in
   AnnotatePageAction; this JSP only wires the UI.
--%>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="changeAnnotationsPage"><wiki:Variable var="pagename"/></c:set>
<div class="page-annotate-tab" data-page="<c:out value='${changeAnnotationsPage}'/>" aria-live="polite">
	<p class="loading-placeholder" style="color:#57606a;font-style:italic;">Loading annotations…</p>
</div>
<script type="text/javascript">
(function () {
	var tab = document.querySelector('.page-annotate-tab');
	if (!tab) return;
	var loaded = false;

	function load() {
		if (loaded) return;
		loaded = true;
		var pageName = tab.getAttribute('data-page');
		var url = 'action/AnnotatePageAction?page=' + encodeURIComponent(pageName);
		if (typeof Wiki !== 'undefined' && Wiki.CsrfProtection) {
			url += '&X-XSRF-TOKEN=' + encodeURIComponent(Wiki.CsrfProtection);
		}
		fetch(url, { credentials: 'same-origin' })
			.then(function (r) {
				if (!r.ok) throw new Error('HTTP ' + r.status);
				return r.text();
			})
			.then(function (html) { tab.innerHTML = html; })
			.catch(function (e) {
				tab.innerHTML = '<p class="warning">Failed to load annotations: '
						+ (e && e.message ? e.message : e) + '</p>';
			});
	}

	// Trigger on click of the tab heading, on direct hash navigation, and when JSPWiki
	// already marked the heading as the active pane on initial render.
	var heading = document.getElementById('annotate');
	if (heading) heading.addEventListener('click', load);
	if (window.location.hash === '#annotate') load();
	if (heading && heading.hasAttribute('data-activePane')) load();
})();
</script>
