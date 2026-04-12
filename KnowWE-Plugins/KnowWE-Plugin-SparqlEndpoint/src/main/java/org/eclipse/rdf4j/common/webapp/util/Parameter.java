/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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
package org.eclipse.rdf4j.common.webapp.util;

/**
 * A parameter consisting of a key and a value, which are both strings.
 */
public class Parameter {

	private final String key;
	private final String value;

	public Parameter(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Parameter) {
			Parameter other = (Parameter) obj;
			return
					key.equals(other.getKey()) &&
							value.equals(other.getValue());
		}

		return false;
	}

	public int hashCode() {
		return key.hashCode();
	}

	public String toString() {
		if (value == null) {
			return key;
		}
		else {
			return key + "=" + value;
		}
	}
}
