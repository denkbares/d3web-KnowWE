package de.knowwe.ontology.kdom.sparql;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.SparqlContentType;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Shows contents of references of SparqlQueryInline.
 * <p/>
 * Created by Veronika Sehne on 30.04.2014.
 */
public class InlineSparqlMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	public static final String ROW_SEPARATOR = "rowSeparator";

	public static final String SEPARATOR = "separator";

	public static final String MARKUP_NAME = "InlineSparql";

	static {
		MARKUP = new DefaultMarkup(MARKUP_NAME);
		MARKUP.setInline(true);
		MARKUP.addAnnotation(SEPARATOR, true);
		MARKUP.addAnnotation(ROW_SEPARATOR, true);
	}

	public InlineSparqlMarkup() {
		super(MARKUP);
		this.setRenderer(new Renderer() {

			@Override
			public void render(Section<?> section, UserContext user, RenderResult result) {
				String sparqlName = DefaultMarkupType.getContent(section);
				String separator = DefaultMarkupType.getAnnotation(section, SEPARATOR);
				String rowSeparator = DefaultMarkupType.getAnnotation(section, ROW_SEPARATOR);
				if (separator == null) {
					separator = ", ";
				}
				if (rowSeparator == null) {
					rowSeparator = separator;
				}
				separator = clean(separator);
				rowSeparator = clean(rowSeparator);
				separator = Strings.unquote(separator);
				rowSeparator = Strings.unquote(rowSeparator);

				sparqlName = clean(sparqlName);
				Collection<TermCompiler> compilers = Compilers.getCompilers(section, TermCompiler.class);

				TermCompiler termCompiler = null;
				for (TermCompiler compiler : compilers) {
					if (compiler instanceof Rdf2GoCompiler) {
						termCompiler = compiler;
						break;
					}
				}
				if (termCompiler == null) {
					return;
				}

				Identifier identifier = new Identifier("SPARQL", sparqlName);
				Section<?> sparqlSection = termCompiler.getTerminologyManager()
						.getTermDefiningSection(identifier);
				if (sparqlSection == null) {
					result.appendHtml("<span style='color:#C00000; background-color:#FFF6BF'>");
					result.append("Sparql with name '" + sparqlName + "' not found.");
					result.appendHtml("</span>");
					return;
				}
				Section<SparqlContentType> contentSection = Sections.findSuccessor(sparqlSection, SparqlContentType.class);

				String query = contentSection.getText();
				OntologyCompiler compiler = Compilers.getCompiler(section, OntologyCompiler.class);

				if (compiler != null) {
					Rdf2GoCore core = compiler.getRdf2GoCore();
					query = Rdf2GoUtils.createSparqlString(core, query);
					QueryResultTable resultTable = core.sparqlSelect(query);

					ClosableIterator<QueryRow> rowIterator = resultTable.iterator();
					List<String> variables = resultTable.getVariables();

					String line = "";
					String cell;

					while (rowIterator.hasNext()) {
						QueryRow row = rowIterator.next();
						for (Iterator<String> variableIterator = variables.iterator(); variableIterator.hasNext(); ) {
							String variable = variableIterator.next();
							Node x = row.getValue(variable);
							cell = x.toString();
							cell = Rdf2GoUtils.trimDataType(core, cell);
							cell = Rdf2GoUtils.trimNamespace(core, cell);
							line += cell;
							if (variableIterator.hasNext()) {
								line += separator;
							}
						}
						if (rowIterator.hasNext()) {
							line += rowSeparator;
						}
					}
					result.append(line);
				}
			}

			private String clean(String string) {
				string = string.replaceAll("%%$", "");
				string = string.replaceAll("/%$", "");
				string = Strings.trim(string);
				return string;
			}

		});
	}
}
