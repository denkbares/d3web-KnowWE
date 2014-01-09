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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Utility methods needed while compiling.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 17.11.2013
 */
public class Compilers {

	/**
	 * Returns the article with the given web and title from the
	 * {@link DefaultArticleManager}.
	 * 
	 * @created 07.01.2014
	 */
	public static Article getArticle(String web, String title) {
		return Environment.getInstance().getArticle(web, title);
	}

	/**
	 * Compiles the given {@link Section} for the given {@link Compiler} (by
	 * calling the {@link CompileScript}s of the {@link Compiler} for the
	 * {@link Type} of the {@link Section} and applying them to the
	 * {@link Section}).
	 * 
	 * @created 07.01.2014
	 */
	public static <C extends Compiler, T extends Type> void compile(C compiler, Section<T> section) {

		ScriptManager<C> scriptManager = CompilerManager.getScriptManager(compiler);
		Map<Priority, List<CompileScript<C, T>>> scripts = scriptManager.getScripts(section.get());
		for (List<CompileScript<C, T>> scriptList : scripts.values()) {
			for (CompileScript<C, T> compileScript : scriptList) {
				compileScript.compile(compiler, section);
			}
		}
	}

	/**
	 * Destroys the given {@link Section} for the given {@link Compiler} (by
	 * calling the {@link CompileScript}s of the {@link Compiler} for the
	 * {@link Type} of the {@link Section} and applying them to the
	 * {@link Section}).
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
				((DestroyScript<C, T>) compileScript).destroy(compiler, section);
			}
		}
	}

	/**
	 * Returns the first {@link PackageCompiler} of the given compiler class,
	 * that compiles a {@link Section} of the type {@link PackageCompileType} on
	 * the given article.
	 * 
	 * @created 15.11.2013
	 * @param master the master article for which we want the {@link Compiler}
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @returns the first {@link Compiler} that compiles the given section.
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
	 * Returns the first {@link Compiler} with the given compiler class, that
	 * compiles the given section.
	 * 
	 * @created 15.11.2013
	 * @param section the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @returns the first {@link Compiler} that compiles the given section.
	 */
	public static <C extends Compiler> C getCompiler(Section<?> section, Class<C> compilerClass) {
		Collection<C> compilers = getCompilers(section, compilerClass);
		if (compilers.isEmpty()) return null;
		else return compilers.iterator().next();
	}

	/**
	 * Returns the first {@link Compiler} of a given ArticleManager and compiler
	 * class.
	 * 
	 * @created 15.11.2013
	 * @param manager the {@link ArticleManager} for which we want the
	 *        {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @returns the first {@link Compiler}s of a given ArticleManager and Class.
	 */
	public static <C extends Compiler> C getCompiler(ArticleManager manager, Class<C> compilerClass) {
		Collection<C> compilers = getCompilers(manager, compilerClass);
		if (compilers.isEmpty()) return null;
		else return compilers.iterator().next();
	}

	/**
	 * Returns all {@link Compiler}s with the given type that compile the given
	 * section.
	 * 
	 * @created 15.11.2013
	 * @param section the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @returns all {@link Compiler}s compiling the given section
	 */
	public static <C extends Compiler> Collection<C> getCompilers(Section<?> section, Class<C> compilerClass) {
		Set<C> compilers = new TreeSet<C>();
		List<Compiler> allCmpilers = section.getArticleManager().getCompilerManager().getCompilers();
		for (Compiler compiler : allCmpilers) {
			if (compilerClass.isAssignableFrom(compiler.getClass())
					&& compiler.isCompiling(section)) {
				compilers.add(compilerClass.cast(compiler));
			}
		}
		return compilers;
	}

	/**
	 * Returns all {@link Compiler}s of a given ArticleManager and class.
	 * 
	 * @created 15.11.2013
	 * @param section the section for which we want the {@link Compiler}s
	 * @param compilerClass the type of the {@link Compiler} we want
	 * @returns all {@link AbstractPackageCompiler}s compiling the given section
	 */
	public static <C extends Compiler> Collection<C> getCompilers(ArticleManager manager, Class<C> compilerClass) {
		Set<C> compilers = new TreeSet<C>();
		List<Compiler> allCmpilers = manager.getCompilerManager().getCompilers();
		for (Compiler compiler : allCmpilers) {
			if (compilerClass.isAssignableFrom(compiler.getClass())) {
				compilers.add(compilerClass.cast(compiler));
			}
		}
		return compilers;
	}

	/**
	 * @deprecated Helper method while we transition to new compiler framework
	 * 
	 * @created 15.11.2013
	 */
	@Deprecated
	public static Article getArticle(Compiler compiler, Section<?> section) {
		if (compiler == null) {
			return null;
		}
		if (compiler instanceof AbstractPackageCompiler) {
			return ((AbstractPackageCompiler) compiler).getCompileSection().getArticle();
		}
		else if (compiler instanceof DefaultGlobalCompiler) {
			return null;
		}
		else {
			throw new IllegalArgumentException("Unknown compiler, implement it!");
		}
	}

	/**
	 * @deprecated Helper method while we transition to new compiler framework
	 * 
	 * @created 15.11.2013
	 */
	@Deprecated
	public static Collection<Article> getCompilingArticleObjects(Section<?> section) {
		Collection<Article> articles = new ArrayList<Article>();
		Set<String> referingArticleTitles =
				Compilers.getPackageManager(section.getArticleManager()).getCompilingArticles(
						section);
		ArticleManager articleManager =
				Compilers.getArticleManager(section.getWeb());
		for (String title : referingArticleTitles) {
			Article article =
					Article.getCurrentlyBuildingArticle(section.getWeb(), title);
			if (article == null) article = articleManager.getArticle(title);
			if (article == null) continue;
			articles.add(article);
		}
		return articles;
	}

	/**
	 * Returns all master articles that compile the given Section. If no master
	 * article compiles the Section, at least the article of the Section itself
	 * is returned, so the Collection always at least contains one article.
	 * 
	 * @deprecated Helper method while we transition to new compiler framework
	 * @created 16.02.2012
	 * @param section is the Section for which you want to know the compiling
	 *        articles
	 * @return a non empty Collection of articles that compile the given Section
	 */
	@Deprecated
	public static Collection<Article> getCompilingArticles(Section<?>
			section) {
		Collection<Article> articles = getCompilingArticleObjects(section);
		if (articles.isEmpty()) articles.add(section.getArticle());
		return articles;
	}

	/**
	 * Returns the TerminologyManagers of the {@link TermCompiler}s compiling
	 * the give compile Section.
	 * 
	 * @created 15.11.2013
	 * @param section the section to get the TerminologyManagers for
	 * @return the {@link TerminologyManager}s of the given section
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(Section<?> section) {
		Collection<TermCompiler> compilers = getCompilers(section, TermCompiler.class);
		return getTerminologyManagers(compilers);
	}

	/**
	 * Returns all TerminologyManagers of the given {@link ArticleManager}.
	 * 
	 * @created 15.11.2013
	 * @param manager the {@link ArticleManager} to get the TerminologyManagers
	 *        for
	 * @return the {@link TerminologyManager}s of the given manager
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(ArticleManager manager) {
		Collection<TermCompiler> compilers = getCompilers(manager, TermCompiler.class);
		return getTerminologyManagers(compilers);
	}

	/**
	 * Returns all TerminologyManagers of the given {@link ArticleManager} that
	 * are from {@link Compiler} of the given compiler class.
	 * 
	 * @created 15.11.2013
	 * @param manager the {@link ArticleManager} to get the TerminologyManagers
	 *        for
	 * @param compilerClass the type of the {@link Compiler}s we want the
	 *        {@link TerminologyManager}s from
	 * @return the {@link TerminologyManager}s of the given manager
	 */
	public static Collection<TerminologyManager> getTerminologyManagers(ArticleManager manager, Class<? extends TermCompiler> compilerClass) {
		Collection<? extends TermCompiler> compilers = getCompilers(manager, compilerClass);
		return getTerminologyManagers(compilers);
	}

	private static Collection<TerminologyManager> getTerminologyManagers(Collection<? extends TermCompiler> compilers) {
		Collection<TerminologyManager> managers = new ArrayList<TerminologyManager>(
				compilers.size());
		for (TermCompiler packageCompiler : compilers) {
			managers.add(packageCompiler.getTerminologyManager());
		}
		return managers;
	}

	/**
	 * Returns the default {@link CompilerManager} of the given web.
	 * 
	 * @created 07.01.2014
	 * @param web the web we want the {@link CompilerManager} from
	 */
	public static CompilerManager getCompilerManager(String web) {
		return Compilers.getArticleManager(web).getCompilerManager();
	}

	/**
	 * Returns the default {@link ArticleManager} of the given web.
	 * 
	 * @created 07.01.2014
	 * @param web the web we want the {@link ArticleManager} from
	 */
	public static ArticleManager getArticleManager(String web) {
		return Environment.getInstance().getArticleManager(web);
	}

	/**
	 * @deprecated use the method
	 *             {@link Compilers#getTerminologyManagers(Section)}
	 * @return the {@link TerminologyManager} for the given (master) article.
	 */
	@Deprecated
	public static TerminologyManager getTerminologyManager(Article article) {
		if (article == null) {
			return Compilers.getCompiler(getArticleManager(Environment.DEFAULT_WEB),
					DefaultGlobalCompiler.class).getTerminologyManager();
		}
		Section<PackageCompileType> compileSection = Sections.findSuccessor(
				article.getRootSection(), PackageCompileType.class);
		// to emulate old behavior (not return null) we return an empty
		// TerminologyManager
		if (compileSection == null) return new TerminologyManager();
		Collection<TerminologyManager> terminologyManagers = getTerminologyManagers(compileSection);
		if (terminologyManagers.isEmpty()) return null;
		return terminologyManagers.iterator().next();
	}

	/**
	 * Returns the default {@link PackageManager} of the given web.
	 * 
	 * @created 07.01.2014
	 * @param web the web we want the {@link PackageManager} from
	 */
	public static PackageManager getPackageManager(String web) {
		return getPackageManager(getArticleManager(web));
	}

	/**
	 * Returns the {@link PackageManager} for the given {@link Section}.
	 * 
	 * @created 07.01.2014
	 * @param section the {@link Section} we want the {@link PackageManager} for
	 */
	public static PackageManager getPackageManager(Section<?> section) {
		return getPackageManager(section.getArticleManager());
	}

	/**
	 * Returns the {@link PackageManager} of the given {@link ArticleManager}.
	 * 
	 * @created 07.01.2014
	 * @param manager the ArticleManager we want the {@link PackageManager} from
	 */
	public static PackageManager getPackageManager(ArticleManager manager) {
		Collection<PackageRegistrationCompiler> compilers = getCompilers(manager,
				PackageRegistrationCompiler.class);
		if (compilers.isEmpty()) return null;
		return compilers.iterator().next().getPackageManager();
	}

	/**
	 * 
	 * @deprecated use the method
	 *             {@link Compilers#getTerminologyManagers(ArticleManager, Class)}
	 * @return the {@link TerminologyManager} that handles global terms for this
	 *         web (similar to the former {@link TermRegistrationScope#GLOBAL}).
	 */
	@Deprecated
	public static TerminologyManager getGlobalTerminologyManager(String web) {
		return Compilers.getCompiler(getArticleManager(web), DefaultGlobalCompiler.class).getTerminologyManager();
	}

	public static void storeObject(Section<?> s, String key, Object o) {
		Compilers.storeObject((Compiler) null, s, key, o);
	}

	public static void storeObject(Compiler compiler, Section<?> s, String key, Object o) {
		s.getSectionStore().storeObject(compiler, key, o);
	}

	public static Object getStoredObject(Section<?> s, String key) {
		return Compilers.getStoredObject((Compiler) null, s, key);
	}

	public static Object getStoredObject(Compiler compiler, Section<?> s, String key) {
		return s.getSectionStore().getObject(compiler, key);
	}

}
