/*
 * Copyright (C) 2014 denkbares GmbH
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Scope;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.util.Icon;

class IncludeRenderer extends DefaultMarkupRenderer {
	private static final Logger LOGGER = LoggerFactory.getLogger(IncludeRenderer.class);

	private static final Icon HEADER_LINK_ICON = Icon.EXTERNAL_LINK.addClasses("include-sourceHeaderLink");

	public IncludeRenderer() {
		setPreFormattedStyle(false);
		setListAnnotations(true);
	}

	@Override
	public void render(Section<?> includeMarkup, UserContext user, RenderResult result) {

		Section<IncludeMarkup> section = Sections.cast(includeMarkup, IncludeMarkup.class);

		// parse parameters
		String frame = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_FRAME);
		boolean isFramed = Strings.equalsIgnoreCase(frame, "show") || Strings.equalsIgnoreCase(frame, "true");

		String urlPara = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_URL_PARA);
		if(urlPara!= null) {
			String url = "http://www.example.com/something.html?"+urlPara;
			List<NameValuePair> params = null;
			try {
				params = URLEncodedUtils.parse(new URI(url), "UTF-8");
			}
			catch (URISyntaxException e) {
				LOGGER.error("invalid uri", e);
			}
			for (NameValuePair param : params) {
				user.getRequest().getSession().setAttribute(param.getName(), param.getValue());
			}
		}

		result.appendHtmlTag("div", "id", section.getID(), "class", "IncludeMarkup"); // add marker for edit mode

		// check for errors in links and/or annotations
		List<Section<WikiReference>> references =
				Sections.successors(section, WikiReference.class);
		for (Section<WikiReference> ref : references) {
			WikiReference.updateReferences(ref);
		}

		// show document title
		renderDocumentInfo(section, result);

		int zoom = 100;
		Section<?> zoomSection =
				DefaultMarkupType.getAnnotationContentSection(section,
						IncludeMarkup.ANNOTATION_ZOOM);
		if (zoomSection != null) {
			try {
				zoom = getZoomPercent(section);
				Messages.clearMessages(null, zoomSection, getClass());

			}
			catch (NumberFormatException e) {
				Messages.storeMessage(
						null, zoomSection, getClass(),
						Messages.error("Zoom value of '" + zoom
								+ "' is not a valid number or percentage"));
			}
		}

		Section<?> showDefSection =
				DefaultMarkupType.getAnnotationContentSection(section,
						IncludeMarkup.ANNOTATION_DEFINITION);
		String showDef = DefaultMarkupType.getAnnotation(section,
				IncludeMarkup.ANNOTATION_DEFINITION);
		boolean hasMessage = Messages.hasMessagesInSubtree(section, Type.ERROR, Type.WARNING);
		// show warning if an error message
		// forces a hidden definition to be shown
		if (hasMessage && Strings.equalsIgnoreCase(showDef, "hide")) {
			Messages.storeMessage(
					null, showDefSection, getClass(),
					Messages.warning("Annotation ignored due to error messages before"));
		}
		else if (showDefSection != null) {
			Messages.clearMessages(null, showDefSection, getClass());
		}

		// render default markup if requested or if there are errors
		if (hasMessage || Strings.equalsIgnoreCase(showDef, "show")) {
			super.render(section, user, result);
		}

		// render included sections
		for (Section<WikiReference> include : references) {
			// find section and render it
			Section<?> referencedSection = WikiReference.getReferencedSection(include);
			int listLevel = WikiReference.getListMarks(include).length();
			// check for simple include
			if (listLevel == 0) {
				renderIncludedSections(user, referencedSection, result,
						zoom, isFramed, false);
				continue;
			}
			// if the include uses an explicit header declaration
			// with (*, #, or - in front), we render our own header
			// (if not preceded with "-")
			// and suppress the existing one
			if (!include.get().isSuppressHeader(include)) {
				String title = WikiReference.getLinkName(include);
				result.appendHtml("<h" + listLevel + ">")
						.append(title)
						.appendHtml("</h" + listLevel + ">\n");
			}
			renderIncludedSections(user, referencedSection, result,
					zoom, isFramed, true);
		}

		result.appendHtmlTag("/div");
	}

	private void renderDocumentInfo(Section<IncludeMarkup> section, RenderResult result) {
		String project = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_PROJECT);
		if (!Strings.isBlank(project)) {
			result.appendHtml("\n<div class='wikiBook-project'>")
					.append(project).appendHtml("</div>\n");
		}
		String title = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_TITLE);
		if (!Strings.isBlank(title)) {
			result.appendHtml("\n<div class='wikiBook-title'>")
					.append(title).appendHtml("</div>\n");
		}
		String author = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_AUTHOR);
		if (!Strings.isBlank(author)) {
			result.appendHtml("\n<div class='wikiBook-author'>")
					.append(author).appendHtml("</div>\n");
		}
	}

	private int getZoomPercent(Section<IncludeMarkup> section) throws NumberFormatException {
		String zoom = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_ZOOM);
		if (Strings.isBlank(zoom)) return 100;

		// parse value
		double factor = Double.parseDouble(zoom.replaceAll("%", "").trim());
		// if value indicates a factor instead of percentage (< 5)
		// and the percentage is not explicitly specified, correct
		if (factor < 5d && !zoom.contains("%")) factor *= 100;
		return (int) Math.round(factor);
	}

	public void renderIncludedSections(UserContext user, Section<?> targetSection, RenderResult result, int zoom, boolean framed, boolean skipHeader) {
		if (targetSection == null) return;
		if (zoom != 100) {
			result.appendHtml("<div style='zoom:" + zoom + "%; clear:both'>");
		}
		result.append("\n");

		// create a new render result that decorated the headings with links
		// render the link right before the line break
		RenderResult decorated = new RenderResult(result);
		decorated.addCustomRenderer(Scope.getScope("HeaderType/LineBreak")::matches, (s, u, r) -> {
			Section<HeaderType> header = Sections.cast(s.getParent(), HeaderType.class);
			r.append(" [").appendHtml(HEADER_LINK_ICON.toHtml())
					.append("|").append(KnowWEUtils.getWikiLink(header)).append("]");
			DelegateRenderer.getRenderer(s, u).render(s, u, r);
		});

		if (framed) {
			// used the framing renderer
			new FramedIncludedSectionRenderer(skipHeader).render(
					targetSection, user, decorated);
		}
		else {
			// or simply render the sections belonging to the header
			FramedIncludedSectionRenderer.renderTargetSections(
					targetSection, skipHeader, user, decorated);
		}

		result.append(decorated);
		if (zoom != 100) {
			result.appendHtml("</div>");
		}
	}
}