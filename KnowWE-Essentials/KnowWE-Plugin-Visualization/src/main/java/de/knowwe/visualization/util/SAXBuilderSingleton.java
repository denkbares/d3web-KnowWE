/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.knowwe.visualization.util;

import org.jdom.input.SAXBuilder;

/**
 * 
 * @author Johanna Latt
 * @created 23.12.2013
 */
public class SAXBuilderSingleton extends SAXBuilder {

	private static SAXBuilderSingleton instance;

	private SAXBuilderSingleton() {
	}

	public static SAXBuilderSingleton getInstance() {
		if (instance == null) {
			instance = new SAXBuilderSingleton();
			instance.setReuseParser(true);
			instance.setValidation(false);
			instance.setFeature("http://xml.org/sax/features/validation", false);
			instance.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
					false);
			instance.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
		}
		return instance;
	}
}