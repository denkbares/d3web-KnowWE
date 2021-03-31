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
package de.d3web.we.kdom.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.indication.ActionIndication;
import de.d3web.indication.inference.PSMethodStrategic;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.NamedObjectReference;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.AnonymousType;

/**
 * @author Jochen
 * @created 29.07.2010
 */
public class QASetIndicationAction extends D3webRuleAction<QASetIndicationAction> {

	public QASetIndicationAction() {
		this.setSectionFinder(new AllTextFinderTrimmed());

		NamedObjectReference qasetType = new NamedObjectReference(new SimpleReferenceRegistrationScript<>(D3webCompiler.class, false));
		qasetType.setSectionFinder(new AllTextFinderTrimmed());
		qasetType.addCompileScript(new CheckTypeHandler());

		this.addChildType(qasetType);

	}

	@Override
	public PSAction createAction(D3webCompiler compiler, Section<QASetIndicationAction> s) {
		ActionIndication a = new ActionIndication();
		List<QASet> qasets = new ArrayList<>();
		a.setQASets(qasets);

		Section<NamedObjectReference> objectRef = Sections.successor(s, NamedObjectReference.class);
		if (objectRef != null) {
			NamedObject object = objectRef.get().getTermObject(compiler, objectRef);
			if (object instanceof QASet) qasets.add((QASet) object);
		}

		return a;
	}

	@Override
	public Class<? extends PSMethod> getProblemSolverContext() {
		return PSMethodStrategic.class;
	}

	static class CheckTypeHandler implements D3webCompileScript<AnonymousType> {

		@Override
		public void compile(D3webCompiler compiler, Section<AnonymousType> s) throws CompilerMessage {
			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			Identifier termIdentifier = new Identifier(Strings.trimQuotes(s.getText()));
			if (terminologyHandler.isDefinedTerm(termIdentifier)) {
				Section<?> termDefinitionSection = terminologyHandler.getTermDefiningSection(termIdentifier);
				if (termDefinitionSection.get() instanceof Term) {
					@SuppressWarnings("unchecked")
					Section<? extends Term> simpleDef = (Section<? extends Term>) termDefinitionSection;
					Class<?> objectClazz = simpleDef.get().getTermObjectClass(compiler, simpleDef);
					if (Question.class.isAssignableFrom(objectClazz) || QContainer.class.isAssignableFrom(objectClazz)) {
						throw new CompilerMessage();
					}
					throw CompilerMessage.error(Strings.trimQuotes(termIdentifier.toPrettyPrint()) + "is defined as: "
							+ objectClazz.getName()
							+ " - expected was Question or Questionnaire");
				}
			}
			throw CompilerMessage.error("Could not find '"
					+ Strings.trimQuotes(termIdentifier.toPrettyPrint())
					+ "' - expected was Question or Questionnaire");
		}
	}

}
