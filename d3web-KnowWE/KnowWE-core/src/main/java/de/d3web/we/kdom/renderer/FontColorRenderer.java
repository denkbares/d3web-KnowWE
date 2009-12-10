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

package de.d3web.we.kdom.renderer;

import java.util.HashMap;
import java.util.Map;

public class FontColorRenderer extends StyleRenderer {
	
	public static String COLOR1 = "color:rgb(40, 40, 160)";
	public static String COLOR2 = "color:rgb(255, 0, 0)";
	public static String COLOR3 = "color:rgb(0, 128, 0)";
	public static String COLOR4 = "color:rgb(200, 0, 200)";
	public static String COLOR5 = "color:rgb(128, 128, 0)";
	public static String COLOR6 = "color:rgb(0, 0, 255)";
	public static String COLOR7 = "color:rgb(255, 0, 102)";
	public static String COLOR8 = "color:rgb(0, 0, 102)";
	
	private static Map<String, FontColorRenderer> renderers = new HashMap<String, FontColorRenderer>();
	
	public static FontColorRenderer getRenderer(String color) {

		if (!renderers.containsKey(color)) {
			renderers.put(color, new FontColorRenderer(color));
		}
		
		return renderers.get(color);
	}

	public FontColorRenderer(String s) {
		super(s);
	}
	
	public String getColor() {
		return this.style;
	}

}
