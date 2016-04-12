package de.knowwe.jspwiki;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.d3web.plugin.Extension;
import de.d3web.plugin.Plugin;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.ScopeUtils;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.plugin.Plugins;

public class JSPWikiMarkupUtils {

	/**
	 * Returns toplevelsections for given article.<br />
	 * toplevelsections are all sections with root as their father in the
	 * treestructure.
	 */
	public static List<Section<?>> getTopLevelSections(
			Article article) {
		Section<?> s = article.getRootSection();
		int i = 0;
		List<Section<?>> topLevelSections = new ArrayList<Section<?>>();
		for (Section<?> tlt : s.getChildren().get(0).getChildren()) {
			if (countHeaderMarks(tlt.getText()) >= i) {
				topLevelSections.add(tlt);
				i = countHeaderMarks(tlt.getText());
			}
		}
		return topLevelSections;
	}

	/**
	 * Returns toplevelsections for given sectionslist.<br />
	 * toplevelsections are all sections with no father in the treestructure.
	 */
	public static List<Section<? extends Type>> getTopLevelSections(
			List<Section<? extends Type>> list) {
		int i = 0;
		List<Section<? extends Type>> topLevelSections = new ArrayList<Section<? extends Type>>();
		for (Section<? extends Type> tlt : list) {
			if (countHeaderMarks(tlt.getText()) >= i) {
				topLevelSections.add(tlt);
				i = countHeaderMarks(tlt.getText());
			}
		}
		return topLevelSections;
	}

	/**
	 * Returns the content sections that are below the specified SectionHeader.
	 * These are all sections that a human naturally associated to be part of
	 * the chapter that is spanned by the header title. This also includes any
	 * sub-headers (and their content sections) of the specified header section.
	 * The header section itself is not included.
	 * 
	 * @param header the header section to get the content for
	 */
	public static List<Section<? extends Type>> getContent(Section<HeaderType> header) {
		return getContent(header, false);
	}

	/**
	 * Returns the content sections that are below the specified SectionHeader.
	 * These are all sections that a human naturally associated to be part of
	 * the chapter that is spanned by the header title. This also includes any
	 * sub-headers (and their content sections) of the specified header section.
	 * The parameter "includeHeader" specified if the header section itself is
	 * included as the first element of the returned list.
	 * 
	 * @param header the header section to get the content for
	 * @param includeHeader if the header section shall be included or not
	 */
	public static List<Section<? extends Type>> getContent(Section<HeaderType> header, boolean includeHeader) {
		int level = countHeaderMarks(header.getText());
		List<Section<?>> list = header.getArticle().getRootSection().getChildren();
		int i = list.indexOf(header) + 1;

		List<Section<? extends Type>> contentSections = new ArrayList<Section<? extends Type>>();
		if (includeHeader) contentSections.add(header);
		for (; i < list.size(); i++) {
			if (countHeaderMarks(list.get(i).getText()) < level) {
				contentSections.add(list.get(i));
			}
			else {
				break;
			}
		}
		return contentSections;
	}

	public static List<Type> getInstalledJSPWikiRootMarkups() {
		PluginManager manager = PluginManager.getInstance();
		Extension[] extensions = manager.getExtensions(
				Plugins.EXTENDED_PLUGIN_ID, Plugins.EXTENDED_POINT_Type);
		List<Extension> matches = ScopeUtils.getMatchingExtensions(
				extensions, new Type[] { RootType.getInstance() });

		Plugin plugin = manager.getPlugin("KnowWE-Plugin-JSPWiki-MarkupSet");
		List<Type> result = new LinkedList<Type>();
		for (Extension extension : matches) {
			if (plugin.equals(extension.getPlugin())) {
				Type type = (Type) extension.getNewInstance();
				result.add(type);
			}
		}
		return result;
	}

	/**
	 * counts ! at the beginning of s.
	 */
	private static int countHeaderMarks(String s) {
		int i = 0;
		if (s.isEmpty() || s.length() < 3) {
			return i;
		}
		if (s.charAt(0) == '!') {
			i++;
			if (s.charAt(1) == '!') {
				i++;
				if (s.charAt(2) == '!') {
					i++;
				}
			}
		}
		return i;
	}
}
