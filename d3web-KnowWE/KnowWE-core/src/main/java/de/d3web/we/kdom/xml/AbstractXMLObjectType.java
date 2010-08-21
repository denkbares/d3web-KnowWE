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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public class AbstractXMLObjectType extends DefaultAbstractKnowWEObjectType {

	public static final String HEAD = "head";
	public static final String TAIL = "tail";
	public static final String TAGNAME = "tagName";

	private final String xmlTagName;

	private final boolean anyXML;

	private static AbstractXMLObjectType defaultInstance;

	public static AbstractXMLObjectType getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new AbstractXMLObjectType();
		}
		return defaultInstance;
	}

	public Section getContentChild(Section s) {
		if (s.getObjectType() instanceof AbstractXMLObjectType) {
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
				s.getTitle(), s.getID(), XMLSectionFinder.ATTRIBUTE_MAP_STORE_KEY);

	}

	// public static Map<String, String> getAttributeMapFor(
	// Section<? extends AbstractXMLObjectType> s) {
	// return (Map<String, String>) KnowWEUtils.getStoredObject(s.getWeb(), s
	// .getTitle(), s.getId(),
	// XMLSectionFinder.ATTRIBUTE_MAP_STORE_KEY);
	//
	// }

	public static String getTagName(Section<? extends AbstractXMLObjectType> s) {
		Map<String, String> attributeMapFor = getAttributeMapFor(s);
		if (attributeMapFor != null) {
			return attributeMapFor.get(AbstractXMLObjectType.TAGNAME);
		}
		else {
			return null;
		}
	}

	public static Section<AbstractXMLObjectType> getXMLFatherElement(Section<? extends AbstractXMLObjectType> s) {
		Section xmlFather = s.findAncestorOfType(AbstractXMLObjectType.class);
		return xmlFather;
	}

	public static int getXMLDepth(Section<AbstractXMLObjectType> s) {

		return getXMLDepth(s, 0);
	}

	private static int getXMLDepth(Section<AbstractXMLObjectType> s, int depth) {
		Section xmlFather = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xmlFather != null && xmlFather.getObjectType() instanceof AbstractXMLObjectType) {
			return getXMLDepth(xmlFather, ++depth);
		}
		else {
			return depth;
		}

	}

	public static Section<? extends AbstractXMLObjectType> findSubSectionOfTag(
			String tagname, Section<? extends AbstractXMLObjectType> s) {
		String tagName2 = getTagName(s);
		if (tagname.equals(tagName2)) {
			return s;
		}

		List<Section<? extends KnowWEObjectType>> children = s.getChildren();
		for (Section<? extends KnowWEObjectType> section : children) {
			if (section.getObjectType() instanceof XMLContent) {
				List<Section<AbstractXMLObjectType>> nodes = section
						.findChildrenOfType(AbstractXMLObjectType.class);
				for (Section<? extends AbstractXMLObjectType> section2 : nodes) {
					Section<? extends AbstractXMLObjectType> found = findSubSectionOfTag(
							tagname, section2);
					if (found != null) return found;
				}

			}
		}
		return null;

	}

	public static void findSubSectionsOfTag(
			String tagname, Section<? extends AbstractXMLObjectType> s, Collection<Section<? extends AbstractXMLObjectType>> c) {
		String tagName2 = getTagName(s);
		if (tagname.equals(tagName2)) {

			c.add(s);
		}
		List<Section<? extends KnowWEObjectType>> children = s.getChildren();
		for (Section section : children) {
			if (section.getObjectType() instanceof XMLContent) {
				Section<XMLContent> conSec = section;
				List<Section<AbstractXMLObjectType>> nodes = conSec
						.findChildrenOfType(AbstractXMLObjectType.class);
				for (Section<AbstractXMLObjectType> section2 : nodes) {
					findSubSectionsOfTag(
							tagname, section2, c);

				}

			}
		}

	}

	/**
	 * ObjectType for XML-Sections with tag name <code>tagName</code>.
	 */
	public AbstractXMLObjectType(String tagName) {
		this(tagName, false);
	}

	/**
	 * ObjectType for XML-Sections with no specific tag name. Finds all
	 * XML-Sections independent of their tag names.
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
		this.addSubtreeHandler(new RegisterNamespaceDefinitionHandler());
	}

	public String getXMLTagName() {
		return xmlTagName;
	}

	static class RegisterNamespaceDefinitionHandler extends SubtreeHandler<AbstractXMLObjectType> {

		public RegisterNamespaceDefinitionHandler() {
			super(true);
		}

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<AbstractXMLObjectType> s) {
			return super.needsToCreate(article, s) && s.getTitle().equals(article.getTitle());
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<AbstractXMLObjectType> s) {

			String value = getAttributeMapFor(s).get("namespace");

			if (value != null) {
				s.addNamespace(value);
				KnowWEEnvironment.getInstance().getNamespaceManager(
						article.getWeb()).registerNamespaceDefinition(s);
			}
			return null;
		}

		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<AbstractXMLObjectType> s) {
			return super.needsToDestroy(article, s) && s.getTitle().equals(article.getTitle());
		}

		@Override
		public void destroy(KnowWEArticle article, Section<AbstractXMLObjectType> s) {

			String value = getAttributeMapFor(s).get("namespace");

			if (value != null) {
				KnowWEEnvironment.getInstance().getNamespaceManager(
						article.getWeb()).unregisterNamespaceDefinition(s);
				s.removeNamespace(value);
			}
		}

	}

}
