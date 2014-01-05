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

import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.ScriptCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public class OntologyCompiler extends AbstractPackageCompiler implements TermCompiler, Rdf2GoCompiler, IncrementalCompiler {

	private Rdf2GoCore rdf2GoCore;
	private TerminologyManager terminologyManager;
	private boolean completeCompilation;
	private ScriptCompiler<OntologyCompiler> scriptCompiler;
	private ScriptCompiler<OntologyCompiler> destroyScriptCompiler;

	public OntologyCompiler(PackageManager manager, Section<? extends PackageCompileType> compileSection) {
		super(manager, compileSection);
		this.rdf2GoCore = new Rdf2GoCore();
		this.terminologyManager = new TerminologyManager();
		this.scriptCompiler = new ScriptCompiler<OntologyCompiler>(this);
		this.destroyScriptCompiler = new ScriptCompiler<OntologyCompiler>(this);
		this.completeCompilation = false;
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

		destroyScriptCompiler = new ScriptCompiler<OntologyCompiler>(this);
		scriptCompiler = new ScriptCompiler<OntologyCompiler>(this);

		Collection<Section<?>> sectionsOfPackage = getPackageManager().getRemovedSections(
				packagesToCompile);
		for (Section<?> section : sectionsOfPackage) {
			destroyScriptCompiler.addSubtree(section);
		}
		destroyScriptCompiler.destroy();

		if (completeCompilation) {
			this.rdf2GoCore = new Rdf2GoCore();
			this.terminologyManager = new TerminologyManager();
			sectionsOfPackage = getPackageManager().getSectionsOfPackage(packagesToCompile);
			completeCompilation = false;
		}
		else {
			sectionsOfPackage = getPackageManager().getAddedSections(packagesToCompile);
		}

		for (Section<?> section : sectionsOfPackage) {
			scriptCompiler.addSubtree(section);
		}
		scriptCompiler.compile();

		rdf2GoCore.commit();

		EventManager.getInstance().fireEvent(new OntologyCompilerFinishedEvent(this));
	}

	/**
	 * Call this method while destroying, if the compilation can no longer be
	 * done incrementally.
	 * 
	 * @created 04.01.2014
	 */
	public void doCompleteCompilation() {
		this.completeCompilation = true;
	}

	@Override
	public void addSectionsToDestroy(Collection<Section<?>> sections) {
		for (Section<?> section : sections) {
			destroyScriptCompiler.addSection(section);
		}
	}

	@Override
	public void addSectionsToCompile(Collection<Section<?>> sections) {
		for (Section<?> section : sections) {
			scriptCompiler.addSection(section);
		}
	}

}
