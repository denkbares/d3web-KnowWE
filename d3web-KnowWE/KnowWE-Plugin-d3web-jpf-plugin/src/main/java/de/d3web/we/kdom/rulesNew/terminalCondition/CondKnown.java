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
package de.d3web.we.kdom.rulesNew.terminalCondition;

import java.util.List;

import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.QuestionRef;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * Implements the CondKnown Condition to be used as child type in
 * {@link TerminalCondition}
 *
 * syntax: KNOWN[<questionID>] / BEKANNT[<questionID>]
 *
 * @author Jochen
 *
 */
public class CondKnown extends D3webTerminalCondition<CondKnown> {

	protected static String[] KEYWORDS = {
			"KNOWN", "BEKANNT" };

	@Override
	protected void init() {
		this.sectionFinder = new CondKnownFinder();
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR7));

		QuestionRef question = new QuestionRef();
		question.setSectionFinder(new SectionFinder() {
			@Override
			public List<SectionFinderResult> lookForSections(String text, Section father) {
				return SectionFinderResult.createSingleItemList(new SectionFinderResult(
						text.indexOf('[') + 1, text.indexOf(']')));
			}
		});
		this.addChildType(question);
	}

	class CondKnownFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {

			for (String key : KEYWORDS) {
				if (text.trim().startsWith(key + "[") && text.trim().endsWith("]")) {
					return new AllTextFinderTrimmed().lookForSections(text,
							father);
				}
			}
			return null;
		}

	}

	@Override
	public TerminalCondition getTerminalCondition(KnowWEArticle article, Section<CondKnown> s) {
		Section<QuestionRef> qRef = s.findSuccessor(QuestionRef.class);
		if (qRef != null) {
			Question q = qRef.get().getObject(article, qRef);

			if (q != null) {
				return new de.d3web.core.inference.condition.CondKnown(q);
			}
		}
		return null;
	}

}
