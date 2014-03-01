/*
 * Copyright (C) 2010 denkbares GmbH, Germany
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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.solutionpanel.SolutionPanelUtils;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.TooltipRenderer;

/**
 * Renders a D3webTerm section by adding the current value(s) as a tooltip.
 * 
 * @author volker_belli
 * @created 30.11.2010
 */
public class ValueTooltipRenderer extends TooltipRenderer {

	public ValueTooltipRenderer(Renderer decoratedRenderer) {
		super(decoratedRenderer);
	}

	@Override
	protected String getTooltip(Section<?> section, UserContext user) {
		if (!(section.get() instanceof D3webTerm<?>)) return null;

		@SuppressWarnings("unchecked")
		Section<D3webTerm<NamedObject>> sec = (Section<D3webTerm<NamedObject>>) section;
		StringBuilder buffer = new StringBuilder();
		Collection<D3webCompiler> compilers = Compilers.getCompilers(section, D3webCompiler.class);
		for (D3webCompiler compiler : compilers) {

			NamedObject namedObject = sec.get().getTermObject(compiler, sec);
			KnowledgeBase knowledgeBase = D3webUtils.getKnowledgeBase(compiler);
			Session session = SessionProvider.getSession(user, knowledgeBase);
			if (namedObject instanceof ValueObject) {
				Value value = D3webUtils.getValueNonBlocking(session, (ValueObject) namedObject);
				if (value == null) continue;
				// if (UndefinedValue.isUndefinedValue(value)) continue;
				if (buffer.length() > 0) buffer.append('\n');
				String name = knowledgeBase.getName();
				if (name == null) name = compiler.getCompileSection().getTitle();
				buffer.append("current value in '").append(name).append("': ");
				buffer.append(SolutionPanelUtils.formatValue(value, 2));
			}
		}
		if (buffer.length() == 0) return null;
		return buffer.toString();
	}
}
