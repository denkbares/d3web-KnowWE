/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.ontology.sparql;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.strings.QuoteSet;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.compile.CompilePriorityLevelFinishedEvent;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.sectionFinder.SplitSectionFinderUnquoted;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.ontology.turtle.TurtleLiteralType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Type of the content of the @construct annotation at a %%Sparql markup. Uses the result of the sparql query to
 * generate statements, based on the template given via the annotation.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 05.10.2023
 */
public class ConstructAnnotationType extends AbstractType {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstructAnnotationType.class);

	private static final String INFO_KEY = ConstructAnnotationType.class.getName() + "Info";

	public ConstructAnnotationType() {
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		addChildType(new SubjectType());
		addChildType(new PredicateType());
		addChildType(new ObjectType());
		addCompileScript(new ConstructSetupScript());
		setRenderer(new ConstuctAnnotationRenderer());
	}

	@NotNull
	private Sections<SubjectType> getSubject(Section<ConstructAnnotationType> section) {
		return $(section).successor(SubjectType.class);
	}

	@NotNull
	private Sections<PredicateType> getPredicate(Section<ConstructAnnotationType> section) {
		return $(section).successor(PredicateType.class);
	}

	@NotNull
	private Sections<ObjectType> getObject(Section<ConstructAnnotationType> section) {
		return $(section).successor(ObjectType.class);
	}

	private static class SubjectType extends ResourceType {

		public SubjectType() {
		}
	}

	private static class PredicateType extends ResourceType {
		public PredicateType() {
			addChildType(new AbbreviatedResourceReference());
		}
	}

	private static class ObjectType extends ResourceType {
		public ObjectType() {
			addChildType(1, new TurtleLiteralType());
		}

		public Value getObject(OntologyCompiler compiler, Section<ObjectType> section, BindingSet bindings) {
			return $(section).successor(Variable.class)
					.map(s -> s.get().getVariableName(s))
					.peek(n -> {
						if (!bindings.hasBinding(n)) {
							throw new IllegalArgumentException("No binding found for variable '?" + n + "'");
						}
					})
					.map(bindings::getValue)
					.findFirst()
					.orElseGet(() -> $(section).successor(AbbreviatedResourceReference.class)
							.map(s -> (Value) s.get().getResourceIRI(compiler.getRdf2GoCore(), s))
							.findFirst()
							.orElseGet(() -> $(section).successor(TurtleLiteralType.class)
									.mapFirst(s -> s.get().getLiteral(compiler.getRdf2GoCore(), s))));
		}
	}

	private static class ResourceType extends StatementPartType {

		public ResourceType() {
			addChildType(new AbbreviatedResourceReference());
		}

		public Resource getResource(OntologyCompiler compiler, Section<? extends ResourceType> section, BindingSet bindings) {
			return $(section).successor(Variable.class)
					.map(s -> s.get().getVariableName(s))
					.map(bindings::getValue)
					.peek(v -> {
						if (!(v instanceof Resource)) {
							throw new IllegalArgumentException("Expected " + IRI.class.getSimpleName() +
									" as predicate, but got: " + v.getClass()
									.getSimpleName());
						}
					})
					.map(Resource.class::cast)
					.findFirst()
					.orElseGet(() -> $(section).successor(AbbreviatedResourceReference.class)
							.mapFirst(s -> s.get().getResourceIRI(compiler.getRdf2GoCore(), s)));
		}
	}

	private static class StatementPartType extends AbstractType {
		public StatementPartType() {
			setSectionFinder(new ConstraintSectionFinder(new SplitSectionFinderUnquoted(Pattern.compile("\\h+"), new QuoteSet('"')), AtMostOneFindingConstraint.getInstance()));
			addChildType(new Variable());
		}
	}

	public static class Variable extends AbstractType {

		public Variable() {
			setSectionFinder(new RegexSectionFinder(Pattern.compile("^\\?[\\w_-]+$")));
		}

		public String getVariableName(Section<Variable> section) {
			return section.getText().substring(1);
		}
	}

	private static class ConstructSetupScript extends OntologyCompileScript<ConstructAnnotationType> {
		@Override
		public void compile(OntologyCompiler compiler, Section<ConstructAnnotationType> section) throws CompilerMessage {
			if (section.get().getSubject(section).isEmpty()) {
				throw CompilerMessage.error("No subject specified in construct annotation: " + section.getText());
			}
			if (section.get().getPredicate(section).isEmpty()) {
				throw CompilerMessage.error("No predicate specified in construct annotation: " + section.getText());
			}
			if (section.get().getObject(section).isEmpty()) {
				throw CompilerMessage.error("No object specified in construct annotation: " + section.getText());
			}
			if ($(section).successor(Variable.class).isEmpty()) {
				throw CompilerMessage.error("No variable specified in construct annotation: " + section.getText());
			}

			ConstructCompileListener listener = new ConstructCompileListener(compiler, section);
			section.storeObject(compiler, ConstructCompileListener.class.getName(), listener);
			EventManager.getInstance().registerListener(listener);
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<ConstructAnnotationType> section) {
			ConstructCompileListener listener = section.getObject(compiler, ConstructSetupScript.class.getName());
			if (listener != null) EventManager.getInstance().unregister(listener);
		}
	}

	private static class ConstructCompileListener implements EventListener {

		private final WeakReference<OntologyCompiler> compiler;
		private final Section<ConstructAnnotationType> section;

		public ConstructCompileListener(OntologyCompiler compiler, Section<ConstructAnnotationType> section) {
			this.section = section;
			// don't reference the compiler directly to prevent memory leaks
			this.compiler = new WeakReference<>(compiler);
		}

		@Override
		public Collection<Class<? extends Event>> getEvents() {
			return List.of(CompilePriorityLevelFinishedEvent.class);
		}

		@Override
		public void notify(Event event) {
			if (!(event instanceof CompilePriorityLevelFinishedEvent priorityLevelFinishedEvent)) return;
			// we want the priority directly before DONE to be finished (so we are compiling during priority done)
			if (priorityLevelFinishedEvent.getPriority() != Priority.increment(Priority.DONE)) return;
			if (priorityLevelFinishedEvent.getCompiler() != compiler.get()) return;

			updateStatements((OntologyCompiler) priorityLevelFinishedEvent.getCompiler(), section);
		}

		private void updateStatements(OntologyCompiler compiler, Section<ConstructAnnotationType> section) {
			Stopwatch stopwatch = new Stopwatch();
			Rdf2GoCore core = compiler.getRdf2GoCore();
			// cleanup last statements
			core.removeStatements(section);

			String query = $(section).ancestor(DefaultMarkupType.class)
					.successor(SparqlType.class)
					.mapFirst(s -> Rdf2GoUtils.createSparqlString(core, s.getText()));
			List<Statement> statements = new ArrayList<>();
			try (CachedTupleQueryResult result = core.sparqlSelect(query)) {
				for (BindingSet binding : result) {
					statements.add(toStatement(compiler, section, binding));
				}
				Messages.clearMessages(compiler, section, this.getClass());
			}
			catch (CompilerMessage e) {
				Messages.storeMessages(compiler, section, this.getClass(), e.getMessages());
			}
			catch (Exception e) {
				LOGGER.error("Exception while generating construct statements", e);
				Messages.storeMessage(compiler, section, this.getClass(), Messages.error(e));
			}
			// add new statements
			core.addStatements(section, statements);
			section.storeObject(compiler, INFO_KEY, "Added " + Strings.pluralOf(statements.size(), "statement") + " in " + stopwatch.getDisplay());
		}

		private Statement toStatement(OntologyCompiler compiler, Section<ConstructAnnotationType> section, BindingSet binding) throws CompilerMessage {
			Resource subject = $(section).successor(SubjectType.class)
					.mapFirst(s -> s.get().getResource(compiler, s, binding));
			if (subject == null) throw CompilerMessage.error("Unable to generate subject");
			IRI predicate = (IRI) $(section).successor(PredicateType.class)
					.mapFirst(s -> s.get().getResource(compiler, s, binding));
			if (predicate == null) throw CompilerMessage.error("Unable to generate object");
			Value object = $(section).successor(ObjectType.class)
					.mapFirst(s -> s.get().getObject(compiler, s, binding));
			if (object == null) throw CompilerMessage.error("Unable to generate object");
			return compiler.getRdf2GoCore().createStatement(subject, predicate, object);
		}
	}

	private static class ConstuctAnnotationRenderer implements Renderer {
		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			DelegateRenderer.getInstance().render(section, user, result);
			OntologyCompiler compiler = Compilers.getCompiler(user, section, OntologyCompiler.class);
			if (compiler != null) {
				result.append(new Span("(" + section.getObject(compiler,INFO_KEY) + ")").clazz("construct-compile-info"));
			}
		}
	}
}
