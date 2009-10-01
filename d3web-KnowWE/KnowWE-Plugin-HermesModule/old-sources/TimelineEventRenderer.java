package de.d3web.we.hermes.timeline;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.javaEnv.KnowWEEnvironment;

// TODO: Auto-generated Javadoc
/**
 * The Class TimelineEventRenderer provides a possibility to render an TimelineEvent to html.
 */
public class TimelineEventRenderer {

	/**
	 * Render to html.
	 * 
	 * @param te the TimelineEvent to be rendered
	 * @param maskHTMLTags signals, if '>', '<' and '"' should be masked.
	 * 
	 * @return the string
	 */
	public static String renderToHTML (TimelineEvent te, boolean maskHTMLTags) {
		//return cached result, if possible
//		if (te.getRenderedOutput() != null) return te.getRenderedOutput();
		
		String colorTagStart = "";
		String colorTagEnd = "</font>";
		
		if (te.getRelevance() == 1) {
			colorTagStart = "<font color = \"red\">"; 
		} else if (te.getRelevance() == 2) {
			colorTagStart = "<font color = \"#FABE30\">";
		} else if (te.getRelevance() == 3) {
			colorTagStart = "<font color = \"green\">";
		}
		
		//if no renderedString is cached, render now
		StringBuffer sb = new StringBuffer("<div class=\"collapsebox-closed\">\n");
		sb.append("<h3>" + colorTagStart + "<i>" +  te.getTimeOutputString() + "</i> - " + te.getHeader() + colorTagEnd + 
		" <a class=\"wikipage\" href=\"Wiki.jsp?page=" + TimelineDatabase.getInstance().getTopicOfEvent(te) + "\"><img src=\"./images/goto.jpg\" alt=\"Zum Wiki\" title=\"Zum Wiki\"/></a>" +
		" <a class=\"wikipage\" href=\"Edit.jsp?page=" + TimelineDatabase.getInstance().getTopicOfEvent(te) + "\"><img src=\"./images/edit.gif\" alt=\"Bearbeiten\" title=\"Bearbeiten\"/></a>" +
				"</h3>\n");
		sb.append(te.getAbstractText() + "\n\n");
		ArrayList<String> sources = te.getSources();
		if (sources.size() == 1) {
			sb.append ("<b>Quelle:</b>");
		} else if (sources.size() > 1) {
			sb.append("<b>Quellen:</b> ");
		}
		for (String aSource : sources) {
			sb.append (aSource + "\n\n");
		}

		sb.append ("</div>\n");
		
		String result = sb.toString();
		
		if (maskHTMLTags) {
			result = result.replaceAll(">", KnowWEEnvironment.HTML_GT);
			result = result.replaceAll("<", KnowWEEnvironment.HTML_ST);
			result = result.replaceAll("\"", KnowWEEnvironment.HTML_QUOTE);
		}
		return result;
	}
	
	public static String renderToSimpleList (List<TimelineEvent> events) {		
		String s = "<ul>";
		for (TimelineEvent te : events) {
			s += "<li>" + te.getTimeOutputString() + ": " + te.getHeader() + "</li>\n";
		}
		
		s += "</ul>\n";
		return s;
	}
}
