package de.knowwe.rdf2go.sparql;

import de.d3web.strings.Identifier;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class TermDefinitionLinkNodeRenderer implements SparqlResultNodeRenderer {

	@Override
	public String renderNode(String text, String variable, UserContext user, Rdf2GoCore core, RenderMode mode) {
		String abbreviatedName = Rdf2GoUtils.reduceNamespace(core, text);
		Identifier identifier = new Identifier(abbreviatedName.split(":"));

		String master = Rdf2GoCore.getMaster(core, Environment.DEFAULT_WEB);
		
		TerminologyManager manager = Environment.getInstance().getTerminologyManager(
				Environment.DEFAULT_WEB, master);
		Section<?> termDefiningSection = manager.getTermDefiningSection(identifier);

		if (termDefiningSection != null) {
			if (mode == RenderMode.HTML) {
				return KnowWEUtils.getLinkHTMLToArticle(termDefiningSection.getTitle(),
						abbreviatedName);
			}
		}
		return text;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
