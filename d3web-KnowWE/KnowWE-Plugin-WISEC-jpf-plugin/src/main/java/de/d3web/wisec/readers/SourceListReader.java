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
