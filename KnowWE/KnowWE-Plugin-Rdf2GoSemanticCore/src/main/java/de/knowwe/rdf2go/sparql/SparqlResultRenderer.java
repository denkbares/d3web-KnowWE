package de.knowwe.rdf2go.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.collections.PartialHierarchy;
import de.d3web.collections.PartialHierarchyTree;
import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.d3web.utils.Pair;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.PaginationRenderer;
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

	private final SparqlResultNodeRenderer[] nodeRenderers;

	public static SparqlResultRenderer getInstance() {
		if (instance == null) instance = new SparqlResultRenderer();
		return instance;
	}

	private SparqlResultRenderer() {
		List<SparqlResultNodeRenderer> nodeRenderer = getNodeRenderer();
		optimizeNodeRenderer(nodeRenderer);
		nodeRenderers = nodeRenderer.toArray(new SparqlResultNodeRenderer[nodeRenderer.size()]);
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

	public List<SparqlResultNodeRenderer> getNodeRenderer() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				Rdf2GoCore.PLUGIN_ID, POINT_ID);
		List<SparqlResultNodeRenderer> renderers = new ArrayList<SparqlResultNodeRenderer>();
		for (Extension extension : extensions) {
			renderers.add((SparqlResultNodeRenderer) extension.getSingleton());
		}
		return renderers;
	}

	public void renderSparqlResult(Section<? extends SparqlType> section, UserContext user, RenderResult result) {

		String query = section.get().getSparqlQuery(section, user);
		RenderOptions opts = section.get().getRenderOptions(section, user);

		result.appendHtml("<div class='sparqlTable' sparqlSectionId='" + opts.getId() + "' id='sparqlTable_" + opts
				.getId() + "'>");
		if (opts.isBorder()) result.appendHtml("<div class='border'>");
		if (opts.isSorting()) {
			query = modifyOrderByInSparqlString(section, user, query);
		}

		SparqlRenderResult renderResult;

		QueryResultTable qrt = null;
		try {
			qrt = opts.getRdf2GoCore().sparqlSelect(query, true, opts.getTimeout());
		}
		catch (RuntimeException e) {
			result.appendHtml("<span class='warning'>"
					+ e.getMessage() + "</span>");
			Log.warning("Exception while executing SPARQL", e);
		}
		if (qrt != null) {
			renderResult = getSparqlRenderResult(qrt, opts, user, section);

//			if (opts.isNavigation() && !opts.isRawOutput()) {
//				renderTableSizeSelector(opts, renderResult.getSize(), result);
//				renderNavigation(opts, renderResult.getSize(), result);
//
//			}

			result.appendHtml(renderResult.getHTML());
		}
		if (opts.isBorder()) result.appendHtml("</div>");
		result.appendHtml("</div>");
	}

	public SparqlRenderResult getSparqlRenderResult(QueryResultTable qrt, UserContext user, Section<?> section) {
		RenderOptions opts = new RenderOptions("defaultID", user);
		opts.setRdf2GoCore(Rdf2GoCore.getInstance());
		return getSparqlRenderResult(qrt, opts, user, section);
	}

	/**
	 * @param qrt the query result to render
	 * @param opts the options to control the rendered output
	 * @return html table with all results of qrt and size of qrt
	 * @created 06.12.2010
	 */
	public SparqlRenderResult getSparqlRenderResult(QueryResultTable qrt, RenderOptions opts, UserContext user, Section section) {
		Rdf2GoUtils.lock(qrt);
		try {
			return renderQueryResultLocked(qrt, opts, user, section);
		}
		finally {
			Rdf2GoUtils.unlock(qrt);
		}
	}

	private SparqlRenderResult renderQueryResultLocked(QueryResultTable qrt, RenderOptions opts, UserContext user, Section<?> section) {

		RenderResult result = new RenderResult(user);
		int i = 0;
		if (!qrt.iterator().hasNext()) {
			result.append(Messages.getMessageBundle().getString(
					"KnowWE.owl.query.no_result"));
			return new SparqlRenderResult(result.toStringRaw(), i);
		}

		boolean zebraMode = opts.isZebraMode();
		boolean rawOutput = opts.isRawOutput();
		boolean isTree = opts.isTree();
		String tableID = UUID.randomUUID().toString();

		List<String> variables = qrt.getVariables();
		boolean tablemode = variables.size() > 1;

		// BEGIN: collapse tree mode code
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
				result.append("%%warning The result table requires at least three columns.");
			}
		}
		// END: collapse tree mode code

		if (tablemode) {
			result.appendHtmlTag("div", "style", "overflow-x: auto");
			result.appendHtml("<table id='")
					.append(tableID)
					.appendHtml("' sortable='multi' class='")
					.append(isTree ? "sparqltable sparqltreetable" : "sparqltable")
					.append("'>");
			result.appendHtml(!zebraMode ? "<tr>" : "<tr class='odd'>");
			int index = 0;
			for (String var : variables) {

				{// BEGIN: collapse tree mode code
					// ignore first two columns if we are in tree mode
					if (isTree && index++ < 2) {
						continue;
					}
				}// END: collapse tree mode code

				result.appendHtml("<th>");
//				result.appendHtml("<a href='#/' onclick=\"KNOWWE.plugin.sparql.sortResultsBy('"
//						+ var + "', '"
//						+ opts.getId() + "');\">");
				result.append(var);
//				result.appendHtml("</a>");
//				if (hasSorting(var, opts.getSortingMap())) {
//					String symbol = getSortingSymbol(var, opts.getSortingMap());
//					result.appendHtml("<img src='KnowWEExtension/images/" + symbol
//							+ "' alt='Sort by '"
				result.appendHtml("</th>&nbsp;");
//				}

			}
			result.appendHtml("</tr>");
		}
		else {
			result.appendHtml("<ul style='white-space: normal'>");
		}
		ResultTableModel table = new ResultTableModel(qrt);

		{// BEGIN: collapse tree mode code
			if (isTree) {
				table = createMagicallySortedTable(table);
			}
		}// END: collapse tree mode code

		Iterator<TableRow> iterator = table.iterator();
		List<String> classNames = new LinkedList<String>();
		Set<String> usedIDs = new HashSet<String>();

		int startRow = PaginationRenderer.getStartRow(section, user);
		int count = PaginationRenderer.getCount(section, user);

		while (iterator.hasNext()) {
			i++;

			if (count == Integer.MAX_VALUE || (i >= startRow && i < (startRow
					+ count))) {

				classNames.clear();

				TableRow row = iterator.next();

				if (tablemode) {
					if (zebraMode && (i + 1) % 2 != 0) {
						classNames.add("odd");
					}

					{// BEGIN: collapse tree mode code
						if (isTree) {
							classNames.add("treetr");
						}
					}// END: collapse tree mode code

					result.appendHtml(classNames.isEmpty()
							? "<tr"
							: "<tr class='" + Strings.concat(" ", classNames) + "'");

					{// BEGIN: collapse tree mode code
						if (isTree) {
							String valueID = valueToID(idVariable, row);
							boolean isNew = usedIDs.add(valueID);
							if (!isNew) {
//								result.append(" style='color:red'");
								valueID = UUID.randomUUID().toString();
							}
							result.append(" data-tt-id='sparql-id-").append(valueID).append("'");
							String parentID = valueToID(parentVariable, row);
							if (!Strings.isBlank(parentID) && !parentID.equals(valueID) && usedIDs.contains(parentID)) {
								// parentID.equals(valueID): hack for skipping
								// top level rows
								result.append(" data-tt-parent-id='sparql-id-")
										.append(parentID).append("'");
							}
						}
					}// END: collapse tree mode code

					result.append(">");
				}

				int index = 0;
				for (String var : variables) {

					{// BEGIN: collapse tree mode code
						// ignore first two columns if we are in tree mode
						if (isTree && index++ < 2) {
							continue;
						}
					}// END: collapse tree mode code

					Node node = row.getValue(var);
					String erg = renderNode(node, var, rawOutput, user, opts.getRdf2GoCore(),
							RenderMode.HTML);

					if (tablemode) {
						result.appendHtml("<td>");
						result.append(erg);
						result.appendHtml("</td>\n");
					}
					else {
						result.appendHtml("<li>");
						result.append(erg);
						result.appendHtml("</li>\n");
					}

				}
				if (tablemode) {
					result.appendHtml("</tr>");
				}
			}
			else {
				iterator.next();
			}

		}

		if (tablemode) {
			result.appendHtml("</table>");
			result.appendHtml("</div>");
		}
		else {
			result.appendHtml("</ul>");
		}
		return new SparqlRenderResult(result.toStringRaw(), i);
	}

	private ResultTableModel createMagicallySortedTable(ResultTableModel table) {
		// creating hierarchy order using PartialHierarchyTree
		PartialHierarchyTree<TableRow> tree = new
				PartialHierarchyTree<TableRow>(
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
		return new Comparator<TableRow>() {

			@Override
			public int compare(TableRow o1, TableRow o2) {
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
			}
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
				String temp = rendered;
				rendered = nodeRenderer.renderNode(rendered, var, user, core, mode);
				if (!temp.equals(rendered) && !nodeRenderer.allowFollowUpRenderer()) break;
			}
			// rendered = KnowWEUtils.maskJSPWikiMarkup(rendered);
		}
		return rendered;
	}

	private String modifyOrderByInSparqlString(Section<?> sec, UserContext user, String sparqlString) {
		StringBuilder sb = new StringBuilder(sparqlString);
		List<Pair<String, Boolean>> multipleSorting = PaginationRenderer.getMultiColumnSorting(sec, user);
		if (multipleSorting.isEmpty()) {
			return sb.toString();
		}
		String sparqlTempString = sparqlString.toLowerCase();
		int orderBy = sparqlTempString.lastIndexOf("order by");
		int limit = sparqlTempString.indexOf("limit", orderBy);
		int offset = sparqlTempString.indexOf("offset", orderBy);
		int nextStatement;
		if (limit > 0 && offset > 0) {
			nextStatement = (limit < offset) ? limit : offset;
		}
		else if (limit > 0 && offset < 0) {
			nextStatement = limit;
		}
		else if (limit < 0 && offset > 0) {
			nextStatement = offset;
		}
		else {
			nextStatement = -1;
		}

		StringBuilder sbOrder = new StringBuilder();

		HashMap<Boolean, String> sortingKeyWord = new HashMap<>(2);
		sortingKeyWord.put(true, "ASC");
		sortingKeyWord.put(false, "DESC");

		for (Pair pair : multipleSorting) {
			sbOrder.append(" " + sortingKeyWord.get(pair.getB()) + "(?" + pair.getA() + ")");
		}

		if (orderBy == -1) {
			if (nextStatement == -1) {
				sb.append(" ORDER BY" + sbOrder.toString());
			}
			else {
				sb.replace(nextStatement, nextStatement, " ORDER BY" + sbOrder.toString());
			}
		}
		else {
			if (nextStatement != -1) {
				sb.replace(orderBy, nextStatement, " ORDER BY" + sbOrder.toString());
			}
			else {
				sb.replace(orderBy, sb.length(), " ORDER BY" + sbOrder.toString());
			}
		}

		return sb.toString();
	}

	private void renderTableSizeSelector(RenderOptions options, int max, RenderResult result) {

		String id = options.getId();
		result.appendHtml("<div class='toolBar'>");

		String[] sizeArray = getReasonableSizeChoices(max);

		result.appendHtml("<span class=fillText>Show </span>"
				+ "<select id='showLines" + id + "'"
				+ " onchange=\"KNOWWE.plugin.sparql.refresh('"
				+ id + "');\">");
		boolean selected = false;
		String selectedByUser = options.getNavigationLimit() + "";
		// if no limit was selected or
		for (String size : sizeArray) {
			if ((size.equals(selectedByUser) && !options.isShowAll())
					|| (size.equals("All") && !selected)) {
				selected = true;
				result.appendHtml("<option selected='selected' value='" + size + "'>" + size + "</option>");
			}
			else {
				result.appendHtml("<option value='" + size + "'>" + size
						+ "</option>");
			}
		}
		result.appendHtml("</select><span class=fillText> lines of </span>" + max);

		result.appendHtml("<div class='toolSeparator'></div>");
		result.appendHtml("</div>");

	}

	private void renderNavigation(RenderOptions options, int max, RenderResult result) {
		String id = options.getId();
		int from = options.getNavigationOffset();
		int selectedSizeInt;
		if (options.isShowAll()) {
			selectedSizeInt = max;
		}
		else {
			selectedSizeInt = options.getNavigationLimit();
		}
		result.appendHtml("<div class='toolBar avoidMenu'>");
		renderToolbarButton(
				"begin.png", "KNOWWE.plugin.sparql.begin('"
						+ id + "')",
				(from > 1), result
		);
		renderToolbarButton(
				"back.png", "KNOWWE.plugin.sparql.back('"
						+ id + "')",
				(from > 1), result
		);
		result.appendHtml("<span class=fillText> Lines </span>");
		result.appendHtml("<input size=3 id='fromLine" + id + "' type=\"field\" onchange=\"KNOWWE.plugin.sparql.refresh('"
				+ id + "');\" value='"
				+ from + "'>");
		result.appendHtml("<span class=fillText> to </span>" + Math.min(from + selectedSizeInt - 1, max));
		renderToolbarButton(
				"forward.png", "KNOWWE.plugin.sparql.forward('"
						+ id + "')",
				(!options.isShowAll() && (from + selectedSizeInt - 1 < max)), result
		);
		renderToolbarButton(
				"end.png", "KNOWWE.plugin.sparql.end('"
						+ id + "','" + max + "')",
				(!options.isShowAll() && (from + selectedSizeInt - 1 < max)), result
		);
		result.appendHtml("</div>");

	}

	private void renderToolbarButton(String icon, String action, boolean enabled, RenderResult builder) {
		int index = icon.lastIndexOf('.');
		String suffix = icon.substring(index);
		icon = icon.substring(0, index);
		if (enabled) {
			builder.appendHtml("<a onclick=\"");
			builder.appendHtml(action);
			builder.appendHtml(";\">");
		}
		builder.appendHtml("<span class='toolButton ");
		builder.appendHtml(enabled ? "enabled" : "disabled");
		builder.appendHtml("'>");
		builder.appendHtml("<img src='KnowWEExtension/navigation_icons/");
		builder.appendHtml(icon);
		if (!enabled) builder.appendHtml("_deactivated");
		builder.appendHtml(suffix).appendHtml("' /></span>");
		if (enabled) {
			builder.appendHtml("</a>");
		}
	}

	private String[] getReasonableSizeChoices(int max) {
		List<String> sizes = new LinkedList<String>();
		String[] sizeArray = new String[] {
				"10", "20", "50", "100", "1000" };
		for (String size : sizeArray) {
			if (Integer.parseInt(size) < max) {
				sizes.add(size);
			}
		}
		sizes.add("All");

		return sizes.toArray(new String[sizes.size()]);

	}

}
