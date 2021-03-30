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
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.terminology.TermCompiler.MultiDefinitionMode;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
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

	private final TreeSet<TermLogEntry> termDefinitions = new TreeSet<>();

	private final Set<TermLogEntry> termReferences = new HashSet<>();

//	private final Map<Class<?>, Set<TermLogEntry>> termClasses =
//			new HashMap<>(2);
//
//	private final Map<String, Set<TermLogEntry>> termIdentifiers =
//			new HashMap<>(2);

	void addTermDefinition(Compiler compiler,
						   Section<?> termDefinition,
						   Class<?> termClass,
						   Identifier termIdentifier) {

		termDefinitions.add(new TermLogEntry(termDefinition, termClass, termIdentifier));
		handleMessagesForDefinition(compiler);
	}

	private void handleMessagesForDefinition(Compiler compiler) {

		MultiDefinitionMode multiDefinitionMode;
		if (compiler instanceof TermCompiler) {
			multiDefinitionMode = ((TermCompiler) compiler).getMultiDefinitionRegistrationMode();
		}
		else {
			multiDefinitionMode = MultiDefinitionMode.ignore;
		}

		Collection<Message> messages = new ArrayList<>(2);
		if (termDefinitions.size() > 1) {
			Set<Class<?>> termClasses = getTermClasses();
			String term = termDefinitions.iterator().next().getTermIdentifier().toPrettyPrint();
			if (termClasses.size() > 1) {
				messages.add(Messages.ambiguousTermClassesError(term, termClasses));
			}
			Collection<Identifier> termIdentifiers = getTermIdentifiers();
			if (termIdentifiers.size() > 1) {
				messages.add(Messages.ambiguousTermCaseWarning(termIdentifiers));
			}
			if (multiDefinitionMode == MultiDefinitionMode.warn) {
				messages.add(Messages.warning(getMultiDefinitionText(term)));
			}
			else if (multiDefinitionMode == MultiDefinitionMode.error) {
				messages.add(Messages.error(getMultiDefinitionText(term)));
			}
		}
		for (TermLogEntry termDefinition : termDefinitions) {
			Messages.storeMessages(compiler, termDefinition.getSection(), this.getClass(), messages);
		}
		for (TermLogEntry termReference : termReferences) {
			Messages.storeMessages(compiler, termReference.getSection(), this.getClass(), messages);
		}
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

		Collection<Message> msgs = new ArrayList<>(2);
		for (TermLogEntry termDefinition : termDefinitions) {
			if (!termDefinition.getTermIdentifier().equals(termIdentifier)) {
				msgs.add(Messages.ambiguousTermCaseWarning(getTermIdentifiers()));
				break;
			}
		}
		if (termDefinitions.size() == 1) {
			Class<?> termClassOfDefinition = termDefinitions.iterator().next().getTermClass();
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

	void removeTermDefinition(Compiler compiler,
							  Section<?> termDefinition,
							  Class<?> termClass,
							  Identifier termIdentifier) {

		if (!termDefinitions.remove(new TermLogEntry(termDefinition, termClass, termIdentifier))) {
			Log.warning("Trying to unregister term log that does not exist: " + termClass.getSimpleName() + ", " + termIdentifier);
		}
		Messages.clearMessages(compiler, termDefinition, this.getClass());
		handleMessagesForDefinition(compiler);
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
		return termDefinitions.stream().map(TermLogEntry::getSection).collect(java.util.stream.Collectors.toUnmodifiableSet());
	}

	public Set<Section<?>> getReferences() {
		return termReferences.stream().map(TermLogEntry::getSection).collect(java.util.stream.Collectors.toUnmodifiableSet());
	}

	Set<Class<?>> getTermClasses() {
		return termDefinitions.stream().map(TermLogEntry::getTermClass).collect(java.util.stream.Collectors.toUnmodifiableSet());
	}

	Collection<Identifier> getTermIdentifiers() {
		// Identifiers hash is case insensitive, here we are case sensitive and have to hash by external form first...
		Map<String, Identifier> identifierMap = new HashMap<>(termDefinitions.size());
		for (TermLogEntry termDefinition : termDefinitions) {
			identifierMap.put(termDefinition.getTermIdentifier().toExternalForm(), termDefinition.getTermIdentifier());
		}
		return Collections.unmodifiableCollection(identifierMap.values());
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
