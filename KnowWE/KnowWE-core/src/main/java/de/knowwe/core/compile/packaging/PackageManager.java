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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.d3web.utils.Pair;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;

public class PackageManager {// implements EventListener {

	public static final String PACKAGE_ATTRIBUTE_NAME = "package";
	public static final String MASTER_ATTRIBUTE_NAME = "master";
	public static final String COMPILE_ATTRIBUTE_NAME = "uses";

	public static final String DEFAULT_PACKAGE = "default";

	public final Compiler compiler;

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
	private final Map<String, TreeSet<Section<?>>> packageToSectionsOfPackage =
			new HashMap<String, TreeSet<Section<?>>>();

	/**
	 * For each article title, you get all Sections of type
	 * {@link PackageManager} defined in this article.
	 */
	// private final Map<String, HashSet<Section<? extends PackageCompiler>>>
	// articleToPackageCompileSections =
	// new HashMap<String, HashSet<Section<? extends PackageCompiler>>>();

	private final Set<Section<? extends PackageCompileType>> packageCompileSections = new HashSet<Section<? extends PackageCompileType>>();
	/**
	 * For each article title, you get all packages compiled in this article by
	 * Sections of the type {@link PackageCompiler}.
	 */
	// private final Map<String, HashSet<String>> articleToCompiledPackages =
	// new HashMap<String, HashSet<String>>();

	/**
	 * For each package, you get all articles compiling this package.
	 */
	private final Map<String, HashSet<Section<? extends PackageCompileType>>> packageToCompilingSections =
			new HashMap<String, HashSet<Section<? extends PackageCompileType>>>();

	private final Map<String, Pair<TreeSet<Section<?>>, TreeSet<Section<?>>>> changedPackages =
			new HashMap<String, Pair<TreeSet<Section<?>>, TreeSet<Section<?>>>>();

	public <C extends Compiler> PackageManager(C compiler) {
		this.compiler = compiler;
		// EventManager.getInstance().registerListener(this);
	}

	private boolean isDisallowedPackageName(String packageName) {
		return packageName == null || packageName.isEmpty();
	}

	public void addDefaultPackage(Article article, String defaultPackage) {
		HashSet<String> defaultPackages = articleToDefaultPackages.get(article.getTitle());
		if (defaultPackages == null) {
			defaultPackages = new HashSet<String>(4);
			articleToDefaultPackages.put(article.getTitle(), defaultPackages);
		}
		defaultPackages.add(defaultPackage);
	}

	public void removeDefaultPackage(Article article, String defaultPackage) {
		HashSet<String> defaultPackages = articleToDefaultPackages.get(article.getTitle());
		if (defaultPackages != null) {
			defaultPackages.remove(defaultPackage);
		}
	}

	public String[] getDefaultPackages(Article article) {
		HashSet<String> defaultPackages = articleToDefaultPackages.get(article.getTitle());
		if (defaultPackages == null) {
			defaultPackages = new HashSet<String>(4);
			defaultPackages.add(DEFAULT_PACKAGE);
		}
		return defaultPackages.toArray(new String[defaultPackages.size()]);
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
			Messages.storeMessage(section, this.getClass(), Messages.error("'"
					+ packageName
					+ "' is not allowed as a package name."));
			return;
		}
		TreeSet<Section<?>> packageList = packageToSectionsOfPackage.get(packageName);
		if (packageList == null) {
			packageList = new TreeSet<Section<?>>();
			packageToSectionsOfPackage.put(packageName, packageList);
		}
		packageList.add(section);
		addSectionToChangedPackagesAsAdded(section, packageName);
		section.addPackageName(packageName);
	}

	private void addSectionToChangedPackagesAsAdded(Section<?> section, String packageName) {
		Pair<TreeSet<Section<?>>, TreeSet<Section<?>>> pair = changedPackages.get(packageName);
		if (pair == null) {
			pair = new Pair<TreeSet<Section<?>>, TreeSet<Section<?>>>(
					new TreeSet<Section<?>>(), new TreeSet<Section<?>>());
			changedPackages.put(packageName, pair);
		}
		TreeSet<Section<?>> added = pair.getA();
		added.add(section);
	}

	private void addSectionToChangedPackagesAsRemoved(Section<?> section, String packageName) {
		Pair<TreeSet<Section<?>>, TreeSet<Section<?>>> pair = changedPackages.get(packageName);
		if (pair == null) {
			pair = new Pair<TreeSet<Section<?>>, TreeSet<Section<?>>>(
					new TreeSet<Section<?>>(), new TreeSet<Section<?>>());
			changedPackages.put(packageName, pair);
		}
		TreeSet<Section<?>> removed = pair.getB();
		removed.add(section);
	}

	/**
	 * Removes the given Section from the package with the given name.
	 * 
	 * @created 28.12.2010
	 * @param section is the Section to remove
	 * @param packageName is the name of the package from which the section is
	 *        removed
	 * @returns whether the Section was removed
	 */
	public boolean removeSectionFromPackage(Section<?> section, String packageName) {
		if (!isDisallowedPackageName(packageName)) {
			TreeSet<Section<?>> packageSet = packageToSectionsOfPackage.get(packageName);
			if (packageSet != null) {
				boolean removed = packageSet.remove(section);
				if (removed) {
					addSectionToChangedPackagesAsRemoved(section, packageName);
				}
				if (packageSet.isEmpty()) {
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
		for (String packageName : new ArrayList<String>(s.getPackageNames())) {
			removeSectionFromPackage(s, packageName);
		}
	}

	/**
	 * Returns an unmodifiable view on the sections of the given packages at the
	 * time of calling this method.
	 * 
	 * @created 15.12.2013
	 * @param packageNames the package names to get the sections for
	 * @return the sections of the given packages
	 */
	public Collection<Section<?>> getSectionsOfPackage(String... packageNames) {
		TreeSet<Section<?>> sectionsOfPackage = new TreeSet<Section<?>>();
		for (String packageName : packageNames) {
			TreeSet<Section<?>> sections = packageToSectionsOfPackage.get(packageName);
			if (sections != null) {
				sectionsOfPackage.addAll(sections);
			}
		}
		return Collections.unmodifiableSet(sectionsOfPackage);
	}

	public boolean hasChanged(String... packageNames) {
		for (String packageName : packageNames) {
			if (changedPackages.containsKey(packageName)) return true;
		}
		return false;
	}

	/**
	 * Returns all sections added to the given packages since the changes were
	 * last cleared with {@link PackageManager#clearChangedPackage()}
	 * 
	 * @created 15.12.2013
	 * @param packageNames the package to return the added sections for
	 * @return the sections last added to the given packages
	 */
	public Collection<Section<?>> getAddedSections(String... packageNames) {
		TreeSet<Section<?>> addedSections = new TreeSet<Section<?>>();
		for (String packageName : packageNames) {
			Pair<TreeSet<Section<?>>, TreeSet<Section<?>>> pair = changedPackages.get(packageName);
			if (pair != null) {
				addedSections.addAll(pair.getA());
			}
		}
		return Collections.unmodifiableSet(addedSections);
	}

	/**
	 * Returns all sections removed from the given packages since the changes
	 * were last cleared with {@link PackageManager#clearChangedPackage()}
	 * 
	 * @created 15.12.2013
	 * @param packageNames the package to return the removed sections for
	 * @return the sections last removed to the given packages
	 */
	public Collection<Section<?>> getRemovedSections(String... packageNames) {
		TreeSet<Section<?>> addedSections = new TreeSet<Section<?>>();
		for (String packageName : packageNames) {
			Pair<TreeSet<Section<?>>, TreeSet<Section<?>>> pair = changedPackages.get(packageName);
			if (pair != null) {
				addedSections.addAll(pair.getB());
			}
		}
		return Collections.unmodifiableSet(addedSections);
	}

	public void registerPackageCompileSection(Section<PackageCompileType> section) {

		String[] packagesToCompile = section.get().getPackagesToCompile(section);

		packageCompileSections.add(section);

		for (String packageToCompile : packagesToCompile) {
			HashSet<Section<? extends PackageCompileType>> compilingSections = packageToCompilingSections.get(packageToCompile);
			if (compilingSections == null) {
				compilingSections = new HashSet<Section<? extends PackageCompileType>>();
				packageToCompilingSections.put(packageToCompile, compilingSections);
			}
			compilingSections.add(section);
		}
		EventManager.getInstance().fireEvent(new RegisteredPackageCompileSectionEvent(section));
	}

	public boolean unregisterPackageCompileSection(Section<PackageCompileType> section) {

		boolean removed = packageCompileSections.remove(section);
		if (removed) {
			// set all Sections that were referenced by this
			// PackageCompiler to reused = false for the given article
			String[] packagesToCompile = section.get().getPackagesToCompile(section);
			for (String packageToCompile : packagesToCompile) {
				HashSet<Section<? extends PackageCompileType>> compilingSections = packageToCompilingSections.get(packageToCompile);
				if (compilingSections.isEmpty()) {
					packageToCompilingSections.remove(packageToCompile);
				}
			}
		}
		EventManager.getInstance().fireEvent(new UnregisteredPackageCompileSectionEvent(section));
		return removed;
	}

	public Set<String> getAllPackageNames() {
		return Collections.unmodifiableSet(packageToSectionsOfPackage.keySet());
	}

	public Collection<Section<? extends PackageCompileType>> getCompileSections() {
		return Collections.unmodifiableCollection(packageCompileSections);
	}

	public void clearChangedPackages() {
		this.changedPackages.clear();
	}

	/**
	 * Returns all the Sections of type {@link PackageCompileType}, that have a
	 * package the given Section is part of.
	 * 
	 * @created 28.12.2010
	 * @param section
	 * @return a Set of Sections compiling the given Section
	 */
	public Set<Section<? extends PackageCompileType>> getCompileSections(Section<?> section) {
		Set<Section<? extends PackageCompileType>> compileSections = new HashSet<Section<? extends PackageCompileType>>();
		for (String packageName : section.getPackageNames()) {
			compileSections.addAll(getCompileSections(packageName));
		}
		return Collections.unmodifiableSet(compileSections);
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
	public Set<Section<? extends PackageCompileType>> getCompileSections(String packageName) {
		HashSet<Section<? extends PackageCompileType>> compilingSections =
				packageToCompilingSections.get(packageName);
		return compilingSections == null
				? Collections.<Section<? extends PackageCompileType>> emptySet()
				: Collections.unmodifiableSet(compilingSections);
	}

	/**
	 * @deprecated
	 * @created 15.11.2013
	 * @param section
	 * @return
	 */
	@Deprecated
	public Set<String> getCompilingArticles(Section<?> section) {
		Set<Section<? extends PackageCompileType>> compileSections = getCompileSections(section);
		Set<String> titles = new HashSet<String>();
		for (Section<? extends PackageCompileType> sections : compileSections) {
			titles.add(sections.getTitle());
		}
		return titles;
	}

	/**
	 * @deprecated
	 * @created 15.11.2013
	 * @return
	 */
	@Deprecated
	public Set<String> getCompilingArticles() {
		Collection<Section<? extends PackageCompileType>> compileSections = getCompileSections();
		Set<String> titles = new HashSet<String>();
		for (Section<? extends PackageCompileType> sections : compileSections) {
			titles.add(sections.getTitle());
		}
		return titles;
	}

	/**
	 * 
	 * @created 15.11.2013
	 * @param packageName
	 * @return
	 * @deprecated
	 */
	@Deprecated
	public Set<String> getCompilingArticles(String packageName) {
		Collection<Section<? extends PackageCompileType>> compileSections = getCompileSections(packageName);
		Set<String> titles = new HashSet<String>();
		for (Section<? extends PackageCompileType> sections : compileSections) {
			titles.add(sections.getTitle());
		}
		return titles;
	}

	//
	// /**
	// * Returns the a Set of all titles of articles that compile any package.
	// *
	// * @created 28.08.2010
	// * @return a Set of titles of articles compiling
	// */
	// public Set<String> getCompilingArticles() {
	// // get articles compiling a package
	// HashSet<String> compilingArticles = new HashSet<String>();
	// for (String packageName : getAllPackageNames()) {
	// compilingArticles.addAll(getCompilingArticles(packageName));
	// }
	// // get articles compiling "this"
	// for (HashSet<Section<? extends PackageCompiler>> compileSections :
	// articleToPackageCompileSections.values()) {
	// for (Section<? extends PackageCompiler> compileSection : compileSections)
	// {
	// Collection<String> packagesToCompile =
	// compileSection.get().getPackagesToCompile(
	// compileSection);
	// if (packagesToCompile.contains(THIS)) {
	// compilingArticles.add(compileSection.getTitle());
	// }
	// }
	// }
	// return compilingArticles;
	// }
	//
	// /**
	// * Returns all titles of articles, that compile the given Section via
	// * packages.
	// *
	// * @created 28.12.2010
	// * @param section
	// * @return a Set of titles of articles compiling the given Section
	// */
	// public Set<String> getCompilingArticles(Section<?> section) {
	// Set<String> matchingArticles = new HashSet<String>();
	// for (String packageName : section.getPackageNames()) {
	// matchingArticles.addAll(getCompilingArticles(packageName));
	// }
	// HashSet<String> compilingPackages =
	// articleToCompiledPackages.get(section.getTitle());
	// if (autocompileArticleEnabled
	// || (compilingPackages != null && compilingPackages.contains(THIS))) {
	// matchingArticles.add(section.getTitle());
	// }
	// return Collections.unmodifiableSet(matchingArticles);
	// }
	//
	// /**
	// * Returns all packages the given article compiles via his Sections of the
	// * type {@link PackageCompiler}.
	// *
	// * @created 29.08.2010
	// * @param title the title of the article to check
	// */
	// public Set<String> getCompiledPackages(String title) {
	// Set<String> referencedPackages = articleToCompiledPackages.get(title);
	// return referencedPackages == null
	// ? Collections.<String> emptySet()
	// : Collections.unmodifiableSet(
	// referencedPackages);
	// }

	// public void updateReusedStates(Article article) {
	// // TODO: not that fast... probably use own map sorted by article
	// // maybe only do this for changed sections of the package... take care
	// // of multiuser-scenarios
	// for (LinkedList<Section<?>> sectionsOfPackageList :
	// packageToSectionsOfPackage.values()) {
	// for (Section<?> sectionOfPackage : sectionsOfPackageList) {
	// if (sectionOfPackage.getTitle().equals(article.getTitle())) {
	// Set<String> compilingArticles = getCompilingArticles(sectionOfPackage);
	// List<Section<?>> nodes = Sections.getSubtreePostOrder(sectionOfPackage);
	// for (Section<?> node : nodes) {
	// if (!node.get().isPackageCompile()) continue;
	// for (String title : new LinkedList<String>(node.getReusedBySet())) {
	// if (!compilingArticles.contains(title)) {
	// node.setReusedBy(title, false);
	// Messages.clearMessages(article, node);
	// }
	// }
	// }
	// }
	// }
	// }
	// }

	// public void updateReferringArticles(Article article) {
	//
	// for (HashSet<Section<? extends PackageCompiler>> packageCompileSections :
	// articleToPackageCompileSections.values()) {
	// for (Section<? extends PackageCompiler> packageCompileSection :
	// packageCompileSections) {
	// Collection<String> packagesToCompile =
	// packageCompileSection.get().getPackagesToCompile(
	// packageCompileSection);
	// for (String packageName : packagesToCompile) {
	// if (changedPackages.contains(packageName)) {
	// Environment.getInstance().getArticleManager(article.getWeb()).addArticleToUpdate(
	// packageCompileSection.getTitle());
	// }
	// }
	// }
	// }
	//
	// changedPackages.clear();
	// }

	// @Override
	// public Collection<Class<? extends Event>> getEvents() {
	// ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends
	// Event>>(3);
	// events.add(FullParseEvent.class);
	// events.add(UpdatingDependenciesEvent.class);
	// events.add(PreCompileFinishedEvent.class);
	// return events;
	// }

	// @Override
	// public void notify(Event event) {
	// if (event instanceof FullParseEvent) {
	// cleanForArticle(((FullParseEvent) event).getArticle());
	// }
	// else if (event instanceof UpdatingDependenciesEvent) {
	// updateReferringArticles(((UpdatingDependenciesEvent)
	// event).getArticle());
	// }
	// else if (event instanceof PreCompileFinishedEvent) {
	// updateReusedStates(((PreCompileFinishedEvent) event).getArticle());
	// }
	// }

}
