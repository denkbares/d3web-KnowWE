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
package de.d3web.we.kdom.questionTree;

import de.knowwe.core.compile.ConstraintModule;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Registering this module to your SuptreeHandler lets him compile, if there was
 * a change in the subtree of the root question in the question tree. The root
 * question is the question with the least dashes above the current object.
 * 
 * Change means, that any Section, except TermReferences, is either not reused
 * or has changed its position, in case it was order sensitive.
 * 
 * @author Albrecht Striffler
 * @created 25.01.2011
 * @param <T> is the Type of the Section this module is used with.
 */
public class RootQuestionChangeConstraint<T extends Type> extends ConstraintModule<T> {

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<T> s) {
		return QuestionDashTreeUtils.isChangeInRootQuestionSubtree(article, s);
	}

}
