/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke;

import java.util.List;
import java.util.Set;

import de.d3web.core.utilities.Triple;
import de.d3web.we.ci4ke.dashboard.rendering.ObjectNameRenderer;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;

/**
 * Renders a link to the TestCase with the given name.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 25.09.2012
 */
public class TestCaseRenderer implements ObjectNameRenderer {

	@Override
	public String render(String objectName) {
		// this is pretty slow... but since it is only needed if TestCaseTests
		// fail and there aren't normally many, it's ok for now
		String web = Environment.DEFAULT_WEB;
		Set<String> allPackageNames = KnowWEUtils.getPackageManager(web).getAllPackageNames();
		List<Triple<TestCaseProvider, Section<?>, Article>> testCaseProviders = TestCaseUtils.getTestCaseProviders(
				allPackageNames.toArray(new String[allPackageNames.size()]), web);
		for (Triple<TestCaseProvider, Section<?>, Article> triple : testCaseProviders) {
			if (triple.getA().getName().equals(objectName)) {
				String link = "<a href='"
						+ Strings.maskHTML(KnowWEUtils.getURLLink(triple.getB()))
						+ "'>" + objectName + "</a>";
				return link;
			}
		}
		return objectName;
	}
}
