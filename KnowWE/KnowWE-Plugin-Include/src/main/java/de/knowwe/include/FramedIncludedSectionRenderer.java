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
import de.knowwe.tools.ToolSet;
import de.knowwe.util.Icon;

/**
 * Renderer to render an included section in default markup framed style.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 05.02.2014
 */
public class FramedIncludedSectionRenderer extends DefaultMarkupRenderer {

	private static final Set<Section<?>> cycleDetection = new HashSet<Section<?>>();
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
				new DefaultTool(Icon.NONE, "Open Page", "Opens page '" + targetSection.getTitle()
						+ "'", "window.location ='" + link + "'"));
	}

	@Override
	protected String getTitleName(Section<?> section, UserContext user) {
		return "Included from '" + section.getTitle() + "'";
	}

	@Override
	protected void renderContents(Section<?> section, UserContext user, RenderResult result) {
		renderTargetSections(section, skipHeader, user, result);
	}

	// method must be synchronized due to using only
	// a static cycle detection field
	// --> otherwise multiple parallel renderings would interfere negatively
	public synchronized static void renderTargetSections(Section<?> targetSection, boolean skipHeader, UserContext user, RenderResult result) {

		// check for cycles
		boolean isNew = cycleDetection.add(targetSection);
		if (!isNew) {
			result.append("\n\n%%error Cyclic include detected, please check you include declarations /%\n");
			return;
		}

		// render the target section
		// but don't forget to check the user access rights
		try {
			if (KnowWEUtils.canView(targetSection, user)) {
				renderTargetSectionsSecure(targetSection, skipHeader, user, result);
			}
			else {
				result.append("\n\n%%error you are not allowed to view the referenced article '" +
						targetSection.getTitle() + "' /%\n");
			}
		}
		finally {
			cycleDetection.remove(targetSection);
		}
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
				if (r != null) {
					r.render(section, user, result);
				}
				else {
					DelegateRenderer.getInstance().render(section, user, result);
				}
			}
		}
		else {
			DelegateRenderer.getInstance().render(targetSection, user, result);
		}
	}
}
