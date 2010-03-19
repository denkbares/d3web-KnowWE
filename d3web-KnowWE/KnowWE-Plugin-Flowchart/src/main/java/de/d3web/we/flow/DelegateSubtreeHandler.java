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
package de.d3web.we.flow;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;

/**
 * Applies the subtree handler to all childs of s of the given type
 * @author Reinhard Hatko
 * Created on: 17.12.2009
 */
public class DelegateSubtreeHandler implements ReviseSubTreeHandler {

	private final ReviseSubTreeHandler handler;
	private final Class sectionType;
	
	
	public DelegateSubtreeHandler(ReviseSubTreeHandler handler,
			Class sectionType) {
		this.handler = handler;
		this.sectionType = sectionType;
	}

	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {

		List<Section> found = new ArrayList();
		s.findSuccessorsOfType(sectionType, found);
		
		
		for (Section child : found) {
			
			
			KDOMReportMessage message = handler.reviseSubtree(article, child);
			KDOMReportMessage.storeMessage(child, message);
			
		}
		
		return null;
		
	}

}
