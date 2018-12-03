/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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
package de.d3web.we.object;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Type for qaset references (usually questions or questionnaires)
 * 
 * @author Volker Belli
 * @created 14.08.2012
 */
public class QASetReference extends D3webTermReference<QASet> {

	private static final QASetRenderer RENDERER = new QASetRenderer();

	private static final class QASetRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult string) {
			Renderer delegate = PlainText.getInstance().getRenderer();

			// check if section is Questionnaire to use other renderer
			Collection<D3webCompiler> compilers = Compilers.getCompilers(section,
					D3webCompiler.class);
			for (D3webCompiler compiler : compilers) {
				TerminologyManager tm = compiler.getTerminologyManager();
				Section<Term> cast = Sections.cast(section, Term.class);
				Identifier termIdentifier = cast.get().getTermIdentifier(compiler, cast);
				if (tm.hasTermOfClass(termIdentifier, Question.class)) {
					delegate = new ValueTooltipRenderer(StyleRenderer.Question);
					break;
				}
				if (tm.hasTermOfClass(termIdentifier, QContainer.class)) {
					delegate = StyleRenderer.Questionaire;
					break;
				}
			}

			delegate.render(section, user, string);
		}
	}

	public QASetReference() {
		this.setRenderer(RENDERER);
		this.addCompileScript(new SimpleReferenceRegistrationScript<>(
				D3webCompiler.class));
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return QASet.class;
	}

}
