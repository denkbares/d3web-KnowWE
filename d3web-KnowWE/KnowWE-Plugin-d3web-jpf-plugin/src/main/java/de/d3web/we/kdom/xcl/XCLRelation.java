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

package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.OwlSubtreeHandler;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.d3webModule.D3WebOWLVokab;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.Patterns;

public class XCLRelation extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.childrenTypes.add(new XCLRelationWeight());
		this.childrenTypes.add(new ComplexFinding());
		this.sectionFinder = new RegexSectionFinder(Patterns.XCRelation,
				Pattern.MULTILINE, 1);
		this.setCustomRenderer(XCLRelationKdomIdWrapperRenderer.getInstance());
		this.addSubtreeHandler(new XCLRelationOWLSubTreeHandler());
	}

	private class XCLRelationOWLSubTreeHandler extends OwlSubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
			IntermediateOwlObject io = new IntermediateOwlObject();
			try {
				UpperOntology uo = UpperOntology.getInstance();

				URI explainsdings = uo.getHelper().createlocalURI(
						s.getTitle() + ".." + s.getID());
				URI solutionuri = ((DefaultSubjectContext) ContextManager
						.getInstance().getContext(s, DefaultSubjectContext.CID))
						.getSolutionURI();
				io.addStatement(uo.getHelper().createStatement(solutionuri, D3WebOWLVokab.ISRATEDBY
						, explainsdings));
				uo.getHelper().attachTextOrigin(explainsdings, s, io);

				io.addStatement(uo.getHelper().createStatement(explainsdings,
						RDF.TYPE, D3WebOWLVokab.EXPLAINS));
				List<Section> children = s.getChildren();
				for (Section current : children) {
					if (current.getObjectType() instanceof ComplexFinding
							|| current.getObjectType() instanceof Finding) {
						IntermediateOwlObject tempio = (IntermediateOwlObject) KnowWEUtils.getStoredObject(
								current, OwlHelper.IOO);
						for (URI curi : tempio.getLiterals()) {
							Statement state = uo.getHelper().createStatement(
									explainsdings,
									D3WebOWLVokab.HASFINDING, curi);
							io.addStatement(state);
							tempio.removeLiteral(curi);
						}
						io.merge(tempio);
					}
					else if (current.getObjectType() instanceof XCLRelationWeight) {
						io.addStatement(uo.getHelper().createStatement(
								explainsdings,
								D3WebOWLVokab.HASWEIGHT,
								uo.getHelper().createLiteral(current.getOriginalText())));

					}

				}
			}
			catch (RepositoryException e) {
				msgs.add(new SimpleMessageError(e.getMessage()));
			}
			SemanticCoreDelegator.getInstance().addStatements(io, s);
			return msgs;

		}

	}
}
