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

import static de.knowwe.util.Color.ColorValues.*;
import static de.knowwe.util.Color.CssNames.*;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 12.01.15.
 */
public enum Color {
	NONE(null, null), GREEN(KNOWWE_GREEN, ColorValues.KNOWWE_GREEN_VALUE), RED(KNOWWE_RED, ColorValues.KNOWWE_RED_VALUE),
	YELLOW(KNOWWE_YELLOW, KNOWWE_YELLOW_VALUE), BLUE(KNOWWE_BLUE, KNOWWE_BLUE_VALUE), GRAY(KNOWWE_GRAY, KNOWWE_GRAY_VALUE),
	OK(KNOWWE_GREEN, KNOWWE_GREEN_VALUE), ERROR(KNOWWE_RED, KNOWWE_RED_VALUE), WARNING(KNOWWE_YELLOW, KNOWWE_YELLOW_VALUE), DISABLED(KNOWWE_GRAY, KNOWWE_GRAY_VALUE);

	private String cssClassName;
	private String colorValue;

	Color(String cssClassName, String colorValue) {
		this.cssClassName = cssClassName;
		this.colorValue = colorValue;
	}

	public String getCssClassName() {
		return cssClassName;
	}

	public String getColorValue() {
		return colorValue;
	}

	public static class CssNames {
		public static final String KNOWWE_GREEN = "knowwe-green";
		public static final String KNOWWE_RED = "knowwe-red";
		public static final String KNOWWE_YELLOW = "knowwe-yellow";
		public static final String KNOWWE_BLUE = "knowwe-blue";
		public static final String KNOWWE_GRAY = "knowwe-gray";
	}

	public static class ColorValues {
		public static final String KNOWWE_GREEN_VALUE = "#6A9514";
		public static final String KNOWWE_RED_VALUE = "#CC3333";
		public static final String KNOWWE_YELLOW_VALUE = "#ffd518";
		public static final String KNOWWE_BLUE_VALUE = "#036";
		public static final String KNOWWE_GRAY_VALUE = "#999";
	}
}
