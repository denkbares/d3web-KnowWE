<%--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
  --%>

<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.apache.wiki.api.core.*" %>
<%@ page import="org.apache.wiki.api.spi.Wiki" %>
<%@ page import="org.apache.wiki.auth.*" %>
<%@ page import="org.apache.wiki.auth.permissions.*" %>
<%@ page import="org.apache.wiki.http.filter.CsrfProtectionFilter" %>
<%@ page import="org.apache.wiki.preferences.Preferences" %>
<%@ page import="de.knowwe.jspwiki.KnowWEPlugin" %>
<%@ page import="de.knowwe.jspwiki.JSPWikiUserContext" %>
<%@ page import="de.knowwe.core.user.UserContextUtil" %>
<%@ page import="org.apache.wiki.render.RenderingManager" %>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%! 
  public void jspInit()
  {
    wiki = Wiki.engine().find( getServletConfig() );
  }
  Logger log = LoggerFactory.getLogger("JSPWikiSearch");
  Engine wiki;
%>
<%
  // Previews render arbitrary submitted markup, so they are guarded like an edit
  if( !"POST".equalsIgnoreCase( request.getMethod() ) ) {
    response.sendError( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
    return;
  }
  if( !CsrfProtectionFilter.isCsrfProtectedPost( request ) ) {
    response.sendRedirect( request.getContextPath() + "/error/Forbidden.html" );
    return;
  }
  Context wikiContext = Wiki.context().create( wiki, request, ContextEnum.PAGE_EDIT.getRequestContext() );
  AuthorizationManager authorization = wiki.getManager( AuthorizationManager.class );
  // Commenters preview their comment, which they may post anyway
  if( !authorization.checkPermission( wikiContext.getWikiSession(),
          PermissionFactory.getPagePermission( wikiContext.getPage(), "edit" ) ) ) {
    wikiContext.setRequestContext( ContextEnum.PAGE_COMMENT.getRequestContext() );
  }
  if( !authorization.hasAccess( wikiContext, response ) ) return;

  response.setContentType("text/html; charset="+wiki.getContentEncoding() );

  String wikimarkup = request.getParameter( "wikimarkup" );
  JSPWikiUserContext userContext = new JSPWikiUserContext(wikiContext, UserContextUtil.getParameters(request));
  userContext.getRequest().setAttribute(KnowWEPlugin.RENDER_MODE, KnowWEPlugin.PREVIEW);
  String html = KnowWEPlugin.renderPreview(wikiContext, wikimarkup);
  String result = wiki.getManager(RenderingManager.class).textToHTML(wikiContext, html);

%>
<%=result%>