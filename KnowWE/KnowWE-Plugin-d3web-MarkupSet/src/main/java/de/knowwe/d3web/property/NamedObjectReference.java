/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.d3web.property;

import java.util.regex.Pattern;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.D3webTermReference;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.KnowWEDomRenderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * This is a type that represents a {@link NamedObject}. Depending on type of
 * the NamedObject, there are different renderings. getNamedObject(...) returns
 * the matching NamedObject from the {@link KnowledgeBase}.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.08.2011
 */
public class NamedObjectReference extends D3webTermReference<NamedObject> {

	public NamedObjectReference() {
		super(NamedObject.class);
		this.clearSubtreeHandlers();

		this.setCustomRenderer(new NamedObjectRenderer());
		String questionAnswerPattern =
				"^\\s*(" + PropertyDeclarationType.NAME + "\\s*(?:#\\s*"
						+ PropertyDeclarationType.NAME + ")?)\\s*";
		this.setSectionFinder(new RegexSectionFinder(Pattern.compile(questionAnswerPattern), 1));

		QuestionReference questionReference = new QuestionReference();
		questionReference.setSectionFinder(new RegexSectionFinder(
				Pattern.compile("^\\s*(" + PropertyDeclarationType.NAME + ")\\s*#"), 1));
		this.addChildType(questionReference);

		PropertyAnswerReference answerReference = new PropertyAnswerReference();
		answerReference.setSectionFinder(new RegexSectionFinder(
				Pattern.compile("^#\\s*(" + PropertyDeclarationType.NAME + ")\\s*"), 1));
		this.addChildType(answerReference);
	}

	protected class PropertyAnswerReference extends AnswerReference {

		@Override
		public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
			return Sections.findChildOfType(s.getFather(), QuestionReference.class);
		}

	}

	/**
	 * 
	 * @author volker_belli
	 * @created 15.12.2010
	 */
	private static final class NamedObjectRenderer extends KnowWEDomRenderer<NamedObjectReference> {

		@SuppressWarnings({
				"rawtypes", "unchecked" })
		@Override
		public void render(KnowWEArticle article, Section<NamedObjectReference> sec, UserContext user, StringBuilder string) {
			NamedObject object = sec.get().getTermObject(article, sec);
			KnowWEDomRenderer renderer;
			if (object instanceof Question) {
				renderer = StyleRenderer.Question;
			}
			else if (object instanceof QContainer) {
				renderer = StyleRenderer.Questionaire;
			}
			else if (object instanceof Solution) {
				renderer = StyleRenderer.SOLUTION;
			}
			else if (object instanceof Choice) {
				renderer = StyleRenderer.CHOICE;
			}
			else if (object instanceof KnowledgeBase) {
				renderer = StyleRenderer.Questionaire;
			}
			else {
				renderer = PlainText.getInstance().getRenderer();
			}
			renderer.render(article, sec, user, string);
		}
	}

	@Override
	public NamedObject getTermObjectFallback(KnowWEArticle article, Section<? extends TermReference<NamedObject>> s) {
		Section<PropertyAnswerReference> propertyAnswerReference = Sections.findChildOfType(s,
				PropertyAnswerReference.class);
		if (propertyAnswerReference != null) {
			return propertyAnswerReference.get().getTermObject(article, propertyAnswerReference);
		}
		String name = KnowWEUtils.trimQuotes(s.getText().trim());
		KnowledgeBase knowledgeBase = D3webModule.getKnowledgeRepresentationHandler(
				article.getWeb()).getKB(article.getTitle());
		if (name.equals("KNOWLEDGEBASE") || name.equals(knowledgeBase.getName())) {
			return knowledgeBase;
		}
		return knowledgeBase.getManager().search(name);
	}

}
