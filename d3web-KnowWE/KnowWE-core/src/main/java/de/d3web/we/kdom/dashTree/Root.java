package de.d3web.we.kdom.dashTree;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.LineSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class Root extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new RootFinder();
	}

	public static int getLevel(Section s) {
		if (s == null)
			return -1;
		if (s.getObjectType() instanceof Root) {
			String text = s.getOriginalText().trim();
			if (text.length() == 0)
				return -1;
			int index = 0;
			while (text.charAt(index) == '-') {
				index++;
			}
			return index;
		} else {
			return -1;
		}
	}

	class RootFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {

			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

			List<SectionFinderResult> lookForSections = LineSectionFinder
					.getInstance().lookForSections(text, father);
			if (lookForSections != null && lookForSections.size() > 0) {
				int index = 0;
				
				//Search for first non-empty line --> todo
				while (index < lookForSections.size()) {
					SectionFinderResult sectionFinderResult = lookForSections
							.get(index);
					index++;
					int start = sectionFinderResult.getStart();
					int end = sectionFinderResult.getEnd();
					String finding = text.substring(start, end);
					finding = finding.replaceAll("\r", "");
					finding = finding.replaceAll("\n", "");
					finding = finding.replaceAll(" ", "");
					if (finding.length() > 0) {
						result.add(sectionFinderResult);
						break;
					}
				}
			}
			return result;
		}

	}

}
