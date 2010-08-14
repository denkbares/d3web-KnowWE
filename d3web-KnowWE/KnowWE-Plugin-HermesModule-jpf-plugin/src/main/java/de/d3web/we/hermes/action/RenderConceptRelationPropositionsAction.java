package de.d3web.we.hermes.action;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;

public class RenderConceptRelationPropositionsAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		String[] rels = {
				"bla", "blubb", "concept mismatch", "dont ask agein" };

		StringBuffer buffy = new StringBuffer();

		buffy.append("<div class=\"semContents\" >");
		buffy.append("<div class=\"questionsheet-layer\">");

		for (String string : rels) {

			String rqst = "KnowWE.jsp" + "?action=setRelation&articleName="
					+ java.net.URLEncoder.encode(parameterMap.getTopic())
					+ "&ObjectID=" + "conceptName" + "&ValueID="
					+ string;

			buffy.append("<INPUT TYPE='radio' NAME='f" + "timestampid" + "id"
					+ string + "' " + "value='"
					+ string + "'" + "id='semanooc"
					+ string + "' " + "rel=\"{url: '" + rqst
					+ "'}\" ");

			buffy.append("class='semano_oc'");
			buffy.append(">" + string + "<br />");

		}

		buffy.append("</div>");
		buffy.append("</div>");
		return buffy.toString();
	}

}
