/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.rdfs.vis.edit;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdfs.vis.markup.VisualizationType;
import de.knowwe.rdfs.vis.util.Utils;

/**
 * @author Johanna Latt
 * @created 13.09.2014
 */
public class MakeNewVisualizationOfConceptAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionID = context.getParameter("kdomid");
		String conceptName = context.getParameter("concept");
		Section<?> section = Sections.get(sectionID);

		String annotation = DefaultMarkupType.getAnnotation(section, "concept");
		if (annotation != null) {
			annotation = conceptName;

			try {
				Sections.ReplaceResult rr = Sections.replace(context, DefaultMarkupType.getAnnotationContentSection(section, "concept")
						.getID(), annotation);

				if (rr != null) {
					Section<?> newSection = Sections.get(Utils.findNewIDFromRenderResult(rr));
					Section<VisualizationType> ancestorSection = Sections.ancestor(newSection, VisualizationType.class);
					context.getOutputStream().write(ancestorSection.getID().getBytes());
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		Compilers.awaitTermination(context.getArticleManager().getCompilerManager());
	}
}
