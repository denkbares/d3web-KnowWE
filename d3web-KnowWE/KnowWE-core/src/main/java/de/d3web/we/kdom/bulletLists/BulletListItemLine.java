package de.d3web.we.kdom.bulletLists;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.LineBreak;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class BulletListItemLine extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new BulletListItemLineFinder();
		this.childrenTypes.add(new BulletType());
		this.childrenTypes.add(new LineBreak());
		this.childrenTypes.add(new BulletCommentType());
		this.childrenTypes.add(new BulletContentType()) ;
		
	}

	class BulletListItemLineFinder extends SectionFinder {

		@SuppressWarnings("unchecked")
		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
				
				
			String lineRegex = "\\r?\\n";
			Pattern linePattern = Pattern.compile(lineRegex);

			Matcher tagMatcher = linePattern.matcher(text);
			ArrayList<SectionFinderResult> resultRegex = new ArrayList<SectionFinderResult>();
			int lastStart = 0;
			while (tagMatcher.find()) {
				
				String line = text.substring(lastStart,
						tagMatcher.end());
				// only lines starting with '*' can be bulletListItemLines
				if (line.trim().startsWith("*")) {
					resultRegex.add(new SectionFinderResult(lastStart,
							tagMatcher.end()));
					lastStart = tagMatcher.end();
				}
			}
			return resultRegex;
		}

	}

}
