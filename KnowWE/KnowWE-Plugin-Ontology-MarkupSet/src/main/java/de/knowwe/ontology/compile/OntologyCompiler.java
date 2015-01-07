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

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.ParallelScriptCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.kdom.namespace.AbbreviationDefinition;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.RuleSet;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class OntologyCompiler extends AbstractPackageCompiler implements Rdf2GoCompiler, IncrementalCompiler {

	private Rdf2GoCore rdf2GoCore;
	private TerminologyManager terminologyManager;
	private boolean completeCompilation = true;
	private boolean firstCompilation = true;
	private ParallelScriptCompiler<OntologyCompiler> scriptCompiler;
	private ParallelScriptCompiler<OntologyCompiler> destroyScriptCompiler;
	private final RuleSet ruleSet;
	private final String compilingArticle;

	public OntologyCompiler(PackageManager manager, Section<? extends PackageCompileType> compileSection, RuleSet ruleSet) {
		super(manager, compileSection);
		this.scriptCompiler = new ParallelScriptCompiler<>(this);
		this.destroyScriptCompiler = new ParallelScriptCompiler<>(this);
		this.ruleSet = ruleSet;
		this.compilingArticle = compileSection.getTitle();
	}

	public OntologyCompiler(PackageManager manager, Section<? extends PackageCompileType> compileSection) {
		this(manager, compileSection, null);
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

		scriptCompiler.compile();

		rdf2GoCore.commit();

		EventManager.getInstance().fireEvent(new OntologyCompilerFinishedEvent(this));

		firstCompilation = false;
		completeCompilation = false;
		destroyScriptCompiler = new ParallelScriptCompiler<>(this);
		scriptCompiler = new ParallelScriptCompiler<>(this);
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
}
