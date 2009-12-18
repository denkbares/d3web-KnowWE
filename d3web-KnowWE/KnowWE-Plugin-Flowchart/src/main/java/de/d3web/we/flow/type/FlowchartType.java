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

package de.d3web.we.flow.type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.we.flow.FlowchartSectionRenderer;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.flow.FlowchartTerminologySubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;


/**
 * 
 *
 * @author hatko
 * Created on: 09.10.2009
 */
public class FlowchartType extends AbstractXMLObjectType {
	
	protected KnowWEDomRenderer renderer = new FlowchartSectionRenderer();

	private static FlowchartType instance;

	private FlowchartType() {
		super("flowchart");
	}

	public static FlowchartType getInstance() {
		if (instance == null)
			instance = new FlowchartType();

		return instance;
	}


	
	
	@Override
	protected void init() {
		
		this.childrenTypes.add(FlowchartContentType.getInstance());
		addReviseSubtreeHandler(new FlowchartTerminologySubTreeHandler());
		
//		setNotRecyclable(true);
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return renderer;
	}
	
	public String getFlowchartName(Section sec) {
		Map<String, String> mapFor = this.getAttributeMapFor(sec);
		return mapFor.get("name");
	}


	public String getFlowchartID(Section sec) {
		Map<String, String> mapFor = this.getAttributeMapFor(sec);
		String id = mapFor.get("id");
		if (id == null) id = mapFor.get("name");
		if (id == null) id = "sheet_01";
		return id;
	}

	public String[] getStartNames(Section sec) {
		List<Section> startSections = new LinkedList<Section>();
		sec.findSuccessorsOfType(StartType.class, startSections);
		return getSectionsContents(startSections);
	}
	
	public String[] getExitNames(Section sec) {
		List<Section> exitSections = new LinkedList<Section>();
		sec.findSuccessorsOfType(ExitType.class, exitSections);
		return getSectionsContents(exitSections);
	}
	
	private static String[] getSectionsContents(List<Section> sections) {
		List<String> result = new LinkedList<String>();
		for (Section start : sections) {
			String content = getSectionContent(start);
			if (content != null) {
				result.add(content);
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	private static String getSectionContent(Section sec) {
		String result = null;
		
		List<Section> children = sec.getChildren();
		if (children.size() == 3) // HOTFIX for parser section returning enclosing xml-tags
			return children.get(1).getOriginalText();
		
		// Old mechanism
		for (Section child : children) {
			String text = child.getOriginalText();
			result = (result == null) ? text : (result + text);
		}
		return result;
	}
	
	
}
