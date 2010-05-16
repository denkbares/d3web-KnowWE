package de.d3web.we.wisec.event;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.event.Event;
import de.d3web.we.event.EventListener;
import de.d3web.we.event.FindingSetEvent;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class WISECFindingSetEventListener implements EventListener<FindingSetEvent> {

	@Override
	public Class<? extends Event> getEvent() {
		return FindingSetEvent.class;
	}

	@Override
	public void notify(String username, Section<? extends KnowWEObjectType> s,
			FindingSetEvent event) {
		
		event.getQuestion();
		event.getValue();
		
		// TODO: Build the SPARQL Query
		String queryString = "";
		
		// TODO: Send the Query to the Semantic Core {@Link SparqlDelegateRenderer}
		SemanticCore sc = SemanticCore.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
		
		try {
			Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO: Do something with the result (Set NumValues in the session)
	
	}

}
