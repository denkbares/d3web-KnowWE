/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.kdom.dashtree;

import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class DashTreeUtils {

	private static DashTreeUtils instance = null;

	public static DashTreeUtils getInstance() {
		if (instance == null) {
			instance = new DashTreeUtils();
		}
		return instance;
	}

	protected DashTreeUtils() {
	}

	/**
	 * Returns a list of all successor DashTree elements of the given element.
	 *
	 * @param section a DashTreeElement or a sub-KDOM-node of a DashTreeElement
	 * @return
	 */
	public static List<Section<DashTreeElement>> findSuccessorDashtreeElements(Section<?> section) {
		return findSuccessorDashtreeElements(section, -1);
	}

	public static List<Section<DashTreeElement>> findChildrenDashtreeElements(Section<?> section) {
		return findSuccessorDashtreeElements(section, 2);
	}

	/**
	 * Returns a list of all successor DashTree elements of the given element to the given depth
	 *
	 * @param section a DashTreeElement or a sub-KDOM-node of a DashTreeElement
	 * @return
	 */
	private static List<Section<DashTreeElement>> findSuccessorDashtreeElements(Section<?> section, int depth) {
		Section<DashTreeElement> element;
		if (section.get() instanceof DashTreeElement) {
			element = Sections.cast(section, DashTreeElement.class);
		}
		else {
			element = Sections.ancestor(section, DashTreeElement.class);
			if (element == null) {
				throw new IllegalArgumentException("Given section neither is nor has an ancestor of type "
						+ DashTreeElement.class.getSimpleName());
			}
		}
		List<Section<DashTreeElement>> found = new ArrayList<Section<DashTreeElement>>();
		if (depth >= 0) {
			Sections.successors(element.getParent(), DashTreeElement.class, depth, found);
		}
		else {
			Sections.successors(element.getParent(), DashTreeElement.class, found);
		}
		found.remove(element);
		return found;
	}

	public static Section<? extends DashTreeElement> getParentDashTreeElement(Section<?> s) {
		Section<? extends DashSubtree> dashSubtree = getParentDashSubtree(s);
		if (dashSubtree != null) {
			return Sections.successor(dashSubtree, DashTreeElement.class);
		}
		return null;
	}

	public static Section<? extends DashTreeElement> getAncestorDashTreeElement(Section<?> s, int dashLevel) {
		Section<? extends DashSubtree> dashSubtree = getAncestorDashSubtree(s, dashLevel);
		if (dashSubtree != null) {
			return Sections.child(dashSubtree, DashTreeElement.class);
		}
		return null;
	}

	public static List<Section<? extends DashTreeElement>> getAncestorDashTreeElements(Section<?> s) {
		List<Section<? extends DashTreeElement>> ancestors = new ArrayList<Section<? extends DashTreeElement>>();
		List<Section<?>> ancestorSubTrees = new ArrayList<Section<?>>();
		Section<?> ancestorSubtree = Sections.ancestor(s, DashSubtree.class).getParent();
		while (ancestorSubtree != null && ancestorSubtree.get() instanceof DashSubtree) {
			ancestorSubTrees.add(ancestorSubtree);
			ancestorSubtree = ancestorSubtree.getParent();
		}
		for (Section<?> subTree : ancestorSubTrees) {
			ancestors.add(Sections.child(subTree, DashTreeElement.class));
		}
		return ancestors;
	}

	public static Section<? extends DashTreeElementContent> getParentDashTreeElementContent(Section<?> s) {
		Section<? extends DashTreeElement> dashTreeFatherElement = getParentDashTreeElement(s);
		if (dashTreeFatherElement != null) {
			return Sections.child(dashTreeFatherElement,
					DashTreeElementContent.class);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Section<? extends DashSubtree> getParentDashSubtree(Section<?> s) {
		Section<? extends DashSubtree> dashSubtree = Sections.ancestor(s,
				DashSubtree.class);
		if (dashSubtree != null) {
			if (dashSubtree.getParent().get() instanceof DashSubtree) {
				return (Section<? extends DashSubtree>) dashSubtree.getParent();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Section<DashSubtree> getAncestorDashSubtree(Section<?> s, int dashLevel) {
		if (dashLevel < 0) return null;
		Section<?> dashSubtree = Sections.ancestor(s, DashSubtree.class);
		if (dashSubtree != null) {
			int fLevel = getDashLevel(dashSubtree);
			if (fLevel < dashLevel) {
				return null;
			}
			for (int i = fLevel; i > dashLevel; i--) {
				dashSubtree = dashSubtree.getParent();
			}
			return (Section<DashSubtree>) dashSubtree;
		}
		return null;
	}

	/**
	 * Creates a String of the number of dashes specified.
	 *
	 * @param dashLevel
	 * @return
	 */
	public static String createDashes(int dashLevel) {
		String result = "";
		for(int i = 0; i < dashLevel; i++) {
			result += "-";
		}
		return  result;
	}

	/**
	 * Delivers the (dash-)level of the element by counting leading '-'
	 *
	 * @param s Only works for DashSubtree oder DashTreeElement sections
	 * @return
	 */
	public static int getDashLevel(Section<?> s) {

		if (s == null) return -1;

		char key = '-';

		if (s.get() instanceof DashSubtree) {
			key = ((DashSubtree) s.get()).getKey();
		}
		else if (s.get() instanceof DashTreeElement) {
			Section<DashSubtree> subtree = Sections.ancestor(s, DashSubtree.class);
			key = subtree.get().getKey();
		}
		else {
			throw new IllegalArgumentException("Only DashSubtree and DashTreeElement are allowed");
		}

		String text = s.getText().trim();

		int index = 0;
		while (index < text.length() && text.charAt(index) == key) {
			index++;
		}
		return index;
	}

	public static int getPositionInFatherDashSubtree(Section<?> s) {

		Section<DashSubtree> subTreeRoot = Sections.ancestor(s,
				DashSubtree.class);

		if (subTreeRoot != null) {

			Section<?> fatherSubTree = subTreeRoot.getParent();
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

}
