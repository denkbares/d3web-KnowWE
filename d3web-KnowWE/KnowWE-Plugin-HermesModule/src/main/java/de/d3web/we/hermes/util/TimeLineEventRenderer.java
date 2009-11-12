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

	String styleTag = "";

	if (te.getImportance() == 1) {
	    styleTag = " style=\"color: red; ";
	} else if (te.getImportance() == 2) {
	    styleTag = " style=\"color: #FABE30; ";
	} else if (te.getImportance() == 3) {
	    styleTag = " style=\"color: green; ";
	}

	styleTag += " background-color: transparent;\"";

	// if no renderedString is cached, render now
	StringBuffer sb = new StringBuffer("<div class='panel'>\n");

	sb
		.append("<h3 "
			+ styleTag
			+ "><i>"
			+ te.getTime().getDescription()
			+ "</i> - "
			+ te.getTitle()
			+ " <a class=\"wikipage\" href=\"Wiki.jsp?page="
			+ te.getTopic()
			+ "\"><img src=\"./images/goto.jpg\" alt=\"Zum Wiki\" title=\"Zum Wiki\"/></a>"
			+ " <a class=\"wikipage\" href=\"Edit.jsp?page="
			+ te.getTopic()
			+ "\"><img src=\"./images/edit.gif\" alt=\"Bearbeiten\" title=\"Bearbeiten\"/></a>"
			+ "</h3>");

	sb.append("\n<div>" + te.getDescription() + "<br>");
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
	//
	// sb.append("<br>textOrigin:" + te.getTopic() + "<br>");
	// sb.append("<br>textOrigin:" + te.getTextOriginNode() + "<br>");

	sb.append("</div>\n</div>\n");

	String result = sb.toString();

	if (maskHTMLTags) {
	    result = result.replaceAll(">", KnowWEEnvironment.HTML_GT);
	    result = result.replaceAll("<", KnowWEEnvironment.HTML_ST);
	    result = result.replaceAll("\"", KnowWEEnvironment.HTML_QUOTE);
	}
	return result;
    }
}