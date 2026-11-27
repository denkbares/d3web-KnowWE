/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.include;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiMarkupUtils;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.DefaultToolSet;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolSet;
import de.knowwe.util.Icon;

/**
 * Renderer to render an included section in default markup framed style.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 05.02.2014
 */
public class FramedIncludedSectionRenderer extends DefaultMarkupRenderer {

	private final boolean skipHeader;

	public FramedIncludedSectionRenderer(boolean skipHeader) {
		this.skipHeader = skipHeader;
		setPreFormattedStyle(false);
	}

	@Override
	protected ToolSet getTools(Section<?> targetSection, UserContext user) {
		Article article = targetSection.getArticle();
		String link = KnowWEUtils.getURLLink(article);
		if (targetSection.get() instanceof HeaderType) {
			Section<HeaderType> header = Sections.cast(targetSection, HeaderType.class);
			String targetHeaderName = header.get().getHeaderText(header);
			// link for section
			link += "#section-"
					+ article.getTitle().replaceAll("\\s", "+")
					+ "-" + targetHeaderName.replaceAll("\\s", "");
		}

		return new DefaultToolSet(
				new DefaultTool(Icon.OPENPAGE, "Open Page", "Opens page '" + targetSection.getTitle()
						+ "'", "window.location ='" + link + "'", Tool.CATEGORY_NAVIGATE));
	}

	@Override
	protected String getTitleName(Section<?> section, UserContext user) {
		return "Included from '" + section.getTitle() + "'";
	}

	@Override
	public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
		renderTargetSections(section, skipHeader, user, result);
	}

	public static void renderTargetSections(Section<?> targetSection, boolean skipHeader, UserContext user, RenderResult result) {

		// check for cycles
		if (includesItself(targetSection, user)) {
			result.append("\n\n%%error Cyclic include detected, please check you include declarations /%\n");
			return;
		}

		// render the target section
		// but don't forget to check the user access rights
		if (KnowWEUtils.canView(targetSection, user)) {
			renderTargetSectionsSecure(targetSection, skipHeader, user, result);
		}
		else {
			result.append("\n\n%%error you are not allowed to view the referenced article '" +
					targetSection.getTitle() + "' /%\n");
		}
	}

	/**
	 * Whether rendering the given included section would include the section itself again, directly or through the
	 * sections it includes (see {@link IncludingType}). This is decided from the KDOM alone, without any state of the
	 * renderings in progress, so renderings of other users or threads do not affect it, and nothing has to be guarded
	 * against them. If included sections include each other, the first of them already refuses to render, so a cycle
	 * never renders recursively.
	 */
	static boolean includesItself(Section<?> targetSection, UserContext user) {
		Set<Section<?>> visited = new HashSet<>();
		Deque<Section<?>> pending = new ArrayDeque<>(getIncludedSections(targetSection, user));
		while (!pending.isEmpty()) {
			Section<?> included = pending.pop();
			if (included == targetSection) return true;
			if (visited.add(included)) pending.addAll(getIncludedSections(included, user));
		}
		return false;
	}

	/**
	 * The sections included by the markups within what is rendered for the given included section: the header and
	 * the sections belonging to it for a header, the section with its successors otherwise.
	 */
	private static List<Section<?>> getIncludedSections(Section<?> targetSection, UserContext user) {
		List<Section<?>> rendered = new ArrayList<>();
		if (targetSection.get() instanceof HeaderType) {
			rendered.addAll(JSPWikiMarkupUtils.getContent(Sections.cast(targetSection, HeaderType.class), true));
		}
		else {
			rendered.add(targetSection);
		}
		List<Section<?>> included = new ArrayList<>();
		for (Section<IncludingType> including : Sections.successors(rendered, IncludingType.class)) {
			included.addAll(including.get().getIncludedSections(including, user));
		}
		return included;
	}

	private static void renderTargetSectionsSecure(Section<?> targetSection, boolean skipHeader, UserContext user, RenderResult result) {
		if (targetSection.get() instanceof HeaderType) {
			Section<HeaderType> header = Sections.cast(targetSection, HeaderType.class);

			if (!skipHeader) {
				// render header
				DelegateRenderer.getInstance().render(targetSection, user, result);
			}

			// render content of the sub-chapter
			List<Section<? extends Type>> content = JSPWikiMarkupUtils.getContent(header);
			for (Section<? extends Type> section : content) {
				Renderer r = section.get().getRenderer();
				r.render(section, user, result);
			}
		}
		else {
			result.append(targetSection, user);
		}
	}
}
