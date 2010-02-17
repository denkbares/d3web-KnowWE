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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * Renders the Mask for the findings of
 * an KnowWEObjectType in the running Wiki.
 * See also KnowWEObjectTypeBrowserHandler.
 * 
 * @author Johannes Dienst
 *
 */
public class KnowWEObjectTypeBrowserAction extends AbstractKnowWEAction {

	private ResourceBundle rb;
	
	@Override
	public String perform(KnowWEParameterMap map) {
		
		rb = KnowWEEnvironment.getInstance().getKwikiBundle(map.getRequest());
		
		String atmUrl = map.get(KnowWEAttributes.ATM_URL);
		String query = map.get(KnowWEAttributes.TYPE_BROWSER_QUERY);
		// handle show additional text
		if (atmUrl != null) {
			String web = map.getWeb();

			if (web == null) {
				web = KnowWEEnvironment.DEFAULT_WEB;
			}
			int queryLength = Integer.valueOf(query);
			return getAdditionalMatchText(map.get(KnowWEAttributes.ATM_URL), web, queryLength);
		}

		// Build the Findings
		StringBuilder buildi = new StringBuilder();

		// Get all searched Types from the parameterMap
		String types = map.get("TypeBrowserParams");
		ArrayList<Section> found = new ArrayList<Section>();

		// Get all instances of a Type over KnowWEEnvironment
		List<KnowWEObjectType> typs = null;
		try {
			typs = KnowWEEnvironment.getInstance().searchTypeInstances(Class.forName(types));

			if (!typs.isEmpty()) {
				Iterator<KnowWEArticle> it = KnowWEEnvironment.getInstance().getArticleManager(
						map.getWeb()).getArticleIterator();
				while (it.hasNext()) {
					KnowWEArticle art = it.next();
					art.getSection().findSuccessorsOfTypeUntyped(Class.forName(types),
							found);
				}
			}

		} catch (ClassNotFoundException e) {
			Logger.getLogger(this.getClass().getName()).warning(
			"Searched Type not Found!");
		}

		// if found is empty display error message
		if (found.isEmpty()) {
			buildi.append("<p class='error box'>"
							+ rb.getString("KnowWE.KnowWeObjectTypeBrowser.errorbox")
							+ "</p>");
			return buildi.toString();
		}

		// Render the Presentation of the findings
		buildi.append(this.renderFindingsSelectionMask(found, types));
		return buildi.toString();
	}

	/**
	 * Renders a table with the results of the search in it.
	 * Code Based upon RenamingRenderer
	 * 
	 * @param found a List with all found Sections in it
	 * @return a HTML formatted table witch lists all the findings in it
	 */
	private String renderFindingsSelectionMask(List<Section> found, String searchedType) {
		StringBuilder mask = new StringBuilder();

		// Create Table Header
		mask.append(createFindingsMaskTableHeader(searchedType));

		// insert findings into table
		String lastTopic = "";
		for (int k = 0; k < found.size(); k++) {

			// Check if Section has an Article
			k = this.getValidNextSection(k, found);
			if (k == -1) break;
			Section sec = found.get(k);
			String currentTopic = sec.getTitle();

			// new topic findings
			if (!lastTopic.equals(currentTopic)) {
				lastTopic = currentTopic;
				mask.append("<thead>");
				mask.append("<tr><td>");
				mask.append("<strong>" + sec.getTitle() + "</strong>");
				mask.append("</td><td></td><td></td>");
			}
			
			String BOLD_OPEN = "BOLD_OPEN";
			String BOLD_CLOSE = "BOLD_CLOSE";
			
			// Create a RenameFinding from the Section for context.
			String [] sectionWords = sec.getOriginalText().split(" ");
			WordBasedRenameFinding f = new WordBasedRenameFinding
											(0,sectionWords.length-1, sectionWords[0], sec);
			String text = BOLD_OPEN + f.contextText() + BOLD_CLOSE;
			
			// replace special characters
			text = replaceSpecialCharacters(text);
			text = text.replaceAll(BOLD_OPEN, "<b>");
			text = text.replaceAll(BOLD_CLOSE, "</b>");			
			
			// Add context with Scroll-Arrows
			mask.append("<tbody>");
			mask.append("<tr>");
			mask.append("<td>"
					+ createAdditionalMatchingTextSpan(sec.getArticle(),
							f.getSec().getId(),
							f.getSec().getAbsolutePositionStartInArticle(),
							0, 'p',
							true, sectionWords.length, sectionWords[0].length()));
			
			mask.append(" " + text + " ");

			mask.append(createAdditionalMatchingTextSpan(sec.getArticle(),
							f.getSec().getId(),
							f.getSec().getAbsolutePositionStartInArticle(),0,
							'a', true, sectionWords.length, sectionWords[0].length()));

			// Add Ancestors of Section to Table and relative Path
			mask.append(createFindingsMaskFatherColumnAndPath(sec));
		}

		mask.append("</table></fieldset>");

		return mask.toString();
	}

	/**
	 * Creates the button used to display additional Context.
	 * 
	 * @param article
	 * @param section
	 * @param start         startIndex of the Finding
	 * @param curWords		currently displayed amount Words(Context)
	 * @param direction		[p]revious or [a]fter
	 * @param span
	 * @param wordCount		amount of words in the found section
	 * @param queryLength	length of the first word of the found section
	 * @return
	 */
	private String createAdditionalMatchingTextSpan(KnowWEArticle article,
			String section, int start, int curWords, char direction, boolean span,
			int wordCount, int queryLength) {

		StringBuilder html = new StringBuilder();

		String arrowLeft = "KnowWEExtension/images/arrow_left.png";
		String arrowRight = "KnowWEExtension/images/arrow_right.png";

		String img;
		
		switch (direction) {
		case 'a':
			img = arrowRight;
			if (curWords > WordBasedRenameFinding.MAX_WORDS + wordCount) {
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
			        + "section: '"+section+"'," 
			        + "index: " + start + ", "
			        + "words: " + curWords + ", "
			        + "direction: '"+direction+"'," 
			        + "wordCount: "+wordCount +	"," 
			        + "queryLength: "+queryLength + "}";

		if (span) {
			html.append("<span id='" + direction + start
					+ "' class='short' style='display: inline;'>");
		}

		html.append("<img width=\"12\" height=\"12\" border=\"0\" src=\"" + img
				+ "\" alt=\"more\" rel=\"" + atmUrl + "\" " //delete queryLength 
				+ "class=\"show-additional-text\"/>");

		if (span) {
			html.append("</span>");
		}
		
		return html.toString();

	}

	/**
	 * Returns an additional text passage around the search result. So the user
	 * can view the result in a larger context.
	 * 
	 * @param amtURL additional text parameters
	 * @param web KnowWEEnvironment
	 * @param query user query string
	 * @return additional text area
	 */
	private String getAdditionalMatchText(String atmURL, String web, int queryLength) {

		// article#sectionId#position(Absolute)#curChars#direction#wordCount
		String[] params = atmURL.split(":");
		String articleTitle = params[0];
		String sectionId = params[1];
		int pos = Integer.parseInt(params[2]); 
		int curWords = Integer.parseInt(params[3]);
		String direction = params[4];
		int wordCount = Integer.parseInt(params[5]);

		String additionalText = "";

		// find article
		Iterator<KnowWEArticle> iter = KnowWEEnvironment.getInstance()
										.getArticleManager(web).getArticleIterator();
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			if (article.getTitle().equals(articleTitle)) {
				
				// get the Section needed for additional Context
				Section section = article.findSection(String.valueOf(sectionId));
				additionalText = WordBasedRenameFinding.getAdditionalContextTypeBrowser(pos,
								 direction, curWords, queryLength,
								 section.getArticle().getSection().getOriginalText(), wordCount);							

				// add highlighting when needed
				additionalText = this.replaceSpecialCharacters(additionalText);
				if ((direction.equals("a")) && (curWords+1 < wordCount)) {						
						additionalText = "<b>" + additionalText + "</b>";
				}

				// Create new Scroll-Arrow
				if (direction.equals("a")) {
					additionalText += createAdditionalMatchingTextSpan(article, sectionId,
							pos, curWords+1, direction.charAt(0), false, wordCount, queryLength);
				} else {
					additionalText = createAdditionalMatchingTextSpan(article, sectionId,
							pos, curWords+1, direction.charAt(0), false, wordCount, queryLength)
							+ additionalText;
				}
			}
		}
		
		return additionalText;
	}
	
	/**
	 * Runs up the DomTree and collects all Fathers from a Section.
	 * 
	 * @param sec
	 * @return
	 */
	private List<Section> getAllFathers(Section sec) {
		ArrayList<Section> found2 = new ArrayList<Section>();
	
		if (sec.getFather() != null) {
			found2.addAll(this.getAllFathers(sec.getFather()));
			found2.add(sec.getFather());
		}
		return found2;
	}

	/**
	 * Replaces characters \n \r > <
	 * 
	 * @param text
	 * @return
	 */
	private String replaceSpecialCharacters(String text) {
		text = text.replace("\n", "");
		text = text.replace("\r", "");
		text = text.replace(">", "&gt;");
		text = text.replace("<", "&lt;");
		return text;
	}

	/**
	 * Renders the columns for Father and Link.
	 * 
	 * @param sec
	 * @return
	 */
	private String createFindingsMaskFatherColumnAndPath(Section sec) {
		StringBuilder mask = new StringBuilder();
		
		mask.append("</td><td>");
	
		ArrayList<Section> fathers = new ArrayList<Section>();
		fathers = new ArrayList<Section>(this.getAllFathers(sec));
		String fString = "";
		for (Section s : fathers) {
			if (!(s.getObjectType() instanceof KnowWEArticle)) {
				String name = s.getObjectType().getName() + ", ";
				if (name.contains(".")) {
					name = name.substring(name.indexOf('.') + 1);
				}
				fString += s.getObjectType().getName() + ", ";
			}
		}
	
		if (fString.lastIndexOf(",") != -1) {
			fString = fString.substring(0, (fString.lastIndexOf(",")));
			mask.append(fString);
		}
		mask.append("</td>");
		
		// Relative Path for Link to Section
		mask.append("<td><a href=/KnowWE/Wiki.jsp?page=" + sec.getTitle()
				+ ">" + sec.getTitle() + "</a></td>");
		mask.append("</tr>");
		mask.append("</tbody>");
		
		return mask.toString();
	}

	/**
	 * Gets a Valid(has Article) Section index in found.
	 * 
	 * @param k
	 * @param found
	 * @return
	 */
	private int getValidNextSection(int k, List<Section> found) {
		for (;k < found.size();k++) {
			try {
				if (found.get(k).getArticle() != null)
					return k;
			} catch (NullPointerException e) {
				Logger.getLogger(this.getClass().getName()).warning(
				"Section is not valid: " + found.get(k).getId() + "!");
			}
		}
		return -1;
	}

	/**
	 * Renders the Header of the findings table.
	 * 
	 * @param searchedType
	 * @return
	 */
	private String createFindingsMaskTableHeader(String searchedType) {
		StringBuilder mask = new StringBuilder();
		mask.append("<fieldset><legend>"
				+ rb.getString("KnowWE.KnowWeObjectTypeBrowser.searchresult")
				+ " '" + searchedType.substring(searchedType.lastIndexOf(".")+1)
				+ "'</legend>");
		mask.append("<table id='sortable1'><colgroup><col class='match' /><col class='section' />");
		mask.append("<col class='preview' /></colgroup>");
		mask.append("<thead><tr><th scope='col'>"
				+ rb.getString("KnowWE.renamingtool.clmn.match")
				+ "</th><th scope='col'>"
				+ rb.getString("KnowWE.KnowWeObjectTypeBrowser.clmn.context")
				+ "</th>");
	
		mask.append("<th scope='col'>"
				+ rb.getString("KnowWE.KnowWeObjectTypeBrowser.clmn.article")
				+ "</th></tr>"
				+ "</thead>");
		return mask.toString();
	}

}
