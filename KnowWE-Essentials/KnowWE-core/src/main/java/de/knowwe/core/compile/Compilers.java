/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Utility methods needed while compiling.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 17.11.2013
 */
public class Compilers {

	private static final String DEFAULT_COMPILERS = "default-compilers";

	/**
	 * Null safe comparator for compilers.
	 * For {@link NamedCompiler}s, name will be used.
	 * For {@link PackageCompiler}
	 */
	public static final Comparator<Compiler> COMPARATOR = Comparator.nullsFirst(Comparator.comparing(Compilers::getCompilerName));

	/**
	 * Compiles the given {@link Section} for the given {@link Compiler} (by calling the {@link CompileScript}s of the
	 * {@link Compiler} for the {@link Type} of the {@link Section} and applying them to the {@link Section}).
	 *
	 * @created 07.01.2014
	 */
	public static <C extends Compiler, T extends Type> void compile(C compiler, Section<T> section) {

		ScriptManager<C> scriptManager = CompilerManager.getScriptManager(compiler);
		Map<Priority, List<CompileScript<C, T>>> scripts = scriptManager.getScripts(section.get());
		for (List<CompileScript<C, T>> scriptList : scripts.values()) {
			for (CompileScript<C, T> compileScript : scriptList) {
				try {
					compileScript.compile(compiler, section);
				}
				catch (CompilerMessage cm) {
					Messages.storeMessages(compiler, section, compileScript.getClass(),
							cm.getMessages());
				}
			}
		}
	}

	/**
	 * Destroys the given {@link Section} for the given {@link Compiler} (by calling the {@link CompileScript}s of the
	 * {@link Compiler} for the {@link Type} of the {@link Section} and applying them to the {@link Section}).
	 *
	 * @created 07.01.2014
	 */
	@SuppressWarnings("unchecked")
	public static <C extends Compiler, T extends Type> void destroy(C compiler, Section<T> section) {

		ScriptManager<C> scriptManager = CompilerManager.getScriptManager(compiler);
		Map<Priority, List<CompileScript<C, T>>> scripts = scriptManager.getScripts(section.get());
		for (List<CompileScript<C, T>> scriptList : scripts.values()) {
			for (CompileScript<C, T> compileScript : scriptList) {
				if (!(compileScript instanceof DestroyScript)) continue;
				try {
					((DestroyScript<C, T>) compileScript).destroy(compiler, section);
				}
				catch (Exception e) {
					String msg = "Unexpected internal exception while destroying with script "
							+ section;
					Log.severe(msg, e);
				}
			}
		}
	}

	/**
	 * Returns the first {@link PackageCompiler} of the given compiler class, that compiles a {@link Section} of the
	 * type {@link PackageCompileType} on the given article.
	 *
	 * @param master        the master article for which we want the {@link Compiler}
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return the first {@link Compiler} that compiles the given section.
	 * @created 15.11.2013
	 */
	@Deprecated
	public static <C extends PackageCompiler> C getCompiler(Article master, Class<C> compilerClass) {
		Collection<C> compilers = getCompilers(master.getArticleManager(), compilerClass);
		for (C compiler : compilers) {
			if (compiler.getCompileSection().getArticle().equals(master)) {
				return compiler;
			}
		}
		return null;
	}

	/**
	 * Returns the first {@link Compiler} of all compilers that compiles  the packages of the given section, that is of
	 * the specified compiler class, or which extends or implements the specified compiler class. If no such compiler
	 * exists, null is returned.
	 *
	 * @param section       the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return the first {@link Compiler} that compiles the given section or null, if the section is not compiled by a
	 * compiler with the given class
	 * @created 15.11.2013
	 */
	@Nullable
	public static <C extends Compiler> C getCompiler(Section<?> section, Class<C> compilerClass) {
		Collection<C> compilers = getCompilers(null, section, compilerClass, true);
		if (compilers.isEmpty()) {
			return null;
		}
		else {
			return compilers.iterator().next();
		}
	}

	/**
	 * Returns the first {@link Compiler} of a given ArticleManager and compiler class.
	 *
	 * @param manager       the {@link ArticleManager} for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return the first {@link Compiler}s of a given ArticleManager and Class.
	 * @created 15.11.2013
	 */
	@Nullable
	public static <C extends Compiler> C getCompiler(ArticleManager manager, Class<C> compilerClass) {
		Collection<C> compilers = getCompilers(null, manager, compilerClass, true);
		if (compilers.isEmpty()) {
			return null;
		}
		else {
			return compilers.iterator().next();
		}
	}

	@NotNull
	public static DefaultGlobalCompiler getGlobalCompiler(Section<?> section) {
		return getGlobalCompiler(section.getArticleManager() == null
				? KnowWEUtils.getDefaultArticleManager() : section.getArticleManager());
	}

	@NotNull
	public static DefaultGlobalCompiler getGlobalCompiler(ArticleManager manager) {
		for (Compiler compiler : manager.getCompilerManager().getCompilers()) {
			if (compiler.getClass() == DefaultGlobalCompiler.class) {
				return (DefaultGlobalCompiler) compiler;
			}
		}
		throw new IllegalStateException("Invalid state: no global compiler");
	}

	@NotNull
	public static PackageRegistrationCompiler getPackageRegistrationCompiler(Section<?> section) {
		return getPackageRegistrationCompiler(section.getArticleManager() == null
				? KnowWEUtils.getDefaultArticleManager() : section.getArticleManager());
	}

	@NotNull
	public static PackageRegistrationCompiler getPackageRegistrationCompiler(ArticleManager manager) {
		for (Compiler compiler : manager.getCompilerManager().getCompilers()) {
			if (compiler.getClass() == PackageRegistrationCompiler.class) {
				return (PackageRegistrationCompiler) compiler;
			}
		}
		throw new IllegalStateException("invalid state: no package registration compiler");
	}

	/**
	 * Returns a {@link Compiler} that compiles the packages of the given section, and is an instance of the given
	 * compiler class. If no such compiler exists, null is returned.
	 * <br>
	 * If multiple compilers exist, we check the given user context, if one of the compilers was marked as the default
	 * compiler using @link {@link #markAsDefaultCompiler(UserContext, Compiler)}
	 *
	 * @param context       the user context for this call
	 * @param section       the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return all {@link Compiler}s compiling the given section
	 * @created 15.11.2013
	 */
	@Nullable
	public static <C extends Compiler> C getCompiler(UserContext context, Section<?> section, Class<C> compilerClass) {
		if (context == null) return getCompiler(section, compilerClass);

		Collection<C> compilers = getCompilers(section, compilerClass);
		// no need to find a default, if there is only one compiler
		if (compilers.size() == 1) {
			return compilers.iterator().next();
		}
		else if (compilers.isEmpty()) {
			return null;
		}

		// check compiler names markes for default
		List<String> defaultCompilerNames = getDefaultCompilers(context, compilerClass);
		if (defaultCompilerNames.isEmpty()) {
			// no defaults found? return first compiler we have...
			return compilers.iterator().next();
		}

		// get the first match of name to available compilers
		Map<String, List<C>> compilersByName = compilers.stream()
				.collect(Collectors.groupingBy(Compilers::getCompilerName));
		for (String name : defaultCompilerNames) {
			List<C> compilersForName = compilersByName.getOrDefault(name, Collections.emptyList());
			if (!compilersForName.isEmpty()) {
				return compilersForName.get(0);
			}
		}

		// defaultCompilerNames seem to contain compiler names that are no longer valid...
		// just weit for it do get cleaned up automatically, return the next best compiler
		return compilers.iterator().next();
	}

	/**
	 * Mark the given compiler as the default compiler for the given user context, for cases where multiple compilers
	 * are present and we need to select one.
	 *
	 * @param context  the user context where the compiler should be marked as the default
	 * @param compiler the compiler to mark as the default
	 */
	public static void markAsDefaultCompiler(@NotNull UserContext context, @NotNull Compiler compiler) {
		List<String> defaultCompilers = getDefaultCompilers(context, compiler.getClass());
		String compilerName = getCompilerName(compiler);
		defaultCompilers.remove(compilerName); // remove if already present
		defaultCompilers.add(0, compilerName); // add at first place
	}

	/**
	 * Marks the given grouping compiler and all its children compilers as default compilers for
	 * the given user.
	 *
	 * @param context  the user context
	 * @param compiler the grouping compiler to mark it and its children compilers as default
	 */
	public static void markSelfAndChildrenCompilersAsDefault(@NotNull UserContext context, @NotNull GroupingCompiler compiler) {
		compiler.getChildCompilers().forEach(c -> Compilers.markAsDefaultCompiler(context, c));
		Compilers.markAsDefaultCompiler(context, compiler);
	}

	@NotNull
	private static List<String> getDefaultCompilers(@NotNull UserContext context, @NotNull Class<? extends Compiler> compilerClass) {
		return getDefaultCompilers(context, compilerClass, false);
	}

	@NotNull
	private static List<String> getDefaultCompilers(@NotNull UserContext context, @NotNull Class<? extends Compiler> compilerClass, boolean doCleanup) {
		//noinspection unchecked
		Map<String, List<String>> defaultCompilerNamesMap = (Map<String, List<String>>) context.getSession()
				.getAttribute(DEFAULT_COMPILERS);
		if (defaultCompilerNamesMap == null) {
			defaultCompilerNamesMap = new HashMap<>();
			context.getSession().setAttribute(DEFAULT_COMPILERS, defaultCompilerNamesMap);
		}

		List<String> defaultCompilerNamesOfClass = defaultCompilerNamesMap.computeIfAbsent(compilerClass.getName(), s -> new ArrayList<>());
		if (defaultCompilerNamesOfClass.isEmpty()) {
			getFallbackDefaultCompiler(context, compilerClass).ifPresent(defaultCompilerNamesOfClass::add);
		}
		else if (doCleanup) {
			Set<String> validCompilerNamesForClass = getCompilers(context.getArticleManager(), compilerClass).stream()
					.map(Compilers::getCompilerName)
					.collect(Collectors.toSet());
			defaultCompilerNamesOfClass.removeIf(c -> !validCompilerNamesForClass.contains(c));
		}
		return defaultCompilerNamesOfClass;
	}

	/**
	 * Returns if a compiler is the default compiler for its compiler class in the specific user context
	 *
	 * @param context  the user context
	 * @param compiler the compiler to check if it's the default compiler
	 * @return true, if it is the default compiler for the user context or none default is set, otherwise false
	 */
	public static boolean isDefaultCompiler(@NotNull UserContext context, @NotNull Compiler compiler) {
		List<String> defaultCompilers = getDefaultCompilers(context, compiler.getClass());

		// if no compiler is set and we also don't find a fallback, every compiler is the default compiler per definition
		if (defaultCompilers.isEmpty()) return true;

		// of course, if the name matches, we have a default compiler
		String compilerName = getCompilerName(compiler);
		if (defaultCompilers.get(0).equals(compilerName)) return true;

		// try again with cleaned up version... if empty, we also have the default compiler
		defaultCompilers = getDefaultCompilers(context, compiler.getClass(), true);
		return defaultCompilers.isEmpty();
	}

	/**
	 * Gets the fallback default compiler. It takes all compilers with the given type, sort them by their name and
	 * takes the first one. If some of the compilers are grouped into a grouped compiler, they also get priority over
	 * the ones that are not.
	 * This should only be used when no default compiler is set (e.g. after a server restart)
	 *
	 * @param context       the user context
	 * @param compilerClass the tye of the compiler we want to get the fallback for
	 * @return the fallback compiler with the specific type
	 */
	private static Optional<String> getFallbackDefaultCompiler(@NotNull UserContext context, @NotNull Class<? extends Compiler> compilerClass) {
		Set<Compiler> childCompilers = Compilers.getCompilers(KnowWEUtils.getArticleManager(context.getWeb()), GroupingCompiler.class)
				.stream()
				.flatMap(c -> c.getChildCompilers().stream())
				.collect(Collectors.toSet());
		return Compilers.getCompilers(context.getArticleManager(), compilerClass)
				.stream()
				.sorted((Comparator<Compiler>) (c1, c2) -> {
					if (childCompilers.contains(c1) && !childCompilers.contains(c2)) {
						return -1;
					}
					else if (!childCompilers.contains(c1) && childCompilers.contains(c2)) {
						return 1;
					}
					return NumberAwareComparator.CASE_INSENSITIVE
							.compare(Compilers.getCompilerName(c1), Compilers.getCompilerName(c2));
				})
				.map(Compilers::getCompilerName)
				.findFirst();
	}

	/**
	 * Provides a name for the given compiler
	 *
	 * @param compiler the compiler to get a name for
	 * @return the name of the given compiler
	 */
	@NotNull
	public static String getCompilerName(Compiler compiler) {
		if (compiler instanceof NamedCompiler) {
			return ((NamedCompiler) compiler).getName();
		}
		else if (compiler == null) {
			return "null";
		}
		else {
			return compiler.getClass().getSimpleName();
		}
	}

	/**
	 * Returns all {@link Compiler}s with the given type that compile the packages of the given section.
	 *
	 * @param section       the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return all {@link Compiler}s compiling the given section
	 * @created 15.11.2013
	 */
	@NotNull
	public static <C extends Compiler> Collection<C> getCompilers(Section<?> section, Class<C> compilerClass) {
		return getCompilers(null, section, compilerClass, false);
	}

	/**
	 * Returns all {@link Compiler}s with the given type that compile the packages of the given section. The compiler(s)
	 * marked as default compiler will be first in the collection.
	 *
	 * @param context       the user context to get the default compilers
	 * @param section       the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return all {@link Compiler}s compiling the given section
	 * @created 15.11.2013
	 */
	@NotNull
	public static <C extends Compiler> Collection<C> getCompilers(UserContext context, Section<?> section, Class<C> compilerClass) {
		return getCompilers(context, section, compilerClass, false);
	}

	private static <C extends Compiler> Collection<C> getCompilers(@Nullable UserContext context, Section<?> section, Class<C> compilerClass, boolean firstOnly) {
		ArticleManager articleManager = section.getArticleManager();
		if (articleManager == null) { // can happen in preview
			articleManager = Environment.getInstance().getArticleManager(section.getWeb());
		}
		List<Compiler> allCompilers = articleManager.getCompilerManager().getCompilers();
		List<C> compilers = new ArrayList<>();
		for (Compiler compiler : allCompilers) {
			if (compilerClass.isInstance(compiler) && compiler.isCompiling(section)) {
				compilers.add(compilerClass.cast(compiler));
			}
		}

		// if we only want one compiler but there are multiple, make sure to check if the section is a (or successor of)
		// package compile section... if yes, we want the associated compiler
		if (firstOnly && compilers.size() > 1) {
			Section<PackageCompileType> compileSection = $(section).closest(PackageCompileType.class).getFirst();
			if (compileSection != null) {
				Collection<PackageCompiler> packageCompilers = compileSection.get().getPackageCompilers(compileSection);
				for (PackageCompiler packageCompiler : packageCompilers) {
					if (compilerClass.isInstance(packageCompiler)) {
						return Collections.singletonList(compilerClass.cast(packageCompiler));
					}
				}
			}
		}

		if (context != null) {
			// sort by order of default compilers, as far as possible/available, otherwise, sort by name
			sortByDefaultCompilersAndName(compilers, context, compilerClass);
		}
		else {
			// make return value consistent, sort by name
			compilers.sort(Comparator.comparing(Compilers::getCompilerName, NumberAwareComparator.CASE_INSENSITIVE));
		}

		return compilers;
	}

	private static <C extends Compiler> void sortByDefaultCompilersAndName(List<C> compilers, @NotNull UserContext context, Class<C> compilerClass) {
		Map<String, Integer> compilerOrder = new HashMap<>();
		@NotNull List<String> defaultCompilers = getDefaultCompilers(context, compilerClass);
		for (int i = 0; i < defaultCompilers.size(); i++) {
			compilerOrder.put(defaultCompilers.get(i), i);
		}
		compilers.sort((o1, o2) -> {
			String n1 = getCompilerName(o1);
			String n2 = getCompilerName(o2);
			int compare = compilerOrder.getOrDefault(n1, Integer.MAX_VALUE)
					.compareTo(compilerOrder.getOrDefault(n2, Integer.MAX_VALUE));
			if (compare == 0) {
				compare = n1.compareTo(n2);
			}
			return compare;
		});
	}

	/**
	 * Returns all {@link Compiler}s with the given type that compile the packages of the given section and also have
	 * compile scripts attached to the type of the given section.
	 *
	 * @param section       the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return all {@link Compiler}s compiling the given section
	 * @created 15.11.2013
	 */
	@NotNull
	public static <C extends Compiler> Collection<C> getCompilersWithCompileScript(Section<?> section, Class<C> compilerClass) {
		Collection<C> compilers = getCompilers(null, section, compilerClass, false);
		ArrayList<C> filteredCompilers = new ArrayList<>(compilers.size());
		for (C compiler : compilers) {
			if (!CompilerManager.getScriptManager(compiler).getScripts(section.get()).isEmpty()) {
				filteredCompilers.add(compiler);
			}
		}
		return filteredCompilers;
	}

	/**
	 * Returns all {@link Compiler}s of a given ArticleManager and class. If a context is given, the default compiler
	 * will be the first element in the returned list.
	 *
	 * @param context       the user context to get the default compilers
	 * @param manager       the {@link ArticleManager} for which we want the {@link Compiler}
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return all {@link AbstractPackageCompiler}s compiling the given section
	 * @created 15.11.2013
	 */
	@NotNull
	public static <C extends Compiler> List<C> getCompilers(UserContext context, ArticleManager manager, Class<C> compilerClass) {
		return getCompilers(context, manager, compilerClass, false);
	}

	/**
	 * Returns all {@link Compiler}s of a given ArticleManager and class.
	 *
	 * @param manager       the {@link ArticleManager} for which we want the {@link Compiler}
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return all {@link AbstractPackageCompiler}s compiling the given section
	 * @created 15.11.2013
	 */
	@NotNull
	public static <C extends Compiler> List<C> getCompilers(ArticleManager manager, Class<C> compilerClass) {
		return getCompilers(null, manager, compilerClass, false);
	}

	private static <C extends Compiler> List<C> getCompilers(@Nullable UserContext context, ArticleManager manager, Class<C> compilerClass, boolean firstOnly) {
		List<Compiler> allCompilers = manager.getCompilerManager().getCompilers();
		List<C> compilers = new ArrayList<>();
		for (Compiler compiler : allCompilers) {
			if (compilerClass.isInstance(compiler)) {
				compilers.add(compilerClass.cast(compiler));
				if (firstOnly) break;
			}
		}
		if (context != null) {
			sortByDefaultCompilersAndName(compilers, context, compilerClass);
		}
		return compilers;
	}

	/**
	 * @created 15.11.2013
	 * @deprecated Helper method while we transition to new compiler framework
	 */
	@Deprecated
	@NotNull
	public static Collection<Article> getCompilingArticleObjects(Section<?> section) {
		Collection<Article> articles = new ArrayList<>();
		PackageManager packageManager = KnowWEUtils.getPackageManager(section.getArticleManager());
		Set<String> referringArticleTitles = packageManager.getCompilingArticles(section);
		ArticleManager articleManager = section.getArticleManager();
		if (articleManager == null) return Collections.emptyList();
		for (String title : referringArticleTitles) {
			Article article = articleManager.getArticle(title);
			if (article == null) continue;
			articles.add(article);
		}
		return articles;
	}

	/**
	 * Returns all master articles that compile the given Section. If no master article compiles the Section, at least
	 * the article of the Section itself is returned, so the Collection always at least contains one article.
	 *
	 * @param section is the Section for which you want to know the compiling articles
	 * @return a non empty Collection of articles that compile the given Section
	 * @created 16.02.2012
	 * @deprecated Helper method while we transition to new compiler framework
	 */
	@Deprecated
	@NotNull
	public static Collection<Article> getCompilingArticles(Section<?> section) {
		Collection<Article> articles = getCompilingArticleObjects(section);
		if (articles.isEmpty()) articles.add(section.getArticle());
		return articles;
	}

	/**
	 * Returns the default {@link CompilerManager} of the given web.
	 *
	 * @param web the web we want the {@link CompilerManager} from
	 * @created 07.01.2014
	 */
	@NotNull
	public static CompilerManager getCompilerManager(String web) {
		return KnowWEUtils.getArticleManager(web).getCompilerManager();
	}

	public static void destroyAndRecompileRegistrations(IncrementalCompiler compiler, Identifier identifier, Class<?>... scriptFilter) {
		Sections.registrations((TermCompiler) compiler, identifier)
				.forEach(s -> destroyAndRecompileSection(compiler, s, scriptFilter));
	}

	public static void destroyAndRecompileReferences(IncrementalCompiler compiler, Identifier identifier, Class<?>... scriptFilter) {
		Sections.references((TermCompiler) compiler, identifier)
				.forEach(s -> destroyAndRecompileSection(compiler, s, scriptFilter));
	}

	public static void destroyAndRecompileSection(IncrementalCompiler compiler, Section<?> section, Class<?>... scriptFilter) {
		compiler.addSectionToDestroy(section, scriptFilter);
		if (Sections.isLive(section)) {
			// the sections that have not been removed from the wiki are also compiled again
			compiler.addSectionToCompile(section, scriptFilter);
		}
	}

	public static void destroyAndRecompileSubtree(IncrementalCompiler compiler, Section<?> section, Class<?>... scriptFilter) {
		compiler.addSubtreeToDestroy(section, scriptFilter);
		if (Sections.isLive(section)) {
			// the sections that have not been removed from the wiki are also compiled again
			compiler.addSubtreeToCompile(section, scriptFilter);
		}
	}

	// very simple method at the moment, we have it to be consistent with #destroyAndRecompileSection
	public static void recompileSection(IncrementalCompiler compiler, Section<?> section, Class<?>... scriptFilter) {
		compiler.addSectionToCompile(section, scriptFilter);
	}

	public static void recompileRegistrations(IncrementalCompiler compiler, Identifier identifier, Class<?>... scriptFilter) {
		Sections.registrations((TermCompiler) compiler, identifier)
				.forEach(s -> compiler.addSectionToCompile(s, scriptFilter));
	}

	public static void recompileReferences(IncrementalCompiler compiler, Identifier identifier, Class<?>... scriptFilter) {
		Sections.references((TermCompiler) compiler, identifier)
				.forEach(s -> compiler.addSectionToCompile(s, scriptFilter));
	}

	/**
	 * Waits for the CompilerManager to finish and handles the InterruptException by logging it.
	 *
	 * @param manager the manager for which you want to await termination
	 */
	public static void awaitTermination(CompilerManager manager) {
		try {
			manager.awaitTermination();
		}
		catch (InterruptedException e) {
			Log.warning("Interrupted while waiting for compiler to finish", e);
		}
	}
}
