/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.core.namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.event.ArticleCreatedEvent;
import de.d3web.we.event.EventManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class KnowWENamespaceManager {

	private final String web;

	private final Map<String, LinkedList<Section<?>>> namespaceDefinitionsMap = new HashMap<String, LinkedList<Section<?>>>();

	private final Map<String, HashSet<Section<? extends NamespaceInclude>>> namespaceIncludesMap = new HashMap<String, HashSet<Section<? extends NamespaceInclude>>>();

	private final Set<String> changedNamespaces = new HashSet<String>();

	public static final boolean AUTOCOMPILE_ARTICLE = ResourceBundle.getBundle("KnowWE_config").getString(
			"namespaces.autocompileArticle").contains("true");

	public KnowWENamespaceManager(String web) {
		this.web = web;
	}

	public String getWeb() {
		return web;
	}

	public void registerNamespaceDefinition(Section<?> s) {
		for (String namespace : s.getNamespaces()) {
			if (namespace.equals(s.getTitle())) continue;
			LinkedList<Section<?>> namespaceList = namespaceDefinitionsMap.get(namespace);
			if (namespaceList == null) {
				namespaceList = new LinkedList<Section<?>>();
				namespaceDefinitionsMap.put(namespace, namespaceList);
			}
			namespaceList.add(s);
			changedNamespaces.add(namespace);
		}
	}

	public boolean unregisterNamespaceDefinition(Section<?> s) {
		Set<String> includingArticles = null;
		for (String namespace : s.getNamespaces()) {
			LinkedList<Section<?>> namespaceList = namespaceDefinitionsMap.get(namespace);
			if (namespaceList != null) {
				boolean removed = namespaceList.remove(s);
				if (removed) {
					changedNamespaces.add(namespace);
					if (includingArticles == null) {
						includingArticles = getArticlesIncluding(namespace);
					}
					for (String article : includingArticles) {
						if (s.isReusedBy(article)) s.setReusedStateRecursively(article, false);
					}
				}
				if (namespaceList.isEmpty()) namespaceDefinitionsMap.remove(namespace);
				return removed;
			}
		}
		return false;
	}

	public void cleanForArticle(KnowWEArticle article) {
		for (LinkedList<Section<?>> list : new ArrayList<LinkedList<Section<?>>>(
				namespaceDefinitionsMap.values())) {
			List<Section<?>> sectionsToRemove = new ArrayList<Section<?>>();
			for (Section<?> sec : list) {
				if (sec.getTitle().equals(article.getTitle())) sectionsToRemove.add(sec);
			}
			for (Section<?> sec : sectionsToRemove) {
				unregisterNamespaceDefinition(sec);
			}
		}
		for (HashSet<Section<? extends NamespaceInclude>> set : new ArrayList<HashSet<Section<? extends NamespaceInclude>>>(
				namespaceIncludesMap.values())) {
			List<Section<? extends NamespaceInclude>> sectionsToRemove = new ArrayList<Section<? extends NamespaceInclude>>();
			for (Section<? extends NamespaceInclude> sec : set) {
				if (sec.getTitle().equals(article.getTitle())) sectionsToRemove.add(sec);
			}
			for (Section<? extends NamespaceInclude> sec : sectionsToRemove) {
				unregisterNamespaceInclude(article, sec);
			}
		}

	}

	public List<Section<?>> getNamespaceDefinitions(String namespace) {
		LinkedList<Section<?>> namespaceDefs = namespaceDefinitionsMap.get(namespace);
		if (namespaceDefs != null) {
			Collections.sort(namespaceDefs);
			return Collections.unmodifiableList(new ArrayList<Section<?>>(namespaceDefs));
		}
		else {
			return Collections.unmodifiableList(new ArrayList<Section<?>>(0));
		}
	}

	public void registerNamespaceInclude(KnowWEArticle article, Section<? extends NamespaceInclude> s) {
		
		if (s.get().getNamespaceToInclude(s).equals(article.getTitle())
				&& !getNamespaceIncludes(article).contains(s)) {
			if (!AUTOCOMPILE_ARTICLE) {

				// If the NamespaceInclude aims at the article it is defined in
				// and autocompile is deactivated, the reused-flags need to be
				// reset. If for example only the NamespaceInclude was added to
				// the article and all other Sections could be reused, all these
				// reused-flags are set to true although no SubtreeHandlers have
				// created yet.

				Set<String> includedNamespaces = getIncludedNamespaces(article);

				Set<Section<?>> alreadyIncluded = new HashSet<Section<?>>();
				for (String incNS : includedNamespaces) {
					List<Section<?>> nsDefs = getNamespaceDefinitions(incNS);
					for (Section<?> nsDef : nsDefs) {
						List<Section<?>> tempNodes = new LinkedList<Section<?>>();
						nsDef.getAllNodesPostOrder(tempNodes);
						alreadyIncluded.addAll(tempNodes);
					}
				}
				List<Section<?>> allNodesPostOrder = article.getAllNodesPostOrder();
				for (Section<?> node : allNodesPostOrder) {
					if (!alreadyIncluded.contains(node)) {
						node.setReusedBy(article.getTitle(), false);
					}
				}
			}
			else {
				return;
			}
		}
		
		HashSet<Section<? extends NamespaceInclude>> namespaceIncludes =
				namespaceIncludesMap.get(article.getTitle());
		if (namespaceIncludes == null) {
			namespaceIncludes = new HashSet<Section<? extends NamespaceInclude>>(4);
			namespaceIncludesMap.put(article.getTitle(), namespaceIncludes);
		}
		namespaceIncludes.add(s);
	}

	public boolean unregisterNamespaceInclude(KnowWEArticle article, Section<? extends NamespaceInclude> s) {
		Set<Section<? extends NamespaceInclude>> namespaceIncludes = namespaceIncludesMap.get(article.getTitle());

		if (namespaceIncludes != null) {
			boolean removed = namespaceIncludes.remove(s);
			if (removed) {
				String includedNamespace = s.get().getNamespaceToInclude(s);
				boolean stillIncluded = false;
				for (Section<? extends NamespaceInclude> nsInclude : namespaceIncludes) {
					if (nsInclude.get().getNamespaceToInclude(nsInclude).equals(includedNamespace)) {
						stillIncluded = true;
						break;
					}
				}
				if (!stillIncluded) {
					List<Section<?>> namespaceDefinitions;
					if (includedNamespace.equals(article.getTitle())) {
						namespaceDefinitions = new ArrayList<Section<?>>(1);
						namespaceDefinitions.add(article.getSection());
					}
					else {
						namespaceDefinitions = getNamespaceDefinitions(
								s.get().getNamespaceToInclude(s));
					}
					for (Section<?> namespaceDef : namespaceDefinitions) {
						namespaceDef.setReusedStateRecursively(article.getTitle(), false);
						KnowWEUtils.clearMessagesRecursively(article, namespaceDef);
					}
				}
			}
			if (namespaceIncludes.isEmpty()) namespaceIncludesMap.remove(article.getTitle());
			return removed;
		}
		return false;
	}

	public Set<Section<? extends NamespaceInclude>> getNamespaceIncludes(KnowWEArticle article) {
		Set<Section<? extends NamespaceInclude>> namespaceIncludes = namespaceIncludesMap.get(article.getTitle());
		return namespaceIncludes == null
				? Collections.unmodifiableSet(new HashSet<Section<? extends NamespaceInclude>>(0))
				: Collections.unmodifiableSet(new HashSet<Section<? extends NamespaceInclude>>(
						namespaceIncludes));
	}

	public Set<String> getArticlesIncluding(String namespace) {
		Set<String> matchingArticles = new HashSet<String>();
		for (String article : namespaceIncludesMap.keySet()) {
			for (Section<? extends NamespaceInclude> namespaceInclude : namespaceIncludesMap.get(article)) {
				if (namespaceInclude.get().getNamespaceToInclude(namespaceInclude).equals(namespace)) {
					matchingArticles.add(article);
				}
			}
		}
		return Collections.unmodifiableSet(matchingArticles);
	}

	public Set<String> getIncludedNamespaces(KnowWEArticle article) {
		Set<Section<? extends NamespaceInclude>> namespaceIncludes = getNamespaceIncludes(article);
		HashSet<String> includedNamespaces = new HashSet<String>();
		for (Section<? extends NamespaceInclude> nsInclude : namespaceIncludes) {
			String tempNamespace = nsInclude.get().getNamespaceToInclude(nsInclude);
			if (tempNamespace != null) includedNamespaces.add(tempNamespace);
		}
		return Collections.unmodifiableSet(includedNamespaces);
	}

	public void updateNamespaceIncludes(KnowWEArticle article) {
		List<String> articlesToRevise = new ArrayList<String>();

		for (HashSet<Section<? extends NamespaceInclude>> includesSet : namespaceIncludesMap.values()) {
			for (Section<? extends NamespaceInclude> nsInclude : includesSet) {
				if (changedNamespaces.contains(nsInclude.get().getNamespaceToInclude(nsInclude))
						&& !article.getTitle().equals(
								nsInclude.get().getNamespaceToInclude(nsInclude))) {
					articlesToRevise.add(nsInclude.getTitle());
				}
			}
		}

		changedNamespaces.clear();

		KnowWEEnvironment env = KnowWEEnvironment.getInstance();
		for (String title : articlesToRevise) {
			KnowWEArticle newArt = new KnowWEArticle(
					env.getArticle(article.getWeb(), title).getSection().getOriginalText(), title,
					env.getRootType(), web, article.getTitle(), false);

			// fire 'article-created' event for the new article
			EventManager.getInstance().fireEvent(ArticleCreatedEvent.getInstance(), web,
					null, newArt.getSection());

			env.getArticleManager(web).saveUpdatedArticle(newArt);
		}
	}

}
