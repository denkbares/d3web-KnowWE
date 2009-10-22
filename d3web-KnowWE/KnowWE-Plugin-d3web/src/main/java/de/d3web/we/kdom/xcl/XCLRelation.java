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

package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class XCLRelation extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.childrenTypes.add(new XCLRelationLineEnd());
		this.childrenTypes.add(new XCLRelationWhiteSpaces());
		this.childrenTypes.add(new XCLRelationWeight());
		this.childrenTypes.add(new ComplexFinding());
		this.sectionFinder = new XCLRelationSectionFinder();
		this.setCustomRenderer(XCLRelationKdomIdWrapperRenderer.getInstance());
	}
	
	
	public static List<String> splitUnquoted(String conditionText2,
			String operatorGreaterEqual) {
		boolean quoted = false;
		List<String> parts = new ArrayList<String>();
		StringBuffer actualPart = new StringBuffer();
		for (int i = 0; i < conditionText2.length(); i++) {

			if (conditionText2.charAt(i) == '"') {
				quoted = !quoted;
			}
			if (quoted) {
				actualPart.append(conditionText2.charAt(i));
				continue;
			}
			if ((i + operatorGreaterEqual.length() <= conditionText2.length())
					&& conditionText2.subSequence(
							i, i + operatorGreaterEqual.length()).
							equals(operatorGreaterEqual)) {
				parts.add(actualPart.toString().trim());
				actualPart = new StringBuffer();
				i += operatorGreaterEqual.length() - 1;
				continue;
			}
			actualPart.append(conditionText2.charAt(i));

		}
		parts.add(actualPart.toString().trim());
		return parts;
	}


	public class XCLRelationSectionFinder extends SectionFinder {
		
		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			Pattern relPattern = Pattern.compile(", *\\r?\\n");
			Matcher m = relPattern.matcher(text);
			int start = 0;
			int end;
			while (m.find()) {
				end = m.end();
				if (containsData(text.substring(start, end)))
					result.add(new SectionFinderResult(start, end));
				start = end;
			}
			return result;
		}
		
		private boolean containsData(String string) {
		int index = 0;
		while (index < string.length()) {
			char charAt = string.charAt(index);
			if (charAt != ' ' && charAt != '\n' && charAt != '\r'
					&& charAt != '}') {
				return true;
			}
			index++;
		}

		return false;
	}
		
		
		
//		public XCLRelationSectionFinder(KnowWEObjectType type) {
//			super(type);
//		}
//
//		@Override
//		public List<Section> lookForSections(Section tmp, Section father) {
//			String text = tmp.getOriginalText();
//			List<Section> result = new ArrayList<Section>();
//			List<String> lines = splitUnquoted(text, ",");
//			for (String string : lines) {
//				if (containsData(string)) {
//					int indexOf = text.indexOf(string);
//					result.add(Section.createSection(this.getType(), father,
//							tmp, indexOf, indexOf + string.length(), father.getArticle()));
//				}
//			}
//
//			return result;
//		}
//
//		private boolean containsData(String string) {
//			int index = 0;
//			while (index < string.length()) {
//				char charAt = string.charAt(index);
//				if (charAt != ' ' && charAt != '\n' && charAt != '\r'
//						&& charAt != '}') {
//					return true;
//				}
//				index++;
//			}
//
//			return false;
//		}
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.d3web.we.dom.AbstractOWLKnowWEObjectType#getOwl(de.d3web.we.dom.Section
	 * )
	 */
	public IntermediateOwlObject getOwl(Section s) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		try {
			UpperOntology uo = UpperOntology.getInstance();

			URI explainsdings = uo.getHelper().createlocalURI(s.getTitle() + ".."
					+ s.getId());
			URI solutionuri = ((SolutionContext) ContextManager.getInstance()
					.getContext(s, SolutionContext.CID)).getSolutionURI();
			io.addStatement(uo.getHelper().createStatement(solutionuri, uo
				.getHelper().createURI("isRatedBy"), explainsdings));
			uo.getHelper().attachTextOrigin(explainsdings, s, io);

			io.addStatement(uo.getHelper().createStatement(explainsdings, RDF.TYPE, uo
				.getHelper().createURI("Explains")));
			for (Section current : s.getChildren()) {
				if (current.getObjectType() instanceof ComplexFinding||current.getObjectType() instanceof Finding) {
					AbstractKnowWEObjectType handler = (AbstractKnowWEObjectType) current
							.getObjectType();
					for (URI curi : handler.getOwl(current).getLiterals()) {
						Statement state = uo.getHelper().createStatement(explainsdings, uo
							.getHelper().createURI("hasFinding"), curi);
						io.addStatement(state);
						handler.getOwl(current).removeLiteral(curi);
					}
					io.merge(handler.getOwl(current));
				} else if (current.getObjectType() instanceof XCLRelationWeight) {
					AbstractKnowWEObjectType handler = (AbstractKnowWEObjectType) current
							.getObjectType();
					if (handler.getOwl(current).getLiterals().size() > 0) {
						io.addStatement(uo.getHelper().createStatement(explainsdings, uo
							.getHelper().createURI("hasWeight"), handler
								.getOwl(current).getLiterals().get(0)));
						io.addAllStatements(handler.getOwl(current)
								.getAllStatements());
					}
				}

			}
		} catch (RepositoryException e) {
			// TODO error management?
		}
		return io;
	}

}
