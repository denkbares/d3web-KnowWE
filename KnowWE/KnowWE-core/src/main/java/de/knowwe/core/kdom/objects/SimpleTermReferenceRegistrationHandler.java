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

import java.util.Collection;

import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
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
	public Collection<Message> create(Article article, Section<SimpleTerm> section) {

		TerminologyManager tHandler = KnowWEUtils.getTerminologyManager(article, scope);
		String termIdentifier = section.get().getTermIdentifier(section);

		tHandler.registerTermReference(section,
					section.get().getTermObjectClass(section), termIdentifier);

		return validateReference(article, section);
	}

	/**
	 * Validates the reference and returns a collection of error or warning
	 * messages if the reference is not correctly specified. Otherwise it
	 * returns an empty collection or a collection of info messages.
	 * 
	 * @created 28.02.2012
	 * @param article the compiling article
	 * @param section the section identifying the reference
	 * @return result messages of validation
	 */
	public Collection<Message> validateReference(Article article, Section<SimpleTerm> section) {
		TerminologyManager tHandler = KnowWEUtils.getTerminologyManager(article, scope);
		String termIdentifier = section.get().getTermIdentifier(section);
		if (!tHandler.isDefinedTerm(termIdentifier)) {
			return Messages.asList(Messages.noSuchObjectError(termIdentifier));
		}
		return Messages.noMessage();
	}
}
