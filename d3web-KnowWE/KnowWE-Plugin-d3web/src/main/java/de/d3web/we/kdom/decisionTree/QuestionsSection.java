/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.kdom.decisionTree;

import java.io.StringReader;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.decisiontree.D3DTBuilder;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.core.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;

public class QuestionsSection extends AbstractKopicSection {

	public static final String TAG = "Questions-section";

	public QuestionsSection() {
		super(TAG);
	}

	@Override
	protected void init() {
		childrenTypes.add(new QuestionsSectionContent());
		subtreeHandler.add(new QuestionsSectionSubTreeHandler());

	}
	
	private class QuestionsSectionSubTreeHandler extends D3webReviseSubTreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
	
			KnowledgeBaseManagement kbm = getKBM(article, s);
			
			if (kbm != null) {
				
				Section content = ((AbstractKopicSection) s.getObjectType()).getContentChild(s);
				if (content != null) {
	
					List<de.d3web.report.Message> messages = D3DTBuilder
							.parse(new StringReader(content.getOriginalText()), new SingleKBMIDObjectManager(kbm));
	
					storeMessages(article, s,messages);
					Report ruleRep = new Report();
					for (Message messageKnOffice : messages) {
						ruleRep.add(messageKnOffice);
					}
					KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
							.getTitle(), s.getOriginalText());
					s.getArticle().getReport().addReport(result);
				}
			}
			return null;
		}
	}

}
