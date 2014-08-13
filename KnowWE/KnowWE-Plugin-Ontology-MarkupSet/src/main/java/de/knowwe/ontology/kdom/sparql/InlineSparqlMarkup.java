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
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.SparqlContentType;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

import static de.knowwe.kdom.renderer.AsynchronRenderer.ASYNCHRONOUS;

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

	public static final String COUNT = "count";

	static {
		MARKUP = new DefaultMarkup(MARKUP_NAME);
		MARKUP.setInline(true);
		MARKUP.addAnnotation(SEPARATOR);
		MARKUP.addAnnotation(ROW_SEPARATOR);
		MARKUP.addAnnotation(COUNT, false, "true", "false");
		MARKUP.addAnnotation(ASYNCHRONOUS, false, "true", "false");
		MARKUP.addAnnotationRenderer(ASYNCHRONOUS, NothingRenderer.getInstance());
	}

	public InlineSparqlMarkup() {
		super(MARKUP);
		this.setRenderer(new AsynchronRenderer(new Renderer() {

			@Override
			public void render(Section<?> section, UserContext user, RenderResult result) {
				String sparqlName = DefaultMarkupType.getContent(section);
				String separator = DefaultMarkupType.getAnnotation(section, SEPARATOR);
				String rowSeparator = DefaultMarkupType.getAnnotation(section, ROW_SEPARATOR);
				String countString = DefaultMarkupType.getAnnotation(section, COUNT);

				boolean count = !(countString == null || countString.equals("false"));

				if (separator == null) {
					separator = ", ";
				}
				else {
					separator = clean(separator);
					separator = Strings.unquote(separator);
				}
				if (rowSeparator == null) {
					rowSeparator = separator;
				}
				else {
					rowSeparator = clean(rowSeparator);
					rowSeparator = Strings.unquote(rowSeparator);
				}

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
					String sparqlResult;
					try {
						QueryResultTable resultTable = core.sparqlSelect(query);

						ClosableIterator<QueryRow> rowIterator = resultTable.iterator();
						List<String> variables = resultTable.getVariables();

						String line = "";
						String cell;

						int lines = 0;
						QueryRow row = null;
						while (rowIterator.hasNext()) {
							row = rowIterator.next();
							lines++;
							if (count) continue;
							for (Iterator<String> variableIterator = variables.iterator(); variableIterator.hasNext(); ) {
								String variable = variableIterator.next();
								Node node = row.getValue(variable);
								if (node == null) continue;
								cell = node.toString();
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
						if (count) {
							if (lines == 1) {
								// special case for SPARQLs with GROUP_CONCAT... they often contain one empty result
								boolean foundContent = false;
								for (String variable : variables) {
									Node node = row.getValue(variable);
									if (node != null && !Strings.isBlank(node.toString())) {
										foundContent = true;
									}
								}
								if (!foundContent) lines = 0;
							}

							sparqlResult = String.valueOf(lines);
						}
						else {
							sparqlResult = line;
						}
					}
					catch (Exception e) {
						sparqlResult = "?";
					}
					result.appendHtmlElement("span", KnowWEUtils.maskJSPWikiMarkup(sparqlResult));
				}
			}

			private String clean(String string) {
				string = string.replaceAll("%%$", "");
				string = string.replaceAll("/%$", "");
				string = Strings.trim(string);
				return string;
			}

		}, true));
	}
}
