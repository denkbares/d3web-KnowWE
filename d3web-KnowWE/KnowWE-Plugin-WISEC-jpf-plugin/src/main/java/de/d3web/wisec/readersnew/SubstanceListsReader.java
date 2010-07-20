package de.d3web.wisec.readersnew;

import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.UpperList;
import de.d3web.wisec.model.WISECModel;
import de.d3web.wisec.readers.WISECReader;

/**
 * Updated reader which is compatible with the new Excel structure.
 * 
 * @author Sebastian Furth
 * @created 19/07/2010
 */
public class SubstanceListsReader extends WISECReader {

	private static final String LISTOFSUBSTANCES = "List of Substances";
	
	private WISECModel model;
	
	public SubstanceListsReader(Workbook workbook) {
		super(workbook);
	}

	@Override
	public void read(WISECModel model) {
		this.model = model;
		int counter = 0;
		List<String> listnames = computeListnames();
		for (String listname : listnames) {
			if (counter < WISECExcelConverter.maxListsToConvert) {
				SubstanceList list = readSubstanceList(listname);
				model.add(list);
				counter++;
			}
		}		
	}
	
	private SubstanceList readSubstanceList(String listname) {
		Sheet sheet = workbook.getSheet(listname);
		String realListName = sheet.getCell(0,0).getContents();
		SubstanceList list = new SubstanceList(realListName);
		
		// read the list criteria
		readListCriteria(realListName, list);
		
		final int HEADER_ROW = 1;
		// read the attributes
		for (int col = 0; col < sheet.getColumns(); col++) {
			String name = sheet.getCell(col, HEADER_ROW).getContents();
			list.attributes.add(name);
		}
		
		// read the data
		for (int row = HEADER_ROW + 1; row < sheet.getRows(); row++) {
			Substance substance = new Substance();
			for (int col = 0; col < sheet.getColumns(); col++) {
				String name  = sheet.getCell(col, HEADER_ROW).getContents();
				String value = sheet.getCell(col, row).getContents();
				substance.add(name, value);
			}
			list.add(substance);
		}
		
		// update upperlist info
		UpperList upperList = getUpperList(realListName);
		if (upperList == null) {
			System.err.println("Upperlist not found for " + realListName);
		}
		else {
			list.upperList = upperList;
			upperList.addChild(list);
		}
		
		return list;
	}
	
	private UpperList getUpperList(String listname) {
		Sheet overviewSheet = workbook.getSheet(LISTOFSUBSTANCES);
		int row = getOverviewRowWithName(listname, overviewSheet);
		String upperListID = overviewSheet.getCell(0, row).getContents();
		
		for (UpperList upperList : model.getUpperLists()) {
			if (upperList.get("LfdNr").equals(upperListID)) {
				return upperList;
			}
		}
		return null;
	}

	private void readListCriteria(String listname, SubstanceList list) {
		Sheet overviewSheet = workbook.getSheet(LISTOFSUBSTANCES);
		int row = getOverviewRowWithName(listname, overviewSheet);

		// last col is the compartment col => overviewSheet.getColumns() - 1
		for (int col = 7; col < overviewSheet.getColumns() - 1; col++) {
			String criteriaName = overviewSheet.getCell(col, 1).getContents();
			String criteriaValue = overviewSheet.getCell(col, row).getContents();
			list.criteria.put(criteriaName, criteriaValue);
		}
	}

	private int getOverviewRowWithName(String listname, Sheet sheet) {
		int col = 3;
		for (int row = 0; row < sheet.getRows(); row++) {
			String name = sheet.getCell(col, row).getContents();
			if (listname.equals(name)) {
				return row;
			}
		}
		return 0;
	}

	
	private List<String> computeListnames() {
		List<String> sheetNames = new ArrayList<String>();
		int numberOfSheets = workbook.getSheets().length;
		for (int i = 5; i < numberOfSheets; i++) {
			Sheet sheet = workbook.getSheet(i);
			sheetNames.add(sheet.getName());
		}
		return sheetNames;
	}
}
