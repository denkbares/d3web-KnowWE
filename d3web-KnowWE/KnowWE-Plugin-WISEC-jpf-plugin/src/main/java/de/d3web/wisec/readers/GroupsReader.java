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
package de.d3web.wisec.readers;

import jxl.Sheet;
import jxl.Workbook;
import de.d3web.wisec.model.WISECModel;

/**
 * Imports the list of groups and the substances included in the particular
 * groups.
 * 
 * @author joba
 * @created 06.08.2010
 */
public class GroupsReader extends WISECReader {

	public static String SHEETNAME = "Groups";

	public GroupsReader(Workbook workbook) {
		super(workbook);
	}

	@Override
	public void read(WISECModel model) {
		final int HEADER_ROW = 0;
		Sheet sheet = workbook.getSheet(SHEETNAME);
		String defaultGroupName = "";
		for (int row = HEADER_ROW + 1; row < sheet.getRows(); row++) {
			String groupName = sheet.getCell(0, row).getContents();
			String substancesLine = sheet.getCell(1, row).getContents();
			String[] substances = new String[0];
			if (substancesLine != null) {
				substances = substancesLine.split("\\$");
			}
			if (groupName == null || groupName.isEmpty()) {
				groupName = defaultGroupName;
			}
			for (String substance : substances) {
				if (!substance.matches("\\s*")) {
					model.addToGroup(groupName, substance.trim());
					model.activeSubstances.add(substance);
				}
			}
		}
	}

}
