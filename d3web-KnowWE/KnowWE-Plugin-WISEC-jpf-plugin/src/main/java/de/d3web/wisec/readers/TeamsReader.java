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

public class TeamsReader extends WISECReader {

	public static String SHEETNAME = "Teams";

	public TeamsReader(Workbook workbook) {
		super(workbook);
	}

	@Override
	public void read(WISECModel model) {
		final int HEADER_ROW = 0;
		Sheet sheet = workbook.getSheet(SHEETNAME);
		String teamName = "";
		for (int row = HEADER_ROW + 1; row < sheet.getRows(); row++) {
			String newTeamName = sheet.getCell(0, row).getContents();
			String newSubstance = sheet.getCell(1, row).getContents();
			String newGroupName = sheet.getCell(2, row).getContents();
			if (newTeamName == null || newTeamName.isEmpty()) {
				newTeamName = teamName;
			}
			else {
				teamName = newTeamName;
			}
			if (newSubstance != null && !newSubstance.isEmpty()) {
				model.addSubstanceToTeam(newTeamName, newSubstance);
			}
			if (newGroupName != null && !newGroupName.isEmpty()) {
				model.addGroupToTeam(newTeamName, newGroupName);
			}
		}
	}

}
