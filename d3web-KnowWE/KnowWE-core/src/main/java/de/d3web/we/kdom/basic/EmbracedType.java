package de.d3web.we.kdom.basic;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class EmbracedType extends DefaultAbstractKnowWEObjectType {

	boolean steal = false;

	public boolean isSteal() {
		return steal;
	}

	public void setSteal(boolean steal) {
		this.steal = steal;
	}

	public EmbracedType(KnowWEObjectType bodyType, String start, String end) {
		this.childrenTypes.add(new EmbraceStart(start));
		this.childrenTypes.add(new EmbraceEnd(end));
		this.childrenTypes.add(bodyType);
		this.sectionFinder = new EmbracementFinder(bodyType, start, end);
	}

	class EmbracementFinder extends SectionFinder {

		private String start;
		private String end;
		private KnowWEObjectType bodyType;

		public EmbracementFinder(KnowWEObjectType body, String start, String end) {
			this.start = start;
			this.end = end;
			this.bodyType = body;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, KnowWEObjectType type) {
			String trimmed = text.trim();
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			if (steal) {
				if (trimmed.contains(start) && trimmed.contains(end)) {
					String body = trimmed.substring(trimmed.indexOf(start)
							+ start.length(), trimmed.indexOf(end) + 1
							- end.length());
					List<SectionFinderResult> lookAheadSections = bodyType
							.getSectioner().lookForSections(body, father, type);
					if (lookAheadSections != null
							&& lookAheadSections.size() > 0) {
						result.add(new SectionFinderResult(text.indexOf(start),
								text.indexOf(end) + end.length()));
					}
				}

			}
			else {

				if (trimmed.startsWith(start) && trimmed.endsWith(end)) {
					String body = trimmed.substring(start.length(), trimmed
							.length()
							- end.length());
					List<SectionFinderResult> lookAheadSections = bodyType.getSectioner().lookForSections(
							body, father, type);
					if (lookAheadSections != null && lookAheadSections.size() > 0) {
						result.add(new SectionFinderResult(text.indexOf(trimmed),
								text.indexOf(trimmed) + trimmed.length()));
					}
				}
			}
			return result;
		}

	}
}
