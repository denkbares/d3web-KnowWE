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

import java.util.ArrayList;
import java.util.Collection;
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
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.RoundBracedType;
import de.d3web.we.kdom.condition.AndOperator;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public class Disjunct extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new AllTextFinderTrimmed();
		this.childrenTypes.add(new RoundBracedType(this));
		this.childrenTypes.add(new AndOperator());
		this.childrenTypes.add(new Conjunct());
		this.addSubtreeHandler(new DisjunctSubTreeHandler());
	}

	private class DisjunctSubTreeHandler implements SubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> reviseSubtree(KnowWEArticle article, Section s) {
			
			List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
			IntermediateOwlObject io = new IntermediateOwlObject();
			try {
				UpperOntology uo = UpperOntology.getInstance();

				URI compositeexpression = uo.getHelper().createlocalURI(
						s.getTitle() + ".." + s.getId());
				io.addStatement(uo.getHelper().createStatement(
						compositeexpression, RDF.TYPE,
						D3WebOWLVokab.DISJUNCTION));
				io.addLiteral(compositeexpression);
				List<Section> children = s.getChildren();
				for (Section current : children) {
					if (current.getObjectType() instanceof Conjunct) {
						IntermediateOwlObject iohandler = (IntermediateOwlObject) KnowWEUtils
								.getStoredObject(current, OwlHelper.IOO);
						for (URI curi : iohandler.getLiterals()) {
							Statement state = uo.getHelper().createStatement(
									compositeexpression,
									D3WebOWLVokab.HASDISJUNCTS, curi);
							io.addStatement(state);
							iohandler.removeLiteral(curi);
						}
						io.merge(iohandler);
					}
				}
			} catch (RepositoryException e) {
				msgs.add(new SimpleMessageError(e.getMessage()));
			}
			KnowWEUtils.storeSectionInfo(s, OwlHelper.IOO, io);

			return msgs;
		}

	}

}
