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

package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.d3web.we.kdom.condition.helper.BracedCondition;
import de.d3web.we.kdom.condition.helper.BracedConditionContent;
import de.d3web.we.kdom.condition.helper.CompCondLineEndComment;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;

/**
 * This class defines a KDOM-Schema to parse composite conditions as known from proposition logics, using 'AND', 'OR',
 * 'NOT' as keywords and brackets '(' and ')' for to express association boundaries
 *
 * @author Jochen
 */
public class CompositeCondition extends AbstractType {

	protected final TerminalCondition terminalCondition = new TerminalCondition();

	public static final char BRACE_OPEN = '(';
	public static final char BRACE_CLOSED = ')';

	public CompositeCondition() {
		this(new String[] { "AND", "UND", "&" },
				new String[] { "OR", "ODER", "|" },
				new String[] { "NOT", "NICHT", "!" });
	}

	public CompositeCondition(String[] andKeys, String[] orKeys, String[] notKeys) {

		// this composite takes everything it gets => needs suitable wrapper
		// type as father
		this.setSectionFinder(new AllTextFinderTrimmed());

		// a composite condition may either be a BracedCondition,...
		BracedCondition braced = new BracedCondition();

		// contains the brackets and the endline-comments
		this.addChildType(braced);
		BracedConditionContent bracedContent = new BracedConditionContent();

		// braced content without brackets and comments
		braced.addChildType(bracedContent);

		// explicit nodes for the endline-comments
		braced.addChildType(new CompCondLineEndComment());
		bracedContent.addChildType(this);

		// ... a disjunctive expression,...
		Disjunct disjunct = new Disjunct(orKeys);
		this.addChildType(disjunct);
		// Disjuncts again allow for a CompositeCondition
		disjunct.addChildType(this);

		// ...a conjunctive expression,...
		Conjunct conjunct = new Conjunct(andKeys);
		this.addChildType(conjunct);
		// Conjuncts again allow for a CompositeCondition
		conjunct.addChildType(this);

		// ... a negated expression,...
		NegatedExpression negatedExpression = new NegatedExpression(notKeys);
		this.addChildType(negatedExpression);
		// a NegatedExpression again allows for a CompositeCondition
		negatedExpression.addChildType(this);

		// ... or finally a TerminalCondition which stops the recursive descent
		this.addChildType(terminalCondition);
	}

	/**
	 * Sets the set of terminalConditions for this CompositeCondition
	 * <p>
	 * Any terminal that is not accepted by one of these will be marked by an UnrecognizedTerminalCondition causing an
	 * error
	 */
	public void setAllowedTerminalConditions(List<Type> types) {
		terminalCondition.setAllowedTerminalConditions(types);
	}

	/**
	 * tells whether a CompositeCondition is a disjunction
	 */
	public boolean isDisjunction(Section<? extends CompositeCondition> c) {
		return !getDisjuncts(c).isEmpty();
	}

	/**
	 * returns the disjuncts of a disjunction
	 */
	public List<Section<? extends NonTerminalCondition>> getDisjuncts(Section<? extends CompositeCondition> c) {

		List<Section<? extends NonTerminalCondition>> result = new ArrayList<>();
		List<Section<Disjunct>> childrenOfType = Sections.children(c, Disjunct.class);

		result.addAll(childrenOfType);
		return result;
	}

	/**
	 * tells whether a CompositeCondition is a Conjunction
	 */
	public boolean isConjunction(Section<? extends CompositeCondition> c) {
		return !getConjuncts(c).isEmpty();
	}

	/**
	 * returns the conjuncts of a conjunction
	 */
	public List<Section<? extends NonTerminalCondition>> getConjuncts(Section<? extends CompositeCondition> c) {

		List<Section<? extends NonTerminalCondition>> result = new ArrayList<>();
		List<Section<Conjunct>> childrenOfType = Sections.children(c, Conjunct.class);

		result.addAll(childrenOfType);
		return result;
	}

	/**
	 * tells whether a CompositeCondition is a braced-expression
	 */
	public boolean isBraced(Section<? extends CompositeCondition> c) {
		return getBraced(c) != null;
	}

	/**
	 * returns the BracedCondition
	 */
	public Section<? extends NonTerminalCondition> getBraced(Section<? extends CompositeCondition> c) {
		return Sections.child(c, BracedCondition.class);
	}

	/**
	 * tells whether a CompositeCondition is a NegatedExpression
	 */
	public boolean isNegation(Section<? extends CompositeCondition> c) {
		return getNegation(c) != null;
	}

	/**
	 * returns the NegatedExpression of a Negation
	 */
	public Section<? extends NonTerminalCondition> getNegation(Section<? extends CompositeCondition> c) {
		return Sections.child(c, NegatedExpression.class);
	}

	/**
	 * tells whether this CompositeCondition is a TerminalCondition
	 */
	public boolean isTerminal(Section<? extends CompositeCondition> c) {
		return getTerminal(c) != null;
	}

	/**
	 * returns the TerminalCondition of a (terminal-)CompositeCondition
	 */
	public Section<? extends TerminalCondition> getTerminal(Section<? extends CompositeCondition> c) {
		return Sections.child(c, TerminalCondition.class);
	}

	public static boolean hasLineBreakAfterComment(String text) {
		int start = Strings.lastIndexOfUnquoted(text, "//");
		if (start != -1) {
			Pattern pattern = Pattern.compile("\\r?\\n");
			Matcher matcher = pattern.matcher(text);
			int lineBreak = -1;
			while (matcher.find()) {
				// attempts to find the last line break
				lineBreak = matcher.start();
			}
			if (lineBreak > start) {
				return true;
			}
		}
		return false;
	}
}
