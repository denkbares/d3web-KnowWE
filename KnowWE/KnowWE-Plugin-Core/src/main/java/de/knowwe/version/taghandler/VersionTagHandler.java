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

package de.knowwe.version.taghandler;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;

/**
 * A tag handler displaying KnowWE build metadata from
 * WEB-INF/classes/metadata.properties
 * 
 * @author Alex Legler
 */
public class VersionTagHandler extends AbstractHTMLTagHandler {

	private static ResourceBundle rb;
	static {
		try {
			rb = ResourceBundle.getBundle("metadata");
		}
		catch (MissingResourceException e) {
			rb = null;
		}
	}

	public VersionTagHandler() {
		super("version");
	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin " + getTagName()
				+ "( = \u00ABchuck|buildnumber|buildtag|buildtype\u00BB)" + "}]";
	}

	@Override
	public void renderHTML(String web, String topic, UserContext user, Map<String, String> values, RenderResult result) {

		StringBuilder html = new StringBuilder();
		if (rb == null) {
			result.append("no build metadata file found");
			return;
		}

		String v = values.get("version");
		if (v == null || v.isEmpty()) {
			html.append("KnowWE ");
			html.append(getBuildTime());
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
			html.append(getBuildNumber());
		}
		else if (v.equals("buildtag")) {
			html.append(getBuildTag());
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

		result.appendHtml(html.toString());
	}

	public static String getBuildNumber() {
		try {
			return rb.getString("build.number");
		}
		catch (MissingResourceException e) {
			return "no build number";
		}
		catch (NullPointerException e) {
			return "no build metadata file found";
		}
	}

	public static String getBuildTime() {
		try {
			return rb.getString("build.time");
		}
		catch (MissingResourceException e) {
			return "no build time";
		}
		catch (NullPointerException e) {
			return "no build metadata file found";
		}
	}

	public static String getBuildTag() {
		try {
			return rb.getString("build.tag");
		}
		catch (MissingResourceException e) {
			return "no build tag";
		}
		catch (NullPointerException e) {
			return "no build metadata file found";
		}
	}

	public static boolean hasVersionInfo() {
		return rb != null;
	}

	@Override
	public String getDescription(UserContext user) {
		return Messages.getMessageBundle(user).getString(
				"KnowWE.Version.description");
	}

}
