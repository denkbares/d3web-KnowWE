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

package de.d3web.we.taghandler;

import java.util.Map;

import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Jochen
 * 
 * Interface for a KnowWE-TagHandler. This mechanism is delegated to the 
 * plugin mechanism of JSPWiki (thus (right now still) depending on JSPWiki)
 * 
 * When the KnowWEPlugin is called by JSPWiki the first attribute-name is taken as 
 * Handler-name. All registered TagHandler are checked for this name. In the case of 
 * (case-insensitiv) matching the render method is called. The resulting String is directly
 * put into the wiki page (by JSPWiki) without going through the rendering pipeline.
 *
 */
public interface TagHandler {
	
	/**
	 * Ought to return lowercase!
	 * 
	 * @return name in lowercase
	 */
	public String getTagName();
	

	/**
	 * The resulting String is rendered into the Wiki-page
	 * It can (in most cases should) contain final HTML.
	 * 
	 * @param topic
	 * @param user
	 * @param value
	 * @param web
	 * @return
	 */
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web);
	 
}
