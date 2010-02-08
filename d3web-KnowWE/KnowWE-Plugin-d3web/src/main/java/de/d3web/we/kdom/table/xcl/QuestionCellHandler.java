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

package de.d3web.we.kdom.table.xcl;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.util.D3webQuestionFactory;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.report.Message;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.KnowledgeUtils;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.utils.KnowWEUtils;

public class QuestionCellHandler implements ReviseSubTreeHandler {
	
	public static final String KEY_REPORT = "report_message";

	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
		KnowledgeBaseManagement mgn = D3webModule
				.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, s);
		
		if (mgn == null) {
			return null;
		}
		
		SingleKBMIDObjectManager mgr = new SingleKBMIDObjectManager(mgn);
		
		String text = s.getOriginalText();
		
		String type = KnowledgeUtils.getQuestionTypeFromDeclaration(text);
		String name = KnowledgeUtils.getQuestionNameFromDeclaration(text); 
		
		Question q = mgr.findQuestion(name);
		
		if(q == null) {
			//create question
			Question q2 = D3webQuestionFactory.createQuestion(name, type, mgr);
			
			if(q2 != null ) {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT , new Message("Created question "+type+" : "+name));
			}
			else {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT , new Message("Failed creating question "+type+" : "+name));
			}
			
			
		} else {
			//TODO: CHECK Type match!
			KnowWEUtils.storeSectionInfo(s, KEY_REPORT , new Message("Question already defined: "+name));

			
		}
		
		return null;

	}

}
