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
