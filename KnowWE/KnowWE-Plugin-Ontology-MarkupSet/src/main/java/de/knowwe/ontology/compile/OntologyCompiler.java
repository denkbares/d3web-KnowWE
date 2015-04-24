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

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.ParallelScriptCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
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
import de.knowwe.rdf2go.RuleSet;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class OntologyCompiler extends AbstractPackageCompiler implements Rdf2GoCompiler, IncrementalCompiler, EventListener {

	public static final String COMMIT_NOTIFICATION_ID = "CommitNotification";
	private Rdf2GoCore rdf2GoCore;
	private TerminologyManager terminologyManager;
	private boolean completeCompilation = true;
	private boolean firstCompilation = true;
	private ParallelScriptCompiler<OntologyCompiler> scriptCompiler;
	private ParallelScriptCompiler<OntologyCompiler> destroyScriptCompiler;
	private final RuleSet ruleSet;
	private final String compilingArticle;

	public OntologyCompiler(PackageManager manager,
							Section<? extends PackageCompileType> compileSection,
							Class<? extends Type> compilingType,
							RuleSet ruleSet) {
		super(manager, compileSection, compilingType);
		EventManager.getInstance().registerListener(this);
		this.scriptCompiler = new ParallelScriptCompiler<>(this);
		this.destroyScriptCompiler = new ParallelScriptCompiler<>(this);
		this.ruleSet = ruleSet;
		this.compilingArticle = compileSection.getTitle();
	}

	@Override
	public String getArticleName() {
		return compilingArticle;
	}

	@Override
	public void destroy() {
		this.rdf2GoCore.destroy();
	}

	@Override
	public Rdf2GoCore getRdf2GoCore() {
		return rdf2GoCore;
	}

	@Override
	public TerminologyManager getTerminologyManager() {
		return terminologyManager;
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
			for (Section<?> section : sectionsOfPackage) {
				destroyScriptCompiler.addSubtree(section);
			}
			destroyScriptCompiler.destroy();
		}
		// While destroying, perhaps a complete compilation was requested by some of the scripts
		if (completeCompilation && !firstCompilation) {
			// Since we we later compile all sections in the compile step, we first have to destroy all of them.
			// This is different from just removing and adding a new compiler to the CompilerManager without
			// destroying in case of a full parse, because we still want to continue using the current compiler
			sectionsOfPackage = getPackageManager().getSectionsOfPackage(packagesToCompile);
			for (Section<?> section : sectionsOfPackage) {
				destroyScriptCompiler.addSubtree(section);
			}
			destroyScriptCompiler.destroy();
		}

		// a complete compilation... we reset TerminologyManager and Rdf2GoCore
		// we compile all sections of the compiled packages, not just the added ones
		if (completeCompilation) {
			this.rdf2GoCore = new Rdf2GoCore(ruleSet);
			createTerminologyManager();
			sectionsOfPackage = getPackageManager().getSectionsOfPackage(packagesToCompile);

		}
		// an incremental compilation... just compile the added sections
		else {
			sectionsOfPackage = getPackageManager().getAddedSections(packagesToCompile);
		}

		for (Section<?> section : sectionsOfPackage) {
			scriptCompiler.addSubtree(section);
		}
		// the %%Ontology markup section is not part of the package, so we add it manually just for this compiler
		scriptCompiler.addSubtree(Sections.ancestor(getCompileSection(), OntologyType.class));

		scriptCompiler.compile();

		if (getCommitType() == CommitType.onDemand) {
			NotificationManager.addGlobalNotification(new StandardNotification("There are changes not yet committed to " +
					"the ontology repository. Committing may take some time. If you want to commit the changes now, " +
					"click <a onclick=\"KNOWWE.plugin.ontology.commitOntology('" + getCompileSection().getID() + "')\">here</a>.",
					Message.Type.INFO, getCommitNotificationId()));
		}
		else {
			commitOntology();
		}

		EventManager.getInstance().fireEvent(new OntologyCompilerFinishedEvent(this));

		firstCompilation = false;
		completeCompilation = false;
		destroyScriptCompiler = new ParallelScriptCompiler<>(this);
		scriptCompiler = new ParallelScriptCompiler<>(this);
	}

	private CommitType getCommitType() {
		Section<OntologyType> ontologySection = Sections.ancestor(getCompileSection(), OntologyType.class);
		String commitTypeString = DefaultMarkupType.getAnnotation(ontologySection, OntologyType.ANNOTATION_COMMIT);
		CommitType commitType;
		try {
			commitType = CommitType.valueOf(commitTypeString);
		}
		catch (IllegalArgumentException | NullPointerException e) {
			commitType = CommitType.onSave;
		}
		return commitType;
	}

	private String getCommitNotificationId() {
		return COMMIT_NOTIFICATION_ID + getCompileSection().getID();
	}

	public void commitOntology() {
		rdf2GoCore.commit();
		NotificationManager.removeGlobalNotification(getCommitNotificationId());
	}

	private void createTerminologyManager() {
		this.terminologyManager = new TerminologyManager(true);
		// register the lns abbreviation immediately as defined
		this.getTerminologyManager().registerTermDefinition(this,
				this.getCompileSection(), AbbreviationDefinition.class,
				new Identifier(Rdf2GoCore.LNS_ABBREVIATION));
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

	public RuleSet getRuleSet() {
		return ruleSet;
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return Collections.singletonList(InitializedArticlesEvent.class);
	}

	@Override
	public void notify(Event event) {
		if (getCommitType() == CommitType.onDemand) {
			commitOntology();
		}
	}
}
