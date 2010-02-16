package de.knowwe.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.module.PageAppendHandler;
import de.d3web.we.taghandler.TagHandler;
/**
 * Provides utilities methods for Plugins used in KnowWE
 *
 * @author Jochen Reutelshöfer, Volker Belli & Markus Friedrich (denkbares GmbH)
 */
public class Plugins {
	public static final String SCOPE_ROOT = "root";
	public static final String SCOPE_GLOBAL = "global";
	public static final String EXTENDED_PLUGIN_ID = "KnowWEExtensionPoints";
	public static final String EXTENDED_POINT_KnowWEAction = "KnowWEAction";
	public static final String EXTENDED_POINT_KnowledgeRepresentationHandler = "KnowledgeRepresentationHandler";
	public static final String EXTENDED_POINT_KnowWEObjectType = "KnowWEObjectType";
	public static final String EXTENDED_POINT_TagHandler = "TagHandler";
	public static final String EXTENDED_POINT_PageAppendHandler = "PageAppendHandler";
	public static final String EXTENDED_POINT_Instantiation = "Instantiation";
	
	/**
	 * Returns all plugged Instantiations
	 * These are used to initialize plugins.
	 * @return List of all Instantiations
	 */
	public static List<Instantiation> getInstantiations() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_Instantiation);
		List<Instantiation> ret = new ArrayList<Instantiation>();
		for (Extension e: extensions) {
			ret.add((Instantiation) e.getSingleton());
		}
		return ret;
	}
	
	/**
	 * Returns a list of all plugged actions. Actions can be executed from the web. Usually
	 * be clicking on pregenerated links on the wiki pages.
	 */
	public static List<KnowWEAction> getKnowWEAction() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_KnowWEAction);
		List<KnowWEAction> ret = new ArrayList<KnowWEAction>();
		for (Extension e: extensions) {
			ret.add((KnowWEAction) e.getSingleton());
		}
		return ret;
	}

	/**
	 * Returns a list of all plugged KnowledgeRepresentationHandlers
	 * @return List of KnowledgeRepresentationHandlers
	 */
	public static List<KnowledgeRepresentationHandler> getKnowledgeRepresentationHandlers() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_KnowledgeRepresentationHandler);
		List<KnowledgeRepresentationHandler> ret = new ArrayList<KnowledgeRepresentationHandler>();
		for (Extension e: extensions) {
			ret.add((KnowledgeRepresentationHandler) e.getNewInstance());
		}
		return ret;
	}

	/**
	 * Returns a List of all KnowWEObjectTypes
	 * @return List of KnowWEObjectTypes
	 */
	public static List<KnowWEObjectType> getRootTypes() {
		return getKnowWEObjectTypes(SCOPE_ROOT);
	}

	/**
	 * Global types are always active at any level of the KDOM parsing process.
	 * Global types need to be TerminalTypes, thus cannot have children in the
	 * parse-tree.
	 * 
	 * DANGER: This can invade the markups/parsing of other modules!
	 * 
	 * @return List of KnowWEObjectTypes
	 */
	public static List<KnowWEObjectType> getGlobalTypes() {
		return getKnowWEObjectTypes(SCOPE_GLOBAL);
	}

	/**
	 * Returns all plugged KnowWE Object Types for the specified scope. The
	 * framework uses the scopes global and root, other scopes can be defined in
	 * plugins.
	 * 
	 * @param scope Scope of the KnowWEObjectTypes
	 * @return a List of KnowWEObjectTypes
	 */
	public static List<KnowWEObjectType> getKnowWEObjectTypes(String scope) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_KnowWEObjectType);
		List<KnowWEObjectType> ret = new ArrayList<KnowWEObjectType>();
		for (Extension e: extensions) {
			if (e.getParameter("scope").equals(scope)) {
				ret.add((KnowWEObjectType) e.getSingleton());
			}
		}
		return ret;
	}

	/**
	 * Returns a List of all plugged TagHandlers
	 * 
	 * COMMENT: Alternatively, those taghandlers can also be introduced
	 * separately using the taghandler.text file. There the class of the
	 * taghandler is listed and will be loaded on KnowWE initialization.
	 * 
	 * @return List of TagHandlers
	 */
	public static List<TagHandler> getTagHandlers() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_TagHandler);
		List<TagHandler> ret = new ArrayList<TagHandler>();
		for (Extension e: extensions) {
			ret.add((TagHandler) e.getSingleton());
		}
		return ret;
	}

	/**
	 * Returns a list of all plugged PageAppendHandlers.
	 * 
	 * These handlers allow a module to append some content to the wiki-page
	 * content. There are 2 kinds of appendHandlers one append content at top of
	 * the page, the other appends at the bottom
	 * 
	 * @return List of PageAppendHandlers
	 */
	public static List<PageAppendHandler> getPageAppendHandlers() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_PageAppendHandler);
		List<PageAppendHandler> ret = new ArrayList<PageAppendHandler>();
		for (Extension e: extensions) {
			ret.add((PageAppendHandler) e.getSingleton());
		}
		return ret;
	}

	/**
	 * Initializes the Javascript files
	 */
	public static void initJS() {
		HashSet<String> files = new HashSet<String>();
		getStripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_PageAppendHandler));
		getStripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_KnowWEObjectType));
		getStripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_TagHandler));
		for (String s: files) {
			KnowWERessourceLoader.getInstance().add(s, KnowWERessourceLoader.RESOURCE_SCRIPT);
		}
	}

	private static void getStripts(HashSet<String> files, Extension[] extensions) {
		for (Extension e: extensions) {
			List<String> scripts = e.getParameters("script");
			if (scripts!=null) {
				for (String s: scripts) {
					files.add(s);
				}
			}
		}
	}
}
