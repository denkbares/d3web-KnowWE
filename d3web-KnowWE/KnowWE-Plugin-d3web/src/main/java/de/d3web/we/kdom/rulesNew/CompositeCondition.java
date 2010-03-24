package de.d3web.we.kdom.rulesNew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.constraint.ExclusiveType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.OneOfStringEnumFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

public class CompositeCondition extends DefaultAbstractKnowWEObjectType {

	public static char BRACE_OPEN = '(';
	public static char BRACE_CLOSED = ')';

	@Override
	protected void init() {
		this.sectionFinder = new AllTextFinderTrimmed();
		this.setCustomRenderer(new de.d3web.we.kdom.renderer.KDOMDepthFontSizeRenderer());

		BracedCondition braced = new BracedCondition();
		this.childrenTypes.add(braced);
		braced.addChildType(this);

		NegatedExpression negatedExpression = new NegatedExpression();
		this.childrenTypes.add(negatedExpression);
		negatedExpression.addChildType(this);

		Conjunct conj = new Conjunct();
		this.addChildType(conj);
		conj.addChildType(this);

		Disjunct disj = new Disjunct();
		this.addChildType(disj);
		disj.addChildType(this);

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
		this.sectionFinder = new ConjunctionSectionFinder(new String[] { "OR" });
	}
}

class Conjunct extends DefaultAbstractKnowWEObjectType {

	static String[] CONJ_SIGNS = {
			"AND", "UND" };

	@Override
	protected void init() {
		this.setSectionFinder(new ConjunctionSectionFinder(CONJ_SIGNS));
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

class ConjunctionSectionFinder extends SectionFinder {

	private final String[] signs;

	public ConjunctionSectionFinder(String[] signs) {
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
		// TODO: caution works only for OP signs with same length!!
		for (Integer integer : allFoundOps) {
			results.add(new SectionFinderResult(lastBeginIndex, integer));
			lastBeginIndex = integer + signs[0].length();
		}

		results.add(new SectionFinderResult(lastBeginIndex, text.length()));

		return results;
	}

}

// class BracedConditionContent extends DefaultAbstractKnowWEObjectType {
// @Override
// protected void init() {
// // TODO Auto-generated method stub
// super.init();
// }
//
// }

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

			if (trimmed.startsWith(Character.toString(CompositeCondition.BRACE_OPEN))
					&& trimmed.endsWith(Character.toString(CompositeCondition.BRACE_CLOSED))) {
				if (closingBracket == trimmed.length() - 1) {
					return SectionFinderResult.createSingleItemList(new SectionFinderResult(
							leadingSpaces + 1, text.length() - followingSpaces - 1));
				}
			}

			int lastEndLineCommentSymbol = SplitUtility.lastIndexOfUnquoted(text, "//");
			if (lastEndLineCommentSymbol > -1) {
				if (lastEndLineCommentSymbol - closingBracket < 3) {
					return SectionFinderResult.createSingleItemList(new SectionFinderResult(
							leadingSpaces + 1, lastEndLineCommentSymbol - 1));
				}
			}

			return null;
		}
	}

}
