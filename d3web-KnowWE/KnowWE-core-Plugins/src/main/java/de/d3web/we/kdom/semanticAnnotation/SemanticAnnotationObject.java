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

/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.module.semantic.OwlGenerator;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.PropertyManager;
import de.d3web.we.module.semantic.owl.UpperOntology;

/**
 * @author kazamatzuri
 * 
 */
public class SemanticAnnotationObject extends DefaultAbstractKnowWEObjectType
		implements OwlGenerator {

	@Override
	public void init() {
		this.childrenTypes.add(new SemanticAnnotationProperty());
		this.childrenTypes.add(new SemanticAnnotationSubject());
		this.childrenTypes.add(new SimpleAnnotation());
		this.sectionFinder = new AllTextSectionFinder();
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	public IntermediateOwlObject getOwl(Section s) {
		UpperOntology uo = UpperOntology.getInstance();
		IntermediateOwlObject io = new IntermediateOwlObject();
		List<Section> childs = s.getChildren();
		URI prop = null;
		URI stringa = null;
		boolean erronousproperty = false;
		String badprop = "";
		for (Section cur : childs) {
			if (cur.getObjectType().getClass().equals(
					SemanticAnnotationProperty.class)) {
				IntermediateOwlObject tempio = ((SemanticAnnotationProperty) cur
						.getObjectType()).getOwl(cur);
				prop = tempio.getLiterals().get(0);
				erronousproperty = !tempio.getValidPropFlag();
				if (erronousproperty) {
					badprop = tempio.getBadAttribute();
				}
			} else if (cur.getObjectType().getClass().equals(
					SimpleAnnotation.class)) {
				IntermediateOwlObject tempio = ((SimpleAnnotation) cur
						.getObjectType()).getOwl(cur);
				if (tempio.getValidPropFlag()) {
					stringa = tempio.getLiterals().get(0);
					prop = tempio.getLiterals().get(0);
				} else {
					badprop = tempio.getBadAttribute();
				}
			}

		}

		boolean validprop = false;
		if (erronousproperty) {
			io.setBadAttribute(badprop);
			io.setValidPropFlag(false);			
		} else if (prop != null) {
			validprop = PropertyManager.getInstance().isValid(prop);
			io.setBadAttribute(prop.getLocalName());
		}

		io.setValidPropFlag(validprop);
		if (!validprop) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
					"invalid property: " + s.getOriginalText());
		}

		if (prop != null && validprop && stringa != null) {
			DefaultSubjectContext sol = (DefaultSubjectContext) ContextManager
					.getInstance().getContext(s, DefaultSubjectContext.CID);
			URI soluri = sol.getSolutionURI();
			Statement stmnt = null;
			try {
				if (PropertyManager.getInstance().isRDFS(prop)) {
					stmnt = uo.getHelper().createStatement(soluri, prop,
							stringa);
					io.addStatement(stmnt);
					io.merge(uo.getHelper().createStatementSrc(soluri, prop,
							stringa, s.getFather().getFather(),
							uo.getHelper().createURI("Annotation")));
				} else if (PropertyManager.getInstance().isRDF(prop)) {
					stmnt = uo.getHelper().createStatement(soluri, prop,
							stringa);
					io.addStatement(stmnt);
					io.merge(uo.getHelper().createStatementSrc(soluri, prop,
							stringa, s.getFather().getFather(),
							uo.getHelper().createURI("Annotation")));
				} else if (PropertyManager.getInstance().isNary(prop)) {
					IntermediateOwlObject tempio = UpperOntology.getInstance()
							.getHelper().createAnnotationProperty(soluri, prop,
									stringa, s.getFather().getFather());
					io.merge(tempio);

				} else {
					stmnt = uo.getHelper().createStatement(soluri, prop,
							stringa);
					io.addStatement(stmnt);
					io.merge(uo.getHelper().createStatementSrc(soluri, prop,
							stringa, s.getFather().getFather(),
							uo.getHelper().createURI("Annotation")));
				}

			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return io;
	}

}
