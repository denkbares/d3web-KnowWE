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
package de.d3web.we.taghandlernew;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.ContentType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.taghandler.TagHandler;
import de.d3web.we.taghandler.TagHandlerAttributeSubTreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * This class replaces the old TagHandlerType.
 * It uses the {@link DefaultMarkupType}.
 * The old markup was [{KnowWEPlugin taghandler,...}]
 * New is:
 * %%KnowWEPlugin 
 * content
 * @Taghandlername theHandlerkey
 * %
 * Also check {@link DefaultTagHandlerTypeContent}
 * 
 * @author Johannes Dienst
 *
 */
public class DefaultMarkupTagHandler extends DefaultMarkupType {

	public DefaultMarkupTagHandler() {
		super(m);
	}
	
	public DefaultMarkupTagHandler(DefaultMarkup markup) {
		super(markup);
	}

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("KnowWEPlugin");
		m.addContentType(new DefaultTagHandlerTypeContent());
		m.addAnnotation("Taghandlername", true);
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new DefaultTagRenderer();
	}
	
	/**
	 * TagHandlerRenderer for new Markup.
	 * 
	 * @author Johannes Dienst
	 *
	 */
	private class DefaultTagRenderer extends KnowWEDomRenderer {
		
		@SuppressWarnings("unchecked")
		@Override
		public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
			
			// To find the AttributValues and the taghandlerName
			Section markupContent = sec.findChildOfType(ContentType.class);

			if(markupContent.getObjectType() instanceof ContentType) {
				
				/** 
				 * get all found attributevalues.
				 * they are needed in the render Method of any TagHandler
				 * markupContent.getId() is used, because the
				 * {@link TagHandlerAttributeSectionFinder} uses the father-id
				 * to store the attributes.
				 */
				Map<String,String> attValues = null;
				Object storedValues = KnowWEEnvironment.getInstance()
						.getArticleManager(sec.getWeb()).getTypeStore()
						.getStoredObject(sec.getTitle(), markupContent.getID(),
								TagHandlerAttributeSubTreeHandler.ATTRIBUTE_MAP);
				if (storedValues != null) {
					if (storedValues instanceof Map) {
						attValues = (Map<String, String>) storedValues;
					}
				}
				if(attValues == null)
					attValues = new HashMap<String,String>();
				
				// Render the Taghandler with its attValues
				String taghandlerName = DefaultMarkupType.getAnnotation(sec, "taghandlername");
				HashMap<String, TagHandler> defaultTagHandlers =
					KnowWEEnvironment.getInstance().getDefaultTagHandlers();
				TagHandler handler = defaultTagHandlers.get(taghandlerName);
				
				if (handler != null) {
					String rendered = handler.render(sec.getTitle(), user,
							attValues, article.getWeb());
					string.append(KnowWEUtils.maskHTML(rendered));
				}
				
				// Taghandler not found
				if (handler == null) {
					string.append(KnowWEUtils
							.maskHTML("<div><p class='info box'>"));
					string.append(KnowWEUtils.maskHTML(KnowWEEnvironment
							.getInstance().getKwikiBundle(user).getString(
									"KnowWE.Taghandler.notFoundError")
							+ "</p></div>"));
				}
			}
		}
	}
}
