package de.d3web.we.kdom.table;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * TableCellStart.
 * 
 * This class represents the start of a <code>TableCell</code>. Therefore it handles
 * the rendering and the sectioning of the <code>TableCell</code> start markup. 
 * 
 * @author smark
 * 
 * @see AbstractKnowWEObjectType
 * @see TableCell
 */
public class TableCellStart extends DefaultAbstractKnowWEObjectType {
	
	@Override
	protected void init() {
		sectionFinder = new TableCellStartFinder(this);
	}

	class TableCellStartFinder extends SectionFinder {

		public TableCellStartFinder(KnowWEObjectType type) {
			super(type);

		}

		@Override
		public List<Section> lookForSections(Section tmpSection, Section father, KnowledgeRepresentationManager kbm,
				KnowWEDomParseReport report, IDGenerator idg) {

			String originalText = tmpSection.getOriginalText();
			int index = originalText.indexOf("|") + 1;
			if(index == -1) return null;

			List<Section> result = new ArrayList<Section>();
			result.add(Section.createSection(this.getType(), father, tmpSection, 0, index, kbm, report, idg));
			return result;			
		}
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
		class TableCellStartRenderer extends KnowWEDomRenderer
		{
			@Override
			public String render(Section sec, KnowWEUserContext user, String web, String topic) 
			{								
				return KnowWEEnvironment.maskHTML( "" );
			}			
		}
		return new TableCellStartRenderer();
	}



}
