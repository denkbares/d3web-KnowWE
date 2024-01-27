/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

package de.d3web.we.reviseHandler;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.AbortCheck;
import de.d3web.we.object.D3webTermDefinition;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * A common handler for terminology object creating scripts
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.2020
 */
public abstract class D3webTerminologyObjectCreationHandler<TermType extends NamedObject, SectionType extends D3webTermDefinition<TermType>> implements D3webHandler<SectionType> {

	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<SectionType> section) {

		Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
		String name = getTermName(compiler, section);
		Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);
		TerminologyManager terminologyHandler = compiler.getTerminologyManager();
		terminologyHandler.registerTermDefinition(compiler, section, termObjectClass, termIdentifier);

		AbortCheck<TermType> abortCheck = section.get().canAbortTermObjectCreation(compiler, section);
		if (abortCheck.skipCreation()) {
			return Collections.emptySet();
		}
		if (abortCheck.hasErrors()) {
			return abortCheck.getErrors();
		}

		if (abortCheck.termExist()) {
			return abortCheck.getErrors();
		}

		KnowledgeBase kb = getKnowledgeBase(compiler);
		TerminologyObject object = kb.getManager().search(name);
		if (object != null) {
			if (termObjectClass.isInstance(object)) {
				return Messages.noMessage();
			}
			else {
				// otherwise, add an error
				return Messages.asList(Messages.error("The term '" + name + "' is already used."));
			}
		}

		createTermObject(name, kb);
		Compilers.recompileRegistrations(compiler, termIdentifier);
		return Messages.noMessage();
	}

	protected String getTermName(D3webCompiler compiler, Section<SectionType> section) {
		return section.get().getTermName(section);
	}

	@NotNull
	protected abstract TermType createTermObject(String name, KnowledgeBase kb);

	@Override
	public void destroy(D3webCompiler compiler, Section<SectionType> section) {
		Identifier identifier = section.get().getTermIdentifier(compiler, section);
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);
		terminologyManager.unregisterTermDefinition(compiler, section, termObjectClass, identifier);
		if (!terminologyManager.isDefinedTerm(identifier)) {
			String name = getTermName(compiler, section);
			TerminologyObject terminologyObject = compiler.getKnowledgeBase().getManager().search(name);
			if (!termObjectClass.isInstance(terminologyObject)) return;
			terminologyObject.destroy();
		}
		Compilers.destroyAndRecompileRegistrations(compiler, identifier);
	}

	@Override
	public boolean isIncrementalCompilationSupported(Section<SectionType> section) {
		return true;
	}
}
