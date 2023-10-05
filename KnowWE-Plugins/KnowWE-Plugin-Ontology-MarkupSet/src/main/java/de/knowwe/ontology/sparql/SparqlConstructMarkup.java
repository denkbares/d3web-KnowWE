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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.ContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Markup for Sparql CONSTRUCT queries. The result of the CONSTRUCT query will be added to ontology.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 05.10.2023
 * @deprecated unfortunately, as of 2023-10-05, CONSTRUCT queries seem to not work in our current setup (GraphDB?).
 * Feel free to try again in later versions.
 */
public class SparqlConstructMarkup extends DefaultMarkupType {

	private static final String MARKUP_NAME = "SparqlConstruct";
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup(MARKUP_NAME);
		MARKUP.setDeprecated("%%Sparql with @construct annotation");
		PackageManager.addPackageAnnotation(MARKUP);
	}

	public SparqlConstructMarkup() {
		super(MARKUP);
		addCompileScript(Priority.DONE, new SparqlConstructScript());
		setRenderer(new SparqlConstructRenderer());
	}

	private static class SparqlConstructScript extends OntologyCompileScript<SparqlConstructMarkup> {
		@Override
		public void compile(OntologyCompiler compiler, Section<SparqlConstructMarkup> section) {
			Stopwatch stopwatch = new Stopwatch();
			String constructQuery = Strings.trim(DefaultMarkupType.getContent(section));
			if (Strings.isBlank(constructQuery)) return;
			List<Statement> statements = new ArrayList<>();
			try (GraphQueryResult result = compiler.getRdf2GoCore().sparqlConstruct(constructQuery)) {
				result.forEach(statements::add);
			}
			statements.sort(Comparator.<Statement, String>comparing(s2 -> s2.getSubject().stringValue())
					.thenComparing(s1 -> s1.getPredicate().stringValue())
					.thenComparing(s -> s.getObject().stringValue()));
			section.storeObject(compiler, SparqlConstructMarkup.class.getName(), statements);
			compiler.getRdf2GoCore().addStatements(section, statements.toArray(Statement[]::new));
			section.storeObject(compiler, SparqlConstructMarkup.class.getName() + Stopwatch.class.getName(), stopwatch.getDisplay());
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<SparqlConstructMarkup> section) {
			compiler.getRdf2GoCore().removeStatements(section);
		}
	}

	private static class SparqlConstructRenderer extends DefaultMarkupRenderer {

		private static final int RENDER_LIMIT = 20;

		@Override
		protected void renderContents(Section<? extends DefaultMarkupType> markupSection, List<Section<ContentType>> contentSections, UserContext user, RenderResult result) {
			OntologyCompiler compiler = Compilers.getCompiler(user, markupSection, OntologyCompiler.class);
			if (compiler == null) {
				result.append("Query was not compiled by any compiler, nothing was added");
			}
			else {
				List<Statement> statements = markupSection.getObject(compiler, SparqlConstructMarkup.class.getName());
				if (statements == null) statements = List.of();
				String durationDisplay = markupSection.getObject(compiler, SparqlConstructMarkup.class.getName() + Stopwatch.class.getName());
				result.append(new Span("Added " + Strings.pluralOf(statements.size(), "statement") + (durationDisplay == null ? "" : ": " + durationDisplay)));

				Rdf2GoCore core = compiler.getRdf2GoCore();
				for (Statement statement : statements.subList(0, Math.min(RENDER_LIMIT, statements.size()))) {
					result.append(new Div().children(
							new Span(Rdf2GoUtils.reduceNamespace(core, statement.getSubject()
									.stringValue())).clazz("construct-subject"),
							new Span(Rdf2GoUtils.reduceNamespace(core, statement.getPredicate()
									.stringValue())).clazz("construct-predicate"),
							new Span(Rdf2GoUtils.reduceNamespace(core, statement.getObject()
									.stringValue())).clazz("construct-object")
					));
				}
				if (statements.size() > RENDER_LIMIT) {
					result.append(new Span(Strings.pluralOf(statements.size() - RENDER_LIMIT, "statement")) + " omitted...");
				}
			}
		}
	}
}
