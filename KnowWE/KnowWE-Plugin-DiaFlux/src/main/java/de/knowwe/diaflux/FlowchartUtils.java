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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.plugin.Extension;
import de.d3web.plugin.JPFPluginManager;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
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
			"cc/flow/rule.css" };

	private static final HashMap<String, WeakHashMap<Flow, HashMap<String, Object>>> flowPropertyStore =
			new HashMap<String, WeakHashMap<Flow, HashMap<String, Object>>>();

	private FlowchartUtils() {
	}

	public static Object storeFlowProperty(Flow flow, String key, Object property) {
		return storeFlowProperty(Environment.DEFAULT_WEB, flow, key, property);
	}

	public static Object getFlowProperty(Flow flow, String key) {
		return getFlowProperty(Environment.DEFAULT_WEB, flow, key);
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
		return createFlowchartRenderer(flowSection, user, DIAFLUX_SCOPE);
	}

	private static String createFlowchartRenderer(Section<FlowchartType> section, UserContext user, String scope) {
		return createFlowchartRenderer(section, user, FlowchartType.getFlowchartName(section),
				scope, isInsertRessources(section));
	}

	public static String createFlowchartRenderer(Section<FlowchartType> section, UserContext user, String parentId, String scope, boolean insertRessources) {

		if (user.getWeb() == null) return "";

		parentId = escapeHtmlId(parentId);
		RenderResult result = prepareFlowchartRenderer(user, parentId, scope, insertRessources);
		result.appendHtml("<script>");
		result.appendHtml("if ($('" + parentId + "').getElements('.FlowchartGroup').length == 0) ");
		result.appendHtml("Flowchart.loadFlowchart('" + section.getID() + "', '" + parentId + "');");
		result.appendHtml("</script>");

		return result.toStringRaw();
	}

	/**
	 * Prepares a div to render a flowchart in later on using JS.
	 * 
	 * @created 20.03.2013
	 * @return
	 */
	public static RenderResult prepareFlowchartRenderer(UserContext user, String parentId, String scope, boolean insertRessources) {
		RenderResult result = new RenderResult(user);
		result.appendHtml("<div class='flowchartContainer'>");

		if (insertRessources) {
			insertDiafluxRessources(result, user);
			addDisplayPlugins(result, user, scope);
			result.append("\n");
		}

		result.appendHtml("<div id='" + parentId + "'>");
		result.appendHtml("</div></div>\n");
		return result;
	}

	public static String getParentID(Section<FlowchartType> section) {
		String flowName = FlowchartType.getFlowchartName(section);
		return escapeHtmlId(flowName);
	}

	private static String escapeHtmlId(String text) {
		text = Strings.encodeURL(text);
		text = text.replaceAll("%", "");
		return text;
	}

	public static void insertDiafluxRessources(RenderResult result, UserContext user) {

		for (String cssfile : CSS) {
			result.appendHtml("<link rel='stylesheet' type='text/css' href='" + cssfile
					+ "'></link>");
		}

		for (String jsfile : JS) {
			result.appendHtml("<script src='" + jsfile + "' type='text/javascript'></script>");
		}

		result.appendHtml("<data id='referredKBInfo' style='display:none;'>");
		result.appendHtml(JSPHelper.getReferrdInfoObjectsAsXML(user.getWeb()));
		result.appendHtml("</data>\n");
		result.appendHtml("<script>KBInfo._updateCache($('referredKBInfo'));</script>");
	}

	private static boolean isInsertRessources(Section<FlowchartType> section) {
		Section<DiaFluxType> diaFluxType = Sections.findAncestorOfType(section, DiaFluxType.class);

		// load JS and CSS only for first DiaFluxSection in page
		return Sections.findSuccessorsOfType(section.getArticle().getRootSection(),
				DiaFluxType.class).indexOf(
				diaFluxType) == 0;
	}

	/**
	 * 
	 * @created 26.05.2011
	 * @param result
	 * @param user
	 * @param section
	 */
	public static void addDisplayPlugins(RenderResult result, UserContext user, String scope) {
		Extension[] extensions = JPFPluginManager.getInstance().getExtensions(
				DiaFluxDisplayEnhancement.PLUGIN_ID, DiaFluxDisplayEnhancement.EXTENSION_POINT_ID);
		next: for (Extension extension : extensions) {

			List<String> scopes = extension.getParameters("scope");

			for (String extScope : scopes) {
				if (extScope.equalsIgnoreCase(scope)) {

					DiaFluxDisplayEnhancement enh = (DiaFluxDisplayEnhancement) extension.getNewInstance();

					if (!enh.activate(user, scope)) continue next;

					for (String script : enh.getScripts()) {
						result.appendHtml("<script src='" + script
								+ "' type='text/javascript'></script>");
					}

					for (String style : enh.getStylesheets()) {
						result.appendHtml("<link rel='stylesheet' type='text/css' href='" + style
								+ "'></link>");
					}
				}
			}

		}
	}

	// Just to load from old articles still containing preview. Should be
	// removed in the future
	public static String removePreview(String source) {
		int previewIndex = source.lastIndexOf("<preview");
		// remove preview
		if (previewIndex != -1) {
			source = source.substring(0, previewIndex) + "</flowchart>\r\n";
		}
		return source;
	}

	public static DiaFluxTrace getTrace(Session session) {
		return session.getSessionObject(DiaFluxTrace.SOURCE);

	}

	public static DiaFluxValueTrace getValueTrace(Session session) {
		return session.getSessionObject(DiaFluxValueTrace.SOURCE);

	}

	/**
	 * Returns the first flowchart in the given web with the provided name
	 * 
	 * @created 02.03.2011
	 * @param flowName The name of the flowchart
	 * @return
	 */
	public static Section<FlowchartType> findFlowchartSection(String web, String flowName) {
		ArticleManager manager = Environment.getInstance().getArticleManager(web);

		for (Iterator<Article> iterator = manager.getArticleIterator(); iterator.hasNext();) {
			Article article = iterator.next();
			List<Section<FlowchartType>> matches = new LinkedList<Section<FlowchartType>>();
			Sections.findSuccessorsOfType(article.getRootSection(), FlowchartType.class, matches);
			for (Section<FlowchartType> match : matches) {
				if (flowName.equalsIgnoreCase(FlowchartType.getFlowchartName(match))) {
					// simply return the first matching flowchart in we found in
					// any article
					return match;
				}
			}
		}
		// not match in no article
		return null;
	}

	/**
	 * Returns the corresponding Flowchart section of the given name, that is
	 * called by the provided section.
	 * 
	 * @created 11.04.2012
	 * @param section
	 * @param calledFlowName
	 * @return
	 */
	public static Section<FlowchartType> findFlowchartSection(
			Section<FlowchartType> section, String calledFlowName) {
		// get all articles compiling this flowchart that will be containing the
		// link
		PackageManager pkgManager =
				Environment.getInstance().getPackageManager(section.getWeb());
		Set<String> compilingArticles = pkgManager.getCompilingArticles(section);
		// get all packages that are compiled by these articles
		Collection<String> allPossiblePackageNames = new ArrayList<String>();
		for (String compilingArticle : compilingArticles) {
			allPossiblePackageNames.addAll(pkgManager.getCompiledPackages(compilingArticle));
		}

		// get all sections compiled by these articles
		Collection<Section<?>> allPossibleSections = new ArrayList<Section<?>>();
		for (String packageName : allPossiblePackageNames) {
			allPossibleSections.addAll(pkgManager.getSectionsOfPackage(packageName));
		}
		// look for flowcharts with the given name in these compiled sections
		Collection<Section<FlowchartType>> matches = new ArrayList<Section<FlowchartType>>();
		for (Section<?> possibleSection : allPossibleSections) {
			if (!(possibleSection.get() instanceof DiaFluxType)) continue;
			Section<FlowchartType> flowchart = Sections.findSuccessor(
					possibleSection, FlowchartType.class);
			if (flowchart == null) continue;
			String flowName = FlowchartType.getFlowchartName(flowchart);
			if (calledFlowName.equalsIgnoreCase(flowName)) {
				matches.add(flowchart);
			}
		}
		// only if there is exactly one match, we know it is the correct one
		if (matches.size() == 1) return matches.iterator().next();
		return null;
	}

	/**
	 * Return the first KB, that is compiling the given flowchart, of none, if
	 * it is not compiled by any article.
	 * 
	 * @created 11.04.2012
	 * @param s
	 * @return
	 */
	public static KnowledgeBase getKB(Section<DiaFluxType> s) {

		Iterator<Article> iterator = KnowWEUtils.getCompilingArticles(s).iterator();
		if (!iterator.hasNext()) return null;

		// TODO how to select right kb, if more than 1?
		Article article = iterator.next();

		return D3webUtils.getKnowledgeBase(s.getWeb(), article.getTitle());
	}

}
