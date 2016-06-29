/*
 * Copyright (C) 2013 denkbares GmbH
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

package de.knowwe.visualization.dot;

import java.util.ArrayList;
import java.util.List;

import de.d3web.strings.Strings;

/**
 * A data class for storing the style info of a node/property etc.
 *
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 02.05.2013
 */
public class RenderingStyle {

	private String shape = "box";
	private String fontsize = "10";
	private List<String> style = new ArrayList<>();
	private String fillcolor = "";
	private String fontcolor = "black";
	private Fontstyle fontstyle = Fontstyle.NORMAL;

	public enum Fontstyle {
		NORMAL, BOLD, ITALIC, UNDERLINING
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getFontcolor() {
		return fontcolor;
	}

	public void addStyle(String newStyle) {
		if (!style.contains(newStyle)) {
			style.add(newStyle);
		}
	}

	public void setFontcolor(String fontcolor) {
		this.fontcolor = fontcolor;
	}

	public String getFontsize() {
		return fontsize;
	}

	public void setFontsize(String fontsize) {
		this.fontsize = fontsize;
	}

	public String getStyle() {
		if (style.isEmpty()) return "";
		return Strings.concat(",", style);
	}

	public void setStyle(String newStyle) {
		this.style.clear();
		style.add(newStyle);
	}

	public String getFillcolor() {
		return fillcolor;
	}

	public void setFillcolor(String fillcolor) {
		this.fillcolor = fillcolor;
		addStyle("filled");
	}

	public String getShape() {
		return shape;
	}

	public Fontstyle getFontstyle() { return fontstyle; }

	public void setFontstyle(Fontstyle f) {
		this.fontstyle = f;
	}
}
