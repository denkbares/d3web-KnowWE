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

package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * This is a marker class. If you are implementing a KnowWEObjectType with a
 * D3webSubtreeHandler that works with KnowWETerms anywhere in the Subtree, but
 * the KnowWEObjectType that the D3webSubtreeHandler is registered to is neither
 * an TermDefinition nor an TermReference, than you have to extend this class to
 * make the D3webSubtreeHandler able to distinguish. D3webSubtreeHandlers that
 * are not registered to a KnowWEObjectType implementing KnowWETerm,
 * respectively TermDefinition, TermReference or TermRelatedDefinition, will
 * create and destroy every time new TermDefinitions are registered or
 * unregistered in the TerminologyManager.
 * 
 * @author Albrecht Striffler
 * @created 19.07.2010
 */
public class TermRelatedDefinition extends DefaultAbstractKnowWEObjectType implements KnowWETerm<Object> {

	@Override
	public String getTermName(Section<? extends KnowWETerm<Object>> s) {
		return s.getOriginalText().trim();
	}

}
