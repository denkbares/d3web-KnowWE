/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.testcase.kdom;

import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.testcase.renderer.TestCaseRunnerRenderer;

/**
 * DefaultMarkup-Type for the test suite result. This type needs the obligatory
 * annotation "TestCase", that is the name of a KnowWEArticle containing a test
 * case.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 25/10/2010
 */
public class TestCaseRunnerType extends DefaultMarkupType {

	private static final String ANNOTATION_TESTCASE = "testcase";
	private static final String ANNOTATION_MODE = "mode";
	private static final String ANNOTATION_WAIT = "wait"; // the count of
															// milliseconds to
															// wait between the
															// rated test cases.

	public static final String MODE_DEBUG = "debug";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("TestCaseRunner");
		MARKUP.addAnnotation(ANNOTATION_TESTCASE, true);
		MARKUP.addAnnotation(ANNOTATION_MODE, false);
		MARKUP.addAnnotation(ANNOTATION_WAIT, false);
	}

	public TestCaseRunnerType() {
		super(MARKUP);
		this.setCustomRenderer(this.getRenderer());
		KnowWERessourceLoader.getInstance().add("testcase.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("testcase.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
	}

	public static String getText(Section<?> sec) {
		assert sec.get() instanceof TestCaseRunnerType;
		return DefaultMarkupType.getContent(sec);
	}

	public static String getTestCase(Section<?> section) {
		assert section.get() instanceof TestCaseRunnerType;
		return DefaultMarkupType.getAnnotation(section, ANNOTATION_TESTCASE);
	}

	public static String getMode(Section<?> section) {
		assert section.get() instanceof TestCaseRunnerType;
		return DefaultMarkupType.getAnnotation(section, ANNOTATION_MODE);
	}

	public static long getWait(Section<?> section) {
		assert section.get() instanceof TestCaseRunnerType;
		String waitStr = DefaultMarkupType.getAnnotation(section, ANNOTATION_MODE);
		return Long.getLong(waitStr).longValue();
	}

	@Override
	public KnowWEDomRenderer<TestCaseRunnerType> getRenderer() {
		return new TestCaseRunnerRenderer();
	}

}
