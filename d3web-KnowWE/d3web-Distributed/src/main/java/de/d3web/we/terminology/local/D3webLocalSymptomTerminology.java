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

package de.d3web.we.terminology.local;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;

public class D3webLocalSymptomTerminology implements LocalTerminologyAccess<IDObject> {

	private final KnowledgeBaseManagement kbm;

	public D3webLocalSymptomTerminology(KnowledgeBaseManagement kbm) {
		super();
		this.kbm = kbm;
	}

	@Override
	public IDObject getObject(String objectId, String valueId) {
		IDObject result = null;
		result = kbm.findQContainer(objectId);
		if (result == null) {
			result = kbm.findQuestion(objectId);
		}
		if (valueId == null) {
			return result;
		}
		else {
			if (result instanceof QuestionChoice) {
				return kbm.findChoice((QuestionChoice) result, valueId);
			}
		}
		return result;
	}
}
