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

package de.knowwe.diaflux.type;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Allows to configure the FluxSolver directly at the knowledge base markup.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 10.08.14.
 */
public class ConfigureDiaFluxCompileScript implements D3webCompileScript<KnowledgeBaseType> {

	public static final String POTENTIAL_SOLUTIONS_ANNOTATION = "diaflux-PotentialSolutions";

	@Override
	public void compile(D3webCompiler compiler, Section<KnowledgeBaseType> section) throws CompilerMessage {
		String suggest = DefaultMarkupType.getAnnotation(section, POTENTIAL_SOLUTIONS_ANNOTATION);
		if (Strings.isBlank(suggest)) return;

		KnowledgeBase base = D3webUtils.getCompiler(section).getKnowledgeBase();
		FluxSolver solver = getSolver(base);
		if (solver != null) {
			boolean flag = Strings.equalsIgnoreCase(suggest, "suggest");
			solver.setSuggestPotentialSolutions(flag);
		}
		else {
			throw new CompilerMessage(new Message(Message.Type.WARNING,
					"No DiaFlux problem solver available in knowledge base, the annotation will be ignored."));
		}
	}

	private FluxSolver getSolver(KnowledgeBase base) {
		// Unlikely, the PSConfigs will be initialized first when creating a session
		// therefore we create a session and ask it for the solver, returning the
		// instance also added to the knowledge base to configure it
		Session session = SessionFactory.createSession(base);
		return session.getPSMethodInstance(FluxSolver.class);
	}
}
