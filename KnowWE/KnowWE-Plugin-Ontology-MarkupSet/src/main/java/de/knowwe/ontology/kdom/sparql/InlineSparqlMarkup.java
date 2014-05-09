package de.knowwe.ontology.kdom.sparql;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.ci.provider.SparqlTestObjectProviderUtils;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.sparql.SparqlContentType;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Shows contents of references of SparqlQueryInline.
 * 
 * Created by Veronika Sehne on 30.04.2014.
 */
public class InlineSparqlMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	public static final String ROW_SEPARATOR = "rowSeparator";

	public static final String SEPARATOR = "separator";

	static {
		MARKUP = new DefaultMarkup("InlineSparql");
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
				Collection<Section<SparqlContentType>> sparqlSections = SparqlTestObjectProviderUtils.getSparqlQueryContentSection(sparqlName);
				Section<SparqlContentType> sparqlSection = sparqlSections.iterator().next();
				String query = sparqlSection.getText();
				OntologyCompiler compiler = Compilers.getCompiler(section, OntologyCompiler.class);

				if (compiler != null) {
					de.knowwe.rdf2go.Rdf2GoCore core = (de.knowwe.rdf2go.Rdf2GoCore) compiler.getRdf2GoCore();
					query = Rdf2GoUtils.createSparqlString(core, query);
					org.ontoware.rdf2go.model.QueryResultTable resultTable = (org.ontoware.rdf2go.model.QueryResultTable) core.sparqlSelect(query);

					ClosableIterator<QueryRow> rowIterator = resultTable.iterator();
					List<String> variables = resultTable.getVariables();

					String line = "";
					String cell = "";

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
