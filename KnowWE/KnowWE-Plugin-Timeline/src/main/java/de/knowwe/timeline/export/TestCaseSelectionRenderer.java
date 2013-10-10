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
package de.knowwe.timeline.export;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.Cookie;

import de.d3web.core.utilities.Triple;
import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.testcases.TestCasePlayerType;
import de.knowwe.testcases.TestCaseProvider;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
class TestCaseSelectionRenderer {
	private static String SELECTOR_KEY = "selectedValue";
	Section<?> section;
	UserContext user;
	List<Triple<TestCaseProvider, Section<?>, Article>> providers;

	public TestCaseSelectionRenderer(Section<?> section, UserContext user,
			List<Triple<TestCaseProvider, Section<?>, Article>> providers) {
		this.section = section;
		this.user = user;
		this.providers = providers;
	}

	public void render(RenderResult string) {

		String selectedID = getSelectedId();

		generateHeader(string);
		boolean unique = areNamesUnique();

		boolean found = false;
		for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {
			if (triple.getA().getTestCase() == null)
				continue;

			found = true;

			String id = triple.getC().getTitle() + "/"
					+ triple.getA().getName();

			String displayedID = (unique) ? triple.getA().getName() : id;
			generateOption(string, selectedID, id, displayedID);
		}

		generateFooter(string);
		if (!found) {
			generateNoTestCaseGeneratedWarning(string);
		}
	}

	private String getSelectedId() {
		if (user == null)
			return "";

		// String key = generateSelectedTestCaseCookieKey(section);
		// TODO: generate cookie key
		String key = "selectedTimeline1";

		String selectedID = "";
		for (Cookie cookie : user.getRequest().getCookies()) {
			if (cookie.getName().equals(key)) {
				selectedID = Strings.decodeURL(cookie.getValue());
			}
		}
		return selectedID;
	}

	private void generateNoTestCaseGeneratedWarning(RenderResult string) {
		DefaultMarkupRenderer
				.renderMessagesOfType(
						Type.WARNING,
						Arrays.asList(Messages
								.warning("There are testcase sections in the specified packages, but none of them generates a testcase.")),
						string);
	}

	private boolean areNamesUnique() {
		Set<String> ids = new HashSet<String>();
		boolean unique = true;
		for (Triple<TestCaseProvider, Section<?>, Article> triple : providers) {
			unique &= ids.add(triple.getA().getName());
		}
		return unique;
	}

	private void generateHeader(RenderResult result) {
		result.appendHtml("<span class='fillText'>Case </span>"
				+ "<select class='testCaseSelector'>");
	}

	private void generateOption(RenderResult result, String selectedID, String id,
			String displayedID) {
		result.appendHtml("<option value='").appendHtml(id).appendHtml("'");
		if (id.equals(selectedID))
 result.appendHtml("selected='selected'");
		result.appendHtml(">").appendHtml(displayedID).appendHtml("</option>");
	}

	private void generateFooter(RenderResult result) {
		result.appendHtml("</select>");
	}

	private String generateSelectedTestCaseCookieKey() {
		int i = 1;
		List<Section<TestCasePlayerType>> sections = Sections
				.findSuccessorsOfType(section.getArticle().getRootSection(),
						TestCasePlayerType.class);
		Section<TestCasePlayerType> testCasePlayerTypeSection = Sections
				.findAncestorOfExactType(section, TestCasePlayerType.class);
		for (Section<TestCasePlayerType> s : new TreeSet<Section<TestCasePlayerType>>(
				sections)) {
			if (testCasePlayerTypeSection.equals(s)) {
				break;
			} else {
				i++;
			}
		}
		return SELECTOR_KEY + "_" + Strings.encodeURL(section.getTitle()) + i;
	}

}