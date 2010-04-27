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
package de.d3web.we.kdom.renderRestriction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;

/**
 * Type for the group-declaration for the RenderRestriction types
 *
 * @author Jochen
 *
 */
public class GroupDeclaration extends DefaultAbstractKnowWEObjectType {

	Pattern p = Pattern.compile("group:?(.*)", Pattern.DOTALL);

	public GroupDeclaration() {
		this.setSectionFinder(AllTextFinderTrimmed.getInstance());
	}

	public String getGroup(Section<GroupDeclaration> s) {
		Matcher matcher = p.matcher(s.getOriginalText());
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;

	}



}
