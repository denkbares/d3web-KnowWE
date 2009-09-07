package de.d3web.we.kdom.kopic.renderer;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class EditCoveringTableCellRenderer extends KnowWEDomRenderer {

	private static String[] options = { "  ", "ja", "! ", "--", "++", "1 ", "2 ", "3 ", "4 ", "5 ", "10" };
	
	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		String currentOp = sec.getOriginalText().trim();
		String secID = sec.getId();
		StringBuilder res = new StringBuilder();
		res.append("<select id=\"editCell"+secID+"\" onchange=\"cellChanged('"+secID+"','"+topic+"');\">");
		res.append("<option value=\"" + currentOp + "\">" + currentOp + "</option>");
		for (int i = 0; i < options.length; i++) {
			if (!options[i].equals(currentOp)) {
				res.append("<option value=\"" + options[i] + "\">" + options[i]
						+ "</option>");
			}
		}
		res.append("</select>");
		return KnowWEEnvironment.maskHTML(res.toString());
	}
	
	

}
