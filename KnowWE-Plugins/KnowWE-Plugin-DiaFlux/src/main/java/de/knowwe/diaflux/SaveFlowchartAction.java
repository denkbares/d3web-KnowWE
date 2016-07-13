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

package de.knowwe.diaflux;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.type.DiaFluxType;
import de.knowwe.diaflux.type.FlowchartType;

/**
 * Receives a xml-encoded flowchart from the editor and replaces the old kdom node with the new content
 *
 * @author Reinhard Hatko
 * @created 24.11.2010
 */
public class SaveFlowchartAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String web = context.getWeb();
		String nodeID = context.getParameter(Attributes.TARGET);
		String topic = context.getTitle();
		String newText = context.getParameter(Attributes.TEXT);

		if (nodeID == null) {
			saveNewFlowchart(context, web, topic, newText);
		}
		else {
			replaceExistingFlowchart(context, web, nodeID, topic, newText);
		}

	}

	/**
	 * Saves a flowchart when the surrounding %%DiaFlux markup exists.
	 *
	 * @created 23.02.2011
	 */
	@SuppressWarnings("unchecked")
	private void replaceExistingFlowchart(UserActionContext context, String web, String nodeID, String topic, String newText) throws IOException {
		Section<DiaFluxType> diaFluxSection = (Section<DiaFluxType>) Sections.get(
				nodeID);

		Section<FlowchartType> flowchartSection = Sections.successor(diaFluxSection,
				FlowchartType.class);

		// new ID for section to be transmitted to the editor
		String id = null;

		// if flowchart is existing, replace flowchart
		if (flowchartSection != null) {
			Set<String> articles = KnowWEUtils.getPackageManager(flowchartSection).getCompilingArticles(
					flowchartSection);
			id = save(context, topic, flowchartSection.getID(), newText);
		}
		else { // no flowchart, insert flowchart
			StringBuilder builder = new StringBuilder("%%DiaFlux");
			builder.append("\r\n");
			builder.append(newText);

			// one line version, breaks because of missing linebreak,
			// see ticket #172
			Pattern pattern = Pattern.compile("^%%DiaFlux([^\\n\\r\\f]+?)(/?%)?\\s*$");
			Matcher matcher = pattern.matcher(diaFluxSection.getText());
			if (matcher.find()) {
				builder.append(Strings.trim(matcher.group(1)));
				builder.append("\r\n");
				builder.append("%\r\n");
			}
			else { // this adds all content, just extract annotations
				builder.append(diaFluxSection.getText().substring(9));
			}

			save(context, topic, nodeID, builder.toString());
		}

		// if new sectionID was found, transfer it to the editor
		if (id != null) {
			context.getWriter().write(id);
		}
	}


	/**
	 * Saves a flowchart for which no section exists in the article yet. Currently only used, when extracting a module
	 * with DiaFlux-Refactoring-Plugin.
	 *
	 * @throws IOException
	 * @created 23.02.2011
	 */
	private void saveNewFlowchart(UserActionContext context, String web, String topic, String newText) throws IOException {
		ArticleManager mgr = Environment.getInstance().getArticleManager(
				context.getWeb());
		Article article = mgr.getArticle(topic);
		Section<RootType> rootSection = article.getRootSection();

		// append flowchart to root section and replace it
		String newArticle = rootSection.getText() + "\r\n%%DiaFlux\r\n" + newText
				+ "\r\n%\r\n";
		String nodeID = rootSection.getID();

		save(context, topic, nodeID, newArticle);

	}

	private String save(UserActionContext context, String topic, String nodeID, String newText) throws IOException {
		Map<String, String> nodesMap = new HashMap<>();
		nodesMap.put(nodeID, newText);
		Sections.ReplaceResult replaceResult = Sections.replace(context, nodesMap);
		String newId = replaceResult.getSectionMapping().get(nodeID);
		replaceResult.sendErrors(context);
		try {
			context.getArticleManager().getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			Log.warning(e.getMessage(), e);
		}
		return newId;
	}

}
