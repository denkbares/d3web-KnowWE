/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Albrecht
 * @created 16.12.2010
 */
public abstract class SimpleDefinition extends AbstractType implements SimpleTerm {

	private final Class<?> termObjectClass;

	public SimpleDefinition(TermRegistrationScope scope, Class<?> termObjectClass) {
		this.termObjectClass = termObjectClass;
		this.addSubtreeHandler(Priority.HIGHER,
				new StringDefinitionRegistrationHandler(scope));
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends SimpleTerm> section) {
		return termObjectClass;
	}

	@Override
	public String getTermIdentifier(Section<? extends SimpleTerm> s) {
		return s.getText();
	}

	private class StringDefinitionRegistrationHandler extends SubtreeHandler<SimpleDefinition> {

		private final TermRegistrationScope scope;

		public StringDefinitionRegistrationHandler(TermRegistrationScope scope) {
			this.scope = scope;
		}

		@Override
		public Collection<Message> create(Article article, Section<SimpleDefinition> s) {

			getTerminologyHandler(article).registerTermDefinition(s, s.get().getTermObjectClass(s),
					s.get().getTermIdentifier(s));

			return new ArrayList<Message>(0);
		}

		private TerminologyManager getTerminologyHandler(Article article) {
			if (scope == TermRegistrationScope.GLOBAL) {
				return KnowWEUtils.getGlobalTerminologyManager(article.getWeb());
			}
			else {
				return KnowWEUtils.getTerminologyManager(article);
			}
		}

		@Override
		public void destroy(Article article, Section<SimpleDefinition> s) {
			getTerminologyHandler(article).unregisterTermDefinition(s,
					s.get().getTermObjectClass(s), s.get().getTermIdentifier(s));
		}

	}

}
