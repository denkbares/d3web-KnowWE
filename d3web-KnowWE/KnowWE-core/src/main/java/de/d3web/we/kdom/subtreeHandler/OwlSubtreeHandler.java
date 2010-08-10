/**
 *
 */
package de.d3web.we.kdom.subtreeHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * @author kazamatzuri
 * @param <T>
 *            This class just implements the destroy-handler for owl-generating
 *            SubtreeHandlers. It should be used as superclass for all
 *            owl-generating (i.e. those that call
 *            SemanticCore.addstatemnts(...)) SubtreeHandlers to facilitate the
 *            incremental build of Articles
 *
 */
public abstract class OwlSubtreeHandler<T extends KnowWEObjectType> extends
		SubtreeHandler<T> {

	// no need to create (and destroy) all the OWL statements again for included
	// Sections
	@Override
	public boolean needsToCreate(KnowWEArticle article, Section<T> s) {
		return s.getTitle().equals(article.getTitle()) && super.needsToCreate(article, s);
	}

	@Override
	public boolean needsToDestroy(KnowWEArticle article, Section<T> s) {
		return s.getTitle().equals(article.getTitle()) && super.needsToDestroy(article, s);
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
