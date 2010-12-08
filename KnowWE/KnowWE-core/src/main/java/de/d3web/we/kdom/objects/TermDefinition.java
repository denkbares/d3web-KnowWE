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

package de.d3web.we.kdom.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.terminology.TerminologyHandler;
import de.d3web.we.utils.KnowWEUtils;

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
		extends DefaultAbstractKnowWEObjectType
		implements KnowWETerm<TermObject> {

	protected String key;

	protected Class<TermObject> termObjectClass;

	private int termScope = KnowWETerm.LOCAL;

	public TermDefinition(Class<TermObject> termObjectClass) {
		if (termObjectClass == null) {
			throw new IllegalArgumentException("termObjectClass can not be null");
		}
		this.termObjectClass = termObjectClass;
		this.key = termObjectClass.getName() + "_STORE_KEY";
	}

	public TermDefinition(Class<TermObject> termObjectClass, boolean register) {
		this(termObjectClass);
		if (register) {
			this.addSubtreeHandler(Priority.HIGHER,
					new TermDefinitionRegistrationHandler());
		}
	}

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
		Section<?> s2 = KnowWEUtils.getTerminologyHandler(article.getWeb()).getTermDefiningSection(
				article, s);
		return (TermObject) KnowWEUtils.getStoredObject(article, s2 != null ? s2 : s, key);
	}

	/**
	 * If a Section is not reused in the current KDOM, its stored object will
	 * not be found in the current SectionStore (unlike the stored object of
	 * reused Sections). It will however still reside in the last SectionStore,
	 * so you can use this method to sill get it from there, e.g. to destroy it
	 * in the method destroy in the SubtreeHandler.
	 */
	@SuppressWarnings("unchecked")
	public TermObject getTermObjectFromLastVersion(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s) {
		return (TermObject) KnowWEUtils.getObjectFromLastVersion(article, s, key);
	}

	/**
	 * When the actual object is created, it should be stored via this method
	 * This allows quick and simple access to the object via getObject() when
	 * needed for the further compilation process
	 */
	public void storeTermObject(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s, TermObject q) {
		KnowWEUtils.storeObject(article, s, key, q);
	}

	@Override
	public int getTermScope() {
		return this.termScope;
	}

	@Override
	public void setTermScope(int termScope) {
		this.termScope = termScope;
	}

	/**
	 * 
	 * This handler registers this Term..
	 * 
	 * @author Jochen
	 * @created 08.10.2010
	 */
	class TermDefinitionRegistrationHandler extends SubtreeHandler<TermDefinition<TermObject>> {


		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TermDefinition<TermObject>> s) {

			TerminologyHandler tHandler = KnowWEUtils.getTerminologyHandler(article.getWeb());

			Section<? extends TermDefinition<TermObject>> before = tHandler.getTermDefiningSection(
					article, s);

			tHandler.registerTermDefinition(article, s);

			Section<? extends TermDefinition<TermObject>> after = tHandler.getTermDefiningSection(
					article, s);

			if (before == after) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedError(
						s.get().getName()
								+ ": " + s.get().getTermName(s), before));
			}

			return new ArrayList<KDOMReportMessage>(0);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<TermDefinition<TermObject>> s) {
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
					article, s);
		}

	}

}
