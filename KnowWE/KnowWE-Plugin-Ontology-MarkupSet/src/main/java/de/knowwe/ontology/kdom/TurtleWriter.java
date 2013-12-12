package de.knowwe.ontology.kdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
import de.knowwe.rdf2go.Rdf2GoCore;

public class TurtleWriter {

	/**
	 * Class to specify some text to be replaced/inserted/deleted at a special
	 * position. The class is comparable to sort them backward before
	 * application. Thus no index positions have to be changed during
	 * applications of multiple changes.
	 * 
	 * @author Volker Belli (denkbares GmbH)
	 * @created 06.12.2013
	 */
	private static class Change implements Comparable<Change> {

		private final String insertText;
		private final int position;
		private int replaceLength;

		public Change(String insertText, int position, int replaceLength) {
			this.insertText = insertText;
			this.position = position;
			this.replaceLength = replaceLength;
		}

		@Override
		public int compareTo(Change o) {
			return (position != o.position)
					? o.position - position
					: o.insertText.compareToIgnoreCase(insertText);
		}

		public String applyTo(String text) {
			return text.substring(0, position)
					+ insertText
					+ text.substring(position + replaceLength);
		}
	}

	/**
	 * Class to specify some section text to be inserted at a special turtle
	 * subtree. Additionally this class knows about the section to be added as a
	 * child this text shall be inserted. It also knows the seperator to be used
	 * 
	 * @author Volker Belli (denkbares GmbH)
	 * @created 23.11.2013
	 */
	private static class Insert extends Change {

		public Insert(String text, int position) {
			super(text, position, 0);
		}
	}

	/**
	 * Class to specify some text range to be deleted.
	 * 
	 * @author Volker Belli (denkbares GmbH)
	 * @created 23.11.2013
	 */
	private static class Remove extends Change {

		public Remove(int position, int replaceLength) {
			super("", position, 0);
		}
	}

	private static class Statements {

		private final List<Statement> insertStatements = new LinkedList<Statement>();
		private final List<Statement> removeStatements = new LinkedList<Statement>();

		public void addInsert(Statement... statements) {
			if (statements != null) {
				Collections.addAll(this.insertStatements, statements);
			}
		}

		public void addInsert(List<Statement> statements) {
			if (statements != null) {
				this.insertStatements.addAll(statements);
			}
		}

		public void addRemove(Statement... statements) {
			if (statements != null) {
				Collections.addAll(this.removeStatements, statements);
			}
		}

		public void addRemove(List<Statement> statements) {
			if (statements != null) {
				this.removeStatements.addAll(statements);
			}
		}

		public void add(Statements statements) {
			addInsert(statements.insertStatements);
			addRemove(statements.removeStatements);
		}

		public List<Statement> removes() {
			return Collections.unmodifiableList(removeStatements);
		}

		public List<Statement> inserts() {
			return Collections.unmodifiableList(insertStatements);
		}

		public void clearInserts() {
			this.insertStatements.clear();
		}

		public Statement getSample() {
			if (hasInserts()) return insertStatements.get(0);
			if (hasRemoves()) return removeStatements.get(0);
			throw new NoSuchElementException("cannot get sample of empty Statements");
		}

		public boolean hasInserts() {
			return !insertStatements.isEmpty();
		}

		public boolean hasRemoves() {
			return !removeStatements.isEmpty();
		}
	}

	/*
	 * Some basic attributes for this writer to be initialized on startup
	 */
	private final Article article;
	private final boolean compactMode;
	private final String preferredIndent;

	/*
	 * The statements to be modified
	 */
	private final Statements statements = new Statements();

	/*
	 * Some cache variables required to process the statements to Changes
	 */
	private List<Insert> inserts = null;
	private List<Remove> removes = null;
	private Statements unprocessed = null;
	private Set<Section<?>> sectionsToDelete = null;
	private Set<Section<?>> sectionsToRetain = null;

	/**
	 * Creates a new {@link TurtleWriter} to modify the turtle statements of the
	 * specified wiki article.
	 * 
	 * @param article the wiki article to be modified
	 */
	public TurtleWriter(Article article) {
		this(article, true);
	}

	/**
	 * Creates a new {@link TurtleWriter} to modify the turtle statements of the
	 * specified wiki article.
	 * 
	 * @param article the wiki article to be modified
	 * @param compactMode if the turtle markup should be created compact (prefer
	 *        single-line-mode) or verbose (prefer readability with line-breaks
	 *        for each property and value, using indenting).
	 */
	public TurtleWriter(Article article, boolean compactMode) {
		this(article, compactMode, "  ");
	}

	/**
	 * Creates a new {@link TurtleWriter} to modify the turtle statements of the
	 * specified wiki article. You can specifiy if the output shall be compact
	 * or expressive/verbose and how to indent the particular turtle sentences
	 * if newly created.
	 * 
	 * @param article the wiki article to be modified
	 * @param compactMode if the turtle markup should be created compact (prefer
	 *        single-line-mode) or verbose (prefer readability with line-breaks
	 *        for each property and value, using indenting).
	 * @param the preferred indent to be used, should consist of spaces and tab
	 *        characters only
	 */
	public TurtleWriter(Article article, boolean compactMode, String preferredIndent) {
		this.article = article;
		this.compactMode = compactMode;
		this.preferredIndent = preferredIndent;
	}

	/**
	 * Adds the specified statements to the the wiki page of this instance. As a
	 * result the wiki page content returned by {@link #getResultText()} will
	 * include these statements in its turtle markup.
	 * <p>
	 * The statements will be included as seamless as possible. This means if
	 * there are any turtle markup, no new markup section will be created. If
	 * there is any turtle with the same subject it will be included in the
	 * subjects relation list. If there is any turtle for the same subject and
	 * relation the object is included in the values list.
	 * <p>
	 * If the article of this turtle writer is null, the text for an article to
	 * be created is returned.
	 * 
	 * @created 23.11.2013
	 * @param statements the statements to be integrated
	 */
	public void addToArticle(Statement... statements) {
		clearChanges();
		this.statements.addInsert(statements);
	}

	/**
	 * Removes the specified statements from the the wiki page of this instance.
	 * As a result the wiki page content returned by {@link #getResultText()}
	 * will no longer include these statements in its turtle markup.
	 * <p>
	 * The statements will be included as seamless as possible. This means if
	 * any predicate sentence will become empty the whole predicate sentence
	 * will be removed. If there is no longer a predicate sentence in a turtle
	 * sentence the whole turtle sentence will be removed as well. If, as a
	 * consequence, any turtle markup becomes empty, the whole markup section
	 * will be removed.
	 * 
	 * @created 23.11.2013
	 * @param statements the statements to be integrated
	 */
	public void removeFromArticle(Statement... statements) {
		clearChanges();
		this.statements.addRemove(statements);
	}

	private void clearChanges() {
		this.inserts = null;
		this.removes = null;
		this.unprocessed = null;
		this.sectionsToDelete = null;
		this.sectionsToRetain = null;
	}

	private void buildChanges() {
		// check if already processed
		if (this.inserts != null) return;

		// prepare build results to be filled
		this.inserts = new LinkedList<Insert>();
		this.removes = new LinkedList<Remove>();
		this.unprocessed = new Statements();
		this.sectionsToDelete = new HashSet<Section<?>>();
		this.sectionsToRetain = new HashSet<Section<?>>();

		// prepare list of all text pieces to be inserted
		// by grouping statements by subject and process them
		if (article != null) {
			for (Statements group : groupBySubject(statements)) {
				processSubjectGroup(group);
			}
		}
		else {
			// if there is no article specified, newly create all statements
			unprocessed.add(statements);
		}

		// if there are any statements left (unprocessed)
		// create a markup for them
		if (!unprocessed.inserts().isEmpty()) {
			String turtle;
			int position;
			Section<TurtleSentence> last = (article == null) ? null :
					Sections.findLastSuccessor(article.getRootSection(), TurtleSentence.class);
			if (last == null) {
				// no turtle sentence in the article,
				// create new markup section at the end
				position = (article == null) ? 0 : Strings.trimRight(article.getText()).length();
				turtle = "\n\n%%Turtle\n"
						+ createTurtle(unprocessed.inserts(), preferredIndent)
						+ ".\n%\n";
			}
			else {
				// add statements as the last sentences
				int positionInArticle = last.getOffsetInArticle();
				position = positionInArticle + last.getText().length();
				String indent = getIndent(positionInArticle);
				turtle = (compactMode ? ".\n" : ".\n\n") + indent
						+ createTurtle(unprocessed.inserts(), indent);
			}
			unprocessed.clearInserts();
			inserts.add(new Insert(turtle, position));
		}
	}

	/**
	 * Returns the result text of this {@link TurtleWriter} after applying all
	 * added and removed statements to the article of this instance. The
	 * returned text is intended to be used to store as the new article's text.
	 * Please note that this writer do not modify any article instance by their
	 * own.
	 * <p>
	 * The added statements will be included as seamless as possible. This means
	 * if there are any turtle markup, no new markup section will be created. If
	 * there is any turtle with the same subject it will be included in the
	 * subjects relation list. If there is any turtle for the same subject and
	 * relation the object is included in the values list.
	 * <p>
	 * If the article of this turtle writer is null, the text for an article to
	 * be created is returned.
	 * 
	 * @created 06.12.2013
	 * @return the resulting wiki text after applying all requested statement
	 *         changes
	 * @see #addTurtle(Statement...)
	 * @see #removeTurtle(Statement...)
	 */
	public String getResultText() {
		buildChanges();

		// insert into text from backwards (to keep insert indexes stable)
		List<Change> changes = new ArrayList<Change>(inserts.size() + removes.size());
		changes.addAll(inserts);
		changes.addAll(removes);
		Collections.sort(inserts);

		// and apply all changes
		String text = (article == null) ? "" : article.getText();
		for (Insert insert : inserts) {
			text = insert.applyTo(text);
		}

		return text;
	}

	/**
	 * Returns a list of statements that have not been considered when creating
	 * the result wiki text using {@link #getResultText()}. These statements are
	 * always a (hopefully empty) subset of the Statements to be removed,
	 * because no statements to be added will be ignored. If a statement is
	 * ignored, the reason is that the particular statement is not found on this
	 * turtle writer's article.
	 * <p>
	 * To check if all statements are implemented into the resulting wiki page
	 * the returned list must be empty!
	 * 
	 * @created 06.12.2013
	 * @return the list the non-removed statements that should have been removed
	 */
	List<Statement> getIgnoredStatements() {
		buildChanges();
		return unprocessed.removes();
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
	 * Returns the indent of the line within this turtle writer's article text,
	 * pointed to by the specified index.
	 * 
	 * @created 26.11.2013
	 * @param index the character position to identify the line to get the
	 *        indent for
	 * @return the indent of the line specified by the index
	 */
	private String getIndent(int index) {
		if (article == null) return preferredIndent;
		String text = article.getText();
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
	private Collection<Statements> groupByPredicate(Statements statements) {
		Map<Resource, List<Statement>> insertMap = mapByPredicate(statements.inserts());
		Map<Resource, List<Statement>> removeMap = mapByPredicate(statements.removes());
		return mergeGroups(insertMap, removeMap);
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
		Map<Resource, List<Statement>> result = mapByPredicate(statements);
		return result.values();
	}

	private Map<Resource, List<Statement>> mapByPredicate(Collection<Statement> statements) {
		Map<Resource, List<Statement>> result = new LinkedHashMap<Resource, List<Statement>>();
		for (Statement statement : statements) {
			URI predicate = statement.getPredicate();
			List<Statement> list = result.get(predicate);
			if (list == null) {
				list = new LinkedList<Statement>();
				result.put(predicate, list);
			}
			list.add(statement);
		}
		return result;
	}

	/**
	 * Group the specified statements by their subject.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be grouped
	 * @return a collection of non-empty statement lists sharing the same
	 *         subject
	 */
	private Collection<Statements> groupBySubject(Statements statements) {
		Map<Resource, List<Statement>> insertMap = mapBySubject(statements.inserts());
		Map<Resource, List<Statement>> removeMap = mapBySubject(statements.removes());
		return mergeGroups(insertMap, removeMap);
	}

	private Collection<Statements> mergeGroups(Map<Resource, List<Statement>> insertMap, Map<Resource, List<Statement>> removeMap) {
		Set<Resource> keys = new LinkedHashSet<Resource>();
		keys.addAll(insertMap.keySet());
		keys.addAll(removeMap.keySet());
		List<Statements> result = new ArrayList<Statements>(keys.size());
		for (Resource key : keys) {
			Statements stmts = new Statements();
			stmts.addInsert(insertMap.get(key));
			stmts.addRemove(removeMap.get(key));
			result.add(stmts);
		}
		return result;
	}

	/**
	 * Group a collection of statements by their subject.
	 * 
	 * @created 26.11.2013
	 * @param statements the statements to be grouped
	 * @return a collection of non-empty statement lists sharing the same
	 *         subject
	 */
	private Collection<List<Statement>> groupBySubject(Collection<Statement> statements) {
		Map<Resource, List<Statement>> result = mapBySubject(statements);
		return result.values();
	}

	private Map<Resource, List<Statement>> mapBySubject(Collection<Statement> statements) {
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
		return result;
	}

	private void applyDeletes() {

	}

	private void removePredicateGroup(Statements statemens) {
		if (!statemens.hasRemoves()) return;
		Resource subject = statements.getSample().getSubject();
		URI predicate = statements.getSample().getPredicate();

		Set<Node> removes = new HashSet<Node>();
		for (Statement stmt : statemens.removes()) {
			removes.add(stmt.getObject());
		}

		// first try to insert into a list of objects,
		// if there is already a turtle with this subject and predicate
		Set<Node> unprocessedNodes = new HashSet<Node>(removes);
		List<Section<TurtlePredSentence>> sentences =
				OntologyUtils.findSentences(article, subject, predicate);
		for (Section<TurtlePredSentence> sentence : sentences) {
			List<Section<TurtleObjectSection>> objects =
					Sections.findSuccessorsOfType(sentence, TurtleObjectSection.class);
			for (Section<TurtleObjectSection> object : objects) {
				Rdf2GoCore core = Rdf2GoCore.getAnyInstance(object);
				Node node = object.get().getNode(core, object);
				if (removes.contains(node)) {
					sectionsToDelete.add(object);
					unprocessedNodes.remove(node);
				}
			}
		}

		// remember the unprocessed remove statements as unprocessed
		for (Statement stmt : statemens.removes()) {
			if (unprocessedNodes.contains(stmt.getObject())) {
				this.unprocessed.addRemove(stmt);
			}
		}
	}

	/**
	 * Method to create an insert for a number of statements that share the same
	 * subject and predicate. The list must contain at least one statement.
	 * 
	 * @created 23.11.2013
	 * @param statements the list of statements that share the same subject
	 * @return the insert required to add the statements or null if they could
	 *         not been inserted in an existing markup
	 */
	private void insertPredicateGroup(Statements statements) {
		if (!statements.hasInserts()) return;
		Resource subject = statements.getSample().getSubject();
		URI predicate = statements.getSample().getPredicate();

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
				String indent = getIndent(predSentence.getOffsetInArticle());
				// create turtle text to be inserted, preceded by a ","
				// if there is an undeleted (!) object before
				String turtle = createObjectTurtle(statements.inserts(), indent + "  ");
				if (!sectionsToDelete.contains(last)) turtle = "," + turtle;
				// create insert right after last object
				int position = last.getOffsetInArticle() + last.getText().length();
				sectionsToRetain.add(predSentence);
				sectionsToRetain.add(Sections.findAncestorOfType(predSentence, TurtleSentence.class));
				inserts.add(new Insert(turtle.toString(), position));
				return;
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
				String indent = getIndent(last.getOffsetInArticle());
				// create turtle text to be inserted
				String turtle = ";" + createPredicateTurtle(statements.inserts(), indent);
				// create insert right after last predicate
				int position = last.getOffsetInArticle() + last.getText().length();
				sectionsToRetain.add(subjectSentence);
				inserts.add(new Insert(turtle, position));
				return;
			}
		}

		// otherwise we would not create a insert for this group
		unprocessed.addInsert(statements.insertStatements);
	}

	/**
	 * Method to create inserts for a number of statements that share the same
	 * subject. The list must contain at least one statement. The created
	 * inserts are added to the specified result list. For all statements where
	 * no insert can be added (because there are no existing markups for that
	 * subject), the statements are added to the unprocessed list.
	 * 
	 * @created 23.11.2013
	 * @param statements the statements that share the same subject
	 */
	private void processSubjectGroup(Statements statements) {

		// group statements (of same subject) by and add them
		for (Statements group : groupByPredicate(statements)) {
			removePredicateGroup(group);
			insertPredicateGroup(group);
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
