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
package de.knowwe.core.dashtree;

import java.util.List;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.InvalidKDOMSchemaModificationOperation;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderDivCorrectTrimmed;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.knowwe.core.CommentLineType;

/**
 * @author Jochen
 * 
 *         A simple DashTree. It takes all and tries to build a (dash-) SubTree
 *         (which is defined recursivly).
 * 
 */

public class DashTree extends DefaultAbstractKnowWEObjectType {

	public DashTree() {
		this.sectionFinder = new AllTextFinderDivCorrectTrimmed();
		this.childrenTypes.add(new DashSubtree());
		this.childrenTypes.add(new CommentLineType());
		this.childrenTypes.add(new OverdashedElement());
		this.setCustomRenderer(new PreRenderer());
	}

	/**
	 * 
	 * replaces the inherited default DashTreeElementContent by the customized
	 * DashTreeElementContent (PropertyDashTreeElementContent) which is a type
	 * that parses and compiles Property-definitions
	 * 
	 * @param dashTree
	 * @param newContentType
	 */
	protected void replaceDashTreeElementContentType(AbstractKnowWEObjectType dashTree, DashTreeElementContent newContentType) {
		List<KnowWEObjectType> types = dashTree.getAllowedChildrenTypes();
		for (KnowWEObjectType knowWEObjectType : types) {
			if (knowWEObjectType instanceof DashSubtree) {
				List<KnowWEObjectType> content = knowWEObjectType
						.getAllowedChildrenTypes();
				for (KnowWEObjectType knowWEObjectType2 : content) {
					if (knowWEObjectType2 instanceof DashTreeElement) {
						try {
							((AbstractKnowWEObjectType) knowWEObjectType2)
									.replaceChildType(
											newContentType,
											DashTreeElementContent.class);

						}
						catch (InvalidKDOMSchemaModificationOperation e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	class PreRenderer extends KnowWEDomRenderer<DashTree> {

		@Override
		public void render(KnowWEArticle article, Section<DashTree> sec,
				KnowWEUserContext user, StringBuilder string) {

			string.append("{{{");
			DelegateRenderer.getInstance().render(article, sec, user, string);
			string.append("}}}");

		}

	}

}
