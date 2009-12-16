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

import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
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
	

//	public static Map<String, String> getAttributeMapFor(
//			Section<? extends AbstractXMLObjectType> s) {
//		return (Map<String, String>) KnowWEUtils.getStoredObject(s.getWeb(), s
//				.getTitle(), s.getId(),
//				XMLSectionFinder.ATTRIBUTE_MAP_STORE_KEY);
//
//	}

	public static String getTagName(Section<AbstractXMLObjectType> s) {
		Map<String, String> attributeMapFor = getAttributeMapFor(s);
		if (attributeMapFor != null) {
			return attributeMapFor.get(AbstractXMLObjectType.TAGNAME);
		} else {
			return null;
		}
	}
	
	public static Section<AbstractXMLObjectType> getXMLFatherElement(Section<? extends AbstractXMLObjectType> s) {
		Section xmlFather = KnowWEObjectTypeUtils.getAncestorOfType(s.getFather(), AbstractXMLObjectType.class);
		return (Section<AbstractXMLObjectType>)xmlFather;
	}

	public static int getXMLDepth(Section<AbstractXMLObjectType> s) {

		return getXMLDepth(s, 0);
	}

	private static int getXMLDepth(Section<AbstractXMLObjectType> s, int depth) {
		Section xmlFather = KnowWEObjectTypeUtils.getAncestorOfType(s.getFather(), AbstractXMLObjectType.class);
		if (xmlFather != null && xmlFather.getObjectType() instanceof AbstractXMLObjectType) {
			return getXMLDepth(xmlFather, ++depth);
		} else {
			return depth;
		}

	}

	public static Section<AbstractXMLObjectType> findSubSectionOfTag(
			String tagname, Section<AbstractXMLObjectType> s) {
		if (tagname.equals(getTagName(s))) {

			return s;
		}
		List<Section> children = s.getChildren();
		for (Section section : children) {
			if (section.getObjectType() instanceof XMLContent) {
				List<Section> nodes = section
						.findChildrenOfType(AbstractXMLObjectType.class);
				for (Section section2 : nodes) {
					Section<AbstractXMLObjectType> found = findSubSectionOfTag(
							tagname, (Section<AbstractXMLObjectType>) section);
					if (found != null)
						return found;
				}

			}
		}
		return null;

	}

	/**
	 * ObjectType for XML-Sections with tag name <code>tagName</code>.
	 */
	public AbstractXMLObjectType(String tagName) {
		this(tagName, false);
	}
	
	/**
	 * ObjectType for XML-Sections with no specific tag name.
	 * Finds all XML-Sections independent of their tag names.
	 */
	public AbstractXMLObjectType() {
		this("AnyXMLObjectType", true);
	}
	
	private AbstractXMLObjectType(String tagName, boolean anyXML) {
		this.xmlTagName = tagName;
		this.anyXML = false;
		childrenTypes.add(0, new XMLHead());
		childrenTypes.add(1, new XMLTail());
		this.sectionFinder = new XMLSectionFinder(anyXML ? null : xmlTagName);
	}


	public String getXMLTagName() {
		return xmlTagName;
	}

}
