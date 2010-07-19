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
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;

/**
 * Render the default interview in KnowWE --- HTML / JS / CSS based
 *
 * @author Martina Freiberg
 * @created 15.07.2010
 */
public class HTMLInterviewRenderer {

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

		// Get the qcontainers for the current session / knowledge base
		List<QContainer> containers = setupKBAndGetInterviewElements(c);

		// Map for all processed TerminologyObjects, avoids endless recursion in
		// cyclic hierarchies
		Set<TerminologyObject> processedTOs = new HashSet<TerminologyObject>();

		// add plugin header
		buffi.append(getInterviewPluginHeader());

		return buffi.toString();
	}

	/**
	 * Gets the knowledge base of the current session and fills the QContainer
	 * list with the QContainers of that session, thereby orders them according
	 * to DFS strategy of the KnowledgeBaseManagement.
	 *
	 * @created 15.07.2010
	 */
	private static List<QContainer> setupKBAndGetInterviewElements(Session c) {

		// get the current knowledge base
		KnowledgeBase b = c.getKnowledgeBase();
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
		html.append("Interview");
		html.append("</h3>");
		return html.toString();
	}
}
