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
import java.util.Collections;
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
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.Type;
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

	private final Set<Section<?>> termDefinitionSections = new HashSet<Section<?>>();

	private final Set<Section<?>> termReferenceSections = new HashSet<Section<?>>();

	private final Map<Class<?>, Set<TermLogEntry>> termClasses =
			new HashMap<Class<?>, Set<TermLogEntry>>(2);

	private final Map<String, Set<TermLogEntry>> termIdentifiers =
			new HashMap<String, Set<TermLogEntry>>(2);

	public void addTermDefinition(Compiler compiler,
			Section<?> termDefinition,
			Class<?> termClass,
			Identifier termIdentifier) {

		TermLogEntry termLogEntry = createAndRegisterTermLogEntry(true, compiler,
				termDefinition, termClass, termIdentifier);
		addTermLogEntryToMap(termLogEntry.getTermIdentifier().toExternalForm(), termLogEntry,
				termIdentifiers);
		addTermLogEntryToMap(termLogEntry.getTermClass(), termLogEntry, termClasses);
		handleMessagesForDefinition(compiler);
	}

	private TermLogEntry createAndRegisterTermLogEntry(
			boolean definition,
			Compiler compiler,
			Section<?> termSection,
			Class<?> termClass,
			Identifier termIdentifier) {

		TermLogEntry logEntry = new TermLogEntry(compiler, termSection,
				termClass, termIdentifier);
		if (definition) {
			termDefinitions.add(logEntry);
			termDefinitionSections.add(termSection);
		}
		else {
			termReferences.add(logEntry);
			termReferenceSections.add(termSection);
		}
		return logEntry;
	}

	private void handleMessagesForDefinition(Compiler compiler) {

		Collection<Message> msgs = new ArrayList<Message>(2);

		if (termClasses.size() > 1) {
			msgs.add(Messages.ambiguousTermClassesError(
					termIdentifiers.keySet().iterator().next(), termClasses.keySet()));
		}
		if (termIdentifiers.size() > 1) {
			msgs.add(Messages.ambiguousTermCaseWarning(termIdentifiers.keySet()));
		}
		storeMessagesForSections(compiler, msgs, getAllSectionsOfLog());
	}

	private Collection<Section<?>> getAllSectionsOfLog() {
		Collection<Section<?>> sectionsOfLog = new ArrayList<Section<?>>(
				termDefinitions.size() + termReferences.size());
		sectionsOfLog.addAll(getDefinitions());
		sectionsOfLog.addAll(getReferences());
		return sectionsOfLog;
	}

	private void storeMessagesForSections(Compiler compiler, Collection<Message> msgs, Collection<Section<?>> sections) {
		for (Section<?> section : sections) {
			Messages.storeMessages(compiler,
					section, this.getClass(), msgs);
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

	private <MapKey> void removeLogEntriesWithSectionFromMap(Compiler compiler, Section<?> termSection, Map<MapKey, Set<TermLogEntry>> entriesMap) {
		List<MapKey> keysToRemove = new LinkedList<MapKey>();
		for (Entry<MapKey, Set<TermLogEntry>> mapEntry : entriesMap.entrySet()) {
			List<TermLogEntry> logEntriesToRemove = new LinkedList<TermLogEntry>();
			for (TermLogEntry logEntry : mapEntry.getValue()) {
				if (logEntry.getSection().equals(termSection)
						&& logEntry.getCompiler().equals(compiler)) {
					logEntriesToRemove.add(logEntry);
				}
			}
			mapEntry.getValue().removeAll(logEntriesToRemove);
			if (mapEntry.getValue().isEmpty()) keysToRemove.add(mapEntry.getKey());
		}
		for (MapKey keyToRemove : keysToRemove) {
			entriesMap.remove(keyToRemove);
		}
	}

	public void addTermReference(Compiler compiler,
			Section<?> termReference,
			Class<?> termClass, Identifier termIdentifier) {

		createAndRegisterTermLogEntry(false, compiler, termReference, termClass, termIdentifier);
		handleMessagesForReference(compiler, termReference, termIdentifier, termClass);
	}

	private void handleMessagesForReference(Compiler compiler,
			Section<?> section,
			Identifier termIdentifier, Class<?> termClass) {

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
		Messages.storeMessages(compiler, section, this.getClass(), msgs);
	}

	public void removeTermDefinition(Compiler compiler,
			Section<?> termDefinition,
			Class<?> termClass,
			Identifier termIdentifier) {
		removeTermLogEntry(compiler, true, termDefinition, termClass, termIdentifier);
		removeLogEntriesWithSectionFromMap(compiler, termDefinition, termClasses);
		removeLogEntriesWithSectionFromMap(compiler, termDefinition, termIdentifiers);
		handleMessagesForDefinition(compiler);
	}

	private void removeTermLogEntry(Compiler compiler,
			boolean definition,
			Section<?> termSection,
			Class<?> termClass, Identifier termIdentifier) {

		TermLogEntry logEntry = new TermLogEntry(compiler, termSection,
				termClass, termIdentifier);
		if (definition) {
			termDefinitions.remove(logEntry);
			termDefinitionSections.remove(termSection);
		}
		else {
			termReferences.remove(logEntry);
			termReferenceSections.remove(logEntry.getSection());
		}
		Messages.clearMessages(compiler, termSection, this.getClass());
	}

	public void removeTermReference(Compiler compiler, Section<?> termReference,
			Class<?> termClass, Identifier termIdentifier) {
		removeTermLogEntry(compiler, false, termReference, termClass, termIdentifier);
	}

	public void removeEntriesOfCompiler(Compiler compiler) {
		Collection<TermLogEntry> toRemove = new ArrayList<TermLogEntry>(termReferences.size());
		for (TermLogEntry entry : termReferences) {
			if (entry.getCompiler().equals(compiler)) {
				toRemove.add(entry);
			}
		}
		for (TermLogEntry entry : toRemove) {
			removeTermReference(entry.getCompiler(), entry.getSection(), entry.getTermClass(),
					entry.getTermIdentifier());
		}
		toRemove = new ArrayList<TermLogEntry>(termDefinitions.size());
		for (TermLogEntry entry : termDefinitions) {
			if (entry.getCompiler().equals(compiler)) {
				toRemove.add(entry);
			}
		}
		for (TermLogEntry entry : toRemove) {
			removeTermDefinition(entry.getCompiler(), entry.getSection(),
					entry.getTermClass(),
					entry.getTermIdentifier());
		}
	}

	public Section<? extends Type> getDefiningSection() {
		if (this.termDefinitions.isEmpty()) return null;
		return this.termDefinitions.first().getSection();
	}

	public Set<Section<? extends Type>> getRedundantDefinitions() {
		Set<Section<?>> result = getDefinitions();
		result.remove(this.getDefiningSection());
		return Collections.unmodifiableSet(result);
	}

	public Set<Section<? extends Type>> getDefinitions() {
		return Collections.unmodifiableSet(termDefinitionSections);
	}

	public Set<Section<? extends Type>> getReferences() {
		return Collections.unmodifiableSet(termReferenceSections);
	}

	public Set<Class<?>> getTermClasses() {
		return Collections.unmodifiableSet(termClasses.keySet());
	}

	public Collection<Identifier> getTermIdentifiers() {
		ArrayList<Identifier> termIdentifiers = new ArrayList<Identifier>(
				this.termIdentifiers.size());
		for (Entry<String, Set<TermLogEntry>> entry : this.termIdentifiers.entrySet()) {
			Set<TermLogEntry> entrySet = entry.getValue();
			if (entrySet.isEmpty()) continue;
			termIdentifiers.add(entrySet.iterator().next().getTermIdentifier());
		}
		return Collections.unmodifiableCollection(termIdentifiers);
	}

	public boolean isEmpty() {
		return termDefinitions.isEmpty() && termReferences.isEmpty();
	}

}
