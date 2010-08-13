package de.d3web.we.core.namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.event.ArticleCreatedEvent;
import de.d3web.we.event.EventManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class KnowWENamespaceManager {

	private final String web;

	private final Map<String, LinkedList<Section<?>>> namespaceDefinitionsMap = new HashMap<String, LinkedList<Section<?>>>();

	private final Map<String, HashSet<Section<? extends NamespaceInclude>>> namespaceIncludesMap = new HashMap<String, HashSet<Section<? extends NamespaceInclude>>>();

	private final Set<String> changedNamespaces = new HashSet<String>();

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
		for (String namespace : s.getNamespaces()) {
			LinkedList<Section<?>> namespaceList = namespaceDefinitionsMap.get(namespace);
			if (namespaceList != null) {
				boolean removed = namespaceList.remove(s);
				if (removed) changedNamespaces.add(namespace);
				if (namespaceList.isEmpty()) namespaceDefinitionsMap.remove(namespace);
				return removed;
			}
		}
		return false;
	}

	public void cleanForArticle(String title) {
		for (LinkedList<Section<?>> list : namespaceDefinitionsMap.values()) {
			List<Section<?>> sectionsToRemove = new ArrayList<Section<?>>();
			for (Section<?> sec : list) {
				if (sec.getTitle().equals(title)) sectionsToRemove.add(sec);
			}
			list.removeAll(sectionsToRemove);
		}
		for (HashSet<Section<? extends NamespaceInclude>> set : namespaceIncludesMap.values()) {
			List<Section<?>> sectionsToRemove = new ArrayList<Section<?>>();
			for (Section<?> sec : set) {
				if (sec.getTitle().equals(title)) sectionsToRemove.add(sec);
			}
			set.removeAll(sectionsToRemove);
		}

	}

	public List<Section<?>> getNamespaceDefinitions(String namespace) {
		LinkedList<Section<?>> namespaceDefs = namespaceDefinitionsMap.get(namespace);
		if (namespaceDefs != null) {
			Collections.sort(namespaceDefs);
			return Collections.unmodifiableList(namespaceDefs);
		}
		else {
			return Collections.unmodifiableList(new ArrayList<Section<?>>(0));
		}
	}

	public void registerNamespaceInclude(KnowWEArticle article, Section<? extends NamespaceInclude> s) {
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
			if (namespaceIncludes.isEmpty()) namespaceIncludesMap.remove(article.getTitle());
			return removed;
		}
		return false;
	}

	public Set<Section<? extends NamespaceInclude>> getNamespaceIncludes(KnowWEArticle article) {
		Set<Section<? extends NamespaceInclude>> namespaceIncludes = namespaceIncludesMap.get(article.getTitle());
		return namespaceIncludes == null
				? Collections.unmodifiableSet(new HashSet<Section<? extends NamespaceInclude>>(0))
				: Collections.unmodifiableSet(namespaceIncludes);
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
