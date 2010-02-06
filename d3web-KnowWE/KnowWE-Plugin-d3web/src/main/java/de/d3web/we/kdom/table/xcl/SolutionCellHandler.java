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
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class SolutionCellHandler implements ReviseSubTreeHandler {

	public static final String KEY_REPORT = "report_message";

	@Override
	public void reviseSubtree(KnowWEArticle article, Section s) {
		KnowledgeBaseManagement mgn = D3webModule
				.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, s);
		
		if (mgn == null) {
			return;
		}
		
		SingleKBMIDObjectManager mgr = new SingleKBMIDObjectManager(mgn);

		String name = s.getOriginalText();
		name = name.replaceAll("__", "").trim();

		Diagnosis d = mgr.findDiagnosis(name);

		if (d == null) {
			Diagnosis newD = mgr.createDiagnosis(name, mgr.getKnowledgeBase()
					.getRootDiagnosis());
			if (newD != null) {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Created solution : " + name));
			} else {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Failed creating solution : " + name));
			}
		}
		// already there
		KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
				"Solution already defined: " + name));

	}

}
