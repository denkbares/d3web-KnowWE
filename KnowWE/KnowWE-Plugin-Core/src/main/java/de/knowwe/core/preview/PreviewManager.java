package de.knowwe.core.preview;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.utils.Scope;
import de.knowwe.plugin.Plugins;

/**
 * Class managing the plugins to show previews of section. The registered
 * preview handlers define a scope (or a number of scopes) which items they are
 * capable to render and a renderer that shall be used to render the scope.
 * <p>
 * When rendering a preview for a specific section, the an ancestor section is
 * searched which matches the scope. After that the ancestor is rendered instead
 * of only the section. By rendering the ancestor, also the desired section will
 * be included in the rendering.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 16.08.2013
 */
public class PreviewManager {

	private final Map<Scope, PreviewRenderer> previewRenderers = new LinkedHashMap<Scope, PreviewRenderer>();

	private static final PreviewManager INSTANCE = new PreviewManager();

	private PreviewManager() {
		initPreviewRenderers();
	}

	public static PreviewManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the closest ancestor section to the specified section that has a
	 * preview renderer available. If the returned section will be rendered, the
	 * specified section will be included in the preview. The method will return
	 * null if there is no preview renderer available to the specified section.
	 * 
	 * @created 16.08.2013
	 * @param section the section to be included in the preview
	 * @return the section that can be rendered as a preview to show the
	 *         specified section, or null if there is no such section
	 */
	public Section<?> getPreviewAncestor(Section<?> section) {
		Section<?> previewSection = section;
		while (previewSection != null) {
			for (Scope scope : previewRenderers.keySet()) {
				if (scope.matches(previewSection)) {
					// we found the closest section
					return previewSection;
				}
			}
			previewSection = previewSection.getParent();
		}
		return null;
	}

	/**
	 * Returns the renderer for a specific preview section. Please note that for
	 * this method a section must be specified that is capable to be directly
	 * rendered as a preview. Therefore the specified section is usually a
	 * section returned by the method {@link #getPreviewAncestor(Section)}. If
	 * no renderer is found, null is returned.
	 * 
	 * @created 16.08.2013
	 * @param section the (closest preview ancestor) section that shall be
	 *        rendered
	 * @return the renderer for the section
	 */
	public PreviewRenderer getPreviewRenderer(Section<?> section) {
		for (Scope scope : previewRenderers.keySet()) {
			if (scope.matches(section)) {
				// we found the closest section
				return previewRenderers.get(scope);
			}
		}
		return null;
	}

	private void initPreviewRenderers() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				"KnowWE-Plugin-Core",
				"PreviewRenderer");
		for (Extension extension : extensions) {
			// get the renderer
			Object object = extension.getSingleton();
			if (object instanceof PreviewRenderer) {
				// add the renderer for every scope
				List<String> scopes = extension.getParameters("scope");
				for (String scopeString : scopes) {
					Scope scope = Scope.getScope(scopeString);
					// but take care not overwrite an existing scope,
					// because it has the higher priority
					if (!previewRenderers.containsKey(scope)) {
						previewRenderers.put(scope, (PreviewRenderer) object);
					}
				}
			}
			else {
				Logger.getLogger(getClass().getName()).warning(
						"extension of class '" + object.getClass().getName() +
								"' is not of the expected type " + Renderer.class.getName());
			}
		}
		Plugins.initResources(extensions);
	}

}
