package de.knowwe.rdf2go.sparql.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.denkbares.semanticcore.utils.DefaultValueComparator;
import com.denkbares.semanticcore.utils.ValueAsDateComparator;
import com.denkbares.semanticcore.utils.ValueComparator;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.util.Color;

public class RenderOptions {

	private final String id;
	private Rdf2GoCore core;

	private boolean zebraMode = true;
	private boolean rawOutput = false;
	private boolean sorting = false;
	private boolean filtering = false;
	private boolean navigation = false;
	private boolean border = true;
	private boolean showAll = false;
	private boolean tree = false;
	private boolean allowJSPWikiMarkup = true;
	private int navigationOffset = 1;
	private int navigationLimit = 50;
	private long timeout = 10000;
	private final Set<String> columnsWithDisabledFilter = new HashSet<>();
	private final Map<String, ColumnSortingType> columnComparators = new HashMap<>();
	private Color color = Color.NONE;
	private List<StyleOption> columnStyles = new ArrayList<>();
	private List<StyleOption> tableStyles = new ArrayList<>();
	private List<StyleOption> columnWidths = new ArrayList<>();
	private RenderMode renderMode = RenderMode.HTML;
	private Map<String, String> sortingOrder = new LinkedHashMap<>();

	public enum ColumnSortingType {
		/**
		 * Try to sort column as a date
		 */
		date(new ValueAsDateComparator()),
		/**
		 * Sort column as a normal value (default)
		 */
		value(new DefaultValueComparator());

		private final ValueComparator valueComparator;

		ColumnSortingType(ValueComparator valueComparator) {
			this.valueComparator = valueComparator;
		}

		public ValueComparator getComparator() {
			return this.valueComparator;
		}
	}

	public RenderOptions(String id) {
		this.id = id;
	}

	public void disableFilterForColumn(String columnName) {
		columnsWithDisabledFilter.add(columnName);
	}

	public Set<String> getColumnsWithDisabledFiltering() {
		return Collections.unmodifiableSet(columnsWithDisabledFilter);
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getSortingMap() {
		return sortingOrder;
	}

	public void setSortingMap(Map<String, String> json) {
		this.sortingOrder = json;
	}

	public boolean isNavigation() {
		return navigation;
	}

	public void setNavigation(boolean navigation) {
		this.navigation = navigation;
	}

	public boolean isBorder() {
		return border;
	}

	public void setBorder(boolean border) {
		this.border = border;
	}

	public boolean isZebraMode() {
		return zebraMode;
	}

	public void setZebraMode(boolean zebraMode) {
		this.zebraMode = zebraMode;
	}

	public boolean isRawOutput() {
		return rawOutput;
	}

	public void setRawOutput(boolean rawOutput) {
		this.rawOutput = rawOutput;
	}

	public boolean isSorting() {
		return sorting;
	}

	public boolean isFiltering() {
		return filtering;
	}

	public void setSorting(boolean sorting) {
		this.sorting = sorting;
	}

	public void setFiltering(boolean filtering) {
		this.filtering = filtering;
	}

	public void setRdf2GoCore(Rdf2GoCore core) {
		this.core = core;
	}

	public Rdf2GoCore getRdf2GoCore() {
		return this.core;
	}

	public int getNavigationOffset() {
		return navigationOffset;
	}

	public List<StyleOption> getColumnStyles() {
		return columnStyles;
	}

	public List<StyleOption> getTableStyles() {
		return tableStyles;
	}

	public boolean isAllowJSPWikiMarkup() {
		return allowJSPWikiMarkup;
	}

	public void setNavigationOffset(String navigationOffset) {
		this.navigationOffset = Integer.parseInt(navigationOffset);
	}

	public List<StyleOption> getColumnWidths() {
		return columnWidths;
	}

	public int getNavigationLimit() {
		return navigationLimit;
	}

	public void setNavigationLimit(String navigationLimit) {
		this.navigationLimit = Integer.parseInt(navigationLimit);
	}

	public boolean isShowAll() {
		return showAll;
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

	public boolean isTree() {
		return tree;
	}

	public void setTree(boolean tree) {
		this.tree = tree;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setColumnSorting(String column, ColumnSortingType comparatorType) {
		columnComparators.put(column, comparatorType);
	}

	public ValueComparator getColumnSorting(String column) {
		return columnComparators.getOrDefault(column, ColumnSortingType.value).getComparator();
	}

	public void setColumnStyles(@NotNull List<StyleOption> styles) {
		this.columnStyles = styles;
	}

	public void setTableStyles(@NotNull List<StyleOption> styles) {
		this.tableStyles = styles;
	}

	public void setColumnWidth(@NotNull List<StyleOption> columnWidth) {
		this.columnWidths = columnWidth;
	}

	public void setAllowJSPWikiMarkup(boolean allowJSPWikiMarkup) {
		this.allowJSPWikiMarkup = allowJSPWikiMarkup;
	}

	public void setRenderMode(RenderMode renderMode) {
		this.renderMode = renderMode;
	}

	public RenderMode getRenderMode() {
		return renderMode;
	}

	public enum RenderMode {
		/**
		 * This is the default. Support HTML content in the sparql result.
		 */
		HTML,
		/**
		 * Display sparql result only using plain text
		 */
		PlainText,
		/**
		 * Try to add tool menus in the sparql result, where appropriate
		 */
		ToolMenu
	}

	public static class StyleOption {
		private String columnName;
		private String styleKey;
		private String styleValue;

		public StyleOption(String columnName, String styleKey, String styleValue) {
			this.columnName = columnName;
			this.styleKey = styleKey;
			this.styleValue = styleValue;
		}

		public String getStyleValue() {
			return styleValue;
		}

		public void setStyleValue(String styleValue) {
			this.styleValue = styleValue;
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public String getStyleKey() {
			return styleKey;
		}

		public void setStyleKey(String styleKey) {
			this.styleKey = styleKey;
		}
	}
}
