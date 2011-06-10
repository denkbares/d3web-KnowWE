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

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.diaFlux.flow.Flow;
import de.d3web.plugin.Extension;
import de.d3web.plugin.JPFPluginManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.flow.kbinfo.JSPHelper;
import de.d3web.we.flow.type.FlowchartPreviewContentType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
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
	public static final String[] JS = new String[] {
			"cc/flow/builder.js", "cc/kbinfo/kbinfo.js", "cc/flow/renderExtensions.js",
			"cc/kbinfo/extensions.js", "cc/flow/flowchart.js",
			"cc/flow/action.js", "cc/flow/guard.js", "cc/flow/node.js", "cc/flow/rule.js",
			"cc/flow/router.js" };

	private static final String[] CSS = new String[] {
			"cc/flow/flowchart.css", "cc/flow/guard.css", "cc/flow/node.css",
			"cc/flow/rule.css", "cc/flow/rendering.css" };

	private static final HashMap<String, WeakHashMap<Flow, HashMap<String, Object>>> flowPropertyStore =
			new HashMap<String, WeakHashMap<Flow, HashMap<String, Object>>>();

	private FlowchartUtils() {
	}

	public static Object storeFlowProperty(Flow flow, String key, Object property) {
		return storeFlowProperty(KnowWEEnvironment.DEFAULT_WEB, flow, key, property);
	}

	public static Object getFlowProperty(Flow flow, String key) {
		return getFlowProperty(KnowWEEnvironment.DEFAULT_WEB, flow, key);
	}

	public static Object storeFlowProperty(String web, Flow flow, String key, Object property) {
		return getPropertyMapForFlow(web, flow).put(key, property);
	}

	public static Object getFlowProperty(String web, Flow flow, String key) {
		return getPropertyMapForFlow(web, flow).get(key);
	}

	private static HashMap<String, Object> getPropertyMapForFlow(String web, Flow flow) {
		HashMap<String, Object> propertyMapForFlow = getFlowPropertyMapForWeb(web).get(flow);
		if (propertyMapForFlow == null) {
			propertyMapForFlow = new HashMap<String, Object>();
			getFlowPropertyMapForWeb(web).put(flow, propertyMapForFlow);
		}
		return propertyMapForFlow;
	}

	private static WeakHashMap<Flow, HashMap<String, Object>> getFlowPropertyMapForWeb(String web) {
		WeakHashMap<Flow, HashMap<String, Object>> webMap = flowPropertyStore.get(web);
		if (webMap == null) {
			webMap = new WeakHashMap<Flow, HashMap<String, Object>>();
			flowPropertyStore.put(web, webMap);
		}
		return webMap;
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
		String web = user.getWeb();

		String width = AbstractXMLType.getAttributeMapFor(section).get("width");
		String height = AbstractXMLType.getAttributeMapFor(section).get("height");

		if (user.getWeb() == null) return "";

		String sourceID = name + "Source";
		String initScript = "<script>" +
				"KBInfo._updateCache($('referredKBInfo'));" +
				"Flowchart.createFromXML('" + name + "', $('" + sourceID
				+ "')).setVisible(true);\n" +
						"</script>\n";

		StringBuilder result = new StringBuilder("\n<div>");

		for (String cssfile : CSS) {
			result.append("<link rel='stylesheet' type='text/css' href='" + cssfile + "'></link>");
		}

		for (String jsfile : JS) {
			result.append("<script src='" + jsfile + "' type='text/javascript'></script>");
		}

		addDisplayPlugins(result);

		result.append("\n");
		result.append("<xml id='" + sourceID + "' style ='display:none;'>" + source + "</xml>\n");
		result.append("<xml id='referredKBInfo' style='display:none;'>");
		result.append(JSPHelper.getReferrdInfoObjectsAsXML(web));
		result.append("</xml>");
		result.append("<div id='" + name + "' style='width:" + width + "px; height: " + height
				+ "px;'>");
		result.append(initScript);
		result.append("</div>\n</div>\n");

		return KnowWEUtils.maskHTML(result.toString());

	}

	/**
	 * 
	 * @created 26.05.2011
	 * @param result
	 */
	public static void addDisplayPlugins(StringBuilder result) {
		Extension[] extensions = JPFPluginManager.getInstance().getExtensions(
				DiaFluxDisplayEnhancement.PLUGIN_ID, DiaFluxDisplayEnhancement.EXTENSION_POINT_ID);
		for (Extension extension : extensions) {
			DiaFluxDisplayEnhancement enh = (DiaFluxDisplayEnhancement) extension.getNewInstance();

			for (String script : enh.getScripts()) {
				result.append("<script src='" + script + "' type='text/javascript'></script>");
			}

			for (String style : enh.getStylesheets()) {
				result.append("<link rel='stylesheet' type='text/css' href='" + style + "'></link>");
			}

		}
	}
}
