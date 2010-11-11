/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package dummies;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.wikiConnector.ConnectorAttachment;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

/**
 * Used in the tests. A WikiConnector is needed to initialize the
 * KnowWEEnvironment. The methods are empty.
 *
 * @author Johannes Dienst
 *
 */
public class KnowWETestWikiConnector implements KnowWEWikiConnector {

	/**
	 * This returns a path, that enables the use of this connector in tests of
	 * projects.
	 *
	 * THIS METHOD NEEDS TO BE UPDATED IF THIS CLASS IS MOVED TO ANOTHER
	 * PROJECT, PACKAGE ETC.
	 *
	 * @author Sebastian Furth
	 * @return relative Path to KnowWEExtensions
	 */
	public String getHackedPath() {
		String clazz = getClass().getName();
		String filename = clazz.substring(clazz.lastIndexOf(".") + 1) + ".class";
		// The absolute file path to the .class-File of THIS class!
		String filePathToThisClass = this.getClass().getResource(filename).getPath();
		// Delete the filename
		String path = filePathToThisClass.substring(0, filePathToThisClass.lastIndexOf("/"));
		// Delete the project .jar from the file (for Hudsons sake)
		path = path.replaceAll("KnowWE-core-0.2-SNAPSHOT.jar!", "");
		path = path.replaceAll("classes", "");
		path = path.replaceAll("dummies", "");
		// Now go up in the folder hierarchy and change to the KnowWE-folder
		String hackedPath = path + "/../../KnowWE/src/main/webapp/KnowWEExtension/";
		return hackedPath;
	}

	@Override
	public String appendContentToPage(String topic, String pageContent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createWikiPage(String topic, String newContent, String author) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean doesPageExist(String Topic) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public KnowWEActionDispatcher getActionDispatcher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getAllArticles(String web) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getArticleSource(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getArticleSource(String name, int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVersion(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getAttachmentPath(String JarName) {
		// TODO Auto-generated method stub
		return "some-path";
	}

	@Override
	public LinkedList<String> getJarAttachments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBaseUrl() {
		return "http://valid_base_url/";
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPageLocked(String articlename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPageLockedCurrentUser(String articlename, String user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveArticle(String name, String text, KnowWEParameterMap map) {
		KnowWEEnvironment.getInstance().getArticleManager(
				KnowWEEnvironment.DEFAULT_WEB).saveUpdatedArticle(
						KnowWEArticle.createArticle(text, name, KnowWEEnvironment.getInstance()
						.getRootType(), KnowWEEnvironment.DEFAULT_WEB));
		return true;
	}

	@Override
	public boolean saveArticle(String name, String text, KnowWEParameterMap map, boolean fullParse) {
		KnowWEEnvironment.getInstance().getArticleManager(
				KnowWEEnvironment.DEFAULT_WEB).saveUpdatedArticle(
						KnowWEArticle.createArticle(text, name, KnowWEEnvironment.getInstance()
								.getRootType(), KnowWEEnvironment.DEFAULT_WEB, fullParse));
		return true;
	}

	@Override
	public boolean setPageLocked(String articlename, String user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void undoPageLocked(String articlename) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean userCanEditPage(String articlename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean userCanEditPage(String articlename, HttpServletRequest r) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRealPath() {
		return "some-path";
	}

	@Override
	public Locale getLocale() {
		return Locale.CANADA_FRENCH;
	}

	@Override
	public Locale getLocale(HttpServletRequest request) {
		return Locale.CANADA_FRENCH;
	}

	@Override
	public List<String> getAttachmentFilenamesForPage(String pageName) {
		return new ArrayList<String>();
	}

	@Override
	public String createWikiLink(String articleName, String linkText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Integer> getVersionCounts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean storeAttachment(String wikiPage, File attachmentFile) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<ConnectorAttachment> getAttachments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSavePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String renderWikiSyntax(String pagedata, KnowWEParameterMap map) {
		return null;
	}

	@Override
	public boolean userIsMemberOfGroup(String username, String groupname, HttpServletRequest r) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAuthor(String name, int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getLastModifiedDate(String name, int version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, Date> getModificationHistory(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}
