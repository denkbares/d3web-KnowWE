package de.d3web.wisec.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class SubstanceListsReader {
	Workbook workbook;
	WISECStatistics statistics = new WISECStatistics();


	public SubstanceListsReader(String xlsFilename, WISECStatistics statistics) throws BiffException,
			IOException {
		workbook = Workbook.getWorkbook(new File(xlsFilename));
		this.statistics = statistics;
	}


	public List<SubstanceList> readSubstanceLists() {
		return readSubstanceLists(10000000);
	}

	public List<SubstanceList> readSubstanceLists(int maxListsToConvert) {
		int counter = 0;
		List<SubstanceList> lists = new ArrayList<SubstanceList>();
		List<String> listnames = computeListnames();
		for (String listname : listnames) {
			if (counter < maxListsToConvert) {
				SubstanceList list = readSubstanceList(listname);
				lists.add(list);
				counter++;
			}
		}
		return lists;
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
				substance.values.put(name, value);
				if (name.equals(WISECExcelConverter.SUBSTANCE_IDENTIFIER)) {
					updateSubstanceStatistics(value, list);
				}
			}
			list.add(substance);
		}
		return list;
	}

	private void updateSubstanceStatistics(String substance, SubstanceList list) {
		if (listAlreadyContains(list, substance)) {
			return;
		}
		else {
			statistics.increment(substance);
		}
	}

	private boolean listAlreadyContains(SubstanceList list, String substanceName) {
		for (Substance substance : list.substances) {
			if (substance.getName().equals(substanceName)) {
				return true;
			}
		}
		return false;
	}

	private void readListCriteria(String listname, SubstanceList list) {
		Sheet overviewSheet = workbook.getSheet("Datentabellen");
		int row = getOverviewRowWithName(listname, overviewSheet);
		for (int col = 7; col < overviewSheet.getColumns(); col++) {
			String criteriaName = overviewSheet.getCell(col, 1).getContents();
			String criteriaValue = overviewSheet.getCell(col, row).getContents();
			list.criteria.put(criteriaName, criteriaValue);
		}
	}

	private int getOverviewRowWithName(String listname, Sheet sheet) {
		int col = 4;
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
		for (int i = 2; i < numberOfSheets; i++) {
			Sheet sheet = workbook.getSheet(i);
			sheetNames.add(sheet.getName());
		}
		return sheetNames;
	}
}
