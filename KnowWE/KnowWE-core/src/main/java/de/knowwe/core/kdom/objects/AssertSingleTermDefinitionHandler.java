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

import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
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
public class AssertSingleTermDefinitionHandler extends SubtreeHandler<Type> {

	TermRegistrationScope scope;

	public AssertSingleTermDefinitionHandler(TermRegistrationScope scope) {
		this.scope = scope;
	}

	@Override
	public Collection<Message> create(Article article, Section<Type> section) {
		TerminologyManager tHandler = KnowWEUtils.getTerminologyManager(article, scope);
		TermIdentifier termIdentifier = KnowWEUtils.getTermIdentifier(section);
		Collection<Section<?>> termDefinitions = tHandler.getTermDefiningSections(termIdentifier);
		Collection<Message> msgs = new ArrayList<Message>(1);
		Message msg = Messages.error("There is more than one definition for the term '"
				+ termIdentifier.toString() + "' which is restricted to only one definition.");
		if (termDefinitions.size() > 1) {
			msgs.add(msg);
			for (Section<?> termDef : termDefinitions) {
				Messages.storeMessages(article, termDef, this.getClass(), msgs);
			}
			for (Section<?> termRef : tHandler.getTermReferenceSections(termIdentifier)) {
				Messages.storeMessages(article, termRef, this.getClass(), msgs);
			}
		}
		return msgs;
	}
}
