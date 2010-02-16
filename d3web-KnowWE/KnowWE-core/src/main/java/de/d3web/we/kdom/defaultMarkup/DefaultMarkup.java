package de.d3web.we.kdom.defaultMarkup;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.basic.PlainText;

public class DefaultMarkup {

	public class Annotation {
		private final String name;
		private final boolean mandatory;
		private final Pattern pattern; // optional
		private final Collection<KnowWEObjectType> types = new LinkedList<KnowWEObjectType>();

		private Annotation(String name, boolean mandatory, Pattern pattern) {
			super();
			this.name = name;
			this.mandatory = mandatory;
			this.pattern = pattern;
		}

		/**
		 * Returns the name of the annotation. The name is the text after the
		 * &#64; that uniquely identify the annotation within a default mark-up.
		 * 
		 * @return the name of the annotation
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Returns whether the annotation is mandatory for the mark-up.
		 * 
		 * @return whether the annotation is mandatory
		 */
		public boolean isMandatory() {
			return this.mandatory;
		}

		/**
		 * Checks if the content of an annotation matches the annotations
		 * pattern.
		 * 
		 * @param annotationContent
		 *            the content string to be checked
		 * @return whether the annotations pattern is matched
		 */
		public boolean matches(String annotationContent) {
			if (pattern == null) return true;
			if (annotationContent == null) return false;
			return pattern.matcher(annotationContent).matches();
		}

		/**
		 * Return all {@link KnowWEObjectType}s that may be accepted as the
		 * content text of the annotation. These types will be used to
		 * sectionize, parse and render the annotations content text, if there
		 * is no other renderer/parser defined in the parent's
		 * {@link DefaultMarkupType}.
		 * <p>
		 * The annotation may also contain any other text. It will be recognized
		 * as {@link PlainText}, such in any other section or wiki-page. It is
		 * in responsibility of the {@link ReviseSubTreeHandler} of the
		 * {@link DefaultMarkupType} instance to check for non-allowed content.
		 * 
		 * @return the KnowWEObjectTypes of this annotation
		 */
		public KnowWEObjectType[] getTypes() {
			return this.types.toArray(new KnowWEObjectType[this.types.size()]);
		}

	}

	private final String name;
	private final Collection<KnowWEObjectType> types = new LinkedList<KnowWEObjectType>();
	private final Map<String, Annotation> annotations = new HashMap<String, Annotation>();

	public DefaultMarkup(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void addAnnotation(String name, boolean mandatory) {
		this.addAnnotation(name, mandatory, null);
	}

	public void addAnnotation(String name, boolean mandatory, Pattern pattern) {
		// do not allow duplicates
		String key = name.toLowerCase();
		if (annotations.containsKey(key)) {
			throw new IllegalArgumentException("annotation " + name + " already added");
		}
		// add new parameter
		Annotation annotation = this.new Annotation(name, mandatory, pattern);
		this.annotations.put(key, annotation);
	}

	public Annotation getAnnotation(String name) {
		String key = name.toLowerCase();
		return this.annotations.get(key);
	}

	public Annotation[] getAnnotations() {
		return this.annotations.values().toArray(new Annotation[this.annotations.size()]);
	}

	public void addAnnotationType(String name, KnowWEObjectType type) {
		Annotation annotation = getAnnotation(name);
		if (annotation == null) {
			throw new IllegalArgumentException("no such annotation defined: " + name);
		}
		annotation.types.add(type);
	}

	public void addContentType(KnowWEObjectType type) {
		this.types.add(type);
	}

	/**
	 * Return all {@link KnowWEObjectType}s that may be accepted as the content
	 * text of the mark-up. These types will be used to sectionize, parse and
	 * render the mark-up's content text, if there is no other renderer/parser
	 * defined in the parent's {@link DefaultMarkupType}.
	 * <p>
	 * The mark-up may also contain any other text. It will be recognized as
	 * {@link PlainText}, such in any other section or wiki-page. It is in
	 * responsibility of the {@link ReviseSubTreeHandler} of the
	 * {@link DefaultMarkupType} instance to check for non-allowed content.
	 * 
	 * @return the KnowWEObjectTypes of this mark-up
	 */
	public KnowWEObjectType[] getTypes() {
		return this.types.toArray(new KnowWEObjectType[this.types.size()]);
	}

}
