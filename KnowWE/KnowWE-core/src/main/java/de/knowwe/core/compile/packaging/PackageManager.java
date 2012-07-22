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
import java.util.Set;

import de.knowwe.core.Environment;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.event.FullParseEvent;
import de.knowwe.event.PreCompileFinishedEvent;
import de.knowwe.event.UpdatingDependenciesEvent;

public class PackageManager implements EventListener {

	public static final String PACKAGE_ATTRIBUTE_NAME = "package";

	public static final String THIS = "this";

	private static final String DEFAULT_PACKAGE = "default";

	private final String web;

	/**
	 * For each article title, you get all default packages used in this
	 * article.
	 */
	private final Map<String, HashSet<String>> articleToDefaultPackages =
			new HashMap<String, HashSet<String>>();

	/**
	 * For each packageName, you get all Sections in the wiki belonging to this
	 * packageName.
	 */
	private final Map<String, LinkedList<Section<?>>> packageToSectionsOfPackage =
			new HashMap<String, LinkedList<Section<?>>>();

	private final Map<String, Map<String, Set<Section<?>>>> deactivatedSectionsMap =
			new HashMap<String, Map<String, Set<Section<?>>>>();

	/**
	 * For each article title, you get all Sections of type
	 * {@link PackageManager} defined in this article.
	 */
	private final Map<String, HashSet<Section<? extends PackageCompiler>>> articleToPackageCompileSections =
			new HashMap<String, HashSet<Section<? extends PackageCompiler>>>();

	/**
	 * For each article title, you get all packages compiled in this article by
	 * Sections of the type {@link PackageCompiler}.
	 */
	private final Map<String, HashSet<String>> articleToCompiledPackages =
			new HashMap<String, HashSet<String>>();

	/**
	 * For each package, you get all articles compiling this package.
	 */
	private final Map<String, HashSet<String>> packageToCompilingArticles =
			new HashMap<String, HashSet<String>>();

	private final Set<String> changedPackages = new HashSet<String>();

	private static boolean autocompileArticleEnabled = KnowWEUtils.getConfigBundle().getString(
			"packaging.autocompileArticle").contains("true");

	public PackageManager(String web) {
		this.web = web;
		EventManager.getInstance().registerListener(this);
	}

	public String getWeb() {
		return web;
	}

	private boolean isDisallowedPackageName(String packageName) {
		return packageName == null || packageName.isEmpty() || packageName.equals(THIS);
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

	public void addDefaultPackage(Article article, String defaultPackage) {
		HashSet<String> defaultPackages = articleToDefaultPackages.get(article.getTitle());
		if (defaultPackages == null) {
			defaultPackages = new HashSet<String>(4);
			articleToDefaultPackages.put(article.getTitle(), defaultPackages);
		}
		defaultPackages.add(defaultPackage);
	}

	public Set<String> getDefaultPackages(Article article) {
		HashSet<String> defaultPackages = articleToDefaultPackages.get(article.getTitle());
		if (defaultPackages == null) {
			defaultPackages = new HashSet<String>(4);
			defaultPackages.add(DEFAULT_PACKAGE);
		}
		return Collections.unmodifiableSet(defaultPackages);
	}

	/**
	 * Adds the given Section to the package with the given name.
	 * 
	 * @created 28.12.2010
	 * @param section is the Section to add
	 * @param packageName is the name of the package the Section is added to
	 * @returns KDOMReportMessages, if something went wrong while adding the
	 *          Section
	 */
	public void addSectionToPackage(Section<?> section, String packageName) {

		if (isDisallowedPackageName(packageName)) {
			Messages.storeMessage(null, section, this.getClass(), Messages.error("'"
					+ packageName
					+ "' is not allowed as a package name."));
			return;
		}
		if (section.getPackageNames().contains(packageName)) {
			Messages.storeMessage(null, section, this.getClass(), Messages.error(
					"This Section is added to " +
							"the package '" + packageName + "' multiple times."));
			addDeactivatedSection(packageName, section);
		}
		else {
			section.addPackageName(packageName);
		}
		List<Section<?>> sectionsOfPackage = getSectionsOfPackage(packageName);
		for (Section<?> sectionOfPackage : sectionsOfPackage) {
			if (sectionOfPackage.equalsOrIsSuccessorOf(section)) {
				Messages.storeMessage(null, sectionOfPackage, this.getClass(),
						Messages.error("This Section is added to " +
								"the package '" + packageName + "' multiple times."));
				addDeactivatedSection(packageName, sectionOfPackage);
				sectionOfPackage.removePackageName(packageName);
			}
		}
		LinkedList<Section<?>> packageList = packageToSectionsOfPackage.get(packageName);
		if (packageList == null) {
			packageList = new LinkedList<Section<?>>();
			packageToSectionsOfPackage.put(packageName, packageList);
		}
		packageList.add(section);
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
			LinkedList<Section<?>> packageList = packageToSectionsOfPackage.get(packageName);
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
					packageToSectionsOfPackage.remove(packageName);
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

	public void cleanForArticle(Article article) {
		for (LinkedList<Section<?>> list : new ArrayList<LinkedList<Section<?>>>(
				packageToSectionsOfPackage.values())) {
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
		for (HashSet<Section<? extends PackageCompiler>> set : new ArrayList<HashSet<Section<? extends PackageCompiler>>>(
				articleToPackageCompileSections.values())) {
			List<Section<? extends PackageCompiler>> sectionsToRemove =
					new ArrayList<Section<? extends PackageCompiler>>();
			for (Section<? extends PackageCompiler> sec : set) {
				if (sec.getTitle().equals(article.getTitle())) {
					sectionsToRemove.add(sec);
				}
			}
			for (Section<? extends PackageCompiler> sec : sectionsToRemove) {
				unregisterPackageCompileSection(article, sec);
			}
		}
		// remove this last so getDefaultPackages correctly works while cleanup
		// and unregister
		articleToDefaultPackages.remove(article.getTitle());
	}

	public List<Section<?>> getSectionsOfPackage(String packageName) {
		LinkedList<Section<?>> sectionsOfPackage = packageToSectionsOfPackage.get(packageName);
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
		PackageManager.autocompileArticleEnabled = autocompileArticleEnabled;
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

	public void registerPackageCompileSection(Article article, Section<? extends PackageCompiler> s) {

		Set<String> packagesToCompile = s.get().getPackagesToCompile(s);

		HashSet<Section<? extends PackageCompiler>> packageCompileSections =
				articleToPackageCompileSections.get(article.getTitle());
		if (packageCompileSections == null) {
			packageCompileSections = new HashSet<Section<? extends PackageCompiler>>(4);
			articleToPackageCompileSections.put(article.getTitle(), packageCompileSections);
		}
		packageCompileSections.add(s);

		HashSet<String> compiledPackages =
				articleToCompiledPackages.get(article.getTitle());
		if (compiledPackages == null) {
			compiledPackages = new HashSet<String>();
			articleToCompiledPackages.put(article.getTitle(), compiledPackages);
		}
		for (String packageToCompile : packagesToCompile) {
			compiledPackages.add(packageToCompile);

			HashSet<String> compilingArticles = packageToCompilingArticles.get(packageToCompile);
			if (compilingArticles == null) {
				compilingArticles = new HashSet<String>();
				packageToCompilingArticles.put(packageToCompile, compilingArticles);
			}
			compilingArticles.add(article.getTitle());
		}

	}

	public boolean unregisterPackageCompileSection(Article article, Section<? extends PackageCompiler> s) {

		Set<Section<? extends PackageCompiler>> packageCompileSections = articleToPackageCompileSections.get(article.getTitle());

		if (packageCompileSections != null) {

			HashSet<String> compiledPackages = articleToCompiledPackages.get(article.getTitle());

			boolean removed = packageCompileSections.remove(s);
			if (removed) {
				// set all Sections that were referenced by this
				// PackageCompiler to reused = false for the given article
				Collection<String> packagesToCompile = s.get().getPackagesToCompile(s);
				for (String packageToCompile : packagesToCompile) {
					boolean stillCompiled = false;
					for (Section<? extends PackageCompiler> packageCompileSection : packageCompileSections) {
						if (packageCompileSection.get().getPackagesToCompile(packageCompileSection).contains(
								packageToCompile)) {
							stillCompiled = true;
							break;
						}
					}
					// but don't set to false, if the package is still
					// compiled via another Section of type PackageCompiler in
					// this article
					if (!stillCompiled) {
						// remove map entries
						compiledPackages.remove(packageToCompile);
						HashSet<String> compilingArticles = packageToCompilingArticles.get(packageToCompile);
						compilingArticles.remove(article.getTitle());

						// cleanup empty set
						if (compilingArticles.isEmpty()) {
							packageToCompilingArticles.remove(packageToCompile);
						}

						List<Section<?>> sectionsOfPackage;
						if (packageToCompile.equals(article.getTitle())
								|| (packageToCompile.equals(THIS))) {
							sectionsOfPackage = new ArrayList<Section<?>>(1);
							sectionsOfPackage.add(article.getRootSection());
						}
						else {
							sectionsOfPackage = getSectionsOfPackage(packageToCompile);
						}
						for (Section<?> oackSection : sectionsOfPackage) {
							oackSection.setReusedByRecursively(article.getTitle(), false);
							Messages.clearMessagesRecursively(article, oackSection);
						}
					}
				}
			}
			// cleanup empty sets
			if (packageCompileSections.isEmpty()) {
				articleToPackageCompileSections.remove(article.getTitle());
			}
			if (compiledPackages.isEmpty()) {
				articleToCompiledPackages.remove(article.getTitle());
			}
			return removed;
		}
		return false;
	}

	public Set<String> getAllPackageNames() {
		return Collections.unmodifiableSet(packageToSectionsOfPackage.keySet());
	}

	/**
	 * Returns the a Set of all titles of articles that compile the package with
	 * the given name.
	 * 
	 * @created 28.08.2010
	 * @param packageName is the name of the package
	 * @return a Set of titles of articles compiling the package with the given
	 *         name.
	 */
	public Set<String> getCompilingArticles(String packageName) {
		HashSet<String> compilingArticles = packageToCompilingArticles.get(packageName);
		return compilingArticles == null
				? Collections.<String> emptySet()
				: Collections.unmodifiableSet(compilingArticles);
	}

	/**
	 * Returns the a Set of all titles of articles that compile any package.
	 * 
	 * @created 28.08.2010
	 * @return a Set of titles of articles compiling
	 */
	public Set<String> getCompilingArticles() {
		// get articles compiling a package
		HashSet<String> compilingArticles = new HashSet<String>();
		for (String packageName : getAllPackageNames()) {
			compilingArticles.addAll(getCompilingArticles(packageName));
		}
		// get articles compiling "this"
		for (HashSet<Section<? extends PackageCompiler>> compileSections : articleToPackageCompileSections.values()) {
			for (Section<? extends PackageCompiler> compileSection : compileSections) {
				Set<String> packagesToCompile = compileSection.get().getPackagesToCompile(
						compileSection);
				if (packagesToCompile.contains(THIS)) {
					compilingArticles.add(compileSection.getTitle());
				}
			}
		}
		return compilingArticles;
	}

	/**
	 * Returns all titles of articles, that compile the given Section via
	 * packages.
	 * 
	 * @created 28.12.2010
	 * @param section
	 * @return a Set of titles of articles compiling the given Section
	 */
	public Set<String> getCompilingArticles(Section<?> section) {
		Set<String> matchingArticles = new HashSet<String>();
		for (String packageName : section.getPackageNames()) {
			matchingArticles.addAll(getCompilingArticles(packageName));
		}
		HashSet<String> compilingPackages = articleToCompiledPackages.get(section.getTitle());
		if (autocompileArticleEnabled
				|| (compilingPackages != null && compilingPackages.contains(THIS))) {
			matchingArticles.add(section.getTitle());
		}
		return Collections.unmodifiableSet(matchingArticles);
	}

	/**
	 * Returns all packages the given article compiles via his Sections of the
	 * type {@link PackageCompiler}.
	 * 
	 * @created 29.08.2010
	 * @param title the title of the article to check
	 */
	public Set<String> getCompiledPackages(String title) {
		Set<String> referencedPackages = articleToCompiledPackages.get(title);
		return referencedPackages == null
				? Collections.<String> emptySet()
				: Collections.unmodifiableSet(
						referencedPackages);
	}

	/**
	 * Returns all Sections of the type {@link PackageCompiler} of the given
	 * article.
	 * 
	 * @created 29.08.2010
	 * @param title the title of the article to check
	 */
	public Set<Section<? extends PackageCompiler>> getPackageCompileSections(String title) {
		Set<Section<? extends PackageCompiler>> packageCompileSections =
				articleToPackageCompileSections.get(title);
		return packageCompileSections == null
				? Collections.unmodifiableSet(new HashSet<Section<? extends PackageCompiler>>(0))
				: Collections.unmodifiableSet(new HashSet<Section<? extends PackageCompiler>>(
						packageCompileSections));
	}

	public void updateReusedStates(Article article) {
		// TODO: not that fast... probably use own map sorted by article
		// maybe only do this for changed sections of the package... take care
		// of multiuser-scenarios
		for (LinkedList<Section<?>> sectionsOfPackageList : packageToSectionsOfPackage.values()) {
			for (Section<?> sectionOfPackage : sectionsOfPackageList) {
				if (sectionOfPackage.getTitle().equals(article.getTitle())) {
					Set<String> compilingArticles = getCompilingArticles(sectionOfPackage);
					List<Section<?>> nodes = Sections.getSubtreePostOrder(sectionOfPackage);
					for (Section<?> node : nodes) {
						if (node.get().isIgnoringPackageCompile()) continue;
						for (String title : new LinkedList<String>(node.getReusedBySet())) {
							if (!compilingArticles.contains(title)) {
								node.setReusedBy(title, false);
								Messages.clearMessages(article, node);
							}
						}
					}
				}
			}
		}
	}

	public void updateReferringArticles(Article article) {

		for (HashSet<Section<? extends PackageCompiler>> packageCompileSections : articleToPackageCompileSections.values()) {
			for (Section<? extends PackageCompiler> packageCompileSection : packageCompileSections) {
				Set<String> packagesToCompile = packageCompileSection.get().getPackagesToCompile(
						packageCompileSection);
				for (String packageName : packagesToCompile) {
					if (changedPackages.contains(packageName)) {
						Environment.getInstance().getArticleManager(article.getWeb()).addArticleToUpdate(
								packageCompileSection.getTitle());
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
