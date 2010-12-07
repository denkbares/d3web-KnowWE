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

import java.util.NoSuchElementException;

import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermReference;

/**
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 12.11.2010
 */
@SuppressWarnings("rawtypes")
public class PropertyReference extends D3webTermReference<Property> {

	/**
	 * @param termObjectClass
	 */
	public PropertyReference() {
		super(Property.class);
	}

	@Override
	public Property getTermObjectFallback(KnowWEArticle article, Section<? extends TermReference<Property>> s) {
		if (s.get() instanceof PropertyReference) {
			try {
				return Property.getUntypedProperty(s.get().getTermName(s));
			}
			catch (NoSuchElementException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public String getTermObjectDisplayName() {
		return "Property";
	}

}
