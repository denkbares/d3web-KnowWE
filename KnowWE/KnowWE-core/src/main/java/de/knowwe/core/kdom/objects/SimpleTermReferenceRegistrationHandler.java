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
package de.knowwe.core.kdom.objects;

import java.util.ArrayList;
import java.util.Collection;

import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.02.2012
 */
public class SimpleTermReferenceRegistrationHandler extends SubtreeHandler<SimpleTerm> {

	private final TermRegistrationScope scope;

	public SimpleTermReferenceRegistrationHandler(TermRegistrationScope scope) {
		this.scope = scope;
	}

	@Override
	public Collection<Message> create(KnowWEArticle article, Section<SimpleTerm> s) {

		TerminologyManager tHandler = KnowWEUtils.getTerminologyManager(article, scope);

		String termIdentifier = s.get().getTermIdentifier(s);

		if (s.get() instanceof SimpleTerm) {
			tHandler.registerTermReference(s,
					s.get().getTermObjectClass(), termIdentifier);
		}
		if (!tHandler.isDefinedTerm(termIdentifier)) {
			return Messages.asList(Messages.noSuchObjectError(termIdentifier));
		}

		return new ArrayList<Message>(0);
	}

}
