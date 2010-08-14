/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class GlobalReplaceAction extends DeprecatedAbstractKnowWEAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.d3web.we.javaEnv.KnowWEAction#perform(de.d3web.we.javaEnv.
	 * KnowWEParameterMap)
	 */
	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(
				parameterMap.getRequest());

		String query = parameterMap.get(KnowWEAttributes.TARGET);
		String replacement = parameterMap.get(KnowWEAttributes.FOCUSED_TERM);
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(
				parameterMap.getWeb());
		String web = parameterMap.getWeb();

		// replaceFindings auspacken
		String replacements = parameterMap.get(KnowWEAttributes.TEXT);
		if (replacements == null) return rb.getString("KnowWE.renamingtool.noreplacement");
		String[] replacementArray = replacements.split("__");

		Map<Section, List<WordBasedRenameFinding>> findingsPerSection = new HashMap<Section, List<WordBasedRenameFinding>>();
		Collection<KnowWEArticle> modifiedArticles = new HashSet<KnowWEArticle>();

		// replaceFindings decodieren
		for (String string : replacementArray) {
			if (!string.contains(WordBasedRenamingAction.TXT_SEPERATOR)) continue;
			String data[] = string.split(WordBasedRenamingAction.TXT_SEPERATOR);
			String article = data[0];
			String sectionNumber = data[1];
			String startIndex = data[2];

			String nodeID = sectionNumber;
			int start = Integer.parseInt(startIndex);

			// search Replacements
			KnowWEArticle art = mgr.getArticle(article);
			if (art == null) {
				// TODO report ERROR
				return "<p class=\"error box\">"
						+ rb.getString("KnowWE.renamingtool.msg.noarticle") + article + "</div>";
			}
			modifiedArticles.add(art);
			Section sec = art.getNode(nodeID);

			// organize replacementRequests per sections
			if (findingsPerSection.containsKey(sec)) {
				findingsPerSection.get(sec).add(
						new WordBasedRenameFinding(start, 0, WordBasedRenameFinding.getContext(
								start, sec, art.getSection().getOriginalText(), query.length()),
								sec));
			}
			else {
				List<WordBasedRenameFinding> set = new ArrayList<WordBasedRenameFinding>();
				set.add(new WordBasedRenameFinding(start, 0, WordBasedRenameFinding.getContext(
						start, sec, art.getSection().getOriginalText(), query.length()), sec));
				findingsPerSection.put(sec, set);
			}
		}

		StringBuilder errors = new StringBuilder();

		int count = 0;
		// Ersetzungen vornehmen
		for (Entry<Section, List<WordBasedRenameFinding>> entry : findingsPerSection.entrySet()) {
			Section sec = entry.getKey();
			List<WordBasedRenameFinding> list = entry.getValue();
			Collections.sort(list);
			StringBuffer buff = new StringBuffer();
			int lastEnd = 0;
			for (WordBasedRenameFinding finding : list) {
				int start = finding.getStart();
				String potentialMatch = sec.getOriginalText().substring(start,
						start + query.length());
				if (potentialMatch.equals(query)) {
					// found
					buff.append(sec.getOriginalText().substring(lastEnd, start));
					buff.append(replacement);
					lastEnd = start + query.length();
					count++;
				}
				else {
					String errorMsg = rb.getString("KnowWE.renamingtool.msg.error").replace("{0}",
							sec.getOriginalText());
					errors.append("<p class=\"error box\">" + errorMsg + "</p>");
					// TODO report!
				}
			}
			// den Rest nach dem letzten match hintendranhaengen
			buff.append(sec.getOriginalText().substring(lastEnd, sec.getOriginalText().length()));
			// section text overridden => KDOM dirty
			sec.setOriginalText(buff.toString());
			sec.getChildren().clear();
		}

		// Artikel im JSPWiki speichern
		for (KnowWEArticle art : modifiedArticles) {
			// Gesamttext zusammenbauen
			String text = art.collectTextsFromLeaves();

			KnowWEEnvironment.getInstance().saveArticle(web, art.getTitle(), text, parameterMap);
			mgr.saveUpdatedArticle(new KnowWEArticle(text, art.getTitle(), KnowWEEnvironment
					.getInstance().getRootType(), web));
		}

		// Meldung gernerieren und zurueckgeben.
		String summary = rb.getString("KnowWE.renamingtool.msg.summary");
		String[] values = {
				String.valueOf(count), String.valueOf(findingsPerSection.size()),
				query, replacement };

		for (int i = 0; i < values.length; i++) {
			summary = summary.replace("{" + i + "}", values[i]);
		}
		return errors.toString() + "<p class=\"info box\">" + summary
				+ rb.getString("KnowWE.renamingtool.msg.redirect") + "</p>";
	}
}
