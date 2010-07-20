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

package de.d3web.we.d3webModule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;

/**
 * Render the default interview in KnowWE --- HTML / JS / CSS based
 * 
 * @author Martina Freiberg
 * @created 15.07.2010
 */
/**
 * 
 * @author meggy
 * @created 20.07.2010
 */
/**
 * 
 * @author meggy
 * @created 20.07.2010
 */
public class QuickInterviewRenderer {

	/**
	 * Assembles and returns the HTML representation of the interview.
	 * 
	 * @created 15.07.2010
	 * @param c
	 * @param web
	 * @return the String representation of the interview
	 */
	public static String renderInterview(Session c, String web) {

		// Assembles the Interview
		StringBuffer buffi = new StringBuffer();
		KnowledgeBase b = c.getKnowledgeBase();

		// Get the qcontainers for the current session / knowledge base
		List<QContainer> containers = setupKBAndGetInterviewElements(b, c);

		// Map all processed TerminologyObjects already in interview table,
		// avoids endless recursion in cyclic hierarchies
		Set<TerminologyObject> processedTOs = new HashSet<TerminologyObject>();

		// add plugin header
		buffi.append(getInterviewPluginHeader());

		buffi.append(getInterviewElmentsHTML(containers, processedTOs, c, web, b));

		return buffi.toString();
	}

	/**
	 * Gets the knowledge base of the current session and fills the QContainer
	 * list with the QContainers of that session, thereby orders them according
	 * to DFS strategy of the KnowledgeBaseManagement.
	 * 
	 * @created 15.07.2010
	 */
	private static List<QContainer> setupKBAndGetInterviewElements(KnowledgeBase b, Session c) {

		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(b);

		// get all qcontainers of kb into a list
		List<QContainer> containers = b.getQContainers();

		// sort according to DFS as implemented in KnowledgeBaseManagement
		kbm.sortQContainers(containers);

		return containers;
	}

	/**
	 * Returns the Plugin Header As String
	 * 
	 * @created 15.07.2010
	 * @return
	 */
	private static String getInterviewPluginHeader() {
		StringBuffer html = new StringBuffer();
		html.append("<h3>");
		html.append("Quick Interview");
		html.append("</h3>");
		return html.toString();
	}

	/**
	 * Create the HTML that appends all interview elements to the HTML displayed
	 * in the plugin underneath the header for the actual interview
	 * 
	 * @param web, b
	 * @param c
	 * 
	 * @created 20.07.2010
	 * @return the HTML String of all interview elements
	 */
	private static String getInterviewElmentsHTML(
			List<QContainer> containers, Set<TerminologyObject> processedTOs, Session c, String web, KnowledgeBase b) {
		StringBuffer html = new StringBuffer();
		boolean even = true;
		boolean first = true;
		boolean empty = false;

		// go through all qcontainers of the knowledge base
		for (QContainer container : containers) {

			processedTOs.add(container); // add to set of processed TOs

			// skip the topmost (sometimes default) root element "Q000"
			if (container.getName().endsWith("Q000")) continue;
			else {
				empty = (container.getNumberOfChildren() == 0) ? true : false;
			}

			String displayClass = first ? "class='visible'" : "class='hidden'";
			first = false;

			// get appropriate header
			html.append(getQContainerTableHeader(displayClass, container));

			// get TableRows for each Question within QContainer
			for (TerminologyObject to : container.getChildren()) {

				// if to is a Question, cast to Question explicitly
				Question q = null;
				if (to instanceof Question) {
					q = (Question) to;
				}
				else {
					continue;
				}

				// if question has no further children, i.e., no follow-up
				// questions
				if (q.getChildren().length == 0) {

					// assigns css classes according to whether line is even/odd
					String trClass = even ? "class='trEven'" : "class='trOdd'";

					// toggle even signature
					even = even ? false : true;

					html.append(getTableRowString(c, q, web, b.getId(), trClass));

					// if there are follow-up questions
					// TODO go on
				}

			}

			// close the QContainerTable
			html.append(getQContainerTableFooter());
		}
		return html.toString();
	}

	/**
	 * Get the header (opening and heading elements) for the interview tables
	 * 
	 * @created 20.07.2010
	 * @return the interview table header
	 */
	private static String getQContainerTableHeader(String displayClass, QContainer container) {
		StringBuffer html = new StringBuffer();
		html.append("<h4 class='qcontainerName pointer extend-htmlpanel-right'>");
		html.append("  " + container.getName() + ": ");
		html.append("</h4>");
		html.append("<table id='ivTbl" + container.getId() + "' " + displayClass + ">");
		return html.toString();
	}

	/**
	 * Get the footer (closing elements) for the interview tables
	 * 
	 * @created 20.07.2010
	 * @return the interview table footer
	 */
	private static String getQContainerTableFooter() {
		return "</table>";
	}

	/**
	 * Returns the string representation for one row of a Questionnaire table.
	 * 
	 * @created 20.07.2010
	 * @param c
	 * @param q
	 * @param web
	 * @param namespace
	 * @param trClass
	 * @return String representation for one row, i.e., a question and its set
	 *         of answers
	 */
	private static String getTableRowString(Session c, Question q, String web,
			String namespace, String trClass) {

		StringBuffer html = new StringBuffer();
		html.append("<tr " + trClass + ">");

		// render the label cell, i.e., the first cell displaying the Question
		html.append("<td>" + q.getName() + "</td>");

		// TODO fill in answers here

		html.append("</tr> \n");
		return html.toString();
	}

}
