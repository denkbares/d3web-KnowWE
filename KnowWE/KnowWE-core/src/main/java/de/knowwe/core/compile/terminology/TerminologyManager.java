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
import de.knowwe.core.KnowWEArticleManager;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.event.ArticleCreatedEvent;
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
public class TerminologyManager implements EventListener {

	private final String web;

	private final String title;

	private final boolean global;

	public final static String HANDLER_KEY = TerminologyManager.class.getSimpleName();

	private boolean modifiedTermDefinitions = false;

	private static final Set<String> occupiedTerms = new HashSet<String>();

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
		EventManager.getInstance().registerListener(this);
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
		for (String occupiedTerm : terminologyExtension.getTermNames()) {
			occupiedTerms.add(occupiedTerm.toLowerCase());
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
			String termIdentifier) {

		KnowWEArticle article = global
				? termDefinition.getArticle()
				: KnowWEArticle.getCurrentlyBuildingArticle(web, title);

		TermIdentifier termIdentifierObject = new TermIdentifier(termIdentifier);

		if (occupiedTerms.contains(termIdentifierObject.getTermIdentifierLowerCase())) {
			Message msg = Messages.error("The term '"
					+ termIdentifierObject.getTermIdentifier()
					+ "' is reserved by the system.");
			Messages.storeMessage(article, termDefinition, this.getClass(), msg);
			return;
		}

		TermLog termRefLog = termLogManager.getLog(termIdentifierObject);
		if (termRefLog == null) {
			termRefLog = new TermLog(web, title);
			termLogManager.putLog(termIdentifierObject, termRefLog);
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
					KnowWEArticleManager artMan = KnowWEEnvironment.getInstance().getArticleManager(
							web);
					artMan.addArticleToUpdate(termRef.getTitle());
				}
			}
		}
		Priority priority = article.getReviseIterator().getCurrentPriority();
		termRefLog.addTermDefinition(priority, termDefinition, termClass, termIdentifierObject);
		modifiedTermDefinitions = true;
		Messages.clearMessages(article, termDefinition, this.getClass());
	}

	public Collection<String> getAllTermsEqualIgnoreCase(String termIdentifier) {
		TermLog termLog = termLogManager.getLog(new TermIdentifier(termIdentifier));
		Collection<String> termIdentifiers;
		if (termLog == null) {
			termIdentifiers = new ArrayList<String>(0);
		}
		else {
			termIdentifiers = termLog.getTermIdentifiers();
		}
		return Collections.unmodifiableCollection(termIdentifiers);
	}

	public <TermObject> void registerTermReference(
			Section<?> termReference,
			Class<?> termClass,
			String termIdentifier) {

		TermIdentifier termIdentifierObject = new TermIdentifier(termIdentifier);

		TermLog termLog = termLogManager.getLog(termIdentifierObject);
		if (termLog == null) {
			termLog = new TermLog(web, title);
			termLogManager.putLog(termIdentifierObject, termLog);
		}
		termLog.addTermReference(termReference, termClass, termIdentifierObject);
	}

	/**
	 * Returns whether a term is defined through a TermDefinition.
	 */
	public boolean isDefinedTerm(String termIdentifier) {
		TermLog termRef = termLogManager.getLog(new TermIdentifier(termIdentifier));
		if (termRef == null) return false;
		if (termRef.getDefiningSection() == null) return false;
		return true;
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no
	 * TermDefinition
	 */
	public boolean isUndefinedTerm(String termIdentifier) {
		TermLog termRef = termLogManager.getLog(new TermIdentifier(termIdentifier));
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
	public Section<?> getTermDefiningSection(String termIdentifier) {
		TermLog refLog = termLogManager.getLog(new TermIdentifier(termIdentifier));
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
	public Collection<Section<?>> getTermDefiningSections(String termIdentifier) {
		Collection<Section<?>> definitions = new ArrayList<Section<?>>();
		TermLog refLog = termLogManager.getLog(new TermIdentifier(termIdentifier));
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
	public Collection<Section<?>> getRedundantTermDefiningSections(String termIdentifier) {
		TermLog refLog = termLogManager.getLog(new TermIdentifier(termIdentifier));
		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getRedundantDefinitions());
		}
		return Collections.unmodifiableSet(new HashSet<Section<?>>(0));
	}

	public Set<String> getOccupiedTerms() {
		return Collections.unmodifiableSet(occupiedTerms);
	}

	/**
	 * For a {@link KnowWETerm} (provided by the Section) the
	 * {@link TermReference}s are returned.
	 */
	public <TermObject> Collection<Section<?>> getTermReferenceSections(String termIdentifier) {

		TermLog refLog = termLogManager.getLog(new TermIdentifier(termIdentifier));

		if (refLog != null) {
			return Collections.unmodifiableCollection(refLog.getReferences());
		}

		return Collections.unmodifiableCollection(new ArrayList<Section<?>>(0));
	}

	public void unregisterTermDefinition(
			Section<?> termDefinition,
			Class<?> termClass,
			String termIdentifier) {

		TermIdentifier termIdentifierObject = new TermIdentifier(termIdentifier);
		TermLog termRefLog = termLogManager.getLog(termIdentifierObject);
		if (termRefLog != null) {
			KnowWEArticle article = global
					? termDefinition.getArticle()
					: KnowWEArticle.getCurrentlyBuildingArticle(web, title);

			Priority priority = article.getReviseIterator().getCurrentPriority();
			termRefLog.removeTermDefinition(priority, termDefinition, termClass,
					termIdentifierObject);

			modifiedTermDefinitions = true;
		}
	}

	public void unregisterTermReference(Section<?> termReference, String termIdentifier, Class<?> termClass) {

		TermIdentifier termIdentifierObject = new TermIdentifier(termIdentifier);
		TermLog refLog = termLogManager.getLog(termIdentifierObject);
		if (refLog != null) {
			refLog.removeTermReference(termReference, termIdentifierObject, termClass);
		}
	}

	public boolean areTermDefinitionsModifiedFor(KnowWEArticle article) {
		return modifiedTermDefinitions;
	}

	/**
	 * Returns all local terms of the given class (e.g. Question, String,...),
	 * that are compiled in the article with the given title.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllDefinedTermsOfType(Class<?> termClass) {
		return getAllDefinedTerms(termClass);
	}

	/**
	 * Returns all local terms that are compiled in the article with the given
	 * title.
	 * 
	 * @created 03.11.2010
	 */
	public Collection<String> getAllDefinedTerms() {
		return getAllDefinedTerms(null);
	}

	public Collection<String> getAllDefinedTerms(Class<?> termClass) {
		Collection<TermLog> termLogEntries = getAllDefinedTermLogEntries(termClass);
		Collection<String> terms = new HashSet<String>();
		for (TermLog logEntry : termLogEntries) {
			terms.addAll(logEntry.getTermIdentifiers());
		}
		return terms;
	}

	private Collection<TermLog> getAllDefinedTermLogEntries(Class<?> termClass) {
		Collection<TermLog> filteredLogEntries = new HashSet<TermLog>();
		for (Entry<String, TermLog> managerEntry : termLogManager.entrySet()) {
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

	private static void removeTermReferenceLogsForArticle(KnowWEArticle article) {
		TerminologyManager masterArticleHandler = KnowWEUtils.getTerminologyManager(article);
		masterArticleHandler.termLogManager = new TermLogManager();

		TerminologyManager globalTerminologyHandler = KnowWEUtils.getGlobalTerminologyManager(article.getWeb());

		Set<Entry<String, TermLog>> entrySet = globalTerminologyHandler.termLogManager.entrySet();
		for (Entry<String, TermLog> entry : new ArrayList<Entry<String, TermLog>>(entrySet)) {
			Set<Section<?>> definitions = entry.getValue().getDefinitions();
			for (Section<?> termDefinition : definitions) {
				if (!termDefinition.getTitle().equals(article.getTitle())) continue;
				TermLog termLog = globalTerminologyHandler.termLogManager.getLog(new TermIdentifier(
						entry.getKey()));
				termLog.removeTermDefinition(termDefinition);
			}
			Set<Section<?>> references = entry.getValue().getReferences();
			for (Section<?> termReference : references) {
				if (!termReference.getTitle().equals(article.getTitle())) continue;
				TermLog termLog = globalTerminologyHandler.termLogManager.getLog(new TermIdentifier(
						entry.getKey()));
				termLog.removeTermReference(termReference);
			}
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
			modifiedTermDefinitions = false;
		}
	}

}
