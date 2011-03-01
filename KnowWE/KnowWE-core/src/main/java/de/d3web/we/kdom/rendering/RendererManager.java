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

package de.d3web.we.kdom.rendering;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.kdom.Type;
import de.d3web.we.user.UserSettingsManager;

/**
 * @author Jochen
 * 
 *         This class manages special renderers for types - possibly coming
 *         along with extensions Thus, an extension can add for example a
 *         special editor for one given type.
 * 
 */
public class RendererManager {

	private static RendererManager instance;

	public static RendererManager getInstance() {
		if (instance == null) {
			instance = new RendererManager();
		}
		return instance;
	}

	private Map<Type, RendererSet> rendererToTypeMap = new HashMap<Type, RendererSet>();

	public KnowWEDomRenderer<? extends Type> getRenderer(Type type, String user, String topic) {

		RenderingMode renderingType = UserSettingsManager.getInstance().getRenderingType(user,
				topic);

		RendererSet set = rendererToTypeMap.get(type);
		if (set != null) {
			KnowWEDomRenderer<? extends Type> renderer = set.getRenderer(user, topic,
					renderingType);
			if (renderer != null) {
				return renderer;
			}
		}

		return null;
	}

	/**
	 * Sets a {@link CustomRenderer} for the {@link Type} type to
	 * the {@link RendererManager}.
	 * 
	 * @param type The {@link Type} the renderer applies to
	 * @param customRenderer The custom renderer for the
	 *        {@link Type};
	 */
	public void setRenderer(Type type, CustomRenderer customRenderer) {

		RendererSet set = rendererToTypeMap.get(type);
		if (set != null) {
			set.addCustomRenderer(customRenderer);
		}
		else {
			set = new RendererSet();
			set.addCustomRenderer(customRenderer);
			rendererToTypeMap.put(type, set);
		}
	}

	/**
	 * Removes a {@link CustomRenderer} for the {@link Type} type
	 * from the {@link RendererManager}.
	 * 
	 * @param type The {@link Type} the renderer applies to
	 */
	public void removeRenderer(Type type) {
		if (rendererToTypeMap.containsKey(type)) {
			rendererToTypeMap.remove(type);
		}
	}
}
