/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.util.Icon;

public class DefaultMarkup implements Cloneable {

	private String name;
	private Collection<Type> types = new ArrayList<>();
	private Map<String, Annotation> annotations = new HashMap<>();
	private final Set<String> ignoredAnnotations = new HashSet<>();
	private String deprecatedAlternative = null;
	private boolean isInline = false;
	private String documentation = null;

	public DefaultMarkup(String name) {
		this.name = name;
	}

	public DefaultMarkup copy() {
		DefaultMarkup clone = new DefaultMarkup(this.name);
		clone.types = new ArrayList<>(this.types);
		clone.annotations = new HashMap<>(this.annotations);
		clone.deprecatedAlternative = this.deprecatedAlternative;
		clone.isInline = this.isInline;
		clone.documentation = this.documentation;
		return clone;
	}

	/**
	 * Returns the name of this markup.
	 *
	 * @return the markup's name
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets some documentation text for this markup. Set the documentation to 'null' to
	 * remove the documentation.
	 *
	 * @param documentation the documentation to be set
	 */
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	/**
	 * Returns the documentation of this markup instance. It returns 'null' if no specific documentation
	 * is available.
	 *
	 * @return the documentation of this markup
	 */
	public String getDocumentation() {
		return documentation;
	}

	/**
	 * Adds a non mandatory new annotation to the markup.
	 *
	 * @param name the name of the annotation to be added
	 */
	public void addAnnotation(String name) {
		this.addAnnotation(name, false, (Pattern) null);
	}

	/**
	 * Adds a new annotation to the markup.
	 *
	 * @param name      the name of the annotation to be added
	 * @param mandatory if the annotation is required for the markup
	 */
	public void addAnnotation(String name, boolean mandatory) {
		this.addAnnotation(name, mandatory, (Pattern) null);
	}

	/**
	 * Adds a new annotation to the markup, where the name is a regular
	 * expression. All annotations with names matching this expression will be
	 * found and allowed.
	 *
	 * @param regex     the regex of the name of the annotations to be added
	 * @param mandatory if the annotation is required for the markup
	 */
	public void addRegexAnnotation(String regex, boolean mandatory) {
		//noinspection RedundantCast
		this.addAnnotation(regex, mandatory, true, (Pattern) null);
	}

	/**
	 * Adds a new annotation to the markup with a fixed list of possible values
	 * (enumeration).
	 *
	 * @param name       the name of the annotation to be added
	 * @param mandatory  if the annotation is required for the markup
	 * @param enumValues the allowed values for the annotation
	 */
	public void addAnnotation(String name, boolean mandatory,
							  String... enumValues) {
		String regex = "^(" + Strings.concat("|", enumValues)
				+ ")$";
		int flags = Pattern.CASE_INSENSITIVE;
		addAnnotation(name, mandatory, Pattern.compile(regex, flags));
	}

	public void addAnnotationIcon(String name, Icon icon) {
		addAnnotationNameType(name, new IconType(icon));
	}

	/**
	 * Adds a new annotation to the markup with a fixed list of possible values
	 * (enumeration).
	 *
	 * @param name       the name of the annotation to be added
	 * @param mandatory  if the annotation is required for the markup
	 * @param enumValues the allowed values for the annotation
	 * @deprecated use {@link #addAnnotation(String, boolean, Class)} instead
	 */
	public void addAnnotation(String name, boolean mandatory, Enum<?>... enumValues) {
		String regex = "^(" + Strings.concat("|", enumValues) + ")$";
		int flags = Pattern.CASE_INSENSITIVE;
		addAnnotation(name, mandatory, Pattern.compile(regex, flags));
	}

	/**
	 * Adds a new annotation to the markup with a fixed list of possible values
	 * (enumeration).
	 *
	 * @param name      the name of the annotation to be added
	 * @param mandatory if the annotation is required for the markup
	 * @param enumClass the allowed values for the annotation
	 */
	public void addAnnotation(String name, boolean mandatory, Class<? extends Enum<?>> enumClass) {
		Enum<?>[] enumConstants = enumClass.getEnumConstants();
		//noinspection deprecation
		addAnnotation(name, mandatory, enumConstants);
	}

	/**
	 * Adds a new annotation to the markup with a pattern to specify the values
	 * allowed for this annotation.
	 *
	 * @param name      the name of the annotation to be added
	 * @param mandatory if the annotation is required for the markup
	 * @param pattern   a regular expression to check the allowed values
	 */
	public void addAnnotation(String name, boolean mandatory, Pattern pattern) {
		this.addAnnotation(name, mandatory, false, pattern);
	}

	/**
	 * Adds a new annotation to the markup with a pattern to specify the values
	 * allowed for this annotation.
	 *
	 * @param name      the name of the annotation to be added
	 * @param mandatory if the annotation is required for the markup
	 * @param isRegex   if the given name is a regex allowing a range of
	 *                  annotations
	 * @param pattern   a regular expression to check the allowed values
	 */
	public void addAnnotation(String name, boolean mandatory, boolean isRegex, Pattern pattern) {
		// do not allow duplicates
		String key = name.toLowerCase();
		if (annotations.containsKey(key)) {
			throw new IllegalArgumentException("annotation " + name
					+ " already added");
		}
		// add new parameter
		Annotation annotation = new Annotation(name, mandatory, isRegex, pattern);
		this.annotations.put(key, annotation);
	}

	public Annotation getAnnotation(String name) {
		String key = name.toLowerCase();
		return this.annotations.get(key);
	}

	/**
	 * Returns an array of all annotations of a specific markup. If the markup
	 * has no annotations defined, an empty array is returned.
	 *
	 * @return the annotations of the markup
	 */
	public Annotation[] getAnnotations() {
		return this.annotations.values().toArray(
				new Annotation[this.annotations.size()]);
	}

	/**
	 * Add completed annotations. Should only be used to reuse annotations of other markups.
	 */
	public void addAnnotation(Annotation... annotations) {
		for (Annotation annotation : annotations) {
			this.annotations.put(annotation.getName().toLowerCase(), annotation);
		}
	}

	public void addAnnotationContentType(String name, Type type) {
		Annotation annotation = getAnnotationHandleIAE(name);
		annotation.types.add(type);
	}

	public void addAnnotationNameType(String name, Type type) {
		Annotation annotation = getAnnotationHandleIAE(name);
		annotation.nameTypes.add(type);
	}

	public void addAnnotationRenderer(String name, Renderer renderer) {
		Annotation annotation = getAnnotationHandleIAE(name);
		annotation.renderer = renderer;
	}

	private Annotation getAnnotationHandleIAE(String name) {
		Annotation annotation = getAnnotation(name);
		if (annotation == null) {
			throw new IllegalArgumentException("no such annotation defined: " + name);
		}
		return annotation;
	}

	public void addContentType(Type type) {
		this.types.add(type);
	}

	/**
	 * Return all {@link Type}s that may be accepted as the content text of the
	 * mark-up. These types will be used to sectionize, parse and render the
	 * mark-up's content text, if there is no other renderer/parser defined in
	 * the parent's {@link DefaultMarkupType}.
	 * <p>
	 * The mark-up may also contain any other text. It will be recognized as
	 * {@link PlainText}, such in any other section or wiki-page. It is in
	 * responsibility of the {@link de.knowwe.core.compile.CompileScript} of the
	 * {@link DefaultMarkupType} instance to check for non-allowed content.
	 *
	 * @return the Types of this mark-up
	 */
	public Type[] getTypes() {
		return this.types.toArray(new Type[this.types.size()]);
	}

	public Set<String> getIgnoredAnnotations() {
		return Collections.unmodifiableSet(this.ignoredAnnotations);
	}

	public void setAnnotationDeprecated(String annotationName) {
		Annotation annotation = getAnnotation(annotationName);
		if (annotation != null) {
			annotation.deprecated = true;
		}
		else {
			throw new IllegalArgumentException("Annotation \"" + annotationName + "\" not defined.");
		}
	}

	/**
	 * Sets this markup as deprecated. Set the name for the alternative markup
	 * replacing this deprecated one.
	 *
	 * @param alternative the alternative markup replacing this deprecated one
	 * @created 26.02.2013
	 */
	public void setDeprecated(String alternative) {
		this.deprecatedAlternative = alternative;
	}

	public boolean isDeprecated() {
		return deprecatedAlternative != null;
	}

	public String getDeprecatedAlternative() {
		return deprecatedAlternative;
	}

	/**
	 * Returns if this markup shall be used as an inline markup, which means
	 * that the markup is used in a line containing other wiki text. In this
	 * case the markup still starts with a "%%" and is terminated either with
	 * "%%" oder "/%" (sourrounded by white spaces).
	 *
	 * @return if the markup shall be used inline
	 * @created 11.11.2013
	 */
	public boolean isInline() {
		return isInline;
	}

	/**
	 * Specifies if this markup shall be used as an inline markup, which means
	 * that the markup is used in a line containing other wiki text. In this
	 * case the markup still starts with a "%%" and is terminated either with
	 * "%%" or "/%" (sourrounded by white spaces).
	 *
	 * @param isInline if the markup shall be used inline
	 * @created 11.11.2013
	 */
	public void setInline(boolean isInline) {
		this.isInline = isInline;
	}

	/**
	 * Specifies annotations that should be ignored while parsing the default markup. Use this method if you have
	 * annotation like markup in your content type and don't want it to be recognized as an annotation. This will
	 * however not work, if another annotation comes prior to the ignored annotation, the content type will end at the
	 * normal annotation...
	 *
	 * @param name the name of the annotation to ignore
	 */
	public void ignoreAnnotation(String name) {
		this.ignoredAnnotations.add(name);
	}

	private static class IconType extends AbstractType {

		public IconType(Icon icon) {
			this.setSectionFinder(AllTextFinder.getInstance());
			this.setRenderer((section, user, result) -> result.appendHtml(icon.toHtml()));
		}

	}

	public class Annotation {

		private final String name;
		private final boolean mandatory;
		private final Pattern pattern; // optional
		private final boolean isRegex;
		private boolean deprecated;

		private String documentation = null;

		private final Collection<Type> nameTypes = new LinkedList<>();
		private final Collection<Type> types = new LinkedList<>();

		private Renderer renderer = null;

		private Annotation(String name, boolean mandatory, boolean isRegex, Pattern pattern) {
			super();
			this.name = name;
			this.mandatory = mandatory;
			this.isRegex = isRegex;
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
		 * Returns the documentation of the annotation. Returns 'null' if no specific
		 * documentation for that annotation is defined.
		 *
		 * @return the name of the annotation
		 */
		public String getDocumentation() {
			return documentation;
		}

		/**
		 * Sets the documentation for this annotation. Set 'null' to remove the documentation.
		 *
		 * @param documentation the documentation to be set
		 */
		public void setDocumentation(String documentation) {
			this.documentation = documentation;
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
		 * Returns whether the annotation is deprecated
		 *
		 * @return true if the annotation is deprecated, false otherwise
		 * @created 18.04.2012
		 */
		public boolean isDeprecated() {
			return deprecated;
		}

		/**
		 * Returns whether this annotations is described with a regular
		 * expression instead of name string.
		 *
		 * @return if the annotation is a regex
		 * @created 05.06.2013
		 */
		public boolean isRegex() {
			return isRegex;
		}

		/**
		 * Checks if the content of an annotation matches the annotations
		 * pattern.
		 *
		 * @param annotationContent the content string to be checked
		 * @return whether the annotations pattern is matched
		 */
		@SuppressWarnings("SimplifiableIfStatement")
		public boolean matches(String annotationContent) {
			if (pattern == null) return true;
			if (annotationContent == null) return false;
			return pattern.matcher(annotationContent).matches();
		}

		/**
		 * Return all {@link Type}s that may be accepted as the content text of
		 * the annotation. These types will be used to sectionize (parse) and
		 * render the annotations content text, if there is no other
		 * renderer/parser defined in the parent's {@link DefaultMarkupType}.
		 * <p>
		 * The annotation may also contain any other text. It will be recognized
		 * as {@link PlainText}, such in any other section or wiki-page. It is
		 * in responsibility of the {@link de.knowwe.core.compile.CompileScript} of the
		 * {@link DefaultMarkupType} instance to check for non-allowed content.
		 *
		 * @return the Types of this annotation
		 */
		public Type[] getContentTypes() {
			return this.types.toArray(new Type[this.types.size()]);
		}

		public Type[] getNameTypes() {
			return this.nameTypes.toArray(new Type[this.nameTypes.size()]);
		}

		public Renderer getRenderer() {
			return renderer;
		}

		public Pattern getPattern() {
			return pattern;
		}
	}

}
