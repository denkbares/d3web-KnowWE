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
import java.util.List;

import de.d3web.core.utilities.Triple;
import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.TestCaseProvider;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class TimelineRenderer extends DefaultMarkupRenderer {


	@Override
	protected void renderContents(Section<?> section, UserContext user,
			RenderResult string) {
		// why check this?
		if (user == null || user.getSession() == null) {
			return;
		}

		List<Triple<TestCaseProvider, Section<?>, Article>> providers = getTestCaseProviders(section);
		if (providers.isEmpty()) {
			renderNoTestCasesFoundMessage(section, string);
			return;
		}

		string.appendHtml("<div class='errors'></div>");
		TestCaseSelectionRenderer tcsr = new TestCaseSelectionRenderer(section,
				user, providers);
		tcsr.render(string);

		string.appendHtml("<div class=\"toolSeparator\"></div><div class='toolBar'></div>");
		string.appendHtml("<div class=\"timelineHolder\" style=\"height: 250px; border: 1px solid #aaa\"></div>");
	}


	public static List<Triple<TestCaseProvider, Section<?>, Article>> getTestCaseProviders(
			Section<?> section) {
		String[] kbpackages = getPackages(section);
		String web = section.getWeb();
		return de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(
				web, kbpackages);
	}
	
	private static String[] getPackages(Section<?> section) {
		return DefaultMarkupType.getPackages(section,
				KnowledgeBaseType.ANNOTATION_COMPILE);
	}

	private void renderNoTestCasesFoundMessage(Section<?> section,
			RenderResult string) {
		StringBuilder message = new StringBuilder();
		message.append("No test cases found in the packages: ");
		String[] packages = getPackages(section);
		buildCommaSeperatedList(message, packages);
		DefaultMarkupRenderer.renderMessagesOfType(Type.WARNING,
				Arrays.asList(Messages.warning(message.toString())), string);
	}

	private void buildCommaSeperatedList(StringBuilder output,
			String[] elements) {
		boolean first = true;
		for (String s : elements) {
			if (!first) {
				output.append(", ");
			}
			output.append(s);
			first = false;
		}
	}
}
