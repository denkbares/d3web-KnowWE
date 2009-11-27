/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.jspwiki;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.PageLock;
import com.ecyrd.jspwiki.PageManager;
import com.ecyrd.jspwiki.SearchResult;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.attachment.AttachmentManager;
import com.ecyrd.jspwiki.auth.AuthorizationManager;
import com.ecyrd.jspwiki.auth.permissions.PagePermission;
import com.ecyrd.jspwiki.auth.permissions.PermissionFactory;
import com.ecyrd.jspwiki.preferences.Preferences;
import com.ecyrd.jspwiki.providers.ProviderException;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.TaggingMangler;
import de.d3web.we.search.GenericSearchResult;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

/**
 * For code documentation look at the KnowWEWikiConnector interface definition
 * 
 * @author Jochen
 * 
 */
public class JSPWikiKnowWEConnector implements KnowWEWikiConnector {

    private ServletContext context = null;
    private WikiEngine engine = null;
    public static final String LINK_PREFIX = "Wiki.jsp?page=";

    public JSPWikiKnowWEConnector(WikiEngine eng) {
	this.context = eng.getServletContext();
	this.engine = eng;
    }

    @Override
    public ServletContext getServletContext() {
	return context;
    }

    @Override
    public KnowWEActionDispatcher getActionDispatcher() {
	// TODO Auto-generated method stub
	return new JSPActionDispatcher();
    }

    @Override
    public boolean saveArticle(String name, String text, KnowWEParameterMap map) {
	try {
	    HttpServletRequest req = map.getRequest();
	    WikiContext context = engine.createContext(req, WikiContext.EDIT);
	    context.setPage(engine.getPage(name));
	    engine.saveText(context, text);
	    // engine.saveText(map.getContext(), text);
	    return true;
	} catch (WikiException e) {
	    e.printStackTrace();
	    return false;
	}
    }

    @Override
    public LinkedList<String> getAttachments() {
	try {
	    LinkedList<String> sortedAttList = new LinkedList<String>();
	    AttachmentManager attachmentManager = this.engine
		    .getAttachmentManager();
	    Collection<Attachment> attList = attachmentManager
		    .getAllAttachments();

	    for (Attachment p : attList) {

		if (p.getFileName().endsWith("jar")) {
		    sortedAttList.add(p.getFileName());
		}

	    }
	    return sortedAttList;
	} catch (ProviderException e) {
	    return null;
	}
    }

    public boolean storeAttachment(String wikiPage, File attachmentFile) {
	try {
	    if (!isPageLocked(wikiPage)) {
		setPageLocked(wikiPage, "WIKI-ENGINE");
		AttachmentManager attachmentManager = this.engine
			.getAttachmentManager();

		Attachment att = new Attachment(engine, wikiPage,
			attachmentFile.getName());
		attachmentManager.storeAttachment(att, attachmentFile);

		undoPageLocked(wikiPage);
		return true;
	    } else {
		return false;
	    }
	} catch (ProviderException e) {
	    return false;
	} catch (IOException e) {
	    return false;
	}

    }

    @Override
    public String getAttachmentPath(String jarName) {

	if (this.getMeAttachment(jarName) != null) {

	    String jarPath = "";

	    // Get Path where all Attachments are stored
	    String storageDir;
	    try {
		storageDir = WikiEngine.getRequiredProperty(this.engine
			.getWikiProperties(),
			"jspwiki.basicAttachmentProvider.storageDir");
		jarPath += storageDir;
	    } catch (NoRequiredPropertyException e) {
		// TODO Auto-generated catch block
		return null;
	    }

	    // Get Attachments ParentPage
	    String parentPage = this.getMeAttachment(jarName).getParentName();
	    jarPath += "/" + parentPage + "-att/";

	    // Get the Attachments directory
	    // TEST: WHAT VERSION IS THE NEWEST
	    jarPath += jarName + "-dir/";

	    // Fixes a bug in which the getVersion returns -1 instead of 1;
	    jarPath += String.valueOf(Math.abs(this.getMeAttachment(jarName)
		    .getVersion()));
	    String fileSuffix = jarName.substring(jarName.lastIndexOf("."));
	    jarPath += fileSuffix;

	    return jarPath;
	}
	return null;
    }

    private Attachment getMeAttachment(String name) {

	Collection<Attachment> attList;

	try {
	    attList = this.engine.getAttachmentManager().getAllAttachments();
	} catch (ProviderException e) {
	    // TODO Auto-generated catch block
	    return null;
	}

	for (Attachment p : attList) {
	    if (p.getFileName().equals(name)) {
		return p;
	    }
	}

	return null;
    }

    public java.util.Map<String, String> getAllArticles(String web) {
	Map<String, String> result = new HashMap<String, String>();
	Collection<WikiPage> pages = null;
	PageManager pageManager = this.engine.getPageManager();
	try {
	    pages = pageManager.getProvider().getAllPages();
	} catch (ProviderException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	if (pages == null)
	    return null;

	for (WikiPage wikiPage : pages) {
	    String pageContent = null;
	    try {
		pageContent = pageManager.getPageText(wikiPage.getName(),
			wikiPage.getVersion());
	    } catch (ProviderException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    if (pageContent != null) {
		result.put(wikiPage.getName(), pageContent);
	    }

	}

	return result;
    }

    public String createWikiPage(String topic, String content, String author) {

	WikiPage wp = new WikiPage(this.engine, topic);

	try {
	    // References Updaten.
	    // this.engine.getReferenceManager().updateReferences(
	    // att.getName(),
	    // new java.util.Vector() );
	    wp.setAuthor(author);
	    this.engine.getPageManager().putPageText(wp, content);
	    this.engine.getSearchManager().reindexPage(wp);

	} catch (ProviderException e) {
	    return null;
	}

	return this.engine.getPureText(wp);
    }

    // private String getHashMapContent(HashMap<String, String> pageContent,
    // String topic ) {
    //		
    // String content = "<Kopic id=\"" + topic + "\"" + ">";
    //		
    // // append the Sections to content if they are not "".
    // // 1. Questionnaires-section
    // if (pageContent.get("qClassHierarchy") != "") {
    // content += "\n<Questionnaires-section>\n";
    // content += pageContent.get("qClassHierarchy");
    // content += "</Questionnaires-section>\n";
    // }
    //		
    // // 2.Questions-section
    // if (pageContent.get("decisionTree") != "") {
    // content += "<Questions-section>\n";
    // content += pageContent.get("decisionTree");
    // content += "</Questions-section>\n";
    // }
    //		
    // // 3. SetCoveringList-Section
    // if (pageContent.get("xcl") != "") {
    // content += "<SetCoveringList-section>\n";
    // content += pageContent.get("xcl");
    // content += "</SetCoveringList-section>\n";
    // }
    //				
    // // 4. Rules-section
    // if (pageContent.get("rules") != "") {
    // content.concat("<Rules-section>\n");
    // content.concat(pageContent.get("rules"));
    // content.concat("</Rules-section>\n");
    // }
    //		
    // // 5. Solutions-Section
    // if (pageContent.get("diagnosisHierarchy") != "") {
    // content += "<Solutions-section>\n";
    // content += pageContent.get("diagnosisHierarchy");
    // content += "</Solutions-section>\n";
    // }
    //				
    // content += "</Kopic>";
    // return content;
    // }

    @Override
    public boolean doesPageExist(String topic) {

	// Check if a Page with the chosen Topic already exists.
	if (this.engine.pageExists(topic)) {
	    return true;
	}
	return false;
    }

    @Override
    public String appendContentToPage(String topic, String content) {

	try {
	    content = this.engine.getPureText(this.engine.getPage(topic))
		    + content;
	    WikiContext context = new WikiContext(this.engine, this.engine
		    .getPage(topic));
	    this.engine.saveText(context, content);
	    this.engine.updateReferences(this.engine.getPage(topic));
	    this.engine.getSearchManager().reindexPage(
		    this.engine.getPage(topic));

	} catch (ProviderException e) {
	    return null;
	} catch (WikiException e1) {
	    return null;
	}

	return this.engine.getPureText(this.engine.getPage(topic));
    }

    @Override
    public String getArticleSource(String name) {
	if (this.engine.getPage(name) == null)
	    return null;
	WikiContext context = new WikiContext(this.engine, this.engine
		.getPage(name));

	String pagedata = context.getEngine().getPureText(
		context.getPage().getName(), context.getPage().getVersion());
	return pagedata;
    }

    @Override
    public String getBaseUrl() {
	return engine.getBaseURL();
    }

    /**
     * Checks if the current user has the rights to edit the given page.
     * IReturns TRUE if the user has editing permission, otherwise FALSE and
     * editing of the page is denied.
     * 
     * @param articlename
     */
    @Override
    public boolean userCanEditPage(String articlename) {
	WikiPage page = new WikiPage(engine, articlename);
	WikiContext context = new WikiContext(this.engine, this.engine
		.getPage(articlename));
	AuthorizationManager authmgr = engine.getAuthorizationManager();
	PagePermission pp = PermissionFactory.getPagePermission(page, "edit");

	return authmgr.checkPermission(context.getWikiSession(), pp);
    }

    /**
     * Checks if the current user has the rights to edit the given page.
     * IReturns TRUE if the user has editing permission, otherwise FALSE and
     * editing of the page is denied.
     * 
     * @param articlename
     * @param r
     *            HttpRequest
     */
    @Override
    public boolean userCanEditPage(String articlename, HttpServletRequest r) {
	WikiPage page = new WikiPage(engine, articlename);
	WikiContext context = new WikiContext(this.engine, r, this.engine
		.getPage(articlename));

	AuthorizationManager authmgr = engine.getAuthorizationManager();
	PagePermission pp = PermissionFactory.getPagePermission(page, "edit");

	return authmgr.checkPermission(context.getWikiSession(), pp);
    }

    /**
     * Checks if the current page has an access lock. If TRUE no user other then
     * the lock owner can edit the page. If FALSE the current page has no lock
     * and can be edited by anyone.
     * 
     * @param articlename
     */
    @Override
    public boolean isPageLocked(String articlename) {
	PageManager mgr = engine.getPageManager();
	WikiPage page = new WikiPage(engine, articlename);
	if (mgr.getCurrentLock(page) == null)
	    return false;
	else
	    return true;
    }

    /**
     * Checks if the current page has been locked by the current user. Returns
     * TRUE if yes, FALSE otherwise.
     * 
     * @param articlename
     * @param user
     * @return
     */
    @Override
    public boolean isPageLockedCurrentUser(String articlename, String user) {
	PageManager mgr = engine.getPageManager();
	WikiPage page = new WikiPage(engine, articlename);
	PageLock lock = mgr.getCurrentLock(page);

	if (lock == null)
	    return false;

	if (lock.getLocker().equals(user)) {
	    return true;
	}
	return false;
    }

    /**
     * Locks a page in the WIKI so no other user can edit the page.
     * 
     * @param articlename
     */
    @Override
    public boolean setPageLocked(String articlename, String user) {
	PageManager mgr = engine.getPageManager();
	WikiPage page = new WikiPage(engine, articlename);
	PageLock lock = mgr.lockPage(page, user);

	if (lock != null)
	    return true;
	return false;
    }

    /**
     * Removes a page lock from a certain page in the WIKI so other users can
     * edit the page.
     * 
     * @param articlename
     */
    @Override
    public void undoPageLocked(String articlename) {
	PageManager mgr = engine.getPageManager();
	WikiPage page = new WikiPage(engine, articlename);

	if (isPageLocked(articlename)) {
	    PageLock lock = mgr.getCurrentLock(page);
	    mgr.unlockPage(lock);
	}
    }

    @Override
    public String getRealPath() {

	return context.getContextPath();
    }

    public Locale getLocale(HttpServletRequest request) {

	WikiContext wikiContext = new WikiContext(this.engine, request,
		this.engine.getPage("Main"));

	return Preferences.getLocale(wikiContext);
    }

    public Locale getLocale() {

	WikiContext wikiContext = new WikiContext(this.engine, this.engine
		.getPage("Main"));

	return Preferences.getLocale(wikiContext);
    }

    @Override
    @Deprecated // why should this be in the jspwiki-dependant project?? 
    // how should knowwe call this not knowing the class SearchResultImpl nor its interface???
    public Collection findPages(String query) {
	ArrayList<SearchResult> result = new ArrayList<SearchResult>();
	if (query.contains("#")) {
	    return null;
	}

	for (GenericSearchResult cur : TaggingMangler.getInstance()
		.searchPages(query.trim())) {
	    WikiPage page = this.engine.getPage(cur.getPagename());
	    result.add(new SearchResultImpl(page, cur.getContexts(), cur
		    .getScore()));
	}

	return result;
    }

    @Override
    public List<String> getAttachmentFilenamesForPage(String pageName) {
	try {
	    List<String> attachmentList = new LinkedList<String>();

	    Collection<Attachment> attList = this.engine.getAttachmentManager()
		    .getAllAttachments();

	    // This is damn inefficient - How can I grab all Attachment for a
	    // specific page???
	    for (Attachment p : attList) {

		if (p.getParentName().equals(pageName)) {
		    attachmentList.add(p.getFileName());
		}

	    }
	    return attachmentList;
	} catch (ProviderException e) {
	    return null;
	}
    }

    @Override
    public String createWikiLink(String articleName, String linkText) {
	String string = (linkText != null && !linkText.equals("")) ? "["
		+ linkText + "|" : "[";

	return string + articleName + "]";
    }

    @Override
    public Map<String, Integer> getVersionCounts() {
	HashMap<String, Integer> result = new HashMap<String, Integer>();
	PageManager pm = engine.getPageManager();
	try {
	    for (Object pageObj : pm.getAllPages()) {
		String pageName = ((WikiPage) pageObj).getName();
		List versionHistory = pm.getVersionHistory(pageName);
		int versionNumber = versionHistory.size();
		result.put(pageName, versionNumber);
	    }
	} catch (ProviderException e) {
	    e.printStackTrace();
	}
	return result;
    }

}
