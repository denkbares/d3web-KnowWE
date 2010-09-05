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

package de.d3web.we.kdom.table;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * TableCellStart.
 * 
 * This class represents the start of a <code>TableCell</code>. Therefore it
 * handles the rendering and the sectioning of the <code>TableCell</code> start
 * markup.
 * 
 * @author smark
 * 
 * @see AbstractKnowWEObjectType
 * @see TableCell
 */
public class TableCellStart extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		sectionFinder = new TableCellStartSectionFinder();
	}

	public class TableCellStartSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
			int index = text.indexOf("|") + 1;
			if (index == -1) return null;

			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			result.add(new SectionFinderResult(0, index));
			return result;
		}
	}

	/**
	 * Returns the renderer for the <code>TableContent</code>.
	 */
	@Override
	public KnowWEDomRenderer getRenderer() {

		/**
		 * This is a renderer for the TableContent. I wraps the
		 * <code>Table</code> tag into an own DIV and delegates the rendering of
		 * each <code>TableCellContent</code> to its own renderer.
		 * 
		 * @author smark
		 */
		class TableCellStartRenderer extends KnowWEDomRenderer {

			@Override
			public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
				string.append(KnowWEEnvironment.maskHTML(""));
			}
		}
		return new TableCellStartRenderer();
	}
}
