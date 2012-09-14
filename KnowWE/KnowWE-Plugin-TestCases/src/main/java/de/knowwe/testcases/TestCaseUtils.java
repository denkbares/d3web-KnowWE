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
package de.knowwe.testcases;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.d3web.core.utilities.Triple;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.09.2012
 */
public class TestCaseUtils {

	public static List<Triple<TestCaseProvider, Section<?>, Article>> getTestCaseProviders(String[] kbpackages, String web) {
		Environment env = Environment.getInstance();
		PackageManager packageManager = env.getPackageManager(web);
		ArticleManager articleManager = env.getArticleManager(web);
		List<Triple<TestCaseProvider, Section<?>, Article>> providers = new LinkedList<Triple<TestCaseProvider, Section<?>, Article>>();
		for (String kbpackage : kbpackages) {
			List<Section<?>> sectionsInPackage = packageManager.getSectionsOfPackage(kbpackage);
			Set<String> articlesReferringTo = packageManager.getCompilingArticles(kbpackage);
			for (String masterTitle : articlesReferringTo) {
				Article masterArticle = articleManager.getArticle(masterTitle);
				for (Section<?> packageSections : sectionsInPackage) {
					TestCaseProviderStorage testCaseProviderStorage =
							(TestCaseProviderStorage) packageSections.getSectionStore().getObject(
									masterArticle,
									TestCaseProviderStorage.KEY);
					if (testCaseProviderStorage != null) {
						for (TestCaseProvider testCaseProvider : testCaseProviderStorage.getTestCaseProviders()) {
							providers.add(new Triple<TestCaseProvider, Section<?>, Article>(
									testCaseProvider,
									packageSections, masterArticle));
						}
					}
				}
			}
		}
		return providers;
	}

}
