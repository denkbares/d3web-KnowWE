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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.d3web.testcase.model.TestCase;
import de.d3web.testing.TestObjectContainer;
import de.d3web.testing.TestObjectProvider;
import de.d3web.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.testcases.ProviderTriple;
import de.knowwe.testcases.TestCaseProvider;

/**
 * Searches the wiki for {@link TestCase}s. The names of {@link TestCase}s are
 * checked against the given name parameter using regex.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 29.06.2012
 */
public class WikiTestCaseProvider implements TestObjectProvider {

	@Override
	public <T> List<TestObjectContainer<T>> getTestObjects(Class<T> clazz, String name) {

		if (clazz == null) {
			Log.warning("Class given to TestObjectProvider was 'null'");
			return Collections.emptyList();
		}
		if (!clazz.equals(TestCase.class)) {
			return Collections.emptyList();
		}
		List<TestObjectContainer<T>> result = new ArrayList<>();
		try {
			Pattern.compile(name);
		}
		catch (java.util.regex.PatternSyntaxException e) {
			return result;
		}
		String web = Environment.DEFAULT_WEB;
		Set<String> allPackageNames = KnowWEUtils.getPackageManager(web).getAllPackageNames();
		List<ProviderTriple> testCaseProviders = de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(
				web, allPackageNames.toArray(new String[allPackageNames.size()]));

		for (ProviderTriple triple : testCaseProviders) {
			TestCaseProvider provider = triple.getA();
			if (provider.getName().matches(name)) {
				result.add(new TestObjectContainer<>(provider.getName(),
						clazz.cast(provider.getTestCase())));
			}
		}

		return result;
	}

}
