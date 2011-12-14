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

package de.knowwe.core.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.KnowWEArticleManager;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.KnowWETerm;
import de.knowwe.core.kdom.objects.KnowWETerm.Scope;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.objects.TermDefinition.MultiDefMode;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.event.ArticleCreatedEvent;
import de.knowwe.event.FullParseEvent;
import de.knowwe.plugin.Plugins;

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
 * 
 */
public class TerminologyHandler implements EventListener {

	private final String web;

	public final static String HANDLER_KEY = TerminologyHandler.class.getSimpleName();

	private final Set<String> modifiedTermDefinitions = new HashSet<String>();

	private final Set<String> occupiedTerms = new HashSet<String>();

	@SuppressWarnings("rawtypes")
	private final Map<String, Map<TermIdentifier, TermReferenceLog>> termReferenceLogsMaps =
			new HashMap<String, Map<TermIdentifier, TermReferenceLog>>();

	@SuppressWarnings("rawtypes")
	private final Map<TermIdentifier, TermReferenceLog> globalTermReferenceLogs =
			new HashMap<TermIdentifier, TermReferenceLog>();

	public TerminologyHandler(String web) {
		this.web = web;
		EventManager.getInstance().registerListener(this);
		// extension point for plugins defining predefined terminology
		Extension[] exts = PluginManager.getInstance().getExtensions(
				Plugins.EXTENDED_PLUGIN_ID,
				Plugins.EXTENDED_POINT_TERMINOLOGY);
		for (Extension extension : exts) {
			Object o = extension.getSingleton();
			if (o instanceof TerminologyExtension) {
				registerTerminology(((TerminologyExtension) o));
			}
		}
	}

	private void registerTerminology(TerminologyExtension terminologyExtension) {
		occupiedTerms.addAll(Arrays.asList(terminologyExtension.getTermNames()));
	}

	public String getWeb() {
		return web;
	}

	@SuppressWarnings("rawtypes")
	private Map<TermIdentifier, TermReferenceLog> getTermReferenceLogsMap(String title, Scope termScope) {
		if (termScope == Scope.GLOBAL) {
			return this.globalTermReferenceLogs;
		}
		Map<TermIdentifier, TermReferenceLog> tmap = termReferenceLogsMaps.get(title);
		if (tmap == null) {
			tmap = new HashMap<TermIdentifier, TermReferenceLog>();
			termReferenceLogsMaps.put(title, tmap);
		}
		return tmap;
	}

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	private <TermObject> TermReferenceLog<TermObject> getTermReferenceLog(KnowWEArticle article,
			Section<? extends KnowWETerm<TermObject>> r) {
		TermReferenceLog refLog = getTermReferenceLogsMap(
				r.get().getTermScope() == Scope.GLOBAL ? null : article.getTitle(),
				r.get().getTermScope()).get(new TermIdentifier(article, r));
		if (refLog != null
				&& refLog.getTermObjectClass().equals(r.get().getTermObjectClass())) {
			return refLog;
		}
		else {
			return null;
		}
	}

	private TermReferenceLog<?> getTermReferenceLog(KnowWEArticle article, String termIdentifier, Scope termScope) {
		return getTermReferenceLogsMap(
				termScope == Scope.GLOBAL ? null : article.getTitle(),
				termScope).get(new TermIdentifier(termIdentifier));
	}

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	private void removeTermReferenceLogsForArticle(KnowWEArticle article) {
		Map<TermIdentifier, TermReferenceLog> logs = getTermReferenceLogsMap(
				article.getTitle(),
				Scope.LOCAL);
		for (TermReferenceLog log : new LinkedList<TermReferenceLog>(logs.values())) {
			if (log.getDefiningSection() != null) {
				this.unregisterTermDefinition(article, log.getDefiningSection());
			}
		}
		termReferenceLogsMaps.remove(article.getTitle());

		logs = getTermReferenceLogsMap(article.getTitle(),
				Scope.GLOBAL);
		for (TermReferenceLog log : new LinkedList<TermReferenceLog>(logs.values())) {
			if (log.getDefiningSection() != null
					&&
					log.getDefiningSection().getArticle().getTitle().equals(
							article.getTitle())) {
				this.unregisterTermDefinition(article, log.getDefiningSection());
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

	/**
	 * Allows to register a new term.
	 * 
	 * TODO: This methods gets huge... find a way to simplify or shorten it.
	 * 
	 * @param s is the term defining section.
	 * @param <TermObject>
	 * @returns true if the sections was registered as the defining section for
	 *          this term. false else.
	 */
	public <TermObject> boolean registerTermDefinition(KnowWEArticle article,
			Section<? extends TermDefinition<TermObject>> s) {

		Collection<Message> msgs = new LinkedList<Message>();
		TermIdentifier termIdentifier = new TermIdentifier(article, s);

		if (occupiedTerms.contains(termIdentifier.toString())) {
			msgs.add(Messages.objectCreationError("The term '"
					+ termIdentifier.toString()
					+ "' is reserved by the system."));
			Messages.storeMessages(article, s, this.getClass(), msgs);
			return false;
		}

		Priority p = article.getReviseIterator().getCurrentPriority();
		TermReferenceLog<TermObject> termRefLog = getTermReferenceLog(article, s);
		// if the termRefLog is null, it could still be, that there is a log
		// for the same term, but a different termObjectClass
		if (termRefLog == null) {
			TermReferenceLog<?> termRefLogWrongClass = getTermReferenceLog(article,
					new TermIdentifier(article, s).toString(), s.get().getTermScope());
			if (termRefLogWrongClass != null) {
				// don't override a termRefLog of another term object class
				msgs.add(Messages.occupiedTermError(termIdentifier.toString(),
						termRefLogWrongClass.getTermObjectClass()));

				// If there is no term defining section or a definition with
				// lower priority or with the same priority, but further down in
				// the article, then that old termRefLog is no longer valid
				if (termRefLogWrongClass.getDefiningSection() == null
						|| termRefLogWrongClass.getPriorityOfDefiningSection().compareTo(p) < 0
							|| (termRefLogWrongClass.getPriorityOfDefiningSection().compareTo(
									p) == 0
									&& termRefLogWrongClass.getDefiningSection().compareTo(
											s) > 0)) {

					globalRecompilationOfTerm(article, s, termRefLogWrongClass);
				}
				Messages.storeMessages(article, s, this.getClass(), msgs);
				return false;
			}
		}
		// so the termRefLog is not null
		else {
			// there is already a term defining section registered for this term
			if (termRefLog.getDefiningSection() != null) {
				if (termRefLog.getDefiningSection() == s
						|| termRefLog.getRedundantDefinitions().contains(s)) {
					// this should not happen
					Logger.getLogger(this.getClass().getName())
							.log(Level.WARNING,
									"Tried to register same TermDefinition twice: '" +
											termIdentifier + "'!");
					// now registration will be ignored
					Messages.storeMessages(article, s, this.getClass(), msgs);
					return false;
				}
				// If there is already a definition with higher priority or
				// the same priority but further up in the article, this
				// definition is redundant.
				else if (termRefLog.getPriorityOfDefiningSection().compareTo(p) > 0
							|| (termRefLog.getPriorityOfDefiningSection().compareTo(p) == 0
									&& termRefLog.getDefiningSection().compareTo(s) < 0)) {

					termRefLog.addRedundantTermDefinition(s, p);
					String termName = termRefLog.getDefiningSection().get().getTermIdentifier(
							termRefLog.getDefiningSection());
					String redTermName = s.get().getTermIdentifier(s);
					if (!termName.equals(redTermName)) {
						msgs.add(Messages.warning("Term name matches with wrong case. Expected: "
								+ termName));
					}
					if (termRefLog.getDefiningSection().get().getMultiDefMode() == MultiDefMode.INACTIVE) {
						globalRecompilationOfTerm(article, s, termRefLog);
						Message mtde = Messages.error("There is more than one definition of the term "
								+
								termName);
						msgs.add(mtde);
						Messages.storeMessage(article,
								termRefLog.getDefiningSection(),
								this.getClass(), mtde);
					}

					Messages.storeMessages(article, s, this.getClass(), msgs);
					return false;
				}
				else {
					if (globalRecompilationOfTerm(article, s, termRefLog)) {
						Messages.storeMessages(article, s, this.getClass(), msgs);
						return false;
					}
				}
			}
			// There already is a termRefLog, but no term defining section.
			// TermReferences need to be compiled again, because there is
			// now a TermDefinition to refer to.
			else {
				for (Section<? extends TermReference<TermObject>> termRef : termRefLog.getReferences()) {
					if (termRef.get().getTermScope() == Scope.GLOBAL) {
						KnowWEArticleManager artMan = KnowWEEnvironment.getInstance().getArticleManager(
								article.getWeb());
						artMan.addAllArticlesToUpdate(termRef.getReusedBySet());
						termRef.clearReusedBySet();
					}
					else {
						termRef.setReusedBy(article.getTitle(), false);
					}
				}
			}
		}
		getTermReferenceLogsMap(article.getTitle(), s.get().getTermScope()).put(
				termIdentifier,
				new TermReferenceLog<TermObject>(s.get().getTermObjectClass(), s, p));
		if (s.get().getTermScope() == Scope.LOCAL) {
			modifiedTermDefinitions.add(article.getTitle());
		}
		Messages.storeMessages(article, s, this.getClass(), msgs);
		return true;
	}

	/**
	 * A TermDefinition with a different termObjectClass was added before
	 * another already existing or with higher priority than the existing
	 * TermDefinition with the same term.... If the scope is LOCAL, we need a
	 * full reparse, because we are already past the destroy step. If the scope
	 * is GLOBAL, we need a full reparse for the same reason, but only if the
	 * last TermDefinition originates from the article currently compiled. If
	 * the Section is from a different article, it will compiled there.
	 * 
	 * @return whether one can skip after the call of this method because of a
	 *         full parse.
	 */
	private <TermObject> boolean globalRecompilationOfTerm(KnowWEArticle article,
			Section<? extends TermDefinition<TermObject>> s,
			TermReferenceLog<?> termRefLog) {

		KnowWEArticleManager artMan = KnowWEEnvironment.getInstance().getArticleManager(
				article.getWeb());
		if (s.get().getTermScope() == Scope.GLOBAL) {
			artMan.addAllArticlesToUpdate(termRefLog.getDefiningSection().getReusedBySet());
			termRefLog.getDefiningSection().clearReusedBySet();
			for (Section<?> termDef : termRefLog.getRedundantDefinitions()) {
				artMan.addAllArticlesToUpdate(termDef.getReusedBySet());
				termDef.clearReusedBySet();
			}
			for (Section<?> termRef : termRefLog.getReferences()) {
				artMan.addAllArticlesToUpdate(termRef.getReusedBySet());
				termRef.clearReusedBySet();
			}
		}
		if (s.get().getMultiDefMode() == MultiDefMode.ACTIVE
				&& (s.get().getTermScope() == Scope.LOCAL
				|| (termRefLog.getDefiningSection().getArticle().getTitle().equals(
						article.getTitle()) && s.compareTo(termRefLog.getDefiningSection()) < 0))) {
			article.setFullParse(this.getClass());
			return true;
		}
		return false;
	}

	public <TermObject> void registerTermReference(KnowWEArticle article, Section<? extends TermReference<TermObject>> s) {

		TermReferenceLog<TermObject> terRefLog = getTermReferenceLog(article, s);
		if (terRefLog == null) {
			TermReferenceLog<?> termRefLogWrongClass = getTermReferenceLog(article,
					new TermIdentifier(article, s).toString(),
					s.get().getTermScope());
			if (termRefLogWrongClass != null) {
				// don't override a termRefLog of another term object class
				Messages.storeMessage(article, s, this.getClass(),
						Messages.occupiedTermError(new TermIdentifier(article, s).toString(),
								termRefLogWrongClass.getTermObjectClass()));
				return;
			}
			terRefLog = new TermReferenceLog<TermObject>(s.get().getTermObjectClass(),
					null, null);
			getTermReferenceLogsMap(article.getTitle(), s.get().getTermScope()).put(
					new TermIdentifier(article, s), terRefLog);
		}
		if (terRefLog.getDefiningSection() != null) {
			String refTermName = s.get().getTermIdentifier(s);
			String termName = terRefLog.getDefiningSection().get().getTermIdentifier(
					terRefLog.getDefiningSection());
			if (!termName.equals(refTermName)) {
				Messages.storeMessage(article, s, this.getClass(),
						Messages.warning("Term name matches with wrong case. Expected: "
								+ termName));
			}
		}
		terRefLog.addTermReference(s);
	}

	/**
	 * Returns whether a term is defined through a TermDefinition.
	 */
	public boolean isDefinedTerm(KnowWEArticle article, String termIdentifier, Scope termScope) {
		TermReferenceLog<?> termRef = getTermReferenceLog(article, termIdentifier,
				termScope);
		if (termRef != null) {
			if (termRef.getDefiningSection() != null) {
				if (termRef.getDefiningSection().get().getMultiDefMode() == MultiDefMode.INACTIVE
						&& !termRef.getRedundantDefinitions().isEmpty()) {
					return false;
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no
	 * TermDefinition
	 */
	public boolean isUndefinedTerm(KnowWEArticle article, String termIdentifier, Scope termScope) {
		TermReferenceLog<?> termRef = getTermReferenceLog(article, termIdentifier,
				termScope);
		if (termRef != null) {
			return termRef.getDefiningSection() == null;
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
	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public Section<? extends TermDefinition<?>> getTermDefiningSection(
			KnowWEArticle article, String termIdentifier, Scope termScope) {

		TermReferenceLog refLog = getTermReferenceLog(article, termIdentifier, termScope);

		if (refLog != null) {
			return refLog.getDefiningSection();
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
	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public Collection<Section<? extends TermDefinition>> getRedundantTermDefiningSections(
			KnowWEArticle article, String termIdentifier, Scope termScope) {

		TermReferenceLog refLog = getTermReferenceLog(article, termIdentifier, termScope);

		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getRedundantDefinitions());
		}

		return Collections.unmodifiableSet(new HashSet<Section<? extends TermDefinition>>(
				0));
	}

	@SuppressWarnings({
			"unchecked", "rawtypes" })
	public Set<Section<? extends TermReference<?>>> getTermReferenceSections(KnowWEArticle article,
			String termIdentifier, Scope termScope) {
		TermReferenceLog refLog = getTermReferenceLog(article, termIdentifier, termScope);

		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getReferences());
		}

		return Collections.unmodifiableSet(new HashSet<Section<? extends TermReference<?>>>(
				0));
	}

	/**
	 * Returns whether a term is defined through an TermDefinition
	 */
	public <TermObject> boolean isDefinedTerm(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> s) {
		return isDefinedTerm(article, new TermIdentifier(article, s).toString(),
				s.get().getTermScope());
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no
	 * TermDefinition
	 */
	public <TermObject> boolean isUndefinedTerm(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> s) {
		return isUndefinedTerm(article, new TermIdentifier(article, s).toString(),
				s.get().getTermScope());
	}

	/**
	 * For a {@link KnowWETerm} (provided by the Section) the
	 * {@link TermDefinition} is returned.
	 */
	public <TermObject> Section<? extends TermDefinition<TermObject>> getTermDefiningSection(
			KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> s) {

		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, s);

		if (refLog != null) {
			return refLog.getDefiningSection();
		}

		return null;
	}

	public Set<String> getOccupiedTerms() {
		return Collections.unmodifiableSet(occupiedTerms);
	}

	/**
	 * For a {@link KnowWETerm} (provided by the Section) the redundant
	 * {@link TermDefinition}s are returned.
	 */
	public <TermObject> Collection<Section<? extends TermDefinition<TermObject>>> getRedundantTermDefiningSections(
			KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> s) {

		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, s);

		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getRedundantDefinitions());
		}

		return Collections.unmodifiableSet(new HashSet<Section<? extends TermDefinition<TermObject>>>(
				0));
	}

	/**
	 * For a {@link KnowWETerm} (provided by the Section) the
	 * {@link TermReference}s are returned.
	 */
	public <TermObject> Set<Section<? extends TermReference<TermObject>>> getTermReferenceSections(
			KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> s) {

		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, s);

		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getReferences());
		}

		return Collections.unmodifiableSet(new HashSet<Section<? extends TermReference<TermObject>>>(
				0));
	}

	public <TermObject> void unregisterTermDefinition(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s) {
		TermReferenceLog<TermObject> termRefLog = getTermReferenceLog(article, s);
		if (termRefLog != null) {
			Section<? extends TermDefinition<TermObject>> definingSection = termRefLog.getDefiningSection();
			if (s == definingSection) {

				if (s.get().getTermScope() == Scope.GLOBAL) {
					KnowWEArticleManager artMan = KnowWEEnvironment.getInstance().getArticleManager(
							article.getWeb());

					artMan.addAllArticlesToUpdate(definingSection.getReusedBySet());
					definingSection.clearReusedBySet();

					for (Section<?> termDef : termRefLog.getRedundantDefinitions()) {
						artMan.addAllArticlesToUpdate(termDef.getReusedBySet());
						termDef.clearReusedBySet();
					}

					for (Section<?> termRef : termRefLog.getReferences()) {
						artMan.addAllArticlesToUpdate(termRef.getReusedBySet());
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
				getTermReferenceLogsMap(article.getTitle(), s.get().getTermScope()).remove(
						new TermIdentifier(article, s));
			}
			else {
				termRefLog.removeDefinition(s);

				if (definingSection != null
						&& termRefLog.getRedundantDefinitions().isEmpty()
						&& definingSection.get().getMultiDefMode() == MultiDefMode.INACTIVE) {

					globalRecompilationOfTerm(article, s, termRefLog);

					Messages.clearMessages(article,
							definingSection,
							this.getClass());
				}
			}
			if (s.get().getTermScope() == Scope.LOCAL) {
				modifiedTermDefinitions.add(article.getTitle());
			}
		}
		Messages.clearMessages(article, s, this.getClass());
	}

	public <TermObject> void unregisterTermReference(KnowWEArticle article, Section<? extends TermReference<TermObject>> s) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, s);
		if (termRef != null) {
			termRef.termReferingSections.remove(s);
		}
	}

	public boolean areTermDefinitionsModifiedFor(KnowWEArticle article) {
		return modifiedTermDefinitions.contains(article.getTitle());
	}

	/**
	 * Returns all global terms of the given class (e.g. Question, String,...).
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllGlobalTermsOfType(Class<?> termClass) {
		return getAllTerms(null, Scope.GLOBAL, termClass);
	}

	/**
	 * Returns all global terms.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllGlobalTerms() {
		return getAllTerms(null, Scope.GLOBAL, null);
	}

	/**
	 * Returns all local terms of the given class (e.g. Question, String,...),
	 * that are compiled in the article with the given title.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllLocalTermsOfType(String title, Class<?> termClass) {
		return getAllTerms(title, Scope.LOCAL, termClass);
	}

	/**
	 * Returns all local terms that are compiled in the article with the given
	 * title.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllLocalTerms(String title) {
		return getAllTerms(title, Scope.LOCAL, null);
	}

	@SuppressWarnings("rawtypes")
	public Collection<String> getAllTerms(String title, Scope scope, Class<?> termClass) {
		Collection<Section<? extends TermDefinition>> allTermDefs = getAllTermDefs(title,
				scope, termClass);
		Collection<String> terms = new HashSet<String>();
		for (Section<? extends TermDefinition> sec : allTermDefs) {
			terms.add(new TermIdentifier(sec.getArticle(),
						sec).toString());
		}
		return terms;
	}

	/**
	 * Returns all global terms of the given class (e.g. Question, String,...).
	 * 
	 * @created 03.11.2010
	 */
	@SuppressWarnings("rawtypes")
	public Collection<Section<? extends TermDefinition>> getAllGlobalTermDefsOfType(Class<?> termClass) {
		return getAllTermDefs(null, Scope.GLOBAL, termClass);
	}

	/**
	 * Returns all global terms.
	 * 
	 * @created 03.11.2010
	 */
	@SuppressWarnings("rawtypes")
	public Collection<Section<? extends TermDefinition>> getAllGlobalTermDefs() {
		return getAllTermDefs(null, Scope.GLOBAL, null);
	}

	/**
	 * Returns all local terms of the given class (e.g. Question, String,...),
	 * that are compiled in the article with the given title.
	 * 
	 * @created 03.11.2010
	 */
	@SuppressWarnings("rawtypes")
	public Collection<Section<? extends TermDefinition>> getAllLocalTermsDefsOfType(String title, Class<?> termClass) {
		return getAllTermDefs(title, Scope.LOCAL, termClass);
	}

	/**
	 * Returns all local terms that are compiled in the article with the given
	 * title.
	 * 
	 * @created 03.11.2010
	 */
	@SuppressWarnings("rawtypes")
	public Collection<Section<? extends TermDefinition>> getAllLocalTermDefs(String title) {
		return getAllTermDefs(title, Scope.LOCAL, null);
	}

	@SuppressWarnings({
			"rawtypes", "unchecked" })
	public Collection<Section<? extends TermDefinition>> getAllTermDefs(String title, Scope scope, Class<?> termClass) {
		Collection<TermReferenceLog> logs = getTermReferenceLogsMap(title, scope).values();
		Collection<Section<? extends TermDefinition>> terms = new HashSet<Section<? extends TermDefinition>>();
		for (TermReferenceLog tl : logs) {
			if (tl.getDefiningSection() != null
					&& (termClass == null || tl.getTermObjectClass().isAssignableFrom(
							termClass))) {
				terms.add(tl.getDefiningSection());
			}
		}
		return terms;
	}

	/**
	 * 
	 * This is an auxiliary data-structure to store the definitions and
	 * references of terms
	 * 
	 * @author Jochen, Albrecht
	 * 
	 * @param <TermObject> is the Class of the term object associated with the
	 *        term.
	 */
	class TermReferenceLog<TermObject> {

		// private final Priority priorityOfDefiningSection;

		// private final Section<? extends TermDefinition<TermObject>>
		// termDefiningSection;

		TreeMap<Priority, TreeSet<Section<? extends TermDefinition<TermObject>>>> definingSections = new TreeMap<Priority, TreeSet<Section<? extends TermDefinition<TermObject>>>>();

		// private final Set<Section<? extends TermDefinition<TermObject>>>
		// redundantTermDefiningSections =
		// new HashSet<Section<? extends TermDefinition<TermObject>>>();

		private final Set<Section<? extends TermReference<TermObject>>> termReferingSections =
				new HashSet<Section<? extends TermReference<TermObject>>>();

		private final Class<TermObject> termObjectClass;

		public TermReferenceLog(Class<TermObject> termObjectClass, Section<? extends TermDefinition<TermObject>> s, Priority p) {
			if (termObjectClass == null) {
				throw new IllegalArgumentException("termObjectClass can not be null");
			}

			this.termObjectClass = termObjectClass;
			if (p != null && s != null) {
				addDefiningSection(p, s);
			}

		}

		public Priority getPriorityOfDefiningSection() {
			return this.definingSections.firstKey();
		}

		private void addDefiningSection(Priority p, Section<? extends TermDefinition<TermObject>> s) {
			TreeSet<Section<? extends TermDefinition<TermObject>>> set = definingSections.get(p);
			if (set == null) {
				// has to be sorted by order of occurrence in the text
				set = new TreeSet<Section<? extends TermDefinition<TermObject>>>();
			}
			set.add(s);
			definingSections.put(p, set);

		}

		public Class<TermObject> getTermObjectClass() {
			return this.termObjectClass;
		}

		public void addRedundantTermDefinition(Section<? extends TermDefinition<TermObject>> s, Priority p) {
			addDefiningSection(p, s);
		}

		public Section<? extends TermDefinition<TermObject>> getDefiningSection() {
			if (this.definingSections.isEmpty()) return null;
			// high priorities have high number, hence lastEntry...
			return this.definingSections.lastEntry().getValue().first();
		}

		public boolean removeDefinition(Section<? extends TermDefinition<TermObject>> s) {
			for (Priority p : this.definingSections.keySet()) {
				Set<Section<? extends TermDefinition<TermObject>>> secs = this.definingSections.get(p);
				if (secs.remove(s)) {
					return true;
				}
			}

			return false;

		}

		public Set<Section<? extends TermDefinition<TermObject>>> getRedundantDefinitions() {
			Set<Section<? extends TermDefinition<TermObject>>> result = new HashSet<Section<? extends TermDefinition<TermObject>>>();
			for (Priority p : this.definingSections.keySet()) {
				Set<Section<? extends TermDefinition<TermObject>>> secs = this.definingSections.get(p);
				result.addAll(secs);
			}
			result.remove(this.getDefiningSection());
			return result;
		}

		public void addTermReference(Section<? extends TermReference<TermObject>> s) {
			termReferingSections.add(s);
		}

		public Set<Section<? extends TermReference<TermObject>>> getReferences() {
			return termReferingSections;
		}

	}

	/**
	 * Wrapper class to identify and match the terms inside the
	 * TerminologyHandler.
	 * 
	 * @author Albrecht Striffler
	 * @created 08.01.2011
	 */
	private class TermIdentifier {

		private final String termIdentifier;

		private final String termIdentifierLowerCase;

		@SuppressWarnings({
				"unchecked", "rawtypes" })
		public TermIdentifier(KnowWEArticle article, Section<? extends KnowWETerm> s) {

			termIdentifier = s.get().getTermIdentifier(s);

			this.termIdentifierLowerCase = this.termIdentifier.toLowerCase();
		}

		public TermIdentifier(String termIdentifier) {
			this.termIdentifier = termIdentifier;
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

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>(
				2);
		events.add(FullParseEvent.class);
		events.add(ArticleCreatedEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		if (event instanceof FullParseEvent) {
			removeTermReferenceLogsForArticle(((FullParseEvent) event).getArticle());
		}
		else if (event instanceof ArticleCreatedEvent) {
			modifiedTermDefinitions.remove(((ArticleCreatedEvent) event).getArticle().getTitle());
		}
	}

}
