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
package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.user.UserContext;

/**
 *
 * @author volker_belli
 * @created 01.12.2010
 */
public class ObjectInfoPageToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, UserContext userContext) {
		if (section.get() instanceof KnowWETerm<?>) {
			@SuppressWarnings("unchecked")
			Section<? extends KnowWETerm<?>> s = (Section<? extends KnowWETerm<?>>) section;
			return new Tool[] { getObjectInfoPageTool(article, s, userContext) };
		}
		return new Tool[] {};
	}

	protected Tool getObjectInfoPageTool(KnowWEArticle article, @SuppressWarnings("rawtypes") Section<? extends KnowWETerm> section, UserContext userContext) {
		@SuppressWarnings("unchecked")
		String objectName = section.get().getTermName(section);
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
