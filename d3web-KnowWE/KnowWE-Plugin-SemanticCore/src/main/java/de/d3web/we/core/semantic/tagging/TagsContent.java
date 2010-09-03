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

package de.d3web.we.core.semantic.tagging;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.openrdf.model.URI;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.OwlSubtreeHandler;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.NothingRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.xml.XMLContent;

public class TagsContent extends XMLContent {

	@Override
	protected void init() {
		this.setCustomRenderer(NothingRenderer.getInstance());
		this.addSubtreeHandler(new TagsContentOWLSubTreeHandler());
	}

	private class TagsContentOWLSubTreeHandler extends OwlSubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			String text = s.getOriginalText();
			IntermediateOwlObject io = new IntermediateOwlObject();
			for (String cur : text.split(" |,")) {
				if (cur.trim().length() > 0) {
					UpperOntology uo = UpperOntology.getInstance();
					try {
						URI suri = uo.getHelper().createlocalURI(
								URLEncoder.encode(s.getTitle(), "UTF-8"));
						URI puri = OwlHelper.HASTAG;
						URI ouri = uo.getHelper().createlocalURI(cur.trim());
						io.merge(uo.getHelper().createProperty(suri, puri,
								ouri, s));
					}
					catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			SemanticCoreDelegator.getInstance().addStatements(io, s);

			return null;
		}
	}

}
