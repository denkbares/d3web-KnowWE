/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.kdom.table;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.kdom.xml.XMLContent;

/**
 * <p>
 * Represents the body of the <code>Table</code> tag.
 * </p>
 * 
 * * This class is used to extend KnowWE with in-view editable tables. The
 * markup for this feature is the following: e.g: | cell 1 | cell 2 or || header
 * cell | normal cell;
 * 
 * Now all tables will be prefixed with a button called "QuickEditFlag". This
 * buttons enabled a quick edit mode for the table. If selected, each cell will
 * be rendered as an HTML input field. Also a save button occours to save the
 * changes.
 * 
 * The <code>Table</code> tag is extendible with the following attributes:
 * <ul>
 * <li>default</li>
 * <li>width</li>
 * <li>column</li>
 * <li>row</li>
 * </ul>
 * 
 * With the <code> default </code> attribute you can specify default values.
 * This values are used to render a HTML DropDown element instead of the input
 * field.
 * 
 * The <code>width</code> attribute declares the width of each input field. Use
 * this if you want to avoid scrolling in the input fields.
 * 
 * The <code>row</code> and <code>column</code> attribute define which row or
 * column is not editable.
 * 
 * 
 * @author smark
 * @see XMLContent
 */
public class Table extends AbstractType {

	public Table() {
		this.addChildType(new TableLine());
		this.setSectionFinder(AllTextFinder.getInstance());
		this.setRenderer(new TableRenderer());
	}

	public void injectTableCellContentChildtype(Type type) {
		injectCellContentType(this, type);
	}

	public static void injectCellContentType(Type master, Type injection) {
		Type cellContentType = de.knowwe.core.kdom.Types.successor(master,
				TableCellContent.class);
		if (cellContentType instanceof AbstractType) {
			((Type) cellContentType).addChildType(injection);
		}
	}

	public boolean isSortable() {
		return false;
	}

}
