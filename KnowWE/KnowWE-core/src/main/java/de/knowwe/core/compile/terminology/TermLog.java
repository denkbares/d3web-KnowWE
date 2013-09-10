/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * This is an auxiliary data-structure to store the definitions and references
 * of terms
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.2012
 */
class TermLog {

	private final TreeSet<TermLogEntry> termDefinitions = new TreeSet<TermLogEntry>();

	private final Set<TermLogEntry> termReferences = new HashSet<TermLogEntry>();

	private final Map<Class<?>, Set<TermLogEntry>> termClasses =
			new HashMap<Class<?>, Set<TermLogEntry>>();

	private final Map<String, Set<TermLogEntry>> termIdentifiers =
			new HashMap<String, Set<TermLogEntry>>();

	private final String web;

	private final String title;

	public TermLog(String web, String title) {
		this.web = web;
		this.title = title;
	}

	public Priority getPriorityOfDefiningSection() {
		return this.termDefinitions.first().getPriority();
	}

	public void addTermDefinition(Priority priority,
			Section<?> termDefinition,
			Class<?> termClass,
			Identifier termIdentifier) {

		TermLogEntry termLogEntry = createAndRegisterTermLogEntry(true, priority, termDefinition,
				termClass, termIdentifier);
		addTermLogEntryToMap(termLogEntry.getTermIdentifier().toExternalForm(), termLogEntry,
				termIdentifiers);
		addTermLogEntryToMap(termLogEntry.getTermClass(), termLogEntry, termClasses);
		handleMessagesForDefinition(termDefinition);
	}

	private TermLogEntry createAndRegisterTermLogEntry(boolean definition, Priority priority,
			Section<?> termSection,
			Class<?> termClass,
			Identifier termIdentifier) {

		TermLogEntry logEntry = new TermLogEntry(priority, termSection, termClass,
				termIdentifier);
		if (definition) {
			termDefinitions.add(logEntry);
		}
		else {
			termReferences.add(logEntry);
		}
		return logEntry;
	}

	private void handleMessagesForDefinition(Section<?> termDefinition) {

		Collection<Message> msgs = new ArrayList<Message>(2);

		if (termClasses.size() > 1) {
			msgs.add(Messages.ambiguousTermClassesError(
					termIdentifiers.keySet().iterator().next(), termClasses.keySet()));
		}
		if (termIdentifiers.size() > 1) {
			msgs.add(Messages.ambiguousTermCaseWarning(termIdentifiers.keySet()));
		}
		storeMessagesForSections(msgs, getAllSectionsOfLog());
	}

	private Collection<Section<?>> getAllSectionsOfLog() {
		Collection<Section<?>> sectionsOfLog = new ArrayList<Section<?>>(
				termDefinitions.size() + termReferences.size());
		sectionsOfLog.addAll(getDefinitions());
		sectionsOfLog.addAll(getReferences());
		return sectionsOfLog;
	}

	private void storeMessagesForSections(Collection<Message> msgs, Collection<Section<?>> sections) {
		Article article = Article.getCurrentlyBuildingArticle(web, title);
		for (Section<?> section : sections) {
			Messages.storeMessages(article, section, this.getClass(), msgs);
		}
	}

	private <MapKey> void addTermLogEntryToMap(MapKey key, TermLogEntry logEntry, Map<MapKey, Set<TermLogEntry>> entriesMap) {
		Set<TermLogEntry> entriesForKey = entriesMap.get(key);
		if (entriesForKey == null) {
			entriesForKey = new HashSet<TermLogEntry>();
			entriesMap.put(key, entriesForKey);
		}
		entriesForKey.add(logEntry);
	}

	private <MapKey> void removeLogEntriesWithSectionFromMap(Section<?> termSection, Map<MapKey, Set<TermLogEntry>> entriesMap) {
		List<MapKey> keysToRemove = new LinkedList<MapKey>();
		for (Entry<MapKey, Set<TermLogEntry>> mapEntry : entriesMap.entrySet()) {
			List<TermLogEntry> logEntriesToRemove = new LinkedList<TermLogEntry>();
			for (TermLogEntry logEntry : mapEntry.getValue()) {
				if (logEntry.getSection() == termSection) logEntriesToRemove.add(logEntry);
			}
			mapEntry.getValue().removeAll(logEntriesToRemove);
			if (mapEntry.getValue().isEmpty()) keysToRemove.add(mapEntry.getKey());
		}
		for (MapKey keyToRemove : keysToRemove) {
			entriesMap.remove(keyToRemove);
		}
	}

	public void addTermReference(Section<?> termReference,
			Class<?> termClass,
			Identifier termIdentifier) {

		createAndRegisterTermLogEntry(false, null, termReference, termClass, termIdentifier);
		handleMessagesForReference(termReference, termIdentifier, termClass);
	}

	private void handleMessagesForReference(Section<?> s,
			Identifier termIdentifier,
			Class<?> termClass) {

		Article article = Article.getCurrentlyBuildingArticle(web, title);
		Collection<Message> msgs = new ArrayList<Message>(2);
		Set<String> termIdentifiersSet = new HashSet<String>(
				termIdentifiers.keySet());
		termIdentifiersSet.add(termIdentifier.toExternalForm());
		if (termIdentifiersSet.size() > 1) {
			msgs.add(Messages.ambiguousTermCaseWarning(termIdentifiersSet));
		}
		if (termClasses.size() == 1) {
			Class<?> termClassOfDefinition = termClasses.keySet().iterator().next();
			boolean assignable = termClass.isAssignableFrom(termClassOfDefinition);
			if (!assignable) {
				msgs.add(Messages.error("The term '"
						+ Strings.trimQuotes(termIdentifier.toString())
						+ "' is registered with the type '" + termClassOfDefinition.getSimpleName()
						+ "' which is incompatible to the type '" + termClass.getSimpleName()
						+ "' of this reference."));
			}
		}
		Messages.storeMessages(article, s, this.getClass(), msgs);
	}

	public void removeTermDefinition(Priority priority,
			Section<?> termDefinition,
			Class<?> termClass,
			Identifier termIdentifier) {

		removeTermLogEntry(true, priority, termDefinition, termClass, termIdentifier);
		removeFromAdditionalSetsAndHandleAmbiguity(termDefinition);
	}

	private void removeTermLogEntry(boolean definition, Priority priority,
			Section<?> termSection,
			Class<?> termClass,
			Identifier termIdentifier) {

		TermLogEntry logEntry = new TermLogEntry(priority, termSection, termClass,
				termIdentifier);
		if (definition) {
			termDefinitions.remove(logEntry);
		}
		else {
			termReferences.remove(logEntry);
		}
	}

	public void removeTermDefinition(Section<?> termDefinition) {
		removeAllEntriesWithSection(true, termDefinition);
		removeFromAdditionalSetsAndHandleAmbiguity(termDefinition);
	}

	private void removeFromAdditionalSetsAndHandleAmbiguity(Section<?> termDefinition) {
		removeLogEntriesWithSectionFromMap(termDefinition, termClasses);
		removeLogEntriesWithSectionFromMap(termDefinition, termIdentifiers);
		handleMessagesForDefinition(termDefinition);
	}

	public void removeTermReference(Section<?> termReference,
			Identifier termIdentifier,
			Class<?> termClass) {

		removeTermLogEntry(false, null, termReference, termClass, termIdentifier);
	}

	public void removeTermReference(Section<?> termReference) {
		removeAllEntriesWithSection(false, termReference);
	}

	private void removeAllEntriesWithSection(boolean definition, Section<?> section) {
		Collection<TermLogEntry> logEntries = definition
				? termDefinitions
				: termReferences;
		Collection<TermLogEntry> entriesToRemove = new LinkedList<TermLogEntry>();
		for (TermLogEntry entry : logEntries) {
			if (entry.getSection() == section) entriesToRemove.add(entry);
		}
		logEntries.removeAll(entriesToRemove);
	}

	public Section<?> getDefiningSection() {
		if (this.termDefinitions.isEmpty()) return null;
		return this.termDefinitions.first().getSection();
	}

	public Set<Section<?>> getRedundantDefinitions() {
		Set<Section<?>> result = getDefinitions();
		result.remove(this.getDefiningSection());
		return result;
	}

	public Set<Section<?>> getDefinitions() {
		Set<Section<?>> result = new HashSet<Section<?>>();
		for (TermLogEntry entry : this.termDefinitions) {
			result.add(entry.getSection());
		}
		return result;
	}

	public Set<Section<?>> getReferences() {
		Set<Section<?>> result = new HashSet<Section<?>>();
		for (TermLogEntry entry : this.termReferences) {
			result.add(entry.getSection());
		}
		return result;
	}

	public Set<Class<?>> getTermClasses() {
		return termClasses.keySet();
	}

	public Collection<Identifier> getTermIdentifiers() {
		ArrayList<Identifier> termIdentifiers = new ArrayList<Identifier>(
				this.termIdentifiers.size());
		for (Entry<String, Set<TermLogEntry>> entry : this.termIdentifiers.entrySet()) {
			Set<TermLogEntry> entrySet = entry.getValue();
			if (entrySet.isEmpty()) continue;
			termIdentifiers.add(entrySet.iterator().next().getTermIdentifier());
		}
		return termIdentifiers;
	}

}
