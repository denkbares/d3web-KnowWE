/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.d3web.we.object;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermReference;

/**
 * Type for {@link IDObject} references
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 11.11.2010
 */
public class IDObjectReference extends D3webTermReference<IDObject> {

	public IDObjectReference() {
		super(IDObject.class);
	}

	@Override
	public IDObject getTermObjectFallback(KnowWEArticle article, Section<? extends TermReference<IDObject>> s) {
		if (s.get() instanceof IDObjectReference) {
			String idObjectName = s.get().getTermName(s);
			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
					article.getWeb()).getKBM(article.getTitle());
			IDObject idObject = mgn.findIDObjectByName(idObjectName);
			return idObject;
		}
		return null;
	}

}
