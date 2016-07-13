/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.d3web.we.kdom.questionTree;

import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionYN;
import com.denkbares.strings.Identifier;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.AnswerDefinition;
import de.d3web.we.object.QuestionDefinition;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeTermRelationScript;
import de.knowwe.kdom.dashtree.DashTreeUtils;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 12.03.2014
 */
public class QuestionTreeQuestionRelationScript extends DashTreeTermRelationScript<D3webCompiler> {

	@Override
	protected void createObjectRelations(Section<TermDefinition> parentSection, D3webCompiler compiler, Identifier parentIdentifier, List<Identifier> childrenIdentifier) {
		NamedObject termObject = D3webUtils.getTermObject(compiler, parentIdentifier);
		// this can happen in certain error scenarios...
		if (!(termObject instanceof Question)) return;
		Question parentQuestion = (Question) termObject;
		TerminologyObject[] parents = parentQuestion.getParents();
		if (parents.length == 0) {
			parentQuestion.getKnowledgeBase().getRootQASet().addChild(parentQuestion);
		}
		for (Identifier childIdentifier : childrenIdentifier) {
			NamedObject childObject = D3webUtils.getTermObject(compiler, childIdentifier);
			if (childObject == null) continue;
			if (childObject instanceof Question) {
				Question childQuestion = (Question) childObject;
				parentQuestion.getKnowledgeBase().getRootQASet().removeChild(childQuestion);
				parentQuestion.addChild(childQuestion);
			}
			else if (parentQuestion instanceof QuestionChoice && childObject instanceof Choice) {
				// nothing to to for QuestionYN, answers are already there and immutable
				if (parentQuestion instanceof QuestionYN) continue;
				((QuestionChoice) parentQuestion).addAlternative((Choice) childObject);
			}
		}
	}

	/**
	 * In QuestionTreeQuestionDefinitions, we have followup questions not only directly as children of the current
	 * question, but also as children of the answers of the question.
	 */
	@Override
	protected List<Section<DashTreeElement>> getChildrenDashtreeElements(Section<?> termDefiningSection) {
		return getQuestionChildrenDashtreeElements(termDefiningSection);
	}

	public static List<Section<DashTreeElement>> getQuestionChildrenDashtreeElements(Section<?> termDefiningSection) {
		List<Section<DashTreeElement>> childrenList = DashTreeUtils.findChildrenDashtreeElements(termDefiningSection);
		LinkedList<Section<DashTreeElement>> augmentedChildrenList = new LinkedList();
		for (Section<DashTreeElement> child : childrenList) {
			augmentedChildrenList.add(child);
			Section<AnswerDefinition> answerDef = Sections.successor(child, AnswerDefinition.class);
			Section<NumericCondLine> numCondLine = Sections.successor(child, NumericCondLine.class);
			if (answerDef == null && numCondLine == null) continue;
			// if we have a AnswerDefinition, look for Questions below
			List<Section<DashTreeElement>> followUpQuestions = DashTreeUtils.findChildrenDashtreeElements(child);
			for (Section<DashTreeElement> followUpQuestion : followUpQuestions) {
				// we ignore &REF sections
				if (Sections.successor(child, QuestionDefinition.class) != null) continue;
				augmentedChildrenList.addAll(followUpQuestions);
			}
		}
		return augmentedChildrenList;
	}

	@Override
	public Class<D3webCompiler> getCompilerClass() {
		return D3webCompiler.class;
	}
}
