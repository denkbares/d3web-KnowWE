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

package de.knowwe.core.kdom.objects;

import java.util.Arrays;
import java.util.Collection;

import de.knowwe.core.compile.TerminologyHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.TermDefinition.MultiDefMode;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.KDOMReportMessage;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.report.message.NoSuchObjectError;
import de.knowwe.report.message.ObjectFound;

/**
 * A type representing a text slice, which _references_ an (existing) Object. It
 * comes along with a ReviseHandler that checks whether the referenced object is
 * existing and throws an error if not.
 * 
 * This should not be used for types _creating_ objects @link
 * {@link TermDefinition}
 * 
 * 
 * @author Jochen, Albrecht
 * 
 * @param <TermObject>
 */
public abstract class TermReference<TermObject>
		extends AbstractType
		implements KnowWETerm<TermObject> {

	private Scope termScope = Scope.LOCAL;

	protected Class<TermObject> termObjectClass;

	public TermReference(Class<TermObject> termObjectClass) {
		if (termObjectClass == null) {
			throw new IllegalArgumentException("termObjectClass can not be null");
		}
		this.termObjectClass = termObjectClass;
		this.addSubtreeHandler(new TermRegistrationHandler());
	}

	@Override
	public Class<TermObject> getTermObjectClass() {
		return this.termObjectClass;
	}

	public abstract String getTermObjectDisplayName();

	@Override
	public String getTermName(Section<? extends KnowWETerm<TermObject>> s) {
		// As default the term name is identical with the identifier
		// however, this method may be overridden
		return getTermIdentifier(s);
	}

	/**
	 * Allows quick and simple access to the object this sections is referring
	 * to.
	 */
	public final TermObject getTermObject(KnowWEArticle article, Section<? extends TermReference<TermObject>> s) {
		TerminologyHandler tHandler = KnowWEUtils.getTerminologyHandler(s.getWeb());
		Section<? extends TermDefinition<TermObject>> defSec = tHandler.getTermDefiningSection(
				article, s);
		if (defSec != null) {
			TermObject c = defSec.get().getTermObject(article, defSec);
			if (c != null) return c;
		}
		if (defSec == null || defSec.get().getMultiDefMode() == MultiDefMode.ACTIVE
				|| tHandler.getRedundantTermDefiningSections(article, defSec).isEmpty()) {
			return getTermObjectFallback(article, s);
		}
		return null;
	}

	/**
	 * Fallback method in case the object isn't defined via an ObjectDef
	 * respectively in the TerminologyManager.
	 */
	public TermObject getTermObjectFallback(KnowWEArticle article, Section<? extends TermReference<TermObject>> s) {
		return null;
	}

	@Override
	public Scope getTermScope() {
		return this.termScope;
	}

	@Override
	public void setTermScope(Scope termScope) {
		this.termScope = termScope;
		if (termScope == Scope.GLOBAL) {
			this.setIgnorePackageCompile(true);
		}
		else {
			this.setIgnorePackageCompile(false);
		}
	}

	class TermRegistrationHandler extends SubtreeHandler<TermReference<TermObject>> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TermReference<TermObject>> s) {

			KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermReference(
					article, s);

			String termName = s.get().getTermIdentifier(s);

			if (s.get().getTermObject(article, s) == null) {
				return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(
						s.get().getTermObjectDisplayName(),
						termName));
			}

			// TODO: give meaningful information about the object
			// e.g. type, range, where it has been defined, ...
			// this functionality should be included automatically in
			// "ObjectFound"
			return Arrays.asList((KDOMReportMessage) new ObjectFound(s.get().getName()));
		}

		@Override
		public void destroy(KnowWEArticle article, Section<TermReference<TermObject>> s) {
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermReference(
					article, s);
		}

	}

}
