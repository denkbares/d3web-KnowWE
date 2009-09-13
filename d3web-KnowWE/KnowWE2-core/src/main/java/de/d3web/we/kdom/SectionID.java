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

package de.d3web.we.kdom;

public class SectionID {
	
	private String id;
	
	private String suffix;
	
	private boolean idConflict;
	
	/**
	 * Generates a new unique SectionID.
	 */
	public SectionID(SectionIDGen idGen) {
		this.id = idGen.createID("", "");
		this.suffix = "";
		this.idConflict = false;
	}
	
	/**
	 * SectionID assures uniqueness of IDs. If the String resulting in the
	 * concatenation of id and suffix already got assigned by the
	 * given SectionIDGen, the given ID in this instantiation will get replaced by
	 * a generated ID. The suffix will stay the same.
	 * 
	 * If no suffix is needed, set suffix to <code>null</code>.
	 * If suffix but no special ID is needed, set id to <code>null</code>.
	 * 
	 */
	public SectionID(String id, String suffix, SectionIDGen idGen){
		this.id = idGen.createID(id, suffix);
		if (suffix == null)
			suffix = "";
		this.suffix = suffix;
		if (id == null) {
			this.idConflict = false;
		} else {
			if (suffix == null) {
				this.idConflict = !this.id.equals(id);
			} else {
				this.idConflict = !this.id.equals(id + suffix);
			}
		}
	}
	
	/**
	 * Returns the id. It already includes the suffix.
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Returns the suffix of this id without the acutal id.
	 */
	public String getSuffix() {
		return suffix;
	}
	
	public boolean isIdConflict() {
		return idConflict;
	}
	
	@Override
	public String toString() {
		return getID();
	}
}
