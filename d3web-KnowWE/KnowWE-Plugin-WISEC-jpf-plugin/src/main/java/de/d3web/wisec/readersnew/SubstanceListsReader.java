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

	private static final String LISTS = "Lists";
	
	private WISECModel model;
	
	public SubstanceListsReader(Workbook workbook) {
		super(workbook);
	}

	@Override
	public void read(WISECModel model) {
		this.model = model;
		int counter = 0;
		List<String> listIDs = computeListIDs();
		for (String listID : listIDs) {
			if (counter < WISECExcelConverter.maxListsToConvert) {
				SubstanceList list = readSubstanceList(listID);
				model.add(list);
				counter++;
			}
		}		
	}
	
	private SubstanceList readSubstanceList(String listID) {
		Sheet sheet = workbook.getSheet(listID);

		SubstanceList list = new SubstanceList(listID);
		list.name = getRealListName(listID);
		
		// read the list criteria
		readListCriteria(listID, list);
		
		// read the attributes
		final int HEADER_ROW = 0;
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
		UpperList upperList = getUpperList(listID);
		if (upperList == null) {
			System.err.println("Upperlist not found for " + listID);
		}
		else {
			list.upperList = upperList;
			upperList.addChild(list);
		}
		
		return list;
	}
	
	private String getRealListName(String listID) {
		Sheet overviewSheet = workbook.getSheet(LISTS);
		int row = getOverviewRowWithID(listID, overviewSheet);

		// The name of the list is in column 2
		return overviewSheet.getCell(2, row).getContents();
	}

	private UpperList getUpperList(String listID) {
		Sheet overviewSheet = workbook.getSheet(LISTS);
		int row = getOverviewRowWithID(listID, overviewSheet);
		String upperListID = overviewSheet.getCell(0, row).getContents();
		
		for (UpperList upperList : model.getUpperLists()) {
			if (upperList.get("ID").equals(upperListID)) {
				return upperList;
			}
		}
		return null;
	}

	private void readListCriteria(String listID, SubstanceList list) {
		Sheet overviewSheet = workbook.getSheet(LISTS);
		int row = getOverviewRowWithID(listID, overviewSheet);

		// last col is the compartment col => overviewSheet.getColumns() - 1
		for (int col = 6; col < overviewSheet.getColumns() - 1; col++) {
			String criteriaName = overviewSheet.getCell(col, 0).getContents();
			String criteriaValue = overviewSheet.getCell(col, row).getContents();
			list.criteria.put(criteriaName, criteriaValue);
		}
	}

	private int getOverviewRowWithID(String listID, Sheet sheet) {
		int idCol = 1;
		// We can start with row 1 because row 0 is the header row!
		for (int row = 1; row < sheet.getRows(); row++) {
			String id = sheet.getCell(idCol, row).getContents();
			if (listID.equals(id)) {
				return row;
			}
		}
		return 0;
	}

	
	private List<String> computeListIDs() {
		List<String> sheetNames = new ArrayList<String>();
		int numberOfSheets = workbook.getSheets().length;
		// first 4 sheets contain general information
		for (int i = 4; i < numberOfSheets; i++) {
			Sheet sheet = workbook.getSheet(i);
			sheetNames.add(sheet.getName());
		}
		return sheetNames;
	}
}
