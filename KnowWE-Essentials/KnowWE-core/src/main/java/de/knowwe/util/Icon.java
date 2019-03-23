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
	public static final Icon NEXT = new Icon("fas fa-angle-right");
	public static final Icon PREVIOUS = new Icon("fas fa-angle-left");
	public static final Icon LAST = new Icon("fas fa-angle-double-right");
	public static final Icon FIRST = new Icon("fas fa-angle-double-left");

	//ARROWS
	public static final Icon RIGHT = new Icon("fas fa-arrow-right");
	public static final Icon LEFT = new Icon("fas fa-arrow-left");

	// Sorting
	public static final Icon ASCENDING = new Icon("fas fa-sort-up");
	public static final Icon DESCENDING = new Icon("fas fa-sort-down");
	public static final Icon FILTER = new Icon("fas fa-filter");

	// BASICS
	public static final Icon EDIT = new Icon("fas fa-pencil-alt");
	public static final Icon HELP = new Icon("fas fa-question-circle");
	public static final Icon INFO = new Icon("fas fa-info-circle");
	public static final Icon ERROR = new Icon("fas fa-exclamation-triangle knowwe-error");
	public static final Icon WARNING = new Icon("fas fa-exclamation-triangle knowwe-warning");
	public static final Icon SEARCH = new Icon("fas fa-search");
	public static final Icon DELETE = new Icon("fas fa-times-circle");
	public static final Icon DOWNLOAD = new Icon("fas fa-download");
	public static final Icon UPLOAD = new Icon("fas fa-upload");
	public static final Icon ATTACHMENT = new Icon("fas fa-paperclip");
	public static final Icon IMPORT = new Icon("fas fa-upload");
	public static final Icon REFRESH = new Icon("fas fa-sync");
	public static final Icon QRCODE = new Icon("fas fa-qrcode");
	public static final Icon LINK = new Icon("fas fa-link");
	public static final Icon EXTERNAL_LINK = new Icon("fas fa-external-link-square-alt");
	public static final Icon CLIPBOARD = new Icon("fas fa-clipboard");
	public static final Icon CLOCK = new Icon("fas fa-clock");
	public static final Icon SHARE = new Icon("fas fa-share");
	public static final Icon LIGHTBULB = new Icon("fas fa-lightbulb");
	public static final Icon SHOW = new Icon("fas fa-plus-square");
	public static final Icon HIDE = new Icon("fas fa-minus-square");
	public static final Icon COG = new Icon("fas fa-cog");
	public static final Icon PREFERENCES = new Icon("fas fa-cog");
	public static final Icon LIST = new Icon("fas fa-list-alt");
	public static final Icon COMMENT = new Icon("fas fa-comment");
	public static final Icon STATISTICS = new Icon("fas fa-chart-line");
	public static final Icon MINUS = new Icon("fas fa-minus");
	public static final Icon PLUS = new Icon("fas fa-plus");
	public static final Icon ADD = new Icon("fas fa-plus-circle");
	public static final Icon BULB = new Icon("fas fa-circle");
	public static final Icon STOP = new Icon("fas fa-stop");
	public static final Icon USER = new Icon("fas fa-user");
	public static final Icon CALENDAR = new Icon("fas fa-calendar-alt");
	public static final Icon GLOBE = new Icon("fas fa-globe");
	public static final Icon ORDERED_LIST = new Icon("fas fa-list-ol");
	public static final Icon COPY_TO_CLIPBOARD = new Icon("fas fa-clipboard");

	// KnowWE specific
	public static final Icon ARTICLE = new Icon("fas fa-file-alt");
	public static final Icon KNOWLEDGEBASE = new Icon("fas fa-book");
	public static final Icon VISUALEDITOR = new Icon("fas fa-eye");
	//temporary?
	public static final Icon SHOWTRACE = new Icon("fas fa-code-branch");
	//temporary
	public static final Icon RENAME = new Icon("fas fa-font");
	public static final Icon RUN = new Icon("fas fa-play");
	//temporary
	public static final Icon PACKAGE = new Icon("fas fa-puzzle-piece");
	public static final Icon EDITSECTION = new Icon("fas fa-edit");
	public static final Icon OPENPAGE = new Icon("fas fa-edit");
	public static final Icon DEBUG = new Icon("fas fa-bug");
	public static final Icon CONSISTENCY = new Icon("fas fa-crosshairs");
	public static final Icon CHECK = new Icon("fas fa-check-square");
	public static final Icon CHECKED = new Icon("fas fa-check");
	public static final Icon DEFER = new Icon("fas fa-share");
	public static final Icon ESTABLISHED = new Icon("fas fa-circle");
	public static final Icon SUGGESTED = new Icon("fas fa-circle");
	public static final Icon EXCLUDED = new Icon("fas fa-ban");
	public static final Icon ABSTRACT = new Icon("fas fa-asterisk");
	public static final Icon PLUG = new Icon("fas fa-plug");

	public static final Icon LOW_PRIO = new Icon("fas fa-exclamation-circle knowwe-lowprio");
	public static final Icon HIGH_PRIO = new Icon("fas fa-exclamation-circle knowwe-highprio");

	// TestCase
	public static final Icon EXPAND_OUTLINE = new Icon("fas fa-plus-square");
	public static final Icon COLLAPSE_OUTLINE = new Icon("fas fa-minus-square");
	public static final Icon EXPAND = new Icon("fas fa-plus-square");
	public static final Icon COLLAPSE = new Icon("fas fa-minus-square");
	public static final Icon OPENTESTCASE = new Icon(LINK.getCssClass());
	public static final Icon EDITTABLE = new Icon("fas fa-table");

	// ON and OFF
	public static final Icon TOGGLE_ON = new Icon("fas fa-toggle-on");
	public static final Icon TOGGLE_OFF = new Icon("fas fa-toggle-off");

	// FILE TYPES
	public static final Icon NEW_FILE = new Icon("fas fa-file");
	public static final Icon FILE = new Icon("fas fa-file");
	public static final Icon FILE_TEXT = new Icon("fas fa-file-alt");
	public static final Icon FILE_EXCEL = new Icon("fas fa-file-excel");
	public static final Icon FILE_WORD = new Icon("fas fa-file-word");
	public static final Icon FILE_PDF = new Icon("fas fa-file-pdf");
	public static final Icon FILE_ZIP = new Icon("fas fa-file-archive");
	public static final Icon FILE_CODE = new Icon("fas fa-file-code");
	public static final Icon FILE_XML = new Icon(FILE_CODE.getCssClass());
	public static final Icon FILE_PLUS = new Icon("fas fa-file-plus");
	public static final Icon FOLDER_PLUS = new Icon("fas fa-folder-plus");

	//PACKAGES
	public static final Icon LAYER_PLUS = new Icon("fas fa-layer-plus");
	public static final Icon CART_PLUS = new Icon("fas fa-cart-plus");
	public static final Icon OBJECT_GROUP = new Icon("fas fa-object-group");
	public static final Icon PLUS_CIRCLE = new Icon("fas fa-plus-circle");
	public static final Icon OBJECT_UNGROUP = new Icon("fas fa-object-ungroup");
	public static final Icon PLUS_OCTAGON = new Icon("fas fa-plus-octagon");

	//SPINNING
	public static final Icon LOADING = new Icon("fas fa-spin fa-refresh");
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
		if (fontAweSomeProAvailable()) {
			cssClass = cssClass.replace("fas ", "far ");
		}
		this.cssClass = cssClass;
		this.style = style;
		this.title = title;
		this.id = id;
	}

	private boolean fontAweSomeProAvailable() {
		return false; // TODO
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
		return "<i class='" + cssClass + "' "
				+ (style == null ? "" : "style='" + style + "'")
				+ (title == null ? "" : "title='" + title + "'")
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
