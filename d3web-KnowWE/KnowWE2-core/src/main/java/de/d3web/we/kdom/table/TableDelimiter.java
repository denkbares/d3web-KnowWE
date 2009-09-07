package de.d3web.we.kdom.table;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectioner;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TableDelimiter extends DefaultAbstractKnowWEObjectType {

	
	
	@Override
	protected void init() {
		sectionFinder =  new RegexSectioner("\\|", this);
	}
	
	
	/**
	 * Returns the renderer for the <code>TableContent</code>.
	 */
	public KnowWEDomRenderer getRenderer()
	{
		/**
		 * This is a renderer for the TableContent. I wraps the <code>Table</code>
		 * tag into an own DIV and delegates the rendering of each <code>TableCellContent</code> 
		 * to its own renderer.
		 * 
		 * @author smark
		 */
		class TableDelimiterRenderer extends KnowWEDomRenderer
		{
			@Override
			public String render(Section sec, KnowWEUserContext user, String web, String topic) 
			{								
				return KnowWEEnvironment.maskHTML( "" );
			}			
		}
		return new TableDelimiterRenderer();
	}
	

	
}
