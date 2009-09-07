package de.d3web.we.kdom.semanticAnnotation;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class XCLComparatorEditorRenderer extends KnowWEDomRenderer {

	private static String[] comps = { "=", "<=", ">=", ">", "<" };

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		String currentOp = sec.getOriginalText().trim();
		StringBuilder res = new StringBuilder("<select id=\"codeCompletion\">");
		res.append("<option value=\"" + currentOp + "\">" + currentOp + "</option>");
		for (int i = 0; i < comps.length; i++) {
			if (!comps[i].equals(currentOp)) {
				res.append("<option value=\"" + comps[i] + "\">" + comps[i]
						+ "</option>");
			}
		}
		res.append("</select>");

		return KnowWEEnvironment.maskHTML(res.toString());
	}

}
