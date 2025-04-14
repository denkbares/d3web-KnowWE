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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.knowwe.core.Environment;
import de.knowwe.core.ResourceLoader;
import de.knowwe.core.compile.packaging.MasterAnnotationWarningHandler;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.ResourceLoader.Type.script;
import static de.knowwe.core.ResourceLoader.Type.stylesheet;

/**
 * @author Benedikt Kaemmerer
 * @created 28.07.2012
 */

public class QuickInterviewMarkup extends DefaultMarkupType {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuickInterviewMarkup.class);

	private static final DefaultMarkup m;

	public static final String MARKUP_NAME = "QuickInterview";

	public static final String UNKNOWN_KEY = "unknown";

	public static final String LANGUAGE_KEY = "language";

	public static final String ABSTRACTIONS_KEY = "abstractions";

	public static final String ANSWERS_KEY = "answers";

	public static final String INPUT_SIZE_KEY = "inputSize";

	static {
		m = new DefaultMarkup(MARKUP_NAME);
		m.addAnnotation(UNKNOWN_KEY, false, "true", "false");
		m.addAnnotationRenderer(UNKNOWN_KEY, NothingRenderer.getInstance());

		m.addAnnotation(LANGUAGE_KEY, false, Pattern.compile("[\\w_-]+"));
		m.addAnnotationRenderer(LANGUAGE_KEY, NothingRenderer.getInstance());

		m.addAnnotation(ABSTRACTIONS_KEY, false, "true", "false");
		m.addAnnotationRenderer(ABSTRACTIONS_KEY, NothingRenderer.getInstance());

		m.addAnnotation(ANSWERS_KEY);
		m.addAnnotationRenderer(ANSWERS_KEY, NothingRenderer.getInstance());

		m.addAnnotation(INPUT_SIZE_KEY, false, Pattern.compile("\\d+"));
		m.addAnnotationRenderer(INPUT_SIZE_KEY, NothingRenderer.getInstance());

		PackageManager.addPackageAnnotation(m);

	}

	public QuickInterviewMarkup() {
		this(m);
	}

	public QuickInterviewMarkup(DefaultMarkup markup) {
		super(markup);
		ResourceLoader.getInstance().add("quickiNeutral.css", stylesheet, ResourceLoader.getJarVersion(QuickInterviewMarkup.class));
		ResourceLoader.getInstance().add("quicki.js", script, ResourceLoader.getJarVersion(QuickInterviewMarkup.class));
		this.setRenderer(new QuickInterviewMarkupRenderer());
		this.addCompileScript(new MasterAnnotationWarningHandler());
	}

	private static class QuickInterviewMarkupRenderer extends DefaultMarkupRenderer {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuickInterviewMarkupRenderer.class);

		@Override
		public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult string) {
			String unknown = DefaultMarkupType.getAnnotation(section, UNKNOWN_KEY);
			String abstractions = DefaultMarkupType.getAnnotation(section,
					ABSTRACTIONS_KEY);
			String answers = DefaultMarkupType.getAnnotation(section, ANSWERS_KEY);
			String language = DefaultMarkupType.getAnnotation(section, LANGUAGE_KEY);
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
			if (language != null) {
				parameters.put(LANGUAGE_KEY, language);
			}

			string.appendHtml("<input id=\"file-input\" type=\"file\" name=\"name\" style=\"display: none;\" onchange=\"KNOWWE.plugin.quicki.handleUploadFile(this.files, '" + section
					.getID() + "')\" />");
			string.appendHtml("<div style=\"display:none\" id=\"dialog-message\" title=\"Choose session\">" + getSavedSessions(user, section
					.getID()) + "</div>");
			string.appendHtmlTag("div", "class", "quickinterview", "sectionId", section.getID(),
					"id", "quickinterview_" + section.getID());
//			appendDropIndicator(string, section.getID());
			QuickInterviewRenderer.renderInterview(section, user, string);
			string.appendHtmlTag("/div");

			List<Section<?>> subsecs = section.getChildren();
			Section<?> first = subsecs.get(0);
			Section<?> last = subsecs.get(subsecs.size() - 1);
			for (Section<?> subsec : subsecs) {
				if (subsec == first && subsec.get() instanceof PlainText) {
					continue;
				}
				if (subsec == last && subsec.get() instanceof PlainText) {
					continue;
				}
				subsec.get().getRenderer().render(subsec, user, string);
			}
		}

		private void appendDropIndicator(RenderResult string, String sectionId) {
			string.appendHtml("<div style=\"display:none; z-index:1; border:5px dotted #162746; background:#eee; box-sizing: border-box; width:100%; height:100%; position:absolute; left:0; right:0;\" id=\"drop-indicator-" + sectionId + "\">");
			string.appendHtml("<div style=\"position:relative; text-align:center; top: 40%; color: #162746; font-weight: bold\">Drop protocol here to load</div>");
			string.appendHtml("</div>");
		}

		/**
		 * Finds previously saved QuickInterview Sessions
		 *
		 * @return String with html code containing options of .xml files
		 * @created 30.11.2012
		 */
		private static String getSavedSessions(UserContext user, String sectionId) {
			WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
			StringBuilder builder = new StringBuilder();
			builder.append("<form id=\"")
					.append(sectionId)
					.append("-form\" action=\"\">");
			try {
				List<WikiAttachment> attachments = wikiConnector.getAttachments(user.getTitle());
				int counter = 0;
				for (WikiAttachment wikiAttachment : attachments) {
					String fileName = wikiAttachment.getFileName();
					if (fileName.endsWith("xml")) {
						counter++;
						String buttonId = "attachment-" + sectionId + "-" + counter;
						builder.append("<input id=\"")
								.append(buttonId)
								.append("\" type=\"radio\" style=\"margin-right:10px\" name=\"quicki-session-record\" value=\"")
								.append(fileName).append("\" /><label for=\"").append(buttonId).append("\">")
								.append(fileName)
								.append("</label><br>");
					}
				}
			}
			catch (IOException e) {
				LOGGER.warn("cannot read saved user session", e);
			}
			builder.append("</form>");
			return builder.toString();
		}

	}

}
