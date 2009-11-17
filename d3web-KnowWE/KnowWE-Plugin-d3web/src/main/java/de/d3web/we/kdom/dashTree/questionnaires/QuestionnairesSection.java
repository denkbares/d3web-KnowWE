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

package de.d3web.we.kdom.dashTree.questionnaires;

import java.io.StringReader;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.dashtree.QuestionnaireBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.we.core.KnowWEParseResult;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;

public class QuestionnairesSection extends AbstractKopicSection {

	public static final String TAG = "Questionnaires-section";

	public QuestionnairesSection() {
		super(TAG);
	}

	@Override
	protected void init() {
		childrenTypes.add(new QuestionnairesContent());
		subtreeHandler.add(new QuestionnairesSubTreeHandler());
	}

	private class QuestionnairesSubTreeHandler extends D3webReviseSubTreeHandler {
	
		@Override
		public void reviseSubtree(KnowWEArticle article, Section s) {
			
			KnowledgeBaseManagement kbm = getKBM(article, s);
			
			if (kbm != null) {
				
			
				Section content = ((AbstractKopicSection) s.getObjectType()).getContentChild(s);
				if (content != null) {
					List<de.d3web.report.Message> messages = QuestionnaireBuilder
					.parse(new StringReader(removeTextIncludeTags(content.getOriginalText())), new SingleKBMIDObjectManager(kbm));
					
					storeMessages(s,messages);
					Report ruleRep = new Report();
					for (Message messageKnOffice : messages) {
						ruleRep.add(messageKnOffice);
					}
					KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
							.getTitle(), removeTextIncludeTags(s.getOriginalText()));
					s.getArticle().getReport().addReport(result);				
				}
			}
		}
	}
}
