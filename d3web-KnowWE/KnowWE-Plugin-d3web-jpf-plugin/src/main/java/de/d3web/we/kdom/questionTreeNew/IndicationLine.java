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

package de.d3web.we.kdom.questionTreeNew;


import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionnaireDef;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class IndicationLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new AllTextFinderTrimmed();

		QuestionnaireDef qc = new QuestionnaireDef();
		qc.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));
		qc.setSectionFinder(new AllTextFinderTrimmed());
		qc.addSubtreeHandler(new CreateIndication());
		this.childrenTypes.add(qc);
	}

	static class CreateIndication extends SubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			KnowledgeBaseManagement mgn = D3webModule
					.getKnowledgeRepresentationHandler(article.getWeb())
					.getKBM(article, this, s);

			Section<QuestionnaireDef> indicationSec = (s);

			// current DashTreeElement
			Section<DashTreeElement> element = KnowWEObjectTypeUtils
					.getAncestorOfType(s, new DashTreeElement());

			String name = indicationSec.get().getTermName(indicationSec);

			QContainer qc = mgn.findQContainer(name);

			if (qc != null) {
				String newRuleID = mgn.createRuleID();
				Condition cond = Utils.createCondition(DashTreeElement.getDashTreeAncestors(element));
				if (cond != null) {
					Rule r = RuleFactory.createIndicationRule(newRuleID, qc,
							cond);
					if (r != null) {
						return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(r.getClass()
								+ " : " + r.getId()));
					}

				}
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(Rule.class
						.getSimpleName()));
			} else {

				return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(name));
			}

		}

	}
}
