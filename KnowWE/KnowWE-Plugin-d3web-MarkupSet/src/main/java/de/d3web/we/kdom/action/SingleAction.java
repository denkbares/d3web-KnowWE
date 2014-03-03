package de.d3web.we.kdom.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.strings.StringFragment;
import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class SingleAction extends AbstractType {

	public SingleAction(AbstractType t) {
		this.setSectionFinder(new SingleActionFinder());

		this.addChildType(t);
		t.setSectionFinder(new AllTextFinderTrimmed());
	}

	class SingleActionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			List<StringFragment> actions = Strings.splitUnquoted(text, ";");
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			for (StringFragment string : actions) {
				result.add(new SectionFinderResult(string.getStart(), string.getStart()
						+ string.getContent().length()));
			}
			return result;
		}

	}
}