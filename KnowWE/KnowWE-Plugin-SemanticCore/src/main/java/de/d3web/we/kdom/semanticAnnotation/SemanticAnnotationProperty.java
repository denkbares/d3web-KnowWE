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

/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.URI;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.OwlSubtreeHandler;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author kazamatzuri
 * 
 */
public class SemanticAnnotationProperty extends DefaultAbstractKnowWEObjectType {

	public SemanticAnnotationProperty() {
		this.sectionFinder = new AnnotationPropertySectionFinder();
		this.childrenTypes.add(new SemanticAnnotationPropertyDelimiter());
		this.childrenTypes.add(new SemanticAnnotationPropertyName());
		this.addSubtreeHandler(new SemanticAnnotationPropertySubTreeHandler());
	}

	public static class AnnotationPropertySectionFinder implements ISectionFinder {

		private final String PATTERN = "[(\\w:)?\\w]*::";

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, KnowWEObjectType type) {
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			Pattern p = Pattern.compile(PATTERN);
			Matcher m = p.matcher(text);
			while (m.find()) {
				result.add(new SectionFinderResult(m.start(), m.end()));
			}
			return result;
		}

	}

	private class SemanticAnnotationPropertySubTreeHandler extends
			OwlSubtreeHandler<SemanticAnnotationProperty> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<SemanticAnnotationProperty> s) {

			Section<SemanticAnnotationPropertyName> name = s.findChildOfType(SemanticAnnotationPropertyName.class);

			IntermediateOwlObject io = new IntermediateOwlObject();
			UpperOntology uo = UpperOntology.getInstance();
			String prop = name.getOriginalText();
			URI property = null;
			if (prop.equals("subClassOf") || prop.equals("subPropertyOf")) {
				property = uo.getRDFS(prop);
			}
			else if (prop.equals("type")) {
				property = uo.getRDF(prop);
			}
			else if (prop.contains(":")) {
				String ns = SemanticCoreDelegator.getInstance().getNameSpaces().get(
						prop.split(":")[0]);
				if (ns == null || ns.length() == 0) {
					io.setBadAttribute("no namespace given");
					io.setValidPropFlag(false);
				}
				else if (ns.equals(prop.split(":")[0])) {
					io.setBadAttribute(ns);
					io.setValidPropFlag(false);
				}
				else {
					property = uo.getHelper().createURI(ns, prop.split(":")[1]);
				}
			}
			else {
				property = uo.getHelper().createlocalURI(prop);
			}
			io.addLiteral(property);
			KnowWEUtils.storeObject(article, s, OwlHelper.IOO, io);
			return null;
		}

	}

}
