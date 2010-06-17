/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.table;

import de.d3web.we.kdom.Section;

/**
 * For the customized quick-edit functionality of a specialized table additional
 * parameters have to be specified. They are called by the edit-renderer to renderer 
 * the edit component of the cells (textfield, dropdown-options...)
 * 
 * The methods MAY return null; for this case defaults for editing are used.
 * 
 * @author Jochen/smark
 *
 */
public interface TableAttributesProvider {
	
	/**
	 * width style of the generated input-element in quick-edit
	 * 
	 * @param s
	 * @return
	 */
	public String getWidthAttribute(Section<Table> s);
	/**
	 * 	 * comma separated list of row numbers, which should be NOT editable
	 * (for header-rows) in quick-edit
	 * 
	 * @param s
	 * @return
	 */
	public String getNoEditColumnAttribute(Section<Table> s);
	/**
	 * comma separated list of column numbers, which should be NOT editable
	 * (for header-columns) in quick-edit
	 * 
	 * @param s
	 * @return
	 */
	public String getNoEditRowAttribute(Section<Table> s);
	/**
	 * comma separated lists of options for a dropdown-menu
	 * 
	 * @param s
	 * @return
	 */
	public String[] getAttributeValues(Section<? extends TableCellContent> s);
	

}
