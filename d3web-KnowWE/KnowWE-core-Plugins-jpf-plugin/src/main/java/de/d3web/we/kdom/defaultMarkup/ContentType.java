package de.d3web.we.kdom.defaultMarkup;

import java.util.Collections;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

public class ContentType extends DefaultAbstractKnowWEObjectType {

	private final static int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
	private final static String SECTION_REGEXP =
			// prefix (declare the markup section)
			"^\\p{Blank}*%%$NAME$\\p{Blank}*[:=\\p{Space}]\\p{Blank}*" +
					// content (any reluctant matched), as group with whitespace
					// characters
					"(\\p{Space}*?(.*?)\\p{Space}*)" +
					// suffix: terminate-tag or end-of-input or declare next
					// parameter
					"(?:(?:^\\p{Blank}*/?%\\p{Blank}*$)" +
					"|" +
					"(?:\\z)" +
					"|" +
					"(?:\\p{Space}+@\\w+))";

	private final DefaultMarkup markup;

	public ContentType(DefaultMarkup markup) {
		this.markup = markup;
		Pattern pattern = getContentPattern(this.markup.getName());
		this.setSectionFinder(new RegexSectionFinder(pattern, 1));
		Collections.addAll(this.childrenTypes, this.markup.getTypes());
	}

	/**
	 * Return the name of the content type section.
	 */
	@Override
	public String getName() {
		return this.markup.getName() + "@content";
	}

	/**
	 * Returns the pattern to match the default block of a default mark-up
	 * section.
	 * 
	 * @param markupName the name of the parent section (opened by
	 *        "%%&lt;markupName&gt;")
	 * @return the pattern to match the default block of the section
	 */
	public static Pattern getContentPattern(String markupName) {
		String regexp = SECTION_REGEXP.replace("$NAME$", markupName);
		return Pattern.compile(regexp, FLAGS);
	}
}
