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

package de.d3web.we.kdom.dashTree.propertyDefinition;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.RoundBracedType;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.DashTreeElementContent;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * @author Jochen
 * 
 * This DashTreeElementContent generates the property-definitions in OWL.
 * For spefification of range and domain restrictions it contains (in round brackets)
 * a PropertyDetails type in addition to the PropertyIDDefinition containing the name
 * of the property.
 *
 */
public class PropertyDashTreeElementContent extends DashTreeElementContent{

	@Override
	protected void init() {
		super.init();
		RoundBracedType e = new RoundBracedType(new PropertyDetails());
		e.setSteal(true);
		this.childrenTypes.add(e);
		this.childrenTypes.add(new PropertyIDDefinition());
		this.addSubtreeHandler(new PropertyDashTreeElementContentOWLSubTreeHandler());
	}

	private class PropertyDashTreeElementContentOWLSubTreeHandler implements SubtreeHandler
	{

		@Override
		public Collection<KDOMReportMessage> reviseSubtree(KnowWEArticle article, Section s) {
			
			List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
			
			Section<PropertyDashTreeElementContent> sec = s;
			if (s.getObjectType() instanceof PropertyDashTreeElementContent) {
				Section<PropertyIDDefinition> propIDSection = sec.findSuccessor(PropertyIDDefinition.class);
				if (propIDSection != null) {
					String propertyName = propIDSection.getOriginalText();
					String rangeDef = null;
					String domainDef = null;
					Section<DomainDefinition> domainDefS = sec.findSuccessor(DomainDefinition.class);
					if (domainDefS != null) {
						domainDef = domainDefS.getOriginalText();
					}

					Section<RangeDefinition> rangeDefS = sec.findSuccessor(RangeDefinition.class);
					if (rangeDefS != null) {
						rangeDef = rangeDefS.getOriginalText();
					}

					UpperOntology uo = UpperOntology.getInstance();
					IntermediateOwlObject io = new IntermediateOwlObject();

					OwlHelper helper = uo.getHelper();
					URI propURI = helper.createlocalURI(propertyName.trim());
					try {
						
						// creates an Object-Property (in any case)
						io.addStatement(helper.createStatement(propURI, RDF.TYPE,
								OWL.OBJECTPROPERTY));
						
						
						// creates a Subproperty relation IF father exists
						Section<? extends DashTreeElement> fatherElement = DashTreeElement.getDashTreeFather((Section<DashTreeElement>)sec.getFather());
						if(fatherElement != null) {
							Section<PropertyIDDefinition> fatherID  = fatherElement.findSuccessor(PropertyIDDefinition.class);
							if(fatherID != null) {
								io.addStatement(helper.createStatement(
										propURI, RDFS.SUBPROPERTYOF, helper
												.createlocalURI(fatherID.getOriginalText()
														.trim())));
							}
						}
						
						//creates Domain restriction if defined
						if (domainDef != null) {
							String[] classes = domainDef.split(",");
							for (String string : classes) {
								if (string.trim().length() > 0) {
									io.addStatement(helper.createStatement(
													propURI, RDFS.DOMAIN, helper
															.createlocalURI(string
																	.trim())));
								}
							}
						}

						//creates Range restriction if defined
						if (rangeDef != null) {
							String[] classes = rangeDef.split(",");
							for (String string : classes) {
								if (string.trim().length() > 0) {
									io.addStatement(helper.createStatement(
													propURI, RDFS.RANGE, helper
															.createlocalURI(string
																	.trim())));
								}
							}
						}
						SemanticCore.getInstance().addStatements(io, s);						
					} catch (RepositoryException e) {						
						msgs.add(new SimpleMessageError(e.getMessage()));
					}

				}
			}
			return msgs;
		}
		
	}
	


}
