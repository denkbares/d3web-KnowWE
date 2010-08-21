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
package de.d3web.we.core.semantic;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * @author kazamatzuri
 * @param <T> This class just implements the destroy-handler for owl-generating
 *        SubtreeHandlers. It should be used as superclass for all
 *        owl-generating (i.e. those that call SemanticCore.addstatemnts(...))
 *        SubtreeHandlers to facilitate the incremental build of Articles
 * 
 */
public abstract class OwlSubtreeHandler<T extends KnowWEObjectType> extends
		SubtreeHandler<T> {

	public OwlSubtreeHandler() {
		super(true);
	}

	// no need to create (and destroy) all the OWL statements again for included
	// Sections
	@Override
	public boolean needsToCreate(KnowWEArticle article, Section<T> s) {
		return super.needsToCreate(article, s)
				&& s.getTitle().equals(article.getTitle());
	}

	@Override
	public boolean needsToDestroy(KnowWEArticle article, Section<T> s) {
		return super.needsToDestroy(article, s) 
				&& s.getTitle().equals(article.getTitle());
	}

	@Override
	public void destroy(KnowWEArticle article, Section<T> s) {

		try {
			SemanticCoreDelegator.getInstance().clearContext(s);
		}
		catch (Exception e) {
			// TODO find some fix for the exception in owlim (see underneath)

			// trying to avoid the wiki crashing because of the Exception thrown
			// in owlim-lib

			Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
					"exception in owlim catched: \n" +
							" java.lang.ArrayIndexOutOfBoundsException2" +
							"com.ontotext.trree.owlim_ext.c.do(Unknown Source)" +
							"com.ontotext.trree.owlim_ext.c$2.remove(Unknown Source)");

			// java.lang.ArrayIndexOutOfBoundsException2
			// com.ontotext.trree.owlim_ext.c.do(Unknown Source)
			// com.ontotext.trree.owlim_ext.c$2.remove(Unknown Source)
			// com.ontotext.trree.owlim_ext.Repository.removeStatements(Unknown
			// Source)
			// com.ontotext.trree.owlim_ext.SailConnectionImpl.commit(SailConnectionImpl.java:222)
			// org.openrdf.repository.sail.SailRepositoryConnection.commit(SailRepositoryConnection.java:81)
			// org.openrdf.repository.base.RepositoryConnectionBase.setAutoCommit(RepositoryConnectionBase.java:168)
			// org.openrdf.repository.base.RepositoryConnectionBase.remove(RepositoryConnectionBase.java:485)
			// de.d3web.we.core.SemanticCore.clearContext(SemanticCore.java:491)
			// de.d3web.we.kdom.subtreeHandler.OwlSubtreeHandler.destroy(OwlSubtreeHandler.java:27)

		}
	}

}
