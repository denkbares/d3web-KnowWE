/*
 * Copyright (C) 2018 denkbares GmbH, Germany
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

package de.d3web.we.quicki;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * @author Jonas Müller
 * @created 02.02.18
 */
public class QuickInterviewToolProvider implements ToolProvider {
	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Tool saveAsFile = getSaveAsFileTool(section, userContext);
		Tool saveAsAttachment = getSaveAsAttachmentTool(section, userContext);
		Tool loadFromFile = getLoadFromFileTool(section, userContext);
		Tool loadFromAttachment = getLoadFromAttachmentTool(section, userContext);
		if (!KnowWEUtils.canWrite(section, userContext)) {
			return new Tool[] { saveAsFile, loadFromFile, loadFromAttachment };
		}
		return new Tool[] { saveAsFile, saveAsAttachment, loadFromFile, loadFromAttachment };
	}

	private Tool getSaveAsFileTool(Section<?> section, UserContext userContext) {
		String jsAction = "action/QuickInterviewSaveAction" + "?" + Attributes.SECTION_ID + "=" + section.getID() + "&download=true";
		return new DefaultTool(Icon.DOWNLOAD, "Save as Download",
				"Starts download of the interview as XML file to your local drive",
				jsAction, Tool.ActionType.HREF, Tool.CATEGORY_DOWNLOAD);
	}

	private Tool getSaveAsAttachmentTool(Section<?> section, UserContext userContext) {
		return new DefaultTool(Icon.FILE_XML, "Save as Attachment",
				"Adds representation of the interview as XML file to the attachments",
				"KNOWWE.plugin.quicki.saveAsAttachment('" + section.getID() + "')", Tool.CATEGORY_DOWNLOAD);
	}

	private Tool getLoadFromFileTool(Section<?> section, UserContext userContext) {
		return new DefaultTool(Icon.UPLOAD, "Load from File",
				"Starts dialogue to load XML file from your local drive",
				"KNOWWE.plugin.quicki.loadFromFile()", Tool.CATEGORY_EXECUTE);
	}

	private Tool getLoadFromAttachmentTool(Section<?> section, UserContext userContext) {
		return new DefaultTool(Icon.ATTACHMENT, "Load from Attachments",
				"Starts dialogue to load XML file from attachments",
				"KNOWWE.plugin.quicki.loadFromAttachment('" + section.getID() + "')", Tool.CATEGORY_EXECUTE);
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}
}
