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

import de.d3web.testcase.TestCaseUtils;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.defaultMarkup.AnnotationRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * DefaultMarkupType for SessionDebugger
 *
 * @author Markus Friedrich (denkbares GmbH)
 * @created 19.01.2012
 */
public class TestCasePlayerType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("TestCasePlayer");
		MARKUP.addAnnotation(PackageManager.COMPILE_ATTRIBUTE_NAME, false);
		MARKUP.addAnnotationRenderer(PackageManager.COMPILE_ATTRIBUTE_NAME, StyleRenderer.ANNOTATION);
		MARKUP.setAnnotationDeprecated(PackageManager.COMPILE_ATTRIBUTE_NAME);
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(TestCaseUtils.VALUE_OUT_OF_RANGE, false, "skip", "set");
		MARKUP.addAnnotationRenderer(TestCaseUtils.VALUE_OUT_OF_RANGE, new AnnotationRenderer("Values out of range: "));
	}

	public static boolean skipNumValueOutOfRange(Section<?> playerSection) {
		String ignoreAnnotation = DefaultMarkupType.getAnnotation(playerSection, TestCaseUtils.VALUE_OUT_OF_RANGE);
		return "skip".equals(ignoreAnnotation);
	}

	public TestCasePlayerType() {
		super(MARKUP);
		//noinspection ConstantConditions
		DefaultMarkupType.getContentType(this).setRenderer(
				new ReRenderSectionMarkerRenderer(new TestCasePlayerRenderer()));
		this.addCompileScript(new PackageRegistrationScript<TestCasePlayerType>() {

			@Override
			public void compile(PackageRegistrationCompiler compiler, Section<TestCasePlayerType> section) {
				for (String packageName : DefaultMarkupType.getPackages(section,
						PackageManager.COMPILE_ATTRIBUTE_NAME)) {
					compiler.getPackageManager().addSectionToPackage(section, packageName);
				}
			}

			@Override
			public void destroy(PackageRegistrationCompiler compiler, Section<TestCasePlayerType> section) {
				compiler.getPackageManager().removeSectionFromAllPackages(section);
			}
		});
	}

}
