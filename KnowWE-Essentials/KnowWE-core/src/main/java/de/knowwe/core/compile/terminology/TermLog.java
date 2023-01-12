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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.ConcatenateCollection;
import com.denkbares.collections.MinimizedHashSet;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.terminology.TermCompiler.MultiDefinitionMode;
import de.knowwe.core.compile.terminology.TermCompiler.ReferenceValidationMode;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

import static de.knowwe.core.compile.terminology.TermCompiler.MultiDefinitionMode.*;

/**
 * This is an auxiliary data-structure to store the definitions and references
 * of terms
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.02.2012
 */
class TermLog {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermLog.class);

	private static final int DEFINITION_TRACKING_LIMIT = 4;
	private final TreeSet<TermLogEntry> termDefinitions = new TreeSet<>();

	private final Set<TermLogEntry> termReferences = new HashSet<>();

	private Map<Class<?>, Set<TermLogEntry>> termClasses = null;
	private Map<Identifier, Set<TermLogEntry>> termIdentifiers = null;
	private boolean hasMessages = false;

	void addTermDefinition(Compiler compiler,
						   Section<?> termDefinition,
						   Class<?> termClass,
						   Identifier termIdentifier) {

		TermLogEntry entry = new TermLogEntry(termDefinition, termClass, termIdentifier);
		termDefinitions.add(entry);
		// avoid the memory overhead if possible, most Terms have only one definition
		if (termDefinitions.size() > DEFINITION_TRACKING_LIMIT) {
			if (termClasses == null || termIdentifiers == null) {
				termClasses = new HashMap<>(4);
				termIdentifiers = new HashMap<>(4);
				for (TermLogEntry definition : termDefinitions) {
					trackDefinition(definition);
				}
			}
			else {
				trackDefinition(entry);
			}
		}
		handleMessagesForDefinition(compiler);
	}

	private void trackDefinition(TermLogEntry definition) {
		termClasses.computeIfAbsent(definition.getTermClass(), k -> new MinimizedHashSet<>())
				.add(definition);
		termIdentifiers.computeIfAbsent(definition.getTermIdentifier(), k -> new MinimizedHashSet<>())
				.add(definition);
	}

	private void handleMessagesForDefinition(Compiler compiler) {

		ReferenceValidationMode validationMode = getReferenceValidationMode(compiler);
		MultiDefinitionMode multiDefinitionMode = getMultiDefinitionMode(compiler);

		Collection<Message> messages = new ArrayList<>(2);
		if (termDefinitions.size() > 1) {
			if (validationMode != ReferenceValidationMode.ignore) {
				Set<Class<?>> termClasses = getTermClasses();
				if (termClasses.size() > 1) {
					messages.add(Messages.ambiguousTermClassesError(getTermVerbalization(), termClasses));
				}
			}
			Collection<Identifier> termIdentifiers = getDefinitionIdentifiers();
			if (termIdentifiers.size() > 1) {
				messages.add(Messages.ambiguousTermCaseWarning(termIdentifiers));
			}
			if (multiDefinitionMode == warn) {
				messages.add(Messages.warning(getMultiDefinitionText(getTermVerbalization())));
			}
			else if (multiDefinitionMode == error) {
				messages.add(Messages.error(getMultiDefinitionText(getTermVerbalization())));
			}
		}
		addMessagesToSections(compiler, messages, new ConcatenateCollection<>(this.termDefinitions, this.termReferences));
	}

	private String getTermVerbalization() {
		return termDefinitions.iterator().next().getTermIdentifier().toPrettyPrint();
	}

	private void addMessagesToSections(Compiler compiler, Collection<Message> messages, Collection<TermLogEntry> termLogEntries) {
		if (messages.isEmpty()) {
			if (hasMessages) {
				for (TermLogEntry entry : termLogEntries) {
					Messages.clearMessages(compiler, entry.getSection(), this.getClass());
				}
			}
			hasMessages = false;
		} else {
			for (TermLogEntry entry : termLogEntries) {
				Messages.storeMessages(compiler, entry.getSection(), this.getClass(), messages);
			}
			hasMessages = true;
		}
	}

	@NotNull
	private ReferenceValidationMode getReferenceValidationMode(Compiler compiler) {
		ReferenceValidationMode validationMode;
		if (compiler instanceof TermCompiler) {
			validationMode = ((TermCompiler) compiler).getReferenceValidationMode();
		}
		else {
			validationMode = ReferenceValidationMode.ignore;
		}
		return validationMode;
	}

	@NotNull
	private MultiDefinitionMode getMultiDefinitionMode(Compiler compiler) {
		MultiDefinitionMode multiDefinitionMode;
		if (compiler instanceof TermCompiler) {
			multiDefinitionMode = ((TermCompiler) compiler).getMultiDefinitionRegistrationMode();
		}
		else {
			multiDefinitionMode = ignore;
		}
		return multiDefinitionMode;
	}

	@NotNull
	private String getMultiDefinitionText(String term) {
		return "The term '" + term + "' has multiple definitions.";
	}

	void addTermReference(Compiler compiler,
						  Section<?> termReference,
						  Class<?> termClass, Identifier termIdentifier) {

		termReferences.add(new TermLogEntry(termReference, termClass, termIdentifier));
		handleMessagesForReference(compiler, termReference, termIdentifier, termClass);
	}

	private void handleMessagesForReference(Compiler compiler,
											Section<?> section,
											Identifier termIdentifier, Class<?> termClass) {

		ReferenceValidationMode validationMode = getReferenceValidationMode(compiler);
		if (validationMode == ReferenceValidationMode.ignore) return;

		Collection<Message> msgs = new ArrayList<>(2);
		for (TermLogEntry termDefinition : termDefinitions) {
			if (!termDefinition.getTermIdentifier().equals(termIdentifier)) {
				msgs.add(Messages.ambiguousTermCaseWarning(getDefinitionIdentifiers()));
				break;
			}
		}
		if (termDefinitions.size() == 1) {
			Class<?> termClassOfDefinition = termDefinitions.iterator().next().getTermClass();
			boolean assignable = termClass.isAssignableFrom(termClassOfDefinition);
			if (!assignable) {
				msgs.add(Messages.error("The term '"
						+ Strings.trimQuotes(termIdentifier.toPrettyPrint())
						+ "' is registered with the type '" + termClassOfDefinition.getSimpleName()
						+ "' which is incompatible to the type '" + termClass.getSimpleName()
						+ "' of this reference."));
			}
		}
		Messages.storeMessages(compiler, section, this.getClass(), msgs);
	}

	boolean removeTermDefinition(Compiler compiler,
							  Section<?> termDefinition,
							  Class<?> termClass,
							  Identifier termIdentifier) {

		TermLogEntry entry = new TermLogEntry(termDefinition, termClass, termIdentifier);
		if (!termDefinitions.remove(entry)) {
			LOGGER.warn("Trying to unregister term log that does not exist: " + termClass.getSimpleName() + ", " + termIdentifier);
			return false;
		}
		if (termDefinitions.size() <= DEFINITION_TRACKING_LIMIT) {
			termClasses = null;
			termIdentifiers = null;
		}
		else {
			untrackDefinition(entry);
		}
		Messages.clearMessages(compiler, termDefinition, this.getClass());
		handleMessagesForDefinition(compiler);
		return true;
	}

	private void untrackDefinition(TermLogEntry entry) {
		Set<TermLogEntry> termLogEntries = termClasses.get(entry.getTermClass());
		if (termLogEntries != null) {
			termLogEntries.remove(entry);
			if (termLogEntries.isEmpty()) termClasses.remove(entry.getTermClass());
		}
		termLogEntries = termIdentifiers.get(entry.getTermIdentifier());
		if (termLogEntries != null) {
			termLogEntries.remove(entry);
			if (termLogEntries.isEmpty()) termIdentifiers.remove(entry.getTermIdentifier());
		}
	}

	void removeTermReference(Compiler compiler, Section<?> termReference,
							 Class<?> termClass, Identifier termIdentifier) {
		termReferences.remove(new TermLogEntry(termReference, termClass, termIdentifier));
		Messages.clearMessages(compiler, termReference, this.getClass());
	}

	Section<? extends Type> getDefiningSection() {
		if (this.termDefinitions.isEmpty()) return null;
		return this.termDefinitions.first().getSection();
	}

	Set<Section<? extends Type>> getRedundantDefinitions() {
		Set<Section<?>> result = getDefinitions();
		result.remove(this.getDefiningSection());
		return Collections.unmodifiableSet(result);
	}

	public Set<Section<?>> getDefinitions() {
		return extractFromEntry(termDefinitions, TermLogEntry::getSection, termDefinitions.size());
	}

	public Set<Section<?>> getReferences() {
		return extractFromEntry(termReferences, TermLogEntry::getSection, termReferences.size());
	}

	Set<Class<?>> getTermClasses() {
		if (termClasses == null) {
			return extractFromEntry(termDefinitions, TermLogEntry::getTermClass, 4);
		}
		else {
			return Collections.unmodifiableSet(termClasses.keySet());
		}
	}

	Collection<Identifier> getDefinitionIdentifiers() {
		if (termIdentifiers == null) {
			return extractFromEntry(termDefinitions, TermLogEntry::getTermIdentifier, 4);
		}
		else {
			return Collections.unmodifiableSet(termIdentifiers.keySet());
		}
	}

	Collection<Identifier> getReferencesIdentifiers() {
		return extractFromEntry(termReferences, TermLogEntry::getTermIdentifier, 4);
	}

	private <E> Set<E> extractFromEntry(Collection<TermLogEntry> entries, Function<TermLogEntry, E> supplier, int size) {
		Set<E> result = new HashSet<>(size);
		for (TermLogEntry termDefinition : entries) {
			result.add(supplier.apply(termDefinition));
		}
		return Collections.unmodifiableSet(result);
	}

	public boolean isEmpty() {
		return termDefinitions.isEmpty() && termReferences.isEmpty();
	}

	public int cleanupStaleSections() {
		AtomicInteger counter = new AtomicInteger();
		termDefinitions.removeIf(log -> {
			boolean remove = !Sections.isLive(log.getSection());
			if (remove) counter.incrementAndGet();
			return remove;
		});
		termReferences.removeIf(log -> {
			boolean remove = !Sections.isLive(log.getSection());
			if (remove) counter.incrementAndGet();
			return remove;
		});
		return counter.get();
	}
}
