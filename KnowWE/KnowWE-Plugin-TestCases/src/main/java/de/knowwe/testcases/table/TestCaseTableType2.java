/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.testcases.table;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.defaultMarkup.AnnotationRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.testcases.prefix.PrefixTestCaseRenderer;
import de.knowwe.testcases.prefix.PrefixedTestCaseProvider;

/**
 * @author Reinhard Hatko
 * @created 18.01.2011
 */
public class TestCaseTableType2 extends DefaultMarkupType {

	private static DefaultMarkup MARKUP = null;
	public static String NAME = "name";

	static {
		MARKUP = new DefaultMarkup("TestCaseTable");
		MARKUP.addContentType(new Table());
		MARKUP.addContentType(new AnonymousType("WhiteSpace", AllTextFinder.getInstance(), NothingRenderer.getInstance()));
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addAnnotation(NAME, false);
		MARKUP.addAnnotationRenderer(NAME, new AnnotationRenderer("Name: "));
		MARKUP.addAnnotation(PrefixedTestCaseProvider.PREFIX_ANNOTATION_NAME, false);
		MARKUP.addAnnotationRenderer(PrefixedTestCaseProvider.PREFIX_ANNOTATION_NAME, new AnnotationRenderer("Prefix: "));
	}

	public TestCaseTableType2() {
		super(MARKUP);
		this.setRenderer(new PrefixTestCaseRenderer(this.getRenderer()));
	}
}
