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

package de.d3web.we.taghandler;

import java.util.Map;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * A bas class for tag handlers having their rendered wiki markup content
 * integrated in a section like for DefaultMarkupTypes. It automatically support
 * the tool extensions, identically to DefaultMarkupType.
 * 
 * @author volker_belli
 * @created 15.10.2010
 */
public abstract class AbstractDefaultStyledTagHandler extends AbstractTagHandler {

	public AbstractDefaultStyledTagHandler(String name) {
		super(name);
	}

	public AbstractDefaultStyledTagHandler(String name, boolean autoUpdate) {
		super(name, autoUpdate);
	}

	@Override
	public final String render(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext, Map<String, String> parameters) {
		String content = renderContent(article, section, userContext, parameters);
		Section<TagHandlerTypeContent> tagNameSection = section.findSuccessor(TagHandlerTypeContent.class);
		String sectionID = section.getID();
		Tool[] tools = ToolUtils.getTools(article, tagNameSection, userContext);

		StringBuilder buffer = new StringBuilder();
		DefaultMarkupRenderer.renderDefaultMarkupStyled(
				getTagName(), content, sectionID, tools, buffer);
		return buffer.toString();
	}

	/**
	 * Renders the content of the handler into wiki markup. The resulting markup
	 * text is rendered into the wiki page as usual wiki markup enterey by the
	 * user, but it is integrated in a DefaultMarkupType-Styled section. This
	 * method must be overwritten by the deriving classes to produce their
	 * output wiki markup.
	 * 
	 * @param web the web where the tag handler is included.
	 * @param topic the topic of the page where the tag handler is included.
	 * @param user the user context for this request
	 * @param parameters the parameters of the tag handler invocation
	 * @return the resulting wiki markup text
	 */
	public abstract String renderContent(KnowWEArticle article, Section<?> section, KnowWEUserContext user, Map<String, String> parameters);

}
