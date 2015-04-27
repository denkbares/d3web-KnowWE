/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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
package de.knowwe.rdfs.vis.markup.sparql;

import java.util.Map;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.Rdf2GoCoreCheckRenderer;
import de.knowwe.rdfs.vis.markup.ConceptVisualizationType;
import de.knowwe.rdfs.vis.markup.PreRenderer;
import de.knowwe.rdfs.vis.markup.VisualizationType;
import de.knowwe.visualization.GraphDataBuilder;

/**
 * Provides backwards compatibility for SparqlVisualization.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 27.04.15
 */
public class SparqlVisType extends SparqlVisualizationType {

	@Override
	public DefaultMarkup createMarkup() {
		DefaultMarkup markup = super.createMarkup();
		markup.setDeprecated("SparqlVisualization");
		return markup;
	}

	@Override
	protected String getMarkupName() {
		return "SparqlVis";
	}
}
