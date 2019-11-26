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

import de.d3web.we.ci4ke.dashboard.rendering.ObjectNameRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.testcases.ProviderTriple;
import de.knowwe.testcases.TestCaseUtils;

/**
 * Renders a link to the TestCase with the given name.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 25.09.2012
 */
public class TestCaseRenderer implements ObjectNameRenderer {

	@Override
	public void render(UserContext context, String objectName, RenderResult result) {
		// this is pretty slow... but since it is only needed if TestCaseTests
		// fail and there aren't normally many, it's ok for now
		Set<String> allPackageNames = KnowWEUtils.getPackageManager(context.getWeb()).getAllPackageNames();
		List<ProviderTriple> testCaseProviders = TestCaseUtils.getTestCaseProviders(
				context.getWeb(), allPackageNames.toArray(new String[allPackageNames.size()]));
		for (ProviderTriple triple : testCaseProviders) {
			if (triple.getA().getName().equals(objectName)) {
				result.appendHtml("<a href='"
						+ KnowWEUtils.getURLLink(triple.getB())
						+ "'>");
				result.append(objectName);
				result.appendHtml("</a>");
				return;
			}
		}
		result.append(objectName);
	}
}
