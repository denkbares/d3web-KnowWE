package de.knowwe.jspwiki;

import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.jspwiki.types.HeaderType;

public class JSPMarkupUtils {

	/**
	 * Returns toplevelsections for given article.<br />
	 * toplevelsections are all sections with root as their father in the
	 * treestructure.
	 */
	public static List<Section<? extends Type>> getTopLevelSections(
			Article article) {
		Section<RootType> s = article.getRootSection();
		int i = 0;
		List<Section<? extends Type>> topLevelSections = new ArrayList<Section<? extends Type>>();
		for (Section<? extends Type> tlt : s.getChildren().get(0).getChildren()) {
			if (count(tlt.getText()) >= i) {
				topLevelSections.add(tlt);
				i = count(tlt.getText());
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
			if (count(tlt.getText()) >= i) {
				topLevelSections.add(tlt);
				i = count(tlt.getText());
			}
		}
		return topLevelSections;
	}

	/**
	 * Returns Content for given SectionHeader.
	 */
	public static List<Section<? extends Type>> getContent(
			Section<HeaderType> sh) {
		int level = count(sh.getText());
		Section<RootType> s = Sections.findAncestorOfType(sh, RootType.class);
		List<Section<? extends Type>> list = s.getChildren();
		int i = list.indexOf(sh) + 1;
		List<Section<? extends Type>> contentSections = new ArrayList<Section<? extends Type>>();
		for (; i < list.size(); i++) {
			if (count(list.get(i).getText()) < level) {
				contentSections.add(list.get(i));
			} else {
				break;
			}
		}
		return contentSections;
	}

	/**
	 * counts ! at the beginning of s.
	 */
	public static int count(String s) {
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
