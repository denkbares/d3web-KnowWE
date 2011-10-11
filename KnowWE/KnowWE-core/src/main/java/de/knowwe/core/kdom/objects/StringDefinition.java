/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.knowwe.core.kdom.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.TerminologyHandler;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.KDOMReportMessage;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.report.message.ObjectAlreadyDefinedError;

/**
 * 
 * @author Albrecht
 * @created 16.12.2010 
 */
public abstract class StringDefinition extends TermDefinition<String> {

	public StringDefinition() {
		super(String.class);
		this.addSubtreeHandler(Priority.HIGHER, new StringDefinitionRegistrationHandler());
	}

	/**
	 * 
	 * This handler registers this Term..
	 * 
	 * @author Jochen, Albrecht
	 * @created 08.10.2010
	 */
	static class StringDefinitionRegistrationHandler extends SubtreeHandler<StringDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<StringDefinition> s) {

			TerminologyHandler tHandler = KnowWEUtils.getTerminologyHandler(article.getWeb());

			tHandler.registerTermDefinition(article, s);

			Section<? extends TermDefinition<String>> defSec = tHandler.getTermDefiningSection(
					article, s);

			if (defSec != s) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedError(
						s.get().getName()
								+ ": " + s.get().getTermIdentifier(s), s));
			}

			s.get().storeTermObject(article, s, s.get().getTermIdentifier(s));

			return new ArrayList<KDOMReportMessage>(0);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<StringDefinition> s) {
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
					article, s);
		}

	}

}
