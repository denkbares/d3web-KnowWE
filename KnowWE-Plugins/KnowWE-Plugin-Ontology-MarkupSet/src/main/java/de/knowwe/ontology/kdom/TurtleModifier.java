/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.kdom;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Statement;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.parsing.Sections.ReplaceResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * This class allows to configure a change to the turtle markups of the whole
 * wiki. It works similar to {@link ArticleTurtleModifier} class, but is not
 * restricted to change the content of a single article.
 * <p>
 * The turtle modifier requires a defined core that defines the source articles
 * to be modified by this TurtleModifier. As a result the (newly) compiled core
 * will reflect the desired changes to its statements. Please note that other
 * cores may be affected by the changes as well, as they compile the same turtle
 * statements.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 17.12.2013
 */
public class TurtleModifier {

	private final Rdf2GoCompiler compiler;
	private final boolean compactMode;
	private final String preferredIndent;
	private final Map<Article, ArticleTurtleModifier> modifiers = new HashMap<>();

	/**
	 * Creates a new TurtleModifier that will change the wiki articles compiled
	 * to the the specified triple store <code>core</code>. All changes will be
	 * applied only to sections and articles being compiled by that core. As a
	 * result the specified core will be recompiled and reflect the requested
	 * changes in its triple store.
	 * <p>
	 * Note that it is not possible to remove statements from a core, that is
	 * not based on some compiled turtle markup.
	 * <p>
	 * By default, the created turtle modifier will use compact mode and two
	 * space characters as indent.
	 * 
	 * @param core the core that compiles the turtle to be modified
	 */
	public TurtleModifier(Rdf2GoCompiler core) {
		this(core, true);
	}

	/**
	 * Creates a new TurtleModifier that will change the wiki articles compiled
	 * to the the specified triple store <code>core</code>. All changes will be
	 * applied only to sections and articles being compiled by that core. As a
	 * result the specified core will be recompiled and reflect the requested
	 * changes in its triple store.
	 * <p>
	 * Note that it is not possible to remove statements from a core, that is
	 * not based on some compiled turtle markup.
	 * <p>
	 * By default, the created turtle modifier will two space characters as
	 * indent.
	 * 
	 * @param core the core that compiles the turtle to be modified
	 * @param compactMode specifies if newly inserted turtle markup should be
	 *        created compact (prefer single-line-mode) or verbose (prefer
	 *        readability with line-breaks for each property and value, using
	 *        indenting).
	 */
	public TurtleModifier(Rdf2GoCompiler core, boolean compactMode) {
		this(core, compactMode, "  ");
	}

	/**
	 * Creates a new TurtleModifier that will change the wiki articles compiled
	 * to the the specified triple store <code>core</code>. All changes will be
	 * applied only to sections and articles being compiled by that core. As a
	 * result the specified core will be recompiled and reflect the requested
	 * changes in its triple store.
	 * <p>
	 * Note that it is not possible to remove statements from a core, that is
	 * not based on some compiled turtle markup.
	 * 
	 * @param core the core that compiles the turtle to be modified
	 * @param compactMode specifies if newly inserted turtle markup should be
	 *        created compact (prefer single-line-mode) or verbose (prefer
	 *        readability with line-breaks for each property and value, using
	 *        indenting).
	 * @param preferredIndent the preferred indent to be used, should consist of
	 *        spaces and tab characters only
	 */
	public TurtleModifier(Rdf2GoCompiler core, boolean compactMode, String preferredIndent) {
		this.compiler = core;
		this.compactMode = compactMode;
		this.preferredIndent = preferredIndent;
	}

	/**
	 * Creates a new turtle modifier as a (deep) copy of the specified modifier.
	 * 
	 * @param modifier the turtle modifier to be copied
	 */
	public TurtleModifier(TurtleModifier modifier) {
		this(modifier.compiler, modifier.compactMode, modifier.preferredIndent);
		addAll(modifier);
	}

	/**
	 * Returns the core used by this turtle modifier.
	 * 
	 * @created 20.12.2013
	 * @return the core of this modifier
	 */
	public Rdf2GoCore getCore() {
		return compiler.getRdf2GoCore();
	}

	/**
	 * Adds all the statements to be inserted and to be deleted from the
	 * specified turtle modifier to this modifier. The specified modifier
	 * remains unchanged.
	 * 
	 * @created 20.12.2013
	 * @param other the turtle modifier to add all statements from
	 */
	public void addAll(TurtleModifier other) {
		for (ArticleTurtleModifier modifier : other.modifiers.values()) {
			getModifier(modifier.getArticle()).addAll(modifier);
		}
	}

	/**
	 * Adds a number of statements that shall be inserted into the turtle
	 * markups of the specified wiki article.
	 * 
	 * @created 09.12.2013
	 * @param target the article to add the statements to
	 * @param statements the statements to be added to the article
	 */
	public void addInsert(Article target, Statement... statements) {
		if (statements == null) return;
		addInsert(target, Arrays.asList(statements));
	}

	/**
	 * Adds a number of statements that shall be inserted into the turtle
	 * markups of the specified wiki article.
	 * 
	 * @created 09.12.2013
	 * @param target the article to add the statements to
	 * @param statements the statements to be added to the article
	 */
	public void addInsert(Article target, List<Statement> statements) {
		if (statements == null) return;
		if (statements.isEmpty()) return;
		getModifier(target).addInsert(statements);
	}

	/**
	 * Defines a number of statements that shall be deleted from all turtle
	 * markups compiled by this turtle modifiers core.
	 * 
	 * @created 09.12.2013
	 * @param statements the statements to be deleted from the markups
	 */
	public void addDelete(Statement... statements) {
		if (statements == null) return;
		addDelete(Arrays.asList(statements));
	}

	/**
	 * Defines a number of statements that shall be deleted from all turtle
	 * markups compiled by this turtle modifiers core.
	 * 
	 * @created 09.12.2013
	 * @param statements the statements to be deleted from the markups
	 */
	public void addDelete(List<Statement> statements) {
		if (statements == null) return;
		if (statements.isEmpty()) return;
		for (Statement statement : statements) {
			for (Article article : compiler.getRdf2GoCore().getSourceArticles(statement)) {
				getModifier(article).addDelete(statement);
			}
		}
	}

	/**
	 * Returns the turtle modifier for the specified article. The returned
	 * modifier is created lazy if required.
	 * 
	 * @created 17.12.2013
	 */
	private ArticleTurtleModifier getModifier(Article article) {
		ArticleTurtleModifier modifier = modifiers.get(article);
		if (modifier == null) {
			modifier = new ArticleTurtleModifier(compiler, article, compactMode, preferredIndent);
			modifiers.put(article, modifier);
		}
		return modifier;
	}

	/**
	 * Commits all configured changes to the wiki. After the commit the object
	 * should not be modified or re-committed. The required changes are going to
	 * be performed with the specified user context.
	 * <p>
	 * If multiple turtle modifiers are applied
	 * 
	 * @created 17.12.2013
	 * @param context the user context used to modify the articles
	 * @throws IOException if this TurtleModifier was not capable to write the
	 *         changes to the wiki
	 * @throws SecurityException if the specified user is not allowed to change
	 *         the articles to be modified (or any of these articles)
	 */
	public ReplaceResult commit(UserContext context) throws IOException, SecurityException {
		// check is all modifiers can apply their changes completely
		for (ArticleTurtleModifier modifier : modifiers.values()) {
			Article article = modifier.getArticle();
			List<Statement> ignored = modifier.getIgnoredStatements();
			if (!ignored.isEmpty()) {
				throw new IOException(
						"cannot remove the statements " + ignored + " from article "
								+ article.getTitle());
			}
			if (!KnowWEUtils.canWrite(article, context)) {
				throw new SecurityException(
						"you are not allowed to modify article " + article.getTitle());
			}
		}

		// to modify all contained articles at once,
		// build a map of changing sections
		Map<String, String> replaceMap = new HashMap<>();
		for (ArticleTurtleModifier modifier : modifiers.values()) {
			String id = modifier.getArticle().getRootSection().getID();
			String newText = modifier.getResultText();
			replaceMap.put(id, newText);
		}

		// apply all changes to the articles
		return Sections.replace(context, replaceMap);
	}

	/**
	 * Returns the set of all articles that are modified by this turtle
	 * modifier.
	 * 
	 * @created 17.12.2013
	 * @return the articles modified (or going to be modified)
	 */
	public Set<Article> getArticles() {
		return modifiers.keySet();
	}

}
