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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class TableUtils {

	/**
	 * Returns a list of sections with only the given type in it.
	 * 
	 * @param setion
	 *             current section
	 * @param classname
	 * @param sections
	 * @return
	 */
	public static List<Section> getCertainSections( Section section, String classname, List<Section> sections ) {
		for( Section child : section.getChildren() ) {
			try {
				if( Class.forName( classname ).isAssignableFrom( child.getObjectType().getClass()) ) {
					sections.add( child );
				} else {
					getCertainSections( child, classname, sections );
				}
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return sections;		
	}

	/**
	 * Returns the column of the table in which the current cell occurs.
	 * 
	 * @param section
	 *             current section
	 * @return
	 */
	public static int getColumn( Section section )
	{
		Section tableLine = KnowWEObjectTypeUtils.getAncestorOfType( section, TableLine.class.getName() );
		List<Section> tmpSections = new ArrayList<Section>();
		getCertainSections( tableLine, TableCellContent.class.getName(), tmpSections );
		
		return tmpSections.indexOf( section ) + 1;
	}

	/**
	 * Returns the row of the table in which the current cell occurs.
	 * 
	 * @param section
	 *             current section
	 * @return
	 */
	public static int getRow( Section section )
	{
		Section tableContent = KnowWEObjectTypeUtils.getAncestorOfType( section, TableContent.class.getName() );
		
		List<Section> sections = new ArrayList<Section>();
		getCertainSections( tableContent, TableLine.class.getName(), sections );
		
		int col = getColumn(section)-1;
		for(int i = 0; i < sections.size(); i++)
		{			
			List<Section> tmpSections = new ArrayList<Section>();
			getCertainSections( sections.get(i), TableCellContent.class.getName(), tmpSections );
			if(tmpSections.size() > col && tmpSections.get(col).equals( section )) return i + 1;
		}
		return 0;
	}

	/**
	 * Checks if the current cell is editable. Returns<code>TRUE</code> if so, 
	 * otherwise <code>FALSE</code>.
	 * 
	 * @param section
	 *             current section
	 * @param rows
	 *             value of the row table attribute
	 * @param cols
	 *            value of the column table attribute
	 * @return
	 */
	public static boolean isEditable( Section section, String rows, String cols )
	{
		if( rows == null && cols == null ) return true;
		
		boolean isRowEditable = true, isColEditable = true;
		if( rows != null ) {
			List<String> rowsIndex = Arrays.asList( splitAttribute( rows ) );
			String cellRow = String.valueOf( getRow( section ) );
			isRowEditable = !rowsIndex.contains( cellRow );
		}
		
		if( cols != null ) {
			List<String> colsIndex = Arrays.asList( splitAttribute( cols ) );
			String cellCol = String.valueOf( getColumn( section ) );
			isColEditable = !colsIndex.contains( cellCol );
		}
		return (isColEditable && isRowEditable);
	}

	/**
	 * Quotes some special chars.
	 * @param content
	 * @return
	 */
	public static String quote( String content ) {
		if(!(content.contains("\"") || content.contains("'"))) 
			return content.trim();
		
		content = content.replace("\"", "\\\"");
		content = content.replace("'", "\\\"");
		return content.trim();
	}

	/**
	 * Split an given attribute into tokens.
	 * 
	 * @param attribute
	 * @return
	 */
	public static String[] splitAttribute(String attribute)
	{
		Pattern p = Pattern.compile("[,|;|:]");
		return p.split( attribute );
	}

	/**
	 * Checks the width attribute of the table tag and returns a HTML string containing
	 * the width as CSS style information.
	 * 
	 * @param input
	 * @return
	 */
	public static String getWidth(String input){
		String pattern = "[+]?[0-9]+\\.?[0-9]+(%|px|em|mm|cm|pt|pc|in)";
		String digit = "[+]?[0-9]+\\.?[0-9]+";
	
		if( input == null ) return "";
		
		if( input.matches( digit )) {
			return "style='width:" + input + "px'";
		}
	    if( input.matches( pattern ) ) {
		    return "style='width:" + input + "'";
	    } else {
		    return "";
	    }
	}

}
