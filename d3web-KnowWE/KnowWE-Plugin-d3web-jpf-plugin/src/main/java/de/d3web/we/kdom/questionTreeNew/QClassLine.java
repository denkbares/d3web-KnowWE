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


import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.DashTreeUtils;
import de.d3web.we.kdom.objects.QuestionnaireDefinition;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;

public class QClassLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {

		initSectionFinder();

		this.childrenTypes.add(new QuestionTreeQuestionnaireDefinition());
	}

	static class QuestionTreeQuestionnaireDefinition extends QuestionnaireDefinition {

		public QuestionTreeQuestionnaireDefinition() {
			setSectionFinder(new AllTextFinderTrimmed());
		}

		@Override
		public int getPosition(Section<QuestionnaireDefinition> s) {
			return DashTreeUtils.getPositionInFatherDashSubtree(s);
		}

		@Override
		public boolean hasViolatedConstraints(KnowWEArticle article, Section<?> s) {
			return DashTreeUtils.isChangedTermDefInAncestorSubtree(article, s, 1);
		}

	}

	private void initSectionFinder() {
		this.sectionFinder = new ConditionalAllTextFinder() {

			@Override
			protected boolean condition(String text, Section<?> father) {

				Section<DashTreeElement> s = father
						.findAncestorOfType(DashTreeElement.class);
				if (DashTreeUtils.getDashLevel(s) == 0) {
					// is root level
					return true;
				}
				Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
						.getFatherDashTreeElement(s);
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


}
