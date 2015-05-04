package de.knowwe.ontology.sparql;

import java.util.Collection;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.Rdf2GoCompiler;
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
		TerminologyManager manager = null;
		Collection<Rdf2GoCompiler> compilers = Compilers.getCompilers(
				KnowWEUtils.getArticleManager(user.getWeb()), Rdf2GoCompiler.class);
		for (Rdf2GoCompiler rdf2GoCompiler : compilers) {
			if (rdf2GoCompiler.getRdf2GoCore() == core) {
				manager = rdf2GoCompiler.getTerminologyManager();
			}
		}
		if (manager != null) {
			Section<?> termDefiningSection = manager.getTermDefiningSection(identifier);
			if (termDefiningSection != null) {
				if (mode == RenderMode.HTML) {
					return KnowWEUtils.getLinkHTMLToArticle(termDefiningSection.getTitle(),
							Strings.concat(":", split));
				}
			}
		}
		return text;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
