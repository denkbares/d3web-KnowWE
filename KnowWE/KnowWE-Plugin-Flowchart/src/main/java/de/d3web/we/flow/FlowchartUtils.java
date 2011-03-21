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
package de.d3web.we.flow;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.flow.kbinfo.SearchInfoObjects;
import de.d3web.we.flow.type.FlowchartPreviewContentType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.kdom.xml.AbstractXMLType;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;

/**
 * 
 * @author Reinhard Hatko
 * @created 09.12.2009
 */
public class FlowchartUtils {

	public static final String PREVIEW_REGEX = "\\s*<!\\[CDATA\\[\\s*(.*)\\s*\\]\\]>\\s*";
	public static final Pattern PREVIEW_PATTERN = Pattern.compile(PREVIEW_REGEX);

	private FlowchartUtils() {
	}

	/**
	 * extracts the preview of a section of Type FlowchartType
	 */
	public static String extractPreview(Section<FlowchartType> flowchartSection) {

		Section<FlowchartPreviewContentType> previewsection = Sections.findSuccessor(
				flowchartSection, FlowchartPreviewContentType.class);

		if (previewsection == null) {
			return null;
		}

		String flowchart = previewsection.getOriginalText();

		Matcher matcher = PREVIEW_PATTERN.matcher(flowchart);
		if (!matcher.matches()) {
			return null;
		}
		else {
			return matcher.group(1);
		}
	}

	/**
	 * Creates a preview from the HTML code saved in the article by including
	 * necessary css-styles
	 * 
	 * @param preview
	 * @return
	 */
	public static String createRenderablePreview(String preview) {
		return "<div>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/flowchart.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/guard.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/node.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/rule.css'></link>" +
				preview +
				"</div>";
	}

	/**
	 * Creates a HTML representation of the preview.
	 * 
	 * @created 25.11.2010
	 * @param flowSection
	 * @param user
	 * @return s the preview including styles, or null if no preview is present
	 */
	public static String createRenderablePreview(Section<FlowchartType> flowSection, UserContext user) {
		// return createFlowchartRenderer(flowSection, user);
		String preview = extractPreview(flowSection);

		if (preview == null) {
			return null;
		}

		return createRenderablePreview(preview);

	}

	// experimental hack
	public static String createFlowchartRenderer(Section<FlowchartType> section, UserContext user) {
		String name = FlowchartType.getFlowchartName(section);
		String source = section.getOriginalText();

		String width = AbstractXMLType.getAttributeMapFor(section).get("width");
		String height = AbstractXMLType.getAttributeMapFor(section).get("height");

		KnowWEEnvironment knowWEEnv = KnowWEEnvironment.getInstance();
		String phraseString = "";

		List<Section<TermReference>> found = new LinkedList<Section<TermReference>>();
		Sections.findSuccessorsOfType(section, TermReference.class, found);

		StringBuilder builder = new StringBuilder();
		for (Section<TermReference> term : found) {
			if (builder.length() == 0) {
				builder.append(term.getText());
			}
			else {
				builder.append(",").append(term.getText());
			}
		}

		List<String> searchObjects = SearchInfoObjects.searchObjects(knowWEEnv, user.getWeb(),
				builder.toString(), null, 400);

		if (user.getWeb() == null) return "";

		SearchInfoObjects.searchObjects(knowWEEnv, user.getWeb(), phraseString, null, 200);

		String sourceID = name + "Source";
		String initScript = "<script>Flowchart.createFromXML('" + name + "', $('" + sourceID
				+ "')).setVisible(true);</script>\n";
		String result = "\n<div>"
				+
				"<link rel='stylesheet' type='text/css' href='cc/flow/flowchart.css'></link>"
				+
				"<link rel='stylesheet' type='text/css' href='cc/flow/guard.css'></link>"
				+
				"<link rel='stylesheet' type='text/css' href='cc/flow/node.css'></link>"
				+
				"<link rel='stylesheet' type='text/css' href='cc/flow/rule.css'></link>"
				+
				"<script src='cc/flow/builder.js' type='text/javascript'></script>"
				+
				"<script src='cc/kbinfo/kbinfo.js' type='text/javascript'></script>"
				+
				"<script src='cc/flow/renderExtensions.js' type='text/javascript'></script>"
				+
				"<script src='cc/kbinfo/extensions.js' type='text/javascript'></script>"
				+
				"<script src='cc/flow/flowchart.js' type='text/javascript'></script>"
				+
				"<script src='cc/flow/action.js' type='text/javascript'></script>" +
				"<script src='cc/flow/guard.js' type='text/javascript'></script>" +
				"<script src='cc/flow/node.js' type='text/javascript'></script>" +
				"<script src='cc/flow/rule.js' type='text/javascript'></script>" +
				"<script src='cc/flow/router.js' type='text/javascript'></script>\n" +
				"<xml id='" + sourceID + "' style ='display:none;'>" + source + "</xml>\n" +
				"<xml id='referredKBInfo' style='display:none;'>" +
				// jspHelper.getReferredInfoObjectsAsXML() +
				"</xml>" +
				"<div id='" + name + "' style='width:" + width + "px; height: " + height + "px;'>" +
				initScript +
				"</div>\n" +
				"</div>\n";
		return KnowWEUtils.maskHTML(result);

	}
}
