package de.knowwe.ontology.sparql;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
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
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(context, section);
		if (core == null) {
			fail(context, HttpServletResponse.SC_NOT_FOUND,
					"The referenced section has no repository. " +
							"Maybe the page content is outdated. Please reload.");
		}
		String sparqlQuery = section.get().getSparqlQuery(section, context);
		String completeQuery = core.prependPrefixesToQuery(core.getNamespaces(), sparqlQuery);
		if (!core.getSparqlCache().remove(completeQuery)) {
			Log.warning("Cache for query " + Strings.ellipsis(sparqlQuery, 50) + " from page "
					+ section.getTitle() + " was not cleared as requested, because it could not be found...");
		}
	}
}
