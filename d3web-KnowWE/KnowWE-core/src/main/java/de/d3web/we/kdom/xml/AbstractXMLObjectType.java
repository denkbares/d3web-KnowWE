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

package de.d3web.we.kdom.xml;

import java.util.Map;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.utils.KnowWEUtils;

public class AbstractXMLObjectType extends DefaultAbstractKnowWEObjectType{
	
	public static final String HEAD = "head";
	public static final String TAIL = "tail";
	public static final String TAGNAME = "tagName";
	
	private String xmlTagName;
	
	private boolean anyXML;
	
	public Section getContentChild(Section s) {
		if(s.getObjectType() instanceof AbstractXMLObjectType) {
			Section content = s.findSuccessor(XMLContent.class);
			return content;
		}
		return null;
	}
	
	@Override
	public String getName() {
		return getXMLTagName();
	}
	
	public static Map<String, String> getAttributeMapFor(Section<? extends AbstractXMLObjectType> s) {
		return (Map<String, String>) KnowWEUtils.getStoredObject(s.getWeb(),
				s.getTitle(), s.getId(), XMLSectionFinder.ATTRIBUTE_MAP_STORE_KEY);
		
	}

	/**
	 * ObjectType for XML-Sections with tag name <code>tagName</code>.
	 */
	public AbstractXMLObjectType(String tagName) {
		this.xmlTagName = tagName;
		this.anyXML = false;
		childrenTypes.add(0, new XMLHead());
		childrenTypes.add(1, new XMLTail());
	}
	
	/**
	 * ObjectType for XML-Sections with no specific tag name.
	 * Finds all XML-Sections independet of thier tag names.
	 */
	public AbstractXMLObjectType() {
		this.xmlTagName = "AnyXMLObjectType";
		this.anyXML = true;
		childrenTypes.add(0, new XMLHead());
		childrenTypes.add(1, new XMLTail());
	}

	/**
	 * XXX Override sectioner here. If in constructor strange breaks in layout occur.
	 */
	@Override
	public SectionFinder getSectioner() {
		this.sectionFinder = new XMLSectionFinder(anyXML ? null : xmlTagName);
		return super.getSectioner();
	}

	public String getXMLTagName() {
		return xmlTagName;
	}

}
