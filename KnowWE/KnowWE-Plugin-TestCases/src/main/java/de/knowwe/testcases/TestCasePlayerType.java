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

import de.d3web.we.knowledgebase.KnowledgeBaseType;
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
		MARKUP.addAnnotation(KnowledgeBaseType.ANNOTATION_COMPILE, false);
		MARKUP.addAnnotationRenderer(KnowledgeBaseType.ANNOTATION_COMPILE, StyleRenderer.ANNOTATION);
	}

	public TestCasePlayerType() {
		super(MARKUP);
		this.setIgnorePackageCompile(true);
		DefaultMarkupType.getContentType(this).setRenderer(
				new ReRenderSectionMarkerRenderer(new TestCasePlayerRenderer()));
	}

}
