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

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.TerminologyHandler;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.KnowWETerm;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * This is an abstract class for types defining objects in d3web, such as
 * solutions, questions, questionnaires...
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 * @param <TermObject>
 */
public abstract class D3webTermDefinition<TermObject> extends TermDefinition<TermObject> {

	public D3webTermDefinition(Class<TermObject> termObjectClass) {
		super(termObjectClass);
	}

	@Override
	public String getTermIdentifier(Section<? extends KnowWETerm<TermObject>> s) {
		return KnowWEUtils.trimQuotes(s.getOriginalText());
	}

	protected Collection<Message> handleRedundantDefinition(KnowWEArticle article, Section<? extends D3webTermDefinition<TermObject>> s) {
		TermObject existingTermObject = s.get().getTermObject(article, s);
		String name = s.get().getTermName(s);
		TerminologyHandler terminologyHandler = KnowWEUtils.getTerminologyHandler(article.getWeb());
		Section<? extends TermDefinition<TermObject>> termDefiningSection =
				terminologyHandler.getTermDefiningSection(article, s);
		if (termDefiningSection != null
				&& existingTermObject == null
				&& !terminologyHandler.getOccupiedTerms().contains(name)) {
			return Messages.asList(D3webUtils.alreadyDefinedButErrors(
					s.get().getTermObjectClass().getSimpleName().toLowerCase(),
					name));
		}
		return new ArrayList<Message>(0);
	}
}
