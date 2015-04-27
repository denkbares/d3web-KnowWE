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

package de.knowwe.rdfs.vis.markup;

import de.knowwe.kdom.defaultMarkup.DefaultMarkup;

/**
 * Provides backwards compativility for ConceptVisualization.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 27.04.15
 */
public class ConceptVisType extends ConceptVisualizationType {


	@Override
	protected DefaultMarkup createMarkup() {
		DefaultMarkup markup = super.createMarkup();
		markup.setDeprecated("ConceptVisualization");
		return markup;
	}

	@Override
	protected String getMarkupName() {
		return "Vis";
	}
}
