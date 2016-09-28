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
import java.util.stream.Collectors;

import com.denkbares.events.EventManager;
import com.denkbares.plugin.Extension;
import com.denkbares.plugin.PluginManager;
import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.plugin.Plugins;

/**
 * This class manages the definition and usage of terms. A term represents some kind of object. For
 * each term that is defined in the wiki (and registered here) it stores the location where it has
 * been defined. Further, for any reference also the locations are stored. The service of this
 * manager is, that for a given term the definition and the references can be asked for. Obviously,
 * this only works if the terms are registered here.
 * <p/>
 *
 * @author Albrecht Striffler (denkbares GmbH)
 */
public class TerminologyManager {

	private static final Set<Identifier> occupiedTerms = new HashSet<>();

	private final TermLogManager termLogManager;

	public TerminologyManager() {
		this(false);
	}

	public TerminologyManager(boolean caseSensitive) {
		termLogManager = new TermLogManager(caseSensitive);
		// extension point for plugins defining predefined terminology
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				Plugins.EXTENDED_PLUGIN_ID,
				Plugins.EXTENDED_POINT_Terminology);
		for (Extension extension : extensions) {
			Object o = extension.getSingleton();
			if (o instanceof TerminologyExtension) {
				registerOccupiedTerm(((TerminologyExtension) o));
			}
		}
	}

	private void registerOccupiedTerm(TerminologyExtension terminologyExtension) {
		for (String occupiedTermInExternalForm : terminologyExtension.getTermNames()) {
			occupiedTerms.add(Identifier.fromExternalForm(occupiedTermInExternalForm));
		}
	}

	/**
	 * Allows to register a new term.
	 *
	 * @param compiler       the compiler which registers the term.
	 * @param termDefinition is the term section defining the term.
	 * @param termIdentifier is the term for which the section is registered
	 */
	public void registerTermDefinition(
			TermCompiler compiler,
			Section<?> termDefinition,
			Class<?> termClass, Identifier termIdentifier) {

		Compiler messageCompiler = compiler instanceof AbstractPackageCompiler
				? (AbstractPackageCompiler) compiler : null;
		if (occupiedTerms.contains(termIdentifier)) {
			Message msg = Messages.error("The term '"
					+ termIdentifier
					+ "' is reserved by the system.");
			Messages.storeMessage(messageCompiler, termDefinition, this.getClass(), msg);
			return;
		}

		synchronized (this) {
			TermLog termRefLog = termLogManager.getLog(termIdentifier);
			if (termRefLog == null) {
				termRefLog = new TermLog();
				termLogManager.putLog(termIdentifier, termRefLog);
			}
			termRefLog.addTermDefinition(compiler, termDefinition, termClass, termIdentifier);
		}

		EventManager.getInstance().fireEvent(new TermDefinitionRegisteredEvent(compiler, termIdentifier));
		Messages.clearMessages(messageCompiler, termDefinition, this.getClass());
	}

	/**
	 * Terms in KnowWE are case insensitive.<br/> If the same term is defined with different cases,
	 * all different versions are returned. If the term is undefined, an empty Collection is
	 * returned.
	 *
	 * @param termIdentifier an {@link Identifier} with arbitrary case for a term for which you want
	 *                       potential other versions with different cases
	 * @return the different versions of {@link Identifier}s or an empty Collection, if the term is
	 * undefined
	 * @created 28.07.2012
	 */
	public synchronized Collection<Identifier> getAllTermsEqualIgnoreCase(Identifier termIdentifier) {
		TermLog termLog = termLogManager.getLog(termIdentifier);
		Collection<Identifier> termIdentifiers;
		if (termLog == null) {
			termIdentifiers = Collections.emptyList();
		}
		else {
			termIdentifiers = termLog.getTermIdentifiers();
		}
		return Collections.unmodifiableCollection(termIdentifiers);
	}

	public synchronized void registerTermReference(
			Compiler compiler,
			Section<?> termReference,
			Class<?> termClass, Identifier termIdentifier) {

		TermLog termLog = termLogManager.getLog(termIdentifier);
		if (termLog == null) {
			termLog = new TermLog();
			termLogManager.putLog(termIdentifier, termLog);
		}
		termLog.addTermReference(compiler, termReference, termClass, termIdentifier);
	}

	/**
	 * Returns whether a term is defined through a TermDefinition.
	 */
	public synchronized boolean isDefinedTerm(Identifier termIdentifier) {
		TermLog termRef = termLogManager.getLog(termIdentifier);
		return termRef != null && termRef.getDefiningSection() != null;
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no TermDefinition
	 */
	public synchronized boolean isUndefinedTerm(Identifier termIdentifier) {
		TermLog termRef = termLogManager.getLog(termIdentifier);
		return termRef != null && termRef.getDefiningSection() == null;
	}

	/**
	 * For a {@link Identifier} the first defining Section is returned. If the term is not defined,
	 * <tt>null</tt> is returned.
	 *
	 * @param termIdentifier the {@link Identifier} for the defining Section you are looking for
	 * @return the first defining Section for this term or <tt>null</tt> if the term is not defined
	 */
	public synchronized Section<? extends Type> getTermDefiningSection(Identifier termIdentifier) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			return refLog.getDefiningSection();
		}
		return null;
	}

	/**
	 * For a {@link Identifier} all defining Sections are returned. If the term is not defined, an
	 * empty Collection is returned.
	 *
	 * @param termIdentifier the {@link Identifier} for the defining Sections you are looking for
	 * @return the defining Sections for this term or an empty Collection if the term is not defined
	 */
	public synchronized Collection<Section<?>> getTermDefiningSections(Identifier termIdentifier) {
		if(termIdentifier == null) {
			return Collections.emptyList();
		}
		Collection<Section<?>> definitions = new ArrayList<>();
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			definitions = refLog.getDefinitions();
		}
		return Collections.unmodifiableCollection(definitions);
	}

	/**
	 * For an Identifier the redundant TermDefinition are returned.
	 */
	public synchronized Collection<Section<?>> getRedundantTermDefiningSections(Identifier termIdentifier) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			return Collections.unmodifiableSet(refLog.getRedundantDefinitions());
		}
		return Collections.emptySet();
	}

	/**
	 * For an Identifier the {@link TermReference}s are returned.
	 */
	public synchronized Collection<Section<?>> getTermReferenceSections(Identifier termIdentifier) {

		TermLog refLog = termLogManager.getLog(termIdentifier);

		if (refLog != null) {
			return Collections.unmodifiableCollection(refLog.getReferences());
		}

		return Collections.emptyList();
	}

	public void unregisterTermDefinition(
			TermCompiler compiler,
			Section<?> termDefinition,
			Class<?> termClass, Identifier termIdentifier) {

		synchronized (this) {
			TermLog termRefLog = termLogManager.getLog(termIdentifier);
			if (termRefLog != null) {
				termRefLog.removeTermDefinition(compiler, termDefinition,
						termClass, termIdentifier);
			}
		}
		EventManager.getInstance()
				.fireEvent(new TermDefinitionUnregisteredEvent(compiler, termIdentifier));
	}

	public synchronized void unregisterTermReference(Compiler compiler, Section<?> termReference, Class<?> termClass, Identifier termIdentifier) {

		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog != null) {
			refLog.removeTermReference(compiler, termReference, termClass, termIdentifier);
		}
	}

	/**
	 * Returns all local terms of the given class (e.g. Question, String,...), that are compiled in
	 * the article with the given title.
	 *
	 * @created 03.11.2010
	 */
	public synchronized Collection<Identifier> getAllDefinedTermsOfType(Class<?> termClass) {
		return getAllDefinedTerms(termClass);
	}

	/**
	 * Returns known and defined terms of this {@link TerminologyManager}
	 *
	 * @created 03.11.2010
	 */
	public synchronized Collection<Identifier> getAllDefinedTerms() {
		return getAllDefinedTerms(null);
	}

	/**
	 * Returns all local terms of the given class (e.g. Question, String,...), that are compiled in
	 * the article with the given title.
	 *
	 * @created 03.11.2010
	 */
	public synchronized Collection<Identifier> getAllDefinedTerms(Class<?> termClass) {
		return getAllDefinedTermLogEntries(termClass).stream()
				.map(TermLog::getTermIdentifiers)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	private synchronized Collection<TermLog> getAllDefinedTermLogEntries(Class<?> termClass) {
		Collection<TermLog> filteredLogEntries = new HashSet<>();
		for (Entry<Identifier, TermLog> managerEntry : termLogManager.entrySet()) {
			Set<Class<?>> termClasses = managerEntry.getValue().getTermClasses();
			if (termClasses.size() != 1) continue;
			boolean hasTermDefOfType = managerEntry.getValue().getDefiningSection() != null
					&& (termClass == null || termClass.isAssignableFrom(termClasses.iterator()
					.next()));
			if (hasTermDefOfType) {
				filteredLogEntries.add(managerEntry.getValue());
			}
		}
		return filteredLogEntries;
	}

	/**
	 * Returns if a term definition has been registered with the specified name and if its class is
	 * of the specified class. Otherwise (if no such term is defined or it does not have a
	 * compatible class) false is returned.
	 *
	 * @param termIdentifier the term to be searched for
	 * @param clazz          the class the term must be a subclass of (or of the same class)
	 * @return if the term has been registered as required
	 * @created 05.03.2012
	 */
	public synchronized boolean hasTermOfClass(Identifier termIdentifier, Class<?> clazz) {
		for (Class<?> termClass : getTermClasses(termIdentifier)) {
			if (clazz.isAssignableFrom(termClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all term classes for a term or an empty Collection, if the term is undefined.<br/> A
	 * term only has multiple term classes, if the term is defined multiple times with a matching
	 * {@link Identifier} but different term classes.
	 *
	 * @param termIdentifier the {@link Identifier} for the term you want the term classes from
	 * @return all term classes or an empty Collection, if undefined
	 * @created 28.07.2012
	 */
	public synchronized Collection<Class<?>> getTermClasses(Identifier termIdentifier) {
		TermLog refLog = termLogManager.getLog(termIdentifier);
		if (refLog == null) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableCollection(refLog.getTermClasses());
		}
	}

}
