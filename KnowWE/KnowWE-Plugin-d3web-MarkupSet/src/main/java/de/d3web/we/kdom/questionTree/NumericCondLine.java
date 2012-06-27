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
import java.util.Collection;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.NumericalInterval.IntervalException;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;

public class NumericCondLine extends AbstractType {

	public NumericCondLine() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return (text.startsWith("[") && text.endsWith("]")) || text.startsWith("<")
						|| text.startsWith(">") || text.startsWith("=");
			}
		};

		this.addSubtreeHandler(new CheckConditionHandler());

		this.setRenderer(StyleRenderer.NUMBER);

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
		public Collection<Message> create(Article article, Section<NumericCondLine> s) {

			Section<DashTreeElement> dte = Sections.findAncestorOfType(s, DashTreeElement.class);
			Section<? extends DashTreeElement> fatherDashTreeElement = DashTreeUtils.getFatherDashTreeElement(dte);

			Condition simpleCondition = QuestionDashTreeUtils.createSimpleCondition(article, dte,
					fatherDashTreeElement);
			if (simpleCondition == null) {
				return Messages.asList(Messages.invalidNumberError(
						"invalid numeric condition"));
			}

			return new ArrayList<Message>(0);
		}

		@Override
		public void destroy(Article article, Section<NumericCondLine> s) {
			Messages.clearMessages(article, s, CheckConditionHandler.class);
		}

	}

	public static Double getValue(Section<NumericCondLine> sec) {

		String content = sec.getText();
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
		String content = sec.getText();
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
			String[] doubles = sec.getText().substring(1,
					sec.getText().length() - 1).split(" ");
			if (doubles.length == 2) {
				try {
					NumericalInterval interval = new NumericalInterval(
							Double.parseDouble(doubles[0]),
							Double.parseDouble(doubles[1]));
					interval.checkValidity();
					return interval;
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
		if (sec.getText().startsWith("[") && sec.getText().endsWith("]")) {
			return true;
		}
		return false;
	}

}
