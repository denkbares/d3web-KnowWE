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
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.ScriptCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;

/**
 * Compiles d3web knowledge bases.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.11.2013
 */
public class D3webCompiler extends AbstractPackageCompiler implements TermCompiler {

	private TerminologyManager terminologyManager;
	private KnowledgeBase knowledgeBase;
	private final Section<? extends PackageCompileType> compileSection;

	public D3webCompiler(PackageManager packageManager,
						 Section<? extends PackageCompileType> compileSection,
						 Class<? extends Type> compilingType) {
		super(packageManager, compileSection, compilingType);
		this.compileSection = compileSection;
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public TerminologyManager getTerminologyManager() {
		if (terminologyManager == null) {
			// in case the compiler doesn't have anything to compile...
			return new TerminologyManager();
		}
		return terminologyManager;
	}

	public KnowledgeBase getKnowledgeBase() {
		if (knowledgeBase == null) {

			return KnowledgeBaseUtils.createKnowledgeBase();
		}
		return knowledgeBase;
	}

	/**
	 * FIXME: This method is currently only needed by the
	 * AnnotationLoadKnowledgeBaseHandler where a knowledge base is loaded from
	 * a file. The better way would be though to instead fill an existing
	 * knowledge base with the contents read from the file. We should implement
	 * this later and then remove this method.
	 *
	 * @created 06.01.2014
	 */
	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	@Override
	public void compilePackages(String[] packagesToCompile) {
		knowledgeBase = KnowledgeBaseUtils.createKnowledgeBase();
		// set id to title of article as default so it is no null
		knowledgeBase.setId(compileSection.getTitle());

		// we init the knowledge base before sending the start event so the
		// knowledge base of the compiler always returns the knowledge base of
		// the current compilation
		EventManager.getInstance().fireEvent(new D3webCompilerStartEvent(this));

		terminologyManager = new TerminologyManager();
		Messages.clearMessages(this);
		ScriptCompiler<D3webCompiler> scriptCompiler = new ScriptCompiler<D3webCompiler>(this);
		Collection<Section<?>> sectionsOfPackage = getPackageManager().getSectionsOfPackage(
				packagesToCompile);
		for (Section<?> section : sectionsOfPackage) {
			scriptCompiler.addSubtree(section);
		}
		// the %%KnowledgeBase markup section is not part of the package, so we add it manually just for this compiler
		scriptCompiler.addSubtree(Sections.ancestor(getCompileSection(), KnowledgeBaseType.class));

		scriptCompiler.compile();

		EventManager.getInstance().fireEvent(new D3webCompilerFinishedEvent(this));
	}

}
