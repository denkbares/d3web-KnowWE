/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.visualization.d3;

import de.knowwe.visualization.Config;
import de.knowwe.visualization.GraphVisualizationRenderer;
import de.knowwe.visualization.SubGraphData;

public class D3VisualizationRenderer implements GraphVisualizationRenderer {

	private final SubGraphData data;
	private final Config config;
	private String source = null;

	public D3VisualizationRenderer(SubGraphData data, Config config) {
		this.data = data;
		this.config = config;
	}

	@Override
	public String generateSource() {
		source = D3Renderer.createD3HTMLSource(data, config);
		return source;
	}

	@Override
	public String getSource() {
		if (source == null) {
			return generateSource();
		}
		return source;
	}

	@Override
	public String getHTMLIncludeSnipplet() {
		if (source == null) {
			return generateSource();
		}
		return source;
	}

	@Override
	public void cleanUp() {
		// nothing to clean up
	}

}
