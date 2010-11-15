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

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup.Annotation;
import de.d3web.we.kdom.packaging.CompileFlag;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class DefaultMarkupSubtreeHandler extends SubtreeHandler<DefaultMarkupType> {

	private final DefaultMarkup markup;

	public DefaultMarkupSubtreeHandler(DefaultMarkup markup) {
		super(true);
		this.markup = markup;
	}


	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<DefaultMarkupType> markupSection) {

		List<Message> msgs = new ArrayList<Message>();

		// check defined annotations
		for (Annotation annotation : this.markup.getAnnotations()) {
			String name = annotation.getName();
			Section<? extends AnnotationType> annotationSection =
					DefaultMarkupType.getAnnotationSection(markupSection, name);

			// check existence of mandatory annotation
			if (annotationSection == null && annotation.isMandatory()) {
				Message message = new Message(Message.ERROR, "The annotation @" + name
						+ " is mandatory, but missing. Please specify that annotation.", "", -1, "");
				msgs.add(message);
			}
		}

		// TODO: refactor this to somewhere else
		if (markupSection.getTitle().equals(article.getTitle())) {
			String value = null;
			Annotation packageAnno = this.markup.getAnnotation(KnowWEPackageManager.ATTRIBUTE_NAME);
			if (packageAnno != null) {
				Section<? extends AnnotationType> annotationSection =
						DefaultMarkupType.getAnnotationSection(markupSection, packageAnno.getName());
				if (annotationSection != null) {
					value = annotationSection.getOriginalText();

				}
			}
			if (value == null) value = KnowWEPackageManager.DEFAULT_PACKAGE;

			if (!markupSection.get().getMarkup().getName().equals(CompileFlag.MARKUP_NAME)) {
				markupSection.addPackageName(value);
				KnowWEEnvironment.getInstance().getPackageManager(article.getWeb()).registerPackageDefinition(
						markupSection);
			}
		}

		// check unrecognized annotations
		List<Section<UnknownAnnotationType>> unknownSections = markupSection.findChildrenOfType(UnknownAnnotationType.class);
		for (Section<UnknownAnnotationType> annotationSection : unknownSections) {
			String name = UnknownAnnotationType.getName(annotationSection);
			Message message = new Message(Message.WARNING, "The annotation @" + name
					+ " is not known to KnowWE. It will be ignored.", "", -1, "");
			msgs.add(message);
		}

		// check annotated sections
		List<Section<AnnotationType>> subSections = markupSection.findChildrenOfType(AnnotationType.class);
		for (Section<AnnotationType> annotationSection : subSections) {
			// check annotations pattern
			Annotation annotation = annotationSection.getObjectType().getAnnotation();
			String text = annotationSection.getOriginalText();
			if (!annotation.matches(text)) {
				String name = annotation.getName();
				Message message = new Message(Message.ERROR, "The value of annotation @" + name
						+ " is invalid: " + text, "", -1, "");
				msgs.add(message);
			}
		}
		if (!msgs.isEmpty()) AbstractKnowWEObjectType.storeMessages(article, markupSection,
				this.getClass(), msgs);

		return null;
	}

	@Override
	public void destroy(KnowWEArticle article, Section<DefaultMarkupType> markupSection) {
		// TODO: refactor this to somewhere else
		if (markupSection.getTitle().equals(article.getTitle())
				&& !markupSection.get().getMarkup().getName().equals(CompileFlag.MARKUP_NAME)) {
				KnowWEEnvironment.getInstance().getPackageManager(article.getWeb()).unregisterPackageDefinition(
						markupSection);
		}
	}
}
