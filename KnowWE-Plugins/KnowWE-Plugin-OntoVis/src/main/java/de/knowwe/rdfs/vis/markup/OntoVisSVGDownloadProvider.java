/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.rdfs.vis.markup;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * @author Johanna Latt
 * @created 20.06.2012
 */
public class OntoVisSVGDownloadProvider implements ToolProvider {

	public static final String PARAM_FILENAME = "filename";

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		// and provide both download and refresh as tools
		Tool download = getDownloadTool(section, userContext);
		return new Tool[] { download };
	}

	protected Tool getDownloadTool(Section<?> section, UserContext userContext) {
		// tool to provide download capability

		String jsAction = "window.location='action/OntoVisSVGDownload" +
				"?" + Attributes.TOPIC + "=" + section.getTitle() +
				"&amp;" + Attributes.WEB + "=" + section.getWeb() +
				"&amp;" + Attributes.SECTION_ID + "=" + section.getID() + "'";
		return new DefaultTool(
				Icon.DOWNLOAD,
				"Download .svg",
				"Download the graph as an .svg-file",
				jsAction,
				Tool.CATEGORY_DOWNLOAD);
	}

}
