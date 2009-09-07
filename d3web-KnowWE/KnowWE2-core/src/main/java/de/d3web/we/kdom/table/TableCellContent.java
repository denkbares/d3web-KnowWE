package de.d3web.we.kdom.table;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;


public class TableCellContent extends DefaultAbstractKnowWEObjectType {
	
	@Override
	protected void init() {
		sectionFinder =  new AllTextFinder(this);
	}

	/**
	 * <p>Returns the renderer for the TableCellContent.</p>
	 * 
	 * @return {@link KnowWEDomRenderer}
	 * @see TableCellContentRenderer
	 */
	@Override
	public KnowWEDomRenderer getRenderer() 
	{
		return new TableCellContentRenderer();
	}
}
