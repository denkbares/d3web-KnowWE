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

package de.d3web.we.terminology;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public abstract class D3webReviseSubTreeHandler implements ReviseSubTreeHandler {
	
	protected KnowledgeBaseManagement getKBM(Section sec) {
		KnowledgeRepresentationHandler handler = KnowledgeRepresentationManager.getInstance().getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			return ((D3webTerminologyHandler) handler).getKBM(sec);
		} 
		return null;
	}
	
	protected void useOldKnowledge(Section s) {
		KnowledgeRepresentationHandler handler = KnowledgeRepresentationManager.getInstance().getHandler("d3web");
		if (handler instanceof D3webTerminologyHandler) {
			handler.buildKnowledge(s);
		}
	}
	
//	protected boolean isUsingOldKnowledge(Section s) {
//		KnowledgeRepresentationHandler handler = KnowledgeRepresentationManager.getInstance().getHandler("d3web");
//		if (handler instanceof D3webTerminologyHandler) {
//			return ((D3webTerminologyHandler) handler).builtKBM(s);
//		}
//		return false;
//	}

}
