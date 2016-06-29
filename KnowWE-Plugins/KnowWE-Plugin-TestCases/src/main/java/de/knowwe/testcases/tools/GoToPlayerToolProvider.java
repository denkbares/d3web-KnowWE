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

import java.util.Collection;

import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.TestCasePlayerRenderer;
import de.knowwe.testcases.TestCasePlayerType;
import de.knowwe.testcases.TestCaseProviderStorage;
import de.knowwe.testcases.TestCaseUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

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
		D3webCompiler compiler = D3webUtils.getCompiler(section);
		TestCaseProviderStorage providerStorage = TestCaseUtils.getTestCaseProviderStorage(
				compiler, section);
		if ((providerStorage != null) && !providerStorage.getTestCaseProviders().isEmpty()) {
			String testcasename = providerStorage.getTestCaseProviders().iterator().next().getName();
			String value = compiler.getCompileSection().getTitle() + "/" + testcasename;
			Section<? extends PackageCompileType> compileSection = compiler.getCompileSection();
			String[] packagesToCompile = compileSection.get().getPackagesToCompile(compileSection);
			Collection<Section<?>> sectionsOfPackages = KnowWEUtils.getPackageManager(section).getSectionsOfPackage(
					packagesToCompile);
			for (Section<?> sectionInPackage : sectionsOfPackages) {
				if (sectionInPackage.get() instanceof TestCasePlayerType) {
					return createTools(value, sectionInPackage);
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
				Icon.OPENTESTCASE,
				"Show in Player",
				"Opens this test case in an associated test case player",
				"TestCasePlayer.change('"
						+ testCaseSection
						+ "', '" + value + "');"
						+ " window.location='"
						+ KnowWEUtils.getURLLink(sectionInPackage) + "';",
				Tool.CATEGORY_INFO) };
	}

}
