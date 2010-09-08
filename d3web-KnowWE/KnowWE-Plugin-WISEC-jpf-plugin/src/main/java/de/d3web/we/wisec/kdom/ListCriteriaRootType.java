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

package de.d3web.we.wisec.kdom;

import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.defaultMarkup.AnnotationType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wisec.kdom.subtreehandler.ListCriteriaOWLSubtreeHandler;

/**
 * The root type of the ListCriteria section
 * 
 * @author Sebastian Furth
 */
public class ListCriteriaRootType extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("ListCriteria");
		m.addContentType(new ListCriteriaType());
		m.addAnnotation("ListID", true);
		// m.addAnnotation("UpperlistID", true);
		m.addAnnotation(ListCriteriaOWLSubtreeHandler.UPPERLIST_ID, true);

	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return new CriteriaListDefaultMarkupRenderer();
	}

	public ListCriteriaRootType() {
		super(m);
	}

	/**
	 * 
	 * @author Jochen
	 * @created 08.09.2010
	 * 
	 *          A modified DefaultMarkupRenderer that without PRE, header and
	 *          annotations
	 * 
	 */
	class CriteriaListDefaultMarkupRenderer extends KnowWEDomRenderer<DefaultMarkupType> {

		private final String iconPath;

		public CriteriaListDefaultMarkupRenderer() {
			this(null);
		}

		public CriteriaListDefaultMarkupRenderer(String iconPath) {
			this.iconPath = iconPath;
		}

		@Override
		public void render(KnowWEArticle article, Section<DefaultMarkupType> section, KnowWEUserContext user, StringBuilder string) {

			String id = section.getID();
			String name = "<span>" + section.getObjectType().getName() + "</span>";
			String icon = "";
			if (this.iconPath != null) {
				icon = "<img class='markupIcon' src='" + this.iconPath + "'></img> ";
			}
			string.append(KnowWEUtils.maskHTML("<div id=\"" + id
					+ "\" class='defaultMarkup'>\n"));
			// string.append(KnowWEUtils.maskHTML("<div class='markupHeader'>" +
			// icon + name
			// + "</div>\n"));
			// render pre-formatted box

			// add an anchor to enable direct link to the section
			String anchorName = KnowWEUtils.getAnchor(section);
			string.append(KnowWEUtils.maskHTML("<a name='" + anchorName + "'></a>"));

			// render messages and content
			renderContents(article, section, user, string);

			// and close the box
			string.append(KnowWEUtils.maskHTML("</div>\n"));
		}

		protected void renderContents(KnowWEArticle article, Section<? extends DefaultMarkupType> section, KnowWEUserContext user, StringBuilder string) {
			List<Section<?>> subsecs = section.getChildren();
			Section<?> first = subsecs.get(0);
			Section<?> last = subsecs.get(subsecs.size() - 1);
			// bloody hack to not render annotations...
			boolean annotationPartStarted = false;
			for (Section<?> subsec : subsecs) {
				if (subsec == first && subsec.getObjectType() instanceof PlainText)
					continue;
				if (subsec == last && subsec.getObjectType() instanceof PlainText)
					continue;
				if (subsec.getOriginalText().trim().startsWith("@")) {
					annotationPartStarted = true;
				}
				if (!(subsec.getObjectType() instanceof AnnotationType)
						&& !annotationPartStarted) {
					subsec.getObjectType().getRenderer().render(article, subsec, user,
							string);
				}
			}
		}

	}
}
