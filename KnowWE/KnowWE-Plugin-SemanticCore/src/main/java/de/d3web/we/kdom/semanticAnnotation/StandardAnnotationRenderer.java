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

package de.d3web.we.kdom.semanticAnnotation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.SemanticCoreUtil;
import de.d3web.we.core.semantic.UpperOntology;
import de.knowwe.core.contexts.Context;
import de.knowwe.core.contexts.ContextManager;
import de.knowwe.core.contexts.DefaultSubjectContext;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.ConditionalRenderer;

public class StandardAnnotationRenderer extends ConditionalRenderer {

	private static String TITLE_QUERY = "SELECT  ?title WHERE {  <URI> ns:hasTitle ?title }";

	@Override
	public void renderDefault(KnowWEArticle article, Section s, UserContext user,
			StringBuilder string) {

		Section<?> sec = s;
		String object = "no object found";
		Section<SimpleAnnotation> objectSection =
					Sections.findSuccessor(sec, SimpleAnnotation.class);
		if (objectSection != null) {
			object = objectSection.getOriginalText();
		}

		Section<AnnotatedString> astring =
					Sections.findSuccessor(sec, AnnotatedString.class);
		String text = "";
		if (astring != null) text = "''" + astring.getOriginalText() + "''";
		else text = "<b>" + object + "</b>";
		Section<SemanticAnnotationContent> content =
					Sections.findSuccessor(sec, SemanticAnnotationContent.class);
		Section<SemanticAnnotationPropertyName> propSection =
					Sections.findSuccessor(sec, SemanticAnnotationPropertyName.class);

		String property = "no property found";
		if (propSection != null) {
			property = propSection.getOriginalText();
		}

		String subject = "no subject found";

		Section<SemanticAnnotationSubject> subjectSection =
					Sections.findSuccessor(sec, SemanticAnnotationSubject.class);
		if (subjectSection != null
				&& subjectSection.getOriginalText().trim().length() > 0) {
			subject = subjectSection.getOriginalText();
		}
		else {

			Context context = ContextManager.getInstance().getContext(sec,
					DefaultSubjectContext.CID);
			if (context != null) {
				String solution = ((DefaultSubjectContext) context)
						.getSubject();

				URI solutionURI = UpperOntology.getInstance().getHelper().createlocalURI(
						solution);
				try {
					subject = URLDecoder.decode(solutionURI.getLocalName(), "UTF-8");
				}
				catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				TupleQueryResult result = SemanticCoreUtil.executeTupleQuery(
						TITLE_QUERY.replaceAll("URI", solutionURI.toString()),
						sec.getTitle());
				if (result != null) {
					try {
						if (result.hasNext()) {
							BindingSet set = result.next();
							String title = set.getBinding("title").getValue()
									.stringValue();
							try {
								title = URLDecoder.decode(title, "UTF-8");
								subject = title;
							}
							catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					catch (QueryEvaluationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		if (content != null) {
			String title = subject + " " + property + " " + object;
			text = KnowWEUtils.maskHTML("<a href=\"#" + s.getID() + "\"></a>"
					+ "<span title='" + title + "'>" + text + "</span>");
		}

		IntermediateOwlObject tempio = (IntermediateOwlObject) KnowWEUtils
				.getStoredObject(article, objectSection, OwlHelper.IOO);

		if (tempio == null) {
			text = KnowWEUtils
					.maskHTML("<p class=\"box error\">IntermediateOwlObject tempio not found"
							+ "</p>");
			string.append(text);
			return;
		}

		if (!tempio.getValidPropFlag()) {
			text = KnowWEUtils
					.maskHTML("<p class=\"box error\">invalid annotation attribute:"
							+ tempio.getBadAttribute() + "</p>");

		}
		tempio = (IntermediateOwlObject) KnowWEUtils
				.getStoredObject(
							Sections.findSuccessor(sec, SemanticAnnotationProperty.class),
						OwlHelper.IOO);
		if (tempio != null && !tempio.getValidPropFlag()) {
			text = KnowWEUtils
					.maskHTML("<p class=\"box error\">invalid annotation attribute:"
							+ tempio.getBadAttribute() + "</p>");

		}
		string.append(text);
	}

}
