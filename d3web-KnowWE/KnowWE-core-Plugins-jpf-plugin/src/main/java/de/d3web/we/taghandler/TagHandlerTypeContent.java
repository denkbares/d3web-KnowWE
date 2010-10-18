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

package de.d3web.we.taghandler;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.Strings;

/**
 * A taghandler can have attributes specified in this schema: %%KnowWEPlugin
 * taghandlername, att1=aat, att2=aata,... The TaghandlerAttributeSectionfinder
 * stores the attributes in the KnowWESectionStore.
 * 
 * @author Johannes Dienst
 * 
 */
public class TagHandlerTypeContent extends DefaultAbstractKnowWEObjectType {

	private class PrefixedSectionFinder extends AllTextSectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
			if (Strings.startWithIgnoreCase(text, tagHandlerName)) {
				return super.lookForSections(text, father, type);
			}
			return null;
		}
	}

	private final String tagHandlerName;

	public TagHandlerTypeContent(String name) {
		this.tagHandlerName = name;
	}

	@Override
	protected void init() {
		this.sectionFinder = new PrefixedSectionFinder();
		this.addSubtreeHandler(new TagHandlerAttributeSubTreeHandler());
	}

	@Override
	public String getName() {
		return this.tagHandlerName;
	}

}
