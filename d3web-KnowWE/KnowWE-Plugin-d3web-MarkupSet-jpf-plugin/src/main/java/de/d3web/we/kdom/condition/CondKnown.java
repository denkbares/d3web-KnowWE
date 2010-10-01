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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;

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
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR7));

		QuestionReference question = new QuestionReference();
		question.setSectionFinder(new ISectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
				return SectionFinderResult.createSingleItemList(new SectionFinderResult(
						text.indexOf('[') + 1, text.indexOf(']')));
			}
		});
		this.addChildType(question);
		this.addSubtreeHandler(new CondKnownCreateHandler());
	}


	class CondKnownFinder implements ISectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

			for (String key : KEYWORDS) {
				if (text.trim().startsWith(key + "[") && text.trim().endsWith("]")) {
					return new AllTextFinderTrimmed().lookForSections(text,
							father, type);
				}
			}
			return null;
		}

	}

	class CondKnownCreateHandler extends D3webSubtreeHandler<CondKnown> {

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<CondKnown> s) {
			return getCondition(article, s) == null;
		}

		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<CondKnown> s) {
			return s.isOrHasSuccessorNotReusedBy(article.getTitle());
		}

		@Override
		public void destroy(KnowWEArticle article, Section<CondKnown> s) {
			storeCondition(article,
					null, s);
		}

		@Override
		public Collection<de.d3web.we.kdom.report.KDOMReportMessage> create(KnowWEArticle article, Section<CondKnown> s) {

			Section<QuestionReference> qRef = s.findSuccessor(QuestionReference.class);
			if (qRef != null) {
				Question q = qRef.get().getTermObject(article, qRef);

				if (q != null) {
					storeCondition(article,
							new de.d3web.core.inference.condition.CondKnown(q), s);
				}
				else {
					return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(""));
					
				}
			}
			return new ArrayList<KDOMReportMessage>(0);
		}

	}

}
