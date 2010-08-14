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

package de.d3web.we.alignment;

import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.basic.IdentifiableInstance;

public class LocalAlignment extends Alignment {

	protected final IdentifiableInstance local;

	public LocalAlignment(IdentifiableInstance local, IdentifiableInstance object, AbstractAlignType type) {
		super(object, type);
		this.local = local;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LocalAlignment)) return false;
		if (o == this) return true;
		LocalAlignment alignment = (LocalAlignment) o;
		if (!getType().equals(alignment.getType())) return false;
		return reflexiveEquals(alignment);
	}

	private boolean reflexiveEquals(LocalAlignment alignment) {
		return (getLocal().equals(alignment.getLocal()) && getObject().equals(alignment.getObject()))
				|| (getLocal().equals(alignment.getObject()) && getObject().equals(
						alignment.getLocal()));
	}

	public IdentifiableInstance getAligned(IdentifiableInstance ii) {
		if (ii.equals(getLocal())) return getObject();
		else return getLocal();
	}

	@Override
	public int hashCode() {
		return type.hashCode() + 37 * object.hashCode() + 37 * local.hashCode();
	}

	@Override
	public String toString() {
		return local.toString() + " " + super.toString();
	}

	public IdentifiableInstance getLocal() {
		return local;
	}

}
