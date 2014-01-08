package de.knowwe.include;

/*
 * Copyright (C) 2012 denkbares GmbH
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import de.d3web.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiMarkupUtils;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.DefaultToolSet;
import de.knowwe.tools.ToolSet;

/**
 * 
 * @author Benedikt Kaemmerer
 * @created 06.07.2012
 */

public class IncludeMarkup extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	public static final String MARKUP_NAME = "Include";

	public static final String ANNOTATION_ZOOM = "zoom";
	public static final String ANNOTATION_FRAME = "frame";

	static {
		m = new DefaultMarkup(MARKUP_NAME);
		m.addAnnotation(ANNOTATION_FRAME, false, "true", "false");
		m.addAnnotation(ANNOTATION_ZOOM, false);
	}

	public IncludeMarkup() {
		super(m);
		this.setRenderer(new IncludeRenderer());
	}

	static class IncludeRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user,
				RenderResult string) {

			String target = "";
			String targetKey = "";
			String frame = "";
			String zoom = "";
			String subSectionKey = "";

			target = Strings.trim(DefaultMarkupType.getContent(section));
			String[] targetArray = target.split("#");
			targetKey = targetArray[0];
			if (targetArray.length > 1) {
				subSectionKey = targetArray[1];
			}

			frame = DefaultMarkupType.getAnnotation(section,
					ANNOTATION_FRAME);

			zoom = DefaultMarkupType.getAnnotation(section,
					ANNOTATION_ZOOM);

			if (zoom != null) {
				zoom = zoom.replaceAll("%", "").trim();
			}

			Article article = Environment.getInstance()
					.getArticleManager(Environment.DEFAULT_WEB)
					.getArticle(targetKey);

			// warning if article not found
			if (article == null) {
				renderWarning(user, section, string, "Article '" + targetKey + "' not found!");
			}
			else {
				// render article
				Section<?> renderarticle = article.getRootSection();

				if (targetArray.length > 1) {
					List<Section<HeaderType>> secList = new ArrayList<Section<HeaderType>>();
					Sections.findSuccessorsOfType(
							article.getRootSection(), HeaderType.class, 2, secList);
					Iterator<?> listIterator = secList.iterator();
					while (listIterator.hasNext()) {
						Section<?> listElement = (Section<?>) listIterator.next();
						String text = listElement.getText();
						while (text.startsWith("!")) {
							text = text.substring(1);
						}
						text = text.trim();
						if (text.startsWith(subSectionKey)) {
							// renderarticle for single section
							renderarticle = listElement;
						}
					}
					// warning if section not found
					if (renderarticle.equals(article.getRootSection())) {
						renderWarning(user, section, string, "Section '" + subSectionKey
								+ "' not found!");
					}
					else {
						if (frame != null && frame.equals("true")) {
							// render section, with frame
							renderFrame(article, user, section, renderarticle, string, true,
									targetKey, zoom, subSectionKey);
						}
						else {
							// render section, no frame
							renderNoFrame(user, renderarticle, string, zoom);
						}
					}
				}
				else {
					if (frame != null && frame.equals("false")) {
						// render whole article, no frame
						renderNoFrame(user, renderarticle, string, zoom);
					}
					else {
						// render whole article, with frame
						renderFrame(article, user, section, renderarticle, string, false,
								targetKey, zoom, subSectionKey);
					}
				}
			}
		}

		public void renderFrame(Article article, UserContext user, Section<?> section, Section<?> renderarticle, RenderResult string, Boolean rendersec, String targetKey, String zoom, String subSectionKey) {

			String link;
			if (rendersec) {
				// link for section
				link = KnowWEUtils.getURLLink(article) + "#section-"
						+ article.getTitle().replaceAll("\\s", "+")
						+ "-" + subSectionKey.replaceAll("\\s", "");
			}
			else {
				// link for article
				link = KnowWEUtils.getURLLink(article);
			}

			ToolSet tools = new DefaultToolSet(
					new DefaultTool(null, "Open Page", "Opens page '" + article.getTitle()
							+ "'", "window.location ='" + link + "'"));

			RenderResult builder = new RenderResult(user);
			String zoomStyle = "";
			if (zoom != null) {
				zoomStyle = "zoom: " + zoom + "%";
			}
			builder.appendHtml("<div style=\"white-space:normal;" + zoomStyle + "\">");
			builder.append("\n");
			renderTarget(user, renderarticle, builder);
			builder.appendHtml("</div>");
			new IncludeDefaultMarkupRenderer().renderDefaultMarkupStyled("Include",
					builder.toStringRaw(),
					section.getID(), "", tools, user,
					string);

		}

		public void renderNoFrame(UserContext user, Section<?> renderarticle, RenderResult string, String zoom) {
			if (zoom != null) {
				string.appendHtml("<div style=\"zoom: " + zoom + "%\">");
			}
			string.append("\n");
			renderTarget(user, renderarticle, string);
			if (zoom != null) {
				string.appendHtml("</div>");
			}
		}

		/**
		 * 
		 * @created 22.11.2012
		 * @param user
		 * @param renderarticle
		 * @param string
		 */
		private void renderTarget(UserContext user, Section<?> renderarticle, RenderResult string) {
			if (renderarticle.get() instanceof HeaderType) {

				// render header
				DelegateRenderer.getInstance().render(renderarticle,
						user, string);

				// render content of the sub-chapter
				List<Section<? extends Type>> content = JSPWikiMarkupUtils.getContent(Sections.cast(
						renderarticle,
						HeaderType.class));
				for (Section<? extends Type> section : content) {
					Renderer r = section.get().getRenderer();
					if (r != null) {
						r.render(section, user, string);
					}
					else {
						DelegateRenderer.getInstance().render(section,
								user, string);
					}
				}
			}
			else {
				DelegateRenderer.getInstance().render(renderarticle,
						user, string);
			}
		}

		public void renderWarning(UserContext user, Section<?> section, RenderResult string, String warning) {
			RenderResult builder = new RenderResult(string);
			Message noSuchSection = new Message(Message.Type.WARNING,
					warning);
			Collection<Message> messages = new HashSet<Message>();
			messages.add(noSuchSection);
			DefaultMarkupRenderer.renderMessagesOfType(Message.Type.WARNING, messages,
					builder);
			new IncludeDefaultMarkupRenderer().renderDefaultMarkupStyled("include",
					builder.toStringRaw(),
					section.getID(), "", new DefaultToolSet(), user,
					string);
		}
	}
}