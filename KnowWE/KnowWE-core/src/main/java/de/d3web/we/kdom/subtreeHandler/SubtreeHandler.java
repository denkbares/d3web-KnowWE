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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule.Operator;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule.Purpose;

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

	private boolean ignorePackageCompile;

	private final List<ConstraintModule<T>> constraintModulesDONT = new ArrayList<ConstraintModule<T>>();
	private final List<ConstraintModule<T>> constraintModulesDO = new ArrayList<ConstraintModule<T>>();

	/**
	 * Creates a new SubtreeHandler.
	 * 
	 * @param ignorePackageCompile: If the handler ignores package compile, it
	 *        will always run independently of any packages, but only for the
	 *        article, the section directly belongs to.
	 */
	public SubtreeHandler(boolean ignorePackageCompile) {
		setIgnorePackageCompile(ignorePackageCompile);
		registerConstraintModule(new CreateConstraintsDO<T>(this));
		registerConstraintModule(new CreateConstraintsDONT<T>());
		registerConstraintModule(new DestroyConstraintsDO<T>());
		registerConstraintModule(new DestroyConstraintsDONT<T>(this));
	}

	public SubtreeHandler() {
		this(false);
	}

	/**
	 * If the handler ignores package compile, it will always run independently
	 * of any packages, but only for the article, the section directly belongs
	 * to.
	 * 
	 * @created 21.01.2011
	 */
	public void setIgnorePackageCompile(boolean ignore) {
		this.ignorePackageCompile = ignore;
	}

	/**
	 * If the handler ignores package compile, it will always run independently
	 * of any packages, but only for the article, the section directly belongs
	 * to.
	 * 
	 * @created 21.01.2011
	 * @returns whether the handler ignores package compile or not.
	 */
	public boolean isIgnoringPackageCompile() {
		return this.ignorePackageCompile;
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
	public final boolean needsToCreate(KnowWEArticle article, Section<T> s) {
		for (ConstraintModule<T> cm : constraintModulesDONT) {
			if (cm.PURPOSE.equals(Purpose.DESTROY)) continue;
			if (cm.violatedConstraints(article, s)) {
				return false;
			}
		}
		for (ConstraintModule<T> cm : constraintModulesDO) {
			if (cm.PURPOSE.equals(Purpose.DESTROY)) continue;
			if (cm.violatedConstraints(article, s)) {
				return true;
			}
		}
		return false;
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
	public final boolean needsToDestroy(KnowWEArticle article, Section<T> s) {
		for (ConstraintModule<T> cm : constraintModulesDONT) {
			if (cm.PURPOSE.equals(Purpose.CREATE)) continue;
			if (cm.violatedConstraints(article, s)) {
				return false;
			}
		}
		for (ConstraintModule<T> cm : constraintModulesDO) {
			if (cm.PURPOSE.equals(Purpose.CREATE)) continue;
			if (cm.violatedConstraints(article, s)) {
				return true;
			}
		}
		return false;
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

	/**
	 * Registers the given constraint to the handler.
	 * 
	 * @created 21.01.2011
	 * @param cm is the constraint to register.
	 */
	public void registerConstraintModule(ConstraintModule<T> cm) {
		registerConstraintModule(Integer.MAX_VALUE, cm);
	}

	/**
	 * Registers the given constraint to the handler.
	 * 
	 * @created 21.01.2011
	 * @param pos determines the position of the constraint in the list of
	 *        constraints checked by this handler.
	 * @param cm is the constraint to register.
	 * 
	 */
	public void registerConstraintModule(int pos, ConstraintModule<T> cm) {
		List<ConstraintModule<T>> modules;
		if (cm.OPERATOR.equals(Operator.DONT_COMPILE_IF_VIOLATED)) {
			modules = constraintModulesDONT;
		}
		else {
			modules = constraintModulesDO;
		}
		if (pos > modules.size() || pos < 0) {
			modules.add(cm);
		}
		else {
			modules.add(pos, cm);
		}
	}

	public void unregisterConstraintModulesOfType(Class<ConstraintModule<T>> moduleClass) {
		ArrayList<ConstraintModule<T>> modulesToremove = new ArrayList<ConstraintModule<T>>();
		for (ConstraintModule<T> module : constraintModulesDONT) {
			if (moduleClass.isAssignableFrom(module.getClass())) {
				modulesToremove.add(module);
			}
		}
		constraintModulesDONT.removeAll(modulesToremove);
		modulesToremove = new ArrayList<ConstraintModule<T>>();
		for (ConstraintModule<T> module : constraintModulesDO) {
			if (moduleClass.isAssignableFrom(module.getClass())) {
				modulesToremove.add(module);
			}
		}
		constraintModulesDO.removeAll(modulesToremove);
	}

	public void unregisterAllConstraintModules() {
		constraintModulesDONT.clear();
		constraintModulesDO.clear();
	}

	// ++++++++++++++++++++++ Constraint classes ++++++++++++++++++++++ //

	private class CreateConstraintsDONT<T2 extends KnowWEObjectType> extends ConstraintModule<T2> {

		public CreateConstraintsDONT() {
			super(Operator.DONT_COMPILE_IF_VIOLATED, Purpose.CREATE);
		}

		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<T2> s) {

			boolean firstBuild = !article.isSecondBuild();
			boolean postDestroyFullParse = article.isPostDestroyFullParse();

			return firstBuild && postDestroyFullParse;
		}
		
	}

	
	private class CreateConstraintsDO<T2 extends KnowWEObjectType> extends ConstraintModule<T2> {

		private final SubtreeHandler<T2> handler;

		public CreateConstraintsDO(SubtreeHandler<T2> handler) {
			super(Operator.COMPILE_IF_VIOLATED, Purpose.CREATE);
			this.handler = handler;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<T2> s) {

			boolean fullparse = article.isFullParse();
			boolean notCompiled = !s.isCompiledBy(article.getTitle(), handler);
			boolean notReused = !s.isReusedBy(article.getTitle());
			boolean changedPosition = s.get().isOrderSensitive()
					&& s.isPositionChangedFor(article.getTitle());
			boolean typeConstraint = s.get() instanceof IncrementalConstraint
					&& ((IncrementalConstraint<T2>) s.get()).violatedConstraints(
					article, s);

			return fullparse || notCompiled || notReused || changedPosition || typeConstraint;
		}

	}

	private class DestroyConstraintsDONT<T2 extends KnowWEObjectType> extends ConstraintModule<T2> {

		SubtreeHandler<T2> handler;

		public DestroyConstraintsDONT(SubtreeHandler<T2> handler) {
			super(Operator.DONT_COMPILE_IF_VIOLATED, Purpose.DESTROY);
			this.handler = handler;
		}

		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<T2> s) {

			boolean fullparse = article.isFullParse();
			boolean notCompiled = !s.isCompiledBy(article.getTitle(), handler);

			return fullparse || notCompiled;
		}

	}

	private class DestroyConstraintsDO<T2 extends KnowWEObjectType> extends ConstraintModule<T2> {

		public DestroyConstraintsDO() {
			super(Operator.COMPILE_IF_VIOLATED, Purpose.DESTROY);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<T2> s) {
			boolean notReused = !s.isReusedBy(article.getTitle());
			boolean changedPosition = s.get().isOrderSensitive() && s.isPositionChangedFor(article.getTitle());
			boolean typeConstraint = (s.get() instanceof IncrementalConstraint<?>
			 && ((IncrementalConstraint<T2>) s.get()).violatedConstraints(
							article, s));
			return notReused || changedPosition || typeConstraint;
		}

	}

}
