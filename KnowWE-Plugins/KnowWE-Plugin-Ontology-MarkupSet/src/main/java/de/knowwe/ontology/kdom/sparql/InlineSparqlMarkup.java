package de.knowwe.ontology.kdom.sparql;

import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;

import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.types.LinkType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsyncPreviewRenderer;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.ontology.sparql.SparqlContentType;
import de.knowwe.ontology.sparql.SparqlMarkupType;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.SparqlCache;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static de.knowwe.kdom.renderer.AsynchronousRenderer.ASYNCHRONOUS;

/**
 * Shows contents of references of SparqlQueryInline.
 * <p>
 *
 * @author Veronika Sehne on 30.04.2014.
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
		MARKUP.addContentType(new InlineSparqlNameReference());
	}

	private static class InlineSparqlNameReference extends SparqlNameReference {

		/**
		 * Returns the actual sparql section that is referenced by the section. If the section cannot be found, null is
		 * returned.
		 *
		 * @param compiler the compiler for which the query is retrieved
		 * @param section  the referencing section contain the reference name
		 * @return the actual sparql section to be executed
		 */
		@Override
		public Section<SparqlMarkupType> getReferencedSection(Rdf2GoCompiler compiler, Section<? extends SparqlNameReference> section) {
			Identifier identifier = getTermIdentifier(compiler, section);
			if (compiler == null) return null;
			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			Section<?> sparqlSection = terminologyManager.getTermDefiningSection(identifier);
			if (sparqlSection == null) {
				Section<LinkType> linkSection = $(section).ancestor(InlineSparqlMarkup.class)
						.parent()
						.successor(LinkType.class)
						.getFirst();
				if (linkSection != null) {
					String link = linkSection.getText();
					int start = link.lastIndexOf("|");
					if (start < 1) start = 0;
					link = Strings.trim(link.substring(start + 1, link.length() - 1));
					Article article = KnowWEUtils.getArticle(section.getWeb(), link);
					if (article != null) {
						return $(article.getRootSection()).successor(SparqlMarkupType.class).getFirst();
					}
				}
			}
			else {
				return Sections.cast(sparqlSection, SparqlMarkupType.class);
			}
			return null;
		}
	}

	public InlineSparqlMarkup() {
		super(MARKUP);
		this.setRenderer(new AsynchronousRenderer(new InlineSparqlRenderer(), true));
	}

	private static class InlineSparqlRenderer implements Renderer, AsyncPreviewRenderer {

		@Override
		public boolean shouldRenderAsynchronous(Section<?> section, UserContext user) {
			Section<SparqlNameReference> reference = Sections.successor(
					DefaultMarkupType.getContentSection(section), SparqlNameReference.class);
			if (reference == null) return true;
			Rdf2GoCompiler compiler = Compilers.getCompiler(user, section, Rdf2GoCompiler.class);
			if (compiler == null) return true;
			Section<SparqlMarkupType> referencedSection = reference.get().getReferencedSection(compiler, reference);
			if (referencedSection == null) return true;
			String query = $(referencedSection).successor(SparqlContentType.class).mapFirst(Section::getText);
			if (query == null) return true;
			Rdf2GoCore core = compiler.getRdf2GoCore();
			query = Rdf2GoUtils.createSparqlString(core, query);
			SparqlCache.State cacheState = core.getCacheState(query);
			return cacheState != SparqlCache.State.available;
		}

		@Override
		public void renderAsyncPreview(Section<?> section, UserContext user, RenderResult result) {
			result.appendHtml(Icon.LOADING.addClasses("asynchronSmall").toHtml());
		}

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			String separator = DefaultMarkupType.getAnnotation(section, SEPARATOR);
			String rowSeparator = DefaultMarkupType.getAnnotation(section, ROW_SEPARATOR);
			String countString = DefaultMarkupType.getAnnotation(section, COUNT);

			boolean count = !(countString == null || countString.equals("false"));

			if (separator == null) {
				separator = ", ";
			}
			else {
				separator = Strings.trim(separator);
				separator = Strings.unquote(separator);
			}
			if (rowSeparator == null) {
				rowSeparator = separator;
			}
			else {
				rowSeparator = Strings.trim(rowSeparator);
				rowSeparator = Strings.unquote(rowSeparator);
			}

			Section<SparqlNameReference> reference = Sections.successor(
					DefaultMarkupType.getContentSection(section), SparqlNameReference.class);

			Rdf2GoCompiler compiler = Compilers.getCompiler(user, section, Rdf2GoCompiler.class);
			Section<SparqlMarkupType> referencedSection = reference == null ? null : reference.get()
					.getReferencedSection(compiler, reference);

			try {
				if (referencedSection == null) {
					throw new Exception("No query found.");
				}
				if (compiler == null) {
					throw new Exception("No compiler found.");
				}

				Rdf2GoCore core = compiler.getRdf2GoCore();
				String query = $(referencedSection).successor(SparqlContentType.class).mapFirst(Section::getText);
				query = Rdf2GoUtils.createSparqlString(core, query);
				long timeout = SparqlContentType.getTimeout(referencedSection);

				// we add addtional info for testability
				result.appendHtmlTag("span", "class", "inline-sparql", "name", reference.get(SparqlNameReference::getTermName));

				TupleQueryResult resultTable = core.sparqlSelect(query, new Rdf2GoCore.Options().timeout(timeout));

				Iterator<BindingSet> rowIterator = resultTable.iterator();
				List<String> variables = resultTable.getBindingNames();

				RenderResult line = new RenderResult(result);
				String cell;

				int lines = 0;
				BindingSet row = null;
				while (rowIterator.hasNext()) {
					row = rowIterator.next();
					lines++;
					if (count) continue;
					for (Iterator<String> variableIterator = variables.iterator(); variableIterator
							.hasNext(); ) {
						String variable = variableIterator.next();
						Value node = row.getValue(variable);
						if (node == null) continue;
						cell = node.stringValue();
						cell = Rdf2GoUtils.trimDataType(core, cell);
						cell = Rdf2GoUtils.trimNamespace(core, cell);
						line.appendJSPWikiMarkup(cell);
						if (variableIterator.hasNext()) {
							line.append(separator);
						}
					}
					if (rowIterator.hasNext()) {
						line.append(rowSeparator);
					}
				}
				if (count) {
					if (lines == 1) {
						// special case for SPARQLs with GROUP_CONCAT... they often contain one empty result
						boolean foundContent = false;
						for (String variable : variables) {
							Value node = row.getValue(variable);
							if (node != null && !Strings.isBlank(node.stringValue())) {
								foundContent = true;
							}
						}
						if (!foundContent) lines = 0;
					}

					result.append(String.valueOf(lines));
				}
				else {
					result.append(line);
				}
				result.appendHtmlTag("/span");
			}
			catch (Exception e) {
				result.appendHtmlElement("span", e.getMessage());
			}
		}
	}
}
