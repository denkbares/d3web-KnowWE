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
import de.d3web.we.core.packaging.KnowWEPackageManager;
import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public class AbstractXMLType extends AbstractType {

	public static final String HEAD = "head";
	public static final String TAIL = "tail";
	public static final String TAGNAME = "tagName";

	private final String xmlTagName;

	private static AbstractXMLType defaultInstance;

	public static AbstractXMLType getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new AbstractXMLType();
		}
		return defaultInstance;
	}

	public Section<XMLContent> getContentChild(Section<?> s) {
		if (s.get() instanceof AbstractXMLType) {
			Section<XMLContent> content = Sections.findSuccessor(s, XMLContent.class);
			return content;
		}
		return null;
	}

	@Override
	public String getName() {
		return getXMLTagName();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getAttributeMapFor(Section<?> s) {
		return (Map<String, String>) KnowWEUtils.getStoredObject(null, s,
				SectionFinderResult.ATTRIBUTE_MAP_STORE_KEY);

	}

	// public static Map<String, String> getAttributeMapFor(
	// Section<? extends AbstractXMLObjectType> s) {
	// return (Map<String, String>) KnowWEUtils.getStoredObject(s.getWeb(), s
	// .getTitle(), s.getId(),
	// XMLSectionFinder.ATTRIBUTE_MAP_STORE_KEY);
	//
	// }

	public static String getTagName(Section<? extends AbstractXMLType> s) {
		Map<String, String> attributeMapFor = getAttributeMapFor(s);
		if (attributeMapFor != null) {
			return attributeMapFor.get(AbstractXMLType.TAGNAME);
		}
		else {
			return null;
		}
	}

	public static Section<AbstractXMLType> getXMLFatherElement(Section<? extends AbstractXMLType> s) {
		Section<AbstractXMLType> xmlFather = Sections.findAncestorOfType(s, AbstractXMLType.class);
		return xmlFather;
	}

	public static int getXMLDepth(Section<AbstractXMLType> s) {

		return getXMLDepth(s, 0);
	}

	private static int getXMLDepth(Section<AbstractXMLType> s, int depth) {
		Section<AbstractXMLType> xmlFather = Sections.findAncestorOfType(s, AbstractXMLType.class);
		if (xmlFather != null && xmlFather.get() instanceof AbstractXMLType) {
			return getXMLDepth(xmlFather, ++depth);
		}
		else {
			return depth;
		}

	}

	public static Section<? extends AbstractXMLType> findSubSectionOfTag(
			String tagname, Section<? extends AbstractXMLType> s) {
		String tagName2 = getTagName(s);
		if (tagname.equals(tagName2)) {
			return s;
		}

		List<Section<? extends Type>> children = s.getChildren();
		for (Section<? extends Type> section : children) {
			if (section.get() instanceof XMLContent) {
				List<Section<AbstractXMLType>> nodes = Sections
						.findChildrenOfType(section, AbstractXMLType.class);
				for (Section<? extends AbstractXMLType> section2 : nodes) {
					Section<? extends AbstractXMLType> found = findSubSectionOfTag(
							tagname, section2);
					if (found != null) return found;
				}

			}
		}
		return null;

	}

	@SuppressWarnings("unchecked")
	public static void findSubSectionsOfTag(
			String tagname, Section<? extends AbstractXMLType> s, Collection<Section<? extends AbstractXMLType>> c) {
		String tagName2 = getTagName(s);
		if (tagname.equals(tagName2)) {

			c.add(s);
		}
		List<Section<? extends Type>> children = s.getChildren();
		for (Section<?> section : children) {
			if (section.get() instanceof XMLContent) {
				Section<XMLContent> conSec = (Section<XMLContent>) section;
				List<Section<AbstractXMLType>> nodes = Sections
						.findChildrenOfType(conSec, AbstractXMLType.class);
				for (Section<AbstractXMLType> section2 : nodes) {
					findSubSectionsOfTag(
							tagname, section2, c);

				}
			}
		}
	}

	/**
	 * ObjectType for XML-Sections with tag name <code>tagName</code>.
	 */
	public AbstractXMLType(String tagName) {
		this(tagName, false);
	}

	/**
	 * ObjectType for XML-Sections with no specific tag name. Finds all
	 * XML-Sections independent of their tag names.
	 */
	public AbstractXMLType() {
		this("AnyXMLObjectType", true);
	}

	private AbstractXMLType(String tagName, boolean anyXML) {
		this.xmlTagName = tagName;
		childrenTypes.add(0, new XMLHead());
		childrenTypes.add(1, new XMLTail());
		this.sectionFinder = new XMLSectionFinder(anyXML ? null : xmlTagName);
	}

	public String getXMLTagName() {
		return xmlTagName;
	}

	public void registerPackageDefinitionHandler() {
		this.addSubtreeHandler(Priority.PRECOMPILE_HIGH, new RegisterPackageDefinitionHandler());
	}

	static class RegisterPackageDefinitionHandler extends SubtreeHandler<AbstractXMLType> {

		public RegisterPackageDefinitionHandler() {
			super(true);
			this.registerConstraintModule(new IgnorePackageConstraint());
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<AbstractXMLType> s) {
			String value = getAttributeMapFor(s).get(KnowWEPackageManager.ATTRIBUTE_NAME);
			KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb()).addSectionToPackage(s, value);
			return null;
		}

		@Override
		public void destroy(KnowWEArticle article, Section<AbstractXMLType> s) {
			KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb()).removeSectionFromAllPackages(s);
		}

		private class IgnorePackageConstraint extends ConstraintModule<AbstractXMLType> {

			public IgnorePackageConstraint() {
				super(Operator.DONT_COMPILE_IF_VIOLATED, Purpose.CREATE_AND_DESTROY);
			}

			@Override
			public boolean violatedConstraints(KnowWEArticle article, Section<AbstractXMLType> s) {
				return s.get().isIgnoringPackageCompile();
			}

		}

	}

}
