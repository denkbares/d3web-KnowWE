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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

/**
 * Renders all actions for the Renaming Tool.
 * 
 * @author Johannes Dienst
 *
 */
public class WordBasedRenamingAction extends AbstractKnowWEAction {

	private ResourceBundle rb;

	private boolean caseSensitive;
	
	public final static String TXT_SEPERATOR = ":";
	
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		rb = KnowWEEnvironment.getInstance().getKwikiBundle(parameterMap.getRequest());
		// get the selected sections from the section selection tree
		String[] sections = null;
		if (parameterMap.containsKey("SelectedSections")) {
			String s = parameterMap.get("SelectedSections");
			Gson gson = new Gson();
			sections = gson.fromJson(s, String[].class);
		}

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

		Map<KnowWEArticle, Collection<WordBasedRenameFinding>> findings = 
							scanForFindings(web, queryContext, queryContextPrevious.length(), sections);

		return renderFindingsSelectionMask(findings, queryString, replacement);
	}

	/**
	 * Returns an additional text passage around
	 * the search result. So the user
	 * can view the result in a larger context.
	 * 
	 * @param amtURL additional text parameters
	 * @param web 
	 * @param query user query string
	 * @return additional text area
	 */
	private String getAdditionalMatchText(String atmURL, String web, String query) {

		// article#section#position#curWords#direction
		String[] params = atmURL.split( WordBasedRenamingAction.TXT_SEPERATOR );
		String articleTitle = params[0];
		String sectionID = params[1];
		int pos = Integer.parseInt(params[2]);
		int curWords = Integer.parseInt(params[3]);
		String direction = params[4];

		String additionalText = "";

		Iterator<KnowWEArticle> iter = 
			KnowWEEnvironment.getInstance().getArticleManager(web).getArticleIterator();
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();

			if (article.getTitle().equals(articleTitle)) {
				Section section = article.getSection().findChild(sectionID);
				String context = WordBasedRenameFinding.getAdditionalContext(
									pos, direction, curWords, query.length(),
									section.getOriginalText());
				
				// No more Words to display
				if (context == null) {
					context = WordBasedRenameFinding.getAdditionalContext(
							pos, direction, curWords-1, query.length(),
							section.getOriginalText());;
					curWords = WordBasedRenameFinding.MAX_WORDS + 1;					
				}
					
				String span = createAdditionalMatchingTextSpan(article, pos,
							sectionID, curWords+1, direction.charAt(0), false);
				
				if( direction.charAt(0) == 'a') {
					additionalText = context + span;
				} else {
				    additionalText = span + context;
				}
			}
		}
		return additionalText;
	}
	
	/**
	 * Renders a table with the results of the search in it.
	 * 
	 * @param findings a map with all found text passages in it
	 * @param query the users query string
	 * @param replacement the replacement string for the query string
	 * @return a HTML formatted table witch list all the findings in it
	 */
	private String renderFindingsSelectionMask(
			Map<KnowWEArticle, Collection<WordBasedRenameFinding>> findings,
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
				+ "</th></tr></thead>"
				
				+ "<thead>"
				+ "<tr><td></td><td></td><td>"
				+ "<input class='check-select check' value='' type='button'  title='Select all checkboxes' rel='{section: undefined}'/>"
				+ "<input class='check-deselect check' value='' type='button' title='Deselect all checkboxes' rel='{section: undefined}'/>"
				+ "</td><td></td></tr>"
				+ "</thead>"

				+ "");

		for (Entry<KnowWEArticle, Collection<WordBasedRenameFinding>> entry : findings.entrySet()) {

			KnowWEArticle article = entry.getKey();
			Collection<WordBasedRenameFinding> findingsInArticle = entry.getValue();
			if (findingsInArticle.size() > 0) {
				mask.append("<thead>");
				mask.append("<tr><td>");
				mask.append("<strong>"
						+ rb.getString("KnowWE.renamingtool.article")
						+ ": " + article.getTitle() + "</strong>");
				mask.append("</td><td></td><td>");
				
				mask.append("<input class='check-select check' value='' type='button'  title='Select all checkboxes' rel='{section: \""+article.getTitle()+"\"}'/>");
				mask.append("<input class='check-deselect check' value='' type='button' title='Deselect all checkboxes' rel='{section: \""+article.getTitle()+"\"}'/>");
/*				mask.append("<input id='check-select' class='check' onclick='selectPerSection(this, \""
								+ article.getTitle()
								+ "\");' value='' type='button'  title='Select all checkboxes'/>");
				mask.append("<input id='check-deselect' class='check' onclick='deselectPerSection(this, \""
								+ article.getTitle()
								+ "\");' value='' type='button' title='Deselect all checkboxes'/>"
								+ "</td><td></td></tr>"); */
				mask.append("</thead>");
			}
			mask.append("<tbody>");
			
			for (WordBasedRenameFinding WordBasedRenameFinding : findingsInArticle) {

				String text = WordBasedRenameFinding.contextText();
				text = highlightQueryResult(text, query);

				String checkBoxID = "replaceBox_"
						+ article.getTitle()
						+ TXT_SEPERATOR
						+ WordBasedRenameFinding.getSec().getId() + TXT_SEPERATOR
						+ WordBasedRenameFinding.getStart();
				
				mask.append("<tr>");
				
				// TODO indexOf only searches the 1. children
				// and does not consider the section to be
				// deeper in the tree!!!
				mask.append("<td>"
						+ createAdditionalMatchingTextSpan(article,
								WordBasedRenameFinding.getStart(),
								WordBasedRenameFinding.getSec().getId(),
								0,
								'p', true));
				mask.append(" " + text + " ");
				mask.append(createAdditionalMatchingTextSpan(article,
								WordBasedRenameFinding.getStart(),
								WordBasedRenameFinding.getSec().getId(),
								0,
								'a', true));
				mask.append("</td>");
				mask.append("<td><i>"
						+ WordBasedRenameFinding.getSec().getObjectType().getName()
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
		mask.append("<td><input id='renaming-replace' value='"
				+ rb.getString("KnowWE.renamingtool.bttn.replace")
				+ "' type='button' class='button'"
				+ " title='Begriff in ausgewählten Stellen ersetzen'/></td>");
		/*mask.append("<td><input onclick='replaceAll();' value='"
						+ rb.getString("KnowWE.renamingtool.bttn.replace")
						+ "' type='button' class='button'"
						+ " title='Begriff in ausgewï¿½hlten Stellen ersetzen'/></td>");*/
		mask.append("<td></td></tr>");
		mask.append("</tfoot>");
		mask.append("</table></fieldset></form>");

		return mask.toString();
	}
	
	/**
	 * Scans all articles for the query expressions. If the expressions is found
	 * a <code>RenameFinding</code> object is created.
	 * 
	 * @param web
	 * @param query any string the user is looking for
	 * @param previousMatchLength
	 * @param sections just add findings to the map which have a corresponding section type - if null, all findings are added  
	 * @return a map containing all findings of the string <code>query<code>
	 */
	public Map<KnowWEArticle, Collection<WordBasedRenameFinding>> scanForFindings(
			String web, String query, int previousMatchLength, String[] sections) {
		Set<String> sectionSet=null;
		if (sections!=null){
			 sectionSet= new HashSet<String>(Arrays.asList(sections));
		}
		
		Map<KnowWEArticle, Collection<WordBasedRenameFinding>> map = 
						new HashMap<KnowWEArticle, Collection<WordBasedRenameFinding>>();
		Iterator<KnowWEArticle> iter = 
						KnowWEEnvironment.getInstance().getArticleManager(web).getArticleIterator();
		
		Pattern p;
		if (getCaseSensitive()) {
			p = Pattern.compile(Pattern.quote(query));
		} else {
			p = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
		}
		
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			map.put(article, new HashSet<WordBasedRenameFinding>());
			String text = article.getSection().getOriginalText();

			Matcher m = p.matcher(text);
			while (m.find()) {
				int start = m.start() + previousMatchLength; 
				int end = start + query.length();
				
				Section sec = article.findSmallestNodeContaining(start, end);
				int startInSec = start - sec.getAbsolutePositionStartInArticle();

				WordBasedRenameFinding f = 
					new WordBasedRenameFinding(startInSec, startInSec+query.length(),
							WordBasedRenameFinding.
								getContext(startInSec,sec, text, query.length()), sec);
				if (sections == null || sectionSet.contains(sec.getObjectType().getName())) {
					map.get(article).add(f);
				}
			}
		}
		return map;
	}
	
	/**
	 * Creates the buttons to navigate through the context.
	 * 
	 * @param article 			the article containing section
	 * @param section 			where the query was found
	 * @param sectionIndex 		position of the section in the article
	 * @param curWords 			currently amount of words displayed
	 * @param direction 		[p]revious [a]fter
	 * @param span
	 * @return
	 */
	private String createAdditionalMatchingTextSpan(KnowWEArticle article,
			int sectionIndex, String sectionId, int curWords, char direction, boolean span) {

		StringBuilder html = new StringBuilder();

		String arrowLeft = "KnowWEExtension/images/arrow_left.png";
		String arrowRight = "KnowWEExtension/images/arrow_right.png";

		String img;

		switch (direction) {
		case 'a':
			img = arrowRight;
			if (curWords > WordBasedRenameFinding.MAX_WORDS) {
				img = arrowLeft;
				curWords = 0;
			}
			break;
		default:
			img = arrowLeft;
			if (curWords > WordBasedRenameFinding.MAX_WORDS) {
				img = arrowRight;
				curWords = 0;
			}
			break;
		}

		// create atmUrl
		String atmUrl = "{article: '"+ article.getTitle()+"'," 
				        + "section: '"+sectionId+"'," 
				        + "index: " + sectionIndex + ", "
				        + "words: " + curWords + ", "
				        + "direction: '"+direction+"'}";		

		if (span) {
			html.append("<span id='" + direction + sectionIndex
					+ "' class='short' style='display: inline;'>");
		}

//		html.append("<a href='javascript:getAdditionalMatchText(\"" + atmUrl
//				+ "\")'>");
		html.append("<img width=\"12\" height=\"12\" border=\"0\" src=\"" + img
				+ "\" alt=\"more\" rel=\""+ atmUrl+"\" class=\"show-additional-text-renaming\"/>");
//		html.append("</a>");

		if (span) {
			html.append("</span>");
		}

		return html.toString();
	}
	
	/**
	 * Searches the query string and highlights it. Works case-sensitive.
	 * 
	 * @param text
	 * @param query
	 * @return
	 */
	private String highlightQueryResult(String text, String query) {
		
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
	 * Replaces the query string with the given replacement. Works
	 * case-sensitive.
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
	
	public boolean getCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
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
}
