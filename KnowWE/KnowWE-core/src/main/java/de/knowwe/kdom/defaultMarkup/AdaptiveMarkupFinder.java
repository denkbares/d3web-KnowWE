package de.knowwe.kdom.defaultMarkup;

import java.util.List;
import java.util.regex.Pattern;

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

	public AdaptiveMarkupFinder(String name, String regex, int flags, int group) {
		this.name = name;
		this.regex = regex;
		this.flags = flags;
		singleLineFinder = new RegexSectionFinder(getSingleLinePattern(), group);
		multiLineFinder = new RegexSectionFinder(getMultiLinePattern(), group);
	}

	private String getContentRegexp() {
		return regex.replace("$NAME$", name);
	}

	private Pattern getSingleLinePattern() {
		String regexp = getContentRegexp().replace("$LINESTART$", "");
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
			return singleLineFinder.lookForSections(text, father, type);
		}
		else {
			return multiLineFinder.lookForSections(text, father, type);
		}
	}
}
