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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * TableCell class.
 * 
 * This class represents a cell of a WIKI table. Each cell has a start and a content area.
 * 
 * @author smark
 * @see AbstractKnowWEObjectType
 * @see TableCellStart
 * @see TableCell
 */
public class TableCell extends DefaultAbstractKnowWEObjectType {
	
	@Override
	protected void init() {
		//sectionFinder =  new AllTextFinder(this);
		sectionFinder = new TableCellSectionFinder();
		
		childrenTypes.add(new TableCellStart());
		childrenTypes.add(new TableCellContent());
	}
	
	/**
	 * TableCellSetioner.
	 * 
	 * This class parses the <code>TableLine</code> into <code>TableCell</code>s.
	 * Looking for the TableCell delimiter character is not enough due the 
	 * appearance of special markup e.g. links. The parser takes therefore this 
	 * special markup into account and handles it. 
	 * 
	 * @author smark
	 * @see SectionFinder
	 */
	public class TableCellSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
			 ArrayList<SectionFinderResult> resultRegex = new ArrayList<SectionFinderResult>();
			
			StringBuilder cell = new StringBuilder();
		    CharacterIterator it = new StringCharacterIterator(text);
		    boolean newCell = false, hasLinkOpen = false;
		    int start = -1, end = -1;

		    for (char ch=it.first(); ch != CharacterIterator.DONE; ch=it.next()) {
		      	switch ( ch ) {
			    case'[':
					hasLinkOpen = true;
					cell.append( ch );
					break;
				case']':
					cell.append( ch );
					hasLinkOpen = false;
					break;
				case'|':
					if( newCell ) {
						if( hasLinkOpen ) {
							hasLinkOpen = false;
							cell.append( ch );
						} else {
							end = it.getIndex();
							newCell = false;
						}
					}
					else {
						newCell = true;
						start = it.getIndex();
					}
					
					break;
				case '\n':
				case '\r':
					end = it.getIndex();
					break;
				default:
					cell.append( ch );
					break;
				}
		        
		        //TODO Hotfix for #28
		        if( !newCell && start > -1) {
		        	resultRegex.add(new SectionFinderResult(start, end));
		        	newCell = true;
		        	start = it.getIndex();
		        	cell = new StringBuilder();
		        }        		
		    }

		    //TODO Hotfix for #28
		    if( start > -1 && end > -1 )
		    	resultRegex.add(new SectionFinderResult(start, end));

		    return resultRegex;
		}
	}
}
