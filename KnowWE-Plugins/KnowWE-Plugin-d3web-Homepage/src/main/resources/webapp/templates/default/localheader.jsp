<%--
  ~ Copyright (C) 2026 denkbares GmbH, Germany
  ~
  ~ This is free software; you can redistribute it and/or modify it under the
  ~ terms of the GNU Lesser General Public License as published by the Free
  ~ Software Foundation; either version 3 of the License, or (at your option) any
  ~ later version.
  ~
  ~ This software is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  ~ FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  ~ details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public License
  ~ along with this software; if not, write to the Free Software Foundation,
  ~ Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
  ~ site: http://www.fsf.org.
  --%>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%--
  d3web.de additions to the head section, included by the default commonheader.jsp after the skin.
  Overriding localheader.jsp (instead of commonheader.jsp) keeps the homepage in sync with the KnowWE core header.
--%>
<link rel="stylesheet" type="text/css" media="screen, projection, print"
	  href="<wiki:Link format='url' templatefile='skins/d3web/d3web.css' />"/>

<%-- Matomo statistics for d3web.de, not for local test instances --%>
<% if (!request.getServerName().matches("localhost|127\\.0\\.0\\.1|\\[::1]")) { %>
<script type="text/javascript">
	var _paq = window._paq = window._paq || [];
	_paq.push(["disableCookies"]);
	_paq.push(['trackPageView']);
	_paq.push(['enableLinkTracking']);
	(function() {
		var u="//stats.denkbares.com/";
		_paq.push(['setTrackerUrl', u+'matomo.php']);
		_paq.push(['setSiteId', '3']);
		var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
		g.type='text/javascript'; g.async=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);
	})();
</script>
<% } %>
