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

	private final Map<String, HashSet<Section<? extends PackageReference>>> packageReferencesMap = new HashMap<String, HashSet<Section<? extends PackageReference>>>();

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
		for (String packageName : s.getPackageNames()) {

			if (packageName.equals(s.getTitle())) continue;

			LinkedList<Section<?>> packageList = packageDefinitionsMap.get(packageName);
			if (packageList != null) {
				boolean removed = packageList.remove(s);
				if (removed) {
					changedPackages.add(packageName);

					// set reused = false for all articles that don't compile
					// this Section any longer
					Set<String> includingArticles = getArticlesReferingTo(packageName);
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
		for (HashSet<Section<? extends PackageReference>> set : new ArrayList<HashSet<Section<? extends PackageReference>>>(
				packageReferencesMap.values())) {
			List<Section<? extends PackageReference>> sectionsToRemove =
					new ArrayList<Section<? extends PackageReference>>();
			for (Section<? extends PackageReference> sec : set) {
				if (sec.getTitle().equals(article.getTitle())) sectionsToRemove.add(sec);
			}
			for (Section<? extends PackageReference> sec : sectionsToRemove) {
				unregisterPackageReference(article, sec);
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

	public void registerPackageReference(KnowWEArticle article, Section<? extends PackageReference> s) {
		
		if (s.get().getPackagesToReferTo(s).contains(article.getTitle())
				&& !getPackageReferences(article).contains(s)) {
			if (!AUTOCOMPILE_ARTICLE) {

				// If the PackageReference aims at the article it is defined in
				// and autocompile is deactivated, the reused-flags need to be
				// reset. If for example only the PackageReference was added to
				// the article and all other Sections could be reused, all these
				// reused-flags are set to true although no SubtreeHandlers have
				// created yet.

				Set<String> referencedPackages = getReferencedPackages(article);

				Set<Section<?>> alreadyReferenced = new HashSet<Section<?>>();
				for (String incPack : referencedPackages) {
					List<Section<?>> packDefs = getPackageDefinitions(incPack);
					for (Section<?> packDef : packDefs) {
						List<Section<?>> tempNodes = new LinkedList<Section<?>>();
						packDef.getAllNodesPostOrder(tempNodes);
						alreadyReferenced.addAll(tempNodes);
					}
				}
				List<Section<?>> allNodesPostOrder = article.getAllNodesPostOrder();
				for (Section<?> node : allNodesPostOrder) {
					if (!alreadyReferenced.contains(node)) {
						node.setReusedBy(article.getTitle(), false);
					}
				}
			}
			else {
				return;
			}
		}
		
		HashSet<Section<? extends PackageReference>> packageReferences =
				packageReferencesMap.get(article.getTitle());
		if (packageReferences == null) {
			packageReferences = new HashSet<Section<? extends PackageReference>>(4);
			packageReferencesMap.put(article.getTitle(), packageReferences);
		}
		packageReferences.add(s);
	}

	public boolean unregisterPackageReference(KnowWEArticle article, Section<? extends PackageReference> s) {
		Set<Section<? extends PackageReference>> packageReferences = packageReferencesMap.get(article.getTitle());

		if (packageReferences != null) {
			boolean removed = packageReferences.remove(s);
			if (removed) {
				// set all Sections that were referenced by this
				// PackageReference to reused = false for the given article
				Collection<String> referencedPackages = s.get().getPackagesToReferTo(s);
				for (String referencedPackage : referencedPackages) {
					boolean stillReferenced = false;
					for (Section<? extends PackageReference> packReference : packageReferences) {
						if (packReference.get().getPackagesToReferTo(packReference).contains(
								referencedPackage)) {
							stillReferenced = true;
							break;
						}
					}
					// but don't set to false, if the package is still
					// referenced in another PackageReference in this
					// article
					if (!stillReferenced) {
						List<Section<?>> packageDefinitions;
						if (referencedPackage.equals(article.getTitle())) {
							packageDefinitions = new ArrayList<Section<?>>(1);
							packageDefinitions.add(article.getSection());
						}
						else {
							packageDefinitions = getPackageDefinitions(referencedPackage);
						}
						for (Section<?> packageDef : packageDefinitions) {
							packageDef.setReusedStateRecursively(article.getTitle(), false);
							KnowWEUtils.clearMessagesRecursively(article, packageDef);
						}
					}
				}
			}
			if (packageReferences.isEmpty()) packageReferencesMap.remove(article.getTitle());
			return removed;
		}
		return false;
	}

	/**
	 * Returns all articles that refer to the given packageName.
	 * 
	 * @created 28.08.2010
	 * @param packageName
	 * @return
	 */
	public Set<String> getArticlesReferingTo(String packageName) {
		Set<String> matchingArticles = new HashSet<String>();
		for (String article : packageReferencesMap.keySet()) {
			for (Section<? extends PackageReference> packageReference : packageReferencesMap.get(article)) {
				if (packageReference.get().getPackagesToReferTo(packageReference).contains(
						packageName)) {
					matchingArticles.add(article);
				}
			}
		}
		return Collections.unmodifiableSet(matchingArticles);
	}

	/**
	 * Returns all Packages the given article refers to in his Sections of the
	 * type PackageReference.
	 * 
	 * @created 29.08.2010
	 * @param article
	 * @return
	 */
	public Set<String> getReferencedPackages(KnowWEArticle article) {
		Set<Section<? extends PackageReference>> packageReferences = getPackageReferences(article);
		HashSet<String> referencedPackages = new HashSet<String>();
		for (Section<? extends PackageReference> packReference : packageReferences) {
			List<String> tempPackages = packReference.get().getPackagesToReferTo(packReference);
			if (tempPackages != null) referencedPackages.addAll(tempPackages);
		}
		return Collections.unmodifiableSet(referencedPackages);
	}

	/**
	 * Returns all Sections of the type PackageReference of the given article.
	 * 
	 * @created 29.08.2010
	 * @param article
	 * @return
	 */
	public Set<Section<? extends PackageReference>> getPackageReferences(KnowWEArticle article) {
		Set<Section<? extends PackageReference>> packageReferences = packageReferencesMap.get(article.getTitle());
		return packageReferences == null
				? Collections.unmodifiableSet(new HashSet<Section<? extends PackageReference>>(0))
				: Collections.unmodifiableSet(new HashSet<Section<? extends PackageReference>>(
						packageReferences));
	}

	public void updatePackageReferences(KnowWEArticle article) {
		List<String> articlesToRevise = new ArrayList<String>();

		for (HashSet<Section<? extends PackageReference>> referencesSet : packageReferencesMap.values()) {
			for (Section<? extends PackageReference> packReference : referencesSet) {
				List<String> packagesToReference = packReference.get().getPackagesToReferTo(
						packReference);
				for (String pack : packagesToReference) {
					if (changedPackages.contains(pack) && !article.getTitle().equals(pack)) {
						articlesToRevise.add(packReference.getTitle());
					}
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
