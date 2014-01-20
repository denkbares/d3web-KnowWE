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

import de.d3web.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
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
	public static Collection<Article> getCompilingArticleObjects(Section<?> section) {
		Collection<Article> articles = new ArrayList<Article>();
		Set<String> referingArticleTitles =
				KnowWEUtils.getPackageManager(section.getArticleManager()).getCompilingArticles(
						section);
		ArticleManager articleManager =
				KnowWEUtils.getArticleManager(section.getWeb());
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
	 * Returns the default {@link CompilerManager} of the given web.
	 * 
	 * @created 07.01.2014
	 * @param web the web we want the {@link CompilerManager} from
	 */
	public static CompilerManager getCompilerManager(String web) {
		return KnowWEUtils.getArticleManager(web).getCompilerManager();
	}

}
