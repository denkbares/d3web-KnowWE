package de.d3web.we.hermes.kdom.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.semantic.DefaultURIContext;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlSubtreeHandler;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.hermes.TimeEvent;
import de.d3web.we.hermes.TimeStamp;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.report.KDOMReportMessage;

public class TimeEventOWLCompiler extends OwlSubtreeHandler<TimeEventNew> {

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TimeEventNew> s) {

		TimeEvent event = TimeEventNew.getEvent(s);
		if (s.hasErrorInSubtree(article) || event == null) {
			return new ArrayList<KDOMReportMessage>(0);
		}
		UpperOntology uo = UpperOntology.getInstance();
		IntermediateOwlObject io = new IntermediateOwlObject();
		try {


			ArrayList<Statement> slist = new ArrayList<Statement>();

			/* creating all the URIs for the resources */
			String localID = s.getTitle() + "_" + s.getID();
			URI localURI = uo.getHelper().createlocalURI(localID);

			URI timeEventURI = uo.getHelper().createlocalURI("Ereignis");

			// Putting the TimeEventURI in a context, so it can be found by
			// subtypes
			DefaultURIContext uc = new DefaultURIContext();
			uc.setSubjectURI(localURI);
			ContextManager.getInstance().attachContext(s, uc);

			DefaultSubjectContext sc = new DefaultSubjectContext(localID);
			ContextManager.getInstance().attachContext(s, sc);

			// handle date infos
			createDateTripels(uo, event.getTime(), slist, localURI);

			// add textorigin
			uo.getHelper().attachTextOrigin(localURI, s, io);

			// handle description
			String description = event.getDescription();
			if (description != null) {
				Literal descriptionURI = uo.getHelper().createLiteral(
						description);
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasDescription"),
						descriptionURI));
			}

			// handle title
			String title = event.getTitle();
			Literal titleURI = uo.getHelper().createLiteral(title);
			slist.add(uo.getHelper().createStatement(localURI,
					uo.getHelper().createlocalURI("hasTitle"), titleURI));

			// handle importance
			Integer importance = event.getImportance();
			if (importance != null) {
				Literal importanceURI = uo.getVf().createLiteral(importance);
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasImportance"),
						importanceURI));
			}

			// handle sources
			List<String> sourceStrings = event.getSources();
			List<Literal> sourceURIs = new ArrayList<Literal>();
			for (String source : sourceStrings) {
				sourceURIs.add(uo.getVf().createLiteral(source));
			}
			for (Literal sURI : sourceURIs) {
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasSource"), sURI));
			}

			io.addStatement(uo.getHelper().createStatement(localURI,
					RDF.TYPE, timeEventURI));
			io.addAllStatements(slist);

		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}

		SemanticCoreDelegator.getInstance().addStatements(io, s);
		return new ArrayList<KDOMReportMessage>(0);
	}

	private void createDateTripels(UpperOntology uo, TimeStamp timeStamp, ArrayList<Statement> slist, URI localURI) throws RepositoryException {
		if (timeStamp != null) {
			Literal dateText = uo.getVf().createLiteral(timeStamp.toString());

			Literal dateStart = uo.getVf().createLiteral(
					timeStamp.getStartPoint()
							.getInterpretableTime());

			Literal dateEnd = null;
			if (timeStamp.getEndPoint() != null) {

				dateEnd = uo.getVf().createLiteral(
						timeStamp.getEndPoint()
								.getInterpretableTime());
			}

			slist.add(uo.getHelper().createStatement(localURI,
					uo.getHelper().createlocalURI("hasStartDate"),
					dateStart));
			if (dateEnd != null) {
				slist.add(uo.getHelper().createStatement(localURI,
						uo.getHelper().createlocalURI("hasEndDate"),
						dateEnd));
			}
			slist.add(uo.getHelper().createStatement(localURI,
					uo.getHelper().createlocalURI("hasDateDescription"),
					dateText));
		}
	}

}
