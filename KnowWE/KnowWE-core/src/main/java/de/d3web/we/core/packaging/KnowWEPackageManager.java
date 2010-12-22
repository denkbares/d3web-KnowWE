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
import de.d3web.we.event.PreCompileFinishedEvent;
import de.d3web.we.event.UpdatingDependenciesEvent;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.utils.KnowWEUtils;

public class KnowWEPackageManager implements EventListener {

	public static final String ATTRIBUTE_NAME = "package";

	public static final String DEFAULT_PACKAGE = "default";

	public static final String THIS = "this";

	private final String web;

	/**
	 * For each packageName, you get all Sections in the wiki belonging to this
	 * packageName.
	 */
	private final Map<String, LinkedList<Section<?>>> packageDefinitionsMap =
			new HashMap<String, LinkedList<Section<?>>>();

	/**
	 * For each article, you get all Sections of type PackageReference defined
	 * in this article.
	 */
	private final Map<String, HashSet<Section<? extends PackageReference>>> packageReferenceMap =
			new HashMap<String, HashSet<Section<? extends PackageReference>>>();

	/**
	 * For each article, you get all packageNames referenced in this article by
	 * Sections of the type PackageReference.
	 */
	private final Map<String, HashSet<String>> referencedPackagesMap =
			new HashMap<String, HashSet<String>>();

	private final Set<String> changedPackages = new HashSet<String>();

	private static boolean autocompileArticleEnabled = ResourceBundle.getBundle("KnowWE_config").getString(
			"packaging.autocompileArticle").contains("true");

	public KnowWEPackageManager(String web) {
		this.web = web;
		EventManager.getInstance().registerListener(this);
	}

	public String getWeb() {
		return web;
	}

	private boolean isDisallowedPackageName(String packageName) {
		return packageName.equals(THIS)
				|| KnowWEEnvironment.getInstance().getArticleManager(
						web).getTitles().contains(packageName);
	}

	public Collection<KDOMReportMessage> registerPackageDefinition(Section<?> s) {
		List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
		for (String packageName : s.getPackageNames()) {
			if (isDisallowedPackageName(packageName)) {
				msgs.add(new DisallowedPackageNameError(packageName));
				continue;
			}
			LinkedList<Section<?>> packageList = packageDefinitionsMap.get(packageName);
			if (packageList == null) {
				packageList = new LinkedList<Section<?>>();
				packageDefinitionsMap.put(packageName, packageList);
			}
			packageList.add(s);
			changedPackages.add(packageName);
		}
		return msgs;
	}

	public boolean unregisterPackageDefinition(Section<?> s) {
		for (String packageName : s.getPackageNames()) {
			if (isDisallowedPackageName(packageName)) {
				continue;
			}
			LinkedList<Section<?>> packageList = packageDefinitionsMap.get(packageName);
			if (packageList != null) {
				boolean removed = packageList.remove(s);
				if (removed) {
					changedPackages.add(packageName);
				}
				if (packageList.isEmpty()) {
					packageDefinitionsMap.remove(packageName);
				}
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
				if (sec.getTitle().equals(article.getTitle())) {
					sectionsToRemove.add(sec);
				}
			}
			for (Section<?> sec : sectionsToRemove) {
				unregisterPackageDefinition(sec);
			}
		}
		for (HashSet<Section<? extends PackageReference>> set : new ArrayList<HashSet<Section<? extends PackageReference>>>(
				packageReferenceMap.values())) {
			List<Section<? extends PackageReference>> sectionsToRemove =
					new ArrayList<Section<? extends PackageReference>>();
			for (Section<? extends PackageReference> sec : set) {
				if (sec.getTitle().equals(article.getTitle())) {
					sectionsToRemove.add(sec);
				}
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
		} else
			return Collections.unmodifiableList(new ArrayList<Section<?>>(0));
	}

	/**
	 * Override the Autocompile-article switch of KnowWE_config. This should
	 * only be used during unittests!
	 * 
	 * @param autocompileArticleEnabled whether autocompile of articles should
	 *        be enabled or disabled
	 * @created 12.10.2010
	 */
	public static void overrideAutocompileArticle(boolean autocompileArticleEnabled) {
		KnowWEPackageManager.autocompileArticleEnabled = autocompileArticleEnabled;
	}

	/**
	 * Returns whether Autocompiling of articles is enabled or not.
	 * 
	 * @created 12.10.2010
	 * @return
	 */
	public static boolean isAutocompileArticleEnabled() {
		return autocompileArticleEnabled;
	}

	public void registerPackageReference(KnowWEArticle article, Section<? extends PackageReference> s) {
		
		List<String> packagesToReferTo = s.get().getPackagesToReferTo(s);

		HashSet<Section<? extends PackageReference>> packageReferences =
				packageReferenceMap.get(article.getTitle());
		if (packageReferences == null) {
			packageReferences = new HashSet<Section<? extends PackageReference>>(4);
			packageReferenceMap.put(article.getTitle(), packageReferences);
		}
		packageReferences.add(s);

		HashSet<String> referencedPackages =
				referencedPackagesMap.get(article.getTitle());
		if (referencedPackages == null) {
			referencedPackages = new HashSet<String>();
			referencedPackagesMap.put(article.getTitle(), referencedPackages);
		}
		for (String packageToReferTo : packagesToReferTo) {
			referencedPackages.add(packageToReferTo);
		}
	}

	public boolean unregisterPackageReference(KnowWEArticle article, Section<? extends PackageReference> s) {

		Set<Section<? extends PackageReference>> packageReferences = packageReferenceMap.get(article.getTitle());

		if (packageReferences != null) {
			boolean removed = packageReferences.remove(s);
			if (removed) {
				// set all Sections that were referenced by this
				// PackageReference to reused = false for the given article
				Collection<String> packagesToReferTo = s.get().getPackagesToReferTo(s);
				for (String packageToReferTo : packagesToReferTo) {
					boolean stillReferenced = false;
					for (Section<? extends PackageReference> packReference : packageReferences) {
						if (packReference.get().getPackagesToReferTo(packReference).contains(
								packageToReferTo)) {
							stillReferenced = true;
							break;
						}
					}
					// but don't set to false, if the package is still
					// referenced in another PackageReference in this
					// article
					if (!stillReferenced) {
						// also remove package from referencedPackagesMap
						referencedPackagesMap.get(article.getTitle()).remove(packageToReferTo);

						List<Section<?>> packageDefinitions;
						if (packageToReferTo.equals(article.getTitle())
								|| (packageToReferTo.equals(THIS))) {
							packageDefinitions = new ArrayList<Section<?>>(1);
							packageDefinitions.add(article.getSection());
						}
						else {
							packageDefinitions = getPackageDefinitions(packageToReferTo);
						}
						for (Section<?> packageDef : packageDefinitions) {
							packageDef.setReusedByRecursively(article.getTitle(), false);
							KnowWEUtils.clearMessagesRecursively(article, packageDef);
						}
					}
				}
			}
			if (packageReferences.isEmpty()) {
				packageReferenceMap.remove(article.getTitle());
			}
			if (referencedPackagesMap.get(article.getTitle()).isEmpty()) {
				referencedPackagesMap.remove(article.getTitle());
			}
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
		for (String article : referencedPackagesMap.keySet()) {
			if (referencedPackagesMap.get(article).contains(packageName)) {
				matchingArticles.add(article);
			}
		}
		return Collections.unmodifiableSet(matchingArticles);
	}

	public Set<String> getArticlesReferingTo(Section<?> section) {
		Set<String> matchingArticles = new HashSet<String>();
		for (String packageName : section.getPackageNames()) {
			matchingArticles.addAll(getArticlesReferingTo(packageName));
		}
		HashSet<String> referencedPackages = referencedPackagesMap.get(section.getTitle());
		if (autocompileArticleEnabled
				|| (referencedPackages != null && referencedPackages.contains(section.getTitle()))
				|| (referencedPackages != null && referencedPackages.contains(THIS))) {
			matchingArticles.add(section.getTitle());
		}
		return matchingArticles;
	}

	/**
	 * Returns all Packages the given article refers to in his Sections of the
	 * type PackageReference.
	 * 
	 * @created 29.08.2010
	 * @param article
	 * @return
	 */
	public Set<String> getReferencedPackages(String title) {
		Set<String> referencedPackages = referencedPackagesMap.get(title);
		return referencedPackages == null
				? Collections.unmodifiableSet(new HashSet<String>(0))
				: Collections.unmodifiableSet(new HashSet<String>(
						referencedPackages));
	}

	/**
	 * Returns all Sections of the type PackageReference of the given article.
	 * 
	 * @created 29.08.2010
	 * @param article
	 * @return
	 */
	public Set<Section<? extends PackageReference>> getPackageReferences(KnowWEArticle article) {
		Set<Section<? extends PackageReference>> packageReferences = packageReferenceMap.get(article.getTitle());
		return packageReferences == null
				? Collections.unmodifiableSet(new HashSet<Section<? extends PackageReference>>(0))
				: Collections.unmodifiableSet(new HashSet<Section<? extends PackageReference>>(
						packageReferences));
	}

	public void updateReusedStates(KnowWEArticle article) {
		// TODO: not that fast... probably use own map sorted by article
		// maybe only do this for changed PackageDefs... take care of
		// multiuser-scenarios
		for (LinkedList<Section<?>> packageDefList : packageDefinitionsMap.values()) {
			for (Section<?> packageDef : packageDefList) {
				if (packageDef.getTitle().equals(article.getTitle())) {
					Set<String> articlesReferingTo = getArticlesReferingTo(packageDef);
					LinkedList<Section<?>> nodes = new LinkedList<Section<?>>();
					packageDef.getAllNodesPostOrder(nodes);
					for (Section<?> node : nodes) {
						if (node.get().isIgnoringPackageCompile()) continue;
						for (String title : new LinkedList<String>(node.getReusedBySet())) {
							if (!articlesReferingTo.contains(title)) {
								node.setReusedBy(title, false);
								KnowWEUtils.clearMessages(article.getWeb(), title, node.getID());
							}
						}
					}
				}
			}
		}
	}

	public void updateReferingArticles(KnowWEArticle article) {

		for (HashSet<Section<? extends PackageReference>> referencesSet : packageReferenceMap.values()) {
			for (Section<? extends PackageReference> packReference : referencesSet) {
				List<String> packagesToReference = packReference.get().getPackagesToReferTo(
						packReference);
				for (String packagName : packagesToReference) {
					if (changedPackages.contains(packagName) && !article.getTitle().equals(packagName)) {
						KnowWEEnvironment.getInstance().getArticleManager(article.getWeb()).addArticleToRefresh(
								packReference.getTitle());
					}
				}
			}
		}

		changedPackages.clear();
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>(3);
		events.add(FullParseEvent.class);
		events.add(UpdatingDependenciesEvent.class);
		events.add(PreCompileFinishedEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		if (event instanceof FullParseEvent) {
			cleanForArticle(((FullParseEvent) event).getArticle());
		}
		else if (event instanceof UpdatingDependenciesEvent) {
			updateReferingArticles(((UpdatingDependenciesEvent) event).getArticle());
		}
		else if (event instanceof PreCompileFinishedEvent) {
			updateReusedStates(((PreCompileFinishedEvent) event).getArticle());
		}
	}

	class DisallowedPackageNameError extends KDOMError {

		private final String packageName;

		public DisallowedPackageNameError(String packageName) {
			this.packageName = packageName;
		}

		@Override
		public String getVerbalization() {
			return "'" + packageName + "' is not allowed as a package name.";
		}

	}



}
