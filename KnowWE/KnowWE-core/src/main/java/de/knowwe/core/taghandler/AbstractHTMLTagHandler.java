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
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;

/**
 * @author Jochen
 * 
 *         An abstract implementation of the TagHandler Interface handling the
 *         tagName in lowercase.
 * 
 */
public abstract class AbstractHTMLTagHandler extends AbstractTagHandler {

	private boolean maskJSPWikiSyntax = false;

	public AbstractHTMLTagHandler(String name) {
		super(name);
	}

	public void setMaskJSPWikiSyntax(boolean mask) {
		this.maskJSPWikiSyntax = mask;
	}

	@Override
	public final String render(Section<?> section, UserContext userContext, Map<String, String> parameters) {

		String renderedHTML = renderHTML(section.getTitle(), userContext, parameters,
				section.getWeb());
		String masked = Strings.maskHTML(renderedHTML);
		if (maskJSPWikiSyntax) {
			masked = Strings.maskJSPWikiMarkup(masked);
		}
		return masked;
	}

	/**
	 * Renders the tag handler into a html string. The resulting html string is
	 * rendered into the wiki page as html. This method mus be overwritten by
	 * the deriving classes to produce their output html.
	 * 
	 * @param web the web where the tag handler is included.
	 * @param title the title of the page where the tag handler is included.
	 * @param user the user context for this request
	 * @param parameters the parameters of the tag handler invocation
	 * @return the resulting wiki markup text
	 */
	public abstract String renderHTML(String title, UserContext user, Map<String, String> parameters, String web);

}
