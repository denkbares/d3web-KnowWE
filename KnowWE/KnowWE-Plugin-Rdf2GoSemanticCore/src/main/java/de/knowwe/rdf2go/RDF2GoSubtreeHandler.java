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

import de.knowwe.core.compile.ConstraintModule;
import de.knowwe.core.compile.SuccessorNotReusedConstraint;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;

/**
 * @author grotheer
 * @param <T> This class just implements the destroy-handler for owl-generating
 *        SubtreeHandlers. It should be used as superclass for all
 *        owl-generating (i.e. those that call SemanticCore.addstatemnts(...))
 *        SubtreeHandlers to facilitate the incremental build of Articles
 * 
 */
public abstract class RDF2GoSubtreeHandler<T extends Type> extends
		SubtreeHandler<T> {

	public RDF2GoSubtreeHandler() {
		super(true);
		this.registerConstraintModule(new RDF2GoHandlerConstraint<T>());
		this.registerConstraintModule(new SuccessorNotReusedConstraint<T>());
	}

	@Override
	public void destroy(Article article, Section<T> section) {
		try {
			Rdf2GoCore.getInstance().removeStatementsForSection(section);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class RDF2GoHandlerConstraint<T2 extends Type> extends ConstraintModule<T2> {

		public RDF2GoHandlerConstraint() {
			super(Operator.DONT_COMPILE_IF_VIOLATED, Purpose.CREATE_AND_DESTROY);
		}

		@Override
		public boolean violatedConstraints(Article article, Section<T2> section) {
			return !section.getTitle().equals(article.getTitle());
		}

	}

}
