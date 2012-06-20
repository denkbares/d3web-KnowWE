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

package de.knowwe.core.compile.terminology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.event.FullParseEvent;
import de.knowwe.plugin.Plugins;

/**
 * This class manages the definition and usage of terms. A term represents some
 * kind of object. For each term that is defined in the wiki (and registered
 * here) it stores the location where it has been defined. Further, for any
 * reference also the locations are stored. The service of this manager is, that
 * for a given term the definition and the references can be asked for.
 * Obviously, this only works if the terms are registered here.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * 
 */
public class TerminologyManager {

	private final String web;

	private final String title;

	private final boolean global;

	public final static String HANDLER_KEY = TerminologyManager.class.getSimpleName();

	private static final Set<TermIdentifier> occupiedTerms = new HashSet<TermIdentifier>();

	private static boolean initializedOccupiedTerms = false;

	private TermLogManager termLogManager = new TermLogManager();

	public TerminologyManager(String web, String title) {
		this.web = web;
		this.title = title;
		if (title == null) {
			global = true;
		}
		else {
			global = false;
		}
		EventManager.getInstance().registerListener(TerminologyManagerCleaner.getInstance());
		if (!initializedOccupiedTerms) {
			// extension point for plugins defining predefined terminology
			Extension[] exts = PluginManager.getInstance().getExtensions(
					Plugins.EXTENDED_PLUGIN_ID,
					Plugins.EXTENDED_POINT_TERMINOLOGY);
			for (Extension extension : exts) {
				Object o = extension.getSingleton();
				if (o instanceof TerminologyExtension) {
					registerOccupiedTerm(((TerminologyExtension) o));
				}
			}
		}
	}

	private void registerOccupiedTerm(TerminologyExtension terminologyExtension) {
		for (String occupiedTermInExternalForm : terminologyExtension.getTermNames()) {
			occupiedTerms.add(TermIdentifier.fromExternalForm(occupiedTermInExternalForm));
		}
	}

	public String getWeb() {
		return web;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Allows to register a new term.
	 * 
	 * @param termDefinition is the term section defining the term.
	 * @param termIdentifier is the term for which the section is registered
	 * @returns true if the sections was registered as the defining section for
	 *          this term. false else.
	 */
	public void registerTermDefinition(
			Section<?> termDefinition,
			Class<?> termClass,
			TermIdentifier termIdentifier) {

		Article article = global
				? termDefinition.getArticle()
				: Article.getCurrentlyBuildingArticle(web, title);

		if (occupiedTerms.contains(termIdentifier)) {
			Message msg = Messages.error("The term '"
					+ termIdentifier.toString()
					+ "' is reserved by the system.");
			Messages.storeMessage(article, termDefinition, this.getClass(), msg);
			return;
		}

		TermLog termRefLog = termLogManager.getLog(termIdentifier);
		if (termRefLog == null) {
			termRefLog = new TermLog(web, title);
			termLogManager.putLog(termIdentifier, termRefLog);
		}
		else {
			// There already is a termRefLog, but no term defining section.
			// TermReferences need to be compiled again, because there is
			// now a TermDefinition to refer to.
			// This can only happen in a global terminology handler, because
			// inside a master article, definitions are always compiled before
			// the references
			if (termRefLog.getDefiningSection() == null) {
				for (Section<?> termRef : termRefLog.getReferences()) {
					ArticleManager artMan = Environment.getInstance().getArticleManager(
							web);
					artMan.addArticleToUpdate(termRef.getTitle());
				}
			}
		}
		Priority priority = article.getReviseIterator().getCurrentPriority();
		termRefLog.addTermDefinition(priority, termDefinition, termClass, termIdentifier);
		Messages.clearMessages(article, termDefinition, this.getClass());
	}

	public Collection<TermIdentifier> getAllTermsEqualIgnoreCase(TermIdentifier termIdentifier) {
		TermLog termLog = termLogManager.getLog(termIdentifier);
		Collection<TermIdentifier> termIdentifiers;
		if (termLog == null) {
			termIdentifiers = Collections.emptyList();
		}
		else {
			termIdentifiers = termLog.getTermIdentifiers();
		}
		return Collections.unmodifiableCollection(termIdentifiers);
	}

	public <TermObject> void registerTermReference(
			Section<?> termReference,
			Class<?> termClass,
			TermIdentifier termIdentifier) {

		TermLog termLog = termLogManager.getLog(termIdentifier);
		if (termLog == null) {
			termLog = new TermLog(web, title);
			termLogManager.putLog(termIdentifier, termLog);
		}
		termLog.addTermReference(termReference, termClass, termIdentifier);
	}

	/**
	 * Returns whether a term is defined through a TermDefinition.
	 */
	public boolean isDefinedTerm(TermIdentifier termIdentifier) {
		TermLog termRef = termLogManager.getLog(termIdentifier);
		if (termRef == null) return false;
		if (termRef.getDefiningSection() == null) return false;
		return true;
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no
	 * TermDefinition
	 */
	public boolean isUndefinedTerm(TermIdentifier termIdentifier) {
		TermLog termRef = termLogManager.getLog(termIdentifier);
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
	public Section<?> getTermDefiningSection(TermIdentifier termIdentifier) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			return refLog.getDefiningSection();
		}
		return null;
	}

	/**
	 * For a TermName all TermDefinitions are returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	public Collection<Section<?>> getTermDefiningSections(TermIdentifier termIdentifier) {
		Collection<Section<?>> definitions = new ArrayList<Section<?>>();
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			definitions = refLog.getDefinitions();
		}
		return Collections.unmodifiableCollection(definitions);
	}

	/**
	 * For a TermName the redundant TermDefinition are returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	public Collection<Section<?>> getRedundantTermDefiningSections(TermIdentifier termIdentifier) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getRedundantDefinitions());
		}
		return Collections.unmodifiableSet(new HashSet<Section<?>>(0));
	}

	public Set<TermIdentifier> getOccupiedTerms() {
		return Collections.unmodifiableSet(occupiedTerms);
	}

	/**
	 * For a {@link KnowWETerm} (provided by the Section) the
	 * {@link TermReference}s are returned.
	 */
	public <TermObject> Collection<Section<?>> getTermReferenceSections(TermIdentifier termIdentifier) {

		TermLog refLog = termLogManager.getLog(termIdentifier);

		if (refLog != null) {
			return Collections.unmodifiableCollection(refLog.getReferences());
		}

		return Collections.unmodifiableCollection(new ArrayList<Section<?>>(0));
	}

	public void unregisterTermDefinition(
			Section<?> termDefinition,
			Class<?> termClass,
			TermIdentifier termIdentifier) {

		TermLog termRefLog = termLogManager.getLog(termIdentifier);
		if (termRefLog != null) {
			Article article = global
					? termDefinition.getArticle()
					: Article.getCurrentlyBuildingArticle(web, title);

			Priority priority = article.getReviseIterator().getCurrentPriority();
			termRefLog.removeTermDefinition(priority, termDefinition, termClass,
					termIdentifier);
		}
	}

	public void unregisterTermReference(Section<?> termReference, TermIdentifier termIdentifier, Class<?> termClass) {

		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			refLog.removeTermReference(termReference, termIdentifier, termClass);
		}
	}

	/**
	 * Returns all local terms of the given class (e.g. Question, String,...),
	 * that are compiled in the article with the given title.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<TermIdentifier> getAllDefinedTermsOfType(Class<?> termClass) {
		return getAllDefinedTerms(termClass);
	}

	/**
	 * Returns all local terms that are compiled in the article with the given
	 * title.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<TermIdentifier> getAllDefinedTerms() {
		return getAllDefinedTerms(null);
	}

	public Collection<TermIdentifier> getAllDefinedTerms(Class<?> termClass) {
		Collection<TermLog> termLogEntries = getAllDefinedTermLogEntries(termClass);
		Collection<TermIdentifier> terms = new HashSet<TermIdentifier>();
		for (TermLog logEntry : termLogEntries) {
			terms.addAll(logEntry.getTermIdentifiers());
		}
		return terms;
	}

	private Collection<TermLog> getAllDefinedTermLogEntries(Class<?> termClass) {
		Collection<TermLog> filteredLogEntries = new HashSet<TermLog>();
		for (Entry<TermIdentifier, TermLog> managerEntry : termLogManager.entrySet()) {
			Set<Class<?>> termClasses = managerEntry.getValue().getTermClasses();
			if (termClasses.size() != 1) continue;
			boolean hasTermDefOfType = managerEntry.getValue().getDefiningSection() != null
					&& (termClass == null || termClass.isAssignableFrom(termClasses.iterator().next()));
			if (hasTermDefOfType) {
				filteredLogEntries.add(managerEntry.getValue());
			}
		}
		return filteredLogEntries;
	}

	/**
	 * Returns if a term has been registered with the specified name and if its
	 * class is of the specified class. Otherwise (if no such term exists or it
	 * does not have a compatible class) false is returned.
	 * 
	 * @created 05.03.2012
	 * @param termIdentifier the term to be searched for
	 * @param clazz the class the term must be a subclass of (or of the same
	 *        class)
	 * @return if the term has been registered as required
	 */
	public boolean hasTermOfClass(TermIdentifier termIdentifier, Class<?> clazz) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog == null) return false;
		for (Class<?> termClass : refLog.getTermClasses()) {
			if (clazz.isAssignableFrom(termClass)) {
				return true;
			}
		}
		return false;
	}

	private static class TerminologyManagerCleaner implements EventListener {

		private static TerminologyManagerCleaner instance = null;

		private static TerminologyManagerCleaner getInstance() {
			if (instance == null) instance = new TerminologyManagerCleaner();
			return instance;
		}

		private static void removeTermReferenceLogsForArticle(Article article) {
			TerminologyManager masterArticleHandler = KnowWEUtils.getTerminologyManager(article);
			masterArticleHandler.termLogManager = new TermLogManager();

			TerminologyManager globalTerminologyHandler = KnowWEUtils.getGlobalTerminologyManager(article.getWeb());

			Set<Entry<TermIdentifier, TermLog>> entrySet = globalTerminologyHandler.termLogManager.entrySet();
			for (Entry<TermIdentifier, TermLog> entry : new ArrayList<Entry<TermIdentifier, TermLog>>(
					entrySet)) {
				Set<Section<?>> definitions = entry.getValue().getDefinitions();
				for (Section<?> termDefinition : definitions) {
					if (!termDefinition.getTitle().equals(article.getTitle())) continue;
					TermLog termLog = globalTerminologyHandler.termLogManager.getLog(entry.getKey());
					termLog.removeTermDefinition(termDefinition);
				}
				Set<Section<?>> references = entry.getValue().getReferences();
				for (Section<?> termReference : references) {
					if (!termReference.getTitle().equals(article.getTitle())) continue;
					TermLog termLog = globalTerminologyHandler.termLogManager.getLog(entry.getKey());
					termLog.removeTermReference(termReference);
				}
			}
		}

		@Override
		public Collection<Class<? extends Event>> getEvents() {
			ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>(
					1);
			events.add(FullParseEvent.class);
			return events;
		}

		@Override
		public void notify(Event event) {
			if (event instanceof FullParseEvent) {
				Article article = ((FullParseEvent) event).getArticle();
				removeTermReferenceLogsForArticle(article);
			}
		}

	}

}
