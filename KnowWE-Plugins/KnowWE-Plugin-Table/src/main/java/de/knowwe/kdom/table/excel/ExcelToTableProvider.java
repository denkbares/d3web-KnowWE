/*
 * Copyright (C) 2025 denkbares GmbH, Germany
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

package de.knowwe.kdom.table.excel;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Tool Provider for ExcelToTableAction
 *
 * @author Philipp Sehne
 */
public class ExcelToTableProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[]{getDownloadTool(section)};
	}

	protected Tool getDownloadTool(Section<?> section) {
		String sectionID = section.getID();
		String jsAction;
		jsAction = "KNOWWE.tableUploadExcel.open(" + jsString(sectionID) + ")";
		// assemble download tool
		return new DefaultTool(
				Icon.UPLOAD,
				"Replace with XLSX",
				"Upload XLSX Table to replace current Table Markup with Excel file content",
				jsAction,
				Tool.ActionType.ONCLICK,
				Tool.CATEGORY_DOWNLOAD);
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	private static String jsString(String value) {
		if (value == null) {
			return "null";
		}
		return "'" + value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t")
				+ "'";
	}
}
