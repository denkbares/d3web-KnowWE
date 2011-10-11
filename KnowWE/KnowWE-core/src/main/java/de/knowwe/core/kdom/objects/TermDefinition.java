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

import de.knowwe.core.compile.TerminologyHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * A type representing a text slice which _defines_ an object (class, instance,
 * question, whatever) i.e., there should be some compilation script
 * (SubtreeHandler) to actually _create_ and store the object.
 * 
 * This should NOT be used for object references @see {@link TermReference}
 * 
 * @author Jochen, Albrecht
 * 
 * @param <TermObject>
 */
public abstract class TermDefinition<TermObject>
		extends AbstractType
		implements KnowWETerm<TermObject> {

	public static enum MultiDefMode {
		/**
		 * Multiple definitions of one term are allowed. If there is more than
		 * one definition of a term, the additional definitions are just
		 * references to the actual definition (highest in priority and first in
		 * the text).
		 */
		ACTIVE,
		/**
		 * Multiple definitions of one term are not allowed. A definition is
		 * only valid if there is exactly one of it.
		 */
		INACTIVE
	}

	protected String key;

	protected Class<TermObject> termObjectClass;

	private Scope termScope = Scope.LOCAL;

	private MultiDefMode multiDefMode = MultiDefMode.ACTIVE;

	public TermDefinition(Class<TermObject> termObjectClass) {
		if (termObjectClass == null) {
			throw new IllegalArgumentException("termObjectClass can not be null");
		}
		this.termObjectClass = termObjectClass;
		this.key = termObjectClass.getName() + "_STORE_KEY";
	}

	@Override
	public Class<TermObject> getTermObjectClass() {
		return this.termObjectClass;
	}

	/**
	 * Allows quick and simple access to the object defined by this section, if
	 * it was stored using storeObject()
	 */
	@SuppressWarnings("unchecked")
	public TermObject getTermObject(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s) {
		// in case the of duplicate definitions, get the one that has actually
		// created the TermObject
		TerminologyHandler tHandler = KnowWEUtils.getTerminologyHandler(s.getWeb());
		Section<? extends TermDefinition<TermObject>> defSec = tHandler.getTermDefiningSection(
				article, s);
		if (defSec != null) s = defSec;
		if (!tHandler.isDefinedTerm(article, s)) return null;
		return (TermObject) KnowWEUtils.getStoredObject(
				s.get().getTermScope() == Scope.GLOBAL ? null : article, s, key);
	}

	/**
	 * When the actual object is created, it should be stored via this method
	 * This allows quick and simple access to the object via getObject() when
	 * needed for the further compilation process
	 */
	public void storeTermObject(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s, TermObject q) {
		KnowWEUtils.storeObject(s.get().getTermScope() == Scope.GLOBAL ? null : article, s,
				key, q);
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

	public MultiDefMode getMultiDefMode() {
		return this.multiDefMode;
	}

	public void setMultiDefMode(MultiDefMode mode) {
		this.multiDefMode = mode;
	}

	@Override
	public String getTermName(Section<? extends KnowWETerm<TermObject>> s) {
		// As default the term name is identical with the identifier
		// however, this method may be overridden
		return getTermIdentifier(s);
	}

}
