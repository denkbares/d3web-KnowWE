/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.testsuite;

import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

/**
 * DefaultMarkup-Type for the test suite result. This type needs the obligatory
 * annotation "TestSuite", that is the name of a KnowWEArticle containing a test
 * suite.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 25/10/2010
 */
public class TestSuiteResultType extends DefaultMarkupType {

	private static final String ANNOTATION_TESTSUITE = "testsuite";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("TestSuiteResult");
		MARKUP.addAnnotation(ANNOTATION_TESTSUITE, true);
	}

	public TestSuiteResultType() {
		super(MARKUP);
		this.setCustomRenderer(this.getRenderer());
		KnowWERessourceLoader.getInstance().add("testsuite.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("testsuite.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
	}

	public static String getText(Section<?> sec) {
		assert sec.getObjectType() instanceof TestSuiteResultType;
		return DefaultMarkupType.getContent(sec);
	}

	public static String getTestSuite(Section<?> section) {
		assert section.getObjectType() instanceof TestSuiteResultType;
		return DefaultMarkupType.getAnnotation(section, ANNOTATION_TESTSUITE);
	}

	@Override
	public KnowWEDomRenderer<TestSuiteResultType> getRenderer() {
		return new TestSuiteResultRenderer();
	}

}
