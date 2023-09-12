package de.knowwe.ontology.sparql;

import java.util.Collection;

import org.eclipse.rdf4j.model.Value;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions.RenderMode;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.tools.ToolMenuDecoratingRenderer;
import de.knowwe.tools.ToolUtils;

public class TermDefinitionLinkNodeRenderer implements SparqlResultNodeRenderer {

	@Override
	public String renderNode(Value node, String text, String variable, UserContext user, Rdf2GoCore core, RenderMode mode) {
		String termName = Rdf2GoUtils.reduceNamespace(core, text);
		String[] split = termName.split(":");
		for (int i = 0; i < split.length; i++) {
			split[i] = Strings.decodeURL(split[i]);
		}
		Identifier identifier = new Identifier(split);

		String rendered = renderDefinitionLink(identifier, termName, user, core, mode);
		return rendered == null ? text : rendered;
	}

	protected String renderDefinitionLink(Identifier identifier, String termName, UserContext user, Rdf2GoCore core, RenderMode mode) {
		TerminologyManager manager = getTerminologyManager(user, core);
		if (manager == null) return null;
		Section<?> termDefiningSection = manager.getTermDefiningSection(identifier);
		if (termDefiningSection == null) return null;
		if (mode == RenderMode.HTML) {
			return "<a href='" + KnowWEUtils.getURLLink(termDefiningSection) + "'>" + termName + "</a>";
		}
		if (mode == RenderMode.ToolMenu) {
			RenderResult renderResult = new RenderResult(user);
			ToolMenuDecoratingRenderer.renderToolMenuDecorator(termName, termDefiningSection.getID(), ToolUtils.hasToolInstances(termDefiningSection, user), renderResult);
			return renderResult.toString();
		}
		return null;
	}

	@Nullable
	protected TerminologyManager getTerminologyManager(UserContext user, Rdf2GoCore core) {
		TerminologyManager manager = null;
		Collection<Rdf2GoCompiler> compilers = Compilers.getCompilers(user,
				KnowWEUtils.getArticleManager(user.getWeb()), Rdf2GoCompiler.class);
		for (Rdf2GoCompiler rdf2GoCompiler : compilers) {
			if (rdf2GoCompiler.getRdf2GoCore() == core) {
				manager = rdf2GoCompiler.getTerminologyManager();
			}
		}
		return manager;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}
}
