package de.knowwe.ontology.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.denkbares.collections.PartialHierarchy;
import com.denkbares.collections.PartialHierarchyException;
import com.denkbares.collections.PartialHierarchyTree;
import com.denkbares.plugin.Extension;
import com.denkbares.plugin.PluginManager;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Pair;
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
import de.knowwe.rdf2go.utils.ResultTableModel;
import de.knowwe.rdf2go.utils.SimpleTableRow;
import de.knowwe.rdf2go.utils.TableRow;

public class SparqlResultRenderer {

	private static final String POINT_ID = "SparqlResultNodeRenderer";

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
			qrt = (CachedTupleQueryResult) opts.getRdf2GoCore().sparqlSelect(query, true, opts.getTimeout());
			qrt = section.get().postProcessResult(qrt, user, opts);
		}
		catch (RuntimeException e) {
			handleRuntimeException(section, result, e);
		}
		if (qrt != null) {
			renderResult = getSparqlRenderResult(qrt, opts, user, section);

			result.appendHtml(renderResult.getHTML());
		}
		if (opts.isBorder()) result.appendHtml("</div>");
		result.appendHtml("</div>");
	}

	public static void handleRuntimeException(Section<? extends SparqlType> section, RenderResult result, RuntimeException e) {
		String message = e.getMessage();
		message = Strings.trimRight(message);
		if (!message.endsWith(".")) message += ".";
		result.appendHtml("<span class='warning'>"
				+ message + " <a onclick='KNOWWE.plugin.sparql.retry(\"" + section.getID()
				+ "\")' title='Try executing the query again, if you think it was only a temporary problem.'"
				+ " class='tooltipster'>Try again...</a></span>");
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
		if (!qrt.iterator().hasNext()) {
			renderResult.appendHtmlElement("span", "No results for this query", "class", "emptySparqlResult");
			return new SparqlRenderResult(renderResult.toStringRaw());
		}

		boolean zebraMode = opts.isZebraMode();
		boolean rawOutput = opts.isRawOutput();
		boolean isTree = opts.isTree();
		boolean isNavigation = opts.isNavigation();

		String tableID = UUID.randomUUID().toString();

		List<String> variables = qrt.getBindingNames();

		// tree table init
		String idVariable = null;
		String parentVariable = null;
		if (isTree) {
			if (qrt.getBindingNames().size() > 2) {
				idVariable = qrt.getBindingNames().get(0);
				parentVariable = qrt.getBindingNames().get(1);
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
						? " class='sparqltable sparqltreetable'"
						: " class='sparqltable'")
				.append(opts.isSorting() ? " sortable='multi'" : "")
				.append(">");
		renderResult.appendHtml(!zebraMode ? "<tr>" : "<tr class='odd'>");
		int column = 0;
		for (String var : variables) {
			// ignore first two columns if we are in tree mode
			if (isTree && column++ < 2) {
				continue;
			}
			renderResult.appendHtml("<th>");
			renderResult.append(var);
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
			table = createMagicallySortedTable(table, renderResult);
			iterator = table.iterator();
		}
		else {
			iterator = table.iterator();
		}

		List<String> classNames = new LinkedList<>();
		Set<String> usedIDs = new HashSet<>();
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
				String valueID = valueToID(idVariable, row);
				boolean isNew = usedIDs.add(valueID);
				if (!isNew) {
					valueID = UUID.randomUUID().toString();
				}
				renderResult.append(" data-tt-id='sparql-id-").append(valueID).append("'");
				String parentID = valueToID(parentVariable, row);
				if (!Strings.isBlank(parentID) && !parentID.equals(valueID) && usedIDs.contains(parentID)) {
					renderResult.append(" data-tt-parent-id='sparql-id-")
							.append(parentID).append("'");
				}
			}
			renderResult.append(">");

			column = 0;
			for (String var : variables) {
				// ignore first two columns if we are in tree mode
				if (isTree && column++ < 2) {
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

	private ResultTableModel createMagicallySortedTable(ResultTableModel table, RenderResult renderResult) {
		// creating hierarchy order using PartialHierarchyTree
		ResultTableHierarchy resultTableHierarchy = new ResultTableHierarchy(table);
		PartialHierarchyTree<TableRow> tree = new
				PartialHierarchyTree<>(
				resultTableHierarchy);

		// add all nodes to create the tree
		Iterator<TableRow> iterator = table.iterator();
		while (iterator.hasNext()) {
			TableRow next = iterator.next();
			try {
				tree.insert(next);
			}
			catch (PartialHierarchyException e) {
				renderResult.appendException(e.getMessage(), e);
				Log.severe("Exception while rendering sorted table.", e);
			}
		}

		// DFS traversion will create the desired order
		// List<TableRow> nodesDFSOrder = tree.getNodesDFSOrder();

		ResultTableModel result = new ResultTableModel(table.getVariables());

		List<PartialHierarchyTree.Node<TableRow>> rootLevelNodes = tree.getRootLevelNodesSorted(getComparator(result));
		for (PartialHierarchyTree.Node<TableRow> node : rootLevelNodes) {
			TableRow row = node.getData();
			Value topLevelConcept = row.getValue(table.getVariables().get(1));

			if (topLevelConcept != null
					&& !topLevelConcept.equals(row.getValue(table.getVariables().get(1)))) {
				// check whether an artificial root node is needed
				SimpleTableRow artificialTopLevelRow = new SimpleTableRow();
				artificialTopLevelRow.addValue(table.getVariables().get(0), topLevelConcept);
				artificialTopLevelRow.addValue(table.getVariables().get(1), topLevelConcept);
				artificialTopLevelRow.addValue(table.getVariables().get(2), topLevelConcept);
				result.addTableRow(artificialTopLevelRow);
			}

			addRowRecursively(node, result);
		}

		return result;
	}

	private void addRowRecursively(PartialHierarchyTree.Node<TableRow> node, final ResultTableModel result) {
		// add current row
		result.addTableRow(node.getData());

		// sort children order alphabetical
		List<PartialHierarchyTree.Node<TableRow>> children = node.getChildrenSorted(getComparator(result));

		// add all children recursively
		for (PartialHierarchyTree.Node<TableRow> child : children) {
			addRowRecursively(child, result);
		}

	}

	private Comparator<TableRow> getComparator(final ResultTableModel result) {
		return (o1, o2) -> {
			Value concept1 = o1.getValue(result.getVariables().get(0));
			Value concept2 = o2.getValue(result.getVariables().get(0));
			if (result.getVariables().size() >= 3) {
				Value tmp1 = o1.getValue(result.getVariables().get(2));
				Value tmp2 = o2.getValue(result.getVariables().get(2));
				if (tmp1 != null && tmp2 != null) {
					concept1 = o1.getValue(result.getVariables().get(2));
					concept2 = o2.getValue(result.getVariables().get(2));
				}

			}

			return concept1.toString().compareTo(concept2.toString());
		};
	}

	private static class ResultTableHierarchy implements PartialHierarchy<TableRow> {

		private final ResultTableModel data;

		public ResultTableHierarchy(ResultTableModel data) {
			this.data = data;
		}

		@Override
		public boolean isSuccessorOf(TableRow row1, TableRow row2) throws PartialHierarchyException {
			LinkedHashSet<TableRow> path = new LinkedHashSet<>();
			return checkSuccessorshipRecursively(row1, row2, path);
		}

		@SuppressWarnings("SimplifiableIfStatement")
		private boolean checkSuccessorshipRecursively(TableRow ascendor, TableRow ancestor, Set<TableRow> path) throws PartialHierarchyException {

			Value ascendorNode = ascendor.getValue(data.getVariables().get(0));
			Value potentialAncestorNode = ancestor.getValue(data.getVariables().get(0));

			if (ascendorNode.equals(potentialAncestorNode)) return true;

			Value ascendorParent = ascendor.getValue(data.getVariables().get(1));
			if (ascendorNode.equals(ascendorParent)) {
				// is artificial, invalid root node
				return false;
			}
			Collection<TableRow> parentRows = data.findRowFor(ascendorParent);
			if (parentRows == null || parentRows.isEmpty()) return false;

			// we remember the path to detect cycles
			path.add(ascendor);

			TableRow parent = parentRows.iterator().next();
			if (path.contains(parent)) {
				throw new PartialHierarchyException(parent, path);
			}
			return checkSuccessorshipRecursively(parent, ancestor, path);
		}

	}

	private String valueToID(String variable, TableRow row) {
		Value value = row.getValue(variable);
		if (value == null) return null;
		int code = value.toString().replaceAll("[\\s\"]+", "").hashCode();
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
				if (node instanceof Literal && nodeRenderer instanceof DecodeUrlNodeRenderer) continue;

				String temp = rendered;
				rendered = nodeRenderer.renderNode(node, rendered, var, user, core, mode);
				if (!temp.equals(rendered) && !nodeRenderer.allowFollowUpRenderer()) break;
			}
			// rendered = KnowWEUtils.maskJSPWikiMarkup(rendered);
		}
		return rendered;
	}

	/**
	 * Right now we cant use Literal#toString, because it renders the xsd inside < >, which somehow does not render in
	 * JSPWiki.
	 */
	private String renderLiteral(Literal node) {
		StringBuilder sb = new StringBuilder();

		sb.append('"');
		sb.append(node.getLabel());
		sb.append('"');

		String language = node.getLanguage();
		if (language != null) {
			sb.append('@');
			sb.append(language);
		}

		URI datatype = node.getDatatype();
		if (datatype != null) {
			sb.append("^^");
			sb.append(datatype);
			sb.append("");
		}

		return sb.toString();
	}

}
