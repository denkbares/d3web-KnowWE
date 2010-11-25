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

package de.d3web.we.flow.type;

import java.util.Map;

import de.d3web.we.flow.FlowchartSectionRenderer;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author Reinhard Hatko
 * @created on: 09.10.2009
 */
public class FlowchartType extends AbstractXMLObjectType {

	protected KnowWEDomRenderer<FlowchartType> renderer = new FlowchartSectionRenderer();

	public FlowchartType() {
		super("flowchart");
	}

	@Override
	protected void init() {
		this.childrenTypes.add(FlowchartContentType.getInstance());
		addSubtreeHandler(Priority.DEFAULT, new FlowchartSubTreeHandler());

	}

	@Override
	public KnowWEDomRenderer<FlowchartType> getRenderer() {
		return renderer;
	}

	public static String getFlowchartName(Section<FlowchartType> sec) {
		Map<String, String> mapFor = AbstractXMLObjectType
				.getAttributeMapFor(sec);
		return mapFor.get("name");
	}



}
