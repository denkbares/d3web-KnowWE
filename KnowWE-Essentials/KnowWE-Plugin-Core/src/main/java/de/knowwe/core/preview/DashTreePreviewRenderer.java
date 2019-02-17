package de.knowwe.core.preview;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.dashtree.DashSubtree;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Renders a preview of a dashtree element. The preview shows the element itself and the direct children elements only
 * (skipping all other items)
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 16.08.2013
 */
public class DashTreePreviewRenderer extends AbstractPreviewRenderer {

	private static final String ELLIPSE = "[...]";
	private static final int maxChildren = 3;

	public DashTreePreviewRenderer() {
	}

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		Section<DashTreeElement> self = Sections.child(section, DashTreeElement.class);
		renderDashTreeElement(self, user, result);

		Set<Section<DashTreeElement>> relevantLines = findRelevantLines(section, relevantSubSections);
		if (relevantLines.isEmpty()) {
			// if no lines are relevant, add the first few lines before ellipse
			// (e.g. the parent line is relevant itself)
			relevantLines = childrenLines(section).stream().limit(maxChildren).collect(Collectors.toSet());
		}

		// iterate the children, append ellipse for each non-relevant line
		boolean isInEllipse = false;
		for (Section<DashTreeElement> line : childrenLines(section)) {
			if (relevantLines.contains(line)) {
				// if line is relevant, print it, and allow further ellipse for skipped line(s)
				if (isInEllipse) result.append('\n');
				renderDashTreeElement(line, user, result);
				isInEllipse = false;
			}
			else if (!isInEllipse) {
				// if not relevant, and not already printed an ellipse, print it and forbid further ellipse
				result.appendJSPWikiMarkup(ELLIPSE);
				isInEllipse = true;
			}
		}
	}

	private void renderDashTreeElement(Section<DashTreeElement> self, UserContext user, RenderResult result) {
		result.append(self, user);
	}

	private Set<Section<DashTreeElement>> findRelevantLines(Section<?> section, Collection<Section<?>> relevantSubSections) {
		// prepare all lines that contain the relevant subsections
		Set<Section<DashTreeElement>> relevantLines = $(relevantSubSections).ancestor(DashTreeElement.class).asSet();

		// find all children lines that are relevant
		return childrenLines(section).filter(relevantLines::contains).asSet();
	}

	private Sections<DashTreeElement> childrenLines(Section<?> section) {
		return $(section).children().filter(DashSubtree.class)
				.children().filter(DashTreeElement.class);
	}

	@Override
	public boolean isPreviewAncestor(Section<?> ancestor, Section<?> relevant) {
		// for references, it is not sufficient to have the referening line itself,
		// but use the parent line instead
		if (relevant.get() instanceof TermReference) {
			Section<DashSubtree> relevantSubTree = $(relevant).ancestor(DashSubtree.class).getFirst();
			if (relevantSubTree == ancestor) {
				// if the preview's subtree is the direct (closest) subtree of the relevant section
				// we only accept if there is no parent section. Otherwise wait for the dash-parent
				return DashTreeUtils.getParentDashSubtree(relevant) == null;
			}
		}

		// for all other elements, we use the default behaviour
		return super.isPreviewAncestor(ancestor, relevant);
	}
}