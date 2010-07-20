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
package de.d3web.we.kdom.rulesNew.ruleAction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.indication.ActionIndication;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.QuestionnaireReference;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * @author Johannes Dienst
 *
 */
public class QuestionIndication extends D3webRuleAction<QuestionIndication> {

	@Override
	public void init() {
		this.sectionFinder = new QuestionIndicationSectionFinder();
		QuestionnaireReference qC = new QuestionnaireReference();
		qC.setSectionFinder(new SeperatedQuestionClassSectionFinder());
		this.childrenTypes.add(qC);
	}

	private class QuestionIndicationSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {

			if (text.contains(";")) {

				int start = 0;
				int end = text.length();
				while (text.charAt(start) == ' ' || text.charAt(start) == '"') {
					start++;
					if (start >= end - 1) return null;
				}
				while (text.charAt(end - 1) == ' ' || text.charAt(end - 1) == '"') {
					end--;
					if (start >= end - 1) return null;
				}

				List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
				result.add(new SectionFinderResult(start, end));
				return result;
			}

			return null;
		}

	}

	private class SeperatedQuestionClassSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {

			if (!text.equals("") && !text.equals(" ")) {
				int start = 0;
				int end;
				if (text.startsWith(" ")) start++;

				List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

				Pattern p = Pattern.compile(";");
				Matcher m = p.matcher(text);
				while (m.find()) {
					end = m.start();
					result.add(new SectionFinderResult(start, end));
					start = end + 1;
					if (text.charAt(start) == ' ') start++;
				}
				result.add(new SectionFinderResult(start, text.length()));
				return result;
			}
			return null;
		}

	}

	@Override
	public PSAction getAction(KnowWEArticle article, Section<QuestionIndication> s) {
		List<Section<QuestionnaireReference>> qContainerrefs = new ArrayList<Section<QuestionnaireReference>>();
		s.findSuccessorsOfType(QuestionnaireReference.class,
				qContainerrefs);


		if (qContainerrefs.size() > 0) {
			ActionIndication a = new ActionIndication();
			List<QASet> qContainers = new ArrayList<QASet>();
			for (Section<QuestionnaireReference> section : qContainerrefs) {
				QContainer qc = section.get().getTermObject(article, section);
				if (qc != null) {
					qContainers.add(qc);
				}
			}
			a.setQASets(qContainers);
			return a;
		}
		return null;
	}
}
