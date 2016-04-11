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

package de.knowwe.rdfs.vis.dotNative;

/**
 * @author Jochen Reutelshöfer
 * @created 07.04.2014
 */
public class GraphvizJNI {

	static {
		System.out.println("Loading library...");
		//System.load("/usr/lib64/graphviz/java/libgv_java.so");

		//libgvplugin_quartz.6.dylib
		System.load("/usr/local/lib/graphviz/libgvplugin_quartz.6.dylib");

		System.out.println("Loaded library.");
	}

	public static void main(String[] args) {
		String data = "";
		byte[] result = new GraphvizJNI().render(data);
		System.out.println("Output: " + new String(result));
	}

	private native byte[] render(String data);

}
