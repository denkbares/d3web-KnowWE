/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.compile;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.semanticcore.config.RdfConfig;
import com.denkbares.semanticcore.config.RepositoryConfig;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.ParallelScriptCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermDefinitionRegisteredEvent;
import de.knowwe.core.compile.terminology.TermDefinitionUnregisteredEvent;
import de.knowwe.core.compile.terminology.TermRegistrationEvent;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.StandardNotification;
import de.knowwe.ontology.kdom.namespace.AbbreviationDefinition;
import de.knowwe.rdf2go.ChangedStatementsEvent;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

import static de.knowwe.core.compile.ParallelScriptCompiler.Mode.compile;
import static de.knowwe.core.compile.ParallelScriptCompiler.Mode.destroy;
import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class OntologyCompiler extends AbstractPackageCompiler
		implements Rdf2GoCompiler, IncrementalCompiler, EventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyCompiler.class);

	static final String COMMIT_NOTIFICATION_ID = "CommitNotification";
	private Rdf2GoCore rdf2GoCore;
	private TerminologyManager terminologyManager;
	private boolean completeCompilation = true;
	private ParallelScriptCompiler<OntologyCompiler> scriptCompiler;
	private ParallelScriptCompiler<OntologyCompiler> destroyScriptCompiler;
	private final RepositoryConfig ruleSet;
	private final MultiDefinitionMode multiDefinitionMode;
	private final ReferenceValidationMode referenceValidationMode;
	private final Set<Priority> commitTracker = ConcurrentHashMap.newKeySet();
	private boolean caseSensitive;
	private boolean isIncrementalBuild;
	private volatile boolean changed;
	private Date buildDate = new Date();

	public OntologyCompiler(PackageManager manager,
							Section<? extends PackageCompileType> compileSection,
							Class<? extends Type> compilingType,
							RepositoryConfig ruleSet, MultiDefinitionMode multiDefMode, ReferenceValidationMode referenceValidationMode) {
		super(manager, compileSection, compilingType);
		synchronized (EventManager.getInstance()) {
			EventManager.getInstance().registerListener(this, EventManager.RegistrationType.WEAK);
		}
		this.multiDefinitionMode = multiDefMode == null ? MultiDefinitionMode.ignore : multiDefMode;
		this.referenceValidationMode = referenceValidationMode == null ? ReferenceValidationMode.error : referenceValidationMode;
		this.scriptCompiler = new ParallelScriptCompiler<>(this, compile);
		this.ruleSet = ruleSet;
		this.caseSensitive = true;
		this.isIncrementalBuild = false;
	}

	public OntologyCompiler(PackageManager manager,
							Section<? extends PackageCompileType> compileSection,
							Class<? extends Type> compilingType,
							RepositoryConfig ruleSet, MultiDefinitionMode multiDefMode,
							ReferenceValidationMode referenceValidationMode,
							boolean caseSensitive) {
		this(manager, compileSection, compilingType, ruleSet, multiDefMode, referenceValidationMode);
		this.caseSensitive = caseSensitive;
	}

	@Override
	@NotNull
	public MultiDefinitionMode getMultiDefinitionRegistrationMode() {
		return multiDefinitionMode;
	}

	@Override
	@NotNull
	public ReferenceValidationMode getReferenceValidationMode() {
		return referenceValidationMode;
	}

	@Override
	public String getArticleName() {
		return getCompileSection().getTitle();
	}

	@Override
	@NotNull
	public Section<OntologyType> getCompileSection() {
		return Sections.cast(super.getCompileSection(), OntologyType.class);
	}

	/**
	 * Returns true, if the last compilation of the compiler was done incrementally, false otherwise
	 */
	@Override
	public boolean isIncrementalBuild() {
		return isIncrementalBuild;
	}

	/**
	 * Returns the name of this compiler, normally given in the content %%Ontology section.
	 */
	@Override
	public String getName() {
		return $(getCompileSection()).successor(OntologyDefinition.class)
				.stream().map(s -> s.get().getTermName(s)).filter(Strings::nonBlank).findAny()
				.orElseGet(() -> getCompileSection().getTitle());
	}

	@Override
	public void destroy() {
		EventManager.getInstance().unregister(this);
		this.rdf2GoCore.close();
		this.rdf2GoCore = null; // make sure the core can be gc-ed, even if there are still references to the compiler
	}

	@Override
	public Rdf2GoCore getRdf2GoCore() {
		return getRdf2GoCore(false);
	}

	/**
	 * Gets the Rdf2GoCore of this OntologyCompiler. Setting the boolean <tt>committed</tt> to true will make sure, that
	 * when calling this method during compilation of the same compiler, all statements of the previous compile priority
	 * are committed and can be accessed via SPARQL. This can however come with negative effects on performance.
	 *
	 * @param committed decides whether the currenlty added statements should be committed when requesting the core
	 *                  during compilation
	 * @return the Rdf2GoCore of this compiler
	 */
	public Rdf2GoCore getRdf2GoCore(boolean committed) {

		if (rdf2GoCore == null) {
			// in case the compiler doesn't have anything to compile...
			return new Rdf2GoCore(RepositoryConfigs.get(RdfConfig.class));
		}
		if (!committed) return rdf2GoCore;

		if (getCompileSection().getArticleManager() == null) return rdf2GoCore;

		// if we are currently in the process of compiling this ontology, we perform a commit
		// on the Rdf2GoCore exactly once per priority (because the compile order is not stable inside
		// on priority anyway)

		Priority currentCompilePriority = getCompileSection()
				.getArticleManager()
				.getCompilerManager()
				.getCurrentCompilePriority(this);
		if (currentCompilePriority != null && !commitTracker.contains(currentCompilePriority)) {
			synchronized (commitTracker) {
				if (!commitTracker.contains(currentCompilePriority)) {
					long before = System.currentTimeMillis();
					rdf2GoCore.commit();
					commitTracker.add(currentCompilePriority);
					long after = System.currentTimeMillis();
					LOGGER.info("Requesting Rdf2GoCore while compiling priority " + currentCompilePriority + ". Committed statements in " + (after - before) + "ms");
				}
			}
		}
		return rdf2GoCore;
	}

	@Override
	public @NotNull TerminologyManager getTerminologyManager() {
		if (terminologyManager == null) {
			createTerminologyManager();
		}
		return terminologyManager;
	}

	private void createTerminologyManager() {
		this.terminologyManager = new TerminologyManager(caseSensitive);
		// register the lns abbreviation immediately as defined
		this.terminologyManager.registerTermDefinition(this, getCompileSection(),
				AbbreviationDefinition.class, new Identifier(Rdf2GoCore.LNS_ABBREVIATION));
	}

	@Override
	public void compilePackages(String[] packagesToCompile) {

		this.changed = false; // reset changed flag, will be updated by ChangedEvent from core
		Collection<Section<?>> sectionsOfPackageRemoved = Collections.emptySet();
		Collection<Section<?>> sectionsOfPackage;
		// a complete compilation... we reset TerminologyManager and Rdf2GoCore
		// we compile all sections of the compiled packages, not just the added ones
		if (completeCompilation) {
			if (this.rdf2GoCore != null) this.rdf2GoCore.close();
			this.rdf2GoCore = new Rdf2GoCore(getName(), null, ruleSet);
			createTerminologyManager();
			sectionsOfPackage = getPackageManager().getSectionsOfPackage(packagesToCompile);
		}
		// an incremental compilation... destroy the removed and compile the added sections
		else {
			isIncrementalBuild = true;
			sectionsOfPackageRemoved = getPackageManager().getRemovedSections(packagesToCompile);
			destroy(sectionsOfPackageRemoved);
			getTerminologyManager().cleanupStaleSection();
			sectionsOfPackage = getPackageManager().getAddedSections(packagesToCompile);
		}
		EventManager.getInstance()
				.fireEvent(new OntologyCompilerStartEvent(this, sectionsOfPackage, sectionsOfPackageRemoved, completeCompilation));
		getCompilerManager().setCurrentCompilePriority(this, Priority.INIT);
		compile(sectionsOfPackage);

		if (getCommitType(this) == CommitType.onDemand) {
			NotificationManager.addGlobalNotification(new StandardNotification("There are changes not yet committed to " +
					"the ontology repository. Committing may take some time. If you want to commit the changes now, " +
					"click <a onclick=\"KNOWWE.plugin.ontology.commitOntology('" + getCompileSection().getID() + "')\">here</a>.",
					Message.Type.INFO, getCommitNotificationId(this)));
		}
		else {
			commitOntology(this);
		}

		// fire proper CompilerFinishedEvent providing information about newly registered terms
		Set<TermRegistrationEvent<OntologyCompiler>> registeredTerms = new HashSet<>(termsRegistered.values());
		Set<TermRegistrationEvent<OntologyCompiler>> unregisteredTerms = new HashSet<>(termsUnregistered.values());
		registeredTerms.removeAll(termsUnregistered.values());
		unregisteredTerms.removeAll(termsRegistered.values());
		EventManager.getInstance()
				.fireEvent(new OntologyCompilerFinishedEvent(this, changed, unregisteredTerms, registeredTerms));

		// clean up and prepare for next compilation step
		buildDate = new Date();
		completeCompilation = false;
		commitTracker.clear();
		termsRegistered.clear();
		termsUnregistered.clear();
		destroyScriptCompiler = new ParallelScriptCompiler<>(this, destroy);
		scriptCompiler = new ParallelScriptCompiler<>(this, compile);
	}

	/**
	 * The date of the last build of this compile
	 */
	public Date getLastModified() {
		return buildDate;
	}

	private void compile(Collection<Section<?>> sectionsOfPackage) {
		for (Section<?> section : sectionsOfPackage) {
			// only compile the OntologyType sections belonging to this compiler
			if (!(section.get() instanceof OntologyType) || getCompileSection() == section) {
				scriptCompiler.addSubtree(section);
			}
		}
		scriptCompiler.run();
	}

	private void destroy(Collection<Section<?>> sectionsOfPackage) {
		for (Section<?> section : sectionsOfPackage) {
			destroyScriptCompiler.addSubtree(section);
		}
		destroyScriptCompiler.run();
	}

	static CommitType getCommitType(OntologyCompiler compiler) {
		String commitTypeString = DefaultMarkupType.getAnnotation(compiler.getCompileSection(), OntologyType.ANNOTATION_COMMIT);
		return Strings.parseEnum(commitTypeString, CommitType.onSave);
	}

	static String getCommitNotificationId(OntologyCompiler compiler) {
		return COMMIT_NOTIFICATION_ID + compiler.getCompileSection().getID();
	}

	public static boolean commitOntology(OntologyCompiler compiler) {
		boolean changed = compiler.rdf2GoCore.commit();
		NotificationManager.removeGlobalNotification(getCommitNotificationId(compiler));
		return changed;
	}

	@Override
	public void addSectionToDestroy(Section<?> section, Class<?>... scriptFilter) {
		destroyScriptCompiler.addSection(section, scriptFilter);
	}

	@Override
	public void addSectionToCompile(Section<?> section, Class<?>... scriptFilter) {
		scriptCompiler.addSection(section, scriptFilter);
	}

	@Override
	public void addSubtreeToDestroy(Section<?> section, Class<?>... scriptFilter) {
		destroyScriptCompiler.addSubtree(section, scriptFilter);
	}

	@Override
	public void addSubtreeToCompile(Section<?> section, Class<?>... scriptFilter) {
		scriptCompiler.addSubtree(section, scriptFilter);
	}

	public RepositoryConfig getReasoning() {
		return ruleSet;
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return List.of(InitializedArticlesEvent.class, ChangedStatementsEvent.class, TermDefinitionRegisteredEvent.class, TermDefinitionUnregisteredEvent.class);
	}

	/**
	 * Just for bookkeeping whether a compile step changes the set of registered terms,
	 * which is required to decide whether the caches/indexes need to be invalidated or not
	 */
	private final Map<Identifier, TermRegistrationEvent<OntologyCompiler>> termsUnregistered = new HashMap<>();
	private final Map<Identifier, TermRegistrationEvent<OntologyCompiler>> termsRegistered = new HashMap<>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void notify(Event event) {
		if (event instanceof InitializedArticlesEvent) {
			if (getCommitType(this) == CommitType.onDemand) {
				commitOntology(this);
			}
		}
		else if (event instanceof ChangedStatementsEvent) {
			if (((ChangedStatementsEvent) event).getCore() == getRdf2GoCore()) {
				this.changed = true;
			}
		}

		// we do some bookkeeping to find out, whether this compilation step actually changed the set of registered terms or not
		if (event instanceof TermDefinitionUnregisteredEvent termRegistrationUnregisteredEvent
				&& termRegistrationUnregisteredEvent.getCompiler().equals(this)) {
			termsUnregistered.put(termRegistrationUnregisteredEvent.getIdentifier(), termRegistrationUnregisteredEvent);
		}
		if (event instanceof TermDefinitionRegisteredEvent termDefinitionRegisteredEvent
				&& termDefinitionRegisteredEvent.getCompiler().equals(this)) {
			termsRegistered.put(termDefinitionRegisteredEvent.getIdentifier(), termDefinitionRegisteredEvent);
		}
	}
}
