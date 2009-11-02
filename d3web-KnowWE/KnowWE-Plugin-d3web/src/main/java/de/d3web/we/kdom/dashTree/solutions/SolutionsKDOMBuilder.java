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

package de.d3web.we.kdom.dashTree.solutions;

import java.util.List;
import java.util.Stack;

import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.basic.TextLine;
import de.d3web.we.kdom.dashTree.DashTreeKDOMBuilder;
import de.d3web.we.kdom.dashTree.Tilde;
import de.d3web.we.kdom.sectionFinder.ExpandedSectionFinderResult;

public class SolutionsKDOMBuilder implements DashTreeKDOMBuilder {

	private Stack<ExpandedSectionFinderResult> sections = new Stack<ExpandedSectionFinderResult>();
	
	public SolutionsKDOMBuilder() {
	}

	public void reInit() {
		sections = new Stack<ExpandedSectionFinderResult>();
	}

	public static String makeDashes(int k) {
		String dashes = "";
		for (int i = 0; i < k; i++) {
			dashes += "-";
		}

		return dashes + " ";
	}

	@Override
	public void finishOldQuestionsandConditions(int dashes) {
	}

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext) {
	}

	public ExpandedSectionFinderResult peek() {
		if (sections.size() == 0)
			return null;
		return sections.peek();
	}

	public void setSections(Stack<ExpandedSectionFinderResult> sections) {
		this.sections = sections;
	}

//	public void setTopic(String topic) {
//		this.topic = topic;
//	}
//
//	public String getTopic() {
//		return topic;
//	}
//
//	public void setIdgen(IDGenerator idgen) {
//		this.idgen = idgen;
//	}

	@Override
	public void line(String text) {

	}

	public Stack<ExpandedSectionFinderResult> getSections() {
		return sections;
	}

	@Override
	public void newLine() {
		sections.push(new ExpandedSectionFinderResult("\r\n", new TextLine(), sections.size() * (-1)));
	}

	@Override
	public void addNode(int dashes, String name, int line, String description, int order) {
			
			// Generate SolutionLine Object
			ExpandedSectionFinderResult father = new ExpandedSectionFinderResult(((dashes != 0) ? makeDashes(dashes) : "" ) 
					+ name + ((description != null) ? " ~ " + description : "" ) + "\r\n", 
					new SolutionLine(), sections.size() * (-1));
			
			sections.push(father);
			
			// Generate Child for each SolutionLine-Element
			generateSection(dashes, name, line, description, order, father);

	}

	private void generateSection(int dashes, String name, int line, String description, 
			int order, ExpandedSectionFinderResult father) {
		
		
		if (dashes == 0) { // root diagnosis (not P000!)
			
//			father.addChild(new ExpandedSectionFinderResult(name + ((description == null) ? "\r\n" : "" ),
//				new SolutionDef(),  getOffset(father)));
			
			father.addChild(new ExpandedSectionFinderResult(name,
					new SolutionDef(),  getOffset(father)));
			
			if (description == null) {
				father.addChild(new ExpandedSectionFinderResult("\r\n", 
						new PlainText(), getOffset(father)));
			}
			
		} else { // normal diagnosis
			
			father.addChild(new ExpandedSectionFinderResult(makeDashes(dashes), 
					new PlainText(), getOffset(father)));
			
			father.addChild(new ExpandedSectionFinderResult(name,
					new SolutionDef(), getOffset(father)));			
			
			if (description == null) {
				father.addChild(new ExpandedSectionFinderResult("\r\n", 
						new PlainText(), getOffset(father)));
			}
			
		}
		
		if (description != null) { // save tilde and description
 			
			// Tilde ("~")
			father.addChild(new ExpandedSectionFinderResult(" ~ ", new Tilde(), getOffset(father)));	
			
			// Description			
			father.addChild(new ExpandedSectionFinderResult(description,
					new SolutionDescription(), getOffset(father)));	
			
			father.addChild(new ExpandedSectionFinderResult("\r\n", 
					new PlainText(), getOffset(father)));
			
		}
		
	}
	
	private int getOffset(ExpandedSectionFinderResult father) {
		int i = 0;
		for (ExpandedSectionFinderResult child:father.getChildren()) {
			i += child.getText().length();
		}
		return i;
	}

	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext) {
		// not necessary in this builder!
	}

	@Override
	public void addInclude(String url, int line, String linetext) {
		// TODO Auto-generated method stub
		
	}

}
