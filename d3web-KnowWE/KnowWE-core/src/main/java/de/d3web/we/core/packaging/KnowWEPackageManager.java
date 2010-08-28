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

package de.d3web.we.core.packaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.event.Event;
import de.d3web.we.event.EventListener;
import de.d3web.we.event.EventManager;
import de.d3web.we.event.FullParseEvent;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class KnowWEPackageManager implements EventListener {

	public static final String ATTRIBUTE_ENAME = "package";

	private final String web;

	private final Map<String, LinkedList<Section<?>>> packageDefinitionsMap = new HashMap<String, LinkedList<Section<?>>>();

	private final Map<String, HashSet<Section<? extends PackageInclude>>> packageIncludesMap = new HashMap<String, HashSet<Section<? extends PackageInclude>>>();

	private final Set<String> changedPackages = new HashSet<String>();

	public static final boolean AUTOCOMPILE_ARTICLE = ResourceBundle.getBundle("KnowWE_config").getString(
			"packaging.autocompileArticle").contains("true");

	public KnowWEPackageManager(String web) {
		this.web = web;
		EventManager.getInstance().registerListener(this);
	}

	public String getWeb() {
		return web;
	}

	public void registerPackageDefinition(Section<?> s) {
		for (String packageName : s.getPackageNames()) {
			if (packageName.equals(s.getTitle())) continue;
			LinkedList<Section<?>> packageList = packageDefinitionsMap.get(packageName);
			if (packageList == null) {
				packageList = new LinkedList<Section<?>>();
				packageDefinitionsMap.put(packageName, packageList);
			}
			packageList.add(s);
			changedPackages.add(packageName);
		}
	}

	public boolean unregisterPackageDefinition(Section<?> s) {
		Set<String> includingArticles = null;
		for (String packageName : s.getPackageNames()) {
			LinkedList<Section<?>> packageList = packageDefinitionsMap.get(packageName);
			if (packageList != null) {
				boolean removed = packageList.remove(s);
				if (removed) {
					changedPackages.add(packageName);
					if (includingArticles == null) {
						includingArticles = getArticlesIncluding(packageName);
					}
					for (String article : includingArticles) {
						if (s.isReusedBy(article)) s.setReusedStateRecursively(article, false);
					}
				}
				if (packageList.isEmpty()) packageDefinitionsMap.remove(packageName);
				return removed;
			}
		}
		return false;
	}

	public void cleanForArticle(KnowWEArticle article) {
		for (LinkedList<Section<?>> list : new ArrayList<LinkedList<Section<?>>>(
				packageDefinitionsMap.values())) {
			List<Section<?>> sectionsToRemove = new ArrayList<Section<?>>();
			for (Section<?> sec : list) {
				if (sec.getTitle().equals(article.getTitle())) sectionsToRemove.add(sec);
			}
			for (Section<?> sec : sectionsToRemove) {
				unregisterPackageDefinition(sec);
			}
		}
		for (HashSet<Section<? extends PackageInclude>> set : new ArrayList<HashSet<Section<? extends PackageInclude>>>(
				packageIncludesMap.values())) {
			List<Section<? extends PackageInclude>> sectionsToRemove = new ArrayList<Section<? extends PackageInclude>>();
			for (Section<? extends PackageInclude> sec : set) {
				if (sec.getTitle().equals(article.getTitle())) sectionsToRemove.add(sec);
			}
			for (Section<? extends PackageInclude> sec : sectionsToRemove) {
				unregisterPackageInclude(article, sec);
			}
		}

	}

	public List<Section<?>> getPackageDefinitions(String packageName) {
		LinkedList<Section<?>> packageDefs = packageDefinitionsMap.get(packageName);
		if (packageDefs != null) {
			Collections.sort(packageDefs);
			return Collections.unmodifiableList(new ArrayList<Section<?>>(packageDefs));
		}
		else {
			return Collections.unmodifiableList(new ArrayList<Section<?>>(0));
		}
	}

	public void registerPackageInclude(KnowWEArticle article, Section<? extends PackageInclude> s) {
		
		if (s.get().getPackageToInclude(s).equals(article.getTitle())
				&& !getPackageIncludes(article).contains(s)) {
			if (!AUTOCOMPILE_ARTICLE) {

				// If the PackageInclude aims at the article it is defined in
				// and autocompile is deactivated, the reused-flags need to be
				// reset. If for example only the PackageInclude was added to
				// the article and all other Sections could be reused, all these
				// reused-flags are set to true although no SubtreeHandlers have
				// created yet.

				Set<String> includedPackages = getIncludedPackages(article);

				Set<Section<?>> alreadyIncluded = new HashSet<Section<?>>();
				for (String incPack : includedPackages) {
					List<Section<?>> packDefs = getPackageDefinitions(incPack);
					for (Section<?> packDef : packDefs) {
						List<Section<?>> tempNodes = new LinkedList<Section<?>>();
						packDef.getAllNodesPostOrder(tempNodes);
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
		
		HashSet<Section<? extends PackageInclude>> packageIncludes =
				packageIncludesMap.get(article.getTitle());
		if (packageIncludes == null) {
			packageIncludes = new HashSet<Section<? extends PackageInclude>>(4);
			packageIncludesMap.put(article.getTitle(), packageIncludes);
		}
		packageIncludes.add(s);
	}

	public boolean unregisterPackageInclude(KnowWEArticle article, Section<? extends PackageInclude> s) {
		Set<Section<? extends PackageInclude>> packageIncludes = packageIncludesMap.get(article.getTitle());

		if (packageIncludes != null) {
			boolean removed = packageIncludes.remove(s);
			if (removed) {
				String includedPackage = s.get().getPackageToInclude(s);
				boolean stillIncluded = false;
				for (Section<? extends PackageInclude> packInclude : packageIncludes) {
					if (packInclude.get().getPackageToInclude(packInclude).equals(includedPackage)) {
						stillIncluded = true;
						break;
					}
				}
				if (!stillIncluded) {
					List<Section<?>> packageDefinitions;
					if (includedPackage.equals(article.getTitle())) {
						packageDefinitions = new ArrayList<Section<?>>(1);
						packageDefinitions.add(article.getSection());
					}
					else {
						packageDefinitions = getPackageDefinitions(
								s.get().getPackageToInclude(s));
					}
					for (Section<?> packageDef : packageDefinitions) {
						packageDef.setReusedStateRecursively(article.getTitle(), false);
						KnowWEUtils.clearMessagesRecursively(article, packageDef);
					}
				}
			}
			if (packageIncludes.isEmpty()) packageIncludesMap.remove(article.getTitle());
			return removed;
		}
		return false;
	}

	public Set<Section<? extends PackageInclude>> getPackageIncludes(KnowWEArticle article) {
		Set<Section<? extends PackageInclude>> packageIncludes = packageIncludesMap.get(article.getTitle());
		return packageIncludes == null
				? Collections.unmodifiableSet(new HashSet<Section<? extends PackageInclude>>(0))
				: Collections.unmodifiableSet(new HashSet<Section<? extends PackageInclude>>(
						packageIncludes));
	}

	public Set<String> getArticlesIncluding(String packageName) {
		Set<String> matchingArticles = new HashSet<String>();
		for (String article : packageIncludesMap.keySet()) {
			for (Section<? extends PackageInclude> packageInclude : packageIncludesMap.get(article)) {
				if (packageInclude.get().getPackageToInclude(packageInclude).equals(packageName)) {
					matchingArticles.add(article);
				}
			}
		}
		return Collections.unmodifiableSet(matchingArticles);
	}

	public Set<String> getIncludedPackages(KnowWEArticle article) {
		Set<Section<? extends PackageInclude>> packageIncludes = getPackageIncludes(article);
		HashSet<String> includedPackages = new HashSet<String>();
		for (Section<? extends PackageInclude> packInclude : packageIncludes) {
			String tempPackage = packInclude.get().getPackageToInclude(packInclude);
			if (tempPackage != null) includedPackages.add(tempPackage);
		}
		return Collections.unmodifiableSet(includedPackages);
	}

	public void updatePackageIncludes(KnowWEArticle article) {
		List<String> articlesToRevise = new ArrayList<String>();

		for (HashSet<Section<? extends PackageInclude>> includesSet : packageIncludesMap.values()) {
			for (Section<? extends PackageInclude> packInclude : includesSet) {
				if (changedPackages.contains(packInclude.get().getPackageToInclude(packInclude))
						&& !article.getTitle().equals(
								packInclude.get().getPackageToInclude(packInclude))) {
					articlesToRevise.add(packInclude.getTitle());
				}
			}
		}

		changedPackages.clear();

		KnowWEEnvironment env = KnowWEEnvironment.getInstance();
		for (String title : articlesToRevise) {
			KnowWEArticle newArt = KnowWEArticle.createArticle(
					env.getArticle(article.getWeb(), title).getSection().getOriginalText(), title,
					env.getRootType(), web, false);

			env.getArticleManager(web).saveUpdatedArticle(newArt);
		}
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>(1);
		events.add(FullParseEvent.class);
		return events;
	}

	@Override
	public void notify(Event event, String web, String username, Section<? extends KnowWEObjectType> s) {
		cleanForArticle(s.getArticle());
	}

}
