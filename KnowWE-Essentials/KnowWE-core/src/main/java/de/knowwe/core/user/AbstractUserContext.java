/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.user;

import java.util.NoSuchElementException;

import com.denkbares.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Abstract UserContext implementation with standard implementations of some
 * methods for KnowWE.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created Mar 4, 2011
 */
public abstract class AbstractUserContext implements UserContext {

	protected final AuthenticationManager manager;

	public AbstractUserContext(AuthenticationManager manager) {
		this.manager = manager;
	}

	@Override
	public boolean userIsAsserted() {
		return manager.userIsAsserted();
	}

	@Override
	public boolean userIsAdmin() {
		return manager.userIsAdmin();
	}

	/**
	 * Returns the name of the current user.
	 *
	 * @return the user name
	 * @created 04.03.2011
	 */
	@Override
	public String getUserName() {
		return manager.getUserName();
	}

	@Override
	public String getTitle() {
		String page = this.getParameter(Attributes.TOPIC);
		if (page == null) {
			page = this.getParameter("page");
		}
		return page;
	}

	@Override
	public Article getArticle() {
		String title = getTitle();
		if (Strings.isNotBlank(title)) {
			return getArticleManager().getArticle(title);
		}
		else {
			String sectionId = getSectionId();
			if (Strings.isNotBlank(sectionId)) {
				return Sections.get(sectionId).getArticle();
			}
			else {
				throw new NoSuchElementException("Request is missing parameter to locate current Article");
			}
		}
	}

	private String getSectionId() {
		return this.getParameter(Attributes.SECTION_ID);
	}

	/**
	 * Returns the web of the user's is currently visiting. It is the web the
	 * article belongs to.
	 *
	 * @return the article's web
	 * @created 04.03.2011
	 */
	@Override
	public String getWeb() {
		String web = this.getParameter(Attributes.WEB);
		if (web == null) web = Environment.DEFAULT_WEB;
		return web;
	}

	@Override
	public String getParameter(String key) {
		return this.getParameters().get(key);
	}

	@Override
	public String getParameter(String key, String defaultValue) {
		return this.getParameters().get(key) != null
				? this.getParameters().get(key)
				: defaultValue;
	}

	@Override
	public ArticleManager getArticleManager() {
		return KnowWEUtils.getArticleManager(getWeb());
	}
}
