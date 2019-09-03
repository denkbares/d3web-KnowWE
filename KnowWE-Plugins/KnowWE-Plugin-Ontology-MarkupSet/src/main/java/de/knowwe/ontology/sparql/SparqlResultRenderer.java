package de.knowwe.ontology.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;

import com.denkbares.plugin.Extension;
import com.denkbares.plugin.PluginManager;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.utils.ResultTableHierarchy;
import com.denkbares.semanticcore.utils.ResultTableModel;
import com.denkbares.semanticcore.utils.TableRow;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Pair;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.ontology.compile.OntologyType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.sparql.utils.SparqlRenderResult;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.util.Color;

public class SparqlResultRenderer {

	private static final String POINT_ID = "SparqlResultNodeRenderer";
	private static final String RESULT_TABLE = "resultTable";
	private static final String RESULT_TABLE_TREE = "resultTableTree";
	private static final String USED_IDS = "usedIDs";

	private static SparqlResultRenderer instance = null;

	private List<SparqlResultNodeRenderer> nodeRenderers;

	public static SparqlResultRenderer getInstance() {
		if (instance == null) instance = new SparqlResultRenderer();
		return instance;
	}

	private SparqlResultRenderer() {
		List<SparqlResultNodeRenderer> nodeRenderer = getNodeRenderers();
		optimizeNodeRenderer(nodeRenderer);
		nodeRenderers = nodeRenderer;
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
		if (containsBoth) nodeRenderer.remove(rnnRenderer);
	}

	public static List<SparqlResultNodeRenderer> getNodeRenderers() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				OntologyType.PLUGIN_ID, POINT_ID);
		List<SparqlResultNodeRenderer> renderers = new ArrayList<>();
		for (Extension extension : extensions) {
			renderers.add((SparqlResultNodeRenderer) extension.getSingleton());
		}
		return renderers;
	}

	public void setNodeRenderers(List<SparqlResultNodeRenderer> nodeRenderers) {
		this.nodeRenderers = nodeRenderers;
	}

	public void renderSparqlResult(Section<? extends SparqlType> section, UserContext user, RenderResult result) {

		String query = section.get().getSparqlQuery(section, user);
		RenderOptions opts = section.get().getRenderOptions(section, user);
		result.appendHtml("<div class='sparqlTable' sparqlSectionId='" + opts.getId() + "' id='sparqlTable_" + opts
				.getId() + "'>");
		if (opts.isBorder()) result.appendHtml("<div class='border'>");

		SparqlRenderResult renderResult;

		CachedTupleQueryResult qrt = null;
		try {
			qrt = (CachedTupleQueryResult) opts.getRdf2GoCore()
					.sparqlSelect(query, new Rdf2GoCore.Options(opts.getTimeout()));
			qrt = section.get().postProcessResult(qrt, user, opts);
		}
		catch (RuntimeException e) {
			handleRuntimeException(section, user, result, e);
		}
		if (qrt != null) {
			renderResult = getSparqlRenderResult(qrt, opts, user, section);

			result.appendHtml(renderResult.getHTML());
		}
		if (opts.isBorder()) result.appendHtml("</div>");
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
			if (message == null) message = "RuntimeException without message.";
			message = Strings.trimRight(message);
			if (!message.endsWith(".")) message += ".";
			result.append(message);
		}
	}

	private static void appendMalformedQueryMessage(Section<? extends SparqlType> section, String message, UserContext user, RenderResult result) {
		message = Strings.encodeHtml(message);
		String query = section.get().getSparqlQuery(section, user);
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(section);
		if (query == null || core == null || Strings.trim(query).isEmpty()) {
			result.append(message);
			return;
		}

		Matcher lineMatcher = Pattern.compile("(?:at line )(\\d+)").matcher(message);
		int lineNumber = -1;
		if (lineMatcher.find()) {
			lineNumber = Integer.parseInt(lineMatcher.group(1)) - 1;
		}

		Matcher columnMatcher = Pattern.compile("(?:, column )(\\d+)").matcher(message);
		int columnNumber = -1;
		if (columnMatcher.find()) {
			columnNumber = Integer.parseInt(columnMatcher.group(1)) - 1;
		}
		if (columnNumber == -1 || lineNumber == -1) {
			result.append(message);
			return;
		}

		query = core.prependPrefixesToQuery(core.getNamespaces(), query);
		String queryLine = query.split("\n")[lineNumber];

		if (queryLine == null) {
			result.append(message);
			return;
		}
		int start = Math.max(0, columnNumber - 15);
		int end = Math.min(columnNumber + 15, queryLine.length());
		while (start > 0 && queryLine.charAt(start) != ' ') start--;
		if (start > 0) start++;
		while (end < queryLine.length() && queryLine.charAt(end) != ' ') end++;
		String queryContextPrefix = "..." + queryLine.substring(start, columnNumber);
		String charAtException = columnNumber >= queryLine.length() ? "<EOL>" : queryLine.substring(columnNumber, columnNumber + 1);
		String queryContextSuffix = columnNumber >= queryLine.length() ? "" : queryLine.substring(columnNumber + 1, end) + "...";

		Scanner scanner = new Scanner(message);
		boolean first = true;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (first) {
				int index = line.indexOf("Encountered ");
				if (index >= 0) {
					result.append(line.substring(0, index + 12))
							.append("'").append(charAtException).append("' ")
							.append("at line ").append(lineNumber + 1)
							.append(", column ").append(columnNumber + 1).append(".");
				}
				else {
					result.append(line);
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
	public SparqlRenderResult getSparqlRenderResult(CachedTupleQueryResult qrt, RenderOptions opts, UserContext user, Section section) {
		Compilers.awaitTermination(section.getArticleManager().getCompilerManager());
		try {
			return renderQueryResultLocked(qrt, opts, user, section);
		}
		catch (Throwable e) {
			String message = "Exception while rendering SPARQL result";
			Log.severe(message, e);
			return new SparqlRenderResult(new RenderResult(user).appendException(e).toStringRaw());
		}
	}

	private SparqlRenderResult renderQueryResultLocked(CachedTupleQueryResult qrt, RenderOptions opts, UserContext user, Section<?> section) {

		RenderResult renderResult = new RenderResult(user);
		if (isEmpty(qrt)) {
			renderResult.appendHtmlElement("span", "No results for this query", "class", "emptySparqlResult");
			return new SparqlRenderResult(renderResult.toStringRaw());
		}

		boolean zebraMode = opts.isZebraMode();
		boolean rawOutput = opts.isRawOutput();
		boolean isTree = opts.isTree();
		boolean isNavigation = opts.isNavigation();
		List<RenderOptions.StyleOption> columnStyle = opts.getColumnStyles();
		List<RenderOptions.StyleOption> tableStyle = opts.getTableStyles();
		if (opts.getColor() != Color.NONE) tableStyle.add(new RenderOptions.StyleOption("table", "border-color", opts.getColor().getColorValue()));

		String tableID = UUID.randomUUID().toString();

		List<String> variables = qrt.getBindingNames();

		// tree table init
		String childIdVariable = null;
		String parentIdVariable = null;
		if (isTree) {
			if (qrt.getBindingNames().size() > 2) {
				childIdVariable = qrt.getBindingNames().get(0);
				parentIdVariable = qrt.getBindingNames().get(1);
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

		renderResult.appendHtmlTag("div", "style", "overflow-x: auto");
		renderResult.appendHtml("<table id='").append(tableID).appendHtml("'")
				.append(isTree
						? " class='sparqltable sparqltreetable"
						: " class='sparqltable")
				.append(opts.getColor() == Color.NONE ? "" : " logLevel")
				.append("'")
				.append(opts.isSorting() ? " sortable='multi'" : "")
				.append(getStyleForKey("table", tableStyle))
				.append(">");

		int column = 0;
		for (String var : variables) {
			if (isSkipped(isTree, column++, var)) {
				continue;
			}
			renderResult.appendHtml("<col" + getStyleForKey(var, columnStyle) +">");
		}

		renderResult.appendHtml(!zebraMode ? "<tr>" : "<tr class='odd'>");

		column = 0;
		for (String var : variables) {
			if (isSkipped(isTree, column++, var)) {
				continue;
			}
			renderResult.appendHtml("<th>");
			renderResult.append(var.replace("_", " "));
			renderResult.appendHtml("</th>");
		}
		renderResult.appendHtml("</tr>");
		ResultTableModel table = new ResultTableModel(qrt);
		PaginationRenderer.setResultSize(user, table.getSize());
		Iterator<TableRow> iterator;
		if (isNavigation) {
			if (opts.isSorting()) {
				List<Pair<String, Boolean>> multiColumnSorting = PaginationRenderer.getMultiColumnSorting(section, user);
				table.sortRows(multiColumnSorting);
			}
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
			Log.info("Create hierarchical sparql result table in " + stopwatch.getDisplay());
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
				ResultTableHierarchy tree = (ResultTableHierarchy) section.getObject(RESULT_TABLE_TREE);
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
			for (String var : variables) {
				if (isSkipped(isTree, column++, var)) {
					continue;
				}

				Value node = row.getValue(var);
				String erg = renderNode(node, var, rawOutput, user, opts.getRdf2GoCore(),
						getRenderMode(section));

				renderResult.appendHtml("<td>");
				renderResult.append(erg);
				renderResult.appendHtml("</td>\n");
			}
			renderResult.appendHtml("</tr>");
		}

		renderResult.appendHtml("</table>");
		renderResult.appendHtml("</div>");
		return new SparqlRenderResult(renderResult.toStringRaw());
	}

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

	private boolean isEmpty(CachedTupleQueryResult qrt) {
		if (qrt.getBindingSets().isEmpty()) return true;
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

	private RenderMode getRenderMode(Section<?> section) {
		Section<DefaultMarkupType> defaultMarkupTypeSection = Sections.ancestor(section, DefaultMarkupType.class);
		String annotation = DefaultMarkupType.getAnnotation(defaultMarkupTypeSection, SparqlMarkupType.RENDER_MODE);
		if (annotation != null) {
			try {
				return RenderMode.valueOf(annotation);
			}
			catch (IllegalArgumentException e) {
				Log.severe("Invalid render mode: " + annotation, e);
			}
		}
		return RenderMode.HTML;
	}

	public void getTreeChildren(Section<? extends SparqlType> section, String parentNodeID, UserActionContext user, RenderResult result) {
		parentNodeID = parentNodeID.replace("sparql-id-", "");
		ResultTableModel table = (ResultTableModel) section.getObject(RESULT_TABLE);
		ResultTableHierarchy tree = (ResultTableHierarchy) section.getObject(RESULT_TABLE_TREE);
		RenderOptions opts = section.get().getRenderOptions(section, user);
		boolean isTree = opts.isTree();
		String query = section.get().getSparqlQuery(section, user);

		CachedTupleQueryResult qrt = null;
		try {
			qrt = (CachedTupleQueryResult) opts.getRdf2GoCore()
					.sparqlSelect(query, new Rdf2GoCore.Options(opts.getTimeout()));
			qrt = section.get().postProcessResult(qrt, user, opts);
		}
		catch (RuntimeException e) {
			handleRuntimeException(section, user, result, e);
		}

		if (qrt != null) {
			List<String> variables = qrt.getBindingNames();
			@SuppressWarnings("unchecked") Map<String, Value> usedIDs = (Map<String, Value>) section.getObject(USED_IDS);
			Collection<TableRow> parents = table.findRowFor(usedIDs.get(parentNodeID));
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

						Value node = child.getValue(var);
						String erg = renderNode(node, var, opts.isRawOutput(), user, opts.getRdf2GoCore(),
								getRenderMode(section));

						result.appendHtml("<td>");
						result.append(erg);
						result.appendHtml("</td>\n");
					}
					result.appendHtml("</tr>");
				}
			}
		}
	}

	private String valueToID(Value childValue, String parentValue) {
		if (childValue == null) return null;
		String valueString = childValue.stringValue() + parentValue;
		int code = valueString.replaceAll("[\\s\"]+", "").hashCode();
		return Integer.toString(code);
	}

	public String renderNode(Value node, String var, boolean rawOutput, UserContext user, Rdf2GoCore core, RenderMode mode) {
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
			for (SparqlResultNodeRenderer nodeRenderer : nodeRenderers) {
				if (node instanceof Literal && nodeRenderer instanceof DecodeUrlNodeRenderer) {
					continue;
				}

				String temp = rendered;
				rendered = nodeRenderer.renderNode(node, rendered, var, user, core, mode);
				if (!temp.equals(rendered) && !nodeRenderer.allowFollowUpRenderer()) break;
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
		if (styles == null){
			return "";
		}

		List<RenderOptions.StyleOption> columnStyles = styles.stream().filter(a -> Strings.equalsIgnoreCase(a.getColumnName(), key)).collect(Collectors.toList());
		if (columnStyles.isEmpty()) return "";
		StringBuilder styleHtmlBuffer = new StringBuilder();
		styleHtmlBuffer.append(" style='");
		for (Iterator<RenderOptions.StyleOption> iterator = columnStyles.iterator(); iterator.hasNext(); ) {
			RenderOptions.StyleOption styleOption = iterator.next();
			styleHtmlBuffer.append(styleOption.getStyleKey());
			styleHtmlBuffer.append(": ");
			styleHtmlBuffer.append(styleOption.getStyleValue());
			if (iterator.hasNext()) styleHtmlBuffer.append("; ");
		}
		styleHtmlBuffer.append("'");
		return styleHtmlBuffer.toString();
	}
}
