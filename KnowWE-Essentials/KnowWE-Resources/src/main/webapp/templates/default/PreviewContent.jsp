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

<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ page import="org.apache.wiki.api.core.Context" %>
<%@ page import="org.apache.wiki.render.RenderingManager" %>
<%@ page import="org.apache.wiki.ui.EditorManager" %>
<%@ page import="de.knowwe.jspwiki.KnowWEPlugin" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%
  // Renders the edited text through KnowWE, the plain wiki preview would show unsaved markup as raw text
  Context wikiContext = Context.findContext( pageContext );
  String preview;
  try {
    String html = KnowWEPlugin.renderPreview( wikiContext, EditorManager.getEditedText( pageContext ) );
    preview = wikiContext.getEngine().getManager( RenderingManager.class ).textToHTML( wikiContext, html );
  }
  finally {
    // The render mode stays on the request otherwise and the sidebar would skip its KnowWE rendering
    request.removeAttribute( KnowWEPlugin.RENDER_MODE );
  }
%>

<%-- Inserts page content for preview. --%>
<div class="page-content preview-content">

  <div class="information">
    <wiki:Editor/>
    <p class="help-block"><fmt:message key="preview.info"/></p>
  </div>

  <div class="preview-body">
    <%=preview%>
  </div>

  <div class="information">
    <fmt:message key="preview.info"/>
  </div>

</div>
