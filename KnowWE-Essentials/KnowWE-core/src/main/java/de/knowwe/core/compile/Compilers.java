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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
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
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Utility methods needed while compiling.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 17.11.2013
 */
public class Compilers {

	/**
	 * Null safe comparator for compilers.
	 * For {@link NamedCompiler}s, name will be used.
	 * For {@link PackageCompiler}
	 */
	public static final Comparator<Compiler> COMPARATOR = Comparator.nullsFirst(Comparator.comparing(Compilers::comparable));

	private static String comparable(Compiler compiler) {
		if (compiler == null) return null;
		if (compiler instanceof NamedCompiler) return ((NamedCompiler) compiler).getName();
		if (compiler instanceof PackageCompiler) {
			Section<? extends PackageCompileType> compileSection = ((PackageCompiler) compiler).getCompileSection();
			return compileSection.getTitle() + compileSection.getPositionInKDOM();
		}
		return compiler.toString();
	}

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
		Collection<C> compilers = getCompilers(section, compilerClass, true);
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
		Collection<C> compilers = getCompilers(manager, compilerClass, true);
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
	 * Returns all {@link Compiler}s with the given type that compile the packages of the given section.
	 *
	 * @param section       the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @return all {@link Compiler}s compiling the given section
	 * @created 15.11.2013
	 */
	@NotNull
	public static <C extends Compiler> Collection<C> getCompilers(Section<?> section, Class<C> compilerClass) {
		return getCompilers(section, compilerClass, false);
	}

	private static <C extends Compiler> Collection<C> getCompilers(Section<?> section, Class<C> compilerClass, boolean firstOnly) {
		ArticleManager articleManager = section.getArticleManager();
		if (articleManager == null) { // can happen in preview
			articleManager = Environment.getInstance().getArticleManager(section.getWeb());
		}
		List<Compiler> allCompilers = articleManager.getCompilerManager().getCompilers();
		Collection<C> compilers = new ArrayList<>();
		for (Compiler compiler : allCompilers) {
			if (compilerClass.isInstance(compiler) && compiler.isCompiling(section)) {
				compilers.add(compilerClass.cast(compiler));
			}
		}
		// if we only want one compiler but there are multiple, make sure to check if the section is an ancestor
		// to a package compile section... if yes, we want the associated compiler
		if (firstOnly && compilers.size() > 1) {
			Section<PackageCompileType> compileSection = Sections.successor(section, PackageCompileType.class);
			if (compileSection != null) {
				Collection<PackageCompiler> packageCompilers = compileSection.get().getPackageCompilers(compileSection);
				for (PackageCompiler packageCompiler : packageCompilers) {
					if (compilerClass.isInstance(packageCompiler)) {
						return Collections.singletonList(compilerClass.cast(packageCompiler));
					}
				}
			}
		}
		return compilers;
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
		Collection<C> compilers = getCompilers(section, compilerClass, false);
		ArrayList<C> filteredCompilers = new ArrayList<>(compilers.size());
		for (C compiler : compilers) {
			if (!CompilerManager.getScriptManager(compiler).getScripts(section.get()).isEmpty()) {
				filteredCompilers.add(compiler);
			}
		}
		return filteredCompilers;
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
	public static <C extends Compiler> Collection<C> getCompilers(ArticleManager manager, Class<C> compilerClass) {
		return getCompilers(manager, compilerClass, false);
	}

	private static <C extends Compiler> Collection<C> getCompilers(ArticleManager manager, Class<C> compilerClass, boolean firstOnly) {
		List<Compiler> allCompilers = manager.getCompilerManager().getCompilers();
		Collection<C> compilers = new ArrayList<>();
		for (Compiler compiler : allCompilers) {
			if (compilerClass.isInstance(compiler)) {
				compilers.add(compilerClass.cast(compiler));
				if (firstOnly) break;
			}
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
		if (packageManager == null) return Collections.emptyList();
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
