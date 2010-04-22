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

package de.d3web.we.kdom.condition.old;

import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.d3webModule.D3WebOWLVokab;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.basic.RoundBracedType;
import de.d3web.we.kdom.condition.CondKnownType;
import de.d3web.we.kdom.kopic.rules.ruleActionLine.SolutionValueAssignment;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.utils.KnowWEUtils;

@Deprecated
public class Conjunct extends DefaultAbstractKnowWEObjectType  {

	@Override
	public void init() {
		this.childrenTypes.add(new RoundBracedType(this));
		this.childrenTypes.add(new SolutionValueAssignment());
		this.childrenTypes.add(new CondKnownType());
		this.childrenTypes.add(new Finding());
		this.sectionFinder = new AllTextFinderTrimmed();
		this.addReviseSubtreeHandler(new ConjunctSubTreeHandler());
	}

	private class ConjunctSubTreeHandler implements ReviseSubTreeHandler{

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
			KDOMReportMessage msg=null;
			IntermediateOwlObject io = new IntermediateOwlObject();
			try {
				UpperOntology uo = UpperOntology.getInstance();
				URI compositeexpression = uo.getHelper().createlocalURI(
						s.getTitle() + ".." + s.getId());
				io.addStatement(uo.getHelper().createStatement(compositeexpression,
						RDF.TYPE, D3WebOWLVokab.CONJUNCTION));
				io.addLiteral(compositeexpression);
				List<Section> children = s.getChildren();
				for (Section current : children) {
					if (current.getObjectType() instanceof Finding) {
						IntermediateOwlObject iohandler =(IntermediateOwlObject) KnowWEUtils.getStoredObject(current,OwlHelper.IOO);
						for (URI curi : iohandler.getLiterals()) {
							Statement state = uo.getHelper().createStatement(
									compositeexpression,D3WebOWLVokab.HASCONJUNCTS
									, curi);
							io.addStatement(state);
							iohandler.removeLiteral(curi);
						}
						io.merge(iohandler);
					}
				}
			} catch (RepositoryException e) {
				// TODO error management?
			}
			KnowWEUtils.storeSectionInfo(s, OwlHelper.IOO, io);
			return msg;
		}

	}


}
