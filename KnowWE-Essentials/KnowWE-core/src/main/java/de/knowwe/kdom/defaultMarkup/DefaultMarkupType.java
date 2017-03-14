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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.AttachmentManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.attachment.AttachmentMarkup;
import de.knowwe.tools.ToolMenuDecoratingRenderer;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static java.util.stream.Collectors.toList;

/**
 * This class represents a section of the top-level default markup. That markup always starts with
 * "%%" followed by an alpha-numerical string. After that an optional ":" is allowed. This is
 * followed by either a one-line declaration or a multiple-line-block terminated by an "/%" denoted
 * in a line with no other content. Because of backward-compatibility reasons, the
 * multiple-line-block can also be terminated by an single "%".
 * <p/>
 * Within the block declaration you may use java-style end-line comments. Within the single-line
 * declaration you may also use this comments at the end of the line. Note: there are no block
 * comments ("/ * ... * /") allowed.
 * <p/>
 * It is also allowed to define multiple additional annotations. An annotation is denoted as "@",
 * followed by its name without spacing. This annotation-header may optionally followed by a ":" or
 * "=". The content of the parameter goes until a new parameter is defined or the markup block is
 * terminated.
 * <p/>
 * <b>Examples:</b>
 * <p/>
 * <pre>
 * %%rule &lt;condition&gt; --> &lt;action&gt;
 *
 * %%rule // define 2 rules in one block
 *   &lt;condition&gt; --> &lt;action&gt;
 *   &lt;condition&gt; --> &lt;action&gt;
 * /%
 *
 * %%rule // use annotations
 *   &lt;condition&gt; --> &lt;action&gt;
 *   &lt;condition&gt; --> &lt;action&gt;
 *   &#64;lazy: create
 * /%
 * </pre>
 * <p/>
 * <p/>
 * The default mark-up forms a KDOM of the following structure. Please not that there might be any
 * PlainText section in between at any level:
 * <p/>
 * <pre>
 * Section&lt;DefaultMarkupType&gt; // %%rule
 * |
 * +--Section&lt;ContentType&gt;
 * |  |
 * |  +--Rule-Block..1  // &lt;condition&gt; --> &lt;action&gt;
 * |  |
 * |  +--[Comment Text]
 * |  |
 * |  +--Rule-Block..n // &lt;condition&gt; --> &lt;action&gt;
 * |  |
 * |  +--...
 * |
 * +--Section&lt;AnnotationType&gt; // &#64;lazy: true
 * |
 * +--Section&lt;AnnotationType&gt; // &#64;...
 * |
 * +--...
 * </pre>
 *
 * @author Volker Belli
 */
public class DefaultMarkupType extends AbstractType {

	private final static int FLAGS =
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;

	private final static String SECTION_REGEXP_BLOCK =
			// Declaration
			"^\\p{Blank}*%%$NAME$\\p{Blank}*" +
					"(?:" +
					// multi-line content with termination starts with an
					// empty rest of the line (only comment is allowed)
					// and followed by any text terminated by "/%" or a
					// single "%" in a line with no other content
					"(?:[:=]?\\p{Blank}*(?://[^$]*?)?$" +
					// only comment allowed before end-of-line
					"(.*?)" + // CONTENT --> anything in multiple lines
					// (reluctant match)
					"^\\p{Blank}*/?%\\s*?(^|\\z)" + // "/%" or "%" in a line
					")" +
					// or single-line content with termination
					"|(?:" +
					// at least one non-whitespace character followed by any
					// non-line-break item
					"[:=\\p{Blank}]\\p{Blank}*([^/][^$]*?(^|\\z))" + // CONTENT
					// --> anything in a single line (reluctant match)
					"))";

	private final static String SECTION_REGEXP_INLINE =
			// Declaration
			"%%$NAME$\\p{Blank}*" +
					// single-line content with termination
					// at least one non-whitespace character followed by any
					// non-line-break item
					"[:=\\p{Blank}]\\p{Blank}*([^/][^$]*?[%/]%)"; // CONTENT

	private DefaultMarkup markup;
	private PackageManager packageManager;

	public DefaultMarkupType(DefaultMarkup markup) {
		applyMarkup(markup);
	}

	public DefaultMarkupType() {

	}

	public void applyMarkup(DefaultMarkup markup) {
		this.markup = markup;
		this.setRenderer(markup.isInline()
				? new ToolMenuDecoratingRenderer(DelegateRenderer.getInstance())
				: new DefaultMarkupRenderer());
		Pattern pattern = getPattern(markup.getName(), markup.isInline());
		this.setSectionFinder(new RegexSectionFinder(pattern, 0));
		// add children
		this.addChildType(new ContentType(markup));
		for (DefaultMarkup.Annotation parameter : markup.getAnnotations()) {
			// update KDOM structure for the annotations
			this.addChildType(new AnnotationType(parameter));
		}
		this.addChildType(10, new UnknownAnnotationType());
		this.addCompileScript(new DefaultMarkupPackageRegistrationScript());
		this.addCompileScript(new DefaultMarkupCompileScript(markup));
		this.addCompileScript(new DefaultMarkupPackageReferenceRegistrationScript());
	}

	@Override
	public String getName() {
		return this.markup.getName();
	}

	public DefaultMarkup getMarkup() {
		return this.markup;
	}

	/**
	 * Returns the contents of the default content block of the specified section. If the section is
	 * not of type "DefaultMarkup" an IllegalArgumentException is thrown.
	 *
	 * @param section the section to take the content block from
	 * @return the contents of the content block, if the section is not null. An empty string
	 * otherwise.
	 * @throws IllegalArgumentException if the specified section is not of {@link
	 *                                  DefaultMarkupType}
	 */
	@NotNull
	public static String getContent(Section<?> section) {
		Section<? extends ContentType> contentSection = getContentSection(section);
		if (contentSection != null) {
			return contentSection.getText();
		}
		else {
			return "";
		}
	}

	/**
	 * Returns the contents section of the default content block of the specified section. If the
	 * section is not of type "DefaultMarkup" an IllegalArgumentException is thrown.
	 *
	 * @param section the section to take the content section from
	 * @return the content section
	 * @throws IllegalArgumentException if the specified section is not of {@link
	 *                                  DefaultMarkupType}
	 */
	@Nullable
	public static Section<? extends ContentType> getContentSection(Section<?> section) {
		if (!DefaultMarkupType.class.isAssignableFrom(section.get().getClass())) {
			throw new IllegalArgumentException("section not of type DefaultMarkupType");
		}
		return Sections.child(section, ContentType.class);
	}

	/**
	 * Returns the ContentType of a DefaultMarkupType
	 *
	 * @param defaultMarkupType {@link DefaultMarkupType}
	 * @return {@link ContentType} or null, if there is no {@link ContentType} as childtype
	 * @created 13.03.2012
	 */
	@Nullable
	public static ContentType getContentType(DefaultMarkupType defaultMarkupType) {
		for (Type type : defaultMarkupType.getChildrenTypes()) {
			if (type instanceof ContentType) {
				return (ContentType) type;
			}
		}
		return null;
	}

	/**
	 * Returns the content of the first annotation section of the specified name. If the section is
	 * not of type "DefaultMarkup" an IllegalArgumentException is thrown. If there is no annotation
	 * section with the specified name, null is returned.
	 *
	 * @param section the section to be searched
	 * @param name    the name of the annotation
	 * @return the content string of the annotation
	 * @throws IllegalArgumentException if the specified section is not of {@link
	 *                                  DefaultMarkupType}
	 */
	@Nullable
	public static String getAnnotation(Section<?> section, String name) {
		Section<?> annotationSection = getAnnotationContentSection(section, name);
		if (annotationSection == null) return null;
		return annotationSection.getText();
	}

	/**
	 * Returns the content of the annotation sections using the specified name. If the sections are
	 * not of type "DefaultMarkup" an IllegalArgumentException is thrown. If there is no annotation
	 * section with the specified name, an empty array is returned.
	 *
	 * @param section the section to be searched
	 * @param name    the name of the annotation
	 * @return the content strings of the found annotation
	 * @created 26.01.2011
	 */
	@NotNull
	public static String[] getAnnotations(Section<?> section, String name) {
		List<Section<? extends AnnotationContentType>> annotationSections = getAnnotationContentSections(section, name);
		String[] result = new String[annotationSections.size()];
		int position = 0;
		for (Section<?> aSection : annotationSections) {
			result[position++] = aSection.getText();
		}
		return result;
	}

	/**
	 * Returns the content section of the first annotation with the specified name. If the section
	 * is not of type "DefaultMarkup" an IllegalArgumentException is thrown. If there is no
	 * annotation with the specified name, null is returned.
	 *
	 * @param section the section to be searched
	 * @param name    the name of the annotation
	 * @return the annotation section
	 * @throws IllegalArgumentException if the specified section is not of {@link
	 *                                  DefaultMarkupType}
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static Section<? extends AnnotationContentType> getAnnotationContentSection(Section<? extends Type> section, String name) {
		if (!DefaultMarkupType.class.isAssignableFrom(section.get().getClass())) {
			throw new IllegalArgumentException("section not of type DefaultMarkupType");
		}

		for (Section<? extends Type> child : findAnnotationContentTypes(section)) {
			Section<AnnotationContentType> annotationContent = (Section<AnnotationContentType>) child;
			String annotationName = annotationContent.get().getName(annotationContent);
			if (annotationName.equalsIgnoreCase(name)) {
				return (Section<? extends AnnotationContentType>) child;
			}
		}
		return null;
	}

	/**
	 * Returns the content section of all annotations section of the specified name. If the section
	 * is not of type "DefaultMarkup" an IllegalArgumentException is thrown. If there is no
	 * annotation with the specified name, an empty list is returned.
	 *
	 * @param section the section to be searched
	 * @param name    the name of the annotation
	 * @return the list of annotation sections
	 * @throws IllegalArgumentException if the specified section is not of {@link
	 *                                  DefaultMarkupType}
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public static List<Section<? extends AnnotationContentType>> getAnnotationContentSections(Section<?> section, String name) {
		return getAnnotationContentSections(section, new String[] { name });
	}

	/**
	 * Returns the content section of all annotations section of the specified name. If the section
	 * is not of type "DefaultMarkup" an IllegalArgumentException is thrown. If there is no
	 * annotation with the specified name, an empty list is returned.
	 *
	 * @param section the section to be searched
	 * @param names   the names of the annotations to be returned
	 * @return the list of annotation sections
	 * @throws IllegalArgumentException if the specified section is not of {@link
	 *                                  DefaultMarkupType}
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public static List<Section<? extends AnnotationContentType>> getAnnotationContentSections(Section<?> section, String... names) {
		if (!DefaultMarkupType.class.isAssignableFrom(section.get().getClass())) {
			throw new IllegalArgumentException("section not of type DefaultMarkupType");
		}
		List<Section<? extends AnnotationContentType>> results = new ArrayList<>();
		for (Section<? extends Type> child : findAnnotationContentTypes(section)) {
			Section<AnnotationContentType> annotationContent = (Section<AnnotationContentType>) child;
			String annotationName = annotationContent.get().getName(annotationContent);
			for (String name : names) {
				if (annotationName.equalsIgnoreCase(name)) {
					results.add((Section<? extends AnnotationContentType>) child);
					break;
				}
			}
		}
		return results;
	}

	/**
	 * Returns the packages the given default markup section belongs to according to its @package
	 * annotations. If there are no such annotations, the default packages for the article are
	 * returned.
	 *
	 * @param section the section to be check for packages
	 * @created 11.03.2012
	 */
	@NotNull
	public static String[] getPackages(Section<?> section) {
		return getPackages(section, PackageManager.PACKAGE_ATTRIBUTE_NAME);
	}

	/**
	 * Returns the packages the given default markup section belongs to according to the defined
	 * annotations. If there are no such annotations, the default packages for the article are
	 * returned.
	 * In case the section is part of an article based on a compiled attachment, we also check the compiling
	 * %%Attachment markups for packages.
	 *
	 * @param section the section to be check for packages
	 * @created 12.03.2012
	 */
	@NotNull
	public static String[] getPackages(Section<?> section, String annotation) {
		if (!DefaultMarkupType.class.isAssignableFrom(section.get().getClass())) {
			throw new IllegalArgumentException("section not of type DefaultMarkupType");
		}
		String[] packageNames = DefaultMarkupType.getAnnotations(section, annotation);
		if (packageNames.length == 0) {
			packageNames = KnowWEUtils.getPackageManager(section).getDefaultPackages(section.getArticle());
		}
		// if we only have the default package, check if this is an article based on an compiled attachment
		// and if yes, get packages from compiling %%Attachment markups
		if (packageNames.length == 1 && packageNames[0].equals(PackageManager.DEFAULT_PACKAGE)) {
			ArticleManager articleManager = section.getArticleManager();
			if (!(articleManager instanceof DefaultArticleManager)) return packageNames;
			AttachmentManager attachmentManager = ((DefaultArticleManager) articleManager).getAttachmentManager();
			String[] collectedPackageNames = attachmentManager.getCompilingAttachmentSections(section
					.getArticle())
					.stream()
					.map(s -> $(s).ancestor(AttachmentMarkup.class).getFirst())
					.filter(Objects::nonNull)
					.map(DefaultMarkupType::getPackages).flatMap(Arrays::stream)
					.collect(toList()).toArray(new String[0]);
			if (collectedPackageNames.length > 0) packageNames = collectedPackageNames;
		}
		return packageNames;
	}

	/**
	 * Returns the content section of all annotations sections in this section. If the section is
	 * not of type "DefaultMarkup" an IllegalArgumentException is thrown.
	 *
	 * @param section the section to be searched
	 * @return the list of annotation sections
	 * @throws IllegalArgumentException if the specified section is not of {@link
	 *                                  DefaultMarkupType}
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public static List<Section<? extends AnnotationContentType>> getAllAnnotationContentSections(Section<?> section) {
		if (!DefaultMarkupType.class.isAssignableFrom(section.get().getClass())) {
			throw new IllegalArgumentException("section not of type DefaultMarkupType");
		}
		List<Section<? extends AnnotationContentType>> results = new ArrayList<>();
		for (Section<? extends Type> child : findAnnotationContentTypes(section)) {
			results.add((Section<? extends AnnotationContentType>) child);

		}
		return results;
	}

	private static List<Section<AnnotationContentType>> findAnnotationContentTypes(Section<? extends Type> section) {
		return Sections.successors(section, AnnotationContentType.class);
	}

	/**
	 * Returns the pattern to match a default mark-up section of a specified name.
	 *
	 * @param name the name of the section ("%%&lt;name&gt;")
	 * @return the pattern to match the complete section
	 */
	public static Pattern getPattern(String name, boolean isInline) {
		String regex = isInline ? SECTION_REGEXP_INLINE : SECTION_REGEXP_BLOCK;
		String regexp = regex.replace("$NAME$", name);
		return Pattern.compile(regexp, FLAGS);
	}

}
