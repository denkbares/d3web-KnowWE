<%--
  ~ Copyright (C) 2016 denkbares GmbH, Germany
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

<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="org.apache.log4j.*" %>
<%@ page import="org.apache.wiki.*" %>
<%@ page import="org.apache.wiki.auth.*" %>
<%@ page import="org.apache.wiki.auth.permissions.*" %>
<%@ page import="org.apache.wiki.preferences.Preferences" %>
<%@ page import="de.knowwe.jspwiki.KnowWEPlugin" %>
<%@ page import="de.knowwe.jspwiki.JSPWikiUserContext" %>
<%@ page import="de.knowwe.core.user.UserContextUtil" %>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%! 
  public void jspInit()
  {
    wiki = WikiEngine.getInstance( getServletConfig() );
  }
  Logger log = Logger.getLogger("JSPWikiSearch");
  WikiEngine wiki;
%>
<%
  // Copied from a top-level jsp -- which would be a better place to put this 
  WikiContext wikiContext = wiki.createContext( request, WikiContext.VIEW );
  if( !wiki.getAuthorizationManager().hasAccess( wikiContext, response ) ) return;

  response.setContentType("text/html; charset="+wiki.getContentEncoding() );

  String wikimarkup = request.getParameter( "wikimarkup" );
  JSPWikiUserContext userContext = new JSPWikiUserContext(wikiContext, UserContextUtil.getParameters(request));
  userContext.getRequest().setAttribute(KnowWEPlugin.RENDER_MODE, KnowWEPlugin.PREVIEW);
  String html = KnowWEPlugin.renderPreview(wikiContext, wikimarkup);
  String result = wiki.textToHTML(wikiContext, html);

%>
<%=result%>