package de.d3web.we.quicki;

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

import java.util.Map;

import de.knowwe.core.RessourceLoader;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * 
 * @author Benedikt Kaemmerer
 * @created 28.07.2012
 */

public class QuickInterviewMarkup extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	public static final String MARKUP_NAME = "QuickInterview";

	public static final String UNKNOWN_KEY = "unknown";

	public static final String ABSTRACTIONS_KEY = "abstractions";

	public static final String ANSWERS_KEY = "answers";

	public static final String SAVE_KEY = "save";

	static {
		m = new DefaultMarkup(MARKUP_NAME);
		m.addAnnotation(UNKNOWN_KEY, false, "true", "false");
		m.addAnnotationRenderer(UNKNOWN_KEY, NothingRenderer.getInstance());

		m.addAnnotation(ABSTRACTIONS_KEY, false, "true", "false");
		m.addAnnotationRenderer(ABSTRACTIONS_KEY, NothingRenderer.getInstance());

		m.addAnnotation(ANSWERS_KEY, false);
		m.addAnnotationRenderer(ANSWERS_KEY, NothingRenderer.getInstance());

		m.addAnnotation(SAVE_KEY, false);
		m.addAnnotationRenderer(SAVE_KEY, NothingRenderer.getInstance());
	}

	public QuickInterviewMarkup() {
		super(m);
		RessourceLoader.getInstance().add("quickiNeutral.css",
				RessourceLoader.RESOURCE_STYLESHEET);
		RessourceLoader.getInstance().add("quicki.js",
				RessourceLoader.RESOURCE_SCRIPT);
		this.setRenderer(new QIRenderer());

	}

	static class QIRenderer extends DefaultMarkupRenderer {

		@Override
		public void renderContents(Section<?> section, UserContext user, RenderResult string) {
			String unknown = DefaultMarkupType.getAnnotation(section, UNKNOWN_KEY);
			String abstractions = DefaultMarkupType.getAnnotation(section,
					ABSTRACTIONS_KEY);
			String answers = DefaultMarkupType.getAnnotation(section, ANSWERS_KEY);
			// String save = DefaultMarkupType.getAnnotation(section, SAVE_KEY);
			Map<String, String> parameters = user.getParameters();
			if (unknown != null) {
				parameters.put(UNKNOWN_KEY, unknown);
			}
			if (abstractions != null) {
				parameters.put(ABSTRACTIONS_KEY, abstractions);
			}
			if (answers != null) {
				parameters.put(ANSWERS_KEY, answers);
			}
			String html = "<div id=\"quickinterview\">"
					+ QuickInterviewAction.callQuickInterviewRenderer(user)
					+ "</div>";
			string.appendHtml(html);

		}

	}

}
