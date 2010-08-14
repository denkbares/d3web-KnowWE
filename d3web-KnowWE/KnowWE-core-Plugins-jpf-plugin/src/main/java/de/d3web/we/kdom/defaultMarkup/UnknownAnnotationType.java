package de.d3web.we.kdom.defaultMarkup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.renderer.StyleRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.Section;

public class UnknownAnnotationType extends DefaultAbstractKnowWEObjectType {

	private final static int FLAGS =
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;

	private final static String SECTION_REGEXP =
				// prefix (declare the parameter)
				"^\\p{Blank}*(@(\\w++)\\p{Blank}*[:=]?\\p{Blank}*" +
						// content (any reluctant matched)
						"\\p{Space}*([^\\p{Space}:=].*?))\\p{Space}*" +
						// suffix: terminate-tag or end-of-input or declare next
						// parameter
						"(?:(?:^\\p{Blank}*%\\p{Blank}*$)" +
						"|" +
						"(?:\\z)" +
						"|" +
						"(?:^\\p{Blank}*@\\w+))";

	private final static Pattern PATTERN = Pattern.compile(SECTION_REGEXP, FLAGS);

	public UnknownAnnotationType() {
		this.setSectionFinder(new RegexSectionFinder(PATTERN, 1));
	}

	@Override
	protected KnowWEDomRenderer getDefaultRenderer() {
		return new StyleRenderer("error_highlight", null);
	}

	/**
	 * Returns the name of the underlying annotation.
	 * 
	 * @return the annotation's name
	 */
	public static String getName(Section<?> section) {
		return getGroup(section, 2);
	}

	/**
	 * Returns the content of the underlying annotation.
	 * 
	 * @return the annotation's name
	 */
	public static String getContent(Section<?> section) {
		return getGroup(section, 3);
	}

	/**
	 * Returns the content of the underlying annotation including the declring
	 * name and the declared content.
	 * 
	 * @return the annotation's name
	 */
	public static String getDeclaration(Section<?> section) {
		return getGroup(section, 1);
	}

	private static String getGroup(Section<?> section, int group) {
		Matcher matcher = PATTERN.matcher(section.getOriginalText());
		if (matcher.find()) {
			return matcher.group(group);
		}
		return null;
	}
}