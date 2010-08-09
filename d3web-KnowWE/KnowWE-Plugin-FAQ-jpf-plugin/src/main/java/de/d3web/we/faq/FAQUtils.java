package de.d3web.we.faq;

import de.d3web.we.utils.KnowWEUtils;

/**
 * Class for collecting some useful global helper functions
 * 
 * @author M. Freiberg
 * @created 09.08.2010
 */
public class FAQUtils {

	/**
	 * replaces the link-markup within a FAQ answer text "-L- -/L-" with correct
	 * HTML linking markup
	 * 
	 * @created 03.08.2010
	 * @param a the String potentially containing the link
	 * @return the correctly HTML-marked-up answer string
	 */
	public static String resolveLinks(String a) {
		if (a.isEmpty() || a.equals(" ") || !a.contains("[")) {
			return a;
		}
		String complete = "";
		String link = "";
		String linktext = "";

		while (a.contains("[")) {

			complete = a.substring(a.indexOf("[") + 1, a.indexOf("]"));
			if (a.contains("|")) {
				link = a.substring(a.indexOf("[") + 1, a.indexOf("|"));
				linktext = a.substring(a.indexOf("|") + 1, a.indexOf("]"));
			}
			else {
				link = complete;
				linktext = complete;
			}

			if (a.contains(".pdf")) {
				a = a.replace(complete, "<b><a href=\"/KnowWE/attach/FAQ%20Entry/" + link + "\">"
						+ linktext + "</a></b>");
			}
			else {
				a = a.replace(complete, "<b><a href=\"" + link + "\">" + linktext + "</a></b>");
			}
			a = a.replaceFirst("\\[", "");
			a = a.replaceFirst("\\]", "");
		}
		return a;
	}

	/**
	 * Generates the String representation of the content of one FAQ entry
	 * 
	 * @created 09.08.2010
	 * @param question
	 * @param answer
	 * @param status
	 * @param major
	 * @return
	 */
	public static String renderFAQPluginInner(String question, String answer, String status, String major) {
		StringBuilder string = new StringBuilder();

		string.append(KnowWEUtils.maskHTML("<div class='faq_question'> Q: "));
		string.append(KnowWEUtils.maskHTML(question));
		string.append(KnowWEUtils.maskHTML("</div>"));
		string.append(KnowWEUtils.maskHTML("<div class='faq_answer'> "));
		string.append(KnowWEUtils.maskHTML(answer));
		string.append(KnowWEUtils.maskHTML("<div class='faq_tags'> "));
		string.append(KnowWEUtils.maskHTML(status));
		string.append(KnowWEUtils.maskHTML(" "));
		string.append(KnowWEUtils.maskHTML(major));
		string.append(KnowWEUtils.maskHTML("</div>"));
		string.append(KnowWEUtils.maskHTML("</div>"));

		return string.toString();
	}

	/**
	 * Generates the frame for rendering the FAQ plugin within an own panel into
	 * the wiki site
	 * 
	 * @created 13.07.2010
	 * @return
	 */
	public static String renderFAQPluginFrame(String pluginInner) {
		StringBuilder string = new StringBuilder();
		string.append(KnowWEUtils.maskHTML("<div class='panel'>"));
		string.append(KnowWEUtils.maskHTML("<h3>FAQ Plugin</h3>"));
		string.append(KnowWEUtils.maskHTML(pluginInner));
		string.append(KnowWEUtils.maskHTML("</div>"));
		return string.toString();
	}
}
