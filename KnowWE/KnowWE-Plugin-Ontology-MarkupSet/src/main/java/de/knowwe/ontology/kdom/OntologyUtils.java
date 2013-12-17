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
package de.knowwe.ontology.kdom;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.utils.Patterns;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.ontology.kdom.namespace.AbbreviationPrefixReference;
import de.knowwe.ontology.kdom.turtle.TurtleMarkup;
import de.knowwe.ontology.kdom.turtle.TurtlePredSentence;
import de.knowwe.ontology.kdom.turtle.TurtlePredicate;
import de.knowwe.ontology.kdom.turtle.TurtleSentence;
import de.knowwe.ontology.kdom.turtle.TurtleSubject;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.02.2013
 */
public class OntologyUtils {

	public static final String RESOURCE_PATTERN = "(?:" + Patterns.QUOTED + "|[^\" ]+)";

	public static final String ABBREVIATED_RESOURCE_PATTERN = "(?:" +
			AbbreviationPrefixReference.ABBREVIATION_PREFIX_PATTERN + ")?" + RESOURCE_PATTERN;

	public static final SectionFinder ABBREVIATED_RESOURCE_FINDER = new ConstraintSectionFinder(
			new RegexSectionFinder(ABBREVIATED_RESOURCE_PATTERN),
			AtMostOneFindingConstraint.getInstance());

	/**
	 * Adds the specified statements to the the specified wiki page. As a result
	 * a new wiki page content is returned with the statements included in some
	 * turtle markup.
	 * <p>
	 * The statements will be included as seamless as possible. This means if
	 * there are any turtle markup, no new markup section will be created. If
	 * there is any turtle with the same subject it will be included in the
	 * subjects relation list. If there is any turtle for the same subject and
	 * relation the object is included in the values list.
	 * <p>
	 * If the article is null, the text for an article to be created is
	 * returned.
	 * 
	 * @created 23.11.2013
	 * @param article the article to integrate the statements into
	 * @param compactMode specifies if the created markup shall be kept compact
	 *        or more structured using line breaks and intends.
	 * @param statements the statements to be integrated
	 * @return the article's content, extended with the statements
	 */
	public static String addTurtle(Article article, boolean compactMode, Statement... statements) {
		TurtleWriter writer = new TurtleWriter(article, compactMode);
		writer.addToArticle(statements);
		return writer.getResultText();
	}

	/**
	 * Adds and deletes the specified statements to/from the the specified wiki
	 * page. As a result a new wiki page content is returned with the statements
	 * included in some turtle markup, and the existing turtle markup to be
	 * modified so that the removed statements are no longer existing on this
	 * article.
	 * <p>
	 * The statements will be included as seamless as possible. This means if
	 * there are any turtle markup, no new markup section will be created. If
	 * there is any turtle with the same subject it will be included in the
	 * subjects relation list. If there is any turtle for the same subject and
	 * relation the object is included in the values list.
	 * <p>
	 * The statements to be removed are removed at every occurrence of the
	 * article. If there are multiple occurrences of the same statement, all
	 * occurrences will be deleted.
	 * <p>
	 * If the article is null, the text for an article to be created is
	 * returned.
	 * 
	 * @created 23.11.2013
	 * @param article the article to integrate the statements into
	 * @param compactMode specifies if the created markup shall be kept compact
	 *        or more structured using line breaks and intends.
	 * @param statementsToAdd the statements to be integrated
	 * @param statementsToRemove the statements to be deleted from the article
	 * @return the article's content, extended with the statements
	 */
	public static String modifyTurtle(Article article, boolean compactMode, Statement[] statementsToAdd, Statement[] statementsToRemove) {
		return modifyTurtle(article, compactMode,
				Arrays.asList(statementsToAdd), Arrays.asList(statementsToRemove));
	}

	/**
	 * Adds and deletes the specified statements to/from the the specified wiki
	 * page. As a result a new wiki page content is returned with the statements
	 * included in some turtle markup, and the existing turtle markup to be
	 * modified so that the removed statements are no longer existing on this
	 * article.
	 * <p>
	 * The statements will be included as seamless as possible. This means if
	 * there are any turtle markup, no new markup section will be created. If
	 * there is any turtle with the same subject it will be included in the
	 * subjects relation list. If there is any turtle for the same subject and
	 * relation the object is included in the values list.
	 * <p>
	 * The statements to be removed are removed at every occurrence of the
	 * article. If there are multiple occurrences of the same statement, all
	 * occurrences will be deleted.
	 * <p>
	 * If the article is null, the text for an article to be created is
	 * returned.
	 * 
	 * @created 23.11.2013
	 * @param article the article to integrate the statements into
	 * @param compactMode specifies if the created markup shall be kept compact
	 *        or more structured using line breaks and intends.
	 * @param statementsToAdd the statements to be integrated
	 * @param statementsToRemove the statements to be deleted from the article
	 * @return the article's content, extended with the statements
	 */
	public static String modifyTurtle(Article article, boolean compactMode, List<Statement> statementsToAdd, List<Statement> statementsToRemove) {
		ArticleTurtleModifier writer = new ArticleTurtleModifier(article, compactMode);
		writer.addInsert(statementsToAdd);
		writer.addDelete(statementsToRemove);
		return writer.getResultText();
	}

	/**
	 * Returns the first turtle predicate sentence of the specified article that
	 * is a predicate sentence for the specified subject and the specified
	 * predicate. If there is no such sentence, null is returned.
	 * 
	 * @created 24.11.2013
	 * @param article the article to be checked for the sentence
	 * @param subject the subject to search for
	 * @param predicate the predicate to search for
	 * @return the sentence for the subject and predicate
	 */
	public static Section<TurtlePredSentence> findSentence(Article article, Resource subject, URI predicate) {
		List<Section<TurtlePredSentence>> sentences = findSentences(article, subject, predicate);
		if (sentences.isEmpty()) return null;
		return sentences.get(0);
	}

	/**
	 * Returns the all turtle predicate sentences of the specified article that
	 * are predicate sentences for the specified subject and the specified
	 * predicate. If there is no such sentences, an empty list is returned.
	 * 
	 * @created 24.11.2013
	 * @param article the article to be checked for the sentence
	 * @param subject the subject to search for
	 * @param predicate the predicate to search for
	 * @return the predicate sentences for the subject and predicate
	 */
	public static List<Section<TurtlePredSentence>> findSentences(Article article, Resource subject, URI predicate) {
		List<Section<TurtlePredSentence>> result = new LinkedList<Section<TurtlePredSentence>>();

		// check all sentences of the specified subject
		for (Section<TurtleSentence> sentence : findSentences(article, subject)) {
			// check all predicate sentences within these sentences
			for (Section<TurtlePredSentence> predSentence : Sections.findSuccessorsOfType(sentence,
					TurtlePredSentence.class)) {
				Section<TurtlePredicate> otherPredicate = Sections.findSuccessor(predSentence,
						TurtlePredicate.class);
				if (otherPredicate == null) continue;
				if (otherPredicate.getText().equals(predicate.toString())) {
					// we found the same subject, so add to result
					result.add(predSentence);
				}
			}
		}

		return result;
	}

	/**
	 * Returns the first turtle sentence of the specified article that is a
	 * sentence for the specified subject. If there is no such sentence, null is
	 * returned.
	 * 
	 * @created 24.11.2013
	 * @param article the article to be checked for the sentence
	 * @param subject the subject to search for
	 * @return the sentence for the subject and predicate
	 */
	public static Section<TurtleSentence> findSentence(Article article, Resource subject) {
		List<Section<TurtleSentence>> sentences = findSentences(article, subject);
		if (sentences.isEmpty()) return null;
		return sentences.get(0);
	}

	/**
	 * Returns the all turtle sentences of the specified article that are
	 * sentences for the specified subject. If there is no such sentences, an
	 * empty list is returned.
	 * 
	 * @created 24.11.2013
	 * @param article the article to be checked for the sentence
	 * @param subject the subject to search for
	 * @return the predicate sentences for the subject and predicate
	 */
	public static List<Section<TurtleSentence>> findSentences(Article article, Resource subject) {
		List<Section<TurtleSentence>> result = new LinkedList<Section<TurtleSentence>>();

		// check each turtle markup
		List<Section<TurtleMarkup>> turtles = Sections.findSuccessorsOfType(
				article.getRootSection(), TurtleMarkup.class);
		for (Section<TurtleMarkup> turtle : turtles) {
			// check each sentence of each markup
			List<Section<TurtleSentence>> sentences =
					Sections.findSuccessorsOfType(turtle, TurtleSentence.class);
			for (Section<TurtleSentence> sentence : sentences) {
				Section<TurtleSubject> otherSubject = Sections.findSuccessor(sentence,
						TurtleSubject.class);
				if (otherSubject == null) continue;
				if (otherSubject.getText().equals(subject.toString())) {
					// we found the same subject, so add to result
					result.add(sentence);
				}
			}
		}
		return result;
	}
}
