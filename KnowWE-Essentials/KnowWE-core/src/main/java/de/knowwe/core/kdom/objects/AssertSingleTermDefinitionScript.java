/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.kdom.objects;

import java.util.Collection;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.02.2012
 */
public class AssertSingleTermDefinitionScript<C extends TermCompiler> implements CompileScript<C, Type> {

	private final Class<C> compilerClass;

	public AssertSingleTermDefinitionScript(Class<C> compilerClass) {
		this.compilerClass = compilerClass;
	}

	@Override
	public void compile(C compiler, Section<Type> section) {
		TerminologyManager tHandler = compiler.getTerminologyManager();
		Identifier termIdentifier = KnowWEUtils.getTermIdentifier(section);
		Collection<Section<?>> termDefinitions = tHandler.getTermDefiningSections(termIdentifier);
		Message msg = Messages.error("There is more than one definition for the term '"
				+ termIdentifier.toString() + "' which is restricted to only one definition.");
		if (termDefinitions.size() > 1) {
			for (Section<?> termDef : termDefinitions) {
				Messages.storeMessage(compiler, termDef, getClass(), msg);
			}
			for (Section<?> termRef : tHandler.getTermReferenceSections(termIdentifier)) {
				Messages.storeMessage(compiler, termRef, getClass(), msg);
			}
		}
		Messages.storeMessage(compiler, section, getClass(), msg);
	}

	@Override
	public Class<C> getCompilerClass() {
		return compilerClass;
	}

}
