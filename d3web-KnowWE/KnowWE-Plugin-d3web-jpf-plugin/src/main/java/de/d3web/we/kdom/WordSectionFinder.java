package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class WordSectionFinder implements ISectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

		if (!text.equals(" ") && !text.equals("\"")
				&& !text.contains("(") && !text.contains(")")) {

			int start = 0;
			int end = text.length();
			while (text.charAt(start) == ' ' || text.charAt(start) == '"') {
				start++;
				if (start >= end) return null;
			}
			while (text.charAt(end - 1) == ' ' || text.charAt(end - 1) == '"') {
				end--;
				if (start >= end) return null;
			}

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			result.add(new SectionFinderResult(start, end));
			return result;
		}
		return null;
	}

}
