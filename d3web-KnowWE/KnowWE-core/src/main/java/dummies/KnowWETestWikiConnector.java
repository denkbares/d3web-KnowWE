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

package dummies;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;


public class KnowWETestWikiConnector implements KnowWEWikiConnector {

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
	public String getAttachmentPath(String JarName) {
		// TODO Auto-generated method stub
		return "some-path";
	}

	@Override
	public LinkedList<String> getAttachments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBaseUrl() {
		return "some-base-URL";
	}



	@Override
	public String getPagePath() {
		// TODO Auto-generated method stub
		return "some-path";
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
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getLocale(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection findPages(String query) {
		// TODO Auto-generated method stub
		return null;
	}

}
