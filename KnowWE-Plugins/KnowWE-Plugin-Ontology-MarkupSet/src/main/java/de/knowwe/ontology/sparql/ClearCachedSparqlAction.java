package de.knowwe.ontology.sparql;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Clears a sparql query from cache
 * <p>
 * Created by Albrecht Striffler (denkbares GmbH) on 09.12.2014.
 */
public class ClearCachedSparqlAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<SparqlType> section = getSection(context, SparqlType.class);
		Rdf2GoCore rdf2GoCore = Rdf2GoUtils.getRdf2GoCore(section);
		if (rdf2GoCore == null) {
			fail(context, HttpServletResponse.SC_NOT_FOUND,
					"The referenced section has no repository. " +
							"Maybe the page content is outdated. Please reload.");
		}
		rdf2GoCore.getSparqlCache().remove(section.get().getSparqlQuery(section, context));
	}
}
