package de.knowwe.core.preview;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Scope;
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
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 26.08.2013
 */
public class DefaultMarkupPreviewRenderer implements PreviewRenderer {

	public final Map<Scope, Boolean> previewItems = new HashMap<Scope, Boolean>();

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
	public void addPreviewItem(Scope scope, boolean forceVisible) {
		previewItems.put(scope, forceVisible);
	}

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		if (section.get() instanceof DefaultMarkupType) {
			// render all sub sections, but not the default markup itself
			DefaultMarkupRenderer.renderContentSections(section.getChildren(), user, " ", result);
		}
		else {
			Section<?> parent = Sections.findAncestorOfType(section, DefaultMarkupType.class);
			if (parent == null) {
				parent = section;
				while (!(parent.getFather().get() instanceof RootType)
						&& parent.getFather() != null) {
					parent = parent.getFather();
				}
			}
			List<Section<?>> previews = new LinkedList<Section<?>>();
			// collect all relevant scoped sections plus the matched section
			for (Entry<Scope, Boolean> entry : previewItems.entrySet()) {
				Scope scope = entry.getKey();
				boolean forcePreview = entry.getValue();
				List<Section<?>> matches = scope.getMatchingAnchestors(parent);
				if (forcePreview) {
					previews.addAll(matches);
				}
				else {
					for (Section<?> match : matches) {
						if (hasSuccessor(match, relevantSubSections)) {
							previews.add(match);
						}
					}
				}
			}
			Collections.sort(previews);
			DefaultMarkupRenderer.renderContentSections(previews, user, " ", result);
		}
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
