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

import java.util.regex.Pattern;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.AnchorRenderer;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Messages;

public class AnnotationType extends AbstractType {

	private final DefaultMarkup.Annotation annotation;

	private static final String REGEX = "$LINESTART$\\s*(@$NAME$[:=\\s].*?)\\s*?(?=$LINESTART$\\s*@|^/?%|$LINESTART$%%|$LINESTART$/%|\\z)";

	private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
			| Pattern.DOTALL;

	public AnnotationType(DefaultMarkup.Annotation annotation) {
		this.annotation = annotation;
		this.setSectionFinder(new AdaptiveMarkupFinder(annotation.getName(), REGEX, FLAGS, 1, false));
		this.addChildType(new AnnotationNameType(annotation));
		this.addChildType(new AnnotationContentType(annotation));
		this.addCompileScript(Priority.HIGHEST, new CheckContentExistsHandler());
		Renderer renderer = annotation.getRenderer();
		if (renderer == null) {
			renderer = AnchorRenderer.getDelegateInstance();
		}
		this.setRenderer(renderer);
	}

	/**
	 * Returns the name of the underlying annotation defined.
	 * 
	 * @return the annotation's name
	 */
	@Override
	public String getName() {
		return this.annotation.getName();
	}

	/**
	 * Returns the underlying annotation.
	 * 
	 * @return the underlying annotation
	 */
	public DefaultMarkup.Annotation getAnnotation() {
		return annotation;
	}

	private static class CheckContentExistsHandler extends DefaultGlobalScript<AnnotationType> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<AnnotationType> section) {
			Section<AnnotationContentType> content = Sections.successor(section,
					AnnotationContentType.class);
			if (content == null) {
				Messages.storeMessage(section, this.getClass(),
						Messages.error("No content found for annotation '"
								+ section.getText() + "'"));
			}
		}
	}

}
