package de.d3web.wisec.readers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import de.d3web.wisec.model.UpperList;
import de.d3web.wisec.model.WISECModel;

public class UpperListReader extends WISECReader {
	public static String SHEETNAME = "ListenListe";
	public Map<String, String> criteria = new LinkedHashMap<String, String>();

	
	public UpperListReader(Workbook workbook) {
		super(workbook);
	}

	@Override
	public void read(WISECModel model) {
		int HEADER_ROW = 1;
		Sheet sheet = workbook.getSheet(SHEETNAME);
		List<String> headers = coputeHeaderNames(sheet.getRow(HEADER_ROW));
		for (int row = HEADER_ROW+1; row < sheet.getRows(); row++) {
			UpperList upperList = new UpperList();
			for (int col = 0; col < sheet.getRow(row).length; col++) {
				String attribute = headers.get(col);
				String value     = sheet.getCell(col, row).getContents();
				upperList.add(attribute, value);
			}
			model.add(upperList);
		}
		
	}

	private List<String> coputeHeaderNames(Cell[] row) {
		List<String> names = new ArrayList<String>();
		for (Cell cell : row) {
			names.add(cell.getContents());
		}
		return names;
	}

}
