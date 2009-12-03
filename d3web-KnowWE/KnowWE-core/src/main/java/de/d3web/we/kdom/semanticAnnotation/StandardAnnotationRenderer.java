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

package de.d3web.we.kdom.semanticAnnotation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.Context;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.renderer.ConditionalRenderer;
import de.d3web.we.utils.SPARQLUtil;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class StandardAnnotationRenderer extends ConditionalRenderer {

	private static String TITLE_QUERY = "SELECT  ?title WHERE {  <URI> lns:hasTitle ?title }";
	
	
	@Override
	public void renderDefault(Section sec, KnowWEUserContext user,
			StringBuilder string) {

		String object = "no object found";
		Section objectSection = sec.findSuccessor(new SimpleAnnotation());
		if (objectSection != null) {
			object = objectSection.getOriginalText();
		}

		Section astring = sec.findSuccessor(new AnnotatedString());
		String text = "";
		if (astring != null)
			text = "''" + astring.getOriginalText() + "''";
		else
			text = "<b>" + object + "</b>";
		Section content = sec.findSuccessor(new SemanticAnnotationContent());
		Section propSection = sec.findSuccessor(new SemanticAnnotationPropertyName());

		String property = "no property found";
		if (propSection != null) {
			property = propSection.getOriginalText();
		}

		String subject = "no subject found";

		Section subjectSectin = sec.findSuccessor(new SemanticAnnotationSubject());
		if (subjectSectin != null
				&& subjectSectin.getOriginalText().trim().length() > 0) {
			subject = subjectSectin.getOriginalText();
		} else {

			Context context = ContextManager.getInstance().getContext(sec,
					DefaultSubjectContext.CID);
			if (context != null) {
				URI solutionURI = ((DefaultSubjectContext) context).getSolutionURI();
				subject = solutionURI
						.getLocalName();
				TupleQueryResult result =  SPARQLUtil.executeTupleQuery(TITLE_QUERY.replaceAll("URI", solutionURI.toString()), sec.getTitle());
				if(result != null) {
					try {
						if(result.hasNext()) {
							BindingSet set = result.next();
							String title = set.getBinding("title").getValue().stringValue();
							 try {
								title = URLDecoder.decode(title, "UTF-8");
								subject = title;
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (QueryEvaluationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}

		if (content != null) {
			String title = subject + " " + property + " " + object;
			text = KnowWEEnvironment.maskHTML("<span title='" + title + "'>"
					+ text + "</span>");
		}
		if (!sec.getObjectType().getOwl(sec).getValidPropFlag()) {
			text = KnowWEEnvironment
					.maskHTML("<p class=\"box error\">invalid annotation attribute:"
							+ sec.getObjectType().getOwl(sec).getBadAttribute()
							+ "</p>");
		}

		string.append(text);
	}

}
