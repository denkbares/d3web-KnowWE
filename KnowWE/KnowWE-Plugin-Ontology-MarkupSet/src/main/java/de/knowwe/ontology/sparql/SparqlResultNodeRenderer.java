/*
 * Copyright (C) 2012 denkbares GmbH
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
import org.ontoware.rdf2go.model.node.Node;

import de.knowwe.core.user.UserContext;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * Renderer for the {@link Node}s of the {@link QueryResultTable} returned by
 * SPARQL queries.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.07.2012
 */
public interface SparqlResultNodeRenderer {

	/**
	 * Gets the text of a {@link Node} from the QueryResultTable. The text might
	 * already be altered from other {@link SparqlResultNodeRenderer} called
	 * before the current one. The argument variable is the name of variable or
	 * column of the {@link QueryResultTable} for the current {@link Node}.
	 * 
	 * @created 13.07.2012
	 * @param node
	 *@param text the text of the node to render or alter
	 * @param variable the name of the variable of this node (or the column name
 *        in the table)   @return an altered/rendered version of the given text
	 */
	String renderNode(Node node, String text, String variable, UserContext user, Rdf2GoCore core, RenderMode mode);

	/**
	 * If the method returns <tt>false</tt>, the returned String of the method
	 * {@link #renderNode(Node, String, String, UserContext, Rdf2GoCore, RenderMode)} is not given to further
	 * {@link SparqlResultNodeRenderer}, but only of the text was changed in the
	 * current renderer. If the method returns <tt>true</tt>, it is given to the
	 * next {@link SparqlResultNodeRenderer}, whether or not the text has
	 * changed.
	 * 
	 * @created 13.07.2012
	 * @return if other renderer are allowed to further alter the returned text
	 *         of the method {@link #renderNode(Node, String, String, UserContext, Rdf2GoCore, RenderMode)}
	 */
	boolean allowFollowUpRenderer();

}
