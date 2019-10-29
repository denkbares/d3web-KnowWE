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
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import com.denkbares.plugin.Extension;
import com.denkbares.plugin.JPFPluginManager;
import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.DiaFluxElement;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.diaflux.DiaFluxTrace.State;
import de.knowwe.diaflux.kbinfo.JSPHelper;
import de.knowwe.diaflux.type.DiaFluxType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.kdom.xml.AbstractXMLType;

/**
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

	private static final HashMap<String, WeakHashMap<Flow, HashMap<String, Object>>> flowPropertyStore = new HashMap<>();

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
		return getFlowPropertyMapForWeb(web).computeIfAbsent(flow, k -> new HashMap<>());
	}

	private static WeakHashMap<Flow, HashMap<String, Object>> getFlowPropertyMapForWeb(String web) {
		return flowPropertyStore.computeIfAbsent(web, k -> new WeakHashMap<>());
	}

	public static String createFlowchartRenderer(Section<FlowchartType> section, UserContext user, boolean insertResources) {
		return createFlowchartRenderer(section, user, FlowchartType.getFlowchartName(section), DIAFLUX_SCOPE, insertResources);
	}

	public static String createFlowchartRenderer(Section<FlowchartType> section, UserContext user, String parentId, String scope, boolean insertResources) {

		if (user.getWeb() == null) return "";
		parentId = escapeHtmlId(parentId);

		RenderResult result = prepareFlowchartRenderer(section, parentId, user, scope, insertResources);
		result.appendHtml("<script>\n");
		result.appendHtml("if ($('" + parentId + "').getElements('.FlowchartGroup').length == 0)\n");
		appendLoadFlowchartScript(section, parentId, result);
		result.appendHtml("</script>");

		return result.toStringRaw();
	}

	private static void appendLoadFlowchartScript(Section<FlowchartType> section, String parentId, RenderResult result) {
		result.appendHtml("Flowchart.loadFlowchart('" + section.getID() + "', '" + parentId + "');\n");
	}

	/**
	 * Prepares a div to render a flowchart in later on using JS.
	 *
	 * @created 20.03.2013
	 */
	public static RenderResult prepareFlowchartRenderer(Section<FlowchartType> flowchartSection, String parentId, UserContext user, String scope, boolean insertRessources) {
		RenderResult result = new RenderResult(user);
		result.appendHtml("<div class='flowchartContainer' sectionid='" + flowchartSection.getID() + "'>");
		if (insertRessources) {
			insertDiaFluxResources(user, scope, result, flowchartSection);
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

	public static void insertDiaFluxResources(UserContext user, RenderResult result, Section<?>... flowcharts) {
		insertDiaFluxResources(user, DIAFLUX_SCOPE, result, flowcharts);
	}

	public static void insertDiaFluxResources(UserContext user, String scope, RenderResult result, Section<?>... flowcharts) {

		for (String cssFile : CSS) {
			result.appendHtml("<link rel='stylesheet' type='text/css' href='" + cssFile + "' />");
		}

		for (String jsFile : JS) {
			result.appendHtml("<script src='" + jsFile + "' type='text/javascript'></script>");
		}

		result.appendHtml("<div id='referredKBInfo' style='display:none;'>");
		result.appendHtml(JSPHelper.getReferredInfoObjectsAsXML(flowcharts));
		result.appendHtml("\n</div>\n");
		result.appendHtml("<script>KBInfo._updateCache($('referredKBInfo'));</script>");

		addDisplayPlugins(result, user, scope);
		result.append("\n");
	}

	public static void addDisplayPlugins(RenderResult result, UserContext user, String scope) {
		Extension[] extensions = JPFPluginManager.getInstance().getExtensions(
				DiaFluxDisplayEnhancement.PLUGIN_ID, DiaFluxDisplayEnhancement.EXTENSION_POINT_ID);
		next:
		for (Extension extension : extensions) {

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
								+ "' />");
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
	 * @param flowName The name of the flowchart
	 * @created 02.03.2011
	 */
	public static Section<FlowchartType> findFlowchartSection(String web, String flowName) {
		ArticleManager manager = Environment.getInstance().getArticleManager(web);

		for (Article article : manager.getArticles()) {
			List<Section<FlowchartType>> matches = new LinkedList<>();
			Sections.successors(article.getRootSection(), FlowchartType.class, matches);
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
	 * Returns the corresponding Flowchart section of the given name, that is called by the provided section.
	 *
	 * @created 11.04.2012
	 */
	public static Section<FlowchartType> findFlowchartSection(
			Section<FlowchartType> section, String calledFlowName) {
		// get all articles compiling this flowchart that will be containing the link
		Collection<D3webCompiler> compilers = Compilers.getCompilers(section, D3webCompiler.class);

		// get all sections compiled by these articles
		Collection<Section<FlowchartType>> matches = new ArrayList<>();
		outer:
		for (D3webCompiler compiler : compilers) {
			PackageManager packageManager = compiler.getPackageManager();
			Section<? extends PackageCompileType> compileSection = compiler.getCompileSection();
			Collection<Section<?>> sectionsOfPackage = packageManager.getSectionsOfPackage(compileSection
					.get().getPackagesToCompile(compileSection));
			for (Section<?> possibleSection : sectionsOfPackage) {
				if (!(possibleSection.get() instanceof DiaFluxType)) continue;
				Section<FlowchartType> flowchart = Sections.successor(
						possibleSection, FlowchartType.class);
				if (flowchart == null) continue;
				String flowName = FlowchartType.getFlowchartName(flowchart);
				if (calledFlowName.equalsIgnoreCase(flowName)) {
					matches.add(flowchart);
					if (matches.size() > 1) break outer;
				}
			}
		}

		// only if there is exactly one match, we know it is the correct one
		if (matches.size() == 1) return matches.iterator().next();
		return null;
	}

	/**
	 * Return the first KB, that is compiling the given flowchart, of none, if it is not compiled by any article.
	 *
	 * @created 11.04.2012
	 */
	public static KnowledgeBase getKB(Section<DiaFluxType> s) {
		return D3webUtils.getKnowledgeBase(s);
	}

	/**
	 * Finds the corresponding kb-Element of the supplied NodeType or EdgeType
	 * section.
	 *
	 * @created 02.05.2014
	 */
	public static DiaFluxElement findObject(Section<? extends AbstractXMLType> node, KnowledgeBase kb) {
		Section<FlowchartType> flowType = Sections.ancestor(node, FlowchartType.class);

		String id = AbstractXMLType.getAttributes(node).get("fcid");
		String flowName = FlowchartType.getFlowchartName(flowType);
		Flow flow = DiaFluxUtils.findFlow(kb, flowName);

		return DiaFluxUtils.findObjectById(flow, id);
	}

	/**
	 * Returns the state of the supplied NodeType or EdgeType section.
	 *
	 * @created 02.05.2014
	 */
	public static State getElementState(Section<? extends AbstractXMLType> node, UserContext user) {
		D3webCompiler compiler = Compilers.getCompiler(node, D3webCompiler.class);
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
		Session session = SessionProvider.getSession(user, kb);
		DiaFluxElement el = FlowchartUtils.findObject(node, kb);
		return DiaFluxTrace.getState(el, session);
	}

}
