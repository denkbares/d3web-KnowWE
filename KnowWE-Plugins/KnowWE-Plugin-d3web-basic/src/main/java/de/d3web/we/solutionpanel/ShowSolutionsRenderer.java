/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.d3web.we.solutionpanel;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Displays a configurable pane presenting derived solutions and abstractions.
 * The following options are available:
 * <ul>
 * <li>@show_established: true/false
 * <li>@show_suggested: true/false
 * <li>@show_excluded: true/false
 * <li>@show_abstractions: true/false
 * <li>@only_derivations: parent name
 * <li>@except_derivations: parent name
 * <li>@master: Name of the article with the knowledge base
 * </ul>
 * 
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 15.10.2010
 */
public class ShowSolutionsRenderer extends DefaultMarkupRenderer {

	@Override
	public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
		// only render the content section
		Section<?> child = DefaultMarkupType.getContentSection(section);
		result.append(child, user);
	}

}
