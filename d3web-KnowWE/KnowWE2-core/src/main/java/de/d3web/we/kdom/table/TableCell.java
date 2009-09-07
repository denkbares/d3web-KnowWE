package de.d3web.we.kdom.table;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

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
		sectionFinder = new TableCellSectioner( this );
		
		childrenTypes.add(new TableCellStart());
		childrenTypes.add(new TableCellContent());
	}
	
	/**
	 * TableCellSetioner.
	 * 
	 * This class parses the <code>TableLine</code> into <code>TableCell</code>s.
	 * 
	 * @author smark
	 * @see SectionFinder
	 */
	class TableCellSectioner extends SectionFinder
	{
		public TableCellSectioner(KnowWEObjectType type) 
		{
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section text, Section father,
				KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) 
		{
			 ArrayList<Section> resultRegex = new ArrayList<Section>();
			
			StringBuilder cell = new StringBuilder();
		    CharacterIterator it = new StringCharacterIterator( text.getOriginalText() );
		    boolean newCell = false, hasLinkOpen = false;
		    int start = -1, end = -1;

		    for (char ch=it.first(); ch != CharacterIterator.DONE; ch=it.next()) 
		    {
		      	switch ( ch )
		       	{
			    case'[':
					hasLinkOpen = true;
					cell.append( ch );
					break;
				case']':
					cell.append( ch );
					break;
				case'|':
					if( newCell )
					{
						if( hasLinkOpen )
						{
							hasLinkOpen = false;
							cell.append( ch );
						}
						else
						{
							end = it.getIndex();
							newCell = false;
						}
					}
					else
					{
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
		        	
		        if( !newCell )
		        {
		        	resultRegex.add(Section.createSection(this.getType(), father, text, start, end, kbm, report, idg));
		        	newCell = true;
		        	start = it.getIndex();
		        }        		
		    }
		    
		    resultRegex.add(Section.createSection(this.getType(), father, text, start, end, kbm, report, idg));

		    return resultRegex;
		}
	}
}
