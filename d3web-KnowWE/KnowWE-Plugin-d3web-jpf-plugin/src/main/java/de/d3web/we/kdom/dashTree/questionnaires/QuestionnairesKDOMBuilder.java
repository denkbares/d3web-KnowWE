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

package de.d3web.we.kdom.dashTree.questionnaires;

import java.util.List;
import java.util.Stack;

import de.d3web.we.kdom.ExpandedSectionFinderResult;
import de.d3web.we.kdom.basic.LineBreak;
import de.d3web.we.kdom.basic.TextLine;
import de.d3web.we.kdom.dashTree.DashTreeKDOMBuilder;
import de.d3web.we.kdom.dashTree.Dashes;
import de.d3web.we.kdom.dashTree.Tilde;
import de.d3web.we.kdom.decisionTree.QClassID;

public class QuestionnairesKDOMBuilder implements DashTreeKDOMBuilder {

	private Stack<ExpandedSectionFinderResult> sections = new Stack<ExpandedSectionFinderResult>();

	public void reInit() {
		sections = new Stack<ExpandedSectionFinderResult>();
	}

	public static String makeDashes(int k) {
		StringBuilder dashes = new StringBuilder();
		for (int i = 0; i < k; i++) {
			dashes.append("-");
		}
		return dashes.toString() + (k > 0 ? " " : "");
	}

	@Override
	public void finishOldQuestionsandConditions(int dashes) {
		throw new AssertionError("should not be called");
	}

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext) {
	}

	public ExpandedSectionFinderResult peek() {
		if (sections.size() == 0) return null;
		return sections.peek();
	}

	public void setSections(Stack<ExpandedSectionFinderResult> sections) {
		this.sections = sections;
	}

	@Override
	public void line(String text) {

	}

	public Stack<ExpandedSectionFinderResult> getSections() {
		return sections;
	}

	@Override
	public void newLine() {
		// TODO change to system property "linebreak"??
		sections.push(new ExpandedSectionFinderResult("\n", new TextLine(),
				sections.size() * (-1)));
	}

	@Override
	public void addNode(int dashes, String name, String ref, int line, String description, int order) {

		String da = makeDashes(dashes);
		String linetext = name + (description != null ? " ~ " + description : "") +
				(order != 0 ? " [" + order + "]" : "") + "\r\n";

		ExpandedSectionFinderResult qsection = new ExpandedSectionFinderResult(da
				+ linetext, new QuestionnaireLine(),
				sections.size() * (-1));

		if (dashes > 0) {
			qsection.addChild(new ExpandedSectionFinderResult(da, new Dashes(), 0));
		}

		if (name != null) {
			qsection.addChild(new ExpandedSectionFinderResult(name, new QClassID(),
					da.length()));
		}

		if (description != null) {
			qsection.addChild(new ExpandedSectionFinderResult(" ~", new Tilde(),
					getOffset(qsection)));

			qsection.addChild(new ExpandedSectionFinderResult(" " + description,
					new QClassDescription(),
					getOffset(qsection)));
		}

		if (order != 0) {
			qsection.addChild(new ExpandedSectionFinderResult(" [" + order + "]",
					new QClassOrder(),
					getOffset(qsection)));
		}

		qsection.addChild(new ExpandedSectionFinderResult("\r\n", new LineBreak(),
				getOffset(qsection)));

		sections.push(qsection);

	}

	private int getOffset(ExpandedSectionFinderResult father) {
		int i = 0;
		for (ExpandedSectionFinderResult child : father.getChildren()) {
			i += child.getText().length();
		}
		return i;
	}

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext) {
		throw new AssertionError("should not be called");
	}

	@Override
	public void addInclude(String url, int line, String linetext) {
		throw new AssertionError("should not be called");
	}

}
