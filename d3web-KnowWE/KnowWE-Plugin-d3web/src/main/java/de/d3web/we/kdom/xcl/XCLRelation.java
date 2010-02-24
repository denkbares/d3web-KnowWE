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

import java.util.List;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.module.semantic.OwlGenerator;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;
import de.d3web.we.utils.Patterns;

public class XCLRelation extends DefaultAbstractKnowWEObjectType implements
		OwlGenerator {

	@Override
	protected void init() {
		this.childrenTypes.add(new XCLRelationWeight());
		this.childrenTypes.add(new ComplexFinding());
		this.sectionFinder = new RegexSectionFinder(Patterns.XCRelation,
				Pattern.MULTILINE, 1);
		this.setCustomRenderer(XCLRelationKdomIdWrapperRenderer.getInstance());
	}

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

			URI explainsdings = uo.getHelper().createlocalURI(
					s.getTitle() + ".." + s.getId());
			URI solutionuri = ((DefaultSubjectContext) ContextManager
					.getInstance().getContext(s, DefaultSubjectContext.CID))
					.getSolutionURI();
			io.addStatement(uo.getHelper().createStatement(solutionuri,
					uo.getHelper().createURI("isRatedBy"), explainsdings));
			uo.getHelper().attachTextOrigin(explainsdings, s, io);

			io.addStatement(uo.getHelper().createStatement(explainsdings,
					RDF.TYPE, uo.getHelper().createURI("Explains")));
			List<Section> children = s.getChildren();
			for (Section current : children) {
				if (current.getObjectType() instanceof ComplexFinding
						|| current.getObjectType() instanceof Finding) {
					OwlGenerator handler = (OwlGenerator) current
							.getObjectType();
					for (URI curi : handler.getOwl(current).getLiterals()) {
						Statement state = uo.getHelper().createStatement(
								explainsdings,
								uo.getHelper().createURI("hasFinding"), curi);
						io.addStatement(state);
						handler.getOwl(current).removeLiteral(curi);
					}
					io.merge(handler.getOwl(current));
				} else if (current.getObjectType() instanceof XCLRelationWeight) {
					XCLRelationWeight handler = (XCLRelationWeight) current
							.getObjectType();

					io.addStatement(uo.getHelper().createStatement(
							explainsdings,
							uo.getHelper().createURI("hasWeight"),
							uo.getHelper().createLiteral(current.getOriginalText())));					

				}

			}
		} catch (RepositoryException e) {
			// TODO error management?
		}
		return io;
	}
}
