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

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.tools.ToolMenuDecoratingRenderer;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.knowwe.core.dashtree.DashTreeElement;
import de.knowwe.core.dashtree.DashTreeUtils;
import de.knowwe.core.renderer.FontColorRenderer;

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
		setCustomRenderer(new ToolMenuDecoratingRenderer<KnowWEObjectType>(
						new FontColorRenderer(FontColorRenderer.COLOR5)));
		setOrderSensitive(true);
	}

	public abstract int getPosition(Section<QuestionnaireDefinition> s);

	static class CreateQuestionnaireHandler
			extends D3webSubtreeHandler<QuestionnaireDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<QuestionnaireDefinition> qcSec) {

			if (KnowWEUtils.getTerminologyHandler(article.getWeb()).isDefinedTerm(article, qcSec)) {
				KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(article,
						qcSec);
				return new ArrayList<KDOMReportMessage>(0);
				// return Arrays.asList((KDOMReportMessage) new
				// ObjectAlreadyDefinedWarning(
				// qcSec.get().getTermName(qcSec)));
			}

			KnowledgeBaseManagement mgn = getKBM(article);

			String name = qcSec.get().getTermName(qcSec);

			IDObject o = mgn.findQContainer(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(
						o.getClass().getSimpleName()));
			}
			else {
				Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
						.getFatherDashTreeElement(qcSec);
				QASet parent = mgn.getKnowledgeBase().getRootQASet();
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					Section<QuestionnaireDefinition> parentQclass = dashTreeFather
							.findSuccessor(QuestionnaireDefinition.class);
					if (parentQclass != null) {
						QASet localParent = mgn.findQContainer(parentQclass.get().getTermName(
								parentQclass));
						if (localParent != null) {
							parent = localParent;
						}
					}
				}

				QContainer qc = mgn.createQContainer(name, parent);
				if (qc != null) {
					if (!article.isFullParse()) {
						parent.moveChildToPosition(qc,
								qcSec.get().getPosition(qcSec));
					}
					qcSec.get().storeTermObject(article, qcSec, qc);
					KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
							article, qcSec);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
							qc.getClass().getSimpleName()
									+ " " + qc.getName()));
				}
				else {
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name,
							this.getClass()));
				}
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
