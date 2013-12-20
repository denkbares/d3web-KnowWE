package de.knowwe.rdf2go.sparql;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
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
		String[] split = abbreviatedName.split(":");
		for (int i = 0; i < split.length; i++) {
			split[i] = Strings.decodeURL(split[i]);
		}
		Identifier identifier = new Identifier(split);
		String master = core.getMaster().getTitle();

		TerminologyManager manager = Environment.getInstance().getTerminologyManager(
				Environment.DEFAULT_WEB, master);
		Section<?> termDefiningSection = manager.getTermDefiningSection(identifier);

		if (termDefiningSection != null) {
			if (mode == RenderMode.HTML) {
				return KnowWEUtils.getLinkHTMLToArticle(termDefiningSection.getTitle(),
						Strings.concat(":", split));
			}
		}
		return text;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
