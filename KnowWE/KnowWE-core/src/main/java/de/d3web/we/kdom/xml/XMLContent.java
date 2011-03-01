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

package de.d3web.we.kdom.xml;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionIDDeclarant;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;

public class XMLContent extends AbstractType implements SectionIDDeclarant {



	public XMLContent() {
		sectionFinder = new AllTextSectionFinder();
	}

	public XMLContent(Type child) {
		this();
		this.childrenTypes.add(child);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public static final String SEPARATOR = "/";
	public static final String CONTENT_SUFFIX = "_content";

	@Override
	public String createSectionID(Section<? extends Type> father) {
		return getEndOfId(father.getID()) + CONTENT_SUFFIX;
	}

	private String getEndOfId(String id) {
		return id.substring(id.lastIndexOf(SEPARATOR) + 1);
	}

}
