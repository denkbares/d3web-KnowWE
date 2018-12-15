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

import com.denkbares.events.EventManager;
import com.denkbares.utils.Pair;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;

public class PackageManager {// implements EventListener {

	public static final String PACKAGE_ATTRIBUTE_NAME = "package";
	public static final String MASTER_ATTRIBUTE_NAME = "master";
	public static final String COMPILE_ATTRIBUTE_NAME = "uses";

	public static final String DEFAULT_PACKAGE = "default";

	public final Compiler compiler;

	/**
	 * For each article title, you get all default packages used in this article.
	 */
	private final Map<String, HashSet<String>> articleToDefaultPackages = new HashMap<>();

	/**
	 * For each packageName, you get all Sections in the wiki belonging to this packageName.
	 */
	private final Map<String, TreeSet<Section<?>>> packageToSectionsOfPackage = new HashMap<>();

	private final Set<Section<? extends PackageCompileType>> packageCompileSections = new HashSet<>();

	/**
	 * For each package, you get all articles compiling this package.
	 */
	private final Map<String, HashSet<Section<? extends PackageCompileType>>> packageToCompilingSections =
			new HashMap<>();

	private final Map<String, Pair<TreeSet<Section<?>>, TreeSet<Section<?>>>> changedPackages =
			new HashMap<>();

	public <C extends Compiler> PackageManager(C compiler) {
		this.compiler = compiler;
		// EventManager.getInstance().registerListener(this);
	}

	public static void addPackageAnnotation(DefaultMarkup markup) {
		markup.addAnnotation(PackageManager.PACKAGE_ATTRIBUTE_NAME, false);
		markup.addAnnotationNameType(PackageManager.PACKAGE_ATTRIBUTE_NAME,
				new PackageAnnotationNameType());
		markup.addAnnotationContentType(PackageManager.PACKAGE_ATTRIBUTE_NAME,
				new PackageTerm());
	}

	private boolean isDisallowedPackageName(String packageName) {
		return packageName == null || packageName.isEmpty();
	}

	public void addDefaultPackage(Article article, String defaultPackage) {
		HashSet<String> defaultPackages = articleToDefaultPackages.get(article.getTitle());
		if (defaultPackages == null) {
			defaultPackages = new HashSet<>(4);
			articleToDefaultPackages.put(article.getTitle(), defaultPackages);
		}
		defaultPackages.add(defaultPackage);
	}

	public void removeDefaultPackage(Article article, String defaultPackage) {
		HashSet<String> defaultPackages = articleToDefaultPackages.get(article.getTitle());
		if (defaultPackages != null) {
			defaultPackages.remove(defaultPackage);
			if (defaultPackages.isEmpty()) {
				articleToDefaultPackages.remove(article.getTitle());
			}
		}
	}

	public String[] getDefaultPackages(Article article) {
		HashSet<String> defaultPackages = articleToDefaultPackages.get(article.getTitle());
		if (defaultPackages == null) {
			defaultPackages = new HashSet<>(4);
			defaultPackages.add(DEFAULT_PACKAGE);
		}
		return defaultPackages.toArray(new String[defaultPackages.size()]);
	}

	/**
	 * Adds the given Section to the package with the given name.
	 *
	 * @param section     is the Section to add
	 * @param packageName is the name of the package the Section is added to
	 * @created 28.12.2010
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
			packageList = new TreeSet<>();
			packageToSectionsOfPackage.put(packageName, packageList);
		}
		packageList.add(section);
		addSectionToChangedPackagesAsAdded(section, packageName);
		section.addPackageName(packageName);
	}

	public boolean hasPackage(String packageName) {
		return packageToSectionsOfPackage.containsKey(packageName);
	}

	private void addSectionToChangedPackagesAsAdded(Section<?> section, String packageName) {
		Pair<TreeSet<Section<?>>, TreeSet<Section<?>>> pair = changedPackages.get(packageName);
		if (pair == null) {
			pair = new Pair<>(new TreeSet<>(), new TreeSet<>());
			changedPackages.put(packageName, pair);
		}
		TreeSet<Section<?>> added = pair.getA();
		added.add(section);
	}

	private void addSectionToChangedPackagesAsRemoved(Section<?> section, String packageName) {
		Pair<TreeSet<Section<?>>, TreeSet<Section<?>>> pair = changedPackages.get(packageName);
		if (pair == null) {
			pair = new Pair<>(new TreeSet<>(), new TreeSet<>());
			changedPackages.put(packageName, pair);
		}
		TreeSet<Section<?>> removed = pair.getB();
		removed.add(section);
	}

	/**
	 * Removes the given Section from the package with the given name.
	 *
	 * @param section     is the Section to remove
	 * @param packageName is the name of the package from which the section is removed
	 * @return whether the Section was removed
	 * @created 28.12.2010
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
	 * @param s is the Section to remove
	 * @created 28.12.2010
	 */
	public void removeSectionFromAllPackages(Section<?> s) {
		for (String packageName : new ArrayList<>(s.getPackageNames())) {
			removeSectionFromPackage(s, packageName);
		}
	}

	/**
	 * Returns an unmodifiable view on the sections of the given packages at the time of calling this method.
	 *
	 * @param packageNames the package names to get the sections for
	 * @return the sections of the given packages
	 * @created 15.12.2013
	 */
	public Collection<Section<?>> getSectionsOfPackage(String... packageNames) {
		TreeSet<Section<?>> sectionsOfPackage = new TreeSet<>();
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
	 * Returns all sections added to the given packages since the changes were last cleared with {@link
	 * #clearChangedPackages()}
	 *
	 * @param packageNames the package to return the added sections for
	 * @return the sections last added to the given packages
	 * @created 15.12.2013
	 */
	public Collection<Section<?>> getAddedSections(String... packageNames) {
		TreeSet<Section<?>> addedSections = new TreeSet<>();
		for (String packageName : packageNames) {
			Pair<TreeSet<Section<?>>, TreeSet<Section<?>>> pair = changedPackages.get(packageName);
			if (pair != null) {
				addedSections.addAll(pair.getA());
			}
		}
		return Collections.unmodifiableSet(addedSections);
	}

	/**
	 * Returns all sections removed from the given packages since the changes were last cleared with {@link
	 * #clearChangedPackages()}
	 *
	 * @param packageNames the package to return the removed sections for
	 * @return the sections last removed to the given packages
	 * @created 15.12.2013
	 */
	public Collection<Section<?>> getRemovedSections(String... packageNames) {
		TreeSet<Section<?>> addedSections = new TreeSet<>();
		for (String packageName : packageNames) {
			Pair<TreeSet<Section<?>>, TreeSet<Section<?>>> pair = changedPackages.get(packageName);
			if (pair != null) {
				addedSections.addAll(pair.getB());
			}
		}
		return Collections.unmodifiableSet(addedSections);
	}

	public void registerPackageCompileSection(Section<? extends PackageCompileType> section) {

		String[] packagesToCompile = section.get().getPackagesToCompile(section);

		packageCompileSections.add(section);

		for (String packageToCompile : packagesToCompile) {
			HashSet<Section<? extends PackageCompileType>> compilingSections = packageToCompilingSections.get(packageToCompile);
			if (compilingSections == null) {
				compilingSections = new HashSet<>();
				packageToCompilingSections.put(packageToCompile, compilingSections);
			}
			compilingSections.add(section);
		}
		EventManager.getInstance().fireEvent(new RegisteredPackageCompileSectionEvent(section));
	}

	public boolean unregisterPackageCompileSection(Section<? extends PackageCompileType> section) {

		boolean removed = packageCompileSections.remove(section);
		if (removed) {
			// set all Sections that were referenced by this
			// PackageCompiler to reused = false for the given article
			String[] packagesToCompile = section.get().getPackagesToCompile(section);
			for (String packageToCompile : packagesToCompile) {
				HashSet<Section<? extends PackageCompileType>> compilingSections = packageToCompilingSections.get(packageToCompile);
				compilingSections.remove(section);
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
	 * Returns all the Sections of type {@link PackageCompileType}, that have a package the given Section is part of.
	 *
	 * @param section the Section we want the compile Sections for
	 * @return a Set of Sections compiling the given Section
	 * @created 28.12.2010
	 */
	public Set<Section<? extends PackageCompileType>> getCompileSections(Section<?> section) {
		Set<Section<? extends PackageCompileType>> compileSections = new HashSet<>();
		for (String packageName : section.getPackageNames()) {
			compileSections.addAll(getCompileSections(packageName));
		}
		return Collections.unmodifiableSet(compileSections);
	}

	/**
	 * Returns the Sections of type {@link PackageCompileType}, that represent the compiler compiling the given
	 * package.
	 *
	 * @param packageName is the name of the package
	 * @return a Set of titles of articles compiling the package with the given name.
	 * @created 28.08.2010
	 */
	public Set<Section<? extends PackageCompileType>> getCompileSections(String packageName) {
		HashSet<Section<? extends PackageCompileType>> compilingSections =
				packageToCompilingSections.get(packageName);
		return compilingSections == null
				? Collections.emptySet()
				: Collections.unmodifiableSet(compilingSections);
	}

	/**
	 * @created 15.11.2013
	 * @deprecated
	 */
	@Deprecated
	public Set<String> getCompilingArticles(Section<?> section) {
		Set<Section<? extends PackageCompileType>> compileSections = getCompileSections(section);
		Set<String> titles = new HashSet<>();
		for (Section<? extends PackageCompileType> sections : compileSections) {
			titles.add(sections.getTitle());
		}
		return titles;
	}

	/**
	 * @created 15.11.2013
	 * @deprecated
	 */
	@Deprecated
	public Set<String> getCompilingArticles() {
		Collection<Section<? extends PackageCompileType>> compileSections = getCompileSections();
		Set<String> titles = new HashSet<>();
		for (Section<? extends PackageCompileType> sections : compileSections) {
			titles.add(sections.getTitle());
		}
		return titles;
	}

	/**
	 * @created 15.11.2013
	 * @deprecated
	 */
	@Deprecated
	public Set<String> getCompilingArticles(String packageName) {
		Collection<Section<? extends PackageCompileType>> compileSections = getCompileSections(packageName);
		Set<String> titles = new HashSet<>();
		for (Section<? extends PackageCompileType> sections : compileSections) {
			titles.add(sections.getTitle());
		}
		return titles;
	}
}
