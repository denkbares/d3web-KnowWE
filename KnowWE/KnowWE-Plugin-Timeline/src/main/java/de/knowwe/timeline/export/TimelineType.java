/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.timeline.export;

import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.utilities.Triple;
import de.d3web.testcase.model.TestCase;
import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.timeline.IDataProvider;
import de.knowwe.timeline.Query;
import de.knowwe.timeline.TestCaseDataProvider;
import de.knowwe.timeline.export.TimelineContentType.TimelineLine;
import de.knowwe.timeline.parser.ParseException;
import de.knowwe.timeline.parser.Token;
import de.knowwe.timeline.serialization.TimelineDrawer;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class TimelineType extends DefaultMarkupType {
	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("Timeline");
		m.addAnnotation(KnowledgeBaseType.ANNOTATION_COMPILE, false);
		m.addAnnotationRenderer(KnowledgeBaseType.ANNOTATION_COMPILE,
				StyleRenderer.ANNOTATION);
		m.addContentType(new TimelineContentType());
	}

	public String drawTimeline(Section<?> section, Triple<TestCaseProvider, Section<?>, Article> provider, TestCase testCase, KnowledgeBase kb) {
		IDataProvider dataProvider = new TestCaseDataProvider(testCase, kb);
		List<Section<TimelineLine>> lines = Sections.findSuccessorsOfType(
				section, TimelineContentType.TimelineLine.class);
		TimelineDrawer drawer = new TimelineDrawer(provider.getB().getID(), dataProvider);
		for (Section<TimelineLine> line : lines) {
			Query query;
			try {
				query = new Query(line.getText());
				drawer.addQuery(query);
			}
			catch (ParseException e) {
				Token tok = e.currentToken.next;

				drawer.addError("<span class='error' style='white-space: pre-wrap;'><b>Parse Exeption:</b> Unexpected '"
						+ tok.image + "' in query '" + line.getText().trim() + "'</span>");
			}
		}
		return drawer.getJSON();
	}

	public TimelineType() {
		super(m);
		setIgnorePackageCompile(true);
		setRenderer(new TimelineRenderer());
	}
	
	public TimelineType(DefaultMarkup m) {
		super(m);
	}
}
