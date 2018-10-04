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

import de.d3web.diaFlux.flow.EndNode;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.xml.AbstractXMLType;
import de.knowwe.kdom.xml.XMLContent;

/**
 * @author Reinhard Hatko
 * @created on: 09.10.2009
 */
public class ExitType extends AbstractXMLType {

	private static ExitType instance;

	private ExitType() {
		super("exit");
		addChildType(new XMLContent(new ExitNodeDef()));
	}

	public static ExitType getInstance() {
		if (instance == null) instance = new ExitType();

		return instance;
	}

	public String getTermName(Section<? extends ExitType> section) {
		Section<ExitNodeDef> term = Sections.successor(section, ExitNodeDef.class);
		return term.get().getTermName(term);
	}

	static class ExitNodeDef extends SimpleDefinition {

		public ExitNodeDef() {
			super(D3webCompiler.class, EndNode.class);
			setSectionFinder(AllTextFinder.getInstance());
			setRenderer(StyleRenderer.FlowchartExit);
		}

		@Override
		public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
			Section<FlowchartType> flowchart = Sections.ancestor(section,
					FlowchartType.class);
			String flowchartName = FlowchartType.getFlowchartName(flowchart);
			return new Identifier(flowchartName, getTermName(section));
		}

		@Override
		public String getTermName(Section<? extends Term> s) {
			return Strings.unquote(Strings.decodeHtml(s.getText()));
		}

	}

}
