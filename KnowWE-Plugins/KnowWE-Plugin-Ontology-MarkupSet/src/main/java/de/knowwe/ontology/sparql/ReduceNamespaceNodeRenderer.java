package de.knowwe.ontology.sparql;

import org.openrdf.model.Value;

import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class ReduceNamespaceNodeRenderer implements SparqlResultNodeRenderer {

	@Override
	public String renderNode(Value node, String text, String variable, UserContext user, Rdf2GoCore core, RenderMode mode) {
		return Rdf2GoUtils.reduceNamespace(core, text);
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return true;
	}

}
