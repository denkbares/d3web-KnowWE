package de.d3web.we.kdom.rulesNew.terminalCondition;

import java.util.List;

import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.objects.QuestionDef;
import de.d3web.we.kdom.objects.QuestionRef;
import de.d3web.we.kdom.objects.QuestionRefImpl;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.OneOfStringEnumUnquotedFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

/**
 * A type implementing a cond-num TerminalCondition {@link TerminalCondition} It
 * has a allowed list of comparators
 * 
 * syntax: <questionID> <comp> <number> e.g.: mileage evaluation >= 130
 * 
 * @author Jochen
 *
 */
public class NumericalFinding extends DefaultAbstractKnowWEObjectType {

	private static String[] comparators = {
			"<=", ">=", "==", "<", ">", };

	@Override
	protected void init() {
		this.setSectionFinder(new NumericalFindingFinder());

		// comparator
		Comparator comparator = new Comparator();
		comparator.setSectionFinder(new OneOfStringEnumUnquotedFinder(comparators));
		this.childrenTypes.add(comparator);

		// question
		QuestionRef<Question> question = new QuestionRefImpl<Question>();
		AllTextFinderTrimmed questionFinder = new AllTextFinderTrimmed();
		questionFinder.addConstraint(SingleChildConstraint.getInstance());
		question.setSectionFinder(questionFinder);
		this.childrenTypes.add(question);

		// answer
		Number num = new Number();
		num.setSectionFinder(AllTextFinderTrimmed.getInstance());
		this.childrenTypes.add(num);
	}

	class NumericalFindingFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			for (String comp : comparators) {
				if (SplitUtility.containsUnquoted(text, comp)) {

					return AllTextFinderTrimmed.getInstance().lookForSections(text,
							father);
				}
			}

			return null;
		}

	}

	class Comparator extends DefaultAbstractKnowWEObjectType {

		@Override
		protected void init() {
			this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR3));
		}
	}

	class Question extends QuestionDef {
		@Override
		protected void init() {
			this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
		}

	}


}
