package de.knowwe.ontology.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.jetbrains.annotations.NotNull;

import com.denkbares.plugin.Extension;
import com.denkbares.plugin.PluginManager;
import com.denkbares.semanticcore.TupleQueryResult;
import com.denkbares.semanticcore.utils.IndexedResultTableModel;
import com.denkbares.semanticcore.utils.ResultTableHierarchy;
import com.denkbares.semanticcore.utils.TableRow;
import com.denkbares.semanticcore.utils.TableRowComparator;
import com.denkbares.semanticcore.utils.ValueComparator;
import com.denkbares.strings.Strings;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Pair;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.ontology.compile.OntologyMarkup;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.SparqlCache;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.sparql.utils.SparqlRenderResult;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.util.Color;
import de.knowwe.util.Icon;

public class SparqlResultRenderer {
	private static final Logger LOGGER = LoggerFactory.getLogger(SparqlResultRenderer.class);

	private static final String POINT_ID = "SparqlResultNodeRenderer";
	private static final String RESULT_TABLE = "resultTable";
	private static final String RESULT_TABLE_TREE = "resultTableTree";
	private static final String USED_IDS = "usedIDs";
	private static final Pattern ARTICLE_LINK_PATTERN = Pattern.compile("\\[\\h*(?:([^|]+)\\h*\\|\\h*)?((\\w+?://)?[^]|]+)\\h*]");
	private static final Collection<String> JSPWIKI_ESCAPE_TOKENS = KnowWEUtils.JSPWIKI_TOKENS.stream()
			.filter(s -> !("[".equals(s) || "]".equals(s)))
			.collect(Collectors.toList());
	private static final Set<IRI> DATE_TYPE_URIS = Set.of(XSD.DATE, XSD.TIME, XSD.DATETIME, XSD.DATETIMESTAMP);

	private static SparqlResultRenderer instance = null;

	private List<SparqlResultNodeRenderer> nodeRenderers;

	public static SparqlResultRenderer getInstance() {
		if (instance == null) {
			instance = new SparqlResultRenderer();
		}
		return instance;
	}

	private SparqlResultRenderer() {
		List<SparqlResultNodeRenderer> nodeRenderer = getNodeRenderers();
		optimizeNodeRenderer(nodeRenderer);
		this.nodeRenderers = nodeRenderer;
	}

	/**
	 * If we trim the namespace, we do not need to reduce it first... improves performance to remove
	 * ReduceNamespaceNodeRenderer
	 */
	private void optimizeNodeRenderer(List<SparqlResultNodeRenderer> nodeRenderer) {
		ReduceNamespaceNodeRenderer rnnRenderer = null;
		boolean containsBoth = false;
		for (SparqlResultNodeRenderer sparqlResultNodeRenderer : nodeRenderer) {
			if (sparqlResultNodeRenderer instanceof ReduceNamespaceNodeRenderer) {
				rnnRenderer = (ReduceNamespaceNodeRenderer) sparqlResultNodeRenderer;
			}
			else if (sparqlResultNodeRenderer instanceof TrimNamespaceNodeRenderer) {
				containsBoth = true;
			}
		}
		if (containsBoth) {
			nodeRenderer.remove(rnnRenderer);
		}
	}

	public static List<SparqlResultNodeRenderer> getNodeRenderers() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				OntologyMarkup.PLUGIN_ID, POINT_ID);
		List<SparqlResultNodeRenderer> renderers = new ArrayList<>();
		for (Extension extension : extensions) {
			renderers.add((SparqlResultNodeRenderer) extension.getSingleton());
		}
		return renderers;
	}

	public void setNodeRenderers(List<SparqlResultNodeRenderer> nodeRenderers) {
		this.nodeRenderers = nodeRenderers;
	}

	public boolean shouldRenderAsynchronous(Section<? extends SparqlType> section, UserContext user) {
		String query = section.get().getSparqlQuery(section, user);
		RenderOptions opts = section.get().getRenderOptions(section, user);
		if (opts.getRdf2GoCore() == null) return true;
		return opts.getRdf2GoCore().getCacheState(query) != SparqlCache.State.available;
	}

	public void renderSparqlResult(Section<? extends SparqlType> section, UserContext user, RenderResult result, boolean renderPreview) {

		String query = section.get().getSparqlQuery(section, user);
		RenderOptions opts = section.get().getRenderOptions(section, user);
		result.appendHtml("<div class='sparqlTable' sparqlSectionId='" + opts.getId() + "' id='sparqlTable_" + opts
				.getId() + "'>");
		if (opts.isBorder()) {
			result.appendHtml("<div class='border'>");
		}

		SparqlRenderResult renderResult;
		TupleQueryResult qrt = null;
		// in case we render preview for async renderer OR if we are just navigating pagination/filtering,
		// make sure to use cached result
		boolean useLastCachedResult = renderPreview || PaginationRenderer.isPaginationRerendering(user);
		Rdf2GoCore.Options options = new Rdf2GoCore.Options().timeout(opts.getTimeout())
				.lastCachedResult(useLastCachedResult);
		try {
			qrt = opts.getRdf2GoCore().sparqlSelect(query, options);
			qrt = section.get().postProcessResult(qrt, user, opts);
		}
		catch (RuntimeException e) {
			if (renderPreview) {
				result.appendHtml(Icon.LOADING.addClasses("asynchronNormal").toHtml());
			}
			else {
				handleRuntimeException(section, user, result, e);
			}
		}
		if (qrt != null) {
			if (renderPreview) {
				result.appendHtmlTag("span", "class", "async-preview-info warning");
				result.appendHtml(Icon.LOADING.addClasses("asynchronSmall").toHtml());
				result.appendHtml(" Database has changed. Showing previous result while rerunning query...");
				result.appendHtmlTag("/span");
			}
			renderResult = getSparqlRenderResult(qrt, opts, user, section);

			result.appendHtml(renderResult.getHTML());
		}
		if (opts.isBorder()) {
			result.appendHtml("</div>");
		}
		result.appendHtml("</div>");
	}

	public static void handleRuntimeException(Section<? extends SparqlType> section, UserContext user, RenderResult result, RuntimeException e) {
		result.appendHtml("<div class='warning'>");
		appendMessage(section, e, user, result);
		result.appendHtml("<br/><a onclick='KNOWWE.plugin.sparql.retry(\"" + section.getID()
						  + "\")' title='Try executing the query again, if you think it was only a temporary problem.'"
						  + " class='tooltipster'>Try again...</a></div>");
	}

	private static void appendMessage(Section<? extends SparqlType> section, RuntimeException e, UserContext user, RenderResult result) {
		if (e instanceof MalformedQueryException || e.getCause() instanceof MalformedQueryException) {
			appendMalformedQueryMessage(section, e.getMessage(), user, result);
		}
		else {
			String message = e.getMessage();
			if (message == null) {
				message = "RuntimeException without message.";
			}
			message = Strings.trimRight(message);
			if (!message.endsWith(".")) {
				message += ".";
			}
			result.append(message);
		}
	}

	private static void appendMalformedQueryMessage(Section<? extends SparqlType> section, String message, UserContext user, RenderResult result) {
		message = Strings.encodeHtml(message);
		String query = section.get().getSparqlQuery(section, user);
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(user, section);
		if (query == null || core == null || Strings.trim(query).isEmpty()) {
			result.append(message);
			return;
		}

		Matcher lineMatcher = Pattern.compile("at line (\\d+)").matcher(message);
		int lineNumber = -1;
		if (lineMatcher.find()) {
			lineNumber = Integer.parseInt(lineMatcher.group(1)) - 1;
		}

		Matcher columnMatcher = Pattern.compile(", column (\\d+)").matcher(message);
		int columnNumber = -1;
		if (columnMatcher.find()) {
			columnNumber = Integer.parseInt(columnMatcher.group(1)) - 1;
		}
		if (columnNumber == -1 || lineNumber == -1) {
			result.append(message);
			return;
		}

		String completeQuery = core.prependPrefixesToQuery(core.getNamespaces(), query);
		String queryLine = completeQuery.split("\n")[lineNumber];

		int lineNumbersWithoutPrefix = query.split("\n").length;
		int lineNumbersWithPrefix = completeQuery.split("\n").length;
		int prefixLineNumbers = lineNumbersWithPrefix - lineNumbersWithoutPrefix;

		if (queryLine == null) {
			result.append(message);
			return;
		}
		int start = Math.max(0, columnNumber - 15);
		int end = Math.min(columnNumber + 15, queryLine.length());
		while (start > 0 && queryLine.charAt(start) != ' ') {
			start--;
		}
		if (start > 0) {
			start++;
		}
		while (end < queryLine.length() && queryLine.charAt(end) != ' ') {
			end++;
		}
		String queryContextPrefix = "..." + queryLine.substring(start, columnNumber);
		String charAtException = columnNumber >= queryLine.length() ? "<EOL>" : queryLine.substring(columnNumber, columnNumber + 1);
		String queryContextSuffix = columnNumber >= queryLine.length() ? "" : queryLine.substring(columnNumber + 1, end) + "...";

		Scanner scanner = new Scanner(message);
		boolean first = true;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (first) {
				int index = line.indexOf("Encountered ");
				int lineNumberWithoutPrefix = lineNumber - prefixLineNumbers + 1;
				if (index >= 0) {
					result.append(line.substring(0, index + 12))
							.append("'").append(charAtException).append("' ")
							.append("at line ").append(lineNumberWithoutPrefix)
							.append(", column ").append(columnNumber + 1).append(".");
				}
				else {
					result.append(line.replaceAll(" line \\d+", " line " + lineNumberWithoutPrefix));
				}
				result.append(" Context: ").append(queryContextPrefix);
				result.appendHtmlElement("span", charAtException, "style", "color: red; font-weight: bold");
				result.append(queryContextSuffix);
				result.append(".\n");
				first = false;
			}
			else {
				result.append(line).append("\n");
			}
		}
	}

	/**
	 * @param qrt  the query result to render
	 * @param opts the options to control the rendered output
	 * @return html table with all results of qrt and size of qrt
	 * @created 06.12.2010
	 */
	public SparqlRenderResult getSparqlRenderResult(TupleQueryResult qrt, RenderOptions opts, UserContext user, Section<?> section) {
		if (section.getArticleManager() != null) {
			Compilers.awaitTermination(section.getArticleManager().getCompilerManager());
		}
		try {
			return renderQueryResultLocked(qrt, opts, user, section);
		}
		catch (Throwable e) {
			String message = "Exception while rendering SPARQL result";
			LOGGER.error(message, e);
			return new SparqlRenderResult(new RenderResult(user).appendException(e).toStringRaw());
		}
	}

	private SparqlRenderResult renderQueryResultLocked(TupleQueryResult qrt, RenderOptions opts, UserContext user, Section<?> section) {

		RenderResult renderResult = new RenderResult(user);
		if (isEmpty(qrt)) {
			PaginationRenderer.setResultSize(user, 0);
			renderResult.appendHtmlElement("span", "No results for this query", "class", "emptySparqlResult");
			return new SparqlRenderResult(renderResult.toStringRaw());
		}

		boolean zebraMode = opts.isZebraMode();
		boolean isTree = opts.isTree();
		boolean isNavigation = opts.isNavigation();
		List<RenderOptions.StyleOption> columnStyle = opts.getColumnStyles();
		List<RenderOptions.StyleOption> tableStyle = opts.getTableStyles();
		List<RenderOptions.StyleOption> columnWidths = opts.getColumnWidths();
		if (opts.getColor() != Color.NONE) {
			tableStyle.add(new RenderOptions.StyleOption("table", "border-color", opts.getColor().getColorValue()));
		}

		String tableID = UUID.randomUUID().toString();

		List<String> variables = qrt.getBindingNames();
		List<String> variablesFiltered = new ArrayList<>(variables);
		Set<String> hiddenColumns = PaginationRenderer.getHiddenColumns(section, user);
		variablesFiltered.removeIf(hiddenColumns::contains);

		// tree table init
		String childIdVariable = null;
		String parentIdVariable = null;
		if (isTree) {
			if (variables.size() > 2) {
				childIdVariable = variables.get(0);
				parentIdVariable = variables.get(1);
			}
			else {
				isTree = false;
				renderResult.append("%%warning The renderResult table requires at least three columns to enable tree mode.\n");
			}
		}

		// navigation mode check
		if (isTree) {
			isNavigation = false;
			//renderResult.append("%%warning The specified flags 'tree' and 'navigation' are not compatible.\n");
		}

		renderResult.appendHtmlTag("div", "style", "overflow-x: auto", "class", "scroll-parent");
		JSONArray variablesJson = new JSONArray();
		for (String variable : variables) {
			variablesJson.put(new JSONArray(List.of(variable, getColumnDisplayName(variable))));
		}
		IndexedResultTableModel table = IndexedResultTableModel.create(qrt);

		Set<String> dateColumns = getDateColumns(table);

		List<String> tableAttributes = new ArrayList<>();
		tableAttributes.addAll(List.of("id", tableID));
		tableAttributes.addAll(List.of("class", "sticky-header sparqltable" + (isTree ? " sparqltreetable" : "") + (opts.getColor() == Color.NONE ? "" : " logLevel")));
		tableAttributes.addAll(List.of("style", getStyleForKey("table", tableStyle)));
		tableAttributes.addAll(List.of("data-columns", variablesJson.toString()));
		renderResult.appendHtmlTag("table", tableAttributes.toArray(String[]::new));

		renderResult.appendHtml("<thead>");
		renderResult.appendHtml(!zebraMode ? "<tr>" : "<tr class='odd'>");

		int column = 0;
		for (String var : variablesFiltered) {
			if (isSkipped(isTree, column++, var)) {
				continue;
			}
			List<String> attributes = new ArrayList<>(Arrays.asList(
					"column-name", var, "filter-provider-action", SparqlFilterProviderAction.class.getSimpleName()));
			if (opts.getColumnsWithDisabledFiltering().contains(var)) {
				attributes.add("class");
				attributes.add("hide-filter");
			}
			if (dateColumns.contains(var)) {
				attributes.add("column-type");
				attributes.add("date");
			}
			renderResult.appendHtmlTag("th", attributes.toArray(new String[0]));
			renderResult.append(getColumnDisplayName(var));
			renderResult.appendHtml("</th>");
		}
		renderResult.appendHtml("</tr>");
		renderResult.appendHtml("</thead>");
		renderResult.appendHtml("<tbody>");

		Iterator<TableRow> iterator;
		if (isNavigation) {
			if (opts.isSorting()) {
				List<Pair<String, Comparator<Value>>> columnComparators =
						PaginationRenderer.getMultiColumnSorting(section, user).stream()
								.map(p -> new Pair<>(p.getA(), createValueComparator(opts, p.getA(), p.getB())))
								.collect(Collectors.toList());
				table = (IndexedResultTableModel) table.sort(new TableRowComparator(columnComparators));
			}
			if (opts.isFiltering()) {
				table = (IndexedResultTableModel) table.filter(PaginationRenderer.getFilter(section, user));
			}
			PaginationRenderer.setResultSize(user, table.getSize());
			int startRow = PaginationRenderer.getStartRow(section, user);
			int count = PaginationRenderer.getCount(section, user);
			if (count != Integer.MAX_VALUE) {
				iterator = table.iterator(startRow - 1, startRow + count - 1);
			}
			else {
				iterator = table.iterator();
			}
		}
		else if (isTree) {
			Stopwatch stopwatch = new Stopwatch();
			// creating hierarchy order using PartialHierarchyTree
			ResultTableHierarchy tree = new ResultTableHierarchy(table);
			LOGGER.info("Create hierarchical sparql result table in " + stopwatch.getDisplay());
			section.storeObject(RESULT_TABLE, table);
			section.storeObject(RESULT_TABLE_TREE, tree);
			iterator = tree.getRoots().iterator();
		}
		else {
			iterator = table.iterator();
		}

		List<String> classNames = new LinkedList<>();
		Map<String, Value> usedIDs = new HashMap<>();
		section.storeObject(USED_IDS, usedIDs);
		int line = 1;

		while (iterator.hasNext()) {
			line++;
			classNames.clear();
			TableRow row = iterator.next();
			if (zebraMode && line % 2 != 0) {
				classNames.add("odd");
			}
			if (isTree) {
				classNames.add("treetr");
			}
			renderResult.appendHtml(classNames.isEmpty()
					? "<tr"
					: "<tr class='" + Strings.concat(" ", classNames) + "'");

			if (isTree) {
				ResultTableHierarchy tree = section.getObject(RESULT_TABLE_TREE);
				Value childValue = row.getValue(childIdVariable);
				Value parentValue = row.getValue(parentIdVariable);
				String valueID = valueToID(childValue, parentValue == null ? "" : parentValue.stringValue());
				usedIDs.put(valueID, childValue);

				renderResult.append(" data-tt-id='sparql-id-").append(valueID).append("'");
				if (!tree.getChildren(row).isEmpty()) {
					renderResult.append(" data-tt-branch='true'");
				}
			}
			renderResult.append(">");

			column = 0;
			for (String var : variablesFiltered) {
				if (isSkipped(isTree, column++, var)) {
					continue;
				}

				List<RenderOptions.StyleOption> allColumnStyles = Stream.concat(columnStyle.stream(), columnWidths.stream())
						.collect(Collectors.toList());
				if (getStyleForKey(var, allColumnStyles).isEmpty()) {
					renderResult.appendHtml("<td>");
				}
				else if (getStyleForKey(var, columnWidths).isEmpty()) {
					renderResult.appendHtml("<td style='" + getStyleForKey(var, columnStyle) + "'>");
				}
				else {
					renderResult.appendHtml("<td style='" + getStyleForKey(var, allColumnStyles) + "; overflow-wrap: break-word'>");
				}

				renderNode(row, var, user, opts, renderResult);

				renderResult.appendHtml("</td>\n");
			}
			renderResult.appendHtml("</tr>");
		}
		renderResult.appendHtml("</tbody>");
		renderResult.appendHtml("</table>");
		if (qrt.getEvaluationTime() > 1000) {
			renderResult.appendHtmlElement("span", "Query evaluation time: " + Stopwatch.getDisplay(qrt.getEvaluationTime()), "class", "sparql-evaluation-time");
		}
		renderResult.appendHtml("</div>");
		return new SparqlRenderResult(renderResult.toStringRaw());
	}

	private static @NotNull Set<String> getDateColumns(IndexedResultTableModel table) {
		Set<String> dateColumns = new HashSet<>();
		for (String variable : table.getVariables()) {
			if (isDateColumn(table, variable)) {
				dateColumns.add(variable);
			}
		}
		return dateColumns;
	}

	/** Checks a single column and exits early on first non-date value.
	 *  Also requires at least one date-typed literal to avoid vacuous truth.
	 */
	private static boolean isDateColumn(IndexedResultTableModel table, String variable) {
		boolean sawDateLiteral = false;
		// Iterate rows and break as soon as the column cannot be a date column
		for (var it = table.rows().iterator(); it.hasNext(); ) {
			Value v = it.next().getValue(variable);
			if (v == null) continue;
			if (!(v instanceof Literal literal)) return false;
			if (!DATE_TYPE_URIS.contains(literal.getDatatype())) return false;
			sawDateLiteral = true; // saw at least one valid date literal
		}
		return sawDateLiteral;
	}

	@NotNull
	private static String getColumnDisplayName(String var) {
		return var.replace("_", " ");
	}

	private Comparator<Value> createValueComparator(RenderOptions opts, String column, boolean ascending) {
		ValueComparator comparator = opts.getColumnSorting(column);
		return ascending ? comparator : comparator.reversed();
	}

	@SuppressWarnings("RedundantIfStatement")
	private boolean isSkipped(boolean isTree, int column, String var) {
		boolean skip = false;
		// ignore first two columns if we are in tree mode
		if (isTree && column < 2) {
			skip = true;
		}
		// ignore column 'sortValue', which should be hidden
		if (isTree && var.equals(ResultTableHierarchy.SORT_VALUE)) {
			skip = true;
		}
		return skip;
	}

	private boolean isEmpty(TupleQueryResult qrt) {
		if (qrt.getBindingSets().isEmpty()) {
			return true;
		}
		if (qrt.getBindingSets().size() == 1) {
			// some queries return one result with only blank entries, we ignore them as their rendering is weird
			BindingSet bindings = qrt.getBindingSets().get(0);
			boolean empty = true;
			for (Binding binding : bindings) {
				if (!Strings.isBlank(binding.getValue().stringValue())) {
					empty = false;
					break;
				}
			}
			return empty;
		}
		return false;
	}

	public void getTreeChildren(Section<? extends SparqlType> section, String parentNodeID, UserActionContext user, RenderResult result) {
		parentNodeID = parentNodeID.replace("sparql-id-", "");
		final IndexedResultTableModel table = section.getObject(RESULT_TABLE);
		final ResultTableHierarchy tree = section.getObject(RESULT_TABLE_TREE);
		RenderOptions opts = section.get().getRenderOptions(section, user);
		boolean isTree = opts.isTree();
		String query = section.get().getSparqlQuery(section, user);

		TupleQueryResult qrt = null;
		try {
			qrt = opts.getRdf2GoCore().sparqlSelect(query, new Rdf2GoCore.Options().timeout(opts.getTimeout()));
			qrt = section.get().postProcessResult(qrt, user, opts);
		}
		catch (RuntimeException e) {
			handleRuntimeException(section, user, result, e);
		}

		if (qrt != null) {
			List<String> variables = qrt.getBindingNames();
			Map<String, Value> usedIDs = section.getObject(USED_IDS);
			Collection<TableRow> parents = table.getRowsForKey(usedIDs.get(parentNodeID));
			for (TableRow parent : parents) {
				for (TableRow child : tree.getChildren(parent)) {
					Value childValue = child.getValue(qrt.getBindingNames().get(0));
					String childId = valueToID(childValue, parentNodeID);
					usedIDs.put(childId, childValue);
					result.appendHtml("<tr class='treetr' data-tt-id='sparql-id-").append(childId).append("'");
					result.append(" data-tt-parent-id='sparql-id-")
							.append(parentNodeID).append("'");
					if (!tree.getChildren(child).isEmpty()) {
						result.append(" data-tt-branch='true' ");
					}
					result.append(">");
					int column = 0;
					for (String var : variables) {
						if (isSkipped(isTree, column++, var)) {
							continue;
						}
						result.appendHtml("<td>");
						renderNode(child, var, user, opts, result);
						result.appendHtml("</td>\n");
					}
					result.appendHtml("</tr>");
				}
			}
		}
	}

	private String valueToID(Value childValue, String parentValue) {
		if (childValue == null) {
			return null;
		}
		String valueString = childValue.stringValue() + parentValue;
		int code = valueString.replaceAll("[\\s\"]+", "").hashCode();
		return Integer.toString(code);
	}

	public void renderNode(TableRow tableRow, String var, UserContext user, RenderOptions opts, RenderResult result) {
		String nodeResult = renderNode(tableRow.getValue(var), var, opts.isRawOutput(), user, opts.getRdf2GoCore(), opts.getRenderMode());

		if (opts.isAllowJSPWikiMarkup()) {
			nodeResult = renderValidJspWikiLinks(user, nodeResult);
			result.append(nodeResult);
		}
		else {
			result.appendJSPWikiMarkup(nodeResult);
		}
	}

	/**
	 * Mask the usual JSPWiki tokens except [ and ], since they are not recognized in tables. Escaping will result in a
	 * rendered ~[ and ~].
	 *
	 * @param text the text to mask
	 * @return the masked text
	 */
	public String maskJSPWikiMarkupInTables(String text) {
		StringBuilder builder = new StringBuilder(text);
		KnowWEUtils.maskJSPWikiTokens(JSPWIKI_ESCAPE_TOKENS, builder);
		return builder.toString();
	}

	/**
	 * JSPWiki seems to ignore links embedded in tables, we parse them manually
	 */
	public String renderValidJspWikiLinks(UserContext user, String text) {
		StringBuilder links = new StringBuilder();
		final Matcher matcher = ARTICLE_LINK_PATTERN.matcher(text);
		int index = 0;
		while (matcher.find()) {
			links.append(text, index, matcher.start());

			String linkUrl = Strings.trim(matcher.group(2));
			String linkLabel = matcher.group(1) == null ? linkUrl : Strings.trim(matcher.group(1));
			boolean pageExists = user.getArticleManager() != null
								 && user.getArticleManager().getArticle(matcher.group(2)) != null;
			boolean internal = matcher.group(3) == null;
			if (internal && pageExists) {
				linkUrl = KnowWEUtils.getURLLink(linkUrl);
			}
			else if (internal) {
				continue; // probably not a link, just ignore
			}

			links.append("<a ").append(internal ? "" : "class='external' ").append("href='")
					.append(linkUrl)
					.append("'>")
					.append(linkLabel)
					.append("</a>");

			index = matcher.end();
		}
		if (index > 0) {
			links.append(text, index, text.length());
			return links.toString();
		}
		else {
			return text;
		}
	}

	@NotNull
	private String generateReplacement(String erg, Matcher matcher, String linkLabel, String linkUrl) {
		return erg.substring(0, matcher.start()) +
			   "<a class='external' href='" + linkUrl + "'>" + linkLabel + "</a>" +
			   erg.substring(matcher.end());
	}

	public String renderNode(Value node, String var, boolean rawOutput, UserContext user, Rdf2GoCore core, RenderOptions.RenderMode mode) {
		if (node == null) {
			return "";
		}
		String rendered;
		if (rawOutput && node instanceof Literal) {
			rendered = renderLiteral((Literal) node);
		}
		else {
			rendered = node.stringValue();
		}
		if (!rawOutput) {
			for (SparqlResultNodeRenderer nodeRenderer : this.nodeRenderers) {
				if (node instanceof Literal && nodeRenderer instanceof DecodeUrlNodeRenderer) {
					continue;
				}
				String temp = rendered;
				rendered = nodeRenderer.renderNode(node, rendered, var, user, core, mode);
				if (!temp.equals(rendered) && !nodeRenderer.allowFollowUpRenderer()) {
					break;
				}
			}
			// rendered = KnowWEUtils.maskJSPWikiMarkup(rendered);
		}
		return rendered;
	}

	/**
	 * Right now we cant use Literal#toString, because it renders the xsd inside < >, which somehow
	 * does not render in JSPWiki.
	 */
	private String renderLiteral(Literal node) {
		StringBuilder sb = new StringBuilder();

		sb.append('"');
		sb.append(node.getLabel());
		sb.append('"');

		node.getLanguage().ifPresent(language -> {
			sb.append('@');
			sb.append(language);
		});

		IRI datatype = node.getDatatype();
		if (datatype != null) {
			sb.append("^^");
			sb.append(datatype);
		}

		return sb.toString();
	}

	private String getStyleForKey(String key, List<RenderOptions.StyleOption> styles) {
		if (styles == null) {
			return "";
		}

		List<RenderOptions.StyleOption> columnStyles = styles.stream()
				.filter(a -> Strings.equalsIgnoreCase(a.getColumnName(), key))
				.toList();
		if (columnStyles.isEmpty()) {
			return "";
		}
		StringBuilder styleHtmlBuffer = new StringBuilder();
		for (Iterator<RenderOptions.StyleOption> iterator = columnStyles.iterator(); iterator.hasNext(); ) {
			RenderOptions.StyleOption styleOption = iterator.next();
			styleHtmlBuffer.append(styleOption.getStyleKey());
			styleHtmlBuffer.append(": ");
			styleHtmlBuffer.append(styleOption.getStyleValue());
			if (iterator.hasNext()) {
				styleHtmlBuffer.append("; ");
			}
		}
		return styleHtmlBuffer.toString();
	}

	/**
	 * add style options for the column group.
	 * Can be used if, for some reason, it is not practicable to set the column style for each <td>
	 *
	 * @param renderResult: RenderResult
	 * @param variables:    List of variables
	 * @param opts:         RenderOptions
	 * @return the updated RenderResult
	 */
	private RenderResult addColumnGroupStyles(RenderResult renderResult, List<String> variables, RenderOptions opts, List<RenderOptions.StyleOption> styleOptions) {
		int column = 0;
		for (String var : variables) {
			if (isSkipped(opts.isTree(), column++, var)) {
				continue;
			}
			renderResult.appendHtml("<col style='" + getStyleForKey(var, styleOptions) + "'>");
		}
		return renderResult;
	}
}
