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
package de.knowwe.testcases.stc;

import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.MatchingAttachmentsRenderer;
import de.knowwe.testcases.ProviderRefreshRenderer;
import de.knowwe.testcases.prefix.PrefixTestCaseRenderer;
import de.knowwe.testcases.prefix.PrefixedTestCaseProvider;

/**
 * Type for TestCaseSTC Markup
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 25.01.2012
 */
public class TestCaseSTCType extends DefaultMarkupType {

	public static final String ANNOTATION_FILE = "file";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("TestCaseSTC");
		MARKUP.addAnnotation(ANNOTATION_FILE, true);
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(PrefixedTestCaseProvider.PREFIX_ANNOTATION_NAME, false);
	}

	public TestCaseSTCType() {
		super(MARKUP);
		addCompileScript(Priority.LOW, new TestCaseSTCSubtreeHandler());
		this.setRenderer(new PrefixTestCaseRenderer(new ProviderRefreshRenderer()));
		DefaultMarkupType.getContentType(this).setRenderer(new MatchingAttachmentsRenderer());
	}

}
