package de.knowwe.ontology.sparql;

import org.openrdf.model.Value;

import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.Rdf2GoCore;

public class ShrinkNodeRenderer implements SparqlResultNodeRenderer {

	private final int maxLength = 75;

	@Override
	public String renderNode(Value node, String text, String variable, UserContext user, Rdf2GoCore core, RenderMode mode) {
		if ((text.length() > maxLength) && (mode == RenderMode.HTML)) {
			String titleText = text.replaceAll("\"", "&#34;");
			titleText = text.replaceAll("'", "&#39;");
			RenderResult result = new RenderResult(user);
			result.appendHtml("<span title='" + titleText + "'>");
			result.append(text.substring(0, maxLength - 3) + "...");
			result.appendHtml("</span>");
			return result.toStringRaw();
		}
		return text;
	}

	@Override
	public boolean allowFollowUpRenderer() {
		return false;
	}

}
