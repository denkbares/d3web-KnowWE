/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

import de.d3web.we.testcase.renderer.TestCaseRenderer;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.objectinfo.TermIsHiddenInCompositeEdit;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.prefix.PrefixTestCaseRenderer;
import de.knowwe.testcases.prefix.PrefixedTestCaseProvider;

/**
 * TestCaseType for defining test cases in wiki markup.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created 18/10/2010
 */
public class TestCaseType extends DefaultMarkupType implements TermIsHiddenInCompositeEdit {

	public static final String TESTCASEKEY = "TestCaseType_Testsuite";
	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("TestCase");
		m.addContentType(new TestCaseContent());
		m.addAnnotation(PrefixedTestCaseProvider.PREFIX_ANNOTATION_NAME, false);
		PackageManager.addPackageAnnotation(m);
	}

	public TestCaseType() {
		super(m);
		setRenderer(new PrefixTestCaseRenderer(new TestCaseRenderer()));
	}

}
