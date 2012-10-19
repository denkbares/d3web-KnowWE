/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.append;

import de.knowwe.core.Environment;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;
import de.knowwe.core.wikiConnector.WikiConnector;


/**
 * This handler appends some basic information about the page and the current
 * user to the page.
 * 
 * @author Reinhard Hatko
 * @created 18.10.2012
 */
public class PageInfoAppendHandler implements PageAppendHandler {

	@Override
	public String getDataToAppend(String topic, String web, UserContext user) {
		StringBuilder html = new StringBuilder();
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		int version = connector.getVersion(topic);
		long modDate = connector.getLastModifiedDate(topic, -1).getTime();
		String userName = user.getUserName();

		// username and topic can not contain special chars, so no masking
		// should be necessary
		html.append("<input type='hidden' id='knowWEInfoPageName' value='" + topic + "'>");
		html.append("<input type='hidden' id='knowWEInfoPageVersion' value='" + version + "'>");
		html.append("<input type='hidden' id='knowWEInfoPageDate' value='" + modDate + "'>");
		html.append("<input type='hidden' id='knowWEInfoUser' value='" + userName + "'>");
		return Strings.maskHTML(html.toString());
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
