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
package de.knowwe.core.compile;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Registering this module to your SuptreeHandler lets him compile, if the terms
 * in the TerminologyHandler were modified since the last build of the article.
 * 
 * @author Albrecht Striffler
 * @created 25.01.2011
 * @param <T> is the Type of the Section this module is used with.
 */
public class ModifiedTermsConstraint<T extends Type> extends ConstraintModule<T> {

	@Override
	public boolean violatedConstraints(Article article, Section<T> s) {
		return KnowWEUtils.getTerminologyManager(article).areTermDefinitionsModifiedFor(
				article);
	}

}
