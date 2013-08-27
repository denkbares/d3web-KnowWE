package de.knowwe.core.preview;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Scope;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Default implementation of a {@link PreviewRenderer} that can be applied to
 * any ancestor-section of a default markup type (as scope of the preview
 * rendering extension), as well as to the whole default markup type. if it is
 * applied to the whole default markup type. the default markup frame is not
 * rendered, but the whole content and all annotations sections.
 * <p>
 * If it is only scoped to certain sub-sections only these sections are
 * rendered. In this case you can additionally specify a set of (relative)
 * scopes that will be additionally rendered.
 * <p>
 * Please not that this preview renderer is for selective rendering only. If you
 * wand to render a complete default markup section without its frame, you may
 * use the {@link DefaultPreviewRenderer} instead.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 26.08.2013
 */
public class DefaultMarkupPreviewRenderer implements PreviewRenderer {

	public static enum Preview {
		/**
		 * Shows all successors matching the scope
		 */
		all,

		/**
		 * Shows successors that contains relevant items
		 */
		relevant,

		/**
		 * Shows successors that contains relevant items, but all if there are
		 * no successors with relevant items
		 */
		relevantOrAll
	}

	public final Map<Scope, Preview> previewItems = new HashMap<Scope, Preview>();

	public DefaultMarkupPreviewRenderer() {
	}

	/**
	 * Adds a new scope of sub-items to be shown. When rendering the preview all
	 * sub-sections will be visible that matches the scopes specified through
	 * this method. If forceVisible is true. all the matching sub-section are
	 * shown. If not, only the matching sub-sections are shown that contains
	 * relevant items.
	 * 
	 * @created 26.08.2013
	 * @param scope the sub-scope to be shown
	 * @param forceVisible if the are always shown or only if the contain
	 *        relevant items
	 */
	public void addPreviewItem(Scope scope, Preview preview) {
		previewItems.put(scope, preview);
	}

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		Section<?> parent = getParentSection(section);
		List<Section<?>> previews = new LinkedList<Section<?>>();
		// collect all relevant scoped sections plus the matched section
		for (Entry<Scope, Preview> entry : previewItems.entrySet()) {
			Scope scope = entry.getKey();
			Preview preview = entry.getValue();
			List<Section<?>> matches = scope.getMatchingAnchestors(parent);
			if (preview.equals(Preview.all)) {
				// add all matches if requested
				previews.addAll(matches);
			}
			else {
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
				if (preview.equals(Preview.relevantOrAll) && !added) {
					previews.addAll(matches);
				}
			}
		}
		Collections.sort(previews);
		renderSections(previews, user, result);
	}

	static void renderSections(List<Section<?>> previews, UserContext user, RenderResult result) {
		List<Section<?>> extended = new LinkedList<Section<?>>();
		for (Section<?> section : previews) {
			// if we have an annotation type also add the plain text right
			// before it
			// which only consists of white-spaces
			if (section.get() instanceof AnnotationType) {
				List<Section<? extends Type>> siblings = section.getFather().getChildren();
				int index = siblings.indexOf(section);
				if (index > 0) {
					Section<?> sibling = siblings.get(index - 1);
					if (Strings.isBlank(sibling.getText())) {
						extended.add(sibling);
					}
				}
			}
			extended.add(section);
		}
		DefaultMarkupRenderer.renderContentSections(extended, user, result);
	}

	private Section<?> getParentSection(Section<?> section) {
		if (section.get() instanceof DefaultMarkupType) {
			return section;
		}
		Section<?> parent = Sections.findAncestorOfType(section, DefaultMarkupType.class);
		if (parent != null) {
			return parent;
		}
		parent = section;
		while (!(parent.getFather().get() instanceof RootType)) {
			parent = parent.getFather();
		}
		return parent;
	}

	/**
	 * Returns true if the specified parent section hat at least one of the
	 * specified successor sections as an successor.
	 * 
	 * @created 26.08.2013
	 * @param parent the parent section to start the search from
	 * @param successors the list of potential successors
	 * @return
	 */
	private static boolean hasSuccessor(Section<?> parent, Collection<Section<?>> successors) {
		if (successors.contains(parent)) return true;
		for (Section<?> child : parent.getChildren()) {
			if (hasSuccessor(child, successors)) return true;
		}
		return false;
	}

}
