/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.dashboard.rendering;

import java.util.HashMap;
import java.util.Map;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;

/**
 * Manager providing the correct ObjectNameRenderer for a certain class.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 24.09.2012
 */

public class ObjectNameRendererManager {

	private static Map<String, ObjectNameRenderer> renderers = null;

	private static Map<String, ObjectNameRenderer> initRenderers() {
		Map<String, ObjectNameRenderer> renderers = new HashMap<>();
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				ObjectNameRenderer.PLUGIN_ID,
				ObjectNameRenderer.EXTENSION_POINT_ID);
		for (Extension extension : extensions) {
			if (extension.getSingleton() instanceof ObjectNameRenderer) {
				ObjectNameRenderer renderer = (ObjectNameRenderer) extension.getSingleton();
				String scopeClass = extension.getParameter("scope");
				renderers.put(scopeClass, renderer);
			}
		}
		return renderers;
	}

	public static ObjectNameRenderer getObjectNameRenderer(Class<?> clazz) {
		if (renderers == null) {
			renderers = initRenderers();
		}
		return renderers.get(clazz.getName());
	}

}