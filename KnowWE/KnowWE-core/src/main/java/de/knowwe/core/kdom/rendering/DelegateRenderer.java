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
import java.util.Map.Entry;
import java.util.logging.Logger;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.MessageRenderer;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

public class DelegateRenderer implements Renderer {

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
	public void render(Section<?> section, UserContext user,
			StringBuilder builder) {

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
				builder.append(section.getText());
			}
			else {
				for (Section<?> subSection : subSections) {
					renderSubSection(subSection, user, builder);
				}
			}
		}
		catch (Exception e) {
			// wow, that was evil!
			// now we log instead AND report the error to the user
			Logger.getLogger(getClass().getName()).warning(
					"Internal error while rendering section");
			builder.append(KnowWEUtils.maskHTML("<span class='warning'>"));
			builder.append("internal error while rendering section: " + e);
			builder.append(KnowWEUtils.maskHTML("</span>"));
			e.printStackTrace();
		}

		if (renderTypes) renderType(section, false, builder);
	}

	protected void renderSubSection(Section<?> subSection, UserContext user, StringBuilder builder) {
		renderAnchor(subSection, builder);

		// any messageRenderer has pre- and post rendering hook
		// first call pre-rendering for all messages of this subsection
		renderMessagesPre(subSection, user, builder);

		// use subSection's renderer
		Renderer renderer = getRenderer(subSection, user);
		renderer.render(subSection, user, builder);

		// then call post rendering for all messages of this subsection
		renderMessagesPost(subSection, user, builder);
	}

	private void renderMessagesPost(Section<?> subSection, UserContext user, StringBuilder builder) {
		// Render errors post
		Map<String, Collection<Message>> errors = Messages.getMessages(subSection,
				Message.Type.ERROR);
		for (Entry<String, Collection<Message>> entry : errors.entrySet()) {
			for (Message kdomNotice : entry.getValue()) {
				MessageRenderer errorRenderer = subSection.get()
							.getErrorRenderer();
				if (errorRenderer != null) {
					builder.append(errorRenderer.postRenderMessage(kdomNotice, user,
								entry.getKey()));
				}
			}
		}

		// Render notices post
		Map<String, Collection<Message>> notices = Messages
				.getMessages(subSection, Message.Type.INFO);
		for (Entry<String, Collection<Message>> entry : notices.entrySet()) {
			for (Message kdomNotice : entry.getValue()) {
				MessageRenderer noticeRenderer = subSection.get()
							.getNoticeRenderer();
				if (noticeRenderer != null) {
					builder.append(noticeRenderer.postRenderMessage(kdomNotice, user,
								entry.getKey()));
				}
			}
		}

		// Render warnings post
		Map<String, Collection<Message>> warnings = Messages.getMessages(subSection,
				Message.Type.WARNING);
		for (Entry<String, Collection<Message>> entry : warnings.entrySet()) {
			for (Message kdomWarning : entry.getValue()) {
				MessageRenderer warningRenderer = subSection.get()
							.getWarningRenderer();
				if (warningRenderer != null) {
					builder.append(warningRenderer
								.postRenderMessage(kdomWarning, user, entry.getKey()));
				}
			}
		}

		if (warnings.size() > 0 || notices.size() > 0 || errors.size() > 0) {
			builder.append(KnowWEUtils.maskHTML("<a name=\"" + subSection.getID()
					+ "\"></a>"));
		}
	}

	private void renderMessagesPre(Section<?> subSection, UserContext user, StringBuilder builder) {
		// Render warnings pre
		Map<String, Collection<Message>> warnings = Messages.getMessages(
				subSection, Message.Type.WARNING);
		for (Entry<String, Collection<Message>> entry : warnings.entrySet()) {
			for (Message kdomWarning : entry.getValue()) {
				MessageRenderer warningRenderer = subSection.get()
						.getWarningRenderer();
				if (warningRenderer != null) {
					builder.append(warningRenderer
							.preRenderMessage(kdomWarning, user, entry.getKey()));
				}
			}
		}

		// Render notices pre
		Map<String, Collection<Message>> notices = Messages
				.getMessages(subSection, Message.Type.INFO);
		for (Entry<String, Collection<Message>> entry : notices.entrySet()) {
			for (Message kdomNotice : entry.getValue()) {
				MessageRenderer noticeRenderer = subSection.get()
						.getNoticeRenderer();
				if (noticeRenderer != null) {
					builder.append(noticeRenderer.preRenderMessage(kdomNotice, user, entry.getKey()));
				}
			}
		}

		// Render errors pre
		Map<String, Collection<Message>> errors = Messages.getMessages(subSection,
				Message.Type.ERROR);
		for (Entry<String, Collection<Message>> entry : errors.entrySet()) {
			for (Message kdomNotice : entry.getValue()) {
				MessageRenderer errorRenderer = subSection.get()
						.getErrorRenderer();
				if (errorRenderer != null) {
					builder.append(errorRenderer.preRenderMessage(kdomNotice, user, entry.getKey()));
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

	public static Renderer getRenderer(Section<?> section, UserContext user) {

		Type objectType = section.get();
		Renderer renderer = objectType.getRenderer();

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
