<%--
   Tab content for the Annotate ("blame") view, included by InfoContent.jsp.
   The annotation is fetched from AnnotatePageAction when the tab is first shown, not while the page info is
   rendered. Computing it reads every version of the page, which is far too much to pay on a view that most
   readers never open.
--%>
<%@ page import="org.apache.wiki.api.core.Context" %>
<%@ page import="org.apache.wiki.api.core.Page" %>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	Context changeAnnotationsContext = Context.findContext(pageContext);
	Page changeAnnotationsPage = changeAnnotationsContext.getPage();
	pageContext.setAttribute("changeAnnotationsPageName",
			changeAnnotationsPage == null ? null : changeAnnotationsPage.getName());
%>
<c:if test="${changeAnnotationsPageName != null}">
	<div id="changeAnnotations" data-annotate-page="<c:out value='${changeAnnotationsPageName}'/>"></div>
	<script type="text/javascript">
		(function () {
			var container = document.getElementById('changeAnnotations');
			if (!container) return;
			var loaded = false;

			function load() {
				if (loaded) return;
				loaded = true;
				container.textContent = 'Loading annotations...';
				fetch('action/AnnotatePageAction?page=' + encodeURIComponent(container.dataset.annotatePage), {
					credentials: 'same-origin'
				}).then(function (response) {
					if (!response.ok) throw new Error(response.status + ' ' + response.statusText);
					return response.text();
				}).then(function (html) {
					container.innerHTML = html;
				}).catch(function (error) {
					container.textContent = '';
					var warning = document.createElement('p');
					warning.className = 'warning';
					warning.textContent = 'No annotation available: ' + error.message;
					container.appendChild(warning);
				});
			}

			// the tab keeps its content hidden until the user opens it, so becoming visible is the trigger. Tabs
			// that are open on arrival are visible right away and load immediately
			if (typeof IntersectionObserver === 'function') {
				var observer = new IntersectionObserver(function (entries) {
					for (var i = 0; i < entries.length; i++) {
						if (entries[i].isIntersecting) {
							observer.disconnect();
							load();
						}
					}
				});
				observer.observe(container);
			}
			else {
				load();
			}
		})();
	</script>
</c:if>
