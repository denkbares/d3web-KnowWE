/*
 * Copyright (C) 2021 denkbares GmbH, Germany
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
package de.knowwe.ontology.sparql.excel;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Stefan Plehn
 * @created 22.03.2013
 */
public class SparqlToExcelDownloadProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		// and provide both download and refresh as tools
		Tool ExcelTool = getDownloadExcelTool(section);
		Tool ExcelToolFiltered = getDownloadExcelToolFiltered(section);
		return new Tool[] { ExcelToolFiltered, ExcelTool };
	}

	protected Tool getDownloadExcelTool(Section<?> section) {
		String title = "Download as XLSX";
		String description = "Download this table as an excel file";
		String fileName = generateFileName(section);
		String jsAction = "KNOWWE.plugin.sparql.downloadExcel(" +
						  "'" + section.getID() + "', " +
						  "'" + fileName + ".xlsx'," +
						  "'SparqlDownloadAction'," +
						  "'false'" +
						  ")";
		return new DefaultTool(
				Icon.DOWNLOAD_LINE,
				title, description,
				jsAction, Tool.ActionType.ONCLICK, Tool.CATEGORY_DOWNLOAD);
	}

	private static String generateFileName(Section<?> section) {
		String fileName = section.getArticle().getTitle();
		String lastHeadline = null;
		for (Section<Type> child : $(section).parent().children()) {
			if (child.get() instanceof HeaderType) {
				Section<HeaderType> header = Sections.cast(child, HeaderType.class);
				lastHeadline = header.get().getHeaderText(header);
			}
			if (child == section) break;
		}
		if (lastHeadline != null) {
			fileName += " " + lastHeadline;
		}
		return Strings.encodeFileName(fileName).replaceAll("'", "");
	}

	protected Tool getDownloadExcelToolFiltered(Section<?> section) {
		String title = "Download as XLSX (filtered)";
		String description = "Download this table as an excel file only containing the filtered results.";
		String fileName = generateFileName(section);
		String jsAction = "KNOWWE.plugin.sparql.downloadExcel(" +
				"'" + section.getID() + "', " +
				"'" + fileName + ".xlsx'," +
				"'SparqlDownloadAction'," +
				"'true'" +
				")";
		return new DefaultTool(
				Icon.DOWNLOAD_BRACKET,
				title, description,
				jsAction, Tool.ActionType.ONCLICK, Tool.CATEGORY_DOWNLOAD);
	}

	protected boolean hasFilter(Map<String, Set<Pattern>> filter) {
		for (Set<Pattern> filterPatterns : filter.values()) {
			if (!filterPatterns.isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
