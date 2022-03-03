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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.MessageRenderer;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;

public class DelegateRenderer implements Renderer {
	private static final Logger LOGGER = LoggerFactory.getLogger(DelegateRenderer.class);

	private static final DelegateRenderer instance = new DelegateRenderer();

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
	public void render(Section<?> section, UserContext user, RenderResult result) {

		boolean renderTypes = isRenderTypes(user.getParameters());
		if (renderTypes) renderType(section, true, result);

		List<Section<?>> subSections = section.getChildren();
		if (subSections.isEmpty()) {
			result.append(section.getText());
		}
		else {
			for (Section<?> subSection : subSections) {
				renderSubSection(subSection, user, result);
			}
		}

		if (renderTypes) renderType(section, false, result);
	}

	public void renderSubSection(Section<?> subSection, UserContext user, RenderResult result) {
		try {
			final RenderResult subResult = new RenderResult(result);

			// any messageRenderer has pre- and post rendering hook
			// first call pre-rendering for all messages of this subsection
			renderMessagesPre(subSection, user, subResult);

			// use subSection's renderer
			subResult.append(subSection, user);

			// then call post rendering for all messages of this subsection
			renderMessagesPost(subSection, user, subResult);

			result.append(subResult);
		}
		catch (Exception e) {
			LOGGER.warn("Internal error while rendering section", e);
			result.appendHtml("<span class='warning'>");
			result.append("Internal error while rendering section: " + e.getMessage());
			result.appendHtml("</span>");
		}
	}

	private void renderMessagesPost(Section<?> subSection, UserContext user, RenderResult builder) {

		boolean renderedAny = false;
		// Render errors post
		Map<Compiler, Collection<Message>> errors = Messages.getMessagesMap(
				subSection,
				Message.Type.ERROR);
		for (Entry<Compiler, Collection<Message>> entry : errors.entrySet()) {
			for (Message kdomNotice : entry.getValue()) {
				MessageRenderer errorRenderer = subSection.get().getMessageRenderer(
						Message.Type.ERROR);
				if (errorRenderer != null) {
					renderedAny = true;
					errorRenderer.postRenderMessage(kdomNotice, user, entry.getKey(),
							builder);
				}
			}
		}

		// Render notices post
		Map<Compiler, Collection<Message>> notices = Messages
				.getMessagesMap(subSection, Message.Type.INFO);
		for (Entry<Compiler, Collection<Message>> entry : notices.entrySet()) {
			for (Message kdomNotice : entry.getValue()) {
				MessageRenderer noticeRenderer = subSection.get().getMessageRenderer(
						Message.Type.INFO);
				if (noticeRenderer != null) {
					renderedAny = true;
					noticeRenderer.postRenderMessage(kdomNotice, user, entry.getKey(),
							builder);
				}
			}
		}

		// Render warnings post
		Map<Compiler, Collection<Message>> warnings = Messages.getMessagesMap(
				subSection,
				Message.Type.WARNING);
		for (Entry<Compiler, Collection<Message>> entry : warnings.entrySet()) {
			for (Message kdomWarning : entry.getValue()) {
				MessageRenderer warningRenderer = subSection.get().getMessageRenderer(
						Message.Type.WARNING);
				if (warningRenderer != null) {
					renderedAny = true;
					warningRenderer.postRenderMessage(kdomWarning, user, entry.getKey(),
							builder);
				}
			}
		}

		if (renderedAny) {
			builder.appendHtml("<a name=\"" + subSection.getID() + "\"></a>");
		}
	}

	private void renderMessagesPre(Section<?> subSection, UserContext user, RenderResult builder) {
		// Render warnings pre
		Map<Compiler, Collection<Message>> warnings = Messages.getMessagesMap(
				subSection, Message.Type.WARNING);
		for (Entry<Compiler, Collection<Message>> entry : warnings.entrySet()) {
			for (Message kdomWarning : entry.getValue()) {
				MessageRenderer warningRenderer = subSection.get()
						.getMessageRenderer(Message.Type.WARNING);
				if (warningRenderer != null) {
					warningRenderer.preRenderMessage(kdomWarning, user,
							warnings.size() > 1 ? entry.getKey() : null, builder);
				}
			}
		}

		// Render notices pre
		Map<Compiler, Collection<Message>> notices = Messages
				.getMessagesMap(subSection, Message.Type.INFO);
		for (Entry<Compiler, Collection<Message>> entry : notices.entrySet()) {
			for (Message kdomNotice : entry.getValue()) {
				MessageRenderer noticeRenderer = subSection.get()
						.getMessageRenderer(Message.Type.INFO);
				if (noticeRenderer != null) {
					noticeRenderer.preRenderMessage(kdomNotice, user,
							notices.size() > 1 ? entry.getKey() : null, builder);
				}
			}
		}

		// Render errors pre
		Map<Compiler, Collection<Message>> errors = Messages.getMessagesMap(
				subSection,
				Message.Type.ERROR);
		for (Entry<Compiler, Collection<Message>> entry : errors.entrySet()) {
			for (Message kdomNotice : entry.getValue()) {
				MessageRenderer errorRenderer = subSection.get()
						.getMessageRenderer(Message.Type.ERROR);
				if (errorRenderer != null) {
					errorRenderer.preRenderMessage(kdomNotice, user,
							errors.size() > 1 ? entry.getKey() : null, builder);
				}
			}
		}
	}

	private void renderType(Section<?> section, boolean openIt,
							RenderResult builder) {
		builder.appendHtml("<sub>&lt;");
		if (!openIt) builder.append('/');
		builder.append(section.get().getName());
		builder.appendHtml("&gt;</sub>");
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
		return "true".equals(debug);
	}
}
