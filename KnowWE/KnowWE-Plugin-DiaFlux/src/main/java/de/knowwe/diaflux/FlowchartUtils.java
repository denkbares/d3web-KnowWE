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
package de.knowwe.diaflux;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import de.d3web.diaFlux.flow.Flow;
import de.d3web.plugin.Extension;
import de.d3web.plugin.JPFPluginManager;
import de.knowwe.core.KnowWEArticleManager;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.kbinfo.JSPHelper;
import de.knowwe.diaflux.type.DiaFluxType;
import de.knowwe.diaflux.type.FlowchartType;

/**
 * 
 * @author Reinhard Hatko
 * @created 09.12.2009
 */
public class FlowchartUtils {

	public static final String DIAFLUX_SCOPE = "diaflux";
	public static final String[] JS = new String[] {
			"cc/flow/builder.js", "cc/kbinfo/kbinfo.js", "cc/flow/renderExtensions.js",
			"cc/kbinfo/extensions.js", "cc/flow/flowchart.js",
			"cc/flow/action.js", "cc/flow/guard.js", "cc/flow/node.js", "cc/flow/rule.js",
			"cc/flow/router.js", "cc/flow/highlight.js" };

	public static final String[] CSS = new String[] {
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


	public static String createFlowchartRenderer(Section<FlowchartType> flowSection, UserContext user) {
		return KnowWEUtils.maskHTML(createFlowchartRenderer(flowSection, user, DIAFLUX_SCOPE));
	}

	private static String createFlowchartRenderer(Section<FlowchartType> section, UserContext user, String scope) {
		String parent = FlowchartType.getFlowchartName(section);
		return createFlowchartRenderer(section, user, parent, scope, isInsertRessources(section));
	}

	public static String createFlowchartRenderer(Section<FlowchartType> section, UserContext user, String parent, String scope, boolean insertRessources) {

		if (user.getWeb() == null) return "";

		StringBuilder result = new StringBuilder("<div>");

		if (insertRessources) {
			insertDiafluxRessources(result, user);
			addDisplayPlugins(result, user, scope);
		}

		parent = escapeHtmlId(parent);

		result.append("\n");
		result.append("<div id='" + parent + "'>");
		result.append("<script>");
		result.append("Flowchart.loadFlowchart('" + section.getID() + "', '" + parent + "');");
		result.append("</script></div></div>\n");

		return result.toString();

	}

	public static String escapeHtmlId(String text) {
		try {
			text = URLEncoder.encode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// seems as like we use the not encoded version anyway
		}
		text = text.replaceAll("%", "");
		return text;
	}

	public static void insertDiafluxRessources(StringBuilder result, UserContext user) {

		for (String cssfile : CSS) {
			result.append("<link rel='stylesheet' type='text/css' href='" + cssfile + "'></link>");
		}

		for (String jsfile : JS) {
			result.append("<script src='" + jsfile + "' type='text/javascript'></script>");
		}

		result.append("<data id='referredKBInfo' style='display:none;'>");
		result.append(JSPHelper.getReferrdInfoObjectsAsXML(user.getWeb()));
		result.append("</data>\n");
		result.append("<script>KBInfo._updateCache($('referredKBInfo'));</script>");
	}

	private static boolean isInsertRessources(Section<FlowchartType> section) {
		Section<DiaFluxType> diaFluxType = Sections.findAncestorOfType(section, DiaFluxType.class);

		// load JS and CSS only for first DiaFluxSection in page
		return Sections.findSuccessorsOfType(section.getArticle().getSection(), DiaFluxType.class).indexOf(
				diaFluxType) == 0;
	}

	/**
	 * 
	 * @created 26.05.2011
	 * @param result
	 * @param user
	 * @param section
	 */
	public static void addDisplayPlugins(StringBuilder result, UserContext user, String scope) {
		Extension[] extensions = JPFPluginManager.getInstance().getExtensions(
				DiaFluxDisplayEnhancement.PLUGIN_ID, DiaFluxDisplayEnhancement.EXTENSION_POINT_ID);
		next: for (Extension extension : extensions) {

			List<String> scopes = extension.getParameters("scope");

			for (String extScope : scopes) {
				if (extScope.equalsIgnoreCase(scope)) {

					DiaFluxDisplayEnhancement enh = (DiaFluxDisplayEnhancement) extension.getNewInstance();

					if (!enh.activate(user, scope)) continue next;

					for (String script : enh.getScripts()) {
						result.append("<script src='" + script
								+ "' type='text/javascript'></script>");
					}

					for (String style : enh.getStylesheets()) {
						result.append("<link rel='stylesheet' type='text/css' href='" + style
								+ "'></link>");
					}
				}
			}

		}
	}

	//Just to load from old articles still containing preview. Should be removed in the future
	public static String getFlowSourceWithoutPreview(Section<FlowchartType> section) {
		String source = section.getText();
		int previewIndex = source.lastIndexOf("<preview");
		// remove preview
		if (previewIndex != -1) {
			source = source.substring(0, previewIndex) + "</flowchart>\r\n";
		}
		return source;
	}

	/**
	 * 
	 * @created 02.03.2011
	 * @param calledFlowName
	 * @return
	 */
	public static Section<FlowchartType> findFlowchartSection(String web, String calledFlowName) {
		KnowWEArticleManager manager = KnowWEEnvironment.getInstance().getArticleManager(web);

		for (Iterator<KnowWEArticle> iterator = manager.getArticleIterator(); iterator.hasNext();) {
			KnowWEArticle article = iterator.next();
			List<Section<FlowchartType>> matches = new LinkedList<Section<FlowchartType>>();
			Sections.findSuccessorsOfType(article.getSection(), FlowchartType.class, matches);
			for (Section<FlowchartType> match : matches) {
				String flowName = FlowchartType.getFlowchartName(match);
				if (calledFlowName.equalsIgnoreCase(flowName)) {
					// simply return the first matching flowchart in we found in
					// any article
					return match;
				}
			}
		}
		// not match in no article
		return null;
	}
}
