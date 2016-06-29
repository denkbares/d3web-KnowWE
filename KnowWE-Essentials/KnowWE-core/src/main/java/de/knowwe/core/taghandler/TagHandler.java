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

package de.knowwe.core.taghandler;

import java.util.Map;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

/**
 * @author Jochen
 * 
 *         Interface for a KnowWE-TagHandler. This mechanism is delegated to the
 *         plugin mechanism of JSPWiki (thus (right now still) depending on
 *         JSPWiki)
 * 
 *         When the KnowWEPlugin is called by JSPWiki the first attribute-name
 *         is taken as Handler-name. All registered TagHandler are checked for
 *         this name. In the case of (case-insensitiv) matching the render
 *         method is called. The resulting String is directly put into the wiki
 *         page (by JSPWiki) without going through the rendering pipeline.
 * 
 */
public interface TagHandler {

	/**
	 * The name of the tag handler.
	 * <p>
	 * In contrast to previous KnowWE version this name should be defined as
	 * camel case. There is no need to define it in lower cases any longer.
	 * 
	 * @return the name of this tag handler
	 */
	String getTagName();

	/**
	 * Returns if the tag handler needs to be updated automatically when the
	 * session has changed.
	 * 
	 * @created 20.10.2010
	 * @return the auto update flag
	 */
	boolean requiresAutoUpdate();

	/**
	 * Renders the tag handler into a wiki markup string. The resulting string
	 * is rendered into the wiki page as wiki markup.
	 * 
	 * @param section the section where the tag handler is used.
	 * @param parameters the parameters of the tag handler invocation
	 * @param result the result where the rendered contents are appended
	 * @param topic the article the tag handler is rendered for.
	 * @param user the user context for this request
	 */
	void render(Section<?> section, UserContext userContext, Map<String, String> parameters, RenderResult result);

	/**
	 * Returns an usage example of this tag handler.
	 * 
	 * @created 15.10.2010
	 * @return an example usage string
	 */
	String getExampleString();

	/**
	 * Returns a description of the tag handler's synopsis.
	 * 
	 * @created 15.10.2010
	 * @param user the current user the description is for
	 * @return a description of the tag handler
	 */
	String getDescription(UserContext user);

}
