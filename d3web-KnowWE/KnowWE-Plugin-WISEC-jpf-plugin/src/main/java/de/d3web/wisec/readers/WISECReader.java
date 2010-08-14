package de.d3web.wisec.readers;

import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Workbook;
import de.d3web.wisec.model.WISECModel;

/**
 * General interface for all readers, that load knowledge from the WISEC Excel
 * sheet.
 * 
 * @author joba
 * 
 */
public abstract class WISECReader {

	protected Workbook workbook;

	public WISECReader(Workbook workbook) {
		this.workbook = workbook;
	}

	public abstract void read(WISECModel model);

	protected List<String> retrieveHeaderNames(Cell[] row) {
		List<String> names = new ArrayList<String>();
		for (Cell cell : row) {
			names.add(cell.getContents());
		}
		return names;
	}

}
