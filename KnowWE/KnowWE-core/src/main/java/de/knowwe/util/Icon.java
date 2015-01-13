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

import de.d3web.strings.Strings;

/**
 * Created by Stefan Plehn, Albrecht Striffler (denkbares GmbH) on 21.11.14.
 * <p>
 * For further icons or an example of the existing ones;
 * http://fontawesome.io/icons/
 */
public class Icon {

	public static final Icon NONE = new Icon(null);

	// Navigation
	public static final Icon NEXT = new Icon("fa-angle-right");
	public static final Icon PREVIOUS = new Icon("fa-angle-left");
	public static final Icon LAST = new Icon("fa-angle-double-right");
	public static final Icon FIRST = new Icon("fa-angle-double-left");

	//ARROWS
	public static final Icon RIGHT = new Icon("fa-arrow-right");
	public static final Icon LEFT = new Icon("fa-arrow-left");

	// Sorting
	public static final Icon ASCENDING = new Icon("fa-sort-asc");
	public static final Icon DESCENDING = new Icon("fa-sort-desc");
	public static final Icon FILTER = new Icon("fa-filter");

	// BASICS
	public static final Icon EDIT = new Icon("fa-pencil");
	public static final Icon HELP = new Icon("fa-question-circle");
	public static final Icon INFO = new Icon("fa-info-circle");
	public static final Icon ERROR = new Icon("fa-exclamation-triangle knowwe-error");
	public static final Icon WARNING = new Icon("fa-exclamation-triangle knowwe-warning");
	public static final Icon SEARCH = new Icon("fa-search");
	public static final Icon DELETE = new Icon("fa-times-circle");
	public static final Icon DOWNLOAD = new Icon("fa-download");
	public static final Icon UPLOAD = new Icon("fa-upoload");
	public static final Icon IMPORT = new Icon("fa-upload");
	public static final Icon REFRESH = new Icon("fa-refresh");
	public static final Icon QRCODE = new Icon("fa-qrcode");
	public static final Icon LINK = new Icon("fa-link");
	public static final Icon CLIPBOARD = new Icon("fa-clipboard");
	public static final Icon CLOCK = new Icon("fa-clock-o");
	public static final Icon SHARE = new Icon("fa-share");
	public static final Icon LIGHTBULB = new Icon("fa-lightbulb");
	public static final Icon SHOW = new Icon("fa-plus-square-o");
	public static final Icon HIDE = new Icon("fa-minus-square-o");
	public static final Icon COG = new Icon("fa-cog");
	public static final Icon PREFERENCES = new Icon("fa-cog");
	public static final Icon LIST = new Icon("fa-list-alt");
	public static final Icon COMMENT = new Icon("fa-comment-o");
	public static final Icon STATISTICS = new Icon("fa-line-chart");
	public static final Icon MINUS = new Icon("fa-minus");
	public static final Icon PLUS = new Icon("fa-minus");
	public static final Icon ADD = new Icon("fa-plus-circle");
	public static final Icon BULB = new Icon("fa-circle");
	public static final Icon STOP = new Icon("fa-stop");
	public static final Icon USER = new Icon("fa-user");

	// KnowWE specific
	public static final Icon ARTICLE = new Icon("fa-file-text-o");
	public static final Icon KNOWLEDGEBASE = new Icon("fa-book");
	public static final Icon VISUALEDITOR = new Icon("fa-eye");
	//temporary?
	public static final Icon SHOWTRACE = new Icon("fa-code-fork");
	//temporary
	public static final Icon RENAME = new Icon("fa-font");
	public static final Icon RUN = new Icon("fa-play");
	//temporary
	public static final Icon PACKAGE = new Icon("fa-puzzle-piece");
	public static final Icon EDITSECTION = new Icon("fa-pencil-square-o");
	public static final Icon OPENPAGE = new Icon("fa-pencil-square-o");
	public static final Icon DEBUG = new Icon("fa-bug");
	public static final Icon CONSISTENCY = new Icon("fa-crosshairs");
	public static final Icon CHECK = new Icon("fa-check-square-o");
	public static final Icon CHECKED = new Icon("fa-check");
	public static final Icon DEFER = new Icon("fa-share");
	public static final Icon ESTABLISHED = new Icon("fa-circle");
	public static final Icon SUGGESTED = new Icon("fa-circle-o");
	public static final Icon EXCLUDED = new Icon("fa-ban");
	public static final Icon ABSTRACT = new Icon("fa-asterisk");

	public static final Icon LOW_PRIO = new Icon("fa-exclamation-circle knowwe-lowprio");
	public static final Icon HIGH_PRIO = new Icon("fa-exclamation-circle knowwe-highprio");

	// TestCase
	public static final Icon EXPAND_OUTLINE = new Icon("fa-plus-square-o");
	public static final Icon COLLAPSE_OUTLINE = new Icon("fa-minus-square-o");
	public static final Icon EXPAND = new Icon("fa-plus-square");
	public static final Icon COLLAPSE = new Icon("fa-minus-square");
	public static final Icon OPENTESTCASE = new Icon(LINK.getCssClass());
	public static final Icon EDITTABLE = new Icon("fa-table");

	// ON and OFF
	public static final Icon TOGGLE_ON = new Icon("fa-toggle-on");
	public static final Icon TOGGLE_OFF = new Icon("fa-toggle-off");

	// FILE TYPES
	public static final Icon NEW_FILE = new Icon("fa-file-o");
	public static final Icon FILE = new Icon("fa-file");
	public static final Icon FILE_TEXT = new Icon("fa-file-text-o");
	public static final Icon FILE_EXCEL = new Icon("fa-file-excel-o");
	public static final Icon FILE_WORD = new Icon("fa-file-word-o");
	public static final Icon FILE_PDF = new Icon("fa-file-pdf-o");
	public static final Icon FILE_ZIP = new Icon("fa-file-zip-o");
	public static final Icon FILE_CODE = new Icon("fa-file-code-o");
	public static final Icon FILE_XML = new Icon(FILE_CODE.getCssClass());

	//SPINNING
	public static final Icon LOADING = new Icon("fa-spinner");
	public static final Icon WAITING = new Icon(LOADING.getCssClass());
	public static final Icon CALCULATING = new Icon(LOADING.getCssClass());

	public static enum Percent {
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
		return "<i class='fa " + cssClass + "' "
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

	public Icon addColor(IconColor color) {
		return new Icon(cssClass + " " + color, style, title, id);
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
			default:
				return this;
		}
	}

}
