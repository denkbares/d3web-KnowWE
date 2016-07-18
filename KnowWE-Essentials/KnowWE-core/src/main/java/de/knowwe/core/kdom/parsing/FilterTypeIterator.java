/*
 * Copyright (C) 2013 denkbares GmbH
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

package de.knowwe.core.kdom.parsing;

import java.util.Iterator;

import de.knowwe.core.kdom.Type;
import de.knowwe.kdom.filter.SectionFilter;

/**
 * Class that decorates an section iterator and filter all instances that does not match a specified
 * type. Additionally it creates a generic typed iterator for the specified target class.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 22.08.2014
 */
public class FilterTypeIterator<OT extends Type> implements Iterator<Section<OT>>, SectionFilter {

	private final Class<OT> clazz;
	private final Iterator base;

	@SuppressWarnings("unchecked")
	private FilterTypeIterator(Iterator base, Class<OT> clazz) {
		this.clazz = clazz;
		this.base = SectionFilter.filter(base, this);
	}

	public static <OT extends Type> FilterTypeIterator<OT> create(Iterator<? extends Section<?>> base, Class<OT> clazz) {
		return new FilterTypeIterator<>(base, clazz);
	}

	@Override
	public boolean hasNext() {
		return base.hasNext();
	}

	@Override
	public Section<OT> next() {
		return Sections.cast((Section) base.next(), clazz);
	}

	@Override
	public boolean accept(Section<?> section) {
		return clazz.isInstance(section.get());
	}
}
