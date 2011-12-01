/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.core.kdom.rendering;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.MessageRenderer;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

public class DelegateRenderer extends KnowWEDomRenderer {

	private static DelegateRenderer instance;

	static {
		instance = new DelegateRenderer();
	}

	public static DelegateRenderer getInstance() {
		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public void render(KnowWEArticle article, Section section,
			UserContext user, StringBuilder builder) {

		boolean renderTypes = isRenderTypes(user.getParameters());
		if (renderTypes) renderType(section, true, builder);

		try {
			List<Section<?>> subSections = section.getChildren();
			if (subSections.size() == 0) {
				// KnowWEDomRenderer renderer = getRenderer(section, user);
				// if (renderer == this) {
				// // avoid endless recursion
				// builder.append(section.getOriginalText());
				// }
				// else {
				// // otherwise use section's renderer instead
				// renderer.render(article, section, user, builder);
				// }
				renderAnchor(section, builder);
				builder.append(section.getOriginalText());
			}
			else {
				for (Section<?> subSection : subSections) {
					renderSubSection(article, subSection, user, builder);
				}
			}
		}
		catch (Exception e) {
			// wow, that was evil!
			// System.out.println(section.get());
			// e.printStackTrace();
			// now we log instead AND report the error to the user
			Logger.getLogger(getClass().getName()).warning(
					"internal error while rendering section" + e.getMessage());
			builder.append(KnowWEUtils.maskHTML("<span class='warning'>"));
			builder.append("internal error while rendering section: " + e);
			builder.append(KnowWEUtils.maskHTML("</span>"));
			e.printStackTrace();
		}

		if (renderTypes) renderType(section, false, builder);
	}

	protected void renderSubSection(KnowWEArticle article, Section<?> subSection, UserContext user, StringBuilder builder) {
		renderAnchor(subSection, builder);

		// any messageRenderer has pre- and post rendering hook
		// first call pre-rendering for all messages of this subsection
		renderMessagesPre(subSection, user, builder, article);

		// use subSection's renderer
		KnowWEDomRenderer renderer = getRenderer(subSection, user);
		renderer.render(article, subSection, user, builder);

		// then call post rendering for all messages of this subsection
		renderMessagesPost(article, subSection, user, builder);
	}

	private void renderMessagesPost(KnowWEArticle article, Section<?> subSection, UserContext user, StringBuilder builder) {
		// Render errors post
		Collection<Message> errors = Messages.getErrors(article, subSection);
		if (errors != null && errors.size() > 0) {
			for (Message kdomNotice : errors) {
				MessageRenderer errorRenderer = subSection.get()
						.getErrorRenderer();
				if (errorRenderer != null) {
					builder.append(errorRenderer.postRenderMessage(kdomNotice, user));
				}
			}
		}

		// Render notices post
		Collection<Message> notices = Messages
				.getNotices(article, subSection);
		if (notices != null && notices.size() > 0) {
			for (Message kdomNotice : notices) {
				MessageRenderer noticeRenderer = subSection.get()
						.getNoticeRenderer();
				if (noticeRenderer != null) {
					builder.append(noticeRenderer.postRenderMessage(kdomNotice, user));
				}
			}
		}

		// Render warnings post
		Collection<Message> warnings = Messages.getWarnings(article,
				subSection);
		if (warnings != null && warnings.size() > 0) {
			for (Message kdomWarning : warnings) {
				MessageRenderer warningRenderer = subSection.get()
						.getWarningRenderer();
				if (warningRenderer != null) {
					builder.append(warningRenderer
							.postRenderMessage(kdomWarning, user));
				}
			}
		}

		if ((warnings != null && warnings.size() > 0) || (notices != null && notices.size() > 0)
				|| (errors != null && errors.size() > 0)) {
			builder.append(KnowWEUtils.maskHTML("<a name=\"" + subSection.getID()
					+ "\"></a>"));
		}
	}

	private void renderMessagesPre(Section<?> subSection, UserContext user, StringBuilder builder, KnowWEArticle article) {
		// Render warnings pre
		Collection<Message> warnings = Messages.getWarnings(article,
				subSection);
		if (warnings != null && warnings.size() > 0) {
			for (Message kdomWarning : warnings) {
				MessageRenderer warningRenderer = subSection.get()
						.getWarningRenderer();
				if (warningRenderer != null) {
					builder.append(warningRenderer
							.preRenderMessage(kdomWarning, user));
				}
			}
		}

		// Render notices pre
		Collection<Message> notices = Messages
				.getNotices(article, subSection);
		if (notices != null && notices.size() > 0) {
			for (Message kdomNotice : notices) {
				MessageRenderer noticeRenderer = subSection.get()
						.getNoticeRenderer();
				if (noticeRenderer != null) {
					builder.append(noticeRenderer.preRenderMessage(kdomNotice, user));
				}
			}
		}

		// Render errors pre
		Collection<Message> errors = Messages.getErrors(article, subSection);
		if (errors != null && errors.size() > 0) {
			for (Message kdomNotice : errors) {
				MessageRenderer errorRenderer = subSection.get()
						.getErrorRenderer();
				if (errorRenderer != null) {
					builder.append(errorRenderer.preRenderMessage(kdomNotice, user));
				}
			}
		}
	}

	private void renderAnchor(Section<?> subSection, StringBuilder builder) {
		// String anchor = subSection.getId();
		// builder.append(KnowWEUtils.maskHTML("<a name='kdomID-"+anchor+"'></a>"));
	}

	private void renderType(Section<?> section, boolean openIt,
			StringBuilder builder) {
		builder.append(KnowWEUtils.maskHTML("<sub>&lt;"));
		if (!openIt) builder.append('/');
		builder.append(section.get().getName());
		builder.append(KnowWEUtils.maskHTML("&gt;</sub>"));
	}

	public static KnowWEDomRenderer<?> getRenderer(Section<?> section, UserContext user) {
		KnowWEDomRenderer<?> renderer = null;

		Type objectType = section.get();
		if (renderer == null) {
			renderer = RendererManager.getInstance().getRenderer(objectType,
					user.getUserName(), section.getTitle());
		}
		if (renderer == null) {
			renderer = objectType.getRenderer();
		}
		if (renderer == null) {
			renderer = DefaultTextRenderer.getInstance();
		}
		return renderer;
	}

	private boolean isRenderTypes(Map<String, String> urlParameterMap) {
		String debug = urlParameterMap.get("renderTypes");
		return debug != null && debug.equals("true");
	}
}
