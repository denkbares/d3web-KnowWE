/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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
package de.knowwe.core.doc;

import java.io.IOException;

import com.denkbares.strings.Strings;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupDocumentation;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Answers with the documentation of a markup or of one of its annotations as an HTML fragment, see
 * {@link DefaultMarkupDocumentation}. The editor shows it when hovering a markup or annotation name or on F1.
 * Parameters are the <code>markup</code> name and optionally the <code>annotation</code> name. Answers 404 if the
 * markup or annotation is unknown.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.11.2026
 */
public class MarkupDocumentationAction extends AbstractAction {

	public static final String PARAM_MARKUP = "markup";
	public static final String PARAM_ANNOTATION = "annotation";

	/**
	 * Open to everybody: the documentation describes the markups installed in the wiki, which are the same for all
	 * users, and links a help page of the wiki only if the user may view it.
	 */
	@Override
	public Access requiredAccess() {
		return Access.NONE;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String markupName = Strings.trim(context.getParameter(PARAM_MARKUP));
		String annotationName = Strings.trim(context.getParameter(PARAM_ANNOTATION));
		DefaultMarkupType type = DefaultMarkupDocumentation.findMarkupType(context.getEnvironment(), markupName);
		if (type == null) {
			fail(context, 404, "Unknown markup: " + markupName);
		}

		HtmlElement documentation;
		if (Strings.isBlank(annotationName)) {
			documentation = DefaultMarkupDocumentation.renderMarkup(context, type, null);
		}
		else {
			DefaultMarkup.Annotation annotation = DefaultMarkupDocumentation.findAnnotation(type.getMarkup(), annotationName);
			if (annotation == null) {
				fail(context, 404, "Unknown annotation @" + annotationName + " of markup " + markupName);
			}
			documentation = DefaultMarkupDocumentation.renderAnnotation(context, type.getMarkup(), annotation);
		}
		context.setContentType(HTML);
		context.getWriter().write(documentation.toString());
	}
}
