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

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParseResult;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

/**
 * TestSuiteType for defining test suites in wiki markup.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 18/10/2010
 */
public class TestSuiteType extends DefaultMarkupType {

	public static final String TESTSUITEKEY = "TestSuiteType_Testsuite";
	public static final String KBSOURCE = "KnowledgeBase";
	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("TestSuite");
		m.addContentType(new TestsuiteContent());
		m.addAnnotation(KBSOURCE, true);
		m.addAnnotation(KnowWEPackageManager.ATTRIBUTE_NAME, false);
	}

	public TestSuiteType() {
		super(m);
		this.addSubtreeHandler(Priority.LOWEST, new TestSuiteSubTreeHandler());
		setCustomRenderer(new TestSuiteRenderer());
	}

	private class TestSuiteSubTreeHandler extends D3webSubtreeHandler<TestSuiteType> {

		@Override
		public boolean isIgnoringPackageCompile() {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TestSuiteType> s) {

			KnowledgeBaseManagement kbm = loadKBM(article, s);

			if (kbm != null) {

				Section<TestsuiteContent> content = s.findSuccessor(TestsuiteContent.class);

				if (content != null) {

					Reader r = new StringReader(content.getOriginalText());
					IDObjectManagement idom = new SingleKBMIDObjectManager(kbm);
					TestsuiteBuilder builder = new TestsuiteBuilder("", idom);

					// Parsing
					List<de.d3web.report.Message> messages = builder.addKnowledge(r, idom, null);

					// Reporting
					storeMessages(article, s, this.getClass(), messages);
					Report testsuiteRep = new Report();

					for (Message messageKnOffice : messages) {
						testsuiteRep.add(messageKnOffice);
					}

					KnowWEParseResult result =
							new KnowWEParseResult(testsuiteRep, s.getTitle(), s.getOriginalText());

					s.getArticle().getReport().addReport(result);

					// Store test suite
					KnowWEUtils.storeObject(s.getArticle().getWeb(), s.getTitle(), s.getID(),
							TestSuiteType.TESTSUITEKEY, builder.getTestsuite());
				}
			}

			return Collections.emptyList();
		}

		/**
		 * Because the knowledge base is in another article we need this method.
		 */
		private KnowledgeBaseManagement loadKBM(KnowWEArticle a, Section<TestSuiteType> s) {
			// We need this for the JUNIT Tests!
			KnowledgeBaseManagement kbm = getKBM(a);

			// Normal behavior, because in general the test suite is in another
			// article
			if (kbm == null) {
				String source = getAnnotation(s, KBSOURCE);
				KnowWEArticle article = KnowWEEnvironment.getInstance().getArticle(a.getWeb(),
						source);
				kbm = getKBM(article);
			}

			return kbm;
		}

	}

}
