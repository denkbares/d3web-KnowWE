package de.knowwe.core.taghandler;

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

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.plugin.Plugins;

public class TagHandlerType extends AbstractType {

	public TagHandlerType() {
		// searches for Strings like [{KnowWEPlugin ...}]
		this.setSectionFinder(new RegexSectionFinder("\\[\\{KnowWEPlugin [^}]*}]"));

		this.addChildType(new TagHandlerTypeStartSymbol());
		this.addChildType(new TagHandlerTypeEndSymbol());
		for (TagHandler tagHandler : Plugins.getTagHandlers()) {
			this.addChildType(new TagHandlerTypeContent(tagHandler.getTagName()));
		}
		this.setRenderer(new TagRenderer());
	}

	@Override
	public String getName() {
		return "KnowWEPlugin";
	}

}
