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
package de.knowwe.testcases.tools;

import java.util.List;
import java.util.Set;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.TestCasePlayerRenderer;
import de.knowwe.testcases.TestCasePlayerType;
import de.knowwe.testcases.TestCaseProviderStorage;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * Provides a Tool creating a tool opening a player and loading one of the
 * TestCases of this section. This tool should only be used with markups, just
 * creating one TestCaseProvider, otherwise one provider is chosen randomly.
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 13.03.2012
 */
public class GoToPlayerToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Section<DefaultMarkupType> defaultMarkupSection;
		if (section.get() instanceof DefaultMarkupType) {
			defaultMarkupSection = Sections.cast(section, DefaultMarkupType.class);
		}
		else {
			defaultMarkupSection = Sections.findAncestorOfType(section,
					DefaultMarkupType.class);
		}
		String[] packages = DefaultMarkupType.getPackages(defaultMarkupSection);
		Environment env = Environment.getInstance();
		PackageManager packageManager = env.getPackageManager(section.getWeb());
		for (String kbpackage : packages) {
			Set<String> articles = packageManager.getCompilingArticles(kbpackage);
			// we need an article compiling the section
			if (articles.size() > 0) {
				String articleTitle = articles.iterator().next();
				TestCaseProviderStorage providerStorage = (TestCaseProviderStorage) section.getSectionStore().getObject(
						Environment.getInstance().getArticle(section.getWeb(), articleTitle),
						TestCaseProviderStorage.KEY);
				if ((providerStorage != null) && providerStorage.getTestCaseProviders().size() > 0) {
					String testcasename = providerStorage.getTestCaseProviders().iterator().next().getName();
					String value = articleTitle + "/" + testcasename;
					List<Section<?>> sectionsInPackage = packageManager.getSectionsOfPackage(kbpackage);
					for (Section<?> sectionInPackage : sectionsInPackage) {
						if (sectionInPackage.get() instanceof TestCasePlayerType) {
							return createTools(value, sectionInPackage);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return getTools(section, userContext).length > 0;
	}

	private Tool[] createTools(String value, Section<?> sectionInPackage) {
		String testCaseSection = TestCasePlayerRenderer.generateSelectedTestCaseCookieKey(DefaultMarkupType.getContentSection(sectionInPackage));
		return new Tool[] { new DefaultTool(
				null,
				"Show in Player",
				"Opens this test case in an associated test case player",
				"TestCasePlayer.change('"
						+ testCaseSection
						+ "', '" + value + "');"
						+ " window.location='"
						+ KnowWEUtils.getURLLink(sectionInPackage) + "';") };
	}

}
