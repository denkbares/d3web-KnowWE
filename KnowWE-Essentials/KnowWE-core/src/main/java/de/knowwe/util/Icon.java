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
	public static final Icon NEXT = new Icon("far fa-angle-right");
	public static final Icon PREVIOUS = new Icon("far fa-angle-left");
	public static final Icon LAST = new Icon("far fa-angle-double-right");
	public static final Icon FIRST = new Icon("far fa-angle-double-left");

	//ARROWS
	public static final Icon RIGHT = new Icon("far fa-arrow-right");
	public static final Icon LEFT = new Icon("far fa-arrow-left");

	// Sorting
	public static final Icon ASCENDING = new Icon("far fa-sort-up");
	public static final Icon DESCENDING = new Icon("far fa-sort-down");
	public static final Icon FILTER = new Icon("far fa-filter");

	// BASICS
	public static final Icon EDIT = new Icon("far fa-pencil-alt");
	public static final Icon HELP = new Icon("far fa-question-circle");
	public static final Icon INFO = new Icon("far fa-info-circle");
	public static final Icon ERROR = new Icon("far fa-exclamation-triangle knowwe-error");
	public static final Icon WARNING = new Icon("far fa-exclamation-triangle knowwe-warning");
	public static final Icon SEARCH = new Icon("far fa-search");
	public static final Icon DELETE = new Icon("far fa-times-circle");
	public static final Icon DOWNLOAD = new Icon("far fa-download");
	public static final Icon UPLOAD = new Icon("far fa-upload");
	public static final Icon ATTACHMENT = new Icon("far fa-paperclip");
	public static final Icon IMPORT = new Icon("far fa-upload");
	public static final Icon REFRESH = new Icon("far fa-sync");
	public static final Icon QRCODE = new Icon("far fa-qrcode");
	public static final Icon LINK = new Icon("far fa-link");
	public static final Icon EXTERNAL_LINK = new Icon("far fa-external-link-square-alt");
	public static final Icon CLIPBOARD = new Icon("far fa-clipboard");
	public static final Icon CLOCK = new Icon("far fa-clock");
	public static final Icon SHARE = new Icon("far fa-share");
	public static final Icon LIGHTBULB = new Icon("far fa-lightbulb");
	public static final Icon SHOW = new Icon("far fa-plus-square");
	public static final Icon HIDE = new Icon("far fa-minus-square");
	public static final Icon COG = new Icon("far fa-cog");
	public static final Icon PREFERENCES = new Icon("far fa-cog");
	public static final Icon LIST = new Icon("far fa-list-alt");
	public static final Icon COMMENT = new Icon("far fa-comment");
	public static final Icon STATISTICS = new Icon("far fa-chart-line");
	public static final Icon MINUS = new Icon("far fa-minus");
	public static final Icon PLUS = new Icon("far fa-plus");
	public static final Icon ADD = new Icon("far fa-plus-circle");
	public static final Icon BULB = new Icon("far fa-circle");
	public static final Icon STOP = new Icon("far fa-stop");
	public static final Icon USER = new Icon("far fa-user");
	public static final Icon CALENDAR = new Icon("far fa-calendar-alt");
	public static final Icon GLOBE = new Icon("far fa-globe");
	public static final Icon ORDERED_LIST = new Icon("far fa-list-ol");
	public static final Icon COPY_TO_CLIPBOARD = new Icon("far fa-clipboard");

	// KnowWE specific
	public static final Icon ARTICLE = new Icon("far fa-file-alt");
	public static final Icon KNOWLEDGEBASE = new Icon("far fa-book");
	public static final Icon VISUALEDITOR = new Icon("far fa-eye");
	//temporary?
	public static final Icon SHOWTRACE = new Icon("far fa-code-branch");
	//temporary
	public static final Icon RENAME = new Icon("far fa-font");
	public static final Icon RUN = new Icon("far fa-play");
	//temporary
	public static final Icon PACKAGE = new Icon("far fa-puzzle-piece");
	public static final Icon EDITSECTION = new Icon("far fa-edit");
	public static final Icon OPENPAGE = new Icon("far fa-edit");
	public static final Icon DEBUG = new Icon("far fa-bug");
	public static final Icon CONSISTENCY = new Icon("far fa-crosshairs");
	public static final Icon CHECK = new Icon("far fa-check-square");
	public static final Icon CHECKED = new Icon("far fa-check");
	public static final Icon DEFER = new Icon("far fa-share");
	public static final Icon ESTABLISHED = new Icon("far fa-circle");
	public static final Icon SUGGESTED = new Icon("far fa-circle");
	public static final Icon EXCLUDED = new Icon("far fa-ban");
	public static final Icon ABSTRACT = new Icon("far fa-asterisk");
	public static final Icon PLUG = new Icon("far fa-plug");

	public static final Icon LOW_PRIO = new Icon("far fa-exclamation-circle knowwe-lowprio");
	public static final Icon HIGH_PRIO = new Icon("far fa-exclamation-circle knowwe-highprio");

	// TestCase
	public static final Icon EXPAND_OUTLINE = new Icon("far fa-plus-square");
	public static final Icon COLLAPSE_OUTLINE = new Icon("far fa-minus-square");
	public static final Icon EXPAND = new Icon("far fa-plus-square");
	public static final Icon COLLAPSE = new Icon("far fa-minus-square");
	public static final Icon OPENTESTCASE = new Icon(LINK.getCssClass());
	public static final Icon EDITTABLE = new Icon("far fa-table");

	// ON and OFF
	public static final Icon TOGGLE_ON = new Icon("far fa-toggle-on");
	public static final Icon TOGGLE_OFF = new Icon("far fa-toggle-off");

	// FILE TYPES
	public static final Icon NEW_FILE = new Icon("far fa-file");
	public static final Icon FILE = new Icon("far fa-file");
	public static final Icon FILE_TEXT = new Icon("far fa-file-alt");
	public static final Icon FILE_EXCEL = new Icon("far fa-file-excel");
	public static final Icon FILE_WORD = new Icon("far fa-file-word");
	public static final Icon FILE_PDF = new Icon("far fa-file-pdf");
	public static final Icon FILE_ZIP = new Icon("far fa-file-archive");
	public static final Icon FILE_CODE = new Icon("far fa-file-code");
	public static final Icon FILE_XML = new Icon(FILE_CODE.getCssClass());
	public static final Icon FILE_PLUS = new Icon("far fa-file-plus");
	public static final Icon FOLDER_PLUS = new Icon("far fa-folder-plus");

	//PACKAGES
	public static final Icon LAYER_PLUS = new Icon("far fa-layer-plus");
	public static final Icon CART_PLUS = new Icon("far fa-cart-plus");
	public static final Icon OBJECT_GROUP = new Icon("far fa-object-group");
	public static final Icon PLUS_CIRCLE = new Icon("far fa-plus-circle");
	public static final Icon OBJECT_UNGROUP = new Icon("far fa-object-ungroup");
	public static final Icon PLUS_OCTAGON = new Icon("far fa-plus-octagon");

	//SPINNING
	public static final Icon LOADING = new Icon("far fa-spin fa-refresh");
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
		this.cssClass = cssClass;
		this.style = style;
		this.title = title;
		this.id = id;
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
