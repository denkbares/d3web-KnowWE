/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.user.UserContext;

/**
 * Markup type to detect jsp-wiki definitions.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 14.02.2014
 */
public class InlineDefinitionType extends DefinitionType {

	public static class InlineDefinitionRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			renderJSPWikiAnchor(Sections.cast(section, DefinitionType.class), result);
			result.appendHtml("<span class='inline-definition'>");

			result.appendHtml("<span class='inline-definition-head'>");
			result.append(Sections.successor(section, DefinitionHead.class), user);
			result.appendHtml("</span>");

			result.appendHtml("<span class='inline-definition-data'>");
			result.append(Sections.successor(section, DefinitionData.class), user);
			result.appendHtml("</span>");

			result.appendHtml("</span>");
		}
	}

	public InlineDefinitionType() {
		this.setSectionFinder(new RegexSectionFinder(
				"^;;[^;:\n\r]+:[^\n\r]+((\n\r?)|$)",
				Pattern.MULTILINE));
		setRenderer(new InlineDefinitionRenderer());
	}
}
