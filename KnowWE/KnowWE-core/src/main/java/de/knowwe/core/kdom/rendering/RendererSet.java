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

package de.knowwe.core.kdom.rendering;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jochen
 * 
 *         A collection of CustomRenderers for one specific Type(-instance) For
 *         the case that multiple renderers are applicable for a situation the
 *         priority question occurs. Right now the order of the list defines the
 *         priority. So priority is set when registering the renderers.
 * 
 */
public class RendererSet {

	private List<de.knowwe.core.kdom.rendering.CustomRenderer> renderers = new ArrayList<de.knowwe.core.kdom.rendering.CustomRenderer>();

	/**
	 * registers a new custom-renderer to this RendererSet
	 * 
	 * @param specialRenderer
	 */
	public void addCustomRenderer(CustomRenderer specialRenderer) {
		// TODO insert in priority order
		this.renderers.add(specialRenderer);
	}

	/**
	 * Looks for a applicable renderer and returns it
	 * 
	 * @param user
	 * @param topic
	 * @param type
	 * @return
	 */
	public KnowWERenderer getRenderer(String user, String topic, RenderingMode type) {
		for (CustomRenderer r : this.renderers) {
			if (r.doesApply(user, topic, type)) {
				return r;
			}
		}
		return null;
	}

}
