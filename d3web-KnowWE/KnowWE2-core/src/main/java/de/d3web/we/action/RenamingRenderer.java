/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.RenameFinding;
import de.d3web.we.kdom.Section;

public class RenamingRenderer implements KnowWEAction {

	private static ResourceBundle rb;

	private boolean caseSensitive;
	
	public final static String TXT_SEPERATOR = ":";

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		rb = KnowWEEnvironment.getInstance().getKwikiBundle(parameterMap.getRequest());
		
		String queryString = parameterMap.get(KnowWEAttributes.TARGET);
		String queryContextPrevious = parameterMap.get(KnowWEAttributes.CONTEXT_PREVIOUS);
		String queryContextAfter = parameterMap.get(KnowWEAttributes.CONTEXT_AFTER);
		String atmUrl = parameterMap.get(KnowWEAttributes.ATM_URL);

		setCaseSensitive(Boolean.parseBoolean( parameterMap.get(KnowWEAttributes.CASE_SENSITIVE )));

		String queryContext = "";
		queryContextAfter = (queryContextAfter != "") ? " " + queryContextAfter : "";
		queryContextPrevious = (queryContextPrevious != "") ? queryContextPrevious + " " : "";

		queryContext = queryContextPrevious + queryString + queryContextAfter;

		String replacement = parameterMap.get(KnowWEAttributes.FOCUSED_TERM);
		String web = parameterMap.getWeb();

		if (web == null) {
			web = KnowWEEnvironment.DEFAULT_WEB;
		}

		// handle show additional text
		if (atmUrl != null) {
			return getAdditionalMatchText(atmUrl, web, queryString);
		}

		Map<KnowWEArticle, Collection<RenameFinding>> findings = scanForFindings(web, queryContext, queryContextPrevious.length());

		return renderFindingsSelectionMask(findings, queryString, replacement);
	}

	/**
	 * <p>
	 * Returns an additional text passage around the search result. So the user
	 * can view the result in a larger context.
	 * </p>
	 * 
	 * @param amtURL
	 *            additional text parameters
	 * @param web
	 *            KnowWEEnvironment
	 * @param query
	 *            user query string
	 * @return additional text area
	 */
	private String getAdditionalMatchText(String atmURL, String web, String query) {

		// article#section#position#curChars#direction
		// e.g. Swimming#0#264#20#a
		String[] params = atmURL.split( RenamingRenderer.TXT_SEPERATOR );
		String articleTitle = params[0];
		int sectionNum = Integer.parseInt(params[1]);
		int pos = Integer.parseInt(params[2]);
		int chars = Integer.parseInt(params[3]);
		String direction = params[4];

		String additionalText = "";

		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		Iterator<KnowWEArticle> iter = mgr.getArticleIterator();
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();

			if (article.getTitle().equals(articleTitle)) {
				Section section = article.getSection().getChildren().get(sectionNum);
				String context = RenameFinding.getAdditionalContext(pos, direction, chars, query.length(), section.getOriginalText());
				String span = createAdditionalMatchingTextSpan(article, sectionNum, pos, chars + RenameFinding.CONTEXT_SIZE_SMALL, direction.charAt(0), false);
				
				if( direction.charAt(0) == 'a')
				{
					additionalText = context + span;
				}
				else
				{
				    additionalText = span + context;
				}
			}
		}
		return additionalText;
	}

	/**
	 * <p>
	 * Scans all articles for the query expressions. If the expressions is found
	 * a <code>RenameFinding</code> object is created.
	 * </p>
	 * 
	 * @param web
	 * @param query
	 *            any string the user is looking for
	 * @return a map containing all findings of the string <code>query<code>
	 */
	public Map<KnowWEArticle, Collection<RenameFinding>> scanForFindings(
			String web, String query, int previousMatchLength) {
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		Map<KnowWEArticle, Collection<RenameFinding>> map = new HashMap<KnowWEArticle, Collection<RenameFinding>>();
		Iterator<KnowWEArticle> iter = mgr.getArticleIterator();
		
		Pattern p;
		
		if (getCaseSensitive()) {
			p = Pattern.compile(query);
		} else {
			p = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
		}
		
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			map.put(article, new HashSet<RenameFinding>());
			String text = article.getSection().getOriginalText();

			Matcher m = p.matcher(text);
			while (m.find()) {
				int start = m.start() + previousMatchLength; 
				int end = start + query.length();
				
				Section sec = article.findSmallestNodeContaining(start, end);
				int startInSec = start - sec.getAbsolutePositionStartInArticle();

				RenameFinding f = new RenameFinding(startInSec, RenameFinding.getContext(startInSec,sec, text, query.length()), sec);
				map.get(article).add(f);
			}
		}
		return map;
	}

	/**
	 * <p>
	 * Renders a table with the results of the search in it.
	 * </p>
	 * 
	 * @param findings
	 *            a map with all found text passages in it
	 * @param query
	 *            the users query string
	 * @param replacement
	 *            the replacement string for the query string
	 * @return a HTML formatted table witch list all the findings in it
	 */
	private String renderFindingsSelectionMask(
			Map<KnowWEArticle, Collection<RenameFinding>> findings,
			String query, String replacement) {
		StringBuffer mask = new StringBuffer();

		mask.append("<form method='post' action=''><fieldset><legend>"
				+ rb.getString("KnowWE.renamingtool.searchresult")
				+ " '" + query + "'</legend>");
		mask.append("<table id='sortable1'><colgroup><col class='match' /><col class='section' />");
		mask.append("<col class='replace' /><col class='preview' /></colgroup>");
		mask.append("<thead><tr><th scope='col'>"
				+ rb.getString("KnowWE.renamingtool.clmn.match")
				+ "</th><th scope='col'>"
				+ rb.getString("KnowWE.renamingtool.clmn.section")
				+ "</th>");
		mask.append("<th scope='col'>"
				+ rb.getString("KnowWE.renamingtool.clmn.replace")
				+ "</th><th scope='col'>"
				+ rb.getString("KnowWE.renamingtool.clmn.preview")
				+ "</th></tr></thead>");

		for (Entry<KnowWEArticle, Collection<RenameFinding>> entry : findings.entrySet()) {

			KnowWEArticle article = entry.getKey();
			Collection<RenameFinding> findingsInArticle = entry.getValue();
			if (findingsInArticle.size() > 0) {
				mask.append("<thead>");
				mask.append("<tr><td>");
				mask.append("<strong>"
						+ rb.getString("KnowWE.renamingtool.article")
						+ ": " + article.getTitle() + "</strong>");
				mask.append("</td><td></td><td>");
				mask.append("<input id='check-select' class='check' onclick='selectPerSection(this, \""
								+ article.getTitle()
								+ "\");' value='' type='button'  title='Select all checkboxes'/>");
				mask.append("<input id='check-deselect' class='check' onclick='deselectPerSection(this, \""
								+ article.getTitle()
								+ "\");' value='' type='button' title='Deselect all checkboxes'/>"
								+ "</td><td></td></tr>");
				mask.append("</thead>");
			}
			mask.append("<tbody>");
			for (RenameFinding renameFinding : findingsInArticle) {

				String text = renameFinding.contextText();
				text = highlightQueryResult(text, query);

				String checkBoxID = "replaceBox_"
						+ article.getTitle()
						+ TXT_SEPERATOR
						+ renameFinding.getSec().getId() + TXT_SEPERATOR
						+ renameFinding.getStart();

				mask.append("<tr>");
				mask.append("<td>"
						+ createAdditionalMatchingTextSpan(article, article
								.getSection().getChildren().indexOf(
										renameFinding.getSec()), renameFinding
								.getStart(), RenameFinding.CONTEXT_SIZE_SMALL,
								'p', true));
				mask.append(" " + text + " ");
				mask.append(createAdditionalMatchingTextSpan(article, article
						.getSection().getChildren().indexOf(
								renameFinding.getSec()), renameFinding
						.getStart(), RenameFinding.CONTEXT_SIZE_SMALL, 'a',
						true));
				mask.append("</td>");
				mask.append("<td><i>"
						+ renameFinding.getSec().getObjectType().getName()
						+ "</i></td>");
				mask.append("<td><input type='checkbox' id='" + checkBoxID
						+ "'></td>");
				mask.append("<td>" + replacePreview(text, query, replacement)
						+ "</td>");
				mask.append("</tr>");
			}
			mask.append("</tbody>");
		}
		mask.append("<tfoot>");
		mask.append("<tr><td></td><td></td>");
		mask
				.append("<td><input onclick='replaceAll();' value='"
						+ rb.getString("KnowWE.renamingtool.bttn.replace")
						+ "' type='button' class='button' title='Begriff in ausgewï¿½hlten Stellen ersetzen'/></td>");
		mask.append("<td></td></tr>");
		mask.append("</tfoot>");
		mask.append("</table></fieldset></form>");

		return mask.toString();
	}

	private String verbalizeModul(String type) {
		int i = type.lastIndexOf(".");
		String modName = type;
		if (i >= 0) {
			modName = type.substring(i + 1);
		}
		String name = null;
		try {
			name = rb.getString("KnowWE.sectionfinder." + modName);

		} catch (Exception e) {
		}
		if (name != null)
			return name;

		return type;
	}

	/**
	 * <p>
	 * Searches the query string and highlights it. Works case-sensitive.
	 * </p>
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	private String highlightQueryResult(String text, String query) {
		// highlight search result due case-sensitivity
		StringTokenizer tokenizer = new StringTokenizer(text,
				"; .,\n\r[](){}?!/|:'<>", true);
		StringBuilder result = new StringBuilder();
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();

			if (token.toLowerCase().contains(query.toLowerCase())) {

				Pattern p;
				if (getCaseSensitive()) {
					p = Pattern.compile(query);
				} else {
					p = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
				}
				Matcher m = p.matcher(token);

				while (m.find()) {
					result.append(token.substring(0, m.start()) + "<strong>"
							+ token.substring(m.start(), m.end()) + "</strong>"
							+ token.substring(m.end(), token.length()));
				}
			} else {
				result.append(token);
			}
		}
		return result.toString();
	}

	/**
	 * <p>
	 * Replaces the query string with the given replacement. Works
	 * case-sensitive.
	 * </p>
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	private String replacePreview(String text, String query, String replacement) {
		StringTokenizer tokenizer = new StringTokenizer(text,
				"; .,\n\r[](){}?!/|:'<>", true);
		StringBuilder result = new StringBuilder();

		if (replacement == null)
			replacement = "";

		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (query.equalsIgnoreCase(token)) {
				result.append(replacement);
			} else {
				result.append(token);
			}
		}
		return result.toString();
	}

	// article, position, sectionNum, chars, direction
	private String createAdditionalMatchingTextSpan(KnowWEArticle article,
			int section, int start, int chars, char direction, boolean span) {

		StringBuilder html = new StringBuilder();

		String arrowLeft = "KnowWEExtension/images/arrow_left.png";
		String arrowRight = "KnowWEExtension/images/arrow_right.png";

		String img;

		switch (direction) {
		case 'a':
			img = arrowRight;
			if (chars > 100) {
				img = arrowLeft;
				chars = 0;
			}
			break;
		default:
			img = arrowLeft;
			if (chars > 100) {
				img = arrowRight;
				chars = 0;
			}
			break;
		}

		// create atmUrl (e.g. schwimming#0#264#20#-1)
		String atmUrl = article.getTitle() + RenamingRenderer.TXT_SEPERATOR 
		        + section + RenamingRenderer.TXT_SEPERATOR 
		        + start + RenamingRenderer.TXT_SEPERATOR
				+ chars + RenamingRenderer.TXT_SEPERATOR
				+ direction;

		if (span) {
			html.append("<span id='" + direction + start
					+ "' class='short' style='display: inline;'>");
		}

		html.append("<a href='javascript:getAdditionalMatchText(\"" + atmUrl
				+ "\")'>");
		html.append("<img width='12' height='12' border='0' src='" + img
				+ "' alt='more'/>");
		html.append("</a>");

		if (span) {
			html.append("</span>");
		}

		return html.toString();
	}

	public boolean getCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
}
