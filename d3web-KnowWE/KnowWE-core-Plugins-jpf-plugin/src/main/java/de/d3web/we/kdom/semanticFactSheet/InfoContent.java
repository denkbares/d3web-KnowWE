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

/**
 * 
 */
package de.d3web.we.kdom.semanticFactSheet;


import java.util.Collection;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.PropertyManager;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author kazamatzuri
 * 
 */
public class InfoContent extends XMLContent {

	@Override
	public void init() {
		this.setCustomRenderer(InfoRenderer.getInstance());
		this.addSubtreeHandler(new InfoContentOWLSubTreeHandler());
	}

	private class InfoContentOWLSubTreeHandler extends SubtreeHandler {
		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			IntermediateOwlObject io = new IntermediateOwlObject();
			String text = s.getOriginalText();
			PropertyManager pm = PropertyManager.getInstance();
			String subjectconcept = ((DefaultSubjectContext) ContextManager
					.getInstance().getContext(s, DefaultSubjectContext.CID))
					.getSubject();
			for (String cur : text.split("\r\n|\r|\n")) {
				if (cur.trim().length() > 0) {
					String[] spaces = cur.split(" ");
					if (spaces.length > 0) {
						String prop = cur.split(" ")[0].trim();
						boolean valid = pm.isValid(prop);
						if (valid) {
							String value = cur.substring(cur.indexOf(" "),
									cur.length()).trim();
							io.merge(UpperOntology.getInstance().getHelper()
									.createProperty(subjectconcept, prop,
											value, s));
						} else {
							io.setValidPropFlag(valid);
							io.setBadAttribute(prop.trim());
							// break at first bad property
							KnowWEUtils.storeSectionInfo(s, OwlHelper.IOO, io);
							return null;
						}
					}

				}
			}
			KnowWEUtils.storeSectionInfo(s, OwlHelper.IOO, io);
			SemanticCore.getInstance().addStatements(io, s);
			return null;
		}

	}

}
