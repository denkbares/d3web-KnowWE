/**
 * 
 */
package de.d3web.we.kdom.subtreeHandler;

import de.d3web.we.core.SemanticCore;
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

	@Override
	public void destroy(KnowWEArticle article, Section s) {
		super.destroy(article, s);
		SemanticCore.getInstance().clearContext(s);
	}

}
