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
package de.d3web.we.kdom.condition;

import java.util.List;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.knowwe.core.renderer.FontColorRenderer;

/**
 * Implements the CondKnown Condition to be used as child type in
 * {@link TerminalCondition}
 * 
 * syntax: KNOWN[<questionID>] / BEKANNT[<questionID>]
 * 
 * @author Jochen
 * 
 */
public class CondKnown extends D3webCondition<CondKnown> {

	protected static String[] KEYWORDS = {
			"KNOWN", "BEKANNT" };

	@Override
	protected void init() {
		this.sectionFinder = new CondKnownFinder();
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7) {

			@Override
			protected void renderContent(KnowWEArticle article, Section section, KnowWEUserContext user, StringBuilder string) {
				StringBuilder buffer = new StringBuilder();
				super.renderContent(article, section, user, buffer);
				string.append(buffer.toString().replace("[", "~[").replace("]", "~]"));
			}
		});

		QuestionReference question = new QuestionReference();
		question.setSectionFinder(new ISectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
				return SectionFinderResult.createSingleItemList(new SectionFinderResult(
						text.indexOf('[') + 1, text.indexOf(']')));
			}
		});
		this.addChildType(question);
	}

	@Override
	protected Condition createCondition(KnowWEArticle article, de.d3web.we.kdom.Section<CondKnown> section) {
		Section<QuestionReference> qRef = section.findSuccessor(QuestionReference.class);
		if (qRef != null) {
			Question q = qRef.get().getTermObject(article, qRef);

			if (q != null) {
				return new de.d3web.core.inference.condition.CondKnown(q);
			}
			else {
				KDOMError msg = new NoSuchObjectError(qRef.getOriginalText());
				KDOMReportMessage.storeSingleError(article, section, getClass(), msg);
				return null;
			}
		}
		KDOMError msg = new NoSuchObjectError("");
		KDOMReportMessage.storeSingleError(article, section, getClass(), msg);
		return null;
	}

	class CondKnownFinder implements ISectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

			for (String key : KEYWORDS) {
				if (text.trim().startsWith(key + "[") && text.trim().endsWith("]")) return new AllTextFinderTrimmed().lookForSections(
						text,
						father, type);
			}
			return null;
		}

	}

}
