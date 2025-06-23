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

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.TerminologyManager;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.tools.CompositeEditToolProvider;
import de.knowwe.core.tools.CompositeEditToolVerbalizer;

/**
 * Common section type for d3web terms
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 06.02.2012
 */
public interface D3webTerm<TermObject extends NamedObject> extends Term, CompositeEditToolVerbalizer {

	@Nullable
	TermObject getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<TermObject>> section);

	@Override
	default Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return new Identifier(true, getTermName(section));
	}

	@Override
	default String getCompositeEditToolText(TermCompiler compiler, Identifier identifier) {
		String type = "";
		if (compiler instanceof D3webCompiler) {
			NamedObject object = getTerminologyObject((D3webCompiler) compiler, identifier);
			if (object != null) {
				type = " <span class='dehighlighted-text'>(" + object.getClass().getSimpleName() + ")</span>";
			}
		}
		return CompositeEditToolProvider.createToolText(identifier.getLastPathElement() + type);
	}

	@Override
	default boolean showToolForIdentifier(TermCompiler compiler, Identifier identifier) {
		return compiler instanceof D3webCompiler d3webCompiler
				&& getTerminologyObject(d3webCompiler, identifier) != null;
	}

	@Nullable
	private NamedObject getTerminologyObject(D3webCompiler compiler, Identifier identifier) {
		TerminologyManager manager = compiler.getKnowledgeBase().getManager();
		if (identifier.countPathElements() == 1) {
			return manager
					.search(identifier.getLastPathElement());
		}
		else if (identifier.countPathElements() == 2) {
			TerminologyObject object = manager.search(identifier.getPathElementAt(0));
			if (object instanceof QuestionChoice questionChoice) {
				return KnowledgeBaseUtils.findChoice(questionChoice, identifier.getLastPathElement());
			}
		}
		return null;
	}
}
