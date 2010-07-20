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

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionnaireDefinition;
import de.d3web.we.kdom.questionTreeNew.QuestionTreeElementDefinition.QuestionTreeElementDefSubtreeHandler;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;

public class QClassLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {

		initSectionFinder();

		QuestionnaireDefinition qc = new QuestionnaireDefinition();
		qc.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR5));
		qc.setSectionFinder(new AllTextFinderTrimmed());
		qc.addSubtreeHandler(Priority.HIGHEST, new CreateQuestionnaireHandler());
		qc.setOrderSensitive(true);
		this.childrenTypes.add(qc);
	}

	private void initSectionFinder() {
		this.sectionFinder = new ConditionalAllTextFinder() {

			@Override
			protected boolean condition(String text, Section<?> father) {

				Section<DashTreeElement> s = KnowWEObjectTypeUtils
						.getAncestorOfType(father, DashTreeElement.class);
				if (DashTreeElement.getLevel(s) == 0) {
					// is root level
					return true;
				}
				Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
						.getDashTreeFather(s);
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					if (dashTreeFather.findSuccessor(QClassLine.class) != null) {
						return true;
					}
				}

				return false;
			}
		};
	}

	static class CreateQuestionnaireHandler
			extends QuestionTreeElementDefSubtreeHandler<QuestionnaireDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<QuestionnaireDefinition> qcSec) {

			KnowledgeBaseManagement mgn = getKBM(article);
			//ReviseSubtreeHandler will be called again with a correct mgn
			if (mgn == null) return null;

			String name = qcSec.getOriginalText();

			IDObject o = mgn.findQContainer(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(o.getClass()
						.getSimpleName()));
			} else {
				Section<DashTreeElement> element = KnowWEObjectTypeUtils
						.getAncestorOfType(qcSec, DashTreeElement.class);
				Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
						.getDashTreeFather(element);
				QASet parent = mgn.getKnowledgeBase().getRootQASet();
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					Section<QClassLine> parentQclass = dashTreeFather
							.findSuccessor(QClassLine.class);
					if (parentQclass != null) {
						QASet localParent = mgn.findQContainer(parentQclass
								.getOriginalText());
						if (localParent != null) {
							parent = localParent;
						}
					}
				}

				QContainer qc = mgn.createQContainer(name, parent);
				if (qc != null) {
					if (!article.isFullParse()) parent.moveChildToPosition(qc,
							qcSec.get().getPosition(qcSec));
					qcSec.get().storeTermObject(article, qcSec, qc);
					KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
							article, qcSec);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(qc.getClass().getSimpleName()
							+ " " + qc.getName()));
				} else {
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name, this.getClass()));
				}
			}
		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionnaireDefinition> s) {

			QContainer q = s.get().getTermObjectFromLastVersion(article, s);
			try {
				if (q != null) q.getKnowledgeBase().remove(q);
			}
			catch (IllegalAccessException e) {
				article.setFullParse(true, this);
				// e.printStackTrace();
			}
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(article, s);
		}

	}

}
