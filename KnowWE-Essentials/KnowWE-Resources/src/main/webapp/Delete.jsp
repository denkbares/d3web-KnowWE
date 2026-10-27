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

<%@ page import="java.util.*" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="org.apache.wiki.api.core.*" %>
<%@ page import="org.apache.wiki.api.spi.Wiki" %>
<%@ page import="org.apache.wiki.api.core.Attachment" %>
<%@ page import="org.apache.wiki.auth.AuthorizationManager" %>
<%@ page import="org.apache.wiki.auth.permissions.PermissionFactory" %>
<%@ page import="org.apache.wiki.http.filter.CsrfProtectionFilter" %>
<%@ page import="org.apache.wiki.pages.PageManager" %>
<%@ page import="org.apache.wiki.tags.BreadcrumbsTag" %>
<%@ page import="org.apache.wiki.tags.BreadcrumbsTag.FixedQueue" %>
<%@ page import="org.apache.wiki.ui.TemplateManager" %>
<%@ page import="org.apache.wiki.util.HttpUtil" %>
<%@ page import="org.apache.wiki.util.TextUtil" %>
<%@ page errorPage="/Error.jsp" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="de.knowwe.core.Environment" %>
<%@ page import="de.knowwe.core.utils.KnowWEUtils" %>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>

<%!
	DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	Logger log = LoggerFactory.getLogger("JSPWiki");
%>

<%
	Engine wiki = Wiki.engine().find(getServletConfig());
	// Create wiki context and check for authorization
	Context wikiContext = Wiki.context().create(wiki, request, ContextEnum.PAGE_DELETE.getRequestContext());
	boolean attachment = wikiContext.getPage() instanceof Attachment;
	AuthorizationManager authorization = wiki.getManager(AuthorizationManager.class);
	boolean canDelete = authorization.checkPermission(wikiContext.getWikiSession(),
			PermissionFactory.getPagePermission(wikiContext.getPage(), "delete"));
	// Editors can clear page content and may delete their own newly created objects.
	if (!canDelete) wikiContext.setRequestContext(ContextEnum.PAGE_EDIT.getRequestContext());
	if (!authorization.hasAccess(wikiContext, response)) return;
	wikiContext.setRequestContext(ContextEnum.PAGE_DELETE.getRequestContext());
	if (wikiContext.getCommand().getTarget() == null) {
		response.sendRedirect(wikiContext.getURL(wikiContext.getRequestContext(), wikiContext.getName()));
		return;
	}
	String deleteAdmin = request.getParameter("delete-admin");
	String deleteall = request.getParameter("delete-all");
	String delete = request.getParameter("delete");
	if ((delete != null || deleteall != null || deleteAdmin != null) && !CsrfProtectionFilter.isCsrfProtectedPost(request)) {
		response.sendRedirect(request.getContextPath() + "/error/Forbidden.html");
		return;
	}

	if (!attachment && deleteall != null && deleteAdmin == null && delete == null) {
		String deleteText = "Deleted by " + wikiContext.getCurrentUser()
				.getName() + " at " + FORMATTER.format(LocalDateTime.now());
		// the context's page describes the latest version, so saving it as is would credit the previous author
		Page deletion = wikiContext.getPage().clone();
		deletion.setAuthor(wikiContext.getCurrentUser().getName());
		deletion.setAttribute(Page.CHANGENOTE, "Deleted page");
		wiki.getManager(PageManager.class).putPageText(deletion, deleteText);
		response.sendRedirect(wikiContext.getURL(ContextEnum.PAGE_VIEW.getRequestContext(), wikiContext.getName()));
		return;
	}
	// Check permanent deletion on the server as well; request parameters are user-controlled.
	boolean permanentDeletion = attachment || deleteAdmin != null || delete != null;
	if (permanentDeletion && !canDelete) {
		boolean authenticated = wikiContext.getWikiSession().isAuthenticated();
		boolean allowDeletionByCreator = authenticated && KnowWEUtils.allowDeletionByCreator(Environment.of(request),
				wikiContext.getName(), attachment, wikiContext.getCurrentUser().getName());
		if (!allowDeletionByCreator) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	}
	// Deleting a page also deletes its attachments. The creator exception must cover each of them.
	if (!attachment && deleteAdmin != null && !canDelete) {
		for (Attachment child : wiki.getManager(org.apache.wiki.attachment.AttachmentManager.class).listAttachments(wikiContext.getPage())) {
			boolean canDeleteChild = authorization.checkPermission(wikiContext.getWikiSession(),
					PermissionFactory.getPagePermission(child, "delete"));
			boolean canEditChild = authorization.checkPermission(wikiContext.getWikiSession(),
					PermissionFactory.getPagePermission(child, "edit"));
			if (!canDeleteChild && !(canEditChild && KnowWEUtils.allowDeletionByCreator(Environment.of(request),
					child.getName(), true, wikiContext.getCurrentUser().getName()))) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
	}
	String pagereq = wikiContext.getName();

	Page wikipage = wikiContext.getPage();
	Page latestversion = wiki.getManager(PageManager.class).getPage(pagereq);

	if (latestversion == null) {
		latestversion = wikiContext.getPage();
	}

	// If deleting an attachment, go to the parent page.
	String redirTo = pagereq;
	if (wikipage instanceof Attachment) {
		redirTo = ((Attachment) wikipage).getParentName();
	}

	if (deleteall != null || deleteAdmin != null) {
		log.info("Deleting page " + pagereq + ". User=" + request.getRemoteUser() + ", host=" + HttpUtil.getRemoteAddress(request));

		wiki.getManager(PageManager.class).deletePage(pagereq);

		FixedQueue trail = (FixedQueue) session.getAttribute(BreadcrumbsTag.BREADCRUMBTRAIL_KEY);
		if (trail != null) {
			trail.removeItem(pagereq);
			session.setAttribute(BreadcrumbsTag.BREADCRUMBTRAIL_KEY, trail);
		}

		response.sendRedirect(TextUtil.replaceString(wiki.getURL(ContextEnum.PAGE_UPLOAD.getRequestContext(), redirTo, ""), "&amp;", "&"));
		return;
	}
	else if (delete != null) {
		log.info("Deleting a range of pages from " + pagereq);

		for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
			String paramName = params.nextElement();

			if (paramName.startsWith("delver")) {
				int version = Integer.parseInt(paramName.substring(7));

				Page p = wiki.getManager(PageManager.class).getPage(pagereq, version);

				log.debug("Deleting version " + version);
				wiki.getManager(PageManager.class).deleteVersion(p);
			}
		}

		response.sendRedirect(
				TextUtil.replaceString(wiki.getURL(ContextEnum.PAGE_VIEW.getRequestContext(), redirTo, "tab=" + request.getParameter("tab")), "&amp;", "&")
		);

		return;
	}

	// Set the content type and include the response content
	// FIXME: not so.
	response.setContentType("text/html; charset=" + wiki.getContentEncoding());
	String contentPage = wiki.getManager(TemplateManager.class)
			.findJSP(pageContext, wikiContext.getTemplate(), "EditTemplate.jsp");
%><wiki:Include page="<%=contentPage%>"/>

