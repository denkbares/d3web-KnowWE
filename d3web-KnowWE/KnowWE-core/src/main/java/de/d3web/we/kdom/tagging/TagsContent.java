/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.kdom.tagging;

import org.openrdf.model.URI;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.NothingRenderer;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class TagsContent extends XMLContent {

	@Override
	protected void init() {		
		this.setCustomRenderer(NothingRenderer.getInstance());
	}

	@Override
	public IntermediateOwlObject getOwl(Section s) {
		String text = s.getOriginalText();
		IntermediateOwlObject io = new IntermediateOwlObject();
		for (String cur : text.split(" |,")) {
			UpperOntology uo = UpperOntology.getInstance();
			URI suri = uo.getHelper().createlocalURI(s.getTitle());
			URI puri = uo.getHelper().createlocalURI("hasTag");
			URI ouri = uo.getHelper().createlocalURI(cur);
			io.merge(uo.getHelper().createProperty(suri, puri, ouri, s));
		}
		return io;
	}

}
