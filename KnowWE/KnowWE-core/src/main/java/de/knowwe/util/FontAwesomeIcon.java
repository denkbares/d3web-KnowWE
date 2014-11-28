/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 21.11.14.
 */
public enum FontAwesomeIcon {

	// Navigation
	NEXT("fa-angle-right"),
	PREVIOUS("fa-angle-left"),
	LAST("fa-angle-double-right"),
	FIRST("fa-angle-double-left"),

	// Sorting
	ASCENDING("fa-caret-down"),
	DESCENDING("fa-caret-up"),
	FILTER("fa-filter"),

	// BASICS
	EDIT("fa-edit"),
	HELP("fa-question-circle"),
	DOWNLOAD("fa-download"),
	REFRESH("fa-refresh"),

	// KnowWE specific
	KNOWLEDGEBASE("fa-book"),

	// ON and OFF
	TOGGLE_ON("fa-toggle-on"),
	TOGGLE_OFF("fa-toggle-off"),

	// FILE TYPES
	FILE("fa-file"),
	FILE_EXCEL("fa-file-excel-o"),
	FILE_WORD("fa-file-word-o"),
	FILE_PDF("fa-file-pdf-o");

	private final String cssClass;

	private FontAwesomeIcon(String cssClass) {
		this.cssClass = cssClass;
	}

	public String toString() {
		return cssClass;
	}

	public String getCssClass() {
		return cssClass;
	}

	/**
	 * @return a HTML tag for that icon
	 */
	public String getIcon() {
		return "<i class='fa " + cssClass + "'></i>";
	}

	/**
	 * @param percent The percentage increase of the icon 33, 100, 200, 300, 400 possible
	 * @return a HTML tag for an enlarged version of that icon
	 */
	public String increaseSize(int percent) {
		return "<i class='fa " + getIncreasedSizeIconCssClass(percent) + "'></i>";
	}

	public String increaseSize(int percent, String... cssClasses) {
		String classes = Arrays.asList(cssClasses).stream().collect(Collectors.joining(" "));
		return "<i class='" + getIncreasedSizeIconCssClass(percent) + " " + classes + "'></i>";
	}

	/**
	 * @param percent The percentage increase of the icon. The values 33, 100, 200, 300, 400 are possible
	 * @return a string for an enlarged version of that icon usable in an HTML class attribute
	 */
	public String getIncreasedSizeIconCssClass(int percent) {
		return "fa " + cssClass + " " + getEnlargedCssClass(percent);
	}

	private String getEnlargedCssClass(int percent) {
		switch (percent) {
			case 33:
				return "fa-lg";

			case 100:
				return "fa-2x";

			case 200:
				return "fa-3x";

			case 300:
				return "fa-4x";

			case 400:
				return "fa-5x";

			default:
				return "";
		}
	}

}
