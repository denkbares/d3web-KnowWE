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
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * 
 * @author Benedikt Kaemmerer
 * @created 28.07.2012
 */

public class QuickInterviewMarkup extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	public static final String MARKUP_NAME = "Quickinterview";

	public static String unknown;

	public static String abstractions;

	public static String answers;

	static {
		m = new DefaultMarkup(MARKUP_NAME);
		m.addAnnotation("unknown", false, "true", "false");
		m.addAnnotation("abstractions", false, "true", "false");
		m.addAnnotation("answers", false);
	}

	public QuickInterviewMarkup() {
		super(m);
		RessourceLoader.getInstance().add("quickiNeutral.css",
				RessourceLoader.RESOURCE_STYLESHEET);
		RessourceLoader.getInstance().add("quicki.js",
				RessourceLoader.RESOURCE_SCRIPT);
		this.setRenderer(new QIRenderer());

	}

	static class QIRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, StringBuilder string) {
			unknown = DefaultMarkupType.getAnnotation(section,
					"unkown");
			abstractions = DefaultMarkupType.getAnnotation(section,
					"abstractions");
			answers = DefaultMarkupType.getAnnotation(section,
					"answers");
			Map<String, String> parameters = user.getParameters();
			if (unknown != null) {
				parameters.put("unknown", unknown);
			}
			if (abstractions != null) {
				parameters.put("abstractions", abstractions);
			}
			if (answers != null) {
				parameters.put("answers", answers);
			}
			String html = "<div id=\"quickinterview\">"
					+ QuickInterviewAction.callQuickInterviewRenderer(user) + "</div>";
			string.append(Strings.maskHTML(html));
			DelegateRenderer.getInstance().render(section,
					user, string);

		}

	}

}
