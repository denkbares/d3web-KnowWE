/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.rendering;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.user.UserSettingsManager;

/**
 * @author Jochen
 * 
 * This class manages special renderers for types - possibly coming along with extensions
 * Thus, an extension can add for example a special editor for one given type.
 *
 */
public class RendererManager {
	
	private static RendererManager instance;
	
	public static RendererManager getInstance() {
		if(instance == null) {
			instance = new RendererManager();
		}
		return instance;
	}
	
	private Map<KnowWEObjectType, RendererSet> rendererToTypeMap = new HashMap<KnowWEObjectType, RendererSet>();
	
	public KnowWEDomRenderer getRenderer(KnowWEObjectType type, String user, String topic) {
		
		RenderingMode renderingType = UserSettingsManager.getInstance().getRenderingType(user, topic);
		
		RendererSet set = rendererToTypeMap.get(type);
		if(set != null) {
			KnowWEDomRenderer renderer = set.getRenderer(user, topic,renderingType);
			if(renderer != null) {
				return renderer;
			}
		}
		
		return null;
	}

}
