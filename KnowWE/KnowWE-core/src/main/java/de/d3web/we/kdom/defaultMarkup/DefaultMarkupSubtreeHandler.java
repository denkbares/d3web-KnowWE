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

package de.d3web.we.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup.Annotation;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class DefaultMarkupSubtreeHandler extends SubtreeHandler<DefaultMarkupType> {

	private final DefaultMarkup markup;

	public DefaultMarkupSubtreeHandler(DefaultMarkup markup) {
		super(true);
		this.markup = markup;
	}

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<DefaultMarkupType> markupSection) {

		List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();

		// check defined annotations
		for (Annotation annotation : this.markup.getAnnotations()) {
			String name = annotation.getName();
			Section<? extends AnnotationType> annotationSection =
					DefaultMarkupType.getAnnotationSection(markupSection, name);

			// check existence of mandatory annotation
			if (annotationSection == null && annotation.isMandatory()) {
				KDOMReportMessage message = new SimpleMessageError("The annotation @" + name
						+ " is mandatory, but missing. Please specify that annotation.");
				msgs.add(message);
			}
		}

		// register section in the package manager
		// TODO: refactor this to somewhere else
		if (!markupSection.get().isIgnoringPackageCompile()) {
			String value = null;
			Annotation packageAnno = this.markup.getAnnotation(KnowWEPackageManager.ATTRIBUTE_NAME);
			if (packageAnno != null) {
				Section<? extends AnnotationType> annotationSection =
						DefaultMarkupType.getAnnotationSection(markupSection, packageAnno.getName());
				if (annotationSection != null) {
					value = annotationSection.getOriginalText();
				}
			}
			KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb()).addSectionToPackage(
					markupSection, value);
		}

		// check unrecognized annotations
		List<Section<UnknownAnnotationType>> unknownSections = Sections.findChildrenOfType(
				markupSection, UnknownAnnotationType.class);
		for (Section<UnknownAnnotationType> annotationSection : unknownSections) {
			String name = UnknownAnnotationType.getName(annotationSection);
			KDOMReportMessage message = new SimpleMessageError("The annotation @" + name
					+ " is not known to KnowWE. It will be ignored.");
			msgs.add(message);
		}

		// check annotated sections
		List<Section<AnnotationType>> subSections = Sections.findChildrenOfType(markupSection,
				AnnotationType.class);
		for (Section<AnnotationType> annotationSection : subSections) {
			// check annotations pattern
			Annotation annotation = annotationSection.get().getAnnotation();
			String text = annotationSection.getOriginalText();
			if (!annotation.matches(text)) {
				String name = annotation.getName();
				KDOMReportMessage message = new SimpleMessageError("The value of annotation @"
						+ name
						+ " is invalid: " + text);
				msgs.add(message);
			}
		}
		return msgs;
	}

	@Override
	public void destroy(KnowWEArticle article, Section<DefaultMarkupType> markupSection) {
		// unregister section in the package manager
		// TODO: refactor this to somewhere else
		if (!markupSection.get().isIgnoringPackageCompile()) {
			KnowWEEnvironment.getInstance().getPackageManager(article.getWeb()).removeSectionFromAllPackages(
						markupSection);
		}
	}
}
