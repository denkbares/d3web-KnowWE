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

import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;
import de.d3web.wisec.writers.ConverterUtils;

/**
 * Updated reader which is compatible with the new Excel structure.
 * 
 * @author Sebastian Furth
 * @created 19/07/2010
 */
public class SubstanceListsReader extends WISECReader {

	// private static final String LISTS = "Lists";

	private static final int HEADER_ROW = 0;

	private WISECModel model;

	public SubstanceListsReader(Workbook workbook) {
		super(workbook);
	}

	@Override
	public void read(WISECModel model) {
		this.model = model;
		int counter = 0;
		List<String> listIDs = computeListIDs(model);
		for (String listID : listIDs) {
			if (counter < WISECExcelConverter.maxListsToConvert) {
				SubstanceList list = readSubstanceList(listID);
				if (list != null) {
					model.add(list);
					counter++;
				}
			}
		}
	}

	private SubstanceList readSubstanceList(String listID) {
		Sheet sheet = workbook.getSheet(listID);
		if (sheet == null) {
			System.err.println("Substance list *** " + listID + "*** not found.");
			return null;
		}

		SubstanceList list = model.getSubstanceListWithID(listID);

		list.substanceAttributes = ConverterUtils.rowToStringArray(sheet.getRow(HEADER_ROW));

		// read the substances
		for (int row = HEADER_ROW + 1; row < sheet.getRows(); row++) {
			Substance substance = new Substance();
			for (int col = 0; col < sheet.getColumns(); col++) {
				String name = sheet.getCell(col, HEADER_ROW).getContents(); // list.substanceAttributes.get(col);
				String value = sheet.getCell(col, row).getContents().trim();
				substance.add(name, value);
			}
			list.add(substance);
		}

		return list;
	}

	private List<String> computeListIDs(WISECModel model) {
		List<String> sheetNames = new ArrayList<String>();
		for (SubstanceList list : model.substanceLists) {
			String id = list.getId();
			sheetNames.add(id);
		}
		return sheetNames;
	}
}
