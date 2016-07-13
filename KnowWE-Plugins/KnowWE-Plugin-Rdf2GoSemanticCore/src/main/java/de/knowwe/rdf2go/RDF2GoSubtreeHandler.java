/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

/**
 *
 */
package de.knowwe.rdf2go;

import java.util.Collection;

import com.denkbares.utils.Log;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;

/**
 * @author grotheer
 * @param <T> This class just implements the destroy-handler for owl-generating
 *        SubtreeHandlers. It should be used as superclass for all
 *        owl-generating (i.e. those that call SemanticCore.addstatemnts(...))
 *        SubtreeHandlers to facilitate the incremental build of Articles
 * 
 */
public abstract class RDF2GoSubtreeHandler<C extends Rdf2GoCompiler, T extends Type> implements Rdf2GoCompileScript<C, T> {

	public abstract Collection<Message> create(C compiler, Section<T> section);

	@Override
	public void compile(C compiler, Section<T> section) throws CompilerMessage {
		throw new CompilerMessage(create(compiler, section));
	}

	@Override
	public void destroy(C compiler, Section<T> section) {
		try {
			compiler.getRdf2GoCore().removeStatements(section);
		}
		catch (Exception e) {
			Log.severe("Exception while removing statements for section " + section.get().getName() +
			 " " + section.getID());
		}
	}

}
