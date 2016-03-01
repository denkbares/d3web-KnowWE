package de.knowwe.ontology.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.collections.PartialHierarchy;
import de.d3web.collections.PartialHierarchyTree;
import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.d3web.utils.Pair;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.PaginationRenderer;
import de.knowwe.ontology.compile.OntologyType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.sparql.utils.SparqlRenderResult;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
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

	public List<SparqlResultNodeRenderer> getNodeRenderers() {
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

		QueryResultTable qrt = null;
		try {
			qrt = opts.getRdf2GoCore().sparqlSelect(query, true, opts.getTimeout());
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
		message = message.replaceAll("[^.]\\s*$", "."); // clean up message end
		result.appendHtml("<span class='warning'>"
				+ message + " <a onclick='KNOWWE.plugin.sparql.retry(\"" + section.getID()
				+ "\")' title='Try executing the query again, if you think it was only a temporary problem.'"
				+ " class='tooltipster'>Try again...</a></span>");
		Log.warning("Exception while executing SPARQL", e);
	}

	public SparqlRenderResult getSparqlRenderResult(QueryResultTable qrt, UserContext user, Section<?> section) {
		RenderOptions opts = new RenderOptions("defaultID");
		//noinspection deprecation
		opts.setRdf2GoCore(Rdf2GoCore.getInstance());
		return getSparqlRenderResult(qrt, opts, user, section);
	}

	/**
	 * @param qrt  the query result to render
	 * @param opts the options to control the rendered output
	 * @return html table with all results of qrt and size of qrt
	 * @created 06.12.2010
	 */
	public SparqlRenderResult getSparqlRenderResult(QueryResultTable qrt, RenderOptions opts, UserContext user, Section section) {
		Compilers.awaitTermination(section.getArticleManager().getCompilerManager());
		Rdf2GoUtils.lock(qrt);
		try {
			return renderQueryResultLocked(qrt, opts, user, section);
		}
		finally {
			Rdf2GoUtils.unlock(qrt);
		}
	}

	private SparqlRenderResult renderQueryResultLocked(QueryResultTable qrt, RenderOptions opts, UserContext user, Section<?> section) {

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

		List<String> variables = qrt.getVariables();

		// tree table init
		String idVariable = null;
		String parentVariable = null;
		if (isTree) {
			if (qrt.getVariables().size() > 2) {
				idVariable = qrt.getVariables().get(0);
				parentVariable = qrt.getVariables().get(1);
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
				.append(isNavigation ? " sortable='multi'" : "")
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
			List<Pair<String, Boolean>> multiColumnSorting = PaginationRenderer.getMultiColumnSorting(section, user);
			table.sortRows(multiColumnSorting);
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
			table = createMagicallySortedTable(table);
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

				Node node = row.getValue(var);
				String erg = renderNode(node, var, rawOutput, user, opts.getRdf2GoCore(),
						RenderMode.HTML);

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

	private ResultTableModel createMagicallySortedTable(ResultTableModel table) {
		// creating hierarchy order using PartialHierarchyTree
		PartialHierarchyTree<TableRow> tree = new
				PartialHierarchyTree<>(
				new ResultTableHierarchy(table));

		// add all nodes to create the tree
		Iterator<TableRow> iterator = table.iterator();
		while (iterator.hasNext()) {
			tree.insertNode(iterator.next());
		}

		// DFS traversion will create the desired order
		// List<TableRow> nodesDFSOrder = tree.getNodesDFSOrder();

		ResultTableModel result = new ResultTableModel(table.getVariables());

		List<de.d3web.collections.PartialHierarchyTree.Node<TableRow>> rootLevelNodes = tree.getRootLevelNodesSorted(getComparator(result));
		for (de.d3web.collections.PartialHierarchyTree.Node<TableRow> node : rootLevelNodes) {
			TableRow row = node.getData();
			Node topLevelConcept = row.getValue(table.getVariables().get(1));

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

	private void addRowRecursively(de.d3web.collections.PartialHierarchyTree.Node<TableRow> node, final ResultTableModel result) {
		// add current row
		result.addTableRow(node.getData());

		// sort children order alphabetical
		List<de.d3web.collections.PartialHierarchyTree.Node<TableRow>> children = node.getChildrenSorted(getComparator(result));

		// add all children recursively
		for (de.d3web.collections.PartialHierarchyTree.Node<TableRow> child : children) {
			addRowRecursively(child, result);
		}

	}

	private Comparator<TableRow> getComparator(final ResultTableModel result) {
		return (o1, o2) -> {
			Node concept1 = o1.getValue(result.getVariables().get(0));
			Node concept2 = o2.getValue(result.getVariables().get(0));
			if (result.getVariables().size() >= 3) {
				Node tmp1 = o1.getValue(result.getVariables().get(2));
				Node tmp2 = o2.getValue(result.getVariables().get(2));
				if (tmp1 != null && tmp2 != null) {
					concept1 = o1.getValue(result.getVariables().get(2));
					concept2 = o2.getValue(result.getVariables().get(2));
				}

			}

			return concept1.toString().compareTo(concept2.toString());
		};
	}

	class ResultTableHierarchy implements PartialHierarchy<TableRow> {

		private final ResultTableModel data;

		public ResultTableHierarchy(ResultTableModel data) {
			this.data = data;
		}

		@Override
		public boolean isSuccessorOf(TableRow row1, TableRow row2) {
			return checkSuccessorshipRecursively(row1, row2);
		}

		@SuppressWarnings("SimplifiableIfStatement")
		private boolean checkSuccessorshipRecursively(TableRow ascendor, TableRow ancestor) {

			Node ascendorNode = ascendor.getValue(data.getVariables().get(0));
			Node potentialAncestorNode = ancestor.getValue(data.getVariables().get(0));

			if (ascendorNode.equals(potentialAncestorNode)) return true;

			Node ascendorParent = ascendor.getValue(data.getVariables().get(1));
			if (ascendorNode.equals(ascendorParent)) {
				// is artificial, invalid root node
				return false;
			}
			Collection<TableRow> parentRows = data.findRowFor(ascendorParent);
			if (parentRows == null || parentRows.size() == 0) return false;

			return checkSuccessorshipRecursively(parentRows.iterator().next(), ancestor);
		}
	}

	private String valueToID(String variable, TableRow row) {
		Node value = row.getValue(variable);
		if (value == null) return null;
		int code = value.toString().replaceAll("[\\s\"]+", "").hashCode();
		return Integer.toString(code);
	}

	public String renderNode(Node node, String var, boolean rawOutput, UserContext user, Rdf2GoCore core, RenderMode mode) {
		if (node == null) {
			return "";
		}
		String rendered = node.toString();
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

}
