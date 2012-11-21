package de.knowwe.jspwiki.types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.jspwiki.renderer.JSPWikiMarkupIDRenderer;

public class CollapsibleBoxType extends AbstractType {
	public CollapsibleBoxType() {
		this.setSectionFinder(new CollapsibleBoxSectionFinder());
		this.addChildType(new SectionHeaderType());
		this.addChildType(new CollapsibleBoxHeaderType());
		this.addChildType(new SectionContentType(4));
		this.setRenderer(new JSPWikiMarkupIDRenderer());
	}

	public class CollapsibleBoxSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {
			List<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
			int depth = 0;
			int marker = 0;
			while (marker != text.length()) {
				depth = 0;
				int start = text.indexOf("%%collapsebox", marker);
				if (start == -1) {
					return results;
				}
				String[] s = text.substring(start).split("\n");
				if (s.length <= 1) {
					return results;
				}
				depth++;
				for (int i = 1; i < s.length; i++) {
					if (s[i].matches("%%[^\\s].*?\\s")) {
						depth++;
					}
					Matcher endsigns = Pattern.compile("(%%|/%|%)\\s").matcher(
							s[i]);
					while (endsigns.find()) {
						depth--;
					}
					marker = text.indexOf(s[i], marker);
					if (depth == 0) {
						int index = s[i].indexOf('%');
						if (s[i].charAt(index + 1) == '%') {
							index++;
						}
						int end = text.indexOf(s[i], marker) + index + 1;
						SectionFinderResult result = new SectionFinderResult(
								start, end);
						results.add(result);
						marker = end;
						break;
					}
				}
			}
			return null;
		}

	}
}
