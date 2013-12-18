/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.turtle;

import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCore;

public class TurtleLongURI extends AbstractType implements NodeProvider<TurtleLongURI> {

	public TurtleLongURI() {
		this.setSectionFinder(new ExpressionInBracketsFinder('<', '>'));
		this.setRenderer(new Renderer() {

			@Override
			public void render(Section<?> section, UserContext user, RenderResult result) {
				result.append(Strings.encodeHtml(section.getText()));
			}
		});
	}



	private String getURI(Section<TurtleLongURI> section) {
		return section.getText().substring(1, section.getText().length() - 1);
	}

	@Override
	public Node getNode(Section<TurtleLongURI> section, Rdf2GoCore core) {
		String uri = getURI(section);
		return core.createURI(uri);
	}
}
