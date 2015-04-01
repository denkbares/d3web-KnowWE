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

package de.d3web.we.reviseHandler;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;

@FunctionalInterface
public interface D3webHandler<T extends Type> extends D3webCompileScript<T> {

	Collection<Message> create(D3webCompiler compiler, Section<T> section);

	@Override
	default void compile(D3webCompiler compiler, Section<T> section) throws CompilerMessage {
		throw new CompilerMessage(create(compiler, section));
	}

	default KnowledgeBase getKnowledgeBase(D3webCompiler compiler) {
		return D3webUtils.getKnowledgeBase(compiler);
	}
	
	@Override
	default Class<D3webCompiler> getCompilerClass() {
		return D3webCompiler.class;
	}

}
