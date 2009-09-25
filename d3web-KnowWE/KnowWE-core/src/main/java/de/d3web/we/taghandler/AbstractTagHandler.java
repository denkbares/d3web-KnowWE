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

package de.d3web.we.taghandler;

import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Jochen
 * 
 * An abstract implementation of the TagHandler Interface handling the tagName in lowercase.
 *
 */
public abstract class AbstractTagHandler implements TagHandler{
	
	private String name = null;
	
	public AbstractTagHandler(String name) {
		this.name = name.toLowerCase();
	}
	
	public static Map<String,String> getAttributeMap(Section tagHandlerSection) {
		Object o = KnowWEUtils.getStoredObject(tagHandlerSection.getWeb(),tagHandlerSection.getTitle(), tagHandlerSection.getFather().getId(), TagHandlerAttributeSectionFinder.ATTRIBUTE_MAP);
		if(o != null) {
			return (Map<String,String>)o;
		}
		return null;
		
	}

	@Override
	public String getTagName() {
		return name.toLowerCase();
	}
	
	public String getExampleString() {
		return "[{KnowWEPlugin " + getTagName() + "}]";
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.Taghandler.standardDescription");
	}
	
}
