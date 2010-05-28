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

package de.d3web.we.kdom.dashTree;

import java.util.List;
import java.util.Stack;

import de.d3web.KnOfficeParser.dashtree.DashTBuilder;
import de.d3web.we.kdom.sectionFinder.ExpandedSectionFinderResult;

public interface DashTreeKDOMBuilder extends DashTBuilder {

	public void reInit();

	public ExpandedSectionFinderResult peek();

	public void setSections(Stack<ExpandedSectionFinderResult> sections);

	// public void setTopic(String topic);
	//
	// public String getTopic();
	//
	// public void setIdgen(IDGenerator idgen);

	public Stack<ExpandedSectionFinderResult> getSections();

	@Override
	public void line(String text);

	@Override
	public void finishOldQuestionsandConditions(int dashes);

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext);

	@Override
	public void newLine();

	@Override
	public void addNode(int dashes, String name, String ref, int line, String description, int order);

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext);

	@Override
	public void addInclude(String url, int line, String linetext);

}
