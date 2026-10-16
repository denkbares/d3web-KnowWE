<%--
    Copyright (C) 2026 denkbares GmbH, Germany

    This is free software; you can redistribute it and/or modify it under the
    terms of the GNU Lesser General Public License as published by the Free
    Software Foundation; either version 3 of the License, or (at your option) any
    later version.

    This software is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
    FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
    details.

    You should have received a copy of the GNU Lesser General Public License
    along with this software; if not, write to the Free Software Foundation,
    Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
    site: http://www.fsf.org.
--%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="org.apache.wiki.api.core.*" %>
<%@ page import="org.apache.wiki.auth.*" %>
<%@ page import="org.apache.wiki.auth.permissions.*" %>
<%@ page import="org.apache.wiki.pages.PageManager" %>
<%@ page import="org.apache.wiki.ui.*" %>
<%@ page import="org.apache.wiki.util.TextUtil" %>
<%@ page import="org.apache.wiki.utils.WikiPageUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="de.knowwe.core.Environment" %>
<%@ page import="de.knowwe.kdom.defaultMarkup.DefaultMarkup" %>
<%@ page import="de.knowwe.kdom.defaultMarkup.DefaultMarkupType" %>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%--
    CodeMirror based page editor. The form keeps the fields Edit.jsp relies on. The edited text is held in a hidden
    textarea that the editor script fills before the form is submitted.
--%>
<%
   Context context = Context.findContext( pageContext );
   Engine engine = context.getEngine();

   String usertext = EditorManager.getEditedText( pageContext );
   String none = ContextEnum.PAGE_NONE.getRequestContext();
   String version = "?version=" + WikiPageUtils.getJarVersion();
   String editorStylesheet = context.getURL( none, "KnowWEExtension/css/KnowWE-CodeMirrorEditor.css" ) + version;
   String editorScript = context.getURL( none, "KnowWEExtension/scripts/KnowWE-CodeMirrorEditor.js" ) + version;

   // the wiki language tells KnowWE markups from JSPWiki styles by the names of the installed markups
   JSONArray markups = new JSONArray();
   for( DefaultMarkup markup : DefaultMarkupType.getInstalledMarkups(Environment.of(request)) ) {
      markups.put( new JSONObject().put( "name", markup.getName() ).put( "inline", markup.isInline() ) );
   }
%>
<script>
  if (typeof KnowWEWikiLanguage !== "undefined") KnowWEWikiLanguage.defineMarkups(<%= markups.toString() %>);
</script>
<wiki:RequestResource type="stylesheet" resource="<%=editorStylesheet%>" />
<wiki:RequestResource type="script" resource="<%=editorScript%>" />
<c:set var='context'><wiki:Variable var='requestcontext' /></c:set>
<wiki:CheckRequestContext context="edit">
<wiki:NoSuchPage> <%-- this is a new page, check if we're cloning --%>
<%
  String clone = request.getParameter( "clone" );
  if( clone != null )
  {
    Page p = engine.getManager( PageManager.class ).getPage( clone );
    if( p != null )
    {
        AuthorizationManager mgr = engine.getManager( AuthorizationManager.class );
        PagePermission pp = new PagePermission( p, PagePermission.VIEW_ACTION );

        try
        {
          if( mgr.checkPermission( context.getWikiSession(), pp ) )
          {
            usertext = engine.getManager( PageManager.class ).getPureText( p );
          }
        }
        catch( Exception e ) { /* ignore, the page is simply not cloned */ }
    }
  }
%>
</wiki:NoSuchPage>
<%
  if( usertext == null )
  {
    usertext = engine.getManager( PageManager.class ).getPureText( context.getPage() );
  }
%>
</wiki:CheckRequestContext>
<% if( usertext == null ) usertext = "";  %>

<form method="post" accept-charset="<wiki:ContentEncoding/>"
      action="<wiki:Link context='${context}' format='url'/>"
       class="knowwe-editform"
          id="editform"
     enctype="application/x-www-form-urlencoded" >

  <wiki:CsrfProtection/>
  <%-- Edit.jsp relies on these being found.  So be careful, if you make changes. --%>
  <input type="hidden" name="page" value="<wiki:Variable var='pagename' />" />
  <input type="hidden" name="action" value="save" />
  <wiki:SpamFilterInputs/>

  <div class="knowwe-editor" data-context="${context}">

    <div class="knowwe-editor-toolbar">
      <div class="knowwe-editor-group">
        <fmt:message key='editor.plain.save.title' var='msg'/>
        <button class="btn btn-success" type="submit" name="ok" accesskey="s" title="${msg}">
          <i class="fa fa-check"></i> <fmt:message key='editor.plain.save.submit${ context == "edit" ? "" : ".comment" }'/>
        </button>
        <fmt:message key='editor.plain.preview.title' var='msg'/>
        <button class="btn btn-default" type="submit" name="preview" accesskey="v" title="${msg}">
          <i class="fa fa-eye"></i> <fmt:message key='editor.plain.preview.submit'/>
        </button>
      </div>

      <wiki:CheckRequestContext context="!comment">
      <input class="form-control knowwe-editor-changenote" type="text" name="changenote" id="changenote"
             maxlength="80" placeholder="<fmt:message key='editor.plain.changenote'/>" value="${changenote}" />
      </wiki:CheckRequestContext>

      <div class="knowwe-editor-group knowwe-editor-group-right">
        <fmt:message key='editor.plain.undo.title' var='msg'/>
        <button class="btn btn-default" type="button" data-editor-cmd="undo" title="${msg}"><i class="fa fa-rotate-left"></i></button>
        <fmt:message key='editor.plain.redo.title' var='msg'/>
        <button class="btn btn-default" type="button" data-editor-cmd="redo" title="${msg}"><i class="fa fa-rotate-right"></i></button>
        <button class="btn btn-default" type="button" data-editor-cmd="find" title="<fmt:message key='editor.plain.find'/> &amp; <fmt:message key='editor.plain.replace'/>"><i class="fa fa-magnifying-glass"></i></button>
        <button class="btn btn-default" type="button" data-editor-cmd="wrap" title="Toggle line wrapping"><i class="fa fa-arrow-turn-down"></i></button>
        <button class="btn btn-default" type="button" data-editor-cmd="help" title="Keyboard shortcuts and tips" aria-label="Keyboard shortcuts and tips"><i class="fa fa-circle-question"></i></button>

        <c:set var="editors" value="<%= engine.getManager( EditorManager.class ).getEditorList() %>" />
        <c:if test='${fn:length(editors) > 1}'>
        <div class="btn-group knowwe-editor-switch">
          <button class="btn btn-default dropdown-toggle" type="button" data-editor-cmd="switch" title="Editor">
            <i class="fa fa-pen"></i> <span class="caret"></span>
          </button>
          <ul class="dropdown-menu dropdown-menu-right">
            <c:forEach items="${editors}" var="edt">
              <c:choose>
                <c:when test="${edt != prefs.editor}">
                  <li><a href="#" class="editor-type" data-editor="${edt}">${edt}</a></li>
                </c:when>
                <c:otherwise>
                  <li class="active"><a>${edt}</a></li>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </ul>
        </div>
        </c:if>
      </div>

      <fmt:message key='editor.plain.cancel.title' var='msg'/>
      <button class="btn btn-danger knowwe-editor-cancel" type="submit" name="cancel" accesskey="q" title="${msg}" formnovalidate="formnovalidate">
        <i class="fa fa-xmark"></i> <fmt:message key='editor.plain.cancel.submit'/>
      </button>
    </div>

    <div class="knowwe-editor-surface"></div>

    <textarea class="knowwe-editor-source" name="<%=EditorManager.REQ_EDITEDTEXT%>"
              rows="20" cols="80"><%=TextUtil.replaceEntities(usertext)%></textarea>

    <div class="knowwe-editor-statusbar">
      <span class="knowwe-editor-position"></span>
      <span class="knowwe-editor-dirty" hidden="hidden">Unsaved changes</span>
    </div>

  </div>

  <wiki:CheckRequestContext context="comment">
    <fieldset class="knowwe-editor-signature">
      <legend><fmt:message key="editor.commentsignature"/></legend>
      <div class="form-group">
        <label for="authorname"><fmt:message key="editor.plain.name"/></label>
        <input class="form-control" type="text" name="author" id="authorname" value="${author}" />
        <label class="checkbox-inline" for="rememberme">
          <input type="checkbox" name="remember" id="rememberme" ${ remember ? "checked='checked'" : "" } />
          <fmt:message key="editor.plain.remember"/>
        </label>
      </div>
      <div class="form-group">
        <label for="link"><fmt:message key="editor.plain.email"/></label>
        <input class="form-control" type="text" name="link" id="link" value="${link}" />
      </div>
    </fieldset>
  </wiki:CheckRequestContext>

</form>
