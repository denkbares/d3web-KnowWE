package de.d3web.we.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.RenameFinding;
import de.d3web.we.kdom.Section;

public class KnowWEObjectTypeBrowserRenderer implements KnowWEAction {

	private static ResourceBundle kwikiBundle = ResourceBundle
			.getBundle("KnowWE_messages");

	@Override
	public String perform(KnowWEParameterMap map) {

		// handle show additional text
		if (map.get(KnowWEAttributes.ATM_URL) != null) {
			String web = map.getWeb();

			if (web == null) {
				web = KnowWEEnvironment.DEFAULT_WEB;
			}
			int queryLength = Integer.valueOf(map.get(KnowWEAttributes.TYPE_BROWSER_QUERY));
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
					art.getSection().findChildrenOfType(Class.forName(types),
							found);
				}
			}

		} catch (ClassNotFoundException e) {
			// TODO: Fehler ausgeben!!!
		}

		// if found is empty display error message
		if (found.isEmpty()) {
			buildi.append("<p class='error box'>"
							+ kwikiBundle.getString("KnowWE.KnowWeObjectTypeBrowser.errorbox")
							+ "</p>");
			return buildi.toString();
		}

		// Render the Presentation of the findings
		buildi.append(this.renderFindingsSelectionMask(found, types));
		return buildi.toString();
	}

	/**
	 * <p>
	 * Renders a table with the results of the search in it.
	 * Code Based upon RenamingRenderer
	 * </p>
	 * 
	 * @param found
	 *            a List with all found Sections in it
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
			if (k >= found.size()) break;
			Section sec = found.get(k);
			String currentTopic = sec.getTopic();

			// new topic findings
			if (!lastTopic.equals(currentTopic)) {
				lastTopic = currentTopic;
				mask.append("<thead>");
				mask.append("<tr><td>");
				mask.append("<strong>" + sec.getTopic() + "</strong>");
				mask.append("</td><td></td><td></td>");
			}
			
			// Create a RenameFinding from the Section for context.
			RenameFinding f = new RenameFinding(0,RenameFinding.getContext(0, sec, sec.getArticle().getSection().getOriginalText(),0),sec);
			String text = f.contextText();
			
			String BOLD_OPEN = "BOLD_OPEN";
			String BOLD_CLOSE = "BOLD_CLOSE";
			
			// Highlighting, when type is longer then the displayed text
			String[] queryText = getHighlightingAndItsLength(sec, text, BOLD_OPEN, BOLD_CLOSE);
			text = queryText[1];
			
			// needed for right higlighting(in ajax function querylength)
			int querylength = Integer.parseInt(queryText[0]);
			
			// Needed for additionalMatchingTextSpan()
			int textlength = text.length();
			
			// replace special characters
			text = replaceSpecialCharacters(text);
			text = text.replaceAll(BOLD_OPEN, "<b>");
			
			// adds an </b> at the end, when section to long
			if (text.equals(text.replaceAll(BOLD_CLOSE, "</b>"))){
				text += "</b>";
			} else {
				text = text.replaceAll(BOLD_CLOSE, "</b>");	
			}
			
			// Add context with Scroll-Arrows
			mask.append("<tbody>");
			mask.append("<tr>");
			mask.append("<td>"
					+ createAdditionalMatchingTextSpan(sec.getArticle(),
							f.getSec().getId(),
							f.getSec().getAbsolutePositionStartInArticle(),RenameFinding.CONTEXT_SIZE_SMALL, 'p', true,
							textlength));
			mask.append(" " + text + " ");
			// Last Parameter indicates how many of the searched Section is displayed
			// Needed for getAdditionalMatchText()
			mask.append(createAdditionalMatchingTextSpan(sec.getArticle(),
							f.getSec().getId(),
							f.getSec().getAbsolutePositionStartInArticle(),RenameFinding.CONTEXT_SIZE_SMALL, 'a', true,
							querylength));

			// Add Ancestors of Section to Table and relative Path
			mask.append(createFindingsMaskFatherColumnAndPath(sec));
		}

		mask.append("</table></fieldset></form>");

		return mask.toString();
	}

	/**
	 * Gets a Valid Section index in found.
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
				// No Article found!
			}

		}
		return found.size();
	}

	private String[] getHighlightingAndItsLength(Section sec,String text, String bold_open, String bold_close) {
		String[] queryText = new String[2];
		int querylength = 0;
		
		if (text.equals(text.replaceAll(Pattern.quote(sec.getOriginalText()), bold_open
				+ sec.getOriginalText() + bold_close))) {
			// where to insert <b>
			int index;
			if (sec.getOriginalText().length() >= 3) {
				index = text.indexOf(sec.getOriginalText().substring(0, 3));
			} else {
				index = text.indexOf(sec.getOriginalText().substring(0, sec.getOriginalText().length()));
			}

			// check if query is in context
			if (index != -1) {
				// set queryLength
				querylength = text.substring(index).length();
				// insert <b>
				text = text.substring(0, index) + bold_open + text.substring(index);
			}
		} else {
			// text fully in mask display
			text = text.replaceAll(Pattern.quote(sec.getOriginalText()), bold_open
					+ sec.getOriginalText() + bold_close);
		}
		
		queryText[0] = Integer.toString(querylength);
		queryText[1] = text;
		return queryText;
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
		mask.append("<td><a href=/KnowWE/Wiki.jsp?page=" + sec.getTopic()
				+ ">" + sec.getTopic() + "</a></td>");
		mask.append("</tr>");
		mask.append("</tbody>");
		
		return mask.toString();
	}

	private Object createFindingsMaskTableHeader(String searchedType) {
		StringBuilder mask = new StringBuilder();
		mask.append("<form method='post' action=''><fieldset><legend>"
				+ kwikiBundle.getString("KnowWE.KnowWeObjectTypeBrowser.searchresult")
				+ " '" + searchedType.substring(searchedType.lastIndexOf(".")+1)
				+ "'</legend>");
		mask.append("<table id='sortable1'><colgroup><col class='match' /><col class='section' />");
		mask.append("<col class='preview' /></colgroup>");
		mask.append("<thead><tr><th scope='col'>"
				+ kwikiBundle.getString("KnowWE.renamingtool.clmn.match")
				+ "</th><th scope='col'>"
				+ kwikiBundle.getString("KnowWE.KnowWeObjectTypeBrowser.clmn.context")
				+ "</th>");

		mask.append("<th scope='col'>"
				+ kwikiBundle.getString("KnowWE.KnowWeObjectTypeBrowser.clmn.article")
				+ "</th></tr>"
				+ "</thead>");
		return mask.toString();
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
				p = Pattern.compile(query);

				Matcher m = p.matcher(token);

				while (m.find()) {
					result.append(token.substring(0, m.start()) + "<b>"
							+ token.substring(m.start(), m.end()) + "</b>"
							+ token.substring(m.end(), token.length()));
				}
			} else {
				result.append(token);
			}
		}
		return result.toString();
	}

	// article, position, sectionNum, chars, direction, contextAvailable
	private String createAdditionalMatchingTextSpan(KnowWEArticle article,
			String section, int start, int chars, char direction, boolean span, int displayedLength) {

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

		// create atmUrl (e.g. swimming#0#264#20#-1)
		String atmUrl = article.getTitle() + "#" + section + "#" + start + "#"
				+ chars + "#" + direction;

		if (span) {
			html.append("<span id='" + direction + start
					+ "' class='short' style='display: inline;'>");
		}

		html.append("<a href='javascript:getAdditionalMatchTextTypeBrowser(\""
				+ atmUrl + "\",\"" + displayedLength + "\" )'>");
		html.append("<img width='12' height='12' border='0' src='" + img
				+ "' alt='more'/>");
		html.append("</a>");

		if (span) {
			html.append("</span>");
		}
		
		return html.toString();

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
	private String getAdditionalMatchText(String atmURL, String web, int queryLength) {

		// article#sectionId#position(Absolute)#curChars#direction
		// e.g. Swimming#0#264#20#a
		String[] params = atmURL.split("#");
		String articleTitle = params[0];
		String sectionId = params[1];
		int pos = Integer.parseInt(params[2]); 
		int chars = Integer.parseInt(params[3]);
		String direction = params[4];

		String additionalText = "";

		// find article
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance()
				.getArticleManager(web);
		Iterator<KnowWEArticle> iter = mgr.getArticleIterator();
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();

			if (article.getTitle().equals(articleTitle)) {
				
				// get the Section needed for additional Context
				Section section = article.findSection(String.valueOf(sectionId));
				additionalText = RenameFinding.getAdditionalContext(pos,
						direction, chars, 0, section.getArticle().getSection().getOriginalText());							
				
				// add highlighting when needed
				// 1. highlighting needed
				if ((queryLength != 0) && (direction.equals("a"))) {
					
					// everything in additionalText is from section
					if (chars < (section.getOriginalText().length() - queryLength)){
						additionalText = this.replaceSpecialCharacters(additionalText);
						additionalText = "<b>" + additionalText + "</b>";
					} else {
						// the section ends within additionalText
						String add1 = this.replaceSpecialCharacters(additionalText.substring(0, (section.getOriginalText().length() - queryLength)));
						String add2 = this.replaceSpecialCharacters(additionalText.substring(section.getOriginalText().length() - queryLength));						
						additionalText = "<b>" + add1 + "</b>" + add2;
					}
				} else {
					additionalText = this.replaceSpecialCharacters(additionalText);
				}

				// Create new Scroll-Arrow
				if (direction.equals("a")) {
					additionalText += createAdditionalMatchingTextSpan(article,
							sectionId, pos, chars + RenameFinding.CONTEXT_SIZE_SMALL, direction.charAt(0),
							false, queryLength);
				} else {
					additionalText = createAdditionalMatchingTextSpan(article,
							sectionId, pos, chars + RenameFinding.CONTEXT_SIZE_SMALL, direction.charAt(0),
							false, 0)
							+ additionalText;
				}
			}
		}
		
		return additionalText;
	}

}
