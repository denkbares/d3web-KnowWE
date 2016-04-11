package de.knowwe.kdom.defaultMarkup;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class AdaptiveMarkupFinder implements SectionFinder {

	private final RegexSectionFinder singleLineFinder;
	private final RegexSectionFinder multiLineFinder;

	private final String name;
	private final String regex;
	private final int flags;
	private final boolean isContent;

	public AdaptiveMarkupFinder(String name, String regex, int flags, int group, boolean isContent) {
		this.name = name;
		this.regex = regex;
		this.flags = flags;
		singleLineFinder = new RegexSectionFinder(getSingleLinePattern(), group);
		multiLineFinder = new RegexSectionFinder(getMultiLinePattern(), group);
		this.isContent = isContent;
	}

	private String getContentRegexp() {
		return regex.replace("$NAME$", name);
	}

	private Pattern getSingleLinePattern() {
		String regexp = getContentRegexp().replace("$LINESTART$", "(?:^|\\p{Space})");
		return Pattern.compile(regexp, flags);
	}

	private Pattern getMultiLinePattern() {
		String regexp = getContentRegexp().replace("$LINESTART$", "^");
		return Pattern.compile(regexp, flags);
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		String[] lines = father.getText().split("\\r?\\n");
		if (lines.length == 1) {
			List<SectionFinderResult> results = singleLineFinder.lookForSections(text, father, type);
			// if in a single line markup the content only consists
			// of the "%" character, the content shall be assumed
			// to be empty (one result with length 0)
			if (isContent && results.size() == 1) {
				SectionFinderResult result = results.get(0);
				int start = result.getStart();
				int end = result.getEnd();
				if (Strings.trim(text.substring(start, end)).equals("%")) {
					return Arrays.asList(new SectionFinderResult(start, start));
				}
			}
			return results;
		}
		else {
			return multiLineFinder.lookForSections(text, father, type);
		}
	}
}
