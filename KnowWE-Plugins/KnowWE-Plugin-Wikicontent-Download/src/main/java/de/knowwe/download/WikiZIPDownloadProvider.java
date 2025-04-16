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
package de.knowwe.download;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * @author Johanna Latt
 * @created 16.04.2012
 */
public class WikiZIPDownloadProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return userContext.userIsAdmin();
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		// and provide both downloads as tools
		if (!userContext.userIsAdmin()) {
			return new Tool[0];
		}
		return new Tool[] {
				getDownloadTool(section, false),
				getDownloadTool(section, true)
		};
	}

	protected Tool getDownloadTool(Section<?> section, boolean fingerprint) {
		// tool to provide download capability
		String jsAction = "window.location='action/DownloadWikiZIP" +
				"?" + Attributes.TOPIC + "=" + section.getTitle() +
				"&amp;" + Attributes.WEB + "=" + section.getWeb() +
				"&amp;" + DownloadWikiZIPAction.PARAM_FINGERPRINT + "=" + fingerprint +
				"&amp;" + DownloadWikiZIPAction.PARAM_VERSIONS + "=" + !fingerprint +
				"'";
		return new DefaultTool(
				Icon.FILE_ZIP,
				fingerprint
						? "Download Finger-Print"
						: "Download Wiki Zip",
				fingerprint
						? "Download the entire Wiki as a Zip-File, including a finger-print for debug purposes, but no version history."
						: "Download the entire Wiki as a Zip-File.",
				jsAction, Tool.CATEGORY_DOWNLOAD);
	}
}
