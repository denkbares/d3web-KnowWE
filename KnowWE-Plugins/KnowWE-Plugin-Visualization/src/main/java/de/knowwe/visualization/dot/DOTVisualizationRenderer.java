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
package de.knowwe.visualization.dot;

import java.io.File;

import com.denkbares.utils.Log;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.GraphVisualizationRenderer;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.util.FileUtils;

/**
 * A GraphVisualizationRenderer using DOT/GraphViz for the visualization of the
 * SubGraphData.
 *
 * @author Jochen Reutelshöfer
 * @created 27.05.2013
 */
public class DOTVisualizationRenderer implements GraphVisualizationRenderer {

	private final SubGraphData data;
	private final Config config;
	private String source = null;
	private File[] createdFiles = new File[] {};

	public DOTVisualizationRenderer(SubGraphData data, Config config) {
		this.data = data;
		this.config = config;
	}

	@Override
	public synchronized String generateSource() {
		source = DOTRenderer.createDotSources(data, config);
		createdFiles = DOTRenderer.createAndWriteFiles(config, source);
		return source;
	}

	@Override
	public String getHTMLIncludeSnipplet() {
		StringBuilder html = new StringBuilder();
		if (!DOTRenderer.checkDotInstallation(config)) {
			html.append("<div class='error'>")
					.append("Unable to find a valid installation of dot/Graphviz at location '")
					.append(config.getDotApp())
					.append("'. Graphvis (<a href='http://www.graphviz.org/'>http://www.graphviz.org/</a>)")
					.append(" has to be installed on the server to generate the visualizations!")
					.append("</div>");
			return html.toString();
		}
		String filePath = DOTRenderer.getFilePath(config);
		String src = filePath.substring(filePath.indexOf(FileUtils.KNOWWEEXTENSION_FOLDER));

		String svg = "<div style='overflow: auto'><object data='" + src + ".svg' " +
				"onload='KNOWWE.plugin.visualization.addClickEventsToGraph(this);' type=\"image/svg+xml\"></object></div>";
		html.append(svg);
		return html.toString();
	}

	@Override
	public synchronized void cleanUp() {
		for (File createdFile : createdFiles) {
			if (!createdFile.delete()) {
				Log.warning("Unable to delete file " + createdFile.getAbsolutePath());
			}
		}
	}

	@Override
	public String getSource() {
		if (source == null) {
			return generateSource();
		}
		return source;
	}

}
