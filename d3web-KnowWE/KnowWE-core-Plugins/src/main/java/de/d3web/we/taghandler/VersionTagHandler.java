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

package de.d3web.we.taghandler;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * A tag handler displaying KnowWE build metadata from
 * WEB-INF/classes/metadata.properties
 * 
 * @author Alex Legler
 */
public class VersionTagHandler extends AbstractTagHandler {

	public VersionTagHandler() {
		super("version");
	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin " + getTagName()
				+ "( = &lt;chuck|buildnumber|buildtag|buildtype&gt;)" + "}]";
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {

		StringBuffer html = new StringBuffer();
		ResourceBundle rb = ResourceBundle.getBundle("metadata");

		String v = values.get("version");
		if (v == null || v.isEmpty()) {
			html.append("KnowWE ");
			html.append(rb.getString("build.time"));
		}
		else if (v.equals("chuck")) {
			try {
				html.append(rb.getString("build.chuck"));
			}
			catch (MissingResourceException e) {
				html.append("no Chuck Norris line");
			}
		}
		else if (v.equals("buildnumber")) {
			try {
				html.append(rb.getString("build.number"));
			}
			catch (MissingResourceException e) {
				html.append("no build number");
			}
		}
		else if (v.equals("buildtag")) {
			try {
				html.append(rb.getString("build.tag"));
			}
			catch (MissingResourceException e) {
				html.append("no build tag");
			}
		}
		else if (v.equals("buildtype")) {
			try {
				html.append(rb.getString("build.type"));
			}
			catch (MissingResourceException e) {
				html.append("no build type");
			}
		}
		else {
			html.append("Invalid build metadata type. Valid types are: chuck, buildnumber, buildtag and buildtype.");
		}

		return html.toString();
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString(
				"KnowWE.Version.description");
	}

}
