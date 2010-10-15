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

import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Jochen
 * 
 *         An abstract implementation of the TagHandler Interface handling the
 *         tagName in lowercase.
 * 
 */
public abstract class AbstractHTMLTagHandler extends AbstractTagHandler {

	public AbstractHTMLTagHandler(String name) {
		super(name);
	}

	@Override
	public final String render(String web, String topic, KnowWEUserContext userContext, Map<String, String> parameters) {
		return KnowWEUtils.maskHTML(renderHTML(topic, userContext, parameters, web));
	}

	/**
	 * Renders the tag handler into a html string. The resulting html string is
	 * rendered into the wiki page as html. This method mus be overwritten by
	 * the deriving classes to produce their output html.
	 * 
	 * @param web the web where the tag handler is included.
	 * @param topic the topic of the page where the tag handler is included.
	 * @param user the user context for this request
	 * @param parameters the parameters of the tag handler invocation
	 * @return the resulting wiki markup text
	 */
	public abstract String renderHTML(String topic, KnowWEUserContext user, Map<String, String> parameters, String web);

}
