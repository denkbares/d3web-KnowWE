/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.d3web.we.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.knowwe.core.dashtree.DashTreeElement;
import de.knowwe.core.dashtree.DashTreeUtils;

/**
 * 
 * Abstract Type for the definition of questionnaires
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class QuestionnaireDefinition extends QASetDefinition<QContainer> {

	public QuestionnaireDefinition() {
		super(QContainer.class);
		addSubtreeHandler(Priority.HIGHEST, new CreateQuestionnaireHandler());
		setCustomRenderer(StyleRenderer.Questionaire);
		setOrderSensitive(true);
	}

	public abstract int getPosition(Section<QuestionnaireDefinition> s);

	static class CreateQuestionnaireHandler
			extends D3webSubtreeHandler<QuestionnaireDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<QuestionnaireDefinition> s) {

			String name = s.get().getTermName(s);

			if (!KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
					article, s)) {
				return new ArrayList<KDOMReportMessage>(0);
			}

			KnowledgeBase kb = getKB(article);

			NamedObject o = kb.getManager().searchQContainer(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(
						o.getClass().getSimpleName()));
			}
			else {
				Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
						.getFatherDashTreeElement(s);
				QASet parent = kb.getRootQASet();
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					Section<QuestionnaireDefinition> parentQclass =
								Sections.findSuccessor(dashTreeFather,
										QuestionnaireDefinition.class);
					if (parentQclass != null) {
						QASet localParent = kb.getManager().searchQContainer(
								parentQclass.get().getTermName(
										parentQclass));
						if (localParent != null) {
							parent = localParent;
						}
					}
				}

				QContainer qc = new QContainer(parent, name);
				if (!article.isFullParse()) {
					parent.addChild(qc,
								s.get().getPosition(s));
				}
				s.get().storeTermObject(article, s, qc);
				return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
							qc.getClass().getSimpleName()
									+ " " + qc.getName()));
			}
		}

		@Override
		public void destroy(KnowWEArticle article,
				Section<QuestionnaireDefinition> s) {

			QContainer q = s.get().getTermObjectFromLastVersion(article, s);
			if (q != null) {
				D3webUtils.removeRecursively(q);
				KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
						article, s);
			}
		}

	}

}
