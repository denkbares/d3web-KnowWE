package de.d3web.we.core.namespace;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class KnowWENamespaceManager {

	private final String web;

	private final Map<String, TreeSet<Section<?>>> namespaceSectionsMap = new HashMap<String, TreeSet<Section<?>>>();

	private final Map<String, HashSet<Section<? extends NamespaceInclude>>> namespaceIncludesMap = new HashMap<String, HashSet<Section<? extends NamespaceInclude>>>();

	public KnowWENamespaceManager(String web) {
		this.web = web;
	}

	public String getWeb() {
		return web;
	}

	public void registerNamespaceDefinition(Section<?> s) {
		for (String namespace : s.getNamespaces()) {
			TreeSet<Section<?>> namespaceList = namespaceSectionsMap.get(namespace);
			if (namespaceList == null) {
				namespaceList = new TreeSet<Section<?>>();
				namespaceSectionsMap.put(namespace, namespaceList);
			}
			namespaceList.add(s);
		}
	}
	
	public boolean unregisterNamespaceDefinition(Section<?> s) {
		for (String namespace : s.getNamespaces()) {
			Set<Section<?>> namespaceList = namespaceSectionsMap.get(namespace);
			if (namespaceList != null) {
				boolean removed = namespaceList.remove(s);
				if (namespaceList.isEmpty()) namespaceSectionsMap.remove(namespace);
				return removed;
			}
		}
		return false;
	}

	public Set<Section<?>> getNamespaceDefinitions(String namespace) {
		TreeSet<Section<?>> namespaceDefs = namespaceSectionsMap.get(namespace);
		return namespaceDefs == null
				? Collections.unmodifiableSet(new TreeSet<Section<?>>())
				: Collections.unmodifiableSet(namespaceDefs);
	}

	public void registerNamespaceInclude(KnowWEArticle article, Section<? extends NamespaceInclude> s) {
		HashSet<Section<? extends NamespaceInclude>> namespaceIncludes = namespaceIncludesMap.get(article.getTitle());
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
			Collection<String> tempNamespaces = nsInclude.get().getIncludedNamespaces(nsInclude);
			if (tempNamespaces != null) includedNamespaces.addAll(tempNamespaces);
		}
		return Collections.unmodifiableSet(includedNamespaces);
	}

}
