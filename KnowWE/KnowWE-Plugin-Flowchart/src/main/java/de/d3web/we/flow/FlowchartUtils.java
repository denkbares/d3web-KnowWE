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
package de.d3web.we.flow;

import de.d3web.we.flow.type.FlowchartPreviewContentType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.Section;

/**
 * 
 * @author Reinhard Hatko
 * @created 09.12.2009
 */
public class FlowchartUtils {

	private FlowchartUtils() {
	}

	/**
	 * extracts the preview of a section of Type FlowchartType
	 */
	public static String extractPreview(Section<FlowchartType> flowchartSection) {

		Section previewsection = flowchartSection.findSuccessor(FlowchartPreviewContentType.class);

		if (previewsection == null) return null;

		String flowchart = previewsection.getOriginalText();

		return flowchart.substring(flowchart.indexOf("<![CDATA[") + 9, flowchart.indexOf("]]>"));
	}

	/**
	 * Creates a preview from the HTML code saved in the article by including
	 * necessary css-styles
	 * 
	 * @param preview
	 * @return
	 */
	public static String createRenderablePreview(String preview) {
		return "<div style='cursor: default !important;'>" +
				"<link rel='stylesheet' type='text/css' href='cc/kbinfo/dropdownlist.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/kbinfo/objectselect.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/kbinfo/objecttree.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/flowchart.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/floweditor.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/guard.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/node.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/nodeeditor.css'></link>" +
				"<link rel='stylesheet' type='text/css' href='cc/flow/rule.css'></link>" +
				"<style type='text/css'>.Node { cursor: default !important; }</style>" +
				preview +
				"</div>";
	}

	/**
	 * Creates a HTML representation of the preview.
	 * 
	 * @created 25.11.2010
	 * @param flowSection
	 * @return s the preview including styles, or null if no preview is present
	 */
	public static String createRenderablePreview(Section<FlowchartType> flowSection) {
		String preview = extractPreview(flowSection);

		if (preview == null) return null;

		return createRenderablePreview(preview);

	}

}
