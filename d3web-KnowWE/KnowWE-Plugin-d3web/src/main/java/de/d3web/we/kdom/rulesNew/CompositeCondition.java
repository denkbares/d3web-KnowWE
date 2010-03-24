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

package de.d3web.we.kdom.rulesNew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.constraint.ExclusiveType;
import de.d3web.we.kdom.dashTree.LineEndComment;
import de.d3web.we.kdom.renderer.FontColorRenderer;
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

	public static char BRACE_OPEN = '(';
	public static char BRACE_CLOSED = ')';

	@Override
	protected void init() {
		// this composite takes everything it gets => needs suitable wrapper
		// type as father
		this.sectionFinder = new AllTextFinderTrimmed();
		this.setCustomRenderer(new de.d3web.we.kdom.renderer.KDOMDepthFontSizeRenderer());


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
		this.addChildType(new TerminalCondition());
	}
}

class TerminalCondition extends DefaultAbstractKnowWEObjectType {
	@Override
	protected void init() {
		this.sectionFinder = AllTextFinderTrimmed.getInstance();
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR6));
	}
}

class Disjunct extends DefaultAbstractKnowWEObjectType {
	@Override
	protected void init() {
		// TODO make it work for "OR", "ODER"
		this.sectionFinder = new ConjunctSectionFinder(new String[] { "OR" });
	}
}

class Conjunct extends DefaultAbstractKnowWEObjectType {

	static String[] CONJ_SIGNS = {
			"AND", "UND" };

	@Override
	protected void init() {
		this.setSectionFinder(new ConjunctSectionFinder(CONJ_SIGNS));
	}

}

class NegatedExpression extends DefaultAbstractKnowWEObjectType {

	static String[] NEG_SIGNS = {
			"NOT", "NICHT" };

	@Override
	protected void init() {
		AnonymousType negationSign = new AnonymousType("NegationSign");
		negationSign.setSectionFinder(new OneOfStringEnumFinder(NEG_SIGNS));
		this.addChildType(negationSign);

		this.sectionFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section father) {
				String trimmed = text.trim();
				for (String sign : NEG_SIGNS) {
					if (trimmed.startsWith(sign)) {
						return AllTextFinderTrimmed.getInstance().lookForSections(text,
								father);
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
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		List<Integer> allFoundOps = new ArrayList<Integer>();
		List<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
		for (String symbol : signs) {
			allFoundOps.addAll(SplitUtility.findIndicesOfUnbraced(text, symbol,
					CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED));
		}

		// without any found conj-sings we dont create any conjuncts
		if (allFoundOps.size() == 0) return null;

		Collections.sort(allFoundOps);
		int lastBeginIndex = 0;
		// TODO: caution works only for OP signs with same length!! (e.g., not
		// with 'OR' and 'ODER')
		for (Integer integer : allFoundOps) {
			results.add(new SectionFinderResult(lastBeginIndex, integer));
			lastBeginIndex = integer + signs[0].length();
		}

		results.add(new SectionFinderResult(lastBeginIndex, text.length()));

		return results;
	}

}

class BracedConditionContent extends DefaultAbstractKnowWEObjectType {
	@Override
	protected void init() {
		this.sectionFinder = new BracedConditionContentFinder();
	}

	class BracedConditionContentFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
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

class BracedCondition extends DefaultAbstractKnowWEObjectType {

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
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			String trimmed = text.trim();
			int leadingSpaces = text.indexOf(trimmed);
			int followingSpaces = text.length() - trimmed.length() - leadingSpaces;
			int closingBracket = SplitUtility.findIndexOfClosingBracket(trimmed, 0,
					CompositeCondition.BRACE_OPEN, CompositeCondition.BRACE_CLOSED);

			// an embracedExpression needs to to start and end with '(' and ')'
			if (trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN))
					&& trimmed.endsWith(Character.toString(CompositeCondition.BRACE_CLOSED))) {
				// and the ending ')' needs to clse the opening
				if (closingBracket == trimmed.length() - 1) {
					return SectionFinderResult.createSingleItemList(new SectionFinderResult(
							leadingSpaces, text.length() - followingSpaces));
				}
			}

			// OR an embracedExpression can be concluded with a lineEnd-comment
			int lastEndLineCommentSymbol = SplitUtility.lastIndexOfUnquoted(text, "//");
			// so has to start with '(' and have a lineend-comment-sign after
			// the closing bracket but nothing in between!
			if (trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN))
					&& lastEndLineCommentSymbol > -1) {
				// TODO fix: < 3 is inaccurate
				// better check that there is no other expression in between
				if (lastEndLineCommentSymbol - closingBracket < 3) {
					return SectionFinderResult.createSingleItemList(new SectionFinderResult(
							leadingSpaces, text.length()));
				}
			}

			return null;
		}
	}

}
