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
 * <p>
 * For further icons or an example of the existing ones;
 * http://fontawesome.io/icons/
 */
public enum Icon {

	NONE(null),

	// Navigation
	NEXT("fa-angle-right"),
	PREVIOUS("fa-angle-left"),
	LAST("fa-angle-double-right"),
	FIRST("fa-angle-double-left"),

	//ARROWS
	RIGHT("fa-arrow-right"),
	LEFT("fa-arrow-left"),

	// Sorting
	ASCENDING("fa-sort-asc"),
	DESCENDING("fa-sort-desc"),
	FILTER("fa-filter"),

	// BASICS
	EDIT("fa-pencil"),
	HELP("fa-question-circle"),
	INFO("fa-info-circle"),
	ERROR("fa-exclamation-triangle knowwe-error"),
	WARNING("fa-exclamation-triangle knowwe-warning"),
	SEARCH("fa-search"),
	DELETE("fa-times-circle"),
	DOWNLOAD("fa-download"),
	UPLOAD("fa-upoload"),
	IMPORT("fa-upload"),
	REFRESH("fa-refresh"),
	QRCODE("fa-qrcode"),
	LINK("fa-link"),
	CLIPBOARD("fa-clipboard"),
	CLOCK("fa-clock-o"),
	SHARE("fa-share"),
	LIGHTBULB("fa-lightbulb"),
	SHOW("fa-plus-square-o"),
	HIDE("fa-minus-square-o"),
	COG("fa-cog"),
	PREFERENCES("fa-cog"),
	LIST("fa-list-alt"),
	COMMENT("fa-comment-o"),
	STATISTICS("fa-line-chart"),
	MINUS("fa-minus"),
	PLUS("fa-minus"),
	ADD("fa-plus-circle"),
	BULB("fa-circle"),

	// KnowWE specific
	ARTICLE("fa-file-text-o"),
	KNOWLEDGEBASE("fa-book"),
	VISUALEDITOR("fa-eye"),
	//temporary?
	SHOWTRACE("fa-code-fork"),
	//temporary
	RENAME("fa-font"),
	RUN("fa-play-circle"),
	//temporary
	PACKAGE("fa-puzzle-piece"),
	EDITSECTION("fa-pencil-square-o"),
	OPENPAGE("fa-pencil-square-o"),
	DEBUG("fa-bug"),
	CONSISTENCY("fa-crosshairs"),
	CHECK("fa-check-square-o"),
	CHECKED("fa-check"),
	DEFER("fa-share"),

	LOW_PRIO("fa-exclamation-circle knowwe-lowprio"),
	HIGH_PRIO("fa-exclamation-circle knowwe-highprio"),

	// TestCase
	EXPAND_OUTLINE("fa-plus-square-o"),
	COLLAPSE_OUTLINE("fa-minus-square-o"),
	EXPAND("fa-plus-square"),
	COLLAPSE("fa-minus-square"),
	OPENTESTCASE(LINK.cssClass),
	EDITTABLE("fa-table"),

	// ON and OFF
	TOGGLE_ON("fa-toggle-on"),
	TOGGLE_OFF("fa-toggle-off"),

	// FILE TYPES
	NEW_FILE("fa-file-o"),
	FILE("fa-file"),
	FILE_TEXT("fa-file-text-o"),
	FILE_EXCEL("fa-file-excel-o"),
	FILE_WORD("fa-file-word-o"),
	FILE_PDF("fa-file-pdf-o"),
	FILE_ZIP("fa-file-zip-o"),
	FILE_CODE("fa-file-code-o"),
	FILE_XML(FILE_CODE.getCssClass());

	private final String cssClass;

	private Icon(String cssClass) {
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

	public String getIconWithAdditionalClasses(String... cssClasses) {
		String classes = Arrays.asList(cssClasses).stream().collect(Collectors.joining(" "));
		return "<i class='fa " + getCssClass() + " " + classes + "'></i>";
	}

	public String getIconClassWithAdditionalClasses(String cssClasses) {
		return "<i class='fa " + getCssClass() + " " + cssClasses + "'></i>";
	}

	/**
	 * @return a HTML tag for that icon
	 */
	public String getIconWithFixedWidth() {
		return "<i class='fa " + cssClass + " fa-fw'></i>";
	}

	/**
	 * @param percent A percentage increase of 33, 100, 200, 300, 400 is possible
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
	 * @param percent A percentage increase of 33, 100, 200, 300, 400 is possible
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

	public String getToolMenuItem(String description) {
		return "<i class='fa " + cssClass + " fa-fw'></i>&nbsp;" + description;
	}

}
