/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.d3web.we.knowledgebase;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.ScriptCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Compiles d3web knowledge bases.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.11.2013
 */
public class D3webCompiler extends AbstractPackageCompiler implements TermCompiler {

	private final TerminologyManager terminologyManager;

	public D3webCompiler(PackageManager packageManager, Section<? extends PackageCompileType> compileSection) {
		super(packageManager, compileSection);
		terminologyManager = new TerminologyManager();
	}

	@Override
	public TerminologyManager getTerminologyManager() {
		return terminologyManager;
	}

	public KnowledgeBase getKnowledgeBase() {
		return D3webUtils.getKnowledgeBase(this);
	}

	@Override
	public void compilePackages(String[] packagesToCompile) {

		EventManager.getInstance().fireEvent(new D3webCompilerStartEvent(this));
		terminologyManager.removeTermsOfCompiler(this);
		ScriptCompiler<D3webCompiler> helper = new ScriptCompiler<D3webCompiler>(
				this);
		Collection<Section<?>> sectionsOfPackage = getPackageManager().getSectionsOfPackage(
				packagesToCompile);
		for (Section<?> section : sectionsOfPackage) {
			helper.addSubtree(section);
		}
		helper.compile();
		EventManager.getInstance().fireEvent(new D3webCompilerFinishedEvent(this));

	}

}
