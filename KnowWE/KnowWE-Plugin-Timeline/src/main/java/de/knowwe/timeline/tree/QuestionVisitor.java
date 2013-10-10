/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.timeline.tree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.d3web.core.knowledge.terminology.Question;
import de.knowwe.timeline.IDataProvider;
import de.knowwe.timeline.Timeset;
import de.knowwe.timeline.parser.ASTsimpleElement;

/**
 * The QuestionVisitor visits all the nodes in the abstract syntax trees and
 * gathers all the question names that it encounters in the query.
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class QuestionVisitor extends AbstractVisitor {

	/**
	 * The set of gathered questions.
	 */
	private final Set<Question> questions;
	
	/**
	 * The data provider to lookup question names.
	 */
	private final IDataProvider dataProvider;

	public QuestionVisitor(IDataProvider dataProvider) {
		questions = new HashSet<Question>();
		this.dataProvider = dataProvider;
	}

	/**
	 * For every simpleElement encountered it adds its question to the set of
	 * questions.
	 */
	@Override
	public Timeset visit(ASTsimpleElement node, Object data) {
		questions.add(dataProvider.searchQuestion(node.question));
		return null;
	}

	/**
	 * Get all questions gathered till now.
	 *  
	 * @return An unmodifiable set of questions gathered.
	 */
	public Set<Question> getQuestions() {
		return Collections.unmodifiableSet(questions);
	}
}
