package de.knowwe.core.preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Scope;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * Default implementation of a {@link PreviewRenderer} that can be applied to any ancestor-section
 * of a default markup type (as scope of the preview rendering extension), as well as to the whole
 * default markup type. if it is applied to the whole default markup type. the default markup frame
 * is not rendered, but the whole content and all annotations sections.
 * <p/>
 * If it is only scoped to certain sub-sections only these sections are rendered. In this case you
 * can additionally specify a set of (relative) scopes that will be additionally rendered.
 * <p/>
 * Please not that this preview renderer is for selective rendering only. If you wand to render a
 * complete default markup section without its frame, you may use the {@link DefaultPreviewRenderer}
 * instead.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 26.08.2013
 */
public class DefaultMarkupPreviewRenderer extends AbstractPreviewRenderer {

	private static final DefaultMarkupRenderer DEFAULT_MARKUP_RENDERER = new DefaultMarkupRenderer();

	public enum Select {
		/**
		 * Shows all successors matching the scope
		 */
		all,

		/**
		 * Shows successors that contains relevant items
		 */
		relevant,

		/**
		 * Shows successors that contains relevant items, but if there are no successors with
		 * relevant items, some example successors are shown (e.g. 5) and then abbreviated by "..."
		 */
		relevantOrSome,

		/**
		 * /** Shows successors that contains relevant items, but all if there are no successors
		 * with relevant items
		 */
		relevantOrAll,

		/**
		 * Includes the sections matching the scope if the sibling after (!) the matched section has
		 * been included by a scope selection that is prior to this scope selection
		 */
		beforeSelected,

		/**
		 * Includes the sections matching the scope if the sibling before (!) the matched section
		 * has been included by a scope selection that is prior to this scope selection
		 */
		afterSelected
	}

	public final Map<Scope, Select> previewItems = new LinkedHashMap<>();

	public DefaultMarkupPreviewRenderer() {
	}

	/**
	 * Adds a new scope of sub-items to be shown. When rendering the preview all sub-sections will
	 * be visible that matches the scopes specified through this method. If forceVisible is true.
	 * all the matching sub-section are shown. If not, only the matching sub-sections are shown that
	 * contains relevant items.
	 *
	 * @param scope the sub-scope to be shown
	 * @param selector if the are always shown or only if the contain relevant items
	 * @created 26.08.2013
	 */
	public void addPreviewItem(Scope scope, Select selector) {
		previewItems.put(scope, selector);
	}

	/**
	 * Removes all preview items that previously have been added to this preview renderer. You may
	 * use this method to reinitialize the renderer.
	 */
	public void clear() {
		previewItems.clear();
	}

	/**
	 * Adds the text of plain text section ins between annotation sections, containing the line
	 * breaks and white-spaces between the single annotations.
	 *
	 * @created 27.08.2013
	 */
	public void addTextBeforeAnnotations() {
		addPreviewItem(Scope.getScope("DefaultMarkupType/PlainText"), Select.beforeSelected);
	}

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		List<Section<?>> previews = new LinkedList<>();
		// collect all relevant scoped sections plus the matched section
		for (Entry<Scope, Select> entry : previewItems.entrySet()) {
			Scope scope = entry.getKey();
			Select preview = entry.getValue();
			List<Section<?>> matches = scope.getMatchingSuccessors(section);
			switch (preview) {

				case all:
					// add all matches if requested
					previews.addAll(matches);
					break;

				case relevant:
				case relevantOrSome:
				case relevantOrAll:
					// add only relevant matches if requested
					boolean added = false;
					for (Section<?> match : matches) {
						if (hasSuccessor(match, relevantSubSections)) {
							previews.add(match);
							added = true;
						}
					}
					// add all if no one has shown to be relevant
					// if this has been requested
					if (preview != Select.relevant && !added) {
						if (preview == Select.relevantOrSome && matches.size() > 8) {
							matches = matches.subList(0, 5);
						}
						previews.addAll(matches);
					}
					break;

				case afterSelected:
					for (Section<?> match : matches) {
						if (containsSectionBefore(previews, match)) {
							previews.add(match);
						}
					}
					break;

				case beforeSelected:
					for (Section<?> match : matches) {
						if (containsSectionAfter(previews, match)) {
							previews.add(match);
						}
//						List<Section<?>> siblings = match.getParent().getChildren();
//						int index = siblings.indexOf(match);
//						if (index == -1) continue;
//						// move index to the sibling that has to be included
//						int selectedIndex = Select.afterSelected.equals(preview)
//								? index - 1 : index + 1;
//						if (selectedIndex < 0) continue;
//						if (selectedIndex >= siblings.size()) continue;
//						Section<?> sibling = siblings.get(selectedIndex);
//						// and add this match if the desired sibling is available
//						if (previews.contains(sibling)) previews.add(match);
					}
					break;
			}
		}
		renderSections(previews, user, result);
	}

	/**
	 * Returns if the specified collection of Sections contains any of the direct sections after the
	 * specified section. The sections after are all sections, that does not intersect with the
	 * specified section, but have their start index equal to the end index of the specified start
	 * section.
	 *
	 * @param set the sections to check for
	 * @param start the section to examine the sections after it
	 * @return if the section after the specified section is in the list
	 */
	private static boolean containsSectionAfter(Collection<Section<?>> set, Section<?> start) {
		Section<?> grandSibling = null;
		Section<?> iter = start;
		while (iter.getParent() != null) {
			List<Section<?>> siblings = iter.getParent().getChildren();
			int index = siblings.indexOf(iter) + 1;
			if (index < siblings.size()) {
				grandSibling = siblings.get(index);
				break;
			}
			iter = iter.getParent();
		}
		// check if we are in the right-most branch
		if (grandSibling == null) return false;
		// now descent the left-most branch if any of the children are in our set
		iter = grandSibling;
		while (true) {
			if (set.contains(iter)) return true;
			List<Section<?>> children = iter.getChildren();
			if (children.isEmpty()) return false;
			iter = children.get(0);
		}
	}

	/**
	 * Returns if the specified collection of Sections contains any of the direct sections before
	 * the specified section. The sections before are all sections, that does not intersect with the
	 * specified section, but have their end index equal to the start index of the specified start
	 * section.
	 *
	 * @param set the sections to check for
	 * @param start the section to examine the sections after it
	 * @return if the section after the specified section is in the list
	 */
	private static boolean containsSectionBefore(Collection<Section<?>> set, Section<?> start) {
		Section<?> grandSibling = null;
		Section<?> iter = start;
		while (iter.getParent() != null) {
			List<Section<?>> siblings = iter.getParent().getChildren();
			int index = siblings.indexOf(iter) - 1;
			if (index >= 0) {
				grandSibling = siblings.get(index);
				break;
			}
			iter = iter.getParent();
		}
		// check if we are in the right-most branch
		if (grandSibling == null) return false;
		// now descent the left-most branch if any of the children are in our set
		iter = grandSibling;
		while (true) {
			if (set.contains(iter)) return true;
			List<Section<?>> children = iter.getChildren();
			if (children.isEmpty()) return false;
			iter = children.get(children.size() - 1);
		}
	}

	static void renderSections(List<Section<?>> previews, UserContext user, RenderResult result) {
		List<Section<?>> list = new ArrayList<>(previews);
		Collections.sort(list);
		DEFAULT_MARKUP_RENDERER.renderContentsAndAnnotations(list, user, result);
	}

	/**
	 * Returns true if the specified parent section hat at least one of the specified successor
	 * sections as an successor.
	 *
	 * @param parent the parent section to start the search from
	 * @param successors the list of potential successors
	 * @created 26.08.2013
	 */
	private static boolean hasSuccessor(Section<?> parent, Collection<Section<?>> successors) {
		if (successors.contains(parent)) return true;
		for (Section<?> child : parent.getChildren()) {
			if (hasSuccessor(child, successors)) return true;
		}
		return false;
	}

}
