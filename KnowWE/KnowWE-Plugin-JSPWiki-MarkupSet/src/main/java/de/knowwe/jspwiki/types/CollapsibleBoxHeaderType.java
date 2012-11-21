package de.knowwe.jspwiki.types;

import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class CollapsibleBoxHeaderType extends AbstractType {
	public CollapsibleBoxHeaderType() {
		this.setSectionFinder(new SectionHeaderSectionFinder());
	}

	public class SectionHeaderSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			String[] rows = text.split("(\n)");
			int end = rows[0].length() + 1;
			SectionFinderResult s = new SectionFinderResult(0, end);
			result.add(s);
			return result;
		}
	}
}
