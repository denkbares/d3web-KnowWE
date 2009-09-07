package de.d3web.we.kdom.table;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TableLineBegin extends DefaultAbstractKnowWEObjectType {
	
	@Override
	protected void init() {
		sectionFinder = new TableLineEndFinder(this);
	}

	class TableLineEndFinder extends SectionFinder {

		public TableLineEndFinder(KnowWEObjectType type) {
			super(type);

		}

		@Override
		public List<Section> lookForSections(Section tmpSection,
				Section father, KnowledgeRepresentationManager kbm,
				KnowWEDomParseReport report, IDGenerator idg) {

			String originalText = tmpSection.getOriginalText();
			int index = originalText.indexOf("|");
			if(index == -1) return null;

			String start = originalText.substring(0, index);

			if (start.length() > 0) {
				List<Section> result = new ArrayList<Section>();
				result.add(Section.createSection(this.getType(), father,
						tmpSection, 0, index, kbm, report,
						idg));
				return result;
			}

			return null;
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
		class TableLineBeginRenderer extends KnowWEDomRenderer
		{
			@Override
			public String render(Section sec, KnowWEUserContext user, String web, String topic) 
			{								
				return KnowWEEnvironment.maskHTML( "" );
			}			
		}
		return new TableLineBeginRenderer();
	}



}
