/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.testcase.toolprovider;

import de.d3web.we.testcase.kdom.TestCaseType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * ToolProvider which provides some download links for the {@link TestCaseType}.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 18/10/2010
 */
public class TestCaseToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {

		Tool downloadXML = getDownloadXMLTool(section.getTitle(), section.getWeb(), section.getID());
		Tool downloadTXT = getDownloadTXTTool(section.getTitle(), section.getWeb(), section.getID());
		return new Tool[] {
				downloadXML, downloadTXT };
	}

	protected Tool getDownloadXMLTool(String topic, String web, String id) {
		// tool to provide download capability
		String jsAction = "window.location='action/TestCaseServlet?type=case&amp;KWiki_Topic="
				+ topic + "&amp;web=" + web + "&amp;filename=" + topic.replaceAll(" ", "_")
				+ "_testcase.xml'";
		return new DefaultTool(
				"KnowWEExtension/images/xml.png",
				"Download XML",
				"Download the whole test case into a single xml file.",
				jsAction,
				Tool.CATEGORY_DOWNLOAD);
	}

	protected Tool getDownloadTXTTool(String topic, String web, String id) {
		// tool to provide download capability
		String jsAction = "window.location='action/TestCaseServlet?type=case&amp;KWiki_Topic="
				+ topic + "&amp;web=" + web + "&amp;filename=" + topic.replaceAll(" ", "_")
				+ "_testcase.txt'";
		return new DefaultTool(
				"KnowWEExtension/images/txt.png",
				"Download TXT",
				"Download the whole test case into a single txt file.",
				jsAction,
				Tool.CATEGORY_DOWNLOAD);
	}

}
