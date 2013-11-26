package de.knowwe.ontology.kdom;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.kdom.turtle.TurtleObjectSection;
import de.knowwe.ontology.kdom.turtle.TurtlePredSentence;
import de.knowwe.ontology.kdom.turtle.TurtleSentence;

public class TurtleWriter {

	/**
	 * Class to specify some text to be inserted at a special position. The
	 * class is comparable to sort them backward for insertation.
	 * 
	 * @author Volker Belli (denkbares GmbH)
	 * @created 23.11.2013
	 */
	private static class Insert implements Comparable<Insert> {

		private final String text;
		private final int position;

		public Insert(String text, int position) {
			super();
			this.text = text;
			this.position = position;
		}

		@Override
		public int compareTo(Insert o) {
			return (position != o.position)
					? o.position - position
					: o.text.compareToIgnoreCase(text);
		}
	}

	private final boolean compactMode;
	private final String preferredIndent;

	public TurtleWriter() {
		this(true);
	}

	public TurtleWriter(boolean compactMode) {
		this(compactMode, "  ");
	}

	public TurtleWriter(boolean compactMode, String preferredIndent) {
		this.compactMode = compactMode;
		this.preferredIndent = preferredIndent;
	}

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
	 * @param statements the statements to be integrated
	 * @return the article's content, extended with the statements
	 */
	public String addTurtle(Article article, Statement... statements) {

		// prepare list of all text pieces to be inserted
		// by grouping statements by subject and process them
		List<Insert> inserts = new LinkedList<Insert>();
		List<Statement> unprocessed = new LinkedList<Statement>();
		if (article != null) {
			for (List<Statement> group : groupBySubject(statements)) {
				insertSubjectGroup(article, group, inserts, unprocessed);
			}
		}
		else {
			// if there is no article specified, newly create all statements
			Collections.addAll(unprocessed, statements);
		}

		String text = (article == null) ? "" : article.getText();

		// if there are any statements left (unprocessed)
		// create a markup for them
		if (!unprocessed.isEmpty()) {
			String turtle;
			int position;
			Section<TurtleSentence> last = (article == null) ? null :
					Sections.findLastSuccessor(article.getRootSection(), TurtleSentence.class);
			if (last == null) {
				// no turtle sentence in the article,
				// create new markup section at the end
				position = Strings.trimRight(text).length();
				turtle = "\n\n%%Turtle\n" + createTurtle(unprocessed, preferredIndent) + ".\n%\n";
			}
			else {
				// add statements as the last sentences
				int positionInArticle = last.getOffsetInArticle();
				position = positionInArticle + last.getText().length();
				String indent = getIndent(text, positionInArticle);
				turtle = (compactMode ? ".\n" : ".\n\n") + indent
						+ createTurtle(unprocessed, indent);
			}
			inserts.add(new Insert(turtle, position));
		}

		// insert into text from backwards (to keep insert indexes stable)
		Collections.sort(inserts);
		for (Insert insert : inserts) {
			int pos = insert.position;
			text = text.substring(0, pos) + insert.text + text.substring(pos);
		}

		return text;
	}

	/**
	 * Create turtle markup for a set of statements that share the same subject
	 * and predicate. The markup will be generated as a list of objects to be
	 * injected into an existing subject+predicate sentence.
	 * <p>
	 * Depending on 'compactMode' the objects will be separated by a line-break
	 * or not.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be inserted, sharing the same subject
	 *        and predicate
	 * @param indent the indent to be used if not in compact mode
	 * @return the created turtle text
	 */
	private String createObjectTurtle(List<Statement> statements, String indent) {
		StringBuilder turtle = new StringBuilder();
		createObjectTurtle(statements, indent, turtle);
		return turtle.toString();
	}

	/**
	 * Create turtle markup for a set of statements that share the same subject
	 * and predicate. The markup will be generated as a list of objects to be
	 * injected into an existing subject+predicate sentence.
	 * <p>
	 * Depending on 'compactMode' the objects will be separated by a line-break
	 * or not.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be inserted, sharing the same subject
	 *        and predicate
	 * @param indent the indent to be used if not in compact mode
	 * @param turtle the buffer to append the created turtle text
	 */
	private void createObjectTurtle(List<Statement> statements, String indent, StringBuilder turtle) {
		boolean first = true;
		for (Statement statement : statements) {
			// add object separator
			if (first) first = false;
			else if (compactMode) turtle.append(", ");
			else turtle.append(",\n").append(indent);
			// add object itself
			turtle.append(toTurtle(statement.getObject()));
		}
	}

	/**
	 * Create turtle markup for a set of statements that share the same subject
	 * and predicate. The markup will be generated as a predicate and a list of
	 * objects to be injected into an existing subject sentence.
	 * <p>
	 * Depending on 'compactMode' the objects of the statement will be separated
	 * by a line-break or not.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be inserted, sharing the same subject
	 *        and predicate
	 * @param indent the indent to be used if not in compact mode
	 * @return the created turtle text
	 */
	private String createPredicateTurtle(List<Statement> statements, String indent) {
		StringBuilder turtle = new StringBuilder();
		createPredicateTurtle(statements, indent, turtle);
		return turtle.toString();
	}

	/**
	 * Create turtle markup for a set of statements that share the same subject
	 * and predicate. The markup will be generated as a predicate and a list of
	 * objects to be injected into an existing subject sentence.
	 * <p>
	 * Depending on 'compactMode' the objects of the statement will be separated
	 * by a line-break or not.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be inserted, sharing the same subject
	 *        and predicate
	 * @param indent the indent to be used if not in compact mode
	 * @param turtle the buffer to append the created turtle text
	 */
	private void createPredicateTurtle(List<Statement> statements, String indent, StringBuilder turtle) {
		String objectIndent = indent + "  ";

		// append predicate and spacing to objects
		URI predicate = statements.get(0).getPredicate();
		turtle.append(toTurtle(predicate));
		if (compactMode) turtle.append(" ");
		else turtle.append("\n").append(objectIndent);

		// append the list of objects
		createObjectTurtle(statements, objectIndent, turtle);
	}

	// private static String createSubjectTurtle(List<Statement> statements,
	// String indent) {
	// StringBuilder turtle = new StringBuilder();
	// createSubjectTurtle(statements, indent, turtle);
	// return turtle.toString();
	// }

	/**
	 * Create turtle markup for a set of statements that share the same subject.
	 * The markup will be generated as a list of predicates and a list of
	 * objects for each predicate. The result is intended to be injected into an
	 * existing turtle markup.
	 * <p>
	 * Depending on 'compactMode' the predicates and objects of the statement
	 * will be separated by a line-break or not.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be inserted, sharing the same subject
	 * @param indent the indent to be used for turtle sentences
	 * @param turtle the buffer to append the created turtle text
	 */
	private void createSubjectTurtle(List<Statement> statements, String indent, StringBuilder turtle) {
		String predicateIndent = indent + "  ";

		// append subject and seperator to first predicate
		Resource subject = statements.get(0).getSubject();
		turtle.append(toTurtle(subject));
		if (compactMode) turtle.append(" ");
		else turtle.append("\n").append(predicateIndent);

		boolean first = true;
		for (List<Statement> group : groupByPredicate(statements)) {
			// add object separator
			if (first) first = false;
			else if (compactMode) turtle.append(";\n").append(predicateIndent);
			else turtle.append(";\n\n").append(predicateIndent);
			// render each predicate
			createPredicateTurtle(group, predicateIndent, turtle);
		}
	}

	private String createTurtle(List<Statement> statements, String indent) {
		StringBuilder turtle = new StringBuilder();
		createTurtle(statements, indent, turtle);
		return turtle.toString();
	}

	/**
	 * Create turtle markup for a set of statements. The statements will be
	 * grouped by subjects and predicates to create minimal verbose turtle
	 * markup. The markup will be generated as a list of turtle sentences. Each
	 * sentence has a list of predicates and a list of objects for each
	 * predicate. The result is intended to be injected into an existing turtle
	 * markup.
	 * <p>
	 * Depending on 'compactMode' the predicates and objects of the statement
	 * will be separated by a line-break or not. Each turtle sentence is created
	 * in a new line, but 'compactMode' also influences the spacing between the
	 * individual lines.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be inserted
	 * @param indent the indent to be used for the turtle sentences
	 * @param turtle the buffer to append the created turtle text
	 */
	private void createTurtle(List<Statement> statements, String indent, StringBuilder turtle) {
		boolean first = true;
		for (List<Statement> group : groupBySubject(statements)) {
			if (first) first = false;
			else if (compactMode) turtle.append(".\n").append(indent);
			else turtle.append(".\n\n").append(indent);
			createSubjectTurtle(group, indent, turtle);
		}
	}

	/**
	 * Returns the indent of the line within the specified text, pointed to by
	 * the specified index.
	 * 
	 * @created 26.11.2013
	 * @param text the text to get the indent
	 * @param index the character position to identify the line to get the
	 *        indent for
	 * @return the indent of the line specified by the index
	 */
	private String getIndent(String text, int index) {
		// search start of line
		while (index > 0 && "\r\n".indexOf(text.charAt(index - 1)) == -1)
			index--;
		// proceed while having white-spaces
		int indent = 0;
		while (index + indent < text.length() && Strings.isBlank(text.charAt(index + indent)))
			indent++;
		return text.substring(index, index + indent);
	}

	/**
	 * Group a set of statements by their predicates.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be grouped
	 * @return a collection of non-empty statement lists sharing the same
	 *         predicate
	 */
	private Collection<List<Statement>> groupByPredicate(Collection<Statement> statements) {
		Map<URI, List<Statement>> result = new LinkedHashMap<URI, List<Statement>>();
		for (Statement statement : statements) {
			URI predicate = statement.getPredicate();
			List<Statement> list = result.get(predicate);
			if (list == null) {
				list = new LinkedList<Statement>();
				result.put(predicate, list);
			}
			list.add(statement);
		}
		return result.values();
	}

	/**
	 * Group a set of statements by their subject.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be grouped
	 * @return a collection of non-empty statement lists sharing the same
	 *         subject
	 */
	private Collection<List<Statement>> groupBySubject(Collection<Statement> statements) {
		Map<Resource, List<Statement>> result = new LinkedHashMap<Resource, List<Statement>>();
		for (Statement statement : statements) {
			Resource subject = statement.getSubject();
			List<Statement> list = result.get(subject);
			if (list == null) {
				list = new LinkedList<Statement>();
				result.put(subject, list);
			}
			list.add(statement);
		}
		return result.values();
	}

	/**
	 * Group a set of statements by their subject.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be grouped
	 * @return a collection of non-empty statement lists sharing the same
	 *         subject
	 */
	private Collection<List<Statement>> groupBySubject(Statement[] statements) {
		return groupBySubject(Arrays.asList(statements));
	}

	/**
	 * Method to create an insert for a number of statements that share the same
	 * subject and predicate. The list must contain at least one statement.
	 * 
	 * @created 23.11.2013
	 * @param article the article to create the inserts for
	 * @param statements the list of statements that share the same subject
	 * @return the insert required to add the statements or null if they could
	 *         not been inserted in an existing markup
	 */
	private Insert insertPredicateGroup(Article article, List<Statement> statements) {
		Resource subject = statements.get(0).getSubject();
		URI predicate = statements.get(0).getPredicate();

		// first try to insert into a list of objects,
		// if there is already a turtle with this subject and predicate
		Section<TurtlePredSentence> predSentence =
				OntologyUtils.findSentence(article, subject, predicate);
		if (predSentence != null) {
			// add to the end of the list
			Section<TurtleObjectSection> last =
					Sections.findLastSuccessor(predSentence, TurtleObjectSection.class);
			if (last != null) {
				// get indent of predicate
				String indent = getIndent(article.getText(), predSentence.getOffsetInArticle());
				// create turtle text to be inserted
				String turtle = "," + createObjectTurtle(statements, indent + "  ");
				// create insert right after last object
				int position = last.getOffsetInArticle() + last.getText().length();
				return new Insert(turtle.toString(), position);
			}
		}

		// otherwise try to find a sentence with the same subject
		// and add the statements there as a new predicate sentence
		Section<TurtleSentence> subjectSentence = OntologyUtils.findSentence(article, subject);
		if (subjectSentence != null) {
			Section<TurtlePredSentence> last =
					Sections.findLastSuccessor(subjectSentence, TurtlePredSentence.class);
			if (last != null) {
				// get indent of predicate
				String indent = getIndent(article.getText(), last.getOffsetInArticle());
				// create turtle text to be inserted
				String turtle = ";" + createPredicateTurtle(statements, indent);
				// create insert right after last predicate
				int position = last.getOffsetInArticle() + last.getText().length();
				return new Insert(turtle, position);
			}
		}

		// otherwise we would not create a insert for this group
		return null;
	}

	/**
	 * Method to create inserts for a number of statements that share the same
	 * subject. The list must contain at least one statement. The created
	 * inserts are added to the specified result list. For all statements where
	 * no insert can be added (because there are no existing markups for that
	 * subject), the statements are added to the unprocessed list.
	 * 
	 * @created 23.11.2013
	 * @param article the article to create the inserts for
	 * @param statements the list of statements that share the same subject
	 * @param results the list where the created inserts for the statements
	 *        shall be added
	 * @param unprocessed a list where to add all unprocessed statements for
	 *        which no inserts have been created
	 */
	private void insertSubjectGroup(Article article, List<Statement> statements, List<Insert> results, List<Statement> unprocessed) {

		// group statements (of same subject) by and add them
		for (List<Statement> group : groupByPredicate(statements)) {
			Insert insert = insertPredicateGroup(article, group);
			if (insert != null) {
				// add to processed inserts if successful
				results.add(insert);
			}
			else {
				// if the group had not been inserted, remember these predicates
				// as failed
				unprocessed.addAll(group);
			}
		}
	}

	/**
	 * Creates turtle markup for the specified node.
	 * 
	 * @created 26.11.2013
	 * @param node the node to create turtle markup for
	 * @return the turtle markup to represent the node's value
	 */
	private String toTurtle(Node node) {
		if (node instanceof URI) return node.asURI().toString();
		if (node instanceof Literal) return node.asLiteral().toSPARQL();
		throw new IllegalArgumentException(
				"non-implemented conversion method for " + node.getClass());
	}

}
