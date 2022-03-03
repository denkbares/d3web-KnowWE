/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.core.preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.plugin.Extension;
import com.denkbares.plugin.PluginManager;
import de.knowwe.core.kdom.parsing.KDOMPositionComparator;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.utils.Scope;
import de.knowwe.plugin.Plugins;

/**
 * Class managing the plugins to show previews of section. The registered preview handlers define a scope (or a number
 * of scopes) which items they are capable to render and a renderer that shall be used to render the scope.
 * <p/>
 * When rendering a preview for a specific section, the an ancestor section is searched which matches the scope. After
 * that the ancestor is rendered instead of only the section. By rendering the ancestor, also the desired section will
 * be included in the rendering.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 16.08.2013
 */
public class PreviewManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreviewManager.class);

	private final Map<Scope, TreeMap<Integer, PreviewRenderer>> previewRenderers = new LinkedHashMap<>();

	private static final PreviewManager INSTANCE = new PreviewManager();

	private PreviewManager() {
		initPreviewRenderers();
	}

	public static PreviewManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the closest ancestor section to the specified section that has a preview renderer available. If the
	 * returned section will be rendered, the specified section will be included in the preview. The method will return
	 * null if there is no preview renderer available to the specified section.
	 *
	 * @param section the section to be included in the preview
	 * @return the section that can be rendered as a preview to show the specified section, or null if there is no such
	 * section
	 * @created 16.08.2013
	 */
	public Section<?> getPreviewAncestor(Section<?> section) {
		Section<?> previewSection = section;
		while (previewSection != null) {
			for (Scope scope : previewRenderers.keySet()) {
				if (scope.matches(previewSection)) {
					PreviewRenderer previewRenderer = previewRenderers.get(scope).firstEntry().getValue();
					if (previewRenderer.matches(section) &&
							previewRenderer.isPreviewAncestor(previewSection, section)) {
						return previewSection;
					}
				}
			}
			previewSection = previewSection.getParent();
		}
		return null;
	}

	/**
	 * Returns the renderer for a specific preview section. Please note that for this method a section must be specified
	 * that is capable to be directly rendered as a preview. Therefore the specified section is usually a section
	 * returned by the method {@link #getPreviewAncestor(Section)}. If no renderer is found, null is returned.
	 *
	 * @param section the (closest preview ancestor) section that shall be rendered
	 * @return the renderer for the section
	 * @created 16.08.2013
	 */
	public PreviewRenderer getPreviewRenderer(Section<?> section) {
		for (Scope scope : previewRenderers.keySet()) {
			if (scope.matches(section)) {
				// we found the closest section
				PreviewRenderer previewRenderer = previewRenderers.get(scope).firstEntry().getValue();
				if (previewRenderer.matches(section)) return previewRenderer;
			}
		}
		return null;
	}

	private void initPreviewRenderers() {
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				"KnowWEExtensionPoints",
				"PreviewRenderer");
		for (Extension extension : extensions) {
			// get the renderer
			Object object = extension.getSingleton();
			if (object instanceof PreviewRenderer) {
				// add the renderer for every scope
				List<String> scopes = extension.getParameters("scope");
				int priority = Integer.parseInt(extension.getParameter("priority"));
				for (String scopeString : scopes) {
					Scope scope = Scope.getScope(scopeString);
					// but take care not overwrite an existing scope,
					// because it has the higher priority
					if (!previewRenderers.containsKey(scope)) {
						TreeMap<Integer, PreviewRenderer> priorityMap = new TreeMap<>();
						priorityMap.put(priority, (PreviewRenderer) object);
						previewRenderers.put(scope, priorityMap);
					}
				}
			}
			else {
				LOGGER.warn("extension of class '" + object.getClass().getName() +
						"' is not of the expected type " + Renderer.class.getName());
			}
		}
		Plugins.initResources(extensions);
	}

	/**
	 * Groups the specified sections by the ancestor section to be rendered as a preview. If a section has no ancestor
	 * to be rendered, the section itself will be used as a group with an empty collection of grouped sections.
	 *
	 * @param items list of sections to be grouped
	 * @return the groups of sections
	 * @created 16.08.2013
	 */
	public Map<Section<?>, Collection<Section<?>>> groupByPreview(Collection<Section<?>> items) {
		List<Section<?>> list = new ArrayList<>(items);
		list.sort(KDOMPositionComparator.getInstance());
		Map<Section<?>, Collection<Section<?>>> result = new LinkedHashMap<>();
		for (Section<?> section : list) {
			Section<?> previewSection = getPreviewAncestor(section);
			// handle if the section has no preview renderer
			if (previewSection == null) {
				result.put(section, Collections.emptyList());
				continue;
			}
			// otherwise add section to preview group
			// or create group if it is new
			result.computeIfAbsent(previewSection, k -> new LinkedList<>()).add(section);
		}
		return result;
	}
}
