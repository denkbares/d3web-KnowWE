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

import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * 
 * 
 * @author Reinhard Hatko
 * @created on: 09.10.2009
 */
public class ExitType extends AbstractXMLObjectType {

	private static ExitType instance;

	private ExitType() {
		super("exit");
	}

	public static ExitType getInstance() {
		if (instance == null) instance = new ExitType();

		return instance;
	}

	@Override
	protected void init() {
		addChildType(new ExitNodeDef());
	}

	static class ExitNodeDef extends TermDefinition<String> {

		public ExitNodeDef() {
			super(String.class);
		}

		@Override
		protected void init() {
			setSectionFinder(new AllTextSectionFinder());
			addSubtreeHandler(Priority.HIGH, new FlowchartTermDefinitionRegistrationHandler());
		}

		@Override
		public String getTermName(Section<? extends KnowWETerm<String>> s) {
			String nodeName = s.getOriginalText();
			Section<FlowchartType> flowchart = s.findAncestorOfType(FlowchartType.class);
			String flowchartName = FlowchartType.getFlowchartName(flowchart);

			return flowchartName + "(" + nodeName + ")";
		}

	}

}
