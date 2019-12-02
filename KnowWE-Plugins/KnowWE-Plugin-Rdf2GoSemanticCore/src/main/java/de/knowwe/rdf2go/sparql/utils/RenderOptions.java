package de.knowwe.rdf2go.sparql.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.Style;

import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.util.Color;

public class RenderOptions {

	boolean zebraMode;
	boolean rawOutput;
	boolean sorting;
	boolean navigation;
	boolean border;
	Map<String, String> sortingOrder;
	String id;
	private Rdf2GoCore core;
	int navigationOffset = 1;
	int navigationLimit = 50;
	boolean showAll;
	private boolean tree = false;
	private long timeout = 10000;
	private Color color;
	private List<StyleOption> columnStyles;
	private List<StyleOption> tableStyles;
	private boolean allowJSPWikiMarkup;
	private List<StyleOption> columnWidths;

	public RenderOptions(String id) {
		this.zebraMode = true;
		this.rawOutput = false;
		this.sorting = false;
		this.navigation = false;
		this.border = true;
		sortingOrder = new LinkedHashMap<>();
		this.id = id;
		showAll = false;
 		color = Color.NONE;
 		this.columnStyles = null;
 		this.tableStyles = null;
 		this.allowJSPWikiMarkup = true;
 		this.columnWidths = null;
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

	public void setSorting(boolean sorting) {
		this.sorting = sorting;
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

	public void setColumnWidth(List<StyleOption> columnWidth) {
		this.columnWidths = columnWidth;
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

	public void setColumnStyles(List<StyleOption> styles) {
		this.columnStyles = styles;
	}

	public void setTableStyles(List<StyleOption> tableStyles) {
		this.tableStyles = tableStyles;
	}

	public void setAllowJSPWikiMarkup(boolean allowJSPWikiMarkup) {
		this.allowJSPWikiMarkup = allowJSPWikiMarkup;
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
