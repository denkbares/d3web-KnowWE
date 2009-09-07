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
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.RenameFinding;
import de.d3web.we.kdom.Section;

public class GlobalReplaceAction implements KnowWEAction {

	private static ResourceBundle kwikiBundle = ResourceBundle.getBundle("KnowWE_messages");
	
	/* (non-Javadoc)
	 * @see de.d3web.we.javaEnv.KnowWEAction#perform(de.d3web.we.javaEnv.KnowWEParameterMap)
	 */
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		String query = parameterMap.get(KnowWEAttributes.TARGET);
		String replacement = parameterMap.get(KnowWEAttributes.FOCUSED_TERM);
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(parameterMap.getWeb());
		String web = parameterMap.getWeb();
		
		//replaceFindings auspacken
		String replacements = parameterMap.get(KnowWEAttributes.TEXT);
		if(replacements == null) return "no replacements given";
		String [] replacementArray = replacements.split("__");
		
		Map<Section, List<RenameFinding>> findingsPerSection = new HashMap<Section, List<RenameFinding>>();
		Collection<KnowWEArticle> modifiedArticles = new HashSet<KnowWEArticle>();
		
		//replaceFindings decodieren
		for (String string : replacementArray) {
			if(!string.contains("#")) continue;
 			String data [] = string.split("#");
			String article = data[0];
			String sectionNumber = data[1];
			String startIndex = data[2];
			
			String nodeID = sectionNumber;
			int start = Integer.parseInt(startIndex);
			
			//search Replacements
			KnowWEArticle art = mgr.getArticle(article);
			if(art == null) {
				//TODO report ERROR
				return "<p class=\"error box\">" + kwikiBundle.getString("KnowWE.renamingtool.msg.noarticle") + article + "</div>";
			}
			modifiedArticles.add(art);
			Section sec = art.getNode(nodeID);
			
			
			//organize replacementRequests per sections
			if(findingsPerSection.containsKey(sec)) {
				findingsPerSection.get(sec).add(new RenameFinding(start,RenameFinding.getContext(start, sec, art.getSection().getOriginalText(), query.length()) , sec));
			}else {
				List<RenameFinding>  set = new ArrayList<RenameFinding>();
				set.add(new RenameFinding(start,RenameFinding.getContext(start, sec, art.getSection().getOriginalText(), query.length()) , sec));
				findingsPerSection.put(sec, set);
			}
		}
		
		StringBuilder errors = new StringBuilder();
		
		int count = 0;
		//Ersetzungen vornehmen
		for (Entry<Section, List<RenameFinding>> entry : findingsPerSection.entrySet()) {
			Section sec = entry.getKey();
			List<RenameFinding> list = entry.getValue();
			Collections.sort(list);
			StringBuffer buff = new StringBuffer();
			int lastEnd = 0;
			for (RenameFinding finding : list) {
				int start = finding.getStart();
				String potentialMatch = sec.getOriginalText().substring(start, start+ query.length());
				if(potentialMatch.equals(query)) {
					//found
					buff.append(sec.getOriginalText().substring(lastEnd, start));
					buff.append(replacement);
					lastEnd = start + query.length();
					count++;
				} else {
					String errorMsg = kwikiBundle.getString("KnowWE.renamingtool.msg.error").replace("{0}", sec.getOriginalText());
					errors.append("<p class=\"error box\">" + errorMsg + "</p>");
					//TODO report!
				}
			}
			//den Rest nach dem letzten match hintendranhaengen
			buff.append(sec.getOriginalText().substring(lastEnd, sec.getOriginalText().length()));
			//section text overridden => KDOM dirty
			sec.setOriginalText(buff.toString());
			sec.getChildren().clear();
		}

		
		
		//Artikel im JSPWiki speichern
		for(KnowWEArticle art : modifiedArticles) {
			// Gesamttext zusammenbauen
			String text = art.collectTextsFromLeaves();
			
			KnowWEEnvironment.getInstance().saveArticle(web, art.getTitle(), text, parameterMap);
			mgr.saveUpdatedArticle(new KnowWEArticle(text, art.getTitle(), KnowWEEnvironment
					.getInstance().getRootTypes(),web));
		}

		//Meldung gernerieren und zurï¿½ckgeben.
		String summary = kwikiBundle.getString("KnowWE.renamingtool.msg.summary");
		String[] values = {String.valueOf(count), String.valueOf(findingsPerSection.size()),
				query, replacement};
		
		for(int i = 0; i < values.length; i++){
			summary = summary.replace("{" + i + "}", values[i]);
		}
		return errors.toString() + "<p class=\"info box\">" + summary + "</p>";
	}
}
