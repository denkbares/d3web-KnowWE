package de.knowwe.rdf2go;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.ontoware.rdf2go.model.Statement;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;

public class RDF2GoStatementCacheViewer extends AbstractHTMLTagHandler {

	public RDF2GoStatementCacheViewer() {
		super("statementcache");
	}

	@Override
	public void renderHTML(String web, String topic, UserContext user, Map<String, String> parameters, RenderResult result) {
		Map<String, WeakHashMap<Section<? extends Type>, List<Statement>>> statementCache = Rdf2GoCore.getInstance().getStatementCache();

		for (String string : statementCache.keySet()) {
			result.append("\"" + string + "\":");
			result.appendHtml("<br>");
			WeakHashMap<Section<? extends Type>, List<Statement>> weakHashMap = statementCache.get(string);
			for (Section<?> sec : weakHashMap.keySet()) {
				result.append(sec.getID());
				result.append(":");
				for (Statement statement : weakHashMap.get(sec)) {
					result.append(statement.toString());
				}
			}
			result.appendHtml("<br>");

		}

	}

}
