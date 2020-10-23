package de.knowwe.ontology.sparql;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.rdf4j.model.Value;

import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;

public class DecodeUrlNodeRenderer implements SparqlResultNodeRenderer {

	@Override
	public String renderNode(Value node, String text, String variable, UserContext user, Rdf2GoCore core, RenderOptions.RenderMode mode) {
		try {
			return URLDecoder.decode(text, "UTF-8");
		}
		catch (Exception e) {
			return text;
		}
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return true;
	}
}
