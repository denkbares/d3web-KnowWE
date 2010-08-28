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

package de.d3web.we.kdom.subtreeHandler;

import java.util.Collection;

import de.d3web.we.kdom.IncrementalConstraints;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;

/**
 * Abstract class for a SubtreeHandler. This handler has to be registered to a
 * type and then, after the KDOM is build, this handler is called with that
 * section and the subtree can be processed (e.g. translated to a target
 * representation).
 * 
 * @author Jochen, Albrecht
 * 
 */
public abstract class SubtreeHandler<T extends KnowWEObjectType> {

	private final boolean ignoreNamespaces;

	public SubtreeHandler(boolean ignoreNamespaces) {
		this.ignoreNamespaces = ignoreNamespaces;
	}

	public SubtreeHandler() {
		this.ignoreNamespaces = false;
	}

	public boolean isIgnoringNamespaces() {
		return this.ignoreNamespaces;
	}

	/**
	 * If this method returns false, the method
	 * <tt>create(KnowWEArticle, Section)</tt> in this handler will not be
	 * called in the revising of the article.
	 * <p/>
	 * If you are implementing an incremental SubtreeHandler, you can overwrite
	 * or extend this method with an algorithm to decide, if this handler needs
	 * or doesn't need to create, depending on the constraints there might be.
	 * 
	 * @param article is the article that calls this method... not necessarily
	 *        the article the Section is hooked into directly, since Sections
	 *        can also be included!
	 * @param s is the Section from which you want to create something
	 * @return true if this handler needs to create, false if not.
	 */
	public boolean needsToCreate(KnowWEArticle article, Section<T> s) {
		return (article.isSecondBuild() || !article.isPostDestroyFullParse())
				&& article.isFullParse()
					|| !s.isReusedBy(article.getTitle())
					|| (s.get().isOrderSensitive() && s.isPositionChangedFor(article.getTitle()))
					|| (s.get() instanceof IncrementalConstraints
						&& ((IncrementalConstraints) s.get()).hasViolatedConstraints(
								article, s));
	}

	/**
	 * Revises this section or subtree and creates whatever needs to be created,
	 * if the method <tt>needsToCreate(KnowWEArticle, Section)</tt> of this
	 * handler returns true.
	 * 
	 * @param article is the article that calls this method... not necessarily
	 *        the article the Section is hooked into directly, since Sections
	 *        can also be included!
	 * @param s is the Section from which you want to create something
	 */
	public abstract Collection<KDOMReportMessage> create(KnowWEArticle article, Section<T> s);

	/**
	 * If this method returns false, the method<tt>destroy(KnowWEArticle,
	 * Section)</tt> in this handler will not be called for that Section of the
	 * last version of the KDOM.
	 * <p/>
	 * If you are implementing an incremental SubtreeHandler, you can overwrite
	 * or extend this method with an algorithm to decide, if this handler needs
	 * or doesn't need to destroy the old stuff, depending on the constraints
	 * there might be.
	 * 
	 * @param article is the last version of the article that calls this
	 *        method... not necessarily the article the Section is hooked into
	 *        directly, since Sections can also be included!
	 * @param s is the old, not reused Section whose stuff you want to destroy
	 * @return true if this handler needs to destroy, false if not.
	 */
	public boolean needsToDestroy(KnowWEArticle article, Section<T> s) {
		return !article.isFullParse()
				&& (!s.isReusedBy(article.getTitle())
						|| (s.get().isOrderSensitive() && s.isPositionChangedFor(article.getTitle()))
						|| (s.get() instanceof IncrementalConstraints
								&& ((IncrementalConstraints) s.get()).hasViolatedConstraints(
										article, s)));
	}

	/**
	 * This method is called after the creation of the new KDOM (but prior to
	 * the revising of the new KDOM) on the Sections of the last KDOM, if the
	 * method <tt>needsToDestroy(KnowWEArticle, Section)</tt> of this handler
	 * returns true. If you are implementing an incremental SubtreeHandler, you
	 * can overwrite this method to implement one, that removes everything the
	 * Section created in the last version of the article. This way you can,
	 * later on in the revise-step, simply add the stuff from the newly created
	 * Sections in the new KDOM to the remaining stuff from the last version of
	 * the article to get a consistent result.
	 * <p/>
	 * 
	 * <b>Attention:</b> Be aware, that the not reused Sections of the last KDOM
	 * may point to reused children that are now hooked in the new KDOM and
	 * themselves only point to Sections inside the new KDOM. So be careful if
	 * you navigate through the KDOM you have access to with the given Section!
	 * <p/>
	 * 
	 * <b>Attention:</b> In case of a full parse (<tt>article.isFullParse() == 
	 * true</tt>), this method will not be executed! Your handler needs to act
	 * like after the restart of the Wiki. However, it is not the duty of the
	 * handler to restore the clean state by destroying everything old by
	 * iterating over all Section and remove their stuff piece by piece. This
	 * restoring of the clean state has to be done somewhere else, e.g. in the
	 * constructor of the KnowWEArticle by checking <tt>isFullParse()</tt>.
	 * 
	 * 
	 * 
	 * @param article is the last version of the article that calls this
	 *        method... not necessarily the article the Section is hooked into
	 *        directly, since Sections can also be included!
	 * @param s is the old, not reused Section whose stuff you want to destroy
	 */
	public void destroy(KnowWEArticle article, Section<T> s) {

	}

}
