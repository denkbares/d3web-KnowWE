/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.testcases.stc;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyManager;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.Solution;

/**
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 14.03.2012
 */
public class LazyKnowledgeBase extends KnowledgeBase {

	private final LazyTerminologyManager manager;

	public LazyKnowledgeBase() {
		manager = new LazyTerminologyManager(this);
	}

	@Override
	public TerminologyManager getManager() {
		return manager;
	}

	private static class LazyTerminologyManager extends TerminologyManager {

		private final KnowledgeBase knowledgeBase;

		public LazyTerminologyManager(KnowledgeBase knowledgeBase) {
			super(knowledgeBase);
			this.knowledgeBase = knowledgeBase;
		}

		@Override
		public Question searchQuestion(String questionName) {
			Question question = super.searchQuestion(questionName);
			if (question == null) {
				question = new QuestionText(knowledgeBase, questionName);
			}
			return question;
		}

		@Override
		public Solution searchSolution(String solutionName) {
			Solution solution = super.searchSolution(solutionName);
			if (solution == null) {
				solution = new Solution(knowledgeBase, solutionName);
			}
			return solution;
		}

	}
}
