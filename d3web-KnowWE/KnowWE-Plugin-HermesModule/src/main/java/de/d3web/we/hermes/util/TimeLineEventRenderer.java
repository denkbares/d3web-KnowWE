package de.d3web.we.hermes.util;

import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.hermes.TimeEvent;

public class TimeLineEventRenderer {

/**
		 * Render to html.
		 * 
		 * @param te the TimelineEvent to be rendered
		 * @param maskHTMLTags signals, if '>', '<' and '"' should be masked.
		 * 
		 * @return the string
		 */
	public static String renderToHTML(TimeEvent te, boolean maskHTMLTags) {
		// return cached result, if possible
		// if (te.getRenderedOutput() != null) return te.getRenderedOutput();

		String colorTagStart = "";
		String colorTagEnd = "</font>";

		if (te.getImportance() == 1) {
			colorTagStart = "<font color = \"red\">";
		} else if (te.getImportance() == 2) {
			colorTagStart = "<font color = \"#FABE30\">";
		} else if (te.getImportance() == 3) {
			colorTagStart = "<font color = \"green\">";
		}

		// if no renderedString is cached, render now
		StringBuffer sb = new StringBuffer(
				"<div class=\"collapsebox\">\n");
			
		sb
				.append("<h4 id=\"section-Main-<spanStyle'colorRgb23523520'>Anl_C3_A4sseF_C3_BCrDenAusbruchDesPeloponnesischenKrieges<Span>435rv\" class=\"collapsetitle\"><div class=\"collapseClose\" title=\"Click to expand\">+</div>"
						+ colorTagStart
						+ "<i>"
						+ te.getTime().getDescription()
						+ "</i> - "
						+ te.getTitle()
						+ colorTagEnd
						+ " <a class=\"wikipage\" href=\"Wiki.jsp?page="
						+ "todo_topic"
						+ "\"><img src=\"./images/goto.jpg\" alt=\"Zum Wiki\" title=\"Zum Wiki\"/></a>"
						+ " <a class=\"wikipage\" href=\"Edit.jsp?page="
						+ "todo_topic"
						+ "\"><img src=\"./images/edit.gif\" alt=\"Bearbeiten\" title=\"Bearbeiten\"/></a>"
						+ "</h4>");
		
		
		sb.append("\n"+te.getDescription() + "<br>");
		List<String> sources = te.getSources();
		if (sources != null) {
			if (sources.size() == 1) {
				sb.append("<b>Quelle:</b><br>");
			} else if (sources.size() > 1) {
				sb.append("<b>Quellen:</b><br> ");
			}
			for (String aSource : sources) {
				sb.append(aSource + "<br>");
			}
		}
		
		sb.append("<br>textOrigin:"+te.getTopic()+ "<br>");
		sb.append("<br>textOrigin:"+te.getTextOriginNode() + "<br>");

		sb.append("</div>\n");

		String result = sb.toString();

		if (maskHTMLTags) {
			result = result.replaceAll(">", KnowWEEnvironment.HTML_GT);
			result = result.replaceAll("<", KnowWEEnvironment.HTML_ST);
			result = result.replaceAll("\"", KnowWEEnvironment.HTML_QUOTE);
		}
		return result;
	}

	public static String renderToSimpleList(List<TimeEvent> events) {
		String s = "<ul>";
		for (TimeEvent te : events) {
			s += "<li>" + te.getTime().getDescription() + ": " + te.getTitle()
					+ "</li>\n";
		}

		s += "</ul>\n";
		return s;
	}
}
