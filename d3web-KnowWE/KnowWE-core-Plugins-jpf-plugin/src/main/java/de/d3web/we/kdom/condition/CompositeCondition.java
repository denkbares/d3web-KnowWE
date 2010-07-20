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

package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.constraint.ExclusiveType;
import de.d3web.we.kdom.dashTree.LineEndComment;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SyntaxError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.OneOfStringEnumFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

/**
 * This class defines a KDOM-Schema to parse composite conditions as known from
 * proposition logics, using 'AND', 'OR', 'NOT' as keywords and brackets '(' and
 * ')' for to express association boundaries
 *
 *
 * @author Jochen
 *
 */
public class CompositeCondition extends DefaultAbstractKnowWEObjectType {

	private final TerminalCondition terminalCondition = new TerminalCondition();

	public static char BRACE_OPEN = '(';
	public static char BRACE_CLOSED = ')';

	public CompositeCondition() {

		// this composite takes everything it gets => needs suitable wrapper
		// type as father
		this.sectionFinder = new AllTextFinderTrimmed();
		// this.setCustomRenderer(new
		// de.d3web.we.kdom.renderer.KDOMDepthFontSizeRenderer());

		// a composite condition may either be a BracedCondition,...
		BracedCondition braced = new BracedCondition(); // contains the brackets
		// and the
		// endline-comments
		this.childrenTypes.add(braced);
		BracedConditionContent bracedContent = new BracedConditionContent(); // without
		// brackets
		// and
		// comments
		braced.addChildType(bracedContent);
		braced.addChildType(new LineEndComment()); // explicit nodes for the
		// endline-comments
		bracedContent.addChildType(this);

		// ... a negated expression,...
		NegatedExpression negatedExpression = new NegatedExpression();
		this.childrenTypes.add(negatedExpression);
		negatedExpression.addChildType(this); // a NegatedExpression again
		// allows for a
		// CompositeCondition

		// ...a conjuctive expression,...
		Conjunct conj = new Conjunct();
		this.addChildType(conj);
		conj.addChildType(this); // Conjuncts again allow for a
		// CompositeCondition

		// ... a disjuctive expression,...
		Disjunct disj = new Disjunct();
		this.addChildType(disj);
		disj.addChildType(this); // Disjuncts again allow for a
		// CompositeCondition

		// ... or finally a TerminalCondition which stops the recursive descent
		this.addChildType(terminalCondition);
	}

	/**
	 * Sets the set of terminalConditions for this CompositeCondition
	 *
	 * Any terminal that is not accepted by one of these will be marked by an
	 * UnrecognizedTerminalCondition causing an error
	 *
	 * @param types
	 */
	public void setAllowedTerminalConditions(List<KnowWEObjectType> types) {
		terminalCondition.setAllowedTerminalConditions(types);
	}

	/**
	 * tells whether a CompositeCondition is a disjunction
	 *
	 * @param c
	 * @return
	 */
	public boolean isDisjunction(Section<CompositeCondition> c) {
		return getDisjuncts(c).size() > 0;
	}

	/**
	 * returns the disjuncts of a disjunction
	 *
	 * @param c
	 * @return
	 */
	public List<Section<? extends NonTerminalCondition>> getDisjuncts(Section<CompositeCondition> c) {

		List<Section<? extends NonTerminalCondition>> result = new ArrayList<Section<? extends NonTerminalCondition>>();
		List<Section<Disjunct>> childrenOfType = c.findChildrenOfType(Disjunct.class);

		result.addAll(childrenOfType);
		return result;
	}

	/**
	 * tells whether a CompositeCondition is a Conjunction
	 *
	 * @param c
	 * @return
	 */
	public boolean isConjunction(Section<CompositeCondition> c) {
		return getConjuncts(c).size() > 0;
	}

	/**
	 * returns the conjunts of a conjunction
	 *
	 * @param c
	 * @return
	 */
	public List<Section<? extends NonTerminalCondition>> getConjuncts(Section<CompositeCondition> c) {

		List<Section<? extends NonTerminalCondition>> result = new ArrayList<Section<? extends NonTerminalCondition>>();
		List<Section<Conjunct>> childrenOfType = c.findChildrenOfType(Conjunct.class);

		result.addAll(childrenOfType);
		return result;
	}

	/**
	 * tells whether a CompositeCondition is a bracedexpression
	 *
	 * @param c
	 * @return
	 */
	public boolean isBraced(Section<CompositeCondition> c) {
		return getBraced(c) != null;
	}

	/**
	 * returns the BracedCondition
	 *
	 * @param c
	 * @return
	 */
	public Section<? extends NonTerminalCondition> getBraced(Section<CompositeCondition> c) {

		List<Section<? extends NonTerminalCondition>> result = new ArrayList<Section<? extends NonTerminalCondition>>();
		Section<? extends BracedCondition> childrenOfType = c.findChildOfType(BracedCondition.class);
		return childrenOfType;
	}

	/**
	 * tells whether a CompositeCondition is a NegatedExpression
	 *
	 * @param c
	 * @return
	 */
	public boolean isNegation(Section<CompositeCondition> c) {
		return getNegation(c) != null;
	}

	/**
	 * returns the NegatedExpression of a Negation
	 *
	 * @param c
	 * @return
	 */
	public Section<? extends NonTerminalCondition> getNegation(Section<CompositeCondition> c) {

		List<Section<? extends NonTerminalCondition>> result = new ArrayList<Section<? extends NonTerminalCondition>>();
		Section<? extends NonTerminalCondition> negEx = c.findChildOfType(NegatedExpression.class);

		return negEx;
	}

	/**
	 * tells whether this CompositeCondition is a TerminalCondition
	 *
	 * @param c
	 * @return
	 */
	public boolean isTerminal(Section<CompositeCondition> c) {
		return getTerminal(c) != null;
	}

	/**
	 * returns the TerminalCondition of a (terminal-)CompositeCondition
	 *
	 * @param c
	 * @return
	 */
	public Section<? extends TerminalCondition> getTerminal(Section<CompositeCondition> c) {

		List<Section<? extends TerminalCondition>> result = new ArrayList<Section<? extends TerminalCondition>>();
		Section<? extends TerminalCondition> terminal = c.findChildOfType(TerminalCondition.class);

		return terminal;
	}


}

/**
 * @author Jochen
 *
 *         Type for a disjunct element in the CompositeCondition
 *
 *         example: 'a OR b' here 'a' and 'b' are nodes of type disjunct
 *
 */
class Disjunct extends NonTerminalCondition {
	@Override
	protected void init() {

		this.sectionFinder = new ConjunctSectionFinder(new String[] {
				"OR", "ODER", "|" });
	}
}

/**
 * @author Jochen
 *
 *         Type for a conjunct element in the CompositeCondition
 *
 *         example: 'a AND b' here 'a' and 'b' are nodes of type conjunct
 *
 */
class Conjunct extends NonTerminalCondition {

	static String[] CONJ_SIGNS = {
			"AND", "UND", "&" };

	@Override
	protected void init() {
		this.setSectionFinder(new ConjunctSectionFinder(CONJ_SIGNS));
	}

}

/**
 * @author Jochen
 *
 *         Type for a negated element in the CompositeCondition
 *
 *         example: 'NOT b' here 'b' is not nodes of type NegatedExpression
 *
 */
class NegatedExpression extends NonTerminalCondition {

	static String[] NEG_SIGNS = {
			"NOT", "NICHT", "!" };

	@Override
	protected void init() {
		AnonymousType negationSign = new AnonymousType("NegationSign");
		negationSign.setSectionFinder(new OneOfStringEnumFinder(NEG_SIGNS));
		this.addChildType(negationSign);

		this.sectionFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
				String trimmed = text.trim();
				for (String sign : NEG_SIGNS) {
					if (trimmed.startsWith(sign)) {
						return new AllTextFinderTrimmed().lookForSections(text,
								father,type);
					}
				}
				return null;
			}
		};
	}

}

class ConjunctSectionFinder extends SectionFinder {

	private final String[] signs;

	public ConjunctSectionFinder(String[] signs) {
		this.signs = signs;
		this.addConstraint(ExclusiveType.getInstance());
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
		Map<Integer, Integer> allFoundOps = new HashMap<Integer, Integer>();
		List<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
		for (String symbol : signs) {
			List<Integer> indicesOfUnbraced = SplitUtility.findIndicesOfUnbraced(text,
					symbol,
					CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);
			// store all found operator sign occ indices and its length
			for (Integer integer : indicesOfUnbraced) {
				allFoundOps.put(integer, symbol.length());
			}

		}

		// without any found conj-sings we dont create any conjuncts
		if (allFoundOps.size() == 0) return null;

		Integer[] keys = allFoundOps.keySet().toArray(
				new Integer[allFoundOps.keySet().size()]);
		Arrays.sort(keys);
		int lastBeginIndex = 0;
		// TODO: caution works only for OP signs with same length!! (e.g., not
		// with 'OR' and 'ODER')
		for (Integer integer : keys) {
			results.add(new SectionFinderResult(lastBeginIndex, integer));
			lastBeginIndex = integer + allFoundOps.get(integer);
		}

		results.add(new SectionFinderResult(lastBeginIndex, text.length()));

		return results;
	}

}

/**
 * @author Jochen
 *
 *         Content of an EmbracedCondition (without the brackets)
 * @see BracedCondition
 *
 */
class BracedConditionContent extends NonTerminalCondition {
	@Override
	protected void init() {
		this.sectionFinder = new BracedConditionContentFinder();
	}

	class BracedConditionContentFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
			String trimmed = text.trim();
			int leadingSpaces = text.indexOf(trimmed);
			if (trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN))) {
				int closingBracket = SplitUtility.findIndexOfClosingBracket(trimmed, 0,
						CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);

				return SectionFinderResult.createSingleItemList(new SectionFinderResult(
						leadingSpaces + 1, closingBracket));

			}
			return null;
		}

	}
}

/**
 * @author Jochen
 *
 *         Any expression enclosed with brackets is a BracedCondition each has a
 *         child of type BracedConditionContent
 *
 */
class BracedCondition extends NonTerminalCondition {

	@Override
	protected void init() {
		this.sectionFinder = new EmbracedExpressionFinder();
		this.sectionFinder.addConstraint(ExclusiveType.getInstance());
	}

	/**
	 *
	 * creates EmbracedExpressions if expression starts with a opening bracket
	 * and concludes with a closing brackets AND these two correspond to each
	 * other
	 *
	 * @author Jochen
	 *
	 */
	class EmbracedExpressionFinder extends SectionFinder {
		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
			String trimmed = text.trim();
			int leadingSpaces = text.indexOf(trimmed);
			int followingSpaces = text.length() - trimmed.length() - leadingSpaces;
			boolean startsWithOpen = trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN));
			int closingBracket = SplitUtility.findIndexOfClosingBracket(trimmed, 0,
					CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);

			// if it doesnt start with an opening bracket
			if (!startsWithOpen) {
				// its not an embraced expression for sure => return null
				return null;
			}

			// throw error if no corresponding closing bracket can be found
			if (closingBracket == -1) {
				KDOMReportMessage.storeSingleError(father.getArticle(), father, this.getClass(), new SyntaxError("missing \")\""));
				return null;
			} else {
				KDOMReportMessage.clearMessages(father.getArticle(), father, this.getClass());
			}

			// an embracedExpression needs to to start and end with '(' and ')'
			if (startsWithOpen
					&& trimmed.endsWith(Character.toString(CompositeCondition.BRACE_CLOSED))) {
				// and the ending ')' needs to close the opening
				if (closingBracket == trimmed.length() - 1) {
					return SectionFinderResult.createSingleItemList(new SectionFinderResult(
							leadingSpaces, text.length() - followingSpaces));
				}

			}

			// OR an embracedExpression can be concluded with a lineEnd-comment
			int lastEndLineCommentSymbol = SplitUtility.lastIndexOfUnquoted(text, "//");
			// so has to start with '(' and have a lineend-comment-sign after
			// the closing bracket but nothing in between!
			if (trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN))) {
				if (lastEndLineCommentSymbol > -1) {
					// TODO fix: < 3 is inaccurate
					// better check that there is no other expression in between
					if (lastEndLineCommentSymbol - closingBracket < 3) {
						return SectionFinderResult.createSingleItemList(new SectionFinderResult(
								leadingSpaces, text.length()));
					}
				}

			}

			return null;
		}
	}

}
