/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.timeline.tree;

import java.util.Date;
import java.util.Map.Entry;
import java.util.SortedMap;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.QuestionValue;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.NumValue;
import de.knowwe.timeline.IDataProvider;
import de.knowwe.timeline.Timeset;
import de.knowwe.timeline.Timespan;
import de.knowwe.timeline.parser.ASTand_op;
import de.knowwe.timeline.parser.ASTor_op;
import de.knowwe.timeline.parser.ASTquery;
import de.knowwe.timeline.parser.ASTsimpleElement;
import de.knowwe.timeline.parser.ASTtimeFilter;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class TimesetVisitor extends AbstractVisitor {

	private final IDataProvider dataProvider;

	public TimesetVisitor(IDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	@Override
	public Timeset visit(ASTand_op node, Object data) {
		Timeset t1 = node.jjtGetChild(0).jjtAccept(this, data);
		Timeset t2 = node.jjtGetChild(1).jjtAccept(this, data);
		return t1.intersect(t2);
	}

	@Override
	public Timeset visit(ASTor_op node, Object data) {
		Timeset t1 = node.jjtGetChild(0).jjtAccept(this, data);
		Timeset t2 = node.jjtGetChild(1).jjtAccept(this, data);
		return t1.union(t2);
	}

	@Override
	public Timeset visit(ASTquery node, Object data) {
		if(node.jjtGetNumChildren() < 1)
			return new Timeset();
		return node.jjtGetChild(0).jjtAccept(this, data);
	}

	@Override
	public Timeset visit(ASTtimeFilter node, Object data) {
		Timeset t = node.jjtGetChild(0).jjtAccept(this, data);
		t.removeDurationNotMatching((long) (node.number * node.unit.getMultiplier()), node.comp);
		return t;
	}

	@Override
	public Timeset visit(ASTsimpleElement node, Object data) {
		String questionName = node.question;
		Question question = dataProvider.searchQuestion(questionName);
		if (question == null) {
			// TODO: show error, invalid question
			return new Timeset();
		}

		Selectors selector = node.selector;
		QuestionValue compVal = null;
		if (node.comp != null) {
			compVal = KnowledgeBaseUtils.findValue(question, node.compValue);
			if (compVal == null) {
				// TODO: show error, invalid compare value
				return new Timeset();
			}
		}

		SortedMap<Date, Value> values = dataProvider.getValues(question);
		Timeset ts = new Timeset();
		Date last = null;
		Value lastVal = null;
		Double lastDouble = null;
		for (Entry<Date, Value> d : values.entrySet()) {
			Value v = d.getValue();

			if (selector != null) {
				if (compVal != null) {
					if (question instanceof QuestionNum) {
						NumValue nVal = (NumValue) v;
						NumValue nCompVal = (NumValue) compVal;
						Double val = nVal.getDouble();
						Double valS = selector.getValue(lastDouble, val);
						if (valS != null
								&& (compVal != null
										&& node.comp.matches(valS, nCompVal.getDouble()) || valS > 0)) {
							ts.add(new Timespan(d.getKey()));
						}
						lastDouble = val;
					}
					else {
						// TODO: show error (invalid combination of selector and comparator for not numeric question
						return new Timeset();
					}
				}
				else {
					if (selector.matches(lastVal, v)) {
						ts.add(new Timespan(d.getKey()));
					}
					lastVal = v;
				}
			}
			else if (compVal == null || node.comp.matches(v, compVal)) {
				ts.add(new Timespan(d.getKey()));
				if (last != null) {
					ts.add(new Timespan(last, d.getKey()));
				}
				last = d.getKey();
			}
			else {
				last = null;
			}

		}
		return ts;
	}
}
