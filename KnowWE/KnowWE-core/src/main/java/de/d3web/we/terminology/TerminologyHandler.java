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

package de.d3web.we.terminology;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.NotUniqueKnowWETerm;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;

/**
 * @author Jochen, Albrecht
 * 
 *         This class manages the definition and usage of terms. A term
 *         represents some kind of object. For each term that is defined in the
 *         wiki (and registered here) it stores the location where it has been
 *         defined. Further, for any reference also the locations are stored.
 *         The service of this manager is, that for a given term the definition
 *         and the references can be asked for. Obviously, this only works if
 *         the terms are registered here.
 *         <p/>
 * 
 *         TODOs:
 *         <p/>
 *         1. Add more class-checks!! Right now it is possible to override
 *         TermReferenceLogs with other Logs with the same term name but a
 *         different TermObject-class. Maybe it should be possible to store all
 *         TermReferenceLogs with the same term name but different
 *         TermObject-classes?
 *         <p/>
 *         2. This should not be a KnowledgerepresentationHandler. Stuff in the
 *         initArticle(...)-method can be done using e.g. a FullParseEvent.
 * 
 * 
 * 
 */
public class TerminologyHandler implements KnowledgeRepresentationHandler {

	public final static String HANDLER_KEY = TerminologyHandler.class.getSimpleName();

	private final Map<String, Boolean> modifiedTermDefinitions = new HashMap<String, Boolean>();

	@SuppressWarnings("unused")
	private String web;

	@SuppressWarnings("unchecked")
	private final Map<String, Map<TermIdentifier, TermReferenceLog>> termReferenceLogsMaps =
			new HashMap<String, Map<TermIdentifier, TermReferenceLog>>();

	@SuppressWarnings("unchecked")
	private final Map<TermIdentifier, TermReferenceLog> globalTermReferenceLogs =
			new HashMap<TermIdentifier, TermReferenceLog>();

	// private static TerminologyHandler instance = null;
	//
	// public static TerminologyHandler getInstance() {
	// if (instance == null) {
	// instance = new TerminologyHandler();
	//
	// }
	//
	// return instance;
	// }

	/**
	 * <b>This constructor SHOULD NOT BE USED!</b>
	 * <p/>
	 * Use KnowWEUtils.getTerminologyHandler(String web) instead!
	 */
	public TerminologyHandler(String web) {
		this.web = web;
	}

	public TerminologyHandler() {
		this.web = KnowWEEnvironment.DEFAULT_WEB;
	}

	@SuppressWarnings("unchecked")
	private Map<TermIdentifier, TermReferenceLog> getTermReferenceLogsMap(String title, int termScope) {
		if (termScope == KnowWETerm.GLOBAL) {
			return this.globalTermReferenceLogs;
		}
		Map<TermIdentifier, TermReferenceLog> tmap = termReferenceLogsMaps.get(title);
		if (tmap == null) {
			tmap = new HashMap<TermIdentifier, TermReferenceLog>();
			termReferenceLogsMaps.put(title, tmap);
		}
		return tmap;
	}

	@SuppressWarnings("unchecked")
	private <TermObject> TermReferenceLog<TermObject> getTermReferenceLog(KnowWEArticle article,
			Section<? extends KnowWETerm<TermObject>> r) {
		TermReferenceLog refLog = getTermReferenceLogsMap(article.getTitle(),
				r.get().getTermScope()).get(
				new TermIdentifier(article, r));
		if (refLog != null && refLog.getTermObjectClass().equals(r.get().getTermObjectClass())) {
			return refLog;
		}
		else {
			return null;
		}
	}

	private TermReferenceLog<?> getTermReferenceLog(KnowWEArticle article, String termName, int termScope) {
		return getTermReferenceLogsMap(article.getTitle(), termScope).get(
				new TermIdentifier(termName));
	}

	@SuppressWarnings("unchecked")
	private void removeTermReferenceLogsForArticle(KnowWEArticle article) {
		Map<TermIdentifier, TermReferenceLog> logs = getTermReferenceLogsMap(article.getTitle(),
				KnowWETerm.LOCAL);
		for (TermReferenceLog log : new LinkedList<TermReferenceLog>(logs.values())) {
			if (log.termDefiningSection != null) {
				this.unregisterTermDefinition(article, log.termDefiningSection);
			}
		}
		termReferenceLogsMaps.remove(article.getTitle());

		logs = getTermReferenceLogsMap(article.getTitle(),
				KnowWETerm.GLOBAL);
		for (TermReferenceLog log : new LinkedList<TermReferenceLog>(logs.values())) {
			if (log.termDefiningSection != null &&
					log.termDefiningSection.getArticle().getTitle().equals(article.getTitle())) {
				this.unregisterTermDefinition(article, log.termDefiningSection);
			}
			else {
				for (Section<?> redTermDef : new ArrayList<Section<?>>(
						log.getRedundantDefinitions())) {
					if (redTermDef.getTitle().equals(article.getTitle())) {
						log.getRedundantDefinitions().remove(redTermDef);
					}
				}
				for (Section<?> termRef : new ArrayList<Section<?>>(
						log.getReferences())) {
					if (termRef.getTitle().equals(article.getTitle())) {
						log.getReferences().remove(termRef);
					}
				}
			}
		}
	}

	public void initArticle(KnowWEArticle article) {
		if (article.isFullParse()) {
			removeTermReferenceLogsForArticle(article);
		}
	}

	public void finishArticle(KnowWEArticle article) {
		modifiedTermDefinitions.put(article.getTitle(), false);
	}

	/**
	 * Allows to register a new term.
	 * 
	 * @param d is the term defining section.
	 * @param <TermObject>
	 */
	public <TermObject> void registerTermDefinition(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> d) {

		Priority p = article.getReviseIterator().getCurrentPriority();
		TermIdentifier termIdentifier = new TermIdentifier(article, d);
		TermReferenceLog<TermObject> termRefLog = getTermReferenceLog(article, d);
		if (termRefLog != null) {
			KnowWEArticleManager artMan = KnowWEEnvironment.getInstance().getArticleManager(
					article.getWeb());
			if (termRefLog.termDefiningSection != null) {
				if (termRefLog.termDefiningSection == d || termRefLog.getRedundantDefinitions().contains(d)) {
					// this should not happen
					Logger.getLogger(this.getClass().getName())
							.log(Level.WARNING, "Tried to register same TermDefinition twice: '" +
									termIdentifier + "'!");
					// now registration will be ignored
					return;
				}
				else if (termRefLog.priorityOfDefiningSection.compareTo(p) > 0
							|| (termRefLog.priorityOfDefiningSection.compareTo(p) == 0
									&& termRefLog.termDefiningSection.compareTo(d) < 0)) {
					// If there is already a definition with higher priority or
					// the same priority but further up in the article, this
					// definition is redundant.
					termRefLog.addRedundantTermDefinition(d);
					return;
				}
				else {
					// A TermDefinition was added before another already
					// existing or with higher priority than the existing
					// TermDefinition with the same term....
					// If the scope is LOCAL, we need a full reparse, because
					// we are already past the destroy step.
					// If the scope is GLOBAL, we need a full reparse for the
					// same reason, but only if the last TermDefinition
					// originates from the article currently compiled. If the
					// Section is from a different article, it will compiled
					// there.
					if (d.get().getTermScope() == KnowWETerm.GLOBAL) {
						artMan.addAllArticlesToRefresh(termRefLog.termDefiningSection.getReusedBySet());
						termRefLog.termDefiningSection.clearReusedBySet();
						for (Section<?> termDef : termRefLog.getRedundantDefinitions()) {
							artMan.addAllArticlesToRefresh(termDef.getReusedBySet());
							termDef.clearReusedBySet();
						}
						for (Section<?> termRef : termRefLog.getReferences()) {
							artMan.addAllArticlesToRefresh(termRef.getReusedBySet());
							termRef.clearReusedBySet();
						}
					}
					if (d.get().getTermScope() == KnowWETerm.LOCAL
							|| termRefLog.termDefiningSection.getArticle().getTitle().equals(
							article.getTitle())) {
						article.setFullParse(this.getClass());
						return;
					}
				}
			}
			else {
				// if the TermDefinition was null before, the
				// TermReferences need to be compiled again, because there is
				// now a TermDefinition to refer to
				for (Section<?> termRef : termRefLog.getReferences()) {
					artMan.addAllArticlesToRefresh(termRef.getReusedBySet());
					termRef.clearReusedBySet();
				}
			}
		}
		getTermReferenceLogsMap(article.getTitle(), d.get().getTermScope()).put(termIdentifier,
				new TermReferenceLog<TermObject>(d.get().getTermObjectClass(), d, p));
		modifiedTermDefinitions.put(article.getTitle(), true);
	}

	public <TermObject> void registerTermReference(KnowWEArticle article, Section<? extends TermReference<TermObject>> r) {
		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, r);
		if (refLog == null) {
			refLog = new TermReferenceLog<TermObject>(r.get().getTermObjectClass(), null, null);
			getTermReferenceLogsMap(article.getTitle(), r.get().getTermScope()).put(
					new TermIdentifier(article, r), refLog);
		}
		refLog.addTermReference(r);
	}

	/**
	 * Returns whether a term is defined through an TermDefinition
	 */
	public boolean isDefinedTerm(KnowWEArticle article, String termName, int termScope) {
		TermReferenceLog<?> termRef = getTermReferenceLog(article, termName, termScope);
		if (termRef != null) {
			return termRef.termDefiningSection != null;
		}
		return false;
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no
	 * TermDefinition
	 */
	public boolean isUndefinedTerm(KnowWEArticle article, String termName, int termScope) {
		TermReferenceLog<?> termRef = getTermReferenceLog(article, termName, termScope);
		if (termRef != null) {
			return termRef.termDefiningSection == null;
		}
		return false;
	}

	/**
	 * For a TermName the TermDefinition is returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Section<? extends TermDefinition> getTermDefiningSection(
			KnowWEArticle article, String termName, int termScope) {

		TermReferenceLog refLog = getTermReferenceLog(article, termName, termScope);

		if (refLog != null) {
			return refLog.termDefiningSection;
		}

		return null;
	}

	/**
	 * For a TermName the redundant TermDefinition are returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<Section<? extends TermDefinition>> getRedundantTermDefiningSections(
			KnowWEArticle article, String termName, int termScope) {

		TermReferenceLog refLog = getTermReferenceLog(article, termName, termScope);

		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getRedundantDefinitions());
		}

		return Collections.unmodifiableSet(new HashSet<Section<? extends TermDefinition>>(
				0));
	}

	@SuppressWarnings("unchecked")
	public Set<Section<? extends TermReference>> getTermReferenceSections(KnowWEArticle article,
			String termName, int termScope) {
		TermReferenceLog refLog = getTermReferenceLog(article, termName, termScope);

		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getReferences());
		}

		return Collections.unmodifiableSet(new HashSet<Section<? extends TermReference>>(0));
	}

	/**
	 * Returns whether a term is defined through an TermDefinition
	 */
	public <TermObject> boolean isDefinedTerm(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			return termRef.termDefiningSection != null;
		}
		return false;
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no
	 * TermDefinition
	 */
	public <TermObject> boolean isUndefinedTerm(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			return termRef.termDefiningSection == null;
		}
		return false;
	}

	/**
	 * For a TermReference the TermDefinition is returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	public <TermObject> Section<? extends TermDefinition<TermObject>> getTermDefiningSection(
			KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {

		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, r);

		if (refLog != null) {
			return refLog.termDefiningSection;
		}

		return null;
	}

	/**
	 * For a TermName the redundant TermDefinition are returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	public <TermObject> Collection<Section<? extends TermDefinition<TermObject>>> getRedundantTermDefiningSections(
			KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {

		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, r);

		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getRedundantDefinitions());
		}

		return Collections.unmodifiableSet(new HashSet<Section<? extends TermDefinition<TermObject>>>(
				0));
	}

	public <TermObject> Set<Section<? extends TermReference<TermObject>>> getTermReferenceSections(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {

		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, r);

		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getReferences());
		}

		return Collections.unmodifiableSet(new HashSet<Section<? extends TermReference<TermObject>>>(
				0));
	}

	// public <TermObject> void setTermReferencesToNotReused(KnowWEArticle
	// article,
	// Section<? extends TermDefinition<TermObject>> r) {
	//
	// Set<Section<? extends TermReference<TermObject>>> refs =
	// getTermReferenceSections(
	// article, r);
	//
	// for (Section<?> ref : refs) {
	// ref.setReusedBy(article.getTitle(), false);
	// }
	// }

	public <TermObject> void unregisterTermDefinition(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> d) {
		TermReferenceLog<TermObject> termRefLog = getTermReferenceLog(article, d);
		if (termRefLog != null) {
			if (d == termRefLog.termDefiningSection) {

				if (d.get().getTermScope() == KnowWETerm.GLOBAL) {
					KnowWEArticleManager artMan = KnowWEEnvironment.getInstance().getArticleManager(
							article.getWeb());

					artMan.addAllArticlesToRefresh(termRefLog.termDefiningSection.getReusedBySet());
					termRefLog.termDefiningSection.clearReusedBySet();

					for (Section<?> termDef : termRefLog.getRedundantDefinitions()) {
						artMan.addAllArticlesToRefresh(termDef.getReusedBySet());
						termDef.clearReusedBySet();
					}

					for (Section<?> termRef : termRefLog.getReferences()) {
						artMan.addAllArticlesToRefresh(termRef.getReusedBySet());
						termRef.clearReusedBySet();
					}

				}
				else {
					for (Section<?> termDef : termRefLog.getRedundantDefinitions()) {
						termDef.setReusedBy(article.getTitle(), false);
					}
					for (Section<?> termRef : termRefLog.getReferences()) {
						termRef.setReusedBy(article.getTitle(), false);
					}
				}
				getTermReferenceLogsMap(article.getTitle(), d.get().getTermScope()).remove(
						new TermIdentifier(article, d));
			}
			else {
				termRefLog.getRedundantDefinitions().remove(d);
			}

		}
		modifiedTermDefinitions.put(article.getTitle(), true);
	}

	public <TermObject> void unregisterTermReference(KnowWEArticle article, Section<? extends TermReference<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			termRef.termReferingSections.remove(r);
		}
	}

	public boolean areTermDefinitionsModifiedFor(KnowWEArticle article) {
		if (!modifiedTermDefinitions.containsKey(article.getTitle())) {
			return false;
		}
		return modifiedTermDefinitions.get(article.getTitle());
	}

	/**
	 * Returns all global terms of the given class (e.g. Question, String,...).
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllGlobalTermsOfType(Class<?> termClass) {
		return getAllTerms(null, KnowWETerm.GLOBAL, termClass);
	}

	/**
	 * Returns all global terms.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllGlobalTerms() {
		return getAllTerms(null, KnowWETerm.GLOBAL, null);
	}

	/**
	 * Returns all local terms of the given class (e.g. Question, String,...),
	 * that are compiled in the article with the given title.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllLocalTermsOfType(String title, Class<?> termClass) {
		return getAllTerms(title, KnowWETerm.LOCAL, termClass);
	}

	/**
	 * Returns all local terms that are compiled in the article with the given
	 * title.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllLocalTerms(String title) {
		return getAllTerms(title, KnowWETerm.LOCAL, null);
	}

	@SuppressWarnings("unchecked")
	public Collection<String> getAllTerms(String title, int scope, Class<?> termClass) {
		Collection<TermReferenceLog> logs = getTermReferenceLogsMap(title, scope).values();
		Collection<String> terms = new HashSet<String>();
		for (TermReferenceLog tl : logs) {
			if (tl.termDefiningSection != null
					&& (termClass == null || tl.getTermObjectClass().isAssignableFrom(termClass))) {
				terms.add(new TermIdentifier(tl.termDefiningSection.getArticle(),
						tl.termDefiningSection).toString());
			}
		}
		return terms;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return TerminologyHandler.HANDLER_KEY;
	}

	@Override
	public URL saveKnowledge(String title) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWeb(String web) {
		this.web = web;
	}

	/**
	 * 
	 * This is an auxiliary data-structure to store the definitions and
	 * references of terms
	 * 
	 * @author Jochen
	 * 
	 * @param <TermObject>
	 */
	class TermReferenceLog<TermObject> {

		private final Priority priorityOfDefiningSection;

		private final Section<? extends TermDefinition<TermObject>> termDefiningSection;

		private final Set<Section<? extends TermDefinition<TermObject>>> redundantTermDefiningSections =
				new HashSet<Section<? extends TermDefinition<TermObject>>>();

		private final Set<Section<? extends TermReference<TermObject>>> termReferingSections =
				new HashSet<Section<? extends TermReference<TermObject>>>();

		private final Class<TermObject> termObjectClass;

		public TermReferenceLog(Class<TermObject> termObjectClass, Section<? extends TermDefinition<TermObject>> d, Priority p) {
			if (termObjectClass == null) {
				throw new IllegalArgumentException("termObjectClass can not be null");
			}
			this.priorityOfDefiningSection = p;
			this.termObjectClass = termObjectClass;
			this.termDefiningSection = d;
		}

		public Class<TermObject> getTermObjectClass() {
			return this.termObjectClass;
		}

		public void addRedundantTermDefinition(Section<? extends TermDefinition<TermObject>> d) {
			redundantTermDefiningSections.add(d);
		}

		public Set<Section<? extends TermDefinition<TermObject>>> getRedundantDefinitions() {
			return redundantTermDefiningSections;
		}

		public void addTermReference(Section<? extends TermReference<TermObject>> r) {
			termReferingSections.add(r);
		}

		public Set<Section<? extends TermReference<TermObject>>> getReferences() {
			return termReferingSections;
		}

	}

	private class TermIdentifier {

		private final String termIdentifier;

		private final String termIdentifierLowerCase;

		@SuppressWarnings("unchecked")
		public TermIdentifier(KnowWEArticle article, Section<? extends KnowWETerm> s) {
			if (s.get() instanceof NotUniqueKnowWETerm) {
				Section<? extends NotUniqueKnowWETerm> nus = (Section<? extends NotUniqueKnowWETerm>) s;
				termIdentifier = nus.get().getUniqueTermIdentifier(article, nus);
			}
			else {
				termIdentifier = s.get().getTermName(s);
			}
			this.termIdentifierLowerCase = this.termIdentifier.toLowerCase();
		}

		public TermIdentifier(String termName) {
			this.termIdentifier = termName;
			this.termIdentifierLowerCase = this.termIdentifier.toLowerCase();
		}

		@Override
		public int hashCode() {
			return termIdentifierLowerCase.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			TermIdentifier other = (TermIdentifier) obj;
			if (termIdentifierLowerCase == null) {
				if (other.termIdentifierLowerCase != null) {
					return false;
				}
			}
			else if (!termIdentifierLowerCase.equals(other.termIdentifierLowerCase)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return termIdentifier;
		}

	}

}
