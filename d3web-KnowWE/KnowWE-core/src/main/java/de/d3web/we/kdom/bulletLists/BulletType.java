package de.d3web.we.kdom.bulletLists;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class BulletType extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new BulletFinder();
	}

	class BulletFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, KnowWEObjectType type) {

			int index = text.indexOf('*');
			char c = text.charAt(index + 1);
			while (c == ' ') {
				index++;
				c = text.charAt(index);
			}
			ArrayList<SectionFinderResult> resultRegex = new ArrayList<SectionFinderResult>();
			resultRegex.add(new SectionFinderResult(0, index));
			return resultRegex;
		}

	}
}
