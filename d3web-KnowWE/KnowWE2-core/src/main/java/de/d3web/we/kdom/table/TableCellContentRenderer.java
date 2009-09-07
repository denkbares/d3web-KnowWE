package de.d3web.we.kdom.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.PlainText;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>Renders the content of a <code>TableCellContent</code> element depending on the
 * state of the QuickEditFlag. If <code>TRUE</code> each cell is rendered as
 * an HTML input field, containing the text of the cell.</p>
 * <p>If the <code>value</code> attribute (@see Table) is given the input filed is replaced
 * by an drop down list. If <code>FALSE</code> simple text is rendered.</p>
 *  
 * <p>e.g:</p>
 * <code>
 * Cell given in JSPWiki syntax "| cell 1"
 * =>
 * "&lt;input type='text' name='sectionID' id='sectionID' value='cell 1' /&gt;"
 * </code>
 * 
 * <p>where <code>sectionID</code> is the id in the KDOM.</p>
 * 
 * @author smark
 * @see KnowWEDomRenderer
 * @see Table
 */
public class TableCellContentRenderer  extends KnowWEDomRenderer
{
	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) 
	{
		StringBuilder sectionText = new StringBuilder();
		String sectionID = sec.getId();
						
		for (Section section : sec.getChildren())
		{
			if ( section.getObjectType() instanceof PlainText)
			{
				sectionText.append(section.getOriginalText().trim());
			}
		}

		StringBuilder html = new StringBuilder();
		html.append( "<td>" );

		if( sec.hasQuickEditModeSet( user.getUsername() ) )
		{
			Section father = getFather( sec , Table.class.getName());
			String values = null, size = null, rows = null, cols = null;

			if( father != null )
			{
				Map<String, String> map = ((AbstractXMLObjectType) father.getObjectType()).getMapFor( father );
			    values = map.get( Table.ATT_VALUES );
			    size   = map.get( Table.ATT_WIDTH );
			    cols   = map.get( Table.ATT_NOEDIT_COLUMN );
			    rows   = map.get( Table.ATT_NOEDIT_ROW );
			}
			
			if( isEditable( sec, rows, cols ) )
			{
				if( values != null )
				{
					html.append( createDefaultValueDropDown( values, sectionText.toString(), sectionID, size));
				}
				else
				{
					html.append( "<input type='text' name='" + sectionText + "' id='" + sectionID + "' value='" + sectionText 
				            + "' class='table-edit-node' " + getWidth( size ) + "/>" );
				}
			}
			else
			{
				html.append( quote(sectionText.toString()) );
			}
		}
		else
		{
			html.append( quote(sectionText.toString()) );
		}
		
		html.append( "</td>" );
		return KnowWEEnvironment.maskHTML( html.toString() );
	}
	
	/**
	 * Quotes some special chars.
	 * @param content
	 * @return
	 */
	private String quote( String content )
	{
		if( !(content.contains("\"") || content.contains("'"))) return content;
		
		content = content.replace("\"", "\\\"");
		content = content.replace("'", "\\\"");
		return content;
	}

	/**
	 * Creates an DropDown element out of the specified default values.
	 * 
	 * @param values
	 * @param cellcontent
	 * @param nodeID
	 * @return
	 */
	private String createDefaultValueDropDown(String values, String cellcontent, String nodeID, String width)
	{
		StringBuilder html = new StringBuilder();
		html.append( "<select id='" + nodeID + "' class='table-edit-node' " + getWidth( width ) + ">" );
		
		List<String> defaultValues = Arrays.asList( splitAttribute( values ) );
		
		if( cellcontent != null && !defaultValues.contains( cellcontent ))
		{
			html.append( "<option value='" + cellcontent + "' selected=\"selected\">" + cellcontent + "</option>" );
		}
		
		for ( String token : defaultValues ) 
		{
			html.append( "<option value='" + token + "'>" + token + "</option>" );
		}
		html.append( "</select>" );
		return html.toString();
	}

	/**
	 * Get the father element of the current cell content section. Search as long as the section
	 * is instance of AbstractXMLObjectType. Used to get the <code>Table</code> section itself.
	 * 
	 * @param child
	 * @return
	 */
	private Section getFather(Section child, String classname)
	{
		if( child == null ) return null;
		
		try {
			if( Class.forName( classname ).isInstance( child.getObjectType() ) ) return child;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		 
		return getFather(child.getFather(), classname);
	}
	
	/**
	 * Checks the width attribute of the table tag and returns a HTML string containing
	 * the width as CSS style information.
	 * 
	 * @param input
	 * @return
	 */
	private String getWidth(String input){
		String pattern = "[+]?[0-9]+\\.?[0-9]+(%|px|em|mm|cm|pt|pc|in)";
		String digit = "[+]?[0-9]+\\.?[0-9]+";

		if( input == null ) return "";
		
    	if( input.matches( digit ))
    	{
    		return "style='width:" + input + "px'";
    	}
        if( input.matches( pattern ) )
        {
    	    return "style='width:" + input + "'";
        }
        else
        {
    	    return "";
        }
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
	private boolean isEditable( Section section, String rows, String cols )
	{
		if( rows == null && cols == null ) return true;
		
		boolean isRowEditable = true, isColEditable = true;
		if( rows != null )
		{
			List<String> rowsIndex = Arrays.asList( splitAttribute( rows ) );
			String cellRow = String.valueOf( getRow( section ) );
			isRowEditable = !rowsIndex.contains( cellRow );
		}
		
		if( cols != null ) 
		{
			List<String> colsIndex = Arrays.asList( splitAttribute( cols ) );
			String cellCol = String.valueOf( getColumn( section ) );
			isColEditable = !colsIndex.contains( cellCol );
		}
		return (isColEditable && isRowEditable);
	}
	
	/**
	 * Returns the column of the table in which the current cell occurs.
	 * 
	 * @param section
	 *             current section
	 * @return
	 */
	private int getColumn( Section section )
	{
		Section tableLine = getFather( section, TableLine.class.getName() );
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
	private int getRow( Section section )
	{
		Section tableContent = getFather( section, TableContent.class.getName() );
		
		List<Section> sections = new ArrayList<Section>();
		getCertainSections( tableContent, TableLine.class.getName(), sections );
		
		int row = -1;
		for(int i = 0; i < sections.size(); i++)
		{			
			List<Section> tmpSections = new ArrayList<Section>();
			getCertainSections( sections.get(i), TableCellContent.class.getName(), tmpSections );
			if( tmpSections.contains( section )) row = i;
		}
		return row + 1;
	}
		
	/**
	 * Returns a list of sections with only the given type in it.
	 * 
	 * @param setion
	 *             current section
	 * @param classname
	 * @param sections
	 * @return
	 */
	private List<Section> getCertainSections( Section section, String classname, List<Section> sections )
	{
		for( Section child : section.getChildren() )
		{
			try {
				if( Class.forName( classname ).isInstance( child.getObjectType() ) )
				{
					sections.add( child );
				}
				else
				{
					getCertainSections( child, classname, sections );
				}
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return sections;		
	}
	
	/**
	 * Split an given attribute into tokens.
	 * 
	 * @param attribute
	 * @return
	 */
	private String[] splitAttribute(String attribute)
	{
		Pattern p = Pattern.compile("[,|;|:]");
		return p.split( attribute );
	}
}
