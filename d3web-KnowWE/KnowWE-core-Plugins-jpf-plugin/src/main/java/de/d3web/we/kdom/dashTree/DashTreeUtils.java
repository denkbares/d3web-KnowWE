package de.d3web.we.kdom.dashTree;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermReference;

public class DashTreeUtils {

	public static Section<? extends DashTreeElement> getFatherDashTreeElement(Section<?> s) {
		Section<? extends DashSubtree> dashSubtree = getFatherDashSubtree(s);
		if (dashSubtree != null) {
			return dashSubtree.findSuccessor(DashTreeElement.class);
		}
		return null;
	}

	public static Section<? extends DashTreeElement> getAncestorDashTreeElement(Section<?> s, int dashLevel) {
		Section<? extends DashSubtree> dashSubtree = getAncestorDashSubtree(s, dashLevel);
		if (dashSubtree != null) {
			return dashSubtree.findChildOfType(DashTreeElement.class);
		}
		return null;
	}

	public static List<Section<? extends DashTreeElement>> getAncestorDashTreeElements(Section<?> s) {
		List<Section<? extends DashTreeElement>> ancestors = new ArrayList<Section<? extends DashTreeElement>>();
		List<Section<?>> ancestorSubTrees = new ArrayList<Section<?>>();
		Section<?> ancestorSubtree = s.findAncestorOfType(DashSubtree.class).getFather();
		while (ancestorSubtree != null && ancestorSubtree.get() instanceof DashSubtree) {
			ancestorSubTrees.add(ancestorSubtree);
			ancestorSubtree = ancestorSubtree.getFather();
		}
		for (Section<?> subTree : ancestorSubTrees) {
			ancestors.add(subTree.findChildOfType(DashTreeElement.class));
		}
		return ancestors;
	}

	/**
	 * Delegates the getDashTreeFather-operation to DashTreeElement
	 * 
	 * @param s
	 * @return
	 */
	public static Section<? extends DashTreeElementContent> getFatherDashTreeElementContent(Section<?> s) {
		Section<? extends DashTreeElement> dashTreeFatherElement = getFatherDashTreeElement(s);
		if (dashTreeFatherElement != null) {
			return dashTreeFatherElement.findChildOfType(DashTreeElementContent.class);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Section<? extends DashSubtree> getFatherDashSubtree(Section<?> s) {
		Section<? extends DashSubtree> dashSubtree = s.findAncestorOfType(DashSubtree.class);
		if (dashSubtree != null) {
			if (dashSubtree.getFather().get() instanceof DashSubtree) {
				return (Section<? extends DashSubtree>) dashSubtree.getFather();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Section<DashSubtree> getAncestorDashSubtree(Section<?> s, int dashLevel) {
		if (dashLevel < 0) return null;
		Section<?> dashSubtree = s.findAncestorOfType(DashSubtree.class);
		if (dashSubtree != null) {
			int fLevel = getDashLevel(dashSubtree);
			if (fLevel < dashLevel) {
				return null;
			}
			for (int i = fLevel; i > dashLevel; i--) {
				dashSubtree = dashSubtree.getFather();
			}
			return (Section<DashSubtree>) dashSubtree;
		}
		return null;
	}

	/**
	 * Delivers the (dash-)level of the element by counting leading '-'
	 * 
	 * @param s
	 * @return
	 */
	public static int getDashLevel(Section<?> s) {

		if (s == null) return -1;

		String text = s.getOriginalText().trim();

		int index = 0;
		while (index < text.length() && text.charAt(index) == '-') {
			index++;
		}
		return index;
	}

	public static int getPositionInFatherDashSubtree(Section<?> s) {

		Section<DashSubtree> subTreeRoot = s.findAncestorOfType(DashSubtree.class);

		if (subTreeRoot != null) {

			Section<?> fatherSubTree = subTreeRoot.getFather();
			if (fatherSubTree.get() instanceof DashSubtree
					|| fatherSubTree.get() instanceof DashTree) {
				int pos = 0;
				for (Section<?> sec : fatherSubTree.getChildren()) {
					if (sec.get() instanceof DashSubtree) {
						if (sec == subTreeRoot) {
							return pos;
						}
						pos++;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Checks in the Subtree with the given dash level, if there are changed
	 * Sections. Ignores TermReferences!
	 */
	public static boolean isChangeInAncestorSubtree(KnowWEArticle article, Section<?> s, int dashLevel) {

		Section<?> subtreeAncestor = DashTreeUtils.getAncestorDashSubtree(s, dashLevel);

		if (subtreeAncestor != null) {
			List<Class<? extends KnowWEObjectType>> filteredTypes =
					new ArrayList<Class<? extends KnowWEObjectType>>(1);
			filteredTypes.add(TermReference.class);

			return subtreeAncestor.isOrHasChangedSuccessor(article.getTitle(), filteredTypes);

		}
		return false;
	}

}
