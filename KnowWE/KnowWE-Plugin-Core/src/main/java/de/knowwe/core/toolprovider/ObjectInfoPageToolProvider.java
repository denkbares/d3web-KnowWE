/*
 * Copyright (C) 2010 denkbares GmbH, Germany
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
package de.knowwe.core.toolprovider;

import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * 
 * @author volker_belli
 * @created 01.12.2010
 */
public class ObjectInfoPageToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (section.get() instanceof SimpleTerm) {
			@SuppressWarnings("unchecked")
			Section<? extends SimpleTerm> s = (Section<? extends SimpleTerm>) section;
			return new Tool[] { getObjectInfoPageTool(s, userContext) };
		}
		return new Tool[] {};
	}

	protected Tool getObjectInfoPageTool(Section<? extends SimpleTerm> section, UserContext userContext) {
		String objectName = section.get().getTermIdentifier(section).toExternalForm();
		String jsAction = "window.location.href = " +
				"'Wiki.jsp?page=ObjectInfoPage&objectname=' + encodeURIComponent('" +
				objectName + "')";
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/infoPage16.png",
				"Show Info Page",
				"Opens the information page for the specific object to show its usage inside this wiki.",
				jsAction);
	}

}
