package de.knowwe.ontology.sparql;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Clears a sparql query from cache
 *
 * Created by Albrecht Striffler (denkbares GmbH) on 09.12.2014.
 */
public class ClearCachedSparqlAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<?> section = Sections.get(context.getParameter(Attributes.SECTION_ID));
		if (section == null || !(section.get() instanceof SparqlType)) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The referenced section was not found. " +
							"Maybe the page content is outdated. Please reload.");
		}
		Rdf2GoCore rdf2GoCore = Rdf2GoUtils.getRdf2GoCore(section);
		if (section == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The referenced section has no repository. " +
							"Maybe the page content is outdated. Please reload.");
		}
		Section<SparqlType> sparqlSection = Sections.cast(section, SparqlType.class);
		rdf2GoCore.clearSparqlResult(sparqlSection.get().getSparqlQuery(sparqlSection, context));
	}
}
