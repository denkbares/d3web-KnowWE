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

package de.d3web.we.plugin.forum;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ForumRenderer extends KnowWEDomRenderer {

	private static boolean sortUpwards = ResourceBundle.getBundle("Forum_config").getString(
			"upwards").equals("true");

	public static String getDate() {

		Date d = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat(
				ResourceBundle.getBundle("Forum_config").getString("timeformat"));
		return fmt.format(d);
	}

	public static void setSortUpwards(boolean sortUp) {
		sortUpwards = sortUp;
	}

	public static void setSortUpwards(String sortParameter) {
		if (sortParameter == null) { // sort-parameter doesn't exist

			sortUpwards = ResourceBundle.getBundle("Forum_config").getString("upwards").equals(
					"true");

		}
		else if (sortParameter.equals("up")) {
			sortUpwards = true;
		}
		else if (sortParameter.equals("down")) {
			sortUpwards = false;

		}
		else { // sort-parameter is set in the wrong way

			sortUpwards = ResourceBundle.getBundle("Forum_config").getString("upwards").equals(
					"true");
		}
	}

	public static boolean getSortUpwards() {
		return sortUpwards;
	}

	public static void sortUpwards() {
		setSortUpwards(true);
	}

	public static void sortDownwards() {
		setSortUpwards(false);
	}

	private static String maskHTML(String s) {
		return KnowWEEnvironment.maskHTML(s);
	}

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {

		// load css-file:
		string.append(maskHTML("<link rel=stylesheet type=text/css href=KnowWEExtension/css/forum.css>\n"));

		// load javascript-file:
		string.append(maskHTML("<script type=text/javascript src=KnowWEExtension/scripts/ForumPlugin.js></script>\n"));

		String topic = sec.getTitle();

		ResourceBundle rb = ForumModule.getForumBundle(user);

		Map<String, String> forumMap = AbstractXMLObjectType.getAttributeMapFor(sec);

		// load sort-parameter from URL:
		setSortUpwards(user.getUrlParameterMap().get("sort"));

		// back link:
		String link = forumMap.get("ref");
		if (link != null) string.append(maskHTML("<a href=" + link + "><< back</a><br><br>\n"));

		String title = forumMap.get("name");
		// title used for pagename?
		if (title != null && !topic.equals(title)) {
			string.append(maskHTML("<h2>" + title + "</h2><br><hr>\n"));
		}

		List<Section> contentSectionList = new ArrayList<Section>();
		sec.findSuccessorsOfType(ForumBox.class, contentSectionList);

		if (!contentSectionList.isEmpty()) {

			// add first-comment
			Section section0 = contentSectionList.get(0);
			section0.getObjectType().getRenderer().render(article, section0, user, string);

			boolean canEditPage = false;
			if (user.getUsername() != "Guest") { // causes endless loop

				// check edit-permission
				canEditPage = KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(
						topic, user.getHttpRequest());

			}

			string.append(maskHTML("<table width=95% border=0>\n"));
			string.append(maskHTML("<tr><th align=left></th><th align=right width=150>\n"));

			// sort posts up- or downwards
			string.append(maskHTML("<a href=Wiki.jsp?page=" + topic.replace(" ", "+")
					+ "&sort=up title='" + rb.getString("Forum.sort.up")
					+ "'><img src=KnowWEExtension/images/ct_up.gif title='"
					+ rb.getString("Forum.sort.up") + "'></a>\n"));
			string.append(maskHTML("<a href=Wiki.jsp?page=" + topic.replace(" ", "+")
					+ "&sort=down title='" + rb.getString("Forum.sort.down")
					+ "'><img src=KnowWEExtension/images/ct_down.gif title='"
					+ rb.getString("Forum.sort.down") + "'></a>\n</th></tr>\n"));

			if (canEditPage) {
				string.append(maskHTML("<tr><td colspan=2><p align=right><textarea id=text name=text cols=68 rows=8></textarea><br>\n"));
				string.append(maskHTML("<input id=topic type=hidden name=topic value='"
						+ topic.replace(" ", "+") + "'></p>"));
				string.append(maskHTML("<div align=right><div class=forumbutton onclick=saveForumBox()>"
						+ rb.getString("Forum.button.postMessage") + "</div></div></td></tr>"));
				string.append(maskHTML("</table><hr><br>\n"));
			}
			else {
				string.append(maskHTML("</table><hr><br>\n"));
			}

			// render posts
			if (sortUpwards) {
				for (int i = 1; i < contentSectionList.size(); i++) {
					Section sectionI = contentSectionList.get(i);
					sectionI.getObjectType().getRenderer().render(article, sectionI, user, string);
				}
				string.append(maskHTML("<div id=newBox></div>"));
			}
			else {
				string.append(maskHTML("<div id=newBox></div>"));
				for (int i = contentSectionList.size() - 1; i > 0; i--) {
					Section sectionI = contentSectionList.get(i);
					sectionI.getObjectType().getRenderer().render(article, sectionI, user, string);
				}
			}

		}
	}
}
