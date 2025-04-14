/*
 * Copyright (C) 2022 denkbares GmbH. All rights reserved.
 */
package de.knowwe.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.collections.PriorityList;
import com.denkbares.plugin.Extension;
import com.denkbares.plugin.JPFExtension;
import com.denkbares.plugin.PluginManager;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import de.knowwe.core.Environment;
import de.knowwe.core.ResourceLoader;
import de.knowwe.core.action.Action;
import de.knowwe.core.append.PageAppendHandler;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TerminologyExtension;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.SectionizerModule;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.taghandler.TagHandler;
import de.knowwe.core.utils.ScopeUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup.Annotation;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.defaultMarkup.UnknownAnnotationType;
import de.knowwe.util.CredentialProvider;

/**
 * Provides utilities methods for Plugins used in KnowWE
 *
 * @author Jochen Reutelshöfer, Volker Belli & Markus Friedrich (denkbares GmbH)
 */
public class Plugins {
	private static final Logger LOGGER = LoggerFactory.getLogger(Plugins.class);

	public static final String EXTENDED_PLUGIN_ID = "KnowWEExtensionPoints";
	public static final String EXTENDED_POINT_KnowWEAction = "Action";
	public static final String EXTENDED_POINT_Type = "Type";
	public static final String EXTENDED_POINT_CompileScript = "CompileScript";
	public static final String EXTENDED_POINT_ToolProvider = "ToolProvider";
	public static final String EXTENDED_POINT_TagHandler = "TagHandler";
	public static final String EXTENDED_POINT_PageAppendHandler = "PageAppendHandler";
	public static final String EXTENDED_POINT_Instantiation = "Instantiation";
	public static final String EXTENDED_POINT_SectionizerModule = "SectionizerModule";
	public static final String EXTENDED_POINT_EventListener = "EventListener";
	public static final String EXTENDED_POINT_Terminology = "Terminology";
	public static final String EXTENDED_POINT_IncrementalCompileScript = "IncrementalCompileScript";
	public static final String EXTENDED_POINT_Renderer = "Renderer";
	public static final String EXTENDED_POINT_Annotation = "Annotation";
	public static final String EXTENDED_POINT_SearchProvider = "SearchProvider";
	public static final String EXTENDED_POINT_Compiler = "Compiler";
	public static final String EXTENDED_POINT_StatusProvider = "StatusProvider";
	public static final String EXTENDED_POINT_IncludeExporter = "IncludeExporter";
	public static final String EXTENDED_POINT_CredentialProvider = "CredentialProvider";

	private static <T> List<T> getSingletons(String point, Class<T> clazz) {
		PluginManager pm = PluginManager.getInstance();
		Extension[] extensions = pm.getExtensions(EXTENDED_PLUGIN_ID, point);
		List<T> result = new ArrayList<>();
		for (Extension e : extensions) {
			result.add(clazz.cast(e.getSingleton()));
		}
		return result;
	}

	/**
	 * Returns all plugged Instantiations These are used to initialize plugins.
	 *
	 * @return List of all Instantiations
	 */
	public static List<Instantiation> getInstantiations() {
		return getSingletons(EXTENDED_POINT_Instantiation, Instantiation.class);
	}

	/**
	 * Returns all plugged StatusProviders. These are used to find out, if the status of the wiki has changed.
	 *
	 * @return List of all StatusProvider
	 */
	public static List<StatusProvider> getStatusProviders() {
		return getSingletons(EXTENDED_POINT_StatusProvider, StatusProvider.class);
	}

	/**
	 * Returns all plugged Instantiations These are used to initialize plugins.
	 *
	 * @return List of all Instantiations
	 */
	public static PriorityList<Double, Compiler> getCompilers() {
		PluginManager pm = PluginManager.getInstance();
		Extension[] extensions = pm.getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_Compiler);
		PriorityList<Double, Compiler> result = new PriorityList<>(5d);
		for (Extension e : extensions) {
			result.add(e.getPriority(), (Compiler) e.getSingleton());
		}
		return result;
	}

	/**
	 * Returns a list of all plugged actions. Actions can be executed from the web. Usually be clicking on
	 * pre-generated links on the wiki pages.
	 */
	public static List<Action> getKnowWEAction() {
		return getSingletons(EXTENDED_POINT_KnowWEAction, Action.class);
	}

	/**
	 * Get the action for the given action name.
	 *
	 * @param actionName the name of the action
	 * @return the action for the given name
	 */
	public static <T extends Action> @Nullable T getAction(String actionName) {
		PluginManager manager = PluginManager.getInstance();
		Extension[] extensions = manager.getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_KnowWEAction);
		for (Extension e : extensions) {
			if (actionName.equals(e.getID()) || actionName.equals(e.getName())) {
				//noinspection unchecked
				return ((T) e.getSingleton());
			}
		}
		return null;
	}

	public static void addChildrenTypesToType(Type type, Type[] path) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type);
		for (Extension extension : ScopeUtils.getMatchingExtensions(extensions, path)) {
			double priority = extension.getPriority();
			Type pluggedType = (Type) extension.getNewInstance();
			type.addChildType(priority, pluggedType);

			// add documentation for default markups
			if (pluggedType instanceof DefaultMarkupType) {
				DefaultMarkup markup = ((DefaultMarkupType) pluggedType).getMarkup();
				if (Strings.isBlank(markup.getDocumentation())) {
					markup.setDocumentation(extension.getParameter("description"));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void addCompileScriptsToType(Type type, Type[] path) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_CompileScript);
		for (Extension extension : ScopeUtils.getMatchingExtensions(extensions, path)) {
			int priorityValue = Integer.parseInt(extension.getParameter("compilepriority"));
			Priority priority = Priority.getPriority(priorityValue);
			if (type instanceof AbstractType) {
				((AbstractType) type).addCompileScript(priority,
						(CompileScript<Compiler, AbstractType>) extension.getSingleton());
			}
			else {
				LOGGER.warn("Tried to plug CompileScript '"
							+ extension.getSingleton().getClass().getSimpleName()
							+ "' into an type '" + type.getClass().getSimpleName()
							+ "' which is not an AbstractType");
			}
		}
	}

	/**
	 * Checks whether the priorities between plugged types is deterministic. Scans through all type extensions for
	 * extensions that have equal scope AND equal priority. If such case is found a warning is logged. The scope 'root'
	 * is omitted from the test.
	 * <p>
	 * <p>
	 * Explanation: This will to problems if the SectionFinders of these types are not disjoint, for instance both
	 * using AllTextFinder(). That case can lead to indeterministic parsing results when KnowWE is launched with
	 * slightly different configurations!
	 * <p>
	 * Action to take: If the warning appears, it is recommend to adjust the priority of the types to make them
	 * distinct according to the intended order. However, if the SectionFinders are disjoint in their acceptance
	 * behaviour there is no danger.
	 * <p>
	 * Note: This test is incomplete as using Path expression with wildcards, scopes can overlap even though their
	 * string respresentation is not equal.
	 */
	public static void checkTypePriorityClarity() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type);
		MultiMap<Pair<String, Double>, Extension> map = new DefaultMultiMap<>();
		for (Extension type : extensions) {
			List<String> scopes = type.getParameters("scope");
			for (String scope : scopes) {
				if (!"root".equals(scope)) {
					map.put(new Pair<>(scope, type.getPriority()), type);
				}
			}
		}
		for (Pair<String, Double> pair : map.keySet()) {
			Set<Extension> setOfTypesWithEqualScopeAndPriority = map.getValues(pair);
			if (setOfTypesWithEqualScopeAndPriority.size() > 1) {
				StringBuilder message = new StringBuilder();
				message.append("Found types with equal scope AND priority. ")
						.append("\nThis is a plugin configuration error and can produce nondeterministic behavior! ")
						.append("\nScope: ").append(pair.getA()).append(". ")
						.append("\nPriority: ").append(pair.getB()).append(". ")
						.append("\nTypes:\n");
				for (Extension extension : setOfTypesWithEqualScopeAndPriority) {
					message.append("\n\tId: ").append(extension.getID())
							.append(" - Name: ").append(extension.getName());
				}
				LOGGER.error(message.toString());
			}
		}
	}

	public static void addRendererToType(Type type, Type[] path) {
		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Renderer);
		Extension match = ScopeUtils.getMatchingExtension(extensions, path);
		if (match != null) {
			if (type instanceof AbstractType) {
				Renderer currentRenderer = type.getRenderer();
				Environment.getInstance().addRendererForType(type, currentRenderer);
				((AbstractType) type).setRenderer((Renderer) match.getSingleton());
			}
			else {
				throw new ClassCastException(
						"renderer can only be plugged to type instances of 'AbstractType', but not to "
						+ type.getClass().getName());
			}
		}
	}

	public static void addAnnotations(Type type, Type[] path) {
		if (type instanceof DefaultMarkupType markupType) {

			List<Type> childrenTypes = markupType.getChildrenTypes();
			DefaultMarkup markup = markupType.getMarkup();

			Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
					EXTENDED_POINT_Annotation);
			for (Extension extension : ScopeUtils.getMatchingExtensions(extensions, path)) {

				// add the annotation itself to the markup
				String regex = extension.getParameter("regex");
				String name = extension.getName();
				if (Strings.isBlank(regex)) {
					markup.addAnnotation(name, false);
				}
				else {
					markup.addAnnotation(name, false, regex);
				}

				// add the documentation from the plugin definition
				markup.getAnnotation(name).setDocumentation(extension.getParameter("description"));

				// add children type(s) defined as class attribute(s)
				for (String subTypeClass : extension.getParameters("class")) {
					Type subType = (Type) getNewInstance(extension, subTypeClass);
					markup.addAnnotationContentType(name, subType);
				}

				// set renderer for the annotation
				String renderer = extension.getParameter("renderer");
				if (!Strings.isBlank(renderer)) {
					Renderer pluggedRenderer = (Renderer) getNewInstance(extension, renderer);
					markup.addAnnotationRenderer(name, pluggedRenderer);
				}

				for (Type childTyp : childrenTypes) {
					if (childTyp.getClass().equals(UnknownAnnotationType.class)) {
						Annotation annotation = markup.getAnnotation(name);
						type.addChildType(new AnnotationType(annotation));
						break;
					}
				}
			}
		}
	}

	private static Object getNewInstance(Extension extension, String clazz) {
		return ((JPFExtension) extension).getNewInstance(clazz);
	}

	public static List<SectionizerModule> getSectionizerModules() {
		return getSingletons(EXTENDED_POINT_SectionizerModule, SectionizerModule.class);
	}

	/**
	 * Returns a List of all plugged TagHandlers
	 * <p>
	 * COMMENT: Alternatively, those tag-handlers can also be introduced separately using the "taghandler.text" file.
	 * There the class of the tag-handler is listed and will be loaded on KnowWE initialization.
	 *
	 * @return List of TagHandlers
	 */
	public static List<TagHandler> getTagHandlers() {
		return getSingletons(EXTENDED_POINT_TagHandler, TagHandler.class);
	}

	/**
	 * Returns a list of all plugged PageAppendHandlers.
	 * <p>
	 * These handlers allow a module to append some content to the wiki-page content. There are 2 kinds of
	 * appendHandlers one append content at top of the page, the other appends at the bottom
	 *
	 * @return List of PageAppendHandlers
	 */
	public static List<PageAppendHandler> getPageAppendHandlers() {
		return getSingletons(EXTENDED_POINT_PageAppendHandler, PageAppendHandler.class);
	}

	/**
	 * Returns a list of all plugged Annotations
	 *
	 * @return List of Annotations
	 * @created 31/07/2012
	 */
	public static List<Annotation> getAnnotations() {
		return getSingletons(EXTENDED_POINT_Annotation, Annotation.class);
	}

	/**
	 * Returns a priority list of credential providers
	 *
	 * @return priority list of credential providers
	 */
	public static PriorityList<Double, CredentialProvider> getCredentialProviders() {
		PluginManager pm = PluginManager.getInstance();
		Extension[] extensions = pm.getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_CredentialProvider);
		PriorityList<Double, CredentialProvider> result = new PriorityList<>(5d);
		for (Extension e : extensions) {
			result.add(e.getPriority(), (CredentialProvider) e.getSingleton());
		}
		return result;
	}

	/**
	 * Initializes the Javascript files
	 */
	public static void initJS() {
		PriorityList<Double, Pair<String, String>> files = new PriorityList<>(5.0);
		addScripts(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_PageAppendHandler), files);
		addScripts(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type), files);
		addScripts(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_TagHandler), files);
		addScripts(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_ToolProvider), files);
		addScripts(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Renderer), files);
		addScripts(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_KnowWEAction), files);
		for (Pair<String, String> s : files) {
			ResourceLoader.getInstance().add(s.getA(), ResourceLoader.Type.script, s.getB());
		}
	}

	/**
	 * Initializes the Javascript module files
	 */
	public static void initJsModules() {
		PriorityList<Double, Pair<String, String>> files = new PriorityList<>(5.0);
		addModules(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_PageAppendHandler), files);
		addModules(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type), files);
		addModules(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_TagHandler), files);
		addModules(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_ToolProvider), files);
		addModules(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Renderer), files);
		addModules(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_KnowWEAction), files);
		for (Pair<String, String> s : files) {
			ResourceLoader.getInstance().add(s.getA(), ResourceLoader.Type.module, s.getB());
		}
	}

	public static void initCSS() {
		PriorityList<Double, Pair<String, String>> files = new PriorityList<>(5.0);
		addCSS(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_PageAppendHandler), files);
		addCSS(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Type), files);
		addCSS(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_TagHandler), files);
		addCSS(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_ToolProvider), files);
		addCSS(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_Renderer), files);
		addCSS(PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				EXTENDED_POINT_KnowWEAction), files);
		for (Pair<String, String> s : files) {
			ResourceLoader.getInstance().addFirst(s.getA(), ResourceLoader.Type.stylesheet, s.getB());
		}
	}

	public static void initResources(Extension[] extensions) {
		PriorityList<Double, Pair<String, String>> cssFiles = new PriorityList<>(5.0);
		addCSS(extensions, cssFiles);
		for (Pair<String, String> s : cssFiles) {
			ResourceLoader.getInstance().addFirst(s.getA(), ResourceLoader.Type.stylesheet, s.getB());
		}
		PriorityList<Double, Pair<String, String>> jsFiles = new PriorityList<>(5.0);
		addScripts(extensions, jsFiles);
		for (Pair<String, String> filename : jsFiles) {
			ResourceLoader.getInstance().add(filename.getA(), ResourceLoader.Type.script, filename.getB());
		}
	}

	private static void addScripts(Extension[] extensions, PriorityList<Double, Pair<String, String>> filesCollector) {
		addParameter(extensions, filesCollector, "script");
	}

	private static void addParameter(Extension[] extensions, PriorityList<Double, Pair<String, String>> filesCollector, String parameterName) {
		for (Extension e : extensions) {
			double priority = e.getPriority();
			List<String> scripts = e.getParameters(parameterName);
			String version = getVersion(e);
			if (scripts != null) {
				for (String script : scripts) {
					Pair<String, String> scriptWithVersionPair = new Pair<>(script, version);
					if (!filesCollector.contains(scriptWithVersionPair)) {
						filesCollector.add(priority, scriptWithVersionPair);
					}
				}
			}
		}
	}

	private static String getVersion(Extension extension) {
		String version = extension.getVersion();
		if ("1.0".equals(version) || version == null) { // probably not a maintained version (1.0 is the default), so get jar modified date as version instead
			Class<?> instanceClass;
			try {
				instanceClass = extension.getInstanceClass();
			} catch (Throwable e) {
				LOGGER.warn("Could not get extension class for extension {}", extension.getName());
				instanceClass = Plugins.class;
			}
			version = ResourceLoader.getJarVersion(instanceClass);
		}
		return version;
	}

	private static void addModules(Extension[] extensions, PriorityList<Double, Pair<String, String>> filesCollector) {
		addParameter(extensions, filesCollector, "module");
	}

	private static void addCSS(Extension[] extensions, PriorityList<Double, Pair<String, String>> filesCollector) {
		addParameter(extensions, filesCollector, "css");
	}

	/**
	 * Get the terminology extension (reserved terms) of the given compiler
	 *
	 * @param compiler the compiler to get the reserved terms for
	 * @return the reserved terms for the compiler
	 */
	public static TerminologyExtension getTerminologyExtension(Compiler compiler) {
		TerminologyExtension terminologyExtension = null;
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				Plugins.EXTENDED_PLUGIN_ID,
				Plugins.EXTENDED_POINT_Terminology);
		for (Extension extension : extensions) {
			Object o = extension.getSingleton();
			if (o instanceof TerminologyExtension) {
				Class<? extends Compiler> compilerClass = ((TerminologyExtension) o).getCompilerClass();
				if (compilerClass.isInstance(compiler)) {
					terminologyExtension = (TerminologyExtension) o;
				}
			}
		}
		return terminologyExtension;
	}
}
