package de.knowwe.rdf2go.sparql;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.node.Node;

import de.d3web.collections.PartialHierarchy;
import de.d3web.collections.PartialHierarchyTree;
import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.strings.Strings;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.sparql.utils.SparqlRenderResult;
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
		nodeRenderers = getNodeRenderer();
	}

	public SparqlResultNodeRenderer[] getNodeRenderer() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				Rdf2GoCore.PLUGIN_ID, POINT_ID);
		SparqlResultNodeRenderer[] renderers = new SparqlResultNodeRenderer[extensions.length];
		for (int i = 0; i < extensions.length; i++) {
			renderers[i] = ((SparqlResultNodeRenderer) extensions[i].getSingleton());
		}
		return renderers;
	}

	public SparqlRenderResult renderQueryResult(QueryResultTable qrt, UserContext user) {
		// TODO
		// is this a good idea?
		RenderOptions opts = new RenderOptions("defaultID");
		opts.setRdf2GoCore(Rdf2GoCore.getInstance());
		return renderQueryResult(qrt, opts, user);
	}

	/**
	 * 
	 * @created 06.12.2010
	 * @param qrt
	 * @param opts TODO
	 * @return html table with all results of qrt and size of qrt
	 */
	public SparqlRenderResult renderQueryResult(QueryResultTable qrt, RenderOptions opts, UserContext user) {
		boolean tablemode = false;
		boolean empty = true;
		boolean zebraMode = opts.isZebraMode();
		boolean rawOutput = opts.isRawOutput();
		boolean isTree = opts.isTree();
		int i = 0;
		String tableID = UUID.randomUUID().toString();

		RenderResult result = new RenderResult(user);



		List<String> variables = qrt.getVariables();
		tablemode = variables.size() > 1;

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

		if (tablemode) {
			result.appendHtml("<table id='").append(tableID).appendHtml("' class='sparqltable'>");
			result.appendHtml(!zebraMode ? "<tr>" : "<tr class='odd'>");
			int index = 0;
			for (String var : variables) {

				if (isTree && index++ < 2) {
					continue;
				}

				result.appendHtml("<td><b>");
				result.appendHtml("<a href='#/' onclick=\"KNOWWE.plugin.semantic.actions.sortResultsBy('"
						+ var + "', '"
						+ opts.getId() + "');\">");
				result.append(var);
				result.appendHtml("</a>");
				if (hasSorting(var, opts.getSortingMap())) {
					String symbol = getSortingSymbol(var, opts.getSortingMap());
					result.appendHtml("<img src='KnowWEExtension/images/" + symbol
							+ "' alt='Sort by '"
							+ var + "border='0' /><b/></td>");
				}

			}
			result.appendHtml("</tr>");
		}
		else {
			result.appendHtml("<ul style='white-space: normal'>");
		}
		ResultTableModel table = new ResultTableModel(qrt);
		if (isTree) {
			table = createMagicallySortedTable(table);
		}

		Iterator<TableRow> iterator = table.iterator();
		List<String> classNames = new LinkedList<String>();
		while (iterator.hasNext()) {
			i++;
			if ((opts.isNavigation() && i >= opts.getNavigationOffset() && i < (opts.getNavigationOffset()
					+ opts.getNavigationLimit()))
					|| (opts.isNavigation() && opts.isShowAll()) || !opts.isNavigation()) {

				empty = false;
				classNames.clear();

				TableRow row = iterator.next();


				if (tablemode) {
					if (zebraMode && (i + 1) % 2 != 0) {
						classNames.add("odd");
					}
					String valueID = valueToID(idVariable, row);
					if (isTree) {
						String parentID = valueToID(parentVariable, row);
						if (parentID != null
								&& !parentID.equals(valueID)) { // hack for top
																// level rows
							classNames.add("child-of-sparql-id-" + parentID);
						}
					}
					result.appendHtml(classNames.isEmpty()
							? "<tr"
							: "<tr class='" + Strings.concat(" ", classNames) + "'");
					if (isTree) {
						String id = valueID;
						result.append(" id='sparql-id-").append(id).append("'");
					}
					result.append(">");
				}

				int index = 0;
				for (String var : variables) {
					// ignore first two columns if we are in tree mode
					if (isTree && index++ < 2) {
						continue;
					}
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

		if (empty) {
			result.append(Messages.getMessageBundle().getString(
					"KnowWE.owl.query.no_result"));
		}
		if (tablemode) {
			if (isTree) {
				result.appendHtml("<script type='text/javascript'>jq$('#")
						.append(tableID)
						.appendHtml("').treeTable({clickableNodeNames: true});</script>");
			}
			result.appendHtml("</table>");
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
			SimpleTableRow artificialTopLevelRow = new SimpleTableRow();
			artificialTopLevelRow.addValue(table.getVariables().get(0), topLevelConcept);
			artificialTopLevelRow.addValue(table.getVariables().get(1), topLevelConcept);
			artificialTopLevelRow.addValue(table.getVariables().get(2), topLevelConcept);
			result.addTableRow(artificialTopLevelRow);

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
			TableRow parentRow = data.findRowFor(ascendorParent);
			if (parentRow == null) return false;
			return checkSuccessorshipRecursively(parentRow, ancestor);

		}

	}

	private String valueToID(String variable, TableRow row) {
		Node value = row.getValue(variable);
		if (value == null) return null;
		return Integer.toString(value.toString().replaceAll("[\\s\"]+", "").hashCode());
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

	private String getSortingSymbol(String value, Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		if (map.containsKey(value)) {
			sb.append("arrow");
			sb.append("_");
			if (map.get(value).equals("ASC")) {
				sb.append("down");
			}
			else {
				sb.append("up");
			}
		}
		sb.append(".png");
		return sb.toString().toLowerCase();
	}

	private boolean hasSorting(String value, Map<String, String> map) {
		if (map.containsKey(value)) {
			return true;
		}
		return false;
	}
}
