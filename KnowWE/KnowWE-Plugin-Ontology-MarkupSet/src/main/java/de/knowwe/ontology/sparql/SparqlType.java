/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.ontology.sparql;

import org.ontoware.rdf2go.model.QueryResultTable;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;

/**
 * SparqlTypes provide a query and RenderOptions for the SparqlResultRender. How those are generated is decided by the
 * implementation.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 26.09.14
 */
public interface SparqlType extends Type {

	String getSparqlQuery(Section<? extends SparqlType> section, UserContext context);

	RenderOptions getRenderOptions(Section<? extends SparqlType> section, UserContext context);

	default QueryResultTable postProcessResult(QueryResultTable queryResultTable, UserContext context, RenderOptions opts) {
		return queryResultTable;
	}
}
