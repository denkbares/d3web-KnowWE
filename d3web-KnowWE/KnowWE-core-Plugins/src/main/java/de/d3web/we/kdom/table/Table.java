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

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * Table class.
 * 
 * This class is used to extend KnowWE with in-view editable tables. The markup for
 * this feature is the following:
 * e.g:
 * &lt;table&gt;
 * | cell 1 | cell 2
 * &lt;/table&gt;
 * 
 * Now all tables will be prefixed with a button called "QuickEditFlag". This buttons
 * enabled a quick edit mode for the table. If selected, each cell will be rendered
 * as an HTML input field. Also a save button occours to save the changes. 
 * 
 * The <code>Table</code> tag is extendible with the following attributes:
 * <ul>
 *   <li>default</li>
 *   <li>width</li>
 *   <li>column</li>
 *   <li>row</li>
 * </ul>
 * 
 * With the <code> default </code> attribute you can specify default values. This 
 * values are used to render a HTML DropDown element instead of the input field.
 * 
 * The <code>width</code> attribute declares the width of each input field. Use this if you
 * want to avoid scrolling in the input fields.
 * 
 *  The <code>row</code> and <code>column</code> attribute define which row or column
 *  is not editable.
 * 
 * @author smark
 * @see AbstractXMLObjectType
 */
public class Table extends AbstractXMLObjectType implements ITable
{
	/**
	 * Attribute name for the default values of each cell.
	 */
	public static final String ATT_VALUES = "default";
	
	/**
	 * Attribute name for the width of each cell.
	 */
	public static final String ATT_WIDTH  = "width";
	
	/**
	 * Attribute name. Lets the user specify columns and row that are non editable. 
	 */
	public static final String ATT_NOEDIT_COLUMN = "column";
	public static final String ATT_NOEDIT_ROW    = "row";
	
	public Table(String tagName) {
		super(tagName);
	}
	
	public Table()
	{
		super("Table");
	}
	
	@Override
	protected void init() 
	{
		childrenTypes.add( new TableContent () );
	}
}
