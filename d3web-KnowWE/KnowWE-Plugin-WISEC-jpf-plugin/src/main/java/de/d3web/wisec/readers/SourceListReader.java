/*
 * Copyright (C) 2010 denkbares GmbH
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.WISECModel;

/**
 * Updated reader which is compatible with the new Excel structure.
 * 
 * @author Sebastian Furth
 * @created 19/07/2010
 */
public class SourceListReader extends WISECReader {

	public static String SHEETNAME = "Source";
	public Map<String, String> criteria = new LinkedHashMap<String, String>();

	public SourceListReader(Workbook workbook) {
		super(workbook);
	}

	@Override
	public void read(WISECModel model) {
		final int HEADER_ROW = 0;
		Sheet sheet = workbook.getSheet(SHEETNAME);
		List<String> headers = retrieveHeaderNames(sheet.getRow(HEADER_ROW));
		for (int row = HEADER_ROW + 1; row < sheet.getRows(); row++) {
			SourceList upperList = new SourceList();
			for (int col = 0; col < sheet.getRow(row).length; col++) {
				String attribute = headers.get(col);
				String value = sheet.getCell(col, row).getContents();
				upperList.add(attribute, value);
			}
			model.add(upperList);
		}

	}

}
