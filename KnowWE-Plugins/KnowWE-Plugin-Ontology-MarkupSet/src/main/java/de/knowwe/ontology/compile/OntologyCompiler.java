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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.semanticcore.config.RdfConfig;
import com.denkbares.semanticcore.config.RepositoryConfig;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.ParallelScriptCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
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
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class OntologyCompiler extends AbstractPackageCompiler implements Rdf2GoCompiler, IncrementalCompiler, EventListener {

	static final String COMMIT_NOTIFICATION_ID = "CommitNotification";
	private Rdf2GoCore rdf2GoCore;
	private TerminologyManager terminologyManager;
	private boolean completeCompilation = true;
	private boolean firstCompilation = true;
	private ParallelScriptCompiler<OntologyCompiler> scriptCompiler;
	private ParallelScriptCompiler<OntologyCompiler> destroyScriptCompiler;
	private final RepositoryConfig ruleSet;
	private final String compilingArticle;
	private final MultiDefinitionMode multiDefinitionMode;
	private final ReferenceValidationMode referenceValidationMode;
	private final Set<Priority> commitTracker = ConcurrentHashMap.newKeySet();
	private boolean casesensitive;

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
		this.scriptCompiler = new ParallelScriptCompiler<>(this);
		this.ruleSet = ruleSet;
		this.compilingArticle = compileSection.getTitle();
		this.casesensitive = true;
	}

	public OntologyCompiler(PackageManager manager,
							Section<? extends PackageCompileType> compileSection,
							Class<? extends Type> compilingType,
							RepositoryConfig ruleSet, MultiDefinitionMode multiDefMode,
							ReferenceValidationMode referenceValidationMode,
							boolean casesensitive) {
		this(manager, compileSection, compilingType, ruleSet, multiDefMode, referenceValidationMode);
		this.casesensitive = casesensitive;
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
		return compilingArticle;
	}

	@Override
	public void destroy() {
		EventManager.getInstance().unregister(this);
		this.rdf2GoCore.destroy();
	}

	@Override
	public Rdf2GoCore getRdf2GoCore() {
		if (rdf2GoCore == null) {
			// in case the compiler doesn't have anything to compile...
			return new Rdf2GoCore(RepositoryConfigs.get(RdfConfig.class));
		}
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
					commitTracker.add(currentCompilePriority);
					rdf2GoCore.commit();
					Log.info("Requesting Rdf2GoCore while compiling priority " + currentCompilePriority + ". Committed statements.");
				}
			}
		}
		return rdf2GoCore;
	}

	@Override
	public TerminologyManager getTerminologyManager() {
		if (terminologyManager == null) {
			createTerminologyManager();
		}
		return terminologyManager;
	}

	private void createTerminologyManager() {
		this.terminologyManager = new TerminologyManager(casesensitive);
		// register the lns abbreviation immediately as defined
		this.terminologyManager.registerTermDefinition(this, getCompileSection(),
				AbbreviationDefinition.class, new Identifier(Rdf2GoCore.LNS_ABBREVIATION));
	}

	@Override
	public void compilePackages(String[] packagesToCompile) {
		EventManager.getInstance().fireEvent(new OntologyCompilerStartEvent(this));

		if (firstCompilation) createTerminologyManager();

		Collection<Section<?>> sectionsOfPackage;
		// If this is the first compilation of this compiler, we do not need to destroy, because the compiler
		// has not created yet.
		if (!firstCompilation) {
			sectionsOfPackage = getPackageManager().getRemovedSections(packagesToCompile);
			destroy(sectionsOfPackage);
		}
		// While destroying, perhaps a complete compilation was requested by some of the scripts
		if (completeCompilation && !firstCompilation) {
			// Since we we later compile all sections in the compile step, we first have to destroy all of them.
			// This is different from just removing and adding a new compiler to the CompilerManager without
			// destroying in case of a full parse, because we still want to continue using the current compiler
			sectionsOfPackage = getPackageManager().getSectionsOfPackage(packagesToCompile);
			destroy(sectionsOfPackage);
		}

		// a complete compilation... we reset TerminologyManager and Rdf2GoCore
		// we compile all sections of the compiled packages, not just the added ones
		if (completeCompilation) {
			if (this.rdf2GoCore != null) this.rdf2GoCore.destroy();
			this.rdf2GoCore = new Rdf2GoCore(ruleSet);
			createTerminologyManager();
			sectionsOfPackage = getPackageManager().getSectionsOfPackage(packagesToCompile);
		}
		// an incremental compilation... just compile the added sections
		else {
			sectionsOfPackage = getPackageManager().getAddedSections(packagesToCompile);
		}

		getCompilerManager().setCurrentCompilePriority(this, Priority.INIT);
		compile(sectionsOfPackage);

		boolean changed = false;
		if (getCommitType(this) == CommitType.onDemand) {
			NotificationManager.addGlobalNotification(new StandardNotification("There are changes not yet committed to " +
					"the ontology repository. Committing may take some time. If you want to commit the changes now, " +
					"click <a onclick=\"KNOWWE.plugin.ontology.commitOntology('" + getCompileSection().getID() + "')\">here</a>.",
					Message.Type.INFO, getCommitNotificationId(this)));
		}
		else {
			changed = commitOntology(this);
		}

		EventManager.getInstance().fireEvent(new OntologyCompilerFinishedEvent(this, changed));

		firstCompilation = false;
		completeCompilation = false;
		commitTracker.clear();
		destroyScriptCompiler = new ParallelScriptCompiler<>(this);
		scriptCompiler = new ParallelScriptCompiler<>(this);
	}

	private void compile(Collection<Section<?>> sectionsOfPackage) {
		for (Section<?> section : sectionsOfPackage) {
			// only compile the OntologyType sections belonging to this compiler
			if (!(section.get() instanceof OntologyType)
					|| Sections.ancestor(getCompileSection(), OntologyType.class) == section) {
				scriptCompiler.addSubtree(section);
			}
		}
		scriptCompiler.compile();
	}

	private void destroy(Collection<Section<?>> sectionsOfPackage) {
		for (Section<?> section : sectionsOfPackage) {
			destroyScriptCompiler.addSubtree(section);
		}
		destroyScriptCompiler.destroy();
	}

	static CommitType getCommitType(OntologyCompiler compiler) {
		Section<OntologyType> ontologySection = Sections.ancestor(compiler.getCompileSection(), OntologyType.class);
		String commitTypeString = DefaultMarkupType.getAnnotation(ontologySection, OntologyType.ANNOTATION_COMMIT);
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

	/**
	 * Call this method while destroying, if the compilation can no longer be done incrementally.
	 *
	 * @created 04.01.2014
	 */
	public void doCompleteCompilation() {
		this.completeCompilation = true;
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
		return Collections.singletonList(InitializedArticlesEvent.class);
	}

	@Override
	public void notify(Event event) {
		if (getCommitType(this) == CommitType.onDemand) {
			commitOntology(this);
		}
	}
}
