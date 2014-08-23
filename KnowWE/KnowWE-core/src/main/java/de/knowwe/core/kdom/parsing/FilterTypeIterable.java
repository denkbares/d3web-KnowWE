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

/**
 * Class that wraps an iterable of sections to filter all sections that are not of a specific type.
 * In contrast to usual filtering iterators, this class is capable to deliver a well-casted steam of
 * sections.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 22.08.14.
 */
public class FilterTypeIterable<OT extends Type> implements Iterable<Section<OT>> {
	private final Class<OT> clazz;
	private final Iterable base;

	private FilterTypeIterable(Iterable base, Class<OT> clazz) {
		this.base = base;
		this.clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Section<OT>> iterator() {
		return FilterTypeIterator.create(base.iterator(), clazz);
	}

	public static <T extends Type, OT extends Type> FilterTypeIterable<OT> create(Iterable<Section<T>> base, Class<OT> clazz) {
		return new FilterTypeIterable<OT>(base, clazz);
	}
}

