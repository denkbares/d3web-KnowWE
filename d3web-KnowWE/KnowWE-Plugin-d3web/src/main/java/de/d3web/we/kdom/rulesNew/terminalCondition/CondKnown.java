package de.d3web.we.kdom.rulesNew.terminalCondition;

import java.util.List;

import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.QuestionRef;
import de.d3web.we.kdom.objects.QuestionRefImpl;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * Implements the CondKnown Condition to be used as child type in
 * {@link TerminalCondition}
 *
 * syntax: KNOWN[<questionID>] / BEKANNT[<questionID>]
 *
 * @author Jochen
 *
 */
public class CondKnown extends DefaultAbstractKnowWEObjectType {

	protected static String[] KEYWORDS = {
			"KNOWN", "BEKANNT" };

	@Override
	protected void init() {
		this.sectionFinder = new CondKnownFinder();

		QuestionRef<Question> question = new QuestionRefImpl<Question>();
		question.setSectionFinder(new SectionFinder() {
			@Override
			public List<SectionFinderResult> lookForSections(String text, Section father) {
				return SectionFinderResult.createSingleItemList(new SectionFinderResult(
						text.indexOf('[') + 1, text.indexOf(']')));
			}});
		this.addChildType(question);
	}

	class CondKnownFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {

			for (String key : KEYWORDS) {
				if (text.trim().startsWith(key + "[") && text.trim().endsWith("]")) {
					return AllTextFinderTrimmed.getInstance().lookForSections(text,
							father);
				}
			}
			return null;
		}

	}


}
