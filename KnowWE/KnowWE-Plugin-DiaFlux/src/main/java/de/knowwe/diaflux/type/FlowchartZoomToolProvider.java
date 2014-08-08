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

package de.knowwe.diaflux.type;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * Provides tools to smoothly zoom flowcharts.
 * <p/>
 * Created by Albrecht Striffler (denkbares GmbH) on 08.08.14.
 */
public class FlowchartZoomToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		DefaultTool zoomOut = new DefaultTool(null, "Zoom Out", "Zooms out the current flowchart", "Flowchart.zoom('" + section
				.getID() + "', -0.1)");
		DefaultTool zoomIn = new DefaultTool(null, "Zoom In", "Zooms in the current flowchart", "Flowchart.zoom('" + section
				.getID() + "', 0.1)");
		DefaultTool zoomToFit = new DefaultTool(null, "Zoom To Fit", "Zooms to flowchart to fit the width of the current page", "Flowchart.zoomToFit('" + section
				.getID() + "')");
		DefaultTool zoom100 = new DefaultTool(null, "Zoom 100%", "Zooms to flowchart back to 100% size", "Flowchart.zoom100('" + section
				.getID() + "')");
		return new Tool[] { zoomToFit, zoomOut, zoomIn, zoom100 };
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}
}
