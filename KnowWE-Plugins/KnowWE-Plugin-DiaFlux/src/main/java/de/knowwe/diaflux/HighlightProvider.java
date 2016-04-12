/*
 * Copyright (C) 2011 denkbares GmbH
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
package de.knowwe.diaflux;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Enables highlighting of active nodes and edges
 *
 * @author Markus Friedrich (denkbares GmbH)
 * @created 23.02.2011
 */
public class HighlightProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Tool refresh = getHighlightTool(section, userContext);
		return new Tool[] { refresh };
	}

	protected Tool getHighlightTool(Section<?> section, UserContext userContext) {
		boolean doHighlighting =
				DiaFluxTraceHighlight.checkForHighlight(userContext,
						DiaFluxTraceHighlight.TRACE_HIGHLIGHT);

		if (doHighlighting) {
			return new DefaultTool(Icon.DEBUG, "Hide Trace",
					"Highlights active nodes and edges in the flowchart.",
					DiaFluxTraceHighlight.getDeactivationJSAction(),
					Tool.CATEGORY_INFO);
		}
		else {
			return new DefaultTool(
					Icon.DEBUG,
					"Show Trace",
					"Highlights active nodes and edges in the flowchart.",
					DiaFluxTraceHighlight.getActivationJSAction(DiaFluxTraceHighlight.TRACE_HIGHLIGHT),
					Tool.CATEGORY_INFO);
		}
	}
}
