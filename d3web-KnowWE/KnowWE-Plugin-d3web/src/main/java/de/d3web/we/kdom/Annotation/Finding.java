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

package de.d3web.we.kdom.Annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.QuotedType;
import de.d3web.we.kdom.condition.NOT;
import de.d3web.we.kdom.filter.TypeSectionFilter;
import de.d3web.we.kdom.renderer.FontColorBackgroundRenderer;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class Finding extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.childrenTypes.add(new NOT());
		this.childrenTypes.add(new FindingComparator());
		this.childrenTypes.add(new QuotedType(new FindingQuestion()));
		this.childrenTypes.add(new FindingQuestion());
		this.childrenTypes.add(new QuotedType(new FindingAnswer()));
		this.childrenTypes.add(new FindingAnswer());
		this.sectionFinder = new FindingSectionFinder();
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR3);
	}

	/**
	 * Only for XclRelation Highlighting.
	 */
	public KnowWEDomRenderer getBackgroundColorRenderer(String color) {
		return FontColorBackgroundRenderer.getRenderer(
				FontColorRenderer.COLOR5, color);
	}

	@Override
	public IntermediateOwlObject getOwl(Section section) {
		UpperOntology uo = UpperOntology.getInstance();
		IntermediateOwlObject io = new IntermediateOwlObject();
		try {
			Section csection = (Section) section.getChildren(
					new TypeSectionFilter(new FindingComparator().getName()))
					.get(0);
			String comparator = ((FindingComparator) csection.getObjectType())
					.getComparator(csection);

			Section qsection = section.findSuccessor(FindingQuestion.class);
			String question = ((FindingQuestion) qsection.getObjectType())
					.getQuestion(qsection);

			Section asection = section.findSuccessor(FindingAnswer.class);
			String answer = ((FindingAnswer) asection.getObjectType())
					.getAnswer(asection);

			URI compuri = uo.getHelper().getComparator(comparator);
			URI questionuri = uo.getHelper().createlocalURI(question);
			URI answeruri = uo.getHelper().createlocalURI(answer);
			URI literalinstance = uo.getHelper().createlocalURI(
					section.getTitle() + ".." + section.getId() + ".."
							+ question + comparator + answer);

			ArrayList<Statement> slist = new ArrayList<Statement>();
			try {
				slist.add(uo.getHelper().createStatement(literalinstance,
						RDF.TYPE, uo.getHelper().createURI("Literal")));
				slist.add(uo.getHelper().createStatement(literalinstance,
						uo.getHelper().createURI("hasInput"), questionuri));
				slist.add(uo.getHelper().createStatement(literalinstance,
						uo.getHelper().createURI("hasComparator"), compuri));
				slist.add(uo.getHelper().createStatement(literalinstance,
						uo.getHelper().createURI("hasValue"), answeruri));
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
			io.addAllStatements(slist);
			io.addLiteral(literalinstance);
		} catch (IndexOutOfBoundsException e) {
			Logger.getLogger(this.getName()).log(Level.WARNING,
					"Finding without subsections");
		}

		return io;
	}

	public class FindingSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			if (text.contains(">") || text.contains("=") || text.contains("<")) {
				if (!text.contains("+=")) {  // hack excluding "+="
					return AllTextFinderTrimmed.getInstance().lookForSections(text,
							father);
				}
			}
			return null;
		}

	}

}
