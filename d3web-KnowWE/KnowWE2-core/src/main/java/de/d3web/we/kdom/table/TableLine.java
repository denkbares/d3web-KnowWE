package de.d3web.we.kdom.table;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * TableLine.
 * 
 * Represents a line of a WIKI table.
 * 
 * @see TextLine
 */
public class TableLine extends TextLine {
	
	@Override
	protected void init() 
	{
		//childrenTypes.add(new TableLineBegin());
		//childrenTypes.add(new TableLineEnd());
		//childrenTypes.add(new TableDelimiter());
		//childrenTypes.add(new TableCellContent());
		childrenTypes.add(new TableCell());
	}
	
	@Override
	public SectionFinder getSectioner() 
	{
		return this.sectionFinder = new TableLineSectionFinder( this );
	}	
	
	/**
	 * Returns the renderer for the <code>TableContent</code>.
	 * 
	 * @return KnowWEDomRenderer
	 */
	public KnowWEDomRenderer getRenderer()
	{
		/**
		 * This is a renderer for the TableLine. It wraps the <code>TableLine</code>
		 * into the according HTML element and delegates the rendering of each <code>TableCell</code> 
		 * to its own renderer.
		 * 
		 * @author smark
		 */
		class TableLineRenderer extends KnowWEDomRenderer
		{
			@Override
			public String render(Section sec, KnowWEUserContext user, String web, String topic) 
			{
				StringBuilder html = new StringBuilder();
				html.append( "<tr>" );
				html.append( SpecialDelegateRenderer.getInstance().render(sec, user, web, topic) );
				html.append( "</tr>" );
								
				return KnowWEEnvironment.maskHTML( html.toString() );
			}			
		}
		return new TableLineRenderer();
	}
	/**
	 * Handles the table lines. Introduced to the fact, that the LineSectionFinder
	 * allows empty lines. In the table context only lines with content are important
	 * (line break after Table tag had been rendered as cell).
	 * 
	 * @author smark
	 * @see SectionFinder
	 */
	class TableLineSectionFinder extends SectionFinder 
	{
		public TableLineSectionFinder(KnowWEObjectType type) 
		{
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section text, Section father,
				KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) 
		{
			String lineRegex = ".+(\\r?\\n)";
			Pattern linePattern = Pattern.compile( lineRegex , Pattern.MULTILINE);
			
	        Matcher tagMatcher = linePattern.matcher( text.getOriginalText() );		
	        ArrayList<Section> resultRegex = new ArrayList<Section>();
	        
	        while (tagMatcher.find()) 
			{
	        	resultRegex.add(Section.createSection(this.getType(), father, text, tagMatcher.start(), tagMatcher.end(), kbm, report, idg));
			}
			return resultRegex;
		}
	}	
	
}
