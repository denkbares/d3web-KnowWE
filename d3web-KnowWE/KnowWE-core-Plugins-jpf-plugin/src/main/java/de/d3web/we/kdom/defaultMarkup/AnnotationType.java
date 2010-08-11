package de.d3web.we.kdom.defaultMarkup;

import java.util.Collections;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;

public class AnnotationType extends DefaultAbstractKnowWEObjectType {

//	private final static int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
//	private final static String SECTION_REGEXP =
//			// prefix (declare the parameter)
//			"\\p{Space}+@$NAME$\\p{Blank}*[:=\\p{Space}]\\p{Blank}*" +
//			// content (any reluctant matched)
//			"\\p{Space}*(.*?)\\p{Space}*" +
//			// suffix: terminate-tag or end-of-input or declare next parameter
//			"(?:(?:^\\p{Blank}*%\\p{Blank}*$)" +
//			"|" +
//			"(?:\\z)" +
//			"|" +
//			"(?:\\p{Blank}+@\\w+))";

	private final DefaultMarkup.Annotation annotation;

	public AnnotationType(DefaultMarkup.Annotation annotation) {
		this.annotation = annotation;
//		Pattern pattern = getAnnotationPattern(this.annotation.getName());
//		this.setSectionFinder(new RegexSectionFinder(pattern, 1));
		this.setSectionFinder(new AnnotationFinder(annotation.getName()));
		Collections.addAll(this.childrenTypes, this.annotation.getTypes());
	}

	/**
	 * Returns the name of the underlying annotation defined.
	 * @return the annotation's name
	 */
	@Override
	public String getName() {
		return this.annotation.getName();
	}

	/**
	 * Returns the underlying annotation.
	 * @return the underlying annotation
	 */
	public DefaultMarkup.Annotation getAnnotation() {
		return annotation;
	}

//	/**
//	 * Returns the pattern to match a annotation block of a default mark-up
//	 * section.
//	 * 
//	 * @param name
//	 *            the name of the parameter ("&#64;&lt;name&gt;")
//	 * @return the pattern to match the default block of the section
//	 */
//	public static Pattern getAnnotationPattern(String name) {
//		String regexp = SECTION_REGEXP.replace("$NAME$", name);
//		return Pattern.compile(regexp, FLAGS);
//	}

}