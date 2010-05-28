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
package de.d3web.we.kdom.dashTree;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.CommentLineType;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Jochen
 * 
 * A simple DashTree. It takes all and tries to build a (dash-) SubTree (which is defined
 * recursivly).
 *
 */
public class DashTree extends DefaultAbstractKnowWEObjectType{
	
	public DashTree() {
		this.sectionFinder = new AllTextSectionFinder();
		this.childrenTypes.add(new SubTree());
		this.childrenTypes.add(new CommentLineType());
		this.setCustomRenderer(new PreRenderer());
	}
	
	class PreRenderer extends KnowWEDomRenderer {

		@Override
		public void render(KnowWEArticle article, Section sec,
				KnowWEUserContext user, StringBuilder string) {

			string.append("{{{");
			DelegateRenderer.getInstance().render(article, sec, user, string);
			string.append("}}}");
			
		}
		
		
		
	}

}
