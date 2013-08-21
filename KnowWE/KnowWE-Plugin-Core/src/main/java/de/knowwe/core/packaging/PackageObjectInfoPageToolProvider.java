/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.packaging;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.taghandler.ObjectInfoTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * 
 * @author stefan
 * @created 08.05.2013
 */
public class PackageObjectInfoPageToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		@SuppressWarnings("unchecked")
		Section<? extends Term> s = (Section<? extends Term>) section;
		return new Tool[] { getObjectInfoPageTool(s, userContext) };
	}

	protected Tool getObjectInfoPageTool(Section<? extends Term> section, UserContext userContext) {
		String packageName = section.getText();
		String jsAction = "window.location.href = "
				+ "'Wiki.jsp?page=ObjectInfoPage&amp;" + ObjectInfoTagHandler.TERM_IDENTIFIER
				+ "=' + encodeURIComponent('"
				+ maskTermForHTML(packageName)
				+ "') + '&amp;" + ObjectInfoTagHandler.OBJECT_NAME + "=' + encodeURIComponent('"
				+ maskTermForHTML(packageName) + "')";
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/infoPage16.png",
				"Show Info Page",
				"Opens the information page for the specific object to show its usage inside this wiki.",
				jsAction);
	}

	private String maskTermForHTML(String string) {
		string = string.replace("\\", "\\\\");
		string = Strings.encodeHtml(string);
		return string;
	}

}
