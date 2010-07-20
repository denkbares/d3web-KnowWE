package de.d3web.wisec.readers;

import jxl.Workbook;
import de.d3web.wisec.model.WISECModel;

/**
 * General interface for all readers, that load knowledge from the 
 * WISEC Excel sheet.
 * @author joba
 *
 */
public abstract class WISECReader {

	protected Workbook workbook;
	
	public WISECReader(Workbook workbook) {
		this.workbook = workbook;
	}
	
	public abstract void read(WISECModel model);
}
