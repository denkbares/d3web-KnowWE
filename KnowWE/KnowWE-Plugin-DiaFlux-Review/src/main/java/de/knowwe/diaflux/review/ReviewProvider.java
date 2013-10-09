/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.knowwe.diaflux.review;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.DiaFluxTraceHighlight;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * 
 * @author Reinhard Hatko
 * @created 17.10.2012
 */
public class ReviewProvider implements ToolProvider {

	public static final String TOOL_CATEGORY = "Review";
	public static final String ICON_PATH = "KnowWEExtension/images/review.png";

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] { getHighlightTool(section, userContext) };
	}

	protected Tool getHighlightTool(Section<?> section, UserContext userContext) {

		boolean highlightActive =
				DiaFluxTraceHighlight.checkForHighlight(userContext,
						DiaFluxReviewHighlight.REVIEW_HIGHLIGHT);

		if (highlightActive) {
			String jsAction = DiaFluxTraceHighlight.getDeactivationJSAction();
			return new DefaultTool(
					ICON_PATH,
					"Hide reviews",
					"Hides the reviews for this flowchart.",
					jsAction, TOOL_CATEGORY);
		}
		else {
			String jsAction = DiaFluxTraceHighlight.getActivationJSAction(DiaFluxReviewHighlight.REVIEW_HIGHLIGHT);
			return new DefaultTool(
					ICON_PATH,
					"Show reviews",
					"Shows the reviews for this flowchart.",
					jsAction, TOOL_CATEGORY);
		}
	}

}
