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

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.QuestionZC;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Abstract Type for the definition of questions. A question with a given type is created and hooked into the root QASet
 * of the knowledge base. The hierarchical position in the terminology needs to be handled by the subclass.
 *
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class QuestionDefinition extends QASetDefinition<Question> {

	public enum QuestionType {
		OC(QuestionOC.class), MC(QuestionMC.class), YN(QuestionYN.class),
		NUM(QuestionNum.class), DATE(QuestionDate.class), TEXT(QuestionText.class),
		INFO(QuestionZC.class);

		private final Class<? extends Question> questionClass;

		QuestionType(Class<? extends Question> questionClass) {
			this.questionClass = questionClass;
		}

		public Class<? extends Question> getQuestionClass() {
			return questionClass;
		}
	}

	public QuestionDefinition() {
		this.addCompileScript(Priority.LOW, new TerminologyLoopDetectionHandler<Question>());
		this.addCompileScript(Priority.LOWER, new TerminologyLoopResolveHandler<Question>());
		this.setRenderer(new ValueTooltipRenderer(StyleRenderer.Question));
	}

	@Override
	public final Class<?> getTermObjectClass(Section<? extends Term> section) {
		QuestionType questionType = getQuestionType(Sections.cast(section, QuestionDefinition.class));
		return (questionType == null) ? Question.class : questionType.getQuestionClass();
	}

	public abstract QuestionType getQuestionType(Section<QuestionDefinition> s);
}
