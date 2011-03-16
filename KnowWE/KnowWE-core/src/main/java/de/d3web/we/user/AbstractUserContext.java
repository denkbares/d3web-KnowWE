/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.d3web.we.user;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.utils.KnowWEUtils;

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
	 * Returns the name of the current user. The result of this method can be
	 * null when the method is called within actions called by the
	 * @link{ActionServlet}.
	 *
	 * @created 04.03.2011
	 * @return the user name
	 */
	@Override
	public String getUserName() {
		return this.getParameter(KnowWEAttributes.USER);
	}

	/**
	 * Returns the topic of the article the user is currently visiting. The
	 * result of this method can be null when the method is called within
	 * actions called by the @link{ActionServlet}.
	 *
	 * @created 04.03.2011
	 * @return the article's topic
	 */
	@Override
	public String getTopic() {
		String page = this.getParameter(KnowWEAttributes.TOPIC);
		if (page == null) {
			page = this.getParameter("page");
		}
		return KnowWEUtils.urldecode(page);
	}

	/**
	 * Returns the web of the user's is currently visiting. It is the web the
	 * article belongs to. The result of this method can be null when the method
	 * is called within actions called by the @link{ActionServlet}.
	 * 
	 * @created 04.03.2011
	 * @return the article's web
	 */
	@Override
	public String getWeb() {
		return this.getParameter(KnowWEAttributes.WEB);
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

}
