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

import com.denkbares.strings.Strings;

/**
 * Created by Stefan Plehn, Albrecht Striffler (denkbares GmbH) on 21.11.14.
 * <p>
 * For further icons or an example of the existing ones; http://fontawesome.io/icons/
 */
public class Icon {

	public static final Icon NONE = new Icon(null);

	// Navigation
	public static final Icon NEXT = new Icon("fa-type fa-angle-right");
	public static final Icon PREVIOUS = new Icon("fa-type fa-angle-left");
	public static final Icon LAST = new Icon("fa-type fa-angles-right");
	public static final Icon FIRST = new Icon("fa-type fa-angles-left");

	//ARROWS
	public static final Icon RIGHT = new Icon("fa-type fa-arrow-right");
	public static final Icon LEFT = new Icon("fa-type fa-arrow-left");

	// Sorting
	public static final Icon ASCENDING = new Icon("fa-type fa-sort-up");
	public static final Icon DESCENDING = new Icon("fa-type fa-sort-down");
	public static final Icon FILTER = new Icon("fa-type fa-filter");

	// BASICS
	public static final Icon PIN = new Icon("fa-type fa-map-pin");
	public static final Icon EDIT = new Icon("fa-type fa-pencil");
	public static final Icon HELP = new Icon("fa-type fa-circle-question");
	public static final Icon INFO = new Icon("fa-type fa-circle-info");
	public static final Icon ERROR = new Icon("fa-type fa-triangle-exclamation knowwe-error");
	public static final Icon WARNING = new Icon("fa-type fa-triangle-exclamation knowwe-warning");
	public static final Icon SEARCH = new Icon("fa-type fa-magnifying-glass");
	public static final Icon DELETE = new Icon("fa-type fa-circle-xmark");
	public static final Icon DOWNLOAD = new Icon("fa-type fa-download");
	public static final Icon UPLOAD = new Icon("fa-type fa-upload");
	public static final Icon ATTACHMENT = new Icon("fa-type fa-paperclip");
	public static final Icon IMPORT = new Icon("fa-type fa-upload");
	public static final Icon REFRESH = new Icon("fa-type fa-rotate");
	public static final Icon QRCODE = new Icon("fa-type fa-qrcode");
	public static final Icon LINK = new Icon("fa-type fa-link");
	public static final Icon EXTERNAL_LINK = new Icon("fa-type fa-up-right-from-square");
	public static final Icon CLIPBOARD = new Icon("fa-type fa-clipboard");
	public static final Icon CLOCK = new Icon("fa-type fa-clock");
	public static final Icon SHARE = new Icon("fa-type fa-share");
	public static final Icon LIGHTBULB = new Icon("fa-type fa-lightbulb");
	public static final Icon SHOW = new Icon("fa-type fa-square-plus");
	public static final Icon HIDE = new Icon("fa-type fa-square-minus");
	public static final Icon COG = new Icon("fa-type fa-gear");
	public static final Icon PREFERENCES = new Icon("fa-type fa-gear");
	public static final Icon LIST = new Icon("fa-type fa-rectangle-list");
	public static final Icon COMMENT = new Icon("fa-type fa-comment");
	public static final Icon STATISTICS = new Icon("fa-type fa-chart-line");
	public static final Icon MINUS = new Icon("fa-type fa-minus");
	public static final Icon PLUS = new Icon("fa-type fa-plus");
	public static final Icon ADD = new Icon("fa-type fa-circle-plus");
	public static final Icon BULB = new Icon("fa-solid fa-circle");
	public static final Icon STOP = new Icon("fa-solid fa-stop");
	public static final Icon USER = new Icon("fa-type fa-user");
	public static final Icon CALENDAR = new Icon("fa-type fa-calendar-days");
	public static final Icon GLOBE = new Icon("fa-type fa-globe");
	public static final Icon ORDERED_LIST = new Icon("fa-type fa-list-ol");
	public static final Icon COPY_TO_CLIPBOARD = new Icon("fa-type fa-clipboard");
	public static final Icon CIRCLE_FULL = new Icon("fa-solid fa-circle");
	public static final Icon CIRCLE_HALF_FULL = new Icon("fa-solid fa-circle-half-stroke");
	public static final Icon CIRCLE_EMPTY = new Icon("fa-type fa-circle");
	public static final Icon TAG = new Icon("fa-solid fa-tag");
	public static final Icon REPEAT = new Icon("fa-solid fa-repeat");
	public static final Icon COMPILER = new Icon("fa-type fa-microchip");
	public static final Icon HISTORY = new Icon("fa-type fa-clock-rotate-left");
	public static final Icon VERSION = new Icon("fa-solid fa-hashtag");

	// KnowWE specific
	public static final Icon ARTICLE = new Icon("fa-type fa-file-lines");
	public static final Icon KNOWLEDGEBASE = new Icon("fa-type fa-book");
	public static final Icon VISUALEDITOR = new Icon("fa-type fa-eye");
	//temporary?
	public static final Icon SHOWTRACE = new Icon("fa-type fa-code-branch");
	//temporary
	public static final Icon RENAME = new Icon("fa-type fa-font");
	public static final Icon RUN = new Icon("fa-solid fa-play");
	public static final Icon SELECT_TARGET = new Icon("fa-type fa-bullseye-pointer");
	public static final Icon UNSELECT_TARGET = new Icon("fa-type fa-eraser");
	public static final Icon CLEAR_UUT_STATE = new Icon("fa-type fa-eraser");
	//temporary
	public static final Icon PACKAGE = new Icon("fa-type fa-puzzle-piece");
	public static final Icon EDITSECTION = new Icon("fa-type fa-pen-to-square");
	public static final Icon OPENPAGE = new Icon("fa-type fa-pen-to-square");
	public static final Icon DEBUG = new Icon("fa-type fa-bug");
	public static final Icon CONSISTENCY = new Icon("fa-type fa-crosshairs");
	public static final Icon CHECK = new Icon("fa-type fa-square-check");
	public static final Icon CHECK2 = new Icon("fa-type fa-circle-check");
	public static final Icon CHECKED = new Icon("fa-type fa-check");
	public static final Icon DEFER = new Icon("fa-type fa-share");
	public static final Icon ESTABLISHED = new Icon("fa-solid fa-circle");
	public static final Icon SUGGESTED = new Icon("fa-solid fa-circle");
	public static final Icon EXCLUDED = new Icon("fa-type fa-ban");
	public static final Icon ABSTRACT = new Icon("fa-type fa-asterisk");
	public static final Icon PLUG = new Icon("fa-type fa-plug");
	public static final Icon MEASUREMENT_DEVICE = new Icon("fa-type fa-weight-scale");
	public static final Icon VISUAL_INSPECTION = new Icon("fa-type fa-eye");
	public static final Icon SWITCHES = new Icon("fa-type fa-sliders");
	public static final Icon GRIP_LINES_VERTICAL = new Icon("fa-type fa-grip-lines-vertical");
	public static final Icon INTERNAL_UNIT_FAILURE = new Icon("fa-type fa-square-xmark");
	public static final Icon TACHOMETER = new Icon("fa-type fa-gauge-high");
	public static final Icon LOCK = new Icon("fa-type fa-lock");

	public static final Icon LOW_PRIO = new Icon("fa-type fa-circle-exclamation knowwe-lowprio");
	public static final Icon HIGH_PRIO = new Icon("fa-type fa-circle-exclamation knowwe-highprio");

	// TestCase
	public static final Icon EXPAND_OUTLINE = new Icon("fa-type fa-square-plus");
	public static final Icon COLLAPSE_OUTLINE = new Icon("fa-type fa-square-minus");
	public static final Icon EXPAND = new Icon("fa-type fa-square-plus");
	public static final Icon COLLAPSE = new Icon("fa-type fa-square-minus");
	public static final Icon OPENTESTCASE = new Icon(LINK.getCssClass());
	public static final Icon EDITTABLE = new Icon("fa-type fa-table");

	// ON and OFF
	public static final Icon TOGGLE_ON = new Icon("fa-type fa-toggle-on");
	public static final Icon TOGGLE_OFF = new Icon("fa-type fa-toggle-off");

	// FILE TYPES
	public static final Icon NEW_FILE = new Icon("fa-type fa-file");
	public static final Icon FILE = new Icon("fa-type fa-file");
	public static final Icon FILE_TEXT = new Icon("fa-type fa-file-lines");
	public static final Icon FILE_EXCEL = new Icon("fa-type fa-file-excel");
	public static final Icon FILE_WORD = new Icon("fa-type fa-file-word");
	public static final Icon FILE_PDF = new Icon("fa-type fa-file-pdf");
	public static final Icon FILE_ZIP = new Icon("fa-type fa-file-zipper");
	public static final Icon FILE_CODE = new Icon("fa-type fa-file-code");
	public static final Icon FILE_XML = new Icon(FILE_CODE.getCssClass());
	public static final Icon FILE_PLUS = new Icon("fa-type fa-file-plus");
	public static final Icon FOLDER_PLUS = new Icon("fa-type fa-folder-plus");

	//PACKAGES
	public static final Icon LAYER_PLUS = new Icon("fa-type fa-layer-plus");
	public static final Icon CART_PLUS = new Icon("fa-type fa-cart-plus");
	public static final Icon OBJECT_GROUP = new Icon("fa-type fa-object-group");
	public static final Icon PLUS_CIRCLE = new Icon("fa-type fa-circle-plus");
	public static final Icon OBJECT_UNGROUP = new Icon("fa-type fa-object-ungroup");
	public static final Icon PLUS_OCTAGON = new Icon("fa-type fa-octagon-plus");
	public static final Icon MINUS_OCTAGON = new Icon("fa-type fa-octagon-minus");

	//SPINNING
	public static final Icon LOADING = new Icon("fa-type fa-spin fa-circle-notch");
	public static final Icon WAITING = new Icon(LOADING.getCssClass());
	public static final Icon CALCULATING = new Icon(LOADING.getCssClass());

	public enum Percent {
		by33,
		by100,
		by200,
		by300,
		by400
	}

	private final String cssClass;

	private final String style;

	private final String title;

	private final String id;

	private Icon(String cssClass) {
		this(cssClass, null, null, null);
	}

	private Icon(String cssClass, String style, String title, String id) {
		if (cssClass != null) {
			if (isFontAweSomeProAvailable()) {
				cssClass = cssClass.replace("fa-type ", "fa-regular ");
			}
			else {
				cssClass = cssClass.replace("fa-type ", "fa-solid ");
			}
		}
		this.cssClass = cssClass;
		this.style = style;
		this.title = title;
		this.id = id;
	}

	private boolean isFontAweSomeProAvailable() {
		// maybe make this nicer?
		try {
			Class.forName("com.denkbares.knowwe.fontawesome.FontAwesomeLoader");
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static Icon fromImage(String image) {
		// we require some icon to get a valid width, color will be set transparent to appear empty
		return new Icon("fa-pencil image-icon", "color: transparent;" +
				"display: inline-block;" +
				"background-image:url(" + image + ");" +
				"background-repeat: no-repeat;" +
				"background-size: contain;" +
				"background-position: 50%",
				null, null);
	}

	public String toString() {
		return cssClass;
	}

	public String getCssClass() {
		return cssClass;
	}

	/**
	 * Returns the HTML tag for the current icon.
	 */
	public String toHtml() {
		String cssClass = this.cssClass;
		if (title != null) {
			if (Strings.isBlank(cssClass)) {
				cssClass = "tooltipster";
			}
			else {
				cssClass += " tooltipster";
			}
		}
		return "<i class='" + cssClass + "' "
				+ (style == null ? "" : "style='" + style + "'")
				+ (title == null ? "" : "title='" + Strings.encodeHtml(title) + "'")
				+ (id == null ? "" : "id='" + id + "'")
				+ "></i>";
	}

	public Icon addStyle(String style) {
		return new Icon(cssClass, style, title, id);
	}

	public Icon addClasses(String... classes) {
		return new Icon(cssClass + " " + Strings.concat(" ", classes), style, title, id);
	}

	public Icon addColor(Color color) {
		return new Icon(cssClass + " " + color.getCssClassName(), style, title, id);
	}

	public Icon addTitle(String title) {
		return new Icon(cssClass, style, title, id);
	}

	public Icon addId(String id) {
		return new Icon(cssClass, style, title, id);
	}

	public Icon fixWidth() {
		return addClasses("fa-fw");
	}

	public Icon increaseSize(Percent percent) {
		switch (percent) {
			case by33:
				return addClasses("fa-lg");
			case by100:
				return addClasses("fa-2x");
			case by200:
				return addClasses("fa-3x");
			case by300:
				return addClasses("fa-4x");
			case by400:
				return addClasses("fa-5x");
		}
		return this;
	}
}
