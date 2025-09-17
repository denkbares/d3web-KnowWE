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
package de.d3web.we.object;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.manage.NamedObjectFinderManager;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.02.2012
 */
public class NamedObjectReference extends D3webTermReference<NamedObject> {

	public NamedObjectReference() {
		this(null);
	}

	public NamedObjectReference(CompileScript<D3webCompiler, TermReference> compileScript) {
		this.setRenderer(new ValueTooltipRenderer(new NamedObjectRenderer()));
		if (compileScript == null) {
			compileScript = new SimpleReferenceRegistrationScript<>(D3webCompiler.class);
		}
		this.addCompileScript(compileScript);
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return NamedObject.class;
	}

	@Override
	public NamedObject getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<NamedObject>> section) {
		Collection<NamedObject> result = findTermObjects(compiler, section);
		return result.isEmpty() ? super.getTermObject(compiler, section) : result.iterator().next();
	}

	@NotNull
	private static Set<NamedObject> findTermObjects(D3webCompiler compiler, Section<? extends D3webTerm<NamedObject>> section) {
		if (section == null) return Collections.emptySet();
		String termIdentifier = section.get().getTermName(section);
		KnowledgeBase knowledgeBase = D3webUtils.getKnowledgeBase(compiler);
		return NamedObjectFinderManager.getInstance().find(knowledgeBase, termIdentifier);
	}
}
