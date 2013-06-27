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
import java.util.HashSet;
import java.util.List;

import de.d3web.plugin.Extension;
import de.d3web.plugin.JPFExtension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.RessourceLoader;
import de.knowwe.core.action.Action;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.IncrementalSectionizerModule;
import de.knowwe.core.kdom.parsing.SectionizerModule;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.taghandler.TagHandler;
import de.knowwe.core.utils.ScopeUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup.Annotation;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.defaultMarkup.UnknownAnnotationType;
import de.knowwe.knowRep.KnowledgeRepresentationHandler;

/**
 * Provides utilities methods for Plugins used in KnowWE
 * 
 * @author Jochen Reutelshöfer, Volker Belli & Markus Friedrich (denkbares GmbH)
 */
public class Plugins {

	public static final String SCOPE_ROOT = "root";
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
	public static final String EXTENDED_POINT_TERMINOLOGY = "Terminology";
	public static final String EXTENDED_POINT_COMPILESCRIPT = "CompileScript";
	public static final String EXTENDED_POINT_Renderer = "Renderer";
	public static final String EXTENDED_POINT_Annotation = "Annotation";
	public static final String EXTENDED_POINT_SEARCHPROVIDER = "SearchProvider";

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

	public static void addChildrenTypesToType(Type type) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type);
		extensions = ScopeUtils.getMatchingExtensions(extensions, type.getPathToRoot());
		for (int i = extensions.length - 1; i >= 0; i--) {
			Extension extension = extensions[i];
			Double priority = extension.getPriority();
			Type pluggedType = (Type) extension.getNewInstance();
			// only types which are not yet on the path to root are plugged to
			// prevent cycles
			if (!pathToRootContainsType(type.getPathToRoot(), pluggedType)) {
				//
				// if (priority != null) {
				// type.addChildType(priority.intValue(), pluggedType);
				// }
				// else {
				type.addChildType(0, pluggedType);
				// }
				pluggedType.setPathToRoot(type.getPathToRoot());
				addChildrenTypesToType(pluggedType);
			}

		}
	}

	/**
	 * 
	 * @created 22.06.2013
	 * @param pathToRoot
	 * @param pluggedType
	 */
	private static boolean pathToRootContainsType(Type[] pathToRoot, Type pluggedType) {
		for (Type type : pathToRoot) {
			if (type.getClass().equals(pluggedType.getClass())) {
				return true;
			}
		}
		return false;
	}

	public static void addSubtreeHandlersToType(Type type) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_SubtreeHandler);
		extensions = ScopeUtils.getMatchingExtensions(extensions, type.getPathToRoot());
		for (int i = 0; i < extensions.length; i++) {
			Extension extension = extensions[i];
			int priorityValue = Integer.parseInt(extension.getParameter("handlerpriority"));
			Priority priority = Priority.getPriority(priorityValue);
			type.addSubtreeHandler(priority, (SubtreeHandler<?>) extension.getSingleton());
		}
	}

	public static void addRendererToType(Type type) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Renderer);
		extensions = ScopeUtils.getMatchingExtensions(extensions, type.getPathToRoot());
		if (extensions.length >= 1) {
			type.setRenderer((Renderer) extensions[0].getSingleton());
		}
	}

	public static void addAnnotations(Type type) {
		if (type instanceof DefaultMarkupType) {

			DefaultMarkupType markupType = (DefaultMarkupType) type;
			Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
					EXTENDED_POINT_Annotation);
			extensions = ScopeUtils.getMatchingExtensions(extensions, type.getPathToRoot());

			for (Extension extension : extensions) {

				List<Type> childrenTypes = markupType.getChildrenTypes();
				markupType.getMarkup().addAnnotation(extension.getName());

				JPFExtension jpfEx = (JPFExtension) extension;
				Renderer pluggedRenderer = (Renderer) jpfEx.getNewInstance(jpfEx.getParameter("renderer"));

				markupType.getMarkup().addAnnotationRenderer(extension.getName(),
						pluggedRenderer);

				int i = 0;
				for (Type childTyp : childrenTypes) {

					if (childTyp.getClass().equals(UnknownAnnotationType.class)) {
						Annotation annotation = markupType.getMarkup().getAnnotation(
								extension.getName());
						type.addChildType(i, new AnnotationType(annotation));

						break;
					}
					i++;
				}

			}
		}

	}

	public static List<SectionizerModule> getSectionizerModules() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_SectionizerModule);
		List<SectionizerModule> sm = new ArrayList<SectionizerModule>();
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
	 * Returns a list of all plugged Annotations
	 * 
	 * @created 31/07/2012
	 * @return List of Annotations
	 */
	public static List<Annotation> getAnnotations() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Annotation);
		List<Annotation> ret = new ArrayList<Annotation>();
		for (Extension e : extensions) {
			ret.add((Annotation) e.getSingleton());
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
		addScripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Renderer));
		addScripts(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_KnowWEAction));
		for (String s : files) {
			RessourceLoader.getInstance().add(s, RessourceLoader.RESOURCE_SCRIPT);
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
		addCSS(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Renderer));
		addCSS(files, PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_KnowWEAction));
		for (String s : files) {
			RessourceLoader.getInstance().add(s, RessourceLoader.RESOURCE_STYLESHEET);
		}
	}

	private static void addScripts(List<String> files, Extension[] extensions) {
		for (Extension e : extensions) {
			List<String> scripts = e.getParameters("script");
			if (scripts != null) {
				for (String s : scripts) {
					if (!files.contains(s)) {
						files.add(s);
					}
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
