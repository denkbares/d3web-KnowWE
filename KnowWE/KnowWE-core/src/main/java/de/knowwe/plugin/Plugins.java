/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg / denkbares GmbH
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
package de.knowwe.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.KnowWERessourceLoader;
import de.knowwe.core.action.Action;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.IncrementalSectionizerModule;
import de.knowwe.core.kdom.parsing.SectionizerModule;
import de.knowwe.core.taghandler.TagHandler;
import de.knowwe.knowRep.KnowledgeRepresentationHandler;

/**
 * Provides utilities methods for Plugins used in KnowWE
 * 
 * @author Jochen Reutelshöfer, Volker Belli & Markus Friedrich (denkbares GmbH)
 */
public class Plugins {

	public static final String SCOPE_ROOT = "root";
	public static final String SCOPE_GLOBAL = "global";
	public static final String EXTENDED_PLUGIN_ID = "KnowWEExtensionPoints";
	public static final String EXTENDED_POINT_KnowWEAction = "Action";
	public static final String EXTENDED_POINT_KnowledgeRepresentationHandler = "KnowledgeRepresentationHandler";
	public static final String EXTENDED_POINT_Type = "Type";
	public static final String EXTENDED_POINT_SubtreeHandler = "SubtreeHandler";
	public static final String EXTENDED_POINT_ToolProvider = "ToolProvider";
	public static final String EXTENDED_POINT_TagHandler = "TagHandler";
	public static final String EXTENDED_POINT_PageAppendHandler = "PageAppendHandler";
	public static final String EXTENDED_POINT_Instantiation = "Instantiation";
	public static final String EXTENDED_POINT_SectionizerModule = "SectionizerModule";
	public static final String EXTENDED_POINT_SemanticCore = "SemanticCoreImpl";
	public static final String EXTENDED_POINT_EventListener = "EventListener";
	public static final String EXTENDED_POINT_SearchProvider = "SearchProvider";
	public static final String EXTENDED_POINT_TERMINOLOGY = "Terminology";
	public static final String EXTENDED_POINT_COMPILESCRIPT = "CompileScript";

	/**
	 * Returns all plugged Instantiations These are used to initialize plugins.
	 * 
	 * @return List of all Instantiations
	 */
	public static List<Instantiation> getInstantiations() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Instantiation);
		List<Instantiation> ret = new ArrayList<Instantiation>();
		for (Extension e : extensions) {
			ret.add((Instantiation) e.getSingleton());
		}
		return ret;
	}

	/**
	 * Returns a list of all plugged actions. Actions can be executed from the
	 * web. Usually be clicking on pregenerated links on the wiki pages.
	 */
	public static List<Action> getKnowWEAction() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_KnowWEAction);
		List<Action> ret = new ArrayList<Action>();
		for (Extension e : extensions) {
			ret.add((Action) e.getSingleton());
		}
		return ret;
	}

	/**
	 * Returns a list of all plugged KnowledgeRepresentationHandlers
	 * 
	 * @return List of KnowledgeRepresentationHandlers
	 */
	public static List<KnowledgeRepresentationHandler> getKnowledgeRepresentationHandlers() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_KnowledgeRepresentationHandler);
		List<KnowledgeRepresentationHandler> ret = new ArrayList<KnowledgeRepresentationHandler>();
		for (Extension e : extensions) {
			ret.add((KnowledgeRepresentationHandler) e.getNewInstance());
		}
		return ret;
	}

	/**
	 * Returns a List of all Types
	 * 
	 * @return List of Types
	 */
	public static List<Type> getRootTypes() {
		return getTypes(SCOPE_ROOT);
	}

	/**
	 * Global types are always active at any level of the KDOM parsing process.
	 * Global types need to be TerminalTypes, thus cannot have children in the
	 * parse-tree.
	 * 
	 * DANGER: This can invade the markups/parsing of other modules!
	 * 
	 * @return List of Types
	 */
	public static List<Type> getGlobalTypes() {
		return getTypes(SCOPE_GLOBAL);
	}

	/**
	 * Returns all plugged KnowWE Object Types for the specified scope. The
	 * framework uses the scopes global and root, other scopes can be defined in
	 * plugins.
	 * 
	 * @param scope Scope of the Types
	 * @return a List of Types
	 */
	public static List<Type> getTypes(String scope) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type);
		List<Type> ret = new ArrayList<Type>();
		for (Extension e : extensions) {
			if (e.getParameter("scope").equals(scope)) {
				ret.add((Type) e.getSingleton());
			}
		}
		return ret;
	}

	public static Collection<SectionizerModule> getSectionizerModules() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_SectionizerModule);
		Collection<SectionizerModule> sm = new ArrayList<SectionizerModule>();
		for (Extension e : extensions) {
			sm.add((SectionizerModule) e.getSingleton());
		}
		sm.add(new IncrementalSectionizerModule());
		return sm;
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
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_TagHandler);
		List<TagHandler> ret = new ArrayList<TagHandler>();
		for (Extension e : extensions) {
			ret.add((TagHandler) e.getSingleton());
		}
		return ret;
	}

	/**
	 * Returns a List of all plugged SemanticCores
	 * 
	 * @return List of SemanticCores
	 */
	// public static List<ISemanticCore> getSemanticCoreImpl() {
	// Extension[] extensions =
	// PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
	// EXTENDED_POINT_SemanticCore);
	// List<ISemanticCore> ret = new ArrayList<ISemanticCore>();
	// for (Extension e : extensions) {
	// ret.add((ISemanticCore) e.getSingleton());
	// }
	// return ret;
	// }

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
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_PageAppendHandler);
		List<PageAppendHandler> ret = new ArrayList<PageAppendHandler>();
		for (Extension e : extensions) {
			ret.add((PageAppendHandler) e.getSingleton());
		}
		return ret;
	}

	/**
	 * Initializes the Javascript files
	 */
	public static void initJS() {
		List<String> files = new ArrayList<String>();
		addScripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_PageAppendHandler));
		addScripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type));
		addScripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_TagHandler));
		addScripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_ToolProvider));
		for (String s : files) {
			KnowWERessourceLoader.getInstance().add(s, KnowWERessourceLoader.RESOURCE_SCRIPT);
		}
	}

	public static void initCSS() {
		HashSet<String> files = new HashSet<String>();
		addCSS(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_PageAppendHandler));
		addCSS(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type));
		addCSS(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_TagHandler));
		addCSS(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_ToolProvider));
		for (String s : files) {
			KnowWERessourceLoader.getInstance().add(s, KnowWERessourceLoader.RESOURCE_STYLESHEET);
		}
	}

	private static void addScripts(List<String> files, Extension[] extensions) {
		for (Extension e : extensions) {
			List<String> scripts = e.getParameters("script");
			if (scripts != null) {
				for (String s : scripts) {
					if (!files.contains(s))
						files.add(s);
				}
			}
		}
	}

	private static void addCSS(HashSet<String> files, Extension[] extensions) {
		for (Extension e : extensions) {
			List<String> scripts = e.getParameters("css");
			if (scripts != null) {
				for (String s : scripts) {
					files.add(s);
				}
			}
		}
	}

}
