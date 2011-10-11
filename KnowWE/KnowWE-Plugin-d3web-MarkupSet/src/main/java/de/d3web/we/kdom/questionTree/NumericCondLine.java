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

package de.d3web.we.kdom.questionTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.NumericalInterval.IntervalException;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.compile.IncrementalConstraint;
import de.knowwe.core.compile.IncrementalMarker;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.report.KDOMReportMessage;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.report.message.InvalidNumberError;

public class NumericCondLine extends AbstractType implements IncrementalMarker, IncrementalConstraint<NumericCondLine> {

	@Override
	protected void init() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section father) {
				return (text.startsWith("[") && text.endsWith("]")) || text.startsWith("<")
						|| text.startsWith(">") || text.startsWith("=");
			}
		};

		this.addSubtreeHandler(new CheckConditionHandler());

		this.setCustomRenderer(StyleRenderer.NUMBER);

	}

	/**
	 * 
	 * This handler just checks whether a valid condition COULD be created from
	 * this NumericConditionLine using the same method as when real rules are
	 * created
	 * 
	 * @author Jochen
	 * @created 03.08.2010
	 */
	static class CheckConditionHandler extends D3webSubtreeHandler<NumericCondLine> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<NumericCondLine> s) {

			Section<DashTreeElement> dte = Sections.findAncestorOfType(s, DashTreeElement.class);
			Section<? extends DashTreeElement> fatherDashTreeElement = DashTreeUtils.getFatherDashTreeElement(dte);

			Condition simpleCondition = QuestionDashTreeUtils.createSimpleCondition(article, dte,
					fatherDashTreeElement);
			if (simpleCondition == null) {
				return Arrays.asList((KDOMReportMessage) new InvalidNumberError(
						"invalid numeric condition"));
			}

			return new ArrayList<KDOMReportMessage>(0);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<NumericCondLine> s) {
			KDOMReportMessage.clearMessages(article, s, CheckConditionHandler.class);
		}

	}

	public static Double getValue(Section<NumericCondLine> sec) {

		String content = sec.getOriginalText();
		if (content.startsWith("\"") && content.endsWith("\"")) {
			content = content.substring(1, content.length() - 1);
		}

		String value = content.substring(getComparator(sec).length()).trim();
		Double d = null;
		try {
			d = Double.parseDouble(value);
		}
		catch (NumberFormatException e) {
			// FIXME
		}
		return d;
	}

	public static String getComparator(Section<NumericCondLine> sec) {
		String content = sec.getOriginalText();
		if (content.startsWith("\"") && content.endsWith("\"")) {
			content = content.substring(1, content.length() - 1).trim();
		}

		String[] comps = {
				"<=", ">=", "<", ">", "=" };
		for (String string : comps) {
			if (content.startsWith(string)) {
				return string;
			}
		}
		return null;
	}

	public static NumericalInterval getNumericalInterval(Section<NumericCondLine> sec) {
		if (isIntervall(sec)) {
			String[] doubles = sec.getOriginalText().substring(1,
					sec.getOriginalText().length() - 1).split(" ");
			if (doubles.length == 2) {
				try {
					return new NumericalInterval(Double.parseDouble(doubles[0]),
							Double.parseDouble(doubles[1]));
				}
				catch (NumberFormatException e) {
					return null;
				}
				catch (IntervalException ie) {
					return null;
				}
			}
		}
		return null;
	}

	public static boolean isIntervall(Section<NumericCondLine> sec) {
		if (sec.getOriginalText().startsWith("[") && sec.getOriginalText().endsWith("]")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<NumericCondLine> s) {
		return QuestionDashTreeUtils.isChangeInRootQuestionSubtree(article, s);
	}

}
