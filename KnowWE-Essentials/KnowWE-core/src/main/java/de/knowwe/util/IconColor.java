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

package de.knowwe.util;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 12.01.15.
 */
public class IconColor {

	public static final IconColor NONE = new IconColor(null);
	public static final IconColor GREEN = new IconColor("knowwe-green");
	public static final IconColor RED = new IconColor("knowwe-red");
	public static final IconColor YELLOW = new IconColor("knowwe-yellow");
	public static final IconColor BLUE = new IconColor("knowwe-blue");
	public static final IconColor GRAY = new IconColor("knowwe-gray");

	public static final IconColor OK = GREEN;
	public static final IconColor ERROR = RED;
	public static final IconColor WARNING = YELLOW;
	public static final IconColor DISABLED = GRAY;

	private final String color;

	private IconColor(String color) {
		this.color = color;
	}

	public String toString() {
		return color;
	}

}
