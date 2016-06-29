/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.kbrenderer.verbalizer;

import java.util.Arrays;
import java.util.Map;

import de.knowwe.kbrenderer.verbalizer.VerbalizationManager.RenderingFormat;

/**
 * The default verbalizer is the most simple verbalizer. It renders all objects
 * to its toString method.
 * 
 * It is used partly as "fail-safe" for the VerbalizationManager, as it makes
 * sure, that every object can be rendered by the VerbalizationManager
 * 
 * @author lemmerich
 * @date june 2008
 */
public class DefaultVerbalizer implements Verbalizer {

	/**
	 * As this is the Default Verbalizer, all objects can be rendered
	 */
	@Override
	public Class<?>[] getSupportedClassesForVerbalization() {
		return new Class[] { Object.class };
	}

	@Override
	/**
	 * As this is the DefaultVerbalizer all possible targets should be rendered
	 */
	public RenderingFormat[] getSupportedRenderingTargets() {
		return RenderingFormat.values();
	}

	@Override
	/**
	 * returns a standard verbalization of any object. o.toString() is used for
	 * HTML and PLAIN_TEXT rendering, for XML rendering this is embedded in a
	 * tag of the class of given object o.
	 * 
	 * parameters are not needed and ignored.
	 */
	public String verbalize(Object o, RenderingFormat targetFormat, Map<String, Object> parameter) {
		if (targetFormat == RenderingFormat.HTML) return renderObjectToHTML(o);
		if (targetFormat == RenderingFormat.PLAIN_TEXT) return renderObjectToPlainText(o);
		if (targetFormat == RenderingFormat.XML) return renderObjectToXML(o);

		// as this is the defaultVerbalizer (that should render everything) this
		// shall never happen!
		return null;
	}

	public static String verbalizeUnexpectedObject(Object o) {
		if (o == null) {
			return "null";
		}
		else if (o instanceof long[]) {
			return "(long[]) " + Arrays.toString((long[]) o);
		}
		else if (o instanceof int[]) {
			return "(int[]) " + Arrays.toString((int[]) o);
		}
		else if (o instanceof short[]) {
			return "(short[]) " + Arrays.toString((short[]) o);
		}
		else if (o instanceof char[]) {
			return "(char[]) " + Arrays.toString((char[]) o);
		}
		else if (o instanceof byte[]) {
			return "(byte[]) " + Arrays.toString((byte[]) o);
		}
		else if (o instanceof boolean[]) {
			return "(boolean[]) " + Arrays.toString((boolean[]) o);
		}
		else if (o instanceof float[]) {
			return "(float[]) " + Arrays.toString((float[]) o);
		}
		else if (o instanceof double[]) {
			return "(double[]) " + Arrays.toString((double[]) o);
		}
		else if (o instanceof Object[]) {
			return "(Object[]) " + verbalizerObjectArray((Object[]) o);
		}
		else {
			return "(" + o.getClass().getName() + ") " + o.toString();
		}
	}

	public static String verbalizerObjectArray(Object[] a) {

		int iMax = a.length - 1;
		if (iMax == -1) return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append("(").append(a[i].getClass().getSimpleName()).append(") ").append(String.valueOf(a[i]));
			if (i == iMax) return b.append(']').toString();
			b.append(", ");
		}
	}

	/**
	 * returns o.toString()
	 * 
	 * @param o object to be rendered as plain text
	 * @return o.toString()
	 */
	protected String renderObjectToPlainText(Object o) {
		return o.toString();
	}

	/**
	 * returns o.toString()
	 * 
	 * @param o object to be rendered as xml
	 * @return o.toString() embedded in a tag of the class of o.
	 */
	protected String renderObjectToXML(Object o) {
		String s = "<" + o.getClass().getName() + ">";
		s += o.toString();
		s += "</" + o.getClass().getName() + ">";
		return s;
	}

	/**
	 * return o.toString()
	 * 
	 * @param o object to be rendered as html
	 * @return o.toString()
	 */
	protected String renderObjectToHTML(Object o) {
		return o.toString();
	}

}
