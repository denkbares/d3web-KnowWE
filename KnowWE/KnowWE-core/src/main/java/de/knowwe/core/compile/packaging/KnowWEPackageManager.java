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

package de.knowwe.core.compile.packaging;

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

import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.event.FullParseEvent;
import de.knowwe.event.PreCompileFinishedEvent;
import de.knowwe.event.UpdatingDependenciesEvent;

public class KnowWEPackageManager implements EventListener {

	public static final String PACKAGE_ATTRIBUTE_NAME = "package";

	public static final String DEFAULT_PACKAGE = "default";

	public static final String THIS = "this";

	private final String web;

	/**
	 * For each packageName, you get all Sections in the wiki belonging to this
	 * packageName.
	 */
	private final Map<String, LinkedList<Section<?>>> packagesMap =
			new HashMap<String, LinkedList<Section<?>>>();

	private final Map<String, Map<String, Set<Section<?>>>> deactivatedSectionsMap =
			new HashMap<String, Map<String, Set<Section<?>>>>();

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
		return packageName.equals(THIS);
	}

	private void addDeactivatedSection(String packageName, Section<?> s) {
		Map<String, Set<Section<?>>> map = deactivatedSectionsMap.get(packageName);
		if (map == null) {
			map = new HashMap<String, Set<Section<?>>>();
			deactivatedSectionsMap.put(packageName, map);
		}
		Set<Section<?>> set = map.get(s.getTitle());
		if (set == null) {
			set = new HashSet<Section<?>>();
			map.put(s.getTitle(), set);
		}
		set.add(s);
	}

	private Set<Section<?>> getDeactivatedSections(String packageName) {
		Set<Section<?>> all = new HashSet<Section<?>>();
		Map<String, Set<Section<?>>> map = deactivatedSectionsMap.get(packageName);
		if (map != null) {
			for (Set<Section<?>> set : map.values()) {
				all.addAll(set);
			}
		}
		return all;
	}

	private Set<Section<?>> getDeactivatedSections(String packageName, String title) {
		Map<String, Set<Section<?>>> map = deactivatedSectionsMap.get(packageName);
		if (map != null) {
			Set<Section<?>> set = map.get(title);
			if (set != null) return set;
		}
		return new HashSet<Section<?>>(0);
	}

	/**
	 * Adds the given Section to the package with the given name.
	 * 
	 * @created 28.12.2010
	 * @param s is the Section to add
	 * @param packageName is the name of the package the Section is added to
	 * @returns KDOMReportMessages, if something went wrong while adding the
	 *          Section
	 */
	public void addSectionToPackage(Section<?> s, String packageName) {

		if (packageName == null || packageName.trim().isEmpty()) {
			if (s.getPackageNames().isEmpty()) {
				packageName = DEFAULT_PACKAGE;
			}
			else {
				return;
			}
		}
		if (isDisallowedPackageName(packageName)) {
			Messages.storeMessage(null, s, this.getClass(), Messages.error("'"
					+ packageName
					+ "' is not allowed as a package name."));
			return;
		}
		if (s.getPackageNames().contains(packageName)) {
			Messages.storeMessage(null, s, this.getClass(), Messages.error(
					"This Section is added to " +
							"the package '" + packageName + "' multiple times."));
			addDeactivatedSection(packageName, s);
		}
		else {
			s.addPackageName(packageName);
		}
		List<Section<?>> sectionsOfPackage = getSectionsOfPackage(packageName);
		for (Section<?> sectionOfPackage : sectionsOfPackage) {
			if (sectionOfPackage.equalsOrIsSuccessorOf(s)) {
				Messages.storeMessage(null, sectionOfPackage, this.getClass(),
						Messages.error("This Section is added to " +
								"the package '" + packageName + "' multiple times."));
				addDeactivatedSection(packageName, sectionOfPackage);
				sectionOfPackage.removePackageName(packageName);
			}
		}
		LinkedList<Section<?>> packageList = packagesMap.get(packageName);
		if (packageList == null) {
			packageList = new LinkedList<Section<?>>();
			packagesMap.put(packageName, packageList);
		}
		packageList.add(s);
		changedPackages.add(packageName);
	}

	/**
	 * Removes the given Section from the package with the given name.
	 * 
	 * @created 28.12.2010
	 * @param s is the Section to remove
	 * @param packageName is the name of the package from which the section is
	 *        removed
	 * @returns whether the Section was removed
	 */
	public boolean removeSectionFromPackage(Section<?> s, String packageName) {
		if (!isDisallowedPackageName(packageName)) {
			LinkedList<Section<?>> packageList = packagesMap.get(packageName);
			if (packageList != null) {
				boolean removed = packageList.remove(s);
				if (removed) {
					changedPackages.add(packageName);
					s.removePackageName(packageName);
					// reactivate deactivated sections
					Set<Section<?>> deactivatedSections = getDeactivatedSections(packageName,
							s.getTitle());
					if (!deactivatedSections.remove(s)) {
						// if the section was itself deactivated, it can not
						// reactivate other sections
						List<Section<?>> notLongerDeactivated = new ArrayList<Section<?>>(
								deactivatedSections.size());
						for (Section<?> dSec : deactivatedSections) {
							if (!dSec.getPackageNames().contains(packageName)) {
								notLongerDeactivated.add(dSec);
								Messages.storeMessages(null, dSec, this.getClass(),
										new ArrayList<Message>());
								dSec.addPackageName(packageName);
							}
						}
						deactivatedSections.removeAll(notLongerDeactivated);
					}
				}
				if (packageList.isEmpty()) {
					packagesMap.remove(packageName);
				}
				return removed;
			}
		}
		return false;
	}

	/**
	 * Removes the given Section from all packages it was added to.
	 * 
	 * @created 28.12.2010
	 * @param s is the Section to remove
	 * @param packageName is the name of the package from which the section is
	 *        removed
	 */
	public void removeSectionFromAllPackages(Section<?> s) {
		for (String packageName : s.getPackageNames()) {
			removeSectionFromPackage(s, packageName);
		}
	}

	public void cleanForArticle(KnowWEArticle article) {
		for (LinkedList<Section<?>> list : new ArrayList<LinkedList<Section<?>>>(
				packagesMap.values())) {
			List<Section<?>> sectionsToRemove = new ArrayList<Section<?>>();
			for (Section<?> sec : list) {
				if (sec.getTitle().equals(article.getTitle())) {
					sectionsToRemove.add(sec);
				}
			}
			for (Section<?> sec : sectionsToRemove) {
				removeSectionFromAllPackages(sec);
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

	public List<Section<?>> getSectionsOfPackage(String packageName) {
		LinkedList<Section<?>> sectionsOfPackage = packagesMap.get(packageName);
		if (sectionsOfPackage != null) {
			Set<Section<?>> deactivatedSections = getDeactivatedSections(packageName);
			ArrayList<Section<?>> cleanedSections = new ArrayList<Section<?>>(
					sectionsOfPackage.size());
			for (Section<?> s : sectionsOfPackage) {
				if (!deactivatedSections.contains(s)) {
					cleanedSections.add(s);
				}
			}
			Collections.sort(cleanedSections);
			return Collections.unmodifiableList(new ArrayList<Section<?>>(cleanedSections));
		}
		else return Collections.unmodifiableList(new ArrayList<Section<?>>(0));
	}

	/**
	 * Override the autocompile-article-switch of KnowWE_config. This should
	 * only be used during unit tests!
	 * 
	 * @param autocompileArticleEnabled whether autocompile of articles should
	 *        be enabled or disabled
	 * @created 12.10.2010
	 */
	public static void overrideAutocompileArticle(boolean autocompileArticleEnabled) {
		KnowWEPackageManager.autocompileArticleEnabled = autocompileArticleEnabled;
	}

	/**
	 * Returns whether autocompiling of articles is enabled or not.
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

						List<Section<?>> sectionsOfPackage;
						if (packageToReferTo.equals(article.getTitle())
								|| (packageToReferTo.equals(THIS))) {
							sectionsOfPackage = new ArrayList<Section<?>>(1);
							sectionsOfPackage.add(article.getSection());
						}
						else {
							sectionsOfPackage = getSectionsOfPackage(packageToReferTo);
						}
						for (Section<?> oackSection : sectionsOfPackage) {
							oackSection.setReusedByRecursively(article.getTitle(), false);
							Messages.clearMessagesRecursively(article, oackSection);
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

	public Set<String> getAllPackageNames() {
		return Collections.unmodifiableSet(packagesMap.keySet());
	}

	/**
	 * Returns all articles that refer to the package with the given name.
	 * 
	 * @created 28.08.2010
	 * @param packageName is the name of the package
	 * @return a Set of articles referring to the package with the given name.
	 */
	public Set<String> getArticlesReferringTo(String packageName) {
		Set<String> matchingArticles = new HashSet<String>();
		for (String article : referencedPackagesMap.keySet()) {
			if (referencedPackagesMap.get(article).contains(packageName)) {
				matchingArticles.add(article);
			}
		}
		return Collections.unmodifiableSet(matchingArticles);
	}

	/**
	 * Returns all articles, that refer to the given Section via packages.
	 * 
	 * @created 28.12.2010
	 * @param section
	 * @return
	 */
	public Set<String> getArticlesReferringTo(Section<?> section) {
		Set<String> matchingArticles = new HashSet<String>();
		for (String packageName : section.getPackageNames()) {
			matchingArticles.addAll(getArticlesReferringTo(packageName));
		}
		HashSet<String> referencedPackages = referencedPackagesMap.get(section.getTitle());
		if (autocompileArticleEnabled
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
		// maybe only do this for changed sections of the package... take care
		// of multiuser-scenarios
		for (LinkedList<Section<?>> sectionsOfPackageList : packagesMap.values()) {
			for (Section<?> sectionOfPackage : sectionsOfPackageList) {
				if (sectionOfPackage.getTitle().equals(article.getTitle())) {
					Set<String> articlesReferringTo = getArticlesReferringTo(sectionOfPackage);
					LinkedList<Section<?>> nodes = new LinkedList<Section<?>>();
					Sections.getAllNodesPostOrder(sectionOfPackage, nodes);
					for (Section<?> node : nodes) {
						if (node.get().isIgnoringPackageCompile()) continue;
						for (String title : new LinkedList<String>(node.getReusedBySet())) {
							if (!articlesReferringTo.contains(title)) {
								node.setReusedBy(title, false);
								Messages.clearMessages(article, node);
							}
						}
					}
				}
			}
		}
	}

	public void updateReferringArticles(KnowWEArticle article) {

		for (HashSet<Section<? extends PackageReference>> referencesSet : packageReferenceMap.values()) {
			for (Section<? extends PackageReference> packReference : referencesSet) {
				List<String> packagesToReferTo = packReference.get().getPackagesToReferTo(
						packReference);
				for (String packagName : packagesToReferTo) {
					if (changedPackages.contains(packagName)
							&& !article.getTitle().equals(packagName)) {
						KnowWEEnvironment.getInstance().getArticleManager(article.getWeb()).addArticleToUpdate(
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
			updateReferringArticles(((UpdatingDependenciesEvent) event).getArticle());
		}
		else if (event instanceof PreCompileFinishedEvent) {
			updateReusedStates(((PreCompileFinishedEvent) event).getArticle());
		}
	}

}
