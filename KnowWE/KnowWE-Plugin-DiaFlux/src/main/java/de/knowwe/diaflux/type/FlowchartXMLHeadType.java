/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import de.d3web.diaFlux.flow.Flow;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.MultiSectionFinder;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.xml.XMLHead;

/**
 * @author Reinhard Hatko
 * @created 08.12.2010
 */
public class FlowchartXMLHeadType extends XMLHead {

	public FlowchartXMLHeadType() {
		addChildType(new FlowchartTermDef());
	}

	public static class FlowchartTermDef extends SimpleDefinition implements RenamableTerm {

		public FlowchartTermDef() {
			super(D3webCompiler.class, Flow.class);
			setSectionFinder(new MultiSectionFinder(
					new RegexSectionFinder(Pattern.compile("name=\"\\s*([^\"]*?)\\s*\""), 1),
					new RegexSectionFinder(Pattern.compile("name=\'\\s*([^\']*?)\\s*\'"), 1)));
			setRenderer(StyleRenderer.Flowchart);
			clearCompileScripts();
			addCompileScript(Priority.HIGHER, new FlowchartRegistrationHandler());
		}

		@Override
		public String getTermName(Section<? extends Term> section) {
			return Strings.decodeHtml(super.getTermName(section));
		}

		@Override
		public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
			return Strings.encodeHtml(newIdentifier.getLastPathElement());
		}
	}

	static class FlowchartRegistrationHandler implements D3webHandler<FlowchartTermDef> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<FlowchartTermDef> s) {
			Collection<Message> messages = new ArrayList<>(1);
			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			Identifier termIdentifier = s.get().getTermIdentifier(s);
			if (terminologyManager.isDefinedTerm(termIdentifier)) {
				messages.add(Messages.objectAlreadyDefinedError(termIdentifier.toString(), s));
			}
			terminologyManager.registerTermDefinition(compiler, s,
					s.get().getTermObjectClass(s), termIdentifier);
			return messages;
		}

	}
}
