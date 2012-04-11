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

package de.knowwe.diaflux.type;

import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.InvalidKDOMSchemaModificationOperation;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.diaflux.FlowchartRenderer;
import de.knowwe.diaflux.FlowchartSubTreeHandler;
import de.knowwe.diaflux.type.FlowchartXMLHeadType.FlowchartTermDef;
import de.knowwe.kdom.xml.AbstractXMLType;
import de.knowwe.kdom.xml.XMLHead;

/**
 * @author Reinhard Hatko
 * @created on: 09.10.2009
 */
public class FlowchartType extends AbstractXMLType {

	public FlowchartType() {
		super("flowchart");
		this.childrenTypes.add(FlowchartContentType.getInstance());
		addSubtreeHandler(Priority.DEFAULT, new FlowchartSubTreeHandler());
		replaceHead();
		setRenderer(new FlowchartRenderer());
	}

	/**
	 * 
	 * @created 08.12.2010
	 */
	public void replaceHead() {
		try {
			this.replaceChildType(new FlowchartXMLHeadType(), XMLHead.class);
		}
		catch (InvalidKDOMSchemaModificationOperation e) {
			e.printStackTrace();
		}
	}

	public static String getFlowchartName(Section<FlowchartType> sec) {
		return Sections.findSuccessor(sec, FlowchartTermDef.class).getText();
	}

}
