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

package de.d3web.we.hermes.kdom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.hermes.TimeStamp;
import de.d3web.we.hermes.kdom.renderer.TimeEventTypeRenderer;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationEndSymbol;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationStartSymbol;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class TimeEventType extends DefaultAbstractKnowWEObjectType {

	public static final String START_TAG = "<<";
	public static final String END_TAG = ">>";

	@Override
	protected void init() {
		sectionFinder = new RegexSectionFinder(START_TAG + "[\\w|\\W]*?"
				+ END_TAG);
		this.childrenTypes.add(new SemanticAnnotationStartSymbol("<<"));
		this.childrenTypes.add(new SemanticAnnotationEndSymbol(">>"));
		this.childrenTypes.add(new TimeEventTitleType());
		this.childrenTypes.add(new TimeEventImportanceType());
		this.childrenTypes.add(new TimeEventDateType());
		this.childrenTypes.add(new TimeEventSourceType());
		this.childrenTypes.add(new TimeEventDescriptionType());

		this.setCustomRenderer(new EditSectionRenderer(
				TimeEventTypeRenderer.getInstance()));
		this.addSubtreeHandler(Priority.HIGH, new TimeEventTypeOWLSubTreeHandler());

	}

	private class TimeEventTypeOWLSubTreeHandler extends
			SubtreeHandler<TimeEventType> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<TimeEventType> section) {

			UpperOntology uo = UpperOntology.getInstance();
			IntermediateOwlObject io = new IntermediateOwlObject();
			try {

				Section<TimeEventType> sec = (Section<TimeEventType>) section;

				/* Getting all the sections from KDOM */
				Section<? extends TimeEventDescriptionType> descriptionSection = sec.findChildOfType(TimeEventDescriptionType.class);
				Section<? extends TimeEventTitleType> titleSection = sec.findChildOfType(TimeEventTitleType.class);
				Section<? extends TimeEventImportanceType> importanceSection = sec.findChildOfType(TimeEventImportanceType.class);
				Section<? extends TimeEventDateType> dateSection = sec.findChildOfType(TimeEventDateType.class);

				List<Section<TimeEventSourceType>> sources = new ArrayList<Section<TimeEventSourceType>>();
				sec.findSuccessorsOfType(TimeEventSourceType.class,
								sources);

				if (descriptionSection == null) {
					return Arrays.asList((KDOMReportMessage) new SimpleMessageError("descriptionSection was null"));
				}
				if (importanceSection == null) {
					return Arrays.asList((KDOMReportMessage) new SimpleMessageError("importanceSection was null"));
				}
				if (dateSection == null) {
					return Arrays.asList((KDOMReportMessage) new SimpleMessageError("dateSection was null"));
				}

				/* Getting all the strings from the sections */
				String description = descriptionSection.getOriginalText();
				String title = titleSection.getOriginalText();
				String importance = importanceSection.getOriginalText();
				String date = dateSection.getOriginalText();
				List<String> sourceStrings = new ArrayList<String>();
				for (Section<TimeEventSourceType> s : sources) {
					sourceStrings.add(s.getOriginalText());
				}

				/* creating all the URIs for the resources */
				String localID = section.getTitle() + "_" + section.getId();
				URI localURI = uo.getHelper().createlocalURI(localID);

				URI timeEventURI = uo.getHelper().createlocalURI("TimeEvent");

				// Putting the TimeEventURI in a context, so it can be found by
				// subtypes
				// NOTE currently revise of OWL is bottom up => context are set
				// too late

				DefaultSubjectContext sc = new DefaultSubjectContext();
				sc.setSubjectURI(localURI);
				ContextManager.getInstance().attachContext(section, sc);

				Literal descriptionURI = uo.getHelper().createLiteral(
						description);
				Literal titleURI = uo.getHelper().createLiteral(title);
				Literal importanceURI = uo.getVf().createLiteral(importance);

				// Literal dateURI = uo.getHelper().createLiteral(date);
				TimeStamp timeStringInterpreter = new TimeStamp(date.trim());

				Literal dateStartURI = uo.getVf().createLiteral(
						timeStringInterpreter.getStartPoint()
								.getInterpretableTime());

				Literal dateEndURI = null;
				if (timeStringInterpreter.getEndPoint() != null) {

					dateEndURI = uo.getVf().createLiteral(
							timeStringInterpreter.getEndPoint()
									.getInterpretableTime());
				}
				Literal dateTextURI = uo.getVf().createLiteral(date.trim());

				List<Literal> sourceURIs = new ArrayList<Literal>();
				for (String source : sourceStrings) {
					sourceURIs.add(uo.getVf().createLiteral(source));
				}

				uo.getHelper().attachTextOrigin(localURI, section, io);

				/* adding all OWL statements to io object */
				io.addStatement(uo.getHelper().createStatement(localURI,
						RDF.TYPE, timeEventURI));

				ArrayList<Statement> slist = new ArrayList<Statement>();
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasDescription"),
						descriptionURI));
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasTitle"), titleURI));
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasImportance"),
						importanceURI));
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasStartDate"),
						dateStartURI));
				if (dateEndURI != null) {
					slist.add(uo.getHelper().createStatement(localURI,
							uo.getHelper().createlocalURI("hasEndDate"),
							dateEndURI));
				}
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasDateDescription"),
						dateTextURI));
				for (Literal sURI : sourceURIs) {
					slist.add(uo.getHelper().createStatement(localURI,
							uo.getHelper().createlocalURI("hasSource"), sURI));
				}

				io.addAllStatements(slist);

			}
			catch (RepositoryException e) {
				e.printStackTrace();
			}

			SemanticCore.getInstance().addStatements(io, section);
			return new ArrayList<KDOMReportMessage>(0);
		}

	}

}
