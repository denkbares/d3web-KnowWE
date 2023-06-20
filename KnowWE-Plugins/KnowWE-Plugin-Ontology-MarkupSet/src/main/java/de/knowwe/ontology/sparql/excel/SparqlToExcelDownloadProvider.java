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

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

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
        Tool ExcelTool = getDownloadExcelTool(section, userContext);
        return new Tool[]{ExcelTool};
    }

    protected Tool getDownloadExcelTool(Section<?> section,
                                        UserContext userContext) {

        String jsAction = "KNOWWE.plugin.sparql.downloadExcel(" +
                "'" + section.getID() + "', " +
                "'" + section.getArticle().getTitle() + ".xlsx'," +
                "'SparqlDownloadAction'" +
                ")";
        return new DefaultTool(
                Icon.FILE_EXCEL,
                "Download as XLSX", "Download this table as an excel file",
                jsAction, Tool.CATEGORY_DOWNLOAD);
    }
}
